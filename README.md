# Trabalho TP3/Assessment - Autenticacao em Microsservicos

## Carlos Eduardo Menezes - 147.072.387-50 

Implementei uma arquitetura com um Config Server, um servidor Eureka, um Gateway e tres microsservicos independentes:

- `config-server`: disponibiliza as configuracoes centralizadas da pasta `config-repo`.
- `eureka-server`: servidor de descoberta dos microsservicos.
- `auth-service`: autentica o usuario e gera tokens JWT.
- `pedido-service`: disponibiliza o CRUD protegido de pedidos usando H2.
- `fornecedores-service`: disponibiliza o CRUD protegido de fornecedores usando H2.
- `gateway-service`: encaminha requisicoes para os servicos descobertos pelo Eureka.

O Config Server roda na porta `8888`, o Eureka na porta `8761`, o Gateway na porta `8085`, o `auth-service` na porta `8081`, o `pedido-service` na porta `8082` e o `fornecedores-service` na porta `8084`. Os tres microsservicos e o Gateway carregam suas configuracoes pelo Config Server e se registram no Eureka.

## Tecnologias

- Java 17
- Spring Boot 3.4.5
- Spring Cloud Netflix Eureka
- Spring Cloud Config Server
- Spring Cloud Gateway
- Maven
- JWT com JJWT
- H2 e Spring Data JPA

## Como executar

Preciso ter Java 17 ou superior e Maven instalados.

Na raiz do projeto, compile os servicos:

```bash
mvn clean package
```

Em seis terminais, execute nesta ordem:

```bash
mvn -pl config-server spring-boot:run
```

```bash
mvn -pl eureka-server spring-boot:run
```

```bash
mvn -pl auth-service spring-boot:run
```

```bash
mvn -pl pedido-service spring-boot:run
```

```bash
mvn -pl fornecedores-service spring-boot:run
```

```bash
mvn -pl gateway-service spring-boot:run
```

O Config Server deve iniciar antes dos outros servicos, pois eles carregam as propriedades da pasta `config-repo` por meio de `http://localhost:8888`.

## Docker Compose

Eu criei um Dockerfile para cada servico e um perfil `docker` no `config-repo`. Nesse perfil, o Eureka e acessado pelo nome `eureka-server` dentro da rede Docker.

Para subir tudo de uma vez:

```bash
docker compose up --build
```

Para listar fornecedores pelo Gateway:

```bash
curl http://localhost:8085/fornecedores-service/api/fornecedores
```

Para consultar pedidos pelo endpoint Feign do fornecedores-service:

```bash
curl http://localhost:8085/fornecedores-service/fornecedores/pedidos
```

As rotas protegidas continuam exigindo o cabecalho `Authorization` com um access token JWT.

## Gateway

O Gateway descobre automaticamente os servicos registrados no Eureka. Eu nao cadastrei rotas manualmente.

Para listar fornecedores pelo Gateway:

```bash
curl http://localhost:8085/fornecedores-service/api/fornecedores -H "Authorization: Bearer COLE_O_ACCESS_TOKEN"
```

O Gateway remove automaticamente o prefixo `fornecedores-service` antes de encaminhar a requisicao para o endpoint `/api/fornecedores` do servico.

## Consulta de pedidos via Feign

O `fornecedores-service` consulta o `pedido-service` usando OpenFeign. O cliente Feign descobre o `pedido-service` pelo Eureka e encaminha o token JWT recebido na requisicao.

Para consultar os pedidos diretamente pelo `fornecedores-service`:

```bash
curl http://localhost:8084/fornecedores/pedidos -H "Authorization: Bearer COLE_O_ACCESS_TOKEN"
```

Para consultar pelo Gateway:

```bash
curl http://localhost:8085/fornecedores-service/fornecedores/pedidos -H "Authorization: Bearer COLE_O_ACCESS_TOKEN"
```

A resposta sera uma lista com os pedidos cadastrados no `pedido-service`.

## Config Server

As configuracoes centralizadas ficam na pasta `config-repo`:

- `application.properties`: configuracoes compartilhadas.
- `eureka-server.properties`: configuracoes do Eureka.
- `auth-service.properties`: configuracoes do auth-service.
- `pedido-service.properties`: configuracoes do pedido-service.
- `fornecedores-service.properties`: configuracoes do fornecedores-service.
- `gateway-service.properties`: configuracoes do gateway-service.
- Arquivos `*-docker.properties`: configuracoes usadas dentro da rede Docker.

Para consultar as configuracoes carregadas pelo Config Server:

