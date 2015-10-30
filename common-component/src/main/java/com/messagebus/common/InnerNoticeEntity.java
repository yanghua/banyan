package com.messagebus.common;

import java.io.Serializable;

/**
 * Created by yanghua on 10/28/15.
 */
public class InnerNoticeEntity implements Serializable {

    private String identifier;
    private String value;
    private String type;

    public InnerNoticeEntity() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
