package io.github.sinri.keel.mysql.jdbc;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.KeelMySQLConfig;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.github.sinri.keel.mysql.matrix.ResultMatrixWithJDBC;

import java.sql.*;

public class KeelJDBCForMySQL {

    private final String jdbcConnectionString;
    private final String username;
    private final String password;

    public KeelJDBCForMySQL(KeelMySQLConfig config) {
        this.jdbcConnectionString = config.buildJDBCConnectionString();
        this.username = config.getUsername();
        this.password = config.getPassword();
        //System.out.println("JDBC: "+this.jdbcConnectionString);
    }

    public ConnectionWrapper makeConnectionWrapper(boolean autoCommit) {
        Connection connection;
        try {
            connection = DriverManager.getConnection(jdbcConnectionString, username, password);
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            Keel.logger("JDBC").exception(e);
            connection = null;
        }
        return ConnectionWrapper.wrap(connection);
    }

    public ConnectionStatementWrapper makeStatementWrapper(boolean autoCommit) {
        Connection connection;
        try {
            connection = DriverManager.getConnection(jdbcConnectionString, username, password);
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            Keel.logger("JDBC").exception(e);
            connection = null;
        }
        return ConnectionStatementWrapper.wrap(connection);
    }

    public ResultMatrix queryForSelection(String sql) {
        try (ConnectionStatementWrapper statement = makeStatementWrapper(true)) {
            ResultSet resultSet = statement.getStatement().executeQuery(sql);
            ResultMatrixWithJDBC resultMatrixWithJDBC = new ResultMatrixWithJDBC(resultSet);
            resultSet.close();
            return resultMatrixWithJDBC;
        } catch (Exception e) {
            Keel.logger("JDBC").exception(e);
            return null;
        }
    }

    public ResultMatrix queryForSelection(String sql, Statement statement) throws SQLException {
        ResultSet resultSet = statement.executeQuery(sql);
        ResultMatrixWithJDBC resultMatrixWithJDBC = new ResultMatrixWithJDBC(resultSet);
        resultSet.close();
        return resultMatrixWithJDBC;
    }

    public ResultMatrix executeForInsertion(String sql) {
        try (ConnectionStatementWrapper statement = makeStatementWrapper(true)) {
            int afx = statement.getStatement().executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            long autoIncKeyFromApi = -1;

            ResultSet rs = statement.getStatement().getGeneratedKeys();

            if (rs.next()) {
                autoIncKeyFromApi = rs.getLong(1);
            }

            rs.close();

            ResultMatrixWithJDBC resultMatrixWithJDBC = new ResultMatrixWithJDBC();
            resultMatrixWithJDBC.setAffectedRows(afx);
            resultMatrixWithJDBC.setLastInsertedID(autoIncKeyFromApi);
            return resultMatrixWithJDBC;
        } catch (Exception e) {
            Keel.logger("JDBC").exception(e);
            return null;
        }
    }

    public ResultMatrix executeForInsertion(String sql, Statement statement) throws SQLException {
        int afx = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

        long autoIncKeyFromApi = -1;

        ResultSet rs = statement.getGeneratedKeys();

        if (rs.next()) {
            autoIncKeyFromApi = rs.getLong(1);
        }

        rs.close();

        ResultMatrixWithJDBC resultMatrixWithJDBC = new ResultMatrixWithJDBC();
        resultMatrixWithJDBC.setAffectedRows(afx);
        resultMatrixWithJDBC.setLastInsertedID(autoIncKeyFromApi);
        return resultMatrixWithJDBC;
    }

    public ResultMatrix executeForModification(String sql) {
        try (ConnectionStatementWrapper statement = makeStatementWrapper(true)) {
            int afx = statement.getStatement().executeUpdate(sql);
            ResultMatrixWithJDBC resultMatrixWithJDBC = new ResultMatrixWithJDBC();
            resultMatrixWithJDBC.setAffectedRows(afx);
            return resultMatrixWithJDBC;
        } catch (Exception e) {
            Keel.logger("JDBC").exception(e);
            return null;
        }
    }

    public ResultMatrix executeForModification(String sql, Statement statement) throws SQLException {
        int afx = statement.executeUpdate(sql);
        ResultMatrixWithJDBC resultMatrixWithJDBC = new ResultMatrixWithJDBC();
        resultMatrixWithJDBC.setAffectedRows(afx);
        return resultMatrixWithJDBC;
    }

    public Statement createStatement(boolean autoCommit) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcConnectionString, username, password);
        connection.setAutoCommit(autoCommit);
        return connection.createStatement();
    }

    public void closeStatement(Statement statement) throws SQLException {
        Connection connection = statement.getConnection();
        statement.close();
        statement.close();
    }

    public Statement begin() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcConnectionString, username, password);
        connection.setAutoCommit(false);
        return connection.createStatement();
    }

    public void commit(Statement statement) throws SQLException {
        Connection connection = statement.getConnection();
        connection.commit();
        statement.close();
        statement.close();
    }

    public void rollback(Statement statement) throws SQLException {
        Connection connection = statement.getConnection();
        connection.rollback();
        statement.close();
        statement.close();
    }

    public static class ConnectionWrapper implements AutoCloseable {
        protected final Connection connection;

        protected ConnectionWrapper(Connection connection) {
            this.connection = connection;
        }

        public static ConnectionWrapper wrap(Connection connection) {
            return new ConnectionWrapper(connection);
        }

        public Connection getConnection() {
            return this.connection;
        }

        @Override
        public void close() throws Exception {
            if (this.connection != null) {
                //System.out.println("JDBC Connection Closing");
                this.connection.close();
            }
        }
    }

    public static class ConnectionStatementWrapper extends ConnectionWrapper implements AutoCloseable {
        protected Statement statement = null;

        protected ConnectionStatementWrapper(Connection connection) {
            super(connection);
            if (this.getConnection() != null) {
                try {
                    this.statement = this.connection.createStatement();
                } catch (SQLException e) {
                    Keel.logger("JDBC").exception(e);
                }
            }
        }

        public static ConnectionStatementWrapper wrap(Connection connection) {
            return new ConnectionStatementWrapper(connection);
        }

        public Statement getStatement() {
            return statement;
        }

        @Override
        public void close() throws Exception {
            if (this.connection != null) {
                //System.out.println("JDBC Connection Closing");
                if (this.statement != null) {
                    this.statement.close();
                }
                this.connection.close();
            }
        }
    }
}
