// =========================================
// 1. CONFIGURAÇÃO E UTILITÁRIOS
// =========================================
const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => document.querySelectorAll(selector);

const fmt = (v) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v || 0);
const fmtNum = (v) => new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(v || 0);

// Verifica autenticação
const token = localStorage.getItem('token');
const user = JSON.parse(localStorage.getItem('user') || '{}');

if (!token) {
    window.location.href = '/login.html';
}

// Configura dados do usuário na sidebar
if (user.nome) {
    $('#userName').textContent = user.nome;
    $('#userRole').textContent = user.perfil || 'Usuário';
}

let empresaId = user.empresaId || 1;

// Define período padrão (mês atual)
const hoje = new Date();
const primeiroDia = new Date(hoje.getFullYear(), hoje.getMonth(), 1).toISOString().split('T')[0];
const ultimoDia = new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0).toISOString().split('T')[0];

let periodo = { ini: primeiroDia, fim: ultimoDia };

// Inicializa inputs de data
if ($('#dtIni')) $('#dtIni').value = periodo.ini;
if ($('#dtFim')) $('#dtFim').value = periodo.fim;

// Variáveis globais para gráficos (para poder destruir e recriar)
let chartReceitasInstance = null;
let chartImpostosInstance = null;

// =========================================
// 2. COMUNICAÇÃO COM API
// =========================================
async function api(path, opts = {}) {
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...opts.headers
    };

    const response = await fetch(path, { ...opts, headers });

    if (response.status === 401) {
        localStorage.clear();
        window.location.href = '/login.html';
        throw new Error('Sessão expirada');
    }

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Erro na requisição');
    }

    // Se for download de arquivo (blob), retorna o response direto
    if (opts.isBlob) return response;
    
    return response.json();
}

function showToast(type, message) {
    const toastEl = type === 'success' ? $('#toastSuccess') : $('#toastError');
    const msgEl = type === 'success' ? $('#toastSuccessMsg') : $('#toastErrorMsg');
    msgEl.textContent = message;
    const toast = new bootstrap.Toast(toastEl);
    toast.show();
}

// =========================================
// 3. NAVEGAÇÃO E RENDERIZAÇÃO
// =========================================
function setPeriodo() {
    periodo.ini = $('#dtIni').value;
    periodo.fim = $('#dtFim').value;
}

async function refresh() {
    setPeriodo();
    const activeTab = document.querySelector('.nav-link.active')?.dataset.tab || 'dashboard';
    await render(activeTab);
}

async function render(tab) {
    // Esconde todas as abas
    $$('.tab-content').forEach(t => t.classList.remove('active'));
    // Mostra a aba selecionada
    const target = $(`#tab-${tab}`);
    if (target) target.classList.add('active');

    // Atualiza sidebar
    $$('.nav-link').forEach(link => link.classList.remove('active'));
    const activeLink = $(`.nav-link[data-tab="${tab}"]`);
    if (activeLink) activeLink.classList.add('active');

    // Atualiza título e breadcrumb
    const titles = {
        dashboard: 'Dashboard', empresas: 'Empresas', usuarios: 'Usuários',
        plano: 'Plano de Contas', lancamentos: 'Lançamentos Contábeis',
        nf: 'Notas Fiscais', fiscal: 'Apuração Fiscal', dre: 'DRE',
        razao: 'Livro Razão', balancete: 'Balancete Analítico',
        fechamento: 'Fechamento Mensal', sped: 'SPED Fiscal',
        nfe: 'NF-e Eletrônica', auditoria: 'Trilha de Auditoria'
    };
    
    const title = titles[tab] || tab;
    $('#pageTitle').textContent = title;
    $('#breadcrumbCurrent').textContent = title;

    // Chama a função de renderização específica
    const renderFn = {
        dashboard: renderDashboard,
        empresas: renderEmpresas,
        usuarios: renderUsuarios,
        plano: renderPlano,
        lancamentos: renderLancamentos,
        nf: renderNf,
        fiscal: renderFiscal,
        dre: renderDre,
        razao: renderRazao,
        balancete: renderBalancete,
        fechamento: renderFechamento,
        sped: renderSped,
        nfe: renderNfe,
        auditoria: renderAuditoria
    }[tab];

    if (renderFn) {
        try {
            // Mostra loading sutil (opcional, pode ser melhorado com spinners)
            await renderFn();
        } catch (error) {
            console.error(error);
            showToast('error', `Erro ao carregar ${title}: ${error.message}`);
        }
    }
}

