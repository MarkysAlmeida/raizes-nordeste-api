# Raízes do Nordeste API

Trabalho Prático - Projeto Multidisciplinar  
Aluno: Marcos Antônio Santos de Almeida  
RU: 4767149  
Trilha: Back-end  
Ano: 2026

---

## 1. Sobre o projeto

O projeto **Raízes do Nordeste API** é uma aplicação back-end desenvolvida com **Java 21**, **Spring Boot** e **PostgreSQL**, criada para representar o funcionamento de uma rede de lanchonetes nordestinas com múltiplas unidades.

O sistema permite:

- cadastro e login de usuários;
- autenticação por JWT;
- controle de acesso por perfil;
- gerenciamento de unidades/filiais;
- cadastro e controle de produtos;
- estoque local por unidade;
- criação de pedidos;
- pagamento simulado;
- uso de pontos de fidelidade;
- cancelamento de pedidos;
- documentação e testes via Swagger/OpenAPI.

A aplicação também possui um front-end simples, feito em **HTML, CSS e JavaScript**, usado como apoio visual para demonstrar os fluxos de Cliente, Funcionário, Gerente e Administrador.

---

## 2. Tecnologias utilizadas

| Tecnologia | Uso no projeto |
|---|---|
| Java 21 | Linguagem principal da aplicação |
| Spring Boot | Framework principal do back-end |
| Spring Web | Criação dos endpoints REST |
| Spring Data JPA | Comunicação com o banco de dados |
| Hibernate | Mapeamento objeto-relacional |
| PostgreSQL | Banco de dados relacional |
| Spring Security | Segurança e controle de acesso |
| JWT | Autenticação por token |
| BCrypt | Criptografia de senhas |
| Swagger/OpenAPI | Documentação e teste dos endpoints |
| HTML, CSS e JavaScript | Front-end simples de apoio |
| IntelliJ IDEA | Ambiente de desenvolvimento |
| Postman | Testes manuais de API |

---

## 3. Pré-requisitos para rodar o projeto

Antes de executar o projeto, instale:

- Java 21;
- Maven;
- PostgreSQL;
- IntelliJ IDEA ou outra IDE Java;
- Postman, opcional;
- Navegador para acessar Swagger e front-end.

Verifique as versões:

```bash
java -version
mvn -version
```

---

## 4. Configuração do banco de dados

Crie o banco no PostgreSQL com o nome:

```sql
CREATE DATABASE raizes_nordeste;
```

No arquivo:

```text
src/main/resources/application.properties
```

use uma configuração semelhante a esta:

```properties
spring.application.name=raizes-nordeste-api

spring.datasource.url=jdbc:postgresql://localhost:5432/raizes_nordeste
spring.datasource.username=postgres
spring.datasource.password=54321

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
```

Atenção: altere `spring.datasource.username` e `spring.datasource.password` se o seu PostgreSQL estiver com outro usuário ou senha.

---

## 5. Como executar o projeto

### 5.1 Executando pelo IntelliJ IDEA

1. Abra o projeto no IntelliJ IDEA.
2. Aguarde o Maven baixar as dependências.
3. Confira se o banco `raizes_nordeste` existe no PostgreSQL.
4. Execute a classe principal da aplicação.
5. Aguarde aparecer no console que o Tomcat iniciou na porta 8080.

### 5.2 Executando pelo terminal

Na pasta raiz do projeto, execute:

```bash
mvn clean install
```

Depois:

```bash
mvn spring-boot:run
```

Se tudo estiver correto, a aplicação ficará disponível em:

```text
http://localhost:8080
```

---

## 6. Como acessar o sistema

### Front-end de apoio

```text
http://localhost:8080/index.html
```

ou:

```text
http://localhost:8080
```

### Swagger/OpenAPI

```text
http://localhost:8080/swagger-ui/index.html
```

---

## 7. Usuários de teste

Usuários sugeridos para teste:

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | admin@raizes.com | 123456 |
| Gerente Centro | gerente.centro@raizes.com | 654321 |
| Funcionário Centro | funcionario.centro@raizes.com | 123456 |
| Cliente Teste | cliente@raizes.com | 123456 |

