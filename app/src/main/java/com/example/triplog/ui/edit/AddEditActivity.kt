package com.example.triplog.ui.edit

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.media.ExifInterface
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
    private var extractedLatitude: Double = 0.0
    private var extractedLongitude: Double = 0.0

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = result.data?.data
            selectedPhotoUri?.let { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                binding.ivPhoto.setImageURI(uri)
                extractGpsFromPhoto(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = cameraImageUri
            binding.ivPhoto.setImageURI(selectedPhotoUri)
            selectedPhotoUri?.let { extractGpsFromPhoto(it) }
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

    private fun extractGpsFromPhoto(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val exif = ExifInterface(inputStream)

            // GPS 추출
            val latLong = FloatArray(2)
            if (exif.getLatLong(latLong)) {
                extractedLatitude = latLong[0].toDouble()
                extractedLongitude = latLong[1].toDouble()
            } else {
                extractedLatitude = 0.0
                extractedLongitude = 0.0
            }

            // 날짜 추출
            val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
            if (dateTime != null) {
                // EXIF 날짜 형식: "2024:03:12 15:30:00" → "2024-03-12"
                val exifDate = dateTime.substring(0, 10).replace(":", "-")
                val currentDate = binding.etDate.text.toString()

                if (currentDate.isEmpty() || currentDate != exifDate) {
                    binding.etDate.setText(exifDate)
                }
            }

            inputStream.close()
        } catch (e: Exception) {
            extractedLatitude = 0.0
            extractedLongitude = 0.0
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
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
        extractedLatitude = record.latitude
        extractedLongitude = record.longitude
        if (record.photoUri.isNotEmpty()) {
            selectedPhotoUri = Uri.parse(record.photoUri)
            try {
                contentResolver.takePersistableUriPermission(
                    selectedPhotoUri!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                binding.ivPhoto.setImageURI(selectedPhotoUri)
            } catch (e: Exception) {
                binding.ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }
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
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
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
            photoUri = photoUriString,
            latitude = extractedLatitude,
            longitude = extractedLongitude
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