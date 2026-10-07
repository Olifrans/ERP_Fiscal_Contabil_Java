
-- ============================================================
-- ERP FISCAL CONTÁBIL - SCRIPT DE INICIALIZAÇÃO (BANCO NOVO)
-- ============================================================

-- ============================================================
-- SEÇÃO 1: ÍNDICES DE PERFORMANCE
-- (Removido "IF NOT EXISTS" para compatibilidade total com MySQL)
-- ============================================================
CREATE INDEX idx_lancamento_empresa_data ON lancamentos(empresa_id, data);
CREATE INDEX idx_item_conta_lancamento ON itens_lancamento(conta_id, lancamento_id);
CREATE INDEX idx_nf_empresa_data ON notas_fiscais(empresa_id, data_emissao);
CREATE INDEX idx_audit_empresa_instante ON audit_log(empresa_id, instante);
CREATE INDEX idx_contas_empresa_codigo ON contas_contabeis(empresa_id, codigo);
CREATE INDEX idx_usuarios_login ON usuarios(login);
CREATE INDEX idx_fechamentos_empresa_periodo ON fechamentos_mensais(empresa_id, ano, mes);

-- ============================================================
-- SEÇÃO 2: DADOS INICIAIS (SEED)
-- ============================================================

-- 2.1 Empresa Demo (Será a ID 1 por ser a primeira)
INSERT IGNORE INTO empresas 
    (razao_social, cnpj, inscricao_estadual, endereco, municipio, uf, regime_tributario, telefone, email)
VALUES 
    ('Empresa Demo LTDA', '12.345.678/0001-90', '123.456.789.000', 'Rua Demo, 100', 'São Paulo', 'SP', 'LUCRO_REAL', '11999999999', 'demo@erp.com');

-- 2.2 Usuário Administrador (Senha: admin123)
-- Hash BCrypt 100% validado para "admin123"
INSERT IGNORE INTO usuarios 
    (login, senha_hash, nome, email, perfil, empresa_id, ativo)
VALUES 
    ('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGfa3ZlW', 'Administrador', 'admin@erp.com', 'ADMIN', 1, 1);

-- 2.3 Plano de Contas Padrão (Vinculado à empresa ID 1)
INSERT IGNORE INTO contas_contabeis (empresa_id, codigo, descricao, natureza, grupo, analitica) VALUES
    (1, '1.1.01.001', 'Caixa Geral', 'DEVEDORA', 'ATIVO', true),
    (1, '1.1.01.002', 'Banco Conta Movimento', 'DEVEDORA', 'ATIVO', true),
    (1, '1.1.02.001', 'Clientes (Contas a Receber)', 'DEVEDORA', 'ATIVO', true),
    (1, '1.1.03.001', 'Estoque de Mercadorias', 'DEVEDORA', 'ATIVO', true),
    (1, '1.1.04.001', 'ICMS a Recuperar', 'DEVEDORA', 'ATIVO', true),
    (1, '1.2.01.001', 'Imóveis', 'DEVEDORA', 'ATIVO', true),
    (1, '1.2.01.005', '(-) Depreciação Acumulada', 'CREDORA', 'ATIVO', true),
    (1, '2.1.01.001', 'Fornecedores', 'CREDORA', 'PASSIVO', true),
    (1, '2.1.01.002', 'Salários a Pagar', 'CREDORA', 'PASSIVO', true),
    (1, '2.1.02.001', 'ICMS a Recolher', 'CREDORA', 'PASSIVO', true),
    (1, '2.1.02.002', 'PIS a Recolher', 'CREDORA', 'PASSIVO', true),
    (1, '2.1.02.003', 'COFINS a Recolher', 'CREDORA', 'PASSIVO', true),
    (1, '2.1.03.001', 'IRPJ a Recolher', 'CREDORA', 'PASSIVO', true),
    (1, '2.1.03.002', 'CSLL a Recolher', 'CREDORA', 'PASSIVO', true),
    (1, '2.3.01.001', 'Capital Social', 'CREDORA', 'PASSIVO', true),
    (1, '3.1.01.001', 'Receita de Vendas de Mercadorias', 'CREDORA', 'RECEITA', true),
    (1, '3.1.01.002', 'Receita de Prestação de Serviços', 'CREDORA', 'RECEITA', true),
    (1, '4.1.01.001', '(-) Impostos sobre Vendas', 'DEVEDORA', 'RECEITA', true),
    (1, '5.1.01.001', 'CMV - Custo da Mercadoria Vendida', 'DEVEDORA', 'DESPESA', true),
    (1, '5.2.01.001', 'Despesas com Pessoal', 'DEVEDORA', 'DESPESA', true),
    (1, '5.2.01.003', 'Despesas Administrativas', 'DEVEDORA', 'DESPESA', true),
    (1, '5.2.01.008', 'Despesas Financeiras', 'DEVEDORA', 'DESPESA', true),
    (1, '3.9.01.001', 'Resultado do Exercício', 'CREDORA', 'RESULTADO', true);




-- 🚀 Como Usar
-- Reinicie a aplicação Spring Boot
-- Acesse http://localhost:8080/login.html
-- Login: admin | Senha: admin123

