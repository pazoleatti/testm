package com.aplana.sbrf.taxaccounting.dao.ndfl;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonDeductionFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonIncomeFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonPrepaymentFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
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
     * @param ndflPersonList
     * @return
     */
    int[] updateRefBookPersonReferences(List<NaturalPerson> ndflPersonList);

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
     * Найти количество записей данных НДФЛ ФЛ привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблицы NDFL_PERSON
     */
    int findPersonCount(long declarationDataId);

    /**
     * Найти все "Сведения о доходах физического лица" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonIncome> findPersonIncome(long declarationDataId);

    /**
     * Найти количество записей данных о доходах и НДФЛ привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблиц NDFL_PERSON и NDFL_PERSON_INCOME
     */
    int findPersonIncomeCount(long declarationDataId);

    /**
     * Найти все NdflPersonIncome по заданным параметрам
     *
     * @param declarationDataId      идентификатор декларации
     * @param ndflPersonIncomeFilter параметры фильтра
     * @param pagingParams           параметры вывода результата
     * @return результат запроса
     */
    public PagingResult<NdflPersonIncome> findPersonIncomeByParameters(long declarationDataId, NdflPersonIncomeFilter ndflPersonIncomeFilter, PagingParams pagingParams);

    /**
     * Найти все "Стандартные, социальные и имущественные налоговые вычеты" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonDeduction> findNdflPersonDeduction(long declarationDataId);

    /**
     * Найти количество записей данных о вычетах привязанных к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    int findPersonDeductionsCount(long declarationDataId);

    /**
     * Найти все NdflPersonDeduction по заданным параметрам
     *
     * @param declarationDataId         идентификатор декларации
     * @param ndflPersonDeductionFilter параметры фильтра
     * @param pagingParams              параметры вывода результата
     * @return результат запроса
     */
    public PagingResult<NdflPersonDeduction> findPersonDeductionByParameters(long declarationDataId, NdflPersonDeductionFilter ndflPersonDeductionFilter, PagingParams pagingParams);

    /**
     * Найти все "Cведения о доходах в виде авансовых платежей" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonPrepayment> findNdflPersonPrepayment(long declarationDataId);

    /**
     * Найти количество записей данных о доходах в виде авансовых платежей привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return количество записей в списке заполненном данными из таблиц NDFL_PERSON и NDFL_PERSON_PREPAYMENT
     */
    int findPersonPrepaymentCount(long declarationDataId);

    /**
     * Найти все NdflPersonPrepayment по заданным параметрам
     *
     * @param declarationDataId          идентификатор декларации
     * @param ndflPersonPrepaymentFilter параметры фильтра
     * @param pagingParams               параметры вывода результата
     * @return результат запроса
     */
    public PagingResult<NdflPersonPrepayment> findPersonPrepaymentByParameters(long declarationDataId, NdflPersonPrepaymentFilter ndflPersonPrepaymentFilter, PagingParams pagingParams);

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
     * @param startDate        - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate          - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @return
     */
    List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdList(List<Long> ndflPersonIdList, Date startDate, Date endDate);

    /**
     * Найти данные о доходах по КПП и ОКТМО для Физлица
     *
     * @param ndflPersonId
     * @param kpp
     * @param oktmo
     * @return
     */
    List<NdflPersonIncome> findIncomesForPersonByKppOktmo(List<Long> ndflPersonId, String kpp, String oktmo);

    /**
     * Найти данные о доходах по КПП и ОКТМО для Физлица
     *
     * @param ndflPersonId
     * @param kpp
     * @param oktmo
     * @param startDate
     * @param endDate
     * @return
     */
    List<NdflPersonIncome> findIncomesForPersonByKppOktmoAndPeriod(List<Long> ndflPersonId, String kpp, String oktmo, Date startDate, Date endDate);

    /**
     * Найти данные о доходах ФЛ по идентификатору и интервалу. Отбор происходит по дате начиления дохода
     *
     * @param ndflPersonId
     * @param startDate    - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate      - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param prFequals1   = true для НДФЛ(1) prFequals1 = false для НДФЛ(2)
     * @return
     */
    List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1);

    /**
     * Метод делает то же что и findIncomesByPeriodAndNdflPersonId, только в рамках временного решения для https://conf.aplana.com/pages/viewpage.action?pageId=27176125 п.24.3
     * нет обора по условию .Раздел 2.Графа 10 ≠ 0
     *
     * @param ndflPersonId
     * @param startDate    - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate      - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param prFequals1   = true для НДФЛ(1) prFequals1 = false для НДФЛ(2)
     * @return
     */
    List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdTemp(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1);

    /**
     * Найти данные о доходах ФЛ по идентификатору и интервалу. Отбор происходит по дате НДФЛ
     *
     * @param ndflPersonId
     * @param startDate    - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate      - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @return
     */
    List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdAndTaxDate(long ndflPersonId, Date startDate, Date endDate);

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
     * @param prFequals1   - является ли признакФ равным 1, для формы 2-НДФЛ
     * @return
     */
    List<NdflPersonDeduction> findDeductionsWithDeductionsMarkNotOstalnie(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1);

    /**
     * Найти данные о авансах ФЛ по идентификатору ФЛ
     *
     * @param ndflPersonId
     * @param startDate    - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate      - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param prFequals1   - является ли признакФ равным 1, для формы 2-НДФЛ
     * @return
     */
    List<NdflPersonPrepayment> findPrepaymentsByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1);

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
     * Найти количество NdflPerson по заданным параметрам
     * @param declarationDataId
     * @param parameters
     * @return
     */
    public int findNdflPersonCountByParameters(long declarationDataId, Map<String, Object> parameters);

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
     * Найти NdflPerson строки данных о доходах которых соответствуют парам кпп и октмо
     *
     * @param declarationDataId
     * @param kpp
     * @param oktmo
     * @return
     */
    List<NdflPerson> findNdflPersonByPairKppOktmo(List<Long> declarationDataId, String kpp, String oktmo, boolean is2Ndfl2);

    /**
     * Найти доходы из КНФ которая является источником для ОНФ 2-НДФЛ
     *
     * @param declarationDataId идентификатор ОНФ для которой необходимо найти строки из КНФ
     * @param kpp               КПП ОНФ
     * @param oktmo             ОКТМО ОНФ
     * @return
     */
    List<NdflPersonIncome> findNdflPersonIncomeConsolidatedRNU2Ndfl(long declarationDataId, String kpp, String oktmo);

    /**
     * Найти доходы из КНФ которая является источником для ОНФ 6-НДФЛ
     *
     * @param declarationDataId идентификатор ОНФ для которой необходимо найти строки из КНФ
     * @param kpp               КПП ОНФ
     * @param oktmo             ОКТМО ОНФ
     * @return
     */
    List<NdflPersonIncome> findNdflPersonIncomeConsolidatedRNU6Ndfl(long declarationDataId, String kpp, String oktmo);

    /**
     * Найти вычеты для определенной операции
     *
     * @param ndflPersonId
     * @param operationId
     * @return
     */
    List<NdflPersonDeduction> findDeductionsByNdflPersonAndOperation(long ndflPersonId, String operationId);

    /**
     * Найти авансы для определенной операции
     *
     * @param ndflPersonId
     * @param operationId
     * @return
     */
    List<NdflPersonPrepayment> findPrepaymentsByNdflPersonAndOperation(long ndflPersonId, String operationId);

    /**
     * Найти авансы для определенной операции
     *
     * @param operationId ключ операции в БД, а не поле ид операции
     * @return
     */
    List<NdflPersonPrepayment> findPrepaymentsByOperationList(List<String> operationId);

    /**
     * Найти доход по идентификатору
     *
     * @param id
     * @return
     */
    NdflPersonIncome getIncome(long id);

    /**
     * Найти вычет по идентификатору
     *
     * @param id
     * @return
     */
    NdflPersonDeduction getDeduction(long id);

    /**
     * Найти аванс по идентификатору
     *
     * @param id
     * @return
     */
    NdflPersonPrepayment getPrepayment(long id);

    /**
     * Найти Физлиц по списку id
     *
     * @param ndflPersonIdList
     * @return
     */
    List<NdflPerson> findByIdList(List<Long> ndflPersonIdList);

    /**
     * Поиск дублей по полю rownum
     *
     * @param tableName
     * @param declarationDataId
     * @return
     */
    List<Integer> findDublRowNum(String tableName, Long declarationDataId);

    /**
     * Поиск дублей по полю rownum
     * @param tableName
     * @param declarationDataId
     * @return
     */
    Map<Long, List<Integer>> findDublRowNumMap(String tableName, Long declarationDataId);

    /**
     * Поиск пропусков по полю rownum
     *
     * @param tableName
     * @param declarationDataId
     * @return
     */
    List<Integer> findMissingRowNum(String tableName, Long declarationDataId);

    /**
     * Поиск пропусков по полю rownum
     * @param tableName
     * @param declarationDataId
     * @return
     */
    Map<Long, List<Integer>> findMissingRowNumMap(String tableName, Long declarationDataId);

}
