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
import android.os.Build;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.widget.Toast;

import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;

// Connection states. Used also as events for triggering the state-change.
enum BT_CONNECTION_STATE {
    NOT_SCANNING,
    SCANNING,
    CONNECTING,
    CONNECTED,
    DISCONNECTING, // use this trigger for disconnecting only the connected device
    DISCONNECTED
}

public class MainActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;

    BluetoothGatt btGatt;

    final int REQUEST_ENABLE_BT = 1;

    AlertDialog.Builder builderConnecting;
    AlertDialog dialogConnecting;

    // Define Permission code for Scan and Connect.
    private static final int BT_SCAN_CONNECT_PERMISSION_CODE = 100;

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
            Toast.makeText(MainActivity.this, "Bluetooth not enabled! Enable it from Settings.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkAndRequestPermissions(); // validate BT SCAN/CONNECT permissions

        // Settings for Connecting-dialog
        createProgressSpinnerForConnectingAlertDialog();

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

    private void createProgressSpinnerForConnectingAlertDialog() {
        builderConnecting = new AlertDialog.Builder(this)
            // User can close this Modal-dialog by pressing Back-button. Todo: is it good idea?
            .setCancelable(true)
            .setView(R.layout.connection_progress);

        dialogConnecting = builderConnecting.create();
    }

    private void showScanConnectRuntimePermissionMessage(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("I understand", okListener)
                .create()
                .show();
    }

    // Function to check and request permission.
    public void checkAndRequestPermissions() {
        String[] permissions = {BLUETOOTH_SCAN, BLUETOOTH_CONNECT};

        // Check and request the missing permissions, and then override
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.

        if ( (ActivityCompat.checkSelfPermission(MainActivity.this, permissions[0]) ==
                PackageManager.PERMISSION_GRANTED)
                        &&
          (ActivityCompat.checkSelfPermission(MainActivity.this, permissions[1]) ==
                 PackageManager.PERMISSION_GRANTED))
        {
            Toast.makeText(MainActivity.this,
                   "Permission for scanning and connecting devices granted previously",
                    Toast.LENGTH_LONG).show();
        }
        // Requesting the permissions.
        // Request both permissions at once. When trying two consecutive requestPermissions,
        // Android started to complain...

        // First time after installation, naturally there are no permissions, so requestPermissions()
        // is called directly (see last else). If user denies to give permissions, application closes.
        // After restarting the application shouldShowRequestPermissionRationale
        // returns true, and Rationale helper-dialog (showScanConnectRuntimePermissionMessage) is shown
        // before requesting permissions again.
        // This is the second (and last) chance to permit to use the application.
        // Next restarts will fall down to requestPermissions() (see last else), because shouldShowRequestPermissionRationale
        // starts to return false.
        // Now Android-OS doesn't show any permission-dialog anymore. We are in 'Don't ask anymore' state.
        // Some earlier OS-versions included 'Don't ask anymore' selection, but nowadays OS decides automatically behalf of the user...
        // Application will close in any new attempts. Only uninstall/install application and starting the procedure again helps...

        else if (shouldShowRequestPermissionRationale(BLUETOOTH_SCAN) &&
                 shouldShowRequestPermissionRationale(BLUETOOTH_CONNECT)) {

            showScanConnectRuntimePermissionMessage("You will next need to allow permissions for finding " +
                            "and connecting Bluetooth Low Energy devices! Seriously, if you now deny permissions, " +
                            "you will need to uninstall and install BLE tester again to proceed for allowing permissions." +
                            "BLE tester is totally useless application without desired runtime permissions, " +
                            "and will stop running..." ,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{ permissions[0], permissions[1] },
                                BT_SCAN_CONNECT_PERMISSION_CODE);
                    }
                });
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { permissions[0], permissions[1] },
                    BT_SCAN_CONNECT_PERMISSION_CODE);
        }
    }

    // This function is called when the user accepts or declines the permissions.
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

        boolean scanAccepted;
        boolean connectionAccepted;

        if ( (grantResults.length > 1) && (requestCode == BT_SCAN_CONNECT_PERMISSION_CODE)){
            scanAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
            connectionAccepted = (grantResults[1] == PackageManager.PERMISSION_GRANTED);

            if ( scanAccepted) {
                Toast.makeText(MainActivity.this, "BT SCAN Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(MainActivity.this, "BT SCAN Permission Denied", Toast.LENGTH_SHORT) .show();
            }

            if ( connectionAccepted) {
                Toast.makeText(MainActivity.this, "BT CONNECTION Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(MainActivity.this, "BT CONNECTION Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
        else {
            scanAccepted = false;
            connectionAccepted = false;
        }

        if (!scanAccepted && !connectionAccepted){
            Toast.makeText(MainActivity.this, "You need to allow permissions for finding (scan) and connecting Bluetooth-devices", Toast.LENGTH_LONG) .show();
            Toast.makeText(MainActivity.this, "Start BLE tester again!", Toast.LENGTH_LONG) .show();

            finish(); // No sense to continue. Finish the application.
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
            //todo
            System.out.println("BluetoothGattCallback: new state " + newState+ "| status: " + status);
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
                // because we probably are already in UI-thread
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
            // so we are probably now on 'non-UI' thread.
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
            if (mConnectionState == BT_CONNECTION_STATE.CONNECTED){
                // Disconnect normally (from UI), after the connection is established
                mConnectionState = BT_CONNECTION_STATE.DISCONNECTING;
                btGatt.disconnect();
            }
            else {
                // Disconnect normally (from UI) before the connection is established.
                // This shouldn't occur, because there is not even Disconnect-button (Connection-Fragment)
                // visible yet...
                mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;
                System.out.println("Failure: disconnect in state" + mConnectionState); //todo: -> LOG!!

                btGatt.close();
            }
        }
        else if (stateChange == BT_CONNECTION_STATE.DISCONNECTED){
            if (mConnectionState == BT_CONNECTION_STATE.DISCONNECTING){
                // Disconnecting the remote device succeeded:
                // CONNECTED -> DISCONNECTING-event -> DISCONNECTING (when using DISCONNECT-button).

                // Show Scanning-fragment instead of Connection-fragment
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
            else{
                // Establishment of the connection failed.
                // If: CONNECTED -> DISCONNECTED-event (from BT-callback),
                // we lost connection from remote-device.

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

            mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;
        }

        System.out.println("State: " + mConnectionState); //todo: remove
    }

}
