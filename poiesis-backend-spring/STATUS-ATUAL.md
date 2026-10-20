# Estado atual do Poiesis

Este documento registra o estado de implementação e validação conhecido em **20/10/2026**. O status das portas é uma fotografia da última verificação local, não uma garantia de que os processos continuem ativos.

## O que o projeto faz

O sistema tem um Gateway e quatro microsserviços Spring Boot, agrupados por responsabilidade de negócio:

| Aplicação | Responsabilidade | Porta |
|---|---|---:|
| Gateway | Entrada HTTP, autenticação da sessão, autorização de rotas, CORS e encaminhamento | 8080 |
| Login | Cadastro, autenticação, cookies de sessão, emissão e revogação de JWT | 8081 |
| Produto | Catálogo, opções de customização e ordens/etapas de produção | 8082 |
| Pedido | Criação e consulta de pedidos; publicação de eventos | 8083 |
| Relatório | Projeções de vendas e produção | 8084 |

Catálogo, Customização e Produção estão consolidados em `produto/`: são pacotes de domínio dentro de uma aplicação, não três módulos Maven ou processos separados. As rotas de produto, customização e produção são `/v1/produtos`, `/v1/customizacoes` e `/v1/producao`.

O Login é responsável pelas credenciais e pela sessão. O navegador usa cookie HttpOnly e proteção CSRF; clientes nativos e ferramentas continuam compatíveis com Bearer. O Gateway valida o acesso de entrada e os serviços internos também aplicam validação e permissões.

Pedido publica `pedido.criado` no RabbitMQ. Produto consome o evento para criar ordens de produção, e Relatório consome a sua própria cópia para atualizar projeções. Esse processamento é assíncrono. Produto, Pedido e Relatório usam bancos H2 em memória; o Login mantém dados de autenticação em H2 em arquivo.

**O backend não implementa estoque.** A quantidade de um item representa o que foi pedido, não um saldo, reserva ou baixa de inventário.

## O que foi verificado

- Java **21.0.12** está instalado.
- Maven está instalado. No ambiente de desenvolvimento ele foi localizado junto ao IntelliJ; posteriormente, `mvn` também ficou disponível no terminal.
- `mvn -f produto/pom.xml clean verify` terminou com **BUILD SUCCESS**: 8 testes executados, 0 falhas e 0 erros. A correção necessária para Spring Boot 4 e a configuração de teste estão incluídas no código atual.
- O frontend usa Expo. Os binários locais de Node.js e npm foram encontrados como Node **24.22.0** e npm **11.19.1**, mas o terminal inicialmente não os reconhecia pelo comando simples; `C:\Program Files\nodejs` deve estar no `PATH`.
- Em uma verificação local, o Expo estava ativo na porta `8090` e Login/Gateway respondiam nas portas `8081`/`8080`, mas Produto, Pedido e Relatório não estavam ativos. A tela do catálogo retornou erro porque Produto não respondia na porta `8082`.

O sucesso dos testes Maven de Produto valida compilação e testes automatizados desse aplicativo; **não significa que todos os serviços foram testados nem que o fluxo completo da interface já foi validado**.

## Como executar no Windows/VS Code

Abra os terminais integrados com **Terminal → New Terminal**. Nos comandos abaixo, ajuste o caminho se o repositório estiver em outra pasta.

### Compilar e testar todos os aplicativos

```powershell
$backend = "C:\Users\dimmo\Downloads\poiesis\poiesis-backend-spring"
foreach ($service in @("login", "pedido", "relatorio", "produto", "gateway")) {
    Write-Host "Testando $service..."
    mvn -f "$backend\$service\pom.xml" clean verify
    if ($LASTEXITCODE -ne 0) { throw "Os testes de $service falharam." }
}
```

Produto possui seu próprio projeto Maven em `produto/pom.xml`; Login, Pedido e Relatório constroem seus módulos a partir de seus POMs na raiz de cada serviço. Se o terminal ainda não reconhecer Node/npm, mas os executáveis estiverem em `C:\Program Files\nodejs`, adicione-os à sessão atual do PowerShell:

