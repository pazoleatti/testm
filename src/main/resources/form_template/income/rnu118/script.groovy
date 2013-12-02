package form_template.income.rnu118

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Форма "(РНУ-117) Регистр налогового учёта доходов и расходов, по операциям со сделками форвард, квалифицированным в качестве операций с ФИСС для целей налогообложения"
 *
 * @author bkinzyabulatov
 *
 * Графа 1    rowNumber           № пп
 * Графа 2.1  transactionNumber   Общая информация о сделке. Номер сделки
 * Графа 2.2  contractor          Общая информация о сделке. Наименование контрагента
 * Графа 2.3  transactionKind     Общая информация о сделке. Вид сделки
 * Графа 3    transactionDate     Дата заключения сделки
 * Графа 4    transactionEndDate  Дата окончания сделки
 * Графа 5    transactionCalcDate Дата осуществления расчетов по сделке
 * Графа 6    bonusSize           Опционная премия Размер опционной премии к получению (+)/ уплате (-), в валютерасчетов
 * Графа 7    bonusCurrency       Опционная премия Валюта опционной премии
 * Графа 8    bonusSum            Опционная премия Сумма опционной премии  к получению (+)/ уплате (-), в рублях
 * Графа 9    course              Курс Банка России на дату осуществления расчетов по уплате премии
 * Графа 10.1 minPrice            Максимальная/ минимальная расчетная стоимость сделки для целей налогообложения на дату заключения. Минимальная расчетная стоимость
 * Графа 10.2 maxPrice            Максимальная/ минимальная расчетная стоимость сделки для целей налогообложения на дату заключения. Максимальная расчетная стоимость
 * Графа 11.1 deviationMinPrice   Сумма отклонения от расчетной стоимости сделки более чем на 20%, руб. Сумма отклонения от минимальной (расчетной) цены
 * Графа 11.2 deviationMaxPrice   Сумма отклонения от расчетной стоимости сделки более чем на 20%, руб. Сумма отклонения от максимальной (расчетной) цены
 * Графа 12.1 request             Требования (+)/ обязательства (-) по исполненным сделкам, руб. Требования
 * Графа 12.2 liability           Требования (+)/ обязательства (-) по исполненным сделкам, руб. Обязательства
 * Графа 13.1 income              Доходы/ расходы, учитываемые в целях налога на прибыль по сделке, руб. Доходы
 * Графа 13.2 outcome             Доходы/ расходы, учитываемые в целях налога на прибыль по сделке, руб. Расходы
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
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (!currentDataRow?.getAlias()?.contains('itg')) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
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

// Все аттрибуты
@Field
def allColumns = ["rowNumber", "transactionNumber", "contractor", "transactionKind",
        "transactionDate", "transactionEndDate", "transactionCalcDate", "bonusSize",
        "bonusCurrency", "bonusSum", "course", "minPrice", "maxPrice", "deviationMinPrice",
        "deviationMaxPrice", "request", "liability", "income", "outcome"]

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ["deviationMinPrice", "deviationMaxPrice", "request", "liability", "income", "outcome"]

// Редактируемые атрибуты
@Field
def editableColumns = ["transactionNumber", "contractor", "transactionKind",
        "transactionDate", "transactionEndDate", "transactionCalcDate", "bonusSize",
        "bonusCurrency", "bonusSum", "minPrice", "maxPrice", "request", "liability"]

// Автозаполняемые атрибуты
@Field
def arithmeticCheckAlias = ["course", "deviationMinPrice", "deviationMaxPrice",
        "income", "outcome"]

// Обязательно заполняемые атрибуты
@Field
def nonEmptyColumns = ["transactionNumber", "contractor", "transactionKind",
        "transactionDate", "transactionEndDate"]
@Field
def nonEmptyColumnsPt2 = ["bonusSize", "bonusCurrency", "bonusSum", "course", "minPrice", "maxPrice",
        "deviationMinPrice", "deviationMaxPrice"]

//// Обертки методов
// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
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
    if (!fileName.endsWith('.xls')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }
    return xml
}

