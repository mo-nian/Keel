package io.github.sinri.keel.tesuto;

import io.vertx.core.Future;

import java.lang.reflect.Method;

/**
 * @since 3.0.10
 */
class TestUnitWrapper {
    private final Method method;
    private final TestUnitResult testUnitResult;


    public TestUnitWrapper(Method method) {
        this.method = method;
        this.testUnitResult = new TestUnitResult(method.getName());
    }

    public Future<TestUnitResult> runTest(KeelTest testInstance) {
        long startTime = System.currentTimeMillis();

        return Future.succeededFuture()
                .compose(vv -> {
                    try {
                        return (Future<?>) this.method.invoke(testInstance);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                })
                .compose(passed -> {
                    long endTime = System.currentTimeMillis();
                    this.testUnitResult.setSpentTime(endTime - startTime).declareDone();
                    return Future.succeededFuture();
                }, throwable -> {
                    long endTime = System.currentTimeMillis();
                    this.testUnitResult.setSpentTime(endTime - startTime).declareFailed(throwable);
                    return Future.succeededFuture();
                })
                .compose(v -> {
                    return Future.succeededFuture(testUnitResult);
                });
    }

    public String getName() {
        return this.method.getName();
    }
}