```text
http://localhost:8888/auth-service/default
http://localhost:8888/pedido-service/default
http://localhost:8888/fornecedores-service/default
http://localhost:8888/eureka-server/default
```

## Endpoints publicos

### POST http://localhost:8081/auth/login

Credenciais de teste:

```json
{
  "username": "usuario",
  "password": "123456"
}
```

Exemplo com cURL:

```bash
curl -X POST http://localhost:8081/auth/login -H "Content-Type: application/json" -d "{\"username\":\"usuario\",\"password\":\"123456\"}"
```

A resposta contem `accessToken` e `refreshToken`.

### POST http://localhost:8081/auth/refresh

Envie o refresh token recebido no login:

```bash
curl -X POST http://localhost:8081/auth/refresh -H "Content-Type: application/json" -d "{\"refreshToken\":\"COLE_O_REFRESH_TOKEN\"}"
```

## Banco H2

O `pedido-service` usa H2 em memoria. O banco e recriado quando o servico e reiniciado.

Console: http://localhost:8082/h2-console

- JDBC URL: `jdbc:h2:mem:pedidodb`
- Usuario: `sa`
- Senha: deixe vazia

## Endpoints protegidos

### POST http://localhost:8082/api/pedidos

A rota exige o access token no cabecalho `Authorization`:

```bash
curl -X POST http://localhost:8082/api/pedidos -H "Authorization: Bearer COLE_O_ACCESS_TOKEN" -H "Content-Type: application/json" -d "{\"descricao\":\"Pedido de teste\"}"
```

### GET http://localhost:8082/api/pedidos

Lista todos os pedidos:

```bash
curl http://localhost:8082/api/pedidos -H "Authorization: Bearer COLE_O_ACCESS_TOKEN"
```

### GET http://localhost:8082/api/pedidos/{id}

Busca um pedido pelo id:

```bash
curl http://localhost:8082/api/pedidos/1 -H "Authorization: Bearer COLE_O_ACCESS_TOKEN"
```

### PUT http://localhost:8082/api/pedidos/{id}

Atualiza a descricao de um pedido:

```bash
curl -X PUT http://localhost:8082/api/pedidos/1 -H "Authorization: Bearer COLE_O_ACCESS_TOKEN" -H "Content-Type: application/json" -d "{\"descricao\":\"Pedido atualizado\"}"
```

### DELETE http://localhost:8082/api/pedidos/{id}

Remove um pedido:

```bash
curl -X DELETE http://localhost:8082/api/pedidos/1 -H "Authorization: Bearer COLE_O_ACCESS_TOKEN"
```

## Fornecedores

O `fornecedores-service` cadastra cinco fornecedores automaticamente quando inicia com o banco vazio. O nome e o CNPJ sao obrigatorios, e o CNPJ e unico.

Console H2: http://localhost:8084/h2-console

- JDBC URL: `jdbc:h2:mem:fornecedoresdb`
- Usuario: `sa`
- Senha: deixe vazia

### POST http://localhost:8084/api/fornecedores

```bash
curl -X POST http://localhost:8084/api/fornecedores -H "Authorization: Bearer COLE_O_ACCESS_TOKEN" -H "Content-Type: application/json" -d "{\"nome\":\"Novo Fornecedor\",\"cnpj\":\"66.666.666/0001-66\"}"
```

### GET http://localhost:8084/api/fornecedores

```bash
curl http://localhost:8084/api/fornecedores -H "Authorization: Bearer COLE_O_ACCESS_TOKEN"
```

### GET http://localhost:8084/api/fornecedores/{id}

```bash
curl http://localhost:8084/api/fornecedores/1 -H "Authorization: Bearer COLE_O_ACCESS_TOKEN"
```

### PUT http://localhost:8084/api/fornecedores/{id}

```bash
curl -X PUT http://localhost:8084/api/fornecedores/1 -H "Authorization: Bearer COLE_O_ACCESS_TOKEN" -H "Content-Type: application/json" -d "{\"nome\":\"Fornecedor Atualizado\",\"cnpj\":\"66.666.666/0001-66\"}"
```

### DELETE http://localhost:8084/api/fornecedores/{id}

```bash
curl -X DELETE http://localhost:8084/api/fornecedores/1 -H "Authorization: Bearer COLE_O_ACCESS_TOKEN"
```

Sem o cabecalho ou com um token invalido, a resposta sera `401 Unauthorized`. Com um token valido, a resposta sera semelhante a:

```json
{
  "id": "id-gerado",
  "descricao": "Pedido de teste",
  "usuario": "usuario",
  "status": "CRIADO"
}
```

