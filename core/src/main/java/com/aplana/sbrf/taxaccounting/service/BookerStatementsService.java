package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.InputStream;

/**
 * Сервис для формы "Загрузка бухгалтерской отчётности из xls"
 *
 * @author Stanislav Yasinskiy
 */
public interface BookerStatementsService {

    /**
     * Загрузка бух отчетности
     *
     * @param realFileName Имя файла что импортируем
     * @param stream       Steam на загруженный фаил
     * @param accountPeriodId     идентификатор периода и подразделения БО
     * @param typeId       0 - Оборотная ведомость по счетам бухгалтерского учёта кредитной организации (Ф-101);
     * @param departmentId Подразделение // TODO (Ramil Timerbaev)
     * @throws IOException, ServiceException
     */
    void importData(String realFileName, InputStream stream, Integer accountPeriodId, int typeId, Integer departmentId, TAUserInfo userInfo);

    /**
     * Получение списка бух отчетностей соответсвующих заданному фильтру
     * @param bookerStatementsFilter
     * @param tAUser
     * @return
     */
    PagingResult<BookerStatementsSearchResultItem> findDataByFilter(BookerStatementsFilter bookerStatementsFilter, TAUser tAUser);

    /**
     * Создание бух отчетности.
     *
     * @param logger логгер
     * @param year год
     * @param periodId идентификатор периода БО из справочника 106 "Коды, определяющие период бухгалтерской отчетности"
     * @param typeId 0 - Оборотная ведомость по счетам бухгалтерского учёта кредитной организации (Ф-101); иначе формы 102
     * @param departmentId идентификатор подразделения
     * @param userInfo информация о пользователе
     */
    void create(Logger logger, Integer year, Long periodId, int typeId, Integer departmentId, TAUserInfo userInfo);
}
