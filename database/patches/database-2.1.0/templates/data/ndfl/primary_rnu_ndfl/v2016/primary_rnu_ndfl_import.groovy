package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.service.*
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.checkAndReadFile
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

    Logger logger
    NdflPersonService ndflPersonService
    DeclarationData declarationData
    DepartmentService departmentService
    CalendarService calendarService
    FiasRefBookService fiasRefBookService
    ReportPeriodService reportPeriodService
    DepartmentReportPeriodService departmentReportPeriodService
    RefBookFactory refBookFactory

    String fileName
    InputStream inputStream

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
    Map<Integer, List<NdflPerson>> ndflPersonCache = [:]

    final String DATE_FORMAT = "dd.MM.yyyy"

    Import(scriptClass) {
        //noinspection GroovyAssignabilityCheck
        super(scriptClass)
        this.logger = (Logger) scriptClass.getProperty("logger")
        this.fileName = (String) scriptClass.getProperty("fileName")
        this.inputStream = (InputStream) scriptClass.getProperty("inputStream")
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

        checkAndReadFile(inputStream, fileName, allValues, headerValues, null, null, 2, paramsMap)
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

        checkPersons(ndflPersons)
        updatePersonsRowNum(ndflPersons)

        if (!logger.containsLevel(LogLevel.ERROR)) {
            checkInterrupted()
            ndflPersonService.deleteAll(declarationData.id)

            ndflPersonService.save(ndflPersons)
        } else {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
        }
    }

    List<String> header = ["№ п/п", "ИНП", "Фамилия", "Имя", "Отчество", "Дата рождения", "Гражданство (код страны)",
                           "ИНН в РФ", "ИНН в ИНО", "ДУЛ Код", "ДУЛ Номер", "Статус (код)", "Код субъекта", "Индекс", "Район", "Город",
                           "Населенный пункт", "Улица", "Дом", "Корпус", "Квартира", "СНИЛС", "ID операции", "Код дохода", "Признак дохода",
                           "Дата начисления дохода", "Дата выплаты дохода", "КПП", "ОКТМО", "Сумма начисленного дохода", "Сумма выплаченного дохода",
                           "Сумма вычета", "Налоговая база", "Процентная ставка (%)", "Дата НДФЛ", "НДФЛ исчисленный", "НДФЛ удержанный",
                           "НДФЛ не удержанный", "НДФЛ излишне удержанный", "НДФЛ возвращенный НП", "Срок перечисления в бюджет", "Дата платежного поручения",
                           "Номер платежного поручения", "Сумма платежного поручения", "Код вычета", "Подтверждающий документ. Тип",
                           "Подтверждающий документ. Дата", "Подтверждающий документ. Номер", "Подтверждающий документ. Код источника",
                           "Подтверждающий документ. Сумма", "Доход. ID операции", "Доход. Дата", "Доход. Код дохода", "Доход. Сумма",
                           "Вычет. Предыдущий период. Дата", "Вычет. Предыдущий период. Сумма", "Вычет. Текущий период. Дата",
                           "Вычет. Текущий период. Сумма", "Сумма фиксированного авансового платежа", "Номер уведомления", "Дата выдачи уведомления",
                           "Код налогового органа, выдавшего уведомление"]

    void checkHeaders(List<List<String>> headersActual) {
        if (headersActual == null || headersActual.isEmpty() || headersActual[0] == null || headersActual[0].isEmpty()) {
            logger.error("Ошибка при загрузке файла \"$fileName\". Не удалось распознать заголовок таблицы.")
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
        NdflPersonExt ndflPerson = new NdflPersonExt(row.index)
        ndflPerson.declarationDataId = declarationData.id
        ndflPerson.inp = row.cell(2).toString(25)
        ndflPerson.lastName = row.cell(3).toString(36)
        ndflPerson.firstName = row.cell(4).toString(36)
        ndflPerson.middleName = row.cell(5).toString(36)
        ndflPerson.fio = (ndflPerson.lastName ?: "") + " " + (ndflPerson.firstName ?: "") + " " + (ndflPerson.middleName ?: "")
        ndflPerson.birthDay = row.cell(6).toDate()
        ndflPerson.citizenship = row.cell(7).toString(3)
        ndflPerson.innNp = row.cell(8).toString(12)
        ndflPerson.innForeign = row.cell(9).toString(50)
        ndflPerson.idDocType = row.cell(10).toString(2)
        ndflPerson.idDocNumber = row.cell(11).toString(25)
        ndflPerson.status = row.cell(12).toInteger(1)?.toString()
        ndflPerson.regionCode = row.cell(13).toString(2)
        ndflPerson.postIndex = row.cell(14).toString(6)
        ndflPerson.area = row.cell(15).toString(50)
        ndflPerson.city = row.cell(16).toString(50)
        ndflPerson.locality = row.cell(17).toString(50)
        ndflPerson.street = row.cell(18).toString(50)
        ndflPerson.house = row.cell(19).toString(20)
        ndflPerson.building = row.cell(20).toString(20)
        ndflPerson.flat = row.cell(21).toString(8)
        ndflPerson.snils = row.cell(22).toString(14)

        ndflPerson.incomes.add(createIncome(row))

        if (!row.isEmpty(45..58)) {
            ndflPerson.deductions.add(createDeduction(row))
        }

        if (!row.isEmpty(59..62)) {
            ndflPerson.prepayments.add(createPrepayment(row))
        }
        return ndflPerson
    }

    NdflPersonIncome createIncome(Row row) {
        NdflPersonIncomeExt personIncome = new NdflPersonIncomeExt(row.index)
        personIncome.operationId = row.cell(23).toString(100)
        personIncome.incomeCode = row.cell(24).toString(4)
        personIncome.incomeType = row.cell(25).toString(2)
        personIncome.incomeAccruedDate = row.cell(26).toDate()
        personIncome.incomePayoutDate = row.cell(27).toDate()
        personIncome.kpp = row.cell(28).toString(9)
        personIncome.oktmo = row.cell(29).toString(11)
        personIncome.incomeAccruedSumm = row.cell(30).toBigDecimal(20, 2)
        personIncome.incomePayoutSumm = row.cell(31).toBigDecimal(20, 2)
        personIncome.totalDeductionsSumm = row.cell(32).toBigDecimal(20, 2)
        personIncome.taxBase = row.cell(33).toBigDecimal(20, 2)
        personIncome.taxRate = row.cell(34).toInteger(2)
        personIncome.taxDate = row.cell(35).toDate()
        personIncome.calculatedTax = row.cell(36).toBigDecimal(20)
        personIncome.withholdingTax = row.cell(37).toBigDecimal(20)
        personIncome.notHoldingTax = row.cell(38).toBigDecimal(20)
        personIncome.overholdingTax = row.cell(39).toBigDecimal(20)
        personIncome.refoundTax = row.cell(40).toLong(15)
        personIncome.taxTransferDate = row.cell(41).toDate()
        personIncome.paymentDate = row.cell(42).toDate()
        personIncome.paymentNumber = row.cell(43).toString(20)
        personIncome.taxSumm = row.cell(44).toLong(20)
        return personIncome
    }

    NdflPersonDeduction createDeduction(Row row) {
        NdflPersonDeduction personDeduction = new NdflPersonDeductionExt(row.index)
        personDeduction.typeCode = row.cell(45).toString(3)
        personDeduction.notifType = row.cell(46).toString(1)
        personDeduction.notifDate = row.cell(47).toDate()
        personDeduction.notifNum = row.cell(48).toString(20)
        personDeduction.notifSource = row.cell(49).toString(4)
        personDeduction.notifSumm = row.cell(50).toBigDecimal(20, 2)
        personDeduction.operationId = row.cell(51).toString(100)
        personDeduction.incomeAccrued = row.cell(52).toDate()
        personDeduction.incomeCode = row.cell(53).toString(4)
        personDeduction.incomeSumm = row.cell(54).toBigDecimal(20, 2)
        personDeduction.periodPrevDate = row.cell(55).toDate()
        personDeduction.periodPrevSumm = row.cell(56).toBigDecimal(20, 2)
        personDeduction.periodCurrDate = row.cell(57).toDate()
        personDeduction.periodCurrSumm = row.cell(58).toBigDecimal(20, 2)
        return personDeduction
    }

    NdflPersonPrepayment createPrepayment(Row row) {
        NdflPersonPrepayment personPrepayment = new NdflPersonPrepaymentExt(row.index)
        personPrepayment.summ = row.cell(59).toBigDecimal(20)
        personPrepayment.notifNum = row.cell(60).toString(20)
        personPrepayment.notifDate = row.cell(61).toDate()
        personPrepayment.notifSource = row.cell(62).toString(4)
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
            for (int i = 0; i < person.incomes.size(); i++) {
                def income = person.incomes[i]
                boolean checkPassed = isIncomeDatesInPeriod(income)
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

    boolean isIncomeDatesInPeriod(NdflPersonIncome income) {
        def incomeAccruedDate = income.incomeAccruedDate?.toDate()
        def incomePayoutDate = income.incomePayoutDate?.toDate()
        boolean incomeAccruedDateCorrect = checkIncomeDate(incomeAccruedDate)
        boolean incomePayoutDateCorrect = checkIncomeDate(incomePayoutDate)
        if (incomeAccruedDateCorrect || incomePayoutDateCorrect) {
            return true
        } else {
            if (!incomeAccruedDateCorrect) {
                logIncomeDatesError(incomeAccruedDate, income, (income as NdflPersonIncomeExt).rowIndex, 26)
            }
            if (!incomePayoutDateCorrect) {
                logIncomeDatesError(incomePayoutDate, income, (income as NdflPersonIncomeExt).rowIndex, 27)
            }
            return false
        }
    }

    void logIncomeDatesError(Date date, NdflPersonIncome income, int rowIndex, int colIndex) {
        logger.error("Дата: \"${date?.format(DATE_FORMAT)}\", указанная в столбце \"${header[colIndex - 1]}\" № ${colIndex}" +
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
                            income.incomeAccruedDate.toDate() >= value.record_version_from?.dateValue &&
                            income.incomeAccruedDate.toDate() <= value.record_version_to?.dateValue)
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

        LocalDateTime toDate() {
            if (value != null && !value.isEmpty()) {
                try {
                    return LocalDateTime.parse(value, DateTimeFormat.forPattern(DATE_FORMAT))
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
