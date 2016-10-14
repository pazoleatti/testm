package form_template.land.include_in_declaration.v2016

import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.FormType
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.DaoException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Земельные участки, подлежащие включению в декларацию.
 *
 * formTemplateId = 917
 * formTypeId = 917
 *
 * TODO:
 *      - тесты для логических проверок
 */

// графа 1  - rowNumber
// графа 2  - department           - атрибут 161 - NAME - «Наименование подразделения», справочник 30 «Подразделения»
// графа 3  - kno
// графа 4  - kpp
// графа 5  - kbk                  - атрибут 7031 - CODE - «Код», справочник 703 «Коды бюджетной классификации земельного налога»
// графа 6  - oktmo                - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
// графа 7  - cadastralNumber
// графа 8  - landCategory         - атрибут 7021 - CODE - «Код», справочник 702 «Категории земли»
// графа 9  - constructionPhase    - атрибут 7011 - CODE - «Код», справочник 701 «Периоды строительства»
// графа 10 - cadastralCost
// графа 11 - taxPart
// графа 12 - ownershipDate
// графа 13 - terminationDate
// графа 14 - period
// графа 15 - benefitCode          - атрибут 7053.7041 - TAX_BENEFIT_ID.CODE - «Код налоговой льготы».«Код», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 16 - benefitBase          - зависит от графы 15 - атрибут 7053.7043 - TAX_BENEFIT_ID.BASE - «Код налоговой льготы».«Основание», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 17 - benefitParam         - зависит от графы 15 - атрибут 7061 - REDUCTION_PARAMS - «Параметры льготы», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 18 - startDate
// графа 19 - endDate
// графа 20 - benefitPeriod
// графа 21 - taxRate
// графа 22 - kv
// графа 23 - kl
// графа 24 - sum
// графа 25 - q1
// графа 26 - q2
// графа 27 - q3
// графа 28 - year

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        consolidation()
        calc()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.GET_SOURCES:
        getSources()
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]

@Field
def refBookCache = [:]

@Field
def allColumns = ['rowNumber', 'department', 'kno', 'kpp', 'kbk', 'oktmo', 'cadastralNumber',
        'landCategory', 'constructionPhase', 'cadastralCost', 'taxPart', 'ownershipDate', 'terminationDate',
        'period', 'benefitCode', 'benefitBase', 'benefitParam', 'startDate', 'endDate', 'benefitPeriod',
        'taxRate', 'kv', 'kl', 'sum', 'q1', 'q2', 'q3', 'year']

// Автозаполняемые атрибуты (все кроме редактируемых)
@Field
def autoFillColumns = allColumns

// Проверяемые на пустые значения атрибуты (графа 1..8, 10, 12, 14, 21, 22, 25..28)
@Field
def nonEmptyColumns = ['rowNumber', 'department', 'kno', 'kpp', 'kbk', 'oktmo', 'cadastralNumber',
        'landCategory', 'cadastralCost', 'ownershipDate', 'period', 'taxRate', 'kv', 'q1', 'q2', 'q3', 'year']

// графа 3, 4, 6
@Field
def groupColumns = ['kno', 'kpp', 'oktmo']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

@Field
def reportPeriod = null

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return reportPeriod
}

