package io.github.sinri.keel.mysql;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.KeelConfiguration;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class KeelMySQLDataSourceProvider {

    private static final Map<String, MySQLDataSource> mySQLDataSourceMap = new ConcurrentHashMap<>();

    public static MySQLDataSource getMySQLDataSource() {
        String defaultName = defaultMySQLDataSourceName();
        return getMySQLDataSource(defaultName);
    }

    public static MySQLDataSource getMySQLDataSource(@Nonnull String dataSourceName) {
        if (!mySQLDataSourceMap.containsKey(dataSourceName)) {
            KeelConfiguration configuration = Keel.getConfiguration().extract("mysql", dataSourceName);
            Objects.requireNonNull(configuration);
            KeelMySQLConfiguration mySQLConfigure = new KeelMySQLConfiguration(dataSourceName, configuration);
            MySQLDataSource mySQLDataSource = new MySQLDataSource(mySQLConfigure);
            mySQLDataSourceMap.put(dataSourceName, mySQLDataSource);
        }
        return mySQLDataSourceMap.get(dataSourceName);
    }

    @Nonnull
    public static String defaultMySQLDataSourceName() {
        return Objects.requireNonNullElse(Keel.getConfiguration().readString("mysql", "default_data_source_name"), "default");
    }
}
