package form_template.land.calc_tax_period.v2016

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Расчет земельного налога за отчетные периоды.
 *
 * formTemplateId = 916
 * formTypeId = 916
 *
 * TODO:
 *      - тесты
 */

// графа    - fix
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
        if (preCalcCheck()) {
            calc()
            logicCheck()
            formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        }
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
        copyFromPrevForm()
        calc()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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

@Field
def compareStyleName = 'Сравнение'

@Field
def allColumns = ['fix', 'rowNumber', 'department', 'kno', 'kpp', 'kbk', 'oktmo', 'cadastralNumber',
        'landCategory', 'constructionPhase', 'cadastralCost', 'taxPart', 'ownershipDate', 'terminationDate',
        'period', 'benefitCode', 'benefitBase', 'benefitParam', 'startDate', 'endDate', 'benefitPeriod',
        'taxRate', 'kv', 'kl', 'sum', 'q1', 'q2', 'q3', 'year']

// Редактируемые атрибуты (графа 3, 4, 5, 21)
@Field
def editableColumns = ['kno', 'kpp', 'kbk', 'taxRate']

// Автозаполняемые атрибуты (все кроме редактируемых)
@Field
def autoFillColumns = allColumns - editableColumns - 'fix'

// Проверяемые на пустые значения атрибуты (графа 1..8, 10, 12, 14, 21, 22, 25 (графа 26, 27, 28 обязательны для некоторых периодов))
@Field
def nonEmptyColumns = ['rowNumber', 'department', 'kno', 'kpp', 'kbk', 'oktmo', 'cadastralNumber',
        'landCategory', 'cadastralCost', 'ownershipDate', 'period', 'taxRate', 'kv', 'q1' /*, 'q2', 'q3', 'year'*/]

