package nz.massey.a336.myapplication2

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note : Note)
    @Update
    suspend fun update(note: Note)

    @Query("SELECT * FROM note_table WHERE pos = :pos")
    fun getNote(pos: Long): LiveData<Note>

    @Query("SELECT * FROM note_table")
    fun getAll() : LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE note LIKE '%'||:word||'%'")
    fun search(word: String) : LiveData<List<Note>>
}