package com.example.ble_test_v13_0;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
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
            new ArrayList<ArrayList<CharacteristicsModel>>();

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

        showGattProfiles();
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
        servicesExpandableListView = (ExpandableListView)fragment_view.findViewById(R.id.Services_expandableListView);

        // Show services in Expandable type of List view (characteristics expanded)
        expandableServicesAdapter = new ServicesExpandableListAdapter(this_context,
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
                Toast.makeText(this_context,
                        serviceModelArrayList.get(groupPosition) + " List Expanded.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}