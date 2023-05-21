package nz.massey.a336.myapplication2

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nz.massey.a336.myapplication2.data.Note

class NoteAdapter(val clickListener: (note: Note) -> Unit) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()){

    var current = -1L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : NoteViewHolder = NoteViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note, clickListener)
        //Log.i("adapter5", "size=${noteList.size}, pos=$position")

        if (note.select) {
            if(note.pos == current){
                holder.arrow.visibility = View.VISIBLE
            } else {
                holder.arrow.visibility = View.INVISIBLE
            }

            holder.mark.visibility = View.VISIBLE
            //holder.block.setBackgroundColor(Color.parseColor("#DADADA"))
            Log.i("adapter", "_" + position.toString() + " " + note.select)
        } else {
            holder.arrow.visibility = View.INVISIBLE
            holder.mark.visibility = View.INVISIBLE
            //holder.block.setBackgroundColor(Color.parseColor("#FFFFFF"))
            Log.i("adapter", position.toString() + " " + note.select)
        }
    }

    class NoteViewHolder(private val rootview : View)
    : RecyclerView.ViewHolder(rootview) {
        val notePos = rootview.findViewById<TextView>(R.id.pos)
        val noteText = rootview.findViewById<TextView>(R.id.note)
        val block = rootview.findViewById<ConstraintLayout>(R.id.block)
        val mark = rootview.findViewById<TextView>(R.id.mark)
        val arrow = rootview.findViewById<ImageView>(R.id.arrow)

        companion object {
            fun inflateFrom(parent: ViewGroup): NoteViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.list_note, parent, false)
                return NoteViewHolder(view)
            }
        }
        fun bind(item: Note, clickListener: (note: Note) -> Unit) {
            notePos.text = item.pos.toString()
            noteText.text = item.note
            block.setOnClickListener {
                clickListener(item)
            }
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
