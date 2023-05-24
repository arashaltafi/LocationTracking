package com.arash.altafi.locationtracking.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import com.arash.altafi.locationtracking.R
import java.net.URLEncoder
import java.util.*

fun Context.mapScreenShot(
    lat: Double, lng: Double, width: Int = 500, height: Int = 500,
    markerUrl: String = "https://static.delta.ir/app/locationIcon.png",
    zoom: Float = 13f
): String {
    val encodeMarkerUrl = URLEncoder.encode(markerUrl, "UTF-8")
    val style = if (isDarkTheme()) "dark-v10" else "streets-v11"
    val mapboxUrl = "https://api.mapbox.com/styles/v1/mapbox/$style/static/"

    return "${mapboxUrl}url-${encodeMarkerUrl}(${lng},${lat})/${lng},${lat},${zoom},0/${width}x${height}?access_token=${
        getString(
            R.string.map_box_token
        )
    }"
}

fun Context.getCityName(location: Location? = null): String {
    return try {
        location?.let {
            var cityName: String?
            val geoCoder = Geocoder(this, Locale.getDefault())
            val address = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
            cityName = address?.get(0)?.adminArea
            if (cityName == null) {
                cityName = address?.get(0)?.locality
                if (cityName == null) {
                    cityName = address?.get(0)?.subAdminArea
                }
            }
            cityName ?: getString(R.string.widget_location_name_unknown)
        } ?: kotlin.run {
            getString(R.string.widget_location_name_unknown)
        }
    } catch (e: Exception) {
        getString(R.string.widget_location_name_unknown)
    }
}

fun Context.getCityName(latitude: Double? = null, longitude: Double? = null): String {
    return try {
        if (latitude != null && longitude != null) {
            var cityName: String?
            val geoCoder = Geocoder(this, Locale.getDefault())
            val address = geoCoder.getFromLocation(latitude, longitude, 1)
            cityName = address?.get(0)?.adminArea
            if (cityName == null) {
                cityName = address?.get(0)?.locality
                if (cityName == null) {
                    cityName = address?.get(0)?.subAdminArea
                }
            }
            cityName ?: getString(R.string.widget_location_name_unknown)
        } else {
            getString(R.string.widget_location_name_unknown)
        }
    } catch (e: Exception) {
        getString(R.string.widget_location_name_unknown)
    }
}

fun Context.openGoogleMap(lat: String, lng: String) {
    try {
        val strUri = "http://maps.google.com/maps?q=loc:$lat,$lng"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(strUri))
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
        ContextCompat.startActivity(this, intent, null)
    } catch (e: ActivityNotFoundException) {
        Log.e("openGoogleMap", "openGoogleMap: ${e.message}")
    }
}

fun Context.openMap(lat: String, lng: String) {
    val intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=loc:$lat,$lng"))
    ContextCompat.startActivity(this, intent, null)
}

fun Context.isDarkTheme(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}