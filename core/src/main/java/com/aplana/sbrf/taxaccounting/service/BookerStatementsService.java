package com.aplana.sbrf.taxaccounting.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * Сервис для формы "Загрузка бухгалтерской отчётности из xls"
 *
 * @author Stanislav Yasinskiy
 */
public interface BookerStatementsService {

    /**
     * Загрузка бух отчетности из хмл
     *
     * @param stream       файл для загрузки
     * @param periodID     id периода
     * @param typeID       0 - Оборотная ведомость по счетам бухгалтерского учёта кредитной организации (Ф-101);
     * @param departmentId Подразделение
     * @throws IOException
     */
    void importXML(InputStream stream, Integer periodID, int typeID, int departmentId) throws IOException;
}