```powershell
$env:Path = "C:\Program Files\nodejs;$env:Path"
node -v
npm -v
```

### Iniciar o backend completo

Para pedidos, ordens de produção e relatórios assíncronos, é necessário um RabbitMQ acessível. Defina `SPRING_RABBITMQ_ADDRESSES` com o endereço AMQP fornecido para o broker, sem gravar credenciais no código ou neste documento. A mesma configuração deve ser usada nos processos Produto, Pedido e Relatório.

Compile os cinco aplicativos antes de iniciar. Depois abra um terminal PowerShell por aplicação. Nos terminais, use:

```powershell
Set-Location C:\Users\dimmo\Downloads\poiesis\poiesis-backend-spring
```

| Terminal | Comando |
|---|---|
| Login | `java -jar .\login\springframework\target\login-spring-0.0.1-SNAPSHOT.jar` |
| Produto, com RabbitMQ | `$env:SPRING_RABBITMQ_ADDRESSES = "<endereço AMQP>"; java -jar .\produto\target\produto-0.0.1-SNAPSHOT.jar` |
| Pedido, com RabbitMQ | `$env:SPRING_RABBITMQ_ADDRESSES = "<endereço AMQP>"; java -jar .\pedido\springframework\target\pedido-spring-0.0.1-SNAPSHOT.jar` |
| Relatório, com RabbitMQ | `$env:SPRING_RABBITMQ_ADDRESSES = "<endereço AMQP>"; java -jar .\relatorio\springframework\target\relatorio-spring-0.0.1-SNAPSHOT.jar` |
| Gateway | `java -jar .\gateway\target\gateway-0.0.1-SNAPSHOT.jar` |

Substitua o texto entre `<...>` pelo endereço configurado pelo provedor; não use os sinais `<` e `>` literalmente. Deixe cada terminal aberto e aguarde o log confirmar a inicialização na porta correspondente. Se não houver broker disponível, cadastro/login e leitura do catálogo podem ser testados sem o fluxo de mensageria; pedidos e processamento de produção/relatórios não terão o fluxo assíncrono completo.

Para testar apenas o catálogo, Produto pode ser iniciado sem conexão ao broker e com os listeners desativados:

```powershell
Set-Location C:\Users\dimmo\Downloads\poiesis\poiesis-backend-spring
$env:SPRING_RABBITMQ_ADDRESSES = ""
java -jar .\produto\target\produto-0.0.1-SNAPSHOT.jar --spring.rabbitmq.listener.simple.auto-startup=false --spring.rabbitmq.listener.direct.auto-startup=false
```

### Iniciar o frontend web

Abra outro terminal:

```powershell
$env:Path = "C:\Program Files\nodejs;$env:Path"
Set-Location C:\Users\dimmo\Downloads\poiesis\poiesis-frontend
npm ci
$env:EXPO_PUBLIC_API_URL = "http://localhost:8080"
npm run web
```

Abra `http://localhost:8090`. O frontend precisa do Gateway em `8080`; login/cadastro também precisam do Login em `8081`, e o catálogo precisa do Produto em `8082`.

## O que falta validar

1. Executar `clean verify` também para Login, Pedido, Relatório e Gateway; até aqui, o resultado de build/teste confirmado é o de Produto.
2. Configurar ou iniciar um broker RabbitMQ acessível e verificar a conexão nos logs de Produto, Pedido e Relatório.
3. Com os cinco processos ativos, validar no navegador cadastro, login, listagem do catálogo, criação/consulta de pedidos, customizações e a atualização assíncrona das ordens e relatórios.
4. Confirmar cookies de sessão e CSRF no navegador; verificar também autorização de operações de cliente e administrador.
5. Executar `npm run lint` e `npx tsc --noEmit` no frontend.
6. Executar k6 somente com o backend completo disponível e registrar o resumo real (checks, falhas e latência); o repositório não contém resultados de execução atuais.

Ao parar os serviços iniciados manualmente, pressione **Ctrl+C** no terminal de cada processo. O script `iniciar-projeto.sh` é Bash e não é executado diretamente pelo PowerShell do Windows.
