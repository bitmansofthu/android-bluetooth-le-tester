package com.example.ble_test_v13_0;

//todo: rename to LVChildItemAckCharacteristicOnClickListener
// Expandable ListViewChild item clicked
public interface LVChildItemReadCharacteristicOnClickListener {
    void onClick(int group_pos, int child_pos,
                 boolean read_access_checked,
                 boolean write_access_checked, String editableValue,
                 boolean notificationChecked);
}
