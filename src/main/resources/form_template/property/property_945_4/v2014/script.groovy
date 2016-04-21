package form_template.property.property_945_4.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Сводная "(945.4) Расчёт налога на имущество по кадастровой стоимости".
 * formTemplateId=612
 *
 * @author Ramil Timerbaev
 */

// графа 1  - rowNum
// графа    - fix
// графа 2  - subject               - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
// графа 3  - taxAuthority
// графа 4  - kpp
// графа 5  - oktmo                 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
// графа 6  - address
// графа 7  - sign
// графа 8  - cadastreNumBuilding
// графа 9  - cadastreNumRoom
// графа 10 - cadastrePriceJanuary
// графа 11 - cadastrePriceTaxFree
// графа 12 - tenure
// графа 13 - taxBenefitCode        - атрибут 2033 TAX_BENEFIT_ID справочника 203 «Параметры налоговых льгот налога на имущество»
//                                     есть атрибут 2021 - CODE - «Код налоговой льготы», справочник 202 «Коды налоговых льгот налога на имущество»
// графа 14 - benefitBasis
// графа 15 - taxBase
// графа 16 - taxRate
// графа 17 - sum
// графа 18 - periodSum
// графа 19 - reductionPaymentSum

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkPrevForm()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkPrevForm()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, null, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        checkPrevForm()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        checkRegionId()
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def refBookCache = [:]
@Field
def recordCache = [:]

@Field
def allColumns = ['rowNum', 'fix', 'subject', 'taxAuthority', 'kpp', 'oktmo', 'address',
        'sign', 'cadastreNumBuilding', 'cadastreNumRoom', 'cadastrePriceJanuary', 'cadastrePriceTaxFree', 'tenure',
        'taxBenefitCode', 'benefitBasis', 'taxBase', 'taxRate', 'sum', 'periodSum', 'reductionPaymentSum']

// Проверяемые на пустые значения атрибуты (графа 1..8, 9..11, 14, 15)
@Field
def nonEmptyColumns = [ 'subject', 'taxAuthority', 'kpp', 'oktmo', 'address',
        'sign', 'cadastreNumBuilding', 'cadastrePriceJanuary', 'cadastrePriceTaxFree', 'tenure', 'taxRate', 'sum']

// Сортируемые атрибуты (графа 2, 3, 4, 5, 7, 8, 9)
@Field
def sortColumns = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'sign', 'cadastreNumBuilding', 'cadastreNumRoom']

// Группируевые атрибуты (графа 3, 4)
@Field
def groupColumns = ['taxAuthority', 'kpp']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 9, 10)
@Field
def totalColumns = ['cadastrePriceJanuary', 'cadastrePriceTaxFree']

@Field
def startDate = null

@Field
def endDate = null

@Field
def reportPeriod = null

@Field
def yearStartDate = null

@Field
def prevForms = null

@Field
def prevReportPeriods = null

// для хранения информации о справочниках
@Field
def refBooks = [:]

