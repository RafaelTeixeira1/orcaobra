/* OrçaObra — front-end simples (sem framework) que consome a API REST. */
'use strict';

const app = document.getElementById('app');
const STATUS_ACAO = {
  ENVIADO: 'Marcar como enviado', APROVADO: 'Cliente aprovou', RECUSADO: 'Cliente recusou',
  RASCUNHO: 'Voltar para rascunho', EM_EXECUCAO: 'Iniciar execução', CONCLUIDO: 'Concluir obra',
  CANCELADO: 'Cancelar',
};
const CATEGORIAS = { MATERIAL: 'Material', MAO_DE_OBRA: 'Mão de obra', OUTROS: 'Outros' };

// ------------------------------------------------------------------ utilitários
const brl = (v) => Number(v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const num = (v, c = 2) => Number(v ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: c, maximumFractionDigits: c });
const pct = (v) => `${num(v)}%`;
const data = (iso) => iso ? iso.split('-').reverse().join('/') : '';
const esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

async function api(caminho, opcoes = {}) {
  const resp = await fetch(`/api${caminho}`, {
    headers: { 'Content-Type': 'application/json' },
    ...opcoes,
    body: opcoes.body ? JSON.stringify(opcoes.body) : undefined,
  });
  if (!resp.ok) {
    let msg = `Erro ${resp.status}`;
    try { const p = await resp.json(); msg = p.detail || p.title || msg; } catch (_) { /* sem corpo */ }
    throw new Error(msg);
  }
  return resp.status === 204 ? null : resp.json();
}

function avisar(msg, erro = false) {
  const el = document.getElementById('aviso');
  el.textContent = msg;
  el.className = erro ? 'aviso erro' : 'aviso';
  el.hidden = false;
  clearTimeout(avisar.t);
  avisar.t = setTimeout(() => { el.hidden = true; }, 4500);
}

function dadosDoForm(form) {
  const obj = {};
  new FormData(form).forEach((v, k) => { if (v !== '') obj[k] = v; });
  return obj;
}

