package io.github.sinri.keel.mysql.matrix;


import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.data.Numeric;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.1
 * @since 1.8 becomes interface
 * May overrides this class to get Customized Data Matrix
 */
public interface ResultMatrix {

    List<JsonObject> getRowList();

    int getTotalFetchedRows();

    int getTotalAffectedRows();

    long getLastInsertedID();

    JsonArray toJsonArray();

    JsonObject getFirstRow() throws KeelSQLResultRowIndexError;

    JsonObject getRowByIndex(int index) throws KeelSQLResultRowIndexError;

    /**
     * @param row
     * @param classOfTableRow
     * @param <T>
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @since 1.10
     */
    static <T extends AbstractTableRow> T buildTableRow(JsonObject row, Class<T> classOfTableRow) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return classOfTableRow.getConstructor(JsonObject.class).newInstance(row);
    }

    /**
     * @param rowList
     * @param classOfTableRow
     * @param <T>
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @since 1.10
     */
    static <T extends AbstractTableRow> List<T> buildTableRowList(List<JsonObject> rowList, Class<T> classOfTableRow) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ArrayList<T> list = new ArrayList<>();
        for (var x : rowList) {
            list.add(ResultMatrix.buildTableRow(x, classOfTableRow));
        }
        return list;
    }

    /**
     * @param index
     * @param classOfTableRow
     * @param <T>
     * @return
     * @since 1.10
     */
    <T extends AbstractTableRow> T buildTableRowByIndex(int index, Class<T> classOfTableRow) throws KeelSQLResultRowIndexError;

    /**
     * @param classOfTableRow
     * @param <T>
     * @return
     * @since 1.10
     */
    <T extends AbstractTableRow> List<T> buildTableRowList(Class<T> classOfTableRow);

    String getOneColumnOfFirstRowAsString(String columnName) throws KeelSQLResultRowIndexError;

    Numeric getOneColumnOfFirstRowAsNumeric(String columnName) throws KeelSQLResultRowIndexError;

    Integer getOneColumnOfFirstRowAsInteger(String columnName) throws KeelSQLResultRowIndexError;

    Long getOneColumnOfFirstRowAsLong(String columnName) throws KeelSQLResultRowIndexError;

    List<String> getOneColumnAsString(String columnName);

    List<Numeric> getOneColumnAsNumeric(String columnName);

    List<Long> getOneColumnAsLong(String columnName);

    List<Integer> getOneColumnAsInteger(String columnName);
}
