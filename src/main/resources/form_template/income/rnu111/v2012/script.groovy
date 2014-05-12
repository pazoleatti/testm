package form_template.income.rnu111.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * (РНУ-111) Регистр налогового учёта доходов, возникающих в связи с применением в сделках по предоставлению Межбанковских кредитов Взаимозависимым лицам и резидентам оффшорных зон Процентных ставок, не соответствующих рыночному уровню
 * formTemplateId=367
 */
// 1 - number
// 2 - name
// 3 - country
// 4 - inn
// 5 - code
// 6 - reasonNumber
// 7 - reasonDate
// 8 - base
// 9 - credit
// 10 - currency
// 11 - date
// 12 - interestRate
// 13 - incomeFactSum
// 14 - taxInterestRate
// 15 - incomeLevelSum
// 16 - deviation
// 17 - incomeAddSum
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
    case FormDataEvent.IMPORT:
        noImport(logger)
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
def editableColumns = ['name', 'code', 'reasonNumber', 'reasonDate', 'base', 'credit', 'currency', 'date',
        'interestRate', 'incomeFactSum', 'taxInterestRate', 'incomeLevelSum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'name', 'code', 'reasonNumber', 'reasonDate', 'base', 'credit',
        'currency', 'date', 'interestRate', 'incomeFactSum', 'taxInterestRate', 'incomeLevelSum', 'deviation',
        'incomeAddSum']

// Все атрибуты
@Field
def allColumns = ['number', 'total', 'name', 'country', 'inn', 'code', 'reasonNumber', 'reasonDate', 'base', 'credit',
        'currency', 'date', 'interestRate', 'incomeFactSum', 'taxInterestRate', 'incomeLevelSum', 'deviation',
        'incomeAddSum']

// Атрибуты для итогов
@Field
def totalColumns = ['incomeFactSum', 'incomeLevelSum', 'incomeAddSum']

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    // РНУ-4
    if (getFormDataRnu(316) == null) {
        def ftRnu4 = formTypeService.get(316);
        throw new ServiceException("Не найдены экземпляры «${ftRnu4.name}» за текущий отчетный период!")
    }
    // РНУ-6
    if (getFormDataRnu(318) == null) {
        def ftRnu6 = formTypeService.get(318);
        throw new ServiceException("Не найдены экземпляры «${ftRnu6.name}» за текущий отчетный период!")
    }

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // Удаление итогов
    deleteAllAliased(dataRows)

    // Номер последней строки формы из предыдущего периода
    def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

    for (row in dataRows) {
        // графа 1
        row.number = ++index

        // графа 16
        if (row.taxInterestRate != null && row.interestRate != null) {
            row.deviation = row.taxInterestRate - row.interestRate
        }

        // графа 17
        if (row.incomeLevelSum != null && row.incomeFactSum != null) {
            row.incomeAddSum = row.incomeLevelSum - row.incomeFactSum
        }
    }

    // Добавление итогов
    dataRows.add(getTotalRow(dataRows))
    dataRowHelper.save(dataRows)
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    if (dataRows.isEmpty()) {
        return
    }

    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        index++
        def errorMsg = "Строка ${row.getIndex()}: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп» (в рамках текущего года)
        if (index != row.number) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // 3. Проверка даты совершения операции и границ отчетного периода
        if (endDate != null && row.reasonDate != null && row.reasonDate.after(endDate)) {
            logger.error(errorMsg + 'Дата совершения операции вне границ отчетного периода!')
        }

        // 4. Арифметические проверки
        if (row.taxInterestRate != null && row.interestRate != null && row.deviation != row.taxInterestRate - row.interestRate) {
            logger.error(errorMsg + "Неверно рассчитана графа «${getColumnName(row, 'deviation')}»!")
        }
        if (row.incomeLevelSum != null && row.incomeFactSum != null && row.incomeAddSum != row.incomeLevelSum - row.incomeFactSum) {
            logger.error(errorMsg + "Неверно рассчитана графа «${getColumnName(row, 'incomeAddSum')}»!")
        }
    }

    // 5. Арифметические проверки итогов
    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Расчет итоговой строки
def getTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.total = 'Итого'
    totalRow.getCell('total').colSpan = 2
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Экземпляр формы по типу
def getFormDataRnu(def id) {
    return formDataService.find(id, FormDataKind.PRIMARY, formDataDepartment.id, formData.reportPeriodId)
}