// ------------------------------------------------------------------ rotas
const rotas = [
  [/^#?\/?$/, telaLista],
  [/^#\/novo$/, telaNovo],
  [/^#\/orcamentos\/([\w-]+)$/, telaDetalhe],
  [/^#\/catalogo$/, telaCatalogo],
];

async function navegar() {
  const hash = location.hash || '#/';
  document.querySelectorAll('.topo nav a').forEach((a) => {
    const r = a.dataset.rota;
    a.classList.toggle('ativo', (r === 'catalogo' && hash.startsWith('#/catalogo')) || (r === 'lista' && !hash.startsWith('#/catalogo')));
  });
  for (const [regex, tela] of rotas) {
    const m = hash.match(regex);
    if (m) {
      try { await tela(...m.slice(1)); } catch (e) { app.innerHTML = `<div class="cartao vazio">${esc(e.message)}</div>`; }
      return;
    }
  }
  location.hash = '#/';
}
window.addEventListener('hashchange', navegar);
navegar();

// ------------------------------------------------------------------ lista
async function telaLista() {
  const lista = await api('/orcamentos');
  app.innerHTML = `
    <div class="linha-titulo">
      <div><h1>Orçamentos</h1><p class="sub">Orce casas e reformas, gere a proposta em PDF e acompanhe a margem da obra.</p></div>
      <a class="botao laranja" href="#/novo">+ Novo orçamento</a>
    </div>
    <div class="cartao">
      ${lista.length === 0 ? '<div class="vazio">Nenhum orçamento ainda. Crie o primeiro!</div>' : `
      <table>
        <thead><tr><th>Data</th><th>Cliente</th><th>Obra</th><th>Status</th><th class="num">Custo direto</th><th class="num">Preço de venda</th></tr></thead>
        <tbody>${lista.map((o) => `
          <tr class="clicavel" data-id="${o.id}">
            <td>${data(o.dataCriacao)}</td><td>${esc(o.cliente)}</td><td>${esc(o.obra)}</td>
            <td><span class="selo ${o.status}">${esc(o.statusDescricao)}</span></td>
            <td class="num">${brl(o.custoDireto)}</td><td class="num"><strong>${brl(o.precoDeVenda)}</strong></td>
          </tr>`).join('')}
        </tbody>
      </table>`}
    </div>`;
  app.querySelectorAll('tr[data-id]').forEach((tr) => tr.addEventListener('click', () => { location.hash = `#/orcamentos/${tr.dataset.id}`; }));
}

// ------------------------------------------------------------------ novo
function telaNovo() {
  app.innerHTML = `
    <h1>Novo orçamento</h1><p class="sub">Os dados do cliente e da obra aparecem na proposta.</p>
    <form id="form-novo">
      <div class="grade c2">
        <div class="cartao"><h2>Cliente</h2>
          <div class="campo"><label>Nome *</label><input name="clienteNome" required></div>
          <div class="grade c2">
            <div class="campo"><label>CPF/CNPJ</label><input name="clienteDocumento"></div>
            <div class="campo"><label>Telefone</label><input name="clienteTelefone"></div>
          </div>
          <div class="campo"><label>E-mail</label><input name="clienteEmail" type="email"></div>
        </div>
        <div class="cartao"><h2>Obra</h2>
          <div class="campo"><label>Descrição *</label><input name="obraDescricao" required placeholder="Casa térrea 2 quartos, 70 m²"></div>
          <div class="campo"><label>Endereço</label><input name="obraEndereco"></div>
          <div class="grade c2">
            <div class="campo"><label>Cidade</label><input name="obraCidade" value="Ceres-GO"></div>
            <div class="campo"><label>Área construída (m²)</label><input name="obraArea" type="number" step="0.01" min="0"></div>
          </div>
        </div>
      </div>
      <div class="cartao"><h2>Margem e validade</h2>
        <div class="grade c3">
          <div class="campo"><label>Margem mínima aceitável (%)</label><input name="margemMinima" type="number" step="0.1" value="5"></div>
          <div class="campo"><label>Validade da proposta (dias)</label><input name="validadeDias" type="number" value="15" min="1"></div>
          <div class="campo"><label>&nbsp;</label><p class="dica">O BDI começa com os valores de referência do TCU e pode ser ajustado depois.</p></div>
        </div>
      </div>
      <div class="acoes"><button type="submit">Criar orçamento</button><a class="botao sec" href="#/">Cancelar</a></div>
    </form>`;
  document.getElementById('form-novo').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const f = dadosDoForm(ev.target);
    try {
      const o = await api('/orcamentos', {
        method: 'POST',
        body: {
          cliente: { nome: f.clienteNome, documento: f.clienteDocumento, email: f.clienteEmail, telefone: f.clienteTelefone },
          obra: { descricao: f.obraDescricao, endereco: f.obraEndereco, cidade: f.obraCidade, areaConstruidaM2: f.obraArea },
          margemMinima: f.margemMinima, validadeDias: f.validadeDias,
        },
      });
      location.hash = `#/orcamentos/${o.id}`;
    } catch (e) { avisar(e.message, true); }
  });
}

// ------------------------------------------------------------------ detalhe
let abaAtual = 'servicos';

async function telaDetalhe(id) {
  const [o, tipos] = await Promise.all([api(`/orcamentos/${id}`), api('/servicos')]);
  const m = o.margem;
  const abaixo = m.abaixoDaMargemMinima;
  const temCustos = o.custosRealizados.length > 0 || o.permiteLancarCustos;
  if (abaAtual === 'acompanhamento' && !temCustos) abaAtual = 'servicos';

  app.innerHTML = `
    <div class="linha-titulo">
      <div>
        <h1>${esc(o.obra.descricao)} <span class="selo ${o.status}">${esc(o.statusDescricao)}</span></h1>
        <p class="sub">${esc(o.cliente.nome)} · ${esc(o.obra.cidade || '')} · criado em ${data(o.dataCriacao)}</p>
      </div>
      <div class="acoes">
        ${o.proximosStatus.map((s) => `<button class="${s === 'CANCELADO' || s === 'RECUSADO' ? 'perigo' : 'sec'}" data-status="${s}">${STATUS_ACAO[s]}</button>`).join('')}
      </div>
    </div>

    <div class="grade c4" style="margin-bottom:20px">
      <div class="kpi"><small>Custo direto</small><strong>${brl(m.custoDiretoPrevisto)}</strong>
        <div class="det">Materiais ${brl(m.previstoPorCategoria.MATERIAL)} · M.O. ${brl(m.previstoPorCategoria.MAO_DE_OBRA)}</div></div>
      <div class="kpi"><small>BDI</small><strong>${pct(m.bdi)}</strong><div class="det">Indiretos ${brl(m.custosIndiretos)} · Impostos ${brl(m.impostos)}</div></div>
      <div class="kpi destaque"><small>Preço de venda</small><strong>${brl(m.precoDeVenda)}</strong>
        <div class="det">${o.obra.areaConstruidaM2 ? `${brl(m.precoDeVenda / o.obra.areaConstruidaM2)} por m²` : 'informe a área para ver o R$/m²'}</div></div>
      <div class="kpi ${abaixo ? 'alerta' : 'ok'}"><small>${o.custosRealizados.length ? 'Margem projetada' : 'Margem de lucro prevista'}</small>
        <strong>${pct(o.custosRealizados.length ? m.margemProjetada : m.margemPrevista)}</strong>
        <div class="det">${abaixo ? '⚠ abaixo do mínimo de ' : 'mínimo aceitável '}${pct(m.margemMinima)} · lucro ${brl(o.custosRealizados.length ? m.lucroProjetado : m.lucroPrevisto)}</div></div>
    </div>

    <div class="abas">
      <button data-aba="servicos">Serviços</button>
      <button data-aba="materiais">Lista de materiais</button>
      <button data-aba="condicoes">BDI e condições</button>
      <button data-aba="proposta">Proposta PDF</button>
      ${temCustos ? '<button data-aba="acompanhamento">Acompanhamento da obra</button>' : ''}
    </div>
    <div id="conteudo-aba"></div>`;

  app.querySelectorAll('[data-status]').forEach((b) => b.addEventListener('click', async () => {
    try {
      await api(`/orcamentos/${id}/status`, { method: 'PATCH', body: { status: b.dataset.status } });
      if (b.dataset.status === 'EM_EXECUCAO') abaAtual = 'acompanhamento';
      avisar('Status atualizado');
      telaDetalhe(id);
    } catch (e) { avisar(e.message, true); }
  }));
  app.querySelectorAll('[data-aba]').forEach((b) => {
    b.classList.toggle('ativa', b.dataset.aba === abaAtual);
    b.addEventListener('click', () => { abaAtual = b.dataset.aba; telaDetalhe(id); });
  });

  const alvo = document.getElementById('conteudo-aba');
  const abas = { servicos: abaServicos, materiais: abaMateriais, condicoes: abaCondicoes, proposta: abaProposta, acompanhamento: abaAcompanhamento };
  await abas[abaAtual](alvo, o, tipos);
}

function abaServicos(alvo, o, tipos) {
  alvo.innerHTML = `
    ${o.editavel ? `
    <form class="cartao" id="form-servico">
      <h2>Adicionar serviço</h2>
      <div class="grade c4">
        <div class="campo"><label>Serviço</label><select name="tipo">${tipos.map((t) => `<option value="${t.codigo}">${esc(t.descricao)}</option>`).join('')}</select></div>
        <div class="campo"><label>Descrição (opcional)</label><input name="descricao" placeholder="Ex.: Paredes da sala"></div>
        <div class="campo"><label>Quantidade (<span id="un">${tipos[0].unidade}</span>)</label><input name="quantidade" type="number" step="0.01" min="0.01" required></div>
        <div id="params" class="grade c2"></div>
      </div>
      <button type="submit">Calcular e adicionar</button>
    </form>` : '<p class="dica">Para editar os serviços, volte o orçamento para rascunho.</p>'}
    <div class="cartao">
      <h2>Serviços orçados</h2>
      ${o.itens.length === 0 ? '<div class="vazio">Nenhum serviço adicionado.</div>' : `
      <table>
        <thead><tr><th>#</th><th>Serviço</th><th class="num">Qtd.</th><th class="num">Materiais</th><th class="num">Mão de obra</th><th class="num">Custo direto</th><th></th></tr></thead>
        <tbody>${o.itens.map((i, n) => `
          <tr class="clicavel" data-abrir="${i.id}">
            <td>${n + 1}</td><td><strong>${esc(i.descricao)}</strong><br><span class="dica">${esc(i.tipoDescricao)}</span></td>
            <td class="num">${num(i.quantidade)} ${i.unidade}</td><td class="num">${brl(i.custoMateriais)}</td>
            <td class="num">${brl(i.custoMaoDeObra)}</td><td class="num"><strong>${brl(i.custoDireto)}</strong></td>
            <td class="num">${o.editavel ? `<button class="perigo" data-remover="${i.id}" title="Remover">✕</button>` : ''}</td>
          </tr>
          <tr class="detalhe" id="det-${i.id}" hidden><td></td><td colspan="6"><table>${i.linhas.map((l) => `
            <tr><td>${esc(l.descricao)}</td><td class="num">${num(l.quantidade)} ${l.unidade}</td><td class="num">× ${brl(l.precoUnitario)}</td><td class="num">${brl(l.total)}</td></tr>`).join('')}
          </table></td></tr>`).join('')}
        </tbody>
      </table><p class="dica">Clique num serviço para ver a composição de materiais e mão de obra.</p>`}
    </div>`;

  alvo.querySelectorAll('[data-abrir]').forEach((tr) => tr.addEventListener('click', (ev) => {
    if (ev.target.closest('[data-remover]')) return;
    const det = document.getElementById(`det-${tr.dataset.abrir}`);
    det.hidden = !det.hidden;
  }));
  alvo.querySelectorAll('[data-remover]').forEach((b) => b.addEventListener('click', async () => {
    try { await api(`/orcamentos/${o.id}/servicos/${b.dataset.remover}`, { method: 'DELETE' }); telaDetalhe(o.id); } catch (e) { avisar(e.message, true); }
  }));

  const form = document.getElementById('form-servico');
  if (!form) return;
  const select = form.querySelector('[name=tipo]');
  const desenharParametros = () => {
    const t = tipos.find((x) => x.codigo === select.value);
    document.getElementById('un').textContent = t.unidade;
    document.getElementById('params').innerHTML = t.parametros.map((p) => `
      <div class="campo"><label>${esc(p.descricao)}</label><input name="p_${p.nome}" type="number" step="0.1" min="0" value="${p.padrao}"></div>`).join('');
  };
  select.addEventListener('change', desenharParametros);
  desenharParametros();
  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const f = dadosDoForm(form);
    const parametros = {};
    Object.keys(f).filter((k) => k.startsWith('p_')).forEach((k) => { parametros[k.slice(2)] = f[k]; });
    try {
      await api(`/orcamentos/${o.id}/servicos`, { method: 'POST', body: { tipo: f.tipo, descricao: f.descricao, quantidade: f.quantidade, parametros } });
      avisar('Serviço calculado e adicionado');
      telaDetalhe(o.id);
    } catch (e) { avisar(e.message, true); }
  });
}

