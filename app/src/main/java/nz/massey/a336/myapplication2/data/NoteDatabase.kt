package nz.massey.a336.myapplication2.data

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract val noteDao : NoteDao

    companion object {
        @Volatile
        private var instance : NoteDatabase? = null

        fun getInstance(context: Application): NoteDatabase {
            return instance ?: synchronized(this){
                instance ?: buildDatabase(context).also{ instance = it }
            }
        }
        private fun buildDatabase(context: Application): NoteDatabase {
            return Room.databaseBuilder(context, NoteDatabase::class.java, "note_db").build()
        }
    }
}