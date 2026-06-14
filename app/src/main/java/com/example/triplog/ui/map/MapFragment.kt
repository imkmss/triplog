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

        val korea = LatLng(36.5, 127.5)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(korea, 7f))

        loadMarkers()
    }

    private fun loadMarkers() {
        googleMap.clear()
        val records = dbHelper.getAll()

        var hasMarker = false
        records.forEach { record ->
            if (record.latitude != 0.0 && record.longitude != 0.0) {
                val position = LatLng(record.latitude, record.longitude)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(record.place)
                        .snippet(record.visitDate)
                )
                hasMarker = true
            }
        }

        // 마커가 있으면 첫 번째 마커로 카메라 이동
        if (hasMarker) {
            val firstRecord = records.first { it.latitude != 0.0 && it.longitude != 0.0 }
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(firstRecord.latitude, firstRecord.longitude), 10f
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (::googleMap.isInitialized) {
            loadMarkers()
        }
    }
}