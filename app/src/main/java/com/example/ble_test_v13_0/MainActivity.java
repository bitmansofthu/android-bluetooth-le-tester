package com.example.ble_test_v13_0;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
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

import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
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

// TODO: move to some proper place
    private RecyclerView servicesRecyclerView;
    private RecyclerView.Adapter servicesAdapter;

    private ExpandableListView servicesExpandableListView;
    private ExpandableListAdapter expandableServicesAdapter;
    ArrayList<ServiceModel> serviceModelArrayList = new ArrayList<>();
    ArrayList<ArrayList<CharacteristicsModel>> characteristicsModelArrayList =
            new ArrayList<ArrayList<CharacteristicsModel>>();
    ArrayList<Boolean> characteristicsItemEnabledList = new ArrayList<>();
    private RecyclerView.LayoutManager serviceLayoutManager;

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
        connectButton.setVisibility(VISIBLE);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                HandleBleConnection();
            }
        });

        showGattProfiles();
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
                    connectButton.setVisibility(VISIBLE);
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
                btGatt = getmMyBTDevice().connectGatt(MainActivity.this, false, gattCallback);
            }
            else if (connectButton.getText() == "Disconnect"){
                connectButton.setText("Disconnecting");
                btGatt.disconnect();
            }

        }
    }

    public class CharacteristicsModel {
        private String characteristicsUUID;

        public CharacteristicsModel(String characteristicsUUID) {
            this.characteristicsUUID = characteristicsUUID;
        }

        public String getCharacteristicsUUID() {
            return this.characteristicsUUID;
        }
    }

    public class ServiceModel {
        private String serviceName;
        private String serviceUUID;

        public ServiceModel(String serviceName, String serviceUUID) {
            this.serviceName = serviceName;
            this.serviceUUID = serviceUUID;
        }

        public String getServiceName() {
            return this.serviceName;
        }
        public String getServiceUUID() {
            return this.serviceUUID;
        }
    }

    public class BTCharacteristicRecyclerViewAdapter extends RecyclerView.Adapter<BTCharacteristicRecyclerViewAdapter.ViewHolder> {

        private ArrayList<CharacteristicsModel> CharacteristicsModelArrayList;
        public Context context;

        public BTCharacteristicRecyclerViewAdapter(ArrayList<CharacteristicsModel> characteristicsList, Context context) {
            this.CharacteristicsModelArrayList = characteristicsList;
            this.context = context;
        }

        @NonNull
        @Override
        public BTCharacteristicRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            //if(viewType == 1){
            view = inflater.inflate(R.layout.recycle_characteristics_item_view, parent, false);
            //}
            return new BTCharacteristicRecyclerViewAdapter.ViewHolder(view);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView characteristicsUUID;

            public ViewHolder(View view) {
                super(view);
                // Define click listener for the ViewHolder's View

                characteristicsUUID = (TextView) view.findViewById(R.id.char_uuid);
            }

            public TextView getCharacteristicsUUID() {
                return characteristicsUUID;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull BTCharacteristicRecyclerViewAdapter.ViewHolder holder, int position) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            CharacteristicsModel characteristicsItem = CharacteristicsModelArrayList.get(position);
            holder.getCharacteristicsUUID().setText(characteristicsItem.getCharacteristicsUUID());

            System.out.println("\tCharacteristics bind!");
        }

        @Override
        public int getItemCount() {
            return CharacteristicsModelArrayList.size();
        }
    }
    public class BTServicesRecyclerViewAdapter extends RecyclerView.Adapter<BTServicesRecyclerViewAdapter.ViewHolder>{

        private ArrayList<ServiceModel> ServiceModelArrayList;
        public Context context;

        public BTServicesRecyclerViewAdapter(ArrayList<ServiceModel> serviceList, Context context) {
            this.ServiceModelArrayList = serviceList;
            this.context = context;
        }

        /*
        * class RecycleAdapter(var mContext: Context, val list: MutableList<ParentData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if(viewType== Constants.PARENT){
            val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.parent_row, parent,false)
           GroupViewHolder(rowView)
        } else {
            val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.child_row, parent,false)
           ChildViewHolder(rowView)
        }
    }        * */

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView serviceName;
            private final TextView serviceUUID;
            public RecyclerView characteristicsRecyclerView;
            public ViewHolder(View view) {
                super(view);
                // Define click listener for the ViewHolder's View

                serviceName = (TextView) view.findViewById(R.id.service_name);
                serviceUUID = (TextView) view.findViewById(R.id.service_id);

                characteristicsRecyclerView = itemView.findViewById(R.id.Characteristics_RecyclerView);
            }

            public TextView getServiceName() {
                return serviceName;
            }
            public TextView getServiceUUID() {
                return serviceUUID;
            }
        }

        @NonNull
        @Override
        public BTServicesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            //if(viewType == 1){
                view = inflater.inflate(R.layout.recycle_service_item_view, parent, false);
            //}
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            ServiceModel serviceItem = ServiceModelArrayList.get(position);
            holder.getServiceName().setText(serviceItem.getServiceName());
            holder.getServiceUUID().setText(serviceItem.getServiceUUID());

            System.out.println("\tService bind to position: " + position + " cnt: " + characteristicsModelArrayList.size());

            if ((characteristicsItemEnabledList.size() > position) &&
                    (characteristicsItemEnabledList.get(position) == false)) {

                if( (characteristicsModelArrayList.size() > position) &&
                        (characteristicsModelArrayList.get(position) !=null) )
                {
                    ArrayList<CharacteristicsModel> CharPerServiceArrayList =
                            new ArrayList<CharacteristicsModel>();

                    CharPerServiceArrayList.addAll(characteristicsModelArrayList.get(position));
                    characteristicsItemEnabledList.set(position, true);

                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false);
                    holder.characteristicsRecyclerView.setLayoutManager(layoutManager);
                    holder.characteristicsRecyclerView.setHasFixedSize(true);
                    BTCharacteristicRecyclerViewAdapter characteristicsRecyclerViewAdapter =
                            new BTCharacteristicRecyclerViewAdapter(CharPerServiceArrayList,
                                                                    holder.characteristicsRecyclerView.getContext());

                    holder.characteristicsRecyclerView.setAdapter(characteristicsRecyclerViewAdapter);
                    characteristicsRecyclerViewAdapter.notifyDataSetChanged();
                }
            }

        }

        @Override
        public int getItemCount() {
            return ServiceModelArrayList.size();
        }
    }

    private void showGattProfiles(){
        serviceModelArrayList.add(new ServiceModel("PROFILE 1", "1234-4567"));
        {
            ArrayList<CharacteristicsModel> CharPerServiceArrayList =
                    new ArrayList<CharacteristicsModel>();
            CharPerServiceArrayList.add(new CharacteristicsModel("12345678"));
            characteristicsModelArrayList.add(CharPerServiceArrayList);
        }

        serviceModelArrayList.add(new ServiceModel("PROFILE 2", "2345-5678"));
        {
            ArrayList<CharacteristicsModel> CharPerServiceArrayList =
                    new ArrayList<CharacteristicsModel>();
                    new ArrayList<CharacteristicsModel>();
            CharPerServiceArrayList.add(new CharacteristicsModel("12345678"));
            CharPerServiceArrayList.add(new CharacteristicsModel("87654321"));
            characteristicsModelArrayList.add(CharPerServiceArrayList);
        }

        serviceModelArrayList.add(new ServiceModel("PROFILE 3", "3456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 4", "4234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 5", "5345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 6", "6456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 7", "7234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 8", "8345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 9", "9456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 10", "10234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 11", "11345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 12", "12456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 13", "13234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 14", "14345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 15", "15456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 16", "16234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 17", "17345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 18", "18456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 19", "19234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 20", "20345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 21", "21456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 22", "22234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 23", "23345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 24", "24456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 25", "25234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 26", "26345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 27", "27456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 28", "28234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 29", "29345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 30", "30456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 31", "31234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 32", "32345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 33", "3456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 34", "34234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 35", "35345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 36", "36456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 37", "37234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 38", "38345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 39", "39456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 40", "40234-4567"));
        {
            ArrayList<CharacteristicsModel> CharPerServiceArrayList =
                    new ArrayList<CharacteristicsModel>();
            new ArrayList<CharacteristicsModel>();
            CharPerServiceArrayList.add(new CharacteristicsModel("97654321"));
            CharPerServiceArrayList.add(new CharacteristicsModel("82345678"));
            CharPerServiceArrayList.add(new CharacteristicsModel("77654321"));
            CharPerServiceArrayList.add(new CharacteristicsModel("62345678********"));
            CharPerServiceArrayList.add(new CharacteristicsModel("57654321----------"));
            characteristicsModelArrayList.add(CharPerServiceArrayList);
        }

        serviceModelArrayList.add(new ServiceModel("PROFILE 41", "41345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 42", "42456-6789"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 43", "43234-4567"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 44", "44345-5678"));
            characteristicsModelArrayList.add(null);
        serviceModelArrayList.add(new ServiceModel("PROFILE 45", "45456-6789"));
            characteristicsModelArrayList.add(null);

/*        for (int i=0; i<serviceModelArrayList.size(); i++)
        {
            characteristicsItemEnabledList.add(false);
        }*/

        showGattProfilesInExpandableListView();
    }

    public void showGattProfilesInExpandableListView(){
        servicesExpandableListView = (ExpandableListView)findViewById(R.id.Services_expandableListView);

        // Show services in Expandable type of List view (characteristics expanded)
        expandableServicesAdapter = new ServicesExpandableListAdapter(MainActivity.this,
                serviceModelArrayList, characteristicsModelArrayList);
        servicesExpandableListView.setAdapter(expandableServicesAdapter);

        expandableServicesAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });

        servicesExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        serviceModelArrayList.get(groupPosition) + " List Expanded.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    private void showGattProfilesInRecyclerView(){
        // Show services in RecyclerView type of List view:
        // One Parent RecyclerView (RV) for services
        // Child RVs for each characteristics (created inside BTServicesRecyclerViewAdapter)
        servicesRecyclerView = findViewById(R.id.Services_recyclerView);
        servicesRecyclerView.setHasFixedSize(true);

        serviceLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        servicesAdapter = new BTServicesRecyclerViewAdapter(serviceModelArrayList, MainActivity.this);
        servicesRecyclerView.setLayoutManager(serviceLayoutManager);
        servicesRecyclerView.setAdapter(servicesAdapter);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(servicesRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL); // TODO: any get-method available???
        //mDividerItemDecoration.setDrawable();
        servicesRecyclerView.addItemDecoration(mDividerItemDecoration);

        servicesAdapter.notifyDataSetChanged();
    }
    */
}
