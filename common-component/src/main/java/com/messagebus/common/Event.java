package com.messagebus.common;

import java.io.Serializable;

/**
 * Created by yanghua on 6/24/15.
 */
public class Event implements Serializable {

    private String description;

    public Event() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Event{" +
            "description='" + description + '\'' +
            '}';
    }
}
