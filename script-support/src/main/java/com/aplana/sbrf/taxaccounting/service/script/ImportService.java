package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

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
}
