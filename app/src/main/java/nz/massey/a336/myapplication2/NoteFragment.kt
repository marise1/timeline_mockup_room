package nz.massey.a336.myapplication2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.findNavController

class NoteFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_note, container, false)

        val txtNote = view.findViewById<EditText>(R.id.txtNote)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener{
            val note = txtNote.text.toString()
            val action = NoteFragmentDirections.actionNoteFragmentToListFragment(note)
            view.findNavController().navigate(action)
        }

        return view
    }
}