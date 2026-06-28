const API = "";
let token = localStorage.getItem("token") || null;
let usuario = token ? decodificarToken(token) : null;

let lojaSelecionada = null;
let carrinho = [];
let lojasCache = [];
let filtroPedidosCliente = "ABERTOS";
let filtroPedidosFuncionario = "ABERTOS";
let filtroPedidosGerente = "ABERTOS";

document.addEventListener("DOMContentLoaded", () => {
    if (token && usuario) {
        abrirDashboardPorPerfil();
    } else {
        mostrarTela("telaLogin");
    }
});

function authHeaders(json = true) {
    const headers = { "Authorization": "Bearer " + token };
    if (json) headers["Content-Type"] = "application/json";
    return headers;
}

async function apiFetch(url, options = {}) {
    const resposta = await fetch(API + url, options);

    if (!resposta.ok) {
        let erro = "Erro na requisição";
        try {
            const dadosErro = await resposta.json();
            erro = dadosErro.erro || dadosErro.message || erro;
        } catch (_) {}
        throw new Error(erro);
    }

    if (resposta.status === 204) return null;

    const text = await resposta.text();
    return text ? JSON.parse(text) : null;
}

function decodificarToken(jwt) {
    try {
        const payload = jwt.split(".")[1];
        const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
        const json = decodeURIComponent(
            atob(base64).split("").map(c => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2)).join("")
        );
        return JSON.parse(json);
    } catch (e) {
        return null;
    }
}

function mostrarTela(id) {
    document.querySelectorAll(".tela").forEach(tela => tela.classList.add("hidden"));
    document.getElementById(id).classList.remove("hidden");
}

function mostrarPainel(ids, ativo) {
    ids.forEach(id => document.getElementById(id).classList.add("hidden"));
    document.getElementById(ativo).classList.remove("hidden");
}

function setMensagem(id, texto, tipo = "ok") {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = texto;
    el.style.color = tipo === "erro" ? "#dc3545" : "#198754";
}

function formatarDinheiro(valor) {
    return Number(valor || 0).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

function valorExibidoPedido(pedido) {
    return pedido.valorFinal ?? pedido.valorPago ?? pedido.valorTotal;
}

function statusCliente(status) {
    if (status === "PAGO" || status === "EM_PREPARO") return "EM PRODUÇÃO";
    if (status === "SAIU_PARA_ENTREGA") return "SAIU PARA ENTREGA";
    if (status === "ENTREGUE") return "ENTREGUE";
    if (status === "CANCELADO") return "CANCELADO";
    return status;
}

function badgeStatus(status) {
    let classe = "badge";
    if (status === "CANCELADO") classe += " badge-red";
    if (status === "AGUARDANDO_PAGAMENTO") classe += " badge-yellow";
    if (status === "PAGO" || status === "EM_PREPARO" || status === "EM PRODUÇÃO") classe += " badge-blue";
    return `<span class="${classe}">${status}</span>`;
}

async function login() {
    const email = document.getElementById("loginEmail").value.trim();
    const senha = document.getElementById("loginSenha").value.trim();

    try {
        const dados = await apiFetch("/usuarios/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, senha })
        });

        token = dados.token;
        usuario = decodificarToken(token);
        localStorage.setItem("token", token);
        abrirDashboardPorPerfil();
    } catch (e) {
        setMensagem("msgLogin", e.message, "erro");
    }
}

function sair() {
    localStorage.removeItem("token");
    token = null;
    usuario = null;
    lojaSelecionada = null;
    carrinho = [];

    document.getElementById("usuarioLogado").classList.add("hidden");
    document.getElementById("btnSair").classList.add("hidden");

    mostrarTela("telaLogin");
}

function abrirDashboardPorPerfil() {
    const unidadeTexto = usuario.unidade ? ` | Loja: ${usuario.unidade}` : "";
    document.getElementById("usuarioLogado").textContent =
        `${usuario.nome || usuario.sub} | ${usuario.role}${unidadeTexto}`;

    document.getElementById("usuarioLogado").classList.remove("hidden");
    document.getElementById("btnSair").classList.remove("hidden");

    if (usuario.role === "CLIENTE") {
        mostrarTela("dashboardCliente");
        carregarPontosCliente();
        clienteNovaCompra();
    } else if (usuario.role === "FUNCIONARIO") {
        mostrarTela("dashboardFuncionario");
        funcMostrarPedidosAbertos();
    } else if (usuario.role === "GERENTE") {
        mostrarTela("dashboardGerente");
        gerMostrarProdutos();
    } else if (usuario.role === "ADMINISTRADOR") {
        mostrarTela("dashboardAdmin");
        admMostrarUsuarios();
    } else {
        mostrarTela("telaLogin");
    }
}

async function cadastrarClientePublico() {
    const nome = document.getElementById("cadClienteNome").value.trim();
    const email = document.getElementById("cadClienteEmail").value.trim();
    const senha = document.getElementById("cadClienteSenha").value.trim();

    try {
        await apiFetch("/usuarios/cliente", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ nome, email, senha })
        });

        setMensagem("msgCadastroCliente", "Cadastro criado com sucesso. Agora faça login.");
        setTimeout(() => mostrarTela("telaLogin"), 1200);
    } catch (e) {
        setMensagem("msgCadastroCliente", e.message, "erro");
    }
}

