package io.github.sinri.keel.elasticsearch;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Developed with ES version 8.9.
 *
 * @since 3.0.7
 */
public interface ESApiMixin {
    ElasticSearchConfig getEsConfig();

    KeelEventLogger getLogger();

    /**
     * Hot Fix 3.1.9.1
     *
     * @since 3.1.10
     * For Bulk API, of which the body is not a json object.
     */
    default Future<JsonObject> call(@Nonnull HttpMethod httpMethod, @Nonnull String endpoint, @Nullable ESApiQueries queries, @Nullable String requestBody) {
        WebClient webClient = WebClient.create(Keel.getVertx());
        String url = this.getEsConfig().clusterApiUrl(endpoint);
        HttpRequest<Buffer> bufferHttpRequest = webClient.requestAbs(httpMethod, url);

        bufferHttpRequest.basicAuthentication(getEsConfig().username(), getEsConfig().password());
        bufferHttpRequest.putHeader("Accept", "application/vnd.elasticsearch+json");
        bufferHttpRequest.putHeader("Content-Type", "application/vnd.elasticsearch+json");

        String opaqueId = this.getEsConfig().opaqueId();
        if (opaqueId != null) {
            bufferHttpRequest.putHeader("X-Opaque-Id", opaqueId);
        }

        JsonObject queriesForLog = new JsonObject();
        if (queries != null) {
            queries.forEach((k, v) -> {
                bufferHttpRequest.addQueryParam(k, v);
                queriesForLog.put(k, v);
            });
        }

        Handler<KeelEventLog> logRequestEnricher = log -> log
                .put("request", new JsonObject()
                        .put("method", httpMethod.name())
                        .put("endpoint", endpoint)
                        .put("queries", queriesForLog)
                        .put("body", requestBody)
                );

        return Future.succeededFuture()
                .compose(v -> {
                    if (httpMethod == HttpMethod.GET || httpMethod == HttpMethod.DELETE) {
                        return bufferHttpRequest.send();
                    } else {
                        return bufferHttpRequest.sendBuffer(Buffer.buffer(Objects.requireNonNullElse(requestBody, "")));
                    }
                })
                .compose(bufferHttpResponse -> {
                    int statusCode = bufferHttpResponse.statusCode();
                    JsonObject resp = bufferHttpResponse.bodyAsJsonObject();

                    if ((statusCode >= 300 || statusCode < 200) || resp == null) {
                        this.getLogger().error(log -> {
                            logRequestEnricher.handle(log);
                            log.message("ES API Response Error")
                                    .put("response", new JsonObject()
                                            .put("status_code", statusCode)
                                            .put("raw", bufferHttpResponse.bodyAsString())
                                    );
                        });
                        return Future.failedFuture("ES API: STATUS CODE IS " + statusCode + " | " + bufferHttpResponse.bodyAsString());
                    }
                    this.getLogger().info(log -> {
                        logRequestEnricher.handle(log);
                        log.message("ES API Response Error")
                                .put("response", new JsonObject()
                                        .put("status_code", statusCode)
                                        .put("body", resp)
                                );
                    });
                    return Future.succeededFuture(resp);
                });
    }

    /**
     * Hot Fix 3.1.9.1
     *
     * @since 3.1.10 based on `io.github.sinri.keel.elasticsearch.ESApiMixin#call(io.vertx.core.http.HttpMethod, java.lang.String, io.github.sinri.keel.elasticsearch.ESApiMixin.ESApiQueries, java.lang.String)`
     */
    default Future<JsonObject> callPost(@Nonnull String endpoint, @Nullable ESApiQueries queries, @Nonnull JsonObject requestBody) {
        return call(HttpMethod.POST, endpoint, queries, requestBody.toString());
    }

    class ESApiQueries extends HashMap<String, String> {
        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            this.forEach(jsonObject::put);
            return jsonObject;
        }
    }
}
