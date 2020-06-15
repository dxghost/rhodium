package com.example.rhodium

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.popup.view.*


// reference: https://www.raywenderlich.com/230-introduction-to-google-maps-api-for-android-with-kotlin#toc-anchor-011

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    private var locationUpdateState = false

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var circleColor = 0

    val mainHandler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)



        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                updateUi(lastLocation)
            }
        }

        createLocationRequest()

        mainHandler.post(object : Runnable {
            override fun run() {
                measureSignalStrength()
                mainHandler.postDelayed(this, 2000)
            }
        })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationPermission()
        setMapUiSetting()

        mFusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                updateUi(lastLocation)
            }
        }

        mMap.setOnCircleClickListener { circle ->
            showDialog(circle.center)
        }
    }

    private fun setMapUiSetting() {
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.setAllGesturesEnabled(true)
        mMap.isMyLocationEnabled = false
    }

    private fun updateUi(location: Location) {
        var currentLatLng = LatLng(location.latitude, location.longitude)
        var circle = CircleOptions()
            .center(currentLatLng)
            .radius(8.0)
            .fillColor(circleColor)

        circle.clickable(true)
        circle.strokeWidth(0f)
        mMap.addCircle(circle)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))


    }

    private fun getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    private fun startLocationUpdates() {
        mFusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun createLocationRequest() {

        locationRequest = LocationRequest()

        locationRequest.interval = 1000

        locationRequest.fastestInterval = 500
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)


        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())


        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun measureSignalStrength() {
        // your code here
        val rnds = (0..6).random()
        circleColor = getColor(R.color.poor)
        println(rnds)
        if (rnds == 0) {
            circleColor = getColor(R.color.excellent)
        }
        if (rnds == 1) {
            circleColor = getColor(R.color.poor)
        }
        if (rnds == 2) {
            circleColor = getColor(R.color.fair)
        }
        if (rnds == 3) {
            circleColor = getColor(R.color.veryPoor)
        }
        if (rnds == 4) {
            circleColor = getColor(R.color.good)
        }
        if (rnds == 5) {
            circleColor = getColor(R.color.noSignal)
        }
    }

    private fun showDialog(location: LatLng) {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("QOC")
        val mAlertDialog = mBuilder.show()
        mDialogView.dialog_text.setText(location.toString())
        mDialogView.close_btn.setOnClickListener {
            mAlertDialog.dismiss()
        }

    }
}
