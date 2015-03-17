package com.messagebus.client.handler.others;

/**
 * Created by yanghua on 3/4/15.
 */
public class RateLimitingPredicate {

    private static final long MIN_REGEN_TIME = 0;

    private double rate;
    private int    depth;
    private double bucketCount;
    private double regenTimeMS;
    private long   lastTimeMS;

    public RateLimitingPredicate(double rate, int depth) {
        this.rate = rate;
        this.regenTimeMS = (1.0 / rate) * 1.0e3;
        if (this.regenTimeMS < 1) this.regenTimeMS = 1;
        this.depth = depth;
        this.bucketCount = this.depth * 1.0;
        this.lastTimeMS = System.currentTimeMillis();
    }

    public boolean accept() {
        if (this.rate == -1.0) return true;

        long curTimeMS = System.currentTimeMillis();
        long delay = curTimeMS - lastTimeMS;

        if (delay >= MIN_REGEN_TIME) {
            double maybeComeTokenNum = ((double) delay * 1.0) / (regenTimeMS * 1.0);
            //recover bucket count
            bucketCount += maybeComeTokenNum;
            if (bucketCount > depth) bucketCount = depth;
            lastTimeMS = curTimeMS;
        }

        if (bucketCount >= 1.0) {
            bucketCount -= 1.0;
            return true;
        }

        return false;
    }


}
