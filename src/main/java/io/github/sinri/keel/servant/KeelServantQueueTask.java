package io.github.sinri.keel.servant;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public abstract class KeelServantQueueTask {
    public final String EPITAPH_DONE = "DONE";
    public final String EPITAPH_ERROR = "ERROR";

    abstract public String getTaskReference();

    public Future<Void> lockTask() {
        return Future.succeededFuture();
    }

    abstract public Future<String> execute();

    public KeelLogger getLogger() {
        return new KeelLogger();
    }

    public final Future<Void> finalExecute() {
        return lockTask()
                .onFailure(throwable -> {
                    getLogger().error(getClass() + " [" + getTaskReference() + "] LOCK FAILED: " + throwable.getMessage());
                    getLogger().exception(throwable);
                })
                .compose(locked -> execute())
                .compose(feedback -> markTaskAsCompleted(this.EPITAPH_DONE, feedback))
                .recover(throwable -> {
                    getLogger().error(getClass() + " [" + getTaskReference() + "] EXECUTE FAILED: " + throwable.getMessage());
                    getLogger().exception(throwable);
                    return markTaskAsCompleted(this.EPITAPH_ERROR, throwable.getMessage());
                });
    }

    public Future<Void> markTaskAsCompleted(String epitaph, String feedback) {
        getLogger().notice(getClass() + "[" + getTaskReference() + "] mark as " + epitaph, new JsonObject().put("feedback", feedback));
        return Future.succeededFuture();
    }
}
