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
        logicalCheckBeforeCalc()
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
    case FormDataEvent.COMPOSE :
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
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
def allColumns = ['fix', 'regionBank', 'regionBankDivision', 'kpp', 'avepropertyPricerageCost', 'workersCount', 'subjectTaxCredit']

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
void logicalCheckBeforeCalc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // справочник "Подразделения"
    def departmentRefDataProvider = refBookFactory.getDataProvider(30)

    // справочник "Параметры подразделения по налогу на прибыль"
    def departmentParamIncomeRefDataProvider = refBookFactory.getDataProvider(33)

    def fieldNumber = 0
    for (row in dataRows) {
        if (row != null && row.getAlias() != null) {
            continue
        }
        fieldNumber++

        def departmentRecords
        def departmentParam
        if (row.regionBankDivision!=null) departmentRecords = departmentRefDataProvider.getRecords(currentDate, null, "ID = '" + row.regionBankDivision + "'", null)?.getRecords()
        if (departmentRecords == null || departmentRecords.isEmpty()) {
            return
        } else {
            departmentParam = departmentRecords.getAt(0)

            long centralId = 113 // ID Центрального аппарата.
            // У Центрального аппарата родительским подразделением должен быть он сам
            if (centralId != row.regionBankDivision) {
                // графа 2 - название подразделения
                if (departmentParam.get('PARENT_ID')?.getReferenceValue()==null) {
                    logger.error("Строка $fieldNumber: Для подразделения территориального банка «${departmentParam.NAME.stringValue}» в справочнике «Подразделения» отсутствует значение наименования родительского подразделения!")
                }
            }
        }

        def departmentParamIncomeRecords
        if (row.regionBankDivision!=null) departmentParamIncomeRecords = departmentParamIncomeRefDataProvider.getRecords(currentDate, null, "DEPARTMENT_ID = " + row.regionBankDivision, null)?.getRecords();
        if (departmentParamIncomeRecords == null || departmentParamIncomeRecords.isEmpty()) {
            logger.error("Строка $fieldNumber: Не найдены настройки подразделения!")
        } else {
            def incomeParam = departmentParamIncomeRecords.getAt(0)

            // графа 4 - кпп
            if (incomeParam?.get('record_id')?.getNumberValue() == null) {
                logger.error("Строка $fieldNumber: Для подразделения «${departmentParam.NAME.stringValue}» на форме настроек подразделений отсутствует значение атрибута «КПП»!")
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

        // 1. Проверка на заполнение поля «<Наименование поля>»
        checkNonEmptyColumns(row, rowNumber, nonEmptyColumns, logger, false)

        // 2. Проверка на уникальность поля «№ пп»
        if (rowNumber != row.number) {
            logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
        }

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

        // графа 2 - название подразделения
        row.regionBank = calc2(row, departmentRefDataProvider)

        // графа 4 - кпп
        row.kpp = calc4(row, departmentParamIncomeRefDataProvider)
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

    index = 0
    for (row in dataRows) {
        // графа 1
        row.number = ++index
    }
    dataRows.add(getTotalRow(dataRows))
    dataRowHelper.save(dataRows)
}


// графа 2 - название подразделения
def calc2(def row, def departmentRefDataProvider) {
    def departmentRecords
    if (row.regionBankDivision!=null) departmentRecords = departmentRefDataProvider.getRecords(currentDate, null, "ID = '" + row.regionBankDivision + "'", null)?.getRecords()
    if (departmentRecords == null || departmentRecords.isEmpty()) {
        return null
    }
    def departmentParam = departmentRecords.getAt(0)

    long centralId = 113 // ID Центрального аппарата.
    // У Центрального аппарата родительским подразделением должен быть он сам
    if (centralId == row.regionBankDivision) {
        return centralId
    } else {
        return departmentParam.get('PARENT_ID').getReferenceValue()
    }
}

// графа 4 - кпп
def calc4(def row, def departmentParamIncomeRefDataProvider){
    def departmentParamIncomeRecords
    if (row.regionBankDivision!=null) departmentParamIncomeRecords = departmentParamIncomeRefDataProvider.getRecords(currentDate, null, "DEPARTMENT_ID = " + row.regionBankDivision, null)?.getRecords();
    if (departmentParamIncomeRecords == null || departmentParamIncomeRecords.isEmpty()) {
        return null
    }
    def incomeParam = departmentParamIncomeRecords.getAt(0)

    return incomeParam.get('record_id').getNumberValue()
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
