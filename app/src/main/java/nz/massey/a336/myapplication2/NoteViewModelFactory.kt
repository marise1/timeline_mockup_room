package nz.massey.a336.myapplication2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nz.massey.a336.myapplication2.data.NoteDao

class NoteViewModelFactory(private val dao: NoteDao)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            return ListViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}