// для записей справочника 203
@Field
def recordsMap = [:]

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getYearStartDate() {
    if (yearStartDate == null) {
        yearStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return yearStartDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}
def calcBasis(def recordId) {
    if (recordId == null) {
        return null
    }
    def record = getRefBookValue(203, recordId)
    if (record == null) {
        return null
    }
    def section = record.SECTION.value ?: ''
    def item = record.ITEM.value ?: ''
    def subItem = record.SUBITEM.value ?: ''
    return String.format("%s%s%s", section.padLeft(4, '0'), item.padLeft(4, '0'), subItem.padLeft(4, '0'))
}
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def totalRow = getDataRow(dataRows, 'total')

    // удалить фикрсированные строки
    deleteAllAliased(dataRows)

    def isYear = isPeriodYear()
    for (def row : dataRows) {

        // графа 14
        row.benefitBasis = calc14(row)
        // графа 15
        row.taxBase = calc15(row, isYear)

        // графа 16
        row.taxRate = calc16(row)

        // графа 17
        row.sum = calc17(row, isYear)

        // графа 18
        row.periodSum = calc18(row, isYear)

        // графа 19
        row.reductionPaymentSum = calc19(row, isYear)
    }

    // сортировка / групировка
    sort(dataRows)

    // итоги и промежуточные итоги
    addFixedRows(dataRows, totalRow)

    updateIndexes(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        def Integer sign = row.sign
        def columns = (sign == 2)? (nonEmptyColumns + 'cadastreNumRoom') : nonEmptyColumns
        checkNonEmptyColumns(row, index, columns, logger, true)

        // для графы 14
        if (row.benefitBasis != calc14(row)) {
            logger.error(errorMsg + "Графа «${getColumnName(row, 'benefitBasis')}» заполнена неверно!")
        }
        // для графы 16
        def is201 = isRefBook201ForCalc16(row)
        def records = getRecords(row, is201)
        if (records == null || records.isEmpty()) {
            if (is201 && row.subject != null) {
                // 1. Проверка существования налоговой ставки по заданному субъекту
                logger.error(errorMsg + "В справочнике «Ставки налога на имущество» не найдена налоговая ставка (дата актуальности записи = ${getReportPeriodEndDate().format('dd.MM.yyyy')}, код субъекта РФ представителя декларации = «${getRefBookValue(4, formDataDepartment.regionId)?.CODE?.value?:''}», код субъекта РФ = «${getRefBookValue(4, row.subject)?.CODE?.value}»)!")
            }
        }
    }
}

def consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def totalRow = getDataRow(dataRows, 'total')

    // удалить фиксированные строки
    deleteAllAliased(dataRows)

    // форма 945.2
    def sourceFormTypeId = 611

    // собрать из источников строки
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == sourceFormTypeId) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceRows = formDataService.getDataRowHelper(source).allSaved
                for (def sourceRow : sourceRows) {
                    if (sourceRow.getAlias() != null) {
                        continue
                    }
                    def newRow = formData.createDataRow()

                    // графа 2
                    newRow.subject = sourceRow.subject
                    // графа 3
                    newRow.taxAuthority = sourceRow.taxAuthority
                    // графа 4
                    newRow.kpp = sourceRow.kpp
                    // графа 5
                    newRow.oktmo = sourceRow.oktmo
                    // графа 6
                    newRow.address = sourceRow.address
                    // графа 7
                    newRow.sign = sourceRow.sign
                    // графа 8
                    newRow.cadastreNumBuilding = sourceRow.cadastreNumBuilding
                    // графа 9
                    newRow.cadastreNumRoom = sourceRow.cadastreNumRoom
                    // графа 10
                    newRow.cadastrePriceJanuary = sourceRow.cadastrePriceJanuary
                    // графа 11
                    newRow.cadastrePriceTaxFree = sourceRow.cadastrePriceTaxFree
                    // графа 12
                    newRow.tenure = getTenure(sourceRow)
                    // графа 13
                    newRow.taxBenefitCode = sourceRow.taxBenefitCode

                    dataRows.add(newRow)
                }
            }
        }
    }
    dataRows.add(totalRow)

    updateIndexes(dataRows)
}

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return reportPeriod
}