async function abaMateriais(alvo, o) {
  const lista = await api(`/orcamentos/${o.id}/materiais`);
  const total = lista.reduce((s, i) => s + Number(i.total), 0);
  alvo.innerHTML = `
    <div class="cartao">
      <h2>Lista de compras</h2>
      <p class="dica">Materiais somados de todos os serviços. Sacos e unidades são arredondados para cima.</p>
      ${lista.length === 0 ? '<div class="vazio">Adicione serviços para gerar a lista.</div>' : `
      <table>
        <thead><tr><th>Material</th><th class="num">Calculado</th><th class="num">Comprar</th><th class="num">Preço unit.</th><th class="num">Total</th></tr></thead>
        <tbody>${lista.map((i) => `<tr><td>${esc(i.descricao)}</td><td class="num">${num(i.quantidade)} ${i.unidade}</td>
          <td class="num"><strong>${num(i.quantidadeCompra, i.unidade === 'un' || i.unidade === 'saco' ? 0 : 2)} ${i.unidade}</strong></td>
          <td class="num">${brl(i.precoUnitario)}</td><td class="num">${brl(i.total)}</td></tr>`).join('')}
          <tr><td colspan="4"><strong>Total da compra</strong></td><td class="num"><strong>${brl(total)}</strong></td></tr>
        </tbody>
      </table>`}
    </div>`;
}

