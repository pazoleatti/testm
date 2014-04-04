package form_template.income.rnu27.v2014

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * (РНУ-27) Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных
 *              и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения
 * formTemplateId=326
 *
 * ЧТЗ http://conf.aplana.com/pages/viewpage.action?pageId=8588102 ЧТЗ_сводные_НФ_Ф2_Э1_т2.doc
 *
 * графа 1  - число  number                 № пп
 * графа 2  - строка issuer                 Эмитент
 * графа 3  - строка regNumber              Государственный регистрационный номер
 * графа 4  - строка tradeNumber            Номер сделки
 * графа 5  - строка currency               Валюта выпуска облигации (справочник)
 * графа 6  - число  prev                   Размер лота на отчётную дату по депозитарному учёту (шт.). Предыдущую
 * графа 7  - число  current                Размер лота на отчётную дату по депозитарному учёту (шт.). Текущую
 * графа 8  - число  reserveCalcValuePrev   Расчётная величина резерва на предыдущую отчётную дату (руб.коп.)
 * графа 9  - число  cost                   Стоимость по цене приобретения (руб.коп.)
 * графа 10 - строка signSecurity           Признак ценной бумаги на текущую отчётную дату (справочник)
 * графа 11 - число  marketQuotation        Quotation Рыночная котировка одной ценной бумаги в иностранной валюте
 * графа 12 - число  rubCourse              Курс рубля к валюте рыночной котировки
 * графа 13 - число  marketQuotationInRub   Рыночная котировка одной ценной бумаги в рублях
 * графа 14 - число  costOnMarketQuotation  costOnMarketQuotation
 * графа 15 - число  reserveCalcValue       Расчетная величина резерва на текущую отчётную дату (руб.коп.)
 * графа 16 - число  reserveCreation        Создание резерва (руб.коп.)
 * графа 17 - число  recovery               Восстановление резерва (руб.коп.)
 *
 * @author ekuvshinov
 * @author lhaziev
 */

