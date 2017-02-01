package com.aplana.sbrf.taxaccounting.model.log;

/**
 * Уровни важности сообщений, записываемых в журнал {@link Logger}
 */
public enum LogLevel {

    /**
     * Информационное сообщение, реакция пользователя не требуется
     */
    INFO(0),

    /**
     * Сообщение, обращающее внимание пользователя на какой-то факт, возможно некритическая ошибка
     */
    WARNING(1),

    /**
     * Сообщение об ошибке, мешающей работе системы и требующей вмешательства пользователя
     */
    ERROR(2);

    /**
     * Идентификатор уровня важности
     */
    private int id;

    LogLevel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LogLevel fromId(int logLevelId) {
        for (LogLevel logLevel : LogLevel.values()) {
            if (logLevel.getId() == logLevelId) {
                return logLevel;
            }
        }

        throw new IllegalArgumentException("Wrong logLevelId: " + logLevelId);
    }
}
