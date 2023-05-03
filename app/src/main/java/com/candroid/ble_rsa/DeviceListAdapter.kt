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
import java.util.UUID

//const val DEVICE_NAME = "HONOR Band 5-C85"
//const val DEVICE_ADDRESS = "10:E9:53:F4:AC:85"
//const val CHARACTERISTIC_UUID = "0000fee1-0000-1000-8000-00805f9b34fb"
//const val SERVICE_UUID = "0000fee0-0000-1000-8000-00805f9b34fb"

private const val DEVICE_ADDRESS = "DD:0D:30:23:4A:9B"
private const val CHARACTERISTIC_UUID = "0000FFF2-0000-1000-8000-00805F9B34FB"
private const val SERVICE_UUID = "0000FFF0-0000-1000-8000-00805F9B34FB"

class DeviceListAdapter(var context: Context, var scannedBleDevices: List<BluetoothDevice>) : RecyclerView.Adapter<DeviceListAdapter.DeviceItemHolder>(){

    private var data: String? = null
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

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS){
                val service = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID))
                val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                characteristic?.setValue(data)
                gatt?.writeCharacteristic(characteristic)
            }
        }
    }

    private lateinit var bluetoothGatt: BluetoothGatt

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
        data = "Hello World"
        bluetoothGatt = device.connectGatt(context, true, gattCallback)
        bluetoothGatt.discoverServices()

        val card = holder.binding
        card.tvDeviceName.setText("Device Name : " + device.name.toString())
        card.tvMacAddress.setText("Mac Address : " + device.address.toString())

        card.btnConnect.isVisible = (device.address == DEVICE_ADDRESS)

        card.btnConnect.setOnClickListener{
            device.connectGatt(context, false, gattCallback)
            Log.i("BLE_RSA Enough", "Bond State : ${device.bondState}")
            Log.i("BLE_RSA Enough", "Connected to ${device.name} - ${device.address}")
        }

    }
}