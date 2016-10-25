package form_template.transport.summary.v2014

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

/**
 * Расчет суммы налога по каждому транспортному средству
 * formTemplateId=203
 *
 * графа 1  - rowNumber
 * графа 2  - taxAuthority
 * графа 3  - kpp
 * графа 4  - okato 96 840 CODE
 * графа 5  - tsTypeCode 42 422 CODE
 * графа 6  - tsType 42 423 NAME
 * графа 7  - model
 * графа 8  - ecoClass 40 400 CODE
 * графа 9  - vi
 * графа 10 - regNumber
 * графа 11 - regDate
 * графа 12 - regDateEnd
 * графа 13 - taxBase
 * графа 14 - taxBaseOkeiUnit 12 57 CODE
 * графа 15 - createYear
 * графа 16 - years
 * графа 17 - stealDateStart
 * графа 18 - stealDateEnd
 * графа 19 - periodStartCost
 * графа 20 - periodEndCost
 * графа 21 - ownMonths
 * графа 22 - partRight
 * графа 23 - coef362
 * графа 24 - taxRate 41 416 VALUE
 * графа 25 - calculatedTaxSum
 * графа 26 - benefitMonths
 * графа 27 - benefitStartDate
 * графа 28 - benefitEndDate
 * графа 29 - coefKl
 * графа 30 - taxBenefitCode 7 19 TAX_BENEFIT_ID 6 16 NAME
 * графа 31 - benefitSum
 * графа 32 - taxBenefitCodeDecrease 7 19 TAX_BENEFIT_ID 6 16 NAME
 * графа 33 - benefitSumDecrease
 * графа 34 - benefitCodeReduction 7 19 TAX_BENEFIT_ID 6 16 NAME
 * графа 35 - benefitSumReduction
 * графа 36 - koefKp
 * графа 37 - taxSumToPay
 * графа 38 - benefitBase
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkRegionId()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkRegionId()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        checkRegionId()
        def sourcesInfo = preComposeCheck()
        if (logger.containsLevel(LogLevel.ERROR))
            return
        consolidation(sourcesInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        checkRegionId()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
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
def groups = ['A', 'B', 'C']

//Все аттрибуты
@Field
def allColumns = ['fix', 'rowNumber', 'taxAuthority', 'kpp', 'okato', 'tsTypeCode', 'tsType', 'model', 'ecoClass', 'vi',
                  'regNumber', 'regDate', 'regDateEnd', 'taxBase', 'taxBaseOkeiUnit', 'createYear', 'years',
                  'stealDateStart', 'stealDateEnd', 'periodStartCost', 'periodEndCost', 'ownMonths', 'partRight',
                  'coef362', 'taxRate', 'calculatedTaxSum', 'benefitMonths', 'benefitStartDate', 'benefitEndDate',
                  'coefKl', 'taxBenefitCode', 'benefitSum', 'taxBenefitCodeDecrease', 'benefitSumDecrease',
                  'benefitCodeReduction', 'benefitSumReduction', 'koefKp', 'taxSumToPay', 'benefitBase']

// Поля, для которых подсчитываются итоговые значения (19, 20, 25, 31, 33, 35, 37)
@Field
def totalColumns = ['periodStartCost', 'periodEndCost', 'calculatedTaxSum', 'benefitSum', 'benefitSumDecrease', 'benefitSumReduction', 'taxSumToPay']

@Field
def editableColumns = ['taxAuthority', 'kpp']

// Проверяемые на пустые значения атрибуты (графа 1..9, 11..15, 21)
@Field
def nonEmptyColumns = ['taxAuthority', 'kpp', 'okato', 'tsTypeCode', 'model', 'vi', 'regNumber', 'regDate',
                       'taxBase', 'taxBaseOkeiUnit', 'createYear', 'years', 'ownMonths', 'partRight', 'coef362',
                       'taxRate', 'calculatedTaxSum', 'taxSumToPay']

@Field
def monthCountInPeriod

@Field
def reportDay = null

@Field
def calendarStartDate = null

@Field
def startDate = null

@Field
def endDate = null

def getCalendarStartDate() {
    if (calendarStartDate == null) {
        calendarStartDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return calendarStartDate
}

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

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = false) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String columnName,
              def Date date, boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date,
            rowIndex, columnName, logger, required)
}

def getProvider(def id) {
    return formDataService.getRefBookProvider(refBookFactory, id, providerCache)
}

@Field
def currentReportPeriod = null

def getReportPeriod() {
    if (currentReportPeriod == null) {
        currentReportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return currentReportPeriod
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    if (reportDay == null) {
        reportDay = reportPeriodService.getReportDate(formData.reportPeriodId)?.time
    }
    return reportDay
}

/** Алгоритмы заполнения полей формы (9.1.1.8.1) Табл. 45. */
def calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def int monthCountInPeriod = getMonthCount()

    // Отчетная дата
    def reportDate = getReportDate()

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()

    /** Уменьшающий процент. */
    def reducingPerc
    /** Пониженная ставка. */
    def loweringRates

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()

        // получение региона по ОКТМО
        def region = getRegion(row.okato)

        // графа 2, 3
        fillTaKpp(row)

        // графа 21
        row.ownMonths = calc21(row, dFrom, dTo)

        // Графа 23 - Коэффициент Кв
        if (row.ownMonths != null) {
            row.coef362 = row.ownMonths.divide(new BigDecimal(monthCountInPeriod.toString()), 4, BigDecimal.ROUND_HALF_UP)
        }

        // Графа 24 (Налоговая ставка)
        calc24(row, region)

        def partRight = null
        if (row.partRight != null && row.partRight ==~ /\d{1,10}\/\d{1,10}/) {
            def partArray = row.partRight.split('/')
            if (!(partArray[1] ==~ /0{1,10}/)) { // в дальнейших проверках выведется
                partRight = (new BigDecimal(partArray[0])) / (new BigDecimal(partArray[1]))
            }
        }
        // Графа 25 (Сумма исчисления налога) = Расчет суммы исчисления налога
        if (row.taxBase != null && row.coef362 != null && row.taxRate != null && partRight != null) {
            def taxRate = getRefBookValue(41, row.taxRate)?.VALUE?.numberValue
            row.calculatedTaxSum = (row.taxBase * taxRate * partRight * row.coef362 * (row.koefKp ?: 1)).setScale(2, BigDecimal.ROUND_HALF_UP)
        }
        // Графа 26 Определяется количество полных месяцев использования льготы в отчетном году
        row.benefitMonths = calc26(row, dFrom, dTo)

        // Графа 29 Коэффициент Кл
        if (row.benefitMonths != null) {
            row.coefKl = ((BigDecimal)row.benefitMonths).divide( new BigDecimal(monthCountInPeriod.toString()), 4, BigDecimal.ROUND_HALF_UP)
        } else {
            row.coefKl = null
        }

        if (row.taxRate != null) {
            def taxRate = getRefBookValue(41, row.taxRate)?.VALUE?.numberValue

            // Графа 31
            if (row.taxBenefitCode != null) {
                if (row.taxBase != null && partRight != null) {
                    row.benefitSum = (row.taxBase * taxRate * partRight * (row.koefKp ?: 1) * (row.coefKl ?: 1)).setScale(2, BigDecimal.ROUND_HALF_UP)
                }
            } else {
                row.benefitSum = null
            }

            // Графа 33
            if (row.taxBenefitCodeDecrease != null) {
                reducingPerc = getRefBookValue(7, row.taxBenefitCodeDecrease)?.PERCENT?.numberValue
                if (reducingPerc != null && row.taxBase != null && partRight != null) {
                    row.benefitSumDecrease = (row.taxBase * taxRate * partRight * (row.koefKp ?: 1) * (row.coefKl ?: 1) * reducingPerc).setScale(2, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP)
                }
            } else {
                row.benefitSumDecrease = null
            }

            // Графа 35
            if (row.benefitCodeReduction != null) {
                loweringRates = getRefBookValue(7, row.benefitCodeReduction)?.RATE?.numberValue
                if (loweringRates != null && row.taxBase != null && partRight != null) {
                    row.benefitSumReduction = ((BigDecimal)(row.taxBase * (taxRate - loweringRates) * partRight * (row.koefKp ?: 1) * (row.coefKl ?: 1))).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP)
                }
            } else {
                row.benefitSumReduction = null
            }
        }

        def period = reportPeriodService.get(formData.reportPeriodId)

        // Графа 37 - Исчисленная сумма налога, подлежащая уплате в бюджет.
        def koef = (period.order == 4) ? 1 : 0.25
        if (row.taxBenefitCode != null) {
            if (row.calculatedTaxSum != null) {
                row.taxSumToPay = (koef * (row.calculatedTaxSum - (row.benefitSum ?: 0))).setScale(0, BigDecimal.ROUND_HALF_UP)
            }
        } else {
            if (row.calculatedTaxSum != null) {
                row.taxSumToPay = (koef * (row.calculatedTaxSum - (row.benefitSumDecrease ?: 0) - (row.benefitSumReduction ?: 0))).setScale(0, BigDecimal.ROUND_HALF_UP)
            }
        }
        /*
         * Графа 9 Единица измерения налоговой базы по ОКЕИ
         * Скрипт для выставления значения по умолчанию в столбец "Единица измерения налоговой базы по ОКЕИ",
         * если это значение не задано.
         */
        if (row.taxBaseOkeiUnit == null) {
            row.taxBaseOkeiUnit = getRecord(12, 'CODE', '251', index, getColumnName(row, 'taxBaseOkeiUnit'), reportDate)?.record_id?.numberValue
        }
    }

    groups.each { section ->
        def from = getDataRow(dataRows, section).getIndex()
        def to = getDataRow(dataRows, "total$section").getIndex() - 2
        calcTotalSum(dataRows[from..to], getDataRow(dataRows, "total$section"), totalColumns)
    }

    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(dataRows, totalRow, totalColumns)

    sortFormDataRows(false)
}

