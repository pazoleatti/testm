package form_template.income.rnu5

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState

/**
 * Форма "(РНУ-5) Простой регистр налогового учёта «расходы»".
 *
 * @version 59
 *
 * @author rtimerbaev
 */

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
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicalCheck(true) && checkNSI()
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
        getData(formData).commit()
        break
}

// графа 1 - rowNumber
// графа 2 - code
// графа 3 - number
// графа 4 - name
// графа 5 - sum

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = formData.createDataRow()

    // Графы 2-5 Заполняется вручную
    ['number', 'sum'].each{ column ->
        newRow.getCell(column).setEditable(true)
        newRow.getCell(column).setStyleAlias('Редактируемая')
    }
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

    // список проверяемых столбцов (графа 2..5)
    def requiredColumns = ['number', 'sum']
    def data = getData(formData)
    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
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

    getRows(data).eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 2
        row.code = row.number

        // графа 3
        row.name = row.number
    }

    // отсортировать/группировать
    data.save(getRows(data).sort { getCodeAttribute(it.code) })

    // cумма "Итого"
    def total = 0

    // нумерация (графа 1) и посчитать "итого"
    getRows(data).eachWithIndex { row, i ->
        row.rowNumber = i + 1
        total += row.sum
    }

    // посчитать "итого по коду"
    def totalRows = [:]
    def tmp = null
    def sum = 0
    getRows(data).eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.code
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != row.code) {
            totalRows.put(i, getNewTotalRow(tmp, sum))
            sum = 0
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == getRows(data).size() - 1) {
            sum += row.sum
            totalRows.put(i + 1, getNewTotalRow(row.code, sum))
            sum = 0
        }

        sum += row.sum
        tmp = row.code
    }
    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        data.insert(row, index + i + 1)
        i = i + 1
    }

    // добавить строку "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    totalRow.sum = total
    setTotalStyle(totalRow)

    insert(data, totalRow)
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    def data = getData(formData)

    // Проверока наличия итоговой строки
    if (!checkAlias(getRows(data), 'total')) {
        logger.error('Итоговые значения не рассчитаны')
        return false
    }

    def i = 1

    // список проверяемых столбцов (графа 1..5)
    requiredColumns = ['rowNumber', 'code', 'number', 'name', 'sum']

    def totalSum = 0
    def hasTotal = false
    def sums = [:]

    for (def row : getRows(data)) {
        if (isTotal(row)) {
            hasTotal = true
            continue
        }
        // 1. Обязательность заполнения полей (графа 1..5)
        if (!checkRequiredColumns(row, requiredColumns, useLog)) {
            return false
        }

        // 2. Проверка на уникальность поля «№ пп» (графа 1)
        for (def rowB : getRows(data)) {
            if(!row.equals(rowB) && row.rowNumber ==rowB.rowNumber){
                logger.error('Нарушена уникальность номера по порядку!')
                return false
            }
        }
        i += 1

        // 3. Проверка итогового значения по коду для графы 5
        sums[row.code] = (sums[row.code] != null ? sums[row.code] : 0) + row.sum

        totalSum += row.sum
    }

    if (hasTotal) {
        // 3. Проверка итогового значения по коду для графы 5
        def hindError = false
        sums.each { code, sum ->
            def totalRowAlias = 'total' + code
            if (!checkAlias(getRows(data), totalRowAlias)) {
                def codeAttribute = getCodeAttribute(code)
                logger.warn("Итоговые значения по коду $codeAttribute не рассчитаны! Необходимо расчитать данные формы.")
            } else {
                def row = getRowByAlias(data, totalRowAlias)
                if (row.sum != sum && !hindError) {
                    hindError = true
                    def codeAttribute = getCodeAttribute(code)
                    logger.error("Неверное итоговое значение по коду $codeAttribute графы «Сумма расходов за отчётный период (руб.)»!")
                }
            }
        }
        if (hindError) {
            return false
        }

        // 4. Проверка итогового значения по всем строкам для графы 5
        def totalRow = getRowByAlias(data,('total'))
        if (totalRow.sum != totalSum) {
            logger.error('Неверное итоговое значение графы «Сумма расходов за отчётный период (руб.)»!')
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
        // справочник 27 - «Классификатор расходов Сбербанка России для целей налогового учёта»
        def refBookId = 27
        for (def row : getRows(data)) {
            if (isTotal(row)) {
                continue
            }

            def index = row.rowNumber
            def errorMsg
            if (index != null) {
                errorMsg = "В строке \"№ пп\" равной $index "
            } else {
                index = getRows(data).indexOf(row) + 1
                errorMsg = "В строке $index "
            }

            // 4. Проверка соответствия графы 2, 3, 4 одной записи в справочнике
            if (row.code != row.number) {
                logger.warn(errorMsg + 'код налогового учета не соответствует номеру балансового счета')
            }
            if (row.code != row.name) {
                logger.warn(errorMsg + 'наименование балансового счета не соответствует номеру балансового счета')
            }

            // 1. Проверка графа «Код налогового учета» (графа 2)
            if (refBookService.getRecordData(refBookId, row.code) == null) {
                logger.warn(errorMsg + 'код налогового учёта в справочнике отсутствует!')
            }

            // 1. Проверка графы «Номер балансового счета» (графа 3)
            if (refBookService.getRecordData(refBookId, row.number) == null) {
                logger.error(errorMsg + 'номер балансового счета в справочнике отсутствует!')
                return false
            }

            // 3. Проверка графы «Наименование балансового счета» (графа 4)
            if (refBookService.getRecordData(refBookId, row.name) == null) {
                logger.warn(errorMsg + 'наименование балансового счета в справочнике отсутствует!')
                return false
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

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceData = getData(source)
                getRows(sourceData).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        def found = false
                        getRows(data).each{ rowB ->
                            // в случае совпадения строк из разных источников,
                            // необходимо использовать суммирование значений ячеек строк форм-источников для «графы 5»
                            if(row.code == rowB.code && row.number == rowB.number && row.name == rowB.name){
                                rowB.sum += row.sum
                                found = true
                            }
                        }
                        if (found) {
                            data.save(getRows(data))
                        } else {
                            insert(data, row)
                        }
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

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
 * Получить новую строку.
 */
def getNewTotalRow(def codeId, def sum) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + codeId)
    newRow.sum = sum
    def alias = getCodeAttribute(codeId)
    newRow.fix = 'Итого по КНУ ' + alias
    newRow.getCell('fix').colSpan = 2
    setTotalStyle(newRow)
    return newRow
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'fix', 'code', 'number', 'name', 'sum'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
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
        def index = row.rowNumber
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getRows(getData(formData)).indexOf(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
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
 * Получить атрибут 130 - "Код налогового учёта" справочник 27 - "Классификатор расходов Сбербанка России для целей налогового учёта".
 *
 * @param id идентификатор записи справочника
 */
def getCodeAttribute(def id) {
    return refBookService.getStringValue(27, id, 'CODE')
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