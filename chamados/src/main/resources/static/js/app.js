/* Utilidades compartilhadas entre as páginas */
const App = (() => {
  const SESSAO = 'chamados.sessao';
  const FLASH = 'chamados.flash';

  const STATUS = {
    ABERTO: 'Aberto',
    EM_ATENDIMENTO: 'Em atendimento',
    AGUARDANDO_CLIENTE: 'Aguardando cliente',
    FECHADO: 'Fechado',
  };
  const PRIORIDADE = { BAIXA: 'Baixa', MEDIA: 'Média', ALTA: 'Alta', CRITICA: 'Crítica' };
  // Mesmas transições da regra de negócio do backend
  const PROXIMOS = {
    ABERTO: ['EM_ATENDIMENTO', 'FECHADO'],
    EM_ATENDIMENTO: ['AGUARDANDO_CLIENTE', 'FECHADO'],
    AGUARDANDO_CLIENTE: ['EM_ATENDIMENTO', 'FECHADO'],
    FECHADO: [],
  };

  class ApiError extends Error {
    constructor(message, status) { super(message); this.status = status; }
  }

  async function api(path, { method = 'GET', body } = {}) {
    let res;
    try {
      res = await fetch(path, {
        method,
        headers: body ? { 'Content-Type': 'application/json' } : {},
        body: body ? JSON.stringify(body) : undefined,
      });
    } catch {
      throw new ApiError('Não foi possível conectar à API. Confira se ela está rodando em localhost:8080.', 0);
    }
    if (res.status === 204) return null;
    const corpo = await res.json().catch(() => null);
    if (!res.ok) {
      const detalhes = corpo && corpo.detalhes && corpo.detalhes.length ? corpo.detalhes.join(' ') : null;
      throw new ApiError(detalhes || (corpo && corpo.erro) || 'Algo deu errado. Tente novamente.', res.status);
    }
    return corpo;
  }

  const esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const data = (iso) => (iso ? new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '—');

  const sessao = () => { try { return JSON.parse(sessionStorage.getItem(SESSAO)); } catch { return null; } };
  const entrar = (s) => sessionStorage.setItem(SESSAO, JSON.stringify(s));
  const sair = () => { sessionStorage.removeItem(SESSAO); location.href = 'index.html'; };

  function toast(texto, erro = false) {
    // Dentro de um <dialog> modal o aviso precisa nascer no próprio diálogo para ficar visível.
    const host = document.querySelector('dialog[open]') || document.body;
    let box = host.querySelector(':scope > .toast-box');
    if (!box) {
      box = document.createElement('div');
      box.className = 'toast-box';
      box.setAttribute('role', 'status');
      box.setAttribute('aria-live', 'polite');
      host.appendChild(box);
    }
    const t = document.createElement('div');
    t.className = 'toast' + (erro ? ' erro' : '');
    t.textContent = texto;
    box.appendChild(t);
    setTimeout(() => { t.remove(); if (!box.children.length) box.remove(); }, 4000);
  }
  const flash = (texto) => sessionStorage.setItem(FLASH, texto);

  /* Protege a página, desenha o topo e devolve a sessão. */
  function iniciarPagina(ativa) {
    const s = sessao();
    if (!s) { location.href = 'index.html'; return null; }
    const link = (href, rotulo) =>
      `<a href="${href}"${ativa === href ? ' aria-current="page"' : ''}>${rotulo}</a>`;
    document.getElementById('topo').innerHTML = `
      <div class="in">
        <a class="brand" href="chamados.html">Chamados</a>
        <nav aria-label="Principal">${link('chamados.html', 'Painel')}${link('novo-chamado.html', 'Novo chamado')}</nav>
        <div class="who"><span>${esc(s.nome)}</span><button type="button" id="sair">Sair</button></div>
      </div>`;
    document.getElementById('sair').addEventListener('click', sair);
    const f = sessionStorage.getItem(FLASH);
    if (f) { sessionStorage.removeItem(FLASH); toast(f); }
    return s;
  }

  return { api, ApiError, esc, data, sessao, entrar, sair, toast, flash, iniciarPagina, STATUS, PRIORIDADE, PROXIMOS };
})();
