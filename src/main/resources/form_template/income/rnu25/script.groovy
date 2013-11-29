package form_template.income.rnu25

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Форма "(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения".
 *
 * TODO:
 *      - в перечне полей графа 9 - необязательная, в логических проверках входит в обязательные поля
 *              http://jira.aplana.com/browse/SBRFACCTAX-4871
 *      - уточнить у аналитика когда проверять наличие формы предыдущего периода (только перед вычислениями или перед проверками и импорте тоже)
 *
 * @author rtimerbaev
 */

// графа 1  - rowNumber
// графа -  - fix
// графа 2  - regNumber
// графа 3  - tradeNumber
// графа 4  - lotSizePrev
// графа 5  - lotSizeCurrent
// графа 6  - reserve
// графа 7  - cost
// графа 8  - signSecurity          атрибут 621 CODE "Код признака" - справочник 62 "Признаки ценных бумаг"
// графа 9  - marketQuotation
// графа 10 - costOnMarketQuotation
// графа 11 - reserveCalcValue
// графа 12 - reserveCreation
// графа 13 - reserveRecovery

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod
isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT :
        importData()
        calc()
        break
    case FormDataEvent.MIGRATION :
        migration()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// все атрибуты
@Field
def allColumns = ['rowNumber', 'fix', 'regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent',
        'reserve', 'cost', 'signSecurity', 'marketQuotation', 'costOnMarketQuotation',
        'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Редактируемые атрибуты (графа 2..5, 7..9)
@Field
def editableColumns = ['regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', 'cost', 'signSecurity', 'marketQuotation']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты (графа 2, 3)
@Field
def groupColumns = ['regNumber', 'tradeNumber']

// Проверяемые на пустые значения атрибуты (графа 1..3, 5..13)
@Field
def nonEmptyColumns = ['rowNumber', 'regNumber', 'tradeNumber', 'lotSizeCurrent', 'reserve',
        'cost', 'signSecurity', 'costOnMarketQuotation',
        'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4..7, 10..13)
@Field
def totalSumColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
        'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

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

/** Расчеты. Алгоритмы заполнения полей формы. */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить строку "итого" и "итого по ГРН: ..."
    deleteAllAliased(dataRows)

    def isImport = (formDataEvent == FormDataEvent.IMPORT)

    // отсортировать/группировать
    if (!isImport) {
        sortRows(dataRows, groupColumns)
    }

    // список групп кодов классификации для которых надо будет посчитать суммы
    def totalGroupsName = []

    def formDataOld = getFormDataOld()
    def dataRowsOld = (formDataOld != null ? formDataService.getDataRowHelper(formDataOld)?.allCached : null)

    // получить номер последний строки предыдущей формы если это не событие импорта
    def rowNumber = (isImport ? 0 : getPrevRowNumber(dataRowsOld))
    dataRows.each { row ->
        if (!isImport) {
            // графа 1
            row.rowNumber = ++rowNumber
        }

        if (!isBalancePeriod) {
            // графа 6
            row.reserve = calc6(dataRowsOld, row)

            // графа 10
            row.costOnMarketQuotation = calc10(row)

            // графа 11
            def sign = getSign(row.signSecurity)
            row.reserveCalcValue = calc11(row, sign)

            // графа 12
            row.reserveCreation = calc12(row)

            // графа 13
            row.reserveRecovery = calc13(row)
        }
        // для итоговых значений по ГРН
        if (!totalGroupsName.contains(row.regNumber)) {
            totalGroupsName.add(row.regNumber)
        }
    }

    // добавить строку "итого"
    def totalRow = getCalcTotalRow(dataRows)
    dataRows.add(totalRow)
    // dataRowHelper.insert(totalRow, dataRows.size() + 1)
    if (dataRows.size() == 1) {
        dataRowHelper.save(dataRows)
        return
    }

    // обновить индексы строк
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
    // итоговые значения по ГРН
    def i = 0
    for (def codeName : totalGroupsName) {
        // получить строки группы
        def rows = getGroupRows(dataRows, codeName)
        // получить алиас для подитоговой строки по ГРН
        def totalRowAlias = 'total' + rows[0].rowNumber.toString()
        // сформировать подитоговую строку ГРН с суммами
        def subTotalRow = getCalcSubtotalsRow(rows, codeName, totalRowAlias)
        // получить индекс последней строки в группе
        def lastRowIndex = rows[rows.size() - 1].getIndex() + i
        // вставить строку с итогами по ГРН
        dataRows.add(lastRowIndex, subTotalRow)
        i++
    }
    dataRowHelper.save(dataRows)
}

/** Логические проверки. */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def formDataOld = getFormDataOld()
    def dataRowsOld = (formDataOld != null ? formDataService.getDataRowHelper(formDataOld)?.allCached : null)
    if (dataRowsOld != null && !dataRowsOld.isEmpty()) {
        // 1. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 11)
        //      в текущем отчетном периоде (выполняется один раз для всего экземпляра)
        def count
        def missContract = []
        def severalContract = []
        dataRowsOld.each { prevRow ->
            if (prevRow.getAlias() != null && prevRow.reserveCalcValue > 0) {
                count = 0
                dataRows.each { row ->
                    if (row.tradeNumber == prevRow.tradeNumber) {
                        count += 1
                    }
                }
                if (count == 0) {
                    missContract.add(prevRow.tradeNumber)
                } else if (count > 1) {
                    severalContract.add(prevRow.tradeNumber)
                }
            }
        }
        if (!missContract.isEmpty()) {
            def message = missContract.join(', ')
            logger.warn("Отсутствуют строки с номерами сделок: $message!")
        }
        if (!severalContract.isEmpty()) {
            def message = severalContract.join(', ')
            logger.warn("Существует несколько строк с номерами сделок: $message!")
        }
    }

    // алиасы графов для арифметической проверки (графа )
    def arithmeticCheckAlias = ['reserve', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    // список групп кодов классификации для которых надо будет посчитать суммы
    def totalGroupsName = []

    def rowNumber = getPrevRowNumber(dataRowsOld)
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 6, 13)
        if (row.lotSizeCurrent == 0 && row.reserve != row.reserveRecovery) {
            logger.warn(errorMsg + 'графы 6 и 13 неравны!')
        }

        // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 7, 10, 11)
        if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
            logger.warn(errorMsg + 'графы 7, 10 и 11 ненулевые!')
        }

        // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 4, 6, 13)
        if (row.lotSizePrev == 0 && (row.reserve != 0 || row.reserveRecovery != 0)) {
            loggerError(errorMsg + 'графы 6 и 13 ненулевые!')
        }

        // 5. Проверка необращающихся акций (графа 8, 11, 12)
        def sign = getSign(row.signSecurity)
        if (sign == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
            logger.warn(errorMsg + 'облигации необращающиеся, графы 11 и 12 ненулевые!')
        }

        // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve > 0 && row.reserveRecovery != 0) {
            loggerError(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
        }

        // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 12)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve < 0 && row.reserveCreation != 0) {
            loggerError(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
        }

        // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve == 0 &&
                (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
            loggerError(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
        }

        // 9. Проверка на положительные значения при наличии созданного резерва
        if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
            logger.warn(errorMsg + 'резерв сформирован. Графы 5, 7, 10 и 11 неположительные!')
        }

        // 10. Проверка корректности создания резерва (графа 6, 11, 12, 13)
        if (row.reserve != null && row.reserveCreation != null &&
                row.reserveCalcValue != null && row.reserveRecovery != null &&
                row.reserve + row.reserveCreation != row.reserveCalcValue + row.reserveRecovery) {
            loggerError(errorMsg + 'резерв сформирован некорректно!')
        }

        // 11. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 4, 5 (за предыдущий период) )
        if (!isBalancePeriod && !isConsolidated && checkOld(row, 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', dataRowsOld)) {
            def curCol = 3
            def curCol2 = 4
            def prevCol = 3
            def prevCol2 = 5
            loggerError("РНУ сформирован некорректно! " + errorMsg + "Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-25 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-25 за предыдущий отчётный период.")
        }

        // 12. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 6, 11 (за предыдущий период) )
        if (!isBalancePeriod && !isConsolidated && checkOld(row, 'tradeNumber', 'reserve', 'reserveCalcValue', dataRowsOld)) {
            def curCol = 3
            def curCol2 = 3
            def prevCol = 6
            def prevCol2 = 11
            loggerError("РНУ сформирован некорректно! " + errorMsg + "Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-25 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-25 за предыдущий отчётный период.")
        }

        // 15. Обязательность заполнения поля графы 1..3, 5..13
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 16. Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.rowNumber) {
            logger.error('Нарушена уникальность номера по порядку!')
        }

        // 17. Арифметические проверки граф 6, 10..13
        if (!isBalancePeriod) {
            needValue['reserve'] = calc6(dataRowsOld, row)
            needValue['costOnMarketQuotation'] = calc10(row)
            needValue['reserveCalcValue'] = calc11(row, sign)
            needValue['reserveCreation'] = calc12(row)
            needValue['reserveRecovery'] = calc13(row)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, false)
        }

        // 18. Проверка итоговых значений по ГРН
        if (!totalGroupsName.contains(row.regNumber)) {
            totalGroupsName.add(row.regNumber)
        }

        // Проверки соответствия НСИ
        checkNSI(62, row, 'signSecurity')
    }

    if (dataRowsOld != null) {
        def totalRow = dataRowHelper.getDataRow(dataRows, 'total')
        def totalRowOld = dataRowHelper.getDataRow(dataRowsOld, 'total')

        // 13. Проверка корректности заполнения РНУ (графа 4, 5 (за предыдущий период))
        if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
            def curCol = 4
            def prevCol = 5
            loggerError("РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе $curCol = «Общий итог» по графе $prevCol формы РНУ-25 за предыдущий отчётный период.")
        }

        // 14. Проверка корректности заполнения РНУ (графа 6, 11 (за предыдущий период))
        if (totalRow.reserve != totalRowOld.reserveCalcValue) {
            def curCol = 6
            def prevCol = 11
            loggerError("РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе $curCol = «Общий итог» по графе $prevCol формы РНУ-25 за предыдущий отчётный период.")
        }
    }

    // 17. Проверка итоговых значений по ГРН
    for (def codeName : totalGroupsName) {
        // получить строки группы
        def rows = getGroupRows(dataRows, codeName)
        // получить алиас для подитоговой строки по ГРН
        def totalRowAlias = 'total' + rows[0].rowNumber.toString()
        // получить посчитанную строку с итогами по ГРН
        def row
        try {
            row = dataRowHelper.getDataRow(dataRows, totalRowAlias)
        } catch(IllegalArgumentException e) {
            loggerError("Итоговые значения по ГРН $codeName не рассчитаны! Необходимо рассчитать данные формы.")
            continue
        }
        // сформировать подитоговую строку ГРН с суммами
        def tmpRow = getCalcSubtotalsRow(rows, codeName, totalRowAlias)

        // сравнить строки
        if (isDiffRow(row, tmpRow, totalSumColumns)) {
            loggerError("Итоговые значения по ГРН $codeName рассчитаны неверно!")
        }
    }

    // 18. Проверка итогового значений по всей форме
    def totalRow = dataRowHelper.getDataRow(dataRows, 'total')
    def tmpTotalRow = getCalcTotalRow(dataRows)
    if (isDiffRow(totalRow, tmpTotalRow, totalSumColumns)) {
        loggerError('Итоговые значения рассчитаны неверно!')
    }
}

