# Serviço Produto

Produto reúne Catálogo, Customização e Produção em um único aplicativo Spring Boot, build Maven e processo de execução. Os códigos-fonte e testes desses domínios estão neste módulo; não existem mais módulos Maven separados para eles.

O serviço mantém os pacotes por domínio e por camada:

- `com.poiesis.catalogo`: cadastro e consulta de produtos.
- `com.poiesis.customizacao`: opções de customização dos produtos.
- `com.poiesis.producao`: ordens e etapas de produção.
- `com.poiesis.produto`: inicialização do aplicativo e segurança compartilhada.

Cada domínio mantém suas responsabilidades e regras, mas compartilha a infraestrutura do processo Produto, incluindo banco H2 e configuração de segurança. Operações e permissões continuam diferenciadas nos controllers. Produto atende na porta `8082`; o Gateway encaminha para ele.

Pedido publica o evento `pedido.criado` no exchange `pedido.eventos`. Produto o consome para criar ordens de produção; o processamento é assíncrono e usa `SPRING_RABBITMQ_ADDRESSES`.

Para compilar e testar a partir desta pasta, execute `mvn clean verify`. O teste de contexto desativa os listeners RabbitMQ; para executar o serviço e consumir ordens de produção, defina `SPRING_RABBITMQ_ADDRESSES`.
