package com.example.ble_test_v13_0;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;

import android.content.Context;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import android.widget.Toast;

import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.view.View.INVISIBLE;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btLeScanner;
    BluetoothGatt btGatt;

    final int REQUEST_ENABLE_BT = 1;
    Button startScanningButton;
    Button connectButton;
    boolean scanning = false;
    // Stops scanning after 10 seconds.
    static final long SCAN_PERIOD = 100000;
    // Definine Permission codes for Scan and Connect.
    // Give any value but unique for each permission...
    private static final int BT_SCAN_PERMISSION_CODE = 100;
    private static final int BT_CONNECT_PERMISSION_CODE = 101;
    Handler handler = new Handler(Looper.myLooper());

    ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>(); //TODO: del !!
    String mMyDevice;
    BluetoothDevice mMyBTDevice;

    public void setmMyDevice(String mMyDevice) {
        this.mMyDevice = mMyDevice;
    }

    public void setmMyBTDevice(BluetoothDevice mMyDevice) {
        this.mMyBTDevice = mMyDevice;
    }

    public BluetoothDevice getmMyBTDevice() {
        return this.mMyBTDevice;
    }

    public String getmMyDevice() {
        return mMyDevice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btLeScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter == null) {
            Toast.makeText(MainActivity.this, "Bluetooth interface not available!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // Well, when 'uses-feature android:name="android.hardware.bluetooth_le"' in manifest
            // is set to true, condition above is probably quite useless.
            // Installation of this application in HW not supporting Low Energy BT (>= Ble v4.0)
            // is not allowed...
            Toast.makeText(this, "BLE not supported!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!btAdapter.isEnabled()) {
            // TODO!!!!!
            // startActivityForResult is deprecated, try with some newer way...
            //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

            Toast.makeText(MainActivity.this, "Bluetooth not enabled!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkAndRequestPermission(BLUETOOTH_SCAN, BT_SCAN_PERMISSION_CODE);
        checkAndRequestPermission(BLUETOOTH_CONNECT, BT_CONNECT_PERMISSION_CODE);

        setmMyDevice("18:5E:0F:9C:D5:A0");

        startScanningButton = findViewById(R.id.scan_button);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        connectButton = findViewById(R.id.connection_button);
        connectButton.setVisibility(INVISIBLE);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                HandleBleConnection();
            }
        });

    }

    // Function to check and request permission.
    public void checkAndRequestPermission(String permission, int requestCode)
    {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.

        if (ActivityCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else if (requestCode == BT_SCAN_PERMISSION_CODE){
            Toast.makeText(MainActivity.this, "Permission for scanning devices granted previously", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == BT_CONNECT_PERMISSION_CODE){
            Toast.makeText(MainActivity.this, "Permission for connecting devices granted previously", Toast.LENGTH_SHORT).show();
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (grantResults.length > 0){
            if (requestCode == BT_SCAN_PERMISSION_CODE) {
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "BT SCAN Permission Granted", Toast.LENGTH_SHORT) .show();
                }
                else {
                    Toast.makeText(MainActivity.this, "BT SCAN Permission Denied", Toast.LENGTH_SHORT) .show();
                }
            }
            else if (requestCode == BT_CONNECT_PERMISSION_CODE) {
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "BT CONNECTION Permission Granted", Toast.LENGTH_SHORT) .show();
                }
                else {
                    Toast.makeText(MainActivity.this, "BT CONNECTION Permission Denied", Toast.LENGTH_SHORT) .show();
                }
            }
        }
    }

    // Device scan callback.
    ScanCallback leScanCallback =
        new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                BluetoothDevice device = result.getDevice(); // TODO: move onto OnCreate??
                int signal = result.getRssi();

                System.out.println("BT address: " + device + " | rssi: " + signal);

//                Toast.makeText(MainActivity.this, device.toString(), Toast.LENGTH_SHORT).show();
                if( (mLeDevices == null) || (!mLeDevices.contains(device))) {
                    //assert mLeDevices != null;
                    mLeDevices.add(device);
                }
                String mydev = getmMyDevice();
                String detected_device = device.toString();
                if (detected_device.contains(mydev)){
                    System.out.println("My device detected!");
                    Toast.makeText(MainActivity.this, "My device detected!", Toast.LENGTH_SHORT).show();
                    connectButton.setVisibility(View.VISIBLE);
                    connectButton.setText("Connect");
                    setmMyBTDevice(device);
                }
            }
        };

    @SuppressLint("SetTextI18n")
    public void startScanning() { // TODO: private ????????????????
        if (scanning == false) {
            scanning = true;
            startScanningButton.setText("Stop scan");
            btLeScanner.startScan(leScanCallback);

            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    btLeScanner.stopScan(leScanCallback);
                    //startScanningButton.setVisibility(View.VISIBLE);
                    startScanningButton.setText("Start scan");
                }
            }, SCAN_PERIOD);

        } else { // scanning
            scanning = false;
            startScanningButton.setText("Start scan");
            btLeScanner.stopScan(leScanCallback);
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                System.out.println("Connected to My device!");
                //Toast.makeText(MainActivity.this, "Connected to My device!", Toast.LENGTH_SHORT).show();
                connectButton.setText("Disconnect");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                System.out.println("Disconnected from My device!");
                //Toast.makeText(MainActivity.this, "Disconnected from My device!", Toast.LENGTH_SHORT).show();
                connectButton.setText("Connect");
                connectButton.setVisibility(View.INVISIBLE);

                startScanningButton.setVisibility(View.VISIBLE);
            }
        }
    };


    public void HandleBleConnection() { // TODO: private ????????????????
        if (connectButton.getVisibility() == View.VISIBLE) {
            if (connectButton.getText() == "Connect"){
                if (scanning == true) {
                    scanning = false;
                    startScanningButton.setText("Start scan");
                    startScanningButton.setVisibility(View.INVISIBLE);
                    btLeScanner.stopScan(leScanCallback);
                }
                connectButton.setText("Connecting");
                btGatt = getmMyBTDevice().connectGatt(MainActivity.this, false, gattCallback);
            }
            else if (connectButton.getText() == "Disconnect"){
                connectButton.setText("Disconnecting");
                btGatt.disconnect();
            }

        }
    }
}