/** Получение импортируемых данных. */
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

        // рассчитать, проверить и сравнить итоги
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
    }
}

void migration() {
    importData()
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.allCached
        def total = getCalcTotalRow(dataRows)
        dataRowHelper.insert(total, dataRows.size() + 1)
    }
}

/*
 * Вспомогательные методы.
 */

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

/**
 * Сверить данные с предыдущим периодом.
 *
 * @param row строка нф текущего периода
 * @param likeColumnName псевдоним графы по которому ищутся соответствующиеся строки
 * @param curColumnName псевдоним графы текущей нф для второго условия
 * @param prevColumnName псевдоним графы предыдущей нф для второго условия
 * @param dataRowsOld строки нф предыдущего периода
 */
def checkOld(def row, def likeColumnName, def curColumnName, def prevColumnName, def dataRowsOld) {
    if (dataRowsOld == null) {
        return false
    }
    if (row.getCell(likeColumnName).value == null) {
        return false
    }
    for (def prevRow : dataRowsOld) {
        if (prevRow.getAlias() != null || prevRow.getAlias() != '') {
            continue
        }
        if (row.getCell(likeColumnName).value == prevRow.getCell(likeColumnName).value &&
                row.getCell(curColumnName).value != prevRow.getCell(prevColumnName).value) {
            return true
        }
    }
}

