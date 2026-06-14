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
import android.content.Intent
import com.example.triplog.ui.DetailActivity

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var dbHelper: DBHelper
    private var focusLat: Double = 0.0
    private var focusLng: Double = 0.0

    companion object {
        fun newInstance(lat: Double = 0.0, lng: Double = 0.0): MapFragment {
            val fragment = MapFragment()
            val args = Bundle()
            args.putDouble("lat", lat)
            args.putDouble("lng", lng)
            fragment.arguments = args
            return fragment
        }
    }

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
        focusLat = arguments?.getDouble("lat") ?: 0.0
        focusLng = arguments?.getDouble("lng") ?: 0.0

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        // 특정 위치로 포커스할 좌표가 있으면 해당 위치로 이동
        if (focusLat != 0.0 && focusLng != 0.0) {
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(focusLat, focusLng), 12f)
            )
        } else {
            val korea = LatLng(36.5, 127.5)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(korea, 7f))
        }


        // 마커 클릭 시 정보창 표시
        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        // 정보창 클릭 시 상세 화면으로 이동
        googleMap.setOnInfoWindowClickListener { marker ->
            val record = dbHelper.getAll().find { it.place == marker.title }
            record?.let {
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra("record_no", it.no)
                startActivity(intent)
            }
        }

        loadMarkers()
    }

    private fun loadMarkers() {
        googleMap.clear()
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

    override fun onResume() {
        super.onResume()
        if (::googleMap.isInitialized) {
            loadMarkers()
        }
    }
}