void fillTaKpp(def row) { // расчет и консолидация
    if (formDataDepartment.regionId == null || row.okato == null) {
        return
    }
    def region = getRegion(row.okato)
    def regionId = region?.record_id?.value
    def allRecords = getAllRecords(210L).values()
    def records = allRecords.findAll { record ->
        record.DECLARATION_REGION_ID.value == formDataDepartment.regionId &&
                record.REGION_ID.value == regionId &&
                record.OKTMO.value == row.okato
    }
    if (records.size() == 1) {
        row.taxAuthority = records[0].TAX_ORGAN_CODE?.value
        row.kpp = records[0].KPP?.value
    } else {
        boolean isMany = records.size() > 1
        LogLevel level = isMany ? LogLevel.WARNING : LogLevel.ERROR
        def okato = getRefBookValue(96, row.okato).CODE.value
        def msg1 = isMany ? "более одной записи, актуальной" : "отсутствует запись, актуальная"
        def endString = getReportPeriodEndDate().format('dd.MM.yyyy')
        def declarationRegionCode = getRefBookValue(4, formDataDepartment.regionId).CODE.value
        def regionCode = region.CODE.value
        logger.log(level, "Строка %s. Графа «%s» = «%s»: В справочнике «Параметры представления деклараций по транспортному налогу» %s на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s»",
                row.getIndex(), getColumnName(row, 'okato'), okato, msg1, endString, declarationRegionCode, formDataDepartment.name, regionCode, okato)
    }
}

def checkTaKpp(def row, def region, def rowIndex) {
    if (formDataDepartment.regionId == null || row.okato == null || row.taxAuthority == null || row.kpp == null) {
        return
    }
    def allRecords = getAllRecords(210L).values()
    def regionId = region?.record_id?.value
    def records = allRecords.findAll { record ->
        record.DECLARATION_REGION_ID.value == formDataDepartment.regionId &&
                record.REGION_ID.value == regionId &&
                record.OKTMO.value == row.okato &&
                record.TAX_ORGAN_CODE.value?.toLowerCase() == row.taxAuthority?.toLowerCase() &&
                record.KPP.value?.toLowerCase() == row.kpp?.toLowerCase()
    }
    if (records.size() < 1) {
        def okato = getRefBookValue(96, row.okato).CODE.value
        def declarationRegionCode = getRefBookValue(4, formDataDepartment.regionId).CODE.value
        def regionCode = region.CODE.value
        logger.error("Строка %s. Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: В справочнике «Параметры представления деклараций по транспортному налогу» " +
                "отсутствует запись, актуальная на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                "справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s», поле «Код налогового органа (кон.)» = «%s» поле «КПП» = «%s»",
                rowIndex, getColumnName(row, 'okato'), okato, getColumnName(row, 'taxAuthority'), row.taxAuthority, getColumnName(row, 'kpp'), row.kpp,
                getReportPeriodEndDate().format('dd.MM.yyyy'), declarationRegionCode,
                formDataDepartment.name, regionCode, okato, row.taxAuthority, row.kpp
        )
    }
}

