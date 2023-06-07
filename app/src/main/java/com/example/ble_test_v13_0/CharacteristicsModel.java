package com.example.ble_test_v13_0;

import java.util.BitSet;

public class CharacteristicsModel {
    private final String characteristicsUUID;
    private final String characteristicsName;
    private byte[] characteristicsValue;

    // Radio group-parameter in such is useless here,
    // but important for giving correct amount of UI-components
    // (see ServicesExpandableListAdapter:getChildrenCount)
    private final boolean radioButtonGroup; //read/write/notification bound inside radio group

    // bit set:
    //  bit 0 = 1: radio-button visible
    //  bit 1 = 1: radio-button checked
    private final BitSet readAccess; // state of radioButton-read
    private final BitSet writeAccess; // state of radioButton-write
    private final BitSet notificationAccess; // state of radioButton-notify

    private int format; // selected format (unsigned integer8, string etc.)

    // confirmAction-parameter (for Confirmation-button for Read-request/Send) in such is useless here,
    // but important for giving correct amount of UI-components
    // (see ServicesExpandableListAdapter:getChildrenCount)
    private final boolean confirmAction;

    public CharacteristicsModel(String characteristicsUUID,
                                String characteristicsName,
                                byte[] characteristicsValue,
                                boolean radioGroup,
                                boolean readAccess,
                                boolean writeAccess,
                                boolean notificationAccess,
                                int format,
                                boolean confirmAction) {

        this.characteristicsUUID = characteristicsUUID;
        this.characteristicsName = characteristicsName;
        this.characteristicsValue = characteristicsValue;

        this.radioButtonGroup = radioGroup;

        this.format = format;

        this.readAccess = new BitSet(2);
        // disable/enable read-access -> read-radio button invisible/visible
        this.readAccess.set(0, readAccess);
        // set read-radio button initially checked according to read-access
        this.readAccess.set(1, readAccess);

        this.writeAccess = new BitSet(2);
        // disable/enable write-access -> write-radio button invisible/visible
        this.writeAccess.set(0, writeAccess);
        // set write-radio button initially unchecked
        this.writeAccess.set(1, false);

        this.notificationAccess = new BitSet(2);
        // disable/enable notify-access -> notify-radio button invisible/visible
        this.notificationAccess.set(0, notificationAccess);
        // set notify-radio button initially unchecked
        this.notificationAccess.set(1, false);

        this.confirmAction = confirmAction;
    }

    public String getCharacteristicsUUID() {
        return this.characteristicsUUID;
    }
    public String getCharacteristicsName() {
        return this.characteristicsName;
    }
    public boolean getReadAccess() {
        return this.readAccess.get(0);
    }
    public boolean getReadChecked() {
        return this.readAccess.get(1);
    }
    public void setReadChecked(boolean enable) {
        this.readAccess.set(1, enable);
    }

    public boolean getWriteAccess() {
        return this.writeAccess.get(0);
    }
    public boolean getWriteChecked() {
        return this.writeAccess.get(1);
    }
    public void setWriteChecked(boolean enable) {
        this.writeAccess.set(1, enable);
    }

    public boolean getNotificationAccess() {
        return this.notificationAccess.get(0);
    }
    public boolean getNotificationChecked() {
        return this.notificationAccess.get(1);
    }
    public void setNotificationChecked(boolean enable) {
        this.notificationAccess.set(1, enable);
    }

    public int getFormat() {
        return this.format;
    }
    public void setFormat(int newFormat) { this.format = newFormat; }

    public byte[] getCharacteristicsValue() {
        return this.characteristicsValue;
    }
    public void setCharacteristicsValue(byte[] newValue) {
        this.characteristicsValue = newValue;
    }

}
