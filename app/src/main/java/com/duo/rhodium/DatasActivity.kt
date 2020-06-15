package com.duo.rhodium

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_datas.*


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

class DatasActivity : AppCompatActivity() {
    var dbHandler: DatabaseHandler? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datas)
        dbHandler = DatabaseHandler(this)

        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        plmnValue.text = tm.networkOperator
        var netWorkType = techTypes[tm.networkType]
        techValue.setText(netWorkType)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
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

//        TODO Uncomment these to see behaviour
//        var f= Milestone()
//        f.location = "location"
//        f.technology = "3g"
//        dbHandler!!.createMilestone(f)
//        var arr = dbHandler!!.readMilestones()
//        cellInfoValue.text = arr.toString()

        cellInfoValue.text = getCellInfo(currentCell).toString()
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