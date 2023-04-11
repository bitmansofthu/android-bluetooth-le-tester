package com.example.ble_test_v13_0;

import androidx.appcompat.app.AppCompatActivity;

import android.database.DataSetObserver;
import android.bluetooth.BluetoothGattCharacteristic;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ServicesActivity extends AppCompatActivity {
    Button disconnectButton;

    private ExpandableListView servicesExpandableListView;
    private ExpandableListAdapter expandableServicesAdapter;

    ArrayList<ServiceModel> serviceModelArrayList = new ArrayList<>();
    ArrayList<ArrayList<CharacteristicsModel>> characteristicsModelArrayList =
            new ArrayList<ArrayList<CharacteristicsModel>>();
    //ArrayList<Boolean> characteristicsItemEnabledList = new ArrayList<>(); //Todo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        disconnectButton = findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
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
        servicesExpandableListView = (ExpandableListView)findViewById(R.id.Services_expandableListView);

        // Show services in Expandable type of List view (characteristics expanded)
        expandableServicesAdapter = new ServicesExpandableListAdapter(ServicesActivity.this,
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

}