const $ = (s) => document.querySelector(s);
const $$ = (s) => document.querySelectorAll(s);
const fmt = (v) => new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(v||0);
const fmtNum = (v) => new Intl.NumberFormat('pt-BR',{minimumFractionDigits:2,maximumFractionDigits:2}).format(v||0);

const token = localStorage.getItem('token');
const user = JSON.parse(localStorage.getItem('user') || '{}');
if (!token) location.href = '/login.html';

let periodo = { ini: '2026-01-01', fim: '2026-10-03' };

$('#userInfo').innerHTML = `<i class="bi bi-person-circle"></i> ${user.nome || 'Usuário'}<br><small>${user.perfil || ''}</small>`;

async function api(path, opts={}) {
    const r = await fetch(path, {
        headers: {
            'Content-Type':'application/json',
            'Authorization': 'Bearer ' + token
        }, ...opts
    });
    if (r.status === 401) { logout(); throw new Error('Sessão expirada'); }
    if (!r.ok) throw new Error(await r.text());
    return r;
}

async function apiJson(path, opts={}) { return (await api(path, opts)).json(); }

function setPeriodo() {
    periodo.ini = $('#dtIni').value;
    periodo.fim = $('#dtFim').value;
}

async function refresh() {
    setPeriodo();
    const active = document.querySelector('.sidebar a.active')?.dataset.tab || 'dashboard';
    await render(active);
}

async function render(tab) {
    $$('.tab').forEach(t => t.classList.add('d-none'));
    $(`#tab-${tab}`).classList.remove('d-none');
    const titles = {
        dashboard:'Dashboard', empresas:'Empresas', usuarios:'Usuários',
        plano:'Plano de Contas', lancamentos:'Lançamentos Contábeis',
        nf:'Notas Fiscais', fiscal:'Apuração Fiscal', dre:'DRE',
        razao:'Livro Razão', balancete:'Balancete Analítico',
        fechamento:'Fechamento Mensal', sped:'SPED Fiscal',
        nfe:'NF-e', auditoria:'Auditoria'
    };
    $('#pageTitle').textContent = titles[tab] || tab;
    const fn = { dashboard:renderDashboard, empresas:renderEmpresas, usuarios:renderUsuarios,
        plano:renderPlano, lancamentos:renderLancamentos, nf:renderNf,
        fiscal:renderFiscal, dre:renderDre, razao:renderRazao, balancete:renderBalancete,
        fechamento:renderFechamento, sped:renderSped, nfe:renderNfe, auditoria:renderAuditoria }[tab];
    if (fn) try { await fn(); } catch(e) { console.error(e); alert('Erro: ' + e.message); }
}

async function renderDashboard() {
    const [dre, ap] = await Promise.all([
        apiJson(`/api/contabil/dre?ini=${periodo.ini}&fim=${periodo.fim}`),
        apiJson(`/api/fiscal/apuracao?ini=${periodo.ini}&fim=${periodo.fim}`)
    ]);
    $('#tab-dashboard').innerHTML = `
        <div class="grid-kpi">
            <div class="card-kpi"><div class="label">Receita Bruta</div><div class="value text-primary">${fmt(dre.receitaBruta)}</div></div>
            <div class="card-kpi"><div class="label">Receita Líquida</div><div class="value text-success">${fmt(dre.receitaLiquida)}</div></div>
            <div class="card-kpi"><div class="label">Lucro Bruto</div><div class="value">${fmt(dre.lucroBruto)}</div></div>
            <div class="card-kpi"><div class="label">Lucro Operacional</div><div class="value">${fmt(dre.lucroOperacional)}</div></div>
            <div class="card-kpi"><div class="label">Lucro Líquido</div><div class="value text-primary">${fmt(dre.lucroLiquido)}</div></div>
            <div class="card-kpi"><div class="label">ICMS a Recolher</div><div class="value text-danger">${fmt(ap.saldoIcms)}</div></div>
            <div class="card-kpi"><div class="label">PIS</div><div class="value text-warning">${fmt(ap.pis)}</div></div>
            <div class="card-kpi"><div class="label">COFINS</div><div class="value text-warning">${fmt(ap.cofins)}</div></div>
        </div>
        <div class="row g-3">
            <div class="col-md-6"><div class="chart-box"><canvas id="chartRec" height="250"></canvas></div></div>
            <div class="col-md-6"><div class="chart-box"><canvas id="chartImp" height="250"></canvas></div></div>
        </div>`;
    new Chart($('#chartRec'), {
        type:'doughnut',
        data:{ labels:Object.keys(dre.detalheReceitas||{}), datasets:[{data:Object.values(dre.detalheReceitas||{}), backgroundColor:['#3b82f6','#10b981','#f59e0b','#ef4444','#8b5cf6']}] },
        options:{ plugins:{ legend:{ position:'bottom' } } }
    });
    new Chart($('#chartImp'), {
        type:'bar',
        data:{ labels:['ICMS','PIS','COFINS','IRPJ','CSLL'],
               datasets:[{label:'Impostos', data:[ap.saldoIcms, ap.pis, ap.cofins, dre.irpj, dre.csll],
                          backgroundColor:['#ef4444','#f59e0b','#3b82f6','#8b5cf6','#10b981']}] },
        options:{ plugins:{ legend:{ display:false } } }
    });
}

