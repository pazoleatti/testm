package form_template.income.rnu22

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * Скрипт для РНУ-22.
 * Форма "(РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования".
 *
 * @version 59
 *
 * - нет условии в проверках соответствия НСИ (потому что действительно нет справочников)
 * TODO:
 *	- графа 19 опущена в регламенте
 *	- консолидация http://jira.aplana.com/browse/SBRFACCTAX-4455
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
        calc()
        allCheck()
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
        calc()
        if (allCheck()) {
            // для сохранения изменений приемников
            data.commit()
        }
        break
}

// графа 1  - rowNumber
// графа 2  - contractNumber
// графа 3  - contractData
// графа 4  - base
// графа 5  - transactionDate
// графа 6  - course
// графа 7  - interestRate
// графа 8  - basisForCalc
// графа 9  - calcPeriodAccountingBeginDate
// графа 10 - calcPeriodAccountingEndDate
// графа 11 - calcPeriodBeginDate
// графа 12 - calcPeriodEndDate
// графа 13 - accruedCommisCurrency
// графа 14 - accruedCommisRub
// графа 15 - commisInAccountingCurrency
// графа 16 - commisInAccountingRub
// графа 17 - accrualPrevCurrency
// графа 18 - accrualPrevRub
// графа 19 - reportPeriodCurrency
// графа 20 - reportPeriodRub

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
 * Вставка строки в случае если форма генерирует динамически строки итого (на основе данных введённых пользователем)
 */
