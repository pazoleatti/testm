package form_template.income.rnu16

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 *  Скрипт для РНУ-16
 *  Форма "(РНУ-16) Регистр налогового учёта доходов по поставочным сделкам с ПФИ, не признаваемыми ФИСС,
 *                          в соответствии с учётной политикой для целей налогообложения ОАО «Сбербанк России»"
 * formTemplateId=499
 *
 * графа - rowNumber
 * графа - code
 * графа - incomeType
 * графа - sum
 *
 * @author akadyrgulov
 * @author Stanislav Yasinskiy
 * */
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
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
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

// Редактируемые атрибуты
@Field
def editableColumns = ['code', 'sum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'code', 'incomeType', 'sum']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['sum']

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
                def Date date, boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        // сортируем по кодам
        dataRowHelper.save(dataRows.sort { getKnu(it.code) })

        // номер последний строки предыдущей формы
        def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

        for (row in dataRows) {
            row.rowNumber = ++number
            row.incomeType = row.code
        }
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size + 1)
    dataRowHelper.save(dataRows)
}


def calcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 3
    ['rowNumber', 'fix', 'code', 'incomeType', 'sum'].each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп»
        if (++i != row.rowNumber) {
            logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        if (row.code != row.incomeType) {
            logger.error(errorMsg + "Вид (наименование) дохода не соответствует коду налогового учета!")
        }

        // Проверки соответствия НСИ
        checkNSI(28, row, "code")
        checkNSI(28, row, "incomeType")
    }

    // 3. Арифметические проверки расчета итоговой строки
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def String getKnu(def code) {
    return getRefBookValue(28, code)?.CODE?.stringValue
}