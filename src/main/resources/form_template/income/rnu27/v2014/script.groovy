package form_template.income.rnu27.v2014

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
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
// графа 2  - issuer                    - текст, было: зависит от графы 3 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
// графа 3  - regNumber                 - текст, было: атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочник 84 «Ценные бумаги»
// графа 4  - tradeNumber
// графа 5  - currency                  - справочник "Общероссийский классификатор валют", отображаемый атрибут "Код валюты. Буквенный",
//                                          было: зависит от графы 3 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»

// графа 6  - prev
// графа 7  - current
// графа 8  - reserveCalcValuePrev
// графа 9  - cost
// графа 10 - signSecurity              - справочник "Признак ценных бумаг", отображаемый атрибут "Код признака"
//                                              было: текст,
//                                              было: зависит от графы 3 - атрибут 869 - SIGN - «Признак ценной бумаги», справочник 84 «Ценные бумаги»
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.ADD_ROW:
        def columns = editableColumns
        if (isBalancePeriod()) {
            columns = allColumns - ['number']
        }
        def autoFillColumns = allColumns - columns
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
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT:
        if (UploadFileName.endsWith(".rnu")) {
            importTransportData()
        } else {
            importData()
        }
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
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

@Field
def editableColumns = ['issuer', 'regNumber', 'tradeNumber', 'currency', 'prev', 'current', 'cost', 'signSecurity',
                       'marketQuotation', 'rubCourse', 'costOnMarketQuotation', 'reserveCalcValue']

// Группируемые атрибуты (графа 2, 3, 4)
@Field
def groupColumns = ['issuer', 'regNumber', 'tradeNumber']