/** Получить данные за предыдущий отчетный период. */
def getFormDataOld() {
    if (isBalancePeriod || isConsolidated) {
        return null
    }
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-25 за предыдущий отчетный период
    if (reportPeriodOld != null) {
        return formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }
    return null
}

/** Получить общую итоговую строку с суммами. */
def getCalcTotalRow(def dataRows) {
    getTotalRow(dataRows, 'Общий итог', 'total')
}

/**
 * Получить подитоговую строку ГРН по коду классификации дохода.
 *
 * @param dataRows строки формы
 * @param regNumber код классификации дохода
 * @param totalRowAlias псевдоним сформированной строки
 */
def getCalcSubtotalsRow(def dataRows, def regNumber, def totalRowAlias) {
    return getTotalRow(dataRows, regNumber.trim() + ' итог', totalRowAlias)
}

/**
 * Сформировать итоговую строку с суммами.
 *
 * @param dataRows строки формы
 * @param regNumberValue значение графы "код классификации дохода"
 * @param alias алиас сформированной строки
 */
def getTotalRow(def dataRows, def regNumberValue, def alias) {
    def newRow = formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = regNumberValue
    newRow.getCell('fix').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcSums(dataRows, newRow)
    return newRow
}

/**
 * Посчитать суммы для строки. Значения рассчитываются по итоговым графам - глобальная переменная totalSumColumns
 *
 * @param dataRows строки значения которой суммируются
 * @param sumsRow строка в которую записать суммы
 */
