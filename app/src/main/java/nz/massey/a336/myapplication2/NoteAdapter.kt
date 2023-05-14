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

    //var noteList = listOf<Note>()
/*
    val list = context.findViewById<RecyclerView>(R.id.listNote)
    val layoutManager = LinearLayoutManager(context)
    init {
        list.adapter = this
        list.layoutManager = layoutManager
    }
*/
/*
    val txtTotal = context.findViewById<TextView>(R.id.txtTotal)
    val btnUp = context.findViewById<ImageButton>(R.id.btnUp)
    val btnDown = context.findViewById<ImageButton>(R.id.btnDown)
*/

    //var matchTotal = 0
    //var nextMatchPos = -1
    //var i = 0
    var current = -1L
    //var search = false
/*
    var noteMatch = listOf<Note>()
        set(value){
            field = value
            if(value.size != 0){
                i=0
                matchTotal = noteMatch.size
                search = true
                current = noteMatch[i].pos
                //list.scrollToPosition(noteList.indexOf(noteMatch[i]))
                //txtTotal.text = "1/$matchTotal"
                //Log.i("setter", "match pos=${noteMatch[i].pos}, ${noteList.indexOf(noteMatch[i])}")
                //Log.i("setter", value.toString())
                notifyDataSetChanged()
            } else{
                Log.i("setter", "no match")
            }
        }
*/

/*
    fun cancelSearch() {
        current = -1
        search = false
        notifyDataSetChanged()
    }
*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : NoteViewHolder = NoteViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note, clickListener)
        //Log.i("adapter5", "size=${noteList.size}, pos=$position")

/*
        if(search){
            for(match in noteMatch){
                //Log.i("match", match.pos.toString() + " " + match.note)
                if(match.note == note.note){
                    note.select = true
                    Log.i("adapter3", "__" + position.toString() + " " + match.pos.toString())
                    break
                }
            }
        }
*/

/*
        btnUp.setOnClickListener{
            if(search){
                i--
                if(i == -1) i = noteMatch.size-1
                current = noteMatch[i].pos
                nextMatchPos = noteList.indexOf(noteMatch[i])
                list.scrollToPosition(nextMatchPos)
                txtTotal.text = "${i+1}/$matchTotal"
                notifyDataSetChanged()
                Log.i("adapter4_up", "i = $i, current = $current")
            }
        }

        btnDown.setOnClickListener{
            if(search){
                i++
                if(i == noteMatch.size) i=0
                current = noteMatch[i].pos
                nextMatchPos = noteList.indexOf(noteMatch[i])
                list.scrollToPosition(nextMatchPos)
                txtTotal.text = "${i+1}/$matchTotal"
                notifyDataSetChanged()
                Log.i("adapter4_down", "nextMatchPos=$nextMatchPos, i=$i, current=$current, "
                        + "first=$firstPos, last=$lastPos")
            }
        }
*/

/*
        holder.block.setOnClickListener {
            clickListener(note)
            note.select = true
            current = -1
            search = false
            notifyDataSetChanged()
            Log.i("adapter0", "__$position " + note.select)
        }
*/

        if (note.select) {
            //note.select = false

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
