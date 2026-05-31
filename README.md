# Projeto Multidisciplinar - Sistema Raízes do Nordeste

## Sobre o projeto

Este projeto consiste em uma API REST desenvolvida com Java e Spring Boot para gerenciamento de pedidos da rede fictícia de lanchonetes **Raízes do Nordeste**.

A aplicação foi construída com foco em backend, autenticação, controle de permissões, estoque por unidade, pedidos, pagamentos, programa de fidelidade, documentação com Swagger e uma interface web inicial para facilitar o uso por pessoas sem conhecimento técnico.

Projeto desenvolvido por **Marcos Antonio**
RU: **4767149**

---

## Funcionalidades desenvolvidas até o momento

### Usuários

* Cadastro de usuários
* Login com e-mail e senha
* Senha criptografada com BCrypt
* Geração de token JWT
* Resposta de login sem expor senha
* Controle de perfis:

    * CLIENTE
    * FUNCIONARIO
    * GERENTE
    * ADMINISTRADOR

### Produtos

* Cadastro de produtos
* Listagem de produtos
* Controle de acesso para cadastro:

    * GERENTE
    * ADMINISTRADOR

### Unidades

* Cadastro de unidades/lojas
* Listagem de unidades disponíveis
* Unidade vinculada ao pedido

### Estoque

* Estoque separado por unidade
* Produto pode ter quantidades diferentes em cada loja
* Baixa automática de estoque ao criar pedido

### Pedidos

* Criação de pedido
* Cálculo automático do valor total
* Associação entre cliente, unidade e produtos
* Atualização de status do pedido
* Controle de status:

    * AGUARDANDO_PAGAMENTO
    * PAGO
    * EM_PREPARO
    * SAIU_PARA_ENTREGA
    * ENTREGUE
    * CANCELADO

### Pagamentos

* Processamento de pagamento mock
* Pagamento aprovado ou recusado
* Pedido muda para PAGO quando aprovado
* Pedido muda para CANCELADO quando recusado

### Fidelidade

* Cliente ganha pontos após pagamento aprovado
* Pontos calculados com base no valor do pedido

### Segurança

* Spring Security
* JWT
* BCrypt
* Rotas protegidas
* Controle de permissões por perfil

### Swagger

* Documentação automática da API
* Acesso em:

```text
http://localhost:8080/swagger-ui/index.html
```

### Front-end inicial

* Tela de login
* Login integrado com API
* Token salvo no navegador
* Tela de lojas disponíveis
* Início da tela de produtos por loja

---

## Tecnologias utilizadas

### Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* Hibernate
* Maven
* JWT
* BCrypt
* Lombok

### Banco de dados

* PostgreSQL

### Documentação

* Swagger / OpenAPI

### Front-end

* HTML
* CSS
* JavaScript

### Ferramentas

* IntelliJ IDEA
* Postman
* Git
* GitHub

---

## Estrutura do projeto

```text
src/main/java/br/com/raizesdonordeste/api
├── config
├── controller
├── dto
├── exception
├── model
│   └── enums
├── repository
├── security
└── service
```

### Camadas da aplicação

```text
Controller
Recebe requisições HTTP

Service
Contém as regras de negócio

Repository
Comunica com o banco de dados

Model
Representa as entidades JPA

DTO
Transporta dados de entrada e saída

Security
Controla JWT e autenticação

Config
Configura segurança e recursos gerais
```

---

## Como executar o projeto

### Pré-requisitos

* Java 21
* Maven
* PostgreSQL
* IntelliJ IDEA

### Banco de dados

Criar o banco no PostgreSQL:

```sql
CREATE DATABASE raizes_nordeste;
```

### Configuração

No arquivo:

```text
src/main/resources/application.properties
```

Configurar:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/raizes_nordeste
spring.datasource.username=postgres
spring.datasource.password=54321

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Executar

No IntelliJ, rodar a classe principal:

```text
RaizesNordesteApiApplication
```

---

## Endpoints principais

### Usuários

```text
POST /usuarios
POST /usuarios/login
```

### Produtos

```text
GET /produtos
POST /produtos
```

### Unidades

```text
GET /unidades
POST /unidades
```

### Estoques

```text
POST /estoques
GET /estoques/unidade/{unidadeId}
```

### Pedidos

```text
POST /pedidos
PATCH /pedidos/{id}/status
```

### Pagamentos

```text
POST /pagamentos/{pedidoId}
```

---

## Fluxo principal

```text
Cliente faz login
↓
Sistema gera token JWT
↓
Cliente escolhe unidade
↓
Sistema mostra produtos disponíveis naquela unidade
↓
Cliente cria pedido
↓
Sistema verifica estoque
↓
Sistema baixa estoque
↓
Sistema calcula total
↓
Cliente realiza pagamento
↓
Pagamento aprovado
↓
Pedido muda para PAGO
↓
Cliente ganha pontos de fidelidade
```

---

## Mudanças e melhorias planejadas

* Melhorar a tela inicial do sistema
* Criar tela separada para cada perfil:

    * Cliente
    * Funcionário
    * Gerente
    * Administrador
* Criar tela de produtos por unidade
* Permitir cliente montar pedido pela interface visual
* Criar painel de pedidos para funcionário alterar status
* Criar painel de gerente para cadastrar produtos e estoques
* Criar painel de administrador para gerenciar usuários, unidades e dados gerais
* Melhorar tratamento de erros no front-end
* Criar collection Postman
* Criar prints para documentação acadêmica
* Criar documentação final em PDF
* Melhorar layout visual do front-end

---

## Autor

Marcos Antonio
RU: 4767149

Projeto desenvolvido para fins acadêmicos na disciplina de Projeto Multidisciplinar.
