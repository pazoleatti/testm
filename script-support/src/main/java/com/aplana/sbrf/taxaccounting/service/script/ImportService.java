package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;

import java.io.IOException;
import java.io.InputStream;

/**
 * Сервис для работы с импортируемыми данными.
 */
@ScriptExposed
public interface ImportService {

    /**
     * Получить из потока данных все данные в виде xml.
     *
     * @param inputStream данные
     * @param fileName имя файла
     * @return xml в виде текста
     */
    String getData(InputStream inputStream, String fileName) throws IOException;

    /**
     * Получить из потока данных все данные в виде xml.
     *
     * @param inputStream данные
     * @param fileName имя файла
     * @param charset кодировка
     * @return xml в виде текста
     */
    String getData(InputStream inputStream, String fileName, String charset) throws IOException;

    /**
     * Получить из потока данных только нужные данные в виде xml.
     *
     * @param inputStream данные
     * @param fileName имя файла
     * @param charset кодировка
     * @param startStr начало таблицы (например, шапка первой колонки)
     * @param endStr конец табцицы (например, надпись "итого" или значения после таблицы "руководитель, фио")
     * @return xml в виде текста
     */
    String getData(InputStream inputStream, String fileName, String charset, String startStr, String endStr) throws IOException;

    /**
     * Получить из потока данных только нужные данные в виде xml.
     *
     * @param inputStream данные
     * @param fileName имя файла
     * @param charset кодировка
     * @param startStr начало таблицы (например, шапка первой колонки)
     * @param endStr конец табцицы (например, надпись "итого" или значения после таблицы "руководитель, фио")
     * @param columnsCount количество колонок в таблице
     * @return xml в виде текста
     */
    String getData(InputStream inputStream, String fileName, String charset, String startStr, String endStr, Integer columnsCount) throws IOException;

    /**
     * Получить из потока данных только нужные данные в виде xml.
     *
     * @param inputStream данные
     * @param fileName имя файла
     * @param charset кодировка
     * @param startStr начало таблицы (например, шапка первой колонки)
     * @param endStr конец табцицы (например, надпись "итого" или значения после таблицы "руководитель, фио")
     * @param columnsCount количество колонок в таблице
     * @param headerRowCount количество строк в шапке
     * @return xml в виде текста
     */
    String getData(InputStream inputStream, String fileName, String charset, String startStr, String endStr,
                   Integer columnsCount, Integer headerRowCount) throws IOException;
    /**
     * Выполняет указанную логику в новой транзакции
     * @param logic код выполняемый в транзакции
     */
    void executeInNewTransaction(TransactionLogic logic);

    /**
     * Выполняет указанную логику в новой транзакции. Вовращает результат
     * @param logic код выполняемый в транзакции
     */
    <T> T returnInNewTransaction(TransactionLogic<T> logic);
}
