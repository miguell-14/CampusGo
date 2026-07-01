package com.example.campusgo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pedidos",
    foreignKeys = [
        ForeignKey(
            entity = Utilizador::class,
            parentColumns = ["id"],
            childColumns = ["utilizadorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("utilizadorId"), Index("categoriaId")]
)
data class Pedido(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val utilizadorId: Long,
    val categoriaId: Long,
    val localizacao: String,
    val descricao: String,
    val dataCriacao: Long,
    val estado: EstadoPedido,
    val fotografiaPath: String? = null
)