// Признак периода ввода остатков
@Field
def isBalancePeriod
isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

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
        if (isBalancePeriod) {
            columns = allColumns - ['number', 'regNumber', 'currency']
        } else {
            columns = editableColumns
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
        prevPeriodCheck()
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.MIGRATION:
        importData()
        if (!hasError()) {
            def data = formDataService.getDataRowHelper(formData)
            def dataRows = data.getAllCached()
            addAllStatic(dataRows)
            def total = getCalcTotalRow(dataRows)
            dataRows.add(total)
            data.save(dataRows)
        }
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

// Редактируемые атрибуты
@Field
def editableColumns = allColumns - ['number', 'fix', 'regNumber', 'currency', 'reserveCalcValuePrev',
        'marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number']

// Группируемые атрибуты
@Field
def groupColumns = ['issuer', 'regNumber', 'tradeNumber']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'issuer', 'tradeNumber', 'reserveCalcValuePrev',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// Атрибуты итоговых строк для которых вычисляются суммы
@Field
def totalColumns = ['prev', 'current', 'reserveCalcValuePrev', 'cost',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// алиасы графов для арифметической проверки
@Field
def arithmeticCheckAlias = ['reserveCalcValuePrev', 'marketQuotation', 'rubCourse', 'marketQuotationInRub',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'recovery']

// Дата окончания отчетного периода
@Field
def endDate = null

/**
 * Обертки методов
 */
// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

/**
 * Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
 */
void prevPeriodCheck() {
    if (formData.kind == FormDataKind.PRIMARY && !isBalancePeriod && !isConsolidated
            && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        throw new ServiceException("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
    }
}

/**
 * @param list
 * @param label
 * @return
 */
def getMessageLP1(def list, def label) {
    StringBuilder sb = new StringBuilder(label)
    for (tradeNumber in list) {
        sb.append(" " + tradeNumber.toString() + ",")
    }
    return sb.substring(0, sb.length() - 1).toString()
}

/**
 * Расчеты всех автоматически заполняемых граф
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    // данные предыдущего отчетного периода
    def dataPrevRows = null
    if (formData.kind == FormDataKind.PRIMARY) {
        def formPrev = getFormPrev()
        def dataPrev = formPrev != null ? formDataService.getDataRowHelper(formPrev) : null
        dataPrevRows = dataPrev?.getAllCached()
    }

    // номер последний строки предыдущей формы
    def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

    for (row in dataRows) {
        row.number = ++number
        if (!isBalancePeriod && formData.kind == FormDataKind.PRIMARY) {
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

/**
 * Расчеты. Алгоритмы заполнения полей формы после импорта.
 */
void calcAfterImport() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (row in dataRows) {
        // Проверим чтобы человек рукамми ввёл всё что необходимо
        def requiredColumns = ['issuer', 'regNumber', 'tradeNumber', 'currency']
        checkNonEmptyColumns(row, row.getIndex(), requiredColumns, logger, true)
    }
    if (!hasError()) {
        def formPrev = getFormPrev()
        def dataPrevRows = formPrev != null ? formDataService.getDataRowHelper(formPrev)?.getAllCached() : null

        for (DataRow row in dataRows) {
            row.reserveCalcValuePrev = calc8(row, dataPrevRows)
            row.costOnMarketQuotation = calc14(row)
            row.reserveCalcValue = calc15(row)
            row.reserveCreation = calc16(row)
            row.recovery = calc17(row)
        }
        dataRowHelper.save(dataRows)
    }
}

/**
 * Логические проверки
 * @return
 */
def logicCheck() {
    def data = formDataService.getDataRowHelper(formData)
    def dataRows = data.getAllCached()
    def formPrev = null
    def dataPrev = null
    def dataPrevRows = null
    if (formData.kind == FormDataKind.PRIMARY) {
        formPrev = getFormPrev()
        dataPrev = formPrev != null ? formDataService.getDataRowHelper(formPrev) : null
        dataPrevRows = dataPrev?.getAllCached()
    }

    def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    for (DataRow row in data.getAllCached()) {
        if (row?.getAlias() == null) {
            number++
            def index = row.getIndex()
            def errorMsg = "Строка ${index}: "

            if (row.current == 0) {
                // 4. LC Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.reserveCalcValuePrev != row.recovery) {
                    logger.warn(errorMsg + "Графы 8 и 17 неравны!")
                }
                // 5. LC • Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7 = 0)
                if (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0) {
                    logger.warn(errorMsg + "Графы 9, 14 и 15 ненулевые!")
                }
            }
            // 6. LC • Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6 = 0)
            if (row.prev == 0 && (row.reserveCalcValuePrev != 0 || row.recovery != 0)) {
                loggerError(errorMsg + "Графы 8 и 17 ненулевые!")
            }
            // 7. LC • Проверка необращающихся облигаций (графа 10 = «x»)
            if (getSign(row.signSecurity) == "-" && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                logger.warn(errorMsg + "Облигации необращающиеся, графы 15 и 16 ненулевые!")
            }
            if (getSign(row.signSecurity) == "+") {
                // 8. LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev > 0 && row.recovery != 0) {
                    loggerError(errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
                // 9. LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev < 0 && row.reserveCreation != 0) {
                    loggerError(errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
                // 10. LC • Проверка создания (восстановления) резерва по обращающимся облигациям (графа 10 = «+»)
                if (row.reserveCalcValue - row.reserveCalcValuePrev == 0 && (row.reserveCreation != 0 || row.recovery != 0)) {
                    loggerError(errorMsg + "Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!")
                }
            }
            // 11. LC • Проверка корректности формирования резерва
            if (row.reserveCalcValuePrev != null && row.reserveCreation != null && row.reserveCalcValue != null && row.recovery != null
                    && row.reserveCalcValuePrev + row.reserveCreation != row.reserveCalcValue + row.recovery) {
                loggerError(errorMsg + "Резерв сформирован неверно!")
            }
            // 12. LC • Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && (row.current <= 0 || row.cost <= 0 || row.costOnMarketQuotation <= 0 || row.reserveCalcValue <= 0)) {
                logger.warn(errorMsg + "Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!")
            }
            // 13. LC • Проверка корректности заполнения РНУ
            if (formData.kind == FormDataKind.PRIMARY && formPrev != null) {
                for (DataRow rowPrev in dataPrevRows) {
                    if (rowPrev.getAlias() == null && row.tradeNumber == rowPrev.tradeNumber && row.prev != rowPrev.current) {
                        logger.warn(errorMsg + "РНУ сформирован некорректно! Не выполняется условие: Если «графа 4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то «графа 6» = «графа 7» формы РНУ-27 за предыдущий отчётный период")
                    }
                }
            }
            // 14. LC • Проверка корректности заполнения РНУ
            if (formData.kind == FormDataKind.PRIMARY && formPrev != null) {
                for (DataRow rowPrev in dataPrevRows) {
                    if (rowPrev.getAlias() == null && row.tradeNumber == rowPrev.tradeNumber && row.reserveCalcValuePrev != rowPrev.reserveCalcValue) {
                        loggerError(errorMsg + "РНУ сформирован некорректно! Не выполняется условие: Если  «графа 4» = «графа 4» формы РНУ-27 за предыдущий отчётный период, то графа 8 = графа 15 формы РНУ-27 за предыдущий отчётный период")
                    }
                }
            }

            // 1. LC Проверка на заполнение поля «<Наименование поля>»
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod)

            // 2. Проверка на уникальность поля «№ пп»
            if (number != row.number) {
                logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
            }

            if (getCurrency(row.issuer) == 'RUR') {
                // 17. LC Проверка графы 11
                if (row.marketQuotation != null) {
                    loggerError(errorMsg + "Неверно заполнена графа «Рыночная котировка одной ценной бумаги в иностранной валюте»!")
                }
                // 18. LC Проверка графы 12
                if (row.rubCourse != null) {
                    loggerError(errorMsg + "Неверно заполнена графы «Курс рубля к валюте рыночной котировки»!")
                }
            }

            if (formData.kind == FormDataKind.PRIMARY && !isBalancePeriod) {
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
                    def regNumber = getRefBookValue(84, getPrevRowWithoutAlias(dataRows, row)?.issuer)?.REG_NUM?.stringValue
                    loggerError("Итоговые значения по «" + regNumber + "» рассчитаны неверно в графе «${getColumnName(row, column)}»!")
                }
            }
        }

        // LC 21
        if (row.getAlias() != null && row.getAlias().indexOf('itogoIssuer') != -1) {
            srow = calcItogIssuer(dataRows.indexOf(row))

            for (column in totalColumns) {
                if (row.get(column) != srow.get(column)) {
                    def issuer = getRefBookValue(84, getPrevRowWithoutAlias(dataRows, row)?.issuer)?.ISSUER?.numberValue
                    loggerError("Итоговые значения для «" + issuer + "» рассчитаны неверно в графе «${getColumnName(row, column)}»!")
                }
            }
        }
    }

    // LC • Проверка корректности заполнения РНУ
    if (formData.kind == FormDataKind.PRIMARY && dataPrev != null && checkAlias(dataPrevRows, 'total') && checkAlias(dataRows, 'total')) {
        def itogoPrev = data.getDataRow(dataPrevRows, 'total')
        def itogo = data.getDataRow(dataRows, 'total')
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
    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod)

    /** 3. LC Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде (выполняется один раз для всего экземпляра)
     * http://jira.aplana.com/browse/SBRFACCTAX-2609
     */
    if (formData.kind == FormDataKind.PRIMARY && dataPrev != null) {
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
            logger.warn(getMessageLP1(notFound, "Отсутствуют строки с номерами сделок:"))
        }
        if (!foundMany.isEmpty()) {
            logger.warn(getMessageLP1(foundMany, "Существует несколько строк с номерами сделок:"))
        }
    }
}

