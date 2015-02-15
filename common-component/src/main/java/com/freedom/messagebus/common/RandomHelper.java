package com.freedom.messagebus.common;

import java.util.Random;

public class RandomHelper {

    /**
     * generate random character and number mixed sequence with a given <p>length</p>
     *
     * @param length the sequence's length that will be generated
     * @return generated sequence
     */

    public static String randomNumberAndCharacter(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        Random randdata = new Random();
        int data = 0;

        for (int i = 0; i < length; i++) {
            int index = rand.nextInt(3);
            switch (index) {
                case 0:
                    data = randdata.nextInt(10);
                    sb.append(data);
                    break;
                case 1:
                    data = randdata.nextInt(26) + 65;
                    sb.append((char) data);
                    break;
                case 2:
                    data = randdata.nextInt(26) + 97;
                    sb.append((char) data);
                    break;

                default:
                    break;
            }
        }

        return sb.toString();
    }

}
