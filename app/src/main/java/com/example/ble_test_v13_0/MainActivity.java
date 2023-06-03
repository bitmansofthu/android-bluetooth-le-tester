package com.example.ble_test_v13_0;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.content.ContentValues.TAG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

// Connection states. Used also as events for triggering the state-change.
enum BT_CONNECTION_STATE {
    NOT_SCANNING,
    SCANNING,
    CONNECTING,
    CONNECTED,
    DISCONNECTING, // use this event for the disconnect-event triggered by this local device (me)
    DISCONNECTED
}

public class MainActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;

    BluetoothGatt btGatt;

    // Stop connecting automatically after 15 seconds, if connection was not established.
    static final long CONNECTING_PERIOD = 15000; // ms

    Handler connection_handler = new Handler(Looper.myLooper());

    AlertDialog.Builder builderConnecting;
    AlertDialog dialogConnecting;

    // Define Permission code for Scan and Connect.
    private static final int BT_SCAN_CONNECT_PERMISSION_CODE = 100;

    public BT_CONNECTION_STATE mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;

    BluetoothDevice mMyBTDevice;

    FragmentManager fm;
    FragmentTransaction ft;

    public void setMyBTDevice(BluetoothDevice mMyDevice) {
        this.mMyBTDevice = mMyDevice;
    }

    public BluetoothDevice getMyBTDevice() {
        return this.mMyBTDevice;
    }

    private final HashMap<String, String> reserved_uuids_with_description =
            new HashMap<>();

    public String reserved_uuid_lookup(String uuid) {
        String name = reserved_uuids_with_description.get(uuid);
        return name == null ? "Unknown" : name;
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
            Toast.makeText(this, "BLE not supported!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!btAdapter.isEnabled()) {
            Toast.makeText(MainActivity.this, "Bluetooth not enabled! Enable it from Settings.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            checkAndRequestRuntimePermissions(); // validate BT SCAN/CONNECT permissions
        }

        // Settings for Connecting-dialog
        createProgressSpinnerForConnectingAlertDialog();

        boolean success = false; // todo

        try {
            success = readBtReservedUUIDsInYamlFiles("service_uuids.yaml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            success = readBtReservedUUIDsInYamlFiles("characteristic_uuids.yaml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

    // YAML formatted file-Parser.
    // return false : failed, return true : success
    // See 'https://www.bluetooth.com/specifications/assigned-numbers/' and
    // 'Assigned Numbers Repository (YAML)'.
    // Notice: this parser decodes only simple list-type, so exactly the format included in these
    // specific files:
    //uuids:
    // - uuid: 0x1800
    //   name: Generic Access
    //   id: org.bluetooth.service.generic_access
    // - uuid: 0x1801
    // ...
    private boolean readBtReservedUUIDsInYamlFiles(String fileName) throws IOException {
        //AssetManager assetManager = MainActivity.this.getAssets();

        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(getAssets().open(fileName),
                            StandardCharsets.UTF_8));

            String line_content;
            int line_nbr = 1;
            int list_item = 0;
            String uuid_key;
            String uuid_16_bit = null;
            String name_key;
            String descriptive_name = null;
            String id_key;
            String service_id = null;

            // loop until end of file
            while ((line_content = br.readLine()) != null) {
                // process line
                String[] line_parts;

                line_content = line_content.trim();

                if (line_content.isEmpty()){
                    continue; // empty line (was including perhaps some spaces)
                }

                if (line_nbr == 1) {
                    if (!line_content.contentEquals("uuids:")){
                        Log.w(TAG, "First line is incorrect. Do not take this YAML-file into usage.");

                        return false;
                    }
                    else{
                        list_item = 0; // continue parsing for checking list item
                    }
                } else {

                    if ( list_item == 0 ) {
                        if (line_content.charAt(0) == '-') {  // list indicator

                            line_content = line_content.substring(1); // remove 'list indicator'
                            line_parts = line_content.split(":", 2); // limit to two parts

                            // trim for removing all leading and trailing space
                            uuid_key = line_parts[0].trim();

                            if (!uuid_key.equals("uuid")){
                                // should always be 'uuid'
                                Log.w(TAG, "Wrong key in line " + line_nbr  + ". Should be 'uuid'.");
                                return false;
                            }

                            uuid_16_bit = line_parts[1].trim();
                            uuid_16_bit = uuid_16_bit.substring(2); // remove '0x'

                            uuid_16_bit = uuid_16_bit.toLowerCase(Locale.ROOT);

                            list_item++;
                        }
                        else{
                            // List indicator '-' not found on row, where it should be located.
                            // Stop parsing.
                            Log.w(TAG, "List indicator not found. Do not take this YAML-file into usage.");
                            return false;
                        }
                    }
                    else if (list_item == 1) {
                        line_parts = line_content.split(":", 2);

                        // trim for removing all leading and trailing space
                        name_key = line_parts[0].trim();

                        // That's what we are looking for!
                        descriptive_name = line_parts[1].trim();

                        if (!name_key.equals("name")){
                            // should always be 'name'
                            Log.w(TAG, "Wrong key in line " + line_nbr + ". Should be 'name'.");
                            return false;
                        }

                        list_item++;
                    }
                    else if (list_item == 2) {
                        // trim for removing all leading and trailing space
                        line_parts = line_content.split(":", 2);

                        id_key = line_parts[0].trim();

                        // e.g. 'org.bluetooth.service.generic_access*
                        service_id = line_parts[1].trim();

                        if (!id_key.equals("id")){
                            // should always be 'id'
                            Log.w(TAG, "Wrong key in line " + line_nbr + ". Should be 'id'.");
                            return false;
                        }

                        // Convert short 16-bit UUID to full 128-bit UUID with formula:
                        // uuid128 = (uuid16 << 96) + base_for_reserved_address,
                        // where base = 0x00 00 10 00 80 00 00 80 5f 9b 34 fb
                        // (contains 12 octets = 12 * 8 bit = 96 bits)
                        // MSB 16 bits (bits 128...112) is filled by 0x0000
                        String uuid_128_bit_reserved =
                                "0000" // MSB
                                .concat(uuid_16_bit) // short 16-bit address
                                .concat("-0000-1000-8000-00805f9b34fb"); // base

                        reserved_uuids_with_description.put(uuid_128_bit_reserved, descriptive_name);

                        list_item = 0; // next line should start new list-item
                    }
                }
                line_nbr++;
            }

        } catch (IOException e) {
            Log.w(TAG, "Failed to open Yaml-file");
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.w(TAG, "Failed to close Yaml-file");
                }
            }
        }

        return true;
    }

    // Start timer for monitoring the establishment of connection.
    // If it's still connecting after CONNECTING_PERIOD, reset the connection establishment.
    private void controlConnectingTimer(BT_CONNECTION_STATE connectionState) {
        if (connectionState == BT_CONNECTION_STATE.CONNECTING){
            connection_handler.postDelayed(() -> {
                // after timeout, start to disconnect (if still connecting)
                if (mConnectionState == BT_CONNECTION_STATE.CONNECTING) {
                    HandleBleConnection(BT_CONNECTION_STATE.DISCONNECTING);
                }
            }, CONNECTING_PERIOD);
        }
        else{
            // reset timer in any other state
            connection_handler.removeCallbacksAndMessages(null);
        }
    }

    private void createProgressSpinnerForConnectingAlertDialog() {
        builderConnecting = new AlertDialog.Builder(this)
            // User cannot close this Modal-dialog by e.g. pressing Back-button.
            // It's closed by timeout or after successful connection.
            .setCancelable(false)
            .setView(R.layout.connection_progress);

        dialogConnecting = builderConnecting.create();
    }

    private void showScanConnectRuntimePermissionMessage(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("You will next need to allow permissions for finding Bluetooth Low Energy" +
                        "devices from neighbourhood, and connecting to the selected device." +
                        "Seriously, if you now deny permissions, you will need to uninstall and " +
                        "install BLE tester application again to proceed for allowing permissions next" +
                        "time, and start to use the application. BLE tester is totally useless" +
                        "application without desired runtime permissions, and will stop running...")
                .setPositiveButton("I understand", okListener)
                .create()
                .show();
    }

    // Function to check and request runtime permissions for SCAN/CONNECT
    // These runtime permissions are needed after Android 12 (API >= v31)
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void checkAndRequestRuntimePermissions() {
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
        // Next restarts will fall down to requestPermissions() (see last else),
        // because shouldShowRequestPermissionRationale starts to return false.
        // Now Android-OS doesn't show any permission-dialog anymore. We are in 'Don't ask anymore' state.
        // Some earlier OS-versions included 'Don't ask anymore' selection, but nowadays OS decides
        // automatically behalf of the user...
        // Application will close in any new attempts. Only uninstall/install application and starting
        // the procedure again helps...

        else if (shouldShowRequestPermissionRationale(BLUETOOTH_SCAN) &&
                 shouldShowRequestPermissionRationale(BLUETOOTH_CONNECT)) {

            showScanConnectRuntimePermissionMessage(
                    (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{ permissions[0], permissions[1] },
                            BT_SCAN_CONNECT_PERMISSION_CODE));
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
                Toast.makeText(MainActivity.this, "BT SCAN Permission Granted",
                        Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(MainActivity.this, "BT SCAN Permission Denied",
                        Toast.LENGTH_SHORT) .show();
            }

            if ( connectionAccepted) {
                Toast.makeText(MainActivity.this, "BT CONNECTION Permission Granted",
                        Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(MainActivity.this, "BT CONNECTION Permission Denied",
                        Toast.LENGTH_SHORT) .show();
            }
        }
        else {
            scanAccepted = false;
            connectionAccepted = false;
        }

        if (!scanAccepted && !connectionAccepted){
            Toast.makeText(MainActivity.this, "You need to allow permissions for finding (scan) and connecting Bluetooth-devices",
                    Toast.LENGTH_LONG) .show();
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
        }

        @Override
        public void onPhyUpdate (BluetoothGatt gatt,
                                 int txPhy,
                                 int rxPhy,
                                 int status){
            Log.w(TAG, "onPhyUpdate: txPhy: " + txPhy +
                            " rxPhy: " + rxPhy +
                            " status: " + status);
        }

        @Override
        public void onMtuChanged (BluetoothGatt gatt,
                                  int mtu,
                                  int status){

            //btGatt.requestMtu(5*23);

            Log.w(TAG, "onMtuChanged: status: " + status +
                                    " | mtu: " + mtu);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == GATT_SUCCESS) {
                runOnUiThread(() -> ((ConnectedFragment) Objects.requireNonNull
                        (fm.findFragmentByTag("CONNECTION"))).GattServicesDiscovered());
            } else {
                Log.w(TAG, "onServicesDiscovered not successful. Status: " + status);
            }
        }

        // Will be here when executing this SW in Android ver <= 12
        // This method was deprecated in API level 33.
        @Override
        public void onCharacteristicRead (BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status)
        {
            if (status == GATT_SUCCESS) {
                runOnUiThread(() -> ((ConnectedFragment) Objects.requireNonNull
                        (fm.findFragmentByTag("CONNECTION"))). //todo: CONNECTION -> const
                        GattCharacteristicsValueReceived(characteristic,
                                characteristic.getValue()));
            } else {
                Log.w(TAG, "onCharacteristicRead failed. Status: " + status);
            }
        }

        // This method is for API level >= 33.
        @Override
        public void onCharacteristicRead(
                BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                byte[] value,
                int status
        )
        {
            if (status == GATT_SUCCESS) {
                runOnUiThread(() -> ((ConnectedFragment) Objects.requireNonNull
                        (fm.findFragmentByTag("CONNECTION"))). //todo: CONNECTION -> const
                        GattCharacteristicsValueReceived(characteristic, value));
            } else {
                Log.w(TAG, "onCharacteristicRead failed. Status: " + status);
            }
        }

        // Callback triggered as a result of a remote characteristic notification.
        // This method is for API level >= 33.
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                BluetoothGattCharacteristic characteristic,
                                byte[] value){
                runOnUiThread(() -> ((ConnectedFragment) Objects.requireNonNull
                        (fm.findFragmentByTag("CONNECTION"))). //todo: CONNECTION -> const
                        GattCharacteristicsValueReceived(characteristic, value));

            Log.w(TAG, "onCharacteristicChanged"); // todo
        }

        // Callback triggered as a result of a remote characteristic notification.
        // This method was deprecated in API level 33 (OS v13).
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic){

            runOnUiThread(() -> ((ConnectedFragment) Objects.requireNonNull
                    (fm.findFragmentByTag("CONNECTION"))). //todo: CONNECTION -> const
                    GattCharacteristicsValueReceived(characteristic,
                    characteristic.getValue()));

            Log.w(TAG, "onCharacteristicChanged"); // todo
        }

        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic,
                                           int status){
            if (status != GATT_SUCCESS){
                runOnUiThread(() -> ((ConnectedFragment) Objects.requireNonNull
                        (fm.findFragmentByTag("CONNECTION"))). //todo: CONNECTION -> const
                        GattCharacteristicsValueWriteFailed(characteristic));

                Log.w(TAG, "onCharacteristicWrite: writing new value to peripheral failed");
            }
        }

    };


    static boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {//from  w w  w.ja  v  a 2  s  . c om
            Method localMethod = gatt.getClass().getMethod("refresh");
            return (boolean) (Boolean) localMethod.invoke(gatt, new Object[0]);
        } catch (Exception ignored) {
        }
        return false;
    }

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
                // Show Connecting-dialog (with progress-spinner).
                // Better to execute with the help of runOnUiThread
                // which should execute this in any case immediately,
                // although we probably are also now executing UI-thread
                // (trigger came also from UI from ScanningFragment).
                runOnUiThread(() -> {
                    dialogConnecting.setTitle("Connecting to '" +
                            getMyBTDevice().getName() + "' ...");
                    dialogConnecting.show();

                    // start Connecting-timer to elapse for avoiding to connect for ages...
                    controlConnectingTimer(BT_CONNECTION_STATE.CONNECTING);
                });

                mConnectionState = BT_CONNECTION_STATE.CONNECTING;

                // Start connecting to the remote device.

                btGatt = getMyBTDevice().connectGatt(MainActivity.this,
                        false, gattCallback,TRANSPORT_LE);
