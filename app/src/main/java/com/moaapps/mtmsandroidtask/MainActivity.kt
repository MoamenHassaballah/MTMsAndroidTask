package com.moaapps.mtmsandroidtask

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.moaapps.mtmsandroidtask.adapters.DestinationsAdapter
import com.moaapps.mtmsandroidtask.adapters.SourceLocationAdapter
import com.moaapps.mtmsandroidtask.databinding.ActivityMainBinding
import com.moaapps.mtmsandroidtask.listeners.DestinationLocationListener
import com.moaapps.mtmsandroidtask.listeners.SourceLocationListener
import com.moaapps.mtmsandroidtask.modules.AppLocation
import com.moaapps.mtmsandroidtask.modules.Status
import com.moaapps.mtmsandroidtask.viewmodels.DestinationsViewModel
import com.moaapps.mtmsandroidtask.viewmodels.DriversViewModel
import com.moaapps.mtmsandroidtask.viewmodels.SourcesViewModel
import java.lang.StringBuilder


class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    LocationListener, SourceLocationListener, DestinationLocationListener, NavigationView.OnNavigationItemSelectedListener {

    companion object{
        private const val TAG = "MainActivity"
    }

    private lateinit var sourcesViewModel: SourcesViewModel
    private lateinit var destinationsViewModel: DestinationsViewModel
    private lateinit var driversViewModel: DriversViewModel

    private lateinit var sourceLocationAdapter: SourceLocationAdapter
    private lateinit var destinationsAdapter: DestinationsAdapter

    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var sourceLocation: AppLocation
    private lateinit var destinationLocation: AppLocation
    private lateinit var loadingDialog:Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sourcesViewModel = ViewModelProvider(this).get(SourcesViewModel::class.java)
        destinationsViewModel = ViewModelProvider(this).get(DestinationsViewModel::class.java)
        driversViewModel = ViewModelProvider(this).get(DriversViewModel::class.java)

        loadingDialog = Dialog(this)
        loadingDialog.setContentView(ProgressBar(this))

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sourcesViewModel.sourcesList.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
                Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, it.msg, Toast.LENGTH_SHORT).show()
                }
                Status.SUCCESS -> {
                    initRV()
                    sourceLocationAdapter = SourceLocationAdapter(it.data!!, this)
                    binding.recyclerView.adapter = sourceLocationAdapter
                }

            }
        })

        destinationsViewModel.destinationsList.observe(this, {
             when(it.status){
                 Status.LOADING ->{
                     binding.progressBar.visibility = View.VISIBLE
                     binding.recyclerView.visibility = View.GONE
                 }

                 Status.ERROR -> {
                     binding.progressBar.visibility = View.GONE
                     Toast.makeText(this, it.msg, Toast.LENGTH_SHORT).show()
                 }

                 Status.SUCCESS -> {
                     initRV()
                     destinationsAdapter = DestinationsAdapter(it.data!!, this)
                     binding.recyclerView.adapter = destinationsAdapter
                 }
             }
        })

        driversViewModel.driversList.observe(this, {
            when (it.status) {
                Status.LOADING -> loadingDialog.show()
                Status.ERROR -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, it.msg, Toast.LENGTH_SHORT).show()
                }
                Status.SUCCESS -> {
                    loadingDialog.dismiss()
                    if (it.data?.isEmpty()!!) {
                        Toast.makeText(this, "No Drivers Found", Toast.LENGTH_SHORT).show()
                    } else {
                        val stringBuilder = StringBuilder()
                        stringBuilder.append("Drivers: ")
                        it.data.forEach { driver ->
                            stringBuilder.append("${driver.name}, ")
                            Log.d(TAG, "Driver: ${driver.name}, lat: ${driver.latitude}, lng: ${driver.longitude}")
                        }
                        stringBuilder.append("are near you")
                        Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        binding.yourLocation.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                moveTop()
                sourcesViewModel.getSourceLocations()
            }
        }

        binding.yourLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (this@MainActivity::sourceLocationAdapter.isInitialized) {
                    sourceLocationAdapter.search(s.toString())
                }
            }

        })

        binding.destination.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                moveTop()
                binding.recyclerView.adapter = null
                binding.progressBar.visibility = View.VISIBLE
            }
        }


        binding.destination.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.length > 3){
                    destinationsViewModel.getDestinations(this@MainActivity, s.toString())
                }
            }
        })


        binding.navView.setNavigationItemSelectedListener(this)
        binding.menu.setOnClickListener {
            binding.drawer.openDrawer(GravityCompat.START)
        }

        binding.requestRide.setOnClickListener {
            if (this::sourceLocation.isInitialized){
                driversViewModel.getNearestDrivers(sourceLocation)
            }else{
                Toast.makeText(this, "Select Your Location", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun initRV() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val cairo = LatLng(30.0444, 31.2357)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cairo))

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 123
            )
        }else{
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, this);
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            updateUserLocation(location)
            mMap.isMyLocationEnabled = true

        }

    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            recreate()
        }
    }


    override fun onLocationChanged(location: Location) {
        updateUserLocation(location)
        locationManager.removeUpdates(this)
    }

    private fun updateUserLocation(location: Location?) {
        if (location != null){
            val userLocation = LatLng(location.latitude, location.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18f))
        }
    }


    private fun moveTop(){
        with(binding){
            if (itemsLayout.visibility == View.GONE){
                topView.animate().setDuration(500)
                    .translationY(-170f)
                topView.animate().setDuration(500)
                    .scaleXBy(0.06f)
                itemsLayout.animate().setDuration(500)
                    .translationY(-170f)
                itemsLayout.visibility = View.VISIBLE
                menu.setImageResource(R.drawable.ic_back)
                menu.setOnClickListener {
                    moveOriginal()
                }
            }
        }

    }

    private fun moveOriginal(){
        hideKeyboard()
        with(binding){
            topView.animate().setDuration(500)
                .translationY(170f)
            topView.animate().setDuration(500)
                .scaleXBy(-0.06f)
            itemsLayout.animate().setDuration(0)
                .translationY(170f)
            itemsLayout.visibility = View.GONE
            menu.setImageResource(R.drawable.ic_menu)
            yourLocation.clearFocus()
            destination.clearFocus()
            menu.setOnClickListener {
                binding.drawer.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = this.currentFocus
        if (view != null)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onSourceLocationSelected(appLocation: AppLocation) {
        this.sourceLocation = appLocation
        moveOriginal()
        binding.yourLocation.setText(appLocation.name)
    }

    override fun onDestinationSelected(destinationLocation: AppLocation) {
        this.destinationLocation = destinationLocation
        moveOriginal()
        binding.destination.setText(destinationLocation.name)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onNavigationItemSelected: ${item.title}")
        binding.drawer.closeDrawer(GravityCompat.START)
        return true
    }
}