package com.moaapps.mtmsandroidtask.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.moaapps.mtmsandroidtask.R
import com.moaapps.mtmsandroidtask.listeners.DestinationLocationListener
import com.moaapps.mtmsandroidtask.modules.AppLocation


class DestinationsAdapter(private var list: ArrayList<AppLocation>, private val listener:DestinationLocationListener) :
    RecyclerView.Adapter<DestinationsAdapter.Holder>() {

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name:TextView = itemView.findViewById(R.id.name)
        val divider:View = itemView.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val destination = list[position]
        with(holder){
            name.text = destination.name
            itemView.setOnClickListener {
                listener.onDestinationSelected(destination) }

            if (position == (list.size - 1)) divider.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
