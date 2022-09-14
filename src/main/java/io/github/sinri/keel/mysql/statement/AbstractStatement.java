package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

import java.util.UUID;

/**
 * @since 1.7
 */
abstract public class AbstractStatement {
    protected static KeelLogger sqlAuditLogger = KeelLogger.silentLogger();
    protected static String SQL_COMPONENT_SEPARATOR = " ";//"\n";
    protected final String statement_uuid;
    private String remarkAsComment = "";

    public AbstractStatement() {
        this.statement_uuid = UUID.randomUUID().toString();
    }

    public static KeelLogger getSqlAuditLogger() {
        return sqlAuditLogger;
    }

    public static void setSqlAuditLogger(KeelLogger sqlAuditLogger) {
        AbstractStatement.sqlAuditLogger = sqlAuditLogger;
    }

    public static void setSqlComponentSeparator(String sqlComponentSeparator) {
        SQL_COMPONENT_SEPARATOR = sqlComponentSeparator;
    }

    /**
     * @return The SQL Generated
     */
    public abstract String toString();

    protected String getRemarkAsComment() {
        return remarkAsComment;
    }

    public AbstractStatement setRemarkAsComment(String remarkAsComment) {
        if (remarkAsComment == null) {
            remarkAsComment = "";
        }
        remarkAsComment = remarkAsComment.replaceAll("[\\r\\n]+", "¦");
        this.remarkAsComment = remarkAsComment;
        return this;
    }

    /**
     * 在给定的SqlConnection上执行SQL，异步返回ResultMatrix，或异步报错。
     * （如果SQL审计日志记录器可用）将为审计记录执行的SQL和执行结果，以及任何异常。
     *
     * @param sqlConnection Fetched from Pool
     * @return the result matrix wrapped in a future, any error would cause a failed future
     * @since 2.8 将整个运作体加入了try-catch，统一加入审计日志，出现异常时一律异步报错。
     */
    public final Future<ResultMatrix> execute(SqlConnection sqlConnection) {
        try {
            String sql = this.toString();
            getSqlAuditLogger().info(statement_uuid + " sql: " + sql);
            return sqlConnection.preparedQuery(sql)
                    .execute()
                    .compose(
                            rows -> {
                                ResultMatrix resultMatrix = ResultMatrix.create(rows);
                                getSqlAuditLogger().info(statement_uuid + " done", new JsonObject()
                                        .put("TotalAffectedRows", resultMatrix.getTotalAffectedRows())
                                        .put("TotalFetchedRows", resultMatrix.getTotalFetchedRows())
                                );
                                return Future.succeededFuture(resultMatrix);
                            },
                            throwable -> {
                                getSqlAuditLogger().error(statement_uuid + " execute failed");
                                getSqlAuditLogger().exception(statement_uuid, throwable);
                                return Future.failedFuture(throwable);
                            }
                    );
        } catch (Throwable throwable) {
            getSqlAuditLogger().error(statement_uuid + " exception");
            getSqlAuditLogger().exception(statement_uuid, throwable);
            return Future.failedFuture(throwable);
        }
    }
}
