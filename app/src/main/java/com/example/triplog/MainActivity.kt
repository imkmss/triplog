package com.example.triplog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.triplog.databinding.ActivityMainBinding
import com.example.triplog.ui.home.HomeFragment
import com.example.triplog.ui.map.MapFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_map -> replaceFragment(MapFragment())
            }
            true
        }

        // 상세 화면에서 지도로 이동 시
        if (intent.getBooleanExtra("navigate_to_map", false)) {
            val lat = intent.getDoubleExtra("latitude", 0.0)
            val lng = intent.getDoubleExtra("longitude", 0.0)
            val mapFragment = MapFragment.newInstance(lat, lng)
            binding.bottomNav.selectedItemId = R.id.nav_map
            replaceFragment(mapFragment)
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}