async function carregarPontosCliente() {
    try {
        const dados = await apiFetch(`/usuarios/${usuario.id}`, {
            headers: authHeaders(false)
        });

        const pontos = dados.pontosFidelidade ?? 0;
        const box = document.getElementById("clientePontosBox");

        if (box) {
            box.textContent = `Pontos disponíveis: ${pontos}`;
        }
    } catch (e) {
        const box = document.getElementById("clientePontosBox");
        if (box) {
            box.textContent = "Pontos indisponíveis";
        }
    }
}

/* CLIENTE */

function clientePaineis() {
    return ["clienteEtapaLoja", "clienteEtapaProdutos", "clienteEtapaPedidos"];
}

async function clienteNovaCompra() {
    lojaSelecionada = null;
    carrinho = [];
    mostrarPainel(clientePaineis(), "clienteEtapaLoja");
    await carregarLojasCliente();
}

async function clienteMostrarPedidosAbertos() {
    filtroPedidosCliente = "ABERTOS";
    document.getElementById("tituloMeusPedidos").textContent = "Pedidos em aberto";
    mostrarPainel(clientePaineis(), "clienteEtapaPedidos");
    await carregarMeusPedidos();
}

async function clienteMostrarPedidosEntregues() {
    filtroPedidosCliente = "ENTREGUES";
    document.getElementById("tituloMeusPedidos").textContent = "Pedidos entregues";
    mostrarPainel(clientePaineis(), "clienteEtapaPedidos");
    await carregarMeusPedidos();
}

async function clienteMostrarPedidos() {
    await clienteMostrarPedidosAbertos();
}

async function carregarLojasCliente() {
    const lojas = await apiFetch("/unidades", { headers: authHeaders(false) });
    const lista = document.getElementById("listaLojasCliente");
    lista.innerHTML = "";

    lojas.filter(l => l.ativo !== false).forEach(loja => {
        const div = document.createElement("div");
        div.className = "item-lista";
        div.innerHTML = `
            <strong>${loja.nome}</strong><br>
            ${loja.cidade}<br>
            ${loja.endereco}
            <div class="item-actions">
                <button class="btn btn-primary" onclick='selecionarLojaCliente(${JSON.stringify(loja)})'>
                    Escolher esta loja
                </button>
            </div>
        `;
        lista.appendChild(div);
    });
}

async function selecionarLojaCliente(loja) {
    lojaSelecionada = loja;
    carrinho = [];
    document.getElementById("tituloLojaCliente").textContent = `Produtos - ${loja.nome}`;
    mostrarPainel(clientePaineis(), "clienteEtapaProdutos");
    atualizarCarrinho();
    await carregarProdutosCliente(loja.id);
}

async function carregarProdutosCliente(unidadeId) {
    const estoques = await apiFetch(`/estoques/unidade/${unidadeId}`, { headers: authHeaders(false) });
    const lista = document.getElementById("listaProdutosCliente");
    lista.innerHTML = "";

    estoques
        .filter(e => e.produto && e.produto.ativo !== false && e.quantidade > 0)
        .forEach(estoque => {
            const p = estoque.produto;
            const div = document.createElement("div");
            div.className = "item-lista";
            div.innerHTML = `
                <strong>${p.nome}</strong><br>
                ${p.descricao || ""}<br>
                <span class="badge">${formatarDinheiro(p.preco)}</span>
                <span class="badge">Estoque: ${estoque.quantidade}</span>
                <div class="item-actions">
                    <button class="btn btn-primary" onclick='adicionarCarrinho(${JSON.stringify(p)}, ${estoque.quantidade})'>
                        Adicionar
                    </button>
                </div>
            `;
            lista.appendChild(div);
        });
}

function adicionarCarrinho(produto, estoqueDisponivel) {
    const item = carrinho.find(i => i.produtoId === produto.id);

    if (item) {
        if (item.quantidade + 1 > estoqueDisponivel) {
            setMensagem("msgCarrinho", "Quantidade maior que o estoque disponível.", "erro");
            return;
        }
        item.quantidade++;
    } else {
        carrinho.push({
            produtoId: produto.id,
            nome: produto.nome,
            preco: produto.preco,
            quantidade: 1,
            estoqueDisponivel
        });
    }

    atualizarCarrinho();
}

function atualizarCarrinho() {
    const lista = document.getElementById("listaCarrinho");
    lista.innerHTML = "";

    if (carrinho.length === 0) {
        lista.innerHTML = "<p>Carrinho vazio.</p>";
        return;
    }

    let total = 0;

    carrinho.forEach(item => {
        total += Number(item.preco) * item.quantidade;

        const div = document.createElement("div");
        div.className = "item-lista";
        div.innerHTML = `
            <strong>${item.nome}</strong><br>
            Quantidade: ${item.quantidade}<br>
            Subtotal: ${formatarDinheiro(Number(item.preco) * item.quantidade)}
            <div class="item-actions">
                <button class="btn btn-secondary" onclick="alterarQuantidadeCarrinho(${item.produtoId}, 1)">+</button>
                <button class="btn btn-secondary" onclick="alterarQuantidadeCarrinho(${item.produtoId}, -1)">-</button>
                <button class="btn btn-danger" onclick="removerCarrinho(${item.produtoId})">Remover</button>
            </div>
        `;
        lista.appendChild(div);
    });

    const totalDiv = document.createElement("div");
    totalDiv.className = "item-lista";
    totalDiv.innerHTML = `<strong>Total: ${formatarDinheiro(total)}</strong>`;
    lista.appendChild(totalDiv);
}