Caso algum usuário não exista no banco, cadastre pelo Swagger usando as rotas indicadas neste README.

---

## 8. IDs usados nos testes

Os IDs podem variar conforme o banco de dados. No banco inicial do projeto, utilize estes valores como referência para os testes.

| Informação | Valor usado nos exemplos |
|---|---|
| Admin | `1` |
| Cliente | `3` |
| Unidade Centro | `1` |
| Produto válido 1 | `1` |
| Produto válido 2 | `2` |
| Produto inexistente | `999999` |
| Unidade para cadastrar gerente de teste | `3` |

Se os IDs forem diferentes no seu banco, consulte pelo Swagger:

```http
GET /usuarios
GET /unidades
GET /estoques/unidade/{unidadeId}
```

No front-end, o painel do gerente também pode exibir o ID dos produtos para facilitar os testes.

---

## 9. Como autenticar no Swagger

1. Acesse:

```text
http://localhost:8080/swagger-ui/index.html
```

2. Execute o login:

```http
POST /usuarios/login
```

3. Copie o token retornado.
4. Clique no botão **Authorize** no Swagger.
5. Cole o token JWT.
6. Confirme em **Authorize**.

Depois disso, os endpoints protegidos poderão ser testados conforme o perfil do usuário logado.

---

## 10. Como autenticar no Postman

1. Crie uma requisição de login.
2. Copie o token retornado.
3. Nas próximas requisições, vá em:

```text
Authorization > Type > Bearer Token
```

4. Cole o token no campo Token.

Também é possível enviar no header manualmente:

```http
Authorization: Bearer SEU_TOKEN_AQUI
```

---

## 11. O que testar pelo Swagger ou Postman

A sequência abaixo foi organizada para facilitar a validação da API pelo Swagger ou Postman. Os exemplos de JSON estão prontos para copiar e colar.

Antes de iniciar, acesse o Swagger em:

```text
http://localhost:8080/swagger-ui/index.html
```

Faça login com o usuário adequado, copie o token retornado e clique em **Authorize** para liberar o acesso às rotas protegidas.

---

### Login válido

Valida se um usuário cadastrado consegue autenticar no sistema.

Endpoint:

```http
POST /usuarios/login
```

Body:

```json
{
  "email": "cliente@raizes.com",
  "senha": "123456"
}
```

Resultado esperado:

```text
200 OK
Retorna token JWT.
```

---

### Login inválido

Valida se o sistema bloqueia autenticação com senha incorreta.

Endpoint:

```http
POST /usuarios/login
```

Body:

```json
{
  "email": "cliente@raizes.com",
  "senha": "senha_errada"
}
```

Resultado esperado:

```text
400, 401 ou 403
Retorna erro de autenticação.
```

---

### Acesso a rota sem permissão

Valida o controle de acesso por perfil. Faça login como cliente e tente acessar uma rota administrativa.

Endpoint:

```http
GET /usuarios
```

Resultado esperado:

```text
403 Forbidden
Cliente não possui permissão para listar todos os usuários.
```

---

### Cadastro público de cliente

Valida se um novo cliente pode ser cadastrado pela rota pública.

Endpoint:

```http
POST /usuarios/cliente
```

Body:

```json
{
  "nome": "Cliente API Teste",
  "email": "cliente.api.teste@raizes.com",
  "senha": "123456"
}
```

Resultado esperado:

```text
200 OK ou 201 Created
Cliente cadastrado com perfil CLIENTE.
```

Se o e-mail já existir, altere para outro, por exemplo:

```text
cliente.api.teste2@raizes.com
```

---

### Cadastro de gerente

Valida se o administrador consegue cadastrar um gerente para uma unidade.

Faça login como administrador:

```http
POST /usuarios/login
```

Body:

```json
{
  "email": "admin@raizes.com",
  "senha": "123456"
}
```

Depois, autorize o Swagger/Postman com o token do administrador.

Endpoint:

```http
POST /usuarios/admin/cadastrar
```

Body:

```json
{
  "nome": "Gerente Teste Unidade 3",
  "email": "gerente.teste.unidade3@raizes.com",
  "senha": "123456",
  "role": "GERENTE",
  "unidadeId": 3
}
```

