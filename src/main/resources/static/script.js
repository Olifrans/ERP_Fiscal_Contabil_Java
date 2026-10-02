const API = '';
const $ = (s) => document.querySelector(s);
const $$ = (s) => document.querySelectorAll(s);
const fmt = (v) => new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(v||0);

let empresaId = 1;
let periodo = { ini: '2026-01-01', fim: '2026-10-03' };

async function api(path, opts={}) {
    const r = await fetch(API + path, {
        headers:{'Content-Type':'application/json'}, ...opts
    });
    if (!r.ok) throw new Error(await r.text());
    return r.json();
}

async function carregarEmpresas() {
    const emp = await api('/api/fiscal/empresas');
    const sel = $('#selEmpresa');
    sel.innerHTML = emp.map(e => `<option value="${e.id}">${e.razaoSocial}</option>`).join('');
    sel.onchange = () => { empresaId = +sel.value; refresh(); };
    empresaId = emp[0]?.id || 1;
    sel.value = empresaId;
}

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
    $('#pageTitle').textContent = {
        dashboard:'Dashboard', empresas:'Empresas', plano:'Plano de Contas',
        lancamentos:'Lançamentos Contábeis', nf:'Notas Fiscais',
        fiscal:'Apuração Fiscal', dre:'DRE'
    }[tab];
    const fn = { dashboard:renderDashboard, empresas:renderEmpresas,
        plano:renderPlano, lancamentos:renderLancamentos, nf:renderNf,
        fiscal:renderFiscal, dre:renderDre }[tab];
    if (fn) await fn();
}

