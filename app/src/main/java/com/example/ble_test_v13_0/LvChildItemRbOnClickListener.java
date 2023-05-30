package com.example.ble_test_v13_0;

// OnClick-Interface for handling presses of Radio buttons of Expandable ListView
public interface LvChildItemRbOnClickListener {
    void onClick(int group_pos, int child_pos,
                 boolean read_access_checked,
                 boolean write_access_checked,
                 boolean notificationChecked);
}
