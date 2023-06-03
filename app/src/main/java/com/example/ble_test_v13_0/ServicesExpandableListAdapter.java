package com.example.ble_test_v13_0;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ServicesExpandableListAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final ArrayList<ServiceModel> groupArrayList;
    private final ArrayList<ArrayList<CharacteristicsModel>> childArrayList;
    private final LVChildItemReadCharacteristicOnClickListener readCharacteristicOnClickListener;
    private final LvChildItemRbOnClickListener rwnRadioButtonOnClickListener;

    private final LvChildItemFormatSpinnerOnItemSelected formatSpinnerOnItemSelected;
    private String editableValue;

    private final String[] mSpinnerFormatItems =
            new String[]{"HEXA", "+-INT", "+INT", "ASCII", "FLOAT", "5", "6", "7", "8", "9"};

    // View lookup cache (view holders for parents and corresponding children for each parent)
    private static class ViewHolderParent {
        TextView ServiceNameTextListView;
        TextView ServiceUuidListTextView;
    }

    // expandable items (children)
    private static class ViewHolderChild {
        TextView CharUuidExpandedView; //todo: more descriptive naming
        TextView CharNameExpandedView;
        Button CharReadExpandedView;
        EditText CharValueExpandedView;
        RadioGroup radioGroupReadWriteNotify;
        RadioButton radioButtonReadAccess;
        RadioButton radioButtonWriteAccess;
        RadioButton radioButtonNotifyAccess;
        Spinner spinnerFormatSelection;
    }

    public ServicesExpandableListAdapter(Context context, ArrayList<ServiceModel> groupArrayList,
                                         ArrayList<ArrayList<CharacteristicsModel>> childArrayList,
                                         LVChildItemReadCharacteristicOnClickListener readCharacteristicOnClickListener,
                                         LvChildItemRbOnClickListener rwnRadioButtonOnClickListener,
                                         LvChildItemFormatSpinnerOnItemSelected formatSpinnerOnItemSelected) {
        this.context = context;
        this.groupArrayList = groupArrayList;
        this.childArrayList = childArrayList;
        this.readCharacteristicOnClickListener = readCharacteristicOnClickListener;
        this.rwnRadioButtonOnClickListener = rwnRadioButtonOnClickListener;
        this.formatSpinnerOnItemSelected = formatSpinnerOnItemSelected;
        editableValue = "";
    }
    @Override
    public int getGroupCount() {
        return this.groupArrayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this.childArrayList.get(groupPosition) != null){
            //System.out.println("getChildrenCount: " + this.childArrayList.get(groupPosition).size());
            return (this.childArrayList.get(groupPosition).size());
        }
        else {
            //System.out.println("getChildrenCount = 0"); //todo
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
        //System.out.println("getChildId = " + childPosition);
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ServiceModel service = (ServiceModel) getGroup(groupPosition);
        String service_name = service.getServiceName();
        String service_uuid = service.getServiceUUID();

        View rowView;
        ViewHolderParent viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new ViewHolderParent();

            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE); //todo: -> constructor

            rowView = layoutInflater.inflate(R.layout.list_service_item_view, null);

            viewHolder.ServiceNameTextListView = rowView.
                                                    findViewById(R.id.service_group_name);

            viewHolder.ServiceUuidListTextView = rowView.
                                                    findViewById(R.id.service_group_id);

            rowView.setTag(viewHolder); // Cache the viewHolder object inside the fresh view
        }
        else {
            rowView = convertView;
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolderParent) rowView.getTag();
        }

        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolder.ServiceNameTextListView.setText(service_name);
        viewHolder.ServiceUuidListTextView.setText(service_uuid);

        return rowView;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {

        CharacteristicsModel characteristic =
                (CharacteristicsModel)getChild(groupPosition, childPosition);

        View rowView;
        ViewHolderChild viewHolderChild;

        if (convertView == null) {
            viewHolderChild = new ViewHolderChild();

            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = layoutInflater.inflate(R.layout.list_characteristic_item_view, null);

            viewHolderChild.CharUuidExpandedView = rowView.
                    findViewById(R.id.expanded_characteristic_uuid);

            viewHolderChild.CharNameExpandedView = rowView.
                    findViewById(R.id.expanded_characteristic_name);

            viewHolderChild.radioGroupReadWriteNotify = rowView.
                    findViewById(R.id.radioGroupReadWriteNotify);

            viewHolderChild.radioButtonReadAccess = rowView.findViewById(R.id.radioButtonRead);
            viewHolderChild.radioButtonWriteAccess = rowView.findViewById(R.id.radioButtonWrite);
            viewHolderChild.radioButtonNotifyAccess = rowView.findViewById(R.id.radioButtonNotification);

            viewHolderChild.spinnerFormatSelection = rowView.
                    findViewById(R.id.spinnerDropDownValueFormat);

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this.context,
                    android.R.layout.simple_list_item_1, mSpinnerFormatItems);
            viewHolderChild.spinnerFormatSelection.setAdapter(spinnerAdapter);

            viewHolderChild.CharReadExpandedView = rowView.
                    findViewById(R.id.read_characteristics_value);

            viewHolderChild.CharValueExpandedView = rowView.
                    findViewById(R.id.characteristic_value);

            rowView.setTag(viewHolderChild); // Cache the viewHolder object inside the fresh view

            //System.out.println("getChildView, convertView == NULL:" +
            //        groupPosition + " / " + childPosition + " / " + isLastChild);
        }
        else {
            //System.out.println("getChildView, convertView !== NULL:" +
//                    groupPosition + " / " + childPosition + " / " + isLastChild);
            rowView = convertView;
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolderChild = (ViewHolderChild) rowView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.

        if (!characteristic.getReadAccess()){
            viewHolderChild.radioButtonReadAccess.setVisibility(View.INVISIBLE);
        }
        else{ viewHolderChild.radioButtonReadAccess.setVisibility(View.VISIBLE);}

        if (!characteristic.getWriteAccess()){
            viewHolderChild.radioButtonWriteAccess.setVisibility(View.INVISIBLE);
        }
        else{ viewHolderChild.radioButtonWriteAccess.setVisibility(View.VISIBLE);}

        if (!characteristic.getNotificationAccess()){
            viewHolderChild.radioButtonNotifyAccess.setVisibility(View.INVISIBLE);
        }
        else{ viewHolderChild.radioButtonNotifyAccess.setVisibility(View.VISIBLE);}

        viewHolderChild.CharReadExpandedView.setOnClickListener(
                v -> readCharacteristicOnClickListener.onClick(groupPosition,
                        childPosition,
                        characteristic.getReadChecked(),
                        characteristic.getWriteChecked(),
                        editableValue,
                        characteristic.getNotificationChecked()
                ));

        viewHolderChild.radioButtonReadAccess.setOnClickListener(v ->
                rwnRadioButtonOnClickListener.onClick(groupPosition, childPosition,
                true, false, false));

        viewHolderChild.radioButtonWriteAccess.setOnClickListener(v ->
                rwnRadioButtonOnClickListener.onClick(groupPosition, childPosition,
                false, true, false));

        viewHolderChild.radioButtonNotifyAccess.setOnClickListener(v ->
                rwnRadioButtonOnClickListener.onClick(groupPosition, childPosition,
                false, false, true));

        viewHolderChild.spinnerFormatSelection.
            setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // view: view within the spinner-adapter
                // position: the position of the view in the adapter
                // id: the row id of the item that is selected
                // position and id seems to contain same value in our spinner-setup
                // -> use position as selected list index of the spinner drop-down list
                formatSpinnerOnItemSelected.onItemSelected(groupPosition, childPosition, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        viewHolderChild.CharValueExpandedView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                editableValue = String.valueOf(s);
            }
        });

        //todo: remove as useless?
        viewHolderChild.CharValueExpandedView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        //todo: remove as useless?
        //viewHolderChild.CharValueExpandedView.getKeyListener()
        viewHolderChild.CharValueExpandedView.setOnKeyListener((v, keyCode, event) -> {
            // onKey-event
            if (!characteristic.getWriteChecked()){
                return false;
            }

            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                   ( ((keyCode >= KeyEvent.KEYCODE_A) && (keyCode <= KeyEvent.KEYCODE_F)) ||
                    ((keyCode >= KeyEvent.KEYCODE_0) && (keyCode <= KeyEvent.KEYCODE_9)) )
            ) {
            }
            return false;
        });

        viewHolderChild.CharUuidExpandedView.setText(characteristic.getCharacteristicsUUID());

        viewHolderChild.CharNameExpandedView.setText(characteristic.getCharacteristicsName());

        viewHolderChild.CharValueExpandedView.setText(characteristic.getCharacteristicsValue());

        viewHolderChild.spinnerFormatSelection.setSelection(characteristic.getFormat());

        if (characteristic.getReadChecked())
        {
            if (!viewHolderChild.radioButtonReadAccess.isChecked()){
                viewHolderChild.radioButtonReadAccess.setChecked(true);
            }

            if (viewHolderChild.CharReadExpandedView.getVisibility() ==
                    View.INVISIBLE){
                viewHolderChild.CharReadExpandedView.setVisibility(View.VISIBLE);
            }

            viewHolderChild.CharReadExpandedView.setText
                    (R.string.characteristic_confirm_request);
        }

        if (characteristic.getWriteChecked())
        {
            if (!viewHolderChild.radioButtonWriteAccess.isChecked()){
                viewHolderChild.radioButtonWriteAccess.setChecked(true);
            }

            if (viewHolderChild.CharReadExpandedView.getVisibility() ==
                    View.INVISIBLE){
                viewHolderChild.CharReadExpandedView.setVisibility(View.VISIBLE);
            }
            viewHolderChild.CharReadExpandedView.setText
                    (R.string.characteristic_confirm_write);
        }

        if (characteristic.getNotificationChecked())
        {
            if (!viewHolderChild.radioButtonNotifyAccess.isChecked()){
                viewHolderChild.radioButtonNotifyAccess.setChecked(true);
            }

            if (viewHolderChild.CharReadExpandedView.getVisibility() == View.VISIBLE){
                viewHolderChild.CharReadExpandedView.setVisibility(View.INVISIBLE);
            }
        }

        return rowView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        //System.out.println("isChildSelectable, group/child: "
//                + groupPosition +"/" + childPosition);
        return true; // what about returning false????
    }

}
