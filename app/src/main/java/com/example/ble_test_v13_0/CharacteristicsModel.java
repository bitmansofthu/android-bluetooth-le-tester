package com.example.ble_test_v13_0;

public class CharacteristicsModel {
    private final String characteristicsUUID;
    private final String characteristicsName;
    private String characteristicsValue;

    public CharacteristicsModel(String characteristicsUUID, String characteristicsName, String characteristicsValue) {
        this.characteristicsUUID = characteristicsUUID;
        this.characteristicsName = characteristicsName;
        this.characteristicsValue = characteristicsValue;
    }

    public String getCharacteristicsUUID() {
        return this.characteristicsUUID;
    }
    public String getCharacteristicsName() {
        return this.characteristicsName;
    }
    public String getCharacteristicsValue() {
        return this.characteristicsValue;
    }
    public void setCharacteristicsValue(String new_value) {
        this.characteristicsValue = new_value;
    }

}
