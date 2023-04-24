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

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.widget.Toast;

import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;

enum BT_CONNECTION_STATE {
    NOT_SCANNING,
    SCANNING,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    DISCONNECTED
}

public class MainActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;

    BluetoothGatt btGatt;

    final int REQUEST_ENABLE_BT = 1;

    AlertDialog.Builder builderConnecting;
    AlertDialog dialogConnecting;

    // Define Permission codes for Scan and Connect.
    // Give any value but unique for each permission...
    private static final int BT_SCAN_PERMISSION_CODE = 100;
    private static final int BT_CONNECT_PERMISSION_CODE = 101;

    public BT_CONNECTION_STATE mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;

    String mMyDevice;
    BluetoothDevice mMyBTDevice;

    FragmentManager fm;
    FragmentTransaction ft;

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
            fm = getSupportFragmentManager();
            ft = fm.beginTransaction();
            ft
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, ScanningFragment.class, null, "SCAN")
                .commit();
        }

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        if (btAdapter == null) {
            Toast.makeText(MainActivity.this, "Bluetooth interface not available!",
                    Toast.LENGTH_SHORT).show();
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

        // Settings for Connecting-dialog
        builderConnecting = new AlertDialog.Builder(this);
        builderConnecting.setCancelable(true); // if you want user to wait for some process to finish,
        builderConnecting.setView(R.layout.connection_progress);
        // Set negative button for cancelling the connection
        builderConnecting.setNegativeButton("Cancel connect",
                (dialog, which) -> {
                    // Sent disconnect-event
                    runOnUiThread(() -> {
                        HandleBleConnection(BT_CONNECTION_STATE.DISCONNECTED);
                        dialog.cancel();
                    });
                });

        dialogConnecting = builderConnecting.create();
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("onStop main");
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("onPause main");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dialogConnecting.dismiss();
        System.out.println("onDestroy main");
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
                HandleBleConnection(BT_CONNECTION_STATE.CONNECTED);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                HandleBleConnection(BT_CONNECTION_STATE.DISCONNECTED);
            }
            else{
                System.out.println("BluetoothGattCallback: new state " + newState+ ". What to do with this event?");
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

                // Show Connecting-dialog (with progress-spinner).
                // Better to execute with the help of runOnUiThread
                // which should execute this in any case immediately,
                // because we probably are here in UI-thread
                // (trigger came also from UI from ScanningFragment).
                runOnUiThread(() -> {
                    dialogConnecting.setTitle("Connecting to \'\'" +
                            getMyBTDevice().getName() + "\'\' ...");
                    dialogConnecting.show();
                });

                // Start connecting to the remote device.
                btGatt = getMyBTDevice().connectGatt(MainActivity.this,
                        false, gattCallback);
            }
        }
        else if (stateChange == BT_CONNECTION_STATE.CONNECTED){

            mConnectionState = BT_CONNECTION_STATE.CONNECTED;
            // CONNECTED-event comes from BT-interface (via Callback),
            // so we are probably now on non-UI thread.
            // Hide Connecting-dialog with runOnUiThread.
            //  -Runs the specified action on the UI thread:
            //    If the current thread is the UI thread,
            //    then the action is executed immediately.
            //    Otherwise the action is posted to the event queue of the UI thread.
            // Otherwise next error:
            //    W/BluetoothGatt: Unhandled exception in callback
            //    android.view.ViewRootImpl$CalledFromWrongThreadException:
            //    Only the original thread that created a view hierarchy can touch
            //    its views.
            runOnUiThread(() -> {

                dialogConnecting.hide();

                // show Connection-fragment instead of Scanning-fragment
                fm = getSupportFragmentManager();
                ft = fm.beginTransaction();
                ft
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container_view, ConnectedFragment.class, null, "CONNECTION")
                    .commit();
            });

        }
        else if (stateChange == BT_CONNECTION_STATE.DISCONNECTING){
            if (mConnectionState == BT_CONNECTION_STATE.CONNECTING){
                // disconnect before the connection is established
                mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;
                btGatt.close();
            }
            else {
                // disconnect after the connection is established
                mConnectionState = BT_CONNECTION_STATE.DISCONNECTING;
                btGatt.disconnect();
            }
        }
        else if (stateChange == BT_CONNECTION_STATE.DISCONNECTED){
            if (mConnectionState == BT_CONNECTION_STATE.CONNECTING){

                //Hide Connecting-dialog in separate thread. Otherwise next error:
                //    W/BluetoothGatt: Unhandled exception in callback
                //    android.view.ViewRootImpl$CalledFromWrongThreadException:
                //    Only the original thread that created a view hierarchy can touch
                //    its views.
                runOnUiThread(() -> {
                    dialogConnecting.hide();
                    Toast.makeText(MainActivity.this, "Failed to connect to the remote device!", Toast.LENGTH_SHORT).show();
                });
            }
            else{
                // show Scanning-fragment instead of Connection-fragment

                // Disconnection should go via states:
                //  CONNECTED -> DISCONNECTING-event -> DISCONNECTING (when using DISCONNECT-button).
                // If: CONNECTED -> DISCONNECTED-event (from BT-callback),
                // we lost connection from remote-device.

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Disconnected from your BLE device",
                              Toast.LENGTH_SHORT).show();

                    fm = getSupportFragmentManager();
                    ft = fm.beginTransaction();
                    ft
                            .setReorderingAllowed(true)
                            .replace(R.id.fragment_container_view, ScanningFragment.class, null, "SCAN")
                            .commit();
                });
            }

            mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;
        }

        System.out.println("State: " + mConnectionState); //todo: remove
    }

}
