package com.example.triplog.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.triplog.R
import com.example.triplog.data.DBHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DBHelper(requireContext())

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 기본 위치를 한국으로 설정
        val korea = LatLng(36.5, 127.5)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(korea, 7f))

        // DB에서 GPS 좌표가 있는 기록만 마커 표시
        val records = dbHelper.getAll()
        records.forEach { record ->
            if (record.latitude != 0.0 && record.longitude != 0.0) {
                val position = LatLng(record.latitude, record.longitude)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(record.place)
                        .snippet(record.visitDate)
                )
            }
        }
    }
}