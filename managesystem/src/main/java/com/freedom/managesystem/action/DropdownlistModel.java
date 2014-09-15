package com.freedom.managesystem.action;

public class DropdownlistModel {

    private String Value;
    private String DisplayText;

    public DropdownlistModel() {
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public String getDisplayText() {
        return DisplayText;
    }

    public void setDisplayText(String displayText) {
        DisplayText = displayText;
    }

    @Override
    public String toString() {
        return "DropdownlistModel{" +
            "Value='" + Value + '\'' +
            ", DisplayText='" + DisplayText + '\'' +
            '}';
    }
}
