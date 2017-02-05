package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ndfl.*;
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
     * Пакетное обновление ссылок NdflPerson.personId на справочник физлиц
     *
     * @param referenceMap карта [NdflPerson.id, RefBookPerson.id]
     */
    int[] updatePersonRefBookReferences(Map<Long, Long> referenceMap);

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
     * Найти все "Сведения о доходах физического лица" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonIncome> findNdflPersonIncome(long declarationDataId);

    /**
     * Найти все "Стандартные, социальные и имущественные налоговые вычеты" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonDeduction> findNdflPersonDeduction(long declarationDataId);

    /**
     * Найти все "Cведения о доходах в виде авансовых платежей" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonPrepayment> findNdflPersonPrepayment(long declarationDataId);

    /**
     * Найти NdflPerson привязанные к декларации для построения отчета.  Если найдено больше 1 запись, метод выкидывает исключение ServiceExeption
     *
     * @param declarationDataId   идентификатор декларации
     * @param subreportParameters заданные параметры отчета для поиска NdflPerson
     * @return NdflPerson или исключение если найденно больше одной записи
     */
    PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> subreportParameters);

    /**
     * Найти обобщенные данные о доходах физ лиц и данные в разрезе ставок
     *
     * @param declarationDataId - идентификатор декларации
     * @param startDate         - "Дата удержания налога" и "Дата платежного поручения" должны быть >= даты начала отчетного периода
     * @param endDate           - "Дата удержания налога" и "Дата платежного поручения" должны быть <= даты окончания отчетного периода
     * @return
     */
    NdflPersonIncomeCommonValue findNdflPersonIncomeCommonValue(long declarationDataId, Date startDate, Date endDate);

    /**
     * Найти данные о доходах физ лиц в разрезе дат
     *
     * @param declarationDataId - идентификатор декларации
     * @param calendarStartDate - "Дата удержания налога" и "Дата платежного поручения" должны быть >= даты начала последнего квартала отчетного периода
     * @param endDate           - "Дата удержания налога" и "Дата платежного поручения" <= даты окончания последнего квартала отчетного периода
     * @return
     */
    List<NdflPersonIncomeByDate> findNdflPersonIncomeByDate(long declarationDataId, Date calendarStartDate, Date endDate);

    /**
     * Удаляет все данные о физлицах из декларации
     *
     * @param declarationDataId
     */
    void deleteAll(long declarationDataId);

    /**
     * Найти данные о доходах ФЛ по идентификатору ФЛ
     *
     * @param ndflPersonId
     * @param startDate    - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate      - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @return
     */
    List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate);

    /**
     * Найти данные о вычетах ФЛ по идентификатору ФЛ
     *
     * @param ndflPersonId
     * @param startDate    - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate      - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @return
     */
    List<NdflPersonDeduction> findDeductionsByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate);

    /**
     * Найти данные о авансах ФЛ по идентификатору ФЛ
     *
     * @param ndflPersonId
     * @param startDate    - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate      - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @return
     */
    List<NdflPersonPrepayment> findPrepaymentsByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate);


    /**
     * Найти все NdflPerson по заданным параметрам
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters        карта наименований параметров и значений
     * @return результат запроса
     * @startIndex - стартовый индекс
     * @pageSize - размер страницы
     */
    public PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> parameters, int startIndex, int pageSize);


    /**
     * Возвращает количество Физлиц для декларации
     * @param declarationDataId
     * @return
     */
    public int getCountNdflPerson(long declarationDataId);

    /**
     * Найти NdflPerson строки данных о доходах которых соответствуют паре кпп и октмо
     * @param declarationDataId
     * @param kpp
     * @param oktmo
     * @return
     */
    List<NdflPerson> findNdflPersonByPairKppOktmo(long declarationDataId, String kpp, String oktmo);
}
