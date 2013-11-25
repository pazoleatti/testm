package form_template.income.rnu25

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения".
 *
 * @version 65
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
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        if (!isBalancePeriod && !isConsolidated && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует или не находится в статусе «Принята»')
        }
        logicalCheck() && checkNSI()
        break
    case FormDataEvent.CALCULATE :
        if (!isBalancePeriod && !isConsolidated && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует или не находится в статусе «Принята»')
            return
        }
        calc()
        !hasError() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        recalculateNumbers()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        recalculateNumbers()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        if (!isBalancePeriod && !isConsolidated && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует или не находится в статусе «Принята»')
            return
        }
        logicalCheck() && checkNSI()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        // TODO (Ramil Timerbaev) уточнить нужен ли пересчет при консолидации? и если не нужен то надо ли проверять наличие итоговой строки?
        calc()
        !hasError() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.IMPORT :
        if (!isBalancePeriod && !isConsolidated && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует или не находится в статусе «Принята»')
            return
        }
        importData()
        if (!hasError()) {
            calc()
        }
        break
    case FormDataEvent.MIGRATION :
        if (!isBalancePeriod && !isConsolidated && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует или не находится в статусе «Принята»')
            return
        }
        importData()
        if (!hasError()) {
            def total = getCalcTotalRow()
            def data = getData(formData)
            insert(data, total)
        }
        break
}

// графа 1  - rowNumber
// графа 2  - regNumber
// графа 3  - tradeNumber
// графа 4  - lotSizePrev
// графа 5  - lotSizeCurrent
// графа 6  - reserve
// графа 7  - cost
// графа 8  - signSecurity Справочник
// графа 9  - marketQuotation
// графа 10 - costOnMarketQuotation
// графа 11 - reserveCalcValue
// графа 12 - reserveCreation
// графа 13 - reserveRecovery

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)

    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(row.getAlias()!=null && index>0){
            row = getRows(data).get(--index)
        }
        if(index!=currentDataRow.getIndex() && getRows(data).get(index).getAlias()==null){
            index++
        }
    }else if (getRows(data).size()>0) {
        for(int i = getRows(data).size()-1;i>=0;i--){
            def row = getRows(data).get(i)
            if(!isFixedRow(row)){
                index = getRows(data).indexOf(row)+1
                break
            }
        }
    }
    data.insert(getNewRow(),index+1)
}

