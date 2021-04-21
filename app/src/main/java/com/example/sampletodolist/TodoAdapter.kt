package com.example.sampletodolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sampletodolist.database.Todo
import com.example.sampletodolist.databinding.ItemTodoBinding

class TodoAdapter(val todoItemClick: (Todo) -> Unit, val todoItemLongClick: (Todo) -> Unit)
    : RecyclerView.Adapter<TodoAdapter.ViewHolder> () {

    private var todos: List<Todo> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoAdapter.ViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoAdapter.ViewHolder, position: Int) {
        holder.bind(todos[position])
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    inner class ViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(todo: Todo) {
            binding.todo = todo

            binding.root.setOnClickListener {
                todoItemClick(todo)
            }

            binding.root.setOnLongClickListener {
                todoItemLongClick(todo)
                true
            }
        }
    }

    fun setTodos(todos: List<Todo>) {
        this.todos = todos
        notifyDataSetChanged()
    }

}