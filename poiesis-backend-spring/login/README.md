# Login Service

O login autentica as credenciais persistidas em H2 e emite JWT próprio assinado com HMAC. Gateway e Resource Servers validam o token com a mesma chave secreta configurada nos `application.properties`.

As roles são armazenadas na tabela `tb_usuario_roles` como `USER` ou `ADMIN`. O cadastro público sempre cria `USER`. O token contém `roles`, com valores simples, e expira após 15 minutos. O administrador acadêmico inicial é `admin@poiesis.com` / `admin123`.

Use `Authorization: Bearer <token>` nas rotas protegidas. Execute a aplicação `SpringframeworkApplication` pelo IntelliJ. A porta padrão do login é `8081`.

O segredo HMAC atual é apenas para execução acadêmica local; substitua-o se o serviço for usado em outro ambiente.

`GET /v1/auth/session` valida a sessão e retorna e-mail, perfis e expiração. `POST /v1/auth/logout` revoga o token no banco; Gateway e serviços também verificam a sessão. Usuários e revogações são persistidos em H2 em arquivo (`POIESIS_AUTH_DB`), inclusive após reinício. O login emite cookie HttpOnly e cookie CSRF; em produção configure `COOKIE_SECURE=true` e HTTPS. O Gateway valida JWT/perfis e encaminha para Produto, Pedido e Relatório. Consulte a [cola de apresentação](../COLA-APRESENTACAO.md).
