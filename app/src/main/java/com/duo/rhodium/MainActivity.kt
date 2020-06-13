package com.duo.rhodium

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*


val PHONE_CODE = 101
val GPS_CODE = 102

class MainActivity : AppCompatActivity() {
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PHONE_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    Toast.makeText(this, "PHONE_ACCESS_GRANTED", Toast.LENGTH_LONG).show()
                    CallAccessBtn.setBackgroundColor(0)
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startActivity(Intent(this, DatasActivity::class.java))
                    }
                } else {
                    Toast.makeText(this, "PHONE_ACCESS_DENIED", Toast.LENGTH_LONG).show()
                }
                return
            }
            GPS_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    Toast.makeText(this, "GPS_ACCESS_GRANTED", Toast.LENGTH_LONG).show()
                    MapAccessBtn.setBackgroundColor(0)
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_PHONE_STATE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startActivity(Intent(this, DatasActivity::class.java))
                    }
                } else {
                    Toast.makeText(this, "GPS_ACCESS_DENIED", Toast.LENGTH_LONG).show()
                }
                return
            }
            else -> {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            MapAccessBtn.setBackgroundColor(0)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            CallAccessBtn.setBackgroundColor(0)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(Intent(this, DatasActivity::class.java))
        }

        MapAccessBtn.setOnClickListener {
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
        }
        CallAccessBtn.setOnClickListener {
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

    }


}