//// Обертки методов

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def isCalc = (formDataEvent == FormDataEvent.CALCULATE)

    def reportPeriod = getReportPeriod()
    def year = reportPeriod.taxPeriod.year
    def startYearDate = Date.parse('dd.MM.yyyy', '01.01.' + year)
    def cadastralNumberMap = [:]
    def allRecords705 = (dataRows.size() > 0 ? getAllRecords(705L) : null)

    // для логической проверки 11
    def needValue = [:]
    // графа 14, 20, 22..28
    def arithmeticCheckAlias = ['period', 'benefitPeriod', 'kv', 'kl', 'sum', 'q1', 'q2', 'q3', 'year']

    // для логической проверки 12
    def records710Map = [:]
    def records710 = getAllRecords(710L)
    for (def record : records710) {
        def key = record?.KPP?.value
        if (records710Map[key] == null) {
            records710Map[key] = record
        }
    }

    for (def row : dataRows) {
        if (row.getAlias()) {
            continue
        }
        def rowIndex = row.getIndex()

        // 1. Проверка обязательности заполнения граф
        checkNonEmptyColumns(row, rowIndex, nonEmptyColumns, logger, true)

        // 2. Проверка одновременного заполнения данных о налоговой льготе
        def value15 = (row.benefitCode ? true : false)
        def value18 = (row.startDate ? true : false)
        if (value15 ^ value18) {
            logger.error("Строка %s: Данные о налоговой льготе указаны не полностью", rowIndex)
        }

        // 3. Проверка корректности заполнения даты возникновения права собственности
        if (row.ownershipDate != null && row.ownershipDate > getReportPeriodEndDate()) {
            def columnName12 = getColumnName(row, 'ownershipDate')
            def dateStr = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно %s", rowIndex, columnName12, dateStr)
        }

        // 4. Проверка корректности заполнения даты прекращения права собственности
        if (row.terminationDate != null && (row.terminationDate < row.ownershipDate || row.terminationDate < startYearDate)) {
            def columnName13 = getColumnName(row, 'terminationDate')
            def dateStr = '01.01.' + year
            def columnName12 = getColumnName(row, 'ownershipDate')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значению графы «%s»",
                    rowIndex, columnName13, dateStr, columnName12)
        }

        // 5. Проверка доли налогоплательщика в праве на земельный участок
        def tmp = logicCheck5(row.taxPart)
        if (tmp != null && tmp.isEmpty()) {
            def columnName11 = getColumnName(row, 'taxPart')
            logger.error("Строка %s: Графа «%s» должна быть заполнена согласно формату: " +
                    "«(от 1 до 10 числовых знаков) / (от 1 до 10 числовых знаков)», " +
                    "без лидирующих нулей в числителе и знаменателе, числитель должен быть меньше либо равен знаменателю",
                    rowIndex, columnName11)
        }

        // 6. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок
        tmp = logicCheck6(row.taxPart)
        if (tmp != null && tmp.isEmpty()) {
            def columnName11 = getColumnName(row, 'taxPart')
            logger.error("Строка %s: Значение знаменателя в графе «%s» не может быть равным нулю", rowIndex, columnName11)
        }

        // 7. Проверка корректности заполнения даты начала действия льготы
        if (row.startDate && row.startDate < row.ownershipDate) {
            def columnName18 = getColumnName(row, 'startDate')
            def columnName12 = getColumnName(row, 'ownershipDate')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»",
                    rowIndex, columnName18, columnName12)
        }

        // 8. Проверка корректности заполнения даты окончания действия льготы
        if (row.endDate && (row.endDate < row.startDate || row.terminationDate && row.terminationDate < row.endDate)) {
            def columnName19 = getColumnName(row, 'endDate')
            def columnName18 = getColumnName(row, 'startDate')
            def columnName13 = getColumnName(row, 'terminationDate')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s» и быть меньше либо равно значению графы «%s»",
                    rowIndex, columnName19, columnName18, columnName13)
        }

        // 9. Проверка наличия в реестре земельных участков с одинаковым кадастровым номером и кодом ОКТМО, периоды владения которых пересекаются
        // сбор данных
        if (row.oktmo && row.cadastralNumber) {
            def groupKey = getRefBookValue(96L, row.oktmo)?.CODE?.value + "#" + row.cadastralNumber
            if (cadastralNumberMap[groupKey] == null) {
                cadastralNumberMap[groupKey] = []
            }
            cadastralNumberMap[groupKey].add(row)
        }

        // 10. Проверка корректности заполнения кода налоговой льготы (графа 15)
        if (row.benefitCode && row.oktmo) {
            def findRecord = allRecords705.find { it?.record_id?.value == row.benefitCode }
            if (findRecord && findRecord?.OKTMO?.value != row.oktmo) {
                def columnName15 = getColumnName(row, 'benefitCode')
                def columnName6 = getColumnName(row, 'oktmo')
                logger.error("Строка %s: Код ОКТМО, в котором действует выбранная в графе «%s» льгота, должен быть равен значению графы «%s»",
                        rowIndex, columnName15, columnName6)
            }
        }

        // 11. Проверка корректности заполнения граф 14, 20, 22-28
        if (!isCalc) {
            needValue.period = calc14(row)
            needValue.benefitPeriod = calc20(row)
            needValue.kv = calc22(row)
            needValue.kl = calc23(row)
            needValue.sum = calc24(row, row.kv, row.kl)
            needValue.q1 = calc25(row)
            needValue.q2 = calc26(row)
            needValue.q3 = calc27(row)
            needValue.year = calc28(row)
            def errorColumns = []
            for (def alias : arithmeticCheckAlias) {
                if (needValue[alias] == null && row[alias] == null) {
                    continue
                }
                if (needValue[alias] == null || row[alias] == null || needValue[alias].compareTo(row[alias]) != 0) {
                    errorColumns.add(getColumnName(row, alias))
                }
            }
            if (!errorColumns.isEmpty()) {
                def columnNames = errorColumns.join('», «')
                logger.error("Строка %s: Графы «%s» заполнены неверно", rowIndex, columnNames)
            }
        }

        // 12. Проверка правильности заполнения КПП
        if (row.kpp && records710Map[row.kpp] == null) {
            logger.error("Строка %s: Не найдено ни одного подразделения, для которого на форме настроек подразделений существует запись с КПП равным «%s»",
                    rowIndex, row.kpp)
        }
    }

    // 9. Проверка наличия в реестре земельных участков с одинаковым кадастровым номером и кодом ОКТМО, периоды владения которых пересекаются
    if (!cadastralNumberMap.isEmpty()) {
        def groupKeys = cadastralNumberMap.keySet().toList()
        for (def groupKey : groupKeys) {
            def rows = cadastralNumberMap[groupKey]
            if (rows.size() <= 1) {
                continue
            }
            def rowIndexes = []
            def tmpRows = rows.collect { it }
            rows.each { row ->
                def start = row.ownershipDate
                def end = (row.terminationDate ?: getReportPeriodEndDate())
                tmpRows.remove(row)
                tmpRows.each { row2 ->
                    def start2 = row2.ownershipDate
                    def end2 = (row2.terminationDate ?: getReportPeriodEndDate())
                    if (!(start > end2 || start2 > end)) {
                        rowIndexes.add(row.getIndex())
                        rowIndexes.add(row2.getIndex())
                    }
                }
            }
            rowIndexes = rowIndexes.unique().sort()
            if (rowIndexes) {
                rowIndexes = rowIndexes.join(', ')
                def value7 = rows[0].cadastralNumber
                def value6 = getRefBookValue(96L, rows[0].oktmo)?.CODE?.value
                logger.error("Строки %s. Кадастровый номер земельного участка «%s», Код ОКТМО «%s»: на форме не должно быть строк с одинаковым кадастровым номером, кодом ОКТМО и пересекающимися периодами владения правом собственности",
                        rowIndexes, value7, value6)
            }
        }
    }
}

