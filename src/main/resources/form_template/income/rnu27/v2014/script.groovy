package form_template.income.rnu27.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import groovy.transform.Field

/**
 * (РНУ-27) Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных
 *              и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения
 * formTemplateId=1326
 *
 * ЧТЗ http://conf.aplana.com/pages/viewpage.action?pageId=8588102 ЧТЗ_сводные_НФ_Ф2_Э1_т2.doc
 *
 * @author ekuvshinov
 * @author lhaziev
 */

// графа 1  - number
// графа    - fix
// графа 2  - issuer                    - зависит от графы 3 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
// графа 3  - regNumber                 - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочник 84 «Ценные бумаги»
// графа 4  - tradeNumber
// графа 5  - currency                  - зависит от графы 3 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
// графа 6  - prev
// графа 7  - current
// графа 8  - reserveCalcValuePrev
// графа 9  - cost
// графа 10 - signSecurity              - зависит от графы 3 - атрибут 869 - SIGN - «Признак ценной бумаги», справочник 84 «Ценные бумаги»
// графа 11 - marketQuotation
// графа 12 - rubCourse                 - абсолюбтное значение поля «Курс валюты» справочника «Курсы валют» валюты из «Графы 5» отчетную дату
// графа 13 - marketQuotationInRub
// графа 14 - costOnMarketQuotation
// графа 15 - reserveCalcValue
// графа 16 - reserveCreation
// графа 17 - recovery

// Признак консолидированной формы
@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        def columns = editableColumns
        if (isBalancePeriod()) {
            columns = allColumns - ['number', 'issuer', 'currency', 'signSecurity']
        }
        formDataService.addRow(formData, currentDataRow, columns, autoFillColumns)
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
        logicCheck()
        break
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED: // после принятия из подготовлена
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

/**
 * Кэши и константы
 */