async function renderDashboard() {
    const dre = await api(`/api/contabil/dre?empresaId=${empresaId}&ini=${periodo.ini}&fim=${periodo.fim}`);
    const ap = await api(`/api/fiscal/apuracao?empresaId=${empresaId}&ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#tab-dashboard').innerHTML = `
        <div class="grid-kpi">
            <div class="card-kpi"><div class="label">Receita Líquida</div><div class="value text-success">${fmt(dre.receitaLiquida)}</div></div>
            <div class="card-kpi"><div class="label">Lucro Operacional</div><div class="value">${fmt(dre.lucroOperacional)}</div></div>
            <div class="card-kpi"><div class="label">Lucro Líquido</div><div class="value text-primary">${fmt(dre.lucroLiquido)}</div></div>
            <div class="card-kpi"><div class="label">ICMS a Recolher</div><div class="value text-danger">${fmt(ap.saldoIcms)}</div></div>
            <div class="card-kpi"><div class="label">PIS</div><div class="value">${fmt(ap.pis)}</div></div>
            <div class="card-kpi"><div class="label">COFINS</div><div class="value">${fmt(ap.cofins)}</div></div>
        </div>
        <div class="row g-3">
            <div class="col-md-6"><div class="card-kpi"><canvas id="chartRec"></canvas></div></div>
            <div class="col-md-6"><div class="card-kpi"><canvas id="chartImp"></canvas></div></div>
        </div>`;
    new Chart($('#chartRec'), {
        type:'doughnut',
        data:{ labels:Object.keys(dre.detalheReceitas||{}), datasets:[{data:Object.values(dre.detalheReceitas||{})}] }
    });
    new Chart($('#chartImp'), {
        type:'bar',
        data:{ labels:['ICMS','PIS','COFINS','IRPJ','CSLL'],
               datasets:[{label:'Impostos', data:[ap.saldoIcms, ap.pis, ap.cofins, dre.irpj, dre.csll],
                          backgroundColor:['#ef4444','#f59e0b','#3b82f6','#8b5cf6','#10b981']}] }
    });
}

async function renderEmpresas() {
    const emp = await api('/api/fiscal/empresas');
    $('#tab-empresas').innerHTML = `
        <div class="card-kpi mb-3">
            <h6>Nova Empresa</h6>
            <div class="row g-2">
                <input id="eRazao" class="form-control col" placeholder="Razão Social">
                <input id="eCnpj" class="form-control col" placeholder="CNPJ">
                <select id="eRegime" class="form-select col">
                    <option>SIMPLES</option><option>LUCRO_PRESUMIDO</option><option>LUCRO_REAL</option>
                </select>
                <button class="btn btn-primary col-auto" onclick="salvarEmpresa()">Salvar</button>
            </div>
        </div>
        <table class="table"><thead><tr><th>CNPJ</th><th>Razão Social</th><th>Regime</th></tr></thead>
        <tbody>${emp.map(e=>`<tr><td>${e.cnpj}</td><td>${e.razaoSocial}</td><td>${e.regimeTributario}</td></tr>`).join('')}</tbody></table>`;
}
window.salvarEmpresa = async () => {
    await api('/api/fiscal/empresas', { method:'POST', body: JSON.stringify({
        razaoSocial: $('#eRazao').value, cnpj: $('#eCnpj').value, regimeTributario: $('#eRegime').value
    })});
    renderEmpresas(); carregarEmpresas();
};

async function renderPlano() {
    const contas = await api('/api/contabil/contas');
    $('#tab-plano').innerHTML = `
        <table class="table"><thead><tr><th>Código</th><th>Descrição</th><th>Natureza</th><th>Grupo</th></tr></thead>
        <tbody>${contas.map(c=>`<tr><td><code>${c.codigo}</code></td><td>${c.descricao}</td><td>${c.natureza}</td><td>${c.grupo}</td></tr>`).join('')}</tbody></table>`;
}

async function renderLancamentos() {
    const lcs = await api(`/api/contabil/lancamentos?empresaId=${empresaId}&ini=${periodo.ini}&fim=${periodo.fim}`);
    const contas = await api('/api/contabil/contas');
    $('#tab-lancamentos').innerHTML = `
        <div class="card-kpi mb-3">
            <h6>Novo Lançamento (Partidas Dobradas)</h6>
            <div class="row g-2 mb-2">
                <input type="date" id="lData" class="form-control col">
                <input id="lHist" class="form-control col" placeholder="Histórico">
                <input id="lDoc" class="form-control col" placeholder="Documento">
            </div>
            <div id="lItens"></div>
            <button class="btn btn-sm btn-outline-secondary mt-2" onclick="addItem()">+ Item</button>
            <button class="btn btn-primary mt-2 float-end" onclick="salvarLanc()">Lançar</button>
        </div>
        <table class="table"><thead><tr><th>Data</th><th>Histórico</th><th>Déb</th><th>Créd</th></tr></thead>
        <tbody>${lcs.map(l=>{
            const d = l.itens.filter(i=>i.tipo==='DEBITO').reduce((s,i)=>s+i.valor,0);
            const c = l.itens.filter(i=>i.tipo==='CREDITO').reduce((s,i)=>s+i.valor,0);
            return `<tr><td>${l.data}</td><td>${l.historico}</td><td>${fmt(d)}</td><td>${fmt(c)}</td></tr>`;
        }).join('')}</tbody></table>`;
    window._contas = contas; addItem();
}
let _itemIdx = 0;
window.addItem = () => {
    const opts = _contas.map(c=>`<option value="${c.id}">${c.codigo} - ${c.descricao}</option>`).join('');
    $('#lItens').insertAdjacentHTML('beforeend', `
        <div class="row g-2 mb-1 item-lanc">
            <select class="form-select col conta">${opts}</select>
            <select class="form-select col tipo"><option>DEBITO</option><option>CREDITO</option></select>
            <input type="number" step="0.01" class="form-control col valor" placeholder="Valor">
            <button class="btn btn-outline-danger col-auto" onclick="this.parentElement.remove()">×</button>
        </div>`);
};
window.salvarLanc = async () => {
    const itens = [...$$('.item-lanc')].map(el => ({
        contaId: +el.querySelector('.conta').value,
        tipo: el.querySelector('.tipo').value,
        valor: +el.querySelector('.valor').value
    }));
    await api('/api/contabil/lancamentos', { method:'POST', body: JSON.stringify({
        empresaId, data: $('#lData').value, historico: $('#lHist').value,
        documento: $('#lDoc').value, itens
    })});
    renderLancamentos();
};

async function renderNf() {
    const ap = await api(`/api/fiscal/apuracao?empresaId=${empresaId}&ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#tab-nf').innerHTML = `
        <div class="card-kpi mb-3">
            <h6>Nova Nota Fiscal</h6>
            <div class="row g-2">
                <input id="nfNum" class="form-control col" placeholder="Número">
                <input type="date" id="nfData" class="form-control col">
                <select id="nfTipo" class="form-select col"><option>SAIDA</option><option>ENTRADA</option></select>
                <input id="nfCfop" class="form-control col" placeholder="CFOP">
                <input id="nfProd" type="number" step="0.01" class="form-control col" placeholder="Valor Produtos">
                <input id="nfIcms" type="number" step="0.01" class="form-control col" placeholder="Valor ICMS">
                <button class="btn btn-primary col-auto" onclick="salvarNf()">Salvar</button>
            </div>
        </div>
        <table class="table"><thead><tr><th>Nº</th><th>Data</th><th>Tipo</th><th>CFOP</th><th>Produtos</th><th>ICMS</th></tr></thead>
        <tbody>${ap.notas.map(n=>`<tr><td>${n.numero}</td><td>${n.dataEmissao}</td><td>${n.tipo}</td><td>${n.cfop}</td><td>${fmt(n.valorProdutos)}</td><td>${fmt(n.valorIcms)}</td></tr>`).join('')}</tbody></table>`;
}
window.salvarNf = async () => {
    await api('/api/fiscal/nf', { method:'POST', body: JSON.stringify({
        empresa:{id:empresaId}, numero:+$('#nfNum').value, dataEmissao:$('#nfData').value,
        tipo:$('#nfTipo').value, cfop:$('#nfCfop').value,
        valorProdutos:+$('#nfProd').value, valorIcms:+$('#nfIcms').value
    })});
    renderNf();
};

async function renderFiscal() {
    const ap = await api(`/api/fiscal/apuracao?empresaId=${empresaId}&ini=${periodo.ini}&fim=${periodo.fim}`);
    $('#tab-fiscal').innerHTML = `
        <div class="grid-kpi">
            <div class="card-kpi"><div class="label">Débito ICMS</div><div class="value">${fmt(ap.debitoIcms)}</div></div>
            <div class="card-kpi"><div class="label">Crédito ICMS</div><div class="value">${fmt(ap.creditoIcms)}</div></div>
            <div class="card-kpi"><div class="label">Saldo ICMS</div><div class="value text-danger">${fmt(ap.saldoIcms)}</div></div>
            <div class="card-kpi"><div class="label">Base PIS/COFINS</div><div class="value">${fmt(ap.basePisCofins)}</div></div>
            <div class="card-kpi"><div class="label">PIS (1,65%)</div><div class="value">${fmt(ap.pis)}</div></div>
            <div class="card-kpi"><div class="label">COFINS (7,6%)</div><div class="value">${fmt(ap.cofins)}</div></div>
        </div>`;
}

async function renderDre() {
    const d = await api(`/api/contabil/dre?empresaId=${empresaId}&ini=${periodo.ini}&fim=${periodo.fim}`);
    const row = (l,v,cls='') => `<tr><td>${l}</td><td class="text-end ${cls}">${fmt(v)}</td></tr>`;
    $('#tab-dre').innerHTML = `
        <div class="card-kpi">
            <h5>Demonstração do Resultado do Exercício</h5>
            <table class="table">
                ${row('Receita Bruta', d.receitaBruta)}
                ${row('(-) Deduções', d.deducoes)}
                ${row('= Receita Líquida', d.receitaLiquida, 'fw-bold')}
                ${row('(-) CMV', d.custoMercadoria)}
                ${row('= Lucro Bruto', d.lucroBruto, 'fw-bold')}
                ${row('(-) Despesas Operacionais', d.despesasOperacionais)}
                ${row('= Lucro Operacional', d.lucroOperacional, 'fw-bold')}
                ${row('(-) IRPJ (15%)', d.irpj)}
                ${row('(-) CSLL (9%)', d.csll)}
                ${row('= Lucro Líquido', d.lucroLiquido, 'fw-bold text-primary')}
            </table>
        </div>`;
}

// navegação
$$('.sidebar a').forEach(a => a.onclick = () => {
    $$('.sidebar a').forEach(x => x.classList.remove('active'));
    a.classList.add('active');
    render(a.dataset.tab);
});

// init
(async () => {
    $('#dtIni').value = periodo.ini;
    $('#dtFim').value = periodo.fim;
    $('#dtIni').onchange = $('#dtFim').onchange = refresh;
    await carregarEmpresas();
    $('.sidebar a').classList.add('active');
    render('dashboard');
})();