// Проверяемые на пустые значения атрибуты (графа 1..5, 8, 13..17)
@Field
def nonEmptyColumns = ['issuer', /*'regNumber',*/ 'tradeNumber', 'currency', 'reserveCalcValuePrev',
                       'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 6..9, 14..17)
@Field
def totalColumns = ['prev', 'current', 'reserveCalcValuePrev', 'cost',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// алиасы графов для арифметической проверки (графа )
@Field
def arithmeticCheckAlias = ['reserveCalcValuePrev', 'marketQuotation', 'rubCourse', 'marketQuotationInRub',
        /*'costOnMarketQuotation', 'reserveCalcValue',*/ 'reserveCreation', 'recovery']

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
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
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
    if (formData.kind == FormDataKind.PRIMARY && !isBalancePeriod()) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, formData.kind, formData.departmentId,
                formData.reportPeriodId, true, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    // данные предыдущего отчетного периода
    def dataPrevRows = null
    if (!isConsolidated) {
        def formPrev = getFormPrev()
        def dataPrev = formPrev != null ? formDataService.getDataRowHelper(formPrev) : null
        dataPrevRows = dataPrev?.allSaved
    }

    if (!isBalancePeriod() && !isConsolidated) {
        for (row in dataRows) {
            row.reserveCalcValuePrev = calc8(row, dataPrevRows)
            row.marketQuotation = calc11(row)
            row.rubCourse = calc12(row)
            row.marketQuotationInRub = calc13(row)
            //row.costOnMarketQuotation = calc14(row)
            //row.reserveCalcValue = calc15(row)
            row.reserveCreation = calc16(row)
            row.recovery = calc17(row)
        }
    }

    // добавить промежуточные итоги (по графе 2 - эмитент, а внутри этой группы по графе 3 - грн)
    addAllStatic(dataRows)

    // добавить строку "итого"
    dataRows.add(getCalcTotalRow(dataRows))

    sortFormDataRows(false)
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def formPrev = null
    def dataPrev = null
    def dataPrevRows = null
    if (!isConsolidated) {
        formPrev = getFormPrev()
        dataPrev = formPrev != null ? formDataService.getDataRowHelper(formPrev) : null
        dataPrevRows = dataPrev?.allSaved
    }

    for (DataRow row in dataRows) {
        if (row?.getAlias() == null) {
            def index = row.getIndex()
            def errorMsg = "Строка ${index}: "

            if (row.current == 0) {
                // 4. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.reserveCalcValuePrev != row.recovery) {
                    rowWarning(logger, row, errorMsg + "Графы 8 и 17 неравны!")
                }
                // 5. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0) {
                    rowWarning(logger, row, errorMsg + "Графы 9, 14 и 15 ненулевые!")
                }
            }
            // 6. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6 = 0)
            if (row.prev == 0 && (row.reserveCalcValuePrev != 0 || row.recovery != 0)) {
                loggerError(row, errorMsg + "Графы 8 и 17 ненулевые!")
            }
            // 7. Проверка необращающихся облигаций (графа 10 = «x»)
            def sign = getSign(row)
            if (sign == "-" && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                rowWarning(logger, row, errorMsg + "Облигации необращающиеся, графы 15 и 16 ненулевые!")
            }
            if (row.reserveCalcValue != null && row.reserveCalcValuePrev != null && sign == "+") {
                // 8. Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev > 0 && row.recovery != 0) {
                    loggerError(row, errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно! Не выполняется условие: если «графа 15» – «графа 8» > 0, то «графа 17» = 0")
                }
                // 9. Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev < 0 && row.reserveCreation != 0) {
                    loggerError(row, errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно! Не выполняется условие: если «графа 15» – «графа 8» < 0, то «графа 16» = 0")
                }
                // 10. Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev == 0 && (row.reserveCreation != 0 || row.recovery != 0)) {
                    loggerError(row, errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно! Не выполняется условие: если «графа 15» – «графа 8» = 0, то «графа 16» и «графа 17» = 0")
                }
            }
            // 11. Проверка корректности формирования резерва
            if (row.reserveCalcValuePrev != null && row.reserveCreation != null && row.reserveCalcValue != null && row.recovery != null
                    && row.reserveCalcValuePrev + row.reserveCreation != row.reserveCalcValue + row.recovery) {
                loggerError(row, errorMsg + "Резерв сформирован неверно! Сумма граф 8 и 16 должна быть равна сумме граф 15 и 17")
            }
            // 12. Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && (row.current <= 0 || row.cost <= 0 || row.costOnMarketQuotation <= 0 || row.reserveCalcValue <= 0)) {
                rowWarning(logger, row, errorMsg + "Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!")
            }
            // 13. Проверка корректности заполнения РНУ
            if (!isConsolidated && formPrev != null) {
                for (DataRow rowPrev in dataPrevRows) {
                    if (rowPrev.getAlias() == null && row.tradeNumber == rowPrev.tradeNumber && row.prev != null && row.prev != rowPrev.current) {
                        rowWarning(logger, row, errorMsg + "РНУ сформирован некорректно! Не выполняется условие: «Графа 6» (${row.prev}) текущей строки РНУ-27 за текущий период = «Графе 7» (${rowPrev.current}) строки РНУ-27 за предыдущий период, значение «Графы 4» которой соответствует значению «Графы 4» РНУ-27 за текущий период.")
                    }
                }
            }
            // 14. Проверка корректности заполнения РНУ
            if (!isConsolidated && formPrev != null) {
                for (DataRow rowPrev in dataPrevRows) {
                    if (rowPrev.getAlias() == null && row.tradeNumber == rowPrev.tradeNumber && row.reserveCalcValuePrev != null && row.reserveCalcValuePrev != rowPrev.reserveCalcValue) {
                        rowWarning(logger, row, errorMsg + "РНУ сформирован некорректно! Не выполняется условие: «Графа 8» (${row.reserveCalcValuePrev}) текущей строки РНУ-27 за текущий период= «Графе 15» (${rowPrev.reserveCalcValue}) строки РНУ-27 за предыдущий период, значение «Графы 4» которой соответствует значению «Графы 4» РНУ-27 за текущий период.")
                    }
                }
            }

            // 1. Проверка на заполнение поля «<Наименование поля>»
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())
            // графа 3 - сделали необязательной, но сообщение о незаполненности выводить нефатальным сообщением
            checkNonEmptyColumns(row, index, ['regNumber'], logger, false)

            if (isRubleCurrency(row.currency)) {
                // 17. Проверка графы 11
                if (row.marketQuotation != null) {
                    loggerError(row, errorMsg + "Неверно заполнена графа «Рыночная котировка одной ценной бумаги в иностранной валюте»!")
                }
                // 18. Проверка графы 12
                if (row.rubCourse != null) {
                    loggerError(row, errorMsg + "Неверно заполнена графы «Курс рубля к валюте рыночной котировки»!")
                }
            }

            if (!isConsolidated && !isBalancePeriod()) {
                // 19. Арифметические проверки граф 5, 8, 11, 12, 13, 14, 15, 16, 17
                def calcValues = [
                        reserveCalcValuePrev: calc8(row, dataPrevRows),
                        marketQuotation: calc11(row),
                        rubCourse: calc12(row),
                        marketQuotationInRub: calc13(row),
                        //costOnMarketQuotation: calc14(row),
                        //reserveCalcValue: calc15(row),
                        reserveCreation: calc16(row),
                        recovery: calc17(row)
                ]
                checkCalc(row, arithmeticCheckAlias, calcValues, logger, true)
            }
        }

        // LC 20
        if (row.getAlias() != null && row.getAlias().indexOf('itogoRegNumber') != -1) {
            srow = calcItogRegNumber(dataRows.indexOf(row) - 1, dataRows)

            for (column in totalColumns) {
                if (row.get(column) != srow.get(column)) {
                    def regNumber = (getRegNumberOrIssuer(dataRows, row, 'regNumber') ?: "ГРН не задан")
                    loggerError(null, "Итоговые значения по «$regNumber» рассчитаны неверно в графе «${getColumnName(row, column)}»!")
                }
            }
        }

        // LC 21
        if (row.getAlias() != null && row.getAlias().indexOf('itogoIssuer') != -1) {
            srow = calcItogIssuer(dataRows.indexOf(row) - 1, dataRows)

            for (column in totalColumns) {
                if (row.get(column) != srow.get(column)) {
                    def issuer = (getRegNumberOrIssuer(dataRows, row, 'issuer') ?: "Эмитент не задан")
                    loggerError(null, "Итоговые значения для «$issuer» рассчитаны неверно в графе «${getColumnName(row, column)}»!")
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
            rowWarning(logger, null, "РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе 6 (${itogo.prev}) = «Итого» по графе 7 (${itogoPrev.current}) формы РНУ-27 за предыдущий отчётный период")
        }
        // 14.
        if (itogo != null && itogoPrev != null && itogo.reserveCalcValuePrev != itogoPrev.reserveCalcValue) {
            rowWarning(logger, null, "РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе 8 (${itogo.reserveCalcValuePrev}) = «Итого» по графе 15 (${itogoPrev.reserveCalcValue}) формы РНУ-27 за предыдущий отчётный период")
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
            logger.warn("Существует несколько строк с номерами сделок: ${foundMany.join(', ')}")
        }
    }
}

