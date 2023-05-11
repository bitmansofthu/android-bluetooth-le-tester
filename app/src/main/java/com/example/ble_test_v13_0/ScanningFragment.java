package com.example.ble_test_v13_0;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

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

    ProgressBar scanningProgressBar;

    // Stop scanning automatically after 60 seconds
    // (if not stopped manually earlier).
    static final long SCAN_PERIOD = 60000; // ms

    Handler scanning_handler = new Handler(Looper.myLooper());

    private RecyclerView.Adapter devicesAdapter;

    ArrayList<BluetoothDevice> mLeDevices = new ArrayList<>();
    ArrayList<DeviceModel> mDevices = new ArrayList<>();

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

        btLeScanner = ((MainActivity) requireActivity()).btAdapter.getBluetoothLeScanner();

        startScanningButton = fragment_view.findViewById(R.id.scan_button);
        startScanningButton.setOnClickListener(v -> toggleScanning());

        scanningProgressBar = fragment_view.findViewById(R.id.scanProgressBar);
        scanningProgressBar.setVisibility(INVISIBLE);

        createBTDevicesInRecyclerViewAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("onStop scan fragment"); //todo
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy scan fragment");
    }

    // This method is called when the fragment is no longer connected to the Activity
    // Any references saved in onAttach should be null out here to prevent memory leaks.
    @Override
    public void onDetach() {
        super.onDetach();
        System.out.println("onDetach scan fragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("onPause scan fragment");
    }

    // Device scan callback.
    ScanCallback leScanCallback =
            new ScanCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    addDevice(result); // add device-details to RecyclerView List adapter

                    BluetoothDevice device = result.getDevice();
                    int signal = result.getRssi();

                    System.out.println("BT address: " + device + " | name: " +
                            device.getName() +
                            " | rssi: " + signal); //todo
                }
            };

    @SuppressLint("MissingPermission")
    // MissingPermission here just to avoid warnings. Runtime permission for BT_SCAN
    // has (and must) been checked earlier.
    public void toggleScanning() {
        // trigger connection establishment for selected BT-device
        if (((MainActivity) requireActivity()).mConnectionState ==
                BT_CONNECTION_STATE.NOT_SCANNING) {

            ((MainActivity) requireActivity()).
                    HandleBleConnection(BT_CONNECTION_STATE.SCANNING);

            startScanningButton.setText(R.string.StopScan);
            scanningProgressBar.setVisibility(VISIBLE);

            // start scanning by cleaning device-list
            if (mLeDevices != null){
                int deviceCnt = mLeDevices.size();
                mLeDevices.clear();
                mDevices.clear();
                devicesAdapter.notifyItemRangeRemoved(0, deviceCnt);

            }

            btLeScanner.startScan(leScanCallback);

            // remove all messages (some delayed posts might still
            // be in the message queue)
            scanning_handler.removeCallbacksAndMessages(null);

            // Stops scanning after a predefined scan period by creating new delayed post.
            scanning_handler.postDelayed(() -> {
                if (((MainActivity) requireActivity()).mConnectionState ==
                        BT_CONNECTION_STATE.SCANNING) {

                    ((MainActivity) requireActivity()).
                            HandleBleConnection(BT_CONNECTION_STATE.NOT_SCANNING);

                    btLeScanner.stopScan(leScanCallback);
                    startScanningButton.setText(R.string.StartScan);
                    scanningProgressBar.setVisibility(INVISIBLE);
                }
            }, SCAN_PERIOD);

        } else if (((MainActivity) requireActivity()).mConnectionState ==
                BT_CONNECTION_STATE.SCANNING) {

            ((MainActivity) requireActivity()).
                    HandleBleConnection(BT_CONNECTION_STATE.NOT_SCANNING);

            startScanningButton.setText(R.string.StartScan);
            scanningProgressBar.setVisibility(INVISIBLE);

            btLeScanner.stopScan(leScanCallback);

            // remove all messages (some delayed posts might still
            // be in the message queue)
            scanning_handler.removeCallbacksAndMessages(null);
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

        if( ( (mLeDevices == null) && (mDevices == null) ) ||
                (!Objects.requireNonNull(mLeDevices).contains(device.getBTDeviceAddress()))) {

            mLeDevices.add(device.getBTDeviceAddress());
            mDevices.add(device);

            // added as last item, so notify the adapter for change of the last item
            devicesAdapter.notifyItemInserted(mDevices.size() - 1 );
        }
    }

    public static class BTDevicesRecyclerViewAdapter extends RecyclerView.Adapter<BTDevicesRecyclerViewAdapter.ViewHolder> {

        private ArrayList<DeviceModel> DeviceModelArrayList;
        public Context context;
        final private RVItemDeviceOnClickListener onClickListener;

        final private RVItemDeviceOnLongClickListener longClickListener;

        public BTDevicesRecyclerViewAdapter(ArrayList<DeviceModel> DeviceList,
                                            RVItemDeviceOnClickListener onClickListener,
                                            RVItemDeviceOnLongClickListener onLongClickListener,
                                            Context context) {
            this.DeviceModelArrayList = DeviceList;
            this.context = context;
            this.onClickListener = onClickListener;
            this.longClickListener = onLongClickListener;
        }

        @NonNull
        @Override
        public BTDevicesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            view = inflater.inflate(R.layout.recycle_device_item_view, parent, false);
            System.out.println("onCreateViewHolder RV");
            return new BTDevicesRecyclerViewAdapter.ViewHolder(view);
        }

        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener
        {
            private final TextView deviceAddress;
            private final TextView deviceName;
            //private final TextView deviceSignal;

            public ViewHolder(View view) {
                super(view);

                // Define click listener for the ViewHolder's View
                view.setOnClickListener(this);
                view.setOnLongClickListener(this);

                deviceAddress = view.findViewById(R.id.device_mac_address);
                deviceName = view.findViewById(R.id.device_name);
            }

            @Override
            public void onClick(View v) {
                int position = getBindingAdapterPosition();

                if (position > RecyclerView.NO_POSITION) {
                    onClickListener.onClick(position);
                }
            }

            @Override
            public boolean onLongClick(View v) {
                int position = getBindingAdapterPosition();

                if (position > RecyclerView.NO_POSITION) {
                    return longClickListener.onLongClick(position);
                }
                return false;
            }

            public TextView getDeviceAddress() {
                return deviceAddress;
            }
            public TextView getDeviceName() {
                return deviceName;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            if (this.DeviceModelArrayList != null){
                DeviceModel deviceItem = DeviceModelArrayList.get(position);
                holder.getDeviceAddress().
                        setText(deviceItem.getBTDeviceAddress().toString());

                holder.getDeviceName().
                        setText(deviceItem.getBTDeviceName());
                //set light blue
                holder.itemView.setBackgroundColor(0xFFD0FDF3);
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
        RecyclerView devicesRecyclerView = fragment_view.findViewById(R.id.BT_Devices_RecyclerView);
        devicesRecyclerView.setHasFixedSize(false);

        RecyclerView.LayoutManager deviceLayoutManager = new LinearLayoutManager(this_context, LinearLayoutManager.VERTICAL, false);

        // Applying OnClickListener to RecyclerView adapter
        RVItemDeviceOnClickListener rVItemDeviceOnClickListener=
                new RVItemDeviceOnClickListener() {

                    public void onClick(int position) {
                    }
                };

        // Applying OnClickListener to RecyclerView adapter
        RVItemDeviceOnLongClickListener rVItemDeviceOnLongClickListener=
                new RVItemDeviceOnLongClickListener() {

                    public boolean onLongClick(int position) {
                        // Selected device item is clicked.
                        // Start to connect to remote device using
                        // MAC-address of device-item.
                        if (position != RecyclerView.NO_POSITION){
                            DeviceModel deviceItem = mDevices.get(position);

                            // set selected BT-device
                            ((MainActivity) requireActivity()).
                                    setMyBTDevice(deviceItem.getBTDeviceAddress());

                            // trigger connection establishment for selected BT-device
                            ((MainActivity) requireActivity()).
                                    HandleBleConnection(BT_CONNECTION_STATE.CONNECTING);
                        }
                        return true;
                    }
                };

        devicesAdapter = new BTDevicesRecyclerViewAdapter(mDevices,
                rVItemDeviceOnClickListener,
                rVItemDeviceOnLongClickListener,
                this_context);

        devicesRecyclerView.setLayoutManager(deviceLayoutManager);
        devicesRecyclerView.setAdapter(devicesAdapter);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(devicesRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL); // TODO: any get-method available???
        //mDividerItemDecoration.setDrawable();
        devicesRecyclerView.addItemDecoration(mDividerItemDecoration);

    }
}