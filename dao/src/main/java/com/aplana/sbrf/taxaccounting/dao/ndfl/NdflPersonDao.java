package com.aplana.sbrf.taxaccounting.dao.ndfl;

import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationIncome;
import com.aplana.sbrf.taxaccounting.model.consolidation.ConsolidationSourceDataSearchFilter;
import com.aplana.sbrf.taxaccounting.model.filter.NdflFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO интерфейс для работы с формой РНУ-НДФЛ
 */
public interface NdflPersonDao {

    /**
     * Найти физлицо по идентификатору
     *
     * @param ndflPersonId идентификатор физлица
     * @return объект физлица с заполненными данными о доходах
     */
    NdflPerson findById(long ndflPersonId);

    /**
     * Пакетное обновление ссылок NdflPerson.personId на справочник физлиц
     *
     * @param ndflPersonList список фл формы
     * @return массив количества обновленных строк для каждого выражения
     */
    int[] updateRefBookPersonReferences(List<NaturalPerson> ndflPersonList);

    /**
     * Создание новой записи ndflPerson, также создаются все потомки (incomes, deductions, prepayments)
     *
     * @param ndflPerson физлицо
     * @return идентификатор созданной записи
     * @deprecated метод нерабочий, требуется починка реализации
     */
    @Deprecated
    Long save(NdflPerson ndflPerson);

    /**
     * Сохранение списка записей ФЛ с доходами, привязанных к НФ.
     */
    void saveAll(final Collection<NdflPerson> ndflPersons);

    /**
     * Удалить данные по указанному ФЛ из декларации, каскадное удаление
     *
     * @param ndflPersonId идентификатор физлица
     */
    void delete(Long ndflPersonId);

    /**
     * Удалить все данные формы
     *
     * @param declarationDataId ид формы
     * @return кол-во удаленных строк
     */
    long deleteByDeclarationId(Long declarationDataId);

    /**
     * Найти физ лица привязанные к налоговой форме
     *
     * @param declarationDataId идентификатор декларации
     * @return список объектов физлиц, incomes, deductions и prepayments в этом методе не заполняются
     */
    List<NdflPerson> findAllByDeclarationId(long declarationDataId);

    /**
     * Возвращает данные по спецотчету по выделенным записям (SBRFNDFL-8445)
     */
    List<NdflPerson> findAllPersonByInpList(List<String> inpList);

    List<NdflPersonIncome> findAllPersonIncomeBySelectedByInpList(List<String> inpList);

    List<NdflPersonDeduction> findAllNdflPersonDeductionBySelectedByInpList(List<String> inpList);

    List<NdflPersonPrepayment> findAllNdflPersonPrepaymentBySelectedByInpList(List<String> inpList);

    /**
     * Возвращяет список строк из раздела 1 по списку ид форм
     *
     * @param declarationDataIds список ид форм
     * @return список строк раздела 1
     */
    List<NdflPerson> findAllByDeclarationIdIn(List<Long> declarationDataIds);

    /**
     * Найти строки раздела 2 "Сведения о доходах и НДФЛ" по списку идентификаторов.
     */
    List<NdflPersonIncome> findAllIncomesByIdIn(List<Long> incomeIds);

    /**
     * Найти все "Сведения о доходах физического лица", привязанные к налоговой форме
     *
     * @param declarationDataId идентификатор налоговой формы
     * @return список объектов доходов физического лица
     */
    List<NdflPersonIncome> findAllIncomesByDeclarationId(long declarationDataId);

    /**
     * Найти все "Сведения о доходах физического лица" привязанные к налоговой форме, отсортированные по rowNum
     *
     * @param declarationDataId идентификатор налоговой формы
     * @return список объектов доходов физического лица
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
     * Найти все NdflPersonIncome по заданным параметрам
     *
     * @param ndflFilter   параметры фильтра
     * @param pagingParams параметры вывода результата
     * @return список объектов DTO доходов
     */
    PagingResult<NdflPersonIncomeDTO> fetchPersonIncomeByParameters(NdflFilter ndflFilter, PagingParams pagingParams);

