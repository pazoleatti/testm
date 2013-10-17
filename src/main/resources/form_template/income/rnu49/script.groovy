package form_template.income.rnu49

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * Скрипт для РНУ-49 (rnu49.groovy).
 * Форма "(РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *      - уникальность инвентарного номера
 *      - расчеты и проверки при консолидации
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        allCheck()
        break
    case FormDataEvent.CALCULATE :
        if (calculate()){
            allCheck()
        }
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
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED : // после принятия из подготовлена
        allCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        if (calculate()){
            allCheck()
        }
        break
}

// графа 1  - rowNumber
// графа 2  - firstRecordNumber
// графа 3  - operationDate
// графа 4  - reasonNumber
// графа 5  - reasonDate
// графа 6  - invNumber
// графа 7  - name
// графа 8  - price
// графа 9  - amort
// графа 10 - expensesOnSale
// графа 11 - sum
// графа 12 - sumInFact
// графа 13 - costProperty
// графа 14 - marketPrice
// графа 15 - sumIncProfit
// графа 16 - profit
// графа 17 - loss
// графа 18 - usefullLifeEnd
// графа 19 - monthsLoss
// графа 20 - expensesSum
// графа 21 - saledPropertyCode
// графа 22 - saleCode

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = data
    def rows = getRows(data)
    def newRow = formData.createDataRow()
    // графа 2..14, 18, 19, 21..22
    ['firstRecordNumber', 'operationDate', 'reasonNumber', 'reasonDate',
            'invNumber', 'name', 'price', 'amort', 'expensesOnSale', 'sum',
            'sumInFact', 'costProperty', 'marketPrice', 'saledPropertyCode', 'saleCode'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        def row = data.getDataRow(rows,'totalA')
        data.insert(newRow,getIndex(row)+1)
    } else if (currentDataRow.getAlias() == null) {
        data.insert(newRow, currentDataRow.getIndex()+1)
    } else {
        def alias = currentDataRow.getAlias()
        def row = data.getDataRow(rows, alias.contains('total') ? alias : 'total' + alias)
        data.insert(newRow, getIndex(row)+1)
    }
}

def allCheck() {
    return !hasError() && logicalCheck() && checkNSI()
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isFixedRow(currentDataRow)) {
        data.delete(currentDataRow)
    }
}

def calculate(){
    def data = data
    def rows = getRows(data)
    def isValid = true
    if (formData.kind != FormDataKind.CONSOLIDATED){
        isValid =  preCalc(data, rows)
        if (isValid){
            sort(data, rows)
            calc(data, rows)
            calcTotal(data, rows)
        }
    } else {
        sort(data, rows)
        calcTotal(data, rows)
    }
    if(isValid){
        data.update(rows)
    }
    return isValid
}

/*
 * Проверка редактируемых обязательных полей перед расчетом.
 */
def preCalc(def data, def rows){

    // Список проверяемых столбцов (графа 2..14, 21, 22)
    def requiredColumns = ['firstRecordNumber', 'operationDate', 'reasonNumber', 'reasonDate',
            'invNumber', 'name', 'price', 'amort', 'expensesOnSale',
            'sum', 'sumInFact', 'costProperty', 'marketPrice', 'saledPropertyCode', 'saleCode']

    for (def row : rows) {
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }
    return true

}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc(def data, def rows) {
    def data46 = getData(formData46)
    def data45 = getData(formData45)
    def data12 = getData(formData12)

    /*
     * Расчеты.
     */
    i=1
    // графа 1, 15..17, 20
    rows.each { row ->
        if (!isFixedRow(row)) {
            // графа 1
            row.rowNumber = i++

            def row46 = getRow46(row, data46)
            def row45 = getRow45(row, data45)

            // графа 8
            def graph8 = getGraph8(row, row46, row45, data12)
            if (graph8 != null) {
                row.price = graph8
            }

            // графа 9
            def graph9 = getGraph9(row, row46, row45)
            if (graph9 != null) {
                row.amort = graph9
            }

            // графа 15
            def tmp
            if (row.sum - row.marketPrice * 0.8 > 0) {
                tmp = 0
            } else {
                tmp = row.marketPrice * 0.8 - row.sum
            }
            row.sumIncProfit = roundTo(tmp, 2)

            tmp = row.sum?:0 - (row.price?:0 - row.amort?:0) - row.expensesOnSale?:0 + row.sumIncProfit?:0

            if (tmp>0) {
                // графа 16
                row.profit = tmp
                row.loss = 0
            } else {
                // графа 17
                row.loss = abs(row.profit)
                row.profit = 0
            }

            // графа 18
            row.usefullLifeEnd = getGraph18(row, row46)

            // графа 19
            row.monthsLoss = getGraph19(row)

            // графа 20
            row.expensesSum = getGraph20(row)
        }
    }
}

