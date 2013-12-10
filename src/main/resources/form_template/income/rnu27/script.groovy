package form_template.income.rnu27

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * (РНУ-27) Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных
 *              и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения
 * formTemplateId=326
 *
 * ЧТЗ http://conf.aplana.com/pages/viewpage.action?pageId=8588102 ЧТЗ_сводные_НФ_Ф2_Э1_т2.doc
 *
 * TODO:
 *      - костыль! в ТФ в столбце для графы 2 могут быть строки содержащие "<" и ">", в ImportServiceImpl
 *      они заменяются на &lt и &gt, при записи в форму надо поменять назад, в 0.3.6 это будет вынесено в ScriptUtils
 *
 * @author ekuvshinov
 * @author lhaziev
 */

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod
isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow.getAlias() == null) formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        prevPeriodCheck()
        logicCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT :
        prevPeriodCheck()
        importData()
        if (!hasError()) {
            calcAfterImport()
            addAllStatic()
        }
        break
    case FormDataEvent.MIGRATION :
        prevPeriodCheck()
        importData()
        if (!hasError()) {
            def data = formDataService.getDataRowHelper(formData)
            def dataRows = data.getAllCached()
            def total = getCalcTotalRow(dataRows)
            data.insert(total, dataRows.size() + 1)
        }
        break
}

// графа 1  - число  number                 № пп
// графа 2  - строка issuer                 эмитит
// графа 3  - строка regNumber              гос номер
// графа 4  - строка tradeNumber            Номер сделки
// графа 5  - строка currency               Валюта выпуска облигации (справочник)
// графа 6  - число  prev                   Размер лота на отчётную дату по депозитарному учёту (шт.). Предыдущую
// графа 7  - число  current                Размер лота на отчётную дату по депозитарному учёту (шт.). Текущую
// графа 8  - число  reserveCalcValuePrev   Расчётная величина резерва на предыдущую отчётную дату (руб.коп.)
// графа 9  - число  cost                   Стоимость по цене приобретения (руб.коп.)
// графа 10 - строка signSecurity           Признак ценной бумаги на текущую отчётную дату (справочник)
// графа 11 - число  marketQuotation        Quotation Рыночная котировка одной ценной бумаги в иностранной валюте
// графа 12 - число  rubCourse              Курс рубля к валюте рыночной котировки
// графа 13 - число  marketQuotationInRub   Рыночная котировка одной ценной бумаги в рублях
// графа 14 - число  costOnMarketQuotation  costOnMarketQuotation
// графа 15 - число  reserveCalcValue       Расчетная величина резерва на текущую отчётную дату (руб.коп.)
// графа 16 - число  reserveCreation        Создание резерва (руб.коп.)
// графа 17 - число  recovery               Восстановление резерва (руб.коп.)


