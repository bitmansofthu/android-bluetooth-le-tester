package com.example.ble_test_v13_0;

public class CharacteristicsModel {
    private String characteristicsUUID;

    public CharacteristicsModel(String characteristicsUUID) {
        this.characteristicsUUID = characteristicsUUID;
    }

    public String getCharacteristicsUUID() {
        return this.characteristicsUUID;
    }
}
