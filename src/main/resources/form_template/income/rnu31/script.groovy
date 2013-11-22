package form_template.income.rnu31

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import groovy.transform.Field

/**
 * Форма "(РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям".
 * formTemplateId=328
 *
 * @version 59
 *
 * @author rtimerbaev
 */

// Признак периода ввода остатков
@Field
def boolean isBalancePeriod = null

def getBalancePeriod(){
    if (isBalancePeriod == null){
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW :
        // Всего форма должна содержать одну строку
        break
    case FormDataEvent.DELETE_ROW :
        // Всего форма должна содержать одну строку
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicalCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        !hasError() && logicalCheck()
        break
    case FormDataEvent.IMPORT :
    case FormDataEvent.MIGRATION :
        importData()
        break
}

// графа 1  - number
// графа 2  - securitiesType
// графа 3  - ofz
// графа 4  - municipalBonds
// графа 5  - governmentBonds
// графа 6  - mortgageBonds
// графа 7  - municipalBondsBefore
// графа 8  - rtgageBondsBefore
// графа 9  - ovgvz
// графа 10 - eurobondsRF
// графа 11 - itherEurobonds
// графа 12 - corporateBonds

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    // данные предыдущего отчета
    def formDataOld = getFormDataOld()
    def data = getData(formData)
    def dataOld = getData(formDataOld)

    /** Строка из предыдущего отчета. */
    def rowOld = getTotalRow(dataOld)

    /** Строка из текущего отчета. */
    def row = getTotalRow(data)
    if (row == null) {
        return true
    }

    // список проверяемых столбцов (графа 1..12)
    def requiredColumns = ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds',
            'municipalBondsBefore', 'rtgageBondsBefore', 'ovgvz', 'eurobondsRF',
            'itherEurobonds', 'corporateBonds']

    // 22. Обязательность заполнения полей графы 1..12
    if (!checkRequiredColumns(row, requiredColumns) && !getBalancePeriod()) {
        return false
    }

    // графы для которых тип ошибки нефатальный (графа 5, 9, 10, 11)
    def warnColumns = ['governmentBonds', 'ovgvz', 'eurobondsRF', 'itherEurobonds']

    // TODO (Ramil Timerbaev) протестировать проверку "начиная с отчета за февраль"
    if (!isFirstMonth()) {
        // 1. Проверка наличия предыдущего экземпляра отчета
        if (rowOld == null && !getBalancePeriod()) {
            logger.error('Форма предыдущего периода не существует или не находится в статусе «Принята»')
            return false
        }

        // 2..11 Проверка процентного (купонного) дохода по видам валютных ценных бумаг (графы 3..12)
        for (def column : requiredColumns) {
            if (row.getCell(column).getValue() < rowOld.getCell(column).getValue()) {
                def securitiesType = row.securitiesType
                def message = "Процентный (купонный) доход по $securitiesType уменьшился!"
                if (column in warnColumns) {
                    logger.warn(message)
                } else {
                    loggerError(message)
                }
                if (!getBalancePeriod()) {
                    return false
                }
            }
        }
    }

    // 12..21. Проверка на неотрицательные значения (графы 3..12)
    for (def column : requiredColumns) {
        def value = row.getCell(column).getValue()
        if (value != null && value < 0) {
            def columnName = getColumnName(row, column)
            def message = "Значения графы \"$columnName\" по строке 1 отрицательное!"
            if (column in warnColumns) {
                logger.warn(message)
            } else {
                loggerError(message)
            }
            if (!getBalancePeriod()) {
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
    // занулить данные и просуммировать из источников

    def row = data.getDataRow(getRows(data),'total')

    // графа 3..12
    def columns = ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds',
            'municipalBondsBefore', 'rtgageBondsBefore', 'ovgvz',
            'eurobondsRF', 'itherEurobonds', 'corporateBonds']
    columns.each { alias ->
        row.getCell(alias).setValue(0)
    }

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            def sourceRow
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceRow = getData(source).getDataRow(getRows(getData(source)),'total')
                columns.each { alias ->
                    row.getCell(alias).setValue(sourceRow.getCell(alias).getValue())
                }
            }
        }
    }
    data.save(getRows(data))
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
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.toString())
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
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-31 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * Первый ли это месяц (январь)
 */
