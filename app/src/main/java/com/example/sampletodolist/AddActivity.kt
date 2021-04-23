package com.example.sampletodolist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.sampletodolist.database.Todo
import com.example.sampletodolist.databinding.ActivityAddBinding

class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding
    private val mainViewModel: MainViewModel by viewModels()
    private var id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add)

        if (intent != null && intent.hasExtra("title") && intent.hasExtra("desc") && intent.hasExtra("id")) {
            binding.etTitle.setText(intent.getStringExtra("title"))
            binding.etDesc.setText(intent.getStringExtra("desc"))
            id = intent.getIntExtra("id", -1)
        }

        binding.apply {
            btnDone.setOnClickListener {
                val title = etTitle.text.toString()
                val desc = etDesc.text.toString()

                if (etTitle.text.isNotEmpty() && etDesc.text.isNotEmpty()) {
                    val todo = Todo(id, title, desc)
                    Log.d("테스트", title)
                    mainViewModel.insert(todo)
                    finish()
                } else {
                    Toast.makeText(applicationContext, "빈 칸을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}