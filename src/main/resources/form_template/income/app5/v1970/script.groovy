package form_template.income.app5.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 6.9	(Приложение 5) Сведения для расчета налога на прибыль
 * formTemplateId=372
 *
 * @author Lenar Haziev
 */

// графа 1  - number
// графа -  - fix
// графа 2  - regionBank                атрибут 161 NAME "Наименование подразделение" - справочник 30 "Подразделения"
// графа 3  - regionBankDivision        атрибут 161 NAME "Наименование подразделение" - справочник 30 "Подразделения"
// графа 4  - kpp                       абсолютное значение - атрибут 234 KPP "КПП" - справочник 33 "Параметры подразделения по налогу на прибыль"
// графа 5  - avepropertyPricerageCost
// графа 6  - workersCount
// графа 7  - subjectTaxCredit
// графа 8  - decreaseTaxSum
// графа 9  - taxRate

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        logicCheckBeforeCalc()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        logicCheckBeforeCalc()
        calc()
        logicCheck()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Все атрибуты
@Field
def allColumns = ['number', 'fix', 'regionBank', 'regionBankDivision', 'kpp', 'avepropertyPricerageCost',
        'workersCount', 'subjectTaxCredit', 'decreaseTaxSum', 'taxRate']

// Редактируемые атрибуты
@Field
def editableColumns = ['regionBankDivision', 'avepropertyPricerageCost', 'workersCount', 'subjectTaxCredit', 'decreaseTaxSum', 'taxRate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['regionBank', 'kpp']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['regionBank', 'regionBankDivision', 'kpp', 'avepropertyPricerageCost', 'workersCount',
        'subjectTaxCredit', 'decreaseTaxSum', 'taxRate']

// Группируемые атрибуты
@Field
def groupColumns = ['regionBankDivision', 'regionBank']

// Атрибуты для итогов
@Field
def totalColumns = ['avepropertyPricerageCost', 'workersCount', 'subjectTaxCredit', 'decreaseTaxSum']

@Field
def endDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, false)
}

//// Кастомные методы

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def newRow = getNewRow()

    def index = 0
    if (currentDataRow != null && currentDataRow.getAlias() != null) {
        // выбрана итоговая - вставить перед итоговой
        index = currentDataRow.getIndex()
    } else if (currentDataRow != null && currentDataRow.getAlias() == null) {
        // выбрана фиксированная строка - после выбранной нефиксированной
        index = currentDataRow.getIndex() + 1
    } else {
        // невыбрана строка - вставить перед итоговой
        def dataRows = dataRowHelper.allCached
        index = getDataRow(dataRows, 'total').getIndex()
    }

    dataRowHelper.insert(newRow, index)
}

// Логические проверки
void logicCheckBeforeCalc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (row in dataRows) {
        if (row != null && row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка наличия значения «Наименование подразделения» в справочнике «Подразделения»
        def departmentParam
        if (row.regionBankDivision != null) {
            departmentParam = getRefBookRecord(30, "ID", "$row.regionBankDivision", getReportPeriodEndDate(),
                    row.getIndex(), getColumnName(row, 'regionBankDivision'), false)
        }
        if (departmentParam == null || departmentParam.isEmpty()) {
            throw new ServiceException(errorMsg + "Не найдено подразделение территориального банка!")
        } else {
            long centralId = 113 // ID Центрального аппарата.
            // У Центрального аппарата родительским подразделением должен быть он сам
            if (centralId != row.regionBankDivision) {
                // графа 2 - название подразделения
                if (departmentParam.get('PARENT_ID')?.getReferenceValue() == null) {
                    throw new ServiceException(errorMsg + "Для подразделения территориального банка «${departmentParam.NAME.stringValue}» в справочнике «Подразделения» отсутствует значение наименования родительского подразделения!")
                }
            }
        }

        // 2. Проверка наличия значения «КПП» в форме настроек подразделения
        def incomeParam
        if (row.regionBankDivision != null) {
            incomeParam = getRefBookRecord(33, "DEPARTMENT_ID", "$row.regionBankDivision", getReportPeriodEndDate() - 1,
                    row.getIndex(), getColumnName(row, 'regionBankDivision'), false)
        }
        if (incomeParam == null || incomeParam.isEmpty()) {
            throw new ServiceException(errorMsg + "Не найдены настройки подразделения!")
        } else {
            // графа 4 - кпп
            if (incomeParam?.get('record_id')?.getNumberValue() == null || incomeParam?.get('KPP')?.getStringValue() == null) {
                throw new ServiceException(errorMsg + "Для подразделения «${departmentParam.NAME.stringValue}» на форме настроек подразделений отсутствует значение атрибута «КПП»!")
            }
        }
    }
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    def rowNumber = 0
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        rowNumber++
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля «<Наименование поля>»
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп»
        if (rowNumber != row.number) {
            logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        // Проверки НСИ
        // 1. Проверка значения графы «КПП» - графа 4 - kpp - абсолютное значение - атрибут 234 KPP "КПП" - справочник 33 "Параметры подразделения по налогу на прибыль"
        if (row.regionBankDivision != null && row.kpp != null && row.kpp != '') {
            def incomeParam = getRefBookRecord(33, "DEPARTMENT_ID", "$row.regionBankDivision", getReportPeriodEndDate() - 1,
                    row.getIndex(), getColumnName(row, 'regionBankDivision'), false)
            if (incomeParam?.KPP?.stringValue != row.kpp) {
                def name = getColumnName(row, 'kpp')
                logger.error("Значение графы «$name» не соответствует значению на форме Настроек подразделений.")
            }
        }
    }

    // 3. Арифметические проверки расчета итоговой строки
    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    // Удаление подитогов
    deleteAllAliased(dataRows)

    for (row in dataRows) {
        // графа 2 - название подразделения
        row.regionBank = calc2(row)

        // графа 4 - кпп
        row.kpp = calc4(row)

        // графа 8  - decreaseTaxSum
        if (row.decreaseTaxSum == null) {
            row.decreaseTaxSum = 0
        }

        // графа 9  - taxRate
        if (row.taxRate == null) {
            row.taxRate = 0
        }
    }
    // Сортировка
    dataRows.sort { a, b ->
        def regionBankA = getRefBookValue(30, a.regionBank)?.NAME?.stringValue
        def regionBankB = getRefBookValue(30, b.regionBank)?.NAME?.stringValue
        if (regionBankA == regionBankB) {
            def regionBankDivisionA = getRefBookValue(30, a.regionBankDivision)?.NAME?.stringValue
            def regionBankDivisionB = getRefBookValue(30, b.regionBankDivision)?.NAME?.stringValue
            return (regionBankDivisionA <=> regionBankDivisionB)
        }
        return (regionBankA <=> regionBankB)
    }

    def index = 0
    for (row in dataRows) {
        // графа 1
        row.number = ++index
    }
    dataRows.add(getTotalRow(dataRows))
    dataRowHelper.save(dataRows)
}