// =========================================
// 4. FUNÇÕES DE RENDERIZAÇÃO POR MÓDULO
// =========================================

async function renderDashboard() {
    const [dre, ap] = await Promise.all([
        api(`/api/contabil/dre?ini=${periodo.ini}&fim=${periodo.fim}`),
        api(`/api/fiscal/apuracao?ini=${periodo.ini}&fim=${periodo.fim}`)
    ]);

    // Atualiza KPIs
    $('#statReceitaLiquida').textContent = fmt(dre.receitaLiquida);
    $('#statLucroLiquido').textContent = fmt(dre.lucroLiquido);
    $('#statIcms').textContent = fmt(ap.saldoIcms);
    $('#statImpostos').textContent = fmt(ap.pis + ap.cofins + (dre.irpj || 0) + (dre.csll || 0));

    // Gráfico de Receitas
    const ctxRec = $('#chartReceitas').getContext('2d');
    if (chartReceitasInstance) chartReceitasInstance.destroy();
    
    chartReceitasInstance = new Chart(ctxRec, {
        type: 'doughnut',
        data: {
            labels: Object.keys(dre.detalheReceitas || {}),
            datasets: [{
                data: Object.values(dre.detalheReceitas || {}),
                backgroundColor: ['#667eea', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'bottom', labels: { usePointStyle: true, padding: 20 } }
            }
        }
    });

    // Gráfico de Impostos
    const ctxImp = $('#chartImpostos').getContext('2d');
    if (chartImpostosInstance) chartImpostosInstance.destroy();

    chartImpostosInstance = new Chart(ctxImp, {
        type: 'bar',
        data: {
            labels: ['ICMS', 'PIS', 'COFINS', 'IRPJ', 'CSLL'],
            datasets: [{
                label: 'Valor (R$)',
                data: [ap.saldoIcms, ap.pis, ap.cofins, dre.irpj || 0, dre.csll || 0],
                backgroundColor: ['#ef4444', '#f59e0b', '#3b82f6', '#8b5cf6', '#10b981'],
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, grid: { color: '#f1f5f9' } },
                x: { grid: { display: false } }
            }
        }
    });
}

async function renderEmpresas() {
    const emp = await api('/api/empresas');
    const tbody = $('#tbodyEmpresas');
    
    if (emp.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">Nenhuma empresa cadastrada</td></tr>';
        return;
    }

    tbody.innerHTML = emp.map(e => `
        <tr>
            <td><span class="fw-bold text-dark">${e.cnpj}</span></td>
            <td>${e.razaoSocial}</td>
            <td>${e.inscricaoEstadual || '-'}</td>
            <td>${e.municipio || '-'}/${e.uf || '-'}</td>
            <td><span class="badge bg-light text-dark border">${e.regimeTributario || '-'}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-primary"><i class="bi bi-pencil"></i></button>
            </td>
        </tr>
    `).join('');
}

window.salvarEmpresa = async () => {
    try {
        await api('/api/empresas', {
            method: 'POST',
            body: JSON.stringify({
                razaoSocial: $('#eRazao').value,
                cnpj: $('#eCnpj').value,
                inscricaoEstadual: $('#eIE').value,
                municipio: $('#eMun').value,
                uf: $('#eUF').value,
                regimeTributario: $('#eRegime').value
            })
        });
        
        showToast('success', 'Empresa salva com sucesso!');
        bootstrap.Modal.getInstance($('#modalEmpresa')).hide();
        $('#formEmpresa').reset();
        renderEmpresas();
    } catch (error) {
        showToast('error', error.message);
    }
};

async function renderPlano() {
    const contas = await api('/api/contabil/contas');
    $('#tab-plano').innerHTML = `
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="bi bi-diagram-3"></i> Plano de Contas</h5>
                <button class="btn btn-primary btn-sm" onclick="toggleFormConta()"><i class="bi bi-plus-lg"></i> Nova Conta</button>
            </div>
            <div class="card-body">
                <div id="formNovaConta" class="d-none mb-4 p-3 bg-light rounded border">
                    <div class="row g-2">
                        <div class="col-md-2"><input id="cCodigo" class="form-control form-control-sm" placeholder="Código (1.1.01)"></div>
                        <div class="col-md-4"><input id="cDesc" class="form-control form-control-sm" placeholder="Descrição"></div>
                        <div class="col-md-2">
                            <select id="cNat" class="form-select form-select-sm"><option>DEVEDORA</option><option>CREDORA</option></select>
                        </div>
                        <div class="col-md-2">
                            <select id="cGrupo" class="form-select form-select-sm">
                                <option>ATIVO</option><option>PASSIVO</option><option>RECEITA</option><option>DESPESA</option><option>RESULTADO</option>
                            </select>
                        </div>
                        <div class="col-md-2"><button class="btn btn-success btn-sm w-100" onclick="salvarConta()">Salvar</button></div>
                    </div>
                </div>
                <div class="table-responsive">
                    <table class="table table-hover">
                        <thead class="table-light"><tr><th>Código</th><th>Descrição</th><th>Natureza</th><th>Grupo</th></tr></thead>
                        <tbody>
                            ${contas.map(c => `
                                <tr>
                                    <td><code class="bg-light px-2 py-1 rounded">${c.codigo}</code></td>
                                    <td class="fw-medium">${c.descricao}</td>
                                    <td><span class="badge ${c.natureza === 'DEVEDORA' ? 'bg-info' : 'bg-warning'}">${c.natureza}</span></td>
                                    <td><span class="badge bg-secondary">${c.grupo}</span></td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    `;
}

window.toggleFormConta = () => $('#formNovaConta').classList.toggle('d-none');

window.salvarConta = async () => {
    try {
        await api('/api/contabil/contas', {
            method: 'POST',
            body: JSON.stringify({
                codigo: $('#cCodigo').value,
                descricao: $('#cDesc').value,
                natureza: $('#cNat').value,
                grupo: $('#cGrupo').value
            })
        });
        showToast('success', 'Conta criada!');
        renderPlano();
    } catch (e) { showToast('error', e.message); }
};

async function renderLancamentos() {
    const [lcs, contas] = await Promise.all([
        api(`/api/contabil/lancamentos?ini=${periodo.ini}&fim=${periodo.fim}`),
        api('/api/contabil/contas')
    ]);

    window._contas = contas;

    $('#tab-lancamentos').innerHTML = `
        <div class="card mb-4">
            <div class="card-header"><h5 class="mb-0"><i class="bi bi-journal-plus"></i> Novo Lançamento</h5></div>
            <div class="card-body">
                <div class="row g-2 mb-3">
                    <div class="col-md-2"><input type="date" id="lData" class="form-control form-control-sm" value="${periodo.fim}"></div>
                    <div class="col-md-4"><input id="lHist" class="form-control form-control-sm" placeholder="Histórico do lançamento"></div>
                    <div class="col-md-3"><input id="lDoc" class="form-control form-control-sm" placeholder="Nº Documento"></div>
                    <div class="col-md-3"><button class="btn btn-outline-secondary btn-sm w-100" onclick="addItem()"><i class="bi bi-plus"></i> Adicionar Item</button></div>
                </div>
                <div id="lItens" class="mb-3"></div>
                <div class="d-flex justify-content-end">
                    <button class="btn btn-primary" onclick="salvarLanc()"><i class="bi bi-check-lg"></i> Lançar Partida</button>
                </div>
            </div>
        </div>
        <div class="card">
            <div class="card-header"><h5 class="mb-0">Últimos Lançamentos</h5></div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead class="table-light">
                            <tr><th>Data</th><th>Documento</th><th>Histórico</th><th class="text-end">Débito</th><th class="text-end">Crédito</th></tr>
                        </thead>
                        <tbody>
                            ${lcs.slice(-10).reverse().map(l => {
                                const d = l.itens.filter(i => i.tipo === 'DEBITO').reduce((s, i) => s + i.valor, 0);
                                const c = l.itens.filter(i => i.tipo === 'CREDITO').reduce((s, i) => s + i.valor, 0);
                                return `<tr>
                                    <td>${l.data}</td>
                                    <td><small class="text-muted">${l.documento || '-'}</small></td>
                                    <td>${l.historico}</td>
                                    <td class="text-end text-danger fw-medium">${fmt(d)}</td>
                                    <td class="text-end text-success fw-medium">${fmt(c)}</td>
                                </tr>`;
                            }).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    `;
    addItem(); // Adiciona primeira linha vazia
}

window.addItem = () => {
    const opts = window._contas.map(c => `<option value="${c.id}">${c.codigo} - ${c.descricao}</option>`).join('');
    $('#lItens').insertAdjacentHTML('beforeend', `
        <div class="row g-2 mb-2 item-lanc align-items-end">
            <div class="col-md-5">
                <select class="form-select form-select-sm conta">${opts}</select>
            </div>
            <div class="col-md-3">
                <select class="form-select form-select-sm tipo">
                    <option value="DEBITO">Débito</option>
                    <option value="CREDITO">Crédito</option>
                </select>
            </div>
            <div class="col-md-3">
                <input type="number" step="0.01" class="form-control form-control-sm valor" placeholder="0,00">
            </div>
            <div class="col-md-1">
                <button class="btn btn-outline-danger btn-sm w-100" onclick="this.closest('.item-lanc').remove()"><i class="bi bi-trash"></i></button>
            </div>
        </div>
    `);
};

window.salvarLanc = async () => {
    const itens = [...$$('.item-lanc')].map(el => ({
        contaId: +el.querySelector('.conta').value,
        tipo: el.querySelector('.tipo').value,
        valor: +el.querySelector('.valor').value
    })).filter(i => i.valor > 0);

    if (itens.length < 2) return showToast('error', 'Mínimo de 2 itens para partida dobrada');

    const deb = itens.filter(i => i.tipo === 'DEBITO').reduce((s, i) => s + i.valor, 0);
    const cred = itens.filter(i => i.tipo === 'CREDITO').reduce((s, i) => s + i.valor, 0);

    if (Math.abs(deb - cred) > 0.01) {
        return showToast('error', `Partida desbalanceada! Débito: ${fmt(deb)} | Crédito: ${fmt(cred)}`);
    }

    try {
        await api('/api/contabil/lancamentos', {
            method: 'POST',
            body: JSON.stringify({
                data: $('#lData').value,
                historico: $('#lHist').value,
                documento: $('#lDoc').value,
                itens: itens
            })
        });
        showToast('success', 'Lançamento registrado com sucesso!');
        renderLancamentos();
    } catch (e) { showToast('error', e.message); }
};

// --- Funções simplificadas para os demais módulos (padrão similar) ---

async function renderNf() {
    const nfs = await api(`/api/fiscal/nf?ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#tab-nf').innerHTML = `
        <div class="card mb-4">
            <div class="card-header"><h5 class="mb-0"><i class="bi bi-receipt-cutoff"></i> Registrar Nota Fiscal</h5></div>
            <div class="card-body">
                <div class="row g-2">
                    <div class="col-md-1"><input id="nfNum" class="form-control form-control-sm" placeholder="Nº"></div>
                    <div class="col-md-2"><input type="date" id="nfData" class="form-control form-control-sm" value="${periodo.fim}"></div>
                    <div class="col-md-2"><select id="nfTipo" class="form-select form-select-sm"><option>SAIDA</option><option>ENTRADA</option></select></div>
                    <div class="col-md-2"><input id="nfCfop" class="form-control form-control-sm" placeholder="CFOP" value="5102"></div>
                    <div class="col-md-3"><input id="nfDest" class="form-control form-control-sm" placeholder="Destinatário"></div>
                    <div class="col-md-2"><input id="nfProd" type="number" step="0.01" class="form-control form-control-sm" placeholder="Vl. Produtos"></div>
                    <div class="col-md-2"><input id="nfIcms" type="number" step="0.01" class="form-control form-control-sm" placeholder="Vl. ICMS"></div>
                    <div class="col-md-2"><button class="btn btn-primary btn-sm w-100" onclick="salvarNf()">Salvar</button></div>
                </div>
            </div>
        </div>
        <div class="card"><div class="card-body p-0"><div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead class="table-light"><tr><th>Nº</th><th>Data</th><th>Tipo</th><th>CFOP</th><th>Destinatário</th><th class="text-end">Produtos</th><th class="text-end">ICMS</th><th>Status</th></tr></thead>
                <tbody>
                    ${nfs.map(n => `<tr>
                        <td>${n.numero}</td><td>${n.dataEmissao}</td><td>${n.tipo}</td><td>${n.cfop || '-'}</td>
                        <td>${n.destinatario || '-'}</td>
                        <td class="text-end fw-medium">${fmt(n.valorProdutos)}</td>
                        <td class="text-end fw-medium">${fmt(n.valorIcms)}</td>
                        <td><span class="badge-status badge-${n.status === 'ASSINADA' ? 'ass' : n.status === 'AUTORIZADA' ? 'aut' : 'dig'}">${n.status || 'DIGITADA'}</span></td>
                    </tr>`).join('')}
                </tbody>
            </table>
        </div></div></div>
    `;
}

window.salvarNf = async () => {
    try {
        await api('/api/fiscal/nf', {
            method: 'POST',
            body: JSON.stringify({
                numero: +$('#nfNum').value, dataEmissao: $('#nfData').value,
                tipo: $('#nfTipo').value, cfop: $('#nfCfop').value,
                destinatario: $('#nfDest').value,
                valorProdutos: +$('#nfProd').value, valorIcms: +$('#nfIcms').value,
                baseIcms: +$('#nfProd').value
            })
        });
        showToast('success', 'Nota Fiscal registrada!');
        renderNf();
    } catch (e) { showToast('error', e.message); }
};

async function renderFiscal() {
    const ap = await api(`/api/fiscal/apuracao?ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#tab-fiscal').innerHTML = `
        <div class="row g-4 mb-4">
            <div class="col-md-4"><div class="stat-card stat-danger"><div class="stat-content"><div class="stat-label">Débito ICMS</div><div class="stat-value">${fmt(ap.debitoIcms)}</div></div></div></div>
            <div class="col-md-4"><div class="stat-card stat-success"><div class="stat-content"><div class="stat-label">Crédito ICMS</div><div class="stat-value">${fmt(ap.creditoIcms)}</div></div></div></div>
            <div class="col-md-4"><div class="stat-card stat-warning"><div class="stat-content"><div class="stat-label">Saldo a Recolher</div><div class="stat-value text-danger">${fmt(ap.saldoIcms)}</div></div></div></div>
            <div class="col-md-4"><div class="stat-card stat-info"><div class="stat-content"><div class="stat-label">Base PIS/COFINS</div><div class="stat-value">${fmt(ap.basePisCofins)}</div></div></div></div>
            <div class="col-md-4"><div class="stat-card"><div class="stat-content"><div class="stat-label">PIS (1,65%)</div><div class="stat-value text-warning">${fmt(ap.pis)}</div></div></div></div>
            <div class="col-md-4"><div class="stat-card"><div class="stat-content"><div class="stat-label">COFINS (7,6%)</div><div class="stat-value text-warning">${fmt(ap.cofins)}</div></div></div></div>
        </div>
    `;
}

async function renderDre() {
    const d = await api(`/api/contabil/dre?ini=${periodo.ini}&fim=${periodo.fim}`);
    const row = (l, v, cls = '', bold = false) => `<tr><td class="${bold ? 'fw-bold' : ''}">${l}</td><td class="text-end ${cls} ${bold ? 'fw-bold' : ''}">${fmt(v)}</td></tr>`;
    
    $('#tab-dre').innerHTML = `
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="bi bi-graph-up"></i> Demonstração do Resultado do Exercício</h5>
                <button class="btn btn-outline-primary btn-sm" onclick="baixarBalancete()"><i class="bi bi-file-pdf"></i> Exportar PDF</button>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-borderless">
                        ${row('Receita Bruta', d.receitaBruta)}
                        ${row('(-) Deduções da Receita', d.deducoes, 'text-danger')}
                        ${row('(=) Receita Líquida', d.receitaLiquida, 'text-primary', true)}
                        <tr><td colspan="2"><hr class="my-2"></td></tr>
                        ${row('(-) Custo da Mercadoria Vendida (CMV)', d.custoMercadoria, 'text-danger')}
                        ${row('(=) Lucro Bruto', d.lucroBruto, '', true)}
                        <tr><td colspan="2"><hr class="my-2"></td></tr>
                        ${row('(-) Despesas Operacionais', d.despesasOperacionais, 'text-danger')}
                        ${row('(=) Lucro Operacional', d.lucroOperacional, '', true)}
                        <tr><td colspan="2"><hr class="my-2"></td></tr>
                        ${row('(-) Provisão IRPJ (15%)', d.irpj, 'text-danger')}
                        ${row('(-) Provisão CSLL (9%)', d.csll, 'text-danger')}
                        ${row('(=) Lucro Líquido do Exercício', d.lucroLiquido, 'text-success', true)}
                    </table>
                </div>
            </div>
        </div>
    `;
}

async function renderRazao() {
    const contas = await api('/api/contabil/contas');
    $('#tab-razao').innerHTML = `
        <div class="card mb-4">
            <div class="card-body">
                <div class="row g-2 align-items-end">
                    <div class="col-md-6">
                        <label class="form-label">Selecione a Conta</label>
                        <select id="rConta" class="form-select">
                            ${contas.map(c => `<option value="${c.id}">${c.codigo} - ${c.descricao}</option>`).join('')}
                        </select>
                    </div>
                    <div class="col-md-2"><button class="btn btn-primary w-100" onclick="verRazao()"><i class="bi bi-eye"></i> Consultar</button></div>
                    <div class="col-md-2"><button class="btn btn-outline-primary w-100" onclick="baixarRazao()"><i class="bi bi-file-pdf"></i> PDF</button></div>
                </div>
            </div>
        </div>
        <div id="razaoResultado"></div>
    `;
}

window.verRazao = async () => {
    const contaId = $('#rConta').value;
    const itens = await api(`/api/razao/analitico/${contaId}?ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#razaoResultado').innerHTML = `
        <div class="card"><div class="card-body p-0"><div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead class="table-light"><tr><th>Data</th><th>Histórico</th><th>Doc</th><th class="text-end">Débito</th><th class="text-end">Crédito</th><th class="text-end">Saldo</th></tr></thead>
                <tbody>
                    ${itens.map(i => `<tr>
                        <td>${i.data}</td><td>${i.historico}</td><td><small>${i.documento || '-'}</small></td>
                        <td class="text-end text-danger">${i.debito > 0 ? fmt(i.debito) : ''}</td>
                        <td class="text-end text-success">${i.credito > 0 ? fmt(i.credito) : ''}</td>
                        <td class="text-end fw-bold">${fmt(i.saldo)}</td>
                    </tr>`).join('')}
                </tbody>
            </table>
        </div></div></div>
    `;
};

window.baixarRazao = async () => {
    const r = await api(`/api/relatorios/razao/pdf/${$('#rConta').value}?ini=${periodo.ini}&fim=${periodo.fim}`, { isBlob: true });
    const blob = await r.blob();
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'livro_razao.pdf';
    a.click();
};

async function renderBalancete() {
    const itens = await api(`/api/razao/balancete?ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#tab-balancete').innerHTML = `
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="bi bi-table"></i> Balancete Analítico</h5>
                <button class="btn btn-primary btn-sm" onclick="baixarBalancete()"><i class="bi bi-file-pdf"></i> Exportar PDF</button>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead class="table-light"><tr><th>Código</th><th>Descrição</th><th class="text-end">Débitos</th><th class="text-end">Créditos</th><th class="text-end">Saldo D</th><th class="text-end">Saldo C</th></tr></thead>
                        <tbody>
                            ${itens.map(i => `<tr>
                                <td><code>${i.codigo}</code></td><td>${i.descricao}</td>
                                <td class="text-end">${fmtNum(i.totalDeb)}</td><td class="text-end">${fmtNum(i.totalCred)}</td>
                                <td class="text-end text-success">${i.saldo >= 0 ? fmtNum(i.saldo) : ''}</td>
                                <td class="text-end text-danger">${i.saldo < 0 ? fmtNum(Math.abs(i.saldo)) : ''}</td>
                            </tr>`).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    `;
}

window.baixarBalancete = async () => {
    const r = await api(`/api/relatorios/balancete/pdf?ini=${periodo.ini}&fim=${periodo.fim}`, { isBlob: true });
    const blob = await r.blob();
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'balancete_analitico.pdf';
    a.click();
};

async function renderFechamento() {
    const fech = await api('/api/fechamento');
    $('#tab-fechamento').innerHTML = `
        <div class="card mb-4">
            <div class="card-header"><h5 class="mb-0"><i class="bi bi-calendar-check"></i> Novo Fechamento Mensal</h5></div>
            <div class="card-body">
                <div class="row g-2 align-items-end">
                    <div class="col-md-3"><input id="fAno" type="number" class="form-control" value="${new Date().getFullYear()}"></div>
                    <div class="col-md-3">
                        <select id="fMes" class="form-select">
                            ${[1,2,3,4,5,6,7,8,9,10,11,12].map(m => `<option value="${m}" ${m === new Date().getMonth() + 1 ? 'selected' : ''}>${String(m).padStart(2, '0')}</option>`).join('')}
                        </select>
                    </div>
                    <div class="col-md-3"><button class="btn btn-primary w-100" onclick="fechar()"><i class="bi bi-lock"></i> Fechar Período</button></div>
                </div>
            </div>
        </div>
        <div class="card"><div class="card-body p-0"><div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead class="table-light"><tr><th>Ano</th><th>Mês</th><th>Data Fechamento</th><th>Usuário</th><th>Status</th><th class="text-end">Ações</th></tr></thead>
                <tbody>
                    ${fech.map(f => `<tr>
                        <td>${f.ano}</td><td>${String(f.mes).padStart(2, '0')}</td><td>${f.dataFechamento}</td><td>${f.usuario}</td>
                        <td>${f.estornado ? '<span class="badge bg-warning text-dark">Estornado</span>' : '<span class="badge bg-success">Fechado</span>'}</td>
                        <td class="text-end">
                            ${!f.estornado ? `<button class="btn btn-sm btn-outline-danger" onclick="estornar(${f.ano}, ${f.mes})"><i class="bi bi-arrow-counterclockwise"></i> Estornar</button>` : '-'}
                        </td>
                    </tr>`).join('')}
                </tbody>
            </table>
        </div></div></div>
    `;
}

window.fechar = async () => {
    if (!confirm('Deseja realmente fechar este período? Lançamentos de resultado serão estornados automaticamente.')) return;
    try {
        await api(`/api/fechamento/fechar?ano=${$('#fAno').value}&mes=${$('#fMes').value}`, { method: 'POST' });
        showToast('success', 'Período fechado com sucesso!');
        renderFechamento();
    } catch (e) { showToast('error', e.message); }
};

window.estornar = async (ano, mes) => {
    if (!confirm('Estornar o fechamento? Os lançamentos de encerramento serão removidos.')) return;
    try {
        await api(`/api/fechamento/estornar?ano=${ano}&mes=${mes}`, { method: 'POST' });
        showToast('success', 'Fechamento estornado!');
        renderFechamento();
    } catch (e) { showToast('error', e.message); }
};

async function renderSped() {
    $('#tab-sped').innerHTML = `
        <div class="card" style="max-width: 600px; margin: 0 auto;">
            <div class="card-body text-center py-5">
                <i class="bi bi-file-earmark-text" style="font-size: 4rem; color: var(--primary);"></i>
                <h4 class="mt-3">Exportar SPED Fiscal (EFD ICMS/IPI)</h4>
                <p class="text-muted mb-4">Gera o arquivo texto no layout oficial do Guia Prático da Receita Federal.</p>
                <div class="row g-2 justify-content-center mb-3">
                    <div class="col-auto"><input type="date" id="spedIni" class="form-control" value="${periodo.ini}"></div>
                    <div class="col-auto"><input type="date" id="spedFim" class="form-control" value="${periodo.fim}"></div>
                </div>
                <button class="btn btn-primary btn-lg" onclick="baixarSped()"><i class="bi bi-download"></i> Gerar e Baixar Arquivo .TXT</button>
            </div>
        </div>
    `;
}

window.baixarSped = async () => {
    const r = await api(`/api/sped/efd?ini=${$('#spedIni').value}&fim=${$('#spedFim').value}`, { isBlob: true });
    const blob = await r.blob();
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `sped_efd_${$('#spedIni').value}_${$('#spedFim').value}.txt`;
    a.click();
    showToast('success', 'Arquivo SPED gerado com sucesso!');
};

async function renderNfe() {
    const nfs = await api(`/api/fiscal/nf?ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#tab-nfe').innerHTML = `
        <div class="card">
            <div class="card-header"><h5 class="mb-0"><i class="bi bi-file-earmark-code"></i> Gestão de NF-e</h5></div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead class="table-light"><tr><th>Nº</th><th>Data</th><th>Destinatário</th><th class="text-end">Valor Total</th><th>Status</th><th class="text-end">Ações</th></tr></thead>
                        <tbody>
                            ${nfs.map(n => `<tr>
                                <td>${n.numero}</td><td>${n.dataEmissao}</td><td>${n.destinatario || '-'}</td>
                                <td class="text-end fw-medium">${fmt(n.valorProdutos)}</td>
                                <td><span class="badge-status badge-${n.status === 'ASSINADA' ? 'ass' : n.status === 'AUTORIZADA' ? 'aut' : 'dig'}">${n.status || 'DIGITADA'}</span></td>
                                <td class="text-end">
                                    ${n.status !== 'ASSINADA' ? `<button class="btn btn-sm btn-outline-primary me-1" onclick="assinarNfe(${n.id})"><i class="bi bi-pen"></i> Assinar</button>` : ''}
                                    <button class="btn btn-sm btn-outline-secondary" onclick="verXml(${n.id})"><i class="bi bi-code"></i> XML</button>
                                </td>
                            </tr>`).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    `;
}

window.assinarNfe = async (id) => {
    try {
        await api(`/api/nfe/gerar/${id}`, { method: 'POST' });
        showToast('success', 'NF-e assinada digitalmente com sucesso!');
        renderNfe();
    } catch (e) { showToast('error', e.message); }
};

window.verXml = async (id) => {
    const r = await api(`/api/nfe/xml/${id}`);
    const xml = await r.text();
    const w = window.open();
    w.document.write('<pre style="padding: 20px; font-family: monospace;">' + xml + '</pre>');
};

async function renderAuditoria() {
    // Simulação: busca os últimos lançamentos como proxy de auditoria visual
    const ini = new Date(); ini.setDate(ini.getDate() - 30);
    const r = await api(`/api/contabil/lancamentos?ini=${ini.toISOString().split('T')[0]}&fim=${periodo.fim}`);
    const lcs = await r.json();
    
    $('#tab-auditoria').innerHTML = `
        <div class="card">
            <div class="card-header"><h5 class="mb-0"><i class="bi bi-shield-check"></i> Trilha de Auditoria do Sistema</h5></div>
            <div class="card-body">
                <div class="alert alert-info d-flex align-items-center">
                    <i class="bi bi-info-circle-fill me-2 fs-4"></i>
                    <div>Todos os eventos de criação, atualização e exclusão são registrados automaticamente na tabela <code>audit_log</code> via <code>@EntityListeners</code>.</div>
                </div>
                <h6 class="mt-4 mb-3">Últimas Atividades Registradas</h6>
                <div class="table-responsive">
                    <table class="table table-sm table-hover">
                        <thead class="table-light"><tr><th>ID</th><th>Data</th><th>Histórico</th><th>Documento</th></tr></thead>
                        <tbody>
                            ${lcs.slice(-15).reverse().map(l => `<tr>
                                <td><span class="badge bg-secondary">#${l.id}</span></td>
                                <td>${l.data}</td>
                                <td>${l.historico}</td>
                                <td><small class="text-muted">${l.documento || '-'}</small></td>
                            </tr>`).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    `;
}

async function renderUsuarios() {
    $('#tab-usuarios').innerHTML = `
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="bi bi-people"></i> Gestão de Usuários</h5>
                <button class="btn btn-primary btn-sm" data-bs-toggle="modal" data-bs-target="#modalUsuario"><i class="bi bi-plus-lg"></i> Novo Usuário</button>
            </div>
            <div class="card-body">
                <div class="alert alert-info">
                    <strong>Usuário Atual:</strong> ${user.nome} (${user.perfil})<br>
                    <small class="text-muted">ID: ${user.usuarioId} | Empresa ID: ${user.empresaId}</small>
                </div>
                <p class="text-muted">Utilize o modal acima para criar novos usuários com perfis de ADMIN, CONTADOR ou FINANCEIRO.</p>
            </div>
        </div>
    `;
}

window.salvarUsuario = async () => {
    try {
        await api('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify({
                nome: $('#uNome').value,
                login: $('#uLogin').value,
                senhaHash: $('#uSenha').value,
                email: $('#uEmail').value,
                perfil: $('#uPerfil').value,
                empresaId: user.empresaId
            })
        });
        showToast('success', 'Usuário criado com sucesso!');
        bootstrap.Modal.getInstance($('#modalUsuario')).hide();
        renderUsuarios();
    } catch (e) { showToast('error', e.message); }
};

window.logout = () => {
    if (confirm('Deseja realmente sair do sistema?')) {
        localStorage.clear();
        window.location.href = '/login.html';
    }
};

// =========================================
// 5. INICIALIZAÇÃO
// =========================================
document.addEventListener('DOMContentLoaded', () => {
    // Event listeners para navegação
    $$('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            render(link.dataset.tab);
        });
    });

    // Event listeners para filtros de data
    $('#dtIni').addEventListener('change', refresh);
    $('#dtFim').addEventListener('change', refresh);

    // Renderiza dashboard inicial
    render('dashboard');
});