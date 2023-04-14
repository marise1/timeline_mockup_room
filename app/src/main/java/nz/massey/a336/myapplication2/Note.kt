package nz.massey.a336.myapplication2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")
data class Note (
    @PrimaryKey(autoGenerate = true)
    var pos: Long = 0L,
    var note: String = ""
)