def roundValue(def value, int newScale) {
    if (value != null) {
        return ((BigDecimal) value).setScale(newScale, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

def calc14(def row) {
    def recordId = row.taxBenefitCode
    if (recordId == null) {
        return null
    }
    def record = getRefBookValue(203, recordId)
    def section = record.SECTION.value ?: ''
    def item = record.ITEM.value ?: ''
    def subItem = record.SUBITEM.value ?: ''
    return String.format("%s%s%s", section.padLeft(4, '0'), item.padLeft(4, '0'), subItem.padLeft(4, '0'))
}

def calc15(def row, def isYear) {
    if (row.cadastrePriceJanuary == null || row.cadastrePriceTaxFree == null) {
        return null
    }
    if (isYear) {
        return roundValue(row.cadastrePriceJanuary - row.cadastrePriceTaxFree, 2)
    }
    return null
}

def calc16(def row) {
    if (row.subject == null) {
        return null
    }
    // определить из какого справочника брать данные
    def is201 = isRefBook201ForCalc16(row)
    // получить данные из справочника
    def records = getRecords(row, is201)
    if (records == null || records.isEmpty()) {
        return null
    }
    def record = records?.get(0)
    return roundValue(record?.RATE?.value, 2)
}

def calc17(def row, def isYear) {
    if (isYear && (row.taxBase == null || row.taxRate == null || row.cadastrePriceTaxFree == null) ||
            !isYear && (row.cadastrePriceJanuary == null || row.cadastrePriceTaxFree == null || row.taxRate == null)) {
        return null
    }
    def tmp
    if (isYear) {
        tmp = row.taxBase * row.taxRate / 100 - row.cadastrePriceTaxFree
    } else {
        tmp = (row.cadastrePriceJanuary - row.cadastrePriceTaxFree) * -row.taxRate / 100
    }
    return roundValue(tmp, 2)
}

/**
 * В формах за 1 кв, полгода и 9 месяцев искать одинаковые строки по графам 2, 3, 4, 5, 7, 8.

 * @param row строка
 * @param isYear признак налоговый ли это периода (не 1 квартал, не полгоде, не 9 месяцев)
 * @return сумма по графе 15 найденных строк
 */
def calc18(def row, def isYear) {
    if (!isYear) {
        return null
    }
    def tmp = BigDecimal.ZERO
    // алиасы для поиска нужных строк (графа 2, 3, 4, 5, 7, 8, 9)
    def searchAliases = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'sign', 'cadastreNumBuilding', 'cadastreNumRoom']

    // получить формы за 1 кв, полгода, 9 месяцев
    def forms = getPrevForms()
    for (def form : forms) {
        if (form.state == WorkflowState.ACCEPTED) {
            def formDataRows = formDataService.getDataRowHelper(form)?.allSaved
            for (def formRow : formDataRows) {
                if (formRow.getAlias() == null && !isDiffRow(row, formRow, searchAliases)) {
                    tmp += (row.sum ?: BigDecimal.ZERO)
                    break
                }
            }
        }
    }
    return roundValue(tmp, 2)
}

def calc19(def row, def isYear) {
    if (!isCalc19(row)) {
        records = getRecords(row, false)
        if (records == null || records.isEmpty()) {
            return null
        }
        def record = records?.get(0)

        def reductionPercent = record?.REDUCTION_PCT?.value
        def reductionSum = record?.REDUCTION_SUM?.value
        if (reductionPercent != null) {
            if (isYear) {
                return (row.sum - row.periodSum) * reductionPercent
            } else {
                return (row.sum * reductionPercent / 100) / 4
            }
        } else if (reductionSum != null) {
            if (isYear) {
                return reductionSum
            } else {
                return reductionSum / 4
            }
        }
    } else {
        return null
    }
}

def getTaxBenefitCode(def parentRecordId) {
    def recordId = getRefBookValue(203, parentRecordId).TAX_BENEFIT_ID.value
    return  getRefBookValue(202, recordId).CODE.value
}

/** Получить предыдущие формы за текущий год. */
def getPrevForms() {
    if (!prevForms) {
        prevForms = []
        def reportPeriods = getPrevReportPeriods()
        for (def reportPeriod : reportPeriods) {
            // получить формы за 1 кв, полгода, 9 месяцев
            def form = formDataService.getLast(formData.formType.id, FormDataKind.SUMMARY, formDataDepartment.id, reportPeriod.id, null, formData.comparativePeriodId, formData.accruing)
            if (form) {
                prevForms.add(form)
            }
        }
    }
    return prevForms
}

/** Получить предыдущие преиоды за год. */
def getPrevReportPeriods() {
    if (prevReportPeriods == null) {
        prevReportPeriods = []
        // получить периоды за год
        def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.PROPERTY, getYearStartDate(), getReportPeriodEndDate())
        for (def reportPeriod : reportPeriods) {
            if (reportPeriod.id == formData.reportPeriodId) {
                continue
            }
            prevReportPeriods.add(reportPeriod)
        }
    }
    return prevReportPeriods
}

def checkPrevForm() {
    // 2. Проверка заполнения атрибута «Регион» подразделения текущей формы (справочник «Подразделения»)
    if (formDataDepartment.regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }

    // 1. Проверить существование и принятость форм, а также наличие данных в них.
    if (!isPeriodYear()) {
        return
    }
    def reportPeriods = getPrevReportPeriods()

    for (def reportPeriod : reportPeriods) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.SUMMARY, formDataDepartment.id,
                reportPeriod.id, false, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

/**
 * Получить срок владения.
 *
 * @param sourceRow строка источника
 */
def getTenure(def sourceRow) {
    def start = getReportPeriodStartDate()
    def end = getReportPeriodEndDate()
    if (sourceRow.propertyRightEndDate != null && sourceRow.propertyRightEndDate < start ||
            sourceRow.propertyRightBeginDate > end) {
        return 0
    }

    def from = sourceRow.propertyRightBeginDate
    def to = sourceRow.propertyRightEndDate
    if (sourceRow.propertyRightEndDate == null || sourceRow.propertyRightEndDate > end) {
        to = end
    }
    if (sourceRow.propertyRightBeginDate == null || sourceRow.propertyRightBeginDate < start) {
        from = start
    }
    return to.format('M').toInteger() - from.format('M').toInteger() + 1
}

/**
 * Получить данные из справочника 201 "Ставки налога на имущество".
 *
 * @param row строка
 * @param is201 true - из 201 справочника, false - из 203 справочника
 * @return список записей из справочника
 */
def getRecords(def row, def is201) {
    if (!is201) {
        return Arrays.asList(getRefBookValue(203, row.taxBenefitCode))
    } else if (row.subject != null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, 201, providerCache)
        def filter = "DECLARATION_REGION_ID = ${formDataDepartment.regionId} and REGION_ID = ${row.subject}"
        if (recordsMap[filter] == null) {
            recordsMap[filter] = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        }
        return recordsMap[filter]
    }
    return null
}
// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def row = dataRows.get(i)
    def newRow = getSubTotal(i, row.taxAuthority, row.kpp, null)

    totalColumns.each {
        newRow[it] = 0
    }
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)
        totalColumns.each {
            newRow[it] += (row[it] ?: 0)
        }
    }
    return newRow
}

