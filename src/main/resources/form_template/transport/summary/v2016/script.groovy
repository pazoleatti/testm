package form_template.transport.summary.v2016

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

/**
 * Расчёт суммы налога по каждому транспортному средству.
 *
 * formTypeId = 200
 * formTemplateId = 1200
 *
 * TODO:
 *      - расчеты
 *      - проверить расчеты
 *      - логические проверки
 *      - проверить логические проверки
 *      - загрузка
 *      - проверить загрузку
 *      - консолидация
 *      - проверить консолидацию
 *      - добавить тесты
 *      - копирование данных
 *      - сравнение данных
 *
 *      - выполнять предрасчетную проверку только перед расчетом? или еще при принятии, проверке, консолидации?
 *      - переименовать taxBaseOkeiUnit в baseUnit как в сведениях о ТС ?
 *
 *      - переименовал taxAuthority в kno !!!!
 *      - переименовал coef362 в coefKv !!!!
 *      - переименовал coefKl в coefKp !!!!
 *      - переименовал koefKp в coefKl !!!!
 *      - переименовал benefitBase в taxBenefitBase !!!!
 */

// графа    - fix
// графа 1  - rowNumber
// графа 2  - kno               - атрибут 2102 - TAX_ORGAN_CODE - «Код налогового органа (кон.)», справочник 210 «Параметры представления деклараций по транспортному налогу»
// графа 3  - kpp               - зависит от графы 2 - атрибут 2103 - KPP - «КПП», справочник 210 «Параметры представления деклараций по транспортному налогу»
// графа 4  - okato             - зависит от графы 2 - атрибут 2104.840 - OKTMO.CODE - «Код ОКТМО».«Код», справочник 210.96 «Параметры представления деклараций по транспортному налогу».«Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
// графа 5  - tsTypeCode        - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
// графа 6  - tsType            - зависит от графы 5 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
// графа 7  - model
// графа 8  - ecoClass          - атрибут 400 - CODE - «Код экологического класса», справочник 40 «Экологические классы»
// графа 9  - vi
// графа 10 - regNumber
// графа 11 - regDate
// графа 12 - regDateEnd
// графа 13 - taxBase
// графа 14 - taxBaseOkeiUnit   - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
// графа 15 - createYear
// графа 16 - years
// графа 17 - ownMonths
// графа 18 - partRight
// графа 19 - coefKv
// графа 20 - taxRate           - атрибут 416 - VALUE - «Ставка (руб.)», справочник 41 «Ставки транспортного налога»
// графа 21 - coefKp            - атрибут 2093 - COEF - «Повышающий коэффициент», справочник 209 «Повышающие коэффициенты транспортного налога»
// графа 22 - calculatedTaxSum
// графа 23 - benefitMonths
// графа 24 - coefKl
// графа 25 - taxBenefitCode    - атрибут 19.15 - TAX_BENEFIT_ID.CODE - «Код налоговой льготы».«Код», справочник 7.6 «Параметры налоговых льгот транспортного налога».«Коды налоговых льгот и вычетов транспортного налога»
// графа 26 - taxBenefitBase    - зависит от графы 9 - атрибут 702 - BASE - «Основание», справочник 7 «Параметры налоговых льгот транспортного налога»
// графа 27 - taxBenefitSum
// графа 28 - deductionCode     - атрибут 15 - CODE - «Код», справочник 6 «Коды налоговых льгот и вычетов транспортного налога»
// графа 29 - deductionSum
// графа 30 - taxSumToPay
// графа 31 - q1
// графа 32 - q2
// графа 33 - q3
// графа 34 - q4

// TODO (Ramil Timerbaev) удалены:
// графа 17 - stealDateStart
// графа 18 - stealDateEnd
// графа 19 - periodStartCost
// графа 20 - periodEndCost
// графа 27 - benefitStartDate
// графа 28 - benefitEndDate
// графа 31 - benefitSum
// графа 32 - taxBenefitCodeDecrease 7 19 TAX_BENEFIT_ID 6 16 NAME
// графа 33 - benefitSumDecrease
// графа 34 - benefitCodeReduction 7 19 TAX_BENEFIT_ID 6 16 NAME
// графа 35 - benefitSumReduction

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkRegionId()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.CHECK:
        checkRegionId()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        checkRegionId()
        def sourcesInfo = preComposeCheck()
        if (logger.containsLevel(LogLevel.ERROR)) {
            return
        }
        consolidation(sourcesInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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

//Все аттрибуты
@Field
def allColumns = ['fix', 'rowNumber', 'kno', 'kpp', 'okato', 'tsTypeCode', 'tsType', 'model', 'ecoClass',
        'vi', 'regNumber', 'regDate', 'regDateEnd', 'taxBase', 'taxBaseOkeiUnit', 'createYear', 'years', 'ownMonths',
        'partRight', 'coefKv', 'taxRate', 'coefKp', 'calculatedTaxSum', 'benefitMonths', 'coefKl', 'taxBenefitCode',
        'taxBenefitBase', 'taxBenefitSum', 'deductionCode', 'deductionSum', 'taxSumToPay', 'q1', 'q2', 'q3', 'q4']

// графа 30..34
@Field
def totalColumns = ['taxSumToPay', 'q1', 'q2', 'q3', 'q4']

// графа 2
@Field
def editableColumns = ['kno']

// Автозаполняемые атрибуты (все кроме редактируемых)
@Field
def autoFillColumns = allColumns - editableColumns - 'fix'

// графа 1..7, 9..11, 13..20, 22, 30, 31 (графа 32..34 обязательны для некоторых периодов)
@Field
def nonEmptyColumns = ['kno', 'tsTypeCode', 'model', 'vi', 'regNumber', 'regDate',
        'taxBase', 'taxBaseOkeiUnit', 'createYear', 'years', 'ownMonths', 'partRight', 'coefKv',
        'taxRate', 'calculatedTaxSum', 'taxSumToPay', 'q1' /*, 'q2', 'q3', 'q4'*/ ]

// графа 2, 3, 4
@Field
def groupColumns = ['kno', 'kpp', 'okato']

// TODO (Ramil Timerbaev) удалить?
@Field
def monthCountInPeriod

// TODO (Ramil Timerbaev) удалить?
@Field
def reportDay = null

@Field
def startDate = null

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

// TODO (Ramil Timerbaev) поменять на новые?
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

// TODO (Ramil Timerbaev) удалить?
// Поиск записи в справочнике по значению (для расчетов)
def getRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String columnName,
              def Date date, boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, null, date,
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

def calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить фиксированные строки
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { groupColumns.contains(it.getAlias())})
    sortRows(dataRows, groupColumns)

    for (def row : dataRows) {
        // TODO (Ramil Timerbaev) дополнить
    }

    // добавить подитоги
    addAllStatic(dataRows)

    // добавить строку "итого"
    dataRows.add(calcTotalRow(dataRows))
    updateIndexes(dataRows)

    // сравение данных с предыдущей формой
    // comparePrevRows()
}

