package com.example.triplog.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.triplog.data.DBHelper
import com.example.triplog.databinding.ActivityDetailBinding
import com.example.triplog.ui.edit.AddEditActivity

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var dbHelper: DBHelper
    private var recordNo: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHelper(this)
        recordNo = intent.getIntExtra("record_no", -1)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "여행 상세"

        loadRecord()
    }

    private fun loadRecord() {
        if (recordNo == -1) return
        val record = dbHelper.getAll().find { it.no == recordNo } ?: return

        binding.tvPlace.text = record.place
        binding.tvDate.text = record.visitDate
        binding.tvMemo.text = record.memo.ifEmpty { "메모 없음" }

        if (record.photoUri.isNotEmpty()) {
            try {
                binding.ivPhoto.setImageURI(Uri.parse(record.photoUri))
            } catch (e: Exception) {
                binding.ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}