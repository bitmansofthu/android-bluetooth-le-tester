package com.example.ble_test_v13_0;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class BTDevicesRecyclerViewAdapter extends RecyclerView.Adapter<BTDevicesRecyclerViewAdapter.ViewHolder> {

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

