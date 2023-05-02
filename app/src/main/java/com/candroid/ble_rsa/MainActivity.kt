package com.candroid.ble_rsa

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.candroid.ble_rsa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceListAdapter: DeviceListAdapter
    private val devices = arrayListOf<BluetoothDevice>()
    private var scanning = false
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val SCAN_PERIOD: Long = 10000 // 10 seconds
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.device?.let {
                if (!devices.contains(it)) {
                    devices.add(it)
                    deviceListAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLE_RSA", "Scan failed with error code $errorCode")
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        deviceListAdapter = DeviceListAdapter(this@MainActivity, devices)
        binding.rvDevices.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.rvDevices.adapter = deviceListAdapter
        Log.i("BLE_RSA Enough", "Here we go!")
        if (!scanning) {
            Log.i("BLE_RSA Enough", "Scanning will start")
            startScan()
        } else {
            Log.i("BLE_RSA Enough", "Scanning will stop")
            stopScan()
        }

        showScannedDevices()
    }
    private fun startScan() {
        if (bluetoothAdapter.isEnabled) {
            devices.clear()
            deviceListAdapter.notifyDataSetChanged()
            scanning = true
            bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
            Handler().postDelayed({
                stopScan()
            }, SCAN_PERIOD)
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
        }
    }

    private fun stopScan() {
        scanning = false
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    private fun showScannedDevices(){
        devices.forEach {
            Log.i("BLE_RSA Enough", "${it.name} - ${it.address}")
        }
    }
}