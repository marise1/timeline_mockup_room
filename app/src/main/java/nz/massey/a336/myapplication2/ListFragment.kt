package nz.massey.a336.myapplication2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.findNavController
import kotlinx.coroutines.launch
import nz.massey.a336.myapplication2.data.Note
import nz.massey.a336.myapplication2.data.NoteDatabase
import nz.massey.a336.myapplication2.databinding.FragmentListBinding

class ListFragment : Fragment() {

    //private val TAG = "ListFragment"
    private var _binding : FragmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root

        val application = requireNotNull(this.activity).application
        val dao = NoteDatabase.getInstance(application).noteDao
        val viewModelFactory = NoteViewModelFactory(dao)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ListViewModel::class.java)

/*
        binding.btnSave.setOnClickListener {
            view.findNavController().navigate(R.id.action_listFragment_to_noteFragment)
        }
        binding.display.text = ListFragmentArgs.fromBundle(requireArguments()).note
*/

        val adapter = NoteAdapter{
                note -> viewModel.onNoteClicked(note)
        }
        binding.listNote.adapter = adapter

        viewModel.notes.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.submitList(it)
                viewModel.noteList = it
            }
            Log.i("livedata", "list")
        })
        viewModel.clickedNote.observe(viewLifecycleOwner, Observer {
            it?.let{
                viewModel.whenNoteClick(it)
                binding.display.text = viewModel.formatNote(it)
                adapter.current = -1
                adapter.submitList(viewModel.noteList)
                adapter.notifyDataSetChanged()
                Log.i("clickedNote", "$it")
            }
        })
        Log.i("oncreateview", "${viewModel.noteList}")

        // insert note when click save button
        binding.btnSave.setOnClickListener{
            val text = binding.txtSearch.text.toString()
            viewModel.addNote(text)
            adapter.current = -1
            adapter.notifyDataSetChanged()
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

        // when click search button, list all the notes with match on the gray area
        binding.btnSearch.setOnClickListener {
            viewModel.search = true
            val word = binding.txtSearch.text.toString()
            val noteMatch = viewModel.search(word)
            Log.i("livedata", "search, word=${word}")

            noteMatch.observe(viewLifecycleOwner, Observer {
                it?.let{
                    Log.i("livedata", "notematch called")

                    if(viewModel.search){
                        Log.i("livedata", "running, size=${it.size}, word=${word}, $it")
                        viewModel.noteMatchList = it

                        viewModel.whenNoteMatch()
                        adapter.current = viewModel.current
                        binding.listNote.scrollToPosition(viewModel.matchPos)
                        binding.txtTotal.text = viewModel.txtTotal
                        binding.display.text = viewModel.formatNotes(it)
                        adapter.submitList(viewModel.noteList)
                        adapter.notifyDataSetChanged()
                    }
                }
            })
        }
        // when rotate during search
        if(viewModel.search){
            adapter.current = viewModel.current
            binding.listNote.scrollToPosition(viewModel.matchPos)
            binding.txtTotal.text = viewModel.txtTotal
            binding.display.text = viewModel.formatNotes(viewModel.noteMatchList)
            adapter.notifyDataSetChanged()
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

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.i("ondestroy", "called")
    }
}