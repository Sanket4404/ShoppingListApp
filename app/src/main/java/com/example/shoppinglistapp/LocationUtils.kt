package com.example.shoppinglistapp

import android.Manifest
import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.location.LocationRequest
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.Priority

import java.util.Locale

class LocationUtils(val context: Context) {

    private val _fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(viewModel: LocationViewModel){
        val locationCallback = object: LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let{
                    val location = LocationData(latitude = it.latitude, longitude = it.longitude)
                    viewModel.updateLocation(location)

                }
            }

        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

        _fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun hasLocationPermission(context: Context): Boolean{
        (return ContextCompat.checkSelfPermission(
            context,
            permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                &&

                (return ContextCompat.checkSelfPermission(
                    context,
                    permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED)

    }

    fun reverseGeocodeLocation(location: LocationData) : String{
        val geocoder = Geocoder(context, Locale.getDefault())
        val coordinate = LatLng(location.latitude, location.longitude)


        return try {
            // Fetch address list for the given latitude and longitude
            val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            // Safely get the first address and return its address line, or return a default string
            addresses?.getOrNull(0)?.getAddressLine(0) ?: "Address Not Found"
        } catch (e: Exception) {
            // Handle any potential exceptions (e.g., network issues or invalid coordinates)
            "Error retrieving address: ${e.localizedMessage}"
        }


    }
}