function abaCondicoes(alvo, o) {
  const b = o.bdi;
  const campo = (nome, rotulo) => `<div class="campo"><label>${rotulo} (%)</label><input name="${nome}" type="number" step="0.01" min="0" value="${b[nome]}" ${o.editavel ? '' : 'disabled'}></div>`;
  alvo.innerHTML = `
    <form class="cartao" id="form-bdi">
      <h2>Composição do BDI</h2>
      <p class="dica">BDI = (1 + AC + S + R) × (1 + DF) × (1 + L) ÷ (1 − I) − 1 &nbsp;(fórmula do TCU, Acórdão 2622/2013)</p>
      <div class="grade c3">
        ${campo('administracaoCentral', 'Administração central')}${campo('seguroEGarantia', 'Seguro e garantia')}${campo('risco', 'Risco')}
        ${campo('despesasFinanceiras', 'Despesas financeiras')}${campo('lucro', 'Lucro')}${campo('impostos', 'Impostos (PIS, COFINS, ISS...)')}
      </div>
      <div class="grade c3">
        <div class="campo"><label>Margem mínima aceitável (%)</label><input name="margemMinima" type="number" step="0.1" value="${o.margemMinima}" ${o.editavel ? '' : 'disabled'}></div>
        <div class="campo"><label>Validade da proposta (dias)</label><input name="validadeDias" type="number" min="1" value="${o.validadeDias}" ${o.editavel ? '' : 'disabled'}></div>
      </div>
      ${o.editavel ? '<button type="submit">Salvar e recalcular</button>' : '<p class="dica">Volte para rascunho para alterar.</p>'}
    </form>`;
  const form = document.getElementById('form-bdi');
  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const f = dadosDoForm(form);
    try {
      await api(`/orcamentos/${o.id}/condicoes`, {
        method: 'PUT',
        body: {
          bdi: { administracaoCentral: f.administracaoCentral, seguroEGarantia: f.seguroEGarantia, risco: f.risco, despesasFinanceiras: f.despesasFinanceiras, lucro: f.lucro, impostos: f.impostos },
          margemMinima: f.margemMinima, validadeDias: f.validadeDias,
        },
      });
      avisar('BDI atualizado');
      telaDetalhe(o.id);
    } catch (e) { avisar(e.message, true); }
  });
}

