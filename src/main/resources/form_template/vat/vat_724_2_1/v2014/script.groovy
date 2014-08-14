package form_template.vat.vat_724_2_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * (724.2.1) Операции, не подлежащие налогообложению (освобождаемые от налогообложения), операции, не признаваемые объектом
 * налогообложения, операции по реализации товаров (работ, услуг), местом реализации которых не признается территория
 * Российской Федерации, а также суммы оплаты, частичной оплаты в счет предстоящих поставок (выполнения работ,
 * оказания услуг), длительность производственного цикла изготовления которых составляет свыше шести месяцев
 *
 * formTemplateId=601
 *
 * @author Stanislav Yasinskiy
 */

// графа 1 - rowNum         № пп
// графа 2 - code           Код операции
// графа 3 - name           Наименование операции
// графа 4 - realizeCost    Стоимость реализованных (переданных) товаров (работ, услуг) без НДС
// графа 5 - obtainCost     Стоимость приобретенных товаров  (работа, услуг), не облагаемых НДС

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        prevCalcCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        prevCalcCheck()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        prevCalcCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        prevCalcCheck()
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
}

// Автозаполняемые атрибуты (графа 4, 5)
@Field
def autoFillColumns = ['realizeCost', 'obtainCost']

// Проверяемые на пустые значения атрибуты (группа 1..5)
@Field
def nonEmptyColumns = ['code', 'name', 'realizeCost', 'obtainCost']

// Поля, для которых подсчитываются итоговые значения (графа 4, 5)
@Field
def totalColumns = ['realizeCost', 'obtainCost']

// Дата начала отчетного периода
@Field
def startDate = null

@Field
def calendarStartDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def prevEndDate = null

@Field
def dateFormat = "dd.MM.yyyy"

@Field
def calcRowAlias4 = ['R1', 'R3', 'R4', 'R5', 'R8', 'R9', 'R10', 'R11', 'R13', 'R14', 'R18']

@Field
def calcRowAlias5 = ['R8', 'R9', 'R13']

@Field
def repordPeriod = null

@Field
def prevReportPeriod = null

// Cправочник «Отчет о прибылях и убытках (Форма 0409102-СБ)»
@Field
def income102DataCache = [:]

//// Кастомные методы

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def row in dataRows) {
        if (row.getAlias() == 'itog') {
            continue
        }

        // графа 4
        row.realizeCost = (row.getAlias() in calcRowAlias4 ? calc4(row) : row.realizeCost)

        // графа 5
        row.obtainCost = (row.getAlias() in calcRowAlias5 ? calc5(row) : null)
    }

    // подсчет итогов
    def itogValues = calcItog(dataRows)
    def itog = getDataRow(dataRows, 'itog')
    totalColumns.each { alias ->
        itog.getCell(alias).setValue(itogValues[alias], itog.getIndex())
    }
    dataRowHelper.save(dataRows);
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row in dataRows) {
        if (row.getAlias() == 'itog') {
            continue
        }

        // 1. Проверка заполнения граф (по графе 5 обязательны тока строки 8, 9 и 13)
        def columns = (row.getAlias() in calcRowAlias5 ? nonEmptyColumns : nonEmptyColumns - 'obtainCost')
        checkNonEmptyColumns(row, row.getIndex(), columns, logger, true)
    }

    // 2. Проверка итоговых значений
    def itogValues = calcItog(dataRows)
    def itog = getDataRow(dataRows, 'itog')
    totalColumns.each { alias ->
        if (itog.getCell(alias).value != itogValues[alias]) {
            logger.error(WRONG_TOTAL, getColumnName(itog, alias))
        }
    }
}