function alterarQuantidadeCarrinho(produtoId, delta) {
    const item = carrinho.find(i => i.produtoId === produtoId);
    if (!item) return;

    const novaQuantidade = item.quantidade + delta;

    if (novaQuantidade <= 0) {
        removerCarrinho(produtoId);
        return;
    }

    if (novaQuantidade > item.estoqueDisponivel) {
        setMensagem("msgCarrinho", "Quantidade maior que o estoque disponível.", "erro");
        return;
    }

    item.quantidade = novaQuantidade;
    atualizarCarrinho();
}

function removerCarrinho(produtoId) {
    carrinho = carrinho.filter(i => i.produtoId !== produtoId);
    atualizarCarrinho();
}

async function finalizarPedido() {
    if (!lojaSelecionada) {
        setMensagem("msgCarrinho", "Escolha uma loja antes.", "erro");
        return;
    }

    if (carrinho.length === 0) {
        setMensagem("msgCarrinho", "Adicione ao menos um produto.", "erro");
        return;
    }

    const canalPedido = document.getElementById("canalPedido").value;
    const formaPagamento = document.getElementById("formaPagamentoCliente").value;
    const usarPontos = document.getElementById("usarPontosCliente").checked;

    try {
        const pedido = await apiFetch("/pedidos", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({
                clienteId: usuario.id,
                unidadeId: lojaSelecionada.id,
                canalPedido,
                itens: carrinho.map(i => ({
                    produtoId: i.produtoId,
                    quantidade: i.quantidade
                }))
            })
        });

        const pagamento = await apiFetch(`/pagamentos/${pedido.id}`, {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({ formaPagamento, usarPontos })
        });

        carrinho = [];
        atualizarCarrinho();

        alert(`Pagamento confirmado. Valor final: ${formatarDinheiro(pagamento.valor)}`);
        await carregarPontosCliente();
        await clienteMostrarPedidosAbertos();
    } catch (e) {
        setMensagem("msgCarrinho", e.message, "erro");
    }
}

async function carregarMeusPedidos() {
    const pedidos = await apiFetch(`/pedidos/cliente/${usuario.id}`, { headers: authHeaders(false) });
    const lista = document.getElementById("listaMeusPedidos");
    lista.innerHTML = "";

    if (!pedidos || pedidos.length === 0) {
        lista.innerHTML = "<p>Nenhum pedido encontrado.</p>";
        return;
    }

    const pedidosFiltrados = pedidos.filter(pedido => {
        if (filtroPedidosCliente === "ENTREGUES") {
            return pedido.statusPedido === "ENTREGUE";
        }

        return pedido.statusPedido !== "ENTREGUE" && pedido.statusPedido !== "CANCELADO";
    });

    if (pedidosFiltrados.length === 0) {
        lista.innerHTML = "<p>Nenhum pedido encontrado para este filtro.</p>";
        return;
    }

    pedidosFiltrados.reverse().forEach(pedido => {
        const podeCancelar = pedido.statusPedido !== "ENTREGUE" && pedido.statusPedido !== "CANCELADO";

        const div = document.createElement("div");
        div.className = "item-lista";
        div.innerHTML = `
            <strong>Pedido #${pedido.id}</strong><br>
            Loja: ${pedido.unidade}<br>
            Status: ${badgeStatus(statusCliente(pedido.statusPedido))}<br>
            Valor: ${formatarDinheiro(valorExibidoPedido(pedido))}
            <div class="item-actions">
                ${podeCancelar ? `<button class="btn btn-danger" onclick="cancelarPedidoCliente(${pedido.id})">Cancelar pedido</button>` : ""}
            </div>
        `;
        lista.appendChild(div);
    });
}

async function cancelarPedidoCliente(pedidoId) {
    try {
        await apiFetch(`/pedidos/${pedidoId}/cancelar`, {
            method: "PATCH",
            headers: authHeaders(false)
        });
        await carregarMeusPedidos();
        await carregarPontosCliente();
    } catch (e) {
        alert(e.message);
    }
}

/* FUNCIONÁRIO */

function funcPaineis() {
    return ["funcPainelPedidos", "funcPainelCadastroCliente"];
}

async function funcMostrarPedidosAbertos() {
    filtroPedidosFuncionario = "ABERTOS";
    document.getElementById("tituloPedidosFuncionario").textContent = "Pedidos em aberto da minha loja";
    mostrarPainel(funcPaineis(), "funcPainelPedidos");
    await carregarPedidosDaMinhaLoja();
}

