package nz.massey.a336.myapplication2

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import nz.massey.a336.myapplication2.data.Note
import nz.massey.a336.myapplication2.data.NoteDatabase
import nz.massey.a336.myapplication2.databinding.FragmentNoteBinding
import nz.massey.a336.myapplication2.viewmodel.NoteViewModel
import nz.massey.a336.myapplication2.viewmodel.NoteViewModelFactory

class NoteFragment : Fragment() {

    private var _binding : FragmentNoteBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel : NoteViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        val view = binding.root

        val application = requireNotNull(this.activity).application
        val dao = NoteDatabase.getInstance(application).noteDao
        val viewModelFactory = NoteViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(NoteViewModel::class.java)

        viewModel.pos = NoteFragmentArgs.fromBundle(requireArguments()).pos
        viewModel.note = NoteFragmentArgs.fromBundle(requireArguments()).note

        // set note
        binding.txtNote.setText(viewModel.note)

        // edit note if back button
        binding.btnBack.setOnClickListener{
            //val text = binding.txtNote.text.toString()
            //if(text != viewModel.note){
                lifecycleScope.launch {
                    dao.update(Note(viewModel.pos, viewModel.note))
                }
            //}
            findNavController().popBackStack()
        }
        Log.i("search0", "search=${viewModel.search}, word=${viewModel.word}")

/*
        binding.txtNote.setOnClickListener {
            viewModel.drawBar = !viewModel.drawBar
            if(viewModel.drawBar){
                binding.bar.animate().translationY(viewModel.num).setDuration(300)
            } else{
                binding.bar.animate().translationY(-viewModel.num).setDuration(300)
            }
        }
*/

        binding.btnUp.setOnClickListener{
            viewModel.btnUp()
            binding.txtTotal.text = viewModel.txtTotal
            binding.txtNote.setText(viewModel.highlightedNote)
            binding.txtNote.clearFocus()
            scroll()
        }

        binding.btnDown.setOnClickListener{
            viewModel.btnDown()
            binding.txtTotal.text = viewModel.txtTotal
            binding.txtNote.setText(viewModel.highlightedNote)
            binding.txtNote.clearFocus()
            scroll()
        }

        binding.txtNote.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.i("search5", "old note=${viewModel.note} new note=${binding.txtNote.text}")
                viewModel.note = binding.txtNote.text.toString()

                if(viewModel.highlightedNote.toString() != viewModel.note){
                    viewModel.cursorPos = binding.txtNote.selectionStart
                    Log.i("scroll3", "cursorPos=${viewModel.cursorPos}")
                }

                // edit note while search
                if(viewModel.search && viewModel.word != ""
                    && viewModel.highlightedNote.toString() != viewModel.note){
                    search()
                    Log.i("search6", "search called")
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        binding.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.word = binding.txtSearch.text.toString()
                Log.i("textchange2", "textchange2, word=${viewModel.word}")
                search()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        binding.btnSearchIcon.setOnClickListener {
            viewModel.search = !viewModel.search
            if(viewModel.search){

                binding.btnSearchIcon.setImageResource(R.drawable.baseline_search_24_turned_on)
                binding.txtSearch.visibility = View.VISIBLE //animate().translationX(-width + 30f).setDuration(300)
                binding.txtSearch.setHint("search note")
                binding.txtSearch.requestFocus()

                binding.btnSettings.visibility = View.GONE
                binding.btnBack.visibility = View.GONE
            } else{

                binding.btnSearchIcon.setImageResource(R.drawable.baseline_search_24)
                binding.txtSearch.setText("")
                binding.txtSearch.visibility = View.INVISIBLE //animate().translationX(width - 30f).setDuration(300)
                binding.btnDown.visibility = View.INVISIBLE
                binding.btnUp.visibility = View.INVISIBLE
                binding.txtTotal.visibility = View.INVISIBLE

                binding.btnSettings.visibility = View.VISIBLE
                binding.btnBack.visibility = View.VISIBLE
            }
        }

        Log.i("oncreateview2", "oncreateView")
        return view
    }

    fun search(){
        Log.i("search2", "search2 called, search=${viewModel.search}, word=${viewModel.word}")

        // when turn off
        if(!viewModel.search){
            // don't clear highlight if didn't search yet or changed note without search
            Log.i("search4", "new note=${binding.txtNote.text}, \nhighlightedNote=${viewModel.highlightedNote}")
            if(viewModel.highlightedNote.toString() != "" &&
                viewModel.highlightedNote.toString() == viewModel.note){
                Log.i("search6", "new note=${binding.txtNote.text}, \nhighlightedNote=${viewModel.highlightedNote}")
                viewModel.clearHighlight()
                binding.txtNote.setText(viewModel.highlightedNote)
            }
            return
        }

        // when erase word
        if(viewModel.word == ""){
            binding.btnDown.visibility = View.INVISIBLE
            binding.btnUp.visibility = View.INVISIBLE
            binding.txtTotal.visibility = View.INVISIBLE

            viewModel.clearHighlight()
            binding.txtNote.setText(viewModel.highlightedNote)
            return
        }

        binding.btnDown.visibility = View.VISIBLE
        binding.btnUp.visibility = View.VISIBLE
        binding.txtTotal.visibility = View.VISIBLE

        viewModel.match()
        Log.i("search1", "note=${viewModel.note} highlightedNote=${viewModel.highlightedNote}")

        binding.txtNote.setText(viewModel.highlightedNote)
        binding.txtTotal.setText(viewModel.txtTotal)
        scroll()
    }

    fun scroll(){
        // set cursor to last place
        binding.txtNote.setSelection(viewModel.cursorPos)
        val layout = binding.txtNote.layout
        val linePos = layout.getLineTop(layout.getLineForOffset(viewModel.current))
        binding.scrollView.scrollTo(0, linePos)
        Log.i("scroll2", "linePos=$linePos, cursorPos=${viewModel.cursorPos}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.i("ondestroyview2", "called")
    }

}