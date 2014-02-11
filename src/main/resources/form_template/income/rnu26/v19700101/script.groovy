package form_template.income.rnu26.v19700101

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций,
 *                                                  РДР, ADR, GDR и опционов эмитента в целях налогообложения".
 * formTemplateId=325
 *
 * @author rtimerbaev
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
        checkCreation()
        break
    case FormDataEvent.CHECK:
        if (!isBalancePeriod && !isConsolidated && !checkPrevPeriod()) {
            logger.warn('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
        }
        logicalCheck() && checkNSI()
        break
    case FormDataEvent.CALCULATE:
        if (!isBalancePeriod && !isConsolidated && !checkPrevPeriod()) {
            logger.warn('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
        }
        calc()
        !hasError() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        recalculateNumbers()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        recalculateNumbers()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicalCheck() && checkNSI()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicalCheck() && checkNSI()
        break
// обобщить
    case FormDataEvent.COMPOSE:
        consolidation()
        // TODO (Ramil Timerbaev) уточнить нужен ли пересчет при консолидации? и если не нужен то надо ли проверять наличие итоговой строки?
        calc()
        !hasError() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.IMPORT:
        if (!isBalancePeriod && !isConsolidated && !checkPrevPeriod()) {
            logger.warn('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
        }
        importData()
        if (!hasError()) {
            calc()
        }
        break
    case FormDataEvent.MIGRATION:
        importData()
        if (!hasError()) {
            def total = getCalcTotalRow()
            def data = getData(formData)
            insert(data, total)
        }
        break
}

// графа 1  - rowNumber
// графа 2  - issuer
// графа 3  - shareType
// графа 4  - tradeNumber
// графа 5  - currency Справочник
// графа 6  - lotSizePrev
// графа 7  - lotSizeCurrent
// графа 8  - reserveCalcValuePrev
// графа 9  - cost
// графа 10 - signSecurity Справочник
// графа 11 - marketQuotation
// графа 12 - rubCourse
// графа 13 - marketQuotationInRub
// графа 14 - costOnMarketQuotation
// графа 15 - reserveCalcValue
// графа 16 - reserveCreation
// графа 17 - reserveRecovery

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)

    def index = 0
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while (row.getAlias() != null && index > 0) {
            row = getRows(data).get(--index)
        }
        if (index != currentDataRow.getIndex() && getRows(data).get(index).getAlias() == null) {
            index++
        }
    } else if (getRows(data).size() > 0) {
        for (int i = getRows(data).size() - 1; i >= 0; i--) {
            def row = getRows(data).get(i)
            if (!isFixedRow(row)) {
                index = getRows(data).indexOf(row) + 1
                break
            }
        }
    }
    data.insert(getNewRow(), index + 1)
}

def recalculateNumbers() {
    def index = 1
    def data = getData(formData)
    getRows(data).each { row ->
        if (!isFixedRow(row)) {
            row.rowNumber = index++
        }
    }
    data.save(getRows(data))
}

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()

    def columns
    if (isBalancePeriod) {
        // все строки, кроме графы 1
        columns = ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost', 'signSecurity', 'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
    } else {
        // графа 2..7, 9..13
        columns = ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent', 'cost', 'signSecurity', 'marketQuotation', 'rubCourse']
    }
    columns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}
/**
 * Удалить строку.
 */
