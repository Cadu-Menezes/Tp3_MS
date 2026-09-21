# Trabalho TP3/Assessment - Autenticacao em Microsservicos

## Carlos Eduardo Menezes - 147.072.387-50 

Implementei uma arquitetura com um servidor Eureka e dois microsservicos independentes:

- `eureka-server`: servidor de descoberta dos microsservicos.
- `auth-service`: autentica o usuario e gera tokens JWT.
- `pedido-service`: disponibiliza o CRUD protegido de pedidos usando H2.

O Eureka roda na porta `8761`, o `auth-service` na porta `8081` e o `pedido-service` na porta `8082`. Os dois microsservicos se registram no Eureka. O pedido-service valida localmente a assinatura, a validade e o tipo do JWT.

## Tecnologias

- Java 17
- Spring Boot 3.4.5
- Spring Cloud Netflix Eureka
- Maven
- JWT com JJWT
- H2 e Spring Data JPA

## Como executar

Preciso ter Java 17 ou superior e Maven instalados.

Na raiz do projeto, compile os servicos:

```bash
mvn clean package
```

Em tres terminais, execute nesta ordem:

```bash
mvn -pl eureka-server spring-boot:run
```

```bash
mvn -pl auth-service spring-boot:run
```

```bash
mvn -pl pedido-service spring-boot:run
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

Sem o cabecalho ou com um token invalido, a resposta sera `401 Unauthorized`. Com um token valido, a resposta sera semelhante a:

```json
{
  "id": "id-gerado",
  "descricao": "Pedido de teste",
  "usuario": "usuario",
  "status": "CRIADO"
}
```

