package form_template.property.property_945_4.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
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
        if (formData.kind != FormDataKind.SUMMARY) {
            logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
        }
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkPrevForm()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        checkPrevForm()
        logicCheck()
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
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def refBookCache = [:]

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

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

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

    dataRowHelper.save(dataRows)
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

        def has201Error = false
        def has203Error = false

        // для графы 16
        def is201 = isRefBook201ForCalc16(row)
        def records = getRecords(row, is201)
        if (records == null || records.isEmpty()) {
            if (is201) {
                // 1. Проверка существования налоговой ставки по заданному субъекту
                logger.error(errorMsg + 'Для текущего субъекта в справочнике «Ставки налога на имущество» не найдена налоговая ставка!')
                has201Error = true
            } else {
                // 2. Проверка существования параметров для заданного субъекта и льготы
                logger.error(errorMsg + 'Для текущего субъекта не предусмотрена заданная налоговая льгота (в справочнике «Параметры налоговых льгот налога на имущество» отсутствует запись по текущему субъекту и льготе, в которой категория имущества не заполнена)!')
                has203Error = true
            }
        }

        // для графы 19
        is201 = isRefBook201ForCalc19(row)
        records = getRecords(row, is201)
        if (records == null || records.isEmpty()) {
            if (is201) {
                if (!has201Error) {
                    // 1. Проверка существования налоговой ставки по заданному субъекту
                    logger.error(errorMsg + 'Для текущего субъекта в справочнике «Ставки налога на имущество» не найдена налоговая ставка!')
                }
            } else if (!has203Error) {
                // 2. Проверка существования параметров для заданного субъекта и льготы
                logger.error(errorMsg + 'Для текущего субъекта не предусмотрена заданная налоговая льгота (в справочнике «Параметры налоговых льгот налога на имущество» отсутствует запись по текущему субъекту и льготе, в которой категория имущества не заполнена)!')
            }
        }
    }
}

def consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def totalRow = getDataRow(dataRows, 'total')

    // удалить фиксированные строки
    deleteAllAliased(dataRows)

    // форма 945.2
    def sourceFormTypeId = 611

    // собрать из источников строки
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == sourceFormTypeId) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceRows = formDataService.getDataRowHelper(source).getAll()
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
    dataRowHelper.save(dataRows)
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
    // алиасы для поиска нужных строк (графа 2, 3, 4, 5, 7, 8)
    def searchAliases = ['subject', 'taxAuthority', 'kpp', 'oktmo', 'sign', 'cadastreNum']

    // получить формы за 1 кв, полгода, 9 месяцев
    def forms = getPrevForms()
    for (def form : forms) {
        if (form.state == WorkflowState.ACCEPTED) {
            def formDataRows = formDataService.getDataRowHelper(form)?.allCached
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
    if (!isRefBook201ForCalc19(row)) {
        records = getRecords(row, false)
        if (records == null || records.isEmpty()) {
            logger.error("Не найдены записи в справочниках для графы 19")
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
            def form = formDataService.getLast(formData.formType.id, FormDataKind.SUMMARY, formDataDepartment.id, reportPeriod.id, null)
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
    if (!isPeriodYear()) {
        return
    }
    def reportPeriods = getPrevReportPeriods()
    // проверить существование и принятость форм, а также наличие данных в них.
    for (def reportPeriod : reportPeriods) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.SUMMARY, formDataDepartment.id, reportPeriod.id, false, logger, true)
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
    } else {
        def provider = formDataService.getRefBookProvider(refBookFactory, 201, providerCache)
        def filter = "REGION_ID = ${row.subject}"
        if (recordsMap[filter] == null) {
            recordsMap[filter] = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        }
        return recordsMap[filter]
    }
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('fix').colSpan = 5
    def row = dataRows.get(i)
    newRow.fix = 'Итого по НО ' + row.taxAuthority + ' и КПП ' + row.kpp
    newRow.setAlias('total#'.concat(i.toString()))
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }

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
def isRefBook201ForCalc19(row) {
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