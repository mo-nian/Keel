package io.github.sinri.keel.mysql;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.properties.KeelOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;

public class KeelMySQLOptions extends KeelOptions {
    public String host;
    public String port;
    public String username;
    public String password;
    public String schema;
    public String charset;
    public String useAffectedRows;
    public String poolMaxSize;
    public String poolShared;
    protected String dataSourceName;

    public KeelMySQLOptions(JsonObject jsonObject) {
        super(jsonObject);
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

    @Override
    protected void initializeProperties() {
        this.host = "127.0.0.1";
        this.port = "3306";
        this.username = "anonymous";
        this.password = "";
        this.schema = "test";
        this.charset = "utf8";
        this.useAffectedRows = BOOL_YES;
        this.poolMaxSize = "8";
        this.poolShared = BOOL_NO;
    }

    public MySQLConnectOptions buildMySQLConnectOptions() {
        return new MySQLConnectOptions()
                .setPort(Integer.parseInt(port))
                .setHost(host)
                .setDatabase(schema)
                .setUser(username)
                .setPassword(password)
                .setCharset(charset)
                .setUseAffectedRows(BOOL_YES.equals(useAffectedRows));
    }

    public PoolOptions buildPoolOptions() {
        return new PoolOptions()
                .setMaxSize(Integer.parseInt(this.poolMaxSize))
                .setShared(Boolean.parseBoolean(this.poolShared));
    }

    public String buildJDBCConnectionString() {
        return "jdbc:mysql://" + host + ":" + port + "/" + schema + "?useSSL=false&useUnicode=true&characterEncoding=" + charset;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}