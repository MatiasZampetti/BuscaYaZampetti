package com.example.buscayazampetti.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * La base de datos de la app: el punto de entrada a Room.
 *
 * entities     -> TODAS las tablas. Si te olvidás una, Room no la crea.
 * version      -> subila (1, 2, 3...) cada vez que cambies una tabla.
 * exportSchema -> en false para la clase. En un proyecto real va en true
 *                 (con una carpeta de esquemas) para escribir y probar migraciones.
 */
@Database(
    entities = [FarmaciaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BuscaYaDatabase : RoomDatabase() {

    // Room genera el código de este método.
    abstract fun farmaciaDao(): FarmaciaDao

    companion object {

        // @Volatile: todos los hilos ven siempre el valor actualizado.
        @Volatile
        private var INSTANCIA: BuscaYaDatabase? = null

        /**
         * Singleton: abrir una base es caro, así que hay UNA sola instancia
         * para toda la app.
         */
        fun getInstance(context: Context): BuscaYaDatabase {
            // Si ya existe, la devolvemos. Si no, la creamos UNA vez
            // (synchronized evita que dos hilos la creen a la vez).
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,   // nunca una Activity (pérdida de memoria)
                    BuscaYaDatabase::class.java,
                    "buscaya.db"                  // nombre del archivo en el teléfono
                )
                    // ⚠️ SOLO PARA DESARROLLO: si subís "version" y no escribiste
                    // una Migration, Room BORRA todas las tablas y las crea de nuevo
                    // vacías. Acá no es grave porque las farmacias vuelven a bajar de
                    // la API, pero en producción el usuario perdería sus datos:
                    // ahí corresponde escribir una Migration.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCIA = it }
            }
        }
    }
}