def deleteRow() {
    getData(formData).delete(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def data = getData(formData)
    /*
     * Проверка обязательных полей.
     */
    for (def row : getRows(data)) {
        if (!isFixedRow(row)) {
            // список проверяемых столбцов (графа 2..7, 9, 10, 11)
            def requiredColumns = ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev',
                    'lotSizeCurrent', 'cost', 'signSecurity']

            if (!checkRequiredColumns(row, requiredColumns)) {
                return
            }
        }
    }

    /*
     * Расчеты.
     */

    // удалить строку "итого" и "итого по Эмитенту: ..."
    def delRow = []
    getRows(data).each { row ->
        if (isFixedRow(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        data.delete(row)
    }

    // отсортировать/группировать
    if (formDataEvent != FormDataEvent.IMPORT) {
        sort(data)
    }

    def reportDate = reportPeriodService.getReportDate(formData.reportPeriodId)?.time
    def formDataOld = getFormDataOld()
    def dataOld = getData(formDataOld)
    getRows(data).eachWithIndex { row, index ->
        if (formDataEvent != FormDataEvent.IMPORT) {
            // графа 1
            row.rowNumber = index + 1
        }

        if (!isBalancePeriod) {
            // графа 8
            row.reserveCalcValuePrev = calc8(row, 'reserveCalcValue', 'tradeNumber', row.tradeNumber, dataOld)

            if (formDataEvent != FormDataEvent.IMPORT) {
                // графа 12 курс валют
                row.rubCourse = calc12(row.currency, reportDate)

                // графа 13
                row.marketQuotationInRub = calc13(row)
            }

            // графа 14
            row.costOnMarketQuotation = calc14(row)

            // графа 15
            row.reserveCalcValue = calc15(row)

            // графа 16
            row.reserveCreation = calc16(row)

            // графа 17
            row.reserveRecovery = calc17(row)
        }
    }

    data.save(getRows(data))

    // добавить строку "итого"
    def totalRow = getCalcTotalRow()
    insert(data, totalRow)

    // графы для которых надо вычислять итого и итого по эмитенту (графа 6..9, 14..17)
    def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost',
            'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

    // посчитать "итого по Эмитенту:..."
    def totalRows = [:]
    def sums = [:]
    tmp = null
    totalColumns.each {
        sums[it] = 0
    }
    getRows(data).eachWithIndex { row, i ->
        if (!isFixedRow(row)) {
            if (tmp == null) {
                tmp = row.issuer
            }
            // если код расходы поменялся то создать новую строку "итого по Эмитента:..."
            if (tmp != row.issuer) {
                totalRows.put(i, getNewRow(tmp, totalColumns, sums, data))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по Эмитента:..."
            if (i == getRows(data).size() - 2) {
                totalColumns.each {
                    sums[it] += (row.getCell(it).getValue() ?: 0)
                }
                totalRows.put(i + 1, getNewRow(row.issuer, totalColumns, sums, data))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += (row.getCell(it).getValue() ?: 0)
            }
            tmp = row.issuer
        }
    }
    // добавить "итого по Эмитенту:..." в таблицу
    def i = 0
    totalRows.each { index, row ->
        data.insert(row, index + i + 1)
        i = i + 1
    }
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)
    for (def row : getRows(data)) {
        if (isFixedRow(row)) {
            continue
        }
        // 15. Обязательность заполнения поля графы 1..3, 5..10, 13, 14
        columns = ['rowNumber', 'issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent',
                'reserveCalcValuePrev', 'cost', 'signSecurity', 'costOnMarketQuotation']
        if (!checkRequiredColumns(row, columns)) {
            return false
        }
    }

    // данные предыдущего отчетного периода
    def formDataOld = getFormDataOld()
    def dataOld = getData(formDataOld)

    if (formDataOld != null && !getRows(dataOld).isEmpty()) {

        // суммы строки общих итогов
        def totalSums = [:]

        // графы для которых надо вычислять итого и итого по эмитенту (графа 6..9, 14..17)
        def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
                'cost', 'costOnMarketQuotation', 'reserveCalcValue',
                'reserveCreation', 'reserveRecovery']

        // признак наличия итоговых строк
        def hasTotal = false

        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        def tmp
        for (def row : getRows(data)) {
            if (isFixedRow(row)) {
                hasTotal = true
                continue
            }

            def index = row.rowNumber
            def errorMsg
            if (index != null && index != '') {
                errorMsg = "В строке \"№ пп\" равной $index "
            } else {
                index = row.getIndex()
                errorMsg = "В строке $index "
            }

            // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 8, 17)
            if (row.lotSizeCurrent == 0 && row.reserveCalcValuePrev != row.reserveRecovery) {
                logger.warn(errorMsg + 'графы 8 и 17 неравны!')
            }

            // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 9, 14, 15)
            if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
                logger.warn(errorMsg + 'графы 9, 14 и 15 ненулевые!')
            }

            // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6, 8, 17)
            if (row.lotSizePrev == 0 && (row.reserveCalcValuePrev != 0 || row.reserveRecovery != 0)) {
                loggerError(errorMsg + 'графы 8 и 17 ненулевые!')
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 5. Проверка необращающихся акций (графа 10, 15, 16)
            def sign = getSign(row.signSecurity)
            if (sign == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                logger.warn(errorMsg + 'акции необращающиеся, графы 15 и 16 ненулевые!')
            }

            // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
            tmp = (row.reserveCalcValue ?: 0) - row.reserveCalcValuePrev
            if (sign == '+' && tmp > 0 && row.reserveRecovery != 0) {
                loggerError(errorMsg + 'акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 16)
            if (sign == '+' && tmp < 0 && row.reserveCreation != 0) {
                loggerError(errorMsg + 'акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
            if (sign == '+' && tmp == 0 &&
                    (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
                loggerError(errorMsg + 'акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 9. Проверка корректности формирования резерва (графа 8, 15, 16, 17)
            if (row.reserveCalcValuePrev + (row.reserveCreation ?: 0) != (row.reserveCalcValue ?: 0) + (row.reserveRecovery ?: 0)) {
                loggerError(errorMsg + 'резерв сформирован неверно!')
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 10. Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                    row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
                logger.warn(errorMsg + 'резерв сформирован. Графы 7, 9, 14 и 15 неположительные!')
            }

            // 11. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 6, 7 (за предыдущий период) )
            if (!isBalancePeriod && !isConsolidated && checkOld(row, 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', formDataOld)) {
                def curCol = 4
                def curCol2 = 6
                def prevCol = 4
                def prevCol2 = 7
                logger.warn("РНУ сформирован некорректно! " + errorMsg + "не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-26 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-26 за предыдущий отчётный период.")
            }

            // 12. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 8, 15 (за предыдущий период) )
            if (!isBalancePeriod && !isConsolidated && checkOld(row, 'tradeNumber', 'reserveCalcValuePrev', 'reserveCalcValue', formDataOld)) {
                def curCol = 4
                def curCol2 = 4
                def prevCol = 8
                def prevCol2 = 15
                loggerError("РНУ сформирован некорректно! " + errorMsg + "не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-26 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-26 за предыдущий отчётный период.")
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 16. Проверка на уникальность поля «№ пп» (графа 1)
            for (def rowB : getRows(data)) {
                if (!row.equals(rowB) && row.rowNumber == rowB.rowNumber) {
                    loggerError('Нарушена уникальность номера по порядку!')
                    if (!isBalancePeriod) {
                        return false
                    }
                }
            }

            // 17. Арифметическая проверка графы 8, 14..17
            if (!isBalancePeriod) {
                // графа 8
                if (row.reserveCalcValuePrev != calc8(row, 'reserveCalcValue', 'tradeNumber', row.tradeNumber, dataOld)) {
                    name = getColumnName(row, 'reserveCalcValuePrev')
                    logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
                }

                // графа 13
                if (row.marketQuotationInRub != calc13(row)) {
                    name = getColumnName(row, 'marketQuotationInRub')
                    logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
                }

                // графа 14
                if (row.costOnMarketQuotation != calc14(row)) {
                    name = getColumnName(row, 'costOnMarketQuotation')
                    logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
                }

                // графа 15
                if (row.reserveCalcValue != calc15(row)) {
                    name = getColumnName(row, 'reserveCalcValue')
                    logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
                }

                // графа 16
                if (row.reserveCreation != calc16(row)) {
                    name = getColumnName(row, 'reserveCreation')
                    logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
                }

                // графа 17
                if (row.reserveRecovery != calc17(row)) {
                    name = getColumnName(row, 'reserveRecovery')
                    logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
                }
            }
            // 17. конец=========================================

            // 18. Проверка итоговых значений по эмитентам
            if (!totalGroupsName.contains(row.issuer)) {
                totalGroupsName.add(row.issuer)
            }

            // 19. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }
        }

        if (dataOld != null && hasTotal) {
            totalRow = data.getDataRow(getRows(data), 'total')
            totalRowOld = data.getDataRow(getRows(dataOld), 'total')

            // 13. Проверка корректности заполнения РНУ (графа 6, 7 (за предыдущий период))
            if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                def curCol = 6
                def prevCol = 7
                loggerError("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе $curCol = «Итого» по графе $prevCol формы РНУ-26 за предыдущий отчётный период.")
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 14. Проверка корректности заполнения РНУ (графа 8, 15 (за предыдущий период))
            if (totalRow.reserveCalcValuePrev != totalRowOld.reserveCalcValue) {
                def curCol = 8
                def prevCol = 15
                loggerError("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе $curCol = «Итого» по графе $prevCol формы РНУ-26 за предыдущий отчётный период.")
                if (!isBalancePeriod) {
                    return false
                }
            }
        }

        if (hasTotal) {
            def totalRow = data.getDataRow(getRows(data), 'total')

            // 18. Проверка итоговых значений по эмитенту
            for (def codeName : totalGroupsName) {
                def row = data.getDataRow(getRows(data), 'total' + getRowNumber(codeName, data))
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        loggerError("Итоговые значения по эмитенту $codeName рассчитаны неверно!")
                        if (!isBalancePeriod) {
                            return false
                        }
                    }
                }
            }

            // 19. Проверка итогового значений по всей форме
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    loggerError('Итоговые значения рассчитаны неверно!')
                    if (!isBalancePeriod) {
                        return false
                    }
                }
            }
        }
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def data = getData(formData)
    def reportDate = reportPeriodService.getReportDate(formData.reportPeriodId)?.time
    getRows(data).each { row ->
        if (!isFixedRow(row)) {

            def index = row.rowNumber
            def errorMsg
            if (index != null && index != '') {
                errorMsg = "В строке \"№ пп\" равной $index "
            } else {
                index = row.getIndex()
                errorMsg = "В строке $index "
            }

            // 1. Проверка актуальности поля «Валюта выпуска ценной бумаги»
            if (row.currency != null && getCurrency(row.currency) == null) {
                logger.warn(errorMsg + 'валюта выпуска ценной бумаги указана неверно!')
            }

            // 1. Проверка курса валюты со справочным - Проверка актуальности значения» графы 6» на дату по «графе 5»
            if (row.rubCourse != null && row.rubCourse != calc12(row.currency, reportDate)) {
                logger.warn(errorMsg + 'неверный курс валюты!')
            }

            // 2. Проверка актуальности поля «Признак ценной бумаги на текущую отчётную дату»
            if (row.signSecurity != null && getSign(row.signSecurity) == null) {
                logger.warn(errorMsg + 'признак ценной бумаги в справочнике отсутствует!')
            }
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)
    // удалить все строки и собрать из источников их строки
    data.clear()
    def newRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        newRows.add(row)
                    }
                }
            }
        }
    }
    if (!newRows.isEmpty()) {
        data.insert(newRows, 1)
        // sort(data)
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
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
        if (formDataEvent == FormDataEvent.IMPORT) {
            if (totalLoad != null) {
                checkTotalRow(totalLoad)
            } else {
                logger.error("Нет итоговой строки.")
            }
        }
    } catch (Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias() == 'total'
}

/**
 * Проверка является ли строка итоговой.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить сумму столбца.
 */
def getSum(def data, def columnAlias) {
    def rows = getRows(data)
    def from = 0
    def to = rows.size() - 1
    if (from > to) {
        return 0
    }
    return summ(formData, rows, new ColumnRange(columnAlias, from, to))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums, def data) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + getRowNumber(alias, data))
    newRow.issuer = alias + ' итог'
    setTotalStyle(newRow)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
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
 * @param prevForm данные нф предыдущего периода
 */
def checkOld(def row, def likeColumnName, def curColumnName, def prevColumnName, def prevForm) {
    if (prevForm == null) {
        return false
    }
    if (row.getCell(likeColumnName).getValue() == null) {
        return false
    }
    for (def prevRow : getRows(getData(prevForm))) {
        if (row.getCell(likeColumnName).getValue() == prevRow.getCell(likeColumnName).getValue() &&
                row.getCell(curColumnName).getValue() != prevRow.getCell(prevColumnName).getValue()) {
            return true
        }
    }
    return false
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    if (isBalancePeriod || isConsolidated) {
        return null
    }
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-26 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * * Посчитать сумму указанного графа для строк с общим значением
 *
 * @param value значение общее для всех строк суммирования
 * @param alias название графа
 */
def calcSumByCode(def value, def alias) {
    def data = getData(formData)
    def sum = 0
    getRows(data).each { row ->
        if (!isFixedRow(row) && row.issuer == value) {
            sum += (row.getCell(alias).getValue() ?: 0)
        }
    }
    return sum
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'issuer', 'shareType', 'tradeNumber', 'currency',
            'lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost', 'signSecurity',
            'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
        row.getCell(it).editable = false
    }
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    getRows(getData(formData)).indexOf(row)
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    // если не заполнены графа 11 и графа 12, то графа 13 должна быть заполнена вручную
    if (row.marketQuotation != null && row.rubCourse != null) {
        columns -= 'marketQuotationInRub'
    }

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.rowNumber
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Получить значение за предыдущий отчетный период.
 *
 * @param needColumnName псевдоним графы значение которой надо получить (графа значения)
 * @param searchColumnName псевдоним графы по которой нужно отобрать значение (графа поиска)
 * @param searchValue значение графы поиска
 * @return возвращает найденое значение, иначе возвратит 0
 */
def calc8(def row, def needColumnName, def searchColumnName, def searchValue, def dataOld) {
    if (isBalancePeriod || isConsolidated) {
        return row.reserveCalcValuePrev
    }
    if (dataOld != null) {
        def dataRowsOld = getRows(dataOld)
        if (dataRowsOld != null && !dataRowsOld.isEmpty()) {
            for (def oldRow : dataRowsOld) {
                if (oldRow.getCell(searchColumnName).getValue() == searchValue) {
                    return roundValue(oldRow.getCell(needColumnName).getValue(), 2)
                }
            }
        }
    }
    return 0
}

def calc13(def row) {
    if (row.marketQuotation != null && row.rubCourse != null) {
        return row.marketQuotation * row.rubCourse
    }
    return null
}

def calc14(def row) {
    def tmp = (row.marketQuotationInRub == null ? 0 : row.lotSizeCurrent * row.marketQuotationInRub)
    return roundValue(tmp, 2)
}

def calc15(def row) {
    def tmp
    if (getSign(row.signSecurity) == '+') {
        def a = (row.cost == null ? 0 : row.cost)
        tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
    } else {
        tmp = 0
    }
    return roundValue(tmp, 2)
}

def calc16(def row) {
    def tmp = row.reserveCalcValue - row.reserveCalcValuePrev
    return roundValue((tmp > 0 ? tmp : 0), 2)
}

def calc17(def row) {
    def tmp = row.reserveCalcValue - row.reserveCalcValuePrev
    return roundValue((tmp < 0 ? tmp.abs() : 0), 2)
}

/**
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
}

/**
 * Проверить данные за предыдущий отчетный период.
 */
def checkPrevPeriod() {
    def formDataOld = getFormDataOld()

    if (formDataOld != null && formDataOld.state == WorkflowState.ACCEPTED) {
        return true
    }
    return false
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}
/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    Date date = new Date()

    def cache = [:]
    def data = getData(formData)
    data.clear()
    def newRows = []

    for (def row : xml.row) {
        def newRow = getNewRow()

        def indexCell = 1

        newRow.rowNumber = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 2
        newRow.issuer = row.cell[indexCell].text()
        indexCell++

        // графа 3
        newRow.shareType = row.cell[indexCell].text()
        indexCell++

        // графа 4
        newRow.tradeNumber = row.cell[indexCell].text()
        indexCell++

        // графа 5
        newRow.currency = getRecords(15, 'CODE_2', row.cell[indexCell].text(), date, cache)
        indexCell++

        // графа 6
        newRow.lotSizePrev = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 7
        newRow.lotSizeCurrent = getNumber(row.cell[indexCell].text())
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
        newRow.reserveRecovery = getNumber(row.cell[indexCell].text())

        newRows.add(newRow)
    }
    data.insert(newRows, 1)

    // итоговая строка
    if (xml.rowTotal.size() == 1) {
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 6
        total.lotSizePrev = getNumber(row.cell[6].text())

        // графа 7
        total.lotSizeCurrent = getNumber(row.cell[7].text())

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
        total.reserveRecovery = getNumber(row.cell[17].text())

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
    loggerError("Не удалось найти запись в справочнике «" + refBookFactory.get(ref_id).getName() + "» с атрибутом $code равным $value!")
    return null
}

/**
 * Получить строки формы.
 *
 * @param formData форма
 */
def getRows(def data) {
    if (data != null) {
        def cached = data.getAllCached()
        return cached
    }
    return null
}

/**
 * Вставить новыую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}

/**
 * Проверка валюты на рубли
 */
def isRubleCurrency(def currencyCode) {
    return refBookService.getStringValue(15, currencyCode, 'CODE') == '810'
}

/**
 * Получить курс валюты
 */
def calc12(def currency, def date) {
    if (currency != null && !isRubleCurrency(currency)) {
        def refCourseDataProvider = refBookFactory.getDataProvider(22)
        def res = refCourseDataProvider.getRecords(date, null, 'CODE_NUMBER=' + currency, null);
        return (!res.getRecords().isEmpty()) ? res.getRecords().get(0).RATE.getNumberValue() : 0//Правильнее null, такой ситуации быть не должно, она должна отлавливаться проверками НСИ
    } else {
        return null;
    }
}

/**
 * Получить признак ценной бумаги
 */
def getSign(def sign) {
    return refBookService.getStringValue(62, sign, 'CODE')
}

/**
 * Получить буквенный код валюты
 */
def getCurrency(def currencyCode) {
    return refBookService.getStringValue(15, currencyCode, 'CODE_2')
}

/**
 * Получение первого rowNumber по issuer
 * @param alias
 * @param data
 * @return
 */
def getRowNumber(def alias, def data) {
    for (def row : getRows(data)) {
        if (row.issuer == alias) {
            return row.rowNumber.toString()
        }
    }
}

/**
 * Расчетать, проверить и сравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def totalColumns = [6: 'lotSizePrev', 7: 'lotSizeCurrent', 8: 'reserveCalcValuePrev', 9: 'cost', 14: 'costOnMarketQuotation',
            15: 'reserveCalcValue', 16: 'reserveCreation', 17: 'reserveRecovery']
    def totalCalc = getCalcTotalRow()
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
        loggerError("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
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
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    // графы для которых надо вычислять итого и итого по эмитенту (графа 6..9, 14..17)
    def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost',
            'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
    // добавить строку "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.issuer = 'Общий итог'
    setTotalStyle(totalRow)
    def data = getData(formData)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(data, alias))
    }
    return totalRow
}

/**
 * Отсорировать данные (по графе 2, 4).
 *
 * @param data данные нф (хелпер)
 */
void sort(def data) {
    // отсортировать/группировать
    getRows(data).sort { def a, def b ->
        // графа 2  - issuer
        // графа 4  - tradeNumber
        if (a.issuer == b.issuer) {
            return a.tradeNumber <=> b.tradeNumber
        }
        return a.issuer <=> b.issuer
    }
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, def precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def msg) {
    if (isBalancePeriod) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}