package form_template.land.calc_tax_period.v2016

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
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
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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
        'taxRate', 'kv', 'kl', 'sum', 'q1', 'q2', 'q3', 'year', 'name']

// Редактируемые атрибуты (графа 2, 3, 4, 5, 21)
@Field
def editableColumns = ['department', 'kno', 'kpp', 'kbk', 'taxRate']

// Автозаполняемые атрибуты (все кроме редактируемых)
@Field
def autoFillColumns = allColumns - editableColumns - 'fix'

// Проверяемые на пустые значения атрибуты (графа 1..8, 10, 12, 14, 21, 22, 25 (графа 26, 27, 28 обязательны для некоторых периодов))
@Field
def nonEmptyColumns = ['rowNumber', 'department', 'kno', 'kpp', 'kbk', 'oktmo', 'cadastralNumber',
        'landCategory', 'cadastralCost', 'ownershipDate', 'period', 'taxRate', 'kv' /*, 'q1', 'q2', 'q3', 'year'*/]

// графа 3, 4, 6
@Field
def groupColumns = ['kno', 'kpp', 'oktmo']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 25..28)
@Field
def totalColumns = null

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
    def allRecords705 = (dataRows.size() > 0 ? getAllRecords2(705L) : null)

    // для логической проверки 2
    def nonEmptyColumnsTmp = nonEmptyColumns + getTotalColumns()

    // для логической проверки 14
    def needValue = [:]
    // графа 14, 20, 22..28
    def arithmeticCheckAlias = ['period', 'benefitPeriod', 'kv', 'kl', 'sum', 'q1', 'q2', 'q3', 'year']

    // для логической проверки 15
    def knoKppMap = [:]

    // 1. Проверка заполнения поля «Регион» справочника «Подразделения» для подразделения формы
    if (formDataDepartment.regionId == null) {
        logger.error("В справочнике «Подразделения» не заполнено поле «Регион» для подразделения «%s»", formDataDepartment.name)
        return
    }

    for (def row : dataRows) {
        if (row.getAlias()) {
            continue
        }
        def rowIndex = row.getIndex()

        // 2. Проверка обязательности заполнения граф
        checkNonEmptyColumns(row, rowIndex, nonEmptyColumnsTmp, logger, true)

        // 3. Проверка одновременного заполнения данных о налоговой льготе
        def value15 = (row.benefitCode ? true : false)
        def value18 = (row.startDate ? true : false)
        if (value15 ^ value18) {
            logger.error("Строка %s: Данные о налоговой льготе указаны не полностью", rowIndex)
        }

        // 4. Проверка корректности заполнения даты возникновения права собственности
        if (row.ownershipDate != null && row.ownershipDate > getReportPeriodEndDate()) {
            def columnName12 = getColumnName(row, 'ownershipDate')
            def dateStr = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно %s", rowIndex, columnName12, dateStr)
        }

        // 5. Проверка корректности заполнения даты прекращения права собственности
        if (row.terminationDate != null && (row.terminationDate < row.ownershipDate || row.terminationDate < startYearDate)) {
            def columnName13 = getColumnName(row, 'terminationDate')
            def dateStr = '01.01.' + year
            def columnName12 = getColumnName(row, 'ownershipDate')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значению графы «%s»",
                    rowIndex, columnName13, dateStr, columnName12)
        }

        // 6. Проверка доли налогоплательщика в праве на земельный участок
        def tmp = logicCheck6(row.taxPart)
        if (tmp != null && tmp.isEmpty()) {
            def columnName11 = getColumnName(row, 'taxPart')
            logger.error("Строка %s: Графа «%s» должна быть заполнена согласно формату: " +
                    "«(от 1 до 10 числовых знаков) / (от 1 до 10 числовых знаков)», " +
                    "без лидирующих нулей в числителе и знаменателе, числитель должен быть меньше либо равен знаменателю",
                    rowIndex, columnName11)
        }

        // 7. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок
        tmp = logicCheck7(row.taxPart)
        if (tmp != null && !tmp) {
            def columnName11 = getColumnName(row, 'taxPart')
            logger.error("Строка %s: Значение знаменателя в графе «%s» не может быть равным нулю", rowIndex, columnName11)
        }

        // 8. Проверка корректности заполнения даты начала действия льготы
        if (row.startDate && row.ownershipDate && row.startDate < row.ownershipDate) {
            def columnName18 = getColumnName(row, 'startDate')
            def columnName12 = getColumnName(row, 'ownershipDate')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»",
                    rowIndex, columnName18, columnName12)
        }

        // 9. Проверка корректности заполнения даты окончания действия льготы
        if (row.benefitCode && row.startDate) {
            if (row.terminationDate) {
                if (row.endDate == null || row.endDate < row.startDate || row.endDate > row.terminationDate) {
                    def columnName19 = getColumnName(row, 'endDate')
                    def columnName18 = getColumnName(row, 'startDate')
                    def columnName13 = getColumnName(row, 'terminationDate')
                    logger.error("Строка %s: Графа «%s» должна быть заполнена. Значение графы должно быть больше либо равно значению графы «%s» и быть меньше либо равно значению графы «%s»",
                            rowIndex, columnName19, columnName18, columnName13)
                }
            } else if (row.endDate && row.endDate < row.startDate) {
                def columnName19 = getColumnName(row, 'endDate')
                def columnName18 = getColumnName(row, 'startDate')
                logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s»",
                        rowIndex, columnName19, columnName18)
            }
        }

        // 12. Проверка наличия в реестре земельных участков с одинаковым кадастровым номером и кодом ОКТМО, периоды владения которых пересекаются
        // сбор данных
        if (row.oktmo && row.cadastralNumber) {
            def groupKey = getRefBookValue(96L, row.oktmo)?.CODE?.value + "#" + row.cadastralNumber
            if (cadastralNumberMap[groupKey] == null) {
                cadastralNumberMap[groupKey] = []
            }
            cadastralNumberMap[groupKey].add(row)
        }

        // 13. Проверка корректности заполнения кода налоговой льготы (графа 15)
        if (row.benefitCode && row.oktmo) {
            def findRecord = allRecords705.find { it?.record_id?.value == row.benefitCode }
            if (findRecord && findRecord?.OKTMO?.value != row.oktmo) {
                def columnName15 = getColumnName(row, 'benefitCode')
                def columnName6 = getColumnName(row, 'oktmo')
                logger.error("Строка %s: Код ОКТМО, в котором действует выбранная в графе «%s» льгота, должен быть равен значению графы «%s»",
                        rowIndex, columnName15, columnName6)
            }
        }

        // 14. Проверка корректности заполнения граф 14, 20, 22-28
        needValue.period = calc14(row)
        needValue.benefitPeriod = calc20(row)
        needValue.kv = calc22(row)
        needValue.kl = calc23(row)
        needValue.sum = calc24(row, row.kv, row.kl, true)
        needValue.q1 = calc25(row, true)
        needValue.q2 = calc26(row, true)
        needValue.q3 = calc27(row, true)
        needValue.year = calc28(row, true)
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

        // 15. Проверка заполнения формы настроек подразделений
        // сбор данных
        if (row.kno && row.kpp) {
            def groupKey = row.kno + "#" + row.kpp
            if (knoKppMap[groupKey] == null) {
                knoKppMap[groupKey] = []
            }
            knoKppMap[groupKey].add(row)
        }

        // 16. Проверка корректности значения пониженной ставки
        // Выполняется в методе calc24()

        // 17. Проверка корректности значения налоговой базы
        // Выполняется в методе getB()

        // 18. Проверка корректности суммы исчисленного налога и суммы налоговой льготы
        // Выполняется в методе getN()
    }

    // 10. Проверка наличия формы предыдущего периода в состоянии «Принята»
    // Выполняется после консолидации, перед копированием данных, в методе copyFromPrevForm()

    // 11. Проверка наличия формы предыдущего периода в состоянии «Принята»
    // Выполняется после расчетов, перед сравнением данных, в методе comparePrevRows()

    // 12. Проверка наличия в реестре земельных участков с одинаковым кадастровым номером и кодом ОКТМО, периоды владения которых пересекаются
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

    // 15. Проверка заполнения формы настроек подразделений
    if (!knoKppMap.isEmpty()) {
        def groupKeys = knoKppMap.keySet().toList()
        for (def groupKey : groupKeys) {
            def rows = knoKppMap[groupKey]
            def row = rows[0]
            def find = getRecord710(row.kno, row.kpp)
            if (!find) {
                def rowIndexes = rows?.collect { it.getIndex() }
                def value3 = row.kno
                def value4 = row.kpp
                logger.error("Строки %s: На форме настроек подразделений отсутствует запись с «Код налогового органа (кон.) = %s» и «КПП = %s»",
                        rowIndexes?.join(', '), value3, value4)
            }
        }
    }

    // 19. Проверка корректности значений итоговых строк (строка "ВСЕГО")
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
    for (def column : getTotalColumns()) {
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
 * Логическая проверка 6. Проверка доли налогоплательщика в праве на земельный участок.
 *
 * @param taxPart значение графы 11
 * @return null - если значение taxPart пусто, пустой список - если ошибка, список из двух элементов (числитель и знаменатель) - все нормально
 */
def logicCheck6(def taxPart) {
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
 * Логическая проверка 7. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок.
 *
 * @param taxPart значение графы 11
 * @return null - если значение taxPart пусто, false если ошибка, true - все нормально
 */
def logicCheck7(def taxPart) {
    if (!taxPart) {
        return null
    }
    def partArray = taxPart?.split('/')

    // 7. Проверка значения знаменателя доли налогоплательщика в праве на земельный участок
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
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { groupColumns.contains(it.getAlias())})
    sortRows(dataRows, groupColumns)

    for (def row : dataRows) {
        // графа 2
        row.department = calc2(row)
        // графа 5
        row.kbk = calc5(row)
        // графа 14
        row.period = calc14(row)
        // графа 20
        row.benefitPeriod = calc20(row)
        // графа 22
        row.kv = calc22(row)
        // графа 23
        row.kl = calc23(row)
        // графа 24
        row.sum = calc24(row, row.kv, row.kl)
        // графа 25
        row.q1 = calc25(row)
        // графа 26
        row.q2 = calc26(row)
        // графа 27
        row.q3 = calc27(row)
        // графа 28
        row.year = calc28(row)
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
    def records = getAllRecords2(703L)
    def record = records.find { it?.CODE?.value == code }
    return record?.record_id?.value
}

def calc14(def row, def periodOrder = null) {
    if (row.ownershipDate == null) {
        return null
    }
    def dates = getPeriodDates(periodOrder)
    def periodStart = dates?.get(0)
    def periodEnd = dates?.get(1)
    BigDecimal tmp
    if (row.terminationDate && row.terminationDate < periodStart || row.ownershipDate > periodEnd) {
        tmp = BigDecimal.ZERO
    } else {
        def end = (row.terminationDate == null || row.terminationDate > periodEnd ? periodEnd : row.terminationDate)
        def start = (row.ownershipDate < periodStart ? periodStart : row.ownershipDate)
        def startM = start.format('M').toInteger() + (start.format('d').toInteger() > 15 ? 1 : 0)
        def endM = end.format('M').toInteger() - (end.format('d').toInteger() > 15 ? 0 : 1)
        tmp = endM - startM + 1
    }
    return round(tmp, 0)
}

// метод взят из формы "Реестр земельных участков" (form_template.land.land_registry.v2016) - там это calc16()
def calc20(def row, def periodOrder = null) {
    if (row.startDate == null) {
        return null
    }
    def dates = getPeriodDates(periodOrder)
    def periodStart = dates?.get(0)
    def periodEnd = dates?.get(1)
    BigDecimal tmp
    if (row.endDate && row.endDate < periodStart || row.startDate > periodEnd) {
        tmp = BigDecimal.ZERO
    } else {
        // 1
        def a = row.ownershipDate?.format('M')?.toInteger()
        if (row.ownershipDate < periodStart) {
            a = periodStart.format('M').toInteger()
        } else if (row.ownershipDate?.format('d')?.toInteger() > 15) {
            a = a + 1
        }
        def b = row.terminationDate?.format('M')?.toInteger()
        if (row.terminationDate != null && row.terminationDate < periodStart) {
            b = periodStart.format('M').toInteger()
        }
        def c = periodStart.format('M').toInteger()
        def startM = [a, b, c].max()

        // 2
        a = null
        if (row.terminationDate == null || row.terminationDate > periodEnd) {
            a = periodEnd.format('M').toInteger()
        } else if (row.terminationDate?.format('d')?.toInteger() > 15) {
            a = row.terminationDate.format('M').toInteger()
        } else {
            a = row.terminationDate.format('M').toInteger() - 1
        }
        b = row.endDate?.format('M')?.toInteger()
        if (row.endDate == null || row.endDate > periodEnd) {
            b = periodEnd.format('M').toInteger()
        }
        c = periodEnd.format('M').toInteger()
        def endM = [a, b, c].grep().min()

        tmp = endM - startM + 1
    }
    return round(tmp, 0)
}

def calc22(def row, def periodOrder = null) {
    BigDecimal tmpPeriod = calc14(row, periodOrder)
    if (tmpPeriod == null) {
        return null
    }
    // Графа 22 = ОКРУГЛ(Графа 14/12; 4)
    return tmpPeriod.divide(12, 4, BigDecimal.ROUND_HALF_UP)
}

// мапа с датами граничных значении периодов
@Field
def datesMap = [:]

/** Получить даты начала и окончания периода по номеру периода в году. */
def getPeriodDates(def periodOrder = null) {
    def period = (periodOrder ?: getReportPeriod()?.order)
    if (datesMap.isEmpty()) {
        def year = getReportPeriod()?.taxPeriod?.year
        def format = 'dd.MM.yyyy'
        datesMap[1] = [ Date.parse(format, '01.01.' + year), Date.parse(format, '31.03.' + year) ]
        datesMap[2] = [ Date.parse(format, '01.04.' + year), Date.parse(format, '30.06.' + year) ]
        datesMap[3] = [ Date.parse(format, '01.07.' + year), Date.parse(format, '30.09.' + year) ]
        datesMap[4] = [ Date.parse(format, '01.01.' + year), Date.parse(format, '31.12.' + year) ]
    }
    return datesMap[period]
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
    def termUse = calc20(row, periodOrder)
    if (termUse == null) {
        return null
    }
    def record705 = getRefBookValue(705L, row.benefitCode)
    def record704Id = record705?.TAX_BENEFIT_ID?.value
    def code15 = getRefBookValue(704L, record704Id)?.CODE?.value
    if (code15 == '3022300' || code15 == '3022400' || code15 == '3029000') {
        def value14 = (periodOrder != null && getReportPeriod()?.order != periodOrder ? calc14(row, periodOrder) : row.period)
        if (value14 == null) {
            return null
        }
        BigDecimal tmp = (value14 - termUse) / value14
        return round(tmp, 4)
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
    // B = ОКРУГЛ(ОКРУГЛ(Графа 10;0) * Графа 11;0);
    def b = round(round(row.cadastralCost, 0) * taxPart, 0)
    def k = getK(row)
    int precision = 0 // точность при делении
    // A - сумма исчисленного налога (сумма налога без учета суммы льготы)
    // A = ОКРУГЛ(B * Графа 21 * Графа 22 * К / 100;0);
    def a = b.multiply(row.taxRate).multiply(value22).multiply(k).divide(100, precision, BigDecimal.ROUND_HALF_UP)

    def record705 = getRefBookValue(705L, row.benefitCode)
    def record704Id = record705?.TAX_BENEFIT_ID?.value
    def code15 = getRefBookValue(704L, record704Id)?.CODE?.value
    def check15 = getCheckValue15(code15)
    if (!check15) {
        return null
    }
    def p = getP(code15, record705)

    BigDecimal tmp = null
    if (check15 == 1) {
        tmp = p
    } else if (check15 == 2 && p != null) {
        if (value23 != null) {
            // Графа 24 = ОКРУГЛ(B * Р * (1 – Графа 23);0);
            tmp = round(b.multiply(p).multiply(1 - value23), 0)
        }
    } else if (check15 == 3 && p != null) {
        // Графа 24 = ОКРУГЛ(А * Р / 100;0);
        tmp = a.multiply(p).divide(100, precision, BigDecimal.ROUND_HALF_UP)
    } else if (check15 == 4 && p != null) {
        if (p >= row.taxRate) {
            if (showMsg) {
                // Логическая проверка 16. Проверка корректности значения пониженной ставки
                def columnName21 = getColumnName(row, 'taxRate')
                logger.error("Строка %s: Значение поля «Пониженная ставка, %%» справочника «Параметры налоговых льгот земельного налога» должно быть меньше значения графы «%s»",
                        row.getIndex(), columnName21)
            }
        } else {
            // Графа 24 = ОКРУГЛ(B * (Графа 21 – Р) / 100;0);
            tmp = b.multiply(row.taxRate - p).divide(100, precision, BigDecimal.ROUND_HALF_UP)
        }
    } else if (check15 == 5 && value23 != null) {
        // Графа 24 = ОКРУГЛ(A * (1 – Графа 23);0)
        tmp = round(a * (1 - value23), 0)
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

    def find = getRecord710(row.kno, row.kpp)
    if (find && getRefBookValue(38L, find?.PREPAYMENT?.value)?.CODE?.value == 0) {
        return round(BigDecimal.ZERO, 0)
    }

    if (row.terminationDate && getReportPeriodStartDate() <= row.terminationDate &&
            row.terminationDate <= getReportPeriodEndDate() && getReportPeriod()?.order != 4) {
        // графа 25 - для всех периодов одинаково расчитывается
        if (periodOrder == 1) {
            return getN(row, periodOrder, showMsg)
        }
        // графа 26 - форма 2 кв
        if (getReportPeriod()?.order == 2 && periodOrder == 2) {
            def value25 = getN(row, 1, showMsg)
            def nYear = getN(row, 4, showMsg)
            if (nYear == null || value25 == null) {
                return null
            }
            return nYear - value25
        }
        // графа 26 - форма 3 кв
        if (getReportPeriod()?.order == 3 && periodOrder == 2) {
            return getN(row, periodOrder, showMsg)
        }
        // графа 27 - форма 3 кв
        if (getReportPeriod()?.order == 2 && periodOrder == 2) {
            def value25 = getN(row, 1, showMsg)
            def value26 = getN(row, 2, showMsg)
            def nYear = getN(row, 4, showMsg)
            if (nYear == null || value25 == null || value26 == null) {
                return null
            }
            return nYear - value25 - value26
        }
    }

    return getN(row, periodOrder, showMsg)
}

def calc28(def row, def showMsg = false) {
    if (getReportPeriod()?.order < 4) {
        return null
    }
    def n = getN(row, 4, showMsg)

    def find = getRecord710(row.kno, row.kpp)
    if (find && getRefBookValue(38L, find?.PREPAYMENT?.value)?.CODE?.value == 0) {
        return n
    }

    if (row.q1 == null || row.q2 == null || row.q3 == null) {
        return null
    }
    if (n != null) {
        BigDecimal tmp = n - row.q1 - row.q2 - row.q3
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
def getN(def row, def periodOrder, def showMsg = false) {
    if (row.taxRate == null) {
        return null
    }

    def value22 = (getReportPeriod()?.order != periodOrder ? calc22(row, periodOrder) : row.kv)
    if (value22 == null) {
        return null
    }

    def value23 = (getReportPeriod()?.order != periodOrder ? calc23(row, periodOrder) : row.kl)

    def value24 = null
    def record705 = getRefBookValue(705L, row.benefitCode)
    def record704Id = record705?.TAX_BENEFIT_ID?.value
    def code15 = getRefBookValue(704L, record704Id)?.CODE?.value
    def check15 = getCheckValue15(code15)
    if (check15 != 1 && check15 != 2) {
        value24 = (getReportPeriod()?.order != periodOrder ? calc24(row, value22, value23) : row.sum)
    }
    if (value24 == null) {
        value24 = BigDecimal.ZERO
    }

    def k = getK(row)
    def b = getB(row, value23, alias25_28Map[periodOrder], showMsg)
    if (b == null) {
        return null
    }
    int precision = 0 // точность при делении
    // Н = В * Графа 21 * Графа 22 * К / 100 – Графа 24;
    def tmp = b.multiply(row.taxRate).multiply(value22).multiply(k).divide(100, precision, BigDecimal.ROUND_HALF_UP).subtract(value24)

    // Логическая проверка 18. Проверка корректности суммы исчисленного налога и суммы налоговой льготы
    if (tmp < 0) {
        if (showMsg) {
            def columnName = getColumnName(row, alias25_28Map[periodOrder])
            logger.warn("Строка %s: Графа «%s», сумма налоговой льготы больше суммы исчисленного налога. Проверьте исходные данные",
                    row.getIndex(), columnName)
        }
        tmp = BigDecimal.ZERO
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
    def check15 = getCheckValue15(code15)
    def p = getP(code15, record705)

    BigDecimal tmp = null
    BigDecimal defaultValue = round(row.cadastralCost, 0) * taxPart
    if (check15 == 1 && p != null) {
        tmp = defaultValue - p
        tmp = (tmp < 0 ? 0 : tmp)
    } else if (check15 == 2 && p != null) {
        if (value23 != null) {
            tmp = defaultValue - defaultValue * p * (1 - value23)
        }
    } else if (check15 == 0 || check15 != 1 || check15 != 2) {
        tmp = defaultValue
    }

    // Логическая проверка 17. Проверка корректности значения налоговой базы
    if (tmp < 0) {
        if (showMsg) {
            def columnName = getColumnName(row, alias)
            logger.error("Строка %s: Не удалось рассчитать графу «%s», значение налоговой базы должно быть больше 0. " +
                    "Проверьте исходные данные", row.getIndex(), columnName)
        }
        return null
    }
    return round(tmp, 0)
}

@Field
def checkValue15Map = [:]

// условия проверки кода графы 15
def getCheckValue15(def code15) {
    if (checkValue15Map[code15]) {
        return checkValue15Map[code15]
    }
    def tmp = 0
    if (code15 == '3022100') {
        tmp = 1
    } else if (code15 == '3022300') {
        tmp = 2
    } else if (code15 == '3022200') {
        tmp = 3
    } else if (code15 == '3022500') {
        tmp = 4
    } else if (code15 == '3022400' || code15 == '3029000') {
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
    def result6 = logicCheck6(value)
    def result7 = logicCheck7(value)
    if (!result6 || !result7) {
        return null
    }
    int precision = 20 // точность при делении
    return result6[0].divide(result6[1], precision, BigDecimal.ROUND_HALF_UP)
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 30
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

    // 1. Проверка заполнения поля «Регион» справочника «Подразделения» для подразделения формы
    def hasRegion = (formDataDepartment.regionId != null)
    if (!hasRegion) {
        def columnName15 = getColumnName(tmpRow, 'benefitCode')
        def columnName16 = getColumnName(tmpRow, 'benefitBase')
        def columnName17 = getColumnName(tmpRow, 'benefitParam')
        logger.warn("Не удалось заполнить графы «%s», «%s», «%s», т.к. для подразделения формы не заполнено поле «Регион» справочника «Подразделения»",
                columnName15, columnName16, columnName17)
    }

    // заполнить кэш данными из справочника ОКТМО
    def limitRows = 10
    if (allValuesCount > limitRows) {
        fillRefBookCache(96L)
        fillRecordCache(96L, 'CODE', getReportPeriodEndDate())
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
            compareTotalValues(totalFromFile, total, getTotalColumns(), logger, 0, false)
            // задание значении итоговой строке нф из итоговой строки файла (потому что в строках из файла стили для простых строк)
            total.setImportIndex(totalFromFile.getImportIndex())
            (getTotalColumns() + 'fix').each { alias ->
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
            [(headerRows[0][29]): headers[1].name],
            [(headerRows[2][0]) : '1']
    ]
    (2..29).each {
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
        // 2. Проверка заполнения кода ОКТМО
        // проверка графы 6
        def xlsColumnName6 = getXLSColumnName(colIndex + colOffset)
        def columnName15 = getColumnName(newRow, 'benefitCode')
        def columnName16 = getColumnName(newRow, 'benefitBase')
        def columnName17 = getColumnName(newRow, 'benefitParam')
        def columnName6 = getColumnName(newRow, 'oktmo')
        logger.warn("Строка %s, столбец %s: Не удалось заполнить графы «%s», «%s», «%s», т.к. не заполнена графа «%s»",
                fileRowIndex, xlsColumnName6, columnName15, columnName16, columnName17, columnName6)
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
    colIndex++
    if (hasRegion && (values[colIndex] || values[colIndex + 1] || values[colIndex + 2])) {
        // графа 15 - атрибут 7053 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 705 «Параметры налоговых льгот земельного налога»
        def record704 = (values[6] ? getRecordImport(704, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false) : null)
        def code = record704?.record_id?.value
        def oktmo = newRow.oktmo
        def param = values[17] ?: null
        def record705 = getRecord705Import(code, oktmo, param)
        newRow.benefitCode = record705?.record_id?.value
        if (values[6] && record705 == null) {
            // 3. Проверка наличия информации о налоговой льготе в  справочнике «Параметры налоговых льгот земельного налога»
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

    // графа 29
    colIndex++
    newRow.name = values[colIndex]

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
    // Логическая проверка 10. Проверка наличия формы предыдущего периода в состоянии «Принята»
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
    def allRecords = getAllRecords2(705L)
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

def getAllRecords2(def refbookId) {
    if (allRecordsMap[refbookId] == null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, refbookId, providerCache)
        def date = (refbookId == 710L ? getReportPeriodEndDate() - 1 : getReportPeriodEndDate())
        allRecordsMap[refbookId] = provider.getRecords(date, null, null, null)
    }
    return allRecordsMap[refbookId]
}

@Field
def sourceTypeId = 912

void consolidation() {
    // графа 6..13, 15..19, 29
    def consolidationColumns = ['oktmo', 'cadastralNumber', 'landCategory', 'constructionPhase',
            'cadastralCost', 'taxPart', 'ownershipDate', 'terminationDate', 'benefitCode',
            'benefitBase', 'benefitParam', 'startDate', 'endDate', 'name']

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

    // Логическая проверка 11. Проверка наличия формы предыдущего периода в состоянии «Принята»
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
    def compareColumns = ['department', 'kno', 'kpp', /* 'kbk', 'oktmo', */ 'cadastralNumber',
            /* 'landCategory', 'constructionPhase', */ 'cadastralCost', 'taxPart', 'ownershipDate', 'terminationDate',
            /* 'benefitCode', 'benefitBase', 'benefitParam', */ 'startDate', 'endDate', 'taxRate']
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

            // графа 5
            def kbk1 = getRefBookValue(703L, row.kbk)?.CODE?.value
            def kbk2 = getRefBookValue(703L, prevRow.kbk)?.CODE?.value
            if (kbk1 != kbk2) {
                row.getCell('kbk').setStyleAlias(compareStyleName)
            }

            // графа 6
            def oktmo1 = getRefBookValue(96L, row.oktmo)?.CODE?.value
            def oktmo2 = getRefBookValue(96L, prevRow.oktmo)?.CODE?.value
            if (oktmo1 != oktmo2) {
                row.getCell('oktmo').setStyleAlias(compareStyleName)
            }

            // графа 8
            def landCategory1 = getRefBookValue(702L, row.landCategory)?.CODE?.value
            def landCategory2 = getRefBookValue(702L, prevRow.landCategory)?.CODE?.value
            if (landCategory1 != landCategory2) {
                row.getCell('landCategory').setStyleAlias(compareStyleName)
            }

            // графа 9
            def constructionPhase1 = getRefBookValue(701L, row.constructionPhase)?.CODE?.value
            def constructionPhase2 = getRefBookValue(701L, prevRow.constructionPhase)?.CODE?.value
            if (constructionPhase1 != constructionPhase2) {
                row.getCell('constructionPhase').setStyleAlias(compareStyleName)
            }

            // сравнение зависимых граф
            def record705 = getRefBookValue(705L, row.benefitCode)
            def prevRecord705 = getRefBookValue(705L, prevRow.benefitCode)

            // графа 15
            def benefitCode1 = getRefBookValue(704L, record705?.TAX_BENEFIT_ID?.value)?.CODE?.value
            def benefitCode2 = getRefBookValue(704L, prevRecord705?.TAX_BENEFIT_ID?.value)?.CODE?.value
            if (benefitCode1 != benefitCode2) {
                row.getCell('benefitCode').setStyleAlias(compareStyleName)
            }

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
    for (def alias : getTotalColumns()) {
        newRow[alias] = BigDecimal.ZERO
    }
    return newRow
}

/** Получить итоговую строку с суммами. */
def calcTotalRow(def dataRows) {
    def newRow = getTotalRow()
    newRow.setAlias('total')
    newRow.fix = 'ВСЕГО'
    calcTotalSum(dataRows, newRow, getTotalColumns())
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
        for (def alias : getTotalColumns()) {
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
        for (def alias : getTotalColumns()) {
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

/** Заполнить refBookCache всеми записями справочника refBookId. */
void fillRefBookCache(def refBookId) {
    def records = getAllRecords2(refBookId)
    for (def record : records) {
        def recordId = record?.record_id?.value
        def key = getRefBookCacheKey(refBookId, recordId)
        if (refBookCache[key] == null) {
            refBookCache.put(key, record)
        }
    }
}

/**
 * Заполнить recordCache всеми записями справочника refBookId из refBookCache.
 *
 * @param refBookId идентификатор справочника
 * @param alias алиас атрибута справочника по которому будет осуществляться поиск
 * @param date дата по которой будет осуществляться поиск
 */
void fillRecordCache(def refBookId, def alias, def date) {
    def keys = refBookCache.keySet().toList()
    def needKeys = keys.findAll { it.contains(refBookId + SEPARATOR) }
    def dateSts = date.format('dd.MM.yyyy')
    def rb = refBookFactory.get(refBookId)
    for (def needKey : needKeys) {
        def recordId = refBookCache[needKey]?.record_id?.value
        def value = refBookCache[needKey][alias]?.value
        def filter = getFilter(alias, value, rb)
        def key = dateSts + filter
        if (recordCache[refBookId] == null) {
            recordCache[refBookId] = [:]
        }
        recordCache[refBookId][key] = recordId
    }
}

/**
 * Формирование фильтра. Взято из FormDataServiceImpl.getRefBookRecord(...)
 *
 * @param alias алиас атрибута справочника по которому будет осуществляться поиск
 * @param value значение атрибута справочника
 * @param rb справочник
 */
def getFilter(def alias, def value, def rb) {
    def filter
    if (value == null || value.isEmpty()) {
        filter = alias + " is null"
    } else {
        RefBookAttributeType type = rb.getAttribute(alias).getAttributeType()
        String template
        // TODO: поиск по выражениям с датами не реализован
        if (type == RefBookAttributeType.REFERENCE || type == RefBookAttributeType.NUMBER) {
            if (!isNumeric(value)) {
                // В справочнике поле числовое, а у нас строка, которая не парсится — ничего не ищем выдаем ошибку
                return null
            }
            template = "%s = %s"
        } else {
            template = "LOWER(%s) = LOWER('%s')"
        }
        filter = String.format(template, alias, value)
    }
    return filter
}

boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?")
}

def getRecord710(def kno, def kpp) {
    def departmentParam = getDepartmentParam()
    return (departmentParam ? getDepartmentParamTable(departmentParam?.record_id?.value, kno, kpp) : null)
}

def getTotalColumns() {
    if (totalColumns == null) {
        def order = getReportPeriod().order
        if (order == 1) {
            totalColumns = ['q1']
        } else if (order == 2) {
            totalColumns = ['q1', 'q2']
        } else if (order == 3) {
            totalColumns = ['q1', 'q2', 'q3']
        } else if (order == 4) {
            totalColumns = ['q1', 'q2', 'q3', 'year']
        }
    }
    return totalColumns
}

@Field
def departmentParam = null

// Получить параметры подразделения (из справочника 700)
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = formDataDepartment.id
        def provider = formDataService.getRefBookProvider(refBookFactory, 700L, providerCache)
        def departmentParamList = provider.getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            return null
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

@Field
def departmentParamTableMap = [:]

// Получить параметры подразделения (из справочника 710)
def getDepartmentParamTable(def departmentParamId, def kno, def kpp) {
    def key = departmentParamId + '#' + kno + '#' + kpp
    if (departmentParamTableMap[key] == null) {
        def filter = "LINK = $departmentParamId and TAX_ORGAN_CODE ='$kno' and KPP ='$kpp'"
        def provider = formDataService.getRefBookProvider(refBookFactory, 710L, providerCache)
        def departmentParamTableList = provider.getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            return null
        }
        departmentParamTableMap[key] = departmentParamTableList.get(0)
    }
    return departmentParamTableMap[key]
}