package com.ahmedkhalifa.motionmix.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume
import com.ahmedkhalifa.motionmix.common.utils.LocationResult

class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val geocoder: Geocoder by lazy {
        Geocoder(context, Locale.getDefault())
    }

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get current location as a formatted address string
     */
    suspend fun getCurrentLocationAddress(): LocationResult {
        return try {
            if (!hasLocationPermission()) {
                return LocationResult.Error("Location permission not granted")
            }

            val location = getCurrentLocation()
            val address = getAddressFromCoordinates(location.latitude, location.longitude)
            LocationResult.Success(address)

        } catch (e: SecurityException) {
            LocationResult.Error("Location permission denied")
        } catch (e: Exception) {
            LocationResult.Error("Failed to get location: ${e.message}")
        }
    }

    /**
     * Get current location coordinates
     */
    private suspend fun getCurrentLocation(): Location = suspendCancellableCoroutine { continuation ->
        try {
            // Try to get last known location first
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        // Request fresh location if last known is null
                        requestFreshLocation { freshLocation ->
                            if (freshLocation != null) {
                                continuation.resume(freshLocation)
                            } else {
                                continuation.resume(
                                    Location("").apply {
                                        latitude = 0.0
                                        longitude = 0.0
                                    }
                                )
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        throw exception
                    }
                }
        } catch (e: SecurityException) {
            if (continuation.isActive) {
                throw e
            }
        }
    }

    /**
     * Request fresh location update
     */
    private fun requestFreshLocation(callback: (Location?) -> Unit) {
        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000L // 10 seconds
            ).apply {
                setMinUpdateIntervalMillis(5000L) // 5 seconds
                setMaxUpdates(1)
            }.build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    super.onLocationResult(locationResult)
                    val location = locationResult.lastLocation
                    callback(location)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            callback(null)
        }
    }

    /**
     * Convert coordinates to readable address
     */
    private suspend fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double
    ): String = suspendCancellableCoroutine { continuation ->
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    val locationString = if (addresses.isNotEmpty()) {
                        buildLocationString(addresses[0])
                    } else {
                        "$latitude, $longitude"
                    }
                    continuation.resume(locationString)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val locationString = if (!addresses.isNullOrEmpty()) {
                    buildLocationString(addresses[0])
                } else {
                    "$latitude, $longitude"
                }
                continuation.resume(locationString)
            }
        } catch (e: Exception) {
            // Fallback to coordinates if geocoding fails
            continuation.resume("$latitude, $longitude")
        }
    }

    /**
     * Build readable location string from address
     */
    private fun buildLocationString(address: Address): String {
        val parts = mutableListOf<String>()

        // Add locality (city)
        address.locality?.let { parts.add(it) }

        // Add admin area (state/province)
        address.adminArea?.let { parts.add(it) }

        // Add country
        address.countryName?.let { parts.add(it) }

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            address.getAddressLine(0) ?: "Unknown Location"
        }
    }

    /**
     * Get required permissions for location access
     */
    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}