@Field
def uniqueColumns = ['okato', 'tsTypeCode', 'vi', 'taxBase', 'taxBaseOkeiUnit']

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def sectionTsTypeCodeMap = ['A': '50000', 'B': '40200', 'C': '40100']
    def sectionTsTypeCode = null

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()
    def reportPeriod = getReportPeriod()

    def rowsMap = [:]

    def isCalc = formDataEvent == FormDataEvent.CALCULATE || formDataEvent == FormDataEvent.COMPOSE

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            sectionTsTypeCode = sectionTsTypeCodeMap[row.getAlias()]
            continue
        }

        def index = row.getIndex()

        // 1. Проверка на заполнение обязательных граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка наличия параметров представления декларации для кода ОКТМО, заданного по графе 4 «Код ОКТМО»
        // в расчете

        // получение региона по ОКТМО
        def region = getRegion(row.okato)

        // 3. Проверка на корректность заполнения кода НО и КПП согласно справочнику параметров представления деклараций
        if (!isCalc) {
            checkTaKpp(row, region, index)
        }

        // 4. Проверка на наличие в форме строк с одинаковым значением граф 4, 5, 9, 13, 14
        def uniqueKey = getKey(row, uniqueColumns)
        if (rowsMap[uniqueKey] == null) {
            rowsMap[uniqueKey] = []
        }
        rowsMap[uniqueKey].add(row)

        // 5. Проверка кода вида ТС по разделу «Наземные транспортные средства»
        // 6. Проверка кода вида ТС по разделу «Водные транспортные средства»
        // 7. Проверка кода вида ТС по разделу «Воздушные транспортные средства»
        if (row.tsTypeCode != null) {
            // проверить выбрана ли верхушка деревьев видов ТС
            def tsTypeCode = getRefBookValue(42L, row.tsTypeCode)?.CODE?.value
            def hasError = tsTypeCode in sectionTsTypeCodeMap.values()
            if (!hasError) {
                // проверить корень дерева видов ТС на соответствие
                def parentTypeCode = getParentTsTypeCode(row.tsTypeCode)
                if (parentTypeCode != null) {
                    hasError = sectionTsTypeCode != parentTypeCode
                }
            }
            if (hasError) {
                logger.error("Строка %s: Значение графы «%s» должно относиться к виду ТС «%s»",
                        index, getColumnName(row, 'tsTypeCode'), getSectionTsTypeName(sectionTsTypeCode))
            }
        }

        // 8. Проверка корректности заполнения даты регистрации ТС
        if (row.regDate != null && row.regDate > dTo) {
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно %s",
                    index, getColumnName(row, 'regDate'), dTo.format('dd.MM.yyyy'))
        }

        // 9. Проверка корректности заполнения даты снятия с регистрации ТС
        if (row.regDateEnd != null && (row.regDateEnd < dFrom || (row.regDate != null && row.regDateEnd < row.regDate))) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значения графы «%s»",
                    index, getColumnName(row, 'regDate'), dFrom.format('dd.MM.yyyy'), getColumnName(row, 'regDate'))
        }

        // 10. Проверка года изготовления ТС
        if (row.createYear != null) {
            Calendar calendarMake = Calendar.getInstance()
            calendarMake.setTime(row.createYear)
            if (calendarMake.get(Calendar.YEAR) > reportPeriod.taxPeriod.year) {
                logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно «%s»",
                        index, getColumnName(row, 'createYear'), reportPeriod.taxPeriod.year)
            }
        }

        // 11. Проверка на наличие даты начала розыска ТС при указании даты возврата ТС
        if (row.stealDateEnd != null && row.stealDateStart == null) {
            logger.error("Строка %s: Графа «%s» должна быть заполнена, если заполнена графа «%s»",
                    index, getColumnName(row, 'stealDateStart'), getColumnName(row, 'stealDateEnd'))
        }

        // 12. Проверка на соответствие дат сведений об угоне
        if (row.stealDateStart != null && row.stealDateEnd != null && row.stealDateEnd < row.stealDateStart) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значения графы «%s»",
                    index, getColumnName(row, 'stealDateEnd'), getColumnName(row, 'stealDateStart'))
        }

        if (row.partRight != null) {
            def parts = row.partRight.split('/')

            // 13. Проверка доли налогоплательщика в праве на ТС (графа 18) на корректность формата введенных данных
            def isOnlyDigits = row.partRight ==~ /\d{1,10}\/\d{1,10}/
            def hasFirstZero = parts.find { it ==~ /0+\d*/ }
            // если числитель больше знаменателя
            def divisorGreaterDenominator = (parts.size() == 2 && (parts[0].size() > parts[1].size() || (parts[0].size() == parts[1].size() && parts[0] > parts[1])))
            if (!isOnlyDigits || hasFirstZero || divisorGreaterDenominator) {
                logger.error("Строка %s: Графа «%s» должна быть заполнена согласно формату: «(от 1 до 10 числовых знаков)/(от 1 до 10 числовых знаков)», числитель должен быть меньше либо равен знаменателю",
                        index, getColumnName(row, 'partRight'))
            }

            // 14. Проверка значения знаменателя доли налогоплательщика в праве на ТС (графа 18)
            if (parts.size() == 2 && parts[1] ==~ /0{1,10}/) {
                logger.error("Строка %s: Значение знаменателя в графе «%s» не может быть равным нулю", index, getColumnName(row, 'partRight'))
            }
        }

        // 15. Проверка значения поля «Коэффициент, определяемый в соответствии с п.3 ст.362 НК РФ»
        if (row.coef362 != null && (row.coef362 < 0.0 || row.coef362 > 1.0)) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно нуля и меньше либо равно единицы",
                    index, getColumnName(row, 'coef362'))
        }

        // 16. Проверка наличия ставки ТС в справочнике
        // в расчете

        // 17. Проверка корректности заполнения даты начала использования льготы
        if (row.benefitStartDate != null && row.benefitStartDate > dTo) {
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно %s",
                    index, getColumnName(row, 'benefitStartDate'), dTo.format('dd.MM.yyyy'))
        }

        // 18. Проверка корректности заполнения даты окончания использования льготы
        if (row.benefitEndDate != null && ((row.benefitEndDate < dFrom) || (row.benefitStartDate != null && row.benefitEndDate < row.benefitStartDate))) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значения графы «%s»",
                    index, getColumnName(row, 'benefitEndDate'), dFrom.format('dd.MM.yyyy'), getColumnName(row, 'benefitStartDate'))
        }

        // 19. Проверка значения поля «Коэффициент, определяемый в соответствии с законами субъектов РФ»
        if (row.coefKl != null && (row.coefKl < 0.0 || row.coefKl > 1.0)) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно нуля и меньше либо равно единицы",
                    index, getColumnName(row, 'coefKl'))
        }

        // ------------------------------------------------------------------------------------------------------------------------------
        // Следующие проверки не проводятся при расчёте
        if (isCalc) {
            continue
        }

        // 20. Проверка одновременного заполнения данных о налоговой льготе
        boolean benefitCondition = row.benefitMonths == null || row.benefitStartDate == null || row.coefKl == null
        if (((row.taxBenefitCode != null) && (benefitCondition || row.benefitSum == null)) ||
                ((row.taxBenefitCodeDecrease != null) && (benefitCondition || row.benefitSumDecrease == null)) ||
                ((row.benefitCodeReduction != null) && (benefitCondition || row.benefitSumReduction == null))
        ){
            logger.error("Строка %s: Данные о налоговой льготе указаны не полностью.", index)
        }

        // 21. Проверка, что исчисленная сумма налога больше или равна сумме налоговой льготы
        if ((row.benefitSum != null && row.calculatedTaxSum < row.benefitSum) ||
                (row.benefitSumDecrease != null && row.calculatedTaxSum < row.benefitSumDecrease) ||
                (row.benefitSumReduction != null && row.calculatedTaxSum < row.benefitSumReduction)
        ) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значения графы с суммой налоговой льготы",
                    index, getColumnName(row, 'calculatedTaxSum'))
        }
    }
    // 4. Проверка на наличие в форме строк с одинаковым значением граф 4, 5, 9, 13, 14
    rowsMap.findAll { key, rows ->
        rows.size() > 1
    }.each { key, rows ->
        def rowIndexes = rows.collect { it.getIndex() }
        def row = rows[0]
        def okato = getRefBookValue(96, row.okato).CODE.value
        def tsTypeCode = getRefBookValue(42L, row.tsTypeCode).CODE.value
        def taxBaseOkeiUnit = getRefBookValue(12, row.taxBaseOkeiUnit).CODE.value
        logger.error("Строки %s. Код ОКТМО «%s», Код вида ТС «%s», Идентификационный номер ТС «%s», Налоговая база «%s», Единица измерения налоговой базы по ОКЕИ «%s»: на форме не должно быть строк с одинаковым кодом ОКТМО, кодом вида ТС, идентификационным номером ТС, налоговой базой и единицей измерения налоговой базы по ОКЕИ",
                rowIndexes.join(', '), okato, tsTypeCode, row.vi, row.taxBase, taxBaseOkeiUnit)
    }
}

/**
 * Получение региона по коду ОКТМО
 */
def getRegion(def record96Id) {
    def record96 = getRefBookValue(96, record96Id)
    def okato = getOkato(record96?.CODE?.stringValue)
    if (okato) {
        def allRecords = getAllRecords(4L).values()
        return allRecords.find { record ->
            record.OKTMO_DEFINITION.value == okato
        }
    }
    return null
}

/**
 * Получить "Код вида ТС" родителя.
 *
 * @param tsTypeCode id на справочник 42 «Коды видов транспортных средств»
 */
def getParentTsTypeCode(def tsTypeCode) {
    // справочник 42 «Коды видов транспортных средств»
    def ids = getProvider(42L).getParentsHierarchy(tsTypeCode)
    if (ids != null && !ids.isEmpty()) {
        return getRefBookValue(42L, ids.get(0))?.CODE?.value
    }
    return null
}

@Field
def tsCodeNamesMap = [:]

def getSectionTsTypeName(def sectionTsTypeCode) {
    if (tsCodeNamesMap[sectionTsTypeCode] == null) {
        def record = getRecord(42, 'CODE', sectionTsTypeCode, -1, null, getReportPeriodEndDate(), true)
        tsCodeNamesMap[sectionTsTypeCode] = record?.NAME?.value ?: ""
    }
    return tsCodeNamesMap[sectionTsTypeCode]
}

@Field
def groupColumnsListList = [['version', 'pastYear'], ['codeOKATO'], ['codeOKATO', 'tsTypeCode', 'baseUnit']]

