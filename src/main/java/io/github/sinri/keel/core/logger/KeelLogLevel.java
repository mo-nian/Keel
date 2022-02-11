package io.github.sinri.keel.core.logger;

/**
 * The Keel Log Level Enum
 * Since 1.10 the SILENT added
 */
public enum KeelLogLevel {
    DEBUG, INFO, NOTICE, WARNING, ERROR, FATAL, SILENT;

    public boolean isMoreSeriousThan(KeelLogLevel standardLevel) {
        return this.ordinal() >= standardLevel.ordinal();
    }

    /**
     * @return should always be silent
     * @since 1.10
     */
    public boolean isSilent() {
        return this.ordinal() >= SILENT.ordinal();
    }
}
