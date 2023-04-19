package com.example.ble_test_v13_0;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanningFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanningFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public View fragment_view; //todo

    Context this_context;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    BluetoothLeScanner btLeScanner;
    Button startScanningButton;

    // Stop scanning after 10 seconds.
    static final long SCAN_PERIOD = 100000;

    Handler handler = new Handler(Looper.myLooper());

    private RecyclerView devicesRecyclerView;
    private RecyclerView.Adapter devicesAdapter;

    private RecyclerView.LayoutManager deviceLayoutManager;

    // Index of the selected device in RecyclerView
    // (no selection by default)
    public int selected_device_position = RecyclerView.NO_POSITION;

    ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>(); //TODO: del !!
    ArrayList<DeviceModel> mDevices = new ArrayList<DeviceModel>();

    public ScanningFragment() {
        // Required empty public constructor
        super(R.layout.fragment_scanning);

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScanningFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScanningFragment newInstance(String param1, String param2) {
        ScanningFragment fragment = new ScanningFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragment_view = inflater.inflate(R.layout.fragment_scanning, container, false);

        this_context = container.getContext();

        return fragment_view;
    }


    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){

        btLeScanner = ((MainActivity)getActivity()).btAdapter.getBluetoothLeScanner();

        startScanningButton = (Button) fragment_view.findViewById(R.id.scan_button);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleScanning();
            }
        });

        createBTDevicesInRecyclerViewAdapter();
    }

    // Device scan callback.
    ScanCallback leScanCallback =
            new ScanCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    addDevice(result); // add device-details to RecyclerView List adapter

                    BluetoothDevice device = result.getDevice(); // TODO: move onto OnCreate??
                    int signal = result.getRssi();
                    System.out.println("BT address: " + device + " | name: " +
                            device.getName() +
                            " | rssi: " + signal);
                }
            };

    //@SuppressLint("SetTextI18n")
    @SuppressLint("MissingPermission")
    // MissingPermission here just to avoid warnings. Runtime permission for BT_SCAN
    // has (and must) been checked earlier.
    public void toggleScanning() {
        // trigger connection establishment for selected BT-device
        if (((MainActivity)getActivity()).mConnectionState ==
                BT_CONNECTION_STATE.NOT_SCANNING) {

            ((MainActivity)getActivity()).
                    HandleBleConnection(BT_CONNECTION_STATE.SCANNING);

            startScanningButton.setText("Stop scan");

            // start scanning by cleaning device-list
            if (mLeDevices != null){
                int deviceCnt = mLeDevices.size();
                mLeDevices.removeAll(mLeDevices);
                mDevices.removeAll(mDevices);
                devicesAdapter.notifyItemRangeRemoved(0, deviceCnt);
                selected_device_position = RecyclerView.NO_POSITION;
            }

            btLeScanner.startScan(leScanCallback);

            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (((MainActivity)getActivity()).mConnectionState ==
                            BT_CONNECTION_STATE.SCANNING) {

                        ((MainActivity)getActivity()).
                                HandleBleConnection(BT_CONNECTION_STATE.NOT_SCANNING);

                        btLeScanner.stopScan(leScanCallback);
                        startScanningButton.setText("Start scan");
                    }
                }
            }, SCAN_PERIOD);

        } else if (((MainActivity)getActivity()).mConnectionState ==
                BT_CONNECTION_STATE.SCANNING) {

            ((MainActivity)getActivity()).
                    HandleBleConnection(BT_CONNECTION_STATE.NOT_SCANNING);

            startScanningButton.setText("Start scan");
            btLeScanner.stopScan(leScanCallback);
        }
    }

    // Avoid warning for 'result.getDevice().getName()' for missing
    // BLUETOOTH_CONNECT permission.
    // Runtime permission is actually checked&granted earlier...
    @SuppressLint("MissingPermission")
    public void addDevice(ScanResult result) {
        String deviceName;
        if (result.getDevice().getName() == null){
            deviceName = "Unknown";
        }
        else{
            deviceName = result.getDevice().getName();
        }
        DeviceModel device =
                new DeviceModel(result.getDevice(),
                        deviceName,
                        result.getRssi());

        if( (mLeDevices == null) ||
                (!mLeDevices.contains(device.getBTDeviceAddress()))) {

            mLeDevices.add(device.getBTDeviceAddress());
            mDevices.add(device);

            devicesAdapter.notifyDataSetChanged();
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

                if (position > RecyclerView.NO_POSITION) {
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

                if (selected_device_position != RecyclerView.NO_POSITION
                        && (selected_device_position == position)){
                    holder.itemView.setBackgroundColor(Color.CYAN);
                }
                else {
                    // light blue
                    holder.itemView.setBackgroundColor(0xFFD0FDF3);
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
        devicesRecyclerView = fragment_view.findViewById(R.id.BT_Devices_RecyclerView);
        devicesRecyclerView.setHasFixedSize(false);

        deviceLayoutManager = new LinearLayoutManager(this_context, LinearLayoutManager.VERTICAL, false);

        // Applying OnClickListener to RecyclerView adapter
        RVItemDeviceOnClickListener rVItemDeviceOnClickListener=
                new RVItemDeviceOnClickListener() {
                    @Override
                    public void onClick(int position) {
                        if (selected_device_position == position) {
                            // Selected device item is clicked again.
                            // Start to connect to remote device using
                            // MAC-address of device-item.

                            DeviceModel deviceItem = mDevices.get(position);

                            // set selected BT-device
                            ((MainActivity)getActivity()).
                                    setMyBTDevice(deviceItem.getBTDeviceAddress());

                            // trigger connection establishment for selected BT-device
                            ((MainActivity)getActivity()).
                                    HandleBleConnection(BT_CONNECTION_STATE.CONNECTING);
                        }
                        else {
                            // Some other item selected.
                            // Update old as well as new position.
                            int previous_selected_device_position = selected_device_position;
                            selected_device_position = position;
                            devicesAdapter.notifyItemChanged(previous_selected_device_position);
                            devicesAdapter.notifyItemChanged(selected_device_position);
//todo
                            System.out.println("prev: " +
                                    previous_selected_device_position + " new: " + selected_device_position);
                        }
                    }
                };

        devicesAdapter = new BTDevicesRecyclerViewAdapter(mDevices,
                rVItemDeviceOnClickListener,
                this_context);

        devicesRecyclerView.setLayoutManager(deviceLayoutManager);
        devicesRecyclerView.setAdapter(devicesAdapter);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(devicesRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL); // TODO: any get-method available???
        //mDividerItemDecoration.setDrawable();
        devicesRecyclerView.addItemDecoration(mDividerItemDecoration);

    }
}