package form_template.income.rnu25

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Форма "(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения".
 *
 * TODO:
 *      - в перечне полей графа 9 - необязательная, в логических проверках входит в обязательные поля
 *              http://jira.aplana.com/browse/SBRFACCTAX-4871
 *
 * @author rtimerbaev
 */

// графа 1  - rowNumber
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
def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
        }
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
            return
        }
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
            return
        }
        logicCheck()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        // TODO (Ramil Timerbaev) уточнить нужен ли пересчет при консолидации? и если не нужен то надо ли проверять наличие итоговой строки?
        calc()
        !logger.containsLevel(LogLevel.ERROR) && logicCheck()
        break
    case FormDataEvent.IMPORT :
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
            return
        }
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
        }
        break
    case FormDataEvent.MIGRATION :
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
            return
        }
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
def allColumns = ['rowNumber', 'regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent',
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
def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
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

/** Добавить новую строку. */
def addNewRow() {
    formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
}

/** Удалить строку. */
def deleteRow() {
    if (currentDataRow.getAlias() == null) {
        formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
    }
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

    def formDataOld = getFormDataOld()
    def dataRowsOld = (formDataOld != null ? formDataService.getDataRowHelper(formDataOld)?.allCached : null)

    // получить номер последний строки предыдущей формы если это не событие импорта
    def rowNumber = (isImport ? 0 : getPrevRowNumber(dataRowsOld))
    dataRows.each { row ->
        if (!isImport) {
            // графа 1
            row.rowNumber = ++rowNumber
        }

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

    // добавить строку "итого"
    def totalRow = getCalcTotalRow(dataRows)
    dataRows.add(totalRow)
    // dataRowHelper.insert(totalRow, dataRows.size() + 1)
    if (dataRows.size() == 1) {
        dataRowHelper.save(dataRows)
        return
    }

    // посчитать "итого по ГРН:..."
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }
    dataRows.eachWithIndex { row, i ->
        if (row.getAlias() == null) {
            if (tmp == null) {
                tmp = row.regNumber
            }
            // если код расходы поменялся то создать новую строку "итого по ГРН:..."
            if (tmp != row.regNumber) {
                totalRows.put(i, getNewTotalRow(tmp, totalColumns, sums, dataRows))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по ГРН:..."
            if (i == dataRows.size() - 2) {
                totalColumns.each {
                    sums[it] += (row.getCell(it).value ?: 0)
                }
                totalRows.put(i + 1, getNewTotalRow(row.regNumber, totalColumns, sums, dataRows))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += (row.getCell(it).value ?: 0)
            }
            tmp = row.regNumber
        }
    }
    // добавить "итого по ГРН:..." в таблицу
    def i = 0
    totalRows.each { index, row ->
        dataRows.add(index + i, row)
        i++
    }
    dataRowHelper.save(dataRows)
}

/** Логические проверки. */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def rowNum = 0
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // 15. Обязательность заполнения поля графы 1..3, 5..13
        checkNonEmptyColumns(row, ++rowNum, nonEmptyColumns, logger, true)
    }

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

    if (dataRows.isEmpty()) {
        return
    }

    // алиасы графов для арифметической проверки (графа )
    def arithmeticCheckAlias = ['reserve', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // суммы строки общих итогов
    def totalSums = [:]
    totalColumns.each { alias ->
        totalSums[alias] = 0
    }
    // список групп кодов классификации для которых надо будет посчитать суммы
    def totalGroupsName = []

    def errorMsg

    def rowNumber = getPrevRowNumber(dataRowsOld)
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        index = row.getIndex()
        errorMsg = "Строка $index: "

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
            logger.error(errorMsg + 'графы 6 и 13 ненулевые!')
        }

        // 5. Проверка необращающихся акций (графа 8, 11, 12)
        def sign = getSign(row.signSecurity)
        if (sign == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
            logger.warn(errorMsg + 'облигации необращающиеся, графы 11 и 12 ненулевые!')
        }

        // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve > 0 && row.reserveRecovery != 0) {
            logger.error(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
        }

        // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 12)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve < 0 && row.reserveCreation != 0) {
            logger.error(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
        }

        // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve == 0 &&
                (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
            logger.error(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
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
            logger.error(errorMsg + 'резерв сформирован некорректно!')
        }

        // 11. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 4, 5 (за предыдущий период) )
        if (checkOld(row, 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', dataRowsOld)) {
            def curCol = 3
            def curCol2 = 4
            def prevCol = 3
            def prevCol2 = 5
            logger.error("РНУ сформирован некорректно! " + errorMsg + "Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-25 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-25 за предыдущий отчётный период.")
        }

        // 12. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 6, 11 (за предыдущий период) )
        if (checkOld(row, 'tradeNumber', 'reserve', 'reserveCalcValue', dataRowsOld)) {
            def curCol = 3
            def curCol2 = 3
            def prevCol = 6
            def prevCol2 = 11
            logger.error("РНУ сформирован некорректно! " + errorMsg + "Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-25 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-25 за предыдущий отчётный период.")
        }

        // 16. Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.rowNumber) {
            logger.error('Нарушена уникальность номера по порядку!')
        }

        // 17. Арифметические проверки граф 6, 10..13
        needValue['reserve'] = calc6(dataRowsOld, row)
        needValue['costOnMarketQuotation'] = calc10(row)
        needValue['reserveCalcValue'] = calc11(row, sign)
        needValue['reserveCreation'] = calc12(row)
        needValue['reserveRecovery'] = calc13(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, false)

        // 18. Проверка итоговых значений по ГРН
        if (!totalGroupsName.contains(row.regNumber)) {
            totalGroupsName.add(row.regNumber)
        }

        // 19. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
        totalColumns.each { alias ->
            if (totalSums[alias] == null) {
                totalSums[alias] = 0
            }
            totalSums[alias] += (row.getCell(alias).value ?: 0)
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
            logger.error("РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе $curCol = «Общий итог» по графе $prevCol формы РНУ-25 за предыдущий отчётный период.")
        }

        // 14. Проверка корректности заполнения РНУ (графа 6, 11 (за предыдущий период))
        if (totalRow.reserve != totalRowOld.reserveCalcValue) {
            def curCol = 6
            def prevCol = 11
            logger.error("РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе $curCol = «Общий итог» по графе $prevCol формы РНУ-25 за предыдущий отчётный период.")
        }
    }

    // Проверка итоговых строк
    def totalRow = dataRowHelper.getDataRow(dataRows, 'total')

    // 17. Проверка итоговых значений по ГРН
    for (def codeName : totalGroupsName) {
        def totalRowAlias = 'total' + getRowNumber(codeName, dataRows)
        def row
        try {
            row = dataRowHelper.getDataRow(dataRows, totalRowAlias)
        } catch(IllegalArgumentException e) {
            logger.error("Итоговые значения по ГРН $codeName не рассчитаны! Необходимо рассчитать данные формы.")
            continue
        }
        for (def alias : totalColumns) {
            if (calcSumByCode(dataRows, codeName, alias) != row.getCell(alias).value) {
                logger.error("Итоговые значения по ГРН $codeName рассчитаны неверно!")
            }
        }
    }

    // 18. Проверка итогового значений по всей форме
    for (def alias : totalColumns) {
        if (totalSums[alias] != totalRow.getCell(alias).value) {
            logger.error('Итоговые значения рассчитаны неверно!')
            break
        }
    }
}

