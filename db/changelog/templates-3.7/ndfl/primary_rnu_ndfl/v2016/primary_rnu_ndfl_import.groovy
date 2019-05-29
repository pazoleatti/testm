package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogEntry
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.ndfl.*
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.service.*
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

            reportPeriod.startDate = toReportPeriodDate(refStartDate)
            reportPeriod.calendarStartDate = toReportPeriodDate(refCalendarStartDate)
            reportPeriod.endDate = toReportPeriodDate(refEndDate)
        }
    }

    Date toReportPeriodDate(Date refDate) {
        Calendar date = Calendar.getInstance();
        date.setTime(refDate)
        date[Calendar.YEAR] = reportPeriod.taxPeriod.year
        return date.getTime()
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

        if (!fileName.endsWith(".xlsx")) {
            logger.error("Ошибка при загрузке файла \"$fileName\". Выбранный файл не соответствует расширению xlsx.")
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            return
        }

        LinkedList<List<String>> allValues = [] as LinkedList
        List<List<String>> headerValues = []
        Map<String, Object> paramsMap = ['rowOffset': 0, 'colOffset': 0] as Map<String, Object>  // отступы сверху и слева для таблицы

        ExcelImportUtils.readSheetsRange(file, allValues, headerValues, HEADER_START_VALUE, 2, paramsMap, 1, null)
        checkHeaders(headerValues)
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            return
        }
        if (allValues.size() == 0) {
            logger.error("Ошибка при загрузке файла \"$fileName\". Отсутствуют данные для загрузки.")
            return
        }

        int TABLE_DATA_START_INDEX = 3
        int rowIndex = TABLE_DATA_START_INDEX
        List<NdflPersonExt> ndflPersonRows = []
        List<NdflPersonExt> ndflPersons = []
        for (def iterator = allValues.iterator(); iterator.hasNext(); rowIndex++) {
            checkInterrupted()

            def row = new Row(rowIndex, iterator.next() as List<String>)
            iterator.remove()
            if (row.isEmpty()) {// все строки пустые - выход
                if (rowIndex == TABLE_DATA_START_INDEX) {
                    logger.error("Ошибка при загрузке файла \"$fileName\". Отсутствуют данные для загрузки.")
                    return
                }
                break
            }
            def ndflPerson = createNdflPerson(row)
            ndflPersonRows << ndflPerson
        }
        Map<Long, NdflPerson> persistedPersonsById = ndflPersonService.findNdflPersonWithOperations(declarationData.id)
                .collectEntries { [it.id, it] }
        checkRequisitesEquality(ndflPersonRows, persistedPersonsById)
        for (NdflPersonExt mergingPerson : ndflPersonRows) {
            merge(ndflPersons, mergingPerson)
        }
        checkPersons(ndflPersons)
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            return
        }

        Collections.sort(ndflPersons, NdflPerson.getComparator())
        ndflPersonService.fillNdflPersonIncomeSortFields((List<? extends NdflPerson>) ndflPersons)
        for (NdflPerson ndflPerson : ndflPersons) {
            Collections.sort(ndflPerson.incomes, NdflPersonIncome.getComparator())
            Collections.sort(ndflPerson.deductions, NdflPersonDeduction.getComparator(ndflPerson))
            Collections.sort(ndflPerson.prepayments, NdflPersonPrepayment.getComparator(ndflPerson))
        }

        // Если в НФ нет данных, то создаем новые из ТФ
        if (persistedPersonsById.isEmpty()) {
            transformOperationId()
            updatePersonsRowNum(ndflPersons)
            if (!logger.containsLevel(LogLevel.ERROR)) {
                checkInterrupted()
                ndflPersonService.save((List<? extends NdflPerson>) ndflPersons)
            } else {
                logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            }
        } else {
            // Если в ТФ есть данные
            // Удаляем операции
            removeOperations(persistedPersonsById)
            List<LogEntry> messages = []

            long personRowNum = 0
            long incomeRowNum = 0
            long deductionRowNum = 0
            long prepaymentRowNum = 0
            List<NdflPerson> updateRowNumPersonList = []
            List<NdflPersonIncome> updateRowNumIncomeList = []
            List<NdflPersonDeduction> updateRowNumDeductionList = []
            List<NdflPersonPrepayment> updateRowNumPrepaymentList = []

            List<NdflPerson> ndflPersonsForCreate = []
            List<NdflPerson> ndflPersonsForUpdate = []
            // Операции для добавления
            List<NdflPersonIncome> incomesForCreate = []
            List<NdflPersonDeduction> deductionsForCreate = []
            List<NdflPersonPrepayment> prepaymentsForCreate = []
            // Операции для обновления
            List<NdflPersonIncome> incomesForUpdate = []
            List<NdflPersonDeduction> deductionsForUpdate = []
            List<NdflPersonPrepayment> prepaymentsForUpdate = []

            for (NdflPersonExt ndflPerson : ndflPersons) {
                ndflPerson.rowNum = ++personRowNum

                if (needPersonUpdate(ndflPerson)) {
                    NdflPerson persistedPerson = null
                    // Проверяем обновлялись ли уже у этого физлица реквизиты
                    if (!ndflPersonImportIdCache.contains(ndflPerson.importId)) {
                        persistedPerson = persistedPersonsById.get(ndflPerson.importId)
                        ndflPersonImportIdCache << ndflPerson.importId
                    }
                    Map<Long, NdflPersonIncome> persistedPersonIncomesById = persistedPerson.incomes.collectEntries {
                        [it.id, it]
                    }
                    Map<Long, NdflPersonDeduction> persistedPersonDeductionsById = persistedPerson.deductions.collectEntries {
                        [it.id, it]
                    }
                    Map<Long, NdflPersonPrepayment> persistedPersonPrepaymentsById = persistedPerson.prepayments.collectEntries {
                        [it.id, it]
                    }

                    if (persistedPerson != null) {
                        persistedPerson.personId = null
                        updateRowNumPersonList << ndflPerson
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
                    }

                    // Определяем списки операций которые будут создаваться либо обновляться у ФЛ
                    int incomesCreateCount = 0
                    for (def income : ndflPerson.incomes) {
                        income.rowNum = new BigDecimal(++incomeRowNum)
                        NdflPersonIncome persistedIncome = null
                        if (income.id != null) {
                            persistedIncome = persistedPersonIncomesById.get(income.id)
                        }
                        if (persistedIncome == null) {
                            income.ndflPersonId = ndflPerson.importId
                            income.modifiedDate = new Date()
                            income.modifiedBy = "${userInfo.getUser().getName()} (${userInfo.getUser().getLogin()})"
                            incomesForCreate << income
                            incomesCreateCount++
                        } else {
                            updateRowNumIncomeList << income
                            if (income.incomeCode?.isEmpty()) income.incomeCode = null
                            if (income.incomeType?.isEmpty()) income.incomeType = null
                            if (income.paymentNumber?.isEmpty()) income.paymentNumber = null
                            boolean updated = false
                            if (income.operationId != persistedIncome.operationId) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, INCOME_OPERATION_ID, persistedIncome.operationId, income.operationId)
                                persistedIncome.operationId = income.operationId
                                updated = true
                            }
                            if (income.incomeCode != persistedIncome.incomeCode) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, INCOME_CODE, persistedIncome.incomeCode, income.incomeCode)
                                persistedIncome.incomeCode = income.incomeCode
                                updated = true
                            }
                            if (income.incomeType != persistedIncome.incomeType) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, INCOME_TYPE, persistedIncome.incomeType, income.incomeType)
                                persistedIncome.incomeType = income.incomeType
                                updated = true
                            }
                            if (income.incomeAccruedDate != persistedIncome.incomeAccruedDate) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, INCOME_ACCRUED_DATE, persistedIncome.incomeAccruedDate?.format(SharedConstants.DATE_FORMAT), income.incomeAccruedDate?.format(SharedConstants.DATE_FORMAT))
                                persistedIncome.incomeAccruedDate = income.incomeAccruedDate
                                updated = true
                            }
                            if (income.incomePayoutDate != persistedIncome.incomePayoutDate) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, INCOME_PAYOUT_DATE, persistedIncome.incomePayoutDate?.format(SharedConstants.DATE_FORMAT), income.incomePayoutDate?.format(SharedConstants.DATE_FORMAT))
                                persistedIncome.incomePayoutDate = income.incomePayoutDate
                                updated = true
                            }
                            if (income.kpp != persistedIncome.kpp) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, KPP, persistedIncome.kpp, income.kpp)
                                persistedIncome.kpp = income.kpp
                                updated = true
                            }
                            if (income.oktmo != persistedIncome.oktmo) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, OKTMO, persistedIncome.oktmo, income.oktmo)
                                persistedIncome.oktmo = income.oktmo
                                updated = true
                            }
                            if (income.incomeAccruedSumm != persistedIncome.incomeAccruedSumm) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, INCOME_ACCRUED_SUMM, persistedIncome.incomeAccruedSumm?.toString(), income.incomeAccruedSumm?.toString())
                                persistedIncome.incomeAccruedSumm = income.incomeAccruedSumm
                                updated = true
                            }
                            if (income.incomePayoutSumm != persistedIncome.incomePayoutSumm) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, INCOME_PAYOUT_SUMM, persistedIncome.incomePayoutSumm?.toString(), income.incomePayoutSumm?.toString())
                                persistedIncome.incomePayoutSumm = income.incomePayoutSumm
                                updated = true
                            }
                            if (income.totalDeductionsSumm != persistedIncome.totalDeductionsSumm) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, TOTAL_DEDUCTIONS_SUMM, persistedIncome.totalDeductionsSumm?.toString(), income.totalDeductionsSumm?.toString())
                                persistedIncome.totalDeductionsSumm = income.totalDeductionsSumm
                                updated = true
                            }
                            if (income.taxBase != persistedIncome.taxBase) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, TAX_BASE, persistedIncome.taxBase?.toString(), income.taxBase?.toString())
                                persistedIncome.taxBase = income.taxBase
                                updated = true
                            }
                            if (income.taxRate != persistedIncome.taxRate) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, TAX_RATE, persistedIncome.taxRate?.toString(), income.taxRate?.toString())
                                persistedIncome.taxRate = income.taxRate
                                updated = true
                            }
                            if (income.taxDate != persistedIncome.taxDate) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, TAX_DATE, persistedIncome.taxDate?.format(SharedConstants.DATE_FORMAT), income.taxDate?.format(SharedConstants.DATE_FORMAT))
                                persistedIncome.taxDate = income.taxDate
                                updated = true
                            }
                            if (income.calculatedTax != persistedIncome.calculatedTax) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, CALCULATED_TAX, persistedIncome.calculatedTax?.toString(), income.calculatedTax?.toString())
                                persistedIncome.calculatedTax = income.calculatedTax
                                updated = true
                            }
                            if (income.withholdingTax != persistedIncome.withholdingTax) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, WITHHOLDING_TAX, persistedIncome.withholdingTax?.toString(), income.withholdingTax?.toString())
                                persistedIncome.withholdingTax = income.withholdingTax
                                updated = true
                            }
                            if (income.notHoldingTax != persistedIncome.notHoldingTax) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, NOT_HOLDING_TAX, persistedIncome.notHoldingTax?.toString(), income.notHoldingTax?.toString())
                                persistedIncome.notHoldingTax = income.notHoldingTax
                                updated = true
                            }
                            if (income.overholdingTax != persistedIncome.overholdingTax) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, OVERHOLDING_TAX, persistedIncome.overholdingTax?.toString(), income.overholdingTax?.toString())
                                persistedIncome.overholdingTax = income.overholdingTax
                                updated = true
                            }
                            if (income.refoundTax != persistedIncome.refoundTax) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, REFOUND_TAX, persistedIncome.refoundTax?.toString(), income.refoundTax?.toString())
                                persistedIncome.refoundTax = income.refoundTax
                                updated = true
                            }
                            if (income.taxTransferDate != persistedIncome.taxTransferDate) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, TAX_TRANSFER_DATE,
                                        persistedIncome.taxTransferDate?.format(SharedConstants.DATE_FORMAT) != SharedConstants.DATE_ZERO_AS_DATE ? persistedIncome.taxTransferDate?.format(SharedConstants.DATE_FORMAT) : SharedConstants.DATE_ZERO_AS_STRING,
                                        income.taxTransferDate?.format(SharedConstants.DATE_FORMAT) != SharedConstants.DATE_ZERO_AS_DATE ? income.taxTransferDate?.format(SharedConstants.DATE_FORMAT) : SharedConstants.DATE_ZERO_AS_STRING)
                                persistedIncome.taxTransferDate = income.taxTransferDate
                                updated = true
                            }
                            if (income.paymentDate != persistedIncome.paymentDate) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, PAYMENT_DATE, persistedIncome.paymentDate?.format(SharedConstants.DATE_FORMAT), income.paymentDate?.format(SharedConstants.DATE_FORMAT))
                                persistedIncome.paymentDate = income.paymentDate
                                updated = true
                            }
                            if (income.paymentNumber != persistedIncome.paymentNumber) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, PAYMENT_NUMBER, persistedIncome.paymentNumber, income.paymentNumber)
                                persistedIncome.paymentNumber = income.paymentNumber
                                updated = true
                            }
                            if (income.taxSumm != persistedIncome.taxSumm) {
                                messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, TAX_SUMM, persistedIncome.taxSumm?.toString(), income.taxSumm?.toString())
                                persistedIncome.taxSumm = income.taxSumm
                                updated = true
                            }
                            if (updated) {
                                persistedIncome.modifiedDate = new Date()
                                persistedIncome.modifiedBy = "${userInfo.getUser().getName()} (${userInfo.getUser().getLogin()})"
                                incomesForUpdate << persistedIncome
                            }
                        }
                    }
                    if (incomesCreateCount > 0) {
                        messages << createNewOperationMessage(ndflPerson, "Сведения о доходах и НДФЛ", incomesCreateCount)
                    }
                    int deductionsCreateCount = 0
                    for (NdflPersonDeduction deduction : ndflPerson.deductions) {
                        deduction.rowNum = new BigDecimal(++deductionRowNum)
                        NdflPersonDeduction persistedDeduction = null
                        if (deduction.id != null) {
                            persistedDeduction = persistedPersonDeductionsById.get(deduction.id)
                        }
                        if (persistedDeduction == null) {
                            deduction.ndflPersonId = ndflPerson.importId
                            deduction.modifiedDate = new Date()
                            deduction.modifiedBy = "${userInfo.getUser().getName()} (${userInfo.getUser().getLogin()})"
                            deductionsForCreate << deduction
                            deductionsCreateCount++
                        } else {
                            updateRowNumDeductionList << deduction
                            boolean updated = false
                            if (deduction.typeCode != persistedDeduction.typeCode) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, TYPE_CODE, persistedDeduction.typeCode, deduction.typeCode)
                                persistedDeduction.typeCode = deduction.typeCode
                                updated = true
                            }
                            if (deduction.notifType != persistedDeduction.notifType) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, NOTIF_TYPE, persistedDeduction.notifType, deduction.notifType)
                                persistedDeduction.notifType = deduction.notifType
                                updated = true
                            }
                            if (deduction.notifDate != persistedDeduction.notifDate) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, NOTIF_DATE, persistedDeduction.notifDate?.format(SharedConstants.DATE_FORMAT), deduction.notifDate?.format(SharedConstants.DATE_FORMAT))
                                persistedDeduction.notifDate = deduction.notifDate
                                updated = true
                            }
                            if (deduction.notifNum != persistedDeduction.notifNum) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, NOTIF_NUM, persistedDeduction.notifNum, deduction.notifNum)
                                persistedDeduction.notifNum = deduction.notifNum
                                updated = true
                            }
                            if (deduction.notifSource != persistedDeduction.notifSource) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, NOTIF_SOURCE, persistedDeduction.notifSource, deduction.notifSource)
                                persistedDeduction.notifSource = deduction.notifSource
                                updated = true
                            }
                            if (deduction.notifSumm != persistedDeduction.notifSumm) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, NOTIF_SUM, persistedDeduction.notifSumm?.toString(), deduction.notifSumm?.toString())
                                persistedDeduction.notifSumm = deduction.notifSumm
                                updated = true
                            }
                            if (deduction.operationId != persistedDeduction.operationId) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, DEDUCTION_OPERATION_ID, persistedDeduction.operationId, deduction.operationId)
                                persistedDeduction.operationId = deduction.operationId
                                updated = true
                            }
                            if (deduction.incomeAccrued != persistedDeduction.incomeAccrued) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, INCOME_ACCRUED, persistedDeduction.incomeAccrued?.format(SharedConstants.DATE_FORMAT), deduction.incomeAccrued?.format(SharedConstants.DATE_FORMAT))
                                persistedDeduction.incomeAccrued = deduction.incomeAccrued
                                updated = true
                            }
                            if (deduction.incomeCode != persistedDeduction.incomeCode) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, DEDUCTION_INCOME_CODE, persistedDeduction.incomeCode, deduction.incomeCode)
                                persistedDeduction.incomeCode = deduction.incomeCode
                                updated = true
                            }
                            if (deduction.incomeSumm != persistedDeduction.incomeSumm) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, INCOME_SUMM, persistedDeduction.incomeSumm?.toString(), deduction.incomeSumm?.toString())
                                persistedDeduction.incomeSumm = deduction.incomeSumm
                                updated = true
                            }
                            if (deduction.periodPrevDate != persistedDeduction.periodPrevDate) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, PERIOD_PREV_DATE, persistedDeduction.periodPrevDate?.format(SharedConstants.DATE_FORMAT), deduction.periodPrevDate?.format(SharedConstants.DATE_FORMAT))
                                persistedDeduction.periodPrevDate = deduction.periodPrevDate
                                updated = true
                            }
                            if (deduction.periodPrevSumm != persistedDeduction.periodPrevSumm) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, PERIOD_PREV_SUMM, persistedDeduction.periodPrevSumm?.toString(), deduction.periodPrevSumm?.toString())
                                persistedDeduction.periodPrevSumm = deduction.periodPrevSumm
                                updated = true
                            }
                            if (deduction.periodCurrDate != persistedDeduction.periodCurrDate) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, PERIOD_CURR_DATE, persistedDeduction.periodCurrDate?.format(SharedConstants.DATE_FORMAT), deduction.periodCurrDate?.format(SharedConstants.DATE_FORMAT))
                                persistedDeduction.periodCurrDate = deduction.periodCurrDate
                                updated = true
                            }
                            if (deduction.periodCurrSumm != persistedDeduction.periodCurrSumm) {
                                messages << createUpdateOperationMessage(ndflPerson, DEDUCTION_TITLE, deduction.id, PERIOD_CURR_SUMM, persistedDeduction.periodCurrSumm?.toString(), deduction.periodCurrSumm?.toString())
                                persistedDeduction.periodCurrSumm = deduction.periodCurrSumm
                                updated = true
                            }
                            if (updated) {
                                persistedDeduction.modifiedDate = new Date()
                                persistedDeduction.modifiedBy = "${userInfo.getUser().getName()} (${userInfo.getUser().getLogin()})"
                                deductionsForUpdate << persistedDeduction
                            }
                        }
                    }
                    if (deductionsCreateCount > 0) {
                        messages << createNewOperationMessage(ndflPerson, "Сведения о вычетах", deductionsCreateCount)
                    }
                    int prepaymentsCreateCount = 0
                    for (NdflPersonPrepayment prepayment : ndflPerson.prepayments) {
                        prepayment.rowNum = new BigDecimal(++prepaymentRowNum)
                        NdflPersonPrepayment persistedPrepayment = null
                        if (prepayment.id != null) {
                            persistedPrepayment = persistedPersonPrepaymentsById.get(prepayment.id)
                        }
                        if (persistedPrepayment == null) {
                            prepayment.ndflPersonId = ndflPerson.importId
                            prepaymentsForCreate << prepayment
                            prepayment.modifiedDate = new Date()
                            prepayment.modifiedBy = "${userInfo.getUser().getName()} (${userInfo.getUser().getLogin()})"
                            prepaymentsCreateCount++
                        } else {
                            updateRowNumPrepaymentList << prepayment
                            boolean updated = false
                            if (prepayment.operationId != persistedPrepayment.operationId) {
                                messages << createUpdateOperationMessage(ndflPerson, PREPAYMENTS_TITLE, prepayment.id, PREPAYMENT_OPERATION_ID, persistedPrepayment.operationId, prepayment.operationId)
                                persistedPrepayment.operationId = prepayment.operationId
                                updated = true
                            }
                            if (prepayment.summ != persistedPrepayment.summ) {
                                messages << createUpdateOperationMessage(ndflPerson, PREPAYMENTS_TITLE, prepayment.id, PREPAYMENT_SUMM, persistedPrepayment.summ?.toString(), prepayment.summ?.toString())
                                persistedPrepayment.summ = prepayment.summ
                                updated = true
                            }
                            if (prepayment.notifNum != persistedPrepayment.notifNum) {
                                messages << createUpdateOperationMessage(ndflPerson, PREPAYMENTS_TITLE, prepayment.id, PREPAYMENT_NOTIF_NUM, persistedPrepayment.notifNum, prepayment.notifNum)
                                persistedPrepayment.notifNum = prepayment.notifNum
                                updated = true
                            }
                            if (prepayment.notifDate != persistedPrepayment.notifDate) {
                                messages << createUpdateOperationMessage(ndflPerson, PREPAYMENTS_TITLE, prepayment.id, PREPAYMENT_NOTIF_DATE, persistedPrepayment.notifDate?.format(SharedConstants.DATE_FORMAT), prepayment.notifDate?.format(SharedConstants.DATE_FORMAT))
                                persistedPrepayment.notifDate = prepayment.notifDate
                                updated = true
                            }
                            if (prepayment.notifSource != persistedPrepayment.notifSource) {
                                messages << createUpdateOperationMessage(ndflPerson, PREPAYMENTS_TITLE, prepayment.id, PREPAYMENT_NOTIF_SOURCE, persistedPrepayment.notifSource, prepayment.notifSource)
                                persistedPrepayment.notifSource = prepayment.notifSource
                                updated = true
                            }
                            if (updated) {
                                persistedPrepayment.modifiedDate = new Date()
                                persistedPrepayment.modifiedBy = "${userInfo.getUser().getName()} (${userInfo.getUser().getLogin()})"
                                prepaymentsForUpdate << persistedPrepayment
                            }
                        }
                    }
                    if (prepaymentsCreateCount > 0) {
                        messages << createNewOperationMessage(ndflPerson, "Сведения о доходах в виде авансовых платежей", prepaymentsCreateCount)
                    }
                } else {
                    transformOperationId()
                    for (def income : ndflPerson.incomes) {
                        income.rowNum = new BigDecimal(++incomeRowNum)
                    }
                    for (def deduction : ndflPerson.deductions) {
                        deduction.rowNum = new BigDecimal(++deductionRowNum)
                    }
                    for (def prepayment : ndflPerson.prepayments) {
                        prepayment.rowNum = new BigDecimal(++prepaymentRowNum)
                    }
                    ndflPersonsForCreate << ndflPerson
                }
            }
            ndflPersonService.save(ndflPersonsForCreate)
            ndflPersonService.updateNdflPersons(ndflPersonsForUpdate)
            if (ndflPersonsForCreate || ndflPersonsForUpdate) {
                refBookPersonService.clearRnuNdflPerson(declarationData.id)
            }
            ndflPersonService.saveIncomes(incomesForCreate)
            ndflPersonService.saveDeductions(deductionsForCreate)
            ndflPersonService.savePrepayments(prepaymentsForCreate)
            ndflPersonService.updateIncomes(incomesForUpdate)
            ndflPersonService.updateDeductions(deductionsForUpdate)
            ndflPersonService.updatePrepayments(prepaymentsForUpdate)

            logger.getEntries().addAll(messages)

            ndflPersonService.updateNdflPersonsRowNum(updateRowNumPersonList)
            ndflPersonService.updateIncomesRowNum(updateRowNumIncomeList)
            ndflPersonService.updateDeductionsRowNum(updateRowNumDeductionList)
            ndflPersonService.updatePrepaymentsRowNum(updateRowNumPrepaymentList)
        }

        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
        }
    }

    /**
     * Проверяет имеет ли ФЛ операции с идентификаторами имеющимися в НФ. Результат используется для определния нужно ли обновлять Физлицо или создавать
     * @param ndflPerson Объект физлица
     * @return true если физлицо нужно обновлять
     */
    boolean needPersonUpdate(NdflPerson ndflPerson) {
        for (NdflPersonIncome income : ndflPerson.incomes) {
            if (income.id != null && ndflPersonService.checkIncomeExists(income.id, declarationData.id)) return true
        }
        for (NdflPersonDeduction deduction : ndflPerson.deductions) {
            if (deduction.id != null && ndflPersonService.checkDeductionExists(deduction.id, declarationData.id)) return true
        }
        for (NdflPersonPrepayment prepayment : ndflPerson.prepayments) {
            if (prepayment.id != null && ndflPersonService.checkPrepaymentExists(prepayment.id, declarationData.id)) return true
        }
        return false
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
        String[] importIds = splitTechnicalCell(row.cell(1).toString())
        Long incomeImportId = null
        Long deductionImportId = null
        Long prepaymentImportId = null
        NdflPersonExt ndflPerson = new NdflPersonExt(row.index)
        if (importIds != null) {
            ndflPerson.importId = Long.valueOf(importIds[0])
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
        ndflPerson.inp = row.cell(3).toString(25)
        ndflPerson.lastName = row.cell(4).toString(60)
        ndflPerson.firstName = row.cell(5).toString(60)
        ndflPerson.middleName = row.cell(6).toString(60)
        ndflPerson.birthDay = row.cell(7).toDate()
        ndflPerson.citizenship = row.cell(8).toString(3)
        ndflPerson.innNp = row.cell(9).toString(12)
        ndflPerson.innForeign = row.cell(10).toString(50)
        ndflPerson.idDocType = row.cell(11).toString(2)
        ndflPerson.idDocNumber = row.cell(12).toString(25)
        ndflPerson.status = row.cell(13).toInteger(1)?.toString()
        ndflPerson.regionCode = row.cell(14).toString(2)
        ndflPerson.postIndex = row.cell(15).toString(6)
        ndflPerson.area = row.cell(16).toString(50)
        ndflPerson.city = row.cell(17).toString(50)
        ndflPerson.locality = row.cell(18).toString(50)
        ndflPerson.street = row.cell(19).toString(50)
        ndflPerson.house = row.cell(20).toString(20)
        ndflPerson.building = row.cell(21).toString(20)
        ndflPerson.flat = row.cell(22).toString(20)
        ndflPerson.snils = row.cell(23).toString(14)
        ndflPerson.asnuId = declarationData.asnuId
        ndflPerson.modifiedDate = importDate

        if (!row.isEmpty(24..45)) {
            ndflPerson.incomes.add(createIncome(row, incomeImportId))
        }

        if (!row.isEmpty(46..59)) {
            ndflPerson.deductions.add(createDeduction(row, deductionImportId))
        } else if (deductionImportId){
            if (emptyDeductionsIds.containsKey(ndflPerson.importId)) {
                emptyDeductionsIds.get(ndflPerson.importId) << deductionImportId
            } else {
                emptyDeductionsIds.put(ndflPerson.importId, [deductionImportId])
            }
        }

        if (!row.isEmpty(60..64)) {
            ndflPerson.prepayments.add(createPrepayment(row, prepaymentImportId))
        } else if (prepaymentImportId){
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
        if (!operationsGrouped.keySet().isEmpty()) {
            List<List<String>> operationsForSeek = operationsGrouped.keySet().collate(1000)
            List<String> persistedOperationIdList = []
            for (List<String> operationsPart : operationsForSeek) {
                persistedOperationIdList.addAll(ndflPersonService.findIncomeOperationId(operationsPart))
            }
            Set<String> operationsForTransform = operationsGrouped.keySet()
            operationsForTransform.removeAll(persistedOperationIdList)
            for (String operationId : operationsForTransform) {
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

        for (List<NdflPersonIncomeExt> incomesGroup : incomesGroupedByOperationId.values()) {
            boolean checkPassed = isIncomeDatesInPeriod(incomesGroup)
            if (!checkPassed) {
                logger.error("Для ФЛ: \"${person.fullName}\", ИНП: \"${person.inp ?: "_"}\" операция \"${incomesGroup[0].operationId}\" " +
                        "не загружена в Налоговую форму №: \"${declarationData.id}\", Период: " +
                        "\"${reportPeriod.taxPeriod.year}, ${reportPeriod.name}\", Подразделение: \"${department.name}\", Вид: \"${declarationTemplate.name}\"" +
                        "${asnuName ? ", АСНУ: \"${asnuName}\"" : ""}. Ни одна из дат операции: \"Дата начисления дохода\", " +
                        "\"Дата выплаты дохода\", \"Дата НДФЛ\", \"Дата платежного поручения\" не соответствуют периоду формы.")
            }
            allRecordsFailed &= !checkPassed
        }
        if (allRecordsFailed) {
            logger.error("Для ФЛ (ФИО: \"${person.fullName}\", ИНП: \"${person.inp}\") " +
                    "отсутствуют операции, принадлежащие периоду формы:\"${reportPeriod.taxPeriod.year}, ${reportPeriod.name}\". " +
                    "Операции по ФЛ не загружены в Налоговую форму №: \"${declarationData.id}\", " +
                    "Период: \"${reportPeriod.taxPeriod.year}, ${reportPeriod.name}\", " +
                    "Подразделение: \"${department.name}\", Вид: \"${declarationTemplate.name}\"" +
                    "${asnuName ? ", АСНУ: \"${asnuName}\"" : ""}.")
        }
        return !allRecordsFailed
    }

    boolean isIncomeDatesInPeriod(List<NdflPersonIncomeExt> incomeGroup) {
        List<NdflPersonIncomeExt> incomesWithAccruedDate = incomeGroup.findAll { it.incomeAccruedDate }
        boolean isAnyAccruedDateInPeriod = incomesWithAccruedDate.any { isDateInReportPeriod(it.incomeAccruedDate) }
        if (isAnyAccruedDateInPeriod) return true

        List<NdflPersonIncomeExt> incomesWithPayoutDate = incomeGroup.findAll { it.incomePayoutDate }
        boolean isAnyPayoutDateInPeriod = incomesWithPayoutDate.any { isDateInReportPeriod(it.incomePayoutDate) }
        if (isAnyPayoutDateInPeriod) return true

        List<NdflPersonIncomeExt> incomesWithTaxDate = incomeGroup.findAll { it.taxDate }
        boolean isAnyTaxDateInPeriod = incomesWithTaxDate.any { isDateInReportPeriod(it.taxDate) }
        if (isAnyTaxDateInPeriod) return true

        // Если хотя бы одна дата была, валидация не пройдена
        if (incomesWithAccruedDate || incomesWithPayoutDate || incomesWithTaxDate) {
            return false
        }

        // Если все даты выше пустые, но есть хотя бы одна дата платежного поручения, тоже берём строки
        List<NdflPersonIncomeExt> incomesWithPaymentDate = incomeGroup.findAll { it.paymentDate }
        boolean isAnyPaymentDateInPeriod = incomesWithPaymentDate.any { isDateInReportPeriod(it.paymentDate) }
        if (isAnyPaymentDateInPeriod) return true

        return false
    }

    boolean isDateInReportPeriod(Date date) {
        return (reportPeriod.calendarStartDate <= date && date <= reportPeriod.endDate)
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

    /**
     * Отвечает за проверки, что реквизиты были изменены для всех строк ТФ (Excel),
     * связанных с одной строкой Раздела 1, ранее загруженной в ПНФ
     * @param ndflPersonList список всех объектов раздела 1
     */
    void checkRequisitesEquality(List<NdflPersonExt> ndflPersonList, Map<Long, NdflPerson> persistedPersonsById) {
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

    String[] splitTechnicalCell(String cellText) {
        if (cellText.isEmpty()) {
            return null
        } else {
            return cellText.split("_")
        }
    }

    /**
     * Удаляет операции из БД, идентификаторы которых не найдены в ТФ
     */
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

}
