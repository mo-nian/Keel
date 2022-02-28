package io.github.sinri.keel.test.logger;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestSuite;



public class KeelLoggerTest {

    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions().setWorkerPoolSize(2));
        Keel.loadPropertiesFromFile("test.sample.properties");

        TestSuite suite = TestSuite.create("KeelLoggerTestSuite");
        suite.test("stdout", context -> {
                    KeelLogger logger = new KeelLogger();
                    logger.debug("debug");
                    logger.info("info");
                    logger.notice("notice");
                    logger.warning("warning");
                    logger.error("error");
                    logger.fatal("fatal");
                })
                .test("stdout-with-aspect", testContext -> {
                    KeelLogger logger = new KeelLogger(new KeelLoggerOptions().setAspect("aspect"));
                    logger.debug("debug");
                    logger.info("info");
                    logger.notice("notice");
                    logger.warning("warning");
                    logger.error("error");
                    logger.fatal("fatal");
                })
//                .test("file_for_second", testContext -> {
//                    KeelLogger logger = new KeelLogger(new File("./log"),"rotate_by_second");
//                    logger.setLowestLevel(KeelLogLevel.NOTICE).setRotateDateTimeFormat("yyyyMMddHHmmss");
//                    for(int i=0;i<3;i++) {
//                        logger.debug("debug");
//                        logger.info("info");
//                        logger.notice("notice");
//                        logger.warning("warning");
//                        logger.error("error");
//                        logger.fatal("fatal");
//                        try {
//                            Thread.sleep(2000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                })
                .test("with-properties", testContext -> {
                    KeelLogger logger = Keel.outputLogger("x");
                    //testContext.assertEquals(logger.getRotateDateTimeFormat(), "yyyyMMddHH");
                    logger.debug("debug");
                    logger.info("info");
                    logger.notice("notice");
                    logger.warning("warning");
                    logger.error("error");
                    logger.fatal("fatal");
                })
                .test("check-properties", testContext -> {
                    KeelLogger logger = Keel.outputLogger("x");

                    JsonObject jsonObject = Keel.getPropertiesReader().toJsonObject();
                    logger.notice("properties to json", jsonObject);
                });
        suite.run();


    }

    public static void test1() {

    }
}