/** Получить признак ценной бумаги. */
def getSign(def row) {
    return getRefBookValue(62, row.signSecurity)?.CODE?.value
}

def isRubleCurrency(def currencyCode) {
    return currencyCode != null ? (getRefBookValue(15, currencyCode)?.CODE?.stringValue in ['810', '643']) : false
}

/**
 * Получить значение группировки подитоговой строки.
 *
 * @param dataRows строки
 * @param row подитоговая строка
 * @param alias алиас графы значение которой используется для группировки
 */
def getRegNumberOrIssuer(def dataRows, DataRow row, def alias) {
    int pos = dataRows.indexOf(row)
    def tmpRow = null
    for (int i = pos; i >= 0; i--) {
        def iRow = getRow(dataRows, i)
        if (iRow.getAlias() == null) {
            tmpRow = iRow
            break
        }
    }
    if (tmpRow != null) {
        return tmpRow[alias]
    } else {
        return row.fix[0..(row.fix.size() - ' Итог'.size() - 1)]
    }
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
        def regNum = row?.regNumber
        def nextRegNum = nextRow?.regNumber
        if (row.getAlias() == null && nextRow == null || regNum != nextRegNum) {
            def itogRegNumberRow = calcItogRegNumber(i, dataRows)
            j++
            dataRows.add(i + j, itogRegNumberRow)
        }

        // графа 2 - эмитент - issuer
        def issuer = row.issuer
        def nextIssuer = nextRow?.issuer
        if (row.getAlias() == null && nextRow == null || issuer != nextIssuer) {
            // если все значения ГРН пустые, то подитог по ГРН не добавится, поэтому перед добавлением подитога по эмитенту, нужно добавить подитого с незадавнным ГРН
            if (j == 0) {
                def itogRegNumberRow = calcItogRegNumber(i, dataRows)
                j++
                dataRows.add(i + j, itogRegNumberRow)
            }
            def itogIssuerRow = calcItogIssuer(i, dataRows)
            j++
            dataRows.add(i + j, itogIssuerRow)
        }
        i += j  // Обязательно чтобы избежать зацикливания в простановке
    }
}

