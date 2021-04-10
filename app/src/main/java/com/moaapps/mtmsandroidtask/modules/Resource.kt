package com.moaapps.mtmsandroidtask.modules

class Resource<T>(val status: Status, val data:T?, val msg:String?) {
    companion object{
        fun<T> loading():Resource<T>{
            return Resource(Status.LOADING, null, null)
        }

        fun<T> success(data:T):Resource<T>{
            return Resource(Status.SUCCESS, data, null)
        }

        fun<T> error(msg: String):Resource<T>{
            return error(msg)
        }
    }
}