def isFirstMonth() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    if (reportPeriod != null && reportPeriod.getOrder() == 1) {
        return true
    }
    return false
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    def data = getData(formData)
    getRows(data).indexOf(row)
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param useLog нужно ли записывать сообщения в лог
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
        def index = row.number
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
            errorMsg = "В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg."
        } else {
            index = getIndex(row) + 1
            errorMsg = "В строке $index не заполнены колонки : $errorMsg."
        }
        loggerError(errorMsg)
        return false
    }
    return true
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
def getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Получить строки формы.
 *
 * @param formData форма
 */
def getRows(def data) {
    def cached = data.getAllCached()
    return cached
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
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def row = formData.createDataRow()
    row.setAlias('total')

    // графа 3..12
    ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds',
            'municipalBondsBefore', 'rtgageBondsBefore', 'ovgvz',
            'eurobondsRF', 'itherEurobonds', 'corporateBonds'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }

    return row
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    def tmp

    if (xml.row.size() > 0) {
        def row = xml.row[0]
        def indexCell = 3
        // def newRow = getNewRow()
        def data = getData(formData)
        def newRow = getRows(data).getAt(0)

        // графа 1
        newRow.number = 1

        // графа 2
        newRow.securitiesType = 'Процентный (купонный) доход по облигациям'

        // графа 3
        newRow.ofz = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 4
        newRow.municipalBonds = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 5
        newRow.governmentBonds = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 6
        newRow.mortgageBonds = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 7
        newRow.municipalBondsBefore = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 8
        newRow.rtgageBondsBefore = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 9
        newRow.ovgvz = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 10
        newRow.eurobondsRF = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 11
        newRow.itherEurobonds = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 12
        newRow.corporateBonds = getNumber(row.cell[indexCell].text())

        save(data)
        // data.clear()
        // data.insert(newRow, 1)
    }

    // итоговая строка
    if (xml.rowTotal.size() > 0) {
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()
        def indexCell = 3

        // графа 3
        total.ofz = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 4
        total.municipalBonds = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 5
        total.governmentBonds = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 6
        total.mortgageBonds = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 7
        total.municipalBondsBefore = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 8
        total.rtgageBondsBefore = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 9
        total.ovgvz = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 10
        total.eurobondsRF = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 11
        total.itherEurobonds = getNumber(row.cell[indexCell].text())
        indexCell++

        // графа 12
        total.corporateBonds = getNumber(row.cell[indexCell].text())

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
 * Получить значение атрибута строки справочника.
 *
 * @param record строка справочника
 * @param alias алиас
 */
def getValue(def record, def alias) {
    def value = record.get(alias)
    switch (value.getAttributeType()) {
        case RefBookAttributeType.DATE :
            return value.getDateValue()
        case RefBookAttributeType.NUMBER :
            return value.getNumberValue()
        case RefBookAttributeType.STRING :
            return value.getStringValue()
        case RefBookAttributeType.REFERENCE :
            return value.getReferenceValue()
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
 * Получить строку из формы.
 *
 * @param data форма
 */
def getTotalRow(def data) {
    if (data != null && !getRows(data).isEmpty() && checkAlias(getRows(data), 'total')) {
        return data.getDataRow(getRows(data), 'total')
    }
    return null
}


/**
 * Расчетать, проверить и сравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def data = getData(formData)
    def totalColumns = [3 : 'ofz', 4 : 'municipalBonds', 5 : 'governmentBonds',
            6 : 'mortgageBonds', 7 : 'municipalBondsBefore', 8 : 'rtgageBondsBefore',
            9 : 'ovgvz', 10 : 'eurobondsRF', 11 : 'itherEurobonds', 12 :'corporateBonds']
    def totalCalc = getRows(data).getAt(0)
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
        logger.error("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def msg) {
    if (getBalancePeriod()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}