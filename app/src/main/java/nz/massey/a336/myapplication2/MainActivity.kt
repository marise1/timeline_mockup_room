package nz.massey.a336.myapplication2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val r = findViewById<RecyclerView>(R.id.listNote)
        val adapter = NoteAdapter()
        r.adapter = adapter
        r.layoutManager = LinearLayoutManager(this)

        //val notes = listOf(Note(1,"text"), Note(2, "text2"))
        val dao = NoteDatabase.getInstance(this).noteDao
        val notes = dao.getAll()
        notes.observe(this, Observer {
            it?.let{
                adapter.submitList(it)
            }
        })

        val txtSearch = findViewById<EditText>(R.id.txtSearch)
        // insert note when click save button
        val btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener{
            val text = txtSearch.text.toString()
            val noteObj = Note()
            noteObj.note = text
            lifecycleScope.launch{
                dao.insert(noteObj)
            }
        }

        // when click search button, list all the notes with match on the gray area
        var noteMatchString : LiveData<String>
        var noteMatch : LiveData<List<Note>>
        var word = "note"
        val area = findViewById<TextView>(R.id.display)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        btnSearch.setOnClickListener {
            word = txtSearch.text.toString()
            //area.text = word
            //lifecycleScope.launch {
            //}
            noteMatch = dao.search(word)
            noteMatchString = Transformations.map(noteMatch){
                    noteMatch -> formatNotes(noteMatch)
            }
            noteMatchString.observe(this, Observer {
                it?.let{
                    area.text = it
                }
            })
        }
    }

    fun formatNotes(notes: List<Note>): String{
        return notes.fold(""){
                str, item -> str + '\n' + formatNote(item)
        }
    }
    fun formatNote(note: Note): String{
        var str = "pos : ${note.pos}"
        str += '\n' + "Name: ${note.note}" + '\n'
        return str
    }

}