/**
 * Логическая проверка 5. Проверка доли налогоплательщика в праве на земельный участок.
 *
 * @param taxPart значение графы 11
 * @return null - если значение taxPart пусто, пустой список - если ошибка, список из двух элементов (числитель и знаменатель) - все нормально
 */
def logicCheck5(def taxPart) {
    if (!taxPart) {
        return null
    }
    def partArray = taxPart?.split('/')
    if (!(taxPart ==~ /\d{1,10}\/\d{1,10}/) ||
            partArray[0].toString().startsWith('0') ||
            partArray[1].toString().startsWith('0') ||
            partArray[0].toBigDecimal() > partArray[1].toBigDecimal()) {
        return []
    }
    return [partArray[0].toBigDecimal(), partArray[1].toBigDecimal()]
}

/**
 * Логическая проверка 5. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок.
 *
 * @param taxPart значение графы 11
 * @return null - если значение taxPart пусто, пустой список - если ошибка, список из двух элементов (числитель и знаменатель) - все нормально
 */
def logicCheck6(def taxPart) {
    if (!taxPart) {
        return null
    }
    def partArray = taxPart?.split('/')

    // 6. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок
    if (partArray.size() != 2 || partArray.size() == 2 && partArray[1] ==~ /\d{1,}/ && partArray[1].toBigDecimal() == 0) {
        return []
    }
    return [partArray[0].toBigDecimal(), partArray[1].toBigDecimal()]
}

void calc() {
    // нет расчетов, но в логических проверках есть проверка расчетов
}

def calc14(def row) {
    if (row.ownershipDate == null) {
        return null
    }
    BigDecimal tmp
    if (row.terminationDate && row.terminationDate < getReportPeriodStartDate() || row.ownershipDate > getReportPeriodEndDate()) {
        tmp = BigDecimal.ZERO
    } else {
        def end = (row.terminationDate == null || row.terminationDate > getReportPeriodEndDate() ? getReportPeriodEndDate() : row.terminationDate)
        def start = (row.ownershipDate < getReportPeriodStartDate() ? getReportPeriodStartDate() : row.ownershipDate)
        def startM = start.format('M').toInteger() + (start.format('d').toInteger() > 15 ? 1 : 0)
        def endM = end.format('M').toInteger() - (end.format('d').toInteger() > 15 ? 0 : 1)
        tmp = endM - startM + 1
    }
    return round(tmp, 0)
}

// метод взят из формы "Реестр земельных участков" (form_template.land.land_registry.v2016) - там это calc16()
def calc20(def row) {
    if (row.startDate == null) {
        return null
    }
    BigDecimal tmp
    if (row.endDate && row.endDate < getReportPeriodStartDate() || row.startDate > getReportPeriodEndDate()) {
        tmp = BigDecimal.ZERO
    } else {
        def end = (row.endDate == null || row.endDate > getReportPeriodEndDate() ? getReportPeriodEndDate() : row.endDate)
        def start = (row.startDate < getReportPeriodStartDate() ? getReportPeriodStartDate() : row.startDate)
        tmp = end.format('M').toInteger() - start.format('M').toInteger() + 1
    }
    return round(tmp, 0)
}

