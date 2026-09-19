# Trabalho TP3 - Autenticacao em Microsservicos

Implementei uma arquitetura com dois microsservicos independentes:

- `auth-service`: autentica o usuario e gera tokens JWT.
- `pedido-service`: disponibiliza a rota protegida de criacao de pedidos.

O `auth-service` roda na porta `8081` e o `pedido-service` na porta `8082`. O pedido-service valida localmente a assinatura, a validade e o tipo do JWT. Ele nao depende de uma chamada ao auth-service para autorizar cada requisicao.

## Tecnologias

- Java 17
- Spring Boot 3.4.5
- Maven
- JWT com JJWT

## Como executar

Preciso ter Java 17 ou superior e Maven instalados.

Na raiz do projeto, compile os dois servicos:

```bash
mvn clean package
```

Em dois terminais, execute:

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

## Endpoint protegido

### POST http://localhost:8082/api/pedidos

A rota exige o access token no cabecalho `Authorization`:

```bash
curl -X POST http://localhost:8082/api/pedidos -H "Authorization: Bearer COLE_O_ACCESS_TOKEN" -H "Content-Type: application/json" -d "{\"descricao\":\"Pedido de teste\"}"
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