void calcSums(def dataRows, def sumsRow) {
    totalSumColumns.each { alias ->
        sumsRow.getCell(alias).value = 0
    }
    dataRows.each { row ->
        if (row.getAlias() == null) {
            totalSumColumns.each { alias ->
                sumsRow.getCell(alias).value += (row.getCell(alias).value ?: 0)
            }
        }
    }
}

/**
 * Поиск строк с одинаковым кодом классификации дохода.
 *
 * @param dataRows строки формы
 * @param regNumber код классификации дохода
 */
def getGroupRows(def dataRows, def regNumber) {
    def rows = []
    dataRows.each { row ->
        if (row.getAlias() == null && row.regNumber == regNumber) {
            rows.add(row)
        }
    }
    return rows
}

/**
 * Получить значение за предыдущий отчетный период для графы 6.
 *
 * @param dataRowsOld строки за предыдущий период
 * @param row строка текущего периода
 * @return возвращает найденое значение, иначе возвратит 0
 */
def calc6(def dataRowsOld, def row) {
    if (isConsolidated) {
        return row.reserve
    }
    if (row.tradeNumber == null) {
        return null
    }
    def value = 0
    def count = 0
    if (dataRowsOld != null && !dataRowsOld.isEmpty()) {
        for (def rowOld : dataRowsOld) {
            if (rowOld.tradeNumber == row.tradeNumber) {
                value = rowOld.reserveCalcValue
                if (value != null) {
                    count += 1
                }
            }
        }
    }
    // если count не равно 1, то или нет формы за предыдущий период,
    // или нет соответствующей записи в предыдущем периода или записей несколько
    return roundTo2(count == 1 ? value : 0)
}

def calc10(def row) {
    if (row.lotSizeCurrent == null) {
        return null
    }
    return roundTo2(row.marketQuotation ? row.lotSizeCurrent * row.marketQuotation : 0)
}

def calc11(def row, def sign) {
    if (sign == null) {
        return null
    }
    def tmp
    if (sign == '+') {
        if (row.costOnMarketQuotation == null) {
            return null
        }
        def a = (row.cost ?: 0)
        tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
    } else {
        tmp = 0
    }
    return roundTo2(tmp)
}

def calc12(def row) {
    if (row.reserve == null || row.reserveCalcValue == null) {
        return null
    }
    def tmp = row.reserveCalcValue - row.reserve
    return roundTo2(tmp > 0 ? tmp : 0)
}

