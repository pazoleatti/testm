package com.aplana.sbrf.taxaccounting.util;

import java.sql.Connection;
import java.util.List;

/**
 * Интерфейс для класса реализуещего работу
 * связанной с не совместимостью тестового бд(hsqlbd) и
 * бд на продакшене(oracle)
 * @author auldanov
 */
public interface BDUtils {

    enum Sequence {
		FORM_DATA_NNN("seq_form_data_nnn"),
        FORM_COLUMN("seq_form_column"),
        REF_BOOK_RECORD("seq_ref_book_record"),
        REF_BOOK_RECORD_ROW("seq_ref_book_record_row_id"),
        REF_BOOK_OKTMO("seq_ref_book_oktmo"),
        DECLARATION_SUBREPORT("seq_declaration_subreport");

        private Sequence(String name) {
            this.name = name;
        }

        private final String name;

        public String getName() {
            return name;
        }
    }

    /**
     * Метод возвращает список зарезервированных id для таблицы seq_form_data_nnn
     */
    List<Long> getNextDataRowIds(Long count);

    /**
     * Метод возвращает список зарезервированных id для таблицы ref_book_record
     */
    List<Long> getNextRefBookRecordIds(Long count);

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

    /**
     * Получение текущего соединения
     * @return
     */
    Connection getConnection();

    /**
     * Проверяет соседиенение с БД
     */
    void checkConnection();
}
