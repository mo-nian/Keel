package io.github.sinri.keel.mysql;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.properties.KeelOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;

public class KeelMySQLOptions extends KeelOptions {
    public String host;
    public int port;
    public String username;
    public String password;
    public String schema;
    public String charset;
    public boolean useAffectedRows;
    public int poolMaxSize;
    public boolean poolShared;
    protected String dataSourceName;
    public boolean allowPublicKeyRetrieval;

    public KeelMySQLOptions() {
        this.host = "127.0.0.1";
        this.port = 3306;
        this.username = "anonymous";
        this.password = "";
        this.schema = "test";
        this.charset = "utf8";
        this.useAffectedRows = true;
        this.allowPublicKeyRetrieval = false;
        this.poolMaxSize = 128;
        this.poolShared = false;
    }

    public static KeelMySQLOptions generateOptionsForDataSourceWithPropertiesReader(String dataSourceName) {
        KeelMySQLOptions keelMySQLOptions = Keel.getPropertiesReader().filter("mysql." + dataSourceName).toConfiguration(KeelMySQLOptions.class);
        keelMySQLOptions.setDataSourceName(dataSourceName);
        return keelMySQLOptions;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public KeelMySQLOptions setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        return this;
    }

    public MySQLConnectOptions buildMySQLConnectOptions() {
        return new MySQLConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(schema)
                .setUser(username)
                .setPassword(password)
                .setCharset(charset)
                .setUseAffectedRows(useAffectedRows);
    }

    public PoolOptions buildPoolOptions() {
        return new PoolOptions()
                .setMaxSize(this.poolMaxSize)
                .setShared(this.poolShared);
    }

    public String buildJDBCConnectionString() {
        return "jdbc:mysql://" + host + ":" + port + "/" + schema
                + "?useSSL=false&useUnicode=true" +
                "&characterEncoding=" + charset
                + "&allowPublicKeyRetrieval=" + (allowPublicKeyRetrieval ? "true" : "false");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
