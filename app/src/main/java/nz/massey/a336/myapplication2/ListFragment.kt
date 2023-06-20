package nz.massey.a336.myapplication2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import nz.massey.a336.myapplication2.data.NoteDatabase
import nz.massey.a336.myapplication2.databinding.FragmentListBinding

class ListFragment : Fragment() {

    //private val TAG = "ListFragment"
    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel : ListViewModel
    lateinit var adapter : NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root
        val width = requireContext().resources.displayMetrics.widthPixels

        val application = requireNotNull(this.activity).application
        val dao = NoteDatabase.getInstance(application).noteDao
        val sharedViewModel : ListViewModel by activityViewModels{ NoteViewModelFactory(dao) }
        viewModel = sharedViewModel

        adapter = NoteAdapter{
                note, noteclick -> viewModel.onItemClicked(note, noteclick)
        }
        binding.listNote.adapter = adapter
        viewModel.notes.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.submitList(it)
                viewModel.noteList = it
            }
            Log.i("livedata", "list")
        })


        // when rotate during search
/*
        if(viewModel.search){
            adapter.current = viewModel.current
            binding.listNote.scrollToPosition(viewModel.matchPos)
            binding.txtTotal.text = viewModel.txtTotal
            binding.display.text = viewModel.formatNotes(viewModel.noteMatchList)
            adapter.notifyDataSetChanged()
        }
*/

        // when come from noteview while search
        Log.i("search2", "search = ${viewModel.search}, word = ${viewModel.word}")
        if(viewModel.search){
            turnOnSearch()
            search()
        }


        // insert note when click save button
        binding.btnSave.setOnClickListener{
            val text = binding.txtSearch.text.toString()
            viewModel.addNote(text)
            //adapter.current = -1
            //adapter.notifyDataSetChanged()
            Log.i("btnSave", "btnSave, text=$text")
        }

        // delete selected note when click delete button
        binding.btnDelete.setOnClickListener{
            viewModel.deleteNote()
        }

        // edit note when click edit button
        binding.btnEdit.setOnClickListener{
            viewModel.editNote(binding.txtSearch.text.toString())
        }

        binding.btnSettings.setOnClickListener {
        }

        binding.btnUp.setOnClickListener{
            if(viewModel.search){
                viewModel.btnUp()
                adapter.current = viewModel.current
                binding.listNote.scrollToPosition(viewModel.nextMatchPos)
                binding.txtTotal.text = viewModel.txtTotal
                adapter.notifyDataSetChanged()
            }
        }
        binding.btnDown.setOnClickListener{
            if(viewModel.search){
                viewModel.btnDown()
                adapter.current = viewModel.current
                binding.listNote.scrollToPosition(viewModel.nextMatchPos)
                binding.txtTotal.text = viewModel.txtTotal
                adapter.notifyDataSetChanged()
            }
        }

        //Log.i("searchBar", "width = $width")
        binding.btnSearchIcon.setOnClickListener {
            Log.i("searchIcon", "search = ${viewModel.search}")
            viewModel.search = !viewModel.search
            if(viewModel.search){
                turnOnSearch()
            } else{
                turnOffSearch()
            }
        }

        binding.txtSearch.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                search()
                Log.i("textchange", "textchange")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        viewModel.clickedNote.observe(viewLifecycleOwner, Observer {
            it?.let{
                viewModel._clickedNote = it
                if(viewModel.noteclick){
                    viewModel.noteclick = false
                    view.findNavController().navigate(R.id.action_listFragment_to_noteFragment)
                    Log.i("clickedNote", "nav")
                } else{
                    if(!viewModel.search){
                        Log.i("clickedNote", "holder")
                        viewModel.whenItemClick(it)
                        binding.display.text = viewModel.formatNote(it)
                        adapter.submitList(viewModel.noteList)
                        adapter.notifyDataSetChanged()
                    }
                }
                Log.i("clickedNote", "clicked note = $it")
            }
        })

        Log.i("oncreateview", "oncreateView")
        return view
    }

    fun turnOnSearch(){
        binding.btnSearchIcon.setImageResource(R.drawable.baseline_search_24_turned_on)
        binding.txtSearch.visibility = View.VISIBLE //animate().translationX(-width + 30f).setDuration(300)
        binding.txtSearch.requestFocus()
    }
    fun turnOffSearch(){
        viewModel.search = false
        adapter.current = -1
        viewModel.setAllNoteFalse()
        adapter.submitList(viewModel.noteList)
        adapter.notifyDataSetChanged()

        binding.btnSearchIcon.setImageResource(R.drawable.baseline_search_24)
        binding.txtSearch.setText("")
        binding.txtSearch.visibility = View.INVISIBLE //animate().translationX(width - 30f).setDuration(300)
        binding.btnDown.visibility = View.INVISIBLE
        binding.btnUp.visibility = View.INVISIBLE
        binding.txtTotal.visibility = View.INVISIBLE
    }

    fun search(){
        viewModel.word = binding.txtSearch.text.toString()
        Log.i("search", "search called, word=${viewModel.word}")

        if(viewModel.word != ""){
            if(viewModel.word.toIntOrNull() != null){
                val num = viewModel.word.toInt()
                viewModel.setAllNoteFalse()
                binding.listNote.scrollToPosition(num)
                if(num > -1 && num < viewModel.noteList.size){
                    viewModel.noteList[num].select = true
                    adapter.submitList(viewModel.noteList)
                    adapter.notifyDataSetChanged()
                }
                return
            }
            binding.btnDown.visibility = View.VISIBLE
            binding.btnUp.visibility = View.VISIBLE
            binding.txtTotal.visibility = View.VISIBLE

            viewModel.search(viewModel.word).observe(viewLifecycleOwner, Observer {
                it?.let{
                    Log.i("livedata", "notematch called")

                    if(viewModel.search){
                        Log.i("livedata", "running, size=${it.size}, word=${viewModel.word}, $it")
                        viewModel.noteMatchList = it

                        viewModel.whenNoteMatch()
                        adapter.current = viewModel.current
                        binding.listNote.scrollToPosition(viewModel.matchPos)
                        binding.txtTotal.text = viewModel.txtTotal
                        binding.display.text = viewModel.formatNotes(it)
                        adapter.submitList(viewModel.noteList)
                        adapter.notifyDataSetChanged()
                        Log.i("livedata", "matchPos = ${viewModel.matchPos}")
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.i("ondestroy", "called")
    }
}