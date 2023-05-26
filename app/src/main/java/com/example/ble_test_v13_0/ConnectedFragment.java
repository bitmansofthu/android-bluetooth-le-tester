package com.example.ble_test_v13_0;

import static android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothStatusCodes;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
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
import java.util.Locale;
import java.util.UUID;

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

    // UUID for Client Characteristic Configuration
    // See BT SIG: public/assigned_numbers/uuids/descriptors.yaml
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    ArrayList<ServiceModel> serviceModelArrayList = new ArrayList<>();
    ArrayList<ArrayList<CharacteristicsModel>> characteristicsModelArrayList =
            new ArrayList<>();

    ArrayList<ArrayList<BluetoothGattCharacteristic>> BtCharacteristicsArrayOfArrayList =
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
        disconnectButton = fragment_view.findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener(v -> {
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

        for(byte byteChar : value)
            string.append(String.format("%02X", byteChar));

        for (int groupPos = 0;
             groupPos < expandableServicesAdapter.getGroupCount();
             groupPos++)
        {
            for (int childPosition = 0;
                 childPosition < expandableServicesAdapter.getChildrenCount(groupPos);
                 childPosition++)
            {
                if (BtCharacteristicsArrayOfArrayList.get(groupPos).get(childPosition)==
                        characteristic)
                {
                    characteristicsModelArrayList.
                        get(groupPos).
                            get(childPosition).setCharacteristicsValue(string.toString());

                    // NOTICE: cast to (BaseExpandableListAdapter) needed
                    // for accessing notifyDataSetChanged !!!
                    ((BaseExpandableListAdapter)
                            expandableServicesAdapter).notifyDataSetChanged();

                    break;
                }
            }
        }

    }

    public void GattCharacteristicsValueWriteFailed(
            BluetoothGattCharacteristic characteristic){
        Toast.makeText(this_context, "Failed to write value. " +
                "Check the content and try again.", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("MissingPermission")
    public void GattServicesDiscovered(){

        // collect discovered services
        List<BluetoothGattService> gattServices =  ((MainActivity) requireActivity()).btGatt.getServices();

        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            String serviceName = ((MainActivity) requireActivity()).
                    reserved_uuid_lookup(gattService.getUuid().toString().toLowerCase(Locale.ROOT));

            String serviceUuid = gattService.getUuid().toString();

            serviceModelArrayList.add(new ServiceModel(serviceName, serviceUuid ));

            // Array list for characteristics included per single service.
            // This list is type of CharacteristicsModel used by ServicesExpandableListAdapter.
            // So only items shown in the display are gathered here from received characteristics.
            ArrayList<CharacteristicsModel> CharPerServiceArrayList =
                    new ArrayList<>();

            ArrayList<BluetoothGattCharacteristic> BtCharacteristicsArrayList =
                    new ArrayList<>();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                String characteristicsName = ((MainActivity) requireActivity()).
                        reserved_uuid_lookup(gattCharacteristic.getUuid().toString().toLowerCase(Locale.ROOT));

                String characteristicsUuid = gattCharacteristic.getUuid().toString();

                // check and set properties
                int property = gattCharacteristic.getProperties();
                boolean readAccess;
                boolean writeAccess;
                boolean notificationAccess;

                readAccess = (property & BluetoothGattCharacteristic.PROPERTY_READ) != 0;

                writeAccess = ((property & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) ||
                        ((property & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) ||
                        ((property & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0);

                notificationAccess = (property & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;

                CharPerServiceArrayList.add(new CharacteristicsModel
                        (characteristicsUuid, characteristicsName, "NULL",
                                true, readAccess, writeAccess, notificationAccess,
                                true));

                BtCharacteristicsArrayList.add(gattCharacteristic);
            }

            characteristicsModelArrayList.add(CharPerServiceArrayList);
            BtCharacteristicsArrayOfArrayList.add(BtCharacteristicsArrayList);
        }

        showGattProfilesInExpandableListView();
    }

    public void readStateHandler(int groupPosition, int childPosition,
                                    boolean enable){

        // Notice, that clicking of the read radio-button doesn't trigger actual
        // read-message to GATT-server. ACK-button does that..

        if (!characteristicsModelArrayList.
                get(groupPosition).
                get(childPosition).getReadAccess()){
            return; // read not supported for this characteristic
        }

        if (enable == characteristicsModelArrayList.
                get(groupPosition).get(childPosition).
                getReadChecked()){
            return; // no state change
        }

        // change the state in the adapter
        characteristicsModelArrayList.get(groupPosition).
                get(childPosition).setReadChecked(enable);
    }

    public void writeStateHandler(int groupPosition, int childPosition,
                                 boolean enable){
        // Notice, that clicking of the write radio-button doesn't trigger actual
        // write-message to GATT-server. ACK-button does that..

        if (!characteristicsModelArrayList.
                get(groupPosition).
                get(childPosition).getWriteAccess()){
            return; // write not supported for this characteristic
        }

        if (enable == characteristicsModelArrayList.
                get(groupPosition).get(childPosition).
                getWriteChecked()){
            return; // no state change
        }

        // change the state in the adapter
        characteristicsModelArrayList.get(groupPosition).
                get(childPosition).setWriteChecked(enable);
    }

    @SuppressLint("MissingPermission")
    public void notificationStateHandler(int groupPosition, int childPosition,
                                    boolean enable){
        // Pushing of the Notification radio-button triggers
        // 'Notification enable'-message to GATT-server.
        // Notification will be disabled, if Read radio-button or
        // Write radio-button will be pushed. That's because single Radio-button
        // can't be toggled by clicking it several times.

        BluetoothGattCharacteristic characteristic =
                BtCharacteristicsArrayOfArrayList.get(groupPosition).
                        get(childPosition);

        if (!characteristicsModelArrayList.
                get(groupPosition).
                get(childPosition).getNotificationAccess()){
            return; // notification not supported for this characteristic
        }

        if (enable == characteristicsModelArrayList.
                get(groupPosition).get(childPosition).
                getNotificationChecked()){
            return; // no state change
        }

        // State change: Notification-radio button is pushed down or up
        // -> notifications are enabled or disabled.

        ((MainActivity) requireActivity()).
                btGatt.setCharacteristicNotification(characteristic, enable);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));

        if (descriptor != null){
            //if (descriptor.getValue()==ENABLE_NOTIFICATION_VALUE)
            if (enable){ descriptor.setValue(ENABLE_NOTIFICATION_VALUE); }
            else{ descriptor.setValue(DISABLE_NOTIFICATION_VALUE); }

            ((MainActivity) requireActivity()).btGatt.writeDescriptor(descriptor);
        }

        // change the state in the adapter
        characteristicsModelArrayList.get(groupPosition).
                get(childPosition).setNotificationChecked(enable);
    }

    // read/write/notify radio buttons
    @SuppressLint("MissingPermission")
    LvChildItemRbOnClickListener rwnRadioButtonOnClickListener =
            (groupPosition, childPosition,
             readChecked,
             writeChecked,
             notificationChecked) -> {

        readStateHandler(groupPosition, childPosition, readChecked);
        writeStateHandler(groupPosition, childPosition, writeChecked);
        notificationStateHandler(groupPosition, childPosition, notificationChecked);
    };

    // onClick-handler for triggering the read.
    // When some characteristics-item is clicked on ServicesExpandableList, content of the
    // attribute will be read from the remote device.
    @SuppressLint("MissingPermission")
    LVChildItemReadCharacteristicOnClickListener readCharacteristicOnClickListener =
        (groupPosition, childPosition,
         read_access_checked,
         write_access_checked, editableValue,
         notification_access_checked) -> {
            //todo remove: System.out.println("Clicked: " + groupPosition +" | " + childPosition);
//            System.out.println("read_checked: " + read_access_checked +
//                    " | write_checked: " + write_access_checked);
        if (read_access_checked){
            ((MainActivity) requireActivity()).
                    btGatt.
                    readCharacteristic(BtCharacteristicsArrayOfArrayList.
                            get(groupPosition).
                            get(childPosition));
        }
        else if (write_access_checked){
            BluetoothGattCharacteristic characteristic =
                    BtCharacteristicsArrayOfArrayList.get(groupPosition).
                            get(childPosition);

            //todo: separate to new function
            if (editableValue != null){
                String nibbleString;
                if ( (editableValue.length() % 2) != 0){
                    // adjust to even number of octets to make nibble-to-byte conversion easier
                    // e.g. 'abc' -> '0abc'
                    nibbleString = "0".
                            concat(editableValue).toLowerCase(Locale.ROOT); // e.g 'A' -> 'a'
                }
                else{
                    nibbleString = editableValue.toLowerCase(Locale.ROOT);
                }

                int nibbleLength = nibbleString.length();
                int byteLength = nibbleLength / 2; // single octet contains two nibbles

                byte[] outValueBuff = new byte[byteLength];

                byte outValueByte = 0;
                boolean badInput = false;
                int index = 0;
                // edited value:
                // e.g. typed value in UI = "abcd" (Hexadecimal nibbles as ASCII-formatted)
                // -> e.g. 'a' is index 0 in editableValue because it's typed first.
                // 'b' is index 1 etc...
                // Outcome should be: outValueBuff= [0xcd, 0xab]
                // (0xcd in index 0), because 0xcd is actually LSB.
                // So reverse_index is used for reversing the byte order:
                int reverse_index = nibbleLength - 1;
                for (; index < nibbleLength; index++, reverse_index--)
                {
                    char nibbleAscii = nibbleString.charAt(index);
                    byte nibbleByte;

                    // Convert ASCII-formatted hexadecimal nibbles to integer

                    if ( (nibbleAscii >= '0') && (nibbleAscii <= '9'))
                    {
                        nibbleByte = (byte)(nibbleAscii - '0');
                    }
                    else if ( (nibbleAscii >= 'a') && (nibbleAscii <= 'f'))
                    {
                        nibbleByte = (byte)(nibbleAscii - 'a');
                        nibbleByte += 10; // e.g. a(hex) = 10(dec)
                    }
                    else{
                        nibbleString = ""; // clean edited value
                        badInput = true;

                        break;
                    }

                    if ((index % 2) == 0) { // lsb nibble
                        outValueByte = (byte) (nibbleByte & 0x0f);
                    }
                    else { // msb nibble
                        outValueByte = (byte) ( (byte) ((outValueByte << 4) & 0xf0) | nibbleByte);
                        outValueBuff[reverse_index / 2] = outValueByte;
                    }
                }

                if (!badInput){
                    // If supported (hexadecimal) value, sent value to peripheral (server).

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        // SDK >=v33 (OS ver.13)

                        int retValue = ((MainActivity) requireActivity()).
                                btGatt.writeCharacteristic
                                        (characteristic,
                                        outValueBuff,
                                         //WRITE_TYPE_NO_RESPONSE, WRITE_TYPE_SIGNED ??
                                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

                        if (retValue != BluetoothStatusCodes.SUCCESS){
                            Log.w(TAG, "Write failed"); // todo: what to do?
                        }
                    }
                    else{
                        // This method was deprecated in API level 33...
                        characteristic.setValue(outValueBuff);
                        boolean retValue = ((MainActivity) requireActivity()).
                                btGatt.writeCharacteristic(characteristic);

                        if (!retValue){
                            Log.w(TAG, "Write failed"); // todo: what to do?
                        }
                    }
                }
                else{
                    Toast.makeText(this_context, "Non-hexadecimal value entered." +
                            "Write new value in range [A...F] and [0...9].", Toast.LENGTH_LONG).show();
                }

                // update new value to the adapter
                characteristicsModelArrayList.
                        get(groupPosition).
                        get(childPosition).setCharacteristicsValue(nibbleString);
                // and notify the adapter for updating the view
                ((BaseExpandableListAdapter)
                        expandableServicesAdapter).notifyDataSetChanged();
            }
        }
        else if (notification_access_checked){
        }
    };

    public void showGattProfilesInExpandableListView() {
        // Show services in Expandable type of List view (characteristics expanded)

        servicesExpandableListView = fragment_view.findViewById(R.id.Services_expandableListView);

        // Create the adapter for ExpandableListView by giving desired data-set
        // (service list and characteristic list), and register OnClick-listener
        // for reading GATT/ATT characteristics attribute values.
        // Listener is published via new interface LVChildItemReadCharacteristicOnClickListener,
        // for avoiding to keep listener inside the adapter (otherwise hard to search from the code
        // where on earth the listener is located...)
        expandableServicesAdapter = new ServicesExpandableListAdapter(this_context,
                serviceModelArrayList, characteristicsModelArrayList,
                readCharacteristicOnClickListener,
                rwnRadioButtonOnClickListener);

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