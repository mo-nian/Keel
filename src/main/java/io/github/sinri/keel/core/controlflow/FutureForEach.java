package io.github.sinri.keel.core.controlflow;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * 给定一个可迭代对象，迭代其元素，依次针对每个元素运行异步代码。
 *
 * @param <T> Type of elements in the source collection
 * @since 1.13
 */
public class FutureForEach<T> {
    private final Function<T, Future<Void>> asyncItemProcessFunction;

    private FutureForEach(Function<T, Future<Void>> itemProcessor) {
        this.asyncItemProcessFunction = itemProcessor;
    }

    public static <T> Future<Void> call(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        return new FutureForEach<T>(itemProcessor).process(collection);
    }

    private Future<Void> process(Iterable<T> collection) {
        Iterator<T> iterator = collection.iterator();
        return Keel.callFutureRepeat(routineResult -> {
            if (iterator.hasNext()) {
                T next = iterator.next();
                return asyncItemProcessFunction.apply(next);
            } else {
                routineResult.stop();
                return Future.succeededFuture();
            }
        });

//        return FutureUntil.call(() -> Future.succeededFuture()
//                .compose(v -> {
//                    if (iterator.hasNext()) {
//                        T next = iterator.next();
//                        return asyncItemProcessFunction.apply(next);
//                    } else {
//                        return Future.succeededFuture();
//                    }
//                })
//                .compose(v -> {
//                    return Future.succeededFuture(!iterator.hasNext());
//                }));
    }

    @Deprecated
    private Future<Void> processWithFutureRecursion(Iterable<T> collection) {
        AtomicReference<Future<Void>> futureAtomicReference = new AtomicReference<>();
        futureAtomicReference.set(Future.succeededFuture());
        collection.forEach(t -> {
            var future = futureAtomicReference.get()
                    .compose(previousK -> {
                        try {
                            return asyncItemProcessFunction.apply(t);
                        } catch (Throwable throwable) {
                            return Future.failedFuture(throwable);
                        }
                    });
            futureAtomicReference.set(future);
        });
        return futureAtomicReference.get();
    }
}
