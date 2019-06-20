package com.aplana.sbrf.taxaccounting.model.log;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Объект для логирования информации в ходе расчётов, проверок и других операций
 * В ходе выполнения операции пользователь добавляет в журнал сообщения при помощи методов {@link #info(String, Object...)},
 * {@link #warn(String, Object...)}, {@link #error(String, Object...)}
 * Вся информация, записываемая в журал будет дублироваться в журнал сервера приложений средствами Commons.Logging
 */
@Getter
@Setter
public class Logger implements Serializable {
    private static final Log LOG = LogFactory.getLog(Logger.class);

    // Идентификатор уведомления
    private String logId;
    // Список сообщений
    private List<LogEntry> entries = new ArrayList<>();

    //Добавили пока на пробу, поскольку необходимо логгирование в справочнике Подразделений
    private TAUserInfo taUserInfo;

    // Ограничение по длине для каждого сообщения об ошибке из Exception
    private static final int MAX_EXCEPTION_LOG_MESSAGE_LENGTH = 10000;

    /**
     * Добавить информационное сообщение в журнал (это сообщения, не требующие особой реакции пользователя)
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void info(String message, Object... args) {
        log(LogLevel.INFO, message, null, null, false, args);
    }

    /**
     * Добавить информационное сообщение в журнал (это сообщения, не требующие особой реакции пользователя)
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void infoExp(String message, String type, String object, Object... args) {
        log(LogLevel.INFO, message, type, object, false, args);
    }

    /**
     * Добавить предупреждающее сообщение в журнал (работа системы на нарушена, но нужно обратить внимание пользователя на что-то)
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void warn(String message, Object... args) {
        log(LogLevel.WARNING, message, null, null, false, args);
    }

    /**
     * Добавить предупреждающее сообщение в журнал (работа системы не нарушена, но нужно обратить внимание пользователя на что-то)
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void warnExp(String message, String type, String object, Object... args) {
        log(LogLevel.WARNING, message, type, object, false, args);
    }

    /**
     * Добавить сообщение об ошибке в журнал (ошибка, требующая вмешательства пользователя для корректной работы системы)
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void error(String message, Object... args) {
        log(LogLevel.ERROR, message, null, null, false, args);
    }

    /**
     * Добавить сообщение об ошибке в журнал (ошибка, требующая вмешательства пользователя для корректной работы системы)
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void errorExp(String message, String type, String object, Object... args) {
        log(LogLevel.ERROR, message, type, object, false, args);
    }

    /**
     * Логирование проверок с настройками фатальности
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param fatal   признак фатальности ошибки
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void logCheck(String message, boolean fatal, String type, String object, Object... args) {
        if (fatal) {
            errorExp(message, type, object, args);
        } else {
            warnExp(message, type, object, args);
        }
    }

    /**
     * Добавить информационное сообщение в журнал (это сообщения, не требующие особой реакции пользователя). Сообщение не добавляется, если оно уже существует в списке сообщений
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void infoIfNotExist(String message, Object... args) {
        log(LogLevel.INFO, message, null, null, true, args);
    }

    /**
     * Добавить информационное сообщение в журнал (это сообщения, не требующие особой реакции пользователя). Сообщение не добавляется, если оно уже существует в списке сообщений
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void infoIfNotExistExp(String message, String type, String object, Object... args) {
        log(LogLevel.INFO, message, type, object, true, args);
    }

    /**
     * Добавить предупреждающее сообщение в журнал (работа системы на нарушена, но нужно обратить внимание пользователя на что-то). Сообщение не добавляется, если оно уже существует в списке сообщений
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void warnIfNotExist(String message, Object... args) {
        log(LogLevel.WARNING, message, null, null, true, args);
    }

    /**
     * Добавить предупреждающее сообщение в журнал (работа системы на нарушена, но нужно обратить внимание пользователя на что-то). Сообщение не добавляется, если оно уже существует в списке сообщений
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void warnIfNotExistExp(String message, String type, String object, Object... args) {
        log(LogLevel.WARNING, message, type, object, true, args);
    }

    /**
     * Добавить сообщение об ошибке в журнал (ошибка, требующая вмешательства пользователя для корректной работы системы). Сообщение не добавляется, если оно уже существует в списке сообщений
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void errorIfNotExistExp(String message, Object... args) {
        log(LogLevel.ERROR, message, null, null, true, args);
    }

    /**
     * Добавить сообщение об ошибке в журнал (ошибка, требующая вмешательства пользователя для корректной работы системы). Сообщение не добавляется, если оно уже существует в списке сообщений
     *
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void errorIfNotExistExp(String message, String type, String object, Object... args) {
        log(LogLevel.ERROR, message, type, object, true, args);
    }

    /**
     * Записать сообщение о неожиданном исключении в журнал
     * В журнал сервера приложений будет выведен стектрейс ошибки
     *
     * @param e исключение
     */
    public void error(Throwable e) {
        String msg = e.getMessage();
        if (msg != null && msg.length() > MAX_EXCEPTION_LOG_MESSAGE_LENGTH) {
            msg = msg.substring(0, MAX_EXCEPTION_LOG_MESSAGE_LENGTH - 1) + '…';
        }
        log(LogLevel.ERROR, "Ошибка: %s", null, null, false, msg);
        LOG.error("Unhandled exception: " + msg, e);
    }

    /**
     * Записывает соощение, которое будет находиться первым в списке
     *
     * @param level   уровень важности
     * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)}
     * @param args    набор объектов для подставновки в текст сообщения, может не задаваться
     */
    public void logTopMessage(LogLevel level, String message, Object... args) {
        int topPosition = 0;

        String extMessage = String.format(message, args);

        LogEntry entry = new LogEntry(level, extMessage);
        entries.add(topPosition, entry);
    }

    public void log(LogLevel level, String message) {
        log(level, message, null, null, false);
    }

    private void log(LogLevel level, String message, String type, String object, boolean excludeIfNotExist, Object... args) {
        String extMessage = message;
        if (args != null && args.length > 0) {
            extMessage = String.format(message, args);
        }

        LogEntry entry = new LogEntry(level, extMessage, type, object);
        if (!excludeIfNotExist || !entries.contains(entry)) {
            entries.add(entry);
        }
    }

    /**
     * Метод проверяет, что в журнале содержится хотя бы одно сообщение с указанным уровнем важности
     *
     * @param level уровень важности
     * @return true, если сообщения есть, false - в противном случае
     */
    public boolean containsLevel(LogLevel level) {
        for (LogEntry entry : entries) {
            if (entry.getLevel() == level) {
                return true;
            }
        }
        return false;
    }

    /**
     * Получить последнюю запись.
     */
    public LogEntry getLastEntry() {
        if (entries.isEmpty()) return null;
        return entries.get(entries.size() - 1);
    }

    /**
     * Очистить содержимое журнала
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Очистить содержимое журнала с определенным уровнем сообщений
     */
    public void clear(LogLevel logLevel) {
        Iterator<LogEntry> it = entries.iterator();
        while (it.hasNext()) {
            LogEntry entry = it.next();
            if (entry.getLevel().equals(logLevel)) {
                it.remove();
            }
        }
    }
}