async function funcMostrarPedidosEntregues() {
    filtroPedidosFuncionario = "ENTREGUES";
    document.getElementById("tituloPedidosFuncionario").textContent = "Pedidos entregues da minha loja";
    mostrarPainel(funcPaineis(), "funcPainelPedidos");
    await carregarPedidosDaMinhaLoja();
}

async function funcAtualizarPedidos() {
    await carregarPedidosDaMinhaLoja();
}

function funcMostrarCadastroCliente() {
    mostrarPainel(funcPaineis(), "funcPainelCadastroCliente");
}

async function carregarPedidosDaMinhaLoja() {
    if (!usuario.unidadeId) {
        document.getElementById("listaPedidosFuncionario").innerHTML =
            "<p>Usuário sem loja vinculada.</p>";
        return;
    }

    const pedidos = await apiFetch(`/pedidos/unidade/${usuario.unidadeId}`, {
        headers: authHeaders(false)
    });

    renderizarPedidosOperacao(pedidos, "listaPedidosFuncionario");
}

/* GERENTE */

function gerPaineis() {
    return ["gerPainelProdutos", "gerPainelPedidos", "gerPainelCadastroFuncionario", "gerPainelCadastroProduto"];
}

async function gerMostrarProdutos() {
    mostrarPainel(gerPaineis(), "gerPainelProdutos");
    await carregarProdutosGerente();
}

async function gerMostrarPedidosAbertos() {
    filtroPedidosGerente = "ABERTOS";
    document.getElementById("tituloPedidosGerente").textContent = "Pedidos em aberto da minha loja";
    mostrarPainel(gerPaineis(), "gerPainelPedidos");
    await carregarPedidosGerente();
}

async function gerMostrarPedidosEntregues() {
    filtroPedidosGerente = "ENTREGUES";
    document.getElementById("tituloPedidosGerente").textContent = "Pedidos entregues da minha loja";
    mostrarPainel(gerPaineis(), "gerPainelPedidos");
    await carregarPedidosGerente();
}

async function gerAtualizarPedidos() {
    await carregarPedidosGerente();
}

function gerMostrarCadastroFuncionario() {
    mostrarPainel(gerPaineis(), "gerPainelCadastroFuncionario");
}

function gerMostrarCadastroProduto() {
    mostrarPainel(gerPaineis(), "gerPainelCadastroProduto");
}

async function carregarPedidosGerente() {
    if (!usuario.unidadeId) {
        document.getElementById("listaPedidosGerente").innerHTML =
            "<p>Gerente sem loja vinculada.</p>";
        return;
    }

    const pedidos = await apiFetch(`/pedidos/unidade/${usuario.unidadeId}`, {
        headers: authHeaders(false)
    });

    renderizarPedidosOperacao(pedidos, "listaPedidosGerente");
}

function renderizarPedidosOperacao(pedidos, containerId) {
    const lista = document.getElementById(containerId);
    lista.innerHTML = "";

    if (!pedidos || pedidos.length === 0) {
        lista.innerHTML = "<p>Nenhum pedido encontrado.</p>";
        return;
    }

    let filtro = null;

    if (containerId === "listaPedidosFuncionario") {
        filtro = filtroPedidosFuncionario;
    }

    if (containerId === "listaPedidosGerente") {
        filtro = filtroPedidosGerente;
    }

    const pedidosFiltrados = pedidos.filter(pedido => {
        if (filtro === "ENTREGUES") {
            return pedido.statusPedido === "ENTREGUE";
        }

        if (filtro === "ABERTOS") {
            return pedido.statusPedido !== "ENTREGUE" && pedido.statusPedido !== "CANCELADO";
        }

        return true;
    });

    if (pedidosFiltrados.length === 0) {
        lista.innerHTML = "<p>Nenhum pedido encontrado para este filtro.</p>";
        return;
    }

    pedidosFiltrados.reverse().forEach(pedido => {
        const div = document.createElement("div");
        div.className = "item-lista";

        let opcoesStatus = "";
        let podeAlterar = true;

        if (containerId === "listaPedidosGerente" || containerId === "listaAdminPedidosCliente") {
            opcoesStatus = `
                <option value="EM_PREPARO">EM_PREPARO</option>
                <option value="SAIU_PARA_ENTREGA">SAIU_PARA_ENTREGA</option>
                <option value="ENTREGUE">ENTREGUE</option>
            `;

            if (pedido.statusPedido === "CANCELADO" || pedido.statusPedido === "ENTREGUE") {
                podeAlterar = false;
            }
        } else {
            if (pedido.statusPedido === "EM_PREPARO" || pedido.statusPedido === "PAGO") {
                opcoesStatus = `<option value="SAIU_PARA_ENTREGA">SAIU_PARA_ENTREGA</option>`;
            } else if (pedido.statusPedido === "SAIU_PARA_ENTREGA") {
                opcoesStatus = `<option value="ENTREGUE">ENTREGUE</option>`;
            } else {
                podeAlterar = false;
            }
        }

        div.innerHTML = `
            <strong>Pedido #${pedido.id}</strong><br>
            Cliente: ${pedido.cliente}<br>
            Loja: ${pedido.unidade}<br>
            Status: ${badgeStatus(pedido.statusPedido)}<br>
            Valor: ${formatarDinheiro(valorExibidoPedido(pedido))}

            ${
                podeAlterar
                    ? `
                        <label>Novo status</label>
                        <select id="statusPedido${pedido.id}">
                            ${opcoesStatus}
                        </select>

                        <div class="item-actions">
                            <button class="btn btn-primary" onclick="alterarStatusPedido(${pedido.id}, '${containerId}')">
                                Alterar status
                            </button>
                        </div>
                    `
                    : `<p class="info">Sem alteração disponível para este pedido.</p>`
            }
        `;
        lista.appendChild(div);
    });
}

