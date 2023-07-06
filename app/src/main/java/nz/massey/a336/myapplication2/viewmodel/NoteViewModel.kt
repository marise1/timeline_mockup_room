package nz.massey.a336.myapplication2.viewmodel

import android.graphics.Color
import android.provider.Settings.Global.getString
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import nz.massey.a336.myapplication2.R
import nz.massey.a336.myapplication2.data.Note
import nz.massey.a336.myapplication2.data.NoteDao

class NoteViewModel(val dao: NoteDao) : ViewModel() {

    var search = false
    var word = ""

    var cursorPos = 0
    var current = -1
    var i = 0
    var txtTotal = ""
    var matchTotal = 0
    var matchList = listOf<MatchResult>()
    var highlightedNote = SpannableString("")

    var note = ""
    var pos = 0L

    fun btnDown(){
        if(matchList.isEmpty()) return
        i++
        if(i == matchTotal) i=0
        txtTotal = "${i+1}/$matchTotal"
        current = matchList[i].range.start
        highlightNote()
    }
    fun btnUp(){
        if(matchList.isEmpty()) return
        i--
        if(i == -1) i = matchTotal-1
        txtTotal = "${i+1}/$matchTotal"
        current = matchList[i].range.start
        highlightNote()
    }

    fun match(){
        highlightedNote = SpannableString(note)

        var str = ""
        val metacharString = "()[]{}^$.\\?*+|"
        for(char in word){
            if(metacharString.contains(char)){
                str = word.replace(""+char, "\\"+char)
            }
        }

        val pattern = str.toRegex()
        matchList = pattern.findAll(note).toList()

        if(matchList.isEmpty()){
            txtTotal = "0/0"
            return
        }
        matchTotal = matchList.size
        txtTotal = "1/${matchTotal}"
        current = matchList[0].range.start

        highlightNote()
    }

    fun highlightNote(){
        for(match in matchList){
            Log.i("match3", "pos=${match.range}")
            if(match.range.start == current){
                highlightedNote.setSpan(BackgroundColorSpan(Color.parseColor("#FFB13C")), match.range.start, match.range.endInclusive+1,0)
                continue
            }
            highlightedNote.setSpan(BackgroundColorSpan(Color.parseColor("#80FFB13C")), match.range.start, match.range.endInclusive+1,0)
        }
    }

    fun clearHighlight(){
        val spanList = highlightedNote.getSpans(0, highlightedNote.length, BackgroundColorSpan::class.java)
        spanList.forEach {
            highlightedNote.removeSpan(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("viewmodel2", "oncleared")
    }

}