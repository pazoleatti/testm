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
     */
    int[] updatePersonRefBookReferences(List<NdflPerson> ndflPersonList);

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
     * Найти данные о доходах по КПП и ОКТМО для Физлица
     *
     * @param ndflPersonId
     * @param kpp
     * @param oktmo
     * @return
     */
    List<NdflPersonIncome> findIncomesForPersonByKppOktmo(long ndflPersonId, String kpp, String oktmo);

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
     * Найти данный о вычетах
     *
     * @param ndflPersonId
     * @return
     */
    List<NdflPersonDeduction> findDeductions(long ndflPersonId);

    /**
     * Найти данные о авансовых платежах
     *
     * @param ndflPersonId
     * @return
     */
    List<NdflPersonPrepayment> findPrepayments(long ndflPersonId);

    /**
     * Найти NdflPerson привязанные к декларации для построения отчета.  Если найдено больше 1 запись, метод выкидывает исключение ServiceExeption
     *
     * @param declarationDataId   идентификатор декларации
     * @param subreportParameters заданные параметры отчета для поиска NdflPerson
     * @return NdflPerson или исключение если найденно больше одной записи
     */
    PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> subreportParameters);

    /**
     * Удаляет все данные о физлицах из декларации
     *
     * @param declarationDataId
     */
    void deleteAll(long declarationDataId);

    /**
     * Найти данные о доходах ФЛ по идентификатору интервалу
     *
     * @param ndflPersonId
     * @param startDate    - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate      - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param prFequals1 = true для НДФЛ(1) prFequals1 = false для НДФЛ(2)
     * @return
     */
    List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1);

    /**
     * Найти данные о вычетах ФЛ с признаком вычета "Остальные"
     *
     * @param ndflPersonId
     * @param startDate    - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate      - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @return
     */
    List<NdflPersonDeduction> findDeductionsWithDeductionsMarkOstalnie(long ndflPersonId, Date startDate, Date endDate);

    /**
     * Найти данные о вычетах ФЛ с признаком вычета Социльный;Стандартный;Имущественный;Инвестиционный
     *
     * @param ndflPersonId
     * @param startDate    - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate      - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @return
     */
    List<NdflPersonDeduction> findDeductionsWithDeductionsMarkNotOstalnie(long ndflPersonId, Date startDate, Date endDate);

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
     *
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
    List<NdflPerson> findNdflPersonByPairKppOktmo(List<Long> declarationDataId, String kpp, String oktmo);

    /**
     * Данные об авансах Физлиц
     *
     * @param ndflPersonIdList
     * @return
     */
    List<NdflPersonPrepayment> findPrepaymentsByNdflPersonIdList(List<Long> ndflPersonIdList);

    /**
     * Найти данные о доходах Физлиц
     *
     * @param ndflPersonIdList
     * @param startDate         - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate           - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @return
     */
    List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdList(List<Long> ndflPersonIdList, Date startDate, Date endDate);

    /**
     * Найти доходы из КНФ которая является источником для ОНФ 2-НДФЛ
     * @param declarationDataId идентификатор ОНФ для которой необходимо найти строки из КНФ
     * @param kpp КПП ОНФ
     * @param oktmo ОКТМО ОНФ
     * @return
     */
    List<NdflPersonIncome> findNdflPersonIncomeConsolidatedRNU2Ndfl(long declarationDataId, String kpp, String oktmo);

    /**
     * Найти доходы из КНФ которая является источником для ОНФ 6-НДФЛ
     * @param declarationDataId идентификатор ОНФ для которой необходимо найти строки из КНФ
     * @param kpp КПП ОНФ
     * @param oktmo ОКТМО ОНФ
     * @return
     */
    List<NdflPersonIncome> findNdflPersonIncomeConsolidatedRNU6Ndfl(long declarationDataId, String kpp, String oktmo);

    /**
     * Найти вычеты для определенной операции
     * @param ndflPersonId
     * @param operationId
     * @return
     */
    List<NdflPersonDeduction> findDeductionsByNdflPersonAndOperation(long ndflPersonId, long operationId);

    /**
     * Найти авансы для определенной операции
     * @param ndflPersonId
     * @param operationId
     * @return
     */
    List<NdflPersonPrepayment> findPrepaymentsByNdflPersonAndOperation(long ndflPersonId, long operationId);

    /**
     * Найти доход по идентификатору
     * @param id
     * @return
     */
    NdflPersonIncome getIncome(long id);

    /**
     * Найти вычет по идентификатору
     * @param id
     * @return
     */
    NdflPersonDeduction getDeduction(long id);

    /**
     * Найти аванс по идентификатору
     * @param id
     * @return
     */
    NdflPersonPrepayment getPrepayment(long id);

    /**
     * Найти Физлиц по списку id
     * @param ndflPersonIdList
     * @return
     */
    List<NdflPerson> findByIdList(List<Long> ndflPersonIdList);
}