def preComposeCheck() {
    def sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)
    def complexMap = [:]
    for (relation in sourcesInfo) {
        if (relation.formType.id != formTypeVehicleId) {
            continue
        }
        def sourceFormData = formDataService.get(relation.formDataId, null)
        def dataRows = formDataService.getDataRowHelper(sourceFormData).allSaved
        groupColumnsListList.each { groupColumns ->
            def columnsKey = groupColumns.toString()
            if (complexMap[columnsKey] == null) {
                complexMap[columnsKey] = [:]
            }
            if (complexMap[columnsKey][relation] == null) {
                complexMap[columnsKey][relation] = [:]
            }
            def rowsMap = complexMap[columnsKey][relation]
            for (row in dataRows) {
                if (row.getAlias() != null) {
                    continue
                }
                def key = getKey(row, groupColumns)
                if (rowsMap[key] == null) {
                    rowsMap[key] = []
                }
                rowsMap[key].add(row)
            }
        }
    }
    int i = 0
    // 1. Проверка наличия повышающего коэффициента для ТС с заполненной графой 25 «Модель (версия) из перечня, утвержденного на налоговый период» в форме-источнике
    def groupColumns = groupColumnsListList[i]
    def columnsKey = groupColumns.toString()
    def sourceRowsMap = complexMap[columnsKey]
    sourceRowsMap.each { Relation relation, rowsMap ->
        rowsMap.each { key, dataRows ->
            def row = dataRows[0]
            if (row.version != null) { // скопировал из первички
                def avgCostId = getRefBookValue(218L, row.version)?.AVG_COST?.value
                // справочник «Повышающие коэффициенты транспортного налога»
                def allRecords = getAllRecords(209L).values()
                def records = allRecords.findAll { record ->
                    record.AVG_COST.value == avgCostId &&
                            record.YEAR_FROM.value <= row.pastYear &&
                            record.YEAR_TO.value >= row.pastYear
                }
                if (records == null || records.isEmpty()) {
                    def rowIndexes = dataRows.collect { it.getIndex() }
                    def avgCost = getRefBookValue(211L, avgCostId).NAME.value
                    logger.warn("Строки %s формы-источника. Графа «%s» = «%s», графа «%s» = «%s». В справочнике «Повышающие коэффициенты транспортного налога» отсутствует запись, " +
                            "актуальная на дату %s, в которой поле «Средняя стоимость» = «%s» и значение «%s» больше значения поля «Количество лет, прошедших с года выпуска ТС (от)» и меньше или равно значения поля «Количество лет, прошедших с года выпуска ТС (до)». " +
                            "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                            rowIndexes.join(', '), getColumnName(row, 'pastYear'), row.pastYear, getColumnName(row, 'averageCost'), avgCost,
                            getReportPeriodEndDate().format("dd.MM.yyyy"), avgCost, getColumnName(row, 'pastYear'),
                            relation.formDataKind.title, relation.formTypeName, relation.department.name, relation.periodName, relation.year
                    )
                }
            }
        }
    }
    i = 1
    // 2. Проверка наличия параметров представления декларации для кода ОКТМО, заданного по графе 2 «Код ОКТМО» формы-источника
    groupColumns = groupColumnsListList[i]
    columnsKey = groupColumns.toString()
    sourceRowsMap = complexMap[columnsKey]
    sourceRowsMap.each { Relation relation, rowsMap ->
        rowsMap.each { key, dataRows ->
            def row = dataRows[0]
            if (row.codeOKATO != null) {
                def region = getRegion(row.codeOKATO)
                def regionId = region?.record_id?.value
                def declarationRegionId = relation.getDepartment().regionId
                // справочник «Параметры представления деклараций по транспортному налогу»
                def allRecords = getAllRecords(210L).values()
                def records = allRecords.findAll { record ->
                    record.DECLARATION_REGION_ID.value == declarationRegionId &&
                            record.REGION_ID.value == regionId &&
                            record.OKTMO.value == row.codeOKATO
                }
                if (records == null || records.isEmpty()) {
                    def rowIndexes = dataRows.collect { it.getIndex() }
                    def declarationRegionCode = getRefBookValue(4, declarationRegionId).CODE.value
                    def regionCode = region.CODE.value
                    def codeOKATO = getRefBookValue(96L, row.codeOKATO).CODE.value
                    logger.error("Строки %s формы-источника. Графа «%s» = «%s»: В справочнике «Параметры представления деклараций по транспортному налогу» отсутствует запись, " +
                            "актуальная на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) справочника «Подразделения» " +
                            "для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s». " +
                            "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                            rowIndexes.join(', '), getColumnName(row, 'codeOKATO'), codeOKATO,
                            getReportPeriodEndDate().format('dd.MM.yyyy'), declarationRegionCode,
                            formDataDepartment.name, regionCode, codeOKATO,
                            relation.formDataKind.title, relation.formTypeName, relation.department.name, relation.periodName, relation.year
                    )
                }
            }
        }
    }
    i = 2
    // 3. Проверка наличия ставки для ТС формы-источника
    groupColumns = groupColumnsListList[i]
    columnsKey = groupColumns.toString()
    sourceRowsMap = complexMap[columnsKey]
    sourceRowsMap.each { Relation relation, rowsMap ->
        rowsMap.each { key, dataRows ->
            def row = dataRows[0]
            if (row.codeOKATO != null && row.tsTypeCode != null && row.taxBase != null && row.baseUnit != null && row.pastYear != null) {
                def region = getRegion(row.codeOKATO)
                def regionId = region?.record_id?.value
                def declarationRegionId = relation.getDepartment().regionId
                // справочник «Ставки транспортного налога»
                def allRecords = getAllRecords(41L).values()
                def records = allRecords.findAll { record ->
                    record.DECLARATION_REGION_ID.value == declarationRegionId &&
                            record.DICT_REGION_ID.value == regionId &&
                            record.CODE.value == row.tsTypeCode &&
                            record.UNIT_OF_POWER.value == row.baseUnit &&
                            ((record.MIN_AGE.value == null) || (record.MIN_AGE.value < row.pastYear)) &&
                            ((record.MAX_AGE.value == null) || (record.MAX_AGE.value >= row.pastYear)) &&
                            ((record.MIN_POWER.value == null) || (record.MIN_POWER.value < row.taxBase)) &&
                            ((record.MAX_POWER.value == null) || (record.MAX_POWER.value >= row.taxBase))
                }
                if (records == null || records.size() != 1) {
                    def rowIndexes = dataRows.collect { it.getIndex() }
                    boolean isMany = records != null && records.size() > 1
                    def declarationRegionCode = getRefBookValue(4, declarationRegionId).CODE.value
                    def regionCode = region.CODE.value
                    def tsTypeCode = getRefBookValue(42L, row.tsTypeCode).CODE.value
                    def baseUnit = getRefBookValue(12L, row.baseUnit).CODE.value
                    def codeOKATO = getRefBookValue(96L, row.codeOKATO).CODE.value
                    def msg1 = isMany ? "более одной записи, актуальной" : "отсутствует запись, актуальная"
                    logger.error("Строки %s формы-источника. Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: В справочнике «Ставки транспортного налога» " +
                            "%s на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                            "справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код ТС» = «%s», поле «Ед. измерения мощности» = «%s». " +
                            "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                            rowIndexes.join(', '), getColumnName(row, 'codeOKATO'), codeOKATO, getColumnName(row, 'tsTypeCode'), tsTypeCode, getColumnName(row, 'baseUnit'), baseUnit,
                            msg1, getReportPeriodEndDate().format('dd.MM.yyyy'), declarationRegionCode,
                            relation.getDepartment().name, regionCode, tsTypeCode, baseUnit,
                            relation.formDataKind.title, relation.formTypeName, relation.department.name, relation.periodName, relation.year
                    )
                }

            }
        }
    }

    return sourcesInfo
}

// Ключ для строк формы-источника, по графам columns
String getKey(def row, def columns) {
    return columns.collect { row[it] }.join('#')
}

@Field
def formTypeVehicleId = 201

/**
 * Консолидация формы
 * Собирает данные с консолидированных нф
 */
