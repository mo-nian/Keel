package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonObject;

/**
 * @since 2.7
 */
public class JsonNullScheme extends JsonValueScheme {
    public JsonNullScheme() {
        super();
        this.setNullable(true);
    }

    @Override
    public JsonElementSchemeType getJsonElementSchemeType() {
        return JsonElementSchemeType.JsonNull;
    }

    @Override
    public JsonObject toJsonObject() {
        return super.toJsonObject()
                .put("scheme_type", getJsonElementSchemeType());
    }

    @Override
    public JsonElementScheme reloadDataFromJsonObject(JsonObject jsonObject) {
        return super.reloadDataFromJsonObject(jsonObject);
    }

    @Override
    public boolean validate(Object object) {
        return object == null;
    }
}
