package com.example.homosep

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.homosep.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the custom MaterialToolbar as the ActionBar
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Set Tracking (Web) as the default home fragment
        if (savedInstanceState == null) {
            loadFragment(TrackingFragment())
        }

        // Setup Floating Action Button for Navigation
        binding.navFab.setOnClickListener {
            val isVisible = binding.fabMenuContainer.visibility == View.VISIBLE
            binding.fabMenuContainer.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        binding.fabDashboard.setOnClickListener { navigateTo(DashboardFragment()) }
        binding.fabDevices.setOnClickListener { navigateTo(DevicesFragment()) }
        binding.fabRtsp.setOnClickListener { navigateTo(RtspStreamsFragment()) }
        binding.fabAbout.setOnClickListener { navigateTo(AboutFragment()) }
        binding.fabHome.setOnClickListener { navigateTo(TrackingFragment()) }
    }

    private fun navigateTo(fragment: Fragment) {
        binding.fabMenuContainer.visibility = View.GONE
        loadFragment(fragment)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}