// TODO (Ramil Timerbaev) убрать?
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
        row.kno = records[0].TAX_ORGAN_CODE?.value
        row.kpp = records[0].KPP?.value
    } else {
        boolean isMany = records.size() > 1
        LogLevel level = isMany ? LogLevel.WARNING : LogLevel.ERROR
        def okato = getRefBookValue(96, row.okato).CODE.value
        def msg1 = isMany ? "более одной записи, актуальной" : "отсутствует запись, актуальная"
        def endString = getReportPeriodEndDate().format('dd.MM.yyyy')
        def declarationRegionCode = getRefBookValue(4, formDataDepartment.regionId).CODE.value
        def regionCode = region?.CODE?.value ?: ''
        logger.log(level, "Строка %s. Графа «%s» = «%s»: В справочнике «Параметры представления деклараций по транспортному налогу» %s на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s»",
                row.getIndex(), getColumnName(row, 'okato'), okato, msg1, endString, declarationRegionCode, formDataDepartment.name, regionCode, okato)
    }
}

// TODO (Ramil Timerbaev) убрать?
def checkTaKpp(def row, def region, def rowIndex) {
    if (formDataDepartment.regionId == null || row.okato == null || row.kno == null || row.kpp == null) {
        return
    }
    def allRecords = getAllRecords(210L).values()
    def regionId = region?.record_id?.value
    def records = allRecords.findAll { record ->
        record.DECLARATION_REGION_ID.value == formDataDepartment.regionId &&
                record.REGION_ID.value == regionId &&
                record.OKTMO.value == row.okato &&
                record.TAX_ORGAN_CODE.value?.toLowerCase() == row.kno?.toLowerCase() &&
                record.KPP.value?.toLowerCase() == row.kpp?.toLowerCase()
    }
    if (records.size() < 1) {
        def okato = getRefBookValue(96, row.okato).CODE.value
        def declarationRegionCode = getRefBookValue(4, formDataDepartment.regionId).CODE.value
        def regionCode = region?.CODE?.value ?: ''
        logger.error("Строка %s. Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: В справочнике «Параметры представления деклараций по транспортному налогу» " +
                "отсутствует запись, актуальная на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                "справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s», поле «Код налогового органа (кон.)» = «%s» поле «КПП» = «%s»",
                rowIndex, getColumnName(row, 'okato'), okato, getColumnName(row, 'kno'), row.kno, getColumnName(row, 'kpp'), row.kpp,
                getReportPeriodEndDate().format('dd.MM.yyyy'), declarationRegionCode,
                formDataDepartment.name, regionCode, okato, row.kno, row.kpp
        )
    }
}

// TODO (Ramil Timerbaev) убрать?
@Field
def uniqueColumns = ['okato', 'tsTypeCode', 'vi', 'taxBase', 'taxBaseOkeiUnit']

