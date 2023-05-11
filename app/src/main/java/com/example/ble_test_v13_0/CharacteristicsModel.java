package com.example.ble_test_v13_0;

public class CharacteristicsModel {
    private final String characteristicsUUID;
    private String characteristicsValue;

    public CharacteristicsModel(String characteristicsUUID, String characteristicsValue) {
        this.characteristicsUUID = characteristicsUUID;
        this.characteristicsValue = characteristicsValue;
    }

    public String getCharacteristicsUUID() {
        return this.characteristicsUUID;
    }
    public String getCharacteristicsValue() {
        return this.characteristicsValue;
    }
    public void setCharacteristicsValue(String new_value) {
        this.characteristicsValue = new_value;
    }

}