def recalculateNumbers(){
    def index = 1
    def data = getData(formData)
    getRows(data).each{row->
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
        columns = ['regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'signSecurity', 'marketQuotation', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
    } else {
        // графа 2..5, 7..9
        columns = ['regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', 'cost', 'signSecurity', 'marketQuotation']
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
    def data = getData(formData)
    data.delete(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def data = getData(formData)
    /*
     * Проверка обязательных полей.
     */

    // список проверяемых столбцов (графа 2, 3, 5, 7, 8)
    def requiredColumns = ['regNumber', 'tradeNumber', 'lotSizeCurrent', 'cost', 'signSecurity']
    for (def row : getRows(data)) {
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return
        }
    }

    /*
     * Расчеты.
     */

    // удалить строку "итого" и "итого по ГРН: ..."
    def delRow = []
    getRows(data).each { row ->
        if (isFixedRow(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        getRows(data).remove(getIndex(data, row))
    }
    if (getRows(data).isEmpty()) {
        return
    }

    // отсортировать/группировать
    if (formDataEvent != FormDataEvent.IMPORT) {
        sort(data)
    }

    // в периоде ввода остатков ничего не пересчитываем
    def tmp
    def formDataOld = getFormDataOld()
    def dataOld = getData(formDataOld)

    getRows(data).eachWithIndex { row, index ->
        if (formDataEvent != FormDataEvent.IMPORT) {
            // графа 1
            row.rowNumber = index + 1
        }

        if (!isBalancePeriod) {
            // графа 6
            row.reserve = getValueForColumn6(dataOld, row)

            // графа 10
            row.costOnMarketQuotation = (row.marketQuotation ? roundTo2(row.lotSizeCurrent * row.marketQuotation) : 0)

            // графа 11
            if (getSign(row.signSecurity) == '+') {
                def a = (row.cost ?: 0)
                tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
            } else {
                tmp = 0
            }
            row.reserveCalcValue = roundTo2(tmp)

            // графа 12
            tmp = roundTo2((row.reserveCalcValue?:0) - (row.reserve?:0))
            row.reserveCreation = (tmp > 0 ? tmp : 0)

            // графа 13
            row.reserveRecovery = (tmp < 0 ? Math.abs(tmp) : 0)
        }
    }
    save(data)

    // добавить строку "итого"
    def totalRow = getCalcTotalRow()
    insert(data, totalRow)

    // графы для которых надо вычислять итого и итого по ГРН (графа 4..7, 10..13)
    def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

    // посчитать "итого по ГРН:..."
    def totalRows = [:]
    tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }
    getRows(data).eachWithIndex { row, i ->
        if (!isFixedRow(row)) {
            if (tmp == null) {
                tmp = row.regNumber
            }
            // если код расходы поменялся то создать новую строку "итого по ГРН:..."
            if (tmp != row.regNumber) {
                totalRows.put(i, getNewRow(tmp, totalColumns, sums, data))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по ГРН:..."
            if (i == getRows(data).size() - 2) {
                totalColumns.each {
                    sums[it] += (row.getCell(it).getValue() ?: 0)
                }
                totalRows.put(i + 1, getNewRow(row.regNumber, totalColumns, sums, data))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += (row.getCell(it).getValue() ?: 0)
            }
            tmp = row.regNumber
        }
    }
    // добавить "итого по ГРН:..." в таблицу
    def i = 0
    totalRows.each { index, row ->
        i = i + 1
        data.insert(row, index + i)
    }
    save(data)
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
        // 15. Обязательность заполнения поля графы 1..3, 5..13
        // список проверяемых столбцов
        def columns = ['rowNumber', 'regNumber', 'tradeNumber', 'lotSizeCurrent', 'reserve',
                'cost', 'signSecurity', 'costOnMarketQuotation',
                'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
        if (!checkRequiredColumns(row, columns)) {
            return false
        }
    }

    def formDataOld = getFormDataOld()
    def dataOld = getData(formDataOld)

    if (dataOld != null && !getRows(dataOld).isEmpty()) {
        // 1. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 11)
        //      в текущем отчетном периоде (выполняется один раз для всего экземпляра)
        def count
        def missContract = []
        def severalContract = []
        getRows(dataOld).each { prevRow ->
            if (!isFixedRow(prevRow) && prevRow.reserveCalcValue > 0) {
                count = 0
                getRows(data).each { row ->
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

    if (!getRows(data).isEmpty()) {
        def i = 1

        // суммы строки общих итогов
        def totalSums = [:]
        // графы для которых надо вычислять итого и итого по ГРН (графа 4..7, 10..13)
        def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
                'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
        // признак наличия итоговых строк
        def hasTotal = false
        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        def name
        def tmp

        for (def row : getRows(data)) {
            if (isFixedRow(row)) {
                hasTotal = true
                continue
            }

            def index = row.rowNumber
            def errorMsg
            if (index!=null && index!='') {
                errorMsg = "В строке \"№ пп\" равной $index "
            } else {
                index = row.getIndex()
                errorMsg = "В строке $index "
            }

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
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 5. Проверка необращающихся акций (графа 8, 11, 12)
            def sign = getSign(row.signSecurity)
            if (sign == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                logger.warn(errorMsg + 'облигации необращающиеся, графы 11 и 12 ненулевые!')
            }

            // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
            if (sign == '+' && row.reserveCalcValue - row.reserve > 0 && row.reserveRecovery != 0) {
                loggerError(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 12)
            if (sign == '+' && row.reserveCalcValue - row.reserve < 0 && row.reserveCreation != 0) {
                loggerError(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
            if (sign == '+' && row.reserveCalcValue - row.reserve == 0 &&
                    (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
                loggerError(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 9. Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                    row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
                logger.warn(errorMsg + 'резерв сформирован. Графы 5, 7, 10 и 11 неположительные!')
            }

            // 10. Проверка корректности создания резерва (графа 6, 11, 12, 13)
            if (row.reserve + row.reserveCreation != row.reserveCalcValue + row.reserveRecovery) {
                loggerError(errorMsg + 'резерв сформирован некорректно!')
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 11. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 4, 5 (за предыдущий период) )
            if (!isBalancePeriod && !isConsolidated && checkOld(row, 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', dataOld)) {
                def curCol = 3
                def curCol2 = 4
                def prevCol = 3
                def prevCol2 = 5
                loggerError("РНУ сформирован некорректно! " + errorMsg + "Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-25 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-25 за предыдущий отчётный период.")
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 12. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 6, 11 (за предыдущий период) )
            if (!isBalancePeriod && !isConsolidated && checkOld(row, 'tradeNumber', 'reserve', 'reserveCalcValue', dataOld)) {
                def curCol = 3
                def curCol2 = 3
                def prevCol = 6
                def prevCol2 = 11
                loggerError("РНУ сформирован некорректно! " + errorMsg + "Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-25 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-25 за предыдущий отчётный период.")
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 16. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.rowNumber) {
                loggerError('Нарушена уникальность номера по порядку!') // TODO (Aydar Kadyrgulov)
                if (!isBalancePeriod) {
                    return false
                }
            }
            i += 1

            // 17. Арифметические проверки граф 6, 10, 11, 12, 13 =========================
            if (!isBalancePeriod) {
                // графа 6
                if (row.reserve != getValueForColumn6(dataOld, row)) {
                    name = getColumnName(row, 'reserve')
                    logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
                }

                // графа 10
                tmp = (row.marketQuotation ? roundTo2(row.lotSizeCurrent * row.marketQuotation) : 0)
                if (row.costOnMarketQuotation != tmp) {
                    name = getColumnName(row, 'costOnMarketQuotation')
                    logger.warn(errorMsg + "Неверно рассчитана графа «$name»!")
                }

                // графа 11
                if (sign == '+') {
                    def a = (row.cost == null ? 0 : row.cost)
                    tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
                } else {
                    tmp = 0
                }
                if (row.reserveCalcValue != roundTo2(tmp)) {
                    name = getColumnName(row, 'reserveCalcValue')
                    logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
                }

                // графа 12
                tmp = roundTo2(row.reserveCalcValue - row.reserve)
                if (row.reserveCreation != (tmp > 0 ? tmp : 0)) {
                    name = getColumnName(row, 'reserveCreation')
                    logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
                }

                // графа 13
                if (row.reserveRecovery != (tmp < 0 ? Math.abs(tmp) : 0)) {
                    name = getColumnName(row, 'reserveRecovery')
                    logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
                }
            }
            // 17. конец арифметической проверки =================================

            // 18. Проверка итоговых значений по ГРН
            if (!totalGroupsName.contains(row.regNumber)) {
                totalGroupsName.add(row.regNumber)
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
            def totalRow = getRowByAlias(data, 'total')
            def totalRowOld = getRowByAlias(dataOld, 'total')
            // 13. Проверка корректности заполнения РНУ (графа 4, 5 (за предыдущий период))
            if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                def curCol = 4
                def prevCol = 5
                loggerError("РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе $curCol = «Общий итог» по графе $prevCol формы РНУ-25 за предыдущий отчётный период.")
                if (!isBalancePeriod) {
                    return false
                }
            }

            // 14. Проверка корректности заполнения РНУ (графа 6, 11 (за предыдущий период))
            if (totalRow.reserve != totalRowOld.reserveCalcValue) {
                def curCol = 6
                def prevCol = 11
                loggerError("РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе $curCol = «Общий итог» по графе $prevCol формы РНУ-25 за предыдущий отчётный период.")
                if (!isBalancePeriod) {
                    return false
                }
            }
        }

        if (hasTotal) {
            def totalRow = getRowByAlias(data, 'total')

            // 17. Проверка итоговых значений по ГРН
            for (def codeName : totalGroupsName) {
                def totalRowAlias = 'total' + getRowNumber(codeName, data)

                if (!checkAlias(getRows(data), totalRowAlias)) {
                    logger.warn("Итоговые значения по ГРН $codeName не рассчитаны! Необходимо расчитать данные формы.")
                    continue
                }
                def row = getRowByAlias(data, 'total' + getRowNumber(codeName, data))
                for (def alias : totalColumns) {
                    if (calcSumByCode(data, codeName, alias) != row.getCell(alias).getValue()) {
                        loggerError("Итоговые значения по ГРН $codeName рассчитаны неверно!")
                        if (!isBalancePeriod) {
                            return false
                        }
                    }
                }
            }

            // 18. Проверка итогового значений по всей форме
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
    getRows(data).each { row->
        // 1. Проверка актуальности поля «Признак ценной бумаги на текущую отчётную дату»
        if (!isFixedRow(row) && row.signSecurity!=null && getSign(row.signSecurity)==null) {
            if (row.rowNumber != null && row.rowNumber != '') {
                logger.warn("В строке $row.rowNumber Признак ценной бумаги на текущую отчётную дату указан неверно!")
            } else {
                def tmp = row.getIndex()
                logger.warn("В строке $tmp Признак ценной бумаги на текущую отчётную дату указан неверно!")
            }
        }
    }
    return true
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
                def sourceData = getData(source)
                getRows(sourceData).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        newRows.add(row)
                    }
                }
            }
        }
    }
    if (!newRows.isEmpty()) {
        data.insert(newRows, 1)
        sort(data)
    }
    def total = getCalcTotalRow()
    insert(data, total)
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
    newRow.regNumber = alias + ' итог'
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
 * @param dataOld данные нф предыдущего периода
 */
def checkOld(def row, def likeColumnName, def curColumnName, def prevColumnName, def dataOld) {
    if (dataOld == null) {
        return false
    }
    if (row.getCell(likeColumnName).getValue() == null) {
        return false
    }
    for (def prevRow : getRows(dataOld)) {
        if (prevRow.getAlias() != null || prevRow.getAlias() != '') {
            continue
        }
        if (row.getCell(likeColumnName).getValue() == prevRow.getCell(likeColumnName).getValue() &&
                row.getCell(curColumnName).getValue() != prevRow.getCell(prevColumnName).getValue()) {
            return true
        }
    }
    return false
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent',
            'reserve', 'cost', 'signSecurity', 'marketQuotation', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
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

    // РНУ-25 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * Посчитать сумму указанного графа для строк с общим кодом классификации
 *
 * @param data данные формы
 * @param regNumber код классификации дохода
 * @param alias название графа
 */
def calcSumByCode(def data, def regNumber, def alias) {
    def sum = 0
    getRows(data).each { row ->
        if (!isFixedRow(row) && row.regNumber == regNumber) {
            sum += (row.getCell(alias).getValue() ?: 0)
        }
    }
    return sum
}

/**
 * Получить номер строки в таблице.
 *
 * @param data данные нф
 * @param row строка
 */
def getIndex(def data, def row) {
    getRows(data).indexOf(row)
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
            def data = getData(formData)
            index = getIndex(data, row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Получить значение за предыдущий отчетный период для графы 6.
 *
 * @param dataOld данные за предыдущий период
 * @param row строка текущего периода
 * @return возвращает найденое значение, иначе возвратит 0
 */
def getValueForColumn6(def dataOld, def row) {
    if (isConsolidated) {
        return row.reserve
    }
    def value = 0
    def count = 0
    if (dataOld != null && !getRows(dataOld).isEmpty()) {
        for (def rowOld : getRows(dataOld)) {
            if (rowOld.tradeNumber == row.tradeNumber) {
                value = roundTo2(rowOld.reserveCalcValue)
                if (value!=null) {
                    count += 1
                }
            }
        }
    }
    // если count не равно 1, то или нет формы за предыдущий период,
    // или нет соответствующей записи в предыдущем периода или записей несколько
    return (count == 1 ? value : 0)
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
    def dataOld = getData(formDataOld)

    if (formDataOld != null && formDataOld.state == WorkflowState.ACCEPTED && !getRows(dataOld).isEmpty()) {
        return true
    }
    return false
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 * @return
 */
def getRowByAlias(def data, def alias) {
    data.getDataRow(getRows(data), alias)
}

/**
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void save(def data) {
    data.save(getRows(data))
}

/**
 * Вставить новую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}

/**
 * Удалить строку из нф
 *
 * @param data данные нф (helper)
 * @param row строка для удаления
 */
void deleteRow(def data, def row) {
    data.delete(row)
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
 * Проверить наличие итоговой строки.
 *
 * @param data данные нф (helper)
 */
def hasTotal(def data) {
    for (def row: getRows(data)) {
        if (row.getAlias() == 'total') {
            return true
        }
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
    Date date = new Date()

    def cache = [:]
    def data = getData(formData)
    data.clear()
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
        newRow.lotSizePrev = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 5
        newRow.lotSizeCurrent = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 6
        newRow.reserve = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 7
        newRow.cost = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 8
        newRow.signSecurity = getRecords(62, 'CODE', row.cell[indexCell].text(), date, cache)
        indexCell++

        // графа 9
        newRow.marketQuotation = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 10
        newRow.costOnMarketQuotation = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 11
        newRow.reserveCalcValue = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 12
        newRow.reserveCreation = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 13
        newRow.reserveRecovery = getNumber(row.cell[indexCell].text())

        newRows.add(newRow)
    }
    data.insert(newRows, 1)

    // итоговая строка
    if (xml.rowTotal.size() == 1) {
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 4
        total.lotSizePrev = getNumber(row.cell[4].text())

        // графа 5
        total.lotSizeCurrent = getNumber(row.cell[5].text())

        // графа 7
        total.cost = getNumber(row.cell[7].text())

        // графа 10
        total.costOnMarketQuotation = getNumber(row.cell[10].text())

        // графа 11
        total.reserveCalcValue = getNumber(row.cell[11].text())

        // графа 12
        total.reserveCreation = getNumber(row.cell[12].text())

        // графа 13
        total.reserveRecovery = getNumber(row.cell[13].text())

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
    String filter = code + "= '"+ value.replaceAll(' ', '')+"'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter] != null) {
            return cache[ref_id][filter]
        }
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1){
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    loggerError("Не удалось найти запись в справочнике (id=$ref_id) с атрибутом $code равным $value!")
    return null
}

/**
 * Получить строку по номеру договора
 * @param data
 * @param tradeNumber
 * @return
 */
def getRowByTradeNumber(def data, def tradeNumber) {
    if (tradeNumber == null) {
        return null
    }
    for (def row : getRows(data)) {
        if (row.tradeNumber == tradeNumber) {
            return row
        }
    }
    return null
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
 * Получить признак ценной бумаги
 */
def getSign(def sign) {
    return  refBookService.getStringValue(62,sign,'CODE')
}

/**
 * Хелпер для округления чисел.
 *
 * @param value значение округляемое до целого
 */
BigDecimal roundTo2(BigDecimal value) {
    if (value != null) {
        return value.setScale(2, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

/**
 * Получение первого rowNumber по regNumber
 * @param alias
 * @param data
 * @return
 */
def getRowNumber(def alias, def data) {
    for(def row: getRows(data)){
        if (row.regNumber==alias) {
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
    def totalColumns = [4 : 'lotSizePrev', 5 : 'lotSizeCurrent', 7 : 'cost', 10 : 'costOnMarketQuotation',
            11 : 'reserveCalcValue', 12 : 'reserveCreation', 13 : 'reserveRecovery']
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
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    // графы для которых надо вычислять итого и итого по ГРН (графа 4..7, 10..13)
    def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
    def totalRow = formData.createDataRow()

    totalRow.setAlias('total')
    totalRow.regNumber = 'Общий итог'
    setTotalStyle(totalRow)
    def data = getData(formData)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(data, alias))
    }
    return totalRow
}

/**
 * Отсорировать данные (по графе 2, 3).
 *
 * @param data данные нф (хелпер)
 */
void sort(def data) {
    // отсортировать/группировать
    getRows(data).sort { def a, def b ->
        // графа 2  - regNumber
        // графа 3  - tradeNumber
        if (a.regNumber == b.regNumber) {
            return a.tradeNumber <=> b.tradeNumber
        }
        return a.regNumber <=> b.regNumber
    }
}

def loggerError(def msg) {
    if (isBalancePeriod) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}