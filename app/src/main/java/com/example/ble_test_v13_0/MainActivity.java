package com.example.ble_test_v13_0;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import android.widget.ProgressBar;
import android.widget.Toast;

import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


enum BT_CONNECTION_STATE {
    NOT_SCANNING,
    SCANNING,
    CONNECTING,
    CONNECTED,
    CONNECTING_FAILED,
    DISCONNECTING,
    DISCONNECTED
}

public class MainActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;

    BluetoothGatt btGatt;

    final int REQUEST_ENABLE_BT = 1;
    Button connectButton;

    ProgressBar connectionProgressBar;
    AlertDialog.Builder builderConnecting;
    AlertDialog dialogConnecting;

    // Define Permission codes for Scan and Connect.
    // Give any value but unique for each permission...
    private static final int BT_SCAN_PERMISSION_CODE = 100;
    private static final int BT_CONNECT_PERMISSION_CODE = 101;

    public BT_CONNECTION_STATE mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;

    String mMyDevice;
    BluetoothDevice mMyBTDevice;

    public void setMyDevice(String mMyDevice) {
        this.mMyDevice = mMyDevice;
    }
    public String getMyDevice() {
        return mMyDevice;
    }

    public void setMyBTDevice(BluetoothDevice mMyDevice) {
        this.mMyBTDevice = mMyDevice;
    }

    public BluetoothDevice getMyBTDevice() {
        return this.mMyBTDevice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
                ft
                .setReorderingAllowed(true)
                //.add(R.id.fragment_container_view, ScanningFragment.class, null, "SCAN")
                .replace(R.id.fragment_container_view, ScanningFragment.class, null, "SCAN")
                //.addToBackStack("scanning") // name can be null
                .commit();

/*            ScanningFragment fragmentScan =
                    (ScanningFragment)fm.findFragmentByTag("SCAN");
            if (fragmentScan.isAdded()){ ft.show(fragmentScan); }
            else { ft.hide(fragmentScan); }
*/
        }

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

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

        setMyDevice("18:5E:0F:9C:D5:A0"); //Todo

        Intent service_activity_launch_intent =
                new Intent(this, ServicesActivity.class);

        connectButton = findViewById(R.id.connection_button);
        connectButton.setVisibility(INVISIBLE);
        connectButton.setText("Disconnect");
        connectButton.setOnClickListener(v -> {
            //startActivity(service_activity_launch_intent);
            HandleBleConnection(BT_CONNECTION_STATE.DISCONNECTING);
        });

        connectionProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        connectionProgressBar.setVisibility(INVISIBLE);

        // Settings for Connecting-dialog
        builderConnecting = new AlertDialog.Builder(this);
        builderConnecting.setTitle("Connecting... This may take a moment.");
        builderConnecting.setCancelable(true); // if you want user to wait for some process to finish,
        builderConnecting.setView(R.layout.connection_progress);
        // Setting Negative "NO" Btn
        builderConnecting.setNegativeButton("Cancel connecting",
                (dialog, which) -> dialog.cancel());

        dialogConnecting = builderConnecting.create();

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
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
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


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
/*
                // Start Services-activity
                Intent service_activity_launch_intent =
                        new Intent(MainActivity.this, ServicesActivity.class);

                startActivity(service_activity_launch_intent);
 */
                HandleBleConnection(BT_CONNECTION_STATE.CONNECTED);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                HandleBleConnection(BT_CONNECTION_STATE.DISCONNECTED);
            }
        }
    };


    @SuppressLint("MissingPermission")
    // MissingPermission here just to avoid warnings. Runtime permission for BT_CONNECT
    // has (and must) been checked earlier.
    public void HandleBleConnection(BT_CONNECTION_STATE stateChange) {
        if (stateChange == BT_CONNECTION_STATE.NOT_SCANNING){
            if (mConnectionState == BT_CONNECTION_STATE.SCANNING){
                mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;
            }
        }
        else if (stateChange == BT_CONNECTION_STATE.SCANNING){
            if (mConnectionState == BT_CONNECTION_STATE.NOT_SCANNING){
                mConnectionState = BT_CONNECTION_STATE.SCANNING;
            }
        }
        else if (stateChange == BT_CONNECTION_STATE.CONNECTING){
            if (mConnectionState == BT_CONNECTION_STATE.NOT_SCANNING){
                mConnectionState = BT_CONNECTION_STATE.CONNECTING;
                connectButton.setVisibility(VISIBLE);
                //connectionProgressBar.setVisibility(VISIBLE);
                // Show Connecting-dialog. Better to execute  in separate thread
                // although this should work in original UI-thread.
                runOnUiThread(() -> {
                    dialogConnecting.show();
                });

                btGatt = getMyBTDevice().connectGatt(MainActivity.this,
                        false, gattCallback);
            }
        }
        else if (stateChange == BT_CONNECTION_STATE.CONNECTED){

            System.out.println("Connected to remote device!");

            mConnectionState = BT_CONNECTION_STATE.CONNECTED;

            //Hide Connecting-dialog in separate thread. Otherwise next error:
            //    W/BluetoothGatt: Unhandled exception in callback
            //    android.view.ViewRootImpl$CalledFromWrongThreadException:
            //    Only the original thread that created a view hierarchy can touch
            //    its views.
            runOnUiThread(() -> {
                connectionProgressBar.setVisibility(INVISIBLE);
                dialogConnecting.hide();
            });
        }
        else if (stateChange == BT_CONNECTION_STATE.DISCONNECTING){
            if (mConnectionState == BT_CONNECTION_STATE.CONNECTING){
                // disconnect before the connection is established
                mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;
                connectButton.setVisibility(INVISIBLE);
                btGatt.close();
            }
            else {
                // disconnect after the connection is established
                mConnectionState = BT_CONNECTION_STATE.DISCONNECTING;
                btGatt.disconnect();
            }
        }
        else if (stateChange == BT_CONNECTION_STATE.CONNECTING_FAILED){

            System.out.println("Failed to connected to the remote device!");
            mConnectionState = BT_CONNECTION_STATE.DISCONNECTED;
        }
        else if (stateChange == BT_CONNECTION_STATE.DISCONNECTED){
            if (mConnectionState == BT_CONNECTION_STATE.CONNECTING){
                System.out.println("Failed to connect to the remote device!");
                connectionProgressBar.setVisibility(INVISIBLE);

                //Hide Connecting-dialog in separate thread. Otherwise next error:
                //    W/BluetoothGatt: Unhandled exception in callback
                //    android.view.ViewRootImpl$CalledFromWrongThreadException:
                //    Only the original thread that created a view hierarchy can touch
                //    its views.
                runOnUiThread(() -> {
                    connectionProgressBar.setVisibility(INVISIBLE);
                    dialogConnecting.hide();
                    Toast.makeText(MainActivity.this, "Failed to connect to the remote device!", Toast.LENGTH_SHORT).show();
                });

            }
            else{
                System.out.println("Disconnected from My device!");
            }

            connectButton.setVisibility(INVISIBLE);

            mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;
        }
        System.out.println("State: " + mConnectionState);
    }

}
