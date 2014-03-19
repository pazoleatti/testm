package form_template.income.rnu31.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Форма "(РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям".
 * formTemplateId=328
 *
 * @author rtimerbaev
 */

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

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        // Всего форма должна содержать одну строку
        break
    case FormDataEvent.DELETE_ROW :
        // Всего форма должна содержать одну строку
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT :
    case FormDataEvent.MIGRATION :
        importData()
        calc()
        logicCheck()
        break
}

// Редактируемые атрибуты (графа 1..12)
@Field
def editableColumns = ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds', 'municipalBondsBefore',
        'rtgageBondsBefore', 'ovgvz', 'eurobondsRF', 'itherEurobonds', 'corporateBonds']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number']

// Проверяемые на пустые значения атрибуты (графа 1..12)
@Field
def nonEmptyColumns = editableColumns

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

void calc() {
}

void logicCheck() {
    if (formData.periodOrder == null) {
        throw new ServiceException("Месячная форма создана как квартальная!")
    }
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    // строка из текущего отчета
    def row = getDataRow(dataRows, 'total')

    // 22. Обязательность заполнения полей графы 3..12
    checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

    // графы для которых тип ошибки нефатальный (графа 5, 9, 10, 11)
    def warnColumns = ['governmentBonds', 'ovgvz', 'eurobondsRF', 'itherEurobonds']

    if (formData.periodOrder > 1 && formData.kind == FormDataKind.PRIMARY) {
        // строка из предыдущего отчета
        def rowOld = getPrevMonthTotalRow()

        // 1. Проверка наличия предыдущего экземпляра отчета
        if (rowOld == null && formDataEvent != FormDataEvent.COMPOSE) {
            logger.error("Не найдены экземпляры \"${formTypeService.get(328).name}\" за прошлый отчетный период!")
        }

        // 2..11 Проверка процентного (купонного) дохода по видам валютных ценных бумаг (графы 3..12)
        if (rowOld != null) {
            for (def column : editableColumns) {
                if (row.getCell(column).value < rowOld.getCell(column).value) {
                    def securitiesType = row.securitiesType
                    def message = "Процентный (купонный) доход по $securitiesType уменьшился!"
                    if (column in warnColumns) {
                        logger.warn(message)
                    } else {
                        logger.error(message)
                    }
                }
            }
        }
    }

    // 12..21. Проверка на неотрицательные значения (графы 3..12)
    for (def column : editableColumns) {
        def value = row.getCell(column).value
        if (value != null && value < 0) {
            def columnName = getColumnName(row, column)
            def message = "Значения графы \"$columnName\" по строке 1 отрицательное!"
            if (column in warnColumns) {
                logger.warn(message)
            } else {
                logger.error(message)
            }
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // занулить данные и просуммировать из источников
    def row = getDataRow(dataRows, 'total')
    editableColumns.each { alias ->
        row.getCell(alias).setValue(0, row.getIndex())
    }

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        if (it.formTypeId == formData.formType.id) {
            def taxPeriodId = reportPeriodService.get(formData.reportPeriodId)?.taxPeriod?.id
            def sourceFormData = formDataService.findMonth(it.formTypeId, it.kind, it.departmentId, taxPeriodId, formData.periodOrder)
            if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(sourceFormData)?.allCached
                def sourceRow = getDataRow(sourceDataRows, 'total')
                editableColumns.each { alias ->
                    row.getCell(alias).setValue(sourceRow.getCell(alias).value + row.getCell(alias).getValue(), row.getIndex())
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

void importData() {

    def xml = getXML('№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[1]): 'Вид ценных бумаг',
            (xml.row[0].cell[2]): 'Ставка налога на прибыль 15%',
            (xml.row[0].cell[6]): 'Ставка налога на прибыль 9%',
            (xml.row[0].cell[8]): 'Ставка налога на прибыль 0%',
            (xml.row[0].cell[9]): 'Ставка налога на прибыль 20%',

            (xml.row[1].cell[2]): 'ОФЗ',
            (xml.row[1].cell[3]): 'Субфедеральные и муниципальные облигации, за исключением муниципальных облигаций, выпущенных до 1 января 2007 года на срок не менее 3 лет',
            (xml.row[1].cell[4]): 'Государственные облигации Республики Беларусь',
            (xml.row[1].cell[5]): 'Ипотечные облигации, выпущенные после 1 января 2007 года',
            (xml.row[1].cell[6]): 'Муниципальные облигации, выпущенные до 1 января 2007 года на срок не менее 3 лет',
            (xml.row[1].cell[7]): 'Ипотечные облигации, выпущенные до 1 января 2007 года',
            (xml.row[1].cell[8]): 'ОВГВЗ',
            (xml.row[1].cell[9]): 'Еврооблигации РФ',
            (xml.row[1].cell[10]): 'Прочие еврооблигации',
            (xml.row[1].cell[11]): 'Корпоративные облигации',

            (xml.row[2].cell[0]): '1',
            (xml.row[2].cell[1]): '2',
            (xml.row[2].cell[2]): '3',
            (xml.row[2].cell[3]): '4',
            (xml.row[2].cell[4]): '5',
            (xml.row[2].cell[5]): '6',
            (xml.row[2].cell[6]): '7',
            (xml.row[2].cell[7]): '8',
            (xml.row[2].cell[8]): '9',
            (xml.row[2].cell[9]): '10',
            (xml.row[2].cell[10]): '11',
            (xml.row[2].cell[11]): '12'
    ]

    checkHeaderEquals(headerMapping)

    try {
        // добавить данные в форму
        def totalLoad = addData(xml,3)
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.toString())
    }

   // addData(xml, 2)
}

// Заполнить форму данными
def addData(def xml, int headRowCount) {

    if (xml.row.size() > 0) {
        def row = xml.row[3]
        def indexCell = 2
        def indexRow = 1
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.allCached
        def newRow = dataRows.get(0)
        // графа 1
        newRow.number = 1
        // графа 2
        newRow.securitiesType = 'Процентный (купонный) доход по облигациям'
        // графа 3
        newRow.ofz = getNumber(row.cell[indexCell++].text(), indexRow, indexCell + 1)
        // графа 4
        newRow.municipalBonds = getNumber(row.cell[indexCell++].text(), indexRow, indexCell + 1)
        // графа 5
        newRow.governmentBonds = getNumber(row.cell[indexCell++].text(), indexRow, indexCell + 1)
        // графа 6
        newRow.mortgageBonds = getNumber(row.cell[indexCell++].text(), indexRow, indexCell + 1)
        // графа 7
        newRow.municipalBondsBefore = getNumber(row.cell[indexCell++].text(), indexRow, indexCell + 1)
        // графа 8
        newRow.rtgageBondsBefore = getNumber(row.cell[indexCell++].text(), indexRow, indexCell + 1)
        // графа 9
        newRow.ovgvz = getNumber(row.cell[indexCell++].text(), indexRow, indexCell + 1)
        // графа 10
        newRow.eurobondsRF = getNumber(row.cell[indexCell++].text(), indexRow, indexCell + 1)
        // графа 11
        newRow.itherEurobonds = getNumber(row.cell[indexCell++].text(), indexRow, indexCell + 1)
        // графа 12
        newRow.corporateBonds = getNumber(row.cell[indexCell++].text(), indexRow, indexCell + 1)

        dataRowHelper.save(dataRows)
    }
}

/**
 * Расчетать, проверить и сравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    def totalColumns = [3 : 'ofz', 4 : 'municipalBonds', 5 : 'governmentBonds',
            6 : 'mortgageBonds', 7 : 'municipalBondsBefore', 8 : 'rtgageBondsBefore',
            9 : 'ovgvz', 10 : 'eurobondsRF', 11 : 'itherEurobonds', 12 :'corporateBonds']
    def totalCalc = dataRows.get(0)
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

// Получение xml с общими проверками
def getXML(def String startStr, def String endStr) {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }
    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }
    if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xlsm')) {
        throw new ServiceException('Выбранный файл не соответствует формату xlsx/xlsm!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    return xml
}

// Получить строку за прошлый месяц
def getPrevMonthTotalRow() {
    // проверка на январь и если не задан месяц формы
    if (formData.periodOrder == null || formData.periodOrder == 1) {
        return null
    }
    def prevFormData = formDataService.getFormDataPrev(formData, formData.departmentId)
    if (prevFormData != null) {
        def prevDataRows = formDataService.getDataRowHelper(prevFormData)?.allCached
        return getDataRow(prevDataRows, 'total')
    }
    return null
}