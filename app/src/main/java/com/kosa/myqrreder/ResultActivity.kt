package com.kosa.myqrreder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kosa.myqrreder.databinding.ActivityMainBinding
import com.kosa.myqrreder.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val result = intent.getStringExtra("msg") ?: "데이터가 존재하지 않아요~"
        setUi(result)
    }

    private fun setUi(result: String){
        binding.tvContent.text = result
        binding.btnGoBack.setOnClickListener {
            finish()//돌아가기 버튼 누르면 ResultActivity가 끝난대 이 finish 하나면
        }
    }
}