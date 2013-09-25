package form_template.income.rnu48_1

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * Скрипт для РНУ-48-1 (rnu48_1.groovy).
 * Форма "3.1	(РНУ-48.1) Регистр налогового учёта «ведомость ввода в эксплуатацию инвентаря и принадлежностей
 * до 40 000 руб.  ".
 *
 * Версия ЧТЗ: 64
 *
 * TODO:
 *          -   что за сквозная нумерация в рамках текущего отчетного года, как ее реализовывать?
 *
 * @author vsergeev
 *
 * Графы:
 * number           -   № пп
 * inventoryNumber  -   Инвентарный номер
 * usefulDate       -   Дата ввода в эксплуатацию
 * amount           -   Сумма, включаемая в состав материальных расходов
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        allCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        allCheck()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteCurrentRow()
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
        calc()
        if (allCheck()) {
            // для сохранения изменений приемников
            data.commit()
        }
        break
}

def allCheck() {
    return !hasError() && logicalCheck()
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

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Расчеты
 */
void calc() {
    def data = data
    def requiredColumns = getEditableColsAliases()
    // Обязательность заполнения полей граф (с 2 по 4)
    i = 1
    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
            return
        } else if (!isTotal(row)){
            row.number = i++
        }
    }

    //расчитываем новые итоговые значения
    def totalResults = getTotalResultsForCols()
    def totalRow = data.getDataRow(getRows(data),getTotalDataRowAlias())
    getTotalColsAliases().each { colName ->
        totalRow[colName] = totalResults[colName]
    }
    data.save(getRows(data))
}

/**
 * Общие проверки
 */
boolean logicalCheck(){
    def data = data
    def rows = getRows(data)
    def requiredColumns = getRequiredColsAliases()
    //  проверка, что в таблице есть хотя бы одна строка
    if (rows.isEmpty()) {
        logger.error ('В таблице отсутствуют заполненные строки!')
        return false
    }

    // 1.  Обязательность заполнения поля графы (с 1 по 4)
    for (def row : rows) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    def reportPeriodRange = getReportPeriodRange()

    //построчно проверяем данные формы
    for (def row : rows) {
        if ( ! isTotal(row)) {
            // 2.  Проверка даты ввода в эксплуатацию и границ отчетного периода
            // Дата начала отчётного периода ≤ графа 3 ≤ дата окончания отчётного периода
            if (! reportPeriodRange.containsWithinBounds(row.usefulDate)){
                def rowStart = getRowIndexString(row)
                logger.error("${rowStart}дата ввода в эксплуатацию вне границ отчетного периода!")
                return false
            }
        }
    }

    def totalDataRow = data.getDataRow(getRows(data),getTotalDataRowAlias())
    if (totalDataRow == null) {
        logger.error('Строка Итого не найдена')
        return false
    }
    def controlTotalResultsForCols = getTotalResultsForCols()
    for (def colName : getTotalColsAliases()) {
        if (totalDataRow[colName] != controlTotalResultsForCols[colName]) {
            logger.error('Строка Итого рассчитана неверно!')
            return false
        }
    }
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = data
    // удалить все строки и собрать из источников их строки
    data.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row, getRows(data).size()+1)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(DataRow row, Object columns) {
    def colNames = []

    columns.each { String col ->
        if (row.getCell(col).getValue() == null || ''.equals(row.getCell(col).getValue())) {
            def name = row.getCell(col).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def errorBegin = getRowIndexString(row)
        def errorMsg = colNames.join(', ')
        logger.error("${errorBegin}не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Начало предупреждений/ошибок
 * @param row
 * @return
 */
def String getRowIndexString(def DataRow row){
    def index = row.number
    if (index != null) {
        return "В строке \"№ пп\" равной $index "
    } else {
        index = getIndex(row) + 1
        return "В строке $index "
    }
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    getRows(data).indexOf(row)
}

def getTotalDataRowAlias() {
    return 'total'
}

/********************************   ОБЩИЕ ФУНКЦИИ   ********************************/

/**
 * возвращает список алиасов столбцов, доступных для редактирования
 */
def getEditableColsAliases() {
    return ['inventoryNumber', 'usefulDate', 'amount']
}

/**
 * возвращает список алиасов обязательных столбцов
 */
def getRequiredColsAliases() {
    return ['number', 'inventoryNumber', 'usefulDate', 'amount']
}

/***********   ОБЩИЕ ФУНКЦИИ ДЛЯ ИТОГОВЫХ СТРОК   ***********/

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && (row.getAlias() in totalRowsAliases)
}

/**
 * возвращает список алиасов для итоговых строк
 */
def getTotalRowsAliases() {
    return ['total']
}

/***********   ОБЩИЕ ФУНКЦИИ ДЛЯ СТОЛБЦОВ, ПО КОТОРЫМ ПОДВОДЯТСЯ ИТОГИ   ***********/

/**
 * возвращает список алиасов для стобцов, по которым подводятся итоги
 */
def getTotalColsAliases() {
    return ['amount']
}

/**
 * находим для всех строк, кроме итоговых, суммы по столбцам, по которым подводят итоги
 * возвращаем мапу вида алиас_столбца -> итоговое_значение
 */
def getTotalResultsForCols() {
    def data = data
    def rows = getRows(data)
    def result = [:]
    for (def colAlias : getTotalColsAliases()) {
        result.put(colAlias, rows.sum {row ->
            if (! isTotal(row)) {    //строка не входит в итоговые
                row[colAlias]
            } else {
                0
            }
        })
    }
    return result
}

/***********   ДОБАВЛЕНИЕ СТРОКИ В ТАБЛИЦУ С ФИКСИРОВАННЫМИ СТРОКАМИ ИТОГОВ   ***********/
def addNewRow() {
    def data = data
    def rows = getRows(data)
    def newRow = formData.createDataRow()

    getEditableColsAliases().each{ value ->
        newRow.getCell(value).editable = true
        newRow.getCell(value).setStyleAlias('Редактируемая')
    }
    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(row.getAlias()!=null && index>0){
            row = rows.get(--index)
        }
        if(index!=currentDataRow.getIndex() && rows.get(index).getAlias()==null){
            index++
        }
    }else if (rows.size()>0) {
        for(int i = rows.size()-1;i>=0;i--){
            def row = rows.get(i)
            if(row.getAlias()==null){
                index = rows.indexOf(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
}

/**
 * Удалить строку.
 */
def deleteCurrentRow() {
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        data.delete(currentDataRow)
    }
}

/***********   ДЛЯ РАБОТЫ С ОТЧЕТНЫМИ ПЕРИОДАМИ   ***********/

/**
 * возвращает диапазон Date..Date (включительно!) для текущего отчетного периода (с нарастающим итогом)
 */
def getReportPeriodRange() {
    def periodStartsDate = reportPeriodService.getStartDate(formData.reportPeriodId).getTime()
    def periodEndsDate = reportPeriodService.getEndDate(formData.reportPeriodId).getTime()

    return periodStartsDate..periodEndsDate
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