function abaProposta(alvo, o) {
  alvo.innerHTML = `
    <form class="cartao" id="form-proposta">
      <h2>Proposta comercial em PDF</h2>
      <p class="dica">O cliente vê o valor de cada serviço já com BDI. Custos, BDI e margem não aparecem.</p>
      <div class="campo"><label>Forma de pagamento</label><input name="condicoesPagamento" value="30% na assinatura do contrato e o restante conforme medições quinzenais."></div>
      <div class="grade c2">
        <div class="campo"><label>Prazo de execução</label><input name="prazoExecucao" value="120 dias corridos após a liberação da obra."></div>
        <div class="campo"><label>Observações</label><input name="observacoes" placeholder="Ex.: não inclui projeto elétrico"></div>
      </div>
      <button type="submit" class="laranja" ${o.itens.length ? '' : 'disabled'}>Gerar PDF</button>
    </form>`;
  document.getElementById('form-proposta').addEventListener('submit', (ev) => {
    ev.preventDefault();
    const q = new URLSearchParams(dadosDoForm(ev.target)).toString();
    window.open(`/api/orcamentos/${o.id}/proposta.pdf?${q}`, '_blank');
  });
}

function abaAcompanhamento(alvo, o) {
  const m = o.margem;
  const linha = (cat) => {
    const prev = Number(m.previstoPorCategoria[cat] || 0);
    const real = Number(m.realizadoPorCategoria[cat] || 0);
    const p = prev > 0 ? Math.min(100, (real / prev) * 100) : (real > 0 ? 100 : 0);
    return `<tr><td>${CATEGORIAS[cat]}</td><td class="num">${brl(prev)}</td><td class="num">${brl(real)}</td>
      <td class="num" style="color:${real > prev ? 'var(--vermelho)' : 'inherit'}">${brl(prev - real)}</td><td style="width:28%"><div class="barra"><span class="${real > prev ? 'estouro' : ''}" style="width:${p}%"></span></div></td></tr>`;
  };
  alvo.innerHTML = `
    <div class="grade c2">
      <div class="cartao">
        <h2>Previsto × realizado</h2>
        <table>
          <thead><tr><th>Categoria</th><th class="num">Previsto</th><th class="num">Realizado</th><th class="num">Saldo</th><th></th></tr></thead>
          <tbody>${['MATERIAL', 'MAO_DE_OBRA', 'OUTROS'].map(linha).join('')}</tbody>
        </table>
        <p class="dica">Já foi gasto ${pct(m.percentualGasto)} do custo direto previsto. A margem projetada considera que cada categoria custará o maior valor entre o previsto e o já realizado.</p>
      </div>
      ${o.permiteLancarCustos ? `
      <form class="cartao" id="form-custo">
        <h2>Lançar custo real</h2>
        <div class="grade c2">
          <div class="campo"><label>Data</label><input name="data" type="date" value="${new Date().toISOString().slice(0, 10)}"></div>
          <div class="campo"><label>Categoria</label><select name="categoria">${Object.entries(CATEGORIAS).map(([k, v]) => `<option value="${k}">${v}</option>`).join('')}</select></div>
        </div>
        <div class="campo"><label>Descrição</label><input name="descricao" required placeholder="NF 1234 - cimento e areia"></div>
        <div class="campo"><label>Valor (R$)</label><input name="valor" type="number" step="0.01" min="0.01" required></div>
        <button type="submit">Lançar</button>
      </form>` : '<div class="cartao"><p class="dica">Lançamentos encerrados para este orçamento.</p></div>'}
    </div>
    <div class="cartao">
      <h2>Lançamentos</h2>
      ${o.custosRealizados.length === 0 ? '<div class="vazio">Nenhum custo lançado.</div>' : `
      <table><thead><tr><th>Data</th><th>Categoria</th><th>Descrição</th><th class="num">Valor</th></tr></thead>
      <tbody>${o.custosRealizados.map((c) => `<tr><td>${data(c.data)}</td><td>${CATEGORIAS[c.categoria]}</td><td>${esc(c.descricao)}</td><td class="num">${brl(c.valor)}</td></tr>`).join('')}</tbody></table>`}
    </div>`;
  const form = document.getElementById('form-custo');
  if (form) {
    form.addEventListener('submit', async (ev) => {
      ev.preventDefault();
      try {
        const r = await api(`/orcamentos/${o.id}/custos`, { method: 'POST', body: dadosDoForm(form) });
        avisar(r.abaixoDaMargemMinima ? `⚠ Atenção: margem projetada caiu para ${pct(r.margemProjetada)}` : 'Custo lançado', r.abaixoDaMargemMinima);
        telaDetalhe(o.id);
      } catch (e) { avisar(e.message, true); }
    });
  }
}