/** Расчет итога Эмитента (группировка). */
def calcItogIssuer(int i, def dataRows) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.getCell('fix').colSpan = 3
    newRow.setAlias('itogoIssuer#'.concat(i ? i.toString() : ""))
    setTotalStyle(newRow)

    def emptyGroup = false
    String tIssuer = 'Эмитент'
    for (int j = i; j >= 0; j--) {
        if (getRow(dataRows, j).getAlias() == null) {
            tIssuer = getRow(dataRows, j).issuer
            break
        } else if (i - j > 0) {
            emptyGroup = true
            break
        }
    }

    newRow.fix = (tIssuer != null ? tIssuer : 'Эмитент не задан') + ' Итог'

    for (column in totalColumns) {
        newRow.getCell(column).setValue(new BigDecimal(0), null)
    }

    if (emptyGroup) {
        return newRow
    }

    for (int j = i; j >= 0; j--) {
        def srow = getRow(dataRows, j)

        if (srow.getAlias() == null) {
            def issuerName = getRow(dataRows, j).issuer
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

/** Расчет итога ГРН (группировка внутри группы по эмитенту). */
def calcItogRegNumber(int i, def dataRows) {
    // создаем итоговую строку ГРН
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.getCell('fix').colSpan = 3
    newRow.setAlias('itogoRegNumber#'.concat(i ? i.toString() : ""))
    setTotalStyle(newRow)

    String tRegNumber = 'ГРН'
    for (int j = i; j >= 0; j--) {
        if (getRow(dataRows, j).getAlias() == null) {
            tRegNumber = getRow(dataRows, j).regNumber
            break
        } else if (i - j > 0) {
            break
        }
    }

    newRow.fix = (tRegNumber != null ? tRegNumber : 'ГРН не задан') + ' Итог'

    for (column in totalColumns) {
        newRow.getCell(column).setValue(new BigDecimal(0), null)
    }

    // идем от текущей позиции вверх и ищем нужные строки
    for (int j = i; j >= 0; j--) {
        def srow = getRow(dataRows, j)
        if (srow.getAlias() != null || srow.regNumber != tRegNumber) {
            break
        }
        for (column in totalColumns) {
            if (srow[column] != null) {
                def value = newRow.getCell(column).value + (BigDecimal) srow.getCell(column).value
                newRow.getCell(column).setValue(value, null)
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
    if (isRubleCurrency(row.currency)) {
        return null
    }
    return row.marketQuotation
}

BigDecimal calc12(DataRow row) {
    if (isRubleCurrency(row.currency)) {
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
    if (getSign(row) == '+') {
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
            tmp = row.reserveCalcValue - row.reserveCalcValuePrev
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
        formPrev = formDataService.getLast(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, reportPeriodPrev.id, null, formData.comparativePeriodId, formData.accruing)
    }
    return formPrev
}

/** Установить стиль для итоговых строк. */
void setTotalStyle(def row) {
    allColumns.each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
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
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.setAlias('total')
    newRow.getCell("fix").colSpan = 2
    newRow.fix = "Общий итог"
    setTotalStyle(newRow)

    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 17
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null      // итоговая строка со значениями из тф для добавления
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    try {
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (rowCells == null || !isEmptyCells(rowCells)) {
            logger.error('Вторая строка должна быть пустой')
        }
        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }
            newRows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex))
        }
    } finally {
        reader.close()
    }

    // итоговая строка
    def totalRow = getCalcTotalRow(newRows)
    newRows.add(totalRow)

    showMessages(newRows, logger)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = [ 'prev' : 6, 'current' : 7, 'reserveCalcValuePrev' : 8, 'cost' : 9,
                'costOnMarketQuotation' : 14, 'reserveCalcValue' : 15, 'reserveCreation' : 16, 'recovery' : 17 ]

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR + " Из файла: $v1, рассчитано: $v2", totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
        // задать итоговой строке нф значения из итоговой строки тф
        totalColumns.each { alias ->
            totalRow[alias] = totalTF[alias]
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalColumns.each { alias ->
            totalRow[alias] = null
        }
    }

    // вставляем строки в БД
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(newRows)
        formDataService.getDataRowHelper(formData).allCached = newRows
    }
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    def DataRow newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}", fileRowIndex))
        return newRow
    }

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def int colOffset = 1
    def int colIndex

    // Графа 2
    colIndex = 2
    newRow.issuer = pure(rowCells[colIndex])
    // Графа 3
    colIndex = 3
    newRow.regNumber = pure(rowCells[colIndex])
    // Графа 4
    colIndex = 4
    newRow.tradeNumber = pure(rowCells[colIndex])
    // Графа 5
    colIndex = 5
    newRow.currency = getRecordIdImport(15, 'CODE_2', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    // Графа 6
    colIndex = 6
    newRow.prev = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // Графа 7
    colIndex = 7
    newRow.current = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // Графа 8
    colIndex = 8
    newRow.reserveCalcValuePrev = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // Графа 9
    colIndex = 9
    newRow.cost = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // Графа 10
    colIndex = 10
    newRow.signSecurity = getRecordIdImport(62, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    // Графа 11
    colIndex = 11
    newRow.marketQuotation = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // Графа 12
    colIndex = 12
    newRow.rubCourse = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // Графа 13
    colIndex = 13
    newRow.marketQuotationInRub = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // Графа 14
    colIndex = 14
    newRow.costOnMarketQuotation = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // Графа 15
    colIndex = 15
    newRow.reserveCalcValue = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // Графа 16
    colIndex = 16
    newRow.reserveCreation = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // Графа 17
    colIndex = 17
    newRow.recovery = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)

    return newRow
}

