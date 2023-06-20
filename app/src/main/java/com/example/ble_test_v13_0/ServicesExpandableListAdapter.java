package com.example.ble_test_v13_0;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class ServicesExpandableListAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final ArrayList<ServiceModel> groupArrayList;
    private final ArrayList<ArrayList<CharacteristicsModel>> childArrayList;
    private final LVChildItemReadCharacteristicOnClickListener readCharacteristicOnClickListener;
    private final LvChildItemRbOnClickListener rwnRadioButtonOnClickListener;

    private final LvChildItemFormatSpinnerOnItemSelected formatSpinnerOnItemSelected;
    private String editableValue;

    public final String[] mSpinnerFormatItems =
            new String[]{"HEX", "+INT", "+-INT", "ASCII", "FLOAT"};

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
        RadioButton radioButtonDisableAccesses;
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

            viewHolderChild.radioButtonDisableAccesses = rowView.findViewById(R.id.radioButtonOff);
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
        }
        else {
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

        viewHolderChild.radioButtonDisableAccesses.setOnClickListener(v ->
                rwnRadioButtonOnClickListener.onClick(groupPosition, childPosition,
                false, false, false));

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

        viewHolderChild.CharUuidExpandedView.setText(characteristic.getCharacteristicsUUID());

        viewHolderChild.CharNameExpandedView.setText(characteristic.getCharacteristicsName());

        byte[] valueInBinary = characteristic.getCharacteristicsValue();

        if ( (valueInBinary != null) && (valueInBinary.length > 0) ){
            int formatIndex = characteristic.getFormat();

            if (formatIndex < mSpinnerFormatItems.length){
                String valueAsciiFormat = formatConversion(valueInBinary,
                        mSpinnerFormatItems[formatIndex], characteristic.getCharacteristicsUUID());

                if (valueAsciiFormat != null) {
                    viewHolderChild.CharValueExpandedView.setText(valueAsciiFormat);
                } else {
                    viewHolderChild.CharValueExpandedView.
                            setText(R.string.empty_characteristic_value);
                }
            }
        }
        else{
            viewHolderChild.CharValueExpandedView.setText(R.string.empty_characteristic_value);
        }

        viewHolderChild.spinnerFormatSelection.setSelection(characteristic.getFormat());

        if (characteristic.getAccessesDisabled()){
            if (!viewHolderChild.radioButtonDisableAccesses.isChecked()){
                viewHolderChild.radioButtonDisableAccesses.setChecked(true);
            }
            if (viewHolderChild.CharReadExpandedView.getVisibility() ==
                    View.VISIBLE){
                viewHolderChild.CharReadExpandedView.setVisibility(View.INVISIBLE);
            }
        }

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
        return true; // what about returning false????
    }

    // Convert raw byte-buffer (valueInBinary) to the string according to
    // the requested format. Notice: Multi-octet fields within the GATT Profile
    // shall be sent least significant octet first (little endian).
    // So valueInBinary[0] is LSB.
    public String formatConversion(byte[] valueInBinary, String format, String uuid){
        String convertedValue;

        if (Objects.equals(format, "HEX")){
            // Hex decimal
            final StringBuilder valueHexDecimalInAsciiFormat =
                    new StringBuilder(valueInBinary.length);

            // LSB first -> MSB first (MSB is first, thus most left character in UI)
            for (int i = valueInBinary.length - 1; i >= 0 ; i--) {
                byte byteChar = valueInBinary[i];
                valueHexDecimalInAsciiFormat.append(String.format("%02X", byteChar));
            }

            convertedValue = valueHexDecimalInAsciiFormat.toString();
        }
        else if (Objects.equals(format, "+INT") ||
                Objects.equals(format, "+-INT") || Objects.equals(format, "FLOAT")){
            long signedLongIntValue = 0;

            int payloadLength = valueInBinary.length;
            int payloadStartIndex = 0; // initially start to encode from first byte
            int payloadStopIndex = valueInBinary.length - 1; // continue to end of the payload

            // todo: create function for solving some profile-specific payloads..
            if (Objects.equals(uuid,"00002a37-0000-1000-8000-00805f9b34fb") &&
                    valueInBinary.length > 1){ // heart rate
                payloadStartIndex = 1; // skip flags-field
                payloadLength--;
            }

            if (payloadLength > 8){
                // 64-bit integer (long) is the biggest supported integer type
                return null;
            }

            // Integers cannot be declared as unsigned in Java,
            // so store first the byte-buffer as signed long integer.
            // Take also care of 'LSB first'-rule, so for doing easier conversion,
            // start from MSB...
            for (int i = payloadStopIndex; i >= payloadStartIndex ; i--) {
                // Java does sign-extension (two's complement) automatically any time,
                // a byte (or short) is converted to int or long,
                // and the purpose of "& 0xFF" on a byte is to
                // UNDO the automatic sign extension.
                byte byteChar = valueInBinary[i];
                signedLongIntValue <<= 8;
                signedLongIntValue |= byteChar & 0xff;
            }

            if (format.equals("+INT")){
                // unsigned integer

                // There is no 'unsigned int'-variable available in java,
                // but unsigned integer can be presented as string using toUnsignedString();
                // and string is, what we need here...
                // Notice: there is no support for Byte.toUnsignedString or
                // Short.toUnsignedString. So lets convert value from long to integer
                // when not more than 4 bytes are available to be encoded.
                // Otherwise lets progress with long.
                int signedIntValue;

                if (payloadLength == 1){ // byte
                    signedIntValue = (int) (signedLongIntValue & 0x000000ff);
                    convertedValue = Integer.toUnsignedString(signedIntValue);
                }
                else if (payloadLength == 2){ //short
                    signedIntValue = (int) (signedLongIntValue & 0x0000ffff);
                    convertedValue = Integer.toUnsignedString(signedIntValue);
                }
                else if (payloadLength == 3){
                    signedIntValue = (int) (signedLongIntValue & 0x00ffffff);
                    convertedValue = Integer.toUnsignedString(signedIntValue);
                }
                else if (payloadLength == 4){ // int
                    signedIntValue = (int) (signedLongIntValue);
                    convertedValue = Integer.toUnsignedString(signedIntValue);
                }
                else{
                    convertedValue = Long.toUnsignedString(signedLongIntValue);
                }
            }
            else if (format.equals("+-INT")){
                if (payloadLength == 1){ // byte
                    byte signedByteValue = (byte) (signedLongIntValue);
                    convertedValue = Byte.toString(signedByteValue);
                }
                else if (payloadLength == 2){ //short
                    short signedShortValue = (short) (signedLongIntValue);
                    convertedValue = Short.toString(signedShortValue);
                }
                else if (payloadLength == 3){
                    int signedIntValue= (int) (signedLongIntValue);
                    convertedValue = Integer.toString(signedIntValue);
                }
                else if (payloadLength == 4){ // int
                    int signedIntValue = (int) (signedLongIntValue);
                    convertedValue = Integer.toString(signedIntValue);
                }
                else{
                    convertedValue = Long.toString(signedLongIntValue);
                }
            }
            else {
                // floating number
                if (payloadLength == 4){
                    float f = ByteBuffer.wrap(valueInBinary).
                                            order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    convertedValue = Float.toString(f);
                }
                else if (payloadLength == 8){
                    double doubleVal = ByteBuffer.wrap(valueInBinary).
                                                    order(ByteOrder.LITTLE_ENDIAN).getDouble();
                    convertedValue = Double.toString(doubleVal);
                }
                else {
                    convertedValue = null;
                }

            }
        }
        else if (Objects.equals(format, "ASCII")){
            // ASCII
            convertedValue = new String(valueInBinary, StandardCharsets.UTF_8);
        }
        else {
            convertedValue = null;
        }

        return  convertedValue;
    }
}