// графа 2 - название подразделения
def calc2(def row) {
    def departmentParam
    if (row.regionBankDivision != null) {
        departmentParam = getRefBookRecord(30, "ID", "$row.regionBankDivision", getReportPeriodEndDate(), -1, null, false)
    }
    if (departmentParam == null || departmentParam.isEmpty()) {
        return null
    }

    long centralId = 113 // ID Центрального аппарата.
    // У Центрального аппарата родительским подразделением должен быть он сам
    if (centralId == row.regionBankDivision) {
        return centralId
    } else {
        return departmentParam.get('PARENT_ID').getReferenceValue()
    }
}

// графа 4 - кпп
def calc4(def row) {
    def incomeParam = null
    if (row.regionBankDivision != null) {
        incomeParam = getRefBookRecord(33, "DEPARTMENT_ID", "$row.regionBankDivision", getReportPeriodEndDate() - 1, -1, null, false)
    }
    return incomeParam?.KPP?.stringValue
}

// Расчет итоговой строки
def getTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 4
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'number'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 9, 1)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tmpRow, 'number'),
            (xml.row[0].cell[2]): getColumnName(tmpRow, 'regionBank'),
            (xml.row[0].cell[3]): getColumnName(tmpRow, 'regionBankDivision'),
            (xml.row[0].cell[4]): getColumnName(tmpRow, 'kpp'),
            (xml.row[0].cell[5]): getColumnName(tmpRow, 'avepropertyPricerageCost'),
            (xml.row[0].cell[6]): getColumnName(tmpRow, 'workersCount'),
            (xml.row[0].cell[7]): getColumnName(tmpRow, 'subjectTaxCredit'),
            (xml.row[0].cell[8]): 'Льготы по налогу в бюджет субъекта (руб.)',
            (xml.row[1].cell[8]): 'Уменьшение суммы налога (руб.)',
            (xml.row[1].cell[9]): 'Ставка налога (%)',

            (xml.row[2].cell[0]): '1'
    ]
    (2..9).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 3)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def int xlsIndexRow = xmlIndexRow + rowOffset

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)

        // графа 3
        def indexCol = 3
        newRow.regionBankDivision = getRecordIdImport(30, 'NAME', row.cell[indexCol].text(), xlsIndexRow, indexCol + colOffset)

        // графа 5
        indexCol = 5
        newRow.avepropertyPricerageCost = getNumber(row.cell[indexCol].text(), xlsIndexRow, indexCol + colOffset)

        // графа 6
        indexCol = 6
        newRow.workersCount = getNumber(row.cell[indexCol].text(), xlsIndexRow, indexCol + colOffset)

        // графа 7
        indexCol = 7
        newRow.subjectTaxCredit = getNumber(row.cell[indexCol].text(), xlsIndexRow, indexCol + colOffset)

        // графа 8
        indexCol = 8
        newRow.decreaseTaxSum = getNumber(row.cell[indexCol].text(), xlsIndexRow, indexCol + colOffset)

        // графа 9
        indexCol = 9
        newRow.taxRate = getNumber(row.cell[indexCol].text(), xlsIndexRow, indexCol + colOffset)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

def getNewRow() {
    def newRow = formData.createDataRow()
    // графа 8
    newRow.decreaseTaxSum = 0
    // графа 9
    newRow.taxRate = 0

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}