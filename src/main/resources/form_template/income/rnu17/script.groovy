package form_template.income.rnu17

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 *  Скрипт для РНУ-17
 *  Форма "(РНУ-17) Регистр налогового учёта расходов по поставочным сделкам с ПФИ, не признаваемыми ФИСС, в соответствии с учётной политикой для целей налогообложения ОАО «Сбербанк России»"
 *
 *  @author bkinzyabulatov
 **/

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck(true)
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow(currentDataRow)
        break
    // проверка при "подготовить"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :
        break
    // проверка при "принять"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED :
        break
    // проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED :
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        checkNSI()
        data.commit()
        break
}

/**
 * Графа 1  rowNumber   № пп
 *          fix
 * Графа 2  knu         Код налогового учета                Справочник 27, атрибут 130
 * Графа 3  incomeType  Вид (наименование) дохода           Справочник 28, атрибут 142
 * Графа 4  sum         Сумма дохода за отчетный период
 */

void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

def logicalCheck(def useLog) {
    def data = data
    def requiredColumns = ['rowNumber','knu','incomeType','sum']
    def hasTotal = false
    def totalSum = 0
    for (def DataRow row : getRows(data)){
        if (!isTotal(row)){
            //проверяем обязательные поля
            if (!checkRequiredColumns(row, requiredColumns, useLog)){
                return false
            }
            //сразу считаем сумму для проверки итогов
            totalSum+=row.sum
        } else {
            hasTotal = true
        }
    }
    if (hasTotal){
        def totalRow = data.getDataRow(getRows(data),'total')
        if (totalSum != totalRow.sum){
            logger.error("Неверно рассчитана графа \"Сумма дохода за отчетный период\"!")
            return false
        }
    }
    return true
}

def checkNSI() {
    def data = data
    getRows(data).each{ row ->
        if (!isTotal(row)) {
            if (row.knu!=null && getKnu(row.knu)==null){
                logger.error("Неверно заполнена графа \"Код налогового учета\"")
                return false
            }
            if (row.incomeType!=null && getIncomeType(row.incomeType)==null){
                logger.error("Неверно заполнена графа \"Вид (наименование) дохода\"")
                return false
            }
        }
    }
}

void calc() {
    def data = data
    def requiredColumns = ['knu','incomeType','sum']
    for (def DataRow row : getRows(data)){
        if (!isTotal(row)){
            //проверяем обязательные поля
            if (!checkRequiredColumns(row, requiredColumns, true)){
                return
            }
        }
    }
    // удалить строку "итого"
    def delRow = []
    getRows(data).each {
        if (isTotal(it)) {
            delRow.add(it)
        }
    }
    delRow.each {
        deleteRow(it)
    }
    if (getRows(data).isEmpty()) {
        return
    }
    getRows(data).sort { it.knu }
    data.save(getRows(data))
    recalculateNumbers()
    def totalSum = 0
    getRows(data).each { row->
        totalSum += row.sum?:0
    }

    // добавить строки "итого"
    def DataRow totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    totalRow.sum = totalSum
    setTotalStyle(totalRow)
    data.insert(totalRow,getRows(data).size()+1)
}

void addNewRow() {
    def data = data
    DataRow<Cell> newRow = getNewRow()
    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(isTotal(row) && index>0){
            row = getRows(data).get(--index)
        }
        if(index!=currentDataRow.getIndex() && !isTotal(getRows(data).get(index))){
            index++
        }
    }else if (getRows(data).size()>0) {
        for(int i = getRows(data).size()-1;i>=0;i--){
            def row = getRows(data).get(i)
            if(!isTotal(row)){
                index = getRows(data).indexOf(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
    recalculateNumbers()
}

/**
 * Получить новую стролу с заданными стилями.
 */
def DataRow getNewRow() {
    def row = formData.createDataRow()
    ['knu','incomeType','sum'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

void deleteRow(def row) {
    data.delete(row)
    recalculateNumbers()
}

void recalculateNumbers(){
    def data = data
    // проставление номеров строк
    def i = 1;
    getRows(data).each{ row->
        if (!isTotal(row)) {
            row.rowNumber = i++
        }
    }
    data.save(getRows(data))
}

void consolidation() {
    def data = data
    // удалить все строки и собрать из источников их строки
    getRows(data).clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceData = getData(source)
                getRows(sourceData).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row,getRows(data).size()+1)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Получить helper формы.
 */
def DataRowHelper getData() {
    return getData(formData)
}

def DataRowHelper getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Получить строки формы.
 */
def List<DataRow<Cell>> getRows(def DataRowHelper data) {
    return data.getAllCached();
}

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def DataRow row, ArrayList<String> columns, def useLog) {
    def colNames = []
    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
        def index = getRows(data).indexOf(row) + 1
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        } else {
            index = getRows(data).indexOf(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

void setTotalStyle(def row) {
    ['rowNumber', 'fix', 'knu', 'incomeType', 'sum'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
        row.getCell(it).setEditable(false)
    }
}

/**
 * Получить атрибут 130 - "Код налогового учёта" справочник 27 - "Классификатор расходов Сбербанка России для целей налогового учёта".
 *
 * @param id идентификатор записи справочника
 */
def getKnu(def id) {
    return refBookService.getStringValue(27, id, 'CODE')
}

/**
 * Получить атрибут 142 - "Вид дохода по операциям" справочник 28 - "Классификатор доходов Сбербанка России для целей налогового учёта".
 *
 * @param id идентификатор записи справочника
 */
def getIncomeType(def id) {
    return refBookService.getStringValue(28, id, 'TYPE_INCOME')
}


