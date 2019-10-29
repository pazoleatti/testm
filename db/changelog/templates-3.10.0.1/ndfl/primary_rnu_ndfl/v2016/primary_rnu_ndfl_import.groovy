package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogEntry
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.ndfl.*
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.refbook.ReportPeriodImport
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.service.*
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService
import com.aplana.sbrf.taxaccounting.service.util.ExcelImportUtils
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.apache.commons.lang3.StringUtils

import java.text.SimpleDateFormat

import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.checkInterrupted

new Import(this).run()

@SuppressWarnings("GrMethodMayBeStatic")
@TypeChecked
class Import extends AbstractScriptClass {

    final String HEADER_START_VALUE = "Идентификаторы строк разделов РНУ НДФЛ";
    final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"
    final String LOG_TYPE_PERSON_MSG_2 = "Значение гр. \"%s\" (\"%s\") отсутствует в справочнике \"%s\""
    final String LOG_TYPE_REFERENCES = "Значение не соответствует справочнику \"%s\""
    final String EMPTY_REQUIRED_FIELD = "Строка %s. Значение гр. %s не указано."
    final String C_INCOME_CODE = "Код дохода"
    final String R_INCOME_CODE = "Коды видов доходов"
    final String C_TYPE_CODE = "Код вычета"
    final String R_TYPE_CODE = "Коды видов вычетов"
    final String SECTION_LINE_MSG = "Раздел %s. Строка %s"
    final String T_PERSON_INCOME = "2" // "Сведения о доходах и НДФЛ"
    final String T_PERSON_DEDUCTION = "3" // "Сведения о вычетах"
    final String REQUISITES_TITLE = "Реквизиты"
    final String INCOME_TITLE = "Сведения о доходах и НДФЛ"
    final String DEDUCTION_TITLE = "Сведения о вычетах"
    final String PREPAYMENTS_TITLE = "Сведения о доходах в виде авансовых платежей"
    final String INP = "ИНП";
    final String LAST_NAME = "Фамилия"
    final String FIRST_NAME = "Имя"
    final String MIDDLE_NAME = "Отчество"
    final String BIRTH_DAY = "Дата рождения"
    final String CITIZENSHIP = "Гражданство (код страны)"
    final String INN = "ИНН в РФ"
    final String INN_FOREIGN = "ИНН в ИНО"
    final String ID_DOC_TYPE = "ДУЛ Код"
    final String ID_DOC_NUMBER = "ДУЛ Номер"
    final String STATUS = "Статус (код)"
    final String REGION_CODE = "Код субъекта"
    final String POST_INDEX = "Индекс"
    final String AREA = "Район"
    final String CITY = "Город"
    final String LOCALITY = "Населенный пункт"
    final String STREET = "Улица"
    final String HOUSE = "Дом"
    final String BUILDING = "Корпус"
    final String FLAT = "Квартира"
    final String SNILS = "СНИЛС"
    final String INCOME_OPERATION_ID = "ID операции"
    final String INCOME_CODE = "Код дохода"
    final String INCOME_TYPE = "Признак дохода"
    final String INCOME_ACCRUED_DATE = "Дата начисления дохода"
    final String INCOME_PAYOUT_DATE = "Дата выплаты дохода"
    final String KPP = "КПП"
    final String OKTMO = "ОКТМО"
    final String INCOME_ACCRUED_SUMM = "Сумма начисленного дохода"
    final String INCOME_PAYOUT_SUMM = "Сумма выплаченного дохода"
    final String TOTAL_DEDUCTIONS_SUMM = "Сумма вычета"
    final String TAX_BASE = "Налоговая база"
    final String TAX_RATE = "Процентная ставка (%)"
    final String TAX_DATE = "Дата НДФЛ"
    final String CALCULATED_TAX = "НДФЛ исчисленный"
    final String WITHHOLDING_TAX = "НДФЛ удержанный"
    final String NOT_HOLDING_TAX = "НДФЛ не удержанный"
    final String OVERHOLDING_TAX = "НДФЛ излишне удержанный"
    final String REFOUND_TAX = "НДФЛ возвращённый НП"
    final String TAX_TRANSFER_DATE = "Срок перечисления в бюджет"
    final String PAYMENT_DATE = "Дата платёжного поручения"
    final String PAYMENT_NUMBER = "Номер платёжного поручения"
    final String TAX_SUMM = "Сумма платёжного поручения"
    final String TYPE_CODE = "Код вычета"
    final String NOTIF_TYPE = "Подтверждающий документ. Тип"
    final String NOTIF_DATE = "Подтверждающий документ. Дата"
    final String NOTIF_NUM = "Подтверждающий документ. Номер"
    final String NOTIF_SOURCE = "Подтверждающий документ. Код источника"
    final String NOTIF_SUM = "Подтверждающий документ. Сумма"
    final String DEDUCTION_OPERATION_ID = "Доход. ID операции"
    final String INCOME_ACCRUED = "Доход. Дата"
    final String DEDUCTION_INCOME_CODE = "Доход. Код дохода"
    final String INCOME_SUMM = "Доход. Сумма"
    final String PERIOD_PREV_DATE = "Применение вычета. Дата"
    final String PERIOD_PREV_SUMM = "Применение вычета. Сумма"
    final String PERIOD_CURR_DATE = "Вычет. Текущий период. Дата"
    final String PERIOD_CURR_SUMM = "Вычет. Текущий период. Сумма"
    final String PREPAYMENT_OPERATION_ID = "СведАванс. ID операции"
    final String PREPAYMENT_SUMM = "Сумма фиксированного авансового платежа"
    final String PREPAYMENT_NOTIF_NUM = "Номер уведомления"
    final String PREPAYMENT_NOTIF_DATE = "Дата выдачи уведомления"
    final String PREPAYMENT_NOTIF_SOURCE = "Код налогового органа, выдавшего уведомление"

    Logger logger
    NdflPersonService ndflPersonService
    RefBookPersonService refBookPersonService
    DeclarationData declarationData
    DepartmentService departmentService
    CalendarService calendarService
    FiasRefBookService fiasRefBookService
    ReportPeriodService reportPeriodService
    DepartmentReportPeriodService departmentReportPeriodService
    RefBookFactory refBookFactory
    CommonRefBookService commonRefBookService

    String fileName // имя загружаемого файла
    File file // временный файл с данными

    // Дата окончания отчетного периода
    ReportPeriod reportPeriod
    // Подразделение формы
    Department department
    // Наименование АСНУ налоговой формы
    String asnuName
    // Вид формы
    DeclarationTemplate declarationTemplate
    // Кэш провайдеров cправочников
    Map<Long, RefBookDataProvider> providerCache = [:]
    // Записи справочника "Коды видов доходов"
    Map<Long, Map<String, RefBookValue>> incomeCodes
    Map<String, List<Long>> incomeTypeMap
    // Коды справочника "Коды видов вычетов"
    List<String> deductionCodes
    // ФЛ формы из БД
    Map<Long, NdflPerson> persistedPersonsById
    // Кэш загруженных ФЛ
    Map<Integer, List<NdflPersonExt>> ndflPersonCache = [:]
    // Операции(доходы, вычеты, авансы) сгруппированные по operationId
    TreeMap<String, List<NdflPersonOperation>> operationsGrouped = new TreeMap<>()
    Date importDate = new Date()

    // Список идентификаторов импортируемых доходов
    List<Long> incomeImportIdList = []

    // Список идентификаторов импортируемых вычетов
    List<Long> deductionImportIdList = []

    // Список идентификаторов импортируемых авансов
    List<Long> prepaymentImportIdList = []

    List<Long> ndflPersonImportIdCache = []

    /**
     * Идентификаторы вычетов, ячейки которых незаполнены, сгруппированные по идентификатору раздела 1
     */
    Map<Long, List<Long>> emptyDeductionsIds = [:]

    /**
     * Идентификаторы авансмов, ячейки которых незаполнены, сгруппированные по идентификатору раздела 1
     */
    Map<Long, List<Long>> emptyPrepaymentsIds = [:]

    @TypeChecked(TypeCheckingMode.SKIP)
    Import(scriptClass) {
        //noinspection GroovyAssignabilityCheck
        super(scriptClass)
        this.logger = (Logger) scriptClass.getProperty("logger")
        this.fileName = (String) scriptClass.getProperty("fileName")
        this.file = (File) scriptClass.getProperty("file")
        this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService")
        this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService")
        this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData")
        this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory")
        this.fiasRefBookService = (FiasRefBookService) scriptClass.getProperty("fiasRefBookService")
        this.calendarService = (CalendarService) scriptClass.getProperty("calendarService")
        this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService")
        this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService")
        this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService")
        this.refBookPersonService = (RefBookPersonService) scriptClass.getProperty("refBookPersonService")
        this.commonRefBookService = (CommonRefBookService) scriptClass.getProperty("commonRefBookService")

        reportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId).reportPeriod
        correctReportPeriod()
        department = departmentService.get(declarationData.departmentId)
        declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        asnuName = getAsnuName()