async function alterarStatusPedido(pedidoId, containerId) {
    const status = document.getElementById(`statusPedido${pedidoId}`).value;

    try {
        await apiFetch(`/pedidos/${pedidoId}/status?status=${status}`, {
            method: "PATCH",
            headers: authHeaders(false)
        });

        if (containerId === "listaPedidosGerente") {
            await carregarPedidosGerente();
        } else if (containerId === "listaAdminPedidosCliente") {
            await adminCarregarPedidosClienteSelecionado();
        } else {
            await carregarPedidosDaMinhaLoja();
        }
    } catch (e) {
        alert(e.message);
    }
}

async function funcionarioCadastrarCliente() {
    const nome = document.getElementById("funcClienteNome").value.trim();
    const email = document.getElementById("funcClienteEmail").value.trim();
    const senha = document.getElementById("funcClienteSenha").value.trim();

    try {
        await apiFetch("/usuarios/funcionario/cadastrar-cliente", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({ nome, email, senha })
        });

        setMensagem("msgFuncionarioCliente", "Cliente cadastrado com sucesso.");
    } catch (e) {
        setMensagem("msgFuncionarioCliente", e.message, "erro");
    }
}

async function gerenteCadastrarFuncionario() {
    const nome = document.getElementById("gerFuncNome").value.trim();
    const email = document.getElementById("gerFuncEmail").value.trim();
    const senha = document.getElementById("gerFuncSenha").value.trim();

    try {
        await apiFetch("/usuarios/gerente/cadastrar", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({
                nome, email, senha,
                role: "FUNCIONARIO",
                unidadeId: usuario.unidadeId
            })
        });

        setMensagem("msgGerFunc", "Funcionário cadastrado.");
    } catch (e) {
        setMensagem("msgGerFunc", e.message, "erro");
    }
}
//painel gerente
async function carregarProdutosGerente() {
    if (!usuario.unidadeId) {
        document.getElementById("listaProdutosGerente").innerHTML =
            "<p>Gerente sem loja vinculada.</p>";
        return;
    }

    const estoques = await apiFetch(`/estoques/unidade/${usuario.unidadeId}`, {
        headers: authHeaders(false)
    });

    const lista = document.getElementById("listaProdutosGerente");
    lista.innerHTML = "";

    estoques.forEach(estoque => {
        const p = estoque.produto;
        const div = document.createElement("div");
        div.className = "item-lista";

        if (p.ativo === false) {
            div.innerHTML = `
                <strong>${p.nome}</strong><br>
                <span class="badge">ID do produto: ${p.id}</span><br>
                ${p.descricao || ""}<br>
                ${formatarDinheiro(p.preco)}
                <span class="badge badge-red">Inativo</span>
                <span class="badge">Qtd: ${estoque.quantidade}</span>

                <div class="item-actions">
                    <button class="btn btn-primary" onclick="gerenteReativarProduto(${p.id})">Reativar</button>
                    <button class="btn btn-danger" onclick="gerenteExcluirProdutoInativo(${p.id})">Excluir</button>
                </div>
            `;
            lista.appendChild(div);
            return;
        }

        div.innerHTML = `
            <strong>${p.nome}</strong><br>
            <span class="badge">ID do produto: ${p.id}</span><br>
            ${p.descricao || ""}<br>
            ${formatarDinheiro(p.preco)}
            <span class="badge">Ativo</span>
            <span class="badge">Qtd: ${estoque.quantidade}</span>

            <div id="editProduto${p.id}" class="hidden">
                <label>Nome</label>
                <input id="prodNome${p.id}" value="${p.nome}">

                <label>Descrição</label>
                <textarea id="prodDesc${p.id}">${p.descricao || ""}</textarea>

                <label>Preço</label>
                <input type="number" step="0.01" id="prodPreco${p.id}" value="${p.preco}">

                <label>Quantidade</label>
                <input type="number" id="prodQtd${p.id}" value="${estoque.quantidade}">
            </div>

            <div class="item-actions">
                <button class="btn btn-secondary" onclick="abrirEditarProduto(${p.id})">Editar</button>
                <button class="btn btn-primary" onclick="gerenteEditarProduto(${p.id})">Salvar</button>
                <button class="btn btn-danger" onclick="gerenteDesativarProduto(${p.id})">Desativar</button>
            </div>
        `;
        lista.appendChild(div);
    });
}

function abrirEditarProduto(id) {
    document.getElementById(`editProduto${id}`).classList.toggle("hidden");
}