/*
// clean GATT-services/characteristics stored locally on cache-file
                boolean refresh = refreshDeviceCache(btGatt);
                Log.w(TAG, "Cache refreshed: " + refresh);
*/
            }
        }
        else if (stateChange == BT_CONNECTION_STATE.CONNECTED){

            mConnectionState = BT_CONNECTION_STATE.CONNECTED;

            // reset Connecting-timer
            controlConnectingTimer(BT_CONNECTION_STATE.CONNECTED);

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
                    .replace(R.id.fragment_container_view, ConnectedFragment.class, null,
                            "CONNECTION")
                    .commit();
            });
        }
        else if (stateChange == BT_CONNECTION_STATE.DISCONNECTING){

            // Disconnecting-event triggered by this local device

            if (mConnectionState == BT_CONNECTION_STATE.CONNECTED){
                // normal disconnect (from UI) after, the connection is established
                mConnectionState = BT_CONNECTION_STATE.DISCONNECTING;
                btGatt.disconnect();
            }
            else {
                // Disconnect (by timeout) before the connection is established.
                mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;
                Log.w(TAG, "Failed to connect to remote device. Canceled by timeout.");

                runOnUiThread(() -> {
                    dialogConnecting.hide();
                    Toast.makeText(MainActivity.this, "Failed to connect to the remote device!",
                            Toast.LENGTH_SHORT).show();

                    if (btGatt != null){
                        // Close gatt-interface (if there is any interface available anymore).
                        // BT-interface might be stuck.
                        btGatt.close();
                    }

                });

            }
        }
        else if (stateChange == BT_CONNECTION_STATE.DISCONNECTED){
            if (mConnectionState == BT_CONNECTION_STATE.DISCONNECTING){
                // Normal local disconnecting to the remote device succeeded:
                // CONNECTED -> DISCONNECTING-event -> DISCONNECTING -> DISCONNECTED-event
                // (when using DISCONNECT-button).

                btGatt.close();

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
                // 1) Establishment of the connection failed (BT-interface via callback triggered this).
                //      CONNECTING -> DISCONNECTED-event,
                //       or
                // 2) We were in connected state, but we lost connection from remote-device
                //    (e.g. weak signal or remote device was unpowered).
                //     CONNECTED -> DISCONNECTED-event,

                // BT_CONNECTION_STATE.DISCONNECTED event is always triggered from BT-interface (see callback)
                // so BT-interface is alive. No specific reset for interface is needed here...

                //Hide Connecting-dialog in separate thread. Otherwise next error:
                //    W/BluetoothGatt: Unhandled exception in callback
                //    android.view.ViewRootImpl$CalledFromWrongThreadException:
                //    Only the original thread that created a view hierarchy can touch
                //    its views.
                runOnUiThread(() -> {
                    dialogConnecting.hide();
                    Toast.makeText(MainActivity.this, "Failed to connect to the remote device!",
                            Toast.LENGTH_SHORT).show();
                });

            }

            mConnectionState = BT_CONNECTION_STATE.NOT_SCANNING;
        }
    }

}
