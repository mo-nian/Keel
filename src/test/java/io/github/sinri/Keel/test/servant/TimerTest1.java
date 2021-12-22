package io.github.sinri.Keel.test.servant;

import io.github.sinri.Keel.Keel;
import io.github.sinri.Keel.servant.KeelServantTimer;
import io.github.sinri.Keel.servant.KeelServantTimerWorker;
import io.vertx.core.VertxOptions;

import java.util.Calendar;

public class TimerTest1 {
    public static void main(String[] args) throws InterruptedException {
        Keel.initializeVertx(new VertxOptions().setWorkerPoolSize(2));
        Keel.loadPropertiesFromFile("test.properties");

        KeelServantTimer keelServantTimer = new KeelServantTimer(Keel.getVertx());

        keelServantTimer.registerWorker("evert2min", new KeelServantTimerWorker("*/2 * * * *") {

            @Override
            protected void work(Calendar calendar) {
                System.out.println("evert2min triggered by " + calendar.getTime() + ", now START on " + Calendar.getInstance().getTime());

                Keel.getVertx().executeBlocking(x -> {
                    try {
                        Thread.sleep(1000 * 60 * 3);
                        x.complete();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        x.fail(e);
                    }
                }).onComplete(ar -> {
                    System.out.println("evert2min triggered by " + calendar.getTime() + ", now END on " + Calendar.getInstance().getTime() + " ar:" + ar);
                });
            }
        });

        Thread.sleep(1000 * 60 * 5);
        keelServantTimer.stop();
        Thread.sleep(1000 * 60 * 5);
        Keel.getVertx().close();
    }
}
