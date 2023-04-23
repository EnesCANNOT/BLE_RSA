package com.candroid.ble_rsa

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.candroid.ble_rsa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var scanHandler: Handler
    private val scannedDevices = mutableListOf<BluetoothDevice>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (!scannedDevices.contains(device)) {
                scannedDevices.add(device)
                Log.d(TAG, "Found BLE device: ${device.name} (${device.address})")
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to BLE device")
                // Connected to the device, discover services
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from BLE device")
                // Disconnected from the device
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Discovered BLE services")
                // Services discovered, do something with them
            } else {
                Log.d(TAG, "Failed to discover BLE services")
                // Failed to discover services
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if BLE is supported
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "BLE not supported on this device")
            return
        }

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            )
        }

        // Get Bluetooth adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled) {
            Log.d(TAG, "Bluetooth not enabled")
            return
        }

        // Start scanning for BLE devices
        scanHandler = Handler(Looper.getMainLooper())
        scanHandler.postDelayed({
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
            connectToFirstDevice()
        }, SCAN_PERIOD)

        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
    }

    private fun connectToFirstDevice() {
        if (scannedDevices.isEmpty()) {
            Log.d(TAG, "No BLE devices found")
            return
        }

        val device = scannedDevices[0]

        Log.d(TAG, "Connecting to BLE device: ${device.name} (${device.address})")
        device.connectGatt(this, false, gattCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted")
            } else {
                Log.d(TAG, "Location permission denied")
            }
        }
    }

    companion object {
        private const val TAG = "MainActivityScanning"
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val SCAN_PERIOD = 10000L // 10 seconds
    }
}