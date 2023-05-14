package nz.massey.a336.myapplication2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import nz.massey.a336.myapplication2.data.Note
import nz.massey.a336.myapplication2.data.NoteDatabase

class ListFragment : Fragment() {

    private val TAG = "ListFragment"
    var _clickedNote : Note? = null
    private val clickedNote = MutableLiveData<Note>()

    var noteList = listOf<Note>()
    var clickPos = -1

    var matchTotal = 0
    var nextMatchPos = -1
    var i = 0
    //var current = -1L
    var search = false
    var noteMatchList = listOf<Note>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        val application = requireNotNull(this.activity).application
/*
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            view.findNavController().navigate(R.id.action_listFragment_to_noteFragment)
        }
*/
        //val note = ListFragmentArgs.fromBundle(requireArguments()).note
        //val area = view.findViewById<TextView>(R.id.display)
        //area.text = note

        val txtTotal = view.findViewById<TextView>(R.id.txtTotal)
        val btnUp = view.findViewById<ImageButton>(R.id.btnUp)
        val btnDown = view.findViewById<ImageButton>(R.id.btnDown)

        val adapter = NoteAdapter{
                note -> onNoteClicked(note)
        }
        val list = view.findViewById<RecyclerView>(R.id.listNote)
        list.adapter = adapter
        //list.layoutManager = LinearLayoutManager()

        //val notes = listOf(Note(1,"text"), Note(2, "text2"))
        val dao = NoteDatabase.getInstance(application).noteDao
        val notes = dao.getAll()
        notes.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.submitList(it)
                //adapter.noteList = it
                noteList = it
            }
            Log.i("livedata", "list")
        })

        // insert note when click save button
        val txtSearch = view.findViewById<EditText>(R.id.txtSearch)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener{
            val text = txtSearch.text.toString()
            val noteObj = Note()
            noteObj.note = text
            adapter.current = -1
            search = false
            setAllNoteFalse()
            lifecycleScope.launch{
                dao.insert(noteObj)
            }
            adapter.notifyDataSetChanged()
            Log.i("btnSave", "btnSave, text=$text")
        }

        // when click search button, list all the notes with match on the gray area
        var click =  false
        val area = view.findViewById<TextView>(R.id.display)
        val btnSearch = view.findViewById<Button>(R.id.btnSearch)
        btnSearch.setOnClickListener {
            click = true
            val word = txtSearch.text.toString()
            val noteMatch = dao.search(word)
            Log.i("livedata", "search, word=$word")

            noteMatch.observe(viewLifecycleOwner, Observer {
                it?.let{
                    Log.i("livedata", "notematch called")
                    if(click){
                        click = false
                        //adapter.noteMatch = it
                        noteMatchList = it
                        Log.i("livedata", "running, size=${it.size}, word=$word, $it")

                        setAllNoteFalse()
                        if(it.size != 0){
                            for(match in noteMatchList){
                                Log.i("setter", "match pos=${match.pos}, ${noteList.indexOf(match)}")
                                var i = noteList.indexOf(match)
                                noteList[i].select = true
                                match.select = true
                            }
                            i=0
                            matchTotal = it.size
                            search = true
                            adapter.current = noteMatchList[i].pos
                            list.scrollToPosition(noteList.indexOf(noteMatchList[i]))
                            txtTotal.text = "1/$matchTotal"

                            adapter.submitList(noteList)
                            adapter.notifyDataSetChanged()
                        }

                    }
                }
            })

            val noteMatchString = Transformations.map(noteMatch){
                    noteMatch -> formatNotes(noteMatch)
            }
            noteMatchString.observe(viewLifecycleOwner, Observer {
                it?.let{
                    area.text = it
                }
            })
        }


        // delete selected note when click delete button
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)
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
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        btnEdit.setOnClickListener{
            _clickedNote?.let{
                lifecycleScope.launch{
                    dao.update(Note(it.pos, txtSearch.text.toString()))
                }
            }
            _clickedNote = null
        }

        clickedNote.observe(viewLifecycleOwner, Observer {
            it?.let{
                _clickedNote = it
                area.text = formatNote(it)
                setAllNoteFalse()
                val i = noteList.indexOf(it)
                noteList[i].select = true
                adapter.current = -1
                search = false
                adapter.submitList(noteList)
                adapter.notifyDataSetChanged()
                Log.i("livedata", "clicked $it, ${noteList.size}, $i")
                //Log.i("livedata", "clicked $it, ${adapter.current}, $search")
                //Log.i(TAG, "not null")
            }
        })

        btnUp.setOnClickListener{
            if(search){
                i--
                if(i == -1) i = noteMatchList.size-1
                adapter.current = noteMatchList[i].pos
                nextMatchPos = noteList.indexOf(noteMatchList[i])
                list.scrollToPosition(nextMatchPos)
                txtTotal.text = "${i+1}/$matchTotal"
                adapter.notifyDataSetChanged()
                Log.i("adapter4_up", "i = $i, current = ${adapter.current}")
            }
        }

        btnDown.setOnClickListener{
            if(search){
                i++
                if(i == noteMatchList.size) i=0
                adapter.current = noteMatchList[i].pos
                nextMatchPos = noteList.indexOf(noteMatchList[i])
                list.scrollToPosition(nextMatchPos)
                txtTotal.text = "${i+1}/$matchTotal"
                adapter.notifyDataSetChanged()
                Log.i("adapter4_down", "${noteMatchList[i]}, nextMatchPos=$nextMatchPos, i=$i, current=${adapter.current}")
                Log.i("adapter4_down", "$noteList")

            }
        }


        return view
    }

    fun setAllNoteFalse(){
        for(note in noteList){
            note.select = false
        }
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