def consolidation(def sourcesInfo) {
    // очистить форму
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    dataRows.removeAll { it.getAlias() == null }
    Map<String, List> dataRowsMap = ['A': [], 'B': [], 'C': []]

    sourcesInfo.each { relation ->
        def source = formDataService.get(relation.formDataId, null)
        def sourceDataRows = formDataService.getDataRowHelper(source).allSaved
        if (source.formType.id == formTypeVehicleId) {
            def alias = null
            for (sRow in sourceDataRows) {
                if (sRow.getAlias() != null) {
                    alias = sRow.getAlias()
                    continue
                }
                if (alias != null && alias in groups) {
                    def newRow = formNewRow(sRow)
                    dataRowsMap[alias].add(newRow)
                }
            }
        } else if (source.getFormType().getId() == formData.getFormType().getId()) {
            def alias = null
            for (row in sourceDataRows) {
                if (row.getAlias() != null) {
                    alias = row.getAlias()
                    continue
                }
                if (alias != null && alias in groups) {
                    dataRowsMap[alias].add(row)
                }
            }
        }
    }

    // копирование данных по разделам
    def int insertIndex = 1
    groups.each { section ->
        def copyRows = dataRowsMap[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            dataRows.addAll(insertIndex, copyRows)
            insertIndex += copyRows.size()
        }
        insertIndex += 2
    }

    updateIndexes(dataRows)
}

def formNewRow(def sRow) {
// новая строка
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias("Редактируемое поле")
    }
    // «Графа 4» принимает значение «графы 2» формы-источника
    newRow.okato = sRow.codeOKATO
    // «Графа 5» принимает значение «графы 4» формы-источника
    newRow.tsTypeCode = sRow.tsTypeCode
    // «Графа 6» принимает значение «графы 5» формы-источника
    // зависимая графа newRow.tsType = sRow.tsType
    // «Графа 7» принимает значение «графы 6» формы-источника
    newRow.model = sRow.model
    // «Графа 8» принимает значение «графы 7» формы-источника
    newRow.ecoClass = sRow.ecoClass
    // «Графа 9» принимает значение «графы 8» формы-источника
    newRow.vi = sRow.identNumber
    // «Графа 10» принимает значение «графы 9» формы-источника
    newRow.regNumber = sRow.regNumber
    // «Графа 11» принимает значение «графы 10» формы-источника
    newRow.regDate = sRow.regDate
    // «Графа 12» принимает значение «графы 11» формы-источника
    newRow.regDateEnd = sRow.regDateEnd
    // «Графа 13» принимает значение «графы 12» формы-источника
    newRow.taxBase = sRow.taxBase
    // «Графа 14» принимает значение «графы 13» формы-источника
    newRow.taxBaseOkeiUnit = sRow.baseUnit
    // «Графа 15» принимает значение «графы 14» формы-источника
    newRow.createYear = sRow.year
    // «Графа 16» принимает значение «графы 15» формы-источника
    newRow.years = sRow.pastYear
    // «Графа 17» принимает значение «графы 16» формы-источника
    newRow.stealDateStart = sRow.stealDateStart
    // «Графа 18» принимает значение «графы 17» формы-источника
    newRow.stealDateEnd = sRow.stealDateEnd
    // «Графа 19» принимает значение «графы 19» формы-источника
    newRow.periodStartCost = sRow.costOnPeriodBegin
    // «Графа 20» принимает значение «графы 20» формы-источника
    newRow.periodEndCost = sRow.costOnPeriodEnd
    // «Графа 22» принимает значение «графы 18» формы-источника
    newRow.partRight = sRow.share
    // «Графа 27» принимает значение «графы 21» формы-источника
    newRow.benefitStartDate = sRow.benefitStartDate
    // «Графа 28» принимает значение «графы 22» формы-источника
    newRow.benefitEndDate = sRow.benefitEndDate
    // «Графа 38» принимает значение «графы 24» формы-источника
    newRow.benefitBase = sRow.base
    // «Графы 30, 32, 34»
    // Если «Графа 23» формы-источника имеет пустое значение, то «Графа 30», «Графа 32» и «Графа 34» не заполняются.
    def taxBenefitCode = sRow.taxBenefitCode
    if (taxBenefitCode != null) {
        def benefitCode = getBenefitCode(taxBenefitCode)
        switch (benefitCode) {
        // Иначе если « Графа 23 » формы - источника
        // имеет значение « 20220 », то « Графа 32 » принимает значение « Графы 23 » формы - источника, « Графа 30 » и « Графа 34 » не заполняются.
            case '20220': newRow.taxBenefitCodeDecrease = taxBenefitCode
                break
        // Иначе если « Графа 23 » формы - источника
        // имеет значение « 20230 », то « Графа 34 » принимает значение « Графы 23 » формы - источника, « Графа 30 » и « Графа 32 » не заполняются.
            case '20230': newRow.benefitCodeReduction = taxBenefitCode
                break
        // Иначе « Графа 30 » принимает значение « Графы 23 » формы - источника, « Графа 32 » и « Графа 34 » не заполняются.
            default: newRow.taxBenefitCode = taxBenefitCode
        }
    }
    // «Графа 36»
    // Если «Графа 25» формы-источника имеет пустое значение, то «Графа 36» не заполняется
    if (sRow.version != null) {
        // "Средняя стоимость транспортных средст"
        def avgPriceRecord = getRefBookValue(218, sRow.version)
        // В справочнике «Повышающие коэффициенты транспортного налога» найти записи
        def allRecords = getAllRecords(209L).values()
        def records = allRecords.findAll { record ->
            record.AVG_COST.value == avgPriceRecord.AVG_COST.value &&
                    record.YEAR_FROM.value <= sRow.pastYear &&
                    record.YEAR_TO.value >= sRow.pastYear
        }
        if (records.size() == 1) {
            newRow.koefKp = records[0].COEF.value
        } else {
            newRow.koefKp = null
        }
    } else {
        newRow.koefKp = null
    }
    return newRow
}

