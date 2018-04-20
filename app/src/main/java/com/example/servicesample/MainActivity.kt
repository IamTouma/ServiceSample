package com.example.servicesample

import android.Manifest.*
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.content.ContextCompat
import android.widget.Toast
import java.io.File


class MainActivity : AppCompatActivity() {

    private val REQUEST_MULTI_PERMISSIONS = 101
    private var isAllowed = false
    private var internalFile: InternalFile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        internalFile = InternalFile(File(filesDir, "log.txt").path)

        checkPermissions()
    }

    private fun checkPermissions() {

        val permissionCheck = ContextCompat.checkSelfPermission(this,
                permission.ACCESS_FINE_LOCATION)

        var reqPermissions = mutableListOf<String>()


        // 既に許可している
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            reqPermissions.add(permission.ACCESS_FINE_LOCATION)
        }

        if (!reqPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    reqPermissions.toTypedArray(),
                    REQUEST_MULTI_PERMISSIONS)
            return
        }

        startLocationService()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_MULTI_PERMISSIONS) {
            if (grantResults.count() > 0) {
//                for (i in 0 until permissions.length) {
                for (i in permissions.indices)
                    if (permissions[i] == permission.ACCESS_FINE_LOCATION) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            isAllowed = true
                        } else {
                            // それでも拒否された時の対応
                            val toast = Toast.makeText(this,
                                    "位置情報の許可がないので計測できません", Toast.LENGTH_SHORT)
                            toast.show()

//                            enableLocationSettings()
                        }
                    // 今回は使いません
//                    } else if (permissions[i] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
//                        if (grantResults[i] === PackageManager.PERMISSION_GRANTED) {
//                            isAllowedExternalWrite = true
//                        } else {
//                            // それでも拒否された時の対応
//                            val toast = Toast.makeText(this,
//                                    "外部書込の許可がないので書き込みできません", Toast.LENGTH_SHORT)
//                            toast.show()
//
//                        }
//                    }
                }

                startLocationService()

            }
        }
    }

    private fun startLocationService() {
//        setContentView(R.layout.activity_main)

        button_start.setOnClickListener {
            startForegroundService(Intent(application, LocationService::class.java))
            //Activityを終了させる
            finish()
        }

        button_stop.setOnClickListener {
            stopService(Intent(application, LocationService::class.java))
        }

        button_log.setOnClickListener {
            log_text.text = internalFile!!.read()
        }
    }

    private fun enableLocationSettings() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }
}