Resultado esperado:

```text
200 OK ou 201 Created
Gerente cadastrado se a unidade ainda não possuir gerente ativo.
```

Se a unidade 3 já possuir gerente ativo, crie uma nova unidade pelo endpoint `/unidades` e use o ID retornado.

---

### Tentativa de cadastrar gerente na mesma unidade

Valida a regra que impede dois gerentes ativos na mesma unidade.

Use token de administrador.

Endpoint:

```http
POST /usuarios/admin/cadastrar
```

Body:

```json
{
  "nome": "Gerente Duplicado Unidade 3",
  "email": "gerente.duplicado.unidade3@raizes.com",
  "senha": "123456",
  "role": "GERENTE",
  "unidadeId": 3
}
```

Resultado esperado:

```text
400 Bad Request
Erro informando que a unidade já possui gerente ativo.
```

---

### Cadastro de funcionário

Valida se o gerente ou administrador consegue cadastrar um funcionário vinculado a uma unidade.

Opção usando gerente:

```http
POST /usuarios/gerente/cadastrar
```

Body:

```json
{
  "nome": "Funcionário Teste",
  "email": "funcionario.teste@raizes.com",
  "senha": "123456",
  "role": "FUNCIONARIO",
  "unidadeId": 1
}
```

Resultado esperado:

```text
200 OK ou 201 Created
Funcionário cadastrado com perfil FUNCIONARIO e vinculado à unidade.
```

Também é possível cadastrar funcionário como administrador:

```http
POST /usuarios/admin/cadastrar
```

Body:

```json
{
  "nome": "Funcionário Admin Teste",
  "email": "funcionario.admin.teste@raizes.com",
  "senha": "123456",
  "role": "FUNCIONARIO",
  "unidadeId": 1
}
```

Teste negativo recomendado: tente cadastrar outro usuário com o mesmo e-mail.

Resultado esperado:

```text
400 Bad Request
Erro informando que o e-mail já está cadastrado.
```

---

### Criar pedido válido

Valida criação de pedido com cliente existente, unidade existente, produto existente, estoque disponível e canal válido.

Faça login como cliente e autorize com o token do cliente.

Endpoint:

```http
POST /pedidos
```

Body:

```json
{
  "clienteId": 3,
  "unidadeId": 1,
  "canalPedido": "APP",
  "itens": [
    {
      "produtoId": 1,
      "quantidade": 1
    }
  ]
}
```

Resultado esperado:

```text
200 OK ou 201 Created
Pedido criado com status AGUARDANDO_PAGAMENTO.
Estoque do produto reduzido na unidade.
```

Guarde o ID retornado do pedido para usar nos testes de pagamento e cancelamento.

Exemplo:

```text
pedidoId = 35
```

---

### Pedido com canal inexistente

Valida erro quando o campo `canalPedido` recebe valor fora do enum permitido.

Endpoint:

```http
POST /pedidos
```

Body:

```json
{
  "clienteId": 3,
  "unidadeId": 1,
  "canalPedido": "IFOOD",
  "itens": [
    {
      "produtoId": 1,
      "quantidade": 1
    }
  ]
}
```

Resultado esperado:

```text
400 Bad Request
Erro de validação/conversão de enum, pois IFOOD não existe em CanalPedido.
```

Canais aceitos:

```text
APP, TOTEM, BALCAO, WEB
```

---

### Pedido com estoque insuficiente

Valida bloqueio quando a quantidade solicitada é maior que o estoque disponível.

Endpoint:

```http
POST /pedidos
```

Body:

```json
{
  "clienteId": 3,
  "unidadeId": 1,
  "canalPedido": "WEB",
  "itens": [
    {
      "produtoId": 1,
      "quantidade": 999999
    }
  ]
}
```

Resultado esperado:

```text
400 Bad Request
Erro de estoque insuficiente.
Pedido não deve ser criado.
```

---

### Pedido com produto inexistente

Valida erro quando o produto informado não existe no banco.

Endpoint:

```http
POST /pedidos
```

Body:

