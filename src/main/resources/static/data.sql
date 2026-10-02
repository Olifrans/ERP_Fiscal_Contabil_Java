-- data.sql — Plano de Contas padrão
INSERT IGNORE INTO contas_contabeis (codigo, descricao, natureza, grupo) VALUES
('1.1.01.001','Caixa Geral','DEVEDORA','ATIVO'),
('1.1.01.002','Banco Conta Movimento','DEVEDORA','ATIVO'),
('1.1.02.001','Clientes','DEVEDORA','ATIVO'),
('1.1.03.001','Estoque de Mercadorias','DEVEDORA','ATIVO'),
('2.1.01.001','Fornecedores','CREDORA','PASSIVO'),
('2.1.02.001','ICMS a Recolher','CREDORA','PASSIVO'),
('2.1.02.002','PIS a Recolher','CREDORA','PASSIVO'),
('2.1.02.003','COFINS a Recolher','CREDORA','PASSIVO'),
('2.1.03.001','IRPJ a Recolher','CREDORA','PASSIVO'),
('2.1.03.002','CSLL a Recolher','CREDORA','PASSIVO'),
('3.1.01.001','Receita de Vendas','CREDORA','RECEITA'),
('3.1.01.002','Receita de Serviços','CREDORA','RECEITA'),
('4.1.01.001','Deduções da Receita','DEVEDORA','RECEITA'),
('5.1.01.001','CMV','DEVEDORA','DESPESA'),
('5.2.01.001','Despesas com Pessoal','DEVEDORA','DESPESA'),
('5.2.01.002','Despesas Administrativas','DEVEDORA','DESPESA');

INSERT IGNORE INTO empresas (razao_social, cnpj, inscricao_estadual, regime_tributario)
VALUES ('Empresa Demo LTDA','12.345.678/0001-90','123.456.789.000','LUCRO_REAL');