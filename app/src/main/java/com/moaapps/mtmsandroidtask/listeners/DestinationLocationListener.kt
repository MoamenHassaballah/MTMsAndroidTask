package com.moaapps.mtmsandroidtask.listeners

import com.moaapps.mtmsandroidtask.modules.AppLocation

interface DestinationLocationListener {
    fun onDestinationSelected(destinationLocation: AppLocation)
}