@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// все атрибуты
@Field
// fix не убирать, стиль на итоги не вешается иначе
def allColumns = ['number', 'fix', 'issuer', 'regNumber', 'tradeNumber', 'currency', 'prev', 'current',
        'reserveCalcValuePrev', 'cost', 'signSecurity', 'marketQuotation', 'rubCourse', 'marketQuotationInRub',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// Редактируемые атрибуты (графа 3, 4, 6, 7, 9, 11, 12)
@Field
def editableColumns = ['regNumber', 'tradeNumber', 'prev', 'current', 'cost', 'marketQuotation', 'rubCourse']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты (графа 2, 3, 4)
@Field
def groupColumns = ['issuer', 'regNumber', 'tradeNumber']

// Проверяемые на пустые значения атрибуты (графа 1..5, 8, 13..17)
@Field
def nonEmptyColumns = ['number', /*'issuer',*/ 'regNumber', 'tradeNumber', /*'currency',*/ 'reserveCalcValuePrev',
        'marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 6..9, 14..17)
@Field
def totalColumns = ['prev', 'current', 'reserveCalcValuePrev', 'cost',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// алиасы графов для арифметической проверки (графа )
@Field
def arithmeticCheckAlias = ['reserveCalcValuePrev', 'marketQuotation', 'rubCourse', 'marketQuotationInRub',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// Дата окончания отчетного периода
@Field
def endDate = null

// Признак периода ввода остатков
@Field
def isBalancePeriod = null

/**
 * Обертки методов
 */
// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    if (!isConsolidated && !isBalancePeriod()) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, true, logger, true)
    }
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    // данные предыдущего отчетного периода
    def dataPrevRows = null
    if (!isConsolidated) {
        def formPrev = getFormPrev()
        def dataPrev = formPrev != null ? formDataService.getDataRowHelper(formPrev) : null
        dataPrevRows = dataPrev?.allCached
    }

    // номер последний строки предыдущей формы
    def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

    for (row in dataRows) {
        row.number = ++number
        if (!isBalancePeriod() && !isConsolidated) {
            row.reserveCalcValuePrev = calc8(row, dataPrevRows)
            row.marketQuotation = calc11(row)
            row.rubCourse = calc12(row)
            row.marketQuotationInRub = calc13(row)
            row.costOnMarketQuotation = calc14(row)
            row.reserveCalcValue = calc15(row)
            row.reserveCreation = calc16(row)
            row.recovery = calc17(row)
        }
    }

    // добавить промежуточные итоги (по графе 2 - эмитент, а внутри этой группы по графе 3 - грн)
    addAllStatic(dataRows)

    // добавить строку "итого"
    dataRows.add(getCalcTotalRow(dataRows))

    // используется save() т.к. есть сортировка
    dataRowHelper.save(dataRows)
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def formPrev = null
    def dataPrev = null
    def dataPrevRows = null
    if (!isConsolidated) {
        formPrev = getFormPrev()
        dataPrev = formPrev != null ? formDataService.getDataRowHelper(formPrev) : null
        dataPrevRows = dataPrev?.allCached
    }

    def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    for (DataRow row in dataRows) {
        if (row?.getAlias() == null) {
            number++
            def index = row.getIndex()
            def errorMsg = "Строка ${index}: "

            if (row.current == 0) {
                // 4. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.reserveCalcValuePrev != row.recovery) {
                    logger.warn(errorMsg + "Графы 8 и 17 неравны!")
                }
                // 5. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0) {
                    logger.warn(errorMsg + "Графы 9, 14 и 15 ненулевые!")
                }
            }
            // 6. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6 = 0)
            if (row.prev == 0 && (row.reserveCalcValuePrev != 0 || row.recovery != 0)) {
                loggerError(errorMsg + "Графы 8 и 17 ненулевые!")
            }
            // 7. Проверка необращающихся облигаций (графа 10 = «x»)
            if (getSign(row.regNumber) == "-" && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                logger.warn(errorMsg + "Облигации необращающиеся, графы 15 и 16 ненулевые!")
            }
            if (row.reserveCalcValue != null && row.reserveCalcValuePrev != null &&
                    getSign(row.regNumber) == "+") {
                // 8. Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev > 0 && row.recovery != 0) {
                    loggerError(errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
                // 9. Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev < 0 && row.reserveCreation != 0) {
                    loggerError(errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
                // 10. Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev == 0 && (row.reserveCreation != 0 || row.recovery != 0)) {
                    loggerError(errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
            }
            // 11. Проверка корректности формирования резерва
            if (row.reserveCalcValuePrev != null && row.reserveCreation != null && row.reserveCalcValue != null && row.recovery != null
                    && row.reserveCalcValuePrev + row.reserveCreation != row.reserveCalcValue + row.recovery) {
                loggerError(errorMsg + "Резерв сформирован неверно!")
            }
            // 12. Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && (row.current <= 0 || row.cost <= 0 || row.costOnMarketQuotation <= 0 || row.reserveCalcValue <= 0)) {
                logger.warn(errorMsg + "Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!")
            }
            // 13. Проверка корректности заполнения РНУ
            if (!isConsolidated && formPrev != null) {
                for (DataRow rowPrev in dataPrevRows) {
                    if (rowPrev.getAlias() == null && row.tradeNumber == rowPrev.tradeNumber && row.prev != rowPrev.current) {
                        logger.warn(errorMsg + "РНУ сформирован некорректно! Не выполняется условие: Если «графа 4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то «графа 6» = «графа 7» формы РНУ-27 за предыдущий отчётный период")
                    }
                }
            }
            // 14. Проверка корректности заполнения РНУ
            if (!isConsolidated && formPrev != null) {
                for (DataRow rowPrev in dataPrevRows) {
                    if (rowPrev.getAlias() == null && row.tradeNumber == rowPrev.tradeNumber && row.reserveCalcValuePrev != rowPrev.reserveCalcValue) {
                        loggerError(errorMsg + "РНУ сформирован некорректно! Не выполняется условие: Если  «графа 4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то графа 8 = графа 15 формы РНУ-27 за предыдущий отчётный период")
                    }
                }
            }

            // 1. Проверка на заполнение поля «<Наименование поля>»
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

            // 2. Проверка на уникальность поля «№ пп»
            if (number != row.number) {
                logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
            }

            if (getCurrencyName(row.regNumber) == 'RUR') {
                // 17. Проверка графы 11
                if (row.marketQuotation != null) {
                    loggerError(errorMsg + "Неверно заполнена графа «Рыночная котировка одной ценной бумаги в иностранной валюте»!")
                }
                // 18. Проверка графы 12
                if (row.rubCourse != null) {
                    loggerError(errorMsg + "Неверно заполнена графы «Курс рубля к валюте рыночной котировки»!")
                }
            }

            if (!isConsolidated && !isBalancePeriod()) {
                // 19. Арифметические проверки граф 5, 8, 11, 12, 13, 14, 15, 16, 17
                def calcValues = [
                        reserveCalcValuePrev: calc8(row, dataPrevRows),
                        marketQuotation: calc11(row),
                        rubCourse: calc12(row),
                        marketQuotationInRub: calc13(row),
                        costOnMarketQuotation: calc14(row),
                        reserveCalcValue: calc15(row),
                        reserveCreation: calc16(row),
                        recovery: calc17(row)
                ]
                checkCalc(row, arithmeticCheckAlias, calcValues, logger, true)
            }
        }

        // LC 20
        if (row.getAlias() != null && row.getAlias().indexOf('itogoRegNumber') != -1) {
            srow = calcItogRegNumber(dataRows.indexOf(row))

            for (column in totalColumns) {
                if (row.get(column) != srow.get(column)) {
                    def tmpRow = getPrevRowWithoutAlias(dataRows, row)
                    def regNumber = getRefBookValue(84, tmpRow?.regNumber)?.REG_NUM?.value
                    loggerError("Итоговые значения по «$regNumber» рассчитаны неверно в графе «${getColumnName(row, column)}»!")
                }
            }
        }

        // LC 21
        if (row.getAlias() != null && row.getAlias().indexOf('itogoIssuer') != -1) {
            srow = calcItogIssuer(dataRows.indexOf(row))

            for (column in totalColumns) {
                if (row.get(column) != srow.get(column)) {
                    def tmpRow = getPrevRowWithoutAlias(dataRows, row)
                    def issuer = getIssuerName(tmpRow?.regNumber)
                    loggerError("Итоговые значения для «$issuer» рассчитаны неверно в графе «${getColumnName(row, column)}»!")
                }
            }
        }
    }

    // LC • Проверка корректности заполнения РНУ
    if (!isConsolidated && dataPrev != null && checkAlias(dataPrevRows, 'total') && checkAlias(dataRows, 'total')) {
        def itogoPrev = getDataRow(dataPrevRows, 'total')
        def itogo = getDataRow(dataRows, 'total')
        // 13.
        if (itogo != null && itogoPrev != null && itogo.prev != itogoPrev.current) {
            loggerError("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе 6 = «Итого» по графе 7 формы РНУ-27 за предыдущий отчётный период")
        }
        // 14.
        if (itogo != null && itogoPrev != null && itogo.reserveCalcValuePrev != itogoPrev.reserveCalcValue) {
            loggerError("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе 8 = «Итого» по графе 15 формы РНУ-27 за предыдущий отчётный период")
        }
    }

    // 22. Проверка итоговых значений по всей форме
    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod())

    /** 3. LC Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде (выполняется один раз для всего экземпляра)
     * http://jira.aplana.com/browse/SBRFACCTAX-2609
     */
    if (!isConsolidated && dataPrev != null) {
        List notFound = []
        List foundMany = []
        if (dataPrevRows != null) {
            for (DataRow rowPrev in dataPrevRows) {
                if (rowPrev.getAlias() == null && rowPrev.reserveCalcValue > 0) {
                    int count = 0
                    for (DataRow row in dataRows) {
                        if (row.getAlias() == null && row.tradeNumber == rowPrev.tradeNumber) {
                            count++
                        }
                    }
                    if (count == 0) {
                        notFound.add(rowPrev.tradeNumber)
                    } else if (count > 1) {
                        foundMany.add(rowPrev.tradeNumber)
                    }
                }
            }
        }
        if (!notFound.isEmpty()) {
            logger.warn("Отсутствуют строки с номерами сделок: ${notFound.join(', ')}")
        }
        if (!foundMany.isEmpty()) {
            logger.warn("Существует несколько строк с номерами сделок: \${foundMany.join(', ')}")
        }
    }
}

// Импорт данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'number'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 17, 2)
    def headerMapping = [
            (xml.row[0].cell[0]) : getColumnName(tmpRow, 'number'),
            (xml.row[0].cell[2]) : getColumnName(tmpRow, 'issuer'),
            (xml.row[0].cell[3]) : getColumnName(tmpRow, 'regNumber'),
            (xml.row[0].cell[4]) : getColumnName(tmpRow, 'tradeNumber'),
            (xml.row[0].cell[5]) : getColumnName(tmpRow, 'currency'),
            (xml.row[0].cell[6]) : getColumnName(tmpRow, 'prev'),
            (xml.row[0].cell[7]) : getColumnName(tmpRow, 'current'),
            (xml.row[0].cell[8]) : getColumnName(tmpRow, 'reserveCalcValuePrev'),
            (xml.row[0].cell[9]) : getColumnName(tmpRow, 'cost'),
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'signSecurity'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'marketQuotation'),
            (xml.row[0].cell[12]): getColumnName(tmpRow, 'rubCourse'),
            (xml.row[0].cell[13]): getColumnName(tmpRow, 'marketQuotationInRub'),
            (xml.row[0].cell[14]): getColumnName(tmpRow, 'costOnMarketQuotation'),
            (xml.row[0].cell[15]): getColumnName(tmpRow, 'reserveCalcValue'),
            (xml.row[0].cell[16]): getColumnName(tmpRow, 'reserveCreation'),
            (xml.row[0].cell[17]): getColumnName(tmpRow, 'recovery'),
            (xml.row[1].cell[0]) : '1'
    ]
    (2..17).each { index ->
        headerMapping.put((xml.row[1].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

/** Ищем вверх по форме первую строку без альяса. */
DataRow getPrevRowWithoutAlias(def dataRows, DataRow row) {
    int pos = dataRows.indexOf(row)
    for (int i = pos; i >= 0; i--) {
        if (getRow(dataRows, i).getAlias() == null) {
            return row
        }
    }
    throw new IllegalArgumentException()
}

/**
 * Заполнить форму данными.
 *
 * @param xml - xml с данными
 * @param headRowCount - количество строк шапки, указывается как n
 */
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []

    for (def row : xml.row) {
        xmlIndexRow++

        /* Пропуск строк шапок */
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = formData.createDataRow()
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def int xlsIndexRow = xmlIndexRow + rowOffset
        def xmlIndexCol

        /* Графа 1 */
        xmlIndexCol = 0
        newRow.number = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // Графа 3 - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочник 84 «Ценные бумаги»
        // TODO (Ramil Timerbaev) могут быть проблемы с нахождением записи,
        // если в справочнике 84 есть несколько записей с одинаковыми значениями в поле REG_NUM
        xmlIndexCol = 3
        def record84 = getRecordImport(84, 'REG_NUM', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)
        newRow.regNumber = record84?.record_id?.value
        if (newRow.regNumber == null) {
            newRow.regNumber = 181182184 as BigDecimal
        }

        // Графа 2 - зависит от графы 3 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
        xmlIndexCol = 2
        def record100 = getRecordImport(100, 'FULL_NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        if (record84 != null && record100 != null) {
            def value1 = record100?.record_id?.value?.toString()
            def value2 = record84?.ISSUER?.value?.toString()
            formDataService.checkReferenceValue(84, value1, value2, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }

        /* Графа 4 */
        newRow.tradeNumber = row.cell[4].text()

        // Графа 5 - зависит от графы 3 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
        xmlIndexCol = 5
        def record15 = getRecordImport(15, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        if (record84 != null && record15 != null) {
            def value1 = record15?.record_id?.value?.toString()
            def value2 = record84?.CODE_CUR?.value?.toString()
            formDataService.checkReferenceValue(84, value1, value2, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }

        /* Графа 6 */
        xmlIndexCol = 6
        newRow.prev = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        /* Графа 7 */
        xmlIndexCol = 7
        newRow.current = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        /* Графа 8 */
        xmlIndexCol = 8
        newRow.reserveCalcValuePrev = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        /* Графа 9 */
        xmlIndexCol = 9
        newRow.cost = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // Графа 10 - зависит от графы 3 - атрибут 869 - SIGN - «Признак ценной бумаги», справочник 84 «Ценные бумаги»
        xmlIndexCol = 10
        def record62 = getRecordImport(62, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        if (record84 != null && record62 != null) {
            def value1 = record62?.record_id?.value?.toString()
            def value2 = record84?.SIGN?.value?.toString()
            formDataService.checkReferenceValue(84, value1, value2, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }

        /* Графа 11 */
        xmlIndexCol = 11
        newRow.marketQuotation = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        /* Графа 12 */
        xmlIndexCol = 12
        newRow.rubCourse = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        /* Графа 14 */
        xmlIndexCol = 14
        newRow.costOnMarketQuotation = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        /* Графа 15 */
        xmlIndexCol = 15
        newRow.reserveCalcValue = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        /* Графа 16 */
        xmlIndexCol = 16
        newRow.reserveCreation = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        /* Графа 17 */
        xmlIndexCol = 17
        newRow.recovery = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

/**
 * Проставляет статические строки.
 * Добавить промежуточные итоги (по графе 2 - эмитент, а внутри этой группы по графе 3 - грн)
 */
void addAllStatic(def dataRows) {
    if (dataRows == null) {
        dataRows = formDataService.getDataRowHelper(formData)?.allCached
    }
    for (int i = 0; i < dataRows.size(); i++) {
        DataRow<Cell> row = getRow(dataRows, i)
        DataRow<Cell> nextRow = getRow(dataRows, i + 1)
        int j = 0

        // графа 3 - грн - regNumber
        def regNum = getRefBookValue(84, row?.regNumber)?.REG_NUM?.value
        def nextRegNum = getRefBookValue(84, nextRow?.regNumber)?.REG_NUM?.value
        if (row.getAlias() == null && nextRow == null || regNum != nextRegNum) {
            def itogRegNumberRow = calcItogRegNumber(i)
            dataRows.add(i + 1, itogRegNumberRow)
            j++
        }

        // графа 2 - эмитент - issuer
        def issuer = getIssuerName(row?.regNumber)
        def nextIssuer = getIssuerName(nextRow?.regNumber)
        if (row.getAlias() == null && nextRow == null || issuer != nextIssuer) {
            def itogIssuerRow = calcItogIssuer(i)
            dataRows.add(i + 2, itogIssuerRow)
            j++
        }
        i += j  // Обязательно чтобы избежать зацикливания в простановке
    }
}

/** Расчет итога Эмитента. */
def calcItogIssuer(int i) {
    def newRow = formData.createDataRow()
    newRow.getCell('fix').colSpan = 3
    newRow.setAlias('itogoIssuer#'.concat(i ? i.toString() : ""))
    setTotalStyle(newRow)

    String tIssuer = 'Эмитент'
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    for (int j = i; j >= 0; j--) {
        if (getRow(dataRows, j).getAlias() == null) {
            tIssuer = getIssuerName(getRow(dataRows, j).regNumber)
            break
        }
    }

    newRow.fix = tIssuer + ' Итог'

    for (column in totalColumns) {
        newRow.getCell(column).setValue(new BigDecimal(0), null)
    }

    for (int j = i; j >= 0; j--) {
        def srow = getRow(dataRows, j)

        if (srow.getAlias() == null) {
            def issuerName = getIssuerName(getRow(dataRows, j).regNumber)
            if (issuerName != tIssuer) {
                break
            }

            for (column in totalColumns) {
                if (srow.get(column) != null) {
                    def value = newRow.getCell(column).value + (BigDecimal) srow.getCell(column).value
                    newRow.getCell(column).setValue(value, null)
                }
            }
        }

    }
    return newRow
}

/** Расчет итога ГРН. */
def calcItogRegNumber(int i) {
    // создаем итоговую строку ГРН
    def newRow = formData.createDataRow()
    newRow.getCell('fix').colSpan = 3
    newRow.setAlias('itogoRegNumber#'.concat(i ? i.toString() : ""))
    setTotalStyle(newRow)

    String tRegNumber = 'ГРН'
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    for (int j = i; j >= 0; j--) {
        if (getRow(dataRows, j).getAlias() == null) {
            tRegNumber = getRefBookValue(84, getRow(dataRows, j).regNumber)?.REG_NUM?.value
            break
        }
    }

    newRow.fix = tRegNumber + ' Итог'

    for (column in totalColumns) {
        newRow.getCell(column).setValue(new BigDecimal(0), null)
    }

    // идем от текущей позиции вверх и ищем нужные строки
    for (int j = i; j >= 0; j--) {
        def srow = getRow(dataRows, j)

        if (srow.getAlias() == null) {
            if (getRefBookValue(84, srow.regNumber)?.REG_NUM?.value != tRegNumber) {
                break
            }

            for (column in totalColumns) {
                if (srow[column] != null) {
                    def value = newRow.getCell(column).value + (BigDecimal) srow.getCell(column).value
                    newRow.getCell(column).setValue(value, null)
                }
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

BigDecimal calc8(DataRow row, def dataPrevRows) {
    if (isConsolidated) {
        return row.reserveCalcValuePrev
    }
    // Расчет графы 8 в соответсвие коментарию Аванесова http://jira.aplana.com/browse/SBRFACCTAX-2562
    def tmp = BigDecimal.ZERO
    tempCount = 0

    if (dataPrevRows != null) {
        for (DataRow rowPrev in dataPrevRows) {
            if (row.tradeNumber == rowPrev.tradeNumber) {
                tmp = rowPrev.reserveCalcValue
                tempCount++
            }
        }
    }
    tmp = (tempCount == 1 ? tmp : BigDecimal.ZERO)
    return roundValue(tmp, 2)
}

BigDecimal calc11(DataRow row) {
    if (getCurrencyName(row.regNumber)?.contains("RUR")) {
        return null
    }
    return row.marketQuotation
}

BigDecimal calc12(DataRow row) {
    if (getCurrencyName(row.regNumber)?.contains("RUR")) {
        return null
    }
    return row.rubCourse
}

BigDecimal calc13(DataRow row) {
    if (row.marketQuotation != null && row.rubCourse != null) {
        return roundValue((BigDecimal) (row.marketQuotation * row.rubCourse), 2)
    } else {
        return null
    }
}

BigDecimal calc14(DataRow row) {
    def tmp = null
    if (row.marketQuotationInRub == null) {
        tmp = BigDecimal.ZERO
    } else if (row.current != null && row.marketQuotationInRub != null) {
        tmp = row.current * row.marketQuotationInRub
    }
    return roundValue(tmp, 2)
}

BigDecimal calc15(DataRow row) {
    def tmp = BigDecimal.ZERO
    if (row.regNumber != null && getSign(row.regNumber) == '+') {
        if (row.costOnMarketQuotation == null) {
            tmp = null
        } else {
            BigDecimal a = (row.cost ?: 0)
            if (a - row.costOnMarketQuotation > 0) {
                tmp = a - row.costOnMarketQuotation
            }
        }
    }
    return roundValue(tmp, 2)
}

BigDecimal calc16(DataRow row) {
    def tmp = null
    if (row.reserveCalcValue != null && row.reserveCalcValuePrev != null) {
        if (row.reserveCalcValue - row.reserveCalcValuePrev > 0) {
            tmp = (row.marketQuotation ?: 0) - (row.prev ?: 0)
        } else {
            tmp = BigDecimal.ZERO
        }
    }
    return roundValue(tmp, 2)

}

BigDecimal calc17(DataRow row) {
    if (row.reserveCalcValue != null && row.reserveCalcValuePrev != null) {
        BigDecimal a = 0
        if (row.reserveCalcValue - row.reserveCalcValuePrev < 0) {
            a = row.reserveCalcValue - row.reserveCalcValuePrev
        }
        return roundValue(a.abs(), 2)
    } else {
        return null
    }
}

BigDecimal roundValue(BigDecimal value, int newScale) {
    if (value != null) {
        return value.setScale(newScale, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

FormData getFormPrev() {
    if (isBalancePeriod() || isConsolidated) {
        return null
    }
    def reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    FormData formPrev = null
    if (reportPeriodPrev != null) {
        formPrev = formDataService.find(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, reportPeriodPrev.id)
    }
    return formPrev
}

/** Установить стиль для итоговых строк. */
void setTotalStyle(def row) {
    allColumns.each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/** Получить название эмитента. */
def getIssuerName(def record84Id) {
    def issuerId = getRefBookValue(84, record84Id?.toLong())?.ISSUER?.value
    return getRefBookValue(100, issuerId)?.FULL_NAME?.value
}

/** Получить буквенный код валюты. */
def getCurrencyName(def record84Id) {
    def record15Id = getRefBookValue(84, record84Id?.toLong())?.CODE_CUR?.value
    return getRefBookValue(15, record15Id)?.CODE_2?.value
}

/** Получить признак ценной бумаги. */
def getSign(def record84Id) {
    def record62Id = getRefBookValue(84, record84Id?.toLong())?.SIGN?.value
    return getRefBookValue(62, record62Id)?.CODE?.value
}

/**
 * Проверить существования строки по алиасу.
 *
 * @param list строки нф
 * @param rowAlias алиас
 * @return <b>true</b> - строка с указанным алиасом есть, иначе <b>false</b>
 */
def checkAlias(def list, def rowAlias) {
    if (rowAlias == null || rowAlias == "" || list == null || list.isEmpty()) {
        return false
    }
    for (def row : list) {
        if (row.getAlias() == rowAlias) {
            return true
        }
    }
    return false
}

/** Получить итоговую строку с суммами. */
def getCalcTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.getCell("fix").colSpan = 2
    newRow.fix = "Общий итог"
    setTotalStyle(newRow)

    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def msg) {
    if (isBalancePeriod()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Признак периода ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}