async function renderEmpresas() {
    const emp = await apiJson('/api/empresas');
    $('#tab-empresas').innerHTML = `
        <div class="card-kpi mb-3">
            <h6><i class="bi bi-plus-circle"></i> Nova Empresa</h6>
            <div class="row g-2">
                <input id="eRazao" class="form-control form-control-sm" placeholder="Razão Social">
                <input id="eCnpj" class="form-control form-control-sm" placeholder="CNPJ">
                <input id="eIE" class="form-control form-control-sm" placeholder="IE">
                <input id="eMun" class="form-control form-control-sm" placeholder="Município">
                <input id="eUF" class="form-control form-control-sm" placeholder="UF" maxlength="2">
                <select id="eRegime" class="form-select form-select-sm">
                    <option>SIMPLES</option><option>LUCRO_PRESUMIDO</option><option>LUCRO_REAL</option>
                </select>
                <button class="btn btn-primary btn-sm col-auto" onclick="salvarEmpresa()"><i class="bi bi-save"></i> Salvar</button>
            </div>
        </div>
        <table class="table"><thead><tr><th>CNPJ</th><th>Razão Social</th><th>IE</th><th>Município</th><th>UF</th><th>Regime</th></tr></thead>
        <tbody>${emp.map(e=>`<tr><td>${e.cnpj}</td><td>${e.razaoSocial}</td><td>${e.inscricaoEstadual||''}</td><td>${e.municipio||''}</td><td>${e.uf||''}</td><td>${e.regimeTributario||''}</td></tr>`).join('')}</tbody></table>`;
}
window.salvarEmpresa = async () => {
    await api('/api/empresas', { method:'POST', body: JSON.stringify({
        razaoSocial: $('#eRazao').value, cnpj: $('#eCnpj').value,
        inscricaoEstadual: $('#eIE').value, municipio: $('#eMun').value,
        uf: $('#eUF').value, regimeTributario: $('#eRegime').value
    })});
    renderEmpresas();
};

async function renderUsuarios() {
    const us = await apiJson('/api/empresas'); // placeholder
    $('#tab-usuarios').innerHTML = `
        <div class="card-kpi mb-3">
            <h6><i class="bi bi-person-plus"></i> Novo Usuário</h6>
            <div class="row g-2">
                <input id="uNome" class="form-control form-control-sm" placeholder="Nome">
                <input id="uLogin" class="form-control form-control-sm" placeholder="Login">
                <input id="uSenha" type="password" class="form-control form-control-sm" placeholder="Senha">
                <input id="uEmail" class="form-control form-control-sm" placeholder="Email">
                <select id="uPerfil" class="form-select form-select-sm">
                    <option>ADMIN</option><option>CONTADOR</option><option>FINANCEIRO</option>
                </select>
                <button class="btn btn-primary btn-sm col-auto" onclick="salvarUsuario()"><i class="bi bi-save"></i> Salvar</button>
            </div>
        </div>
        <div class="alert alert-info small">Usuário atual: <b>${user.nome}</b> (${user.perfil})</div>`;
}
window.salvarUsuario = async () => {
    await api('/api/auth/register', { method:'POST', body: JSON.stringify({
        nome: $('#uNome').value, login: $('#uLogin').value,
        senhaHash: $('#uSenha').value, email: $('#uEmail').value,
        perfil: $('#uPerfil').value, empresaId: user.empresaId
    })});
    alert('Usuário criado');
};