//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// все атрибуты
@Field
def allColumns = ['number', 'issuer', 'regNumber', 'tradeNumber', 'currency', 'prev', 'current',
        'reserveCalcValuePrev', 'cost', 'signSecurity', 'marketQuotation', 'rubCourse', 'marketQuotationInRub',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// Редактируемые атрибуты
@Field
def editableColumns = ['issuer', 'regNumber', 'tradeNumber', 'currency', 'prev', 'current',
        'reserveCalcValuePrev', 'cost', 'signSecurity', 'marketQuotation', 'rubCourse',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты
@Field
def groupColumns = ['regNumber', 'tradeNumber']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'issuer', 'regNumber', 'tradeNumber', 'currency', 'reserveCalcValuePrev',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// Атрибуты итоговых строк для которых вычисляются суммы
@Field
def totalColumns = ['prev', 'current', 'reserveCalcValuePrev', 'cost',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// алиасы графов для арифметической проверки
@Field
def arithmeticCheckAlias = ['reserveCalcValuePrev', 'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation',
        'reserveCalcValue', 'reserveCreation', 'recovery']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()


//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
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
    if (!isBalancePeriod && !isConsolidated && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        logger.error('Форма предыдущего периода не существует или не находится в статусе «Принята»')
    }
}

/**
 * Логические проверки
 */
def logicCheck() {
    def data = formDataService.getDataRowHelper(formData)
    def dataRows = data.getAllCached()

    def formPrev = getFormPrev()
    def dataPrev = formDataService.getDataRowHelper(formPrev)
    def dataPrevRows = dataPrev.getAllCached()

    /** 1. LC Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде (выполняется один раз для всего экземпляра)
     * http://jira.aplana.com/browse/SBRFACCTAX-2609
     */
    if (dataPrev != null) {
        List notFound = []
        List foundMany = []
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
                }
                if (count != 0 && count != 1) {
                    foundMany.add(rowPrev.tradeNumber)
                }
            }
        }
        if (!notFound.isEmpty()) {
            StringBuilder sb = new StringBuilder("Отсутствуют строки с номерами сделок :")
            for (tradeNumber in notFound) {
                sb.append(" " + tradeNumber.toString() + ",")
            }
            String message = sb.toString()
            logger.warn(message.substring(0, message.length() - 1))
        }
        if (!foundMany.isEmpty()) {
            StringBuilder sb = new StringBuilder("Отсутствуют строки с номерами сделок :")
            for (tradeNumber in foundMany) {
                sb.append(" " + tradeNumber.toString() + ",")
            }
            String message = sb.toString()
            logger.warn(message.substring(0, message.length() - 1))
        }
    }

    def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    for (DataRow row in data.getAllCached()) {
        if (row?.getAlias() == null) {
            number++
            def index = row.getIndex()
            def errorMsg = "Строка ${index}: "

            if (row.current == 0) {
                // 2. LC Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.reserveCalcValuePrev != row.current) {
                    logger.warn(errorMsg + "Графы 8 и 17 неравны!")
                }
                // 3. LC • Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.cost != row.costOnMarketQuotation || row.cost != row.reserveCalcValue || row.cost == 0) {
                    logger.warn(errorMsg + "Графы 9, 14 и 15 ненулевые!")
                }
            }
            // 4. LC • Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6 = 0)
            if (row.prev == 0 && (row.reserveCalcValuePrev != row.recovery || row.recovery != 0)) {
                loggerError(errorMsg + "Графы 8 и 17 ненулевые!")
            }
            // 5. LC • Проверка необращающихся облигаций (графа 10 = «x»)
            if (getSign(row.signSecurity) == "x" && (row.reserveCalcValue != row.reserveCreation || row.reserveCreation != 0)) {
                logger.warn(errorMsg + "Облигации необращающиеся, графы 15 и 16 ненулевые!")
            }
            if (getSign(row.signSecurity) == "+") {
                // 6. LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev > 0 && row.recovery != 0) {
                    loggerError(errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
                // 7. LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev < 0 && row.reserveCreation != 0) {
                    loggerError(errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
                // 8. LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev == 0 && (row.reserveCreation != 0 || row.recovery != 0)) {
                    loggerError(errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
            }
            // 9. LC • Проверка корректности формирования резерва
            if (row.reserveCalcValuePrev != null && row.reserveCreation != null && row.reserveCalcValue != null && row.recovery != null
                    && row.reserveCalcValuePrev + row.reserveCreation != row.reserveCalcValue + row.recovery) {
                loggerError(errorMsg + "Резерв сформирован неверно!")
            }
            // 10. LC • Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && (row.current < 0 || row.cost < 0 || row.costOnMarketQuotation < 0 || row.reserveCalcValue < 0)) {
                logger.warn(errorMsg + "Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!")
            }
            // 11. LC • Проверка корректности заполнения РНУ
            if (formPrev != null) {
                for (DataRow rowPrev in dataPrevRows) {
                    if (rowPrev.getAlias() == null && row.tradeNumber == rowPrev.tradeNumber && row.prev != rowPrev.current) {
                        logger.warn(errorMsg + "РНУ сформирован некорректно! Не выполняется условие: Если  «графа 4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то «графа 6» = «графа 7» формы РНУ-27 за предыдущий отчётный период")
                    }
                }
            }
            // 12. LC • Проверка корректности заполнения РНУ
            if (formPrev != null) {
                for (DataRow rowPrev in dataPrevRows) {
                    if (rowPrev.getAlias() == null && row.tradeNumber == rowPrev.tradeNumber && row.reserveCalcValuePrev != rowPrev.reserveCalcValue) {
                        loggerError(errorMsg + "РНУ сформирован некорректно! Не выполняется условие: Если  «графа 4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то графа 8 = графа 15 формы РНУ-27 за предыдущий отчётный период")
                    }
                }
            }

            // 15. LC Проверка на заполнение поля «<Наименование поля>»
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

            // 16. Проверка на уникальность поля «№ пп»
            if (number != row.number) {
                logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
            }

            if (getCurrency(row.currency) == 'RUR') {
                // 17. LC Проверка графы 11
                if (row.marketQuotation != null) {
                    loggerError(errorMsg + "Неверно заполнена графа «Рыночная котировка одной ценной бумаги в иностранной валюте»!")
                }
                // 18. LC Проверка графы 12
                if (row.rubCourse != null) {
                    loggerError(errorMsg + "Неверно заполнена графы «Курс рубля к валюте рыночной котировки»!")
                }
            }

            if (!isBalancePeriod) {
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

            // Проверки НСИ
            checkNSI(15, row, "currency")
            checkNSI(62, row, "signSecurity")
/*          RefBook rb = refBookFactory.get(15);
            if (row.currency != null && getCourse(row.currency, reportDate) == null){
                logger.warn(errorMsg + "В справочнике «" + rb.getName() + "» не найдено значение «" + row.rubCourse + "», соответствующее атрибуту «" + rb.getAttribute("RATE").getName() + "»!")
            } */
        }

        // LC 20
        if (row.getAlias() != null && row.getAlias().indexOf('itogoRegNumber') != -1) {
            srow = calcItogRegNumber(dataRows.indexOf(row))

            for (column in totalColumns) {
                if (row.get(column) != srow.get(column)) {
                    loggerError("Итоговые значения по «"+ getPrevRowWithoutAlias(dataRows, row).regNumber+"» рассчитаны неверно в графе «${getColumnName(row, column)}»!")
                }
            }
        }

        // LC 21
        if (row.getAlias() != null && row.getAlias().indexOf('itogoIssuer') != -1) {
            srow = calcItogIssuer(dataRows.indexOf(row))

            for (column in totalColumns) {
                if (row.get(column) != srow.get(column)) {
                    loggerError("Итоговые значения для «"+ getPrevRowWithoutAlias(dataRows, row).issuer+"» рассчитаны неверно в графе «${getColumnName(row, column)}»!")
                }
            }
        }
    }

    // LC • Проверка корректности заполнения РНУ
    if (dataPrev != null && checkAlias(dataPrevRows, 'itogo') && checkAlias(dataRows, 'itogo')) {
        DataRow itogoPrev = data.getDataRow(dataPrevRows, 'itogo')
        DataRow itogo = data.getDataRow(dataRows, 'itogo')
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
    checkTotalSum(dataRows, totalColumns, logger, true)
}

/**
 * @author ivildanov
 * Ищем вверх по форме первую строку без альяса
 */
DataRow getPrevRowWithoutAlias(def dataRows, DataRow row) {
    int pos = dataRows.indexOf(row)
    for (int i = pos; i >= 0; i--) {
        if (getRow(i).getAlias() == null) {
            return row
        }
    }
    throw new IllegalArgumentException()
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}

/**
 * Получение импортируемых данных.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    if (!fileName.contains('.r')) {
        logger.error('Формат файла должен быть *.rnu')
        return
    }

    def xmlString = importService.getData(is, fileName, 'cp866')
    if (xmlString == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    try {
        // добавить данные в форму
        def totalLoad = addData(xml)

        // расчетать, проверить и сравнить итоги
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
    }
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (int i = 0; i < dataRows.size(); i++) {
        DataRow<Cell> row = getRow(i, dataRows)
        DataRow<Cell> nextRow = getRow(i + 1, dataRows)
        int j = 0

        if (row.getAlias() == null && nextRow == null || row.regNumber != nextRow.regNumber || row.issuer != nextRow.issuer) {
            def itogRegNumberRow = calcItogRegNumber(i)
            dataRows.add(i + 1, itogRegNumberRow)
            j++
        }

        if (row.getAlias() == null && nextRow == null || row.issuer != nextRow.issuer) {
            def itogIssuerRow = calcItogIssuer(i)
            dataRows.add(i + 2, itogIssuerRow)
            j++
        }
        i += j  // Обязательно чтобы избежать зацикливания в простановке
    }
}

/**
 * Расчет итога Эмитета
 * @author ivildanov
 */
def calcItogIssuer(int i) {
    def newRow = formData.createDataRow()
    newRow.getCell('issuer').colSpan = 2
    newRow.setAlias('itogoIssuer#'.concat(i?i.toString():""))

    String tIssuer = 'Эмитет'
    for (int j = i; j >= 0; j--) {
        if (getRow(j).getAlias() == null) {
            tIssuer = getRow(j).issuer
            break
        }
    }

    newRow.issuer = tIssuer?.concat(' Итог')

    for (column in totalColumns) {
        newRow.getCell(column).value = new BigDecimal(0)
    }

    for (int j = i; j >= 0; j--) {
        srow = getRow(j)

        if (srow.getAlias() == null) {
            if (((getRow(j).issuer != tIssuer))) {
                break
            }

            for (column in totalColumns) {
                if (srow.get(column) != null) {
                    newRow.getCell(column).value = newRow.getCell(column).value + (BigDecimal) srow.get(column)
                }
            }
        }

    }
    setTotalStyle(newRow)
    newRow
}

/**
 * Расчет итога ГРН
 * @author ivildanov
 */
def calcItogRegNumber(int i) {
    // создаем итоговую строку ГРН
    def newRow = formData.createDataRow()
    newRow.getCell('regNumber').colSpan = 2
    newRow.setAlias('itogoRegNumber#'.concat(i?i.toString():""))

    String tRegNumber = 'ГРН'
    for (int j = i; j >= 0; j--) {
        if (getRow(j).getAlias() == null) {
            tRegNumber = getRow(j).regNumber
            break
        }
    }

    newRow.regNumber = tRegNumber?.concat(' Итог')

    for (column in totalColumns) {
        newRow.getCell(column).value = new BigDecimal(0)
    }

    // идем от текущей позиции вверх и ищем нужные строки
    for (int j = i; j >= 0; j--) {
        srow = getRow(j)

        if (srow.getAlias() == null) {
            if (((getRow(j).regNumber != tRegNumber))) {
                break
            }

            for (column in totalColumns) {
                if (srow.get(column) != null) {
                    newRow.getCell(column).value = newRow.getCell(column).value + (BigDecimal) srow.get(column)
                }
            }
        }
    }
    setTotalStyle(newRow)
    return newRow
}

/**
 * Получение строки по номеру
 * @author ivildanov
 */
DataRow<Cell> getRow(int i) {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if ((i < dataRows.size()) && (i >= 0)) {
        return dataRows.get(i)
    } else {
        return null
    }
}

/**
 * Алгоритмы заполнения полей формы
 * Алгоритмы заполнения полей формы «Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения»
 */

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    def reportDate = reportPeriodService.getReportDate(formData.reportPeriodId).time

    // данные предыдущего отчетного периода
    def formDataOld = getFormPrev()
    def dataPrevRows = formDataOld != null ? formDataService.getDataRowHelper(formDataOld)?.getAllCached() : null

    // номер последний строки предыдущей формы
    def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

    for (row in dataRows) {
        row.number = ++number
        if (!isBalancePeriod) {
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

    addAllStatic()

    // добавить строку "итого"
    dataRows.add(getCalcTotalRow(dataRows))

    // используется save() т.к. есть сортировка
    dataRowHelper.save(dataRows)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы после импорта.
 */
void calcAfterImport() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (row in dataRows) {
        // Проверим чтобы человек рукамми ввёл всё что необходимо
        def requiredColumns = ['issuer', 'regNumber', 'tradeNumber', 'currency']
        if(!checkRequiredColumns(row,requiredColumns)){
            return
        }
    }
    if (!hasError()) {
        def formPrev = getFormPrev()
        for (DataRow row in dataRows) {
            row.reserveCalcValuePrev = calc8(row, formPrev)
            row.costOnMarketQuotation = calc14(row)
            row.reserveCalcValue = calc15(row)
            row.reserveCreation = calc16(row)
            row.recovery = calc17(row)
        }
        dataRowHelper.save(dataRows);
    }
}

/**
 * Расчет графы 8
 */
BigDecimal calc8(DataRow row, def dataPrevRows) {
    if (isConsolidated) {
        return row.reserveCalcValuePrev
    }
    // Расчет графы 8 в соответсвие коментарию Аванесова http://jira.aplana.com/browse/SBRFACCTAX-2562
    temp = new BigDecimal(0)
    tempCount = 0

    if (formPrev != null) {
        for (DataRow rowPrev in dataPrevRows) {
            if (row.tradeNumber == rowPrev.tradeNumber) {
                temp = rowPrev.reserveCalcValue
                tempCount++
            }
        }
    }
    if (tempCount == 1) {
        return roundValue(temp, 2)
    } else {
        return (BigDecimal) 0
    }
}

/**
 * Расчет графы 11
 * @author ivildanov
 */
BigDecimal calc11(DataRow row) {
    if (getCurrency(row.currency)?.contains("RUR")) {
        return null
    }
    return row.marketQuotation
}

/**
 * Расчет графы 12
 */
BigDecimal calc12(DataRow row) {
    if (getCurrency(row.currency)?.contains("RUR")) {
        return null
    }
    return row.rubCourse
    //return getCourse(row.currency,reportDate)
}

/**
 * Расчет графы 13
 * @author ivildanov
 */
BigDecimal calc13(DataRow row) {
    if (row.marketQuotation != null && row.rubCourse != null) {
        return roundValue((BigDecimal) (row.marketQuotation * row.rubCourse), 2)
    } else {
        return null
    }
}

/**
 * Расчет графы 14
 * @author ivildanov
 */
BigDecimal calc14(DataRow row) {
    if (row.marketQuotationInRub == null) {
        return (BigDecimal) 0
    } else {
        return roundValue((BigDecimal) (row.current * row.marketQuotationInRub), 2)
    }
}

/**
 * Расчет графы 15
 * @author ivildanov
 */
BigDecimal calc15(DataRow row) {
    BigDecimal a

    if (row.cost != null) {
        a = row.cost
    } else {
        a = 0
    }

    if (getSign(row.signSecurity) == "+") {
        if (a - row.costOnMarketQuotation > 0) {
            return a - row.costOnMarketQuotation
        } else {
            return (BigDecimal) 0
        }
    } else if (getSign(row.signSecurity) == "-") {
        return (BigDecimal) 0
    } else {
        return null
    }
}

/**
 * Расчет графы 16
 * @author ivildanov
 */
BigDecimal calc16(DataRow row) {
    if (row.reserveCalcValue != null && row.reserveCalcValuePrev != null) {
        if (row.reserveCalcValue - row.reserveCalcValuePrev > 0) {
            return roundValue(row.reserveCalcValue - row.reserveCalcValuePrev, 2)
        } else {
            return (BigDecimal) 0
        }
    } else {
        return null
    }
}

/**
 * Расчет графы 17
 * @author ivildanov
 */
BigDecimal calc17(DataRow row) {
    if (row.reserveCalcValue != null && row.reserveCalcValuePrev != null) {
        BigDecimal a
        if (row.reserveCalcValue - row.reserveCalcValuePrev < 0) {
            a = row.reserveCalcValue - row.reserveCalcValuePrev
        } else {
            a = 0
        }
        return roundValue(a.abs(), 2)
    } else {
        return null
    }
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal roundValue(BigDecimal value, int newScale) {
    if (value != null) {
        return value.setScale(newScale, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

FormData getFormPrev() {
    if (isBalancePeriod || isConsolidated) {
        return null
    }
    reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    FormData formPrev = null
    if (reportPeriodPrev != null) {
        formPrev = formDataService.find(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, reportPeriodPrev.id)
    }
    return formPrev
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    allColumns.each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    Date date = new Date()

    def cache = [:]
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = new LinkedList<DataRow<Cell>>()

    for (def row : xml.row) {
        def newRow = getNewRow()

        def indexCell = 1

        newRow.number = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 2
        // TODO (Ramil Timerbaev) костыль! в ТФ в столбце для графы 2 могут быть строки содержащие "<" и ">",
        // в ImportServiceImpl они заменяются на &lt и &gt, при записи в форму надо поменять назад, в 0.3.6 это будет вынесено в ScriptUtils
        def tmp = row.cell[indexCell].text()
        newRow.issuer = tmp.replaceAll('&lt;', '<').replaceAll('&gt;', '>').replaceAll('&amp;', '&')
        indexCell++

        // графа 3
        newRow.regNumber = row.cell[indexCell].text()
        indexCell++

        // графа 4
        newRow.tradeNumber = row.cell[indexCell].text()
        indexCell++

        // графа 5
        newRow.currency = getRecords(15, 'CODE_2', row.cell[indexCell].text(), date, cache)
        indexCell++

        // графа 6
        newRow.prev = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 7
        newRow.current = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 8
        newRow.reserveCalcValuePrev = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 9
        newRow.cost = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 10
        newRow.signSecurity = getRecords(62, 'CODE', row.cell[indexCell].text(), date, cache)
        indexCell++

        // графа 11
        newRow.marketQuotation = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 12
        newRow.rubCourse = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 13
        newRow.marketQuotationInRub = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 14
        newRow.costOnMarketQuotation = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 15
        newRow.reserveCalcValue = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 16
        newRow.reserveCreation = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 17
        newRow.recovery = getNumber(row.cell[indexCell].text())

        rows.add(newRow)
    }
    dataRowHelper.save(rows)

    if (xml.rowTotal.size() == 1) {
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 6
        total.prev = getNumber(row.cell[6].text())

        // графа 7
        total.current = getNumber(row.cell[7].text())

        // графа 8
        total.reserveCalcValuePrev = getNumber(row.cell[8].text())

        // графа 9
        total.cost = getNumber(row.cell[9].text())

        // графа 14
        total.costOnMarketQuotation = getNumber(row.cell[14].text())

        // графа 15
        total.reserveCalcValue = getNumber(row.cell[15].text())

        // графа 16
        total.reserveCreation = getNumber(row.cell[16].text())

        // графа 17
        total.recovery = getNumber(row.cell[17].text())

        return total
    } else {
        return null
    }
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    try {
        return new BigDecimal(tmp)
    } catch (Exception e) {
        throw new Exception("Значение \"$value\" не может быть преобразовано в число. " + e.message)
    }
}

/**
 * Получить id справочника.
 *
 * @param ref_id идентификатор справончика
 * @param code атрибут справочника
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return
 */
def getRecords(def ref_id, String code, String value, Date date, def cache) {
    String filter = code + " like '"+ value.replaceAll(' ', '')+ "%'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter] != null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1){
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось найти запись в справочнике (id=$ref_id) с атрибутом $code равным $value!")
    return null
}


/**
 * Получить буквенный код валюты
 */
def getCurrency(def currencyCode) {
    return getRefBookValue(15, currencyCode)?.CODE_2?.getStringValue()
}

/**
 * Получить признак ценной бумаги
 */
def getSign(def sign) {
    return getRefBookValue(62, sign)?.CODE?.getStringValue()
}

/**
 * Проверка валюты на рубли
 */
def isRubleCurrency(def currencyCode) {
    return getRefBookValue(15, currencyCode)?.CODE?.stringValue == '810'
}

/**
 * Получить курс валюты
 */
def getCourse(def currency, def date) {
    if (currency != null && date != null)
        if (isRubleCurrency(currency)) {
            return 1
        } else {
            return getRefBookRecord(22, 'CODE_NUMBER', "${currency}", date, -1, null, true)?.RATE.getNumberValue()
        }
    return null
}

def getNewRow() {
    def newRow = formData.createDataRow()
    def columns
    if (isBalancePeriod) {
        // все строки, кроме графы 1
        columns = ['issuer', 'regNumber', 'tradeNumber', 'currency', 'prev', 'current', 'reserveCalcValuePrev',
                'cost', 'signSecurity', 'marketQuotation', 'rubCourse', 'marketQuotationInRub',
                'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']
    } else {
        columns = ['currency', 'issuer', 'regNumber', 'tradeNumber', 'prev', 'current', 'reserveCalcValuePrev',
                'cost', 'signSecurity', 'marketQuotation', 'rubCourse', 'costOnMarketQuotation',
                'reserveCalcValue', 'reserveCreation', 'recovery']
    }
    columns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
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

/**
 * Расчетать, проверить и сравнить итоги.
 * // TODO посмотреть проверку
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def totalColumns = [6:'prev', 7:'current', 8: 'reserveCalcValuePrev', 9:'cost', 14:'costOnMarketQuotation', 15:'reserveCalcValue',
            16 : 'reserveCreation', 17 : 'recovery']
    def totalCalc
    def errorColums = []
    if (totalCalc != null) {
        totalColumns.each { index, columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColums.add(index)
            }
        }
    }
    if (!errorColums.isEmpty()) {
        def columns = errorColums.join(', ')
        logger.error("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.issuer = "Общий итог"
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def msg) {
    if (isBalancePeriod) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}

/**
 * Получение строки по номеру
 */
DataRow<Cell> getRow(int i, def dataRows) {
    if ((i < dataRows.size()) && (i >= 0)) {
        return dataRows.get(i)
    } else {
        return null
    }
}