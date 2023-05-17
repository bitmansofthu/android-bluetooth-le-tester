package com.example.ble_test_v13_0;

public class CharacteristicsModel {
    private final String characteristicsUUID;
    private final String characteristicsName;
    private String characteristicsValue;
    private final boolean radioGroup;
    private final boolean readAccess;
    private final boolean writeAccess;
    private final boolean notificationAccess;
    private final boolean acknowledge;

    public CharacteristicsModel(String characteristicsUUID, String characteristicsName, String characteristicsValue,
                                boolean radioGroup, boolean readAccess, boolean writeAccess, boolean notificationAccess,
                                boolean acknowledge) {
        this.characteristicsUUID = characteristicsUUID;
        this.characteristicsName = characteristicsName;
        this.characteristicsValue = characteristicsValue;

        // Radio group-parameter in such is useless here,
        // but important for giving correct amount of UI-components
        // (see ServicesExpandableListAdapter:getChildrenCount)
        this.radioGroup = radioGroup;

        this.readAccess = readAccess;
        this.writeAccess = writeAccess;
        this.notificationAccess = notificationAccess;

        // Acknowledge-parameter (for ACK-button) in such is useless here,
        // but important for giving correct amount of UI-components
        // (see ServicesExpandableListAdapter:getChildrenCount)
        this.acknowledge = acknowledge;
    }

    public String getCharacteristicsUUID() {
        return this.characteristicsUUID;
    }
    public String getCharacteristicsName() {
        return this.characteristicsName;
    }
    public boolean getReadAccess() {
        return this.readAccess;
    }
    public boolean getWriteAccess() {
        return this.writeAccess;
    }
    public boolean getNotificationAccess() {
        return this.notificationAccess;
    }
    public String getCharacteristicsValue() {
        return this.characteristicsValue;
    }
    public void setCharacteristicsValue(String new_value) {
        this.characteristicsValue = new_value;
    }

}
