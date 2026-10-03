-- Tabelas principais criadas via JPA ddl-auto=update
-- Apenas índices e constraints adicionais:

CREATE INDEX IF NOT EXISTS idx_lancamento_empresa_data ON lancamentos(empresa_id, data);
CREATE INDEX IF NOT EXISTS idx_item_conta_lancamento ON itens_lancamento(conta_id, lancamento_id);
CREATE INDEX IF NOT EXISTS idx_nf_empresa_data ON notas_fiscais(empresa_id, data_emissao);
CREATE INDEX IF NOT EXISTS idx_audit_empresa_instante ON audit_log(empresa_id, instante);