def calc22(def row, def periodOrder = null) {
    if (row.period == null) {
        return null
    }
    def n = getMonthCount(periodOrder)
    // Графа 22 = ОКРУГЛ(Графа 14/N; 4)
    return row.period.divide(n, 4, BigDecimal.ROUND_HALF_UP)
}

/** Получить количество месяцев в периоде. Для периода "год" равен 12 месяцев, для остальные периодов - 3. */
def getMonthCount(def periodOrder = null) {
    def order = (periodOrder ?: getReportPeriod()?.order)
    return (order == 4 ? 12 : 3)
}

def calc23(def row, def periodOrder = null) {
    if (row.benefitCode == null) {
        return null
    }
    def termUse = calc20(row)
    if (termUse == null || termUse == 0) {
        return termUse
    }
    def n = getMonthCount(periodOrder)
    BigDecimal tmp = (n - termUse) / n
    return round(tmp, 4)
}

def calc24(def row, def value22, def value23) {
    if (row.cadastralCost == null || row.taxRate == null || value22 == null || row.benefitCode == null) {
        return null
    }
    def taxPart = calcTaxPart(row.taxPart)
    if (taxPart == null) {
        return null
    }
    def k = getK(row)
    int precision = 2 // точность при делении
    // A - сумма исчисленного налога (сумма налога без учета суммы льготы)
    // A = Графа 10 * Графа 11 * Графа 21 * Графа 22 * К / 100
    def a = row.cadastralCost.multiply(taxPart).multiply(row.taxRate).multiply(value22).multiply(k).divide(100, precision, BigDecimal.ROUND_HALF_UP)

    def record705 = getRefBookValue(705L, row.benefitCode)
    def record704Id = record705?.TAX_BENEFIT_ID?.value
    def code15 = getRefBookValue(704L, record704Id)?.CODE?.value
    def check5 = getCheckValue15(code15)
    if (!check5) {
        return null
    }
    def p = getP(code15, record705)

    BigDecimal tmp = null
    if (check5 == 1 || check5 == 2) {
        tmp = null
    } else if (check5 == 3 && p != null && value23 != null) {
        // Графа 24 = А * Р / 100 * (1 – Графа 23)
        tmp = a.multiply(p).divide(100, precision, BigDecimal.ROUND_HALF_UP).multiply(1 - value23)
    } else if (check5 == 4 && p != null && value23 != null) {
        if (p < row.taxRate) {
            // Графа 24 = А * (Графа 21 – Р) / 100 * (1 – Графа 23)
            tmp = a.multiply(row.taxRate - p).divide(100, precision, BigDecimal.ROUND_HALF_UP).multiply(1 - value23)
        }
    } else if (check5 == 5 && value23 != null) {
        // Графа 24 = A * (1 – Графа 23)
        tmp = a * (1 - value23)
    }
    return round(tmp, 0)
}

def calc25(def row) {
    return calc25_27(row, 1)
}

def calc26(def row) {
    return calc25_27(row, 2)
}

def calc27(def row) {
    return calc25_27(row, 3)
}

def calc25_27(def row, def periodOrder) {
    if (getReportPeriod()?.order < periodOrder) {
        return null
    }
    def h = getH(row, periodOrder)
    if (h != null) {
        // Графа 25, 26, 27 = ОКРУГЛ(Н/4; 0);
        return h.divide(4, 0, BigDecimal.ROUND_HALF_UP)
    }
    return null
}

def calc28(def row) {
    if (getReportPeriod()?.order < 4) {
        return null
    }
    if (row.q1 == null || row.q2 == null || row.q3 == null) {
        return null
    }
    def h = getH(row, 4)
    if (h != null) {
        BigDecimal tmp = h - row.q1 - row.q2 - row.q3
        return round(tmp, 0)
    }
    return null
}

/** Коэффициент, увеличивающий налоговую базу, если на земельном участке ведется строительство. */
def getK(def row) {
    def code9 = getRefBookValue(701L, row.constructionPhase)?.CODE?.value
    BigDecimal k = (code9 == 1 ? 2 : (code9 == 2 ? 4 : 1))
    return round(k, 0)
}

/** Значение из справочника «Параметры налоговых льгот земельного налога», необходимое для расчета суммы налоговой льготы. */
def getP(def code15, def record705) {
    def check15 = getCheckValue15(code15)
    def tmp = null
    if (check15 == 1) {
        tmp = record705?.REDUCTION_SUM?.value
    } else if (check15 == 2) {
        tmp = calcTaxPart(record705?.REDUCTION_SEGMENT?.value)
    } else if (check15 == 3) {
        tmp = record705?.REDUCTION_PERCENT?.value
    } else if (check15 == 4) {
        tmp = record705?.REDUCTION_RATE?.value
    }
    return tmp
}