    /**
     * Возвращяет список строк из раздела 3 по списку ид форм
     *
     * @param declarationDataId ид формы
     * @return список объектов вычетов физического лица
     */
    List<NdflPersonDeduction> findAllDeductionsByDeclarationId(long declarationDataId);

    /**
     * Возвращяет список строк из раздела 3 по списку ид форм
     *
     * @param declarationDataIds списсок ид форм
     * @return список строк раздела 3
     */
    List<NdflPersonDeduction> findAllDeductionsByDeclarationIds(List<Long> declarationDataIds);

    /**
     * Найти все NdflPersonDeduction по заданным параметрам
     *
     * @param ndflFilter   параметры фильтра
     * @param pagingParams параметры вывода результата
     * @return список DTO вычетов.
     */
    PagingResult<NdflPersonDeductionDTO> fetchPersonDeductionByParameters(NdflFilter ndflFilter, PagingParams pagingParams);

    /**
     * Найти все "Cведения о доходах в виде авансовых платежей" привязанные к декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return список авансов
     */
    List<NdflPersonPrepayment> fetchNdflPersonPrepaymentByDeclarationData(long declarationDataId);

    /**
     * Найти все NdflPersonPrepayment по заданным параметрам
     *
     * @param ndflFilter   параметры фильтра
     * @param pagingParams параметры вывода результата
     * @return список DTO авансов
     */
    PagingResult<NdflPersonPrepaymentDTO> fetchPersonPrepaymentByParameters(NdflFilter ndflFilter, PagingParams pagingParams);

    /**
     * Найти данные о доходах ФЛ
     *
     * @param ndflPersonId идентификатор физлица
     * @return список объектов доходов
     */
    List<NdflPersonIncome> fetchNdflPersonIncomeByNdflPerson(long ndflPersonId);

    /**
     * Возвращяет данные о доходах для Физлица по КПП и ОКТМО
     *
     * @param ndflPersonId список идентификаторов физического лица
     * @param kpp          кпп
     * @param oktmo        октмо
     * @return список объектов доходов физического лица
     */
    List<NdflPersonIncome> fetchNdflPersonIncomeByNdflPersonKppOktmo(List<Long> ndflPersonId, String kpp, String oktmo);

    /**
     * Возвращяет строки раздела 1 формы, у которых есть операции, относящиеся к заданной паре КПП/ОКТМО и
     * у которых хотя бы у одной строки дата начиления дохода принадлежит отчетному периоду,
     * вместе со всеми стркоами разделов 2-4 этих операций.
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
     * Найти все NdflPerson по заданным параметрам
     *
     * @param declarationDataId идентификатор декларации
     * @param parameters        карта наименований параметров и значений
     * @param pagingParams      параметры вывода результата
     * @return результат запроса
     */
    PagingResult<NdflPerson> fetchNdflPersonByParameters(long declarationDataId, Map<String, Object> parameters, PagingParams pagingParams);

    /**
     * Найти все NdflPerson по заданным параметрам
     *
     * @param ndflFilter   значения фильтра
     * @param pagingParams параметры вывода результата
     * @return результат запроса
     */
    PagingResult<NdflPerson> fetchNdflPersonByParameters(NdflFilter ndflFilter, PagingParams pagingParams);

    /**
     * Найти количество NdflPerson по заданным параметрам
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param parameters        параметры поиска
     * @return количествонайденных записей
     */
    int getNdflPersonCountByParameters(long declarationDataId, Map<String, Object> parameters);

    /**
     * количество записей по парметрам
     *
     * @param sqlQuery   sql запрос
     * @param parameters параметры
     * @return количество найденных записей
     */
    int getCount(String sqlQuery, Map<String, Object> parameters);

    /**
     * Найти данные о вычетах
     *
     * @param ndflPersonId идентификатор физического лица
     * @return список объектов вычетов
     */
    List<NdflPersonDeduction> fetchNdflPersonDeductionByNdflPerson(long ndflPersonId);

    /**
     * Найти данные о авансовых платежах
     *
     * @param ndflPersonId идентификатор физического лица
     * @return список объектов авансов
     */
    List<NdflPersonPrepayment> fetchNdflPersonPrepaymentByNdflPerson(long ndflPersonId);

