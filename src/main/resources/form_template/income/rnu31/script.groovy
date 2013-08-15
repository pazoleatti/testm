package form_template.income.rnu31

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType

/**
 * Форма "(РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям".
 *
 * @version 59
 *
 * TODO:
 *      - при импорте нет получения имени файла для определения типа файла (xls или csv)
 *      - проверки корректности данных проверить когда будут сделаны вывод сообщении
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck(true)
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        break
    case FormDataEvent.ADD_ROW :
        // addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        // deleteRow()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck(true)
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        break
    case FormDataEvent.IMPORT :
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
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = getNewRow()
    def index = 0
    if (currentDataRow != null) {
        if (currentDataRow.getAlias() == null) {
            index = getIndex(currentDataRow)
        } else {
            index = getIndex(currentDataRow) - 1
        }
    }
    data.insert(newRow, index + 1)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    getData(formData).delete(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def data = getData(formData)
    /*
     * Проверка объязательных полей.
     */

    // список проверяемых столбцов (графа 3..12)
    def requiredColumns = ['ofz', 'municipalBonds', 'governmentBonds',
            'mortgageBonds', 'municipalBondsBefore', 'rtgageBondsBefore',
            'ovgvz', 'eurobondsRF', 'itherEurobonds', 'corporateBonds']

    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    // данные предыдущего отчета
    def formDataOld = getFormDataOld()
    def data = getData(formData)
    def dataOld = getData(formDataOld)

    /** Строка из предыдущего отчета. */
    def rowOld = (formDataOld != null && !getRows(dataOld).isEmpty() ? dataOld.getDataRow(getRows(dataOld),'total') : null)

    /** Строка из текущего отчета. */
    def row = (formData != null && !getRows(data).isEmpty() ? data.getDataRow(getRows(data),'total') : null)
    if (row == null) {
        return true
    }

    // список проверяемых столбцов (графа 1..12)
    def requiredColumns = ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds',
            'municipalBondsBefore', 'rtgageBondsBefore', 'ovgvz', 'eurobondsRF',
            'itherEurobonds', 'corporateBonds']

    // 22. Обязательность заполнения полей графы 1..12
    if (!checkRequiredColumns(row, requiredColumns, useLog)) {
        return false
    }

    // графы для которых тип ошибки нефатальный (графа 5, 9, 10, 11)
    def warnColumns = ['governmentBonds', 'ovgvz', 'eurobondsRF', 'itherEurobonds']

    // TODO (Ramil Timerbaev) протестировать проверку "начиная с отчета за февраль"
    if (!isFirstMonth()) {
        // 1. Проверка наличия предыдущего экземпляра отчета
        if (rowOld == null) {
            logger.error('Отсутствует предыдущий экземпляр отчета')
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
                    logger.error(message)
                }
                return false
            }
        }
    }

    // 12..21. Проверка на неотрицательные значения (графы 3..12)
    for (def column : requiredColumns) {
        if (row.getCell(column).getValue() < 0) {
            def columnName = getColumnName(row, column)
            def message = "Значения графы \"$columnName\" по строке 1 отрицательное!"
            if (column in warnColumns) {
                logger.warn(message)
            } else {
                logger.error(message)
            }
            return false
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
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriod.isBalancePeriod()) {
        logger.error('Налоговая форма не может быть в периоде ввода остатков.')
        return
    }

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
    // TODO (Ramil Timerbaev) Костыль! это значение должно передаваться в скрипт
    def fileName = 'fileName.xls'

    def is = ImportInputStream
    if (is == null) {
        return
    }

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Вид ценных бумаг', null);
    if (xmlString == null) {
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        return
    }

    // количество строк в шапке
    def headRowCount = 4

    // проверка заголовка таблицы
    if (!checkTableHead(xml, headRowCount)) {
        logger.error('Заголовок таблицы не соответствует требуемой структуре!')
        return
    }

    // добавить данные в форму
    addData(xml, headRowCount)
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
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
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
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def row = formData.createDataRow()

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
 * @param headRowCount количество строк в шапке
 */
void addData(def xml, headRowCount) {
    if (xml == null) {
        return
    }
    def data = getData(formData)

    def tmp

    def indexRow = 0
    // TODO (Ramil Timerbaev) Проверка корректности данных
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= headRowCount) {
            continue
        }

        if (row.cell[0].text() == 'Процентный (купонный) доход по облигациям') {
            def newRow = getNewRow()
            def index = 0

            // графа 1
            newRow.number = 1

            // графа 2
            newRow.securitiesType = row.cell[index].text()
            index++

            // графа 3
            newRow.ofz = getNumber(row.cell[index].text())
            index++

            // графа 4
            newRow.municipalBonds = getNumber(row.cell[index].text())
            index++

            // графа 5
            newRow.governmentBonds = getNumber(row.cell[index].text())
            index++

            // графа 6
            newRow.mortgageBonds = getNumber(row.cell[index].text())
            index++

            // графа 7
            newRow.municipalBondsBefore = getNumber(row.cell[index].text())
            index++

            // графа 8
            newRow.rtgageBondsBefore = getNumber(row.cell[index].text())
            index++

            // графа 9
            newRow.ovgvz = getNumber(row.cell[index].text())
            index++

            // графа 10
            newRow.eurobondsRF = getNumber(row.cell[index].text())
            index++

            // графа 11
            newRow.itherEurobonds = getNumber(row.cell[index].text())
            index++

            // графа 12
            newRow.corporateBonds = getNumber(row.cell[index].text())

            data.clear()
            data.insert(newRow, 1)
            data.commit()
            logger.info('Данные загружены')
            break
        }
    }
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

/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, def headRowCount) {
    def colCount = 11
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (xml.row[0].cell[0] == 'Вид ценных бумаг' &&
            xml.row[0].cell[1] == 'Ставка налога на прибыль' &&
            xml.row[1].cell[1] == '15' &&
            xml.row[2].cell[1] == 'ОФЗ' &&
            xml.row[2].cell[2] == 'Субфедеральные и муниципальные облигации, за исключением муниципальных облигаций, выпущенных до 1 января 2007 года на срок не менее 3 лет' &&
            xml.row[2].cell[3] == 'Государственные облигации Республики Беларусь' &&
            xml.row[2].cell[4] == 'Ипотечные облигации, выпущенные после  1 января 2007 года' &&
            xml.row[1].cell[5] == '9' &&
            xml.row[2].cell[5] == 'Муниципальные облигации, выпущенные до 1 января 2007 года на срок не менее 3 лет' &&
            xml.row[2].cell[6] == 'Ипотечные облигации, выпущенные до  1 января 2007 года' &&
            xml.row[1].cell[7] == '0' &&
            xml.row[2].cell[7] == 'ОВГВЗ' &&
            xml.row[1].cell[8] == '20' &&
            xml.row[2].cell[8] == 'Еврооблигации РФ' &&
            xml.row[2].cell[9] == 'Прочие еврооблигации' &&
            xml.row[2].cell[10] == 'Корпоративные облигации')
    return result
}

/**
 * Получить значение атрибута строки справочника.

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