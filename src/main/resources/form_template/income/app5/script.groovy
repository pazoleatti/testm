package form_template.income.app5

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field


/**
 * 6.9	(Приложение 5) Сведения для расчета налога на прибыль
 *
 * @author Lenar Haziev
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow.getAlias() == null) formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        calc()
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
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

// Редактируемые атрибуты
@Field
def editableColumns = ['regionBankDivision', 'avepropertyPricerageCost', 'workersCount', 'subjectTaxCredit']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['regionBank', 'kpp']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['regionBank', 'regionBankDivision', 'kpp', 'avepropertyPricerageCost', 'workersCount', 'subjectTaxCredit']

// Группируемые атрибуты
@Field
def groupColumns = ['regionBankDivision', 'regionBank']

// Атрибуты для итогов
@Field
def totalColumns = ['avepropertyPricerageCost', 'workersCount', 'subjectTaxCredit']

// Все атрибуты
@Field
def allColumns = ['fix', 'regionBank', 'regionBankDivision', 'kpp', 'avepropertyPricerageCost', 'workersCount', 'subjectTaxCredit']

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    def rowNum = 0
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        rowNum++

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, false)

        // Проверки соответствия НСИ
        checkNSI(30, row, "regionBank")
        checkNSI(30, row, "regionBankDivision")
        checkNSI(33, row, "kpp")
    }

    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // справочник "Подразделения"
    def departmentRefDataProvider = refBookFactory.getDataProvider(30)

    // справочник "Параметры подразделения по налогу на прибыль"
    def departmentParamIncomeRefDataProvider = refBookFactory.getDataProvider(33)

    def index = 0
    for (row in dataRows) {
        index++

        def departmentRecords
        if (row.regionBankDivision!=null) departmentRecords = departmentRefDataProvider.getRecords(currentDate, null, "ID = '" + row.regionBankDivision + "'", null);
        if (departmentRecords == null || departmentRecords.getRecords().isEmpty()) {
            logger.error("Строка ${index}: Не найдено родительское подразделение." )
            continue
        }
        def departmentParam = departmentRecords.getRecords().getAt(0)

        def departmentParamIncomeRecords = departmentParamIncomeRefDataProvider.getRecords(currentDate, null, "DEPARTMENT_ID = '" + row.regionBankDivision + "'", null);
        if (departmentParamIncomeRecords == null || departmentParamIncomeRecords.getRecords().isEmpty()) {
            logger.error("Строка ${index}: Не найдены настройки подразделения.")
            continue
        }
        def incomeParam = departmentParamIncomeRecords.getRecords().getAt(0)

        def parentDepartmentId = null;
        long centralId = 113 // ID Центрального аппарата.
        // У Центрального аппарата родительским подразделением должен быть он сам
        if (centralId == row.regionBankDivision) {
            parentDepartmentId = centralId
        } else {
            parentDepartmentId = departmentParam.get('PARENT_ID').getReferenceValue()
        }

        // графа 2 - название подразделения
        row.regionBank = parentDepartmentId

        // графа 4 - кпп
        row.kpp = incomeParam.get('record_id').getNumberValue()
    }
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // Сортировка
    dataRows.sort { a, b ->
        if (getRefBookValue(30, a.regionBank)?.NAME?.stringValue == getRefBookValue(30, b.regionBank)?.NAME?.stringValue) {
            return -(getRefBookValue(30, b.regionBankDivision)?.NAME?.stringValue <=> getRefBookValue(30, a.regionBankDivision)?.NAME?.stringValue)
        }
        return -(getRefBookValue(30, b.regionBank)?.NAME?.stringValue <=> getRefBookValue(30, a.regionBank)?.NAME?.stringValue)
    }

    index = 1
    for (row in dataRows) {
        // графа 1
        row.number = index++
    }
    dataRows.add(getTotalRow(dataRows))
    dataRowHelper.save(dataRows)
}

/**
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
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

/**
 * Получить номер строки в таблице.
 *
 * @param data данные нф (helper)
 * @param row строка
 */
def getIndex(def dataRows, def row) {
    dataRows.indexOf(row)
}