package com.example.campusgo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.campusgo.data.dao.CategoriaDao
import com.example.campusgo.data.dao.PedidoDao
import com.example.campusgo.data.dao.UtilizadorDao
import com.example.campusgo.data.model.Categoria
import com.example.campusgo.data.model.Pedido
import com.example.campusgo.data.model.Utilizador

// Base de dados Room da app (SQLite local, sem backend) — entidades e versão do schema.
@Database(
    entities = [Utilizador::class, Categoria::class, Pedido::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Um DAO por entidade — Room gera a implementação em tempo de compilação.
    abstract fun utilizadorDao(): UtilizadorDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun pedidoDao(): PedidoDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        // Categorias iniciais — sem isto o dropdown de "Criar pedido" arranca vazio e
        // é impossível testar o fluxo do Utilizador antes de existir o CRUD de categorias
        // do Admin (planeado para mais tarde). O Admin continua livre para editar/eliminar estas.
        private val CATEGORIAS_SEED = listOf(
            "Limpeza" to "Problemas de limpeza em espaços comuns",
            "Manutenção" to "Avarias e reparações em instalações",
            "Segurança" to "Ocorrências relacionadas com segurança no campus",
            "Informática" to "Problemas com equipamento ou rede informática"
        )

        // v1 -> v2: foto de perfil do utilizador (círculo no separador "Perfil" da home).
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE utilizadores ADD COLUMN fotoPerfilPath TEXT")
            }
        }

        // Corre uma única vez, só quando o ficheiro da BD é criado pela primeira vez.
        private val seedCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CATEGORIAS_SEED.forEach { (nome, descricao) ->
                    val values = ContentValues().apply {
                        put("nome", nome)
                        put("descricao", descricao)
                    }
                    db.insert("categorias", SQLiteDatabase.CONFLICT_IGNORE, values)
                }
            }
        }

        // Singleton com double-checked locking: garante uma só instância da BD em toda a app.
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campusgo.db"
                ).addMigrations(MIGRATION_1_2).addCallback(seedCallback).build().also { instance = it }
            }
        }
    }
}
