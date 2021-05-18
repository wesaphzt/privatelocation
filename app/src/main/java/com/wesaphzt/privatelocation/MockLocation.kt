package com.wesaphzt.privatelocation

import android.location.Location
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import java.sql.Time
import java.util.*

/**
 *Date: 2021/4/27
 *Author: 锅得铁
 *#
 */
class MockLocation {
    val TAG = "MockLocation"
    val rand = Random()
    var azimuth = 300//往哪儿走
//    var startTime: Long = 1619366400//2021-04-26 00:00:00 ，一天86 400秒
//    val endTime: Long = 1619452800//2021-04-27 00:00:00 ，一天86 400秒
    var speed: Float = 0.2f//以米为单位
    var anchorLat = 37.12 //锚点Lat
    var anchorLon = 79.94//锚点Lon
    var range = 0.05 //范围 KM
    var step = 0.5 / 1000  //步距 0.6M
    var timeInterval :Long = 5 //时间间隔5秒
    val seeds = arrayOf(360, 180, 90, 45, 30, 15, 5)
    var goCount = 0

     fun location(listener: LocationListener) {
        var currentLat: Double = anchorLat
        var currentLon: Double = anchorLon
        var index = 0
//        while (startTime < endTime) {
        while (true){
            index++
            //val nextPoint = nextPoint(currentLat, currentLon, seeds[rand.nextInt(seeds.size)], step)
            val nextPoint = nextPoint(currentLat, currentLon, rand.nextInt(330), step)
            currentLat = nextPoint[0]
            currentLon = nextPoint[1]
            val location = LocationPoint(currentLat, currentLon)
            listener.onChange(currentLat ,currentLon )
            if (outOfRange(currentLon, anchorLon, range, currentLat, anchorLat)) {
                currentLat = anchorLat
                currentLon = anchorLon
                Log.i(TAG, "GO BACK")

                continue
            }
            //startTime += timeInterval
            Thread.sleep(timeInterval*100);

            Log.i(TAG, "${index}次")

        }


    }

    private fun outOfRange(currentLon: Double, anchorLon: Double, range: Double, currentLat: Double, anchorLat: Double): Boolean {
        if (currentLon > anchorLon + range || currentLon < anchorLon - range) {
            return return true
        }
        if (currentLat > anchorLat + range || currentLat < anchorLat - range) {
            return return true
        }
        return false
    }


    /**
     *
     */

    fun nextPoint(lat: Double, lon: Double, azimuth: Int, radius: Double): Array<Double> {
        if (azimuth > 360 || azimuth < 1) {
            return arrayOf(lat, lon)
        }
        goCount++
        val isBlack = goCount > 300
        val nextLon = if (isBlack) lon + radius * cos(azimuth) else lon - radius * cos(azimuth)
        val nextLat = if (isBlack) lat + radius * sin(azimuth) else lat - radius * sin(azimuth)
        if (isBlack) goCount = 0
        return arrayOf(nextLat, nextLon)
    }

    fun sin(num: Int): Double {
        return Math.sin(num * Math.PI / 180)
    }

    fun cos(num: Int): Double {
        return Math.cos(num * Math.PI / 180)
    }


}
class LocationPoint(lat:Double,lon: Double)
interface  LocationListener{
    fun  onChange( lat:Double, lon: Double):Unit
}
