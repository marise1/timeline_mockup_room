package nz.massey.a336.myapplication2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.findNavController
import nz.massey.a336.myapplication2.data.NoteDatabase
import nz.massey.a336.myapplication2.databinding.FragmentListBinding
import nz.massey.a336.myapplication2.viewmodel.ListViewModel
import nz.massey.a336.myapplication2.viewmodel.NoteViewModelFactory

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
        val viewModelFactory = NoteViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ListViewModel::class.java)

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
        Log.i("search2", "select=${viewModel.select}, search=${viewModel.search}, word=${viewModel.word}")
/*
        if(viewModel.search){
            viewModel.select = 1
            turnOnSearch()
            search()
        }
*/

/*
        if(viewModel.drawBar){
            binding.bar.animate().translationY(viewModel.num).setDuration(300)
        }
*/


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
                binding.listNote.scrollToPosition(viewModel.matchPos)
                binding.txtTotal.text = viewModel.txtTotal
                adapter.notifyDataSetChanged()
            }
        }
        binding.btnDown.setOnClickListener{
            if(viewModel.search){
                viewModel.btnDown()
                adapter.current = viewModel.current
                binding.listNote.scrollToPosition(viewModel.matchPos)
                binding.txtTotal.text = viewModel.txtTotal
                adapter.notifyDataSetChanged()
            }
        }

        //Log.i("searchBar", "width = $width")
        binding.btnSearchIcon.setOnClickListener {
            viewModel.select = (viewModel.select+1)%3
            when(viewModel.select){
                0 -> turnOffSearch()
                1 -> turnOnSearch()
                2 -> turnOnSearch()
            }
        }

        // 60dp is bar height, dp to px
/*
        viewModel.num = 60f * (requireContext().resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        binding.rootview.setOnClickListener {
            viewModel.drawBar = !viewModel.drawBar
            if(viewModel.drawBar){
                binding.bar.animate().translationY(viewModel.num).setDuration(300)
            } else{
                binding.bar.animate().translationY(-viewModel.num).setDuration(300)
            }
        }
*/

        binding.txtSearch.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                viewModel.word = binding.txtSearch.text.toString()
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
                if(!viewModel.search){
                    if(viewModel.noteclick){
                        viewModel.noteclick = false
                        val action = ListFragmentDirections.actionListFragmentToNoteFragment(it.note, it.pos)
                        view.findNavController().navigate(action)
                        Log.i("clickedNote", "nav")
                    } else {
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
        if(viewModel.select == 2){
            binding.btnSearchIcon.setImageResource(R.drawable.baseline_search_24_pos)
            binding.txtSearch.setHint("go to pos")
            binding.txtSearch.setText("")
            return
        }
        viewModel.search = true

        binding.btnSearchIcon.setImageResource(R.drawable.baseline_search_24_turned_on)
        binding.txtSearch.visibility = View.VISIBLE //animate().translationX(-width + 30f).setDuration(300)
        binding.txtSearch.setHint("search note")
        binding.txtSearch.requestFocus()
        binding.btnSettings.visibility = View.GONE
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
        binding.btnSettings.visibility = View.VISIBLE
    }

    fun search(){
        Log.i("search", "search called, search=${viewModel.search}, word=${viewModel.word}")

        binding.btnDown.visibility = View.INVISIBLE
        binding.btnUp.visibility = View.INVISIBLE
        binding.txtTotal.visibility = View.INVISIBLE

        if(viewModel.word != "" && viewModel.search){
            if(viewModel.select == 2){
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
                return
            }
            binding.btnDown.visibility = View.VISIBLE
            binding.btnUp.visibility = View.VISIBLE
            binding.txtTotal.visibility = View.VISIBLE

            viewModel.search(viewModel.word).observe(viewLifecycleOwner, Observer {
                it?.let{
                    //Log.i("livedata", "notematch called, ${viewModel.search}")

                    //if(viewModel.search){
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
                    //}
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