        incomeCodes = getRefIncomeCodes()
        incomeTypeMap = getRefIncomeTypes()
        deductionCodes = getRefDeductionCodes()
    }

    void correctReportPeriod() {
        def record = refBookFactory.getDataProvider(RefBook.Id.PERIOD_CODE.getId()).getRecordData(reportPeriod.dictTaxPeriodId)
        if (record) {
            def refStartDate = record.START_DATE.dateValue
            def refCalendarStartDate = record.CALENDAR_START_DATE.dateValue
            def refEndDate = record.END_DATE.dateValue

            reportPeriod.startDate = toDate(reportPeriod.taxPeriod.year, refStartDate)
            reportPeriod.calendarStartDate = toDate(reportPeriod.taxPeriod.year, refCalendarStartDate)
            reportPeriod.endDate = toDate(reportPeriod.taxPeriod.year, refEndDate)
        }
    }

    @Override
    void run() {
        initConfiguration()
        switch (formDataEvent) {
            case FormDataEvent.IMPORT:
                importData()
        }
    }

    void importData() {
        checkInterrupted()

        logForDebug("Начало импорта Excel-файла декларации")

        if (!fileName.endsWith(".xlsx")) {
            logger.error("Ошибка при загрузке файла \"$fileName\". Выбранный файл не соответствует расширению xlsx.")
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            return
        }

        LinkedList<List<String>> allValues = [] as LinkedList
        List<List<String>> headerValues = []
        // отступы сверху и слева для таблицы
        Map<String, Object> paramsMap = ['rowOffset': 0, 'colOffset': 0] as Map<String, Object>

        logForDebug("Начало чтения Excel-файла декларации")

        ExcelImportUtils.readSheetsRange(file, allValues, headerValues, HEADER_START_VALUE, 2, paramsMap, 1, null)
        checkHeaders(headerValues)
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            return
        }
        if (allValues.size() == 0) {
            logger.error("Ошибка при загрузке файла \"$fileName\". Отсутствуют данные для загрузки в файле " +
                    "или файл не соответствует требуемой структуре (не заполнена строка, следующая непосредственно после шапки таблицы).")
            return
        }

        logForDebug("Завершено чтение Excel-файла декларации")

        persistedPersonsById = ndflPersonService.findNdflPersonWithOperations(declarationData.id)
                .collectEntries { [it.id, it] }
        boolean declarationEmpty = persistedPersonsById.isEmpty()

        logForDebug("Обработка прочитанных данных и сохранение их в память из Excel-файла декларации")

        int TABLE_DATA_START_INDEX = 3
        int rowIndex = TABLE_DATA_START_INDEX
        List<NdflPersonExt> ndflPersonRows = []
        List<NdflPersonExt> ndflPersons = []
        Set<Long> incomeIdsForRemove = []
        for (def iterator = allValues.iterator(); iterator.hasNext(); rowIndex++) {
            checkInterrupted()

            def row = new Row(rowIndex, iterator.next() as List<String>)
            iterator.remove()
            if (row.isEmpty()) {// все строки пустые - выход
                if (rowIndex == TABLE_DATA_START_INDEX) {
                    logger.error("Ошибка при загрузке файла \"$fileName\". Отсутствуют данные для загрузки в файле " +
                            "или файл не соответствует требуемой структуре (не заполнена строка, следующая непосредственно после шапки таблицы).")
                    return
                }
                break
            }
            def ndflPerson = createNdflPerson(row)
            ndflPersonRows << ndflPerson

            if (!declarationEmpty) {
                String[] importIds = splitTechnicalCell(row)
                if (importIds && importIds.length > 1 && !importIds[1].isEmpty()) {
                    long incomeId = Long.parseLong(importIds[1])
                    incomeIdsForRemove << incomeId
                }
            }
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            return
        }
        logForDebug("Заврешена обработка и сохранение в память прочитанных данных из Excel-файла декларации")

        persistedPersonsById = ndflPersonService.findNdflPersonWithOperations(declarationData.id)
                .collectEntries { [it.id, it] }
        logForDebug("Проверка изменения реквизитов для всех строк ТФ в разделе 1")
        checkRows(ndflPersonRows)
        logForDebug("Завершена проверка изменения реквизитов для всех строк ТФ в разделе 1")
        logForDebug("Начата процедура обработки ФЛ из файла")
        for (NdflPersonExt mergingPerson : ndflPersonRows) {
            merge(ndflPersons, mergingPerson)
        }
        logForDebug("Закончена процедура обработки ФЛ из файла")
        logForDebug("Проверка корректной обработки ФЛ из файла")
        checkPersons(ndflPersons)
        logForDebug("Закончена проверка корректной обработки ФЛ из файла")
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            return
        }

        ndflPersonService.fillNdflPersonIncomeSortFields((List<? extends NdflPerson>) ndflPersons)

        // Если в НФ нет данных, то создаем новые из ТФ
        if (declarationEmpty) {
            logForDebug("В НФ нет данных, создаем новые из ТФ")

            logForDebug("Запущена сортировка загруженных данных декларации")
            Collections.sort(ndflPersons, NdflPerson.getComparator())
            for (NdflPerson ndflPerson : ndflPersons) {
                Collections.sort(ndflPerson.incomes, NdflPersonIncome.getComparator())
                Collections.sort(ndflPerson.deductions, NdflPersonDeduction.getComparator(ndflPerson))
                Collections.sort(ndflPerson.prepayments, NdflPersonPrepayment.getComparator(ndflPerson))
            }
            logForDebug("Закончена сортировка загруженных данных декларации")

            logForDebug("Поиск идентификаторов операций которые отсутствуют в НФ и изменение их")
            transformOperationId()
            logForDebug("Обновление номеров строк данных декларации")
            updatePersonsRowNum(ndflPersons)
            if (!logger.containsLevel(LogLevel.ERROR)) {
                checkInterrupted()
                logForDebug("Сохранение обработанных данных декларации из Excel-файла в базу")
                ndflPersonService.save((List<? extends NdflPerson>) ndflPersons)
                logForDebug("Завершено сохранение обработанных данных декларации из Excel-файла в базу")
            } else {
                logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            }
        } else {
            // Если в ТФ есть данные
            // Удаляем операции
            logForDebug("В НФ есть данные. Удаляем операции.")
            if (!incomeIdsForRemove.isEmpty()) {
                removeOperations(incomeIdsForRemove.toList(), persistedPersonsById.values())
            }
            logForDebug("Удаление существующих операций заверешно.")
            List<LogEntry> messages = []

            List<NdflPerson> ndflPersonsForCreate = []
            List<NdflPerson> ndflPersonsForUpdate = []
            // Операции для добавления
            List<NdflPersonIncome> incomesForCreate = []
            List<NdflPersonDeduction> deductionsForCreate = []
            List<NdflPersonPrepayment> prepaymentsForCreate = []

            boolean operationsTransformed = false

            logForDebug("Запущена процедура обновления данных физ лиц.")
            for (NdflPersonExt ndflPerson : ndflPersons) {
                if (needPersonUpdate(ndflPerson)) {
                    logForDebug("Запущена процедура обновления данных ФЛ $ndflPerson.importId")
                    NdflPerson persistedPerson = null
                    // Проверяем обновлялись ли уже у этого физлица реквизиты
                    if (!ndflPersonImportIdCache.contains(ndflPerson.importId)) {
                        persistedPerson = persistedPersonsById.get(ndflPerson.importId)
                        ndflPersonImportIdCache << ndflPerson.importId
                    }

                    if (persistedPerson != null) {
                        logForDebug("Начата процедура обновления данных ФЛ $ndflPerson.importId")
                        persistedPerson.personId = null
                        if (ndflPerson.middleName?.isEmpty()) ndflPerson.middleName = null
                        if (ndflPerson.innNp?.isEmpty()) ndflPerson.innNp = null
                        if (ndflPerson.innForeign?.isEmpty()) ndflPerson.innForeign = null
                        if (ndflPerson.regionCode?.isEmpty()) ndflPerson.regionCode = null
                        if (ndflPerson.postIndex?.isEmpty()) ndflPerson.postIndex = null
                        if (ndflPerson.area?.isEmpty()) ndflPerson.area = null
                        if (ndflPerson.city?.isEmpty()) ndflPerson.city = null
                        if (ndflPerson.locality?.isEmpty()) ndflPerson.locality = null
                        if (ndflPerson.street?.isEmpty()) ndflPerson.street = null
                        if (ndflPerson.house?.isEmpty()) ndflPerson.house = null
                        if (ndflPerson.building?.isEmpty()) ndflPerson.building = null
                        if (ndflPerson.flat?.isEmpty()) ndflPerson.flat = null
                        if (ndflPerson.snils?.isEmpty()) ndflPerson.snils = null
                        boolean updated = false
                        if (ndflPerson.inp != persistedPerson.inp) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, INP, persistedPerson.inp, ndflPerson.inp)
                            persistedPerson.inp = ndflPerson.inp
                            updated = true
                        }
                        if (ndflPerson.lastName != persistedPerson.lastName) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, LAST_NAME, persistedPerson.lastName, ndflPerson.lastName)
                            persistedPerson.lastName = ndflPerson.lastName
                            updated = true
                        }
                        if (ndflPerson.firstName != persistedPerson.firstName) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, FIRST_NAME, persistedPerson.firstName, ndflPerson.firstName)
                            persistedPerson.firstName = ndflPerson.firstName
                            updated = true
                        }
                        if (ndflPerson.middleName != persistedPerson.middleName) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, MIDDLE_NAME, persistedPerson.middleName, ndflPerson.middleName)
                            persistedPerson.middleName = ndflPerson.middleName
                            updated = true
                        }
                        if (ndflPerson.birthDay != persistedPerson.birthDay) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, BIRTH_DAY, persistedPerson.birthDay?.format(SharedConstants.DATE_FORMAT), ndflPerson.birthDay?.format(SharedConstants.DATE_FORMAT))
                            persistedPerson.birthDay = ndflPerson.birthDay
                            updated = true
                        }
                        if (ndflPerson.citizenship != persistedPerson.citizenship) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, CITIZENSHIP, persistedPerson.citizenship, ndflPerson.citizenship)
                            persistedPerson.citizenship = ndflPerson.citizenship
                            updated = true
                        }
                        if (ndflPerson.innNp != persistedPerson.innNp) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, INN, persistedPerson.innNp, ndflPerson.innNp)
                            persistedPerson.innNp = ndflPerson.innNp
                            updated = true
                        }
                        if (ndflPerson.innForeign != persistedPerson.innForeign) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, INN_FOREIGN, persistedPerson.innForeign, ndflPerson.innForeign)
                            persistedPerson.innForeign = ndflPerson.innForeign
                            updated = true
                        }
                        if (ndflPerson.idDocType != persistedPerson.idDocType) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, ID_DOC_TYPE, persistedPerson.idDocType, ndflPerson.idDocType)
                            persistedPerson.idDocType = ndflPerson.idDocType
                            updated = true
                        }
                        if (ndflPerson.idDocNumber != persistedPerson.idDocNumber) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, ID_DOC_NUMBER, persistedPerson.idDocNumber, ndflPerson.idDocNumber)
                            persistedPerson.idDocNumber = ndflPerson.idDocNumber
                            updated = true
                        }
                        if (ndflPerson.status != persistedPerson.status) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, STATUS, persistedPerson.status, ndflPerson.status)
                            persistedPerson.status = ndflPerson.status
                            updated = true
                        }
                        if (ndflPerson.regionCode != persistedPerson.regionCode) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, REGION_CODE, persistedPerson.regionCode, ndflPerson.regionCode)
                            persistedPerson.regionCode = ndflPerson.regionCode
                            updated = true
                        }
                        if (ndflPerson.postIndex != persistedPerson.postIndex) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, POST_INDEX, persistedPerson.postIndex, ndflPerson.postIndex)
                            persistedPerson.postIndex = ndflPerson.postIndex
                            updated = true
                        }
                        if (ndflPerson.area != persistedPerson.area) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, AREA, persistedPerson.area, ndflPerson.area)
                            persistedPerson.area = ndflPerson.area
                            updated = true
                        }
                        if (ndflPerson.city != persistedPerson.city) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, CITY, persistedPerson.city, ndflPerson.city)
                            persistedPerson.city = ndflPerson.city
                            updated = true
                        }
                        if (ndflPerson.locality != persistedPerson.locality) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, LOCALITY, persistedPerson.locality, ndflPerson.locality)
                            persistedPerson.locality = ndflPerson.locality
                            updated = true
                        }
                        if (ndflPerson.street != persistedPerson.street) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, STREET, persistedPerson.street, ndflPerson.street)
                            persistedPerson.street = ndflPerson.street
                            updated = true
                        }
                        if (ndflPerson.house != persistedPerson.house) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, HOUSE, persistedPerson.house, ndflPerson.house)
                            persistedPerson.house = ndflPerson.house
                            updated = true
                        }
                        if (ndflPerson.building != persistedPerson.building) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, BUILDING, persistedPerson.building, ndflPerson.building)
                            persistedPerson.building = ndflPerson.building
                            updated = true
                        }
                        if (ndflPerson.flat != persistedPerson.flat) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, FLAT, persistedPerson.flat, ndflPerson.flat)
                            persistedPerson.flat = ndflPerson.flat
                            updated = true
                        }
                        if (ndflPerson.snils != persistedPerson.snils) {
                            messages << createUpdateOperationMessage(ndflPerson, REQUISITES_TITLE, ndflPerson.importId, SNILS, persistedPerson.snils, ndflPerson.snils)
                            persistedPerson.snils = ndflPerson.snils
                            updated = true
                        }
                        if (updated) {
                            persistedPerson.modifiedDate = new Date()
                            persistedPerson.modifiedBy = "${userInfo.getUser().getName()} (${userInfo.getUser().getLogin()})"
                            ndflPersonsForUpdate << persistedPerson
                        }
                        logForDebug("Закончена процедура обновления данных ФЛ $ndflPerson.importId")
                    }

                    int incomesCreateCount = 0
                    logForDebug("Запущена процедура обновления сведений о доходах физ.лиц")
                    for (def income : ndflPerson.incomes) {
                        logForDebug("Процедура обновления сведений о доходах №$income.id")
                        income.ndflPersonId = ndflPerson.importId
                        income.modifiedDate = new Date()
                        income.modifiedBy = "${userInfo.getUser().getName()} (${userInfo.getUser().getLogin()})"
                        incomesForCreate << income
                        incomesCreateCount++
                        logForDebug("Завершена процедура обновления сведений о доходах №$income.id")
                    }
                    logForDebug("Заверешена процедура обновления сведений о доходах физ.лиц")

                    if (incomesCreateCount > 0) {
                        messages << createNewOperationMessage(ndflPerson, "Сведения о доходах и НДФЛ", incomesCreateCount)
                    }
                    int deductionsCreateCount = 0
                    logForDebug("Запущена процедура обновления сведений о вычетах физ.лиц")
                    for (NdflPersonDeduction deduction : ndflPerson.deductions) {
                        logForDebug("Запущена процедура обновления сведений о вычетах №$deduction.id")
                        deduction.ndflPersonId = ndflPerson.importId
                        deduction.modifiedDate = new Date()
                        deduction.modifiedBy = "${userInfo.getUser().getName()} (${userInfo.getUser().getLogin()})"
                        deductionsForCreate << deduction
                        deductionsCreateCount++
                        logForDebug("Завершена процедура обновления сведений о вычетах №$deduction.id")
                    }
                    logForDebug("Заврешена процедура обновления сведений о вычетах физ.лиц")

                    if (deductionsCreateCount > 0) {
                        messages << createNewOperationMessage(ndflPerson, "Сведения о вычетах", deductionsCreateCount)
                    }
                    int prepaymentsCreateCount = 0
                    logForDebug("Запущена процедура обновления сведений о доходах в виде авансовых платежей физ.лиц")
                    for (NdflPersonPrepayment prepayment : ndflPerson.prepayments) {
                        logForDebug("Обновление сведений о доходах в виде авансового платежа №$prepayment.id")
                        prepayment.ndflPersonId = ndflPerson.importId
                        prepaymentsForCreate << prepayment
                        prepayment.modifiedDate = new Date()
                        prepayment.modifiedBy = "${userInfo.getUser().getName()} (${userInfo.getUser().getLogin()})"
                        prepaymentsCreateCount++
                        logForDebug("Закочено обновление сведений о доходах в виде авансового платежа №$prepayment.id")
                    }
                    logForDebug("Заврешена процедура обновления сведений о доходах в виде авансовых платежей физ.лиц")

                    if (prepaymentsCreateCount > 0) {
                        messages << createNewOperationMessage(ndflPerson, "Сведения о доходах в виде авансовых платежей", prepaymentsCreateCount)
                    }

                    logForDebug("Завершена процедура обновления данных ФЛ $ndflPerson.importId")
                } else {
                    if (!operationsTransformed) {
                        transformOperationId()
                    }
                    operationsTransformed = true

                    ndflPersonsForCreate << ndflPerson
                }
            }
            logForDebug("Завершена процедура обновления данных физ лиц.")
            logForDebug("Сохранение созданных ФЛ в базу")
            if (!ndflPersonsForCreate.isEmpty()) {
                ndflPersonService.save(ndflPersonsForCreate)
            }
            logForDebug("Обновление существующих ФЛ в базе")
            ndflPersonService.updateNdflPersons(ndflPersonsForUpdate)
            if (ndflPersonsForCreate || ndflPersonsForUpdate) {
                logForDebug("Обновление ID ФЛ в базе")
                refBookPersonService.clearRnuNdflPerson(declarationData.id)
            }
            logForDebug("Сохранение созданных сведений о доходах ФЛ в базу")
            ndflPersonService.saveIncomes(incomesForCreate)
            logForDebug("Сохранение созданных сведений о вычетах ФЛ в базу")
            ndflPersonService.saveDeductions(deductionsForCreate)
            logForDebug("Сохранение созданных сведений о авансовых платежах ФЛ в базу")
            ndflPersonService.savePrepayments(prepaymentsForCreate)

            logForDebug("Сохранение всех сообщений в область уведомлений")
            logger.getEntries().addAll(messages)

            sortAndUpdateRowNumInUpdatedDeclaration()
        }

        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
        }
    }

    private void sortAndUpdateRowNumInUpdatedDeclaration() {
        logForDebug("Запущена процедура сортировки данных")

        List<NdflPerson> updatedPersons = ndflPersonService.findNdflPersonWithOperations(declarationData.id)

        BigDecimal incomeRowNum = BigDecimal.ZERO
        BigDecimal deductionRowNum = BigDecimal.ZERO
        BigDecimal prepaymentRowNum = BigDecimal.ZERO
        Long personRowNum = 0L
        ndflPersonService.fillNdflPersonIncomeSortFields((List<? extends NdflPerson>) updatedPersons)
        logForDebug("Начата сортировка ФЛ")
        Collections.sort(updatedPersons, NdflPerson.getComparator())
        logForDebug("Закончена сортировка ФЛ")
        logForDebug("Начата сортировка всех операций декларации")
        for (NdflPerson ndflPerson : updatedPersons) {
            Collections.sort(ndflPerson.incomes, NdflPersonIncome.getComparator())
            Collections.sort(ndflPerson.deductions, NdflPersonDeduction.getComparator(ndflPerson))
            Collections.sort(ndflPerson.prepayments, NdflPersonPrepayment.getComparator(ndflPerson))

            for (NdflPersonIncome income : ndflPerson.incomes) {
                incomeRowNum = incomeRowNum.add(BigDecimal.ONE)
                income.rowNum = incomeRowNum
            }

            for (NdflPersonDeduction deduction : ndflPerson.deductions) {
                deductionRowNum = deductionRowNum.add(BigDecimal.ONE)
                deduction.rowNum = deductionRowNum
            }

            for (NdflPersonPrepayment prepayment : ndflPerson.prepayments) {
                prepaymentRowNum = prepaymentRowNum.add(BigDecimal.ONE)
                prepayment.rowNum = prepaymentRowNum
            }

            ndflPerson.rowNum = ++personRowNum
        }
        logForDebug("Закончена сортировка операций декларации")
        logForDebug("Закончена процедура сортировки данных")

        logForDebug("Обновление номеров строк всех разделов")
        ndflPersonService.updateRowNum(updatedPersons)
    }

    /**
     * Проверяет, существует ли ФЛ с пришедшим идентифкатором в НФ.
     * Результат используется для определния нужно ли обновлять Физлицо или создавать.
     *
     * @param ndflPerson Объект физлица
     * @return true если физлицо нужно обновлять
     */
    boolean needPersonUpdate(NdflPersonExt ndflPerson) {
        boolean result = false
        if (ndflPerson.importId) {
            def existingPerson = ndflPersonService.get(ndflPerson.importId)
            result = existingPerson.getDeclarationDataId() == declarationData.id
        }
        return result
    }

    List<String> header = ["Идентификаторы строк разделов РНУ НДФЛ (технический столбец - не заполнять при добавлении новой строки)",
                           "№ п/п", "ИНП", "Фамилия", "Имя", "Отчество", "Дата рождения", "Гражданство (код страны)",
                           "ИНН в РФ", "ИНН в ИНО", "ДУЛ Код", "ДУЛ Номер", "Статус (код)", "Код субъекта", "Индекс", "Район", "Город",
                           "Населенный пункт", "Улица", "Дом", "Корпус", "Квартира", "СНИЛС", "ID операции", "Код дохода", "Признак дохода",
                           "Дата начисления дохода", "Дата выплаты дохода", "КПП", "ОКТМО", "Сумма начисленного дохода", "Сумма выплаченного дохода",
                           "Сумма вычета", "Налоговая база", "Процентная ставка (%)", "Дата НДФЛ", "НДФЛ исчисленный", "НДФЛ удержанный",
                           "НДФЛ не удержанный", "НДФЛ излишне удержанный", "НДФЛ возвращённый НП", "Срок перечисления в бюджет", "Дата платёжного поручения",
                           "Номер платёжного поручения", "Сумма платёжного поручения", "Код вычета", "Подтверждающий документ. Тип",
                           "Подтверждающий документ. Дата", "Подтверждающий документ. Номер", "Подтверждающий документ. Код источника",
                           "Подтверждающий документ. Сумма", "Доход. ID операции", "Доход. Дата", "Доход. Код дохода", "Доход. Сумма",
                           "Применение вычета. Дата", "Применение вычета. Сумма", "Вычет. Текущий период. Дата",
                           "Вычет. Текущий период. Сумма", "СведАванс. ID операции",
                           "Сумма фиксированного авансового платежа", "Номер уведомления", "Дата выдачи уведомления",
                           "Код налогового органа, выдавшего уведомление"]

    void checkHeaders(List<List<String>> headersActual) {
        if (headersActual == null || headersActual?.isEmpty() || headersActual[0] == null || headersActual[0]?.isEmpty()) {
            logger.error("Ошибка при загрузке файла \"$fileName\". Заголовок таблицы не соответствует требуемой структуре.")
            return
        }
        for (int i = 0; i < header.size(); i++) {
            if (i >= headersActual[0].size() || header[i] != headersActual[0][i]) {
                logger.error("Ошибка при загрузке файла \"$fileName\". Заголовок таблицы не соответствует требуемой структуре.")
                logger.error("Столбец заголовка таблицы \"${i >= headersActual[0].size() ? "Не задан или отсутствует" : headersActual[0][i]}\" № ${i} " +
                        "не соответствует ожидаемому \"${header[i]}\" № ${i}")
                break
            }
        }
    }

    NdflPersonExt createNdflPerson(Row row) {
        String[] importIds = splitTechnicalCell(row)
        Long incomeImportId = null
        Long deductionImportId = null
        Long prepaymentImportId = null
        NdflPersonExt ndflPerson = new NdflPersonExt(row.index)
        if (importIds) {
            if (importIds[0]) {
                if (importIds[0].isNumber()) {
                    ndflPerson.importId = Long.valueOf(importIds[0])
                } else {
                    logger.error("Ошибка при загрузке файла \"$fileName\". В строке № \"$row.index\" указан несуществующий идентификатор строки Раздела 1.")
                }
            } else {
                logger.error("Ошибка при загрузке файла \"$fileName\". В строке № \"$row.index\" пустой идентификатор строки Раздела 1.")
            }
            if (importIds.length > 1 && !importIds[1].isEmpty()) {
                incomeImportId = Long.valueOf(importIds[1])
                incomeImportIdList << incomeImportId
            }
            if (importIds.length > 2 && !importIds[2].isEmpty()) {
                deductionImportId = Long.valueOf(importIds[2])
                deductionImportIdList << deductionImportId
            }
            if (importIds.length > 3 && !importIds[3].isEmpty()) {
                prepaymentImportId = Long.valueOf(importIds[3])
                prepaymentImportIdList << prepaymentImportId
            }
        }
        ndflPerson.declarationDataId = declarationData.id
        ndflPerson.inp = row.cell(3).toString(25)?.toUpperCase()
        ndflPerson.lastName = row.cell(4).toString(60)?.toUpperCase()
        ndflPerson.firstName = row.cell(5).toString(60)?.toUpperCase()
        ndflPerson.middleName = row.cell(6).toString(60)?.toUpperCase()
        ndflPerson.birthDay = row.cell(7).toDate()
        ndflPerson.citizenship = row.cell(8).toString(3)
        ndflPerson.innNp = row.cell(9).toString(12)?.toUpperCase()
        ndflPerson.innForeign = row.cell(10).toString(50)?.toUpperCase()
        ndflPerson.idDocType = row.cell(11).toString(2)
        ndflPerson.idDocNumber = row.cell(12).toString(25)?.toUpperCase()
        ndflPerson.status = row.cell(13).toInteger(1)?.toString()
        ndflPerson.regionCode = row.cell(14).toString(2)
        ndflPerson.postIndex = row.cell(15).toString(6)
        ndflPerson.area = row.cell(16).toString(50)?.toUpperCase()
        ndflPerson.city = row.cell(17).toString(50)?.toUpperCase()
        ndflPerson.locality = row.cell(18).toString(50)?.toUpperCase()
        ndflPerson.street = row.cell(19).toString(50)?.toUpperCase()
        ndflPerson.house = row.cell(20).toString(20)?.toUpperCase()
        ndflPerson.building = row.cell(21).toString(20)?.toUpperCase()
        ndflPerson.flat = row.cell(22).toString(20)?.toUpperCase()
        ndflPerson.snils = row.cell(23).toString(14)?.toUpperCase()
        ndflPerson.asnuId = declarationData.asnuId
        ndflPerson.modifiedDate = importDate

        if (!row.isEmpty(24..45)) {
            ndflPerson.incomes.add(createIncome(row, incomeImportId))
        }

        if (!row.isEmpty(46..59)) {
            ndflPerson.deductions.add(createDeduction(row, deductionImportId))
        } else if (deductionImportId) {
            if (emptyDeductionsIds.containsKey(ndflPerson.importId)) {
                emptyDeductionsIds.get(ndflPerson.importId) << deductionImportId
            } else {
                emptyDeductionsIds.put(ndflPerson.importId, [deductionImportId])
            }
        }

        if (!row.isEmpty(60..64)) {
            ndflPerson.prepayments.add(createPrepayment(row, prepaymentImportId))
        } else if (prepaymentImportId) {
            if (emptyPrepaymentsIds.containsKey(ndflPerson.importId)) {
                emptyPrepaymentsIds.get(ndflPerson.importId) << prepaymentImportId
            } else {
                emptyPrepaymentsIds.put(ndflPerson.importId, [prepaymentImportId])
            }
        }

        return ndflPerson
    }

    NdflPersonIncomeExt createIncome(Row row, Long importId) {
        NdflPersonIncomeExt personIncome = new NdflPersonIncomeExt(row.index)
        personIncome.operationId = row.cell(24).toString(100)
        personIncome.incomeCode = row.cell(25).toString(4)
        personIncome.incomeType = row.cell(26).toString(2)
        personIncome.incomeAccruedDate = row.cell(27).toDate()
        personIncome.incomePayoutDate = row.cell(28).toDate()
        personIncome.kpp = row.cell(29).toString(9)
        personIncome.oktmo = row.cell(30).toString(11)
        personIncome.incomeAccruedSumm = row.cell(31).toBigDecimal(20, 2)
        personIncome.incomePayoutSumm = row.cell(32).toBigDecimal(20, 2)
        personIncome.totalDeductionsSumm = row.cell(33).toBigDecimal(20, 2)
        personIncome.taxBase = row.cell(34).toBigDecimal(20, 2)
        personIncome.taxRate = row.cell(35).toInteger(2)
        personIncome.taxDate = row.cell(36).toDate()
        personIncome.calculatedTax = row.cell(37).toBigDecimal(20)
        personIncome.withholdingTax = row.cell(38).toBigDecimal(20)
        personIncome.notHoldingTax = row.cell(39).toBigDecimal(20)
        personIncome.overholdingTax = row.cell(40).toBigDecimal(20)
        personIncome.refoundTax = row.cell(41).toLong(15)
        if (row.cell(42).toString(11) == SharedConstants.DATE_ZERO_AS_STRING) {
            personIncome.taxTransferDate = Date.parse(SharedConstants.DATE_FORMAT, SharedConstants.DATE_ZERO_AS_DATE)
        } else {
            personIncome.taxTransferDate = row.cell(42).toDate()
        }
        personIncome.paymentDate = row.cell(43).toDate()
        personIncome.paymentNumber = row.cell(44).toString(20)
        personIncome.taxSumm = row.cell(45).toBigDecimal(20)
        personIncome.id = importId
        personIncome.asnuId = declarationData.asnuId
        personIncome.modifiedDate = importDate
        if (personIncome.operationId != null && operationsGrouped.containsKey(personIncome.operationId)) {
            operationsGrouped.get(personIncome.operationId).add(personIncome)
        } else if (personIncome.operationId != null) {
            operationsGrouped.put(personIncome.operationId, [(NdflPersonOperation) personIncome])
        }
        return personIncome
    }

    NdflPersonDeductionExt createDeduction(Row row, Long importId) {
        NdflPersonDeduction personDeduction = new NdflPersonDeductionExt(row.index)
        personDeduction.typeCode = row.cell(46).toString(3)
        personDeduction.notifType = row.cell(47).toString(1)
        personDeduction.notifDate = row.cell(48).toDate()
        personDeduction.notifNum = row.cell(49).toString(20)
        personDeduction.notifSource = row.cell(50).toString(4)
        personDeduction.notifSumm = row.cell(51).toBigDecimal(20, 2)
        personDeduction.operationId = row.cell(52).toString(100)
        personDeduction.incomeAccrued = row.cell(53).toDate()
        personDeduction.incomeCode = row.cell(54).toString(4)
        personDeduction.incomeSumm = row.cell(55).toBigDecimal(20, 2)
        personDeduction.periodPrevDate = row.cell(56).toDate()
        personDeduction.periodPrevSumm = row.cell(57).toBigDecimal(20, 2)
        personDeduction.periodCurrDate = row.cell(58).toDate()
        personDeduction.periodCurrSumm = row.cell(59).toBigDecimal(20, 2)
        personDeduction.id = importId
        personDeduction.asnuId = declarationData.asnuId
        personDeduction.modifiedDate = importDate
        if (personDeduction.operationId != null && operationsGrouped.containsKey(personDeduction.operationId)) {
            operationsGrouped.get(personDeduction.operationId).add(personDeduction)
        } else if (personDeduction.operationId != null) {
            operationsGrouped.put(personDeduction.operationId, [(NdflPersonOperation) personDeduction])
        }
        return personDeduction
    }

    NdflPersonPrepaymentExt createPrepayment(Row row, Long importId) {
        NdflPersonPrepayment personPrepayment = new NdflPersonPrepaymentExt(row.index)
        personPrepayment.operationId = row.cell(60).toString(100)
        personPrepayment.summ = row.cell(61).toBigDecimal(20)
        personPrepayment.notifNum = row.cell(62).toString(20)
        personPrepayment.notifDate = row.cell(63).toDate()
        personPrepayment.notifSource = row.cell(64).toString(4)
        personPrepayment.id = importId
        personPrepayment.asnuId = declarationData.asnuId
        personPrepayment.modifiedDate = importDate
        if (personPrepayment.operationId != null && operationsGrouped.containsKey(personPrepayment.operationId)) {
            operationsGrouped.get(personPrepayment.operationId).add(personPrepayment)
        } else if (personPrepayment.operationId != null) {
            operationsGrouped.put(personPrepayment.operationId, [(NdflPersonOperation) personPrepayment])
        }
        return personPrepayment
    }

    boolean merge(List<NdflPersonExt> persons, NdflPersonExt person) {
        NdflPerson foundPerson = getOrPutToCache(person)
        if (foundPerson) {
            foundPerson.incomes.addAll(person.incomes)
            foundPerson.deductions.addAll(person.deductions)
            foundPerson.prepayments.addAll(person.prepayments)
            return false
        } else {
            persons.add(person)
            return true
        }
    }

    NdflPersonExt getOrPutToCache(NdflPersonExt personToFind) {
        int hash = Objects.hash(personToFind.inp, personToFind.lastName, personToFind.firstName, personToFind.middleName,
                personToFind.birthDay, personToFind.snils, personToFind.citizenship, personToFind.innNp,
                personToFind.innForeign, personToFind.idDocType, personToFind.idDocNumber, personToFind.status,
                personToFind.regionCode, personToFind.postIndex, personToFind.area, personToFind.city,
                personToFind.locality, personToFind.street, personToFind.house, personToFind.building, personToFind.flat)
        def foundPersons = ndflPersonCache.get(hash)
        if (foundPersons != null && !foundPersons.isEmpty()) {
            for (def person : foundPersons) {
                if (personsEquals(person, personToFind)) {
                    return person
                }
            }
        }
        if (foundPersons == null) {
            foundPersons = []
            ndflPersonCache.put(hash, foundPersons)
        }
        foundPersons.add(personToFind)
        return null
    }

    boolean personsEquals(NdflPerson ndflPerson1, NdflPerson ndflPerson2) {
        return ndflPerson1.inp == ndflPerson2.inp &&
                ndflPerson1.lastName == ndflPerson2.lastName &&
                ndflPerson1.firstName == ndflPerson2.firstName &&
                ndflPerson1.middleName == ndflPerson2.middleName &&
                ndflPerson1.birthDay == ndflPerson2.birthDay &&
                ndflPerson1.snils == ndflPerson2.snils &&
                ndflPerson1.citizenship == ndflPerson2.citizenship &&
                ndflPerson1.innNp == ndflPerson2.innNp &&
                ndflPerson1.innForeign == ndflPerson2.innForeign &&
                ndflPerson1.idDocType == ndflPerson2.idDocType &&
                ndflPerson1.idDocNumber == ndflPerson2.idDocNumber &&
                ndflPerson1.status == ndflPerson2.status &&
                ndflPerson1.regionCode == ndflPerson2.regionCode &&
                ndflPerson1.postIndex == ndflPerson2.postIndex &&
                ndflPerson1.area == ndflPerson2.area &&
                ndflPerson1.city == ndflPerson2.city &&
                ndflPerson1.locality == ndflPerson2.locality &&
                ndflPerson1.street == ndflPerson2.street &&
                ndflPerson1.house == ndflPerson2.house &&
                ndflPerson1.building == ndflPerson2.building &&
                ndflPerson1.flat == ndflPerson2.flat
    }

    /**
     * Ищет идентификаторы операций которые отсутствуют в налоговой форме и изменяет их
     */

    void transformOperationId() {
        Set<String> operationsSet = operationsGrouped.keySet()
        if (!operationsSet.isEmpty()) {
            List<String> persistedOperationIdList = ndflPersonService.findIncomeOperationId(new ArrayList<String>(operationsSet))
            operationsSet.removeAll(persistedOperationIdList)
            for (String operationId : operationsSet) {
                List<NdflPersonOperation> operations = operationsGrouped.get(operationId)
                String uuid = UUID.randomUUID().toString()
                for (NdflPersonOperation operation : operations) {
                    operation.operationId = uuid
                }
            }
        }
    }

    /**
     * Facade для выполнения проверок по загружаемым данным
     * @param persons список физлиц из ТФ
     */
    void checkPersons(List<NdflPersonExt> persons) {
        if (persons) {
            checkRequiredPersonFields(persons)
            checkRequiredOperationFields(persons)
            checkPersonIncomeDates(persons)
            if (logger.containsLevel(LogLevel.ERROR)) {
                return
            }
            checkPersonId(persons)
            checkOperationId(persons)
            checkReferences(persons)
        }
    }

    /**
     * Проверка заполненности обязательных граф раздела 1 "Реквизиты ФЛ"
     * @param persons список ФЛ
     */
    void checkRequiredPersonFields(List<NdflPersonExt> persons) {
        def aliasList = ["inp", "lastName", "firstName", "birthDay"]
        def aliasNameList = [INP, LAST_NAME, FIRST_NAME, BIRTH_DAY]
        for (def person : persons) {
            List<String> emptyFields = new ArrayList()
            for (int i = 0; i < aliasList.size(); i++) {
                if (person[aliasList[i]] == null || (person[aliasList[i]]) instanceof String && (StringUtils.isBlank((String) person[aliasList[i]]))) {
                    emptyFields.add(aliasNameList[i])
                }
            }
            if (emptyFields.size() != 0) {
                String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [person.fullName, person.inp])
                logger.errorExp(EMPTY_REQUIRED_FIELD, "Не указан обязательный реквизит ФЛ", fioAndInp, person.rowIndex, emptyFields.join(", "))
            }
        }
    }

    void checkRequiredOperationFields(List<NdflPersonExt> persons) {
        for (def person : persons) {
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [person.fullName, person.inp])
            checkRequiredFieldsIncome(person.incomes, fioAndInp)
            checkRequiredFieldsDeduction(person.deductions, fioAndInp)
            checkRequiredFieldsPrepayment(person.prepayments, fioAndInp)
        }
    }

    void checkRequiredFieldsIncome(List<NdflPersonIncomeExt> incomes, String fioAndInp) {
        for (NdflPersonIncome income : incomes) {
            checkRequiredFields(income, ["kpp", "oktmo"], [KPP, OKTMO], fioAndInp)
        }
    }

    void checkRequiredFieldsDeduction(List<NdflPersonDeductionExt> deductions, String fioAndInp) {
        for (NdflPersonDeduction deduction : deductions) {
            checkRequiredFields(deduction,
                    ["typeCode", "notifType", "notifDate", "notifNum", "notifSource", "operationId", "incomeAccrued", "incomeCode", "incomeSumm", "periodCurrDate", "periodCurrSumm"],
                    [TYPE_CODE, NOTIF_TYPE, NOTIF_DATE, NOTIF_NUM, NOTIF_SOURCE, DEDUCTION_OPERATION_ID, INCOME_ACCRUED, INCOME_CODE, INCOME_SUMM, PERIOD_CURR_DATE, PERIOD_CURR_SUMM],
                    fioAndInp)
        }
    }

    void checkRequiredFieldsPrepayment(List<NdflPersonPrepaymentExt> prepayments, String fioAndInp) {
        for (NdflPersonPrepayment prepayment : prepayments) {
            checkRequiredFields(prepayment,
                    ["operationId", "summ", "notifDate", "notifNum", "notifSource"],
                    [PREPAYMENT_OPERATION_ID, PREPAYMENT_SUMM, PREPAYMENT_NOTIF_DATE, PREPAYMENT_NOTIF_NUM, PREPAYMENT_NOTIF_SOURCE],
                    fioAndInp)
        }

    }

    /**
     * Проверка заполненности обязательных полей
     *
     * @param operation строка из 2, 3, или 4 раздела
     * @param aliasList список названий полей операции, которые необходимо проверить
     * @param aliasNameList список названий граф операций, которые необходимо проверить
     * @param fioAndInp ФИО и ИНП ФЛ
     */
    void checkRequiredFields(NdflPersonOperation operation, List<String> aliasList, List<String> aliasNameList, String fioAndInp) {
        List<String> emptyFields = new ArrayList()
        for (int i = 0; i < aliasList.size(); i++) {
            if (operation[aliasList[i]] == null || (operation[aliasList[i]]) instanceof String && (StringUtils.isBlank((String) operation[aliasList[i]]))) {
                emptyFields.add(aliasNameList[i])
            }
        }
        if (emptyFields.size() != 0) {
            logger.errorExp(EMPTY_REQUIRED_FIELD, "Отсутствие значения в графе не соответствует алгоритму заполнения РНУ НДФЛ", fioAndInp, operation["rowIndex"], emptyFields.join(", "))
        }
    }

    void checkPersonIncomeDates(List<NdflPersonExt> persons) {
        boolean allChecksFailed = true
        for (def person : persons) {
            allChecksFailed &= !checkIncomesDates(person)
        }

        if (allChecksFailed) {
            logger.error("В файле \"$fileName\" отсутствуют операции, принадлежащие периоду формы: " +
                    "\"${reportPeriod.taxPeriod.year}, ${reportPeriod.name}\".")
        }
    }

    boolean checkIncomesDates(NdflPersonExt person) {
        if (!person.incomes) return true

        boolean allRecordsFailed = true
        Map<String, List<NdflPersonIncomeExt>> incomesGroupedByOperationId = person.incomes.groupBy { it.operationId }

        for (List<NdflPersonIncomeExt> operationIncomes : incomesGroupedByOperationId.values()) {
            if (!isOperationBelongToPeriod(operationIncomes, getImportPeriod())) {
                logger.error("Для ФЛ: \"${person.fullName}\", ИНП: \"${person.inp ?: "_"}\" операция: \"${operationIncomes[0].operationId}\" " +
                        "не загружена в Налоговую форму №: \"${declarationData.id}\", Период: " +
                        "\"${reportPeriod.taxPeriod.year}, ${reportPeriod.name}\", Подразделение: \"${department.name}\", Вид: \"${declarationTemplate.name}\"" +
                        "${asnuName ? ", АСНУ: \"${asnuName}\"" : ""}. Ни одна из дат операции: \"Дата начисления дохода\", " +
                        "\"Дата выплаты дохода\", \"Дата НДФЛ\", \"Дата платежного поручения\" не соответствуют периоду загрузки данных.")
            } else {
                allRecordsFailed = false
                if (!isOperationBelongToPeriod(operationIncomes, getCalendarPeriod())) {
                    logger.warn("Для ФЛ: \"${person.fullName}\", ИНП: \"${person.inp ?: "_"}\" операция: \"${operationIncomes[0].operationId}\" " +
                            "загруженная в Налоговую форму №: \"${declarationData.id}\", Период: " +
                            "\"${reportPeriod.taxPeriod.year}, ${reportPeriod.name}\", Подразделение: \"${department.name}\", Вид: \"${declarationTemplate.name}\"" +
                            "${asnuName ? ", АСНУ: \"${asnuName}\"" : ""}. Ни одна из дат операции: \"Дата начисления дохода\", " +
                            "\"Дата выплаты дохода\", \"Дата НДФЛ\", \"Дата платежного поручения\" не принадлежит последним трем месяцам отчетного периода.")
                }
            }
        }
        if (allRecordsFailed) {
            logger.error("Для ФЛ (ФИО: \"${person.fullName}\", ИНП: \"${person.inp}\") " +
                    "отсутствуют операции, принадлежащие периоду загрузки данных. " +
                    "Операции по ФЛ не загружены в Налоговую форму №: \"${declarationData.id}\", " +
                    "Период: \"${reportPeriod.taxPeriod.year}, ${reportPeriod.name}\", " +
                    "Подразделение: \"${department.name}\", Вид: \"${declarationTemplate.name}\"" +
                    "${asnuName ? ", АСНУ: \"${asnuName}\"" : ""}.")
        }
        return !allRecordsFailed
    }

    boolean isOperationBelongToPeriod(List<NdflPersonIncomeExt> operationIncomes, Period period) {
        if (operationIncomes.any {
            isDateBelongToPeriod(it.incomeAccruedDate, period) || isDateBelongToPeriod(it.incomePayoutDate, period) || isDateBelongToPeriod(it.taxDate, period)
        }) {
            return true
        }
        if (operationIncomes.every { !it.incomeAccruedDate && !it.incomePayoutDate && !it.taxDate } &&
                operationIncomes.any { isDateBelongToPeriod(it.paymentDate, period) }) {
            return true
        }
        return false
    }

    boolean isDateBelongToPeriod(Date date, Period period) {
        return period.startDate <= date && date <= period.endDate
    }

    void checkReferences(List<NdflPersonExt> persons) {
        for (def person : persons) {
            if (person.incomes) {
                for (def income : person.incomes) {
                    checkIncomeCode(income, person)
                }
            }
            if (person.deductions) {
                String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [person.fullName, person.inp])
                for (def deduction : person.deductions) {
                    if (deduction.typeCode && !deductionCodes.contains(deduction.typeCode)) {
                        String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                                C_TYPE_CODE, deduction.typeCode ?: "",
                                R_TYPE_CODE
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, deduction.rowNum ?: "")
                        logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_TYPE_CODE), fioAndInp, pathError, errMsg)
                    }
                }
            }
        }
    }

    void checkIncomeCode(NdflPersonIncomeExt income, NdflPersonExt person) {
        if (income.incomeCode && !incomeCodes.find { key, value ->
            value.CODE?.stringValue == income.incomeCode &&
                    (income.incomeAccruedDate == null ||
                            income.incomeAccruedDate >= value.record_version_from?.dateValue &&
                            income.incomeAccruedDate <= value.record_version_to?.dateValue)
        }) {
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [person.fullName, person.inp])
            String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                    C_INCOME_CODE, income.incomeCode ?: "",
                    R_INCOME_CODE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, income.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_CODE), fioAndInp, pathError, errMsg)
        }
    }

    void checkRows(List<NdflPersonExt> persons) {
        checkRequisitesEquality(persons)
    }

    /**
     * Отвечает за проверки, что реквизиты были изменены для всех строк ТФ (Excel),
     * связанных с одной строкой Раздела 1, ранее загруженной в ПНФ
     * @param ndflPersonList список всех объектов раздела 1
     */
    void checkRequisitesEquality(List<NdflPersonExt> ndflPersonList) {
        Map<Long, List<NdflPersonExt>> ndflPersonsGroupedByImportid = [:]
        for (NdflPersonExt ndflPerson : ndflPersonList) {
            if (ndflPerson.importId != null) {
                List<NdflPersonExt> group = ndflPersonsGroupedByImportid.get(ndflPerson.importId)
                if (group == null) {
                    ndflPersonsGroupedByImportid.put(ndflPerson.importId, [ndflPerson])
                } else {
                    group << ndflPerson
                }
            }
        }
        for (List<NdflPersonExt> ndflPersonGroup : ndflPersonsGroupedByImportid.values()) {
            if (!requisitesEquals(ndflPersonGroup)) {
                NdflPerson filePerson = ndflPersonGroup.get(0)
                NdflPerson persistedPerson = persistedPersonsById.get(filePerson.importId)
                logRequisitesComparisionError(persistedPerson ?: filePerson, ndflPersonGroup*.rowIndex as List<Integer>, filePerson.importId)
            }
        }
    }

    /**
     * Проверяет что все объекты Раздела1 РНУ эквивалентны
     * @param personsGroup список обектов Раздела1 для сравнения
     * @return true - если все объекты из {@code personsGroup} равны, иначе false
     */
    boolean requisitesEquals(List<NdflPersonExt> personsGroup) {
        NdflPerson principalPerson = personsGroup.get(0)
        for (int i = 1; i < personsGroup.size(); i++) {
            if (!personsEquals(principalPerson, personsGroup.get(i))) {
                return false
            }
        }
        return true
    }

    /**
     * Добавляет в {@com.aplana.sbrf.taxaccounting.model.log.Logger ошибку указывающую что провалилась проверка,
     * что реквизиты были изменены для всех строк ТФ (Excel), связанных с одной строкой Раздела 1, ранее загруженной в ПНФ}
     * @param fileName название загружаемого ТФ excel
     * @param fileRowNums список номеров строк некорректных реквизитов из файла
     * @param id идентификатор объекта NdflPerson
     */
    void logRequisitesComparisionError(NdflPerson person, List<Integer> fileRowNums, long id) {
        logger.errorExp("Лист \"РНУ НДФЛ\". Строки: \"${fileRowNums.join(", ")}\". Значения реквизитов ФЛ (столбцы 2 - 23) отличаются между собой, " +
                "хотя должны полностью совпадать. ФЛ: ${person.fullName}, ИНП: ${person.inp}, идентификатор строки Раздела 1: \"${id}\".",
                "Не совпадают реквизиты одного ФЛ, указанные в разных строках", "${person.fullName}, ИНП: ${person.inp}")
    }

    void checkPersonId(List<NdflPersonExt> persons) {
        for (def person : persons) {
            if (person.importId && !persistedPersonsById.containsKey(person.importId)) {
                logger.error("Ошибка при загрузке файла \"$fileName\". В строке № \"$person.rowIndex\" указан несуществующий идентификатор строки Раздела 1.")
            }
        }
    }

    /**
     * Проверяет для каждой идОперации из разделов 3 и 4, наличие соответствующей идОперации в разделе 2
     * @param ndflPersonList список физлиц по котрым проходят проверки
     */
    void checkOperationId(List<NdflPersonExt> ndflPersonList) {
        for (NdflPersonExt ndflPerson : ndflPersonList) {
            List<String> operationIdList = ndflPerson.incomes.operationId
            for (def ndflPersonDeduction : ndflPerson.deductions) {
                if (!operationIdList.contains(ndflPersonDeduction.operationId)) {
                    logOperationIdError("№51", "Доход. ID операции", ndflPerson.fullName, ndflPerson.inp, ndflPersonDeduction.operationId, ndflPersonDeduction.rowIndex)
                }
            }
            for (def ndflPersonPrepayment : ndflPerson.prepayments) {
                if (!operationIdList.contains(ndflPersonPrepayment.operationId)) {
                    logOperationIdError("№59", "СведАванс. ID операции", ndflPerson.fullName, ndflPerson.inp, ndflPersonPrepayment.operationId, ndflPersonPrepayment.rowIndex)
                }
            }
        }
    }

    void logOperationIdError(String columnNumber, String columnName, String fio, String inp, String operationId, int fileRowNum) {
        logger.error("Ошибка при загрузке файла \"${fileName}\". В столбце ${columnNumber} \"${columnName}\" указано некорректное значение.")
        logger.error("Для ФЛ (ФИО: \"${fio}\", ИНП: \"${inp}\") указан \"ID операции\": \"${operationId ?: ""}\" в строке № ${fileRowNum} , который не указан в столбце №23 \"ID операции\" ни для одной строки файла по ФЛ")
    }

    void updatePersonsRowNum(List<NdflPersonExt> persons) {
        long rowNum = 0
        long incomeRowNum = 0
        long deductionRowNum = 0
        long prepaymentRowNum = 0
        for (def person : persons) {
            person.rowNum = ++rowNum
            for (def income : person.incomes) {
                income.rowNum = new BigDecimal(++incomeRowNum)
            }
            for (def deduction : person.deductions) {
                deduction.rowNum = new BigDecimal(++deductionRowNum)
            }
            for (def prepayment : person.prepayments) {
                prepayment.rowNum = new BigDecimal(++prepaymentRowNum)
            }
        }
    }

    String[] splitTechnicalCell(Row row) {
        String cellText = row.cell(1).toString()
        if (cellText.isEmpty()) {
            return null
        } else {
            return cellText.split("_")
        }
    }

    /**
     * Удаляет операции из БД, идентификаторы которых не найдены в ТФ
     */
    @Deprecated
    void removeOperations(Map<Long, NdflPerson> persistedPersonsById) {
        Collection<NdflPerson> ndflPersonFromDeclarationData = persistedPersonsById.values()
        int rowsCount = 0
        int personsCount = ndflPersonFromDeclarationData.size()
        int incomesCount = 0
        int deductionsCount = 0
        int prepaymentsCount = 0
        List<LogEntry> removeMessages = []
        List<Long> incomesForRemove = []
        List<Long> deductionsForRemove = []
        List<Long> prepaymentsForRemove = []
        List<Long> personIdsForRemove = []
        for (NdflPerson person : ndflPersonFromDeclarationData) {
            List<Long> incomesIdList = person.incomes*.id
            List<Long> deductionsIdList = person.deductions*.id
            List<Long> prepaymentsIdList = person.prepayments*.id
            int personIncomesCount = incomesIdList.size()
            incomesCount += personIncomesCount
            int personDeductionsCount = deductionsIdList.size()
            deductionsCount += personDeductionsCount
            int personPrepaymentCount = prepaymentsIdList.size()
            prepaymentsCount += personPrepaymentCount
            rowsCount += (personIncomesCount + personDeductionsCount + personPrepaymentCount)
            incomesIdList.removeAll(incomeImportIdList)
            deductionsIdList.removeAll(deductionImportIdList)
            if (emptyDeductionsIds.containsKey(person.id)) {
                deductionsIdList.addAll(emptyDeductionsIds.get(person.id))
            }
            prepaymentsIdList.removeAll(prepaymentImportIdList)
            if (emptyPrepaymentsIds.containsKey(person.id)) {
                prepaymentsIdList.addAll(emptyPrepaymentsIds.get(person.id))
            }
            int personIncomesRemovedCount = incomesIdList.size()
            int personDeductionsRemovedCount = deductionsIdList.size()
            int personPrepaymentsRemovedCount = prepaymentsIdList.size()
            incomesForRemove.addAll(incomesIdList)
            deductionsForRemove.addAll(deductionsIdList)
            prepaymentsForRemove.addAll(prepaymentsIdList)
            def personInfo = "${person.lastName ?: ""} ${person.firstName ?: ""} ${person.middleName ?: ""}, ИНП: ${person.inp ?: ""}, " +
                    "ДУЛ: ${person.idDocType ?: ""}, ${person.idDocNumber ?: ""}"
            if (personIncomesCount == personIncomesRemovedCount) {
                removeMessages << new LogEntry(LogLevel.INFO, "Удалено физическое лицо: $personInfo".toString())
                personIdsForRemove << person.id
            }
            if (personIncomesRemovedCount != 0) {
                removeMessages << new LogEntry(LogLevel.INFO, "Удалены данные у ФЛ: $personInfo. " +
                        "В Разделе ${INCOME_TITLE} удалено строк ($personIncomesRemovedCount)")
            }
            if (personDeductionsRemovedCount != 0) {
                removeMessages << new LogEntry(LogLevel.INFO, "Удалены данные у ФЛ: $personInfo. " +
                        "В Разделе ${DEDUCTION_TITLE} удалено строк ($personDeductionsRemovedCount)")
            }
            if (personPrepaymentsRemovedCount != 0) {
                removeMessages << new LogEntry(LogLevel.INFO, "Удалены данные у ФЛ: $personInfo. " +
                        "В Разделе ${PREPAYMENTS_TITLE} удалено строк ($personPrepaymentsRemovedCount)")
            }
        }
        if (!incomesForRemove.isEmpty()) {
            ndflPersonService.deleteNdflPersonIncome(incomesForRemove)
        }
        if (!deductionsForRemove.isEmpty()) {
            ndflPersonService.deleteNdflPersonDeduction(deductionsForRemove)
        }
        if (!prepaymentsForRemove.isEmpty()) {
            ndflPersonService.deleteNdflPersonPrepayment(prepaymentsForRemove)
        }
        if (!personIdsForRemove.isEmpty()) {
            ndflPersonService.deleteNdflPersonBatch(personIdsForRemove)
        }
        logger.info("При загрузке файла: в Разделе 1 удалено ${personIdsForRemove.size()} строк из $personsCount, " +
                "в Разделе 2 удалено ${incomesForRemove.size()} строк из $incomesCount, " +
                "в Разделе 3 удалено ${deductionsForRemove.size()} строк из $deductionsCount, " +
                "в Разделе 4 удалено ${prepaymentsForRemove.size()} строк из $prepaymentsCount.")
        logger.getEntries().addAll(removeMessages)
    }

    /**
     * Удаляет операции из БД по идентификаторам операций
     */
    void removeOperations(List<Long> incomesIdsForRemove, Collection<NdflPerson> personsFromDeclaration) {
        List<Long> incomesIdsWithRelatedByOperationIdForRemove = []
        List<Long> deductionsIdsForRemove = []
        List<Long> prepaymentsIdsForRemove = []
        List<LogEntry> removeMessages = []
        int incomesCount = 0
        int deductionsCount = 0
        int prepaymentsCount = 0
        for (NdflPerson person : personsFromDeclaration) {
            List<NdflPersonIncome> personIncomes = person.incomes
            incomesCount += personIncomes.size()
            deductionsCount += person.getDeductions().size()
            prepaymentsCount += person.getPrepayments().size()

            Set<String> operationsIdsBySuitableIncomes = new HashSet<>()
            personIncomes.each {
                incomesIdsForRemove.contains(it.id) ?: operationsIdsBySuitableIncomes.add(it.operationId)
            }

            if (operationsIdsBySuitableIncomes.isEmpty()) {
                continue
            }

            def suitableIncomes = ndflPersonService
                    .findNdflPersonIncomeByPersonAndOperations(person.id, operationsIdsBySuitableIncomes)
            incomesIdsWithRelatedByOperationIdForRemove.addAll(suitableIncomes)

            def deductionsIdsByPerson = ndflPersonService.getDeductionsIdsByPersonAndIncomes(person.id, suitableIncomes)
            deductionsIdsForRemove.addAll(deductionsIdsByPerson)
            def prepaymentsIdsByPerson = ndflPersonService.getPrepaymentsIdsByPersonAndIncomes(person.id, suitableIncomes)
            prepaymentsIdsForRemove.addAll(prepaymentsIdsByPerson)

            def personInfo = "${person.lastName ?: ""} ${person.firstName ?: ""} ${person.middleName ?: ""}, ИНП: ${person.inp ?: ""}, " +
                    "ДУЛ: ${person.idDocType ?: ""}, ${person.idDocNumber ?: ""}"

            removeMessages << new LogEntry(LogLevel.INFO, "Удалены данные у ФЛ: $personInfo. " +
                    "В Разделе ${INCOME_TITLE} удалено строк (${suitableIncomes.size()})")

            if (!deductionsIdsByPerson.isEmpty()) {
                removeMessages << new LogEntry(LogLevel.INFO, "Удалены данные у ФЛ: $personInfo. " +
                        "В Разделе ${DEDUCTION_TITLE} удалено строк (${deductionsIdsByPerson.size()})")
            }
            if (!prepaymentsIdsByPerson.isEmpty()) {
                removeMessages << new LogEntry(LogLevel.INFO, "Удалены данные у ФЛ: $personInfo. " +
                        "В Разделе ${PREPAYMENTS_TITLE} удалено строк (${prepaymentsIdsByPerson.size()})")
            }
        }

        ndflPersonService.deleteNdflPersonIncome(incomesIdsWithRelatedByOperationIdForRemove)
        if (!deductionsIdsForRemove.isEmpty()) {
            ndflPersonService.deleteNdflPersonDeduction(deductionsIdsForRemove)
        }
        if (!prepaymentsIdsForRemove.isEmpty()) {
            ndflPersonService.deleteNdflPersonPrepayment(prepaymentsIdsForRemove)
        }
        logger.info("При загрузке файла: " +
                "в Разделе 2 удалено ${incomesIdsWithRelatedByOperationIdForRemove.size()} строк из $incomesCount, " +
                "в Разделе 3 удалено ${deductionsIdsForRemove.size()} строк из $deductionsCount, " +
                "в Разделе 4 удалено ${prepaymentsIdsForRemove.size()} строк из $prepaymentsCount.")
        logger.getEntries().addAll(removeMessages)
    }
    
    /**
     * Создает сообщение о добавлении даннных
     * @param ndflPerson объект физлица
     * @param sectionName название раздела
     * @param newOperationCount количество новых записей
     * @return запись для логгера с сообщением
     */
    LogEntry createNewOperationMessage(NdflPerson ndflPerson, String sectionName, int newOperationCount) {
        return new LogEntry(LogLevel.INFO, String.format("Добавлены данные у ФЛ: (%s %s %s, ИНП: %s, ДУЛ: %s, %s). В Раздел %s добавлено строк (%d)",
                ndflPerson.lastName ?: "",
                ndflPerson.firstName ?: "",
                ndflPerson.middleName ?: "",
                ndflPerson.inp ?: "",
                ndflPerson.idDocType ?: "",
                ndflPerson.idDocNumber ?: "",
                sectionName,
                newOperationCount
        ))
    }

    /**
     * Создает сообщение об изменениии данных у физлица
     * @param ndflPerson объект физлица
     * @param sectionName название раздела
     * @param id иденитфикатор строки
     * @param attrName название изменяемой графы
     * @param oldValue старое значение
     * @param newValue новое значение
     * @return запись для логгера с сообщением
     */
    LogEntry createUpdateOperationMessage(NdflPerson ndflPerson, String sectionName, Long id, String attrName, Object oldValue, Object newValue) {
        return new LogEntry(LogLevel.INFO, String.format("Изменены данные у ФЛ: (%s %s %s, ИНП: %s, ДУЛ: %s, %s). Раздел %s. Идентификатор строки: %d. Обновлена гр. \"%s\". Старое значение: \"%s\". Новое значение: \"%s\"",
                ndflPerson.lastName ?: "",
                ndflPerson.firstName ?: "",
                ndflPerson.middleName ?: "",
                ndflPerson.inp ?: "",
                ndflPerson.idDocType ?: "",
                ndflPerson.idDocNumber ?: "",
                sectionName,
                id,
                attrName,
                oldValue ?: "-",
                newValue ?: "-"
        ))
    }


    class NdflPersonExt extends NdflPerson {
        int rowIndex
        Long importId

        NdflPersonExt(int rowIndex) {
            super()
            this.rowIndex = rowIndex
        }

        @Override
        List<NdflPersonIncomeExt> getIncomes() {
            return super.getIncomes() as List<NdflPersonIncomeExt>
        }

        @Override
        List<NdflPersonDeductionExt> getDeductions() {
            return super.getDeductions() as List<NdflPersonDeductionExt>
        }

        @Override
        List<NdflPersonPrepaymentExt> getPrepayments() {
            return super.getPrepayments() as List<NdflPersonPrepaymentExt>
        }
    }

    int incomeLastRowNum = 0

    class NdflPersonIncomeExt extends NdflPersonIncome {
        int rowIndex

        NdflPersonIncomeExt(int rowIndex) {
            this.rowIndex = rowIndex
            rowNum = ++incomeLastRowNum
        }
    }

    int deductionLastRowNum = 0

    class NdflPersonDeductionExt extends NdflPersonDeduction {
        int rowIndex

        NdflPersonDeductionExt(int rowIndex) {
            this.rowIndex = rowIndex
            rowNum = ++deductionLastRowNum
        }
    }

    int prepaymentLastRowNum = 0

    class NdflPersonPrepaymentExt extends NdflPersonPrepayment {
        int rowIndex

        NdflPersonPrepaymentExt(int rowIndex) {
            this.rowIndex = rowIndex
            rowNum = ++prepaymentLastRowNum
        }
    }

    class Row {
        int index
        List<String> values

        Row(int index, List<String> values) {
            this.index = index
            this.values = values
        }

        Cell cell(int idx) {
            return new Cell(idx, values.get(idx - 1), this)
        }

        boolean isEmpty(List<Integer> range = 1..values.size()) {
            if (values) {
                for (int index : range) {
                    if (values[index - 1]) {
                        return false
                    }
                }
            }
            return true
        }
    }

    class Cell {
        int index
        String value
        Row row

        Cell(int index, String value, Row row) {
            this.index = index
            this.value = value
            this.row = row
        }

        Cell notEmpty() {
            if (!value) {
                logger.error("Ошибка при проверке ячейки файла \"$fileName\". Отсутствует значение для ячейки столбца \"${header[index - 1]}\" " +
                        "№ ${index} для строки ${row.index}.")
            }
            return this
        }

        Date toDate() {
            if (value != null && !value.isEmpty()) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat(SharedConstants.DATE_FORMAT)
                    formatter.setLenient(false)
                    return formatter.parse(value)
                } catch (Exception ignored) {
                    logIncorrectTypeError("Дата")
                }
            }
            return null
        }

        private Integer toInteger(Integer precision = null) {
            return toBigDecimal(precision)?.intValue()
        }

        Long toLong(Integer precision = null) {
            return toBigDecimal(precision)?.longValue()
        }

        BigDecimal toBigDecimal(Integer precision = null, Integer scale = null) {
            assert (precision == null || precision > 0) && (scale == null || scale > 0)
            if (value) {
                try {
                    def bigDecimal = new BigDecimal(value)
                    if (precision != null && bigDecimal.precision() > precision || scale != null && bigDecimal.scale() > scale) {
                        logIncorrectTypeError("${!scale ? "Целое число" : "Число"}${!precision ? "" : "/${precision}${!scale ? "" : ".${scale}/"}"}")
                    }
                    return bigDecimal
                } catch (NumberFormatException ignored) {
                    logIncorrectTypeError("${!scale ? "Целое число" : "Число"}${!precision ? "" : "/${precision}${!scale ? "" : ".${scale}/"}"}")
                }
            }
            return (BigDecimal) null
        }

        String toString(Integer maxLength = null) {
            if (value && maxLength && value.length() > maxLength) {
                logIncorrectTypeError("Строка/${maxLength}/")
            }
            return value
        }

        void logIncorrectTypeError(def type) {
            logger.error("Ошибка при определении значения ячейки файла \"$fileName\". Тип данных ячейки столбца \"${header[index - 1]}\" № " + (index - 1) +
                    " строки ${row.index} не соответствует ожидаемому \"$type\".")
        }
    }

    Period importPeriodCache = null

    /**
     * Возвращяет период загрузки данных
     */
    Period getImportPeriod() {
        if (importPeriodCache == null) {
            def importPeriodRecordsById = refBookFactory.getDataProvider(RefBook.Id.REPORT_PERIOD_IMPORT.getId())
                    .getRecordDataVersionWhere(" where asnu_id = ${declarationData.asnuId} and report_period_type_id = ${reportPeriod.dictTaxPeriodId} ", new Date())
            if (importPeriodRecordsById.size() > 1) {
                def asnu = getAsnuById(declarationData.asnuId)
                def periodType = getReportPeriodTypeById(reportPeriod.dictTaxPeriodId)
                logger.warn("В справочнике \"Дополнительные интервалы для загрузки данных\" найдено более одной записи" +
                        " для АСНУ: \"$asnu.name\", кода периода: $periodType.code и актуальной на текущую дату.")
                importPeriodCache = new Period(reportPeriod.startDate, reportPeriod.endDate)
            } else if (importPeriodRecordsById.isEmpty()) {
                importPeriodCache = new Period(reportPeriod.startDate, reportPeriod.endDate)
            } else {
                Map<String, RefBookValue> record = importPeriodRecordsById.values()[0]
                importPeriodCache = getImportPeriod(record)
            }
        }
        return importPeriodCache
    }

    Period getImportPeriod(Map<String, RefBookValue> record) {
        ReportPeriodImport reportPeriodImport = new ReportPeriodImport()
        reportPeriodImport.startDate = record."PERIOD_START_DATE".dateValue
        reportPeriodImport.endDate = record."PERIOD_END_DATE".dateValue
        reportPeriodImport.reportPeriodType = getReportPeriodTypeById(record."REPORT_PERIOD_TYPE_ID".referenceValue.longValue())
        reportPeriodImport.asnu = getAsnuById(record."ASNU_ID".referenceValue.longValue())
        return getImportPeriod(reportPeriodImport)
    }

    Period getImportPeriod(ReportPeriodImport reportPeriodImport) {
        Date startDate = reportPeriodImport.startDate
        Date endDate = reportPeriodImport.endDate
        int periodYear = reportPeriod.taxPeriod.year
        Date importStartDate = toDate(periodYear, reportPeriodImport.startDate)
        Date importEndDate = toDate(periodYear, reportPeriodImport.endDate)
        if (reportPeriodImport.reportPeriodType.code == "21" &&
                toDate(startDate, 11, 30) <= startDate && startDate <= toDate(startDate, 12, 31)) {
            importStartDate = toDate(periodYear - 1, reportPeriodImport.startDate)
        } else if (reportPeriodImport.reportPeriodType.code == "34" &&
                toDate(endDate, 1, 1) <= endDate && endDate <= toDate(endDate, 2, 5)) {
            importEndDate = toDate(periodYear + 1, reportPeriodImport.endDate)
        }
        return new Period(importStartDate, importEndDate)
    }

    Period calendarPeriodCache = null
    /**
     * Возвращяет период 3х последних месяцев отчетного периода
     */
    Period getCalendarPeriod() {
        if (calendarPeriodCache == null) {
            calendarPeriodCache = new Period(reportPeriod.calendarStartDate, reportPeriod.endDate)
        }
        return calendarPeriodCache
    }

    Date toDate(Date date, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date)
        calendar[Calendar.MONTH] = month - 1
        calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
        return calendar.getTime()
    }

    Date toDate(int year, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date)
        calendar[Calendar.YEAR] = year
        return calendar.getTime()
    }

    RefBookAsnu getAsnuById(long id) {
        return (RefBookAsnu) commonRefBookService.fetchRecord(RefBook.Id.ASNU.getId(), id)
    }

    ReportPeriodType getReportPeriodTypeById(long id) {
        return (ReportPeriodType) commonRefBookService.fetchRecord(RefBook.Id.PERIOD_CODE.getId(), id)
    }

    String getAsnuName() {
        if (declarationData.asnuId) {
            RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId())
            def asnuRecord = asnuProvider.getRecordData(declarationData.asnuId)
            if (asnuRecord == null) {
                logger.error("Не найдена АСНУ с ид = ${declarationData.asnuId}")
            } else {
                return asnuRecord.get("NAME").getStringValue()
            }
        }
        return null
    }

    /**
     * Получить "Коды видов доходов"
     * @return
     */
    Map<Long, Map<String, RefBookValue>> getRefIncomeCodes() {
        // Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
        Map<Long, Map<String, RefBookValue>> mapResult = [:]
        PagingResult<Map<String, RefBookValue>> refBookMap = getRefBook(RefBook.Id.INCOME_CODE.id)
        refBookMap.each { Map<String, RefBookValue> refBook ->
            mapResult.put((Long) refBook?.id?.numberValue, refBook)
        }
        return mapResult
    }

    /**
     * Получить "Виды доходов"
     * @return
     */
    Map<String, List<Long>> getRefIncomeTypes() {
        // Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>
        Map<String, List<Long>> mapResult = [:]
        PagingResult<Map<String, RefBookValue>> refBookList = getRefBook(RefBook.Id.INCOME_KIND.id)
        refBookList.each { Map<String, RefBookValue> refBook ->
            String mark = refBook?.MARK?.stringValue
            List<Long> incomeTypeIdList = mapResult.get(mark)
            if (incomeTypeIdList == null) {
                incomeTypeIdList = []
            }
            incomeTypeIdList.add(refBook?.INCOME_TYPE_ID?.referenceValue)
            mapResult.put(mark, incomeTypeIdList)
        }
        return mapResult
    }

    /**
     * Получить "Коды видов вычетов"
     * @return
     */
    List<String> getRefDeductionCodes() {
        if (!deductionCodes) {
            deductionCodes = []
            PagingResult<Map<String, RefBookValue>> refBookList = getRefBook(RefBook.Id.DEDUCTION_TYPE.id)
            refBookList.each { Map<String, RefBookValue> refBook ->
                deductionCodes.add(refBook?.CODE?.stringValue)
            }
        }
        return deductionCodes
    }

    /**
     * Получить записи справочника по его идентификатору в отчётном периоде
     * @param refBookId - идентификатор справочника
     * @return - список записей справочника
     */
    PagingResult<Map<String, RefBookValue>> getRefBook(long refBookId) {
        // Передаем как аргумент только срок действия версии справочника
        PagingResult<Map<String, RefBookValue>> refBookList = getProvider(refBookId).getRecordsVersion(reportPeriod.startDate, reportPeriod.endDate, null, null)
        return refBookList
    }

    /**
     * Получение провайдера с использованием кеширования.
     * @param providerId
     * @return
     */
    RefBookDataProvider getProvider(long providerId) {
        if (!providerCache.containsKey(providerId)) {
            providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
        }
        return providerCache.get(providerId)
    }

    class Period {
        private Date startDate;
        private Date endDate;

        Period(Date startDate, Date endDate) {
            this.startDate = startDate
            this.endDate = endDate
        }
    }

}