```json
{
  "clienteId": 3,
  "unidadeId": 1,
  "canalPedido": "APP",
  "itens": [
    {
      "produtoId": 999999,
      "quantidade": 1
    }
  ]
}
```

Resultado esperado:

```text
400 Bad Request ou 404 Not Found
Erro informando produto não encontrado ou estoque não encontrado.
Pedido não deve ser criado.
```

---

### Aprovar pagamento

Valida o pagamento simulado e a alteração automática do pedido para `EM_PREPARO`.

Pré-condição:

```text
Ter um pedido com status AGUARDANDO_PAGAMENTO.
```

Endpoint:

```http
POST /pagamentos/{pedidoId}
```

Exemplo:

```http
POST /pagamentos/35
```

Body:

```json
{
  "formaPagamento": "PIX",
  "usarPontos": false
}
```

Resultado esperado:

```text
200 OK ou 201 Created
Pagamento aprovado.
Pedido atualizado automaticamente para EM_PREPARO.
Valor final registrado no pagamento.
```

Importante: não use `PATCH /pedidos/{id}/status?status=PAGO`. O status PAGO não deve ser definido manualmente. O pagamento deve ser feito pelo endpoint `/pagamentos/{pedidoId}`.

---

### Usar pontos no pagamento

Valida o desconto por pontos de fidelidade.

Pré-condição:

```text
O cliente precisa possuir pontos disponíveis.
```

Caso o cliente ainda não tenha pontos, entregue um pedido antes usando as rotas de atualização de status.

Depois, crie um novo pedido e pague usando pontos.

Endpoint:

```http
POST /pagamentos/{pedidoId}
```

Body:

```json
{
  "formaPagamento": "PIX",
  "usarPontos": true
}
```

Resultado esperado:

```text
Pagamento aprovado.
Valor final pago menor que o valor total do pedido.
Pontos usados são descontados do cliente.
```

---

### Atualizar status e gerar pontos de fidelidade

Valida que o cliente só recebe pontos quando o pedido é entregue.

Pré-condição:

```text
Pedido pago e com status EM_PREPARO.
```

Use token de funcionário, gerente ou administrador.

Primeiro altere para saiu para entrega:

```http
PATCH /pedidos/{id}/status?status=SAIU_PARA_ENTREGA
```

Depois altere para entregue:

```http
PATCH /pedidos/{id}/status?status=ENTREGUE
```

Resultado esperado:

```text
Pedido muda para ENTREGUE.
Cliente recebe pontos de fidelidade conforme o valor pago.
```

Para conferir os pontos:

```http
GET /usuarios/3
```

Resultado esperado:

```text
Campo pontosFidelidade com valor atualizado.
```

---

### Cancelar pedido

Valida o cancelamento de pedido ainda não entregue.

Pré-condição:

```text
Pedido deve estar em AGUARDANDO_PAGAMENTO, EM_PREPARO ou SAIU_PARA_ENTREGA.
Pedido ENTREGUE não pode ser cancelado.
```

Crie um novo pedido válido antes deste teste para não usar um pedido já entregue.

Endpoint:

```http
PATCH /pedidos/{id}/cancelar
```

Exemplo:

```http
PATCH /pedidos/36/cancelar
```

Body:

```text
Não possui body.
```

Resultado esperado:

```text
Pedido muda para CANCELADO.
Estoque é devolvido.
Se pontos foram usados no pagamento, os pontos são devolvidos ao cliente.
```

---

### Pedido entregue não pode ser cancelado

Valida a regra negativa de cancelamento.

Pré-condição:

```text
Pedido já deve estar com status ENTREGUE.
```

Endpoint:

```http
PATCH /pedidos/{id}/cancelar
```

Resultado esperado:

```text
400 Bad Request
Erro informando que pedido entregue não pode ser cancelado.
```

---

## 12. O que mostrar como evidência no trabalho

Para montar as evidências no documento final, tire prints dos seguintes testes:

- login válido retornando token;
- login inválido retornando erro;
- acesso sem permissão retornando 403;
- cadastro público de cliente;
- cadastro de gerente;
- tentativa de cadastrar gerente duplicado na mesma unidade;
- cadastro de funcionário;
- criação de pedido válido;
- pedido com canal inexistente;
- pedido com estoque insuficiente;
- pedido com produto inexistente;
- pagamento aprovado;
- uso de pontos no pagamento;
- cancelamento de pedido.

