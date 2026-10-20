# Cola para apresentação do Poiesis

## Visão geral: quatro microsserviços

O Gateway é a única entrada pública HTTP (`localhost:8080`). Atrás dele há quatro microsserviços organizados por responsabilidade de negócio, não por tipo de usuário.

| Serviço | Responsabilidade | Porta |
|---|---|---:|
| Login | Cadastro de contas, autenticação, emissão e revogação de JWT | 8081 |
| Produto | Catálogo, customizações e ordens/etapas de produção | 8082 |
| Pedido | Validação, criação e consulta de pedidos | 8083 |
| Relatório | Projeções de vendas e produção | 8084 |
| Gateway | Autenticação da entrada, autorização por rota, CORS e roteamento | 8080 |

Produto reúne as funções antes separadas em Catálogo, Customização e Produção. As rotas existentes permanecem: `/v1/produtos`, `/v1/customizacoes` e `/v1/producao`. O cliente e o administrador usam as mesmas fronteiras de domínio, com operações liberadas conforme suas roles.

## Fluxo de login e segurança

1. O cliente envia e-mail e senha a `POST /v1/auth/login` no Gateway. O Login verifica a senha BCrypt e emite JWT com `USER` ou `ADMIN`.
2. A resposta define o cookie `POIESIS_SESSION` como `HttpOnly`, `SameSite=Strict` e com expiração de 15 minutos. Um cookie separado `POIESIS_CSRF`, acessível ao navegador, permite a proteção contra CSRF. O corpo continua retornando token para compatibilidade com clientes nativos, Postman e k6.
3. O frontend web envia os cookies e o cabeçalho `X-POIESIS-CSRF`; o app nativo envia `Authorization: Bearer ...`. O Gateway valida assinatura, expiração, revogação e acesso à rota. Em requisições por cookie, exige CSRF nas operações que alteram estado e converte a sessão em Bearer ao encaminhar.
4. Os serviços continuam verificando JWT e regras de domínio, mesmo se alguém tentar acessá-los sem passar pelo Gateway. Isso é defesa em profundidade; em produção, as portas internas também devem ficar isoladas da internet.
5. Configure `COOKIE_SECURE=true` e HTTPS em produção. Para a demonstração local HTTP, o cookie Secure fica desligado por padrão.

| Operação | Acesso |
|---|---|
| Cadastro/login | Público |
| Leitura de produtos | Pública |
| Criar/editar/inativar produto | `ADMIN` |
| Consultar/criar customização | `USER` ou `ADMIN` |
| Editar/inativar customização | `ADMIN` |
| Criar/consultar pedidos | `USER` ou `ADMIN` (cliente só vê os próprios) |
| Consultar/alterar produção e consultar relatórios | `ADMIN` |

## Organização de código, SOLID e nomes

As classes estão separadas por camada e responsabilidade:

- `domain/entity`: modelos e regras de negócio, sem dependência de Spring/JPA.
- `domain/usecase`, `domain/service`: operações/regras de negócio.
- `domain/repository` ou `domain/port`: contratos que o domínio precisa.
- `springframework/controller`: endpoints e DTOs HTTP.
- `springframework/controller/mapper`: conversão entre DTO e domínio. Mappers não são adaptadores; por isso foram separados/renomeados para essa pasta.
- `springframework/repository/entity`: representação JPA das tabelas.
- `springframework/repository/mapper`: conversão entre entidade JPA e modelo de domínio.
- `springframework/repository/*PersistenceAdapter`: adaptadores que implementam os contratos de repositório do domínio usando Spring Data.

`domain/entity` e `repository/entity` não são uma duplicação acidental: o primeiro representa o modelo de negócio; o segundo é o formato de persistência. Os adaptadores convertem entre eles. Exemplo: `ProdutoPersistenceAdapter` implementa `ProdutoRepository`; `CatalogoMapper` só mapeia dados HTTP e não deve ser chamado de Adapter.
Os mappers de pedido, customização, produção, catálogo e usuário também foram separados: controller mappers não dependem de entidades JPA; persistence mappers não dependem de DTOs HTTP.

Exemplos para explicar o princípio de responsabilidade única:

