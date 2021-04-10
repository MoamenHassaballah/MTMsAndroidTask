package com.moaapps.mtmsandroidtask.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.moaapps.mtmsandroidtask.R
import com.moaapps.mtmsandroidtask.listeners.SourceLocationListener
import com.moaapps.mtmsandroidtask.modules.AppLocation


class SourceLocationAdapter(private var list: ArrayList<AppLocation>, private val listener:SourceLocationListener) :
    RecyclerView.Adapter<SourceLocationAdapter.Holder>() {

    private val originalList = ArrayList<AppLocation>()
    init {
        this.originalList.addAll(list)
    }

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
        val sourceLocation = list[position]
        with(holder){
            name.text = sourceLocation.name
            itemView.setOnClickListener {
                listener.onSourceLocationSelected(sourceLocation) }
            if (position == (list.size - 1)) divider.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun search(query:String){
        list.clear()
        if (query.isEmpty()){
            list.addAll(originalList)
        }else{
            originalList.forEach {
                if (it.name.contains(query, ignoreCase = true)) list.add(it)
            }
        }
        notifyDataSetChanged()
    }
}
