package io.github.sinri.keel.core.logger2;

public enum KeelLogLevel {
    DEBUG, INFO, NOTICE, WARNING, ERROR, FATAL, SILENT;

    public boolean isEnoughSeriousAs(KeelLogLevel standardLevel) {
        return this.ordinal() >= standardLevel.ordinal();
    }

    public boolean isNegligibleThan(KeelLogLevel standardLevel) {
        return this.ordinal() < standardLevel.ordinal();
    }

    /**
     * @return should always be silent
     * @since 1.10
     */
    public boolean isSilent() {
        return this.ordinal() >= SILENT.ordinal();
    }
}
