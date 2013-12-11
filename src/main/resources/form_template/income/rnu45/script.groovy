package form_template.income.rnu45

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Скрипт для РНУ-45
 * Форма "(РНУ-45) Регистр налогового учёта «ведомость начисленной амортизации по нематериальным активам»"  (341)
 * formTemplateId=341
 *
 * графа 1	- rowNumber
 * графа 2	- inventoryNumber
 * графа 3	- name
 * графа 4	- buyDate
 * графа 5	- usefulLife
 * графа 6	- expirationDate
 * графа 7	- startCost
 * графа 8	- depreciationRate
 * графа 9	- amortizationMonth
 * графа 10	- amortizationSinceYear
 * графа 11	- amortizationSinceUsed
 *
 * @author akadyrgulov
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:

        // TODO убрать когда появится механизм назначения periodOrder при создании формы
        if (formData.periodOrder == null)
            return

        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
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
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
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
def editableColumns = ['inventoryNumber', 'name', 'buyDate', 'usefulLife', 'expirationDate', 'startCost']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'inventoryNumber', 'name', 'buyDate', 'usefulLife', 'expirationDate', 'startCost',
        'depreciationRate', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['startCost', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']

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

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    if (!isBalancePeriod && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.getFormType().getName()
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
}

//// Кастомные методы

@Field
def formDataPrev = null // Форма предыдущего месяца
@Field
def dataRowHelperPrev = null // DataRowHelper формы предыдущего месяца

// Получение формы предыдущего месяца
def getFormDataPrev() {
    if (formDataPrev == null) {
        formDataPrev = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    }
    return formDataPrev
}

// Получение DataRowHelper формы предыдущего месяца
def getDataRowHelperPrev() {
    if (dataRowHelperPrev == null) {
        def formDataPrev = getFormDataPrev()
        if (formDataPrev != null) {
            dataRowHelperPrev = formDataService.getDataRowHelper(formDataPrev)
        }
    }
    return dataRowHelperPrev
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        def reportDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
        def reportDateStart = reportPeriodService.getStartDate(formData.reportPeriodId).time

        def formDataOld = getFormDataPrev()
        def dataOld = formDataOld != null ? getDataRowHelperPrev() : null
        def index = 0

        for (def row in dataRows) {
            // графа 1
            row.rowNumber = ++index
            // графа 8
            row.depreciationRate = calc8(row)
            // графа 9
            row.amortizationMonth = calc9(row)
            // для граф 10 и 11
            prevValues = getPrev10and11(dataOld, row)
            // графа 10
            row.amortizationSinceYear = calc10(row, reportDateStart, reportDate, prevValues[0])
            // графа 11
            row.amortizationSinceUsed = calc11(row, reportDateStart, reportDate, prevValues[1])
        }
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size + 1)
    dataRowHelper.save(dataRows)
}

def calcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 6
    ['rowNumber', 'fix', 'startCost', 'depreciationRate', 'amortizationMonth', 'amortizationSinceYear',
            'amortizationSinceUsed'].each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
}

// Ресчет графы 8
def BigDecimal calc8(def row) {
    if (row.usefulLife == null || row.usefulLife == 0) {
        return null
    }
    return ((1 / row.usefulLife) * 100).setScale(4, RoundingMode.HALF_UP)
}

// Ресчет графы 9
def BigDecimal calc9(def row) {
    if (row.startCost == null || row.depreciationRate == null) {
        return null
    }
    return (row.startCost * row.depreciationRate).setScale(2, RoundingMode.HALF_UP)
}

// Ресчет графы 10
def BigDecimal calc10(def row, def reportDateStart, def reportDate, def oldRow10) {
    Calendar buyDate = calc10and11(row)
    if (buyDate != null && reportDateStart != null && reportDate != null && row.amortizationMonth != null)
        return row.amortizationMonth + ((buyDate.get(Calendar.MONTH) == Calendar.JANUARY || (buyDate.after(reportDateStart) && buyDate.before(reportDate))) ? 0 : ((oldRow10 == null) ? 0 : oldRow10))
    return null
}

// Ресчет графы 11
def BigDecimal calc11(def row, def reportDateStart, def reportDate, def oldRow11) {
    Calendar buyDate = calc10and11(row)
    if (buyDate != null && reportDateStart != null && reportDate != null && row.amortizationMonth != null)
        return row.amortizationMonth + ((buyDate.after(reportDateStart) && buyDate.before(reportDate)) ? 0 : ((oldRow11 == null) ? 0 : oldRow11))
    return null
}

// Общая часть ресчета граф 10 и 11
Calendar calc10and11(def row) {
    if (row.buyDate == null) {
        return null
    }
    Calendar buyDate = Calendar.getInstance()
    buyDate.setTime(row.buyDate)
    return buyDate
}

def logicCheck() {
    if (formData.periodOrder == null) {
        throw new ServiceException("Месячная форма создана как квартальная!")
    }

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {
        // Инвентарные номера
        def Set<String> invSet = new HashSet<String>()
        def formDataOld = getFormDataPrev()
        def dataOld = formDataOld != null ? getDataRowHelperPrev() : null
        // Отчетная дата
        def reportDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
        //Начальная дата отчетного периода
        def reportDateStart = reportPeriodService.getEndDate(formData.reportPeriodId).time

        // алиасы графов для арифметической проверки
        def arithmeticCheckAlias = ['depreciationRate', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']
        // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
        def needValue = [:]

        for (def row in dataRows) {
            if (row.getAlias() != null) {
                continue
            }

            def index = row.getIndex()
            def errorMsg = "Строка $index: "

            // 1. Проверка на заполнение поля
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

            // 2. Проверка на уникальность поля «инвентарный номер»
            if (invSet.contains(row.inventoryNumber)) {
                logger.error(errorMsg + "Инвентарный номер не уникальный!")
            } else {
                invSet.add(row.inventoryNumber)
            }

            // 3. Проверка на нулевые значения
            if (row.startCost == 0 && row.amortizationMonth == 0 && row.amortizationSinceYear == 0 && row.amortizationSinceUsed == 0) {
                logger.error(errorMsg + "Все суммы по операции нулевые!")
            }

            // 4. Арифметические проверки расчета неитоговых граф
            needValue['depreciationRate'] = calc8(row)
            needValue['amortizationMonth'] = calc9(row)
            prevValues = getPrev10and11(dataOld, row)
            needValue['amortizationSinceYear'] = calc10(row, reportDateStart, reportDate, prevValues[0])
            needValue['amortizationSinceUsed'] = calc11(row, reportDateStart, reportDate, prevValues[1])
            checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        }
        // 5. Арифметические проверки расчета итоговой строки
        checkTotalSum(dataRows, totalColumns, logger, true)
    }
}

// Получить значение за предыдущий отчетный период для графы 10 и 11
def getPrev10and11(def dataOld, def row) {
    if (dataOld != null)
        for (def rowOld : dataOld.getAllCached()) {
            if (rowOld.inventoryNumber == row.inventoryNumber) {
                return [rowOld.amortizationSinceYear, rowOld.amortizationSinceUsed]
            }
        }
    return [null, null]
}