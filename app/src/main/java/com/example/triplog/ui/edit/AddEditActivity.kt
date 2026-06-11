package com.example.triplog.ui.edit

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.triplog.data.DBHelper
import com.example.triplog.data.TripRecord
import com.example.triplog.databinding.ActivityAddEditBinding
import java.io.File
import java.util.Calendar

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private lateinit var dbHelper: DBHelper
    private var editRecord: TripRecord? = null
    private var selectedPhotoUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = result.data?.data
            binding.ivPhoto.setImageURI(selectedPhotoUri)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = cameraImageUri
            binding.ivPhoto.setImageURI(selectedPhotoUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHelper(this)

        val recordNo = intent.getIntExtra("record_no", -1)
        if (recordNo != -1) {
            editRecord = dbHelper.getAll().find { it.no == recordNo }
            editRecord?.let { loadRecord(it) }
            supportActionBar?.title = "기록 수정"
        } else {
            supportActionBar?.title = "기록 추가"
        }

        // 날짜 필드 클릭 시 DatePickerDialog 표시
        binding.etDate.isFocusable = false
        binding.etDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnSelectPhoto.setOnClickListener {
            showPhotoPickerDialog()
        }

        binding.btnSave.setOnClickListener {
            saveRecord()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        // 기존 날짜가 있으면 파싱해서 캘린더에 설정
        val existingDate = binding.etDate.text.toString()
        if (existingDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            val parts = existingDate.split("-")
            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.etDate.setText(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadRecord(record: TripRecord) {
        binding.etPlace.setText(record.place)
        binding.etDate.setText(record.visitDate)
        binding.etMemo.setText(record.memo)
        if (record.photoUri.isNotEmpty()) {
            selectedPhotoUri = Uri.parse(record.photoUri)
            binding.ivPhoto.setImageURI(selectedPhotoUri)
        }
    }

    private fun showPhotoPickerDialog() {
        val options = arrayOf("카메라로 촬영", "갤러리에서 선택")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("사진 선택")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val photoFile = File(getExternalFilesDir(null), "photo_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun saveRecord() {
        val place = binding.etPlace.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val memo = binding.etMemo.text.toString().trim()

        if (place.isEmpty()) {
            binding.etPlace.error = "여행지명을 입력해주세요"
            return
        }
        if (date.isEmpty()) {
            binding.etDate.error = "날짜를 선택해주세요"
            return
        }

        val photoUriString = selectedPhotoUri?.toString() ?: editRecord?.photoUri ?: ""

        val record = TripRecord(
            no = editRecord?.no ?: 0,
            place = place,
            visitDate = date,
            memo = memo,
            photoUri = photoUriString
        )

        if (editRecord == null) {
            dbHelper.insert(record)
        } else {
            dbHelper.update(record)
        }

        setResult(Activity.RESULT_OK)
        finish()
    }
}