static String pure(String cell) {
    return StringUtils.cleanString(cell).intern()          
}


boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def rowsMap = [:]
    def regNumberMap = [:]
    def rowList = []
    def total = null
    dataRows.each{ row ->
        if (row.getAlias() == null) {
            rowList.add(row)
        } else if (row.getAlias().contains('itogoRegNumber')) {
            regNumberMap.put(row, rowList)
            rowList = []
        } else if (row.getAlias().contains('itogoIssuer')) {
            rowsMap.put(row, regNumberMap)
            regNumberMap = [:]
        } else {
            total = row
        }
    }

    dataRows.clear()

    // сортируем и добавляем все строки
    def tmpSortedRows = rowsMap.keySet().sort { it.fix }
    tmpSortedRows.each { keyRow ->
        def subMap = rowsMap[keyRow]
        def tmpSortedRows2 =  subMap.keySet().sort { it.fix }
        tmpSortedRows2.each { keySubTotalRow ->
            def dataRowsList = subMap[keySubTotalRow]
            sortAddRows(dataRowsList, dataRows)
            dataRows.add(keySubTotalRow)
        }
        dataRows.add(keyRow)
    }
    // если остались данные вне иерархии, добавляем их перед итогами
    sortAddRows(rowList, dataRows)
    dataRows.add(total)

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