Em cada print, tente mostrar:

- endpoint utilizado;
- método HTTP;
- body enviado, quando houver;
- status da resposta;
- resposta da API.

## 13. Endpoints principais

### Usuários

```http
POST /usuarios/login
POST /usuarios/cliente
POST /usuarios/funcionario/cadastrar-cliente
POST /usuarios/gerente/cadastrar
POST /usuarios/admin/cadastrar
GET /usuarios
GET /usuarios/{id}
PATCH /usuarios/admin/{id}
DELETE /usuarios/admin/{id}
```

### Unidades

```http
GET /unidades
POST /unidades
PATCH /unidades/{id}
PATCH /unidades/{id}/desativar
DELETE /unidades/{id}
```

### Produtos

```http
GET /produtos
POST /produtos
PATCH /produtos/{produtoId}/usuario/{usuarioId}
PATCH /produtos/{produtoId}/usuario/{usuarioId}/desativar
```

### Estoques

```http
GET /estoques/unidade/{unidadeId}
POST /estoques
DELETE /estoques/produto/{produtoId}/unidade/{unidadeId}
```

### Pedidos

```http
POST /pedidos
GET /pedidos/cliente/{clienteId}
GET /pedidos/unidade/{unidadeId}
PATCH /pedidos/{id}/status?status=SAIU_PARA_ENTREGA
PATCH /pedidos/{id}/status?status=ENTREGUE
PATCH /pedidos/{id}/cancelar
```

### Pagamentos

```http
POST /pagamentos/{pedidoId}
```

---

## 14. Fluxo principal do pedido

O fluxo principal do pedido é:

```text
AGUARDANDO_PAGAMENTO
        ↓
EM_PREPARO
        ↓
SAIU_PARA_ENTREGA
        ↓
ENTREGUE
```

Observação:

```text
O status PAGO existe por compatibilidade, mas não deve ser definido manualmente pelo endpoint de atualização de pedido. O pagamento é realizado pelo endpoint /pagamentos/{pedidoId}, e após aprovação o pedido entra em EM_PREPARO.
```

---

## 15. Regras de negócio importantes

- Somente clientes podem se cadastrar publicamente.
- Funcionário pode cadastrar clientes.
- Gerente pode cadastrar clientes e funcionários da sua unidade.
- Administrador pode cadastrar clientes, funcionários e gerentes.
- Cada unidade pode possuir somente um gerente ativo.
- Funcionários e gerentes devem estar vinculados a uma unidade.
- Clientes não precisam estar vinculados a uma unidade fixa.
- Um pedido só pode ser criado se houver estoque suficiente.
- O canal do pedido deve ser válido: APP, TOTEM, BALCAO ou WEB.
- Após pagamento aprovado, o pedido entra em EM_PREPARO.
- O cliente só ganha pontos após o pedido ser entregue.
- Pontos podem ser usados como desconto em pedidos futuros.
- Pedido entregue não pode ser cancelado.
- Pedido cancelado devolve estoque.

---

## 16. Observações para correção

Para correção do projeto, recomenda-se verificar:

- o link do repositório;
- se o banco `raizes_nordeste` foi criado;
- se o `application.properties` está com usuário e senha corretos;
- se a aplicação está rodando em `localhost:8080`;
- se o Swagger abre corretamente;
- se o front-end abre corretamente;
- se os usuários de teste existem no banco;
- se os prints dos testes foram anexados ao relatório final.

---

## 17. Estrutura resumida do projeto

```text
src/main/java
├── controller
├── service
├── repository
├── model
├── dto
├── config
├── security
└── exception

src/main/resources
├── application.properties
├── data.sql
└── static
    ├── index.html
    ├── style.css
    └── script.js
```

---

## 18. Conclusão

O projeto demonstra uma API REST com autenticação, autorização por perfil, persistência em banco relacional, controle de estoque por unidade, criação de pedidos, pagamento simulado, pontos de fidelidade e testes documentados. A estrutura em camadas facilita manutenção e evolução futura do sistema.
