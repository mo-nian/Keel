package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.mysql.KeelMySQLQuoter;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class UpdateStatement extends AbstractStatement {
    /**
     * UPDATE [LOW_PRIORITY] [IGNORE] table_reference
     * SET assignment_list
     * [WHERE where_condition]
     * [ORDER BY ...]
     * [LIMIT row_count]
     */

    String ignoreMark = "";
    String schema;
    String table;
    final List<String> assignments = new ArrayList<>();
    //    final List<KeelMySQLCondition> whereConditions = new ArrayList<>();
    final ConditionsComponent whereConditionsComponent = new ConditionsComponent();
    final List<String> sortRules = new ArrayList<>();
    long limit = 0;

    public UpdateStatement() {

    }

    public UpdateStatement ignore() {
        this.ignoreMark = "IGNORE";
        return this;
    }

    public UpdateStatement table(String table) {
        this.schema = null;
        this.table = table;
        return this;
    }

    public UpdateStatement table(String schema, String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    public UpdateStatement setWithExpression(Map<String, String> columnExpressionMapping) {
        columnExpressionMapping.forEach((k, v) -> assignments.add(k + "=" + v));
        return this;
    }

    public UpdateStatement setWithExpression(String column, String expression) {
        assignments.add(column + "=" + expression);
        return this;
    }

    public UpdateStatement setWithValue(String column, Number value) {
        assignments.add(column + "=" + (new KeelMySQLQuoter(value)));
        return this;
    }

    public UpdateStatement setWithValue(String column, String value) {
        assignments.add(column + "=" + (new KeelMySQLQuoter(value)));
        return this;
    }

    public UpdateStatement where(Function<ConditionsComponent, ConditionsComponent> function) {
        function.apply(whereConditionsComponent);
        return this;
    }

//    public UpdateStatement where(KeelMySQLCondition condition) {
//        whereConditions.add(condition);
//        return this;
//    }
//
//    public UpdateStatement whereForRaw(Function<RawCondition, RawCondition> f) {
//        RawCondition condition = new RawCondition();
//        whereConditions.add(f.apply(condition));
//        return this;
//    }
//
//    public UpdateStatement whereForCompare(Function<CompareCondition, CompareCondition> f) {
//        CompareCondition condition = new CompareCondition();
//        whereConditions.add(f.apply(condition));
//        return this;
//    }
//
//    public UpdateStatement whereForAmongst(Function<AmongstCondition, AmongstCondition> f) {
//        whereConditions.add(f.apply(new AmongstCondition()));
//        return this;
//    }
//
//    public UpdateStatement whereForAndGroup(Function<GroupCondition, GroupCondition> f) {
//        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_AND);
//        whereConditions.add(f.apply(condition));
//        return this;
//    }
//
//    public UpdateStatement whereForOrGroup(Function<GroupCondition, GroupCondition> f) {
//        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_OR);
//        whereConditions.add(f.apply(condition));
//        return this;
//    }

    public UpdateStatement orderByAsc(String x) {
        sortRules.add(x);
        return this;
    }

    public UpdateStatement orderByDesc(String x) {
        sortRules.add(x + " DESC");
        return this;
    }

    public UpdateStatement limit(long limit) {
        this.limit = limit;
        return this;
    }

    public String toString() {
        String sql = "UPDATE " + ignoreMark;
        if (schema != null) {
            sql += " " + schema + ".";
        }
        sql += table;
        sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "SET " + KeelHelper.joinStringArray(assignments, ",");
        if (!whereConditionsComponent.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "WHERE " + whereConditionsComponent;
        }
        if (!sortRules.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "ORDER BY " + KeelHelper.joinStringArray(sortRules, ",");
        }
        if (limit > 0) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "LIMIT " + limit;
        }
        return sql;
    }

    /**
     * @param sqlConnection get from pool
     * @return future with affected rows; -1 when failed
     * @since 1.7
     */
    public Future<Integer> executeForAffectedRows(SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getTotalAffectedRows()))
                .recover(throwable -> {
                    Keel.outputLogger("MySQL").warning(getClass().getName() + " executeForAffectedRows failed [" + throwable.getMessage() + "] when executing SQL: " + this);
                    return Future.succeededFuture(-1);
                });
    }
}
