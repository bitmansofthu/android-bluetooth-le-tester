package com.example.ble_test_v13_0;

// Expandable ListViewChild item's Request/Send button clicked
public interface LVChildItemConfirmCharacteristicOnClickListener {
    void onClick(int group_pos, int child_pos,
                 boolean read_access_checked,
                 boolean write_access_checked, String editableValue,
                 boolean notificationChecked);
}
