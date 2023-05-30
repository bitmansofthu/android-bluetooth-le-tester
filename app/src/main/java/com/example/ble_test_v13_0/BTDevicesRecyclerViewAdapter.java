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

    private final ArrayList<DeviceModel> DeviceModelArrayList;
    public Context context; // private ?
    private final RVItemDeviceOnClickListener onClickListener;

    private final RVItemDeviceOnLongClickListener longClickListener;

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
        return new BTDevicesRecyclerViewAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener
    {
        private final TextView deviceAddress;
        private final TextView deviceName;
        private final TextView deviceSignal;

        public ViewHolder(View view) {
            super(view);

            // Define click listener for the ViewHolder's View
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            this.deviceAddress = view.findViewById(R.id.device_mac_address);
            this.deviceName = view.findViewById(R.id.device_name);
            this.deviceSignal = view.findViewById(R.id.device_signal_level);
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
            return this.deviceAddress;
        }
        public TextView getDeviceName() {
            return this.deviceName;
        }

        public TextView getDeviceSignalLevel() {
            return this.deviceSignal;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (this.DeviceModelArrayList != null){
            // Get element from adapter's dataset at this position and replace the
            // contents of the view with that element

            DeviceModel deviceItem = DeviceModelArrayList.get(position);
            holder.getDeviceAddress().
                    setText(deviceItem.getBTDeviceAddress().toString());

            holder.getDeviceName().
                    setText(deviceItem.getBTDeviceName());

            String signalLevel = deviceItem.getSignalLevel() + " dBm";
            holder.getDeviceSignalLevel().setText(signalLevel);
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

