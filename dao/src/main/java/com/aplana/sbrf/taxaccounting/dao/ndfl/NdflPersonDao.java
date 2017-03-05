package com.aplana.sbrf.taxaccounting.dao.ndfl;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO интерфейс для работы с формой РНУ-НДФЛ
 *
 * @author Andrey Drunk
 */
public interface NdflPersonDao {

    /**
     * Найти NdflPerson по идентификатору, возвращает объект с заполненными данными о доходах
     */
    NdflPerson get(long ndflPersonId);

    /**
     * Пакетное обновление ссылок NdflPerson.personId на справочник физлиц
     *
     * @param ndflPersonList список объектов для обновления
     */
    int[] updatePersonRefBookReferences(List<NdflPerson> ndflPersonList);

    /**
     * Создание новой записи ndflPerson, также создаются все потомки (incomes, deductions, prepayments)
     *
     * @param ndflPerson
     * @return идентификатор созданной записи
     */
    Long save(NdflPerson ndflPerson);

    /**
     * Удалить данные по указанному ФЛ из декларации, каскадное удаление
     *
     * @param ndflPersonId
     */
    void delete(Long ndflPersonId);

    /**
     * Найти все данные НДФЛ физ лица привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return список NdflPerson заполненый данными из таблицы NDFL_PERSON, incomes, deductions и prepayments в этом методе не заполняются
     */
    List<NdflPerson> findPerson(long declarationDataId);

    /**
     * Найти все "Сведения о доходах физического лица" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonIncome> findPersonIncome(long declarationDataId);

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
     * Найти данные о доходах ФЛ
     *
     * @param ndflPersonId
     * @return
     */
    List<NdflPersonIncome> findIncomes(long ndflPersonId);

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
     *  Найти данные о доходах по КПП и ОКТМО для Физлица
     * @param ndflPersonId
     * @param kpp
     * @param oktmo
     * @return
     */
    List<NdflPersonIncome> findIncomesForPersonByKppOktmo(long ndflPersonId, String kpp, String oktmo);

    /**
     * Найти данные о доходах ФЛ по идентификатору и интервалу
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
     * @param pagingParams      параметры вывода результата
     * @return результат запроса
     */
    public PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> parameters, PagingParams pagingParams);

    /**
     * @param sqlQuery
     * @param parameters
     * @return
     */
    int getCount(String sqlQuery, Map<String, Object> parameters);

    /**
     * Данные об авансах Физлиц
     *
     * @param ndflPersonIdList
     * @return
     */
    List<NdflPersonPrepayment> findPrepaymentsByNdflPersonIdList(List<Long> ndflPersonIdList);

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
     * Найти NdflPerson строки данных о доходах которых соответствуют паре кпп и октмо
     * @param declarationDataId
     * @param kpp
     * @param oktmo
     * @return
     */
    List<NdflPerson> findNdflPersonByPairKppOktmo(long declarationDataId, String kpp, String oktmo);

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
