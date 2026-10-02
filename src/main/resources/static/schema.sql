-- schema.sql
CREATE TABLE IF NOT EXISTS empresas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    razao_social VARCHAR(200) NOT NULL,
    cnpj VARCHAR(20) NOT NULL UNIQUE,
    inscricao_estadual VARCHAR(20),
    endereco VARCHAR(300),
    regime_tributario VARCHAR(30)
);