package com.aplana.sbrf.taxaccounting.service;

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
     * @param realFileName Имя файла что импортируем (валидируем расширение должно быть xml)
     * @param stream       Steam на загруженный фаил
     * @param periodID     id периода
     * @param typeID       0 - Оборотная ведомость по счетам бухгалтерского учёта кредитной организации (Ф-101);
     * @param departmentId Подразделение    @throws IOException, ServiceException
     */
    void importXML(String realFileName, InputStream stream, Integer periodID, int typeID, int departmentId);
}
