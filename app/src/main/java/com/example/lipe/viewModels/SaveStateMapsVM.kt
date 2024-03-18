package com.example.lipe.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class SaveStateMapsVM : ViewModel() {
    var lat: Double = 0.0
    var long: Double = 0.0
    var zoom: Float = 0f

    fun saveMapState(cameraPosition: CameraPosition) {
        lat = cameraPosition.target.latitude
        long = cameraPosition.target.longitude
        zoom = cameraPosition.zoom
    }

    fun restoreMapState(): CameraPosition {
        return CameraPosition.Builder()
            .target(LatLng(lat, long))
            .zoom(zoom)
            .build()
    }
}