def addNewRow() {
    def data = data
    DataRow<Cell> newRow = getNewRow()
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
 * Добавить новую строку.
 */
def DataRow getNewRow() {
    def DataRow row = formData.createDataRow()

    // графа 2..12
    ['contractNumber', 'contractData', 'base', 'transactionDate', 'course',
            'interestRate', 'basisForCalc', 'calcPeriodAccountingBeginDate',
            'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isTotal(currentDataRow)) {
        data.delete(currentDataRow)
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {

    def data = data
    // РНУ-22 за предыдущий отчетный период
    def formDataOld = getFormDataOld()

    /*
     * Проверка обязательных полей.
     */

    // список проверяемых столбцов (графа 2..8)
    def requiredColumns = ['contractNumber', 'contractData', 'base',
            'transactionDate', 'course', 'interestRate', 'basisForCalc']
    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
            return
        }
    }

    /*
     * Расчеты.
     */

    // удалить строку "итого"
    def total = null
    for (def row : getRows(data)) {
        if (row.getAlias() == 'total') {
            total = row
            break
        }
    }
    if (total != null) {
        data.delete(total)
    }
    if (getRows(data).isEmpty()) {
        return
    }

    // графа 1, 13..20
    getRows(data).eachWithIndex { DataRow row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 13, 15
        def temp
        if (row.calcPeriodAccountingBeginDate!=null && row.calcPeriodAccountingEndDate!=null) {
            temp = getColumn13or15or19(row, row.calcPeriodAccountingBeginDate, row.calcPeriodAccountingEndDate)
        } else {
            temp = getColumn13or15or19(row, row.calcPeriodBeginDate, row.calcPeriodEndDate)
        }
        row.accruedCommisCurrency = temp
        row.commisInAccountingCurrency = temp

        // графа 14
        row.accruedCommisRub = roundTo2(row.accruedCommisCurrency * row.course)

        // графа 16
        if (row.commisInAccountingCurrency!=null) {
            row.commisInAccountingRub = roundTo2(row.commisInAccountingCurrency * row.course)
        }

        if (formDataOld!=null) {
            for(def rowOld in getRows(getData(formDataOld))){
                if (rowOld.contractNumber==row.contractNumber){
                    // графа 17
                    row.accrualPrevCurrency = rowOld.reportPeriodCurrency
                    // графа 18
                    row.accrualPrevRub = rowOld.reportPeriodRub
                    break
                }
            }
        }

        // графа 19
        //TODO (Bulat Kinzyabulatov|Sariya Mustafina) описание опущено в регламенте
        row.reportPeriodCurrency = getColumn13or15or19(row, row.calcPeriodBeginDate, row.calcPeriodEndDate)

        // графа 20
        if (row.reportPeriodCurrency!=null) {
            row.reportPeriodRub = roundTo2(row.reportPeriodCurrency * row.course)
        }
    }
    data.save(getRows(data))

    // добавить строку "итого"
    def DataRow totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.contractNumber = 'Итого'
    setTotalStyle(totalRow)

    // графы для которых надо вычислять итого (графа 13..20)
    def totalColumns = ['accruedCommisCurrency', 'accruedCommisRub',
            'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency',
            'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub']
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(formData, alias))
    }
    data.insert(totalRow,getRows(data).size()+1)
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = data
    // РНУ-22 за предыдущий отчетный период
    def formDataOld = getFormDataOld()

    def tmp

    /** Дата начала отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def a = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def b = (tmp ? tmp.getTime() : null)

    if (!getRows(data).isEmpty()) {
        def i = 1

        // список проверяемых столбцов (графа 1..8, 13, 14)
        def requiredColumns = ['rowNumber', 'contractNumber', 'contractData', 'base',
                'transactionDate', 'course', 'interestRate', 'basisForCalc',
                'accruedCommisCurrency', 'accruedCommisRub']

        // суммы строки общих итогов
        def totalSums = [:]

        // столбцы для которых надо вычислять итого и итого по коду классификации дохода (графа 13..20)
        def totalColumns = ['accruedCommisCurrency', 'accruedCommisRub',
                'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency',
                'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub']

        // признак наличия итоговых строк
        def hasTotal = false
        def numberList = []

        for (def row : getRows(data)) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }
            def errorBegin = getRowIndexString(row)

            // 7. Обязательность заполнения поля графы 1..8, 13..20
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }

            // 1. Проверка даты совершения операции и границ отчётного периода (графа 5, 10, 12)
            if (!(a != null && b != null && ((row.transactionDate != null && row.transactionDate <= a) ||
                    (row.calcPeriodAccountingEndDate != null && row.calcPeriodAccountingEndDate <= b) ||
                    (row.calcPeriodEndDate != null && row.calcPeriodEndDate <= b)))) {
                logger.error(errorBegin +'дата совершения операции вне границ отчётного периода!')
                return false
            }

            // 2. Проверка на нулевые значения (графа 13..20)
            def allNull = true
            ['accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency',
                    'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
                    'reportPeriodCurrency', 'reportPeriodRub'].each { alias ->
                tmp = row.getCell(alias).getValue()
                if (tmp != null && tmp != 0) {
                    allNull = false
                }
            }
            if (allNull) {
                logger.error(errorBegin + 'все суммы по операции нулевые!')
                return false
            }

            // 3. Проверка на сумму платы (графа 4)
            if (row.base != null && !(row.base > 0)) {
                logger.warn(errorBegin +'суммы платы равны 0!')
            }

            // 4. Проверка задания расчётного периода
            if (row.calcPeriodAccountingBeginDate > row.calcPeriodAccountingEndDate &&
                    row.calcPeriodBeginDate > row.calcPeriodEndDate) {
                logger.warn(errorBegin +'неправильно задан расчётный период!')
            }

            // 5. Проверка на корректность даты договора
            if (row.contractData > b) {
                logger.error(errorBegin +'дата договора неверная!')
                return false
            }

            // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода (графа 14, 16)
            if (row.accruedCommisRub < row.commisInAccountingRub) {
                logger.warn(errorBegin + "сумма данных бухгалтерского учёта превышает сумму начисленных платежей для документа ${row.contractNumber}")
            }

            // 8. Проверка на заполнение поля «<Наименование поля>»
            // При заполнении граф 9 и 10, графы 11 и 12 должны быть пустыми.
            def checkColumn9and10 = row.calcPeriodAccountingBeginDate != null && row.calcPeriodAccountingEndDate != null &&
                    (row.calcPeriodBeginDate != null || row.calcPeriodEndDate != null)
            // При заполнении граф 11 и 12, графы 9 и 10 должны быть пустыми.
            def checkColumn11and12 = (row.calcPeriodAccountingBeginDate != null || row.calcPeriodAccountingEndDate != null) &&
                    row.calcPeriodBeginDate != null && row.calcPeriodEndDate != null
            if (checkColumn9and10 || checkColumn11and12) {
                logger.error(errorBegin + 'поля в графах 9, 10, 11, 12 заполены неверно!')
                return false
            }

            // 9. Проверка на уникальность поля «№ пп» (графа 1)
            if (numberList.contains(row.rowNumber)) {
                logger.error(errorBegin + "нарушена уникальность номера по порядку ${row.rowNumber}!")
                return false
            }else{
                numberList.add(row.rowNumber)
            }

            // Арифметическая проверка графы 13
            if (row.calcPeriodAccountingBeginDate!=null && row.calcPeriodAccountingEndDate!=null) {
                tmp = getColumn13or15or19(row, row.calcPeriodAccountingBeginDate, row.calcPeriodAccountingEndDate)
            } else {
                tmp = getColumn13or15or19(row, row.calcPeriodBeginDate, row.calcPeriodEndDate)
            }
            if (row.accruedCommisCurrency != tmp) {
                logger.warn(errorBegin + 'неверно рассчитана графа «Сумма начисленной комиссии. Валюта»!')
            }

            // Арифметическая проверка графы 15
            if (row.commisInAccountingCurrency != tmp) {
                logger.warn(errorBegin + 'неверно рассчитана графа «Сумма комиссии, отражённая в бухгалтерском учёте. Валюта»!')
            }

            // Арифметическая проверка графы 14
            if (row.commisInAccountingCurrency!=null && (row.accruedCommisRub != roundTo2(row.accruedCommisCurrency * row.course))) {
                logger.warn(errorBegin + 'неверно рассчитана графа «Сумма начисленной комиссии. Рубли»!')
            }

            // Арифметическая проверка графы 16
            if (row.commisInAccountingRub != roundTo2(row.commisInAccountingCurrency * row.course)) {
                logger.warn(errorBegin + 'неверно рассчитана графа «Сумма комиссии, отражённая в бухгалтерском учёте. Рубли»!')
            }

            if (formDataOld!=null) {
            // Арифметическая проверка графы 17
                // Арифметическая проверка графы 18
                for(def rowOld in getRows(getData(formDataOld))){
                    if (rowOld.contractNumber==row.contractNumber){
                        // графа 17
                        if (row.accrualPrevCurrency != rowOld.reportPeriodCurrency){
                            logger.warn(errorBegin + 'неверно рассчитана графа «Сумма доначисления. Предыдущий период. Валюта»!')
                        }
                        // графа 18
                        if(row.accrualPrevRub != rowOld.reportPeriodRub){
                            logger.warn(errorBegin + 'неверно рассчитана графа «Сумма доначисления. Предыдущий период. Рубли»!')
                        }
                        break
                    }
                }
            }

            // Арифметическая проверка графы 19
            tmp = getColumn13or15or19(row, row.calcPeriodBeginDate, row.calcPeriodEndDate)
            if (row.reportPeriodCurrency != tmp) {
                logger.warn(errorBegin + 'неверно рассчитана графа «Сумма доначисления. Отчётный период. Валюта»!')
            }

            // Арифметическая проверка графы 20
            if (row.reportPeriodCurrency!=null && row.reportPeriodRub != roundTo2(row.reportPeriodCurrency * row.course)) {
                logger.warn(errorBegin + 'неверно рассчитана графа «Сумма доначисления. Отчётный период. Рубли»!')
            }

            // Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue()?:0)
            }
        }

        if (hasTotal) {
            def totalRow = data.getDataRow(getRows(data),'total')

            // Проверка итогового значений по всей форме (графа 13..20)
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    logger.error("Неверное итоговое значение графы ${getColumnName(totalRow,alias)}!")
                    return false
                }
            }
        }
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
                        data.insert(row,getRows(data).size()+1)
                    }
                }
            }
        }
    }
    //TODO http://jira.aplana.com/browse/SBRFACCTAX-4455
    def ignoredRows = []
    for(def row : getRows(data)){
        if (!ignoredRows.contains(row)) {
            for(def rowB : getRows(data)){
                if(row!=rowB && isEqualRows(row,rowB) && !ignoredRows.contains(rowB)){
                    addRowToRow(row,rowB)
                    ignoredRows.add(rowB)
                }
            }
        }
    }
    data.save(getRows(data))
    ignoredRows.each{
        data.delete(it)
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Определение идентичности строк(графа 2,3,5)
 * @param rowA
 * @param rowB
 * @return
 */
