package com.example.ble_test_v13_0;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;

import android.content.Context;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

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
    ArrayList<DeviceModel> mDevices = new ArrayList<DeviceModel>();
    String mMyDevice;
    BluetoothDevice mMyBTDevice;

    public void setMyDevice(String mMyDevice) {
        this.mMyDevice = mMyDevice;
    }

    public void setMyBTDevice(BluetoothDevice mMyDevice) {
        this.mMyBTDevice = mMyDevice;
    }

    public BluetoothDevice getMyBTDevice() {
        return this.mMyBTDevice;
    }

    public String getMyDevice() {
        return mMyDevice;
    }

// TODO: move to some proper place
    private RecyclerView servicesRecyclerView;
    private RecyclerView.Adapter servicesAdapter;

    private RecyclerView.LayoutManager serviceLayoutManager;

    // Index of the selected device in RecyclerView
    // (no selection by default)
    private int selected_device_position = -1;

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

        setMyDevice("18:5E:0F:9C:D5:A0"); //Todo

        createBTDevicesInRecyclerViewAdapter();

        startScanningButton = findViewById(R.id.scan_button);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });
        Intent service_activity_launch_intent =
                new Intent(this, ServicesActivity.class);

        connectButton = findViewById(R.id.connection_button);
        connectButton.setVisibility(VISIBLE);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //HandleBleConnection(); //Todo
                startActivity(service_activity_launch_intent);
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

                addDevice(result); // add device-details to Expanded List adapter

                BluetoothDevice device = result.getDevice(); // TODO: move onto OnCreate??
                int signal = result.getRssi();
                System.out.println("BT address: " + device + " | rssi: " + signal);

                String myDevice = getMyDevice();
                String detected_device = device.toString();
                if (detected_device.contains(myDevice)){
                    System.out.println("My device detected!");
                    Toast.makeText(MainActivity.this, "My device detected!", Toast.LENGTH_SHORT).show();
                    connectButton.setVisibility(VISIBLE);
                    connectButton.setText("Connect");
                    setMyBTDevice(device);
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

                startScanningButton.setVisibility(VISIBLE);
            }
        }
    };


    public void HandleBleConnection() { // TODO: private ????????????????
        if (connectButton.getVisibility() == VISIBLE) {
            if (connectButton.getText() == "Connect"){
                if (scanning == true) {
                    scanning = false;
                    startScanningButton.setText("Start scan");
                    startScanningButton.setVisibility(View.INVISIBLE);
                    btLeScanner.stopScan(leScanCallback);
                }
                connectButton.setText("Connecting");
                btGatt = getMyBTDevice().connectGatt(MainActivity.this, false, gattCallback);
            }
            else if (connectButton.getText() == "Disconnect"){
                connectButton.setText("Disconnecting");
                btGatt.disconnect();
            }

        }
    }

    public class BTDevicesRecyclerViewAdapter extends RecyclerView.Adapter<BTDevicesRecyclerViewAdapter.ViewHolder> {

        private ArrayList<DeviceModel> DeviceModelArrayList;
        public Context context;
        final private RVItemDeviceOnClickListener onClickListener;

        public BTDevicesRecyclerViewAdapter(ArrayList<DeviceModel> DeviceList,
                                            RVItemDeviceOnClickListener onClickListener,
                                            Context context) {
            this.DeviceModelArrayList = DeviceList;
            this.context = context;
            this.onClickListener = onClickListener;
        }

        @NonNull
        @Override
        public BTDevicesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            view = inflater.inflate(R.layout.recycle_device_item_view, parent, false);

            return new BTDevicesRecyclerViewAdapter.ViewHolder(view);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            private final TextView deviceAddress;
            private final TextView deviceName;
            //private final TextView deviceSignal;

            public ViewHolder(View view) {
                super(view);

                // Define click listener for the ViewHolder's View
                view.setOnClickListener((View.OnClickListener) this);
                //view.setOnLongClickListener((View.OnLongClickListener) this);

                deviceAddress = (TextView) view.findViewById(R.id.device_mac_address);
                deviceName = (TextView) view.findViewById(R.id.device_name);
            }

            @Override
            public void onClick(View v) {
                int position = getBindingAdapterPosition();

                if (position >= RecyclerView.NO_POSITION) {
                    onClickListener.onClick(position);
                }
            }

            public TextView getDeviceAddress() {
                return deviceAddress;
            }
            public TextView getDeviceName() {
                return deviceName;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull BTDevicesRecyclerViewAdapter.ViewHolder holder, int position) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            if (this.DeviceModelArrayList != null){
                DeviceModel deviceItem = DeviceModelArrayList.get(position);
                holder.getDeviceAddress().
                        setText(deviceItem.getBTDeviceAddress().toString());

                holder.getDeviceName().
                        setText(deviceItem.getBTDeviceName());

                if (selected_device_position >= 0 && (selected_device_position == position)){
                    holder.itemView.setBackgroundColor(Color.CYAN);
                }
                else {
                    holder.itemView.setBackgroundColor(Color.YELLOW);
                }
            }
        }

        @Override
        public int getItemCount() {
            if (this.DeviceModelArrayList == null){
                return 0;
            }
            return this.DeviceModelArrayList.size();
        }
    }


    private void createBTDevicesInRecyclerViewAdapter(){
        // Show BT devices in RecyclerView type of List view:
        servicesRecyclerView = findViewById(R.id.BT_Devices_RecyclerView);
        servicesRecyclerView.setHasFixedSize(false);

        serviceLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // Applying OnClickListener to RecyclerView adapter
        RVItemDeviceOnClickListener rVItemDeviceOnClickListener=
                new RVItemDeviceOnClickListener() {
            @Override
            public void onClick(int position) {
            /*      if (selected_position == position){
                        // unselect position, if this is item is clicked twice
                        selected_position = -1;
            }*/

                // Updating old as well as new positions
                servicesAdapter.notifyItemChanged(selected_device_position);
                selected_device_position = position;
                servicesAdapter.notifyItemChanged(selected_device_position);

            }
        };

        servicesAdapter = new BTDevicesRecyclerViewAdapter(mDevices,
                rVItemDeviceOnClickListener,
                MainActivity.this);

        servicesRecyclerView.setLayoutManager(serviceLayoutManager);
        servicesRecyclerView.setAdapter(servicesAdapter);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(servicesRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL); // TODO: any get-method available???
        //mDividerItemDecoration.setDrawable();
        servicesRecyclerView.addItemDecoration(mDividerItemDecoration);

    }

    public void addDevice(ScanResult result) {
        DeviceModel device =
                new DeviceModel(result.getDevice(),
                        "unknown",
                        result.getRssi());

        if( (mLeDevices == null) ||
                (!mLeDevices.contains(device.getBTDeviceAddress()))) {

            mLeDevices.add(device.getBTDeviceAddress());
            mDevices.add(device);

            servicesAdapter.notifyDataSetChanged();
        }
    }

}
