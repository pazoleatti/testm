package com.aplana.sbrf.taxaccounting.model.exception;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

import java.util.List;

/**
 * Класс-исключение, использующийся когда на сервисном слое происходит ошибка связанная с тем,
 * что скрипт выполнился с ошибками.
 *
 * @author sgoryachkin
 */
public class ServiceLoggerException extends ServiceException {
    private static final long serialVersionUID = -6734031798154470925L;

    private String uuid;

    public ServiceLoggerException(String message, String uuid, Object... params) {
        super(message, params);
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Вывод ошибок/сообщений ввиде строки
     *
     * @param logEntries Список сообщений
     * @param limit      Лимит вывода
     * @return
     */
    public static String getLogEntriesString(List<LogEntry> logEntries, int limit) {
        if (logEntries == null || logEntries.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        for (LogEntry entry : logEntries) {
            if (limit > 0 && ++counter > limit) {
                break;
            }
            builder.append(entry.getLevel()).append(" ").append(entry.getMessage());
        }
        return builder.toString();
    }

    /**
     * Вывод ошибок/сообщений ввиде строки
     *
     * @param logEntries Список сообщений
     * @return
     */
    public static String getLogEntriesString(List<LogEntry> logEntries) {
        return getLogEntriesString(logEntries, -1);
    }
}
