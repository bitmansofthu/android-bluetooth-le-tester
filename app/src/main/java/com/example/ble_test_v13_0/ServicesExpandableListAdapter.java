package com.example.ble_test_v13_0;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ServicesExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ServiceModel> groupArrayList;
    private ArrayList<ArrayList<CharacteristicsModel>> childArrayList;

    public ServicesExpandableListAdapter(Context context, ArrayList<ServiceModel> groupArrayList,
                                         ArrayList<ArrayList<CharacteristicsModel>> childArrayList) {
        this.context = context;
        this.groupArrayList = groupArrayList;
        this.childArrayList = childArrayList;
    }
    @Override
    public int getGroupCount() {
        return this.groupArrayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this.childArrayList.get(groupPosition) != null){
            return this.childArrayList.get(groupPosition).size();
        }
        else {
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.groupArrayList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.childArrayList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false; //todo: true?
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ServiceModel service = (ServiceModel) getGroup(groupPosition);
        String service_name = service.getServiceName();
        String service_uuid = service.getServiceUUID();

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.services_list_group, null);
        }
        TextView ServiceNameTextListView = (TextView) convertView
                .findViewById(R.id.service_group_name);
        ServiceNameTextListView.setTypeface(null, Typeface.BOLD);
        ServiceNameTextListView.setText(service_name);
        TextView ServiceUuidListTextView = (TextView) convertView
                .findViewById(R.id.service_group_id);
        ServiceUuidListTextView.setTypeface(null, Typeface.BOLD);
        ServiceUuidListTextView.setText(service_uuid);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        CharacteristicsModel characteristic = (CharacteristicsModel)getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item_view, null);
        }
        TextView CharUuidExpandedView = (TextView) convertView
                .findViewById(R.id.expanded_characteristic_list_item);
        CharUuidExpandedView.setText(characteristic.getCharacteristicsUUID());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