// TODO (Ramil Timerbaev) передалать/дополнить
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { groupColumns.contains(it.getAlias())})

    def isCalc = (formDataEvent == FormDataEvent.CALCULATE)

    // для логической проверки 1
    def nonEmptyColumnsTmp = nonEmptyColumns
    if (getReportPeriod().order == 2) {
        nonEmptyColumnsTmp = nonEmptyColumnsTmp + 'q2'
    } else if (getReportPeriod().order == 3) {
        nonEmptyColumnsTmp = nonEmptyColumnsTmp + 'q2' + 'q3'
    } else if (getReportPeriod().order == 4) {
        nonEmptyColumnsTmp = nonEmptyColumnsTmp + 'q2' + 'q3' + 'q4'
    }

    // для логической проверки N
    def needValue = [:]
    // графа ...
    def arithmeticCheckAlias = []

    for (def row : dataRows) {
        if (row.getAlias()) {
            continue
        }
        def rowIndex = row.getIndex()

        // N. Проверка обязательности заполнения граф
        checkNonEmptyColumns(row, rowIndex, nonEmptyColumnsTmp, logger, true)

        // N. Проверка корректности заполнения граф ...
        if (!isCalc) {
            // needValue.someName = calcN(row)
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
                logger.error("Строка %s: Графы «%s» заполнены неверно. Выполните расчет формы", rowIndex, columnNames)
            }
        }
    }

    // N. Проверка наличия формы предыдущего периода в состоянии «Принята»
    // Выполняется после консолидации, перед копированием данных, в методе copyFromPrevForm()

    // N. Проверка наличия формы предыдущего периода в состоянии «Принята»
    // Выполняется после расчетов, перед сравнением данных, в методе comparePrevRows()

    // N. Проверка корректности значений итоговых строк (строка "итого")
    if (!isCalc) {
        def lastSimpleRow = null
        def subTotalMap = [:]
        for (def row : dataRows) {
            if (!row.getAlias()) {
                lastSimpleRow = row

                // подитог кно/кпп/октмо - проверка отсутствия подитога
                def key2 = row.getCell('kno').refBookDereference + '#' +
                        row.getCell('kpp').refBookDereference + '#' +
                        row.getCell('okato').refBookDereference

                if (subTotalMap[key2] == null) {
                    def findSubTotal = dataRows.find {
                        it.getAlias()?.startsWith('total2') &&
                                it.getCell('kno').refBookDereference == row.getCell('kno').refBookDereference &&
                                it.getCell('kpp').refBookDereference == row.getCell('kpp').refBookDereference &&
                                it.getCell('okato').refBookDereference == row.getCell('okato').refBookDereference
                    }
                    subTotalMap[key2] = (findSubTotal != null)
                    if (findSubTotal == null) {
                        def subMsg = getColumnName(row, 'kno') + '=' + (row.getCell('kno').refBookDereference ?: 'не задан') + ', ' +
                                getColumnName(row, 'kpp') + '=' + (row.getCell('kpp').refBookDereference ?: 'не задан') + ', ' +
                                getColumnName(row, 'okato') + '=' + (row.getCell('okato').refBookDereference ?: 'не задан')
                        logger.error(GROUP_WRONG_ITOG, subMsg)
                    }
                }

                // подитог кно/кпп - проверка отсутствия подитога
                def key1 = row.getCell('kno').refBookDereference + '#' + row.getCell('kpp').refBookDereference
                if (subTotalMap[key1] == null) {
                    def findSubTotal = dataRows.find {
                        it.getAlias()?.startsWith('total1') &&
                                it.getCell('kno').refBookDereference == row.getCell('kno').refBookDereference &&
                                it.getCell('kpp').refBookDereference == row.getCell('kpp').refBookDereference
                    }
                    subTotalMap[key1] = (findSubTotal != null)
                    if (findSubTotal == null) {
                        def subMsg = getColumnName(row, 'kno') + '=' + (row.getCell('kno').refBookDereference ?: 'не задан') + ', ' +
                                getColumnName(row, 'kpp') + '=' + (row.getCell('kpp').refBookDereference ?: 'не задан')
                        logger.error(GROUP_WRONG_ITOG, subMsg)
                    }
                }
                continue
            }

            if (row.getAlias() != null && row.getAlias().indexOf('total2') != -1) {
                // подитог кно/кпп/октмо
                // принадлежность подитога к последей простой строке
                if (row.getCell('kno').refBookDereference != lastSimpleRow.getCell('kno').refBookDereference ||
                        row.getCell('kpp').refBookDereference != lastSimpleRow.getCell('kpp').refBookDereference ||
                        row.getCell('okato').refBookDereference != lastSimpleRow.getCell('okato').refBookDereference) {
                    logger.error(GROUP_WRONG_ITOG_ROW, row.getIndex())
                    continue
                }
                // проверка сумм
                def srow = calcSubTotalRow2(dataRows.indexOf(row) - 1, dataRows, row.kno /*, row.kpp, row.okato*/)
                checkTotalRow(row, srow)
            } else if (row.getAlias() != null && row.getAlias().indexOf('total1') != -1) {
                // подитог кно/кпп
                // принадлежность подитога к последей простой строке
                if (row.getCell('kno').refBookDereference != lastSimpleRow.getCell('kno').refBookDereference ||
                        row.getCell('kpp').refBookDereference != lastSimpleRow.getCell('kpp').refBookDereference) {
                    logger.error(GROUP_WRONG_ITOG_ROW, row.getIndex())
                    continue
                }
                // проверка сумм
                def srow = calcSubTotalRow1(dataRows.indexOf(row) - 1, dataRows, row.kno /*, row.kpp*/ )
                checkTotalRow(row, srow)
            }
            lastSubTotalRow = row
        }

        // строка "итого"
        def totalRow = dataRows.find { 'total'.equals(it.getAlias()) }
        if (totalRow != null) {
            def tmpTotalRow = calcTotalRow(dataRows)
            checkTotalRow(totalRow, tmpTotalRow)
        } else {
            logger.error("Итоговые значения рассчитаны неверно!")
        }
    }
}

/**
 * Проверить итоги/подитоги. Для логической проверки 18.
 *
 * @param row итоговая строка нф
 * @param tmpRow посчитанная итоговая строка
 */
void checkTotalRow(def row, def tmpRow) {
    def errorColumns = []
    for (def column : totalColumns) {
        if (row[column] != tmpRow[column]) {
            errorColumns.add(getColumnName(row, column))
        }
    }
    if (!errorColumns.isEmpty()) {
        def columnNames = errorColumns.join('», «')
        logger.error("Строка %s: Графы «%s» заполнены неверно. Выполните расчет формы", row.getIndex(), columnNames)
    }
}

/** Получение региона по коду ОКТМО. */
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

