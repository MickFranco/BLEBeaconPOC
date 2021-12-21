package com.mrzhevskyi.beaconpoc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.minew.beaconplus.sdk.MTPeripheral
import com.mrzhevskyi.beaconpoc.*
import com.mrzhevskyi.beaconpoc.databinding.ItemBeaconBinding
import timber.log.Timber
import java.lang.NumberFormatException

class BeaconAdapter(private val onClickListener: (MTPeripheral) -> Unit) : RecyclerView.Adapter<BeaconAdapter.BeaconHolder>() {

    class BeaconHolder(val binding: ItemBeaconBinding): RecyclerView.ViewHolder(binding.root)

    private val differ: AsyncListDiffer<MTPeripheral> =
        AsyncListDiffer(this, object : DiffUtil.ItemCallback<MTPeripheral>() {
            override fun areItemsTheSame(oldItem: MTPeripheral, newItem: MTPeripheral): Boolean {
                return oldItem.mMTFrameHandler.mac == newItem.mMTFrameHandler.mac
            }

            override fun areContentsTheSame(oldItem: MTPeripheral, newItem: MTPeripheral): Boolean {
                return false
            }
        })

    private val allBeacons: HashSet<MTPeripheral> = HashSet()
    private var latestList = mutableListOf<MTPeripheral>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeaconHolder {
        val inflater = LayoutInflater.from(parent.context)
        return BeaconHolder(ItemBeaconBinding.inflate(inflater,parent,false))
    }

    override fun onBindViewHolder(holder: BeaconHolder, position: Int) {
        val beacon = differ.currentList[position]
        holder.binding.tvName.text = beacon.mMTFrameHandler.name
        holder.binding.tvMac.text = beacon.mMTFrameHandler.mac
        holder.binding.tvBattery.text = beacon.mMTFrameHandler.battery.toString()+"%"

        if(latestList.contains(beacon)) {
            holder.binding.tvRssi.text = beacon.mMTFrameHandler.rssi.toString()
        }else{
            holder.binding.tvRssi.text = "Off"
        }


        holder.binding.etLimit.setText(beacon.getRssiLimit().toString())
        holder.binding.swBeacon.isChecked = beacon.isTracked()
        holder.binding.swBeacon.setOnCheckedChangeListener { p0, checked ->
            if(checked) {
                try {
                    beacon.track(holder.binding.etLimit.text.toString().toInt())
                }catch (e: Exception){e.printStackTrace()}
            }
            else beacon.stopTracking()

        }

        holder.itemView.setOnClickListener {
            onClickListener.invoke(beacon)
        }
    }

    override fun getItemCount(): Int {
       return differ.currentList.size
    }

    fun setValues(list: List<MTPeripheral>){
        latestList = list.toMutableList()
        allBeacons.addAll(list)
        val currentList = list.toMutableList()
        for(b in allBeacons){
            if(!currentList.contains(b)) currentList.add(b)
        }
        differ.submitList(currentList)
    }
}