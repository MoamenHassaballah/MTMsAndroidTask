package com.moaapps.mtmsandroidtask.viewmodels

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.moaapps.mtmsandroidtask.modules.AppLocation
import com.moaapps.mtmsandroidtask.modules.Driver
import com.moaapps.mtmsandroidtask.modules.Resource

class DriversViewModel : ViewModel() {

    val driversList = MutableLiveData<Resource<ArrayList<Driver>>>()

    fun getNearestDrivers(s: AppLocation){
        driversList.postValue(Resource.loading())

        FirebaseFirestore.getInstance().collection("Drivers")
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val list = ArrayList<Driver>()
                        task.result?.forEach {
                            val driver = it.toObject(Driver::class.java)

                            val sourceLocation = Location("")
                            sourceLocation.latitude = s.latitude
                            sourceLocation.longitude = s.longitude

                            val driverLocation = Location("")
                            driverLocation.latitude = driver.latitude
                            driverLocation.longitude = driver.longitude

                            if ((sourceLocation.distanceTo(driverLocation)/1000) < 10){
                                list.add(driver)
                            }
                        }
                        driversList.postValue(Resource.success(list))
                    }else{
                        driversList.postValue(Resource.error(task.exception?.message!!))
                    }
                }
    }
}