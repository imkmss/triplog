package com.example.triplog.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.triplog.MainActivity
import com.example.triplog.R
import com.example.triplog.data.DBHelper
import com.example.triplog.databinding.ActivityDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var dbHelper: DBHelper
    private lateinit var photoAdapter: PhotoViewAdapter
    private var recordNo: Int = -1
    private var recordLatitude: Double = 0.0
    private var recordLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHelper(this)
        recordNo = intent.getIntExtra("record_no", -1)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "여행 상세"

        photoAdapter = PhotoViewAdapter(mutableListOf())
        binding.rvPhotos.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvPhotos.adapter = photoAdapter

        loadRecord()
    }

    private fun loadRecord() {
        if (recordNo == -1) return
        val record = dbHelper.getAll().find { it.no == recordNo } ?: return

        binding.tvPlace.text = record.place
        binding.tvDate.text = record.visitDate
        binding.tvMemo.text = record.memo.ifEmpty { "메모 없음" }

        // 사진 목록 로딩
        val photos = dbHelper.getPhotos(recordNo)
        photoAdapter.updateList(photos.toMutableList())

        // GPS 좌표가 있으면 지도 표시
        if (record.latitude != 0.0 && record.longitude != 0.0) {
            recordLatitude = record.latitude
            recordLongitude = record.longitude
            binding.mapContainer.visibility = View.VISIBLE

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.detailMap) as SupportMapFragment
            mapFragment.getMapAsync(this)

            binding.mapOverlay.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("navigate_to_map", true)
                intent.putExtra("latitude", recordLatitude)
                intent.putExtra("longitude", recordLongitude)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(recordLatitude, recordLongitude)
        googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title(binding.tvPlace.text.toString())
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14f))
        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}