package form_template.income.rnu50

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * Форма "(РНУ-50) Регистр налогового учёта «ведомость понесённых убытков от реализации амортизируемого имущества»"
 * formTemplateId=365
 *
 * @version 59
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
        calc()
        allCheck()
        break
    case FormDataEvent.ADD_ROW :
        break
    case FormDataEvent.DELETE_ROW :
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        allCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        postCalc(data)
        if (allCheck()) {
            // для сохранения изменений приемников
            data.commit()
        }
        break
}

// графа 1  - rowNumber
// графа 2  - rnu49rowNumber
// графа 3  - invNumber
// графа 4  - lossReportPeriod
// графа 5  - lossTaxPeriod

def allCheck() {
    return !hasError() && logicalCheck()
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = data
    def newRow = formData.createDataRow()
    // графа 2..5
    ['rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
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
            if(row.getAlias()==null){
                index = getRows(data).indexOf(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        data.delete(currentDataRow)
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def data = data

    /*
     * Расчеты
     */
    // удалить все строки
    data.clear()

    def formData49 = formData49
    if (formData49 == null){
        return
    }
    def DataRowHelper data49 = getData(formData49)

    for (def row49 in getRows(data49)){
        if (!isTotal(row49) && row49.usefullLifeEnd!=null &&
            row49.monthsLoss!=null &&
            row49.expensesSum!=null){
            def DataRow newRow = formData.createDataRow()
            // 3 графа
            newRow.invNumber = row49.invNumber
            // 2 графа
            newRow.rnu49rowNumber = row49.firstRecordNumber
            def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).getTime()
            def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).getTime()
            def date = row49.operationDate
            if (date>=startDate && date<=endDate) {
                // 4 графа
                if(row49.usefullLifeEnd > row49.operationDate){
                    newRow.lossReportPeriod = row49.expensesSum * (endDate[Calendar.MONTH] - date[Calendar.MONTH])
                }else{
                    newRow.lossReportPeriod = row49.expensesSum
                }
            }
            date = row49.usefullLifeEnd// 18 графа РНУ-49
            // 5 графа
            if (date < startDate){
                newRow.lossTaxPeriod = row49.expensesSum * 3
            } else if (date>=startDate && date<=endDate){
                newRow.lossTaxPeriod = row49.expensesSum * (endDate[Calendar.MONTH] - row49.usefullLifeEnd[Calendar.MONTH])
            }
            data.insert(newRow, getRows(data).size()+1)
        }
    }

    postCalc(data)
}

/**
 * Сортировка, нумерование, итоги
 * @param data
 */
void postCalc(def data){
    if (getRows(data).isEmpty()) {
        return
    }

    def sortColumns = ['rnu49rowNumber', 'invNumber']
    getRows(data).sort({ DataRow a, DataRow b ->
        sortRow(sortColumns, a, b)
    })

    getRows(data).eachWithIndex { row, i ->
        // графа 1
        row.rowNumber = i + 1
    }
    data.save(getRows(data))

    def DataRow totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.rnu49rowNumber = 'Итого'
    totalRow.lossReportPeriod = getSum('lossReportPeriod')
    totalRow.lossTaxPeriod = getSum('lossTaxPeriod')
    setTotalStyle(totalRow)
    data.insert(totalRow,getRows(data).size()+1)
}

int sortRow(List<String> params, DataRow a, DataRow b) {
    for (String param : params) {
        def aD = a.getCell(param).value
        def bD = b.getCell(param).value

        if (aD != bD) {
            return aD <=> bD
        }
    }
    return 0
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    // 4. Проверки существования необходимых экземпляров форм
    if (formData49 == null){
        logger.error('Отсутствуют данные РНУ-49!')
        return false
    }

    def data = data
    def rows = getRows(data)
    if (!rows.isEmpty()) {
        // список проверяемых столбцов (графа 1..5)
        def requiredColumns = ['rowNumber', 'rnu49rowNumber', 'invNumber',
                'lossReportPeriod', 'lossTaxPeriod']

        def hasTotalRow = false

        // суммы строки общих итогов
        def totalSums = [:]
        def totalSumColumns = ['lossReportPeriod', 'lossTaxPeriod']
        for (def row : rows) {
            if (isTotal(row)) {
                hasTotalRow = true
                continue
            }
            def rowStart = getRowIndexString(row)
            // 1. Обязательность заполнения полей (графа 1..5)
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }

            // 2. Проверка на нулевые значения
            if (isEmpty(row.lossReportPeriod) && isEmpty(row.lossTaxPeriod)) {
                logger.error(rowStart + 'все суммы по операции нулевые!')
                return false
            }

            // 3. Проверка формата номера записи в РНУ-49 (графа 2)
            if (!row.rnu49rowNumber.matches('\\w{2}-\\w{6}')) {
                logger.error(rowStart + 'неправильно указан номер записи в РНУ-49 (формат: ГГ-НННННН, см. №852-р в актуальной редакции)!')
                return false
            }

            // 5. Проверка итогового значених по всей форме - подсчет сумм для общих итогов
            totalSumColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += row.getCell(alias).getValue()
            }
        }


        // 5. Проверка итоговых значений формы	Заполняется автоматически.
        if (hasTotalRow) {
            def totalRow = data.getDataRow(rows, 'total')
            for (def alias : totalSumColumns) {
                if (totalRow.getCell(alias).getValue() != totalSums[alias]) {
                    logger.error('Итоговые значения формы рассчитаны неверно!')
                    return false
                }
            }
        } else {
            logger.error('Итоговая строка не рассчитана!')
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
 * Принять.
 */
void acceptance() {
    if (!logicalCheck()) {
        return
    }
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() {
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
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
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def data = data
    def from = 0
    def to = getRows(data).size()-1
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(data), new ColumnRange(columnAlias, from, to))
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    getRows(data).indexOf(row)
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
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
def String getRowIndexString(def DataRow row){
    def index = row.rowNumber
    if (index != null) {
        return "В строке \"№ пп\" равной $index "
    } else {
        index = getIndex(row) + 1
        return "В строке $index "
    }

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

def FormData getFormData49(){
    return formDataService.find(312, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

