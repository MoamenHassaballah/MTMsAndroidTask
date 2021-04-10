package com.moaapps.mtmsandroidtask.listeners

import com.moaapps.mtmsandroidtask.modules.AppLocation

interface SourceLocationListener {
    fun onSourceLocationSelected(appLocation: AppLocation)
}