package com.moaapps.mtmsandroidtask.viewmodels

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moaapps.mtmsandroidtask.modules.Resource
import com.moaapps.mtmsandroidtask.modules.AppLocation

class DestinationsViewModel : ViewModel() {

    val destinationsList = MutableLiveData<Resource<ArrayList<AppLocation>>>()

    fun getDestinations(context: Context, locationName:String){
        destinationsList.postValue(Resource.loading())
        val geocoder = Geocoder(context)
        val addressList = geocoder.getFromLocationName(locationName, 10)
        val list = ArrayList<AppLocation>()
        if (addressList.isNotEmpty()){
            addressList.forEach {
                val destination = AppLocation(it.getAddressLine(0), it.latitude, it.longitude)
                list.add(destination)
            }
            destinationsList.postValue(Resource.success(list))
        }
    }
}