def calc13(def row) {
    if (row.reserve == null || row.reserveCalcValue == null) {
        return null
    }
    def tmp = (row.reserveCalcValue ?: 0) - (row.reserve ?: 0)
    return roundTo2(tmp < 0 ? -tmp : 0)
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 *
 * return итоговая строка
 */
def addData(def xml) {
    reportPeriodEndDate = reportPeriodService?.get(formData?.reportPeriodId)?.taxPeriod?.getEndDate()
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()
    def newRows = []

    def indexRow = 0
    for (def row : xml.row) {
        indexRow++

        def newRow = getNewRow()
        def indexCell = 1

        // графа 1
        newRow.rowNumber = indexRow
        indexCell++

        // графа 2
        newRow.regNumber = row.cell[indexCell].text()
        indexCell++

        // графа 3
        newRow.tradeNumber = row.cell[indexCell].text()
        indexCell++

        // графа 4
        newRow.lotSizePrev = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)
        indexCell++

        // графа 5
        newRow.lotSizeCurrent = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)
        indexCell++

        // графа 6
        newRow.reserve = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)
        indexCell++

        // графа 7
        newRow.cost = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)
        indexCell++

        // графа 8
        newRow.signSecurity = getRecordIdImport(62, 'CODE', row.cell[indexCell].text(), indexRow, indexCell + 1, true)
        indexCell++

        // графа 9
        newRow.marketQuotation = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)
        indexCell++

        // графа 10
        newRow.costOnMarketQuotation = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)
        indexCell++

        // графа 11
        newRow.reserveCalcValue = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)
        indexCell++

        // графа 12
        newRow.reserveCreation = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)
        indexCell++

        // графа 13
        newRow.reserveRecovery = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        newRows.add(newRow)
    }
    dataRowHelper.save(newRows)

    // итоговая строка
    if (xml.rowTotal.size() == 1) {
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 4
        total.lotSizePrev = getNumber(row.cell[4].text(), indexRow, indexCell + 1)

        // графа 5
        total.lotSizeCurrent = getNumber(row.cell[5].text(), indexRow, indexCell + 1)

        // графа 7
        total.cost = getNumber(row.cell[7].text(), indexRow, indexCell + 1)

        // графа 10
        total.costOnMarketQuotation = getNumber(row.cell[10].text(), indexRow, indexCell + 1)

        // графа 11
        total.reserveCalcValue = getNumber(row.cell[11].text(), indexRow, indexCell + 1)

        // графа 12
        total.reserveCreation = getNumber(row.cell[12].text(), indexRow, indexCell + 1)

        // графа 13
        total.reserveRecovery = getNumber(row.cell[13].text(), indexRow, indexCell + 1)

        return total
    } else {
        return null
    }
}

/** Получить признак ценной бумаги. */
def getSign(def recordId) {
    return getRefBookValue(62, recordId)?.CODE?.value
}

/** Хелпер для округления чисел. */
BigDecimal roundTo2(BigDecimal value) {
    if (value != null) {
        return value.setScale(2, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

/**
 * Cравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalCalc = getCalcTotalRow(dataRows)

    def totalSumColumns = [4 : 'lotSizePrev', 5 : 'lotSizeCurrent', 7 : 'cost', 10 : 'costOnMarketQuotation',
            11 : 'reserveCalcValue', 12 : 'reserveCreation', 13 : 'reserveRecovery']
    def errorColums = []
    if (totalCalc != null) {
        totalSumColumns.each { index, columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColums.add(index)
            }
        }
    }
    if (!errorColums.isEmpty()) {
        def columns = errorColums.join(', ')
        loggerError("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/**
 * Получить значение "Номер по порядку" из формы предыдущего периода.
 *
 * @param dataRowsOld строки предыдущего периода
 */
def getPrevRowNumber(def dataRowsOld) {
    if (dataRowsOld == null || dataRowsOld.isEmpty()) {
        return 0
    }
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // получить номер последний строки предыдущей формы если текущая форма не первая в этом году
    if (reportPeriod != null && reportPeriod.order > 1) {
        // пропустить последние 2 строки - итоги общие и итоги последнего раздела
        return dataRowsOld[dataRowsOld.size() - 3].rowNumber
    }
    return 0
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    if (!isBalancePeriod && !isConsolidated && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.getFormType().getName()
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
}

def loggerError(def msg) {
    if (isBalancePeriod) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}