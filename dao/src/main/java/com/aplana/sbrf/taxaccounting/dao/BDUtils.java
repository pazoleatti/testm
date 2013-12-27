package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

/**
 * Интерфейс для класса реализуещего работу
 * связанной с не совместимостью тестового бд(hsqlbd) и
 * бд на продакшене(oracle)
 * @author auldanov
 */
public interface BDUtils {

    enum Sequence {
        DATA_ROW("seq_data_row"),
        FORM_COLUMN("seq_form_column");

        private Sequence(String name) {
            this.name = name;
        }

        private final String name;

        public String getName() {
            return name;
        }
    }

    /**
     * Метод возвращает список зарезервированных id для таблицы data_row
     */
    public List<Long> getNextDataRowIds(Long count);

    /**
     * Метод возвращает список зарезервированных id
     * по которым можно осуществлять вставку в таблицу
     *
     * Размещена здесь, так как использует вызов хранимки,
     * которая отказывается наботать в hsql
     *
     * @param sequence Последовательность
     * @param count Необходимое количество
     * @return Идентификаторы из последовательности
     */
    List<Long> getNextIds(Sequence sequence, Long count);
}
