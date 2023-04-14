package nz.massey.a336.myapplication2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : NoteViewHolder = NoteViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }

    class NoteViewHolder(private val rootview : View)
        : RecyclerView.ViewHolder(rootview) {
        val notePos = rootview.findViewById<TextView>(R.id.pos)
        val noteText = rootview.findViewById<TextView>(R.id.note)
        companion object {
            fun inflateFrom(parent: ViewGroup): NoteViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.list_note, parent, false)
                return NoteViewHolder(view)
            }
        }
        fun bind(item: Note) {
            notePos.text = item.pos.toString()
            noteText.text = item.note
        }
    }
}
private class NoteDiffCallback : DiffUtil.ItemCallback<Note>(){
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.pos == newItem.pos
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}