async function gerenteReativarProduto(produtoId) {
    try {
        await apiFetch(`/produtos/${produtoId}/usuario/${usuario.id}`, {
            method: "PATCH",
            headers: authHeaders(),
            body: JSON.stringify({ ativo: true })
        });

        await carregarProdutosGerente();
    } catch (e) {
        alert(e.message);
    }
}

async function gerenteCadastrarProduto() {
    const nome = document.getElementById("gerProdutoNome").value.trim();
    const descricao = document.getElementById("gerProdutoDescricao").value.trim();
    const preco = Number(document.getElementById("gerProdutoPreco").value);
    const quantidade = Number(document.getElementById("gerProdutoQuantidade").value);

    try {
        const produto = await apiFetch("/produtos", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({ nome, descricao, preco })
        });

        await apiFetch("/estoques", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({
                produtoId: produto.id,
                unidadeId: usuario.unidadeId,
                quantidade
            })
        });

        setMensagem("msgGerProduto", "Produto cadastrado na loja.");
        await carregarProdutosGerente();
    } catch (e) {
        setMensagem("msgGerProduto", e.message, "erro");
    }
}

async function gerenteEditarProduto(produtoId) {
    const painel = document.getElementById(`editProduto${produtoId}`);
    if (painel.classList.contains("hidden")) {
        abrirEditarProduto(produtoId);
        return;
    }

    const body = {
        nome: document.getElementById(`prodNome${produtoId}`).value,
        descricao: document.getElementById(`prodDesc${produtoId}`).value,
        preco: Number(document.getElementById(`prodPreco${produtoId}`).value),
        quantidade: Number(document.getElementById(`prodQtd${produtoId}`).value),
        ativo: true
    };

    try {
        await apiFetch(`/produtos/${produtoId}/usuario/${usuario.id}`, {
            method: "PATCH",
            headers: authHeaders(),
            body: JSON.stringify(body)
        });

        await carregarProdutosGerente();
    } catch (e) {
        alert(e.message);
    }
}

async function gerenteDesativarProduto(produtoId) {
    if (!confirm("Deseja desativar este produto?")) return;

    try {
        await apiFetch(`/produtos/${produtoId}/usuario/${usuario.id}/desativar`, {
            method: "PATCH",
            headers: authHeaders(false)
        });

        await carregarProdutosGerente();
    } catch (e) {
        alert(e.message);
    }
}

async function gerenteExcluirProdutoInativo(produtoId) {
    if (!confirm("Deseja excluir definitivamente este produto da sua loja?")) return;

    try {
        await apiFetch(`/estoques/produto/${produtoId}/unidade/${usuario.unidadeId}`, {
            method: "DELETE",
            headers: authHeaders(false)
        });

        await carregarProdutosGerente();
    } catch (e) {
        alert(e.message);
    }
}

/* ADMIN */

function admPaineis() {
    return [
        "admPainelUsuarios",
        "admPainelLojas",
        "admPainelPedidosCliente",
        "admPainelCadastroUsuario",
        "admPainelCadastroLoja"
    ];
}

async function admMostrarUsuarios() {
    mostrarPainel(admPaineis(), "admPainelUsuarios");
    await adminCarregarUsuarios();
}

async function admMostrarLojas() {
    mostrarPainel(admPaineis(), "admPainelLojas");
    await adminCarregarLojas();
}


async function admMostrarPedidosCliente() {
    mostrarPainel(admPaineis(), "admPainelPedidosCliente");
    await adminPreencherClientesPedidos();
}

async function admMostrarCadastroUsuario() {
    mostrarPainel(admPaineis(), "admPainelCadastroUsuario");
    await carregarLojasSelect("admUserUnidadeId");
    adminAtualizarCampoLojaUsuario();
}

function admMostrarCadastroLoja() {
    mostrarPainel(admPaineis(), "admPainelCadastroLoja");
}


async function carregarLojasSelect(selectId) {
    lojasCache = await apiFetch("/unidades", { headers: authHeaders(false) });
    const select = document.getElementById(selectId);
    select.innerHTML = "";
    lojasCache.filter(l => l.ativo !== false).forEach(loja => {
        const opt = document.createElement("option");
        opt.value = loja.id;
        opt.textContent = `${loja.id} - ${loja.nome}`;
        select.appendChild(opt);
    });
}

function adminAtualizarCampoLojaUsuario() {
    const role = document.getElementById("admUserRole").value;
    const grupo = document.getElementById("admGrupoUnidadeUsuario");

    if (role === "GERENTE" || role === "FUNCIONARIO") {
        grupo.classList.remove("hidden");
    } else {
        grupo.classList.add("hidden");
    }
}

async function adminPreencherClientesPedidos() {
    const usuarios = await apiFetch("/usuarios", { headers: authHeaders(false) });
    const clientes = usuarios.filter(u => u.role === "CLIENTE");

    const select = document.getElementById("admSelectClientePedidos");
    select.innerHTML = "";

    clientes.forEach(cliente => {
        const opt = document.createElement("option");
        opt.value = cliente.id;
        opt.textContent = `${cliente.id} - ${cliente.nome} (${cliente.email})`;
        select.appendChild(opt);
    });

    document.getElementById("listaAdminPedidosCliente").innerHTML = "";

    if (clientes.length === 0) {
        document.getElementById("listaAdminPedidosCliente").innerHTML = "<p>Nenhum cliente cadastrado.</p>";
    }
}

