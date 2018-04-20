package com.example.servicesample

import android.Manifest
import android.Manifest.*
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import java.io.File
import java.io.IOException
import android.graphics.Color
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.LocationProvider
import android.os.Bundle
import android.provider.Settings

class LocationService : Service(), LocationListener {
    private lateinit var internalFile: InternalFile
    private lateinit var locationManager: LocationManager

    override fun onCreate() {
        super.onCreate()

        internalFile = InternalFile(File(filesDir, "log.txt").path)
        locationManager = (getSystemService(Context.LOCATION_SERVICE) as LocationManager)!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        return super.onStartCommand(intent, flags, startId)

        val channelId = "default"
        val title = applicationContext.getString(R.string.app_name)

        // ForegroundにするためNotificationが必要、Contextを設定
        val notificationManager = (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)!!
        val channel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "Silent Notification"
        channel.setSound(null, null)
        channel.enableLights(false)
        channel.lightColor = Color.BLUE
        channel.enableVibration(false)
        notificationManager.createNotificationChannel(channel)

        val notification = Notification.Builder(applicationContext, channelId)
                .setContentText(title)
                .setSmallIcon(android.R.drawable.btn_star)
                .setContentText("GPS")
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setWhen(System.currentTimeMillis())
                .build()

        startForeground(1, notification)

        startGPS()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) {

        val sdf = SimpleDateFormat("MM/dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Tokyo")
        val currentTime = sdf.format(location.time)

        val strBuf = StringBuilder()
        strBuf.append("----------\n")
        strBuf.append("Latitude = ${location.latitude} \n")
        strBuf.append("Longitude = ${location.longitude} \n")
        strBuf.append("Accuracy = ${location.accuracy} \n")
        strBuf.append("Altitude = ${location.altitude} \n")
        strBuf.append("Time = $currentTime\n")
        strBuf.append("Speed = ${location.speed} \n")
        strBuf.append("Bearing = ${location.bearing} \n")
        strBuf.append("----------\n")

        Log.d("debug", strBuf.toString())

        internalFile.write(strBuf.toString())
    }

    override fun onProviderDisabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        var str: String = ""
        when (status) {
            LocationProvider.AVAILABLE ->
                internalFile.write("LocationProvider.AVAILABLE\n")

            LocationProvider.OUT_OF_SERVICE ->
                internalFile.write("LocationProvider.OUT_OF_SERVICE\n")

            LocationProvider.TEMPORARILY_UNAVAILABLE ->
                internalFile.write("LocationProvider.TEMPORARILY_UNAVAILABLE\n")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGPS()
    }

    protected fun startGPS() {
        var strBuf = StringBuffer()
        strBuf.append("Start GPS\n")
        Log.d("debug", strBuf.toString())
        internalFile.write(strBuf.toString())


        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPSを設定するように促す
            enableLocationSettings()
            return
        }

        Log.d("debug", "locationManager.requestLocationUpdates")

        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1, 50f, this)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopGPS() {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        locationManager.removeUpdates(this)
    }

    private fun enableLocationSettings() {
        var settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        application.startActivity(settingsIntent)
    }
}
