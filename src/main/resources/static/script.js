async function login() {
    const email = document.getElementById("email").value;
    const senha = document.getElementById("senha").value;
    const mensagem = document.getElementById("mensagem");

    const resposta = await fetch("/usuarios/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, senha })
    });

    if (!resposta.ok) {
        mensagem.innerText = "E-mail ou senha inválidos";
        mensagem.style.color = "red";
        return;
    }

    const dados = await resposta.json();
    localStorage.setItem("token", dados.token);

    document.getElementById("loginCard").classList.add("hidden");
    document.getElementById("lojasCard").classList.remove("hidden");

    carregarLojas();
}

async function carregarLojas() {
    const token = localStorage.getItem("token");

    const resposta = await fetch("/unidades", {
        headers: {
            "Authorization": "Bearer " + token
        }
    });

    const lojas = await resposta.json();
    const lista = document.getElementById("listaLojas");

    lista.innerHTML = "";

    lojas.forEach(loja => {
        const div = document.createElement("div");
        div.className = "loja";

        div.innerHTML = `
            <strong>${loja.nome}</strong><br>
            ${loja.cidade}<br>
            ${loja.endereco}<br><br>
            <button onclick="carregarProdutosDaLoja(${loja.id})">
                Ver produtos
            </button>
        `;

        lista.appendChild(div);
    });
}

async function carregarProdutosDaLoja(unidadeId) {
    const token = localStorage.getItem("token");

    const resposta = await fetch(`/estoques/unidade/${unidadeId}`, {
        headers: {
            "Authorization": "Bearer " + token
        }
    });

    const estoques = await resposta.json();

    const lista = document.getElementById("listaLojas");

    lista.innerHTML = "<h3>Produtos disponíveis</h3>";

    estoques.forEach(estoque => {
        const div = document.createElement("div");
        div.className = "loja";

        div.innerHTML = `
            <strong>${estoque.produto.nome}</strong><br>
            ${estoque.produto.descricao}<br>
            Preço: R$ ${estoque.produto.preco}<br>
            Estoque disponível: ${estoque.quantidade}
        `;

        lista.appendChild(div);
    });
}