- `CriarPedidoUseCase` valida e coordena a criação do pedido.
- `CatalogoIntegration` adapta a API de Produto ao contrato usado pelo domínio Pedido.
- `ProdutoPersistenceAdapter` converte chamadas do domínio em operações de banco.
- `PedidoProducer` publica eventos; `PedidoConsumer` em Produto e Relatório os processam.
- `SecurityConfig` do Gateway centraliza autorização de rota; cada serviço mantém validação e autorização interna necessárias.

## Pedido, Produção e Relatório via RabbitMQ

Pedido publica um evento de criação (`pedido.eventos`, routing key `pedido.criado`). Produto e Relatório mantêm filas próprias e processam cada cópia de forma assíncrona. Pedido não espera pelos consumidores: a ordem e as métricas podem aparecer alguns segundos depois. A conexão é configurada pela variável `SPRING_RABBITMQ_ADDRESSES` para Produto; o processo de Pedido publica no broker configurado no serviço.

Cada microsserviço tem seu próprio banco; agora Produto tem um único banco para catálogo, customização e produção. O H2 de negócio é em memória. O RabbitMQ desacopla Pedido de Produto/Relatório, mas não cria uma transação distribuída: a publicação ocorre depois do commit do pedido.

## Estoque: de onde veio o número 53?

O backend atual não tem campo de estoque, tabela de inventário, saldo inicial, reserva ou baixa automática. `quantidade` em `ItemPedido` é somente o número de unidades solicitadas naquele pedido. Cadastro e inativação alteram o estado do produto no catálogo, e produção acompanha ordens/status; nenhuma dessas operações atualiza estoque.

Portanto, **53 não é um saldo calculado pelo modelo/banco atual do projeto**. Não há dado persistido no esquema atual que explique esse saldo; trate-o como valor de outra fonte ou de uma demonstração anterior até identificar a origem. O teste `tests/fluxo_sem_estoque.py` cria pedido de quantidade 2 e valida pedido/produção, não inventário.

## Como explicar o k6

O script `multi_request/teste-carga.js` atravessa o Gateway e mede o fluxo de pedido:

1. `setup`: uma autenticação (`POST /v1/auth/login`) e um produto temporário (`POST /v1/produtos`).
2. Cenário `shared-iterations`: `POST /v1/pedidos` para cada iteração.
3. `teardown`: `GET /v1/pedidos` confere persistência, IDs e integridade dos itens; `DELETE /v1/produtos/{id}` faz limpeza.

Configuração padrão: **10 VUs no máximo, 10 iterações totais e `maxDuration=2m`**. `ITERATIONS` é o total compartilhado entre VUs, não iterações por VU. Para ampliar, por exemplo, use `VUS=20 ITERATIONS=100 k6 run multi_request/teste-carga.js`.

O script registra `pedidos_criados`, `pedidos_falhos`, `pedidos_persistidos` e `checks`. `checks: rate==1` exige que verificações de login, produto, pedidos, persistência e limpeza passem. O resumo padrão também mostra taxa de falhas HTTP e `http_req_duration` (latência), mas o script não define um limite de latência nem um threshold próprio para falhas HTTP. Cada execução deve ser apresentada com seu resumo real; o repositório não armazena uma saída de execução k6, então não há números medidos permanentes para citar.

O k6 usa Bearer do corpo do login, não cookie, para continuar compatível com a ferramenta. O teste não avalia inventário nem consumo de RabbitMQ como medida de latência.

## Roteiro curto de demonstração

1. Definir `SPRING_RABBITMQ_ADDRESSES` e iniciar com `./iniciar-projeto.sh`; para parar, `./iniciar-projeto.sh parar`.
2. Fazer login pelo Gateway; demonstrar cookie no navegador ou Bearer no Postman.
3. Cadastrar produto, criar uma opção de customização e criar pedido como cliente.
4. Entrar como `ADMIN`, demonstrar edição de produto/opção, ordens em `/v1/producao` e relatórios.
5. Mostrar nos logs o evento e aguardar alguns segundos para os consumidores assíncronos.
6. Se demonstrar o k6, informar VUs, iterações, taxa de checks, erros e latência exibidos naquela execução — não confundir `quantidade` de pedido com saldo de estoque.