void calcTotal(def data, def rows){
    // подразделы
    ['A', 'B', 'V', 'G', 'D', 'E'].each { section ->
        firstRow = data.getDataRow(rows,section)
        lastRow = data.getDataRow(rows,'total' + section)
        // графы для которых считать итого (графа 9..13, 15..17, 20)
        ['amort', 'expensesOnSale', 'sum', 'sumInFact', 'costProperty', 'sumIncProfit',
                'profit', 'loss', 'expensesSum'].each {
            lastRow.getCell(it).setValue(getSum(it, firstRow, lastRow))
        }
    }
}

/**
 * Отсортировать / группировать строки
 */
void sort(def data, def rows) {
    def sortRows = []
    def from
    def to

    ['A', 'B', 'V', 'G', 'D', 'E'].each { section ->
        from = getIndexByAlias(data, section) + 1
        to = getIndexByAlias(data, 'total'+section) - 1
        if (from<=to) {
            sortRows.add(rows[from..to])
        }

    }

    sortRows.each {
        it.sort { it.operationDate }
    }
    data.save(rows)
}

/**
 * Получить номер строки в таблице по псевдонимиу (0..n).
 */
def getIndexByAlias(def data, String rowAlias) {
    def row = getRowByAlias(data,rowAlias)
    return (row != null ? getIndex(row) : -1)
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    if (alias == null || alias == '' || data == null) {
        return null
    }
    return data.getDataRow(getRows(data),alias)
}

/**
 * Логические проверки.
 *
 */
