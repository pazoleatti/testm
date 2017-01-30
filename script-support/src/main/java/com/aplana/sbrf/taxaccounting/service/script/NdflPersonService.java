package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncomeByDate;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncomeCommonValue;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
@ScriptExposed
public interface NdflPersonService {

    /**
     * Создает новую запись о доходах ФЛ привязанную к ПНФ
     *
     * @param ndflPerson фл
     * @return
     */
    Long save(NdflPerson ndflPerson);

    /**
     * Получить запись с данными о доходах
     *
     * @param ndflPersonId
     * @return
     */
    NdflPerson get(Long ndflPersonId);

    /**
     * Найти все данные о доходах физ лица привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPerson> findNdflPerson(long declarationDataId);

    /**
     * Найти NdflPerson привязанные к декларации для построения отчета.  Если найдено больше 1 запись, метод выкидывает исключение ServiceExeption
     * @param declarationDataId идентификатор декларации
     * @param subreportParameters заданные параметры отчета для поиска NdflPerson
     * @return NdflPerson или исключение если найденно больше одной записи
     */
    PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> subreportParameters);

    /**
     * Найти обобщенные данные о доходах физ лиц и данные в разрезе ставок
     * @param declarationDataId - идентификатор декларации
     * @param startDate - "Дата удержания налога" и "Дата платежного поручения" должны быть >= даты начала отчетного периода
     * @param endDate - "Дата удержания налога" и "Дата платежного поручения" должны быть <= даты окончания отчетного периода
     * @return
     */
    NdflPersonIncomeCommonValue findNdflPersonIncomeCommonValue(long declarationDataId, Date startDate, Date endDate);

    /**
     * Найти данные о доходах физ лиц в разрезе дат
     * @param declarationDataId - идентификатор декларации
     * @param calendarStartDate - "Дата удержания налога" и "Дата платежного поручения" должны быть >= даты начала последнего квартала отчетного периода
     * @param endDate - "Дата удержания налога" и "Дата платежного поручения" <= даты окончания последнего квартала отчетного периода
     * @return
     */
    List<NdflPersonIncomeByDate> findNdflPersonIncomeByDate(long declarationDataId, Date calendarStartDate, Date endDate);

    /**
     * Удаляет все данные о физлицах из декларации
     *
     * @param declarationDataId
     */
    void deleteAll(long declarationDataId);
}
