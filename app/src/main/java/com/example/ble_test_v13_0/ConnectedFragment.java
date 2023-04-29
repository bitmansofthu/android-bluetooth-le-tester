package com.example.ble_test_v13_0;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConnectedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectedFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public View fragment_view; //todo

    Context this_context;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button disconnectButton;

    private ExpandableListView servicesExpandableListView;
    private ExpandableListAdapter expandableServicesAdapter;

    ArrayList<ServiceModel> serviceModelArrayList = new ArrayList<>();
    ArrayList<ArrayList<CharacteristicsModel>> characteristicsModelArrayList =
            new ArrayList<>();

    ArrayList<ArrayList<BluetoothGattCharacteristic>> BTcharacteristicsArrayOfArrayList =
            new ArrayList<>();

    public ConnectedFragment() {
        // Required empty public constructor
        super(R.layout.fragment_connected);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConnectedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConnectedFragment newInstance(String param1, String param2) {
        ConnectedFragment fragment = new ConnectedFragment();
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
        fragment_view = inflater.inflate(R.layout.fragment_connected, container, false);

        this_context = container.getContext();

        return fragment_view;

    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        disconnectButton = (Button) fragment_view.findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((MainActivity) requireActivity()).mConnectionState ==
                        BT_CONNECTION_STATE.CONNECTED) {

                    // DISCONNECTING-event is generated only, when normal local disconnect is done.
                    // E.g disconnect from remote device is detected by BT gatt-callback,
                    // causing DISCONNECTED-event to be generated.
                    ((MainActivity) requireActivity()).
                            HandleBleConnection(BT_CONNECTION_STATE.DISCONNECTING);
                }
                //else
                // What about, if the state != CONNECTED ?
                // Fragment should be closed somehow.
                //I did some test where I powered off the remote BLE-device.
                // -> onConnectionStateChange-event with newState STATE_DISCONNECTED
                // -> causes ConnectedFragment to be closed from FragmentTransaction MainActivity.
                // OK!
            }
        });

        discoverGattServices();
    }

    @SuppressLint("MissingPermission")
    private void discoverGattServices() {
        ((MainActivity) requireActivity()).btGatt.discoverServices();
    }

    public void GattCharacteristicsValueReceived(BluetoothGattCharacteristic characteristic,
                                                 byte[] value)
    {
        final StringBuilder string = new StringBuilder(value.length);
        string.append("0x");
        for(byte byteChar : value)
            string.append(String.format("%02X ", byteChar));

        for (int groupPos = 0;
             groupPos < expandableServicesAdapter.getGroupCount();
             groupPos++)
        {
            for (int childPosition = 0;
                 childPosition < expandableServicesAdapter.getChildrenCount(groupPos);
                 childPosition++)
            {
                if (BTcharacteristicsArrayOfArrayList.get(groupPos).get(childPosition)==
                        characteristic)
                {
                    characteristicsModelArrayList.
                        get(groupPos).
                            get(childPosition).setCharacteristicsValue(string.toString());

                    // NOTICE: cast to (BaseExpandableListAdapter)
                    // for accessing notifyDataSetChanged !!!
                    ((BaseExpandableListAdapter)
                            expandableServicesAdapter).notifyDataSetChanged();

                    break;
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    public void GattServicesDiscovered(){

        // collect discovered services
        List<BluetoothGattService> gattServices =  ((MainActivity) requireActivity()).btGatt.getServices();

        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            serviceModelArrayList.add(new ServiceModel("SERVICE", gattService.getUuid().toString()));

            ArrayList<CharacteristicsModel> CharPerServiceArrayList =
                    new ArrayList<CharacteristicsModel>();

            ArrayList<BluetoothGattCharacteristic> BTcharacteristicsArrayList =
                    new ArrayList<>();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                CharPerServiceArrayList.add(new CharacteristicsModel
                        (gattCharacteristic.getUuid().toString(),
                                "NULL"));
                //gattCharacteristic.getDescriptors()
                BTcharacteristicsArrayList.add(gattCharacteristic);
            }

            characteristicsModelArrayList.add(CharPerServiceArrayList);
            BTcharacteristicsArrayOfArrayList.add(BTcharacteristicsArrayList);
        }

        showGattProfilesInExpandableListView();
    }

    public void showGattProfilesInExpandableListView(){
        // Show services in Expandable type of List view (characteristics expanded)

        servicesExpandableListView = (ExpandableListView)fragment_view.findViewById(R.id.Services_expandableListView);

        @SuppressLint("MissingPermission") LVChildItemReadCharacteristicOnClickListener readCharacteristicOnClickListener =
            (groupPosition, childPosition) -> {
                System.out.println("Clicked: " + groupPosition +" | " + childPosition);

                ((MainActivity) requireActivity()).
                        btGatt.
                            readCharacteristic(BTcharacteristicsArrayOfArrayList.
                                get(groupPosition).
                                    get(childPosition));
            };

        expandableServicesAdapter = new ServicesExpandableListAdapter(this_context,
                serviceModelArrayList, characteristicsModelArrayList,
                readCharacteristicOnClickListener);

        servicesExpandableListView.setAdapter(expandableServicesAdapter);

        //todo: unregister in onDestroy
        expandableServicesAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });

    }

}