    /**
     * Найти вычеты для определенной операции
     *
     * @param ndflPersonId идентификатор физического лица
     * @param operationId  идентификатор операции
     * @return список объектов вычетов
     */
    List<NdflPersonDeduction> fetchNdflPersonDeductionByNdflPersonAndOperation(long ndflPersonId, String operationId);

    /**
     * Найти авансы для определенной операции
     *
     * @param ndflPersonId идентификатор физического лица
     * @param operationId  идентификатор операции
     * @return список объектов авансов
     */
    List<NdflPersonPrepayment> fetchNdflPeronPrepaymentByNdflPersonAndOperation(long ndflPersonId, String operationId);

    /**
     * Найти доход по идентификатору
     *
     * @param id идентификатор строки операции дохода
     * @return объект дохода
     */
    NdflPersonIncome fetchOneNdflPersonIncome(long id);

    /**
     * Найти вычет по идентификатору
     *
     * @param id идентификатор строки операции вычета
     * @return объект вычета
     */
    NdflPersonDeduction fetchOneNdflPersonDeduction(long id);

    /**
     * Найти аванс по идентификатору
     *
     * @param id идентификатор строки операции аванса
     * @return объект аванса
     */
    NdflPersonPrepayment fetchOneNdflPersonPrepayment(long id);

    /**
     * Найти Физлиц по списку id
     *
     * @param ndflPersonIdList список идентификаторов физического лица
     * @return список физических лиц
     */
    List<NdflPerson> findByIdIn(List<Long> ndflPersonIdList);

    /**
     * Поиск дублей по полю rownum
     *
     * @param tableName         наименование таблицы
     * @param declarationDataId идентификатор налоговой формы
     * @return возвращает значение row_num
     */
    List<Integer> fetchDublByRowNum(String tableName, Long declarationDataId);

    /**
     * Поиск дублей по полю rownum
     *
     * @param tableName         наименование таблицы
     * @param declarationDataId идентификатор налоговой формы
     * @return возвращает значение row_num
     */
    Map<Long, List<Integer>> fetchDublByRowNumMap(String tableName, Long declarationDataId);

    /**
     * Поиск пропусков по полю rownum
     *
     * @param tableName         наименование таблицы
     * @param declarationDataId идентификатор налоговой формы
     * @return возвращает значение row_num
     */
    List<Integer> findMissingRowNum(String tableName, Long declarationDataId);

    /**
     * Поиск пропусков по полю rownum
     *
     * @param tableName         наименование таблицы
     * @param declarationDataId идентификатор налоговой формы
     * @return возвращает значение row_num
     */
    Map<Long, List<Integer>> findMissingRowNumMap(String tableName, Long declarationDataId);

    /**
     * Получить число ФЛ в NDFL_PERSON
     *
     * @param declarationDataId идентификатор налоговой формы
     * @return число ФЛ
     */
    int getNdflPersonCount(Long declarationDataId);

    /**
     * Получить максимальное количество элементов среди всех разделов конкретной декларации
     * (NDFL_PERSON, NDFL_PERSON_INCOME, NDFL_PERSON_DEDUCTION, NDFL_PERSON_PREPAYMEN)
     *
     * @param declarationDataId идентификатор налоговой формы
     * @return максимальное количество элемнтов
     */
    long getNdflPersonAllSectionMaxCount(long declarationDataId);

    /**
     * Получить число справок ФЛ в NDFL_REFERENCES
     *
     * @param declarationDataId идентификатор налоговой формы
     * @return число справок ФЛ
     */
    int getNdflPersonReferencesCount(Long declarationDataId);

    /**
     * Получить число ФЛ в 6НДФЛ
     *
     * @param declarationDataId идентификатор налоговой формы
     * @return число ФЛ
     */
    int get6NdflPersonCount(Long declarationDataId);

    /**
     * Обновляет данные строки для раздела 2 (Сведения о доходах и НДФЛ)
     *  @param personIncome данные строки раздела 2
     * @param taUserInfo   пользователь, выполняющий изменения
     */
    void updateOneNdflIncome(NdflPersonIncome personIncome, TAUserInfo taUserInfo);

