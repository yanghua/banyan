package com.freedom.messagebus.client.formatter;

import com.freedom.messagebus.client.model.MessageFormat;

/**
 * formatter factory
 */
public class FormatterFactory {

    /**
     * get a formatter with a given format
     *
     * @param format the message format
     * @return the specific message formatter
     */
    public static IFormatter getFormatter(MessageFormat format) {
        IFormatter instance = null;
        switch (format) {
            case Text:
                instance = new DefaultTextFormatter();
                break;

            case Object:
                instance = new ObjectFormatter();
                break;

            case Stream:
                instance = new StreamFormatter();
                break;

            //TODO
        }

        return instance;
    }

}
