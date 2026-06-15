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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.triplog.data.DBHelper
import com.example.triplog.data.TripPhoto
import com.example.triplog.data.TripRecord
import com.example.triplog.databinding.ActivityAddEditBinding
import java.io.File
import java.util.Calendar

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private lateinit var dbHelper: DBHelper
    private lateinit var photoAdapter: PhotoAdapter
    private var editRecord: TripRecord? = null
    private var cameraImageUri: Uri? = null
    private var extractedLatitude: Double = 0.0
    private var extractedLongitude: Double = 0.0
    private val photoList = mutableListOf<TripPhoto>()

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            try {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            extractGpsFromPhoto(uri)
            addPhoto(uri.toString())
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            cameraImageUri?.let { uri ->
                extractGpsFromPhoto(uri)
                addPhoto(uri.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHelper(this)

        photoAdapter = PhotoAdapter(
            photoList,
            onThumbnailSelected = { photo ->
                photoAdapter.updateThumbnail(photo)
            },
            onPhotoDeleted = { photo ->
                photoList.remove(photo)
                photoAdapter.updateList(photoList)
            }
        )

        binding.rvPhotos.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvPhotos.adapter = photoAdapter

        val recordNo = intent.getIntExtra("record_no", -1)
        if (recordNo != -1) {
            editRecord = dbHelper.getAll().find { it.no == recordNo }
            editRecord?.let { loadRecord(it) }
            supportActionBar?.title = "기록 수정"
        } else {
            supportActionBar?.title = "기록 추가"
        }

        binding.etDate.isFocusable = false
        binding.etDate.setOnClickListener { showDatePickerDialog() }
        binding.btnSelectPhoto.setOnClickListener { showPhotoPickerDialog() }
        binding.btnSave.setOnClickListener { saveRecord() }
    }

    private fun addPhoto(uri: String) {
        val isFirst = photoList.isEmpty()
        val photo = TripPhoto(
            id = System.currentTimeMillis().toInt(),
            recordId = editRecord?.no ?: 0,
            photoUri = uri,
            isThumbnail = isFirst
        )
        photoList.add(photo)
        photoAdapter.notifyDataSetChanged()
        binding.rvPhotos.scrollToPosition(photoList.size - 1)
    }

    private fun extractGpsFromPhoto(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val exif = ExifInterface(inputStream)
            val latLong = FloatArray(2)
            if (exif.getLatLong(latLong)) {
                extractedLatitude = latLong[0].toDouble()
                extractedLongitude = latLong[1].toDouble()
            } else {
                extractedLatitude = 0.0
                extractedLongitude = 0.0
                // GPS 정보 없을 때 알림
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("GPS 정보 없음")
                    .setMessage("이 사진에는 GPS 정보가 없어 지도에 표시되지 않습니다.")
                    .setPositiveButton("확인", null)
                    .show()
            }
            val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
            if (dateTime != null) {
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
                binding.etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
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

        val photos = dbHelper.getPhotos(record.no)
        photoList.clear()
        photoList.addAll(photos)
        photoAdapter.notifyDataSetChanged()
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
            this, "${packageName}.fileprovider", photoFile
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
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
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

        val record = TripRecord(
            no = editRecord?.no ?: 0,
            place = place,
            visitDate = date,
            memo = memo,
            photoUri = "",
            latitude = extractedLatitude,
            longitude = extractedLongitude
        )

        val recordId = if (editRecord == null) {
            dbHelper.insert(record)
        } else {
            dbHelper.update(record)
            dbHelper.deletePhotos(record.no)
            record.no.toLong()
        }

        // 사진 저장
        photoList.forEach { photo ->
            dbHelper.insertPhoto(recordId, photo.photoUri, photo.isThumbnail)
        }

        setResult(Activity.RESULT_OK)
        finish()
    }
}