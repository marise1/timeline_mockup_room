package nz.massey.a336.myapplication2

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import nz.massey.a336.myapplication2.data.Note
import nz.massey.a336.myapplication2.data.NoteDatabase
import nz.massey.a336.myapplication2.databinding.FragmentListBinding
import nz.massey.a336.myapplication2.databinding.FragmentNoteBinding

class NoteFragment : Fragment() {

    private var _binding : FragmentNoteBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel : ListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        val view = binding.root

        val application = requireNotNull(this.activity).application
        val dao = NoteDatabase.getInstance(application).noteDao
        val sharedViewModel : ListViewModel by activityViewModels{ NoteViewModelFactory(dao) }
        viewModel = sharedViewModel

        if(viewModel.search){
            binding.btnSearchIcon.setImageResource(R.drawable.baseline_search_24_turned_on)
            binding.btnDown.visibility = View.VISIBLE
            binding.btnUp.visibility = View.VISIBLE
            binding.txtTotal.visibility = View.VISIBLE
            binding.txtSearch.visibility = View.VISIBLE
            binding.txtSearch.setText(viewModel.word)
            binding.txtSearch.requestFocus()
        }

        val note = viewModel._clickedNote!!
        binding.txtNote.setText(note.note)
        binding.btnBack.setOnClickListener{
            val text = binding.txtNote.text.toString()
            if(text != note.note){
                lifecycleScope.launch {
                    dao.update(Note(note.pos, text))
                }
            }
            viewModel.nav = true
            findNavController().popBackStack()
        }

        return view
    }
}