def boolean isEqualRows(def DataRow rowA, def DataRow rowB){
    return rowA.contractNumber==rowB.contractNumber &&
            rowA.contractData==rowB.contractData &&
            rowA.transactionDate==rowB.transactionDate
}

/**
 * Увеличиваем значения строки А на значения строки B
 * @param rowA
 * @param rowB
 */
void addRowToRow(def DataRow rowA, def DataRow rowB){
    def columns = ['accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency', 'commisInAccountingRub',
            'accrualPrevCurrency', 'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub']
    //суммируем графы 13-20
    columns.each{col->
        addGraph(rowA, rowB, col)
    }
}

def addGraph(def DataRow rowA, def DataRow rowB, def String graph){
    def a = rowA.getCell(graph).getValue()
    def b = rowB.getCell(graph).getValue()
    rowA.getCell(graph).setValue((a!=null) ? (a + (b ?: 0)) : ((b ?: 0)))
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
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить значение графа 13 (аналогично для графа 15 и графа 19)
 *
 * @param row строка нф
 * @param date1 дата начала
 * @param date2 дата окончания
 */
def getColumn13or15or19(def DataRow row, def Date date1, def Date date2) {
    if (date1 == null || date2 == null) {
        return 0
    }
    def division = row.basisForCalc * (date2 - date1 + 1)
    if (division == 0) {
        logger.error('Деление на ноль. Возможно неправильно выбраны даты.')
        return 0
    }
    return roundTo2((row.base * row.interestRate) / (division))
}

/**
 * Получить сумму столбца.
 */
def getSum(def form, def columnAlias) {
    if (form == null) {
        return 0
    }
    def data = getData(form)
    def to = 0
    def from = getRows(data).size() - 1
    if (to > from) {
        return 0
    }
    return summ(form, getRows(data), new ColumnRange(columnAlias, to, from))
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'contractNumber', 'contractData', 'base', 'transactionDate',
            'course', 'interestRate', 'basisForCalc', 'calcPeriodAccountingBeginDate', 'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate',
            'accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency',
            'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
            'reportPeriodCurrency', 'reportPeriodRub'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получить данные за предыдущий отчетный период
 */
def FormData getFormDataOld() {
    // предыдущий отчётный период
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // (РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования (За предыдущий отчетный период)
    def FormData formDataOld = (prevReportPeriod != null ?
        formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, prevReportPeriod.id) : null)

    return formDataOld
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    getRows(data).indexOf(row)
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def boolean checkRequiredColumns(def DataRow row, def List columns) {
    def colNames = []

    columns.each {def String col ->
        if (row.getCell(col).getValue() == null || ''.equals(row.getCell(col).getValue())) {
            def name = getColumnName(row,col)
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
 * @param data форма
 */
def List<DataRow<Cell>> getRows(def DataRowHelper data) {
    return data.getAllCached()
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
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def DataRow row, def alias) {
    if (row != null && alias != null) {
        def column = row.getCell(alias).getColumn()
        def name = column.getGroupName() +' '+ column.getName()
        return name.trim().replace('%', '%%')
    }
    return ''
}
