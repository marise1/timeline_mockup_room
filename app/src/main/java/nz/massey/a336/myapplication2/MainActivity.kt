package nz.massey.a336.myapplication2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TAG = "Activity"
    var _clickedNote : Note? = null
    private val clickedNote = MutableLiveData<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = NoteAdapter(this){
            note -> onNoteClicked(note)
        }
/*
        val list = findViewById<RecyclerView>(R.id.listNote)
        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(this)
*/

        //val notes = listOf(Note(1,"text"), Note(2, "text2"))
        val dao = NoteDatabase.getInstance(this).noteDao
        val notes = dao.getAll()
        notes.observe(this, Observer {
            it?.let{
                adapter.submitList(it)
                adapter.noteList = it
            }
            Log.i("livedata", "list")
        })

        // insert note when click save button
        val txtSearch = findViewById<EditText>(R.id.txtSearch)
        val btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener{
            val text = txtSearch.text.toString()
            val noteObj = Note()
            noteObj.note = text
            lifecycleScope.launch{
                dao.insert(noteObj)
            }
            adapter.cancelSearch()
            Log.i("btnSave", "btnSave, text=$text")
        }

        // when click search button, list all the notes with match on the gray area
        var click =  false
        val area = findViewById<TextView>(R.id.display)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        btnSearch.setOnClickListener {
            click = true
            val word = txtSearch.text.toString()
            val noteMatch = dao.search(word)
            Log.i("livedata", "search, word=$word")

            noteMatch.observe(this, Observer {
                it?.let{
                    Log.i("livedata", "notematch called")
                    if(click){
                        click = false
                        adapter.noteMatch = it
                        Log.i("livedata", "running, size=${it.size}, word=$word, $it")
                    }
                }
            })

            val noteMatchString = Transformations.map(noteMatch){
                    noteMatch -> formatNotes(noteMatch)
            }
            noteMatchString.observe(this, Observer {
                it?.let{
                    area.text = it
                }
            })
        }


        // delete selected note when click delete button
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        btnDelete.setOnClickListener{
            _clickedNote?.let {
                lifecycleScope.launch {
                    dao.delete(it)
                }
            }
            _clickedNote = null
            if(_clickedNote == null) Log.i(TAG, "null")
        }

        // edit note when click edit button
        val btnEdit = findViewById<Button>(R.id.btnEdit)
        btnEdit.setOnClickListener{
            _clickedNote?.let{
                lifecycleScope.launch{
                    dao.update(Note(it.pos, txtSearch.text.toString()))
                }
            }
            _clickedNote = null
        }

        clickedNote.observe(this, Observer {
            it?.let{
                _clickedNote = it
                area.text = formatNote(it)
                //Log.i(TAG, "not null")
            }
            Log.i("livedata", "clicked item")
        })

    }

    fun onNoteClicked(note: Note){
        clickedNote.value = note
    }

    fun formatNotes(notes: List<Note>): String{
        return notes.fold(""){
                str, item -> str + '\n' + formatNote(item)
        }
    }
    fun formatNote(note: Note): String{
        return "${note.pos} ${note.note} \n"
    }

}