async function renderPlano() {
    const contas = await apiJson('/api/contabil/contas');
    $('#tab-plano').innerHTML = `
        <div class="card-kpi mb-3">
            <h6><i class="bi bi-plus-circle"></i> Nova Conta</h6>
            <div class="row g-2">
                <input id="cCodigo" class="form-control form-control-sm" placeholder="Código (ex: 1.1.04.001)">
                <input id="cDesc" class="form-control form-control-sm" placeholder="Descrição">
                <select id="cNat" class="form-select form-select-sm"><option>DEVEDORA</option><option>CREDORA</option></select>
                <select id="cGrupo" class="form-select form-select-sm">
                    <option>ATIVO</option><option>PASSIVO</option><option>RECEITA</option><option>DESPESA</option><option>RESULTADO</option>
                </select>
                <button class="btn btn-primary btn-sm col-auto" onclick="salvarConta()"><i class="bi bi-save"></i> Salvar</button>
            </div>
        </div>
        <table class="table"><thead><tr><th>Código</th><th>Descrição</th><th>Natureza</th><th>Grupo</th></tr></thead>
        <tbody>${contas.map(c=>`<tr><td><code>${c.codigo}</code></td><td>${c.descricao}</td><td>${c.natureza}</td><td><span class="badge bg-secondary">${c.grupo}</span></td></tr>`).join('')}</tbody></table>`;
}
window.salvarConta = async () => {
    await api('/api/contabil/contas', { method:'POST', body: JSON.stringify({
        codigo: $('#cCodigo').value, descricao: $('#cDesc').value,
        natureza: $('#cNat').value, grupo: $('#cGrupo').value
    })});
    renderPlano();
};

async function renderLancamentos() {
    const [lcs, contas] = await Promise.all([
        apiJson(`/api/contabil/lancamentos?ini=${periodo.ini}&fim=${periodo.fim}`),
        apiJson('/api/contabil/contas')
    ]);
    $('#tab-lancamentos').innerHTML = `
        <div class="card-kpi mb-3">
            <h6><i class="bi bi-journal-plus"></i> Novo Lançamento (Partidas Dobradas)</h6>
            <div class="row g-2 mb-2">
                <input type="date" id="lData" class="form-control form-control-sm">
                <input id="lHist" class="form-control form-control-sm" placeholder="Histórico">
                <input id="lDoc" class="form-control form-control-sm" placeholder="Documento">
            </div>
            <div id="lItens"></div>
            <button class="btn btn-sm btn-outline-secondary mt-2" onclick="addItem()"><i class="bi bi-plus"></i> Item</button>
            <button class="btn btn-primary btn-sm mt-2 float-end" onclick="salvarLanc()"><i class="bi bi-check"></i> Lançar</button>
        </div>
        <table class="table"><thead><tr><th>Data</th><th>Documento</th><th>Histórico</th><th class="text-end">Débito</th><th class="text-end">Crédito</th></tr></thead>
        <tbody>${lcs.map(l=>{
            const d = l.itens.filter(i=>i.tipo==='DEBITO').reduce((s,i)=>s+i.valor,0);
            const c = l.itens.filter(i=>i.tipo==='CREDITO').reduce((s,i)=>s+i.valor,0);
            return `<tr><td>${l.data}</td><td>${l.documento||''}</td><td>${l.historico}</td><td class="text-end">${fmt(d)}</td><td class="text-end">${fmt(c)}</td></tr>`;
        }).join('')}</tbody></table>`;
    window._contas = contas; addItem();
}
let _itemIdx = 0;
window.addItem = () => {
    const opts = _contas.map(c=>`<option value="${c.id}">${c.codigo} - ${c.descricao}</option>`).join('');
    $('#lItens').insertAdjacentHTML('beforeend', `
        <div class="row g-2 mb-1 item-lanc">
            <select class="form-select form-select-sm conta">${opts}</select>
            <select class="form-select form-select-sm tipo"><option>DEBITO</option><option>CREDITO</option></select>
            <input type="number" step="0.01" class="form-control form-control-sm valor" placeholder="Valor">
            <button class="btn btn-outline-danger btn-sm col-auto" onclick="this.parentElement.remove()">×</button>
        </div>`);
};
window.salvarLanc = async () => {
    const itens = [...$$('.item-lanc')].map(el => ({
        contaId: +el.querySelector('.conta').value,
        tipo: el.querySelector('.tipo').value,
        valor: +el.querySelector('.valor').value
    })).filter(i => i.valor > 0);
    if (itens.length < 2) { alert('Mínimo 2 itens'); return; }
    await api('/api/contabil/lancamentos', { method:'POST', body: JSON.stringify({
        data: $('#lData').value, historico: $('#lHist').value,
        documento: $('#lDoc').value, itens
    })});
    renderLancamentos();
};

