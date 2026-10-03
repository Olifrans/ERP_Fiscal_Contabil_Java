-- Empresa demo
INSERT IGNORE INTO empresas (razao_social, cnpj, inscricao_estadual, endereco, municipio, uf, regime_tributario, telefone, email)
VALUES ('Empresa Demo LTDA','12.345.678/0001-90','123.456.789.000','Rua Demo, 100','São Paulo','SP','LUCRO_REAL','11999999999','demo@erp.com');

-- Usuário admin (senha: admin123 hash BCrypt)
INSERT IGNORE INTO usuarios (login, senha_hash, nome, email, perfil, empresa_id, ativo)
VALUES ('admin','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH','Administrador','admin@erp.com','ADMIN',1,1);

-- Plano de Contas padrão (empresa_id=1)
INSERT IGNORE INTO contas_contabeis (empresa_id, codigo, descricao, natureza, grupo, analitica) VALUES
(1,'1.1.01.001','Caixa Geral','DEVEDORA','ATIVO',true),
(1,'1.1.01.002','Banco Conta Movimento','DEVEDORA','ATIVO',true),
(1,'1.1.02.001','Clientes','DEVEDORA','ATIVO',true),
(1,'1.1.03.001','Estoque de Mercadorias','DEVEDORA','ATIVO',true),
(1,'2.1.01.001','Fornecedores','CREDORA','PASSIVO',true),
(1,'2.1.02.001','ICMS a Recolher','CREDORA','PASSIVO',true),
(1,'2.1.02.002','PIS a Recolher','CREDORA','PASSIVO',true),
(1,'2.1.02.003','COFINS a Recolher','CREDORA','PASSIVO',true),
(1,'2.1.03.001','IRPJ a Recolher','CREDORA','PASSIVO',true),
(1,'2.1.03.002','CSLL a Recolher','CREDORA','PASSIVO',true),
(1,'3.1.01.001','Receita de Vendas','CREDORA','RECEITA',true),
(1,'3.1.01.002','Receita de Serviços','CREDORA','RECEITA',true),
(1,'4.1.01.001','Deduções da Receita','DEVEDORA','RECEITA',true),
(1,'5.1.01.001','CMV','DEVEDORA','DESPESA',true),
(1,'5.2.01.001','Despesas com Pessoal','DEVEDORA','DESPESA',true),
(1,'5.2.01.002','Despesas Administrativas','DEVEDORA','DESPESA',true),
(1,'3.9.01.001','Resultado do Exercício','CREDORA','RESULTADO',true);