package io.github.sinri.keel.facade.async;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.function.Function;

/**
 * 给定一个可迭代对象，迭代其元素，依次针对每个元素运行异步代码。
 *
 * @param <T> Type of elements in the source collection
 * @since 1.13
 */
public class FutureForEach<T> {
    private final Function<T, Future<Void>> asyncItemProcessFunction;

    private FutureForEach(@Nonnull Function<T, Future<Void>> itemProcessor) {
        this.asyncItemProcessFunction = itemProcessor;
    }

    static <T> Future<Void> call(@Nonnull Iterable<T> collection, @Nonnull Function<T, Future<Void>> itemProcessor) {
        return new FutureForEach<>(itemProcessor).process(collection);
    }

    private Future<Void> process(@Nonnull Iterable<T> collection) {
        Iterator<T> iterator = collection.iterator();
        return KeelAsyncKit.repeatedlyCall(routineResult -> {
            if (iterator.hasNext()) {
                T next = iterator.next();
                return asyncItemProcessFunction.apply(next);
            } else {
                routineResult.stop();
                return Future.succeededFuture();
            }
        });
    }
}
