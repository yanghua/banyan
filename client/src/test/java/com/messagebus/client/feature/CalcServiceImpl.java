package com.messagebus.client.feature;

import org.apache.thrift.TException;

/**
 * Created by yanghua on 4/17/15.
 */
public class CalcServiceImpl implements CalcService.Iface {

    @Override
    public int calcSum() throws TException {
        int sum = 0;
        for (int i = 1; i <= 100; i++) {
            sum += i;
        }
        return sum;
    }
}