def getOkato(def codeOkato) {
    if (!codeOkato || codeOkato.length() < 3) {
        return codeOkato
    }
    codeOkato = codeOkato?.substring(0, 3)
    if (codeOkato && !(codeOkato in ["719", "718", "118"])) {
        codeOkato = codeOkato?.substring(0, 2)
    }
    return codeOkato
}

// TODO (Ramil Timerbaev) удалить?
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

// TODO (Ramil Timerbaev) удалить?
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
def groupColumnsListList = [
        ['version', 'pastYear'],
        ['codeOKATO'],
        ['codeOKATO', 'tsTypeCode', 'baseUnit']
]

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
                    def periodName = getReportPeriod().name
                    def periodYear = getReportPeriod().taxPeriod.year
                    logger.warn("Строки %s формы-источника. Графа «%s» = «%s», графа «%s» = «%s». В справочнике «Повышающие коэффициенты транспортного налога» отсутствует запись, " +
                            "актуальная на дату %s, в которой поле «Средняя стоимость» = «%s» и значение «%s» больше значения поля «Количество лет, прошедших с года выпуска ТС (от)» и меньше или равно значения поля «Количество лет, прошедших с года выпуска ТС (до)». " +
                            "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                            rowIndexes.join(', '), getColumnName(row, 'pastYear'), row.pastYear, getColumnName(row, 'averageCost'), avgCost,
                            getReportPeriodEndDate().format("dd.MM.yyyy"), avgCost, getColumnName(row, 'pastYear'),
                            relation.formDataKind.title, relation.formType.name, relation.department.name, periodName, periodYear
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
                    def regionCode = region?.CODE?.value ?: ''
                    def codeOKATO = getRefBookValue(96L, row.codeOKATO).CODE.value
                    def periodName = getReportPeriod().name
                    def periodYear = getReportPeriod().taxPeriod.year
                    logger.error("Строки %s формы-источника. Графа «%s» = «%s»: В справочнике «Параметры представления деклараций по транспортному налогу» отсутствует запись, " +
                            "актуальная на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) справочника «Подразделения» " +
                            "для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s». " +
                            "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                            rowIndexes.join(', '), getColumnName(row, 'codeOKATO'), codeOKATO,
                            getReportPeriodEndDate().format('dd.MM.yyyy'), declarationRegionCode,
                            formDataDepartment.name, regionCode, codeOKATO,
                            relation.formDataKind.title, relation.formType.name, relation.department.name, periodName, periodYear
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
                    def regionCode = region?.CODE?.value ?: ''
                    def tsTypeCode = getRefBookValue(42L, row.tsTypeCode).CODE.value
                    def baseUnit = getRefBookValue(12L, row.baseUnit).CODE.value
                    def codeOKATO = getRefBookValue(96L, row.codeOKATO).CODE.value
                    def msg1 = isMany ? "более одной записи, актуальной" : "отсутствует запись, актуальная"
                    def periodName = getReportPeriod().name
                    def periodYear = getReportPeriod().taxPeriod.year
                    logger.error("Строки %s формы-источника. Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: В справочнике «Ставки транспортного налога» " +
                            "%s на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                            "справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код ТС» = «%s», поле «Ед. измерения мощности» = «%s». " +
                            "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                            rowIndexes.join(', '), getColumnName(row, 'codeOKATO'), codeOKATO, getColumnName(row, 'tsTypeCode'), tsTypeCode, getColumnName(row, 'baseUnit'), baseUnit,
                            msg1, getReportPeriodEndDate().format('dd.MM.yyyy'), declarationRegionCode,
                            relation.getDepartment().name, regionCode, tsTypeCode, baseUnit,
                            relation.formDataKind.title, relation.formType.name, relation.department.name, periodName, periodYear
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
        newRow.getCell(it).setStyleAlias("Редактируемая")
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
            def regionCode = region?.CODE?.value ?: ''
            def tsTypeCode = getRefBookValue(42L, row.tsTypeCode).CODE.value
            def taxBaseOkeiUnit = getRefBookValue(12L, row.taxBaseOkeiUnit).CODE.value
            def okato = getRefBookValue(96L, row.okato).CODE.value
            def msg1 = isMany ? "более одной записи, актуальной" : "отсутствует запись, актуальная"
            logger.error("Строка %s. Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: В справочнике «Ставки транспортного налога» " +
                    "%s на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
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
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def subTotalRow1Map = [:] // подитог 1ого уровня -> мапа с подитогами 2ого уровня
    def subTotalRow2Map = [:] // подитог 2ого уровня -> строки подгруппы
    def simpleRows = []
    def total = null

    // разложить группы по мапам
    dataRows.each{ row ->
        if (row.getAlias() == null) {
            simpleRows.add(row)
        } else if (row.getAlias().contains('total2')) {
            subTotalRow2Map.put(row, simpleRows)
            simpleRows = []
        } else if (row.getAlias().contains('total1')) {
            subTotalRow1Map.put(row, subTotalRow2Map)
            subTotalRow2Map = [:]
        } else {
            total = row
        }
    }
    dataRows.clear()

    // отсортировать и добавить все строки
    def tmpSorted1Rows = subTotalRow1Map.keySet().toList()?.sort { getSortValue(it) }
    tmpSorted1Rows.each { keyRow1 ->
        def subMap = subTotalRow1Map[keyRow1]
        def tmpSorted2Rows = subMap.keySet().toList()?.sort { getSortValue(it) }
        tmpSorted2Rows.each { keyRow2 ->
            def dataRowsList = subMap[keyRow2]
            sortAddRows(dataRowsList, dataRows)
            dataRows.add(keyRow2)
        }
        dataRows.add(keyRow1)
    }
    // если остались данные вне иерархии, то добавить их перед итогом
    sortAddRows(simpleRows, dataRows)
    dataRows.add(total)

    dataRowHelper.saveSort()
}

// значение группируемых столбцов для сортировки подитоговых строк
def getSortValue(def row) {
    return row.getCell('kno').refBookDereference + '#' + row.getCell('kpp').refBookDereference + '#' + row.getCell('okato').refBookDereference
}

void sortAddRows(def addRows, def dataRows) {
    if (!addRows.isEmpty()) {
        def firstRow = addRows[0]
        // Массовое разыменовывание граф НФ
        def columnNameList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, addRows, columnNameList)
        sortRowsSimple(addRows)
        dataRows.addAll(addRows)
    }
}

// TODO (Ramil Timerbaev) выполнять предрасчетную проверку только перед расчетом? или еще при принятии, проверке, консолидации?
// Проверка заполнения атрибута «Регион» подразделения текущей формы (справочник «Подразделения»)
void checkRegionId() {
    if (formDataDepartment.regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }
}

// TODO (Ramil Timerbaev) переделать
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 35
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
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

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()
    def totalRowFromFileMap = [:]           // мапа для хранения строк итогов/подитогов со значениями из файла (стили простых строк)
    def totalRowMap = [:]                   // мапа для хранения строк итогов/подитогов нф с посчитанными значениями и со стилями

    // TODO (Ramil Timerbaev) нужно ли это сообщение?
    // 1. Проверка заполнения поля «Регион» справочника «Подразделения» для подразделения формы
    def departmentRegionId = formDataDepartment.regionId
    if (!departmentRegionId) {
        logger.warn("Ну удалось заполнить графу «%s», т.к. для подразделения формы не заполнено поле «Регион» справочника «Подразделения»",
                getColumnName(tmpRow, 'taxBenefitCode'))
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
        // пропуск итоговой строки
        if (rowValues[INDEX_FOR_SKIP]?.trim()?.equalsIgnoreCase("итого")) {
            // получить значения итоговой строки из файла
            rowIndex++
            totalRowFromFileMap[rowIndex] = getNewTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // пропуск подитоговых строк
        if (rowValues[INDEX_FOR_SKIP]?.trim()?.toLowerCase()?.contains("итого по ")) {
            def record210 = getRecord210(rowValues[2], rowValues[3], rowValues[4], fileRowIndex, 2, colOffset)
            knoId = record210?.record_id?.value
            def subTotalRow
            // сформировать и подсчитать подитоги
            if (rowValues[INDEX_FOR_SKIP]?.trim()?.toLowerCase()?.contains("октмо")) {
                // def oktmo = (rowValues[6] ? getRecordIdImport(96L, 'CODE', rowValues[6], fileRowIndex, 6 + colOffset) : null)
                subTotalRow = calcSubTotalRow2(rowIndex - 1, rows, knoId /*, rowValues[3], oktmo*/)
            } else {
                subTotalRow = calcSubTotalRow1(rowIndex - 1, rows, knoId /*, rowValues[3]*/ )
            }
            rows.add(subTotalRow)
            // получить значения подитоговой строки из файла
            rowIndex++
            totalRowFromFileMap[rowIndex] = getNewTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            totalRowMap[rowIndex] = subTotalRow

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, departmentRegionId)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // итоговая строка
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)
    totalRowMap[rowIndex] = totalRow
    updateIndexes(rows)

    // сравнение итогов
    if (!totalRowFromFileMap.isEmpty()) {
        // сравнение
        totalRowFromFileMap.keySet().toArray().each { index ->
            def totalFromFile = totalRowFromFileMap[index]
            def total = totalRowMap[index]
            compareTotalValues(totalFromFile, total, totalColumns, logger, 0, false)
            // задание значении итоговой строке нф из итоговой строки файла (потому что в строках из файла стили для простых строк)
            total.setImportIndex(totalFromFile.getImportIndex())
            (totalColumns + 'fix').each { alias ->
                total[alias] = totalFromFile[alias]
            }
        }
    }

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

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping = [
            // название первого столбца хранится в нулевой скрытой графе
            [(headerRows[0][0]) : headers[0].fix],
            [(headerRows[0][2]) : headers[0].kno],
            [(headerRows[0][3]) : headers[0].kpp],
            [(headerRows[0][4]) : headers[0].okato],
            [(headerRows[0][5]) : headers[0].tsTypeCode],
            [(headerRows[0][6]) : headers[0].tsType],
            [(headerRows[0][7]) : headers[0].model],
            [(headerRows[0][8]) : headers[0].ecoClass],
            [(headerRows[0][9]) : headers[0].vi],
            [(headerRows[0][10]): headers[0].regNumber],
            [(headerRows[0][11]): headers[0].regDate],
            [(headerRows[0][12]): headers[0].regDateEnd],
            [(headerRows[0][13]): headers[0].taxBase],
            [(headerRows[0][14]): headers[0].taxBaseOkeiUnit],
            [(headerRows[0][15]): headers[0].createYear],
            [(headerRows[0][16]): headers[0].years],
            [(headerRows[0][17]): headers[0].ownMonths],
            [(headerRows[0][18]): headers[0].partRight],
            [(headerRows[0][19]): headers[0].coefKv],
            [(headerRows[0][20]): headers[0].taxRate],
            [(headerRows[0][21]): headers[0].coefKp],
            [(headerRows[0][22]): headers[0].calculatedTaxSum],
            [(headerRows[0][23]): headers[0].benefitMonths],
            [(headerRows[0][24]): headers[0].coefKl],
            [(headerRows[0][25]): headers[0].taxBenefitCode],
            [(headerRows[1][25]): headers[1].taxBenefitCode],
            [(headerRows[1][26]): headers[1].taxBenefitBase],
            [(headerRows[1][27]): headers[1].taxBenefitSum],
            [(headerRows[0][28]): headers[0].deductionCode],
            [(headerRows[1][28]): headers[1].deductionCode],
            [(headerRows[1][29]): headers[1].deductionSum],
            [(headerRows[0][30]): headers[0].taxSumToPay],
            [(headerRows[0][31]): headers[0].q1],
            [(headerRows[0][32]): headers[0].q2],
            [(headerRows[0][33]): headers[0].q3],
            [(headerRows[0][34]): headers[0].q4],
            [(headerRows[2][0]) : '1']
    ]
    (2..34).each {
        headerMapping.add([(headerRows[2][it]) : it.toString()])
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
 * @param departmentRegionId регион подразделения
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def departmentRegionId) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    def colIndex

    // TODO (Ramil Timerbaev) как восстанавливать значение для подитога первого уровня? в не отображается только кно и кпп
    // графа 2 - атрибут 2102 - TAX_ORGAN_CODE - «Код налогового органа (кон.)», справочник 210 «Параметры представления деклараций по транспортному налогу»
    colIndex = 2
    def record210 = getRecord210(values[colIndex], values[3], values[4], fileRowIndex, colIndex, colOffset)
    newRow.kno = record210?.record_id?.value

    // графа 3 - зависит от графы 2 - не проверяется, потому что используется для нахождения записи для графы 2
    // графа 4 - зависит от графы 2 - не проверяется, потому что используется для нахождения записи для графы 2

    // графа 5 - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
    colIndex = 5
    def record42 = getRecordImport(42, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    newRow.tsTypeCode = record42?.record_id?.value

    // графа 6 - зависит от графы 5 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
    colIndex++
    if (record42) {
        def expectedValues = [record42?.NAME?.value]
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'tsType'), record42?.CODE?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 7
    colIndex++
    newRow.model = values[colIndex]

    // графа 8 - атрибут 400 - CODE - «Код экологического класса», справочник 40 «Экологические классы»
    colIndex++
    newRow.ecoClass = getRecordIdImport(40, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

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

    // графа 14 - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
    colIndex++
    newRow.taxBaseOkeiUnit = getRecordIdImport(12, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 15
    colIndex++
    newRow.createYear = parseDate(values[colIndex], "yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 16
    colIndex++
    newRow.years = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 17
    colIndex++
    newRow.ownMonths = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 18
    colIndex++
    newRow.partRight = values[colIndex]

    // графа 19
    colIndex++
    newRow.coefKv = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // TODO (Ramil Timerbaev) неуникальный атрибут
    // графа 20 - атрибут 416 - VALUE - «Ставка (руб.)», справочник 41 «Ставки транспортного налога»
    colIndex++
    newRow.taxRate = getRecordIdImport(41L, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // TODO (Ramil Timerbaev) неуникальный атрибут
    // графа 21 - атрибут 2093 - COEF - «Повышающий коэффициент», справочник 209 «Повышающие коэффициенты транспортного налога»
    colIndex++
    newRow.coefKp = getRecordIdImport(209L, 'COEF', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 22
    colIndex++
    newRow.calculatedTaxSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 23
    colIndex++
    newRow.benefitMonths = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 24
    colIndex++
    newRow.coefKl = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // TODO (Ramil Timerbaev)
    // графа 25 - атрибут 19.15 - TAX_BENEFIT_ID.CODE - «Код налоговой льготы».«Код», справочник 7.6 «Параметры налоговых льгот транспортного налога».«Коды налоговых льгот и вычетов транспортного налога»
    colIndex++
    if (departmentRegionId && record210?.OKTMO?.value) {
        def record6 = getAllRecords(6L)?.values()?.find { it?.CODE?.value == values[colIndex] }
        def regionId = getRegion(record210?.OKTMO?.value)?.record_id?.value
        def record7 = null
        if (record6) {
            record7 = getAllRecords(7L)?.values()?.find {
                it?.DECLARATION_REGION_ID?.value == departmentRegionId &&
                        it?.DICT_REGION_ID?.value == regionId &&
                        it?.TAX_BENEFIT_ID?.value == record6?.record_id?.value &&
                        it?.BASE?.value == values[colIndex + 1]
            }
        }
        if (record7) {
            newRow.taxBenefitCode = record7?.record_id?.value
        } else {
            // TODO (Ramil Timerbaev) нужно ли это сообщение?
            // 3. Проверка наличия информации о налоговой льготе в справочнике «Параметры налоговых льгот транспортного налога»
            def columnName9 = getColumnName(newRow, 'taxBenefitCode')
            logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. в справочнике " +
                    "«Параметры налоговых льгот транспортного налога» не найдена соответствующая запись",
                    fileRowIndex, getXLSColumnName(colIndex + 1), columnName9)
        }
    }

    // графа 26 - зависит от графы 25 - не проверяется, потому что используется для нахождения записи для графы 9
    colIndex++

    // графа 27
    colIndex++
    newRow.taxBenefitSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 28 - атрибут 15 - CODE - «Код», справочник 6 «Коды налоговых льгот и вычетов транспортного налога»
    colIndex++
    newRow.deductionCode = getRecordIdImport(6L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 29..34
    colIndex = 28
    ['deductionSum', 'taxSumToPay', 'q1', 'q2', 'q3', 'q4'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

// TODO (Ramil Timerbaev)
/**
 * Получить новую итоговую строку нф по значениям из экселя. Строка используется только для получения значении,
 * для вставки в бд сформируются другие строки с нормальными стилями и алиасами.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewTotalRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа fix
    def int colIndex = 0
    newRow.fix = values[colIndex]

    // TODO (Ramil Timerbaev) неуникальный атрибут!
    // графа 2
    colIndex = 2
    def record210 = getRecord210(values[colIndex], values[3], values[4], fileRowIndex, colIndex, colOffset)
    newRow.kno = record210?.record_id?.value

//    // графа 3
//    colIndex++
//    newRow.kpp = values[colIndex]
//
//    // графа 4 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
//    colIndex = 6
//    newRow.okato = getRecordIdImport(96L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 30..34
    colIndex = 29
    ['taxSumToPay', 'q1', 'q2', 'q3', 'q4'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

def getRecord210(def kno, def kpp, def oktmo, def fileRowIndex, def colIndex, def colOffset) {
    def record210 = null
    def record96 = getRecordImport(96, 'CODE', oktmo, fileRowIndex, colIndex + colOffset, false)
    if (record96) {
        def oktmoId = record96?.record_id?.value
        def declarationRegionId = formDataDepartment.regionId
        def regionId = getRegion(oktmoId)?.record_id?.value
        record210 = getAllRecords(210L)?.values()?.find {
            it?.DECLARATION_REGION_ID?.value == declarationRegionId &&
                    it?.REGION_ID?.value == regionId &&
                    it?.TAX_ORGAN_CODE?.value == kno &&
                    it?.KPP?.value == kpp &&
                    it?.OKTMO?.value == oktmoId
        }
        // TODO (Ramil Timerbaev)
        if (!record210) {
            logger.info("========kno = $kno, kpp = $kpp, oktmo = $oktmo")
            logger.info("========declarationRegionId = $declarationRegionId")
            logger.info("========record96 = $record96")
            logger.info("========regionId = $regionId")
            logger.info("========oktmoId = $oktmoId")
            logger.info("========record210 = $record210")
            logger.info("========getAllRecords(210L) = " + getAllRecords(210L))
        }
    }
    return record210
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

// TODO (Ramil Timerbaev) переделать
// Сравнения данных с данными формы предыдущего периода
void comparePrevRows() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // TODO (Ramil Timerbaev) нужнали эта проверка?
    // Логическая проверка 10. Проверка наличия формы предыдущего периода в состоянии «Принята»
    def prevDataRows = getPrevDataRows()
    if (!prevDataRows) {
        def prevReportPeriod = getPrevReportPeriod()
        def periodName = prevReportPeriod?.periodName
        def year = prevReportPeriod?.year
        logger.warn("Не удалось сравнить данные формы с данными формы за предыдущий период. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: %s %s для подразделения «%s»",
                periodName, year, formDataDepartment.name)
    }

    // сброс окрашивания если предыдущих данных нет
    if (!prevDataRows) {
        def rows = dataRows.findAll { !it.getAlias() }
        for (def row : rows) {
            setDefaultStyles(row)
        }
        return
    }

    def currentActualRowsMap = getActualRowsMap(dataRows)
    def prevActualRowsMap = getActualRowsMap(prevDataRows)

    // графа 2..13, 15..19, 21 (25..27)
    def compareColumns = ['department', 'kno', 'kpp', 'kbk', 'okato', 'cadastralNumber', 'landCategory',
            'constructionPhase', 'cadastralCost', 'taxPart', 'ownershipDate', 'terminationDate',
            'benefitCode', /* 'benefitBase', 'benefitParam', */ 'startDate', 'endDate', 'taxRate']
    def reportPeriod = getReportPeriod()
    switch (reportPeriod.order) {
        case 4: compareColumns.add('q3')
        case 3: compareColumns.add('q2')
        case 2: compareColumns.add('q1')
    }

    // сравнение
    currentActualRowsMap.each { groupKey, row ->
        def prevRow = prevActualRowsMap[groupKey]
        if (prevRow == null) {
            // окрасить всю строку
            allColumns.each { alias ->
                row.getCell(alias).setStyleAlias(compareStyleName)
            }
        } else {
            // окрасить ячейки с расхождениями
            for (def alias : compareColumns) {
                if (row[alias] != prevRow[alias]) {
                    row.getCell(alias).setStyleAlias(compareStyleName)
                }
            }

            // сравнение зависимых граф
            def record705 = getRefBookValue(705L, row.benefitCode)
            def prevRecord705 = getRefBookValue(705L, prevRow.benefitCode)

            // графа 16
            def benefitBase1 = getRefBookValue(704L, record705?.TAX_BENEFIT_ID?.value)?.BASE?.value
            def benefitBase2 = getRefBookValue(704L, prevRecord705?.TAX_BENEFIT_ID?.value)?.BASE?.value
            if (benefitBase1 != benefitBase2) {
                row.getCell('benefitBase').setStyleAlias(compareStyleName)
            }

            // графа 17
            def benefitParam1 = record705?.REDUCTION_PARAMS?.value
            def benefitParam2 = prevRecord705?.REDUCTION_PARAMS?.value
            if (benefitParam1 != benefitParam2) {
                row.getCell('benefitParam').setStyleAlias(compareStyleName)
            }
        }
    }
}

// Получить ключ группировки при копировании и сравнении данных из предыдущего периода (по графе 6, 7)
def getGroupKey(def row) {
    return row.okato + '#' + row.cadastralNumber
}

// Получить ключ группировки 1ого уровня (по графе 3, 4)
def getGroupL1Key(def row) {
    return row?.kno + '#' + row?.kpp
}

/**
 * Получить мапу с актуальными строками. Ключ - графа 6 + графа 7, значение - актуальная строка.
 * Если строк несколько, то берется строка с большим значением в графе 12.
 */
def getActualRowsMap(def rows) {
    def map = [:]
    for (def row : rows) {
        if (row.getAlias()) {
            continue
        }
        def key = getGroupKey(row)
        map[key] = (map[key] != null && map[key].ownershipDate > row.ownershipDate) ? map[key] : row
    }
    return map
}

/**
 * Получить итоговую строку.
 *
 * @param level null - итог, 1 - подитого первого уровня, 2 - подитог второго уровня
 */
def creatTotalRow(def level = null) {
    def newRow = createRow()
    newRow.getCell("fix").colSpan = 2
    // TODO (Ramil Timerbaev) возможно надо добавить скрытый столбец перед okato, потому что в подитоговой строке okato не надо отображать
    if (level == 1) {
        newRow.getCell("kpp").colSpan = 2
    }
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    for (def alias : totalColumns) {
        newRow[alias] = BigDecimal.ZERO
    }
    return newRow
}

/** Получить итоговую строку с суммами. */
def calcTotalRow(def dataRows) {
    def newRow = creatTotalRow()
    newRow.setAlias('total')
    newRow.fix = 'итого'
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

/**
 * Добавить промежуточные итоги.
 * По графе 2, 3 (КНО/КПП) - 1 уровень группировки, а внутри этой группы по графе 4 (октмо) - 2 уровнь группировки.
 */
void addAllStatic(def dataRows) {
    for (int i = 0; i < dataRows.size(); i++) {
        def row = getRow(dataRows, i)
        def nextRow = getRow(dataRows, i + 1)
        int j = 0

        // 2 уровнь группировки
        def value2 = row?.okato
        def nextValue2 = nextRow?.okato
        if (row.getAlias() == null && nextRow == null || value2 != nextValue2) {
            def subTotalRow2 = calcSubTotalRow2(i, dataRows, row.kno /*, row.kpp, row.okato*/)
            j++
            dataRows.add(i + j, subTotalRow2)
        }

        // 1 уровнь группировки
        def value1 = getGroupL1Key(row)
        def nextValue1 = getGroupL1Key(nextRow)
        if (row.getAlias() == null && nextRow == null || value1 != nextValue1) {
            // если все значения пустые, то подитог по 2 уровню группировки не добавится,
            // поэтому перед добавлением подитога по 1 уровню группировки, нужно добавить подитог с пустыми значениями по 2 уровню
            if (j == 0) {
                def subTotalRow2 = calcSubTotalRow2(i, dataRows, row.kno /*, row.kpp, row.okato*/)
                j++
                dataRows.add(i + j, subTotalRow2)
            }
            def subTotalRow1 = calcSubTotalRow1(i, dataRows, row.kno /*, row.kpp*/ )
            j++
            dataRows.add(i + j, subTotalRow1)
        }
        i += j  // Обязательно чтобы избежать зацикливания в простановке
    }
}

/** Расчет итога 1 уровня группировки - по графе 2, 3 КНО/КПП. */
def calcSubTotalRow1(int i, def dataRows, def kno /*, def kpp*/) {
    def newRow = creatTotalRow(1)
    newRow.setAlias('total1#' + i)
    newRow.fix = 'Итого по КНО/КПП'

    // значения группы
    newRow.kno = kno
    // newRow.kpp = kpp

    for (int j = i; j >= 0; j--) {
        def srow = getRow(dataRows, j)
        if (srow.getAlias()) {
            continue
        }
        if (newRow.kno != srow.kno || newRow.kpp != srow.kpp) {
            break
        }
        for (def alias : totalColumns) {
            if (srow[alias] != null) {
                newRow[alias] = newRow[alias] + srow[alias]
            }
        }
    }
    return newRow
}

/** Расчет итога 2 уровня группировки - по графе 4 ОКТМО (группировка внутри группы по КНО/КПП). */
def calcSubTotalRow2(int i, def dataRows, def kno /*, def kpp, def okato*/) {
    def newRow = creatTotalRow(2)
    newRow.setAlias("total2#" + i)
    newRow.fix = 'ИТОГО по КНО/КПП/ОКТМО'

    // значения группы
    newRow.kno = kno
//    newRow.kpp = kpp
//    newRow.okato = okato

    // идем от текущей позиции вверх и ищем нужные строки
    for (int j = i; j >= 0; j--) {
        def srow = getRow(dataRows, j)
        if (srow.getAlias() != null || srow.okato != newRow.okato) {
            break
        }
        for (def alias : totalColumns) {
            if (srow[alias] != null) {
                newRow[alias] = newRow[alias] + srow[alias]
            }
        }
    }
    return newRow
}

/** Получение строки по номеру. */
def getRow(def dataRows, int i) {
    if (i < dataRows.size() && i >= 0) {
        return dataRows.get(i)
    } else {
        return null
    }
}

def createRow() {
    return (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
}

def getNewRow() {
    def newRow = createRow()
    setDefaultStyles(newRow)
    return newRow
}

void setDefaultStyles(def row) {
    editableColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
}