package com.freedom.messagebus.interactor.pubsub.impl.zookeeper;

import org.apache.zookeeper.Watcher;

public class ZKEventType {

    private int currentIndex;

    public ZKEventType(Watcher.Event.EventType eventType) {
        currentIndex = eventType.getIntValue();
    }

    public static enum EventType {
        None(0), NodeCreated(1), NodeDelete(2), NodeDataChanged(3), NodeChildrenChanged(4);

        private int idx;
        private static EventType[] eventTypes = new EventType[5];

        static {
            eventTypes[0] = None;
            eventTypes[1] = NodeCreated;
            eventTypes[2] = NodeDelete;
            eventTypes[3] = NodeDataChanged;
            eventTypes[4] = NodeChildrenChanged;
        }

        private EventType(int idx) {
            this.idx = idx;
        }

        public static EventType fromIndex(int idx) {
            if (idx < 0 || idx >= eventTypes.length)
                throw new IllegalArgumentException("illegal index : " + idx);

            return eventTypes[idx];
        }
    }

    public EventType get() {
        return EventType.fromIndex(this.currentIndex);
    }

}