/** Консолидация. */
void consolidation() {
    formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
}

/** Проверка при создании формы. */
void checkCreation() {
    formDataService.checkUnique(formData, logger)
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
        logger.error('Формат файла должен быть *.r??')
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

/** Получить сумму столбца. */
def getSum(def dataRows, def columnAlias) {
    def sum = 0
    dataRows.each { row ->
        if (row.getAlias() == null) {
            sum += (row.getCell(columnAlias).value ?: 0)
        }
    }
    return sum
}

/** Получить новую строку. */
def getNewTotalRow(def alias, def totalColumns, def sums, def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + getRowNumber(alias, dataRows))
    newRow.regNumber = alias + ' итог'
    setTotalStyle(newRow)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
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

/** Установить стиль для итоговых строк. */
void setTotalStyle(def row) {
    allColumns.each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/** Получить данные за предыдущий отчетный период. */
def getFormDataOld() {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-25 за предыдущий отчетный период
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }
    return formDataOld
}

/**
 * Посчитать сумму указанного графа для строк с общим кодом классификации.
 *
 * @param dataRows строки формы
 * @param regNumber код классификации дохода
 * @param alias название графа
 */
def calcSumByCode(def dataRows, def regNumber, def alias) {
    def sum = 0
    dataRows.each { row ->
        if (row.getAlias() == null && row.regNumber == regNumber) {
            sum += (row.getCell(alias).value ?: 0)
        }
    }
    return sum
}

/**
 * Получить значение за предыдущий отчетный период для графы 6.
 *
 * @param dataRowsOld строки за предыдущий период
 * @param row строка текущего периода
 * @return возвращает найденое значение, иначе возвратит 0
 */
def calc6(def dataRowsOld, def row) {
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

/** Проверить данные за предыдущий отчетный период. */
def checkPrevPeriod() {
    def dataRowsOld = null
    def formDataOld = getFormDataOld()
    if (formDataOld != null) {
        def dataRowHelperOld = formDataService.getDataRowHelper(formDataOld)
        dataRowsOld = dataRowHelperOld.allCached
    }

    if (formDataOld?.state == WorkflowState.ACCEPTED && !dataRowsOld.isEmpty()) {
        return true
    }
    return false
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
        def indexCell = 0

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
    dataRowHelper.insert(newRows, 1)

    // итоговая строка
    if (xml.rowTotal.size() == 1) {
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()
        def indexCell

        // графа 4
        indexCell = 3
        total.lotSizePrev = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 5
        indexCell = 4
        total.lotSizeCurrent = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 7
        indexCell = 6
        total.cost = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 10
        indexCell = 9
        total.costOnMarketQuotation = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 11
        indexCell = 10
        total.reserveCalcValue = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 12
        indexCell = 11
        total.reserveCreation = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 13
        indexCell = 12
        total.reserveRecovery = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        return total
    } else {
        return null
    }
}

/** Получить признак ценной бумаги. */
def getSign(def recordId) {
    if (recordId == null) {
        return null
    }
    def record = getRefBookValue(62, recordId)
    return record?.CODE?.value
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
 * Получение первого rowNumber по regNumber.
 *
 * @param alias
 * @param dataRows строки нф
 */
def getRowNumber(def alias, def dataRows) {
    for (def row: dataRows) {
        if (row.regNumber == alias) {
            return row.rowNumber.toString()
        }
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

    def totalColumns = [4 : 'lotSizePrev', 5 : 'lotSizeCurrent', 7 : 'cost', 10 : 'costOnMarketQuotation',
            11 : 'reserveCalcValue', 12 : 'reserveCreation', 13 : 'reserveRecovery']
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

/** Получить итоговую строку с суммами. */
def getCalcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()

    totalRow.setAlias('total')
    totalRow.regNumber = 'Общий итог'
    setTotalStyle(totalRow)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(dataRows, alias))
    }
    return totalRow
}

/**
 * Получить значение "Номер по порядку" из формы предыдущего периода.
 *
 * @param dataRowsOld строки предыдущего периода
 */
def getPrevRowNumber(def dataRowsOld) {
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // получить номер последний строки предыдущей формы если текущая форма не первая в этом году
    if (reportPeriod != null && reportPeriod.order > 1 &&
            dataRowsOld != null && !dataRowsOld.isEmpty()) {
        // пропустить последние 2 строки - итоги общие и итоги последнего раздела
        return dataRowsOld[dataRowsOld.size() - 3].rowNumber
    }
    return 0
}