/**
 * Налог.
 * Для графы 25..28 необходимо пересчитывать значения используемых столбцов 22, 23, 24 с учетом своего периода.
 *
 * @param row строка нф
 * @param periodOrder порядковый номер периода в налоговом
 */
def getH(def row, def periodOrder) {
    if (row.taxRate == null) {
        return null
    }

    def value22 = (getReportPeriod()?.order != periodOrder ? calc22(row, periodOrder) : row.kv)
    if (value22 == null) {
        return null
    }

    def value23 = (getReportPeriod()?.order != periodOrder ? calc23(row, periodOrder) : row.kl)

    def value24 = (getReportPeriod()?.order != periodOrder ? calc24(row, value22, value23) : row.sum)
    if (value24 == null) {
        value24 = BigDecimal.ZERO
    }

    def k = getK(row)
    def b = getB(row, value23)
    if (b == null) {
        return null
    }
    int precision = 2 // точность при делении
    // Н = В * Графа 21 * Графа 22 * К / 100 – Графа 24;
    def tmp = b.multiply(row.taxRate).multiply(value22).multiply(k).divide(100, precision, BigDecimal.ROUND_HALF_UP).subtract(value24)

    if (tmp < 0) {
        return null
    }
    return tmp
}

/** Налоговая база. */
def getB(def row, def value23) {
    if (row.cadastralCost == null) {
        return null
    }
    def taxPart = calcTaxPart(row.taxPart)
    if (taxPart == null) {
        return null
    }

    def record705 = getRefBookValue(705L, row.benefitCode)
    def record704Id = record705?.TAX_BENEFIT_ID?.value
    def code15 = getRefBookValue(704L, record704Id)?.CODE?.value
    def check5 = getCheckValue15(code15)
    def p = getP(code15, record705)

    BigDecimal tmp = null
    BigDecimal defaultValue = row.cadastralCost * taxPart
    if (check5 == 1 && p != null && value23 != null) {
        tmp = defaultValue - p
        tmp = (tmp < 0 ? 0 : tmp)
    } else if (check5 == 2 && p != null && value23 != null) {
        tmp = defaultValue - defaultValue * p * (1 - value23)
    } else if (check5 == 0 || check5 != 1 || check5 != 2) {
        tmp = defaultValue
    }

    if (tmp < 0) {
        return null
    }
    return round(tmp, 20)
}

@Field
def checkValue15Map = [:]

// условия проверки кода графы 15
def getCheckValue15(def code15) {
    if (checkValue15Map[code15]) {
        return checkValue15Map[code15]
    }
    def tmp = 0
    if (code15 == '3022100' || code15?.startsWith('30212')) {
        tmp = 1
    } else if (code15 == '3022300') {
        tmp = 2
    } else if (code15 == '3022200') {
        tmp = 3
    } else if (code15 == '3022500') {
        tmp = 4
    } else if (code15 == '3022400' || code15?.startsWith('30211')) {
        tmp = 5
    }
    checkValue15Map[code15] = tmp
    return checkValue15Map[code15]
}

/**
 * Получить рассчитанное значение в графе 11.
 *
 * @param value значение графы 11 (или атрибут «Доля необлагаемой площади» справочник «Параметры налоговых льгот земельного налога» 705)
 * @return 1 - если value не задано, null - если value имеет неправильное значение, результат деления - если все нормально
 */
