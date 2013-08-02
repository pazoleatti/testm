package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.io.InputStream;

/**
 * Сервис для работы с импортируемыми данными.
 */
@ScriptExposed
public interface ImportService {

    /**
     * Получить из потока данных данные для скрипта в виде xml.
     *
     * @param inputStream данные
     * @param format тип файла (csv или xml)
     * @return xml в виде текста
     */
    String getData(InputStream inputStream, String format);

    /**
     * Получить из потока данных все данные для скрипта в виде xml.
     *
     * @param inputStream данные
     * @param format тип файла (csv или xml)
     * @param charset кодировка
     * @return xml в виде текста
     */
    String getData(InputStream inputStream, String format, String charset);

    /**
     * Получить из потока данных только нужные данные в виде xml.
     *
     * @param inputStream данные
     * @param format тип файла (csv или xml)
     * @param charset кодировка
     * @param startStr начало таблицы (например, шапка первой колонки)
     * @param endStr конец табцицы (например, надпись "итого" или значения после таблицы "руководитель, фио")
     * @param headRow количество строк в шапке таблицы
     * @return xml в виде текста
     */
    String getData(InputStream inputStream, String format, String charset,
                   String startStr, String endStr, Integer headRow);
}
