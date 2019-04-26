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

import java.math.BigDecimal;
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
     * @deprecated реализация на уровне DAO содержит ошибку, использование метода требует сначала починки
     */
    @Deprecated
    Long save(NdflPerson ndflPerson);

    /**
     * Создание многих записей ФЛ с доходами, привязанных к НФ.
     */
    void save(Collection<NdflPerson> ndflPersons);

    /**
     * Получить запись с данными о доходах
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
     * Возвращяет строки раздела 1 формы, у которых есть операции, относящиеся к заданной паре КПП/ОКТМО и
     * у которых хотя бы у одной строки дата начиления дохода принадлежит отчетному периоду,
     * вместе со всеми этими операциями.
     *
     * @param declarationId идентификатор формы
     * @param kpp           КПП
     * @param oktmo         ОКТМО
     * @param startDate     начало периода
     * @param endDate       окончание периода
     * @return список строк раздела2
     */
    List<NdflPerson> findAllFor2Ndfl(long declarationId, String kpp, String oktmo, Date startDate, Date endDate);

    /**
     * Возвращает количество Физлиц для декларации
     *
     * @param declarationDataId
     * @return
     */
    int getCountNdflPerson(long declarationDataId);

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
     *
     * @param ndflPersonList список записей раздела 1 которым нужно заполнить поля раздела 2
     */
    void fillNdflPersonIncomeSortFields(List<NdflPerson> ndflPersonList);

    /**
     * Сгенерировать идентификаторы для операций из узла СведДох из ТФ xml
     * @param count количество идентификаторов которое должно быть сгенерированно
     * @return список сгенерированных идентификаторов
     */
    List<BigDecimal> generateOperInfoIds(int count);
}

