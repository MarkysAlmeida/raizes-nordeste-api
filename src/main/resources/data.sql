-- Dados iniciais do sistema Raízes do Nordeste
-- Senha padrão para todos os usuários: 123456

-- UNIDADES / LOJAS
INSERT INTO unidades (id, nome, cidade, endereco, ativo) VALUES
                                                             (1, 'Unidade Centro', 'Recife', 'Rua Principal, 100', true),
                                                             (2, 'Unidade Boa Viagem', 'Recife', 'Av. Boa Viagem, 1000', true),
                                                             (3, 'Unidade Shopping Recife', 'Recife', 'Shopping Recife - Loja 25', true)
    ON CONFLICT (id) DO NOTHING;

-- PRODUTOS
INSERT INTO produtos (id, nome, descricao, preco, ativo) VALUES
                                                             (1, 'Tapioca de Carne Seca', 'Tapioca recheada com carne seca e queijo coalho', 32.90, true),
                                                             (2, 'Tapioca de Frango', 'Tapioca recheada com frango desfiado e requeijão', 28.90, true),
                                                             (3, 'Cuscuz com Queijo Coalho', 'Cuscuz nordestino servido com queijo coalho', 22.90, true),
                                                             (4, 'Cuscuz com Carne de Sol', 'Cuscuz recheado com carne de sol e manteiga de garrafa', 34.90, true),
                                                             (5, 'Bolo de Macaxeira', 'Fatia de bolo de macaxeira tradicional', 12.90, true),
                                                             (6, 'Bolo de Milho', 'Fatia de bolo de milho cremoso', 11.90, true),
                                                             (7, 'Suco de Caju', 'Suco natural de caju', 9.90, true),
                                                             (8, 'Suco de Acerola', 'Suco natural de acerola', 9.90, true),
                                                             (9, 'Café Nordestino', 'Café coado acompanhado de rapadura', 8.90, true),
                                                             (10, 'Combo Café da Manhã', 'Cuscuz, café, bolo e suco regional', 39.90, true)
    ON CONFLICT (id) DO NOTHING;

-- USUÁRIOS
INSERT INTO usuarios (id, nome, email, senha, role, unidade_id, pontos_fidelidade, ativo) VALUES
                                                                                              (1, 'Administrador', 'admin@raizes.com', '$2a$10$olComMwCahTdoxEzooze4.LA.kXNgH8liam3ysPj.jb84Z35AXhSS', 'ADMINISTRADOR', null, 0, true),
                                                                                              (2, 'Gerente Centro', 'gerente.centro@raizes.com', '$2a$10$olComMwCahTdoxEzooze4.LA.kXNgH8liam3ysPj.jb84Z35AXhSS', 'GERENTE', 1, 0, true),
                                                                                              (3, 'Gerente Boa Viagem', 'gerente.boaviagem@raizes.com', '$2a$10$olComMwCahTdoxEzooze4.LA.kXNgH8liam3ysPj.jb84Z35AXhSS', 'GERENTE', 2, 0, true),
                                                                                              (4, 'Gerente Shopping Recife', 'gerente.shopping@raizes.com', '$2a$10$olComMwCahTdoxEzooze4.LA.kXNgH8liam3ysPj.jb84Z35AXhSS', 'GERENTE', 3, 0, true),
                                                                                              (5, 'Funcionário Centro', 'funcionario.centro@raizes.com', '$2a$10$olComMwCahTdoxEzooze4.LA.kXNgH8liam3ysPj.jb84Z35AXhSS', 'FUNCIONARIO', 1, 0, true),
                                                                                              (6, 'Funcionário Boa Viagem', 'funcionario.boaviagem@raizes.com', '$2a$10$olComMwCahTdoxEzooze4.LA.kXNgH8liam3ysPj.jb84Z35AXhSS', 'FUNCIONARIO', 2, 0, true),
                                                                                              (7, 'Funcionário Shopping Recife', 'funcionario.shopping@raizes.com', '$2a$10$olComMwCahTdoxEzooze4.LA.kXNgH8liam3ysPj.jb84Z35AXhSS', 'FUNCIONARIO', 3, 0, true),
                                                                                              (8, 'Cliente Teste', 'cliente@raizes.com', '$2a$10$olComMwCahTdoxEzooze4.LA.kXNgH8liam3ysPj.jb84Z35AXhSS', 'CLIENTE', null, 100, true)
    ON CONFLICT (email) DO NOTHING;

-- ESTOQUE POR UNIDADE
INSERT INTO estoques (id, produto_id, unidade_id, quantidade) VALUES
                                                                  (1, 1, 1, 40),
                                                                  (2, 2, 1, 35),
                                                                  (3, 3, 1, 50),
                                                                  (4, 4, 1, 25),
                                                                  (5, 5, 1, 30),
                                                                  (6, 6, 1, 30),
                                                                  (7, 7, 1, 60),
                                                                  (8, 8, 1, 60),
                                                                  (9, 9, 1, 80),
                                                                  (10, 10, 1, 20),

                                                                  (11, 1, 2, 30),
                                                                  (12, 2, 2, 30),
                                                                  (13, 3, 2, 40),
                                                                  (14, 4, 2, 20),
                                                                  (15, 5, 2, 25),
                                                                  (16, 6, 2, 25),
                                                                  (17, 7, 2, 50),
                                                                  (18, 8, 2, 50),
                                                                  (19, 9, 2, 70),
                                                                  (20, 10, 2, 15),

                                                                  (21, 1, 3, 25),
                                                                  (22, 2, 3, 25),
                                                                  (23, 3, 3, 35),
                                                                  (24, 4, 3, 15),
                                                                  (25, 5, 3, 20),
                                                                  (26, 6, 3, 20),
                                                                  (27, 7, 3, 40),
                                                                  (28, 8, 3, 40),
                                                                  (29, 9, 3, 60),
                                                                  (30, 10, 3, 10)
    ON CONFLICT (id) DO NOTHING;

-- AJUSTE DAS SEQUÊNCIAS
SELECT setval('unidades_id_seq', (SELECT MAX(id) FROM unidades));
SELECT setval('produtos_id_seq', (SELECT MAX(id) FROM produtos));
SELECT setval('usuarios_id_seq', (SELECT MAX(id) FROM usuarios));
SELECT setval('estoques_id_seq', (SELECT MAX(id) FROM estoques));