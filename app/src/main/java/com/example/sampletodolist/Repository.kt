package com.example.sampletodolist

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.sampletodolist.database.Todo
import com.example.sampletodolist.database.TodoDao
import com.example.sampletodolist.database.TodoDatabase
import java.lang.Exception

class Repository(application: Application) {
    private val todoDatabase: TodoDatabase = TodoDatabase.getInstance(application)!!
    private val todoDao: TodoDao = todoDatabase.todoDao()
    private val todos: LiveData<List<Todo>> = todoDao.getAll()

    fun getAll(): LiveData<List<Todo>> {
        return todos
    }

    fun insert(todo: Todo) {
        try {
            val thread = Thread(Runnable {
                todoDao.insert(todo)
            })
            thread.start()
        } catch (e: Exception) { }
    }

    fun delete(todo: Todo) {
        try {
            val thread = Thread(Runnable {
                todoDao.delete(todo)
            })
            thread.start()
        } catch (e: Exception) { }
    }
}