def calcTaxPart(def value) {
    if (!value) {
        return BigDecimal.ONE
    }
    def result5 = logicCheck5(value)
    def result6 = logicCheck6(value)
    if (!result5 || !result6) {
        return null
    }
    int precision = 20 // точность при делении
    return result5[0].divide(result5[1], precision, BigDecimal.ROUND_HALF_UP)
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 28
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
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
    def totalRowFromFileMap = [:]           // мапа для хранения строк итогов/подитогов со значениями из файла (стили простых строк)
    def totalRowMap = [:]                   // мапа для хранения строк итогов/подитогов нф с посчитанными значениями и со стилями

    def hasRegion = (formDataDepartment.regionId != null)
    if (!hasRegion) {
        def columnName15 = getColumnName(tmpRow, 'benefitCode')
        logger.warn("Не удалось заполнить графу «%s», т.к. для подразделения формы не заполнено поле «Регион» справочника «Подразделения»",
                columnName15)
    }

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
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, hasRegion)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

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
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping = [
            [(headerRows[0][1]) : headers[0].department],
            [(headerRows[0][2]) : headers[0].kno],
            [(headerRows[0][3]) : headers[0].kpp],
            [(headerRows[0][4]) : headers[0].kbk],
            [(headerRows[0][5]) : headers[0].oktmo],
            [(headerRows[0][6]) : headers[0].cadastralNumber],
            [(headerRows[0][7]) : headers[0].landCategory],
            [(headerRows[0][8]) : headers[0].constructionPhase],
            [(headerRows[0][9]) : headers[0].cadastralCost],
            [(headerRows[0][10]): headers[0].taxPart],
            [(headerRows[0][11]): headers[0].ownershipDate],
            [(headerRows[0][12]): headers[0].terminationDate],
            [(headerRows[0][13]): headers[0].period],
            [(headerRows[0][14]): headers[0].benefitCode],
            [(headerRows[1][14]): headers[1].benefitCode],
            [(headerRows[1][15]): headers[1].benefitBase],
            [(headerRows[1][16]): headers[1].benefitParam],
            [(headerRows[1][17]): headers[1].startDate],
            [(headerRows[1][18]): headers[1].endDate],
            [(headerRows[1][19]): headers[1].benefitPeriod],
            [(headerRows[0][20]): headers[0].taxRate],
            [(headerRows[0][21]): headers[0].kv],
            [(headerRows[0][22]): headers[0].kl],
            [(headerRows[0][23]): headers[0].sum],
            [(headerRows[0][24]): headers[0].q1],
            [(headerRows[1][24]): headers[1].q1],
            [(headerRows[1][25]): headers[1].q2],
            [(headerRows[1][26]): headers[1].q3],
            [(headerRows[1][27]): headers[1].year]
    ]
    (0..27).each {
        headerMapping.add([(headerRows[2][it]) : (it + 1).toString()])
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 * @param hasRegion признак необходимости заполнения графы 15, 16, 17
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def hasRegion) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2 - атрибут 161 - NAME - «Наименование подразделения», справочник 30 «Подразделения»
    def int colIndex = 1
    newRow.department = getRecordIdImport(30L, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 3
    colIndex++
    newRow.kno = values[colIndex]

    // графа 4
    colIndex++
    newRow.kpp = values[colIndex]

    // графа 5 - атрибут 7031 - CODE - «Код», справочник 703 «Коды бюджетной классификации земельного налога»
    colIndex++
    newRow.kbk = getRecordIdImport(703L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 6 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
    colIndex++
    if (values[colIndex]) {
        newRow.oktmo = getRecordIdImport(96L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    } else {
        def xlsColumnName6 = getXLSColumnName(colIndex + colOffset)
        def columnName15 = getColumnName(newRow, 'benefitCode')
        def columnName6 = getColumnName(newRow, 'oktmo')
        //
        logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. не заполнена графа «%s»",
                fileRowIndex, xlsColumnName6, columnName15, columnName6)
    }

    // графа 7
    colIndex++
    newRow.cadastralNumber = values[colIndex]

    // графа 8 - атрибут 7021 - CODE - «Код», справочник 702 «Категории земли»
    colIndex++
    newRow.landCategory = getRecordIdImport(702L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 9 - атрибут 7011 - CODE - «Код», справочник 701 «Периоды строительства»
    colIndex++
    newRow.constructionPhase = getRecordIdImport(701L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 10
    colIndex++
    newRow.cadastralCost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 11
    colIndex++
    newRow.taxPart = values[colIndex]

    // графа 12
    colIndex++
    newRow.ownershipDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 13
    colIndex++
    newRow.terminationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 14
    colIndex++
    newRow.period = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 15, 16, 17
    if (hasRegion) {
        // графа 15 - атрибут 7053 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 705 «Параметры налоговых льгот земельного налога»
        colIndex = 14
        def record704 = (values[5] ? getRecordImport(704, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset) : null)
        def code = record704?.record_id?.value
        def oktmo = newRow.oktmo
        def param = values[16] ?: null
        def record705 = getRecord705Import(code, oktmo, param)
        newRow.benefitCode = record705?.record_id?.value
        if (record704 && record705 == null) {
            def xlsColumnName15 = getXLSColumnName(colIndex + colOffset)
            def columnName15 = getColumnName(newRow, 'benefitCode')
            def columnName16 = getColumnName(newRow, 'benefitBase')
            def columnName17 = getColumnName(newRow, 'benefitParam')
            logger.warn("Строка %s, столбец %s: Не удалось заполнить графы: «%s», «%s», «%s», т.к. в справочнике " +
                    "«Параметры налоговых льгот земельного налога» не найдена соответствующая запись",
                    fileRowIndex, xlsColumnName15, columnName15, columnName16, columnName17)
        }

        // графа 16 - зависит от графы 15 - атрибут 7053.7043 - TAX_BENEFIT_ID.BASE - «Код налоговой льготы».«Основание», справочник 705 «Параметры налоговых льгот земельного налога»
        if (record704 && record705) {
            colIndex++
            def expectedValues = [record704?.BASE?.value]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'benefitBase'), record704?.CODE?.value, fileRowIndex, colIndex + colOffset, logger, false)
        }

        // графа 17 - зависит от графы 15 - атрибут 7061 - REDUCTION_PARAMS - «Параметры льготы», справочник 705 «Параметры налоговых льгот земельного налога»
        // проверять не надо, т.к. участвует в поиске родительской записи
    }

    // графа 18
    colIndex = 17
    newRow.startDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 19
    colIndex++
    newRow.endDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 20..28
    ['benefitPeriod', 'taxRate', 'kv', 'kl', 'sum', 'q1', 'q2', 'q3', 'year'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def columns = groupColumns + (allColumns - groupColumns)
    // массовое разыменование справочных и зависимых значений
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns())
    sortRows(dataRows, columns)
    dataRowHelper.saveSort()
}

def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

/**
 * Получить запись справочника 705 "Параметры налоговых льгот земельного налога".
 *
 * @param code графа 11 - код налоговой льготы (id справочника 704)
 * @param oktmo графа 3 - код ОКТМО (id справочника 96)
 * @param param графа 13 - параметры льготы (строка, может быть null)
 */
def getRecord705Import(def code, def oktmo, def param) {
    if (code == null || oktmo == null) {
        return null
    }
    def allRecords = getAllRecords(705L)
    for (def record : allRecords) {
        if (code == record?.TAX_BENEFIT_ID?.value && oktmo == record?.OKTMO?.value &&
                ((param ?: null) == (record?.REDUCTION_PARAMS?.value ?: null) || param?.equalsIgnoreCase(record?.REDUCTION_PARAMS?.value))) {
            return record
        }
    }
    return null
}

@Field
def allRecordsMap = [:]

def getAllRecords(def refbookId) {
    if (allRecordsMap[refbookId] == null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, refbookId, providerCache)
        def filter = (refbookId == 705 ? 'DECLARATION_REGION_ID = ' + formDataDepartment.regionId : null)
        allRecordsMap[refbookId] = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
    }
    return allRecordsMap[refbookId]
}

