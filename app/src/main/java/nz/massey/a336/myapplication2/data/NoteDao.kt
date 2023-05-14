package nz.massey.a336.myapplication2.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note : Note)
    @Update
    suspend fun update(note: Note)
    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM note_table WHERE pos = :pos")
    fun getNote(pos: Long): LiveData<Note>

    @Query("SELECT * FROM note_table")
    fun getAll() : LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE note LIKE '%'||:word||'%'")
    fun search(word: String) : LiveData<List<Note>>
}