    /**
     * Обновляет данные строки для раздела 3 (Сведения о вычетах)
     *  @param personDeduction данные строки раздела 3
     * @param taUserInfo      пользователь, выполняющий изменения
     */
    void updateOneNdflDeduction(NdflPersonDeduction personDeduction, TAUserInfo taUserInfo);

    /**
     * Обновляет данные строки для раздела 4 (Сведения о доходах в виде авансовых платежей)
     *  @param personPrepayment данные строки раздела 4
     * @param taUserInfo       пользователь, выполняющий изменения
     */
    void updateOneNdflPrepayment(NdflPersonPrepayment personPrepayment, TAUserInfo taUserInfo);

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
     * Удалить несколько доходов
     *
     * @param ids список идентификаторов доходов
     */
    void deleteNdflPersonIncomeBatch(List<Long> ids);

    /**
     * Удалить несколько вычетов
     *
     * @param ids список идентификаторов вычетов
     */
    void deleteNdflPersonDeductionBatch(List<Long> ids);

    /**
     * Удалить несколько авансов
     *
     * @param ids список идентификаторов авансов
     */
    void deleteNdflPersonPrepaymentBatch(List<Long> ids);

    /**
     * Удалить несколько фл
     *
     * @param ids список идентификаторов фл
     */
    void deleteNdflPersonBatch(List<Long> ids);

    /**
     * Возвращяет true, если в форме имеются строки раздела 1, иначе false
     *
     * @param declarationDataId ид формы РНУ
     * @return true, если в форме имеются строки раздела 1, иначе false
     */
    boolean ndflPersonExistsByDeclarationId(long declarationDataId);

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
     * @param actualDate        актуальная дата версии
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
     * @param personIdList список идентификаторов физических лиц из справочника
     * @return список объектов физических лиц состояние которых идентично состоянию соответствующих полей в справочнике "Физические лица"
     */
    List<NdflPerson> fetchRefBookPersonsAsNdflPerson(List<Long> personIdList, Date actualDate);

    /**
     * Возвращяет страницу из значений КПП, полученных из строк раздела 2 формы РНУ
     *
     * @param declarationDataId ид формы РНУ
     * @param kpp               фильтр поиска
     * @param pagingParams      данные пагинатора
     * @return страница из значений КПП, полученных из строк раздела 2 формы РНУ
     */
    PagingResult<KppSelect> findAllKppByDeclarationDataId(long declarationDataId, String kpp, PagingParams pagingParams);

    /**
     * Найти строки дохода с определенными ИНП и ИдОперации
     *
     * @param declarationDataId идентификатор налоговой формы к которой относится строка операции
     * @param inp               идентификатор налогоплательщика у строки операции
     * @param operationId       идентификатор операции
     * @return список строк операций дохода соответствующие требуемым критериям
     */
    List<NdflPersonIncome> findDeclarartionDataIncomesWithSameOperationIdAndInp(Long declarationDataId, String inp, String operationId);

    /**
     * Найти записи раздела 1 для операций с определенными ИНП и ИдОперации
     *
     * @param declarationDataId идентификатор налоговой формы к которой относится строка операции
     * @param inp               идентификатор налогоплательщика у строки операции
     * @param operationId       идентификатор операции
     * @return список найденных записей раздела 1
     */
    List<NdflPerson> findDeclarartionDataPersonWithSameOperationIdAndInp(Long declarationDataId, String inp, String operationId);

    /**
     * Найти значение для заполнения графы "Дата операции"
     *
     * @param declarationDataId идентификатор налоговой формы к которой относится строка операции
     * @param inp               идентификатор налогоплательщика у строки операции
     * @param operationId       идентификатор операции
     * @return полученное значение
     */
    Date findOperationDate(Long declarationDataId, String inp, String operationId);

    /**
     * Сгенерировать идентификаторы для операций из узла СведДох из ТФ xml
     * @param count количество идентификаторов которое должно быть сгенерированно
     * @return список сгенерированных идентификаторов
     */
    List<BigDecimal> generateOperInfoIds(int count);
}