// Расчет графы 21
int calc21(DataRow sRow, Date reportPeriodStartDate, Date reportPeriodEndDate) {
    // Дугона – дата угона
    Date stealingDate = Calendar.getInstance().time
    // Двозврата – дата возврата
    Date returnDate = Calendar.getInstance().time
    // Дпостановки – дата постановки ТС на учет
    Date deliveryDate = Calendar.getInstance().time
    // Дснятия – дата снятия ТС с учета
    Date removalDate = Calendar.getInstance().time
    // владенеи в месяцах
    int ownMonths
    // Срока нахождения в угоне (Мугон)
    int stealingMonths

    if (sRow.regDate == null) {
        return 0
    }

    /*
     * Если  [«графа 14»(источника) заполнена И «графа 14»(источника)< «Дата начала периода»]
     * ИЛИ [«графа 13»>«Дата окончания периода»], то
     * Графа 12=0
     */
    if ((sRow.regDateEnd != null && sRow.regDateEnd.compareTo(reportPeriodStartDate) < 0)
            || sRow.regDate.compareTo(reportPeriodEndDate) > 0) {
        return 0
    } else { // иначе
        //Определяем Мугон
        //Если «графа 15» (источника) не заполнена, то Мугон = 0
        if (sRow.stealDateStart == null) {
            stealingMonths = 0
        } else { // инчае
            /**
             * Если «графа 15»(источника)< «Дата начала периода», то
             *  Дугона = «Дата начала периода»
             *  Иначе
             *  Дугона = «графа 15»(источника)
             */
            if (sRow.stealDateStart.compareTo(reportPeriodStartDate) < 0) {
                stealingDate = reportPeriodStartDate
            } else {
                stealingDate = sRow.stealDateStart
            }

            /**
             * Определяем Двозврат
             * Если [«графа 16»(источника) не заполнена] ИЛИ [«графа 16»(источника)> «Дата окончания периода»], то
             *  Двозврата = «Дата окончания периода»
             * Иначе
             * Двозврата = «графа 16»(источника)
             *
             */
            if (sRow.stealDateEnd == null || sRow.stealDateEnd.compareTo(reportPeriodEndDate) > 0) {
                returnDate = reportPeriodEndDate
            } else {
                returnDate = sRow.stealDateEnd
            }

            /**
             * Определяем Мугон
             * Если (МЕСЯЦ(Двозврата)-МЕСЯЦ(Дугона)-1)<0, то
             *  Мугон = 0
             * Иначе
             *  Мугон = МЕСЯЦ(Двозврата)-МЕСЯЦ(Дугона)-1
             */
            def diff1 = (returnDate[Calendar.YEAR] * 12 + returnDate[Calendar.MONTH]) - (stealingDate[Calendar.YEAR] * 12 + stealingDate[Calendar.MONTH]) - 1
            if (diff1 < 0) {
                stealingMonths = 0
            } else {
                stealingMonths = diff1
            }
        }

        /**
         * Определяем Дснятия
         * Если «графа 14»(источника) не заполнена ИЛИ «графа 14»(источника)> «Дата окончания периода», то
         *  Дснятия = «Дата окончания периода»
         * Иначе
         *  Дснятия = «графа 14»(источника)
         */
        if (sRow.regDateEnd == null || sRow.regDateEnd.compareTo(reportPeriodEndDate) > 0) {
            removalDate = reportPeriodEndDate
        } else {
            removalDate = sRow.regDateEnd
        }

        /**
         * Определяем Дпостановки
         * Если «графа 13»(источника)< «Дата начала периода», то
         *  Дпостановки = «Дата начала периода»
         * Иначе
         *  Дпостановки = «графа 13»(источника)
         */
        if (sRow.regDate != null && sRow.regDate.compareTo(reportPeriodStartDate) < 0) {
            deliveryDate = reportPeriodStartDate
        } else {
            deliveryDate = sRow.regDate
        }

        /**
         * Определяем Мвлад
         * Мвлад = МЕСЯЦ[Дснятия] - МЕСЯЦ[Дпостановки]+1
         */
        ownMonths = (removalDate[Calendar.YEAR] * 12 + removalDate[Calendar.MONTH]) - (deliveryDate[Calendar.YEAR] * 12 + deliveryDate[Calendar.MONTH]) + 1
        /**
         * Определяем графу 12
         * Графа 12=Мвлад-Мугон
         */
        return ownMonths - stealingMonths
    }
}

def calc26(def row, def reportPeriodStartDate, def reportPeriodEndDate) {
    if (row.benefitStartDate == null && row.benefitEndDate == null) {
        return null
    } else {
        if (row.benefitEndDate != null && row.benefitEndDate.compareTo(reportPeriodStartDate) < 0 ||
                row.benefitStartDate.compareTo(reportPeriodEndDate) > 0) {
            return 0
        } else {
            //Определяем Доконч
            def dOkonch
            if (row.benefitEndDate == null || row.benefitEndDate.compareTo(reportPeriodEndDate) > 0) {
                dOkonch = reportPeriodEndDate
            } else {
                dOkonch = row.benefitEndDate
            }
            // Определяем Днач
            def dNach = (row.benefitStartDate.compareTo(reportPeriodStartDate) < 0) ? reportPeriodStartDate : row.benefitStartDate
            // Определяем Мльгот
            return dOkonch[Calendar.MONTH] - dNach[Calendar.MONTH] + 1
        }
    }

}

/** Число полных месяцев в текущем периоде (либо отчетном либо налоговом). */
def getMonthCount() {
    if (monthCountInPeriod == null) {
        def period = reportPeriodService.get(formData.reportPeriodId)
        if (period == null) {
            logger.error('Не найден отчетный период для налоговой формы.')
        } else {
            // 1. Отчетные периоды:
            //  a.	первый квартал (с января по март включительно) - 3 мес.
            //  b.	второй квартал (с апреля по июнь включительно) - 3 мес.
            //  c.	третий квартал (с июля по сентябрь включительно) - 3 мес.
            // 2. Налоговый период
            //  a.	год (с января по декабрь включительно) - 12 мес.
            monthCountInPeriod = period.order < 4 ? 3 : 12
        }
    }
    return monthCountInPeriod
}

/**
 * Графа 24 (Налоговая ставка)
 * Скрипт для вычисления налоговой ставки
 */
void calc24(def row, def region) {
    if (row.tsTypeCode != null && row.years != null && row.taxBase != null) {
        def regionId = region?.record_id?.value
        def declarationRegionId = formDataDepartment.regionId
        // справочник «Ставки транспортного налога»
        def allRecords = getAllRecords(41L).values()
        def records = allRecords.findAll { record ->
            record.DECLARATION_REGION_ID.value == declarationRegionId &&
                    record.DICT_REGION_ID.value == regionId &&
                    record.CODE.value == row.tsTypeCode &&
                    record.UNIT_OF_POWER.value == row.taxBaseOkeiUnit &&
                    ((record.MIN_AGE.value == null) || (record.MIN_AGE.value < row.years)) &&
                    ((record.MAX_AGE.value == null) || (record.MAX_AGE.value >= row.years)) &&
                    ((record.MIN_POWER.value == null) || (record.MIN_POWER.value < row.taxBase)) &&
                    ((record.MAX_POWER.value == null) || (record.MAX_POWER.value >= row.taxBase))
        }

        if (records != null && records.size() == 1) {
            row.taxRate = records[0].record_id.value
        } else if (formDataEvent == FormDataEvent.CALCULATE) { // выводим только при расчете
            boolean isMany = records != null && records.size() > 1
            def declarationRegionCode = getRefBookValue(4, declarationRegionId).CODE.value
            def regionCode = region.CODE.value
            def tsTypeCode = getRefBookValue(42L, row.tsTypeCode).CODE.value
            def taxBaseOkeiUnit = getRefBookValue(12L, row.taxBaseOkeiUnit).CODE.value
            def okato = getRefBookValue(96L, row.okato).CODE.value
            def msg1 = isMany ? "более одной записи, актуальной" : "отсутствует запись, актуальная"
            logger.error("Строка %s. Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: В справочнике «Ставки транспортного налога» " +
                    "%s, на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                    "справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код ТС» = «%s», поле «Ед. измерения мощности» = «%s». ",
                    row.getIndex(), getColumnName(row, 'okato'), okato, getColumnName(row, 'tsTypeCode'), tsTypeCode, getColumnName(row, 'taxBaseOkeiUnit'), taxBaseOkeiUnit,
                    msg1, getReportPeriodEndDate().format('dd.MM.yyyy'), declarationRegionCode,
                    formDataDepartment.name, regionCode, tsTypeCode, taxBaseOkeiUnit
            )
        }
    }
}

// Получение подитоговых строк
def getTotalRow(def dataRows) {
    return dataRows.find { it.getAlias() != null && it.getAlias().equals('total') }
}

def getBenefitCode(def parentRecordId) {
    def recordId = getRefBookValue(7, parentRecordId).TAX_BENEFIT_ID.value
    return getRefBookValue(6, recordId).CODE.value
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    groups.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, "total$section")
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1
        def sectionsRows = (from < to ? dataRows.subList(from, to) : [])

        // Массовое разыменование строк НФ
        def columnList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, sectionsRows, columnList)

        sortRowsSimple(sectionsRows)
    }
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

// Проверка заполнения атрибута «Регион» подразделения текущей формы (справочник «Подразделения»)
void checkRegionId() {
    if (formDataDepartment.regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }
}

