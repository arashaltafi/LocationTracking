package com.arash.altafi.locationtracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arash.altafi.locationtracking.databinding.ActivityMainBinding
import com.arash.altafi.locationtracking.event.LocationEvent
import com.bumptech.glide.Glide
import com.arash.altafi.locationtracking.service.LocationService
import com.arash.altafi.locationtracking.utils.getCityName
import com.arash.altafi.locationtracking.utils.mapScreenShot
import com.arash.altafi.locationtracking.utils.openGoogleMap
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val foreGroundLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when {
                it.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    checkBackgroundLocation()
                }
            }
        }

    private val postNotificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, LocationService::class.java)
                )
            } else {

            }
        }

    private val backgroundLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                //access background
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    ContextCompat.startForegroundService(
                        this,
                        Intent(this, LocationService::class.java)
                    )
                }
            } else {
                //not access
            }
        }

    private fun checkBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnStartService.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        checkForeGroundPermission()
                    }
                } else {
                    intentToSetting()
                }
            } else {
                checkForeGroundPermission()
            }
        }

        binding.btnStopService.setOnClickListener {
            stopService(Intent(this, LocationService::class.java))
        }

    }

    private fun intentToSetting() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
        )
    }

    private fun checkForeGroundPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            foreGroundLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            ContextCompat.startForegroundService(this, Intent(this, LocationService::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent?) = binding.apply {
        tvLatitude.text = "latitude is ${locationEvent?.latitude}"
        tvLongitude.text = "longitude is ${locationEvent?.longitude}"
        tvPlaceName.text =
            "Location: ${getCityName(locationEvent?.latitude, locationEvent?.longitude)}"

        ivLocation.setOnClickListener {
            if (locationEvent?.latitude != null && locationEvent.longitude != null) {
                openGoogleMap(
                    locationEvent.latitude.toString(),
                    locationEvent.longitude.toString()
                )
            }
        }

        val url = mapScreenShot(
            locationEvent?.latitude ?: 0.0,
            locationEvent?.longitude ?: 0.0
        )
        Glide.with(this@MainActivity).load(url).into(binding.ivLocation)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}