// графа 3, 4, 6
@Field
def groupColumns = ['kno', 'kpp', 'oktmo']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 25..28)
@Field
def totalColumns = ['q1', 'q2', 'q3', 'year']

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

    // для логической проверки 1
    def nonEmptyColumnsTmp = nonEmptyColumns
    if (getReportPeriod().order == 2) {
        nonEmptyColumnsTmp = nonEmptyColumnsTmp + 'q2'
    } else if (getReportPeriod().order == 3) {
        nonEmptyColumnsTmp = nonEmptyColumnsTmp + 'q2' + 'q3'
    } else if (getReportPeriod().order == 4) {
        nonEmptyColumnsTmp = nonEmptyColumnsTmp + 'q2' + 'q3' + 'year'
    }

    // для логической проверки 13
    def needValue = [:]
    // графа 14, 20, 22..28
    def arithmeticCheckAlias = ['period', /* 'benefitPeriod', */ 'kv', 'kl', 'sum', 'q1', 'q2', 'q3', 'year']

    // для логической проверки 14
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
        checkNonEmptyColumns(row, rowIndex, nonEmptyColumnsTmp, logger, true)

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
        if (tmp != null && !tmp) {
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

        // 11. Проверка наличия в реестре земельных участков с одинаковым кадастровым номером и кодом ОКТМО, периоды владения которых пересекаются
        // сбор данных
        if (row.oktmo && row.cadastralNumber) {
            def groupKey = getRefBookValue(96L, row.oktmo)?.CODE?.value + "#" + row.cadastralNumber
            if (cadastralNumberMap[groupKey] == null) {
                cadastralNumberMap[groupKey] = []
            }
            cadastralNumberMap[groupKey].add(row)
        }

        // 12. Проверка корректности заполнения кода налоговой льготы (графа 15)
        if (row.benefitCode && row.oktmo) {
            def findRecord = allRecords705.find { it?.record_id?.value == row.benefitCode }
            if (findRecord && findRecord?.OKTMO?.value != row.oktmo) {
                def columnName15 = getColumnName(row, 'benefitCode')
                def columnName6 = getColumnName(row, 'oktmo')
                logger.error("Строка %s: Код ОКТМО, в котором действует выбранная в графе «%s» льгота, должен быть равен значению графы «%s»",
                        rowIndex, columnName15, columnName6)
            }
        }

        // 13. Проверка корректности заполнения граф 14, 20, 22-28
        if (!isCalc) {
            needValue.period = calc14(row)
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
                logger.error("Строка %s: Графы «%s» заполнены неверно. Выполните расчет формы", rowIndex, columnNames)
            }

            // проверка для графы 20 отдельная, потому что ее значение надо проверить, хотя сама графа нерасчетная
            tmp = calc20(row)
            if (tmp == null && row.benefitPeriod != null || tmp != null && row.benefitPeriod == null ||
                    tmp.compareTo(row.benefitPeriod) != 0) {
                def columnName = getColumnName(row, 'benefitPeriod')
                logger.error("Строка %s: Графа «%s» заполнена неверно", rowIndex, columnName)
            }
        }

        // 14. Проверка правильности заполнения КПП
        if (row.kpp && records710Map[row.kpp] == null) {
            logger.error("Строка %s: Не найдено ни одного подразделения, для которого на форме настроек подразделений существует запись с КПП равным «%s»",
                    rowIndex, row.kpp)
        }

        // 15. Проверка корректности значения пониженной ставки
        // Выполняется в методе calc24()

        // 16. Проверка корректности значения налоговой базы
        // Выполняется в методе getB()

        // 17. Проверка корректности суммы исчисленного налога и суммы налоговой льготы
        // Выполняется в методе getH()
    }

    // 9. Проверка наличия формы предыдущего периода в состоянии «Принята»
    // Выполняется после консолидации, перед копированием данных, в методе copyFromPrevForm()

    // 10. Проверка наличия формы предыдущего периода в состоянии «Принята»
    // Выполняется после расчетов, перед сравнением данных, в методе comparePrevRows()

    // 11. Проверка наличия в реестре земельных участков с одинаковым кадастровым номером и кодом ОКТМО, периоды владения которых пересекаются
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

    // 18. Проверка корректности значений итоговых строк (строка "ВСЕГО")
    if (!isCalc) {
        def lastSimpleRow = null
        def subTotalMap = [:]
        for (def row : dataRows) {
            if (!row.getAlias()) {
                lastSimpleRow = row

                // подитог кно/кпп/октмо - проверка отсутствия подитога
                def key2 = row.kno + '#' + row.kpp + '#' + row.oktmo
                if (subTotalMap[key2] == null) {
                    def findSubTotal = dataRows.find { it.getAlias()?.startsWith('total2') && it.kno == row.kno && it.kpp == row.kpp && it.oktmo == row.oktmo }
                    subTotalMap[key2] = (findSubTotal != null)
                    if (findSubTotal == null) {
                        def subMsg = getColumnName(row, 'kno') + '=' + (row.kno ?: 'не задан') + ', ' +
                                getColumnName(row, 'kpp') + '=' + (row.kpp ?: 'не задан') + ', ' +
                                getColumnName(row, 'oktmo') + '=' + (getRefBookValue(96L, row.oktmo)?.CODE?.value ?: 'не задан')
                        logger.error(GROUP_WRONG_ITOG, subMsg)
                    }
                }

                // подитог кно/кпп - проверка отсутствия подитога
                def key1 = row.kno + '#' + row.kpp
                if (subTotalMap[key1] == null) {
                    def findSubTotal = dataRows.find { it.getAlias()?.startsWith('total1') && it.kno == row.kno && it.kpp == row.kpp }
                    subTotalMap[key1] = (findSubTotal != null)
                    if (findSubTotal == null) {
                        def subMsg = getColumnName(row, 'kno') + '=' + (row.kno ?: 'не задан') + ', ' +
                                getColumnName(row, 'kpp') + '=' + (row.kpp ?: 'не задан')
                        logger.error(GROUP_WRONG_ITOG, subMsg)
                    }
                }
                continue
            }

            if (row.getAlias() != null && row.getAlias().indexOf('total2') != -1) {
                // подитог кно/кпп/октмо
                // принадлежность подитога к последей простой строке
                if (row.kno != lastSimpleRow.kno || row.kpp != lastSimpleRow.kpp || row.oktmo != lastSimpleRow.oktmo) {
                    logger.error(GROUP_WRONG_ITOG_ROW, row.getIndex())
                    continue
                }
                // проверка сумм
                def srow = calcSubTotalRow2(dataRows.indexOf(row) - 1, dataRows, row.kno, row.kpp, row.oktmo)
                checkTotalRow(row, srow)
            } else if (row.getAlias() != null && row.getAlias().indexOf('total1') != -1) {
                // подитог кно/кпп
                // принадлежность подитога к последей простой строке
                if (row.kno != lastSimpleRow.kno || row.kpp != lastSimpleRow.kpp) {
                    logger.error(GROUP_WRONG_ITOG_ROW, row.getIndex())
                    continue
                }
                // проверка сумм
                def srow = calcSubTotalRow1(dataRows.indexOf(row) - 1, dataRows, row.kno, row.kpp)
                checkTotalRow(row, srow)
            }
            lastSubTotalRow = row
        }

        // строка "ВСЕГО"
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
 * Логическая проверка 6. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок.
 *
 * @param taxPart значение графы 11
 * @return null - если значение taxPart пусто, false если ошибка, true - все нормально
 */
def logicCheck6(def taxPart) {
    if (!taxPart) {
        return null
    }
    def partArray = taxPart?.split('/')

    // 6. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок
    if (partArray.size() == 2 && partArray[1] ==~ /\d{1,}/ && partArray[1].toBigDecimal() == 0) {
        return false
    }
    return true
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить фиксированные строки
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    def showMsg = (formDataEvent == FormDataEvent.CALCULATE)
    for (def row : dataRows) {
        // графа 2
        row.department = calc2(row)
        // графа 5
        row.kbk = calc5(row)
        // графа 14
        row.period = calc14(row)
        // графа 22
        row.kv = calc22(row)
        // графа 23
        row.kl = calc23(row)
        // графа 24
        row.sum = calc24(row, row.kv, row.kl, showMsg)
        // графа 25
        row.q1 = calc25(row, showMsg)
        // графа 26
        row.q2 = calc26(row, showMsg)
        // графа 27
        row.q3 = calc27(row, showMsg)
        // графа 28
        row.year = calc28(row, showMsg)
    }

    // добавить подитоги
    addAllStatic(dataRows)

    // добавить строку "всего"
    dataRows.add(calcTotalRow(dataRows))
    updateIndexes(dataRows)

    // сравение данных с предыдущей формой
    comparePrevRows()
}

def calc2(def row) {
    return (row.department ?: formData.departmentId)
}

def calc5(def row) {
    if (row.kbk) {
        return row.kbk
    }
    def oktmo = (row.oktmo ? getRefBookValue(96L, row.oktmo)?.CODE?.value : null)
    if (!oktmo || oktmo?.size() < 6) {
        return null
    }
    def value = null
    if (oktmo[2] == '3' || oktmo[2] == '9') {
        value = '103'
    } else if (oktmo[2] == '7' && oktmo[5] != '3' && oktmo[5] != '0') {
        value = '204'
    } else if (oktmo[2] == '7' && oktmo[5] == '0') {
        value = '211'
    } else if (oktmo[2] == '7' && oktmo[5] == '3') {
        value = '212'
    } else if (oktmo[5] == '7') {
        value = '305'
    } else if (oktmo[5] == '4') {
        value = '310'
    } else if (oktmo[5] == '1') {
        value = '313'
    }
    if (value == null) {
        return null
    }
    def code = "182 1 06 0603 $value 1000 110".replaceAll(' ', '')
    def records = getAllRecords(703L)
    def record = records.find { it?.CODE?.value == code }
    return record?.record_id?.value
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

def calc24(def row, def value22, def value23, def showMsg = false) {
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
        if (p >= row.taxRate) {
            if (showMsg) {
                // Логическая проверка 15. Проверка корректности значения пониженной ставки
                def columnName21 = getColumnName(row, 'taxRate')
                logger.error("Строка %s: Значение поля «Пониженная ставка, %%» справочника «Параметры налоговых льгот земельного налога» должно быть меньше значения графы «%s»",
                        row.getIndex(), columnName21)
            }
        } else {
            // Графа 24 = А * (Графа 21 – Р) / 100 * (1 – Графа 23)
            tmp = a.multiply(row.taxRate - p).divide(100, precision, BigDecimal.ROUND_HALF_UP).multiply(1 - value23)
        }
    } else if (check5 == 5 && value23 != null) {
        // Графа 24 = A * (1 – Графа 23)
        tmp = a * (1 - value23)
    }
    return round(tmp, 0)
}

def calc25(def row, def showMsg = false) {
    return calc25_27(row, 1, showMsg)
}

def calc26(def row, def showMsg = false) {
    return calc25_27(row, 2, showMsg)
}

def calc27(def row, def showMsg = false) {
    return calc25_27(row, 3, showMsg)
}

def calc25_27(def row, def periodOrder, def showMsg = false) {
    if (getReportPeriod()?.order < periodOrder) {
        return null
    }
    def h = getH(row, periodOrder, showMsg)
    if (h != null) {
        // Графа 25, 26, 27 = ОКРУГЛ(Н/4; 0);
        return h.divide(4, 0, BigDecimal.ROUND_HALF_UP)
    }
    return null
}

def calc28(def row, def showMsg = false) {
    if (getReportPeriod()?.order < 4) {
        return null
    }
    if (row.q1 == null || row.q2 == null || row.q3 == null) {
        return null
    }
    def h = getH(row, 4, showMsg)
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

// мапа с алиасами графы 25..28 (номер периода -> алиас графы)
@Field
def alias25_28Map = [
        1 : 'q1',   // графа 25
        2 : 'q2',   // графа 26
        3 : 'q3',   // графа 27
        4 : 'year', // графа 28
]

/**
 * Налог.
 * Для графы 25..28 необходимо пересчитывать значения используемых столбцов 22, 23, 24 с учетом своего периода.
 *
 * @param row строка нф
 * @param periodOrder порядковый номер периода в налоговом
 * @param showMsg показывать ли сообщение логической проверки 16, 17
 */
def getH(def row, def periodOrder, def showMsg = false) {
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
    def b = getB(row, value23, alias25_28Map[periodOrder], showMsg)
    if (b == null) {
        return null
    }
    int precision = 2 // точность при делении
    // Н = В * Графа 21 * Графа 22 * К / 100 – Графа 24;
    def tmp = b.multiply(row.taxRate).multiply(value22).multiply(k).divide(100, precision, BigDecimal.ROUND_HALF_UP).subtract(value24)

    // Логическая проверка 17. Проверка корректности суммы исчисленного налога и суммы налоговой льготы
    if (tmp < 0) {
        if (showMsg) {
            def columnName = getColumnName(row, alias25_28Map[periodOrder])
            logger.error("Строка %s: Не удалось рассчитать графу «%s», сумма исчисленного налога должна быть " +
                    "больше или равна сумме налоговой льготы. Проверьте исходные данные", row.getIndex(), columnName)
        }
        return null
    }

    // TODO (Ramil Timerbaev)
    if (showMsg) {
        def i = 24 + periodOrder
        logger.info("отладка расчета графы $i строка ${row.getIndex()}: H = В * Графа 21 * Графа 22 * К / 100 – Графа 24 = $b * ${row.taxRate} * $value22 * $k / 100 – $value24 = $tmp")
    }
    return tmp
}

/** Налоговая база. */
def getB(def row, def value23, def alias, def showMsg = false) {
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
        // TODO (Ramil Timerbaev)
        if (showMsg && alias == 'q1') {
            logger.info("отладка расчета графы 25 строка ${row.getIndex()}: В = Графа 10 * Графа 11 – Р = ${row.cadastralCost} * $taxPart – $p = $tmp")
        }
    } else if (check5 == 2 && p != null && value23 != null) {
        tmp = defaultValue - defaultValue * p * (1 - value23)
        // TODO (Ramil Timerbaev)
        if (showMsg && alias == 'q1') {
            logger.info("отладка расчета графы 25 строка ${row.getIndex()}: В = (Графа 10 * Графа 11 – Графа 10 * Графа 11 * Р * (1 – Графа 23)) = (${row.cadastralCost} * $taxPart - ${row.cadastralCost} * $taxPart * $p) * (1 - $value23) = $tmp")
        }
    } else if (check5 == 0 || check5 != 1 || check5 != 2) {
        tmp = defaultValue
        // TODO (Ramil Timerbaev)
        if (showMsg && alias == 'q1') {
            logger.info("отладка расчета графы 25 строка ${row.getIndex()}: В = Графа 10 * Графа 11 = ${row.cadastralCost} * $taxPart = $tmp")
        }
    }

    // Логическая проверка 16. Проверка корректности значения налоговой базы
    if (tmp < 0) {
        if (showMsg) {
            def columnName = getColumnName(row, alias)
            logger.error("Строка %s: Не удалось рассчитать графу «%s», значение налоговой базы должно быть больше 0. " +
                    "Проверьте исходные данные", row.getIndex(), columnName)
        }
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
    int COLUMN_COUNT = 29
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

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
        // пропуск итоговой строки
        if (rowValues[INDEX_FOR_SKIP]?.trim()?.equalsIgnoreCase("всего")) {
            // получить значения итоговой строки из файла
            rowIndex++
            totalRowFromFileMap[rowIndex] = getNewTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // пропуск подитоговых строк
        if (rowValues[INDEX_FOR_SKIP]?.trim()?.toLowerCase()?.contains("итого")) {
            // сформировать и подсчитать подитоги
            def subTotalRow
            if (rowValues[INDEX_FOR_SKIP]?.trim()?.toLowerCase()?.contains("итого по ")) {
                subTotalRow = calcSubTotalRow1(rowIndex - 1, rows, rowValues[3], rowValues[4])
            } else {
                def oktmo = (rowValues[6] ? getRecordIdImport(96L, 'CODE', rowValues[6], fileRowIndex, 6 + colOffset) : null)
                subTotalRow = calcSubTotalRow2(rowIndex - 1, rows, rowValues[3], rowValues[4], oktmo)
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
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, hasRegion)
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
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping = [
            [(headerRows[0][0]) : headers[0].fix],
            [(headerRows[0][2]) : headers[0].department],
            [(headerRows[0][3]) : headers[0].kno],
            [(headerRows[0][4]) : headers[0].kpp],
            [(headerRows[0][5]) : headers[0].kbk],
            [(headerRows[0][6]) : headers[0].oktmo],
            [(headerRows[0][7]) : headers[0].cadastralNumber],
            [(headerRows[0][8]) : headers[0].landCategory],
            [(headerRows[0][9]) : headers[0].constructionPhase],
            [(headerRows[0][10]): headers[0].cadastralCost],
            [(headerRows[0][11]): headers[0].taxPart],
            [(headerRows[0][12]): headers[0].ownershipDate],
            [(headerRows[0][13]): headers[0].terminationDate],
            [(headerRows[0][14]): headers[0].period],
            [(headerRows[0][15]): headers[0].benefitCode],
            [(headerRows[1][15]): headers[1].benefitCode],
            [(headerRows[1][16]): headers[1].benefitBase],
            [(headerRows[1][17]): headers[1].benefitParam],
            [(headerRows[1][18]): headers[1].startDate],
            [(headerRows[1][19]): headers[1].endDate],
            [(headerRows[1][20]): headers[1].benefitPeriod],
            [(headerRows[0][21]): headers[0].taxRate],
            [(headerRows[0][22]): headers[0].kv],
            [(headerRows[0][23]): headers[0].kl],
            [(headerRows[0][24]): headers[0].sum],
            [(headerRows[0][25]): headers[0].q1],
            [(headerRows[1][25]): headers[1].q1],
            [(headerRows[1][26]): headers[1].q2],
            [(headerRows[1][27]): headers[1].q3],
            [(headerRows[1][28]): headers[1].year],
            [(headerRows[2][0]) : '1']
    ]
    (2..28).each {
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
 * @param hasRegion признак необходимости заполнения графы 15, 16, 17
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def hasRegion) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2 - атрибут 161 - NAME - «Наименование подразделения», справочник 30 «Подразделения»
    def int colIndex = 2
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
        colIndex = 15
        def record704 = (values[6] ? getRecordImport(704, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset) : null)
        def code = record704?.record_id?.value
        def oktmo = newRow.oktmo
        def param = values[17] ?: null
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
    colIndex = 18
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

    // графа 3
    colIndex = 3
    newRow.kno = values[colIndex]

    // графа 4
    colIndex = 4
    newRow.kpp = values[colIndex]

    // графа 6 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
    colIndex = 6
    newRow.oktmo = getRecordIdImport(96L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 25..28
    colIndex = 24
    [ 'q1', 'q2', 'q3', 'year'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
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

// Предрасчетные проверки
def preCalcCheck() {
    // 1. Проверка заполнения атрибута «Регион»
    if (formDataDepartment.regionId == null) {
        logger.error("В справочнике «Подразделения» не заполнено поле «Регион» для подразделения «%s»", formDataDepartment.name)
        return false
    }
    return true
}

// значение группируемых столбцов для сортировки подитоговых строк
def getSortValue(def row) {
    return row.kno + '#' + row.kpp + '#' + (row.oktmo ? getRefBookValue(96L, row.oktmo) : '')
}

@Field
def prevDataRows = null

/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    if (prevDataRows != null) {
        return prevDataRows
    }
    if (getPrevReportPeriod()?.period == null) {
        prevDataRows = []
        return prevDataRows
    }
    def prevFormData = formDataService.getFormDataPrev(formData)
    prevDataRows = (prevFormData?.state == WorkflowState.ACCEPTED ? formDataService.getDataRowHelper(prevFormData)?.allSaved : [])
    return prevDataRows
}

@Field
def prevReportPeriodMap = null

/**
 * Получить предыдущий отчетный период
 *
 * @return мапа с данными предыдущего периода:
 *      period - период (может быть null, если предыдущего периода нет);
 *      periodName - название;
 *      year - год;
 */
def getPrevReportPeriod() {
    if (prevReportPeriodMap != null) {
        return prevReportPeriodMap
    }
    def reportPeriod = getReportPeriod()
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def find = false
    // предыдущий период в том же году, что и текущий, и номера периодов отличаются на единицу
    if (prevReportPeriod && reportPeriod.order > 1 && reportPeriod.order - 1 == prevReportPeriod.order &&
            reportPeriod.taxPeriod.year == prevReportPeriod.taxPeriod.year) {
        find = true
    }
    // если текущий период первый в налоговом периоде, то предыдущий период должен быть последним, и года налоговых периодов должны отличаться на единицу
    if (!find && prevReportPeriod && reportPeriod.order == 1 && prevReportPeriod.order == 4 &&
            reportPeriod.taxPeriod.year - 1 == prevReportPeriod.taxPeriod.year) {
        find = true
    }
    prevReportPeriodMap = [:]
    if (find) {
        prevReportPeriodMap.period = prevReportPeriod
        prevReportPeriodMap.periodName = prevReportPeriod.name
        prevReportPeriodMap.year = prevReportPeriod.taxPeriod.year
    } else {
        // получение названии периодов
        def filter = 'L = 1'
        def provider = formDataService.getRefBookProvider(refBookFactory, 8L, providerCache)
        def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        records?.sort { it?.END_DATE?.value }

        prevReportPeriodMap.period = null
        prevReportPeriodMap.periodName = records[reportPeriod.order - 2].NAME?.value
        prevReportPeriodMap.year = (reportPeriod.order == 1 ? reportPeriod.taxPeriod.year - 1 : reportPeriod.taxPeriod.year)
    }
    return prevReportPeriodMap
}

/** Копирование данных из формы предыдущего периода. */
void copyFromPrevForm() {
    // Логическая проверка 9. Проверка наличия формы предыдущего периода в состоянии «Принята»
    def prevDataRows = getPrevDataRows()
    if (!prevDataRows) {
        def prevReportPeriod = getPrevReportPeriod()
        def periodName = prevReportPeriod?.periodName
        def year = prevReportPeriod?.year
        logger.warn("Данные по земельным участкам из предыдущего отчетного периода не были скопированы. " +
                "В Системе отсутствует форма за период: %s %s для подразделения «%s»",
                periodName, year, formDataDepartment.name)
    }

    // разложить строки формы предыдущего периода по мапе (ключ - графа 6 и графа 7, значение - список строк)
    def prevRowsMap = getActualRowsMap(prevDataRows)

    // отбор нужных строк и заполнение данных
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        if (row.getAlias()) {
            continue
        }
        def key = getGroupKey(row)
        def findRow = prevRowsMap[key]

        // графа 2
        row.department = (findRow?.department == null ? formData.departmentId : findRow.department)
        // графа 3
        row.kno = findRow?.kno
        // графа 4
        row.kpp = findRow?.kpp
        // графа 21
        row.taxRate = (findRow?.taxRate == null ? round((BigDecimal) 1.5, 4) : findRow?.taxRate)
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
def sourceTypeId = 912

void consolidation() {
    // графа 6..13, 15..20
    def consolidationColumns = ['oktmo', 'cadastralNumber', 'landCategory', 'constructionPhase',
            'cadastralCost', 'taxPart', 'ownershipDate', 'terminationDate', 'benefitCode',
            'benefitBase', 'benefitParam', 'startDate', 'endDate', 'benefitPeriod']

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
        sourceDataRows.each { sourceRow ->
            def newRow = getNewRow()
            consolidationColumns.each { alias ->
                newRow[alias] = sourceRow[alias]
            }
            dataRows.add(newRow)
        }
    }
    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

// Сравнения данных с данными формы предыдущего периода
void comparePrevRows() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

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
    def compareColumns = ['department', 'kno', 'kpp', 'kbk', 'oktmo', 'cadastralNumber', 'landCategory',
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
        }
    }
}

// Получить ключ группировки при копировании и сравнении данных из предыдущего периода (по графе 6, 7)
def getGroupKey(def row) {
    return row.oktmo + '#' + row.cadastralNumber
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

/** Получить итоговую строку. */
def getTotalRow() {
    def newRow = createRow()
    newRow.getCell("fix").colSpan = 3
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
    def newRow = getTotalRow()
    newRow.setAlias('total')
    newRow.fix = 'ВСЕГО'
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

/**
 * Добавить промежуточные итоги.
 * По графе 3, 4 (КНО/КПП) - 1 уровень группировки, а внутри этой группы по графе 6 (октмо) - 2 уровнь группировки.
 */
void addAllStatic(def dataRows) {
    for (int i = 0; i < dataRows.size(); i++) {
        def row = getRow(dataRows, i)
        def nextRow = getRow(dataRows, i + 1)
        int j = 0

        // 2 уровнь группировки
        def value2 = row?.oktmo
        def nextValue2 = nextRow?.oktmo
        if (row.getAlias() == null && nextRow == null || value2 != nextValue2) {
            def subTotalRow2 = calcSubTotalRow2(i, dataRows, row.kno, row.kpp, row.oktmo)
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
                def subTotalRow2 = calcSubTotalRow2(i, dataRows, row.kno, row.kpp, row.oktmo)
                j++
                dataRows.add(i + j, subTotalRow2)
            }
            def subTotalRow1 = calcSubTotalRow1(i, dataRows, row.kno, row.kpp)
            j++
            dataRows.add(i + j, subTotalRow1)
        }
        i += j  // Обязательно чтобы избежать зацикливания в простановке
    }
}

/** Расчет итога 1 уровня группировки - по графе 3, 4 КНО/КПП. */
def calcSubTotalRow1(int i, def dataRows, def kno, def kpp) {
    def newRow = getTotalRow()
    newRow.setAlias('total1#' + i)
    newRow.fix = 'ИТОГО ПО КНО/КПП'

    // значения группы
    newRow.kno = kno
    newRow.kpp = kpp

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

/** Расчет итога 2 уровня группировки - по графе 6 ОКТМО (группировка внутри группы по КНО/КПП). */
def calcSubTotalRow2(int i, def dataRows, def kno, def kpp, def oktmo) {
    def newRow = getTotalRow()
    newRow.setAlias("total2#" + i)
    newRow.fix = 'ИТОГО'

    // значения группы
    newRow.kno = kno
    newRow.kpp = kpp
    newRow.oktmo = oktmo

    // идем от текущей позиции вверх и ищем нужные строки
    for (int j = i; j >= 0; j--) {
        def srow = getRow(dataRows, j)
        if (srow.getAlias() != null || srow.oktmo != newRow.oktmo) {
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