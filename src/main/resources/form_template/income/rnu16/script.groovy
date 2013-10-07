package form_template.income.rnu16

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 *  Скрипт для РНУ-16
 *  Форма "(РНУ-16) Регистр налогового учёта доходов по поставочным сделкам с ПФИ, не признаваемыми ФИСС, в соответствии с учётной политикой для целей налогообложения ОАО «Сбербанк России»"
 *
 *  @author akadyrgulov
 **/

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

// графа - rowNumber
// графа - code
// графа - incomeType
// графа - sum

/**
 * Проверка при создании формы.
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
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = formData.createDataRow()

    // Графы 2-5 Заполняется вручную
    ['code', 'incomeType', 'sum'].each{ column ->
        newRow.getCell(column).setEditable(true)
        newRow.getCell(column).setStyleAlias('Редактируемая')
    }

    def i = getRows(data).size()
    while(i>0 && isTotalRow(getRows(data).get(i-1))){i--}
    data.insert(newRow, i + 1)

    // проставление номеров строк
    //TODO возможно убрать http://jira.aplana.com/browse/SBRFACCTAX-4270
    i = 1;
    getRows(data).each{ row->
        if (!isTotal(row)) {
            row.rowNumber = i++
        }
    }
    save(data)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    def data = getData(formData)
    data.delete(currentDataRow)
    // проставление номеров строк
    def i = 1;
    getRows(data).each{ row->
        if (!isTotal(row)) {
            row.rowNumber = i++
        }
    }
    save(data)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    /*
     * Проверка обязательных полей.
     */

    def data = getData(formData)

    for (def row : getRows(data)) {
        if (!isTotal(row)) {
            // список проверяемых столбцов (графа ..)
            def requiredColumns = ['code', 'incomeType', 'sum']
            if (!checkRequiredColumns(row, requiredColumns, true)) {
                return
            }
        }
    }

    /*
     * Расчеты.
     */

    // удалить строки "итого" и "итого по коду"
    def delRow = []
    getRows(data).each {
        if (isTotal(it)) {
            delRow += it
        }
    }
    delRow.each {
        deleteRow(data, it)
    }
    if (getRows(data).isEmpty()) {
        return
    }

    // отсортировать/группировать
    data.save(getRows(data).sort { it.code })

    // пронумеровать заново
    getRows(data).eachWithIndex { row, index ->
        row.rowNumber = index + 1
    }
    save(data)

    def total4 = 0
    getRows(data).each { row ->
        total4 += row.sum
    }

    // добавить строки "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    totalRow.sum = total4
    setTotalStyle(totalRow)
    insert(data, totalRow)
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck() {
    /*
     * Проверка обязательных полей.
     */

    def data = getData(formData)

    for (def row : getRows(data)) {
        if (!isTotal(row)) {
            // список проверяемых столбцов (графа ..)
            def requiredColumns = ['rowNumber', 'code', 'incomeType', 'sum']
            if (!checkRequiredColumns(row, requiredColumns, true)) {
                return false
            }
        }
    }

    // признак наличия итоговых строк
    def hasTotal = false
    def totalSum = 0
    for (def row : getRows(data)) {
        if (isTotal(row)) {
            hasTotal = true
            continue
        }
        totalSum += row.getCell('sum').getValue()
    }

    if (hasTotal) {
        def totalRow = getRowByAlias(data,'total')
        if (totalSum != totalRow.getCell('sum').getValue()) {
            logger.error("Неверно рассчитана графа \"Сумма дохода за отчетный период\"!")
            return false
        }
    }
        return true
}

/**
* Проверки соответствия НСИ.
*/

def checkNSI() {
    def data = getData(formData)
    if (!getRows(data).isEmpty()) {
        // справочник 28 - "Классификатор доходов ОАО «Сбербанк России» для целей налогового учёта"
        def incomeClassRefBookId = 28L
        for (def row : getRows(data)) {
            if (isTotal(row)) {
                continue
            }

            def index = getIndex(row) + 1

            // 1. Проверка графы «Код налогового учета»
            if (refBookService.getRecordData(incomeClassRefBookId, row.code) == null) {
                logger.error("В строке $index код налогового учёта в справочнике отсутствует!")
                return false
            }

            // 2. Проверка графы «Вид (наименование) дохода»
            if (refBookService.getRecordData(incomeClassRefBookId, row.incomeType) == null) {
                logger.error("В строке $index вид (наименование) дохода в справочнике отсутствует!")
                return false
            }

            if (row.code != row.incomeType) {
                logger.error("В строке $index поле \"Код налогового учёта\" не соответствует полю \"Вид (наименование) дохода\"!")
                return false
            }
        }
    }

    return true
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
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'fix', 'code', 'incomeType', 'sum'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
        row.getCell(it).setEditable(false)
    }
}

/**
 * Проверка является ли строка итоговой (любой итоговой, т.е. по коду, либо основной)
 */
def isTotalRow(row){
    row.getAlias()==~/total\d*/
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
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
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
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    return data.getDataRow(getRows(data), alias)
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
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    def data = getData(formData)
    getRows(data).indexOf(row)
}

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)

    // удалить все строки и собрать из источников их строки
    data.clear()
    // TODO не определен вид консолидации
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
    logger.info('Формирование консолидированной формы прошло успешно.')
}