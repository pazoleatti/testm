package com.aplana.sbrf.taxaccounting.model.log;

/**
 * Уровни важности сообщений, записываемых в журнал {@link Logger}
 */
public enum LogLevelType {

    INCOME(1),
    DEDUCTION(2),
    PREPAYMENT(3);

    /**
     * Идентификатор уровня
     */
    private int id;

    LogLevelType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LogLevelType fromId(int logLevelId) {
        for (LogLevelType logLevel : LogLevelType.values()) {
            if (logLevel.getId() == logLevelId) {
                return logLevel;
            }
        }

        throw new IllegalArgumentException("Wrong logLevelId: " + logLevelId);
    }
}
