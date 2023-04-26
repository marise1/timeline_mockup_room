package nz.massey.a336.myapplication2

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(val clickListener: (note: Note) -> Unit) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()){

    //var search = false
    var noteMatch = listOf<Note>() //listOf( Note(-1, ""))
        set(value){
            field = value
            //search = true
            for(match in noteMatch){
                match.select = true
            }
            Log.i("setter", value.toString())
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : NoteViewHolder = NoteViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note, clickListener)

/*
        if(search){
            for(match in noteMatch){
                //Log.i("match", match.pos.toString() + " " + match.note)

                if(match.note == note.note){
                    note.select = true
                    Log.i("adapter", "__" + position.toString() + " " + match.pos.toString())
                    break
                }
            }
        }
*/

        holder.block.setOnClickListener {
            clickListener(note)
            //search = false
            note.select = true
            notifyDataSetChanged()
            Log.i("adapter0", "$position " + note.select)
        }

        if (note.select) {
            note.select = false
/*
            if(search){
                holder.mark.visibility = View.VISIBLE
                holder.block.setBackgroundColor(Color.parseColor("#FFFFFF"))
                return
            }
*/
            holder.mark.visibility = View.VISIBLE
            //holder.block.setBackgroundColor(Color.parseColor("#DADADA"))
            Log.i("adapter", "_" + position.toString() + " " + note.select)
        } else {
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
        //var select = false
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
