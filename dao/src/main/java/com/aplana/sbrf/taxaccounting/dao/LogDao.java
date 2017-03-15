package com.aplana.sbrf.taxaccounting.dao;

/**
 * DAO для работы с группой логов
 *
 * @author pmakarov
 */
public interface LogDao {

    /**
     * Сохраняет группу логов
     *
     * @param logId уникальный идентификатор группы логов {@link java.util.UUID}
     */
    void save(String logId);

    /**
     * Удаление записей, на которые нет ссылок из других таблиц и которые старше 24 часов
     * @return Количество удаленных запсией
     */
    int clean();
}
