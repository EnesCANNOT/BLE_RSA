package com.candroid.ble_rsa

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.candroid.ble_rsa.databinding.DeviceItemBinding

const val deviceName = "HONOR Band 5-C85"
const val deviceMacAddress = "10:E9:53:F4:AC:85"
class DeviceListAdapter(var context: Context, var scannedBleDevices: List<BluetoothDevice>) : RecyclerView.Adapter<DeviceListAdapter.DeviceItemHolder>(){

    private val gattCallback = object : BluetoothGattCallback(){
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // Cihaz bağlandığında yapılacak işlemler
                    Log.i("BLE_RSA Enough", "Connected to GATT server.")
                    Log.i("BLE_RSA Enough", "Attempting to start service discovery: " +
                            gatt?.discoverServices())
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    // Cihazdan bağlantı kesildiğinde yapılacak işlemler
                    Log.i("BLE_RSA Enough", "Disconnected from GATT server.")
                }
            }
        }
    }

    inner class DeviceItemHolder(binding: DeviceItemBinding) : RecyclerView.ViewHolder(binding.root){
        var binding: DeviceItemBinding
        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceItemHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DeviceItemBinding.inflate(layoutInflater, parent, false)
        return DeviceItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return scannedBleDevices.size
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: DeviceItemHolder, position: Int) {
        val device = scannedBleDevices.get(position)
        val card = holder.binding
        card.tvDeviceName.setText("Device Name : " + device.name.toString())
        card.tvMacAddress.setText("Mac Address : " + device.address.toString())

        card.btnConnect.isVisible = (device.address == deviceMacAddress)

        card.btnConnect.setOnClickListener{
            Log.i("BLE RSA Enough", "Button Clicked")
            device.connectGatt(context, false, gattCallback)
            Log.i("BLE_RSA Enough", "Bond State : ${device.bondState}")
            Log.i("BLE_RSA Enough", "Connected to ${device.name} - ${device.address}")
        }
    }
}