async function adminCarregarPedidosClienteSelecionado() {
    const clienteId = document.getElementById("admSelectClientePedidos").value;

    if (!clienteId) {
        document.getElementById("listaAdminPedidosCliente").innerHTML = "<p>Selecione um cliente.</p>";
        return;
    }

    const pedidos = await apiFetch(`/pedidos/cliente/${clienteId}`, {
        headers: authHeaders(false)
    });

    renderizarPedidosOperacao(pedidos, "listaAdminPedidosCliente");
}

async function adminCarregarUsuarios() {
    const usuarios = await apiFetch("/usuarios", { headers: authHeaders(false) });
    const lista = document.getElementById("listaAdminUsuarios");
    lista.innerHTML = "";

    usuarios.forEach(u => {
        const div = document.createElement("div");
        div.className = "item-lista";

        const ativo = u.ativo !== false;

        if (!ativo) {
            div.innerHTML = `
                <strong>${u.nome}</strong><br>
                ID: ${u.id}<br>
                E-mail: ${u.email}<br>
                Perfil: <span class="badge">${u.role}</span><br>
                ${u.unidade ? `Loja: ${u.unidade}<br>` : ""}
                <span class="badge badge-red">Usuário inativo</span>

                <div class="item-actions">
                    <button class="btn btn-primary" onclick="adminReativarUsuario(${u.id})">Reativar</button>
                    <button class="btn btn-danger" onclick="adminExcluirUsuario(${u.id})">Excluir</button>
                </div>
            `;

            lista.appendChild(div);
            return;
        }

        div.innerHTML = `
            <strong>${u.nome}</strong><br>
            ID: ${u.id}<br>
            E-mail: ${u.email}<br>
            Perfil: <span class="badge">${u.role}</span><br>
            ${u.unidade ? `Loja: ${u.unidade}<br>` : ""}
            ${u.pontosFidelidade !== undefined ? `Pontos: ${u.pontosFidelidade}<br>` : ""}
            <span class="badge">Usuário ativo</span>

            <div id="admEditUsuario${u.id}" class="hidden">
                <label>Nome</label>
                <input id="admEditNome${u.id}" value="${u.nome}">

                <label>E-mail</label>
                <input id="admEditEmail${u.id}" value="${u.email}">

                <label>Nova senha (opcional)</label>
                <input type="password" id="admEditSenha${u.id}" placeholder="Deixe em branco para manter">

                <label>Perfil</label>
                <select id="admEditRole${u.id}">
                    <option value="CLIENTE" ${u.role === "CLIENTE" ? "selected" : ""}>Cliente</option>
                    <option value="FUNCIONARIO" ${u.role === "FUNCIONARIO" ? "selected" : ""}>Funcionário</option>
                    <option value="GERENTE" ${u.role === "GERENTE" ? "selected" : ""}>Gerente</option>
                </select>
            </div>

            <div class="item-actions">
                <button class="btn btn-secondary" onclick="toggle('admEditUsuario${u.id}')">Editar</button>
                <button class="btn btn-primary" onclick="adminEditarUsuario(${u.id})">Salvar</button>
                <button class="btn btn-danger" onclick="adminDesativarUsuario(${u.id})">Desativar</button>
            </div>
        `;
        lista.appendChild(div);
    });
}

async function adminDesativarUsuario(id) {
    if (!confirm("Deseja desativar este usuário?")) return;

    try {
        await apiFetch(`/usuarios/admin/${id}`, {
            method: "PATCH",
            headers: authHeaders(),
            body: JSON.stringify({ ativo: false })
        });

        await adminCarregarUsuarios();
    } catch (e) {
        alert(e.message);
    }
}

async function adminReativarUsuario(id) {
    try {
        await apiFetch(`/usuarios/admin/${id}`, {
            method: "PATCH",
            headers: authHeaders(),
            body: JSON.stringify({ ativo: true })
        });

        await adminCarregarUsuarios();
    } catch (e) {
        alert(e.message);
    }
}

function toggle(id) {
    document.getElementById(id).classList.toggle("hidden");
}

async function adminCadastrarUsuario() {
    const nome = document.getElementById("admUserNome").value.trim();
    const email = document.getElementById("admUserEmail").value.trim();
    const senha = document.getElementById("admUserSenha").value.trim();
    const role = document.getElementById("admUserRole").value;

    const body = { nome, email, senha, role };

    if (role === "GERENTE" || role === "FUNCIONARIO") {
        body.unidadeId = Number(document.getElementById("admUserUnidadeId").value);
    }

    try {
        await apiFetch("/usuarios/admin/cadastrar", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify(body)
        });

        setMensagem("msgAdmUser", "Usuário cadastrado.");
    } catch (e) {
        setMensagem("msgAdmUser", e.message, "erro");
    }
}

