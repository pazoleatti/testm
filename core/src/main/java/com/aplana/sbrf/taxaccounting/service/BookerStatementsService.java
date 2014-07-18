package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.BookerStatementsFilter;
import com.aplana.sbrf.taxaccounting.model.BookerStatementsSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

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
    void importXML(String realFileName, InputStream stream, Integer periodID, int typeID, Integer departmentId, TAUserInfo userInfo);

    /**
     * Получение списка бух отчетностей соответсвующих заданному фильтру
     * @param bookerStatementsFilter
     * @return
     */
    PagingResult<BookerStatementsSearchResultItem> findDataByFilter(BookerStatementsFilter bookerStatementsFilter);

    /**
     * Создание бух отчетности
     */
    void create(Logger logger, Integer year, Long periodId, int typeId, Integer departmentId, TAUserInfo userInfo);
}
