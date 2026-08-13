# Microsserviço de Eventos

Microsserviço responsável pelo gerenciamento dos eventos da aplicação
Eventos Express.

## Tecnologias

- Java 21
- Spring Boot
- Spring Data JPA
- Maven
- H2 Database

## Porta

O serviço é executado na porta 8080.

## Endpoints

- POST /eventos
- GET /eventos
- GET /eventos/{id}
- PUT /eventos/{id}
- DELETE /eventos/{id}

## Executando o projeto

mvn clean test
mvn spring-boot:run

## Banco H2

Console: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:eventosdb
Usuário: sa
Senha: vazia