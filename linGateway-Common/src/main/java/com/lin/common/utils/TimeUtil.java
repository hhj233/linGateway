package com.lin.common.utils;


import java.util.concurrent.TimeUnit;

/**
 * @author linzj
 */
public final class TimeUtil {
    private static volatile long currentTimeMills;

    static {
        currentTimeMills = System.currentTimeMillis();
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    currentTimeMills = System.currentTimeMillis();
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Throwable e){

                    }
                }
            }
        });
        daemon.setDaemon(true);
        daemon.setName("common-lin-gateway-tick-thread");
        daemon.start();
    }

    public static long getCurrentTimeMills() {
        return currentTimeMills;
    }

}
