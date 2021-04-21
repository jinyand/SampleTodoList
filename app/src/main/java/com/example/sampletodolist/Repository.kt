package com.example.sampletodolist

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.sampletodolist.database.Todo
import com.example.sampletodolist.database.TodoDao
import com.example.sampletodolist.database.TodoDatabase

class Repository(application: Application) {
    private val todoDatabase: TodoDatabase = TodoDatabase.getInstance(application)!!
    private val todoDao: TodoDao = todoDatabase.todoDao()
    private val todos: LiveData<List<Todo>> = todoDao.getAll()

    fun getAll(): LiveData<List<Todo>> {
        return todos
    }

    fun insert(todo: Todo) {
        todoDao.insert(todo)
    }

    fun delete(todo: Todo) {
        todoDao.delete(todo)
    }
}