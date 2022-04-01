package io.github.sinri.keel.mysql;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.github.sinri.keel.mysql.matrix.ResultMatrixWithVertx;
import io.github.sinri.keel.mysql.statement.SelectStatement;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class KeelMySQLKit {
    private final MySQLPool pool;
    private final KeelMySQLOptions options;

    public KeelMySQLKit(Vertx vertx, KeelMySQLOptions options) {
        this.options = options;
        Keel.outputLogger(getClass().getName()).info("useAffectedRows: " + options.useAffectedRows);
        pool = MySQLPool.pool(
                vertx,
                options.buildMySQLConnectOptions(),
                options.buildPoolOptions()
        );
    }

    public KeelMySQLOptions getOptions() {
        return options;
    }

    protected static String makePlaceholderString(int x) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < x; i++) {
            if (i > 0) result.append(",");
            result.append("?");
        }
        return result.toString();
    }

    protected static String makeStandardWidthField(int x, int w) {
        StringBuilder s = new StringBuilder("" + x);
        if (s.length() < w) {
            for (int i = 0; i < w - s.length(); i++) {
                s.insert(0, "0");
            }
        }
        return String.valueOf(s);
    }

    public static String toMySQLDatetime(LocalDateTime datetime) {
        return makeStandardWidthField(datetime.getYear(), 4)
                + "-" + makeStandardWidthField(datetime.getMonthValue(), 2)
                + "-" + makeStandardWidthField(datetime.getDayOfMonth(), 2)
                + " "
                + makeStandardWidthField(datetime.getHour(), 2)
                + ":" + makeStandardWidthField(datetime.getMinute(), 2)
                + ":" + makeStandardWidthField(datetime.getSecond(), 2);
    }

    /**
     * @return Y-m-d H:i:s
     * @since 1.7
     */
    public static String nowAsMySQLDatetime() {
        return toMySQLDatetime(LocalDateTime.now());
    }

    public MySQLPool getPool() {
        return pool;
    }

    /**
     * @param sqlConnection
     * @param sqlTemplate
     * @param batch
     * @param useRecover
     * @return
     * @since 1.1
     */
    public static Future<Long> executeSqlForLastInsertedID(
            SqlConnection sqlConnection,
            String sqlTemplate,
            List<Tuple> batch,
            boolean useRecover
    ) {
        Future<Long> future = sqlConnection.preparedQuery(sqlTemplate).executeBatch(batch).compose(rows -> {
            long lastInsertId = rows.property(MySQLClient.LAST_INSERTED_ID);
            // here the lastInsertId would be the first id batch inserted.
            // if not inserted, would not come here but as failed
            return Future.succeededFuture(lastInsertId);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(-1L));
        }
        return future;
    }

    /**
     * @param sqlConnection
     * @param sqlTemplate
     * @param data
     * @param useRecover
     * @return
     * @since 1.1
     */
    public static Future<Long> executeSqlForLastInsertedID(
            SqlConnection sqlConnection,
            String sqlTemplate,
            Tuple data,
            boolean useRecover
    ) {
        Future<Long> future = sqlConnection.preparedQuery(sqlTemplate).execute(data).compose(rows -> {
            long lastInsertId = rows.property(MySQLClient.LAST_INSERTED_ID);
            // here the lastInsertId would be the first id batch inserted.
            // if not inserted, would not come here but as failed
            return Future.succeededFuture(lastInsertId);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(-1L));
        }
        return future;
    }

    /**
     * @param sqlConnection
     * @param sqlTemplate
     * @param data
     * @param useRecover
     * @return
     * @since 1.1
     */
    public static Future<Integer> executeSqlForAffectedRowCount(
            SqlConnection sqlConnection,
            String sqlTemplate,
            Tuple data,
            boolean useRecover
    ) {
        Future<Integer> future = sqlConnection.preparedQuery(sqlTemplate).execute(data).compose(rows -> {
            // conditions matched zero rows: afx = 0
            // conditions matched rows and modified them all: afx = all rows
            // if `useAffectedRows` is set to `true`,
            //   conditions matched rows but not modified any: afx = 0
            //   conditions matched rows but modified partly: afx = exact partly rows
            // otherwise,
            //   conditions matched rows but not modified any: afx = all rows
            //   conditions matched rows but modified partly: afx = all rows
            int afx = rows.rowCount();
            return Future.succeededFuture(afx);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(-1));
        }
        return future;
    }

    /**
     * @param sqlConnection
     * @param sqlTemplate
     * @param useRecover
     * @return
     * @since 1.1
     */
    public static Future<Integer> executeSqlForAffectedRowCount(
            SqlConnection sqlConnection,
            String sqlTemplate,
            boolean useRecover
    ) {
        Future<Integer> future = sqlConnection.preparedQuery(sqlTemplate).execute().compose(rows -> {
            // conditions matched zero rows: afx = 0
            // conditions matched rows and modified them all: afx = all rows
            // if `useAffectedRows` is set to `true`,
            //   conditions matched rows but not modified any: afx = 0
            //   conditions matched rows but modified partly: afx = exact partly rows
            // otherwise,
            //   conditions matched rows but not modified any: afx = all rows
            //   conditions matched rows but modified partly: afx = all rows
            int afx = rows.rowCount();
            return Future.succeededFuture(afx);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(-1));
        }
        return future;
    }

    /**
     * @param sqlConnection
     * @param sqlTemplate
     * @param data
     * @param useRecover
     * @return
     * @since 1.1
     */
    public static Future<ResultMatrix> executeSqlForResultMatrix(
            SqlConnection sqlConnection,
            String sqlTemplate,
            Tuple data,
            boolean useRecover
    ) {
        Future<ResultMatrix> future = sqlConnection.preparedQuery(sqlTemplate).execute(data).compose(rows -> {
            ResultMatrix resultMatrix = new ResultMatrixWithVertx(rows);
            return Future.succeededFuture(resultMatrix);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(null));
        }
        return future;
    }

    /**
     * @param sqlConnection
     * @param sqlTemplate
     * @param useRecover
     * @return
     * @since 1.1
     */
    public static Future<ResultMatrix> executeSqlForResultMatrix(
            SqlConnection sqlConnection,
            String sqlTemplate,
            boolean useRecover
    ) {
        Future<ResultMatrix> future = sqlConnection.preparedQuery(sqlTemplate).execute().compose(rows -> {
            ResultMatrix resultMatrix = new ResultMatrixWithVertx(rows);
            return Future.succeededFuture(resultMatrix);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(null));
        }
        return future;
    }

    /**
     * @param transactionBody the function with sql connection for future
     * @param <T>             the final result class/type
     * @return future with final result if committed, or failed future if rollback
     * @since 1.10
     */
    public <T> Future<T> executeInTransaction(Function<SqlConnection, Future<T>> transactionBody) {
        AtomicReference<T> finalResult = new AtomicReference<>();
        AtomicReference<Throwable> cause = new AtomicReference<>();
        return getPool()
                .withTransaction(transactionBody)
                .onSuccess(finalResult::set)
                .onFailure(cause::set)
                .eventually(v -> {
                    if (cause.get() == null) {
                        return Future.succeededFuture(finalResult.get());
                    }
                    return Future.failedFuture(cause.get());
                });
    }

    /**
     * @param selection the SELECT STATEMENT BUILDER
     * @return the future for ResultMatrix, nullable
     * @since 1.4
     */
    public Future<ResultMatrix> queryInConnection(SelectStatement selection) {
        return getPool().withConnection(
                sqlConnection -> KeelMySQLKit.executeSqlForResultMatrix(
                        sqlConnection,
                        selection.toString(),
                        false
                )
        );
    }

    private static final String KEY_MYSQL_CONNECTION_UUID = "MySQLConnectionUUID";
    private static final String KEY_MYSQL_CONNECTION = "MySQLConnection";

    /**
     * It should only be used in Worker Verticle and target prepared.
     *
     * @return SqlConnection prepared in context with KEY_MYSQL_CONNECTION
     */
    public static SqlConnection getSqlConnectionFromVerticleContext() {
        return Keel.getVertx().getOrCreateContext().get(KEY_MYSQL_CONNECTION);
    }

    public static String getSqlConnectionUUIDFromVerticleContext() {
        return Keel.getVertx().getOrCreateContext().get(KEY_MYSQL_CONNECTION_UUID);
    }

    public static void setSqlConnectionToVerticleContext(SqlConnection sqlConnection) {
        Keel.getVertx().getOrCreateContext().put(KEY_MYSQL_CONNECTION, sqlConnection);
        Keel.getVertx().getOrCreateContext().put(KEY_MYSQL_CONNECTION_UUID, UUID.randomUUID().toString());
    }
}