def logicalCheck() {
    def data = data
    def rows = getRows(data)
    def data12 = getData(formData12)
    def data46 = getData(formData46)
    def data45 = getData(formData45)
    def numbers = []
    if (!rows.isEmpty()) {
        // Список проверяемых столбцов (графа 1..17, 21, 22)
        def columns = ['rowNumber', 'firstRecordNumber', 'operationDate', 'reasonNumber',
                'reasonDate', 'invNumber', 'name', 'price', 'amort', 'expensesOnSale',
                'sum', 'sumInFact', 'costProperty', 'marketPrice', 'sumIncProfit',
                'profit', 'loss', 'saledPropertyCode', 'saleCode']

        for (def row : rows) {
            if (isFixedRow(row)) {
                continue
            }
            def rowStart = getRowIndexString(row)

            // 1. Обязательность заполнения поля графы (графа 1..22)
            if (!checkRequiredColumns(row, columns)) {
                return false
            }

            // для консолидированной формы пропускаем остальные проверки
            if (formData.kind == FormDataKind.CONSOLIDATED){
                return true
            }

            // 2. Проверка на уникальность поля «инвентарный номер» (графа 6)
            // TODO в рамках текущего года
            if (row.invNumber in numbers) {
                logger.error("Инвентарный номер ${row.invNumber} не уникальный!")
                return false
            } else {
                numbers += row.invNumber
            }

            // 3. Проверка на нулевые значения (графа 8, 13, 15, 17, 20)
            if (row.price == 0 &&
                    row.costProperty != 0 &&
                    row.sumIncProfit == 0 &&
                    row.loss != 0 &&
                    row.expensesSum == 0) {
                logger.error(rowStart + 'все суммы по операции нулевые!')
                return false
            }
            // 4. Проверка формата номера первой записи	Формат графы 2: ГГ-НННН
            if (!row.firstRecordNumber.matches('\\w{2}-\\w{6}')) {
                logger.error(rowStart + 'неправильно указан номер предыдущей записи!')
                return false
            }

            // 6. Проверка существования необходимых экземляров форм (РНУ-46)
            if (data46 == null || getRows(data46).size()==0){
                logger.error('Отсутствуют данные РНУ-46!!')
                return false
            }

            def row46 = getRow46(row, data46)
            def row45 = getRow45(row, data45)

            // Арифметическая проверка графы 8
            def graph8 = getGraph8(row, row46, row45, data12)
            if (graph8!=null && row.price != graph8) {
                logger.warn(rowStart + 'неверное значение графы «Цена приобретения»!')
            }

            // Арифметическая проверка графы 9
            def graph9 = getGraph9(row, row46, row45)
            if (row.amort != graph9) {
                logger.warn(rowStart + 'неверное значение графы «Фактически начислено амортизации (отнесено на расходы)»!')
            }

            // Арифметическая проверка графы 15
            if (graph9!=null && row.sum - row.marketPrice * 0.8 > 0) {
                tmp = 0
            } else {
                tmp = row.marketPrice * 0.8 - row.sum
            }
            if (row.sumIncProfit != roundTo(tmp, 2)) {
                logger.warn(rowStart + 'неверное значение графы «Сумма к увеличению прибыли (уменьшению убытка)»!')
            }

            // Арифметическая проверка графы 16
            if (row.profit != (row.sum - (row.price - row.amort) - row.expensesOnSale + row.sumIncProfit)) {
                logger.warn(rowStart + 'неверное значение графы «Прибыль от реализации»!')
            }

            // Арифметическая проверка графы 17
            if (row.loss != row.profit) {
                logger.warn(rowStart + 'неверное значение графы «Убыток от реализации»!')
            }

            // Арифметическая проверка графы 18
            if (row.usefullLifeEnd != getGraph18(row, row46)) {
                logger.warn(rowStart + 'неверное значение графы «Дата истечения срока полезного использования»!')
            }

            // Арифметическая проверка графы 19
            if (row.monthsLoss != getGraph19(row)) {
                logger.warn(rowStart + 'неверное значение графы «Количество месяцев отнесения убытков на расходы»!')
            }

            // Арифметическая проверка графы 20
            if (row.expensesSum != getGraph20(row)) {
                logger.warn(rowStart + 'неверное значение графы «Сумма расходов, приходящаяся на каждый месяц»!')
            }

        }
        // 9. Проверка итоговых значений формы
        // графы для которых считать итого (графа 9-13,15-17, 20)
        def totalColumns = ['amort', 'expensesOnSale', 'sum', 'sumInFact', 'costProperty', 'sumIncProfit',
                'profit', 'loss', 'expensesSum']
        // подразделы
        for (def section : ['A', 'B', 'V', 'G', 'D', 'E']) {
            firstRow = data.getDataRow(rows,section)
            lastRow = data.getDataRow(rows,'total' + section)
            for (def column : totalColumns) {
                if (lastRow.getCell(column).getValue().equals(getSum(column, firstRow, lastRow))) {
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
    if (!getRows(data).isEmpty()) {
        for (def row : getRows(data)) {
            if (isFixedRow(row)) {
                continue
            }

            def rowStart = getRowIndexString(row)
            // 1. Проверка шифра при реализации амортизируемого имущества
            // Графа 21 (группа «А») = 1 или 2, и графа 22 = 1
            def saledPropertyCode = getSaledPropertyCode(row.saledPropertyCode)
            def saleCode = getSaleCode(row.saleCode)
            if (isSection('A', row) &&
                    ((saledPropertyCode != 1 && saledPropertyCode != 2) || saleCode != 1)) {
                logger.error(rowStart + 'для реализованного амортизируемого имущества (группа «А») указан неверный шифр!')
                return false
            }

            // 2. Проверка шифра при реализации прочего имущества
            // Графа 21 (группа «Б») = 3 или 4, и графа 22 = 1
            if (isSection('B', row) &&
                    ((saledPropertyCode != 3 && saledPropertyCode != 4) || saleCode != 1)) {
                logger.error(rowStart + 'для реализованного прочего имущества (группа «Б») указан неверный шифр!')
                return false
            }

            // 3. Проверка шифра при списании (ликвидации) амортизируемого имущества
            // Графа 21 (группа «В») = 1 или 2, и графа 22 = 2
            if (isSection('V', row) &&
                    ((saledPropertyCode != 1 && saledPropertyCode != 2) || saleCode != 2)) {
                logger.error(rowStart + 'для списанного (ликвидированного) амортизируемого имущества (группа «В») указан неверный шифр!')
                return false
            }

            // 4. Проверка шифра при реализации имущественных прав (кроме прав требования, долей паёв)
            // Графа 21 (группа «Г») = 5, и графа 22 = 1
            if (isSection('G', row) &&
                    (saledPropertyCode != 5 || saleCode != 1)) {
                logger.error(rowStart + 'для реализованных имущественных прав (кроме прав требования, долей паёв) (группа «Г») указан неверный шифр!')
                return false
            }

            // 5. Проверка шифра при реализации прав на земельные участки
            // Графа 21 (группа «Д») = 6, и графа 22 = 1
            if (isSection('D', row) &&
                    (saledPropertyCode != 6 || saleCode != 1)) {
                logger.error(rowStart + 'для реализованных прав на земельные участки (группа «Д») указан неверный шифр!')
                return false
            }

            // 6. Проверка шифра при реализации долей, паёв
            if (isSection('E', row) &&
                    (saledPropertyCode != 7 || saleCode != 1)) {
                logger.error(rowStart + 'для реализованных имущественных прав (кроме прав требования, долей паёв) (группа «Е») указан неверный шифр!')
                return false
            }

            // 7. Проверка актуальности поля «Шифр вида реализованного (выбывшего) имущества»
            // Проверка соответствия «графы 21» справочным данным справочника «Шифр вида реализованного (выбывшего) имущества»
            if (row.saledPropertyCode!=null && getSaledPropertyCode(row.saledPropertyCode)==null) {
                logger.warn(rowStart + "в справочнике \"Шифр вида реализованного (выбывшего) имущества\" не найдено значение с id = ${row.saledPropertyCode} в поле \"Шифр вида реализованного (выбывшего) имущества\"!")
            }

            // 8. Проверка актуальности поля «Шифр вида реализации (выбытия)»
            // Проверка соответствия «графы 22» справочным данным справочника «Шифр вида реализации (выбытия)»
            if (saleCode !=null && getSaleCode(row.saleCode)==null) {
                logger.warn(rowStart + "в справочнике \"Шифр вида реализации (выбытия)\" не найдено значение с id = ${saleCode} в поле \"Шифр вида реализации (выбытия)\"!")
            }
        }
    }
    return true
}

/**
 * Проверка наличия и статуса консолидированной формы при осуществлении перевода формы в статус "Подготовлена"/"Принята".
 */
void checkOnPrepareOrAcceptance(def value) {
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = formDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error("$value первичной налоговой формы невозможно, т.к. уже подготовлена консолидированная налоговая форма.")
            }
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = data
    // удалить нефиксированные строки
    def deleteRows = []
    getRows(data).each { row ->
        if (!isFixedRow(row)) {
            deleteRows += row
        }
    }
    data.delete(deleteRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                // подразделы
                ['A', 'B', 'V', 'G', 'D', 'E'].each { section ->
                    copyRows(getData(source), data, section, 'total' + section)
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверки при переходе "Отменить принятие".
 */
void checkOnCancelAcceptance() {
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(),
            formData.getFormType().getId(), formData.getKind());
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData form = formDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias, def rowStart, def rowEnd) {
    def from = getIndex(rowStart) + 1
    def to = getIndex(rowEnd) - 1
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(data), new ColumnRange(columnAlias, from, to))
}

def isSection(def section, def row) {
    def data = data
    def sectionRow = data.getDataRow(getRows(data), section)
    def totalRow = data.getDataRow(getRows(data), 'total' + section)
    return getIndex(row) > getIndex(sectionRow) && getIndex(row) < getIndex(totalRow)
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    return row.getIndex() - 1
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(DataRow row, List<String> columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def errorBegin = getRowIndexString(row)
        def errorMsg = colNames.join(', ')
        logger.error(errorBegin+ "не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Начало предупреждений/ошибок
 * @param row
 * @return
 */
def getRowIndexString(def DataRow row){
    def index = row.rowNumber
    if (index != null) {
        return "В строке \"№ пп\" равной $index "
    } else {
        index = getIndex(row) + 1
        return "В строке $index "
    }

}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceData хелпер источника
 * @param destinationData хелпер приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def DataRowHelper sourceData, def DataRowHelper destinationData, def fromAlias, def toAlias) {
    def from = getIndexByAlias(sourceData, fromAlias) + 1
    def to = getIndexByAlias(sourceData, toAlias)
    if (from > to) {
        return
    }
    def copyRows = getRows(sourceData).subList(from, to)
    getRows(destinationData).addAll(getIndexByAlias(destinationData, toAlias), copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    getRows(destinationData).eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
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
 * Получить данные формы.
 *
 * @param formData форма
 */
def DataRowHelper getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

def DataRowHelper getData(){
    return getData(formData)
}

/**
 * Получить строки формы.
 *
 * @param formData форма
 */
def List<DataRow<Cell>> getRows(def DataRowHelper data) {
    return data.getAllCached()
}

def FormData getFormData46(){
    return formDataService.find(342, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

def FormData getFormData12(){
    return formDataService.find(364, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

def FormData getFormData45(){
    return formDataService.find(341, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * Если ничего не возвращает, то ручное заполнение
 * @param row49
 * @param row46
 * @param row45
 * @param data12
 * @return
 */
def getGraph8(DataRow row49, DataRow row46, DataRow row45, DataRowHelper data12) {
    // графа 8
    // Если «Графа 21» = 1, то
    // «Графа 8» =Значение «Графы 4» РНУ-46, где «Графа 2» = «Графа 6» РНУ-49
    // Если «Графа 21» = 2, то
    // «Графа 8» =Значение «Графы 7» РНУ-45, где «Графа 2» = «Графа 6» РНУ-49 иначе ручной ввод
    // - Подробности ручного ввода
    // Если «Графа 21» = 3 или 4, то
    // указывается цена приобретения такого имущества;
    // Если «Графа 21» = 5, то
    // указывается цена их приобретения, включая расходы, связанные с приобретением;
    // Если «Графа 21» = 6, то
    // «Графа 8» = «Графа 12» РНУ-12 - сумма, отнесённая на расходы по КНУ 21393, КНУ 21394 и КНУ 21395 до момента реализации прав
    // Если «Графа 21» = 7, то
    // указывается цена их приобретения, включая расходы, связанные с их приобретением;
    def saledPropertyCode = getSaledPropertyCode(row49.saledPropertyCode)
    if(row46!=null && saledPropertyCode == 1){
        return row46.cost
    }

    if(row45!=null && saledPropertyCode == 2){
        return row45.startCost
    }

    if(data12!=null && saledPropertyCode == 6){
        def knus = ['21393','21394','21395']
        def sum = 0
        for(def row12:getRows(data12)){
            if(getCodeAttribute(row12.code) in knus){
                sum+=row12.outcomeInBuh
            }
        }
        return sum
    }
}

def getGraph9(def DataRow row49, def DataRow row46, def DataRow row45){
    // графа 9
    // Если «Графа 21» = 1 , то
    // то указываются данные из граф 12 и 16 РНУ-46, где «Графа 2» = «Графа 6» РНУ-49
    // Если «Графа 21» = 2, то
    // указывается значение графы 11 РНУ-45, где «Графа 2» = «Графа 6» РНУ-49
    // Если «Графа 21» = 3, 5, 6, 7 то
    // указывается 0
    // Если «Графа 21» = 4, то заполняется вручную
    def prevSaledPropertyCode = row49.saledPropertyCode
    if(row46!=null && prevSaledPropertyCode == 1){
        return (row46.cost10perExploitation?:0) + (row46.amortExploitation?:0)
    }

    if(row45!=null && prevSaledPropertyCode == 2){
        return row45.cost10perTaxPeriod
    }

    if(prevSaledPropertyCode in [3,5,6,7]){
        return 0
    }
}

def getGraph18(def DataRow row49, def DataRow row46){
    // Если «Графа 17» > 0 и «Графа 21» = 1 и «Графа 22» = 1, то указывается значение графы 18 РНУ-46, где графа 6 РНУ-49 = графа 2 РНУ-46
    // Иначе, не заполняется
    if(row46!=null && row49.loss>0 && getSaledPropertyCode(row49.saledPropertyCode) == 1 && getSaleCode(row49.saleCode) == 1){
        return row46.usefullLifeEnd
    }
    return null
}

def getGraph19(def DataRow row49){
    // Если «Графа 17» > 0, то «Графа 19» = «Графа 18» – «Графа 3»
    // Если «Графа 18» = «Графа 3», то «Графа 19» = 1
    // Иначе не заполняется
    if (row49.loss > 0 && row49.usefullLifeEnd!=null && row49.operationDate!=null){
        tmp = row49.usefullLifeEnd[Calendar.MONTH] - row49.operationDate[Calendar.MONTH]
        return (tmp == 0) ? 1 : tmp
    }
    return null
}

def getGraph20(def DataRow row49){
    if (row49.monthsLoss != 0 && row49.monthsLoss!=null) {
        if (row49.sum > 0 && row49.loss!=null) {
            return roundTo(row49.loss / row49.monthsLoss, 2)
        }
    }
    return null
}

/**
 * Получить атрибут 130 - "Код налогового учёта" справочник 27 - "Классификатор расходов Сбербанка России для целей налогового учёта".
 *
 * @param id идентификатор записи справочника
 */
def getCodeAttribute(def id) {
    return refBookService.getStringValue(27, id, 'CODE')
}

/**
 * Получить атрибут 804 - "Шифр вида реализованного (выбывшего) имущества" справочник 82 - "Шифры видов реализованного (выбывшего) имущества".
 *
 * @param id идентификатор записи справочника
 */
def getSaledPropertyCode(def id) {
    return refBookService.getNumberValue(82, id, 'CODE')
}

/**
 * Получить атрибут 806 - "Шифр вида реализации (выбытия)" справочник 83 - "Шифры видов реализации (выбытия)".
 *
 * @param id идентификатор записи справочника
 */
def getSaleCode(def id) {
    return refBookService.getNumberValue(83, id, 'CODE')
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal roundTo(BigDecimal value, int round) {
    if (value != null) {
        return value.setScale(round, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

def DataRow getRow46(DataRow row49, DataRowHelper data46) {
    if(data46!=null && getSaledPropertyCode(row49.saledPropertyCode) == 1){
        for(def row46:getRows(data46)){
            if(row46.invNumber == row49.invNumber){
                return row46
            }
        }
    }
    return null
}

def DataRow getRow45(DataRow row49, DataRowHelper data45) {
    if(data45!=null && getSaledPropertyCode(row49.saledPropertyCode) == 2){
        for(def row45:getRows(data45)){
            if(row45.inventoryNumber == row49.invNumber){
                return row45
            }
        }
    }
    return null
}