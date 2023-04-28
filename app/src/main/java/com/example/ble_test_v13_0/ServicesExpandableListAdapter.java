package com.example.ble_test_v13_0;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ServicesExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ServiceModel> groupArrayList;
    private ArrayList<ArrayList<CharacteristicsModel>> childArrayList;
    private LVChildItemReadCharacteristicOnClickListener readCharacteristicOnClickListener;

    // View lookup cache
    private static class ViewHolderParent {
        TextView ServiceNameTextListView;
        TextView ServiceUuidListTextView;
    }

    private static class ViewHolderChild {
        TextView CharUuidExpandedView;
        Button CharReadExpandedView;
        TextView CharValueExpandedView;
    }

    public ServicesExpandableListAdapter(Context context, ArrayList<ServiceModel> groupArrayList,
                                         ArrayList<ArrayList<CharacteristicsModel>> childArrayList,
                                         LVChildItemReadCharacteristicOnClickListener readCharacteristicOnClickListener) {
        this.context = context;
        this.groupArrayList = groupArrayList;
        this.childArrayList = childArrayList;
        this.readCharacteristicOnClickListener = readCharacteristicOnClickListener;
    }
    @Override
    public int getGroupCount() {
        return this.groupArrayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this.childArrayList.get(groupPosition) != null){
            return (this.childArrayList.get(groupPosition).size());
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
        return true; //todo: true?
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ServiceModel service = (ServiceModel) getGroup(groupPosition);
        String service_name = service.getServiceName();
        String service_uuid = service.getServiceUUID();

        ViewHolderParent viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new ViewHolderParent();

            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = layoutInflater.inflate(R.layout.list_service_item_view, null);

            viewHolder.ServiceNameTextListView = (TextView) convertView.
                                                    findViewById(R.id.service_group_name);
            viewHolder.ServiceNameTextListView.setTypeface(null, Typeface.BOLD);

            viewHolder.ServiceUuidListTextView = (TextView) convertView.
                                                    findViewById(R.id.service_group_id);
            viewHolder.ServiceUuidListTextView.setTypeface(null, Typeface.BOLD);

            convertView.setTag(viewHolder); // Cache the viewHolder object inside the fresh view
        }
        else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolderParent) convertView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolder.ServiceNameTextListView.setText(service_name);
        viewHolder.ServiceUuidListTextView.setText(service_uuid);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {

        // Notice: different implementation compared to getGroupView.
        // If resources (findViewById) were retrieved in 'convertView == null',
        // only visible child-items on the display would have been set (not invisible ones).
        // When invisible ones were scrolled to be visible, still no initialization was done...
        // So view-holders were not created for all child-items.
        // groupPosition and childPosition were not set correctly for all items...
        CharacteristicsModel characteristic =
                (CharacteristicsModel)getChild(groupPosition, childPosition);

        View rowView;

        ViewHolderChild viewHolderChild = new ViewHolderChild();

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = layoutInflater.inflate(R.layout.list_characteristic_item_view, null);
        }
        else {
            rowView = convertView;
            // View is being recycled, retrieve the viewHolder object from tag
            // viewHolderChild = (ViewHolderChild) rowView.getTag();
        }

        viewHolderChild.CharUuidExpandedView = (TextView) rowView.
                findViewById(R.id.expanded_characteristic_list_item);

        viewHolderChild.CharReadExpandedView = (Button) rowView.
                findViewById(R.id.read_characteristics_value);
        viewHolderChild.CharReadExpandedView.setOnClickListener(
                v -> {
                    readCharacteristicOnClickListener.onClick(groupPosition, childPosition);
                });

        viewHolderChild.CharValueExpandedView = (TextView) rowView.
                findViewById(R.id.characteristic_value);

        rowView.setTag(viewHolderChild); // Cache the viewHolder object inside the fresh view

        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolderChild.CharUuidExpandedView.setText(characteristic.getCharacteristicsUUID());

        viewHolderChild.CharValueExpandedView.setText(characteristic.getCharacteristicsValue());

        return rowView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
