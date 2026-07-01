package com.example.campusgo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.campusgo.data.dao.CategoriaDao
import com.example.campusgo.data.dao.PedidoDao
import com.example.campusgo.data.dao.UtilizadorDao
import com.example.campusgo.data.model.Categoria
import com.example.campusgo.data.model.Pedido
import com.example.campusgo.data.model.Utilizador

@Database(
    entities = [Utilizador::class, Categoria::class, Pedido::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun utilizadorDao(): UtilizadorDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun pedidoDao(): PedidoDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campusgo.db"
                ).build().also { instance = it }
            }
        }
    }
}
