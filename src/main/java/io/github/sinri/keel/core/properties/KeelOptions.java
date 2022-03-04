package io.github.sinri.keel.core.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Keel Options as POJO
 * Its `properties`, as POJO, just store the String Values.
 * You may use `get` methods to get them as the expected format.
 * <p>
 * As of 1.12, the fields could be other than String with auto cast.
 */
abstract public class KeelOptions {
    final static public String BOOL_YES = "YES";
    final static public String BOOL_NO = "NO";

    public KeelOptions() {
        initializeProperties();
    }

    public KeelOptions(JsonObject jsonObject) {
        initializeProperties();
        overwritePropertiesWithJsonObject(jsonObject);
    }

    public static <T extends KeelOptions> T loadWithYamlFilePath(String yamlFilePath, Class<T> classOfT) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Path.of(yamlFilePath));
        } catch (IOException e) {
            URL resource = KeelOptions.class.getClassLoader().getResource(yamlFilePath);
            if (resource == null) {
                throw new RuntimeException("Embedded one is not found after not found in FS");
            }
            try {
                String file = resource.getFile();
                bytes = Files.readAllBytes(Path.of(file));
            } catch (IOException ex) {
                throw new RuntimeException("read embedded config failed", ex);
            }
        }

        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();

        try {
            return mapper.readValue(new String(bytes, StandardCharsets.UTF_8), classOfT);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("YAML parse to JSON failed", e);
        }

//        try {
//            String yamlString = new String(bytes, StandardCharsets.UTF_8);
//            JsonNode root = new YAMLMapper().readTree(yamlString);
//            JsonObject json = new JsonObject(root.toString());
//
//            System.out.println("json parsed from yaml: "+json);
//
//            try {
//                return classOfT.getConstructor(JsonObject.class).newInstance(json);
//            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//                //Keel.outputLogger("KeelOptions").exception("Load YAML to POJO failed",e);
//                throw new RuntimeException("Load YAML to POJO failed", e);
//            }
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("YAML parse to JSON failed", e);
//        }
    }

    abstract protected void initializeProperties();

//    /**
//     * Note: One YAML document in one file!
//     *
//     * @param yamlFilePath YAML FILE PATH
//     * @param mappedClass  the class of Mapped Class, extending KeelOptions
//     * @param <T>          Mapped Class, extending KeelOptions
//     * @return the mapped class instance
//     * @deprecated
//     */
//    public static <T> Future<T> loadWithYamlFilePath1(String yamlFilePath, Class<T> mappedClass) {
//        return Future.succeededFuture()
//                .compose(v -> Keel.getVertx().fileSystem().readFile(yamlFilePath))
//                .recover(throwable -> {
//                    URL resource = KeelOptions.class.getClassLoader().getResource(yamlFilePath);
//                    if (resource == null) {
//                        return Future.failedFuture("Embedded one is not found after not found in FS: " + throwable.getMessage());
//                    }
//                    return Keel.getVertx().fileSystem().readFile(resource.getPath());
//                })
//                .compose(buffer -> new YamlProcessor().process(Keel.getVertx(), null, buffer))
//                .compose(jsonObject -> {
//                    try {
//                        T x = mappedClass.getConstructor(JsonObject.class).newInstance(jsonObject);
//                        return Future.succeededFuture(x);
//                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//                        //Keel.outputLogger("KeelOptions").exception("Load YAML to POJO failed",e);
//                        return Future.failedFuture(new Exception("Load YAML to POJO failed", e));
//                    }
//                });
//    }

    public final void overwritePropertiesWithJsonObject(JsonObject jsonObject) {
        jsonObject.forEach(stringObjectEntry -> {
            try {
                Field field = this.getClass().getField(stringObjectEntry.getKey());
                Object value = stringObjectEntry.getValue();

                Class<?> type = field.getType();
                if (type == boolean.class || type == Boolean.class) {
                    field.setBoolean(this, value.toString().equals(BOOL_YES));
                } else if (type == Byte.class || type == byte.class) {
                    field.setByte(this, Byte.parseByte(value.toString()));
                } else if (type == short.class || type == Short.class) {
                    field.setShort(this, Short.parseShort(value.toString()));
                } else if (type == int.class || type == Integer.class) {
                    field.setInt(this, Integer.parseInt(value.toString()));
                } else if (type == long.class || type == Long.class) {
                    field.setLong(this, Long.parseLong(value.toString()));
                } else if (type == float.class || type == Float.class) {
                    field.setFloat(this, Float.parseFloat(value.toString()));
                } else if (type == double.class || type == Double.class) {
                    field.setDouble(this, Double.parseDouble(value.toString()));
                } else if (KeelOptions.class.isAssignableFrom(type)) {
                    if (value instanceof JsonObject) {
                        try {
                            JsonObject x = (JsonObject) value;
                            field.set(this, type.getConstructor(JsonObject.class).newInstance(x));
                        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                            // just ignore
                        }
                    } else if (value instanceof JsonArray) {

                    }
                } else if (field.getType().isInstance(value)) {
                    field.set(this, value);
                }
                // else ignore
            } catch (NoSuchFieldException e) {
                // just ignore!
            } catch (IllegalAccessException e) {
                // just ignore
            }
        });
    }
}
