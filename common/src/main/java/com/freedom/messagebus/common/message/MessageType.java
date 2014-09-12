package com.freedom.messagebus.common.message;

public enum MessageType {

    AppMessage("appMessage", 0),
    AuthreqMessage("authreqMessage", 1),
    AuthrespMessage("authrespMessage", 2),
    LookupreqMessage("lookupMessage", 3),
    LookuprespMessage("lookuprespMessage", 4),
    CacheExpiredMessage("cacheExpiredMessage", 5);

    private String type;
    private int index;

    private MessageType(String type, int index) {
        this.type = type;
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

}