def getReportPeriodStartDate() {
    if (!startDate) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getCalendarStartDate() {
    if (!calendarStartDate) {
        calendarStartDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return calendarStartDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getPrevReportPeriodEndDate() {
    if (prevEndDate == null) {
        def prevReportPeriodId = getPrevReportPeriod()?.id
        if (prevReportPeriodId != null) {
            prevEndDate = reportPeriodService.getEndDate(prevReportPeriodId).time
        }
    }
    return prevEndDate
}

def getRepordPeriod() {
    if (repordPeriod == null) {
        repordPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return repordPeriod
}

def getPrevReportPeriod() {
    if (prevReportPeriod == null) {
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    }
    return prevReportPeriod
}

// Получение данных из справочника «Отчет о прибылях и убытках» для текужего подразделения и отчетного периода
def getIncome102Data(def date) {
    if(date==null){
        return []
    }
    if (!income102DataCache.containsKey(date)) {
        def records = bookerStatementService.getRecords(52L, formData.departmentId, date, null)
        if (records == null) {
            return []
        }
        income102DataCache.put(date, records)
    }
    return income102DataCache.get(date)
}

// Проверка наличия необходимых записей в справочнике «Отчет о прибылях и убытках»
void checkIncome102() {
    // формирование названия периода
    def periodName = null
    def period = null
    def prevPeriod = null

    // Наличие экземпляра Отчета о прибылях и убытках подразделения и периода, для которых сформирована текущая форма
    if (getIncome102Data(getReportPeriodEndDate()) == []) {
        period = getBookerStatementPeriod(getReportPeriodEndDate())
    }
    // Наличие экземпляра Отчета о прибылях и убытках подразделения и предыдущего периода
    if (getRepordPeriod().order > 1 && getIncome102Data(getPrevReportPeriodEndDate()) == []) {
        prevPeriod = getBookerStatementPeriod(getPrevReportPeriodEndDate())
    }

    if (prevPeriod && period) {
        periodName = prevPeriod + " и " + period
    } else if (prevPeriod || period) {
        periodName = (prevPeriod ?: period)
    }

    if (periodName) {
        throw new ServiceException("Экземпляр Отчета о прибылях и убытках за $periodName " +
                "не существует (отсутствуют данные для расчета)! Расчеты не могут быть выполнены.")
    }
}

void prevCalcCheck() {
    // 1. Проверка наличия экземпляра «Отчета о прибылях и убытках» по соответствующему подразделению за соответствующий налоговый период
    checkIncome102()

    // 2. Проверка наличия в справочнике «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ» данных для графы 4 и 5
    // эта проверка происходит при расчетах в методе getOpuCodes

    // 3. Проверка наличия символов ОПУ в Экземпляре Отчета о прибылях и убытках, необходимых для заполнения «Графы 4» и «Графы 5»
    // эта проверка происходит при расчетах в методе getSumByOpuCodes
}

// Консолидация
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    dataRows.each{
        it.realizeCost = null
        it.obtainCost = null
    }
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getCalendarStartDate(), getReportPeriodEndDate()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.formType.taxType == TaxType.VAT) {
            formDataService.getDataRowHelper(source).allCached.each { srcRow ->
                if (srcRow.getAlias() != null && !srcRow.getAlias().equals('itog')) {
                    def row = dataRowHelper.getDataRow(dataRows, srcRow.getAlias())
                    row.realizeCost = (row.realizeCost ?: 0) + (srcRow.realizeCost ?: 0)
                    row.obtainCost = (row.obtainCost ?: 0) + (srcRow.obtainCost ?: 0)
                }
            }
        }
    }

    dataRowHelper.update(dataRows)
}

def calc4(def row) {
    return calc4or5(row, 0)
}

def calc5(def row) {
    return calc4or5(row, 1)
}

/**
 * Посчитать значение для графы 4 или 5.
 *
 * @param row строка
 * @param columnFlag признак графы: 0 – Графа 4, 1 – Графа 5
 */
def calc4or5(def row, def columnFlag) {
    // список кодов ОПУ из справочника
    def opuCodes = getOpuCodes(row.code, row, columnFlag)
    // сумма кодов ОПУ из отчета 102
    def sum = getSumByOpuCodes(opuCodes, row, columnFlag)
    return roundValue(sum, 2)
}

/**
 * Получить список кодов ОПУ.
 *
 * @param code код операции
 * @param row строка
 * @param columnFlag признак графы: 0 – Графа 4, 1 – Графа 5
 */
def getOpuCodes(def code, def row, def columnFlag) {
    def columnAlias = (columnFlag == 0 ? 'realizeCost' : 'obtainCost')
    // потом поправить фильтр и id справочника
    def filter = "BOX_724_2_1 = $columnFlag AND CODE = '$code'"
    def records = refBookFactory.getDataProvider(102L)?.getRecords(getReportPeriodEndDate(), null, filter, null)
    if (records == null || records.isEmpty()) {
        // условия выполнения расчетов
        // 2. Проверка наличия в справочнике «Классификатор соответствия кодов операций налоговой формы 724.2.1 по НДС символам ОПУ» данных для графы 4 и 5
        throw new ServiceException("Строка ${row.getIndex()}: В справочнике «%s» нет данных для заполнения графы «${getColumnName(row, columnAlias)}»! Расчеты не могут быть выполнены.",
                refBookFactory.get(102L).name)
    }
    def opuCodes = []
    records.each { record ->
        opuCodes.add(record?.OPU?.value)
    }

    return opuCodes
}

/**
 * Посчитать сумму по кодам ОПУ.
 *
 * @param opuCodes список кодов ОПУ
 * @param row строка
 * @param columnFlag признак графы: 0 – Графа 4, 1 – Графа 5
 */
def getSumByOpuCodes(def opuCodes, def row, def columnFlag) {
    def columnAlias = (columnFlag == 0 ? 'realizeCost' : 'obtainCost')
    def tmp = BigDecimal.ZERO
    def hasData = false
    // В отчете 102 данные хранятся по периодам как у прибыли (1 квартал, полгода, 9 месяцев, год)
    // Для формы 724.2.1 нужны квартальные данные, поэтому для получения кваратальных значении из отчета 102
    // сначало берутся данные за текущий периода, а потом вычитаются данные запредыдущий период (например: 9 месяцев - полгода)
    for (def income102Row : getIncome102Data(getReportPeriodEndDate())) {
        if (income102Row?.OPU_CODE?.value in opuCodes) {
            tmp += (income102Row?.TOTAL_SUM?.value ?: 0)
            hasData = true
        }
    }
    if (!hasData) {
        // условия выполнения расчетов
        // 3. Проверка наличия символов ОПУ в Экземпляре Отчета о прибылях и убытках, необходимых для заполнения «Графы 4» и «Графы 5»
        def start = getReportPeriodStartDate().format(dateFormat)
        def end = getReportPeriodEndDate().format(dateFormat)
        logger.warn("Строка ${row.getIndex()}: Экземпляр Отчета о прибылях и убытках за период $start - $end не содержит записей " +
                "для заполнения графы «${getColumnName(row, columnAlias)}» по следующим символам ОПУ: «${opuCodes.join(', ')}»!")
        return BigDecimal.ZERO
    }
    if (getRepordPeriod().order > 1) {
        for (def income102Row : getIncome102Data(getPrevReportPeriodEndDate())) {
            if (income102Row?.OPU_CODE?.value in opuCodes) {
                tmp -= (income102Row?.TOTAL_SUM?.value ?: 0)
            }
        }
    }
    return tmp
}

def calcItog(def dataRows) {
    def itogValues = [:]
    totalColumns.each {alias ->
        itogValues[alias] = roundValue(0)
    }
    for (def row in dataRows) {
        if (row.getAlias() == 'itog') {
            continue
        }
        totalColumns.each { alias ->
            itogValues[alias] += roundValue(row.getCell(alias).value ?: 0)
        }
    }
    return itogValues
}

def roundValue(def value, int precision = 2) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNum'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tmpRow, 'rowNum'),
            (xml.row[0].cell[1]): getColumnName(tmpRow, 'code'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'name'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'realizeCost'),
            (xml.row[0].cell[4]): getColumnName(tmpRow, 'obtainCost')
    ]

    (1..5).each { index ->
        headerMapping.put((xml.row[1].cell[index - 1]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()
    def indexRow = 0

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[2].text() == 'Итого') {
            break
        }

        def dataRow = dataRows.get(indexRow)
        dataRow.setImportIndex(xlsIndexRow)
        indexRow++

        def xmlIndexCol = -1

        def values = [:]
        xmlIndexCol++
        values.rowNum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        values.code = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        values.name = row.cell[xmlIndexCol].text()

        // Проверить фиксированные значения (графа 1..3)
        ['rowNum', 'code', 'name'].each { alias ->
            def value = values[alias]?.toString()
            def valueExpected = dataRow.getCell(alias).value?.toString()
            checkFixedValue(dataRow, value, valueExpected, indexRow, alias, logger, true)
        }

        // графа 4
        xmlIndexCol++
        dataRow.realizeCost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        // графа 5
        xmlIndexCol++
        dataRow.obtainCost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
    }
    dataRowHelper.save(dataRows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def int rnuIndexRow = 2
    def int colOffset = 1

    dataRows.each {
        it.realizeCost = null
        it.realizeCost = null
    }

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def indexRow = rnuIndexRow - 2
        def dataRow = dataRows.get(indexRow - 1)

        def values = [:]
        values.rowNum = parseNumber(row.cell[1].text(), rnuIndexRow, 1 + colOffset, logger, true)
        values.code = row.cell[2].text()
        values.name = row.cell[3].text()

        // Проверить фиксированные значения (графа 1..3)
        ['rowNum', 'code', 'name'].each { alias ->
            def value = values[alias]?.toString()
            def valueExpected = dataRow.getCell(alias).value?.toString()
            checkFixedValue(dataRow, value, valueExpected, indexRow, alias, logger, true)
        }

        // графа 4
        dataRow.realizeCost = parseNumber(row.cell[4].text(), rnuIndexRow, 4 + colOffset, logger, true)

        // графа 5
        dataRow.obtainCost = parseNumber(row.cell[5].text(), rnuIndexRow, 5 + colOffset, logger, true)
    }
    def itogValues = calcItog(dataRows)
    def totalRow = getDataRow(dataRows, 'itog')
    totalColumns.each { alias ->
        totalRow[alias] = itogValues[alias]
    }

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow += 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 4
        total.realizeCost = parseNumber(row.cell[4].text(), rnuIndexRow, 4 + colOffset, logger, true)
        // графа 5
        total.obtainCost = parseNumber(row.cell[5].text(), rnuIndexRow, 5 + colOffset, logger, true)

        def colIndexMap = ['realizeCost' : 4, 'obtainCost' : 5]
        for (def alias : totalColumns) {
            def v1 = total[alias]
            def v2 = totalRow[alias]
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.error(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
                break
            }
        }
    }
    dataRowHelper.save(dataRows)
}

/**
 * Получить название периода Бухалтерской отчетности по дате.
 *
 * @param date дата.
 */
def getBookerStatementPeriod(def date) {
    def name = bookerStatementService.getPeriodValue(date)?.NAME?.value
    return name + " " + getRepordPeriod()?.taxPeriod?.year
}