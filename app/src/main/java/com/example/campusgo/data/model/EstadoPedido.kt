package com.example.campusgo.data.model

// Estados possíveis de um pedido, do submetido pelo Utilizador até à decisão do Admin.
enum class EstadoPedido {
    SUBMETIDO,
    EM_ANALISE,
    CONCLUIDO,
    REJEITADO
}