/**
 * Импорт данных
 */
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 17, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Эмитент',
            (xml.row[0].cell[3]): 'Государственный регистрационный номер',
            (xml.row[0].cell[4]): 'Номер сделки',
            (xml.row[0].cell[5]): 'Валюта выпуска облигации',
            (xml.row[0].cell[6]): 'Размер лота на отчётную дату по депозитарному учёту (шт.).',
            (xml.row[0].cell[8]): 'Расчётная величина резерва на предыдущую отчётную дату (руб.коп.)',
            (xml.row[0].cell[9]): 'Стоимость по цене приобретения (руб.коп.)',
            (xml.row[0].cell[10]): 'Признак ценной бумаги на текущую отчётную дату',
            (xml.row[0].cell[11]): 'Рыночная котировка одной облигации в иностранной валюте',
            (xml.row[0].cell[12]): 'Курс рубля к валюте рыночной котировки',
            (xml.row[0].cell[13]): 'Рыночная котировка одной облигации в рублях',
            (xml.row[0].cell[14]): 'Стоимость по рыночной котировке (руб.коп.)',
            (xml.row[0].cell[15]): 'Расчетная величина резерва на текущую отчётную дату (руб.коп.)',
            (xml.row[0].cell[16]): 'Создание резерва (руб.коп.)',
            (xml.row[0].cell[17]): 'Восстановление резерва (руб.коп.)',
            (xml.row[1].cell[6]): 'Предыдущую',
            (xml.row[1].cell[7]): 'Текущую',
            (xml.row[2].cell[0]): '1'
    ]
    (2..17).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
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
 * Заполнить форму данными
 * @param xml - xml с данными
 * @param headRowCount - количество строк шапки, указывается как n-1
 */
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        /* Пропуск строк шапок */
        if (xmlIndexRow <= headRowCount) {
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
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        /* Графа 1 */
        newRow.number = parseNumber(row.cell[0].text(), xlsIndexRow, 0 + colOffset, logger, false)

        /* Графа 2 */
        newRow.issuer = getRecordIdImport(84, 'ISSUER', row.cell[2].text(), xlsIndexRow, 2 + colOffset, false)

        /* Графа 3 */
        getRecordIdImport(84, 'REG_NUM', row.cell[3].text(), xlsIndexRow, 3 + colOffset, false)

        /* Графа 4 */
        newRow.tradeNumber = row.cell[4].text()

        /* Графа 5 */
        getRecordIdImport(15, 'CODE_2', row.cell[5].text(), xlsIndexRow, 5 + colOffset, false)

        /* Графа 6 */
        newRow.prev = parseNumber(row.cell[6].text(), xlsIndexRow, 6 + colOffset, logger, false)

        /* Графа 7 */
        newRow.current = parseNumber(row.cell[7].text(), xlsIndexRow, 7 + colOffset, logger, false)

        /* Графа 8 */
        newRow.reserveCalcValuePrev = parseNumber(row.cell[8].text(), xlsIndexRow, 8 + colOffset, logger, false)

        /* Графа 9 */
        newRow.cost = parseNumber(row.cell[9].text(), xlsIndexRow, 9 + colOffset, logger, false)

        /* Графа 10 */
        newRow.signSecurity = getRecordIdImport(62, 'CODE', row.cell[10].text(), xlsIndexRow, 10 + colOffset, false)

        /* Графа 11 */
        newRow.marketQuotation = parseNumber(row.cell[11].text(), xlsIndexRow, 11 + colOffset, logger, false)

        /* Графа 12 */
        newRow.rubCourse = parseNumber(row.cell[12].text(), xlsIndexRow, 12 + colOffset, logger, false)

        /* Графа 14 */
        newRow.costOnMarketQuotation = parseNumber(row.cell[14].text(), xlsIndexRow, 14 + colOffset, logger, false)

        /* Графа 15 */
        newRow.reserveCalcValue = parseNumber(row.cell[15].text(), xlsIndexRow, 15 + colOffset, logger, false)

        /* Графа 16 */
        newRow.reserveCreation = parseNumber(row.cell[16].text(), xlsIndexRow, 16 + colOffset, logger, false)

        /* Графа 17 */
        newRow.recovery = parseNumber(row.cell[17].text(), xlsIndexRow, 17 + colOffset, logger, false)

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
        dataRows = formDataService.getDataRowHelper(formData)?.getAllCached()
    }
    for (int i = 0; i < dataRows.size(); i++) {
        DataRow<Cell> row = getRow(i, dataRows)
        DataRow<Cell> nextRow = getRow(i + 1, dataRows)
        int j = 0

        // графа 3 - грн - regNumber
        def regNum = getRefBookValue(84, row?.issuer)?.REG_NUM?.stringValue
        def nextRegNum = getRefBookValue(84, nextRow?.issuer)?.REG_NUM?.stringValue
        if (row.getAlias() == null && nextRow == null || regNum != nextRegNum) {
            def itogRegNumberRow = calcItogRegNumber(i)
            dataRows.add(i + 1, itogRegNumberRow)
            j++
        }

        // графа 2 - эмитент - issuer
        def issuer = getRefBookValue(84, row?.issuer)?.ISSUER?.numberValue
        def nextIssuer = getRefBookValue(84, nextRow?.issuer)?.ISSUER?.numberValue
        if (row.getAlias() == null && nextRow == null || issuer != nextIssuer) {
            def itogIssuerRow = calcItogIssuer(i)
            dataRows.add(i + 2, itogIssuerRow)
            j++
        }
        i += j  // Обязательно чтобы избежать зацикливания в простановке
    }
}