void addNewRow() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1 || currentDataRow.getAlias() == 'total') {
        index = getDataRow(dataRows, 'totalC').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
    } else {
        def alias = currentDataRow.getAlias()
        if (alias.contains('total')) {
            index = getDataRow(dataRows, alias).getIndex()
        } else {
            index = getDataRow(dataRows, 'total' + alias).getIndex()
        }
    }
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias("Редактируемое поле")
    }
    dataRows.add(index - 1, newRow)
    formDataService.saveCachedDataRows(formData, logger)
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 39
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('rowNumber').column.name
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    def templateRows = formTemplate.rows
    def titleMap = [:]
    groups.each {
        def title = getDataRow(templateRows, it).fix
        titleMap[title] = it
    }

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def allValuesCount = allValues.size()
    def section = null
    def mapRows = [:]
    def totalRowFromFileMap = [:]
    def totalAll = null

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
        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        def firstValue = rowValues[INDEX_FOR_SKIP]
        if (firstValue != null && firstValue != '' && firstValue != 'Итого' && firstValue != 'Общий итог') {
            section = titleMap[firstValue]
            if (section == null) {
                logger.error("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
            }
            mapRows.put(section, [])
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (firstValue in ['Итого', 'Общий итог']) {
            rowIndex++
            def total = getTotalFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            if (firstValue == 'Общий итог') {
                totalAll = total
            } else {
                totalRowFromFileMap[section] = total
            }
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        mapRows[section].add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // копирование данных по разделам
    def rows = []
    groups.each { group ->
        def headRow = getDataRow(templateRows, group)
        def totalRow = getDataRow(templateRows, 'total' + group)
        rows.add(headRow)
        def copyRows = mapRows[group]
        if (copyRows != null && !copyRows.isEmpty()) {
            rows.addAll(copyRows)
        }
        rows.add(totalRow)

        // сравнение итогов
        updateIndexes(rows)
        def totalRowFromFile = totalRowFromFileMap[group]
        compareSimpleTotalValues(totalRow, totalRowFromFile, copyRows, totalColumns, formData, logger, false)
    }
    def totalRow = getDataRow(templateRows, 'total')
    rows.add(totalRow)
    updateIndexes(rows)
    def tmpRows = rows.findAll { !it.getAlias() }
    compareSimpleTotalValues(totalRow, totalAll, tmpRows, totalColumns, formData, logger, false)

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
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
            // название первого столбца хранится в нулевой скрытой графе
            ([(headerRows[0][0]): tmpRow.getCell('rowNumber').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('taxAuthority').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('kpp').column.name]),
            ([(headerRows[0][4]): tmpRow.getCell('okato').column.name]),
            ([(headerRows[0][5]): tmpRow.getCell('tsTypeCode').column.name]),
            ([(headerRows[0][6]): tmpRow.getCell('tsType').column.name]),
            ([(headerRows[0][7]): tmpRow.getCell('model').column.name]),
            ([(headerRows[0][8]): tmpRow.getCell('ecoClass').column.name]),
            ([(headerRows[0][9]): tmpRow.getCell('vi').column.name]),
            ([(headerRows[0][10]): tmpRow.getCell('regNumber').column.name]),
            ([(headerRows[0][11]): tmpRow.getCell('regDate').column.name]),
            ([(headerRows[0][12]): tmpRow.getCell('regDateEnd').column.name]),
            ([(headerRows[0][13]): tmpRow.getCell('taxBase').column.name]),
            ([(headerRows[0][14]): tmpRow.getCell('taxBaseOkeiUnit').column.name]),
            ([(headerRows[0][15]): tmpRow.getCell('createYear').column.name]),
            ([(headerRows[0][16]): tmpRow.getCell('years').column.name]),
            ([(headerRows[0][17]): 'Сведения об угоне']),
            ([(headerRows[1][17]): 'Дата начала розыска ТС']),
            ([(headerRows[1][18]): 'Дата возврата ТС']),
            ([(headerRows[0][19]): tmpRow.getCell('periodStartCost').column.name]),
            ([(headerRows[0][20]): tmpRow.getCell('periodEndCost').column.name]),
            ([(headerRows[0][21]): tmpRow.getCell('ownMonths').column.name]),
            ([(headerRows[0][22]): tmpRow.getCell('partRight').column.name]),
            ([(headerRows[0][23]): tmpRow.getCell('coef362').column.name]),
            ([(headerRows[0][24]): tmpRow.getCell('taxRate').column.name]),
            ([(headerRows[0][25]): tmpRow.getCell('calculatedTaxSum').column.name]),
            ([(headerRows[0][26]): tmpRow.getCell('benefitMonths').column.name]),
            ([(headerRows[0][27]): tmpRow.getCell('benefitStartDate').column.name]),
            ([(headerRows[0][28]): tmpRow.getCell('benefitEndDate').column.name]),
            ([(headerRows[0][29]): tmpRow.getCell('coefKl').column.name]),
            ([(headerRows[0][30]): tmpRow.getCell('taxBenefitCode').column.name]),
            ([(headerRows[0][31]): tmpRow.getCell('benefitSum').column.name]),
            ([(headerRows[0][32]): tmpRow.getCell('taxBenefitCodeDecrease').column.name]),
            ([(headerRows[0][33]): tmpRow.getCell('benefitSumDecrease').column.name]),
            ([(headerRows[0][34]): tmpRow.getCell('benefitCodeReduction').column.name]),
            ([(headerRows[0][35]): tmpRow.getCell('benefitSumReduction').column.name]),
            ([(headerRows[0][36]): tmpRow.getCell('koefKp').column.name]),
            ([(headerRows[0][37]): tmpRow.getCell('taxSumToPay').column.name]),
            ([(headerRows[0][38]): tmpRow.getCell('benefitBase').column.name])
    ]
    (1..38).each { index ->
        headerMapping.add(([(headerRows[2][index]): index.toString()]))
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
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias("Редактируемое поле")
    }
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2
    def colIndex = 2
    newRow.taxAuthority = values[colIndex]
    // графа 3
    colIndex++
    newRow.kpp = values[colIndex]
    // графа 4 - 96 840 CODE
    colIndex++
    newRow.okato = getRecordIdImport(96L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 5 - 42 422 CODE
    colIndex++
    def record42 = getRecordImport(42L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    newRow.tsTypeCode = record42?.record_id?.value
    // графа 6 - 42 423 NAME - зависит от графы 5
    colIndex++
    if (record42 != null) {
        def value1 = values[colIndex]
        def value2 = record42?.NAME?.value?.toString()
        formDataService.checkReferenceValue(42, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }
    // графа 7
    colIndex++
    newRow.model = values[colIndex]
    // графа 8 - 40 400 CODE
    colIndex++
    newRow.ecoClass = getRecordIdImport(40L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 9
    colIndex++
    newRow.vi = values[colIndex]
    // графа 10
    colIndex++
    newRow.regNumber = values[colIndex]
    // графа 11
    colIndex++
    newRow.regDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // графа 12
    colIndex++
    newRow.regDateEnd = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // графа 13
    colIndex++
    newRow.taxBase = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 14 - 12 57 CODE
    colIndex++
    newRow.taxBaseOkeiUnit = getRecordIdImport(12L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 15
    colIndex++
    newRow.createYear = parseDate(values[colIndex], "yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // графа 16
    colIndex++
    newRow.years = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 17
    colIndex++
    newRow.stealDateStart = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // графа 18
    colIndex++
    newRow.stealDateEnd = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // графа 19
    colIndex++
    newRow.periodStartCost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 20
    colIndex++
    newRow.periodEndCost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 21
    colIndex++
    newRow.ownMonths = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 22
    colIndex++
    newRow.partRight = values[colIndex]
    // графа 23
    colIndex++
    newRow.coef362 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // def errorMsg = "Строка $rowIndex: "
    // получение региона по ОКТМО
    // def region = getRegionByOKTMO(newRow.okato)

    // графа 24 - 41 416 VALUE
    colIndex++
    // newRow.taxRate = calc24(newRow, region, errorMsg, false)
    tmpRow = getTaxRateImport(values, fileRowIndex, colIndex + colOffset, newRow)
    newRow.taxRate = tmpRow.taxRate
    //newRow.taxRate = getRecordIdImport(41L, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 25
    colIndex++
    newRow.calculatedTaxSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 26
    colIndex++
    newRow.benefitMonths = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 27
    colIndex++
    newRow.benefitStartDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // графа 28
    colIndex++
    newRow.benefitEndDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    // графа 29
    colIndex++
    newRow.coefKl = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 30 - 7 19 TAX_BENEFIT_ID 6 16 NAME
    colIndex++
    newRow.taxBenefitCode = tmpRow.taxBenefitCode
    // графа 31
    colIndex++
    newRow.benefitSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 32 - 7 19 TAX_BENEFIT_ID 6 16 NAME
    colIndex++
    newRow.taxBenefitCodeDecrease = tmpRow.taxBenefitCodeDecrease
    // графа 33
    colIndex++
    newRow.benefitSumDecrease = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 34 - 7 19 TAX_BENEFIT_ID 6 16 NAME
    colIndex++
    newRow.benefitCodeReduction = tmpRow.benefitCodeReduction
    // графа 35
    colIndex++
    newRow.benefitSumReduction = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 36
    colIndex++
    newRow.koefKp = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 37
    colIndex++
    newRow.taxSumToPay = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 38
    colIndex++
    newRow.benefitBase = values[colIndex]

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
def getTotalFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 19
    def colIndex = 19
    newRow.periodStartCost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 20
    colIndex = 20
    newRow.periodEndCost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 25
    colIndex = 25
    newRow.calculatedTaxSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 31
    colIndex = 31
    newRow.benefitSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 33
    colIndex = 33
    newRow.benefitSumDecrease = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 35
    colIndex = 35
    newRow.benefitSumReduction = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    // графа 37
    colIndex = 37
    newRow.taxSumToPay = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// что бы восстановить значение графы 30 или 32 или 34, надо
// 1. по надписи из эксельки найти запись для код льготы (из справочника 6)
// 2. по окато из эксельки (графа 4) найти регион (из справочника 4)
// 3. по найденым льготе и региону + по идентификатору региона формы найти нужную льготу (из справочника 7)
def getTaxRateImport(def values, def rowIndex, def colIndex, def row) {
    def okatoId = row.okato
    def taxRate = values[24]
    def taxBenefitCode = values[30]
    def taxBenefitCodeDecrease = values[32]
    def benefitCodeReduction = values[34]

    def declarationRegionId = formDataDepartment.regionId
    if (!declarationRegionId && rowIndex == 1) {
        logger.warn("На форме невозможно заполнить графы по ставке налога и кодам налоговых льгот, так как атрибут " +
                "«Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    } else if (declarationRegionId) {
        if (taxRate || taxBenefitCode || taxBenefitCodeDecrease || benefitCodeReduction) {
            if (!okatoId) {
                logger.warn("Строка $rowIndex файла: На форме невозможно заполнить графы по ставке налога и кодам " +
                        "налоговых льгот, так как в файле не заполнена графа «" + getColumnName(row, 'okato') + "»!")
            } else {
                def okato = getOkato(getRefBookValue(96L, okatoId)?.CODE?.value)
                def region = getRegion(row.okato)
                if(!region){
                    logger.warn("Строка $rowIndex, столбец " + getXLSColumnName(colIndex) + ": " +
                            "На форме невозможно заполнить графы по ставке налога и кодам налоговых льгот, так как в справочнике " +
                            "«Коды субъектов Российской Федерации» отсутствует запись, в которой графа " +
                            "«Определяющая часть кода ОКТМО» равна значению первых символов графы «Код ОКТМО» ($okato) формы!")
                } else {
                    def regionId = region?.record_id?.value

                    row.taxBenefitCode = getTaxRate(regionId, okato, taxBenefitCode, 'taxBenefitCode', rowIndex, colIndex)
                    row.taxBenefitCodeDecrease = getTaxRate(regionId, okato, taxBenefitCodeDecrease, 'taxBenefitCodeDecrease', rowIndex, colIndex)
                    row.benefitCodeReduction = getTaxRate(regionId, okato, benefitCodeReduction, 'benefitCodeReduction', rowIndex, colIndex)

                    if (taxRate) {
                        calc24(row, region)
                        if (!row.taxRate) {
                            def tmpRow = formData.createDataRow()
                            def declarationRegionCode = getRefBookValue(4L, declarationRegionId)?.CODE?.value
                            logger.warn("Строка $rowIndex, столбец " + getXLSColumnName(colIndex) + ": " +
                                    "На форме не заполнена графа «" + getColumnName(tmpRow, 'taxRate') + "», " +
                                    "так как в справочнике «Ставки транспортного налога» не найдена запись, " +
                                    "актуальная на дату «" + getReportPeriodEndDate().format("dd.MM.yyyy") + "», соответствующая следующим параметрам: " +
                                    "«Код субъекта РФ представителя декларации» = «$declarationRegionCode», " +
                                    "«Код субъекта РФ» = «$okato», " +
                                    "«Код вида ТС» = «${row.tsTypeCode}», " +
                                    "«Количество лет, прошедших с года выпуска ТС» = «${row.years}», " +
                                    "«Налоговая база» (мощность) = «${row.taxBase}», " +
                                    "«Единица измерения налоговой базы по ОКЕИ» = «${row.taxBaseOkeiUnit}»!")
                        }
                    }
                }
            }
        }
    }
    return row
}

def getTaxRate(def regionId, def okato, def taxBenefitCode, def colname, def rowIndex, def colIndex) {
    if (!taxBenefitCode || !regionId) {
        return null
    }
    def ref_id = 7
    def declarationRegionId = formDataDepartment.regionId
    def taxBenefitId = getRecordIdImport(6L, 'CODE', taxBenefitCode, rowIndex, colIndex)
    if (taxBenefitId == null) {
        return null
    }
    String filter = "DECLARATION_REGION_ID = $declarationRegionId and DICT_REGION_ID = $regionId and TAX_BENEFIT_ID = $taxBenefitId"
    if (recordCache[ref_id] != null) {
        if (recordCache[ref_id][filter] != null) {
            return recordCache[ref_id][filter]
        }
    } else {
        recordCache[ref_id] = [:]
    }

    def provider = refBookFactory.getDataProvider(ref_id)
    def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
    if (records.size() > 0) {
        recordCache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return recordCache[ref_id][filter]
    } else {
        def tmpRow = formData.createDataRow()
        def region = getRefBookValue(4L, declarationRegionId)
        logger.warn("Строка $rowIndex, столбец " + getXLSColumnName(colIndex) + ": " +
                "На форме не заполнена графа «" + getColumnName(tmpRow, colname) + "», так как в справочнике " +
                "«Параметры налоговых льгот транспортного налога» не найдена запись, " +
                "актуальная на дату «" + getReportPeriodEndDate().format("dd.MM.yyyy") + "», " +
                "в которой поле «Код субъекта РФ представителя декларации» = «${region?.CODE?.value}», " +
                "поле «Код субъекта РФ» = «$okato», поле «Код налоговой льготы» = «$taxBenefitCode»!")
    }
    return null
}

def getOkato(def codeOkato) {
    if(!codeOkato || codeOkato.length() < 3){
        return codeOkato
    }
    codeOkato = codeOkato?.substring(0, 3)
    if(codeOkato && !(codeOkato in ["719", "718", "118"])){
        codeOkato = codeOkato?.substring(0, 2)
    }
    return codeOkato
}

@Field
def formTypeMap = [:]

def getFormType(def id) {
    if (formTypeMap[id] == null) {
        formTypeMap[id] = formTypeService.get(id)
    }
    return formTypeMap[id]
}

@Field
def allRecordsMap = [:]

/**
 * Получить все записи справочника.
 *
 * @param refBookId id справочника
 * @return мапа с записями справочника (ключ "id записи" -> запись)
 */
def getAllRecords(def refBookId) {
    if (allRecordsMap[refBookId] == null) {
        def date = getReportPeriodEndDate()
        def provider = getProvider(refBookId)
        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(date, null)
        allRecordsMap[refBookId] = provider.getRecordData(uniqueRecordIds)
    }
    return allRecordsMap[refBookId]
}