package nz.massey.a336.myapplication2

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import nz.massey.a336.myapplication2.data.Note
import nz.massey.a336.myapplication2.data.NoteDao

class ListViewModel(val dao: NoteDao) : ViewModel() {

    var noteclick = false
    var _clickedNote : Note? = null
    val clickedNote = MutableLiveData<Note>()

    var noteList = listOf<Note>()

    var txtTotal = ""
    var matchPos = -1
    var current = -1L
    var matchTotal = 0
    var nextMatchPos = -1
    var i = 0
    var search = false
    var noteMatchList = listOf<Note>()

    val notes = dao.getAll()

    fun setAllNoteFalse(){
        for(note in noteList){
            note.select = false
        }
    }
    fun onItemClicked(note: Note, click: Boolean){
        noteclick = click
        clickedNote.value = note
        Log.i("itemclick", "noteclick = $click")
    }
    fun whenItemClick(note: Note){
        _clickedNote = note
        setAllNoteFalse()
        search = false
        val i = noteList.indexOf(note)
        noteList[i].select = true
    }

    fun addNote(text : String){
        val noteObj = Note()
        noteObj.note = text
        search = false
        setAllNoteFalse()
        viewModelScope.launch{
            dao.insert(noteObj)
        }
    }
    fun deleteNote(){
        _clickedNote?.let {
            viewModelScope.launch {
                dao.delete(it)
            }
        }
        _clickedNote = null
    }
    fun editNote(text: String){
        _clickedNote?.let{
            viewModelScope.launch{
                dao.update(Note(it.pos, text))
            }
        }
        _clickedNote = null
    }

    fun search(word : String) : LiveData<List<Note>>{
        val noteMatch = dao.search(word)
        return noteMatch
    }
    fun whenNoteMatch(){
        setAllNoteFalse()

        if(noteMatchList.isEmpty()){
            current = -1
            search = false
            txtTotal = "0/0"
            return
        }

        for(match in noteMatchList){
            Log.i("setter", "match pos=${match.pos}, ${noteList.indexOf(match)}")
            //Log.i("setter", "$match, $noteList")
            val i = noteList.indexOf(match)
            noteList[i].select = true
        }
        i=0
        matchTotal = noteMatchList.size
        txtTotal = "1/${matchTotal}"
        current = noteMatchList[i].pos
        matchPos = noteList.indexOf(noteMatchList[i])
    }

    fun btnUp(){
        i--
        if(i == -1) i = noteMatchList.size-1
        current = noteMatchList[i].pos
        nextMatchPos = noteList.indexOf(noteMatchList[i])
        txtTotal = "${i+1}/$matchTotal"
        Log.i("adapter4_up", "i = $i, current = $current")
    }
    fun btnDown(){
        i++
        if(i == noteMatchList.size) i=0
        current = noteMatchList[i].pos
        nextMatchPos = noteList.indexOf(noteMatchList[i])
        txtTotal = "${i+1}/$matchTotal"
        Log.i("adapter4_down", "${noteMatchList[i]}, " + "nextMatchPos=$nextMatchPos, i=$i, current=$current")
        //Log.i("adapter4_down", "$noteList")
    }

    fun formatNotes(notes: List<Note>): String{
        return notes.fold(""){
                str, item -> str + '\n' + formatNote(item)
        }
    }
    fun formatNote(note: Note): String{
        return "${note.pos} ${note.note} \n"
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("viewmodel", "oncleared")
    }
}