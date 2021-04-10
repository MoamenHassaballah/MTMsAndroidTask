package com.moaapps.mtmsandroidtask.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.moaapps.mtmsandroidtask.modules.Resource
import com.moaapps.mtmsandroidtask.modules.AppLocation
import kotlin.collections.ArrayList

class SourcesViewModel : ViewModel() {

    val sourcesList = MutableLiveData<Resource<ArrayList<AppLocation>>>()

    fun getSourceLocations(){
        sourcesList.postValue(Resource.loading())
        FirebaseFirestore.getInstance().collection("Source")
            .get().addOnCompleteListener {
                if (it.isSuccessful){
                    val list = ArrayList<AppLocation>()
                    for (snapshot in it.result!!){
                        list.add(snapshot.toObject(AppLocation::class.java))
                    }
                    list.sortWith(compareBy(AppLocation::name))
                    sourcesList.postValue(Resource.success(list))
                }else{
                    sourcesList.postValue(Resource.error(it.exception?.message!!))
                }
            }
    }
}