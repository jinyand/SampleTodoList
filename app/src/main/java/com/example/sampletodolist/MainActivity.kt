package com.example.sampletodolist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sampletodolist.database.Todo
import com.example.sampletodolist.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // 뷰모델
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        setRecyclerView()

    }

    private fun deleteDialog(todo: Todo) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Delete selected item?")
            .setNegativeButton("취소") { _, _ -> }
            .setNeutralButton("삭제") {_, _, ->
                mainViewModel.delete(todo)
            }
        builder.show()
    }

    private fun setRecyclerView() {
        // Set contactItemClick & contactItemLongClick lambda
        // click = put extras & start AddActivity
        // longclick = delete dialog
        val adapter = TodoAdapter ({todo -> goAddActivity(todo)}, {todo -> deleteDialog(todo)})

        binding.apply {
            rvMain.adapter = adapter
            rvMain.layoutManager = LinearLayoutManager(applicationContext)
            rvMain.setHasFixedSize(true) // 아이템의 크기가 변경되지 않는다 = true
        }

        mainViewModel.getAll().observe(this, Observer { todos ->
            adapter.setTodos(todos!!)
            // adpater를 통해 ui 업데이트
        })
    }

    private fun goAddActivity(todo: Todo) {
        val intent = Intent(this, AddActivity::class.java)
        intent.putExtra("title", todo.title)
        intent.putExtra("desc", todo.description)
        intent.putExtra("id", todo.id)
        startActivity(intent)
    }
}