package nz.massey.a336.myapplication2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.lifecycle.*
import androidx.navigation.findNavController
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


        val adapter = NoteAdapter{
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
        viewModel.clickedNote.observe(viewLifecycleOwner, Observer {
            it?.let{
                if(viewModel.noteclick){
                    viewModel.noteclick = false
                    val action = ListFragmentDirections.actionListFragmentToNoteFragment(it.pos, it.note)
                    view.findNavController().navigate(action)
                    Log.i("clickedNote", "nav")
                } else{
                    viewModel.whenItemClick(it)
                    adapter.current = -1
                    binding.display.text = viewModel.formatNote(it)
                    adapter.submitList(viewModel.noteList)
                    adapter.notifyDataSetChanged()
                    Log.i("clickedNote", "holder")
                }
                Log.i("clickedNote", "clicked note = $it")
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

        binding.btnSettings.setOnClickListener {
/*
            if(binding.btnSettings.getTag(R.id.btnSettings) == "cancel"){
                binding.txtSearch.animate().alpha(0f).translationX(30f).setDuration(300)
                Log.i("btnSettings", "cancel")
            }
*/
        }

        binding.txtSearch2.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                viewModel.search = true
                val word = binding.txtSearch2.text.toString()
                if(word != ""){
                    viewModel.search(word).observe(viewLifecycleOwner, Observer {
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
                Log.i("livedata", "search, word=${word}")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        var on = false
        binding.btnSearchIcon.setOnClickListener {
            on = !on
            if(on){
                binding.btnSearchIcon.setImageResource(R.drawable.baseline_search_24_turned_on)
                binding.txtSearch2.animate().translationX(-200f).setDuration(300)
            } else{
                binding.btnSearchIcon.setImageResource(R.drawable.baseline_search_24)
                binding.txtSearch2.animate().translationX(200f).setDuration(300)
                binding.txtSearch2.text = null
            }
/*
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_search_24)
            val colorInt = ResourcesCompat.getColor(resources, R.color.turnedOn, null)
            drawable.setTint(colorInt)

            binding.btnSettings.setImageResource(R.drawable.cancel)
            binding.btnSettings.setTag(R.id.btnSettings, "cancel")
*/
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