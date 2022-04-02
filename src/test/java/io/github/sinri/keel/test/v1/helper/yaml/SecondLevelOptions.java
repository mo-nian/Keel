package io.github.sinri.keel.test.v1.helper.yaml;

import io.github.sinri.keel.core.properties.KeelOptions;
import io.vertx.core.json.JsonObject;

public class SecondLevelOptions extends KeelOptions {
    private String filterName;

    public SecondLevelOptions() {
        super();
        initializeProperties();
    }

    public SecondLevelOptions(JsonObject jsonObject) {
        super();
        initializeProperties();
        overwritePropertiesWithJsonObject(jsonObject);
    }

    public String getFilterName() {
        return filterName;
    }

    public SecondLevelOptions setFilterName(String filterName) {
        this.filterName = filterName;
        return this;
    }

    protected void initializeProperties() {
        filterName = "default";
    }

    public String toString() {
        return "SecondLevelOptions{filterName=" + filterName + "}";
    }


}