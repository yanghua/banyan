package com.freedom.messagebus.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc: the definition of exchange type
 * more detail see : http://lostechies.com/derekgreer/2012/03/28/rabbitmq-for-windows-exchange-types/
 * User: yanghua
 * Date: 6/29/14
 * Time: 8:53 AM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public enum RouterType {

    DIRECT,
    FANOUT,
    TOPIC,
    HEADERS;

    private static final Map<String, RouterType> lookups = new HashMap<String, RouterType>(4);

    static {
        lookups.put("direct", DIRECT);
        lookups.put("fanout", FANOUT);
        lookups.put("topic", TOPIC);
        lookups.put("headers", HEADERS);
    }


    public static String fromEnum(RouterType item) {
        for (Map.Entry<String, RouterType> entry : lookups.entrySet()) {
            if (entry.getValue().equals(item))
                return entry.getKey();
        }

        throw new IllegalArgumentException("illegal param : " + item.toString());
    }

    public static RouterType lookup(String produceModeStr) {
        for (Map.Entry<String, RouterType> entry : lookups.entrySet()) {
            if (entry.getKey().equals(produceModeStr))
                return entry.getValue();
        }

        throw new IllegalArgumentException("illegal param : " + produceModeStr +
                                               " . can not fetch a enum item .");
    }

    @Override
    public String toString() {
        for (Map.Entry<String, RouterType> item : lookups.entrySet()) {
            if (this.equals(item.getValue())) {
                return item.getKey();
            }
        }

        throw new UnknownError("unknown Enum item ");
    }
}