void sortAddRows(def addRows, def dataRows) {
    if (!addRows.empty) {
        def firstRow = addRows[0]
        // Массовое разыменовывание граф НФ
        def columnNameList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, addRows, columnNameList)

        sortRowsSimple(addRows)
        dataRows.addAll(addRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 17
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'number')
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
    def totalRowFromFileMap = [:]           // мапа для хранения строк итогов/подитогов со значениями из файла (стили простых строк)
    def totalRowMap = [:]                   // мапа для хранения строк итогов/подитогов нф с посчитанными значениями и со стилями
    def prevRowIsSimple = false             // признако того что предыдущая строка была подитоговая (для определения подитога по грн или по эмитенту)

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
        if (rowValues[INDEX_FOR_SKIP] == "Общий итог") {
            // получить значения итоговой строки из файла
            rowIndex++
            totalRowFromFileMap[rowIndex] = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, true)

            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // пропуск подитоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "ГРН не задан" || rowValues[INDEX_FOR_SKIP].contains(" Итог")) {
            // сформировать и подсчитать подитоги
            def subTotalRow = (prevRowIsSimple ? calcItogRegNumber(rowIndex - 1, rows) : calcItogIssuer(rowIndex - 1, rows))
            rows.add(subTotalRow)
            prevRowIsSimple = false
            // получить значения подитоговой строки из файла
            rowIndex++
            totalRowFromFileMap[rowIndex] = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, true)
            totalRowMap[rowIndex] = subTotalRow

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        prevRowIsSimple = true
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // итоговая строка
    def totalRow = getCalcTotalRow(rows)
    rows.add(totalRow)
    totalRowMap[rowIndex] = totalRow
    updateIndexes(rows)

    // сравнение итогов
    if (!totalRowFromFileMap.isEmpty()) {
        // сравнение
        totalRowFromFileMap.keySet().toArray().each { index ->
            def totalFromFile = totalRowFromFileMap[index]
            def total = totalRowMap[index]
            compareTotalValues(totalFromFile, total, totalColumns, logger, false)
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
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]): getColumnName(tmpRow, 'number')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'issuer')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'regNumber')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'tradeNumber')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'currency')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'prev')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'current')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'reserveCalcValuePrev')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'signSecurity')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'marketQuotation')]),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'rubCourse')]),
            ([(headerRows[0][13]): getColumnName(tmpRow, 'marketQuotationInRub')]),
            ([(headerRows[0][14]): getColumnName(tmpRow, 'costOnMarketQuotation')]),
            ([(headerRows[0][15]): getColumnName(tmpRow, 'reserveCalcValue')]),
            ([(headerRows[0][16]): getColumnName(tmpRow, 'reserveCreation')]),
            ([(headerRows[0][17]): getColumnName(tmpRow, 'recovery')]),
            ([(headerRows[1][0]): '1'])
    ]
    (2..17).each { index ->
        headerMapping.add(([(headerRows[1][index]): index.toString()]))
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
 * @param isTotal признак итоговой/подитоговой строки (для них надо еще получить значение из скрытого столбца)
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def isTotal = false) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    def editableIColumns = editableColumns
    if (isBalancePeriod()) {
        editableIColumns = allColumns - ['number']
    }

    editableIColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    // графа 1
    def colIndex = 1
    if (isTotal) {
        newRow.fix = values[colIndex]
    }

    // графа 2..4
    ['issuer', 'regNumber', 'tradeNumber'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 5
    colIndex = 5
    newRow.currency = getRecordIdImport(15, 'CODE_2', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 6..9
    ['prev', 'current', 'reserveCalcValuePrev', 'cost'].each { alias ->
        colIndex++
        newRow[alias] = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    }

    // графа 10
    colIndex = 10
    newRow.signSecurity = getRecordIdImport(62, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 11..17
    ['marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery'].each { alias ->
        colIndex++
        newRow[alias] = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    }

    return newRow
}