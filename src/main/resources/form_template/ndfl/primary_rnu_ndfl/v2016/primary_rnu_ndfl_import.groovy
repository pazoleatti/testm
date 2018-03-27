package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogEntry
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonOperation
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.service.*

import java.text.SimpleDateFormat

import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.readSheetsRange
import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.checkInterrupted

new Import(this).run()

@SuppressWarnings("GrMethodMayBeStatic")
class Import extends AbstractScriptClass {

    final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"
    final String LOG_TYPE_PERSON_MSG_2 = "Значение гр. \"%s\" (\"%s\") отсутствует в справочнике \"%s\""
    final String LOG_TYPE_REFERENCES = "Значение не соответствует справочнику \"%s\""
    final String C_INCOME_CODE = "Код дохода"
    final String R_INCOME_CODE = "Коды видов доходов"
    final String C_TYPE_CODE = "Код вычета"
    final String R_TYPE_CODE = "Коды видов вычетов"
    final String SECTION_LINE_MSG = "Раздел %s. Строка %s"
    final String T_PERSON_INCOME = "2" // "Сведения о доходах и НДФЛ"
    final String T_PERSON_DEDUCTION = "3" // "Сведения о вычетах"
    final String INCOME_TITLE = "Сведения о доходах и НДФЛ"
    final String DEDUCTION_TITLE = "Сведения о вычетах"
    final String PREPAYMENTS_TITLE = "Сведения о доходах в виде авансовых платежей"
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

    // Список идентификаторов импортируемых доходов
    List<Long> incomeImportIdList = []

    // Список идентификаторов импортируемых вычетов
    List<Long> deductionImportIdList = []

    // Список идентификаторов импортируемых авансов
    List<Long> prepaymentImportIdList = []

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

        reportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId).reportPeriod
        department = departmentService.get(declarationData.departmentId)
        declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        asnuName = getAsnuName()

        incomeCodes = getRefIncomeCodes()
        incomeTypeMap = getRefIncomeTypes()
        deductionCodes = getRefDeductionCodes()
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

        LinkedList<List<String>> allValues = []
        List<List<String>> headerValues = []
        Map<String, Object> paramsMap = ['rowOffset': 0, 'colOffset': 0]  // отступы сверху и слева для таблицы

        readSheetsRange(file, allValues, headerValues, null, null, 2, paramsMap, 1, null)
        checkHeaders(headerValues)
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            return
        }

        int TABLE_DATA_START_INDEX = 3
        int rowIndex = TABLE_DATA_START_INDEX
        List<NdflPerson> ndflPersons = []
        for (def iterator = allValues.iterator(); iterator.hasNext(); rowIndex++) {
            checkInterrupted()

            def row = new Row(index: rowIndex, values: iterator.next())
            iterator.remove()
            if (row.isEmpty()) {// все строки пустые - выход
                if (rowIndex == TABLE_DATA_START_INDEX) {
                    logger.error("Ошибка при загрузке файла \"$fileName\". Отсутствуют данные для загрузки.")
                    return
                }
                break
            }

            def ndflPerson = createNdflPerson(row)
            merge(ndflPersons, ndflPerson)
        }

        // Если в НФ нет данных, то создаем новые из ТФ
        if (ndflPersonService.findNdflPersonCountByParameters(declarationData.id, [:]) == 0) {
            transformOperationId()
            updatePersonsRowNum(ndflPersons)
            if (!logger.containsLevel(LogLevel.ERROR)) {
                checkInterrupted()

                ndflPersonService.save(ndflPersons)
            } else {
                logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            }
        } else {
            // Если в ТФ есть данные
            // Удаляем операции
            removeOperations()
            List<LogEntry> messages = []
            // Сохраненные физлица в бд
            List<NdflPerson> persistedPersons = ndflPersonService.findNdflPerson(declarationData.id)
            Map<Integer, List<NdflPerson>> ndflPersonPersistedCache = [:]
            for (NdflPerson persistedPerson : persistedPersons) {
                int hash = Objects.hash(persistedPerson.inp, persistedPerson.lastName, persistedPerson.firstName, persistedPerson.middleName,
                        persistedPerson.birthDay, persistedPerson.snils, persistedPerson.citizenship, persistedPerson.innNp,
                        persistedPerson.innForeign, persistedPerson.idDocType, persistedPerson.idDocNumber, persistedPerson.status,
                        persistedPerson.regionCode, persistedPerson.postIndex, persistedPerson.area, persistedPerson.city,
                        persistedPerson.locality, persistedPerson.street, persistedPerson.house, persistedPerson.building, persistedPerson.flat)
                List<NdflPerson> cached = ndflPersonPersistedCache.get(hash)
                if (cached == null) {
                    ndflPersonPersistedCache.put(hash, [persistedPerson])
                } else {
                    cached << ndflPersonPersistedCache
                }
            }
            long personRowNum = 0
            long incomeRowNum = 0
            long deductionRowNum = 0
            long prepaymentRowNum = 0
            List<NdflPerson> updateRowNumPersonList = []
            List<NdflPersonIncome> updateRowNumIncomeList = []
            List<NdflPersonDeduction> updateRowNumDeductionList = []
            List<NdflPersonPrepayment> updateRowNumPrepaymentList = []

            List<NdflPerson> ndflPersonsForCreate = []

            // Операции для добавления
            List<NdflPersonIncome> incomesForCreate = []
            List<NdflPersonDeduction> deductionsForCreate = []
            List<NdflPersonPrepayment> prepaymentsForCreate = []
            // Операции для обновления
            List<NdflPersonIncome> incomesForUpdate = []
            List<NdflPersonDeduction> deductionsForUpdate = []
            List<NdflPersonPrepayment> prepaymentsForUpdate = []
            // Ведем поиск полностью совпадающих ФЛ из ТФ и БД, сначала по хеш-коду полей, затем по equals
            for (NdflPerson ndflPerson : ndflPersons) {
                ndflPerson.rowNum = ++personRowNum
                if (ndflPerson.snils?.isEmpty()) ndflPerson.snils = null
                if (ndflPerson.middleName?.isEmpty()) ndflPerson.middleName = null
                if (ndflPerson.innNp?.isEmpty()) ndflPerson.innNp = null
                if (ndflPerson.innForeign?.isEmpty()) ndflPerson.innForeign = null
                if (ndflPerson.area?.isEmpty()) ndflPerson.area = null
                if (ndflPerson.city?.isEmpty()) ndflPerson.city = null
                if (ndflPerson.locality?.isEmpty()) ndflPerson.locality = null
                if (ndflPerson.street?.isEmpty()) ndflPerson.street = null
                if (ndflPerson.house?.isEmpty()) ndflPerson.house = null
                if (ndflPerson.building?.isEmpty()) ndflPerson.building = null
                if (ndflPerson.flat?.isEmpty()) ndflPerson.flat = null
                int hash = Objects.hash(ndflPerson.inp, ndflPerson.lastName, ndflPerson.firstName, ndflPerson.middleName,
                        ndflPerson.birthDay, ndflPerson.snils, ndflPerson.citizenship, ndflPerson.innNp,
                        ndflPerson.innForeign, ndflPerson.idDocType, ndflPerson.idDocNumber, ndflPerson.status,
                        ndflPerson.regionCode, ndflPerson.postIndex, ndflPerson.area, ndflPerson.city,
                        ndflPerson.locality, ndflPerson.street, ndflPerson.house, ndflPerson.building, ndflPerson.flat)
                for (NdflPerson persistedPerson : ndflPersonPersistedCache.get(hash)) {
                    if (personsEquals(persistedPerson, ndflPerson)) {
                        ndflPerson.id = persistedPerson.id
                    } else {
                        ndflPerson.id = null
                    }
                }
                // Флаг показывающий нужно ли ФЛ обновлять или создавать
                boolean needPersonUpdate = needPersonUpdate(ndflPerson);
                // У физлица id будет null если его реквизиты не совпали с физлицом из БД
                if (ndflPerson.id != null) {
                    if (needPersonUpdate) {
                        // Определяем списки операций которые будут создаваться либо обновляться у ФЛ
                        int incomesCreateCount = 0
                        for (NdflPersonIncome income : ndflPerson.incomes) {
                            income.rowNum = ++incomeRowNum
                            NdflPersonIncome persistedIncome = null
                            if (income.id != null) {
                                persistedIncome = ndflPersonService.getIncome(income.id)
                            }
                            if (income.id == null || persistedIncome == null) {
                                income.ndflPersonId = ndflPerson.id
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
                                    messages << createUpdateOperationMessage(ndflPerson, INCOME_TITLE, income.id, TAX_TRANSFER_DATE, persistedIncome.taxTransferDate?.format(SharedConstants.DATE_FORMAT), income.taxTransferDate?.format(SharedConstants.DATE_FORMAT))
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
                                    persistedIncome.modifiedBy = userInfo.getUser().getName()
                                    incomesForUpdate << persistedIncome
                                }
                            }
                        }
                        if (incomesCreateCount > 0) {
                            messages << createNewOperationMessage(ndflPerson, "Сведения о доходах и НДФЛ", incomesCreateCount)
                        }
                        int deductionsCreateCount = 0
                        for (NdflPersonDeduction deduction : ndflPerson.deductions) {
                            deduction.rowNum = ++deductionRowNum
                            NdflPersonDeduction persistedDeduction = null
                            if (deduction.id != null) {
                                persistedDeduction = ndflPersonService.getDeduction(deduction.id)
                            }
                            if (deduction.id == null || persistedDeduction == null) {
                                deduction.ndflPersonId = ndflPerson.id
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
                                    persistedDeduction.modifiedBy = userInfo.getUser().getName()
                                    deductionsForUpdate << persistedDeduction
                                }
                            }
                        }
                        if (deductionsCreateCount > 0) {
                            messages << createNewOperationMessage(ndflPerson, "Сведения о вычетах", deductionsCreateCount)
                        }
                        int prepaymentsCreateCount = 0
                        for (NdflPersonPrepayment prepayment : ndflPerson.prepayments) {
                            prepayment.rowNum = ++prepaymentRowNum
                            NdflPersonPrepayment persistedPrepayment = null
                            if (prepayment.id != null) {
                                persistedPrepayment = ndflPersonService.getPrepayment(prepayment.id)
                            }
                            if (prepayment.id == null || persistedPrepayment == null) {
                                prepayment.ndflPersonId = ndflPerson.id
                                prepaymentsForCreate << prepayment
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
                                    persistedPrepayment.modifiedBy = userInfo.getUser().getName()
                                    prepaymentsForUpdate << persistedPrepayment
                                }
                            }
                        }
                        if (prepaymentsCreateCount > 0) {
                            messages << createNewOperationMessage(ndflPerson, "Сведения о доходах в виде авансовых платежей", prepaymentsCreateCount)
                        }
                    }
                    updateRowNumPersonList << ndflPerson
                } else {
                    // создаем новых физлиц
                    if (!needPersonUpdate) {
                        transformOperationId()
                        for (NdflPersonIncome income : ndflPerson.incomes) {
                            income.rowNum = ++incomeRowNum
                        }
                        for (NdflPersonDeduction deduction : ndflPerson.deductions) {
                            deduction.rowNum = ++deductionRowNum
                        }
                        for (NdflPersonPrepayment prepayment : ndflPerson.prepayments) {
                            prepayment.rowNum = ++prepaymentRowNum
                        }
                        ndflPersonsForCreate << ndflPerson
                    } else {
                        updateRowNumPersonList << ndflPerson
                    }
                }
            }
            ndflPersonService.saveIncomes(incomesForCreate)
            ndflPersonService.saveDeductions(deductionsForCreate)
            ndflPersonService.savePrepayments(prepaymentsForCreate)
            ndflPersonService.updateIncomes(incomesForUpdate)
            ndflPersonService.updateDeductions(deductionsForUpdate)
            ndflPersonService.updatePrepayments(prepaymentsForUpdate)
            ndflPersonService.save(ndflPersonsForCreate)
            logger.getEntries().addAll(messages)
            checkPersons(ndflPersons)
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
            logger.error("Ошибка при загрузке файла \"$fileName\". Не удалось распознать заголовок таблицы.")
            return
        }
        for (int i = 0; i < header.size(); i++) {
            if (i >= headersActual[0].size() || header[i] != headersActual[0][i]) {
                logger.error("Ошибка при загрузке файла \"$fileName\". Заголовок таблицы не соответствует требуемой структуре.")
                logger.error("Столбец заголовка таблицы \"${i >= headersActual[0].size() ? "Не задан или отсутствует" : headersActual[0][i]}\" № ${i + 1} " +
                        "не соответствует ожидаемому \"${header[i]}\" № ${i + 1}")
                break
            }
        }
    }

    NdflPersonExt createNdflPerson(Row row) {
        String[] importIds = splitTechnicalCell(row.cell(1).toString())
        Long incomeImportId = null
        Long deductionImportId = null
        Long prepaymentImportId = null
        if (importIds != null) {
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
        NdflPersonExt ndflPerson = new NdflPersonExt(row.index)
        ndflPerson.declarationDataId = declarationData.id
        ndflPerson.inp = row.cell(3).toString(25)
        ndflPerson.lastName = row.cell(4).toString(36)
        ndflPerson.firstName = row.cell(5).toString(36)
        ndflPerson.middleName = row.cell(6).toString(36)
        ndflPerson.fio = (ndflPerson.lastName ?: "") + " " + (ndflPerson.firstName ?: "") + " " + (ndflPerson.middleName ?: "")
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
        ndflPerson.flat = row.cell(22).toString(8)
        ndflPerson.snils = row.cell(23).toString(14)

        if (!row.isEmpty(24..45)) {
            ndflPerson.incomes.add(createIncome(row, incomeImportId))
        }

        if (!row.isEmpty(46..59)) {
            ndflPerson.deductions.add(createDeduction(row, deductionImportId))
        }

        if (!row.isEmpty(60..64)) {
            ndflPerson.prepayments.add(createPrepayment(row, prepaymentImportId))
        }

        return ndflPerson
    }

    NdflPersonIncome createIncome(Row row, Long importId) {
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
        personIncome.taxSumm = row.cell(45).toLong(20)
        personIncome.id = importId
        if (operationsGrouped.containsKey(personIncome.operationId)) {
            operationsGrouped.get(personIncome.operationId).add(personIncome)
        } else {
            operationsGrouped.put(personIncome.operationId, [personIncome])
        }
        return personIncome
    }

    NdflPersonDeduction createDeduction(Row row, Long importId) {
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
        if (operationsGrouped.containsKey(personDeduction.operationId)) {
            operationsGrouped.get(personDeduction.operationId).add(personDeduction)
        } else {
            operationsGrouped.put(personDeduction.operationId, [personDeduction])
        }
        return personDeduction
    }

    NdflPersonPrepayment createPrepayment(Row row, Long importId) {
        NdflPersonPrepayment personPrepayment = new NdflPersonPrepaymentExt(row.index)
        personPrepayment.operationId = row.cell(60).toString(100)
        personPrepayment.summ = row.cell(61).toBigDecimal(20)
        personPrepayment.notifNum = row.cell(62).toString(20)
        personPrepayment.notifDate = row.cell(63).toDate()
        personPrepayment.notifSource = row.cell(64).toString(4)
        personPrepayment.id = importId
        if (operationsGrouped.containsKey(personPrepayment.operationId)) {
            operationsGrouped.get(personPrepayment.operationId).add(personPrepayment)
        } else {
            operationsGrouped.put(personPrepayment.operationId, [personPrepayment])
        }
        return personPrepayment
    }

    boolean merge(List<NdflPerson> persons, NdflPerson person) {
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

    NdflPerson getOrPutToCache(NdflPerson personToFind) {
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

    void checkPersons(List<NdflPerson> persons) {
        if (persons) {
            checkPersonIncomeDates(persons)
            checkReferences(persons)
        }
    }

    void checkPersonIncomeDates(List<NdflPerson> persons) {
        boolean allChecksFailed = true
        for (def person : persons) {
            allChecksFailed &= !checkIncomesDates(person)
        }

        if (allChecksFailed) {
            logger.error("В файле \"$fileName\" отсутствуют операции, принадлежащие периоду формы: " +
                    "\"${reportPeriod.taxPeriod.year}, ${reportPeriod.name}\".")
        }
    }

    boolean checkIncomesDates(NdflPerson person) {
        if (person.incomes) {
            boolean allRecordsFailed = true
            boolean atLeastOneRecordFailed = false
            Map<String, List<NdflPersonIncome>> incomesGroupedByOperationId = [:]
            for (NdflPersonIncome income : person.incomes) {
                List<NdflPersonIncome> incomes = incomesGroupedByOperationId.get(income.operationId)
                if (incomes == null) {
                    incomesGroupedByOperationId.put(income.operationId, [income])
                } else {
                    incomes.add(income)
                }
            }
            for (List<NdflPersonIncome> incomesGroup : incomesGroupedByOperationId.values()) {
                boolean checkPassed = isIncomeDatesInPeriod(incomesGroup)
                allRecordsFailed &= !checkPassed
                atLeastOneRecordFailed |= !checkPassed
            }
            if (allRecordsFailed) {
                logger.error("Для ФЛ (${person.lastName} ${person.firstName}${person.middleName ? " " + person.middleName : ""}, " +
                        "ИНП ${person.inp} отсутствуют операции, принадлежащие периоду формы: Операции по ФЛ не загружено в Налоговую форму " +
                        "№: \"${declarationData.id}\", Период: \"${reportPeriod.taxPeriod.year}, ${reportPeriod.name}\"," +
                        " Подразделение: \"${department.name}\", Вид: \"${declarationTemplate.name}\"" +
                        "${asnuName ? ", АСНУ: \"${asnuName}\"" : ""}.")
            }
            return !atLeastOneRecordFailed
        }
        return true
    }

    boolean isIncomeDatesInPeriod(List<NdflPersonIncome> incomeGroup) {
        def incomeAccruedDate = null
        def incomePayoutDate = null
        int incomeAccruedRowIndex = 0
        int incomePayoutRowIndex = 0
        for (NdflPersonIncome income : incomeGroup) {
            if (income.incomeAccruedDate != null) {
                incomeAccruedDate = income.incomeAccruedDate
                incomeAccruedRowIndex = (income as NdflPersonIncomeExt).rowIndex
            }
            if (income.incomePayoutDate != null) {
                incomePayoutDate = income.incomePayoutDate
                incomePayoutRowIndex = (income as NdflPersonIncomeExt).rowIndex
            }
        }
        boolean incomeAccruedDateCorrect = checkIncomeDate(incomeAccruedDate)
        boolean incomePayoutDateCorrect = checkIncomeDate(incomePayoutDate)
        if (incomeAccruedDateCorrect || incomePayoutDateCorrect) {
            return true
        } else {
            if (!incomeAccruedDateCorrect) {
                logIncomeDatesError(incomeAccruedDate, incomeGroup.get(0), incomeAccruedRowIndex, 26)
            }
            if (!incomePayoutDateCorrect) {
                logIncomeDatesError(incomePayoutDate, incomeGroup.get(0), incomePayoutRowIndex, 27)
            }
            return false
        }
    }

    void logIncomeDatesError(Date date, NdflPersonIncome income, int rowIndex, int colIndex) {
        logger.error("Дата: \"${date?.format(SharedConstants.DATE_FORMAT)}\", указанная в столбце \"${header[colIndex]}\" № ${colIndex}" +
                " для строки ${rowIndex} не соответствует периоду формы: \"${reportPeriod.taxPeriod.year}, ${reportPeriod.name}\"." +
                " Операция \"${income.operationId}\" не загружена в Налоговую форму №: \"${declarationData.id}\", Период: " +
                "\"${reportPeriod.taxPeriod.year}, ${reportPeriod.name}\", Подразделение: \"${department.name}\", Вид: \"${declarationTemplate.name}\"" +
                "${asnuName ? ", АСНУ: \"${asnuName}\"" : ""}.")

    }

    boolean checkIncomeDate(Date date) {
        if (!date) {
            return true
        }
        if (!(reportPeriod.startDate <= date && date <= reportPeriod.endDate)) {
            return false
        }
        return true
    }

    void checkReferences(List<NdflPerson> persons) {
        for (def person : persons) {
            if (person.incomes) {
                for (def income : person.incomes) {
                    checkIncomeCode(income, person)
                }
            }
            if (person.deductions) {
                String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [(person as NdflPersonExt).fio, person.inp])
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

    void checkIncomeCode(NdflPersonIncome income, NdflPerson person) {
        if (income.incomeCode && !incomeCodes.find { key, value ->
            value.CODE?.stringValue == income.incomeCode &&
                    (income.incomeAccruedDate == null ||
                            income.incomeAccruedDate >= value.record_version_from?.dateValue &&
                            income.incomeAccruedDate <= value.record_version_to?.dateValue)
        }) {
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [(person as NdflPersonExt).fio, person.inp])
            String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                    C_INCOME_CODE, income.incomeCode ?: "",
                    R_INCOME_CODE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, income.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_CODE), fioAndInp, pathError, errMsg)
        }
    }

    void updatePersonsRowNum(List<NdflPerson> persons) {
        long rowNum = 0
        for (def person : persons) {
            person.rowNum = ++rowNum
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
    void removeOperations() {
        List<NdflPerson> ndflPersonFromDeclarationData = ndflPersonService.findNdflPerson(declarationData.id)
        int initialCounter = 0
        int removedCounter = 0
        List<LogEntry> removeMessages = []
        List<Long> incomesForRemove = []
        List<Long> deductionsForRemove = []
        List<Long> prepaymentsForRemove = []
        List<Long> ndflPersonIdForRemove = []
        for (NdflPerson np : ndflPersonFromDeclarationData) {
            List<Long> incomesIdList = ndflPersonService.fetchIncomeIdByNdflPerson(np.id)
            List<Long> deductionsIdList = ndflPersonService.fetchDeductionIdByNdflPerson(np.id)
            List<Long> prepaymentsIdList = ndflPersonService.fetchPrepaymentIdByNdflPerson(np.id)
            int incomesInitialSize = incomesIdList.size()
            int deductionsInitialSize = deductionsIdList.size()
            int prepaymentInitialSize = prepaymentsIdList.size()
            initialCounter += (incomesInitialSize + deductionsInitialSize + prepaymentInitialSize)
            incomesIdList.removeAll(incomeImportIdList)
            deductionsIdList.removeAll(deductionImportIdList)
            prepaymentsIdList.removeAll(prepaymentImportIdList)
            int incomesRemovedCount = incomesIdList.size()
            int deductionsRemovedCount = deductionsIdList.size()
            int prepaymentsRemovedCount = prepaymentsIdList.size()
            incomesForRemove.addAll(incomesIdList)
            deductionsForRemove.addAll(deductionsIdList)
            prepaymentsForRemove.addAll(prepaymentsIdList)
            removedCounter += incomesRemovedCount
            removedCounter += deductionsRemovedCount
            removedCounter += prepaymentsRemovedCount
            if (incomesRemovedCount != 0) {
                removeMessages << createRemoveMessage(np, INCOME_TITLE, incomesRemovedCount)
            }
            if (deductionsRemovedCount != 0) {
                removeMessages << createRemoveMessage(np, DEDUCTION_TITLE, deductionsRemovedCount)
            }
            if (prepaymentsRemovedCount != 0) {
                removeMessages << createRemoveMessage(np, PREPAYMENTS_TITLE, prepaymentsRemovedCount)
            }
            if (incomesInitialSize == incomesRemovedCount) {
                ndflPersonIdForRemove << np.id
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
        if (!ndflPersonIdForRemove.isEmpty()) {
            ndflPersonService.deleteNdflPersonBatch(ndflPersonIdForRemove)
        }

        logger.info("При загрузке файла было удалено: %d, из: %d", removedCounter, initialCounter)
        logger.getEntries().addAll(removeMessages)
    }

    /**
     * Создает сообщение о удалении даннух у ФЛ
     * @param ndflPerson объект физлица
     * @param sectionName название раздела
     * @param removedCount количество удаленных записей
     * @return запись для логгера с сообщением
     */
    LogEntry createRemoveMessage(NdflPerson ndflPerson, String sectionName, int removedCount) {
        return new LogEntry(LogLevel.INFO, String.format("Удалены данные у ФЛ: (%s %s %s, ИНП: %s, ДУЛ: %s, %s). В Разделе %s удалено строк (%d)",
                ndflPerson.lastName ?: "",
                ndflPerson.firstName ?: "",
                ndflPerson.middleName ?: "",
                ndflPerson.inp ?: "",
                ndflPerson.idDocType ?: "",
                ndflPerson.idDocNumber ?: "",
                sectionName,
                removedCount
        ))
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

    LogEntry createUpdateOperationMessage(NdflPerson ndflPerson, String sectionName, Long id, String attrName, String oldValue, String newValue) {
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
                oldValue ?: "",
                newValue ?: ""
        ))
    }


    class NdflPersonExt extends NdflPerson {
        int rowIndex
        def fio

        NdflPersonExt(int rowIndex) {
            super()
            this.rowIndex = rowIndex
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
        ArrayList<String> values

        Cell cell(int idx) {
            return new Cell(index: idx, value: values[idx - 1], row: this)
        }

        boolean isEmpty(def range = 1..values.size()) {
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
            return null
        }

        String toString(Integer maxLength = null) {
            if (value && maxLength && value.length() > maxLength) {
                logIncorrectTypeError("Строка/${maxLength}/")
            }
            return value
        }

        void logIncorrectTypeError(def type) {
            logger.error("Ошибка при определении значения ячейки файла \"$fileName\". Тип данных ячейки столбца \"${header[index - 1]}\" № $index" +
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