/** Добавить итоги и промежуточные итоги. */
void addFixedRows(def dataRows, totalRow) {
    // промежуточные итоги
    addAllAliased(dataRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, dataRows)
        }
    }, groupColumns)

    // итоги
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
}

/**
 * Условие при расчете граф 14 для получения данных из справочника 201 "Ставки налога на имущество"
 * или из справочника 203 "Параметры налоговых льгот налога на имущество".
 */
def isRefBook201ForCalc16(row) {
    return row.taxBenefitCode == null || getTaxBenefitCode(row.taxBenefitCode) != '2012400'
}

/**
 * Условие при расчете граф 17 для получения данных из справочника 203 "Параметры налоговых льгот налога на имущество".
 */
def isCalc19(row) {
    return row.taxBenefitCode == null || getTaxBenefitCode(row.taxBenefitCode) != '2012500'
}

/** Получить признак является ли текущий период годом. */
def isPeriodYear() {
    return getReportPeriod()?.order == 4
}

void sort(def dataRows) {
    dataRows.sort { def a, def b ->
        // графа 2  - subject (справочник)
        // графа 3  - taxAuthority
        // графа 4  - kpp
        // графа 5  - oktmo
        // графа 7  - sign
        // графа 8  - cadastreNumBuilding
        // графа 9  - cadastreNumRoom

        def valuesA = [(a.subject ? getRefBookValue(4, a.subject)?.NAME?.value : null), a.taxAuthority, a.kpp,
                       (a.oktmo ? getRefBookValue(96, a.oktmo)?.CODE?.value : null), a.sign, a.cadastreNumBuilding, a.cadastreNumRoom]
        def valuesB = [(b.subject ? getRefBookValue(4, b.subject)?.NAME?.value : null), b.taxAuthority, b.kpp,
                       (b.oktmo ? getRefBookValue(96, b.oktmo)?.CODE?.value : null), b.sign, b.cadastreNumBuilding, b.cadastreNumRoom]

        for (int i = 0; i < 7; i++) {
            def valueA = valuesA[i]
            def valueB = valuesB[i]
            if (valueA != valueB) {
                return valueA <=> valueB
            }
        }
        return 0
    }
}
// Проверка заполнения атрибута «Регион» подразделения текущей формы (справочник «Подразделения»)
void checkRegionId() {
    if (formDataDepartment.regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }
}
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 19
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()
    def totalRowFromFile = null
    def subTotalRows = [:]

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP].contains("Итого по НО ")) {
            rowIndex++
            def subTotal = getNewSubTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            rows.add(subTotal)
            subTotalRows[subTotal.fix] = subTotal

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (rowValues[INDEX_FOR_SKIP] == "Общий итог") {
            totalRowFromFile = getNewTotalFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // итоговая строка из макета//
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows
    def totalRow = getDataRow(templateRows, 'total')

    // сравнение подитогов
    def onlySimpleRows = rows.findAll { it.getAlias() == null }
    addFixedRows(onlySimpleRows, totalRow)
    def onlySubTotalTmpRows = onlySimpleRows.findAll { it.getAlias() != null }
    onlySubTotalTmpRows.each { subTotalTmpRow ->
        def key = subTotalTmpRow.fix
        def subTotalRow = subTotalRows[key]
        compareTotalValues(subTotalRow, subTotalTmpRow, totalColumns, logger, false)
    }

    // сравнение итога
    rows.add(totalRow)
    updateIndexes(rows)
    compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}
/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]) : getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[0][2]) : getColumnName(tmpRow, 'subject')]),
            ([(headerRows[0][3]) : getColumnName(tmpRow, 'taxAuthority')]),
            ([(headerRows[0][4]) : getColumnName(tmpRow, 'kpp')]),
            ([(headerRows[0][5]) : getColumnName(tmpRow, 'oktmo')]),
            ([(headerRows[0][6]) : getColumnName(tmpRow, 'address')]),
            ([(headerRows[0][7]) : getColumnName(tmpRow, 'sign')]),
            ([(headerRows[0][8]) : 'Кадастровый номер']),
            ([(headerRows[0][10]): 'Кадастровая стоимость']),
            ([(headerRows[0][14]): getColumnName(tmpRow, 'benefitBasis')]),
            ([(headerRows[0][15]): getColumnName(tmpRow, 'taxBase')]),
            ([(headerRows[0][16]): getColumnName(tmpRow, 'taxRate')]),
            ([(headerRows[0][17]): getColumnName(tmpRow, 'sum')]),
            ([(headerRows[0][18]): getColumnName(tmpRow, 'periodSum')]),
            ([(headerRows[0][19]): getColumnName(tmpRow, 'reductionPaymentSum')]),
            ([(headerRows[1][8]) : 'Здание']),
            ([(headerRows[1][9]) : 'Помещение']),
            ([(headerRows[1][10]): 'на 1 января']),
            ([(headerRows[1][11]): 'в т.ч. необлагаемая налогом']),
    ]

    (2..19).each {
        headerMapping.add(([(headerRows[2][it]): it.toString()]))
    }
    checkHeaderEquals(headerMapping, logger)
}
/**
 * Получить новую подитоговую строку из файла.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    // графа fix
    def title = values[1]

    def newRow = getSubTotal(rowIndex - 1, null, null, title)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    // графа 10
    newRow.cadastrePriceJanuary = parseNumber(values[10], fileRowIndex, 10 + colOffset, logger, true)
    // графа 11
    newRow.cadastrePriceTaxFree = parseNumber(values[11], fileRowIndex, 11 + colOffset, logger, true)

    return newRow
}
/**
 * Получить итоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewTotalFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    // графа 10
    newRow.cadastrePriceJanuary = parseNumber(values[10], fileRowIndex, 10 + colOffset, logger, true)
    // графа 11
    newRow.cadastrePriceTaxFree = parseNumber(values[11], fileRowIndex, 11 + colOffset, logger, true)

    return newRow
}
/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 1
    // графа fix
    // графа 2
    newRow.subject = getRecordIdImport(4, 'CODE', values[2], fileRowIndex, 2 + colOffset)
    // графа 3
    newRow.taxAuthority = values[3]
    // графа 4
    newRow.kpp = values[4]
    // графа 5
    newRow.oktmo = getRecordIdImport(96, 'CODE', values[5], fileRowIndex, 5 + colOffset)
    // графа 6
    newRow.address = values[6]
    // графа 7
    newRow.sign = parseNumber(values[7], fileRowIndex, 7 + colOffset, logger, true)
    // графа 8
    newRow.cadastreNumBuilding = values[8]
    // графа 9
    newRow.cadastreNumRoom = values[9]
    // графа 10
    newRow.cadastrePriceJanuary = parseNumber(values[10], fileRowIndex, 10 + colOffset, logger, true)
    // графа 11
    newRow.cadastrePriceTaxFree = parseNumber(values[11], fileRowIndex, 11 + colOffset, logger, true)
    // графа 12
    newRow.tenure = parseNumber(values[12], fileRowIndex, 12 + colOffset, logger, true)
    // графа 14
    newRow.benefitBasis = values[14]
    // графа 15
    newRow.taxBase = parseNumber(values[15], fileRowIndex, 15 + colOffset, logger, true)
    // графа 16
    newRow.taxRate = parseNumber(values[16], fileRowIndex, 16 + colOffset, logger, true)
    // графа 17
    newRow.sum = parseNumber(values[17], fileRowIndex, 17 + colOffset, logger, true)
    // графа 18
    newRow.periodSum = parseNumber(values[18], fileRowIndex, 18 + colOffset, logger, true)
    // графа 19
    newRow.reductionPaymentSum = parseNumber(values[19], fileRowIndex, 19 + colOffset, logger, true)
    // графа 13
    def record202Id = getRecordIdImport(202, 'CODE', values[13], fileRowIndex, 13 + colOffset)
    if (record202Id) {
        def declarationRegionId = formDataDepartment.regionId?.toString()
        def regionId = newRow.subject?.toString()
        def taxBenefinId = record202Id
        filter = "DECLARATION_REGION_ID = $declarationRegionId and REGION_ID = $regionId and TAX_BENEFIT_ID = $taxBenefinId and PARAM_DESTINATION = 2"
        def provider = formDataService.getRefBookProvider(refBookFactory, 203, providerCache)
        def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        def taxRecordId = records?.find { calcBasis(it?.record_id?.value) == newRow.benefitBasis }?.record_id?.value
        if (taxRecordId) {
            newRow.taxBenefitCode = taxRecordId
        } else {
            RefBook rb = getRefBook(203)
            logger.warn(REF_BOOK_NOT_FOUND_IMPORT_ERROR, fileRowIndex, getXLSColumnName(13 + colOffset), rb.getName(), rb.getAttribute('TAX_BENEFIT_ID').getName(), values[13], getReportPeriodEndDate().format('dd.MM.yyyy'))
        }
    }
    return newRow
}
def getRefBook(def id) {
    if (refBooks[id] == null) {
        refBooks[id] = refBookFactory.get(id)
    }
    return refBooks[id]
}

DataRow<Cell> getSubTotal(def int i, def taxAuthority, def kpp, String title) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.getCell('fix').colSpan = 5
    newRow.fix = (title != null ? title : 'Итого по НО ' + taxAuthority + ' и КПП ' + kpp)
    newRow.setAlias('total#'.concat(i.toString()))
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}