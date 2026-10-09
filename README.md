
---

# 🏛️ ERP Fiscal Contábil Profissional

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-MySQL%208.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Sistema ERP (Enterprise Resource Planning) para gestão **Fiscal e Contábil** de empresas brasileiras. Desenvolvido com arquitetura moderna, multi-tenant, segurança robusta e aderência às normas contábeis (partidas dobradas) e fiscais (SPED, NF-e).

---

## 🚀 Funcionalidades Principais

### 📊 Módulo Contábil
- **Plano de Contas**: Estrutura hierárquica personalizável (Ativo, Passivo, Receita, Despesa, Resultado).
- **Lançamentos Contábeis**: Registro com validação rigorosa de **partidas dobradas** (Débito = Crédito).
- **Livro Razão**: Consulta analítica detalhada por conta e período.
- **Balancete de Verificação**: Relatório analítico com saldos devedores e credores.
- **DRE Automática**: Demonstração do Resultado do Exercício com cálculo de IRPJ (15%) e CSLL (9%).
- **Fechamento Mensal**: Rotina de encerramento com estorno automático de contas de resultado.

### 🧾 Módulo Fiscal
- **Gestão de Notas Fiscais**: Registro de NF-e de entrada e saída com CFOP, base de cálculo e impostos.
- **Apuração de Impostos**: Cálculo automático de ICMS (Débito/Crédito), PIS (1,65%) e COFINS (7,6%).
- **Geração de SPED Fiscal**: Exportação de arquivo `.txt` no layout oficial da EFD ICMS/IPI (Blocos 0, C e 9).
- **NF-e (Estrutura)**: Geração de XML no layout 4.00 e assinatura digital via certificado A1 (PKCS#12).

### 🛡️ Segurança e Auditoria
- **Autenticação JWT**: Tokens seguros com tempo de expiração configurável.
- **Controle de Acesso**: Perfis de usuário (ADMIN, CONTADOR, FINANCEIRO).
- **Multi-Tenant**: Isolamento lógico de dados por `empresa_id`.
- **Trilha de Auditoria**: Registro automático (`@EntityListeners`) de todas as criações, atualizações e exclusões na tabela `audit_log`.

### 📈 Relatórios e Dashboard
- **Dashboard Interativo**: KPIs em tempo real e gráficos (Chart.js) de receitas e impostos.
- **Exportação PDF**: Geração de Balancete e Livro Razão em PDF profissional (OpenPDF).

---

## 🛠️ Stack Tecnológica

| Categoria | Tecnologias |
| :--- | :--- |
| **Backend** | Java 21 (LTS), Spring Boot 3.4.2, Spring Security, Spring Data JPA |
| **Banco de Dados** | MySQL 8.0 (com HikariCP) |
| **Segurança** | JWT (JJWT 0.12.6), BCrypt Password Encoder |
| **Fiscal/Relatórios** | Apache Santuario (XMLSec), OpenPDF 2.0.3 |
| **Frontend** | HTML5, CSS3, Bootstrap 5.3, Vanilla JavaScript, Chart.js |
| **Build & Tools** | Maven, Lombok, Spring DevTools, Swagger/OpenAPI |

---

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado:
- ☕ **JDK 21** (Obrigatório. Versões 25+ causam incompatibilidade com Spring Boot 3.4.x).
- 🐘 **MySQL 8.0** (ou superior) rodando localmente ou via Docker.
- 🛠️ **Maven 3.8+** (ou use o wrapper `mvnw` incluído no projeto).
- 💻 **IDE**: IntelliJ IDEA (recomendado), Eclipse ou VS Code com extensão Java.

---

## ⚙️ Instalação e Configuração

### 1. Clonar o Repositório
```bash
git clone https://github.com/seu-usuario/erp-fiscal-contabil.git
cd erp-fiscal-contabil
```

### 2. Configurar o Banco de Dados
O sistema cria o banco de dados automaticamente, mas o MySQL deve estar rodando. 
Edite o arquivo `src/main/resources/application.properties` se suas credenciais forem diferentes:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fiscal_erp?createDatabaseIfNotExist=true&useTimezone=true&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root # Altere se necessário
```

### 3. Compilar e Executar
O projeto utiliza o `DataInitializer` para criar a empresa demo e o usuário admin automaticamente na primeira execução.

**No Linux/macOS:**
```bash
./mvnw clean spring-boot:run
```

**No Windows (PowerShell/CMD):**
```cmd
.\mvnw.cmd clean spring-boot:run
```

> 💡 **Dica**: Na primeira execução, observe o console. Você verá mensagens como `✅ Empresa Demo criada` e `✅ Usuário 'admin' criado com sucesso!`.

---

## 🔑 Acesso ao Sistema

Após a inicialização (mensagem `Started EscolaApplication in X.XXX seconds`), acesse:

- **Frontend (Sistema)**: [http://localhost:8080/login.html](http://localhost:8080/login.html)
- **Documentação da API (Swagger)**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

**Credenciais Padrão:**
- **Usuário**: `admin`
- **Senha**: `admin123`

---

## 📂 Estrutura do Projeto

```text
src/main/java/com/senai/escola/
├── config/             # Configurações de Segurança, JWT, CORS e Inicialização de Dados
├── controller/         # Endpoints REST da API
├── dto/                # Data Transfer Objects (Records do Java)
├── entity/             # Entidades JPA (Tabelas do Banco)
├── repository/         # Interfaces Spring Data JPA
├── service/            # Regras de Negócio (Contábil, Fiscal, Relatórios, SPED, NF-e)
├── audit/              # Listener de Auditoria (@EntityListeners)
└── tenant/             # Filtro de contexto Multi-Tenant (ThreadLocal)
```

---

## 🔧 Solução de Problemas Comuns (Troubleshooting)

| Problema | Solução |
| :--- | :--- |
| `Unsupported class file major version 69` | Você está usando Java 25. Mude o JDK do projeto e do Maven para **Java 21**. |
| `Credenciais inválidas` no login | O usuário não foi criado. Verifique se a classe `DataInitializer.java` existe e se o banco `fiscal_erp` está vazio antes de rodar. |
| `Table 'fiscal_erp.usuarios' doesn't exist` | O Hibernate não criou as tabelas. Verifique se `spring.jpa.hibernate.ddl-auto=update` e `spring.jpa.defer-datasource-initialization=true` estão no `application.properties`. |
| Erro de CORS no Frontend | Certifique-se de que a classe `WebConfig.java` está configurada com `@CrossOrigin` ou `addCorsMappings`. |

---

## 🗺️ Roadmap (Próximas Melhorias)

- [ ] Integração real com WebService da SEFAZ para transmissão de NF-e (via NFeJava).
- [ ] Geração do SPED Contribuições (PIS/COFINS) e SPED Contábil (ECD).
- [ ] Módulo de Folha de Pagamento com cálculo de INSS/FGTS e integração eSocial.
- [ ] Emissão de Boletos Bancários (CNAB 240) e integração Pix.
- [ ] Migração de scripts SQL para **Flyway** ou **Liquibase** para controle de versão de banco.
- [ ] Testes de integração automatizados com **Testcontainers**.

---

## 📄 Licença

Este projeto é de código aberto e está disponível sob a licença [MIT](LICENSE). Sinta-se à vontade para usar, modificar e contribuir.

---

**Desenvolvido com ❤️ e ☕ por [Francisco Olifrans/Aulas SENAI Suzano]**  
*Dúvidas ou sugestões? Abra uma Issue no repositório!*

---