async function renderNf() {
    const nfs = await apiJson(`/api/fiscal/nf?ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#tab-nf').innerHTML = `
        <div class="card-kpi mb-3">
            <h6><i class="bi bi-receipt-cutoff"></i> Nova Nota Fiscal</h6>
            <div class="row g-2">
                <input id="nfNum" class="form-control form-control-sm" placeholder="Número">
                <input type="date" id="nfData" class="form-control form-control-sm">
                <select id="nfTipo" class="form-select form-select-sm"><option>SAIDA</option><option>ENTRADA</option></select>
                <input id="nfCfop" class="form-control form-control-sm" placeholder="CFOP" value="5102">
                <input id="nfDest" class="form-control form-control-sm" placeholder="Destinatário">
                <input id="nfCnpjDest" class="form-control form-control-sm" placeholder="CNPJ Dest">
                <input id="nfProd" type="number" step="0.01" class="form-control form-control-sm" placeholder="Valor Produtos">
                <input id="nfIcms" type="number" step="0.01" class="form-control form-control-sm" placeholder="Valor ICMS">
                <button class="btn btn-primary btn-sm col-auto" onclick="salvarNf()"><i class="bi bi-save"></i> Salvar</button>
            </div>
        </div>
        <table class="table"><thead><tr><th>Nº</th><th>Data</th><th>Tipo</th><th>CFOP</th><th>Destinatário</th><th class="text-end">Produtos</th><th class="text-end">ICMS</th><th>Status</th></tr></thead>
        <tbody>${nfs.map(n=>`<tr>
            <td>${n.numero}</td><td>${n.dataEmissao}</td><td>${n.tipo}</td><td>${n.cfop||''}</td>
            <td>${n.destinatario||''}</td><td class="text-end">${fmt(n.valorProdutos)}</td>
            <td class="text-end">${fmt(n.valorIcms)}</td>
            <td><span class="badge-status badge-${n.status==='ASSINADA'?'ass':n.status==='AUTORIZADA'?'aut':'dig'}">${n.status||'DIGITADA'}</span></td>
        </tr>`).join('')}</tbody></table>`;
}
window.salvarNf = async () => {
    await api('/api/fiscal/nf', { method:'POST', body: JSON.stringify({
        numero:+$('#nfNum').value, dataEmissao:$('#nfData').value,
        tipo:$('#nfTipo').value, cfop:$('#nfCfop').value,
        destinatario:$('#nfDest').value, cnpjDestinatario:$('#nfCnpjDest').value,
        valorProdutos:+$('#nfProd').value, valorIcms:+$('#nfIcms').value,
        baseIcms:+$('#nfProd').value
    })});
    renderNf();
};

async function renderFiscal() {
    const ap = await apiJson(`/api/fiscal/apuracao?ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#tab-fiscal').innerHTML = `
        <div class="grid-kpi">
            <div class="card-kpi"><div class="label">Débito ICMS</div><div class="value">${fmt(ap.debitoIcms)}</div></div>
            <div class="card-kpi"><div class="label">Crédito ICMS</div><div class="value">${fmt(ap.creditoIcms)}</div></div>
            <div class="card-kpi"><div class="label">Saldo ICMS</div><div class="value text-danger">${fmt(ap.saldoIcms)}</div></div>
            <div class="card-kpi"><div class="label">Base PIS/COFINS</div><div class="value">${fmt(ap.basePisCofins)}</div></div>
            <div class="card-kpi"><div class="label">PIS (1,65%)</div><div class="value text-warning">${fmt(ap.pis)}</div></div>
            <div class="card-kpi"><div class="label">COFINS (7,6%)</div><div class="value text-warning">${fmt(ap.cofins)}</div></div>
        </div>
        <div class="card-kpi">
            <h6>Notas do Período</h6>
            <table class="table"><thead><tr><th>Nº</th><th>Data</th><th>Tipo</th><th class="text-end">Valor</th><th class="text-end">ICMS</th></tr></thead>
            <tbody>${ap.notas.map(n=>`<tr><td>${n.numero}</td><td>${n.dataEmissao}</td><td>${n.tipo}</td><td class="text-end">${fmt(n.valorProdutos)}</td><td class="text-end">${fmt(n.valorIcms)}</td></tr>`).join('')}</tbody></table>
        </div>`;
}

async function renderDre() {
    const d = await apiJson(`/api/contabil/dre?ini=${periodo.ini}&fim=${periodo.fim}`);
    const row = (l,v,cls='',bold=false) => `<tr><td class="${bold?'fw-bold':''}">${l}</td><td class="text-end ${cls} ${bold?'fw-bold':''}">${fmt(v)}</td></tr>`;
    $('#tab-dre').innerHTML = `
        <div class="card-kpi">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="m-0">Demonstração do Resultado do Exercício</h5>
                <button class="btn btn-sm btn-outline-primary" onclick="baixarBalancete()"><i class="bi bi-file-pdf"></i> PDF Balancete</button>
            </div>
            <table class="table">
                ${row('Receita Bruta', d.receitaBruta)}
                ${row('(-) Deduções', d.deducoes)}
                ${row('= Receita Líquida', d.receitaLiquida, 'text-primary', true)}
                ${row('(-) CMV', d.custoMercadoria)}
                ${row('= Lucro Bruto', d.lucroBruto, '', true)}
                ${row('(-) Despesas Operacionais', d.despesasOperacionais)}
                ${row('= Lucro Operacional', d.lucroOperacional, '', true)}
                ${row('(-) IRPJ (15%)', d.irpj)}
                ${row('(-) CSLL (9%)', d.csll)}
                ${row('= Lucro Líquido', d.lucroLiquido, 'text-success', true)}
            </table>
        </div>`;
}

async function renderRazao() {
    const contas = await apiJson('/api/contabil/contas');
    $('#tab-razao').innerHTML = `
        <div class="card-kpi mb-3">
            <div class="row g-2">
                <select id="rConta" class="form-select form-select-sm">
                    ${contas.map(c=>`<option value="${c.id}">${c.codigo} - ${c.descricao}</option>`).join('')}
                </select>
                <button class="btn btn-primary btn-sm col-auto" onclick="verRazao()"><i class="bi bi-eye"></i> Ver</button>
                <button class="btn btn-outline-primary btn-sm col-auto" onclick="baixarRazao()"><i class="bi bi-file-pdf"></i> PDF</button>
            </div>
        </div>
        <div id="razaoResultado"></div>`;
}
window.verRazao = async () => {
    const contaId = $('#rConta').value;
    const itens = await apiJson(`/api/razao/analitico/${contaId}?ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#razaoResultado').innerHTML = `
        <table class="table"><thead><tr><th>Data</th><th>Histórico</th><th>Doc</th><th class="text-end">Débito</th><th class="text-end">Crédito</th><th class="text-end">Saldo</th></tr></thead>
        <tbody>${itens.map(i=>`<tr><td>${i.data}</td><td>${i.historico}</td><td>${i.documento||''}</td><td class="text-end">${i.debito>0?fmt(i.debito):''}</td><td class="text-end">${i.credito>0?fmt(i.credito):''}</td><td class="text-end fw-bold">${fmt(i.saldo)}</td></tr>`).join('')}</tbody></table>`;
};
window.baixarRazao = async () => {
    const r = await api(`/api/relatorios/razao/pdf/${$('#rConta').value}?ini=${periodo.ini}&fim=${periodo.fim}`);
    const blob = await r.blob();
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'razao.pdf';
    a.click();
};

async function renderBalancete() {
    const itens = await apiJson(`/api/razao/balancete?ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#tab-balancete').innerHTML = `
        <div class="card-kpi">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="m-0">Balancete Analítico</h5>
                <button class="btn btn-primary btn-sm" onclick="baixarBalancete()"><i class="bi bi-file-pdf"></i> Exportar PDF</button>
            </div>
            <table class="table"><thead><tr><th>Código</th><th>Descrição</th><th class="text-end">Débitos</th><th class="text-end">Créditos</th><th class="text-end">Saldo D</th><th class="text-end">Saldo C</th></tr></thead>
            <tbody>${itens.map(i=>`<tr><td><code>${i.codigo}</code></td><td>${i.descricao}</td><td class="text-end">${fmtNum(i.totalDeb)}</td><td class="text-end">${fmtNum(i.totalCred)}</td><td class="text-end">${i.saldo>=0?fmtNum(i.saldo):''}</td><td class="text-end">${i.saldo<0?fmtNum(Math.abs(i.saldo)):''}</td></tr>`).join('')}</tbody></table>
        </div>`;
}
window.baixarBalancete = async () => {
    const r = await api(`/api/relatorios/balancete/pdf?ini=${periodo.ini}&fim=${periodo.fim}`);
    const blob = await r.blob();
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'balancete.pdf';
    a.click();
};

async function renderFechamento() {
    const fech = await apiJson('/api/fechamento');
    $('#tab-fechamento').innerHTML = `
        <div class="card-kpi mb-3">
            <h6><i class="bi bi-calendar-check"></i> Novo Fechamento</h6>
            <div class="row g-2">
                <input id="fAno" type="number" class="form-control form-control-sm" value="2026">
                <select id="fMes" class="form-select form-select-sm">
                    ${[1,2,3,4,5,6,7,8,9,10,11,12].map(m=>`<option value="${m}">${m}</option>`).join('')}
                </select>
                <button class="btn btn-primary btn-sm col-auto" onclick="fechar()"><i class="bi bi-lock"></i> Fechar</button>
            </div>
        </div>
        <table class="table"><thead><tr><th>Ano</th><th>Mês</th><th>Data</th><th>Usuário</th><th>Status</th><th>Ação</th></tr></thead>
        <tbody>${fech.map(f=>`<tr>
            <td>${f.ano}</td><td>${String(f.mes).padStart(2,'0')}</td><td>${f.dataFechamento}</td><td>${f.usuario}</td>
            <td>${f.estornado?'<span class="badge bg-warning">Estornado</span>':'<span class="badge bg-success">Fechado</span>'}</td>
            <td>${!f.estornado?`<button class="btn btn-sm btn-outline-danger" onclick="estornar(${f.ano},${f.mes})"><i class="bi bi-arrow-counterclockwise"></i> Estornar</button>`:''}</td>
        </tr>`).join('')}</tbody></table>`;
}
window.fechar = async () => {
    try {
        await api(`/api/fechamento/fechar?ano=${$('#fAno').value}&mes=${$('#fMes').value}`, { method:'POST' });
        renderFechamento();
    } catch(e) { alert(e.message); }
};
window.estornar = async (ano, mes) => {
    if (!confirm('Estornar fechamento?')) return;
    await api(`/api/fechamento/estornar?ano=${ano}&mes=${mes}`, { method:'POST' });
    renderFechamento();
};

async function renderSped() {
    $('#tab-sped').innerHTML = `
        <div class="card-kpi">
            <h5><i class="bi bi-file-earmark-text"></i> SPED Fiscal (EFD ICMS/IPI)</h5>
            <p class="text-muted small">Gera arquivo texto no layout do Guia Prático da Receita Federal.</p>
            <div class="row g-2">
                <input type="date" id="spedIni" class="form-control form-control-sm" value="${periodo.ini}">
                <input type="date" id="spedFim" class="form-control form-control-sm" value="${periodo.fim}">
                <button class="btn btn-primary btn-sm col-auto" onclick="baixarSped()"><i class="bi bi-download"></i> Gerar e Baixar SPED</button>
            </div>
        </div>`;
}
window.baixarSped = async () => {
    const r = await api(`/api/sped/efd?ini=${$('#spedIni').value}&fim=${$('#spedFim').value}`);
    const blob = await r.blob();
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `sped_efd_${$('#spedIni').value}_${$('#spedFim').value}.txt`;
    a.click();
};

async function renderNfe() {
    const nfs = await apiJson(`/api/fiscal/nf?ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#tab-nfe').innerHTML = `
        <div class="card-kpi">
            <h5><i class="bi bi-file-earmark-code"></i> NF-e Eletrônica</h5>
            <p class="text-muted small">Gera XML, assina digitalmente com certificado A1 e disponibiliza para transmissão.</p>
            <table class="table"><thead><tr><th>Nº</th><th>Data</th><th>Destinatário</th><th class="text-end">Valor</th><th>Status</th><th>Ações</th></tr></thead>
            <tbody>${nfs.map(n=>`<tr>
                <td>${n.numero}</td><td>${n.dataEmissao}</td><td>${n.destinatario||''}</td>
                <td class="text-end">${fmt(n.valorProdutos)}</td>
                <td><span class="badge-status badge-${n.status==='ASSINADA'?'ass':n.status==='AUTORIZADA'?'aut':'dig'}">${n.status||'DIGITADA'}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="assinarNfe(${n.id})"><i class="bi bi-pen"></i> Assinar</button>
                    <button class="btn btn-sm btn-outline-secondary" onclick="verXml(${n.id})"><i class="bi bi-code"></i> XML</button>
                </td>
            </tr>`).join('')}</tbody></table>
        </div>`;
}
window.assinarNfe = async (id) => {
    try {
        const r = await api(`/api/nfe/gerar/${id}`, { method:'POST' });
        const xml = await r.text();
        alert('NF-e assinada com sucesso!');
        renderNfe();
    } catch(e) { alert('Erro: ' + e.message); }
};
window.verXml = async (id) => {
    const r = await api(`/api/nfe/xml/${id}`);
    const xml = await r.text();
    const w = window.open();
    w.document.write('<pre>' + xml + '</pre>');
};

async function renderAuditoria() {
    // Busca últimos 30 dias
    const ini = new Date(); ini.setDate(ini.getDate() - 30);
    const r = await api(`/api/contabil/lancamentos?ini=${ini.toISOString().split('T')[0]}&fim=${periodo.fim}`);
    $('#tab-auditoria').innerHTML = `
        <div class="card-kpi">
            <h5><i class="bi bi-shield-check"></i> Trilha de Auditoria</h5>
            <p class="text-muted small">Todos os eventos de criação/atualização/exclusão são registrados automaticamente via @EntityListeners.</p>
            <div class="alert alert-info small">
                <b>Nota:</b> Os logs completos estão na tabela <code>audit_log</code> do banco de dados.
                Para visualização em produção, integre com Elasticsearch/Graylog.
            </div>
            <h6>Últimos Lançamentos (com auditoria ativa)</h6>
            <table class="table"><thead><tr><th>ID</th><th>Data</th><th>Histórico</th><th>Documento</th></tr></thead>
            <tbody>${(await r.json()).slice(-20).reverse().map(l=>`<tr><td>#${l.id}</td><td>${l.data}</td><td>${l.historico}</td><td>${l.documento||''}</td></tr>`).join('')}</tbody></table>
        </div>`;
}

// Navegação
$$('.sidebar a[data-tab]').forEach(a => a.onclick = () => {
    $$('.sidebar a').forEach(x => x.classList.remove('active'));
    a.classList.add('active');
    render(a.dataset.tab);
});

window.logout = () => {
    localStorage.clear();
    location.href = '/login.html';
};

// Init
(async () => {
    $('#dtIni').value = periodo.ini;
    $('#dtFim').value = periodo.fim;
    $('#dtIni').onchange = $('#dtFim').onchange = refresh;
    render('dashboard');
})();