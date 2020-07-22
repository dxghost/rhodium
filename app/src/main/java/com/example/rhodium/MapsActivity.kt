package com.example.rhodium

import android.net.TrafficStats
import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.*
import android.system.Os.close
import android.telephony.*
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.mashape.unirest.http.Unirest
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.popup.view.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt
import kotlin.text.Typography.tm

val techTypes = arrayListOf(
    "NETWORK_TYPE_UNKNOWN",
    "NETWORK_TYPE_GPRS",
    "NETWORK_TYPE_EDGE",
    "NETWORK_TYPE_UMTS",
    "NETWORK_TYPE_CDMA",
    "NETWORK_TYPE_EVDO_0",
    "NETWORK_TYPE_EVDO_A",
    "NETWORK_TYPE_1xRTT",
    "NETWORK_TYPE_HSDPA",
    "NETWORK_TYPE_HSUPA",
    "NETWORK_TYPE_HSPA",
    "NETWORK_TYPE_IDEN",
    "NETWORK_TYPE_EVDO_B",
    "NETWORK_TYPE_LTE",
    "NETWORK_TYPE_EHRPD",
    "NETWORK_TYPE_HSPAP",
    "NETWORK_TYPE_GSM",
    "NETWORK_TYPE_TD_SCDMA",
    "NETWORK_TYPE_IWLAN"
)


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    var dbHandler: DatabaseHandler? = null

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private var locationUpdateState = false

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var circleColor = 0
    private var downloadRate = 0
    private var uploadRate = 0


    val mainHandler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(Intent(this, Permissions::class.java))
        }

        dbHandler = DatabaseHandler(this)

        AndroidNetworking.initialize(applicationContext)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                updateUi(lastLocation)
            }
        }

        createLocationRequest()

        mainHandler.post(object : Runnable {
            override fun run() {
//                measureSignalStrength()
                mainHandler.postDelayed(this, 2000)
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        locationUpdateState = true
        startLocationUpdates()
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
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

        var nodes = dbHandler!!.readMilestones()

        for (node in nodes) {
            var latlong = node.location!!.split(",")
            var targetlocation = LatLng(latlong[0].toDouble(), latlong[1].toDouble())
            drawCircleOnMap(targetlocation, node.color!!.toInt())
        }


    }

    private fun setMapUiSetting() {
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.setAllGesturesEnabled(true)
        mMap.isMyLocationEnabled = false
    }

    private fun drawCircleOnMap(location: LatLng, color: Int) {
        var circle = CircleOptions()
            .center(location)
            .radius(8.0)
            .fillColor(color)

        circle.clickable(true)
        circle.strokeWidth(0f)
        mMap.addCircle(circle)
    }

    // whenever user's location changed, this method will be called with user's new location
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUi(location: Location) {
        var currentLatLng = LatLng(location.latitude, location.longitude)

        // store data in database with passed location as argument
        // if output true , then there is a circle with same location on map so don't need to add
        if (storeDataInDb(location)) {
            return
        }

        // create a circle centered with user's new location
        var circle = CircleOptions()
            .center(currentLatLng)
            .radius(8.0)
            .fillColor(circleColor)

        circle.clickable(true)
        circle.strokeWidth(0f)
        mMap.addCircle(circle)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun storeDataInDb(location: Location): Boolean {

        findViewById<TextView>(R.id.downloadId).text = downloadRate.toString() + " KB/sec"
        findViewById<TextView>(R.id.uploadId).text = uploadRate.toString() + " KB/sec"

        Thread(Runnable {
            measureDownloadRate()
        }).start()

        Thread(Runnable {
            pingToSite()
        }).start()


        var f = initMilestone()
        var currentLatLng = LatLng(location.latitude, location.longitude)
        var currentLatLngString =
            currentLatLng.latitude.toString() + "," + currentLatLng.longitude.toString()

        var cellInfo = dbHandler!!.readMilestoneByLocation(currentLatLngString)

        if (cellInfo.location != null) {
            return true
        }

        f.location = currentLatLng.latitude.toString() + "," + currentLatLng.longitude.toString()
        var sigStrength = f.signalStrength?.toInt() ?: 0
        f.color = measureSignalStrength(sigStrength).toString()
        dbHandler!!.createMilestone(f)
        return false
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

        locationRequest.interval = 3000

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
                        PHONE_CODE
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }

    private fun pingToSite() {
        // this code does not work !
        var first = TrafficStats.getTotalRxPackets()

        val begin = Date().time

        var testUrl =
            "http://downloadly.ir/"

        var res = URL(testUrl)

        var urlConnection = res.openConnection()
        urlConnection.connect()


        var data = res.readBytes()

        val end = Date().time
        var difference = (end - begin).toFloat().div(1000)


        var sec = TrafficStats.getTotalRxPackets()
        var delta = sec - first
        println("Number of packet : " + delta)
        println("Average Download Speed is : " + delta.div(difference).toInt())


    }

    private fun measureUploadRate(file: File, len: Int) {

        var first = TrafficStats.getTotalTxBytes()

        val begin = Date().time
        var url = "http://uupload.ir/process.php"
        // var url = "http://speedtest.tele2.net/upload.php"
        // var url = "http://www.csm-testcenter.org/test"
        try {
            val response = Unirest.post(url)
                .field("file", file)
                .asString()
        } catch (e: Exception) {

        }

        var sec = TrafficStats.getTotalTxBytes()
        var delta = sec - first

        val end = Date().time
        var difference: Float = (end - begin).toFloat().div(1000)
        uploadRate = (delta).toDouble().div(difference * 1000).toInt()


    }

    private fun measureDownloadRate() {

        val begin = Date().time

        var testUrl =
            "https://dl2.soft98.ir/ebook/tondamooze.shabakeh.ebook.rar?1595310728"

        var res = URL(testUrl)

        var urlConnection = res.openConnection()
        urlConnection.connect()


        var dataSize = res.readBytes().size

        val end = Date().time
        var difference: Float = (end - begin).toFloat().div(1000)
        downloadRate = dataSize.toDouble().div(difference * 1000).toInt()

        var data = res.readBytes()

        var file = File(Environment.getExternalStorageDirectory(), "test.zip")
        file.writeBytes(data)
        measureUploadRate(file, data.size)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initMilestone(): Milestone {
        dbHandler = DatabaseHandler(this)

//        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        var netWorkType = techTypes[tm.networkType]
//
//
//        var allCellInfo = tm.allCellInfo
//        var currentCell = allCellInfo.get(0)
//        when (netWorkType) {
//            "NETWORK_TYPE_EDGE" -> {
//                for (cellInfo in allCellInfo) {
//                    if (cellInfo is CellInfoGsm) {
//                        currentCell = cellInfo
//                    }
//                }
//            }
//            "NETWORK_TYPE_GPRS" -> {
//                for (cellInfo in allCellInfo) {
//                    if (cellInfo is CellInfoGsm)
//                        currentCell = cellInfo
//                }
//            }
//            "NETWORK_TYPE_HSPA" -> {
//                for (cellInfo in allCellInfo) {
//                    if (cellInfo is CellInfoWcdma)
//                        currentCell = cellInfo
//                }
//            }
//            "NETWORK_TYPE_UMTS" -> {
//                for (cellInfo in allCellInfo) {
//                    if (cellInfo is CellInfoWcdma)
//                        currentCell = cellInfo
//                }
//            }
//            "NETWORK_TYPE_LTE" -> {
//                for (cellInfo in allCellInfo) {
//                    if (cellInfo is CellInfoLte)
//                        currentCell = cellInfo
//                }
//            }
//            else ->
//                currentCell = allCellInfo[0]
//        }
//
//
        var f = Milestone()
//        var json = getCellInfo(currentCell)
        f.technology = "type"
        f.lac = "LAC"
        f.rxLev = "RxLev"
        f.tac = "TAC"
        f.cinr = "CINR"
        f.plmn = "12"
        f.c1 = "C1"
        f.c2 = "C2"
        f.rsrp = "RSRP"
        f.rsrq = "RSRQ"
        f.rscp = "RSCP"
        f.cellID = "cell_identity"
        f.signalStrength = "3"

        return f
    }

    private fun measureSignalStrength(strength: Int): Int {
        circleColor = getColor(R.color.poor)

        if (strength == 0) {
            circleColor = getColor(R.color.veryPoor)
        }
        if (strength == 1) {
            circleColor = getColor(R.color.poor)
        }
        if (strength == 2) {
            circleColor = getColor(R.color.fair)
        }
        if (strength == 3) {
            circleColor = getColor(R.color.good)
        }
        if (strength >= 4) {
            circleColor = getColor(R.color.excellent)
        }
//        if (streng == 5) {
//            circleColor = getColor(R.color.noSignal)
//        }
        return circleColor
    }


    // this method will be called when user clicks on a circle and gets location of circle
    // will display a popup
    // just fetch record corresponding with location and then display it
    private fun showDialog(location: LatLng) {

        var currentLatLngString = location.latitude.toString() + "," + location.longitude.toString()
        var cellInfo = dbHandler!!.readMilestoneByLocation(currentLatLngString)


        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("Location QOC")
        val mAlertDialog = mBuilder.show()
        mDialogView.location.setText(location.latitude.toString() + "," + location.longitude.toString())

        mDialogView.plmnId.setText(cellInfo.plmn)
        mDialogView.technology.setText(cellInfo.technology)
        mDialogView.cinrId.setText(cellInfo.cinr)
        mDialogView.lacId.setText(cellInfo.lac)
        mDialogView.tacId.setText(cellInfo.tac)
        mDialogView.rxLev.setText(cellInfo.rxLev)
        mDialogView.c1.setText(cellInfo.c1)
        mDialogView.c2.setText(cellInfo.c2)
        mDialogView.rscp.setText(cellInfo.rscp)
        mDialogView.rsrp.setText(cellInfo.rsrp)
        mDialogView.rsrq.setText(cellInfo.rsrq)
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
        map["signal_strength"] = cellSignalGsm.level.toString()
        map["type"] = "2G"
    } else if (cellInfo is CellInfoWcdma) {
        val cellIdentityWcdma = cellInfo.cellIdentity
        val cellSignalWcdma = cellInfo.cellSignalStrength
        map["cell_identity"] = cellIdentityWcdma.cid.toString()
        map["LAC"] = cellIdentityWcdma.lac.toString()
        map["RSCP"] = cellSignalWcdma.dbm.toString()
        map["signal_strength"] = cellSignalWcdma.level.toString()
        map["type"] = "3G"
    } else if (cellInfo is CellInfoLte) {
        val cellIdentityLte = cellInfo.cellIdentity
        val cellSignalLte = cellInfo.cellSignalStrength
        map["cell_identity"] = cellIdentityLte.ci.toString()
        map["TAC"] = cellIdentityLte.tac.toString()
        map["RSRP"] = cellSignalLte.rsrp.toString()
        map["RSRQ"] = cellSignalLte.rsrq.toString()
        map["CINR"] = cellSignalLte.rssnr.toString()
        map["signal_strength"] = cellSignalLte.level.toString()
        map["type"] = "4G"
    }
    return map
}