//// Кастомные методы

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = null
    def i = 0
    for (def DataRow row : dataRows){
        if (row?.getAlias()?.contains('itg')) {
            totalRow = row
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // Поля 1-4 обязательны для заполнения. Если заполнено 5, то 6-11.2 тоже обязательны
        def requiredColumns = row.transactionCalcDate != null ? (nonEmptyColumns + nonEmptyColumnsPt2) : nonEmptyColumns
        checkNonEmptyColumns(row, index, requiredColumns, logger, true)

        if (++i != row.rowNumber) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        def values = [:]
        calc9(row, values)
        calc11(row, values)
        calc13(row, values)

        checkCalc(row, arithmeticCheckAlias, values, logger, true)

        // Проверки соответствия НСИ
        checkNSI(15, row, "bonusCurrency")
    }
    // Проверка итогов
    def totalCorrupt = false
    if(totalRow == null){
        totalCorrupt = true
    } else {
        for(def alias : totalColumns){
            totalCorrupt |= (totalRow[alias] != dataRows.sum{it -> (it.getAlias()==null) ? it[alias]?:0 : 0})
        }
    }
    if (totalCorrupt){
        logger.error("Итоговые значения рассчитаны неверно!")
    }
}

/**
 * Алгоритмы заполнения полей формы
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // Удаление итогов
    deleteAllAliased(dataRows)

    def index = 0

    // Расчет ячеек
    dataRows.each{row->
        row.rowNumber=++index
        calc9(row, row)
        calc11(row, row)
        calc13(row, row)
    }

    // Добавление строки итогов
    def totalRow = formData.createDataRow()
    totalRow.transactionNumber = "Итого"
    totalRow.setAlias('itg')
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    totalColumns.each {
        totalRow[it] = 0
    }
    for(def row : dataRows) {
        if(row?.getAlias()?.contains('itg')){
            continue
        }
        totalColumns.each {
            totalRow[it] += row[it] != null ? row[it] : 0
        }
    }
    dataRows.add(dataRows.size(), totalRow)
    dataRowHelper.save(dataRows)
}

void calc9(def row, def result) {
    if (row.bonusCurrency != null && row.transactionCalcDate != null) {
        def record = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache, 'CODE_NUMBER', "${row.bonusCurrency}",
                row.transactionCalcDate, row.getIndex(), getColumnName(row, 'bonusCurrency'), logger, true)
        result.course = record?.RATE?.numberValue
    }
}

void calc11(def row, def result) {
    if (row.bonusSize == null || row.minPrice == null || row.course == null) {
        result.deviationMinPrice = null
        result.deviationMaxPrice = null
        return
    }
    if (0 < row.bonusSize && row.bonusSize < row.minPrice){
        result.deviationMinPrice = (row.minPrice - row.bonusSize) / row.course
        result.deviationMaxPrice = BigDecimal.ZERO
        return
    }
    if (row.maxPrice == null) {
        result.deviationMinPrice = null
        result.deviationMaxPrice = null
        return
    }
    if (0 < row.bonusSize && row.bonusSize > row.minPrice ||
            0 > row.bonusSize && row.bonusSize > -row.maxPrice){
        result.deviationMinPrice = BigDecimal.ZERO
        result.deviationMaxPrice = BigDecimal.ZERO
        return
    }
    if (0 > row.bonusSize && row.bonusSize < -row.maxPrice){
        result.deviationMaxPrice = (-row.maxPrice - row.bonusSize) * row.course
        result.deviationMinPrice = BigDecimal.ZERO
    }
}

void calc13(def row, def result) {
    if (row.bonusSum == null || row.request == null || row.liability == null){
        result.income = null
        result.outcome = null
        return
    }
    def sum12 = row.request + row.liability
    if (row.bonusSum > 0 && sum12 > 0){
        result.income = row.bonusSum + sum12
    }
    if (row.bonusSum < 0 && sum12 < 0){
        result.outcome = row.bonusSum + sum12
    }
}