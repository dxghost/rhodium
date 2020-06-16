package com.example.rhodium

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.*
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    var dbHandler: DatabaseHandler? = null
    var id = 0
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    val PHONE_CODE = 101
    val GPS_CODE = 102

    private var locationUpdateState = false

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var circleColor = 0

    val mainHandler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        getCallPermission()
        dbHandler = DatabaseHandler(this)

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


    private fun getCallPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                GPS_CODE
            )
        } else {
            Toast.makeText(this, "GPS_ACCESS_ALREADY_ACQUIRED", Toast.LENGTH_SHORT).show()
        }


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                PHONE_CODE
            )
        } else {
            Toast.makeText(this, "CALL_ACCESS_ALREADY_ACQUIRED", Toast.LENGTH_SHORT).show()
        }

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



    // whenever user's location changed, this method will be called with user's new location
    private fun updateUi(location: Location) {

        // if use `currentLatLng.toString() it will return something like this : LatLng(53.000212321, 39.31543545)
        var currentLatLng = LatLng(location.latitude, location.longitude)

        // create a circle centered with user's new location
        var circle = CircleOptions()
            .center(currentLatLng)
            .radius(8.0)
            .fillColor(circleColor)

        circle.clickable(true)
        circle.strokeWidth(0f)
        mMap.addCircle(circle)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))

        // store data in database with passed location as argument
        storeDataInDb(location)
    }

    private fun storeDataInDb(location: Location) {

        // call a function that fetches network cell

        var f = Milestone()
        var currentLatLng = LatLng(location.latitude, location.longitude)

        f.location = currentLatLng.toString()
        f.technology = "3g"
        f.signalStrength = currentLatLng.latitude.toString()
        f.lac = currentLatLng.longitude.toString()
        f.rxLev = currentLatLng.longitude.toString()
        f.tac = currentLatLng.latitude.toString()
        f.rac = currentLatLng.longitude.toString()
        f.plmn = currentLatLng.latitude.toString()
        f.c1 = currentLatLng.longitude.toString()
        f.c2 = currentLatLng.latitude.toString()
        f.rscp = currentLatLng.longitude.toString()
        f.rsrp = currentLatLng.latitude.toString()
        f.rsrq = currentLatLng.longitude.toString()
        f.ecno = currentLatLng.latitude.toString()
        f.cellID = currentLatLng.longitude.toString()
        f.rxLev = currentLatLng.latitude.toString()
        dbHandler!!.createMilestone(f)
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
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(
                        this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }

    private fun getCellInfoJson() : Any {
        dbHandler = DatabaseHandler(this)

        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var netWorkType = techTypes[tm.networkType]

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return 0
        }

        var allCellInfo = tm.allCellInfo
        var currentCell = allCellInfo.get(0)
        when (netWorkType) {
            "NETWORK_TYPE_EDGE" -> {
                for (cellInfo in allCellInfo) {
                    if (cellInfo is CellInfoGsm) {
                        currentCell = cellInfo
                    }
                }
            }
            "NETWORK_TYPE_GPRS" -> {
                for (cellInfo in allCellInfo) {
                    if (cellInfo is CellInfoGsm)
                        currentCell = cellInfo
                }
            }
            "NETWORK_TYPE_HSPA" -> {
                for (cellInfo in allCellInfo) {
                    if (cellInfo is CellInfoWcdma)
                        currentCell = cellInfo
                }
            }
            "NETWORK_TYPE_UMTS" -> {
                for (cellInfo in allCellInfo) {
                    if (cellInfo is CellInfoWcdma)
                        currentCell = cellInfo
                }
            }
            "NETWORK_TYPE_LTE" -> {
                for (cellInfo in allCellInfo) {
                    if (cellInfo is CellInfoLte)
                        currentCell = cellInfo
                }
            }
            else ->
                currentCell = allCellInfo[0]
        }


        var f = Milestone()
        var currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
        f.location = currentLatLng.toString()
        f.technology = "3g"
        f.lac = "1234"
        f.rxLev = "1234"
        f.tac = "1234"
        f.rac = "1234"
        f.plmn = "1234"
        f.c1 = "1234"
        f.c2 = "1234"
        dbHandler!!.createMilestone(f)
        var arr = dbHandler!!.readMilestones()
        var json = getCellInfo(currentCell)
        return json
    }

    private fun measureSignalStrength() {
        val rnds = (0..6).random()
        circleColor = getColor(R.color.poor)

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


    // this method will be called when user clicks on a circle and gets location of circle
    // will display a popup
    // just fetch record corresponding with location and then display it
    private fun showDialog(location: LatLng) {
        var cellInfo  = dbHandler!!.readMilestoneByLocation(location.toString())


        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("Location QOC")
        val mAlertDialog = mBuilder.show()
        mDialogView.location.setText(location.latitude.toString() + "," + location.longitude.toString())

        mDialogView.plmnId.setText(cellInfo.plmn)
        mDialogView.technology.setText(cellInfo.technology)
        mDialogView.racId.setText(cellInfo.rac)
        mDialogView.lacId.setText(cellInfo.lac)
        mDialogView.tacId.setText(cellInfo.tac)
        mDialogView.rxLev.setText(cellInfo.rxLev)
        mDialogView.c1.setText(cellInfo.c1)
        mDialogView.c2.setText(cellInfo.c2)
        mDialogView.rscp.setText(cellInfo.rscp)
        mDialogView.rsrp.setText(cellInfo.rsrp)
        mDialogView.rsrq.setText(cellInfo.rsrq)
        mDialogView.ecno.setText(cellInfo.ecno)
        mDialogView.cellId.setText(cellInfo.cellID)
        mDialogView.signalStrength.setText(cellInfo.signalStrength)

        mDialogView.close_btn.setOnClickListener {
            mAlertDialog.dismiss()
        }

    }
}


@RequiresApi(Build.VERSION_CODES.O)
private fun getCellInfo(cellInfo: CellInfo): HashMap<Any?, String?> {
    var map = hashMapOf<Any?, String?>()
    if (cellInfo is CellInfoGsm) {
        val cellIdentityGsm = cellInfo.cellIdentity
        val cellSignalGsm = cellInfo.cellSignalStrength
        map["cell_identity"] = cellIdentityGsm.cid.toString()
        map["LAC"] = cellIdentityGsm.lac.toString()
        map["RxLev"] = cellSignalGsm.asuLevel.toString()
        map["Level_of_strength"] = cellSignalGsm.level.toString()
        map["type"] = "2"
    } else if (cellInfo is CellInfoWcdma) {
        val cellIdentityWcdma = cellInfo.cellIdentity
        val cellSignalWcdma = cellInfo.cellSignalStrength
        map["cell_identity"] = cellIdentityWcdma.cid.toString()
        map["LAC"] = cellIdentityWcdma.lac.toString()
        map["RSCP"] = cellSignalWcdma.dbm.toString()
        map["Level_of_strength"] = cellSignalWcdma.level.toString()
        map["type"] = "3"
    } else if (cellInfo is CellInfoLte) {
        val cellIdentityLte = cellInfo.cellIdentity
        val cellSignalLte = cellInfo.cellSignalStrength
        map["cell_identity"] = cellIdentityLte.ci.toString()
        map["TAC"] = cellIdentityLte.tac.toString()
        map["RSRP"] = cellSignalLte.rsrp.toString()
        map["RSRQ"] = cellSignalLte.rsrq.toString()
        map["CINR"] = cellSignalLte.rssnr.toString()
        map["Level_of_strength"] = cellSignalLte.level.toString()
        map["type"] = "4"
    }
    return map
}