@Field
def sourceTypeId = 916

void consolidation() {
    // собрать значения КПП из формы настроек
    def kpps = []
    def records710 = getAllRecords(710L)
    for (def record710 : records710) {
        if (record710?.DEPARTMENT_ID?.value == formData.departmentId) {
            kpps.add(record710?.KPP?.value)
        }
    }
    if (!kpps) {
        logger.error("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        formDataService.getDataRowHelper(formData).allCached = []
        return
    }

    // графа 1..28
    def consolidationColumns = allColumns - 'rowNumber'
    def dataRows = []

    // получить источники
    def sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)

    // собрать данные из источнков
    for (Relation relation : sourcesInfo) {
        if (relation.formType.id != sourceTypeId) {
            continue
        }
        FormData sourceFormData = formDataService.get(relation.formDataId, null)
        def sourceDataRows = formDataService.getDataRowHelper(sourceFormData).allSaved
        for (def sourceRow : sourceDataRows) {
            if (!sourceRow.getAlias() && sourceRow.kpp in kpps) {
                def newRow = getNewRow()
                consolidationColumns.each { alias ->
                    newRow[alias] = sourceRow[alias]
                }
                dataRows.add(newRow)
            }
        }
    }
    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

/** Получить результат для события FormDataEvent.GET_SOURCES. */
void getSources() {
    // нестандратны только формы-источники - "Расчет земельного налога за отчетные периоды"
    if (!(form && needSources)) {
        // формы-приемники, декларации-истчоники, декларации-приемники не переопределять
        return
    }

    def reportPeriod = getReportPeriod()
    def reportPeriodId = reportPeriod?.id
    def periodOrder = null
    def comparativePeriodId = null
    def accruing = false
    def SBId = 0

    def departments = departmentService.getAllChildren(SBId)
    for (def department : departments) {
        def departmentId = department.id
        def tmpFormData = formDataService.getLast(sourceTypeId, FormDataKind.SUMMARY, departmentId,
                reportPeriodId, periodOrder, comparativePeriodId, accruing)
        if (tmpFormData == null) {
            continue
        }
        def relation = getRelation(tmpFormData, department, reportPeriod)
        if (relation) {
            sources.sourceList.add(relation)
        }
    }
    sources.sourcesProcessedByScript = true
}

/**
 * Получить запись для источника-приемника.
 *
 * @param tmpFormData нф
 * @param department подразделение
 * @param period период нф
 * @param monthOrder номер месяца (для ежемесячной формы)
 */
def getRelation(FormData tmpFormData, Department department, ReportPeriod period, Integer monthOrder = null) {
    // boolean excludeIfNotExist - исключить несозданные источники
    if (excludeIfNotExist && tmpFormData == null) {
        return null
    }
    // WorkflowState stateRestriction - ограничение по состоянию для созданных экземпляров
    if (stateRestriction && tmpFormData != null && stateRestriction != tmpFormData.state) {
        return null
    }
    Relation relation = new Relation()
    def isSource = true

    DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(tmpFormData?.departmentReportPeriodId) as DepartmentReportPeriod
    DepartmentReportPeriod comparativePeriod = getDepartmentReportPeriodById(tmpFormData?.comparativePeriodId) as DepartmentReportPeriod
    FormType formType = getFormTypeById(tmpFormData.formType.id) as FormType
    def performers = [getDepartmentById(tmpFormData.performer.printDepartmentId) as Department]

    // boolean light - заполняются только текстовые данные для GUI и сообщений
    if (light) {
        /**************  Параметры для легкой версии ***************/
        /** Идентификатор подразделения */
        relation.departmentId = tmpFormData.departmentId
        /** полное название подразделения */
        relation.fullDepartmentName = getDepartmentFullName(tmpFormData.departmentId)
        /** Дата корректировки */
        relation.correctionDate = departmentReportPeriod?.correctionDate
        /** Вид нф */
        relation.formTypeName = formType?.name
        /** Год налогового периода */
        relation.year = period.taxPeriod.year
        /** Название периода */
        relation.periodName = period.name
        /** Название периода сравнения */
        relation.comparativePeriodName = comparativePeriod?.reportPeriod?.name
        /** Дата начала периода сравнения */
        relation.comparativePeriodStartDate = comparativePeriod?.reportPeriod?.startDate
        /** Год периода сравнения */
        relation.comparativePeriodYear = comparativePeriod?.reportPeriod?.taxPeriod?.year
        /** название подразделения-исполнителя */
        relation.performerNames = [tmpFormData.performer.name]
    }
    /**************  Общие параметры ***************/
    /** подразделение */
    relation.department = department
    /** Период */
    relation.departmentReportPeriod = departmentReportPeriod
    /** Статус ЖЦ */
    relation.state = tmpFormData?.state
    /** форма/декларация создана/не создана */
    relation.created = (tmpFormData != null)
    /** является ли форма источников, в противном случае приемник*/
    relation.source = isSource
    /** Введена/выведена в/из действие(-ия) */
    try {
        relation.status = getFormTemplateById(tmpFormData.formTemplateId) != null
    } catch (DaoException e) {
        relation.status = false
    }
    /** Налог */
    relation.taxType = TaxType.LAND

    /**************  Параметры НФ ***************/
    /** Идентификатор созданной формы */
    relation.formDataId = tmpFormData?.id
    /** Вид НФ */
    relation.formType = formType
    /** Тип НФ */
    relation.formDataKind = tmpFormData.kind
    /** подразделение-исполнитель*/
    relation.performers = performers
    /** Период сравнения. Может быть null */
    relation.comparativePeriod = comparativePeriod
    /** Номер месяца */
    relation.month = monthOrder
    if (tmpFormData) {
        /** Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения) */
        relation.accruing = tmpFormData.accruing
        /** Признак ручного ввода */
        relation.manual = tmpFormData.manual
    }
    return relation
}

@Field
def departmentReportPeriodMap = [:]

def getDepartmentReportPeriodById(def id) {
    if (id != null && departmentReportPeriodMap[id] == null) {
        departmentReportPeriodMap[id] = departmentReportPeriodService.get(id)
    }
    return departmentReportPeriodMap[id]
}

// Мапа для хранения типов форм (id типа формы -> тип формы)
@Field
def formTypeMap = [:]

/** Получить тип фомры по id. */
def getFormTypeById(def id) {
    if (formTypeMap[id] == null) {
        formTypeMap[id] = formTypeService.get(id)
    }
    return formTypeMap[id]
}


// Мапа для хранения подразделений (id подразделения -> подразделение)
@Field
def departmentMap = [:]

/** Получить подразделение по id. */
def getDepartmentById(def id) {
    if (id != null && departmentMap[id] == null) {
        departmentMap[id] = departmentService.get(id)
    }
    return departmentMap[id]
}

@Field
def formTemplateMap = [:]

def getFormTemplateById(def formTemplateId) {
    if (formTemplateId == null) {
        return null
    }

    if (formTemplateMap[formTemplateId] == null) {
        formTemplateMap[formTemplateId] = formDataService.getFormTemplate(formTemplateId)
    }
    return formTemplateMap[formTemplateId]
}

// Мапа для хранения полного названия подразделения (id подразделения  -> полное название)
@Field
def departmentFullNameMap = [:]

/** Получить полное название подразделения по id подразделения. */
def getDepartmentFullName(def id) {
    if (departmentFullNameMap[id] == null) {
        departmentFullNameMap[id] = departmentService.getParentsHierarchy(id)
    }
    return departmentFullNameMap[id]
}