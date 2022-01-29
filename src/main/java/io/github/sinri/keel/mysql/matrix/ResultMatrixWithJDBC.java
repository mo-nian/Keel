package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.Keel;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.data.Numeric;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ResultMatrixWithJDBC implements ResultMatrix {
    private final List<JsonObject> rowList;
    private Long lastInsertedID;
    private Integer affectedRows;

    public ResultMatrixWithJDBC() {
        rowList = new ArrayList<>();
    }

    public ResultMatrixWithJDBC(ResultSet resultSet) {
        rowList = new ArrayList<>();

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= columnCount; i++) {
                    int columnType = metaData.getColumnType(i);
                    if (
                            columnType == Types.DATE || columnType == Types.TIME
                                    || columnType == Types.DATALINK || columnType == Types.TIMESTAMP
                                    || columnType == Types.TIME_WITH_TIMEZONE || columnType == Types.TIMESTAMP_WITH_TIMEZONE
                    ) {
                        row.put(metaData.getColumnLabel(i), resultSet.getString(i));
                    } else {
                        row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                    }
                }
                rowList.add(row);
            }
        } catch (SQLException e) {
            Keel.logger("JDBC").exception(e);
        }
    }

    @Override
    public List<JsonObject> getRowList() {
        return rowList;
    }

    @Override
    public int getTotalFetchedRows() {
        return rowList.size();
    }

    @Override
    public int getTotalAffectedRows() {
        return affectedRows;
    }

    public void setAffectedRows(Integer affectedRows) {
        this.affectedRows = affectedRows;
    }

    @Override
    public long getLastInsertedID() {
        return lastInsertedID;
    }

    public void setLastInsertedID(Long lastInsertedID) {
        this.lastInsertedID = lastInsertedID;
    }

    @Override
    public JsonArray toJsonArray() {
        JsonArray objects = new JsonArray();
        for (var row : rowList) {
            objects.add(row);
        }
        return objects;
    }

    @Override
    public JsonObject getFirstRow() {
        return getRowByIndex(0);
    }

    @Override
    public JsonObject getRowByIndex(int index) {
        return rowList.get(index);
    }

    @Override
    public String getOneColumnOfFirstRowAsString(String columnName) {
        return getFirstRow().getString(columnName);
    }

    @Override
    public Numeric getOneColumnOfFirstRowAsNumeric(String columnName) {
        return Numeric.create(getFirstRow().getNumber(columnName));
    }

    @Override
    public Integer getOneColumnOfFirstRowAsInteger(String columnName) {
        return getFirstRow().getInteger(columnName);
    }

    @Override
    public Long getOneColumnOfFirstRowAsLong(String columnName) {
        return getFirstRow().getLong(columnName);
    }

    @Override
    public List<String> getOneColumnAsString(String columnName) {
        List<String> columns = new ArrayList<>();
        for (var row : rowList) {
            columns.add(row.getString(columnName));
        }
        return columns;
    }

    @Override
    public List<Numeric> getOneColumnAsNumeric(String columnName) {
        List<Numeric> columns = new ArrayList<>();
        for (var row : rowList) {
            columns.add(Numeric.create(row.getNumber(columnName)));
        }
        return columns;
    }

    @Override
    public List<Long> getOneColumnAsLong(String columnName) {
        List<Long> columns = new ArrayList<>();
        for (var row : rowList) {
            columns.add(row.getLong(columnName));
        }
        return columns;
    }

    @Override
    public List<Integer> getOneColumnAsInteger(String columnName) {
        List<Integer> columns = new ArrayList<>();
        for (var row : rowList) {
            columns.add(row.getInteger(columnName));
        }
        return columns;
    }
}
