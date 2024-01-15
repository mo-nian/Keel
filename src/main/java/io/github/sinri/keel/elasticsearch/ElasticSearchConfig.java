package io.github.sinri.keel.elasticsearch;

import io.github.sinri.keel.facade.KeelConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.7
 */
public class ElasticSearchConfig extends KeelConfiguration {

    public ElasticSearchConfig(KeelConfiguration configuration) {
        super(configuration);
    }

    public ElasticSearchConfig(String esKey) {
        this(Keel.getConfiguration().extract("es", esKey));
    }

    /*
    String esUsername = Keel.config("es.kumori.username");
        String esPassword = Keel.config("es.kumori.password");
        String esClusterHost = Keel.config("es.kumori.cluster.host");
        int esClusterPort = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.cluster.port")), 9200);
        String esClusterScheme = Objects.requireNonNullElse(Keel.config("es.kumori.cluster.scheme"), "http");
        int esPoolSize = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.pool.size")), 16);
        int esRestMaxConnection = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.rest.maxConnection")), 320);
        int esRestMaxConnectionPerRoute = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.rest.maxConnectionPerRoute")), 160);

     */

    public String username() {
        return readString("username");
    }

    public String password() {
        return readString("password");
    }

    public @Nonnull String clusterHost() {
        return Objects.requireNonNull(readString("cluster", "host"));
    }

    public int clusterPort() {
        String s = readString("cluster", "port");
        return Integer.parseInt(Objects.requireNonNullElse(s, "9200"));
    }

    public @Nonnull String clusterScheme() {
        String s = readString("cluster", "scheme");
        return Objects.requireNonNullElse(s, "http");
    }

    public @Nonnull String clusterApiUrl(@Nonnull String endpoint) {
        return this.clusterScheme() + "://" + this.clusterHost() + ":" + this.clusterPort() + endpoint;
    }

    public @Nullable String opaqueId() {
        return readString("opaqueId");
    }
}