/**
 * Расчет итога Эмитента
 * @author ivildanov
 */
def calcItogIssuer(int i) {
    def newRow = formData.createDataRow()
    newRow.getCell('fix').colSpan = 3
    newRow.setAlias('itogoIssuer#'.concat(i ? i.toString() : ""))

    String tIssuer = 'Эмитент'
    for (int j = i; j >= 0; j--) {
        if (getRow(j).getAlias() == null) {
            tIssuer = getRefBookValue(84, getRow(j).issuer)?.ISSUER?.numberValue
            break
        }
    }

    newRow.fix = tIssuer?.concat(' Итог')

    for (column in totalColumns) {
        newRow.getCell(column).setValue(new BigDecimal(0), null)
    }

    for (int j = i; j >= 0; j--) {
        def srow = getRow(j)

        if (srow.getAlias() == null) {
            if (((((String) getRefBookValue(84, srow.issuer)?.ISSUER?.numberValue) != tIssuer))) {
                break
            }

            for (column in totalColumns) {
                if (srow.get(column) != null) {
                    newRow.getCell(column).setValue(newRow.getCell(column).value + (BigDecimal) srow.get(column), null)
                }
            }
        }

    }
    setTotalStyle(newRow)
    return newRow
}

/**
 * Расчет итога ГРН
 * @author ivildanov
 */