// ------------------------------------------------------------------ catálogo
async function telaCatalogo() {
  const [materiais, funcoes] = await Promise.all([api('/catalogo/materiais'), api('/catalogo/mao-de-obra')]);
  const tabela = (itens, tipo, colUn) => `
    <table><thead><tr><th>Descrição</th><th>${colUn}</th><th class="num" style="width:180px">Preço (R$)</th><th style="width:90px"></th></tr></thead>
    <tbody>${itens.map((i) => `<tr><td>${esc(i.descricao)}<br><span class="dica">${esc(i.codigo)}</span></td>
      <td>${tipo === 'materiais' ? i.unidade : 'hora'}</td>
      <td><input type="number" step="0.01" min="0" value="${tipo === 'materiais' ? i.preco : i.valorHora}" data-cod="${esc(i.codigo)}" data-tipo="${tipo}"></td>
      <td><button class="sec" data-salvar="${esc(i.codigo)}">Salvar</button></td></tr>`).join('')}</tbody></table>`;
  app.innerHTML = `
    <h1>Catálogo de preços</h1>
    <p class="sub">Atualize com os preços da sua região. Orçamentos já calculados mantêm os preços da época (ficam “congelados”).</p>
    <div class="cartao"><h2>Materiais</h2>${tabela(materiais, 'materiais', 'Unidade')}</div>
    <div class="cartao"><h2>Mão de obra (custo por hora, com encargos)</h2>${tabela(funcoes, 'mao-de-obra', 'Unidade')}</div>`;
  app.querySelectorAll('[data-salvar]').forEach((b) => b.addEventListener('click', async () => {
    const input = b.closest('tr').querySelector('input');
    try {
      await api(`/catalogo/${input.dataset.tipo}/${encodeURIComponent(input.dataset.cod)}`, { method: 'PUT', body: { valor: input.value } });
      avisar('Preço atualizado');
    } catch (e) { avisar(e.message, true); }
  }));
}
