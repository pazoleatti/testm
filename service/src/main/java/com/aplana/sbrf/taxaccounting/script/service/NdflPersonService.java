package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationSourceDataSearchFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.Collection;
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
    int[] updateRefBookPersonReferences(List<NaturalPerson> ndflPersonList);

    /**
     * Создает новую запись о доходах ФЛ привязанную к ПНФ
     *
     * @param ndflPerson фл
     * @return
     */
    Long save(NdflPerson ndflPerson);

    void save(Collection<NdflPerson> ndflPersons);

    /**
     * Получить запись с данными о доходах
     *
     * @param ndflPersonId
     * @return
     */
    NdflPerson get(Long ndflPersonId);

    /**
     * Возвращяет данные только 1ого раздела формы РНУ
     *
     * @param declarationDataId идентификатор формы РНУ
     */
    List<NdflPerson> findNdflPerson(long declarationDataId);

    /**
     * Возвращяет список строк из раздела 1 по списку ид форм
     *
     * @param declarationDataIds список ид форм
     * @return список строк раздела 1
     */
    List<NdflPerson> findAllNdflPersonsByDeclarationIds(List<Long> declarationDataIds);

    /**
     * Возвращяет данные все разделов формы РНУ
     *
     * @param declarationDataId идентификатор формы РНУ
     * @return данные всех 4х разделов формы РНУ
     */
    List<NdflPerson> findNdflPersonWithOperations(long declarationDataId);

    /**
     * Найти все "Сведения о доходах физического лица" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonIncome> findNdflPersonIncome(long declarationDataId);

    /**
     * Найти все "Сведения о доходах физического лица" привязанные к декларации, отсортированные по rowNum
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonIncome> findAllIncomesByDeclarationIdByOrderByRowNumAsc(long declarationDataId);

    /**
     * Возвращяет список строк из раздела 2 по списку ид форм
     *
     * @param declarationDataIds список ид форм
     * @return список строк раздела 2
     */
    List<NdflPersonIncome> findAllIncomesByDeclarationIds(List<Long> declarationDataIds);

    /**
     * Возвращяет данные о доходах для Физлица по КПП и ОКТМО
     *
     * @param ndflPersonId список идентификаторов физического лица
     * @param kpp          кпп
     * @param oktmo        октмо
     * @return список объектов доходов физического лица
     */
    List<NdflPersonIncome> findIncomesForPersonByKppOktmo(List<Long> ndflPersonId, String kpp, String oktmo);

    /**
     * Возвращяет данные о доходах в форме по КПП и ОКТМО
     *
     * @param declarationId идентификатор формы
     * @param kpp           КПП
     * @param oktmo         ОКТМО
     * @return список строк 2 раздела
     */
    List<NdflPersonIncome> findAllIncomesByDeclarationIdAndKppAndOktmo(long declarationId, String kpp, String oktmo);

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
     * Найти все "Стандартные, социальные и имущественные налоговые вычеты" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonDeduction> findNdflPersonDeduction(long declarationDataId);

    /**
     * Возвращяет список строк из раздела 3 по списку ид форм
     *
     * @param declarationDataIds список ид форм
     * @return список строк раздела 3
     */
    List<NdflPersonDeduction> findAllDeductionsByDeclarationIds(List<Long> declarationDataIds);

    /**
     * Найти все "Cведения о доходах в виде авансовых платежей" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPersonPrepayment> findNdflPersonPrepayment(long declarationDataId);

    /**
     * Найти сведения о доходах
     *
     * @param ndflPersonId
     * @return
     */
    List<NdflPersonIncome> findIncomes(long ndflPersonId);

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
     * Найти идентификаторы доходов относящиеся к ФЛ
     *
     * @param ndflPersonId идентификатор фл формы
     * @return список идентификаторов доходов
     */
    List<Long> fetchIncomeIdByNdflPerson(long ndflPersonId);

    /**
     * Найти идентификаторы вычетов относящиеся к ФЛ
     *
     * @param ndflPersonId идентификатор фл формы
     * @return список идентификаторов вычетов
     */
    List<Long> fetchDeductionIdByNdflPerson(long ndflPersonId);

    /**
     * Найти идентификаторы авансов относящиеся к ФЛ
     *
     * @param ndflPersonId идентификатор фл формы
     * @return список идентификаторов авансов
     */
    List<Long> fetchPrepaymentIdByNdflPerson(long ndflPersonId);

    /**
     * Найти NdflPerson привязанные к декларации для построения отчета.  Если найдено больше 1 запись, метод выкидывает исключение ServiceExeption
     *
     * @param declarationDataId   идентификатор декларации
     * @param subreportParameters заданные параметры отчета для поиска NdflPerson
     * @return NdflPerson или исключение если найденно больше одной записи
     */
    PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> subreportParameters);

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
     * Найти количество NdflPerson по заданным параметрам
     *
     * @param declarationDataId
     * @param parameters
     * @return
     */
    public int findNdflPersonCountByParameters(long declarationDataId, Map<String, Object> parameters);

    /**
     * Удаляет все данные о физлицах из декларации
     *
     * @param declarationDataId ид формы
     * @return кол-во удаленных строк
     */
    long deleteAll(long declarationDataId);

    /**
     * Найти данные о доходах ФЛ по идентификатору интервалу. Отбор происходит по дате начиления дохода
     *
     * @param ndflPersonId
     * @param startDate    - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate      - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param prFequals1   = true для НДФЛ(1) prFequals1 = false для НДФЛ(2)
     * @return
     */
    List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate, boolean prFequals1);

    /**
     * Метод делает то же что и fetchNdflPersonIncomeByPeriodNdflPersonId, только в рамках временного решения для https://conf.aplana.com/pages/viewpage.action?pageId=27176125 п.24.3
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
    List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdAndTaxDate(long ndflPersonId, int taxRate, Date startDate, Date endDate);

    /**
     * Найти данные о доходах ФЛ по идентификатору ФЛ и интервалу. Отбор происходит по дате выплаты дохода
     *
     * @param ndflPersonId
     * @param startDate
     * @param endDate
     * @return
     */
    List<NdflPersonIncome> findIncomesByPayoutDate(long ndflPersonId, int taxRate, Date startDate, Date endDate);

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
    List<NdflPersonPrepayment> findPrepaymentsByPeriodAndNdflPersonId(long ndflPersonId, int taxRate, Date startDate, Date endDate, boolean prFequals1);

    /**
     * Возвращает количество Физлиц для декларации
     *
     * @param declarationDataId
     * @return
     */
    public int getCountNdflPerson(long declarationDataId);

    /**
     * Найти NdflPerson строки данных о доходах которых соответствуют паре кпп и октмо
     *
     * @param declarationDataId
     * @param kpp
     * @param oktmo
     * @return
     */
    List<NdflPerson> findNdflPersonByPairKppOktmo(long declarationDataId, String kpp, String oktmo, boolean is2Ndfl2);

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
     * @param startDate        - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate          - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @return
     */
    List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonIdList(List<Long> ndflPersonIdList, Date startDate, Date endDate);

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
     *
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
     *
     * @param tableName
     * @param declarationDataId
     * @return
     */
    Map<Long, List<Integer>> findMissingRowNumMap(String tableName, Long declarationDataId);

    /**
     * Найти идентификаторы операций в указанном диапазоне в алфавитном порядке
     *
     * @param startOperationId начало диапазона
     * @param endOperationId   конец диапазона
     * @return список идентификторов операций
     */
    List<String> fetchIncomeOperationIdRange(String startOperationId, String endOperationId);

    /**
     * Найти идентификаторы доходов по иденитфикатору операции.
     *
     * @param operationIdList список идентификаторов доходов
     * @return список найденных идентификаторов
     */
    List<String> findIncomeOperationId(List<String> operationIdList);

    /**
     * Удалить несколько доходов
     *
     * @param ids список идентификаторов доходов
     */
    void deleteNdflPersonIncome(List<Long> ids);

    /**
     * Удалить несколько вычетов
     *
     * @param ids список идентификаторов вычетов
     */
    void deleteNdflPersonDeduction(List<Long> ids);

    /**
     * Удалить несколько авансов
     *
     * @param ids список идентификаторов авансов
     */
    void deleteNdflPersonPrepayment(List<Long> ids);

    /**
     * Удалить несколько фл
     *
     * @param ids список идентификаторов фл
     */
    void deleteNdflPersonBatch(List<Long> ids);

    /**
     * Возвращяет признак наличия в форме операции (строк 2 раздела)
     *
     * @param declarationDataId ид формы
     * @return признак наличия в форме операции (строк 2 раздела)
     */
    boolean incomeExistsByDeclarationId(long declarationDataId);

    /**
     * Проеряет наличие дохода в налоговой форме
     *
     * @param ndflPersonIncomeId идентификатор дохода
     * @param declarationDataId  идентификатор налоговой формы
     * @return true если существует
     */
    boolean checkIncomeExists(long ndflPersonIncomeId, long declarationDataId);

    /**
     * Проеряет наличие вычета в налоговой форме
     *
     * @param ndflPersonDeductionId идентификатор вычета
     * @param declarationDataId     идентификатор налоговой формы
     * @return true если существует
     */
    boolean checkDeductionExists(long ndflPersonDeductionId, long declarationDataId);

    /**
     * Проеряет наличие аванса в налоговой форме
     *
     * @param ndflPersonPrepaymentId идентификатор авнса
     * @param declarationDataId      идентификатор налоговой формы
     * @return true если существует
     */
    boolean checkPrepaymentExists(long ndflPersonPrepaymentId, long declarationDataId);

    /**
     * Сохраняет доходы
     *
     * @param incomes список объектов доходов
     */
    void saveIncomes(List<NdflPersonIncome> incomes);

    /**
     * Сохраняет вычеты
     *
     * @param deductions список объектов вычетов
     */
    void saveDeductions(List<NdflPersonDeduction> deductions);

    /**
     * Сохраняет авансы
     *
     * @param prepayments список объектов авансов
     */
    void savePrepayments(List<NdflPersonPrepayment> prepayments);

    /**
     * Обновляет физлиц. Этот метод не обновляет доходы вычеты и авансы физлица
     *
     * @param persons список физлиц
     */
    void updateNdflPersons(List<NdflPerson> persons);

    /**
     * Обновляет доходы
     *
     * @param incomes список объектов доходов
     */
    void updateIncomes(List<NdflPersonIncome> incomes);

    /**
     * Обновляет вычеты
     *
     * @param deductions список объектов вычетов
     */
    void updateDeductions(List<NdflPersonDeduction> deductions);

    /**
     * Обновляет авансы
     *
     * @param prepayments список объектов авансов
     */
    void updatePrepayments(List<NdflPersonPrepayment> prepayments);

    /**
     * Изменяет только rowNum у всех строк каждого раздела формы РНУ
     *
     * @param ndflPersons стркои первого раздела формы РНУ
     */
    void updateRowNum(List<NdflPerson> ndflPersons);

    /**
     * Оюновляет номер строки у физлиц
     *
     * @param persons список физлиц
     */
    void updateNdflPersonsRowNum(List<NdflPerson> persons);

    /**
     * Обновляет номер строки у доходов
     *
     * @param incomes список объектов доходов
     */
    void updateIncomesRowNum(List<NdflPersonIncome> incomes);

    /**
     * Обновляет номер строки у вычетов
     *
     * @param deductions список объектов вычетов
     */
    void updateDeductionsRowNum(List<NdflPersonDeduction> deductions);

    /**
     * Обновляет номер строки у авансов
     *
     * @param prepayments список объектов авансов
     */
    void updatePrepaymentsRowNum(List<NdflPersonPrepayment> prepayments);

    /**
     * Найти количество ИНП по физлицам, операции которых попадают в период и сумма их доходов > 0
     *
     * @param ndflPersonIdList список идентификаторов физлиц
     * @param periodStartDate  дата начала периода
     * @param periodEndDate    дата конца периода
     * @return количество найденных ИНП
     */
    int findInpCountWithPositiveIncomeByPersonIdsAndAccruedIncomeDatePeriod(List<Long> ndflPersonIdList, Date periodStartDate, Date periodEndDate);

    /**
     * Находит авансы имеющие ИНП, ИдОперации такие же как и доходы передаваемые в {@code ndflPersonIncomeIdList} и
     * если дата начисления дохода входит в период ограниченный параметрами {@code periodStartDate} и {@code periodEndDate}
     *
     * @param ndflPersonIncomeIdList список идентификаторов доходов на основании которых будет производится выборка авансов
     * @param periodStartDate        дата начала периода
     * @param periodEndDate          дата окончания периода
     * @return список объектов авансов
     */
    List<NdflPersonPrepayment> fetchPrepaymentByIncomesIdAndAccruedDate(List<Long> ndflPersonIncomeIdList, Date periodStartDate, Date periodEndDate);

    /**
     * Получает данные из справочника по физическим лицам и заполняет ими класс модели соответствующий Разделу 1 РНУ НДФЛ - {@code com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson}.
     *
     * @param declarationDataId идентификатор налоговой формы
     * @return список объектов физических лиц состояние которых идентично состоянию соответствующих полей в справочнике "Физические лица"
     */
    List<NdflPerson> fetchRefBookPersonsAsNdflPerson(Long declarationDataId, Date actualDate);

    /**
     * Определяет список операций которые нужно включить в КНФ
     *
     * @param searchData данные для поиска
     * @return список объектов доходов
     */
    List<ConsolidationIncome> fetchIncomeSourcesConsolidation(ConsolidationSourceDataSearchFilter searchData);

    /**
     * Получает список вычетов которые нужно включить в КНФ. Поиск происходит по доходам включенных в КНФ.
     *
     * @param incomeIds идентификаторы операций доходов
     * @return список объектов вычетов
     */
    List<NdflPersonDeduction> fetchDeductionsForConsolidation(List<Long> incomeIds);

    /**
     * Получает список авансов которые нужно включить в КНФ. Поиск происходит по доходам включенных в КНФ.
     *
     * @param incomeIds идентификаторы операций доходов
     * @return список объектов авансов
     */
    List<NdflPersonPrepayment> fetchPrepaymentsForConsolidation(List<Long> incomeIds);

    /**
     * Получает данные из справочника по физическим лицам и заполняет ими класс модели соответствующий Разделу 1 РНУ НДФЛ - {@code com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson}.
     *
     * @param ndflPersonIdList список идентификаторов физических лиц
     * @return список объектов физических лиц состояние которых идентично состоянию соответствующих полей в справочнике "Физические лица"
     */
    List<NdflPerson> fetchRefBookPersonsAsNdflPerson(List<Long> ndflPersonIdList, Date actualDate);

    /**
     * Заполнить поля раздела 2 используемые для сортировки
     * @param ndflPersonList  список записей раздела 1 которым нужно заполнить поля раздела 2
     */
    void fillNdflPersonIncomeSortFields(List<NdflPerson> ndflPersonList);
}