async function adminEditarUsuario(id) {
    const body = {
        nome: document.getElementById(`admEditNome${id}`).value,
        email: document.getElementById(`admEditEmail${id}`).value,
        role: document.getElementById(`admEditRole${id}`).value
    };

    const novaSenha = document.getElementById(`admEditSenha${id}`).value;
    if (novaSenha && novaSenha.trim() !== "") {
        body.senha = novaSenha.trim();
    }

    try {
        await apiFetch(`/usuarios/admin/${id}`, {
            method: "PATCH",
            headers: authHeaders(),
            body: JSON.stringify(body)
        });

        await adminCarregarUsuarios();
    } catch (e) {
        alert(e.message);
    }
}

async function adminExcluirUsuario(id) {
    if (!confirm("Deseja excluir definitivamente este usuário?")) return;

    try {
        await apiFetch(`/usuarios/admin/${id}`, {
            method: "DELETE",
            headers: authHeaders(false)
        });

        await adminCarregarUsuarios();
    } catch (e) {
        if (e.message && (e.message.includes("vinculados") || e.message.includes("foreign key") || e.message.includes("pedidos"))) {
            alert("Não é possível excluir este usuário porque ele possui pedidos vinculados. Mantenha o usuário desativado para preservar o histórico.");
        } else {
            alert(e.message);
        }
    }
}

async function adminCarregarLojas() {
    const lojas = await apiFetch("/unidades", { headers: authHeaders(false) });
    const lista = document.getElementById("listaAdminLojas");
    lista.innerHTML = "";

    lojas.forEach(loja => {
        const div = document.createElement("div");
        div.className = "item-lista";

        if (loja.ativo === false) {
            div.innerHTML = `
                <strong>${loja.nome}</strong><br>
                ID: ${loja.id}<br>
                Cidade: ${loja.cidade}<br>
                Endereço: ${loja.endereco}<br>
                <span class="badge badge-red">Inativa</span>

                <div class="item-actions">
                    <button class="btn btn-primary" onclick="adminReativarLoja(${loja.id})">
                        Reativar loja
                    </button>
                    <button class="btn btn-danger" onclick="adminExcluirLoja(${loja.id})">
                        Excluir loja
                    </button>
                </div>
            `;

            lista.appendChild(div);
            return;
        }

        div.innerHTML = `
            <strong>${loja.nome}</strong><br>
            ID: ${loja.id}<br>
            Cidade: ${loja.cidade}<br>
            Endereço: ${loja.endereco}<br>
            <span class="badge">Ativa</span>

            <div id="admEditLoja${loja.id}" class="hidden">
                <label>Nome</label>
                <input id="admLojaNomeEdit${loja.id}" value="${loja.nome}">
                <label>Cidade</label>
                <input id="admLojaCidadeEdit${loja.id}" value="${loja.cidade}">
                <label>Endereço</label>
                <input id="admLojaEnderecoEdit${loja.id}" value="${loja.endereco}">
            </div>

            <div class="item-actions">
                <button class="btn btn-secondary" onclick="toggle('admEditLoja${loja.id}')">Editar</button>
                <button class="btn btn-primary" onclick="adminEditarLoja(${loja.id})">Salvar</button>
                <button class="btn btn-danger" onclick="adminDesativarLoja(${loja.id})">Desativar</button>
            </div>
        `;
        lista.appendChild(div);
    });
}

async function adminReativarLoja(id) {
    try {
        await apiFetch(`/unidades/${id}`, {
            method: "PATCH",
            headers: authHeaders(),
            body: JSON.stringify({ ativo: true })
        });

        await adminCarregarLojas();
    } catch (e) {
        alert(e.message);
    }
}

async function adminExcluirLoja(id) {
    if (!confirm("Deseja excluir definitivamente esta loja?")) return;

    try {
        await apiFetch(`/unidades/${id}`, {
            method: "DELETE",
            headers: authHeaders(false)
        });

        await adminCarregarLojas();
    } catch (e) {
        alert(e.message);
    }
}

async function adminCadastrarLoja() {
    const nome = document.getElementById("admLojaNome").value.trim();
    const cidade = document.getElementById("admLojaCidade").value.trim();
    const endereco = document.getElementById("admLojaEndereco").value.trim();

    try {
        await apiFetch("/unidades", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({ nome, cidade, endereco })
        });

        setMensagem("msgAdmLoja", "Loja cadastrada.");
    } catch (e) {
        setMensagem("msgAdmLoja", e.message, "erro");
    }
}

async function adminEditarLoja(id) {
    const body = {
        nome: document.getElementById(`admLojaNomeEdit${id}`).value,
        cidade: document.getElementById(`admLojaCidadeEdit${id}`).value,
        endereco: document.getElementById(`admLojaEnderecoEdit${id}`).value
    };

    try {
        await apiFetch(`/unidades/${id}`, {
            method: "PATCH",
            headers: authHeaders(),
            body: JSON.stringify(body)
        });

        await adminCarregarLojas();
    } catch (e) {
        alert(e.message);
    }
}

async function adminDesativarLoja(id) {
    try {
        await apiFetch(`/unidades/${id}/desativar`, {
            method: "PATCH",
            headers: authHeaders(false)
        });

        await adminCarregarLojas();
    } catch (e) {
        alert(e.message);
    }
}

