package form_template.income.rnu25

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения".
 *
 * @version 65
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *      - графа 4 заполняется автоматически при пересчете, но является редактируемой, возможно надо брать возможность редактировать
 *      - проблемы с индексами
 *      - получение имени файла при импорте
 *      - импорт данных сделан без чтз
 *
 * @author rtimerbaev
 */

/** Отчётный период. */
def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

/** Признак периода ввода остатков. */
def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
            return
        }
        logicalCheck(true)
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
            return
        }
        calc()
        logicalCheck(false)
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck(true)
        checkNSI()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        checkNSI()
        // для сохранения изменении приемников
        getData(formData).commit()
        break
    case FormDataEvent.IMPORT :
        importData()
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

    def newRow = getNewRow()
    if (getRows(data).size()>0) {
        for(int i = getRows(data).size()-1;i>=0;i--){
            def row = getRows(data).get(i)
            if(!isTotal(row)){
                newRow.rowNumber = row.rowNumber+1
                break
            }
        }
    } else {
        newRow.rowNumber = 1
    }

    insert(data, newRow)
    // data.insert(newRow, getIndex(data, currentDataRow) + 1) // TODO (Ramil Timerbaev) проблемы с индексом

    return newRow
}

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()

    // графа 2..5, 7..9
    ['regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent',
            'cost', 'signSecurity', 'marketQuotation',].each {
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
     * Проверка объязательных полей.
     */

    // список проверяемых столбцов (графа 2, 3, 5, 7, 8)
    def requiredColumns = ['regNumber', 'tradeNumber', 'lotSizeCurrent', 'cost', 'signSecurity']
    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчеты.
     */

    // удалить строку "итого" и "итого по ГРН: ..."
    def delRow = []
    getRows(data).each { row ->
        if (isTotal(row)) {
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
    getRows(data).sort { it.regNumber }

    def tmp
    def formDataOld = getFormDataOld()
    def dataOld = getData(formDataOld)

    getRows(data).eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 4
        row.lotSizePrev = getValueForColumn4(dataOld, formDataOld, row)

        // графа 6
        row.reserve = getValueForColumn6(dataOld, row)

        // графа 10
        row.costOnMarketQuotation = (row.marketQuotation ? round(row.lotSizeCurrent * row.marketQuotation, 2) : 0)

        // графа 11
        if (getSign(row.signSecurity) == '+') {
            def a = (row.cost ?: 0)
            tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
        } else {
            tmp = 0
        }
        row.reserveCalcValue = round(tmp, 2)

        // графа 12
        tmp = round(row.reserveCalcValue - row.reserve, 2)
        row.reserveCreation = (tmp > 0 ? tmp : 0)

        // графа 13
        row.reserveRecovery = (tmp < 0 ? Math.abs(tmp) : 0)
    }
    save(data)

    // графы для которых надо вычислять итого и итого по ГРН (графа 4..7, 10..13)
    def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
    // добавить строку "итого"
    def totalRow = formData.createDataRow()
    insert(data, totalRow)
    totalRow.setAlias('total')
    totalRow.regNumber = 'Общий итог'
    setTotalStyle(totalRow)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(data, alias))
    }

    // посчитать "итого по ГРН:..."
    def totalRows = [:]
    tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }
    getRows(data).eachWithIndex { row, i ->
        if (!isTotal(row)) {
            if (tmp == null) {
                tmp = row.regNumber
            }
            // если код расходы поменялся то создать новую строку "итого по ГРН:..."
            if (tmp != row.regNumber) {
                totalRows.put(i, getNewRow(tmp, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по ГРН:..."
            if (i == getRows(data).size() - 2) {
                totalColumns.each {
                    sums[it] += (row.getCell(it).getValue() ?: 0)
                }
                totalRows.put(i + 1, getNewRow(row.regNumber, totalColumns, sums))
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
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    def formDataOld = getFormDataOld()
    def dataOld = getData(formDataOld)

    def data = getData(formData)

    if (dataOld != null && !getRows(dataOld).isEmpty()) {
        // 1. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 11)
        //      в текущем отчетном периоде (выполняется один раз для всего экземпляра)
        def count
        def missContract = []
        def severalContract = []
        getRows(dataOld).each { prevRow ->
            if (prevRow.reserveCalcValue > 0) {
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

        // список проверяемых столбцов (графа ..)
        def columns = ['rowNumber', 'regNumber', 'tradeNumber', 'lotSizeCurrent', 'reserve',
                'cost', 'signSecurity', 'costOnMarketQuotation',
                'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
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
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 15. Обязательность заполнения поля графы 1..3, 5..13
            if (!checkRequiredColumns(row, columns, useLog)) {
                return false
            }

            // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 6, 13)
            if (row.lotSizeCurrent == 0 && row.reserve != row.reserveRecovery) {
                logger.warn('Графы 6 и 13 неравны!')
            }

            // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 7, 10, 11)
            if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
                logger.warn('Графы 7, 10 и 11 ненулевые!')
            }

            // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 4, 6, 13)
            if (row.lotSizePrev == 0 && (row.reserve != 0 || row.reserveRecovery != 0)) {
                logger.error('Графы 6 и 13 ненулевые!')
                return false
            }

            // 5. Проверка необращающихся акций (графа 8, 11, 12)
            def sign = getSign(row.signSecurity)
            if (sign == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                logger.warn('Облигации необращающиеся, графы 11 и 12 ненулевые!')
            }

            // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
            if (sign == '+' && row.reserveCalcValue - row.reserve > 0 && row.reserveRecovery != 0) {
                logger.error('Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
                return false
            }

            // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 12)
            if (sign == '+' && row.reserveCalcValue - row.reserve < 0 && row.reserveCreation != 0) {
                logger.error('Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
                return false
            }

            // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
            if (sign == '+' && row.reserveCalcValue - row.reserve == 0 &&
                    (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
                logger.error('Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
                return false
            }

            // 9. Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                    row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
                logger.warn('Резерв сформирован. Графы 5, 7, 10 и 11 неположительные!')
            }

            // 10. Проверка корректности создания резерва (графа 6, 11, 12, 13)
            if (row.reserve + row.reserveCreation != row.reserveCalcValue + row.reserveRecovery) {
                logger.error('Резерв сформирован некорректно!')
                return false
            }

            // 11. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 4, 5 (за предыдущий период) )
            if (checkOld(row, 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', dataOld)) {
                def curCol = 3
                def curCol2 = 4
                def prevCol = 3
                def prevCol2 = 5
                logger.error("РНУ сформирован некорректно! Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-25 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-25 за предыдущий отчётный период.")
                return false
            }

            // 12. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 6, 11 (за предыдущий период) )
            if (checkOld(row, 'tradeNumber', 'reserve', 'reserveCalcValue', dataOld)) {
                def curCol = 3
                def curCol2 = 3
                def prevCol = 6
                def prevCol2 = 11
                logger.error("РНУ сформирован некорректно! Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-25 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-25 за предыдущий отчётный период.")
                return false
            }

            // 16. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                return false
            }
            i += 1

            // 17. Арифметические проверки граф 6, 10, 11, 12, 13 =========================
            // графа 4
            if (row.lotSizePrev != getValueForColumn4(dataOld, formDataOld, row)) {
                name = getColumnName(row, 'lotSizePrev')
                logger.warn("Неверно рассчитана графа «$name»!")
            }

            // графа 6
            if (row.reserve != getValueForColumn6(dataOld, row)) {
                name = getColumnName(row, 'reserve')
                logger.warn("Неверно рассчитана графа «$name»!")
            }

            // графа 10
            tmp = (row.marketQuotation ? round(row.lotSizeCurrent * row.marketQuotation, 2) : 0)
            if (row.costOnMarketQuotation != tmp) {
                name = getColumnName(row, 'costOnMarketQuotation')
                logger.warn("Неверно рассчитана графа «$name»!")
            }

            // графа 11
            if (sign == '+') {
                def a = (row.cost == null ? 0 : row.cost)
                tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
            } else {
                tmp = 0
            }
            if (row.reserveCalcValue != round(tmp, 2)) {
                name = getColumnName(row, 'reserveCalcValue')
                logger.warn("Неверно рассчитана графа «$name»!")
            }

            // графа 12
            tmp = round(row.reserveCalcValue - row.reserve, 2)
            if (row.reserveCreation != (tmp > 0 ? tmp : 0)) {
                name = getColumnName(row, 'reserveCreation')
                logger.warn("Неверно рассчитана графа «$name»!")
            }

            // графа 13
            if (row.reserveRecovery != (tmp < 0 ? Math.abs(tmp) : 0)) {
                name = getColumnName(row, 'reserveRecovery')
                logger.warn("Неверно рассчитана графа «$name»!")
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
                logger.error("РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе $curCol = «Общий итог» по графе $prevCol формы РНУ-25 за предыдущий отчётный период.")
                return false
            }

            // 14. Проверка корректности заполнения РНУ (графа 6, 11 (за предыдущий период))
            if (totalRow.reserve != totalRowOld.reserveCalcValue) {
                def curCol = 6
                def prevCol = 11
                logger.error("РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе $curCol = «Общий итог» по графе $prevCol формы РНУ-25 за предыдущий отчётный период.")
                return false
            }
        }

        if (hasTotal) {
            def totalRow = getRowByAlias(data, 'total')

            // 17. Проверка итоговых значений по ГРН
            for (def codeName : totalGroupsName) {
                def totalRowAlias = 'total' + codeName

                if (!checkAlias(getRows(data), totalRowAlias)) {
                    logger.warn("Итоговые значения по ГРН $codeName не рассчитаны! Необходимо расчитать данные формы.")
                    continue
                }
                def row = getRowByAlias(data, 'total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(data, codeName, alias) != row.getCell(alias).getValue()) {
                        logger.error("Итоговые значения по ГРН $codeName рассчитаны неверно!")
                        return false
                    }
                }
            }

            // 18. Проверка итогового значений по всей форме
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    logger.error('Итоговые значения рассчитаны неверно!')
                    return false
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
    getRows (data).each { row->
        // 1. Проверка актуальности поля «Признак ценной бумаги на текущую отчётную дату»
        if (!isTotal(row) && row.signSecurity!=null && getSign(row.signSecurity)==null) {
            logger.warn('Признак ценной бумаги на текущую отчётную дату указан неверно!')
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

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceData = getData(source)
                getRows(sourceData).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        insert(data, row)
                    }
                }
            }
        }
    }
    data.commit()
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
        return
    }

    def is = ImportInputStream
    if (is == null) {
        return
    }

    if (!fileName.contains('.r')) {
        logger.error("Некорректное расширение файла")
        return
    }

    def xmlString = importService.getData(is, fileName, 'cp866')
    if (xmlString == null) {
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        return
    }

    def data = getData(formData)
    def totalColumns = [4:'lotSizePrev', 5:'lotSizeCurrent', 7:'cost', 10:'costOnMarketQuotation', 11:'reserveCalcValue']
    // добавить данные в форму
    boolean canCommit = true
//    {
        def totalLoad = addData(xml)
        if (totalLoad!=null) {
            calc()
//            logicalCheck(false)
//            checkNSI()

            def totalCalc
            for (def row : getRows(data))
                if (isTotal(row)) totalCalc = row

            totalColumns.each{k, v->
                if (totalCalc[v]!=totalLoad[v]) {
                    logger.error("Итоговая сумма в графе $k в транспортном файле некорректна")
                    //canCommit = false
                }
            }

        } else {
            logger.error("Нет итоговой строки.")
            canCommit = false
        }
        logger.info('Закончена загрузка файла ' + fileName)
/*    } catch(Exception e) {
        logger.error(""+e.message)
        canCommit = false
    }  */
    //в случае ошибок откатить изменения
    if (!canCommit) {
        logger.error("Загрузка файла $fileName завершилась ошибкой")
    }
    data.commit()
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('tot')
}

/**
 * Получить сумму столбца.
 */
def getSum(def data, def columnAlias) {
    def from = 0
    def to = getRows(data).size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(data), new ColumnRange(columnAlias, from, to))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('tot' + alias)
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
        if (!isTotal(row) && row.regNumber == regNumber) {
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
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns, def useLog) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
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
    def value = 0
    def count = 0
    if (dataOld != null && !getRows(dataOld).isEmpty()) {
        for (def rowOld : getRows(dataOld)) {
            if (rowOld.tradeNumber == row.tradeNumber) {
                value = round(rowOld.reserveCalcValue, 2)
                count += 1
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
 * Получить значение за предыдущий отчетный период для графы 4
 *
 * @param dataOld данные за предыдущий период (helper)
 * @param formDataOld форма за предыдущий период
 * @param row строка текущего периода
 * @return возвращает найденое значение, иначе возвратит 0
 */
def getValueForColumn4(def dataOld, def formDataOld, def row) {
    def value = 0
    def count = 0
    if (dataOld != null && !getRows(dataOld).isEmpty() && formDataOld.state == WorkflowState.ACCEPTED) {
        for (def rowOld : getRows(dataOld)) {
            if (rowOld.tradeNumber == row.tradeNumber) {
                value = (getSign(rowOld.signSecurity) == '+' && getSign(row.signSecurity) == '-' ? rowOld.lotSizePrev : 0)
                count += 1
            }
        }
    }
    // если count не равно 1, то или нет формы за предыдущий период,
    // или нет соответствующей записи в предыдущем периода или записей несколько
    return (count == 1 ? value : 0)
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
 */
def addData(def xml) {
    Date date = new Date()

    def cache = [:]
    def data = getData(formData)
    data.clear()

    def total = formData.createDataRow()

    def indexRow = -1
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= 0) {// || indexRow>20) {
            continue
        }
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

        insert(data, newRow)
    }
    // проверка итоговой строки
    if (xml.rowTotal.size()==1)
        for (def row : xml.rowTotal) {

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

        }
    else {
        return null
    }
    data.commit()
    return total
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    if (value == null) {
        return null
    }
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    // поменять запятую на точку и убрать пробелы
    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
    return new BigDecimal(tmp)
}

def getRecords(def ref_id, String code, String value, Date date, def cache) {
    String filter = code + "= '"+ value.replaceAll(' ', '')+"'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter]!=null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1){
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось определить элемент справочника!")
    return null;
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
