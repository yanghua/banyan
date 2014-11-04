package com.freedom.messagebus.common;

import java.text.NumberFormat;

public class NumberHelper {

    public static double fractionDigits(double original, int digitNum) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(digitNum);
        String tmp = format.format(original);
        tmp = tmp.replace(",", "");
        return Double.valueOf(tmp);
    }

}