def calcItogRegNumber(int i) {
    // создаем итоговую строку ГРН
    def newRow = formData.createDataRow()
    newRow.getCell('fix').colSpan = 3
    newRow.setAlias('itogoRegNumber#'.concat(i ? i.toString() : ""))

    String tRegNumber = 'ГРН'
    for (int j = i; j >= 0; j--) {
        if (getRow(j).getAlias() == null) {
            tRegNumber = getRefBookValue(84, getRow(j).issuer)?.REG_NUM?.stringValue
            break
        }
    }

    newRow.fix = tRegNumber?.concat(' Итог')

    for (column in totalColumns) {
        newRow[column] = 0
    }

    // идем от текущей позиции вверх и ищем нужные строки
    for (int j = i; j >= 0; j--) {
        def srow = getRow(j)

        if (srow.getAlias() == null) {
            if (getRefBookValue(84, srow.issuer)?.REG_NUM?.stringValue != tRegNumber) {
                break
            }

            for (column in totalColumns) {
                if (srow[column] != null) {
                    newRow[column] += (BigDecimal) srow[column]
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
 * Расчет графы 8
 */
BigDecimal calc8(DataRow row, def dataPrevRows) {
    if (isConsolidated) {
        return row.reserveCalcValuePrev
    }
    // Расчет графы 8 в соответсвие коментарию Аванесова http://jira.aplana.com/browse/SBRFACCTAX-2562
    def temp = new BigDecimal(0)
    tempCount = 0

    if (dataPrevRows != null) {
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
        return BigDecimal.ZERO
    }
}

/**
 * Расчет графы 11
 * @author ivildanov
 */
BigDecimal calc11(DataRow row) {
    if (getCurrency(row.issuer)?.contains("RUR")) {
        return null
    }
    return row.marketQuotation
}

/**
 * Расчет графы 12
 */
BigDecimal calc12(DataRow row) {
    if (getCurrency(row.issuer)?.contains("RUR")) {
        return null
    }
    return row.rubCourse
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
        return BigDecimal.ZERO
    } else {
        return roundValue(row.current * row.marketQuotationInRub, 2)
    }
}

/**
 * Расчет графы 15
 * @author ivildanov
 */
BigDecimal calc15(DataRow row) {
    BigDecimal a = (row.cost ?: 0)

    if (getSign(row.signSecurity) == "+" && a - row.costOnMarketQuotation > 0) {
        return a - row.costOnMarketQuotation
    } else {
        return BigDecimal.ZERO
    }
}

/**
 * Расчет графы 16
 * @author ivildanov
 */
BigDecimal calc16(DataRow row) {
    if (row.reserveCalcValue != null && row.reserveCalcValuePrev != null) {
        if (row.reserveCalcValue - row.reserveCalcValuePrev > 0) {
            return roundValue((row.marketQuotation ?: 0) - row.prev, 2)
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
    def reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
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
 * Получить числовое значение.
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
    String filter = code + " like '" + value.replaceAll(' ', '') + "%'"
    if (cache[ref_id] != null) {
        if (cache[ref_id][filter] != null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось найти запись в справочнике «" + refBookFactory.get(ref_id).getName() + "» с атрибутом $code равным $value!")
    return null
}

/**
 * Получить буквенный код валюты
 */
def getCurrency(def issuerRecordId) {
    def currencyCode = getRefBookValue(84, issuerRecordId)?.CODE_CUR?.stringValue
    def recordId = getRecords(15, 'CODE', currencyCode, getReportPeriodEndDate(), recordCache)
    def value = getRefBookValue(15, recordId)?.CODE_2?.stringValue
    return value
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

def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
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
 * Рассчитать, проверить и сравнить итоги.
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    def totalColumns = [6: 'prev', 7: 'current', 8: 'reserveCalcValuePrev', 9: 'cost', 14: 'costOnMarketQuotation',
            15: 'reserveCalcValue', 16: 'reserveCreation', 17: 'recovery']
    def totalCalc = getCalcTotalRow(dataRows)
    def errorColumns = []
    if (totalCalc != null) {
        totalColumns.each { index, columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColumns.add(index)
            }
        }
    }
    if (!errorColumns.isEmpty()) {
        def columns = errorColumns.join(', ')
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
    newRow.getCell("fix").colSpan = 2
    newRow.fix = "Общий итог"
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

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}