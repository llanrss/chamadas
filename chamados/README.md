# Sistema de Chamados - API REST (Spring Boot)

Java 21 · Spring Boot 4.1.1 · Maven · Spring Web MVC · Spring Data JPA · H2 · Bean Validation

## Como rodar
Na pasta que contém `pom.xml` e `mvnw`:

    macOS / Linux:      chmod +x mvnw && ./mvnw spring-boot:run
    Windows PowerShell: .\mvnw spring-boot:run

(Na primeira execução o wrapper baixa o Maven e as dependências: precisa de internet.)
Aplicação em http://localhost:8080 (404 em `/` é esperado).
Console do H2: http://localhost:8080/h2-console  ·  JDBC URL `jdbc:h2:mem:chamadosdb`  ·  usuário `sa`  ·  senha vazia.

## Testes
    ./mvnw test        (Windows: .\mvnw test)

## Rotas
| Recurso | Rotas |
|---|---|
| Clientes | POST/GET `/clientes`, GET/PUT/DELETE `/clientes/{id}`, GET `/clientes/{id}/chamados` |
| Categorias | POST/GET `/categorias`, GET/PUT/DELETE `/categorias/{id}` |
| Técnicos | POST/GET `/tecnicos`, GET/PUT/DELETE `/tecnicos/{id}` |
| Chamados | POST `/chamados`, GET `/chamados?status=&prioridade=&clienteId=&page=&size=&sort=`, GET/PUT/DELETE `/chamados/{id}` |
| Ações | PATCH `/chamados/{id}/atribuir`, PATCH `/chamados/{id}/status` |
| Comentários | POST/GET `/chamados/{id}/comentarios` |

## Roteiro de teste rápido (curl)
    curl -i -X POST localhost:8080/clientes   -H "Content-Type: application/json" -d '{"nome":"Ana Souza","email":"ana@exemplo.com","telefone":"47999999999"}'
    curl -i -X POST localhost:8080/categorias -H "Content-Type: application/json" -d '{"nome":"Rede","descricao":"Problemas de rede"}'
    curl -i -X POST localhost:8080/tecnicos   -H "Content-Type: application/json" -d '{"nome":"Carlos","email":"carlos@empresa.com","perfil":"TECNICO"}'
    curl -i -X POST localhost:8080/chamados   -H "Content-Type: application/json" -d '{"titulo":"Sem internet","descricao":"Rede caiu","prioridade":"ALTA","clienteId":1,"categoriaId":1}'
    curl -i -X PATCH localhost:8080/chamados/1/status   -H "Content-Type: application/json" -d '{"status":"EM_ATENDIMENTO"}'   # 409 (sem técnico)
    curl -i -X PATCH localhost:8080/chamados/1/atribuir -H "Content-Type: application/json" -d '{"tecnicoId":1}'                # 200
    curl -i -X PATCH localhost:8080/chamados/1/status   -H "Content-Type: application/json" -d '{"status":"EM_ATENDIMENTO"}'   # 200
    curl -i -X POST localhost:8080/chamados/1/comentarios -H "Content-Type: application/json" -d '{"texto":"Reiniciei o roteador","autor":"Carlos"}'
    curl -i -X PATCH localhost:8080/chamados/1/status   -H "Content-Type: application/json" -d '{"status":"FECHADO"}'           # 200, dataFechamento preenchida
    curl -i "localhost:8080/chamados?status=FECHADO&page=0&size=5"

## Regras
- Chamado nasce `ABERTO`; `dataAbertura` é automática.
- Transições: ABERTO → EM_ATENDIMENTO | FECHADO · EM_ATENDIMENTO → AGUARDANDO_CLIENTE | FECHADO · AGUARDANDO_CLIENTE → EM_ATENDIMENTO | FECHADO.
- EM_ATENDIMENTO exige técnico atribuído. Chamado FECHADO não pode ser atribuído nem alterado.
- Erros: 400 (validação/JSON/enum inválido), 404 (não encontrado), 409 (regra de negócio / registro em uso).
