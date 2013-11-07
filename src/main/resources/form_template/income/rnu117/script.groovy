package form_template.income.rnu117

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Форма "(РНУ-117) Регистр налогового учёта доходов и расходов, по операциям со сделками форвард, квалифицированным в качестве операций с ФИСС для целей налогообложения"
 *
 * @author bkinzyabulatov
 * TODO заполнение граф 7.1, 7.2 - вручную или автоматом
 * TODO 7.1 - справочник форвардных курсов?
 *
 * Графа 1    rowNumber           № пп
 * Графа 2.1  transactionNumber   Общая информация о сделке. Номер сделки
 * Графа 2.2  transactionKind     Общая информация о сделке. Вид сделки
 * Графа 2.3  contractor          Общая информация о сделке. Наименование контрагента
 * Графа 3    transactionDate     Дата заключения сделки
 * Графа 4    transactionEndDate  Дата окончания сделки
 * Графа 5    resolveDate         Дата фактического исполнения сделки
 * Графа 6    transactionType     Тип сделки Справочник 16 Аттрибут 70 TYPE
 * Графа 7.1  courseFix           Цена сделки. Форвардный курс (цена)/ фиксированная ставка
 * Графа 7.2  course              Цена сделки. Курс Банка России/ обменный курс/ плавающая ставка
 * Графа 8.1  minPrice            Максимальная/ минимальная расчетная стоимость сделки для целей налогообложения на  дату заключения. Минимальная расчетная стоимость
 * Графа 8.2  maxPrice            Максимальная/ минимальная расчетная стоимость сделки для целей налогообложения на  дату заключения. Максимальная расчетная стоимость
 * Графа 9.1  request             Требования (+)/ обязательства (-) по сделке, руб. Требования
 * Графа 9.2  liability           Требования (+)/ обязательства (-) по сделке, руб. Обязательства
 * Графа 10.1 income              Доходы/ расходы, учитываемые в целях налога на прибыль по сделке, руб. Доходы
 * Графа 10.2 outcome             Доходы/ расходы, учитываемые в целях налога на прибыль по сделке, руб. Расходы
 * Графа 11.1 deviationMinPrice   Сумма отклонения от расчетной стоимости сделки более чем на 20%. Сумма отклонения от минимальной (расчетной) цены, руб.
 * Графа 11.2 deviationMaxPrice   Сумма отклонения от расчетной стоимости сделки более чем на 20%. Сумма отклонения от максимальной (расчетной) цены, руб.
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

//Все аттрибуты
@Field
def allColumns = ["rowNumber", "transactionNumber", "transactionKind", "contractor", "transactionDate",
        "transactionEndDate", "resolveDate", "transactionType", "courseFix", "course", "minPrice", "maxPrice",
        "request", "liability", "income", "outcome", "deviationMinPrice", "deviationMaxPrice"]

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ["request", "liability", "income", "outcome", "deviationMinPrice", "deviationMaxPrice"]

// Редактируемые атрибуты
@Field
def editableColumns = ["transactionNumber", "transactionKind", "contractor", "transactionDate",
        "transactionEndDate", "resolveDate", "transactionType", "courseFix", "course", "minPrice", "maxPrice", "request", "liability"]

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ["rowNumber", "income", "outcome", "deviationMinPrice", "deviationMaxPrice"]

// Обязательно заполняемые атрибуты
@Field
def nonEmptyColumns = ["rowNumber", "transactionNumber", "transactionKind", "contractor", "transactionDate",
        "transactionEndDate", "resolveDate", "transactionType", "courseFix", "course", "minPrice", "maxPrice",
        "request", "liability", "income", "outcome", "deviationMinPrice", "deviationMaxPrice"]

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
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

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def numbers = []
    def totalRow = null
    def rowNum = 0
    for (def DataRow row : dataRows){
        if (row?.getAlias()?.contains('itg')) {
            totalRow = row
            continue
        }

        rowNum++
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, false)

        def rowStart
        def index = row.rowNumber
        if (index != null) {
            rowStart = "В строке \"№ пп\" равной $index "
        } else {
            index = dataRows.indexOf(row) + 1
            rowStart = "В строке $index "
        }

        if (row.rowNumber in numbers){
            logger.error("${rowStart}нарушена уникальность номера по порядку ${row.rowNumber}!")
        }else {
            numbers += row.rowNumber
        }
        def values = getValues(dataRows, row, null)
        for (def colName : autoFillColumns) {
            if (row[colName] != values[colName]){
                isValid = false
                logger.error("${rowStart}неверно рассчитана графа \"${getColumnName(row,colName)}\"!")
            }
        }
        // Проверки соответствия НСИ
        checkNSI(16, row, "transactionType")
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

    // Расчет ячеек
    dataRows.each{row->
        getValues(dataRows, row, row)
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

/**
 * получаем мапу со значениями, расчитанными для каждой конкретной строки или сразу записываем в строку (для расчетов)
 */
def getValues(def dataRows, def row, def result) {
    if(result == null){
        result = [:]
    }

    setGraph1(dataRows, row, result)
    setGraph10(row, result)
    setGraph11(row, result)

    return result
}

void setGraph1(def dataRows, def row, def result) {
    result.rowNumber = dataRows.indexOf(row) + 1
}

void setGraph10(def row, def result) {
    switch (row.transactionKind){
        case "DF FX":
        case "DF PM":
        case "NDF PM":
            def sum9 = row.request + row.liability
            if (sum9 > 0){
                result.income = sum9
                result.outcome = 0
            } else {
                result.income = 0
                result.outcome = sum9
            }
            break
        case "NDF FX":
        case "FRA":
            if (row.request != null){
                result.income = row.request
                result.outcome = 0
            } else if (row.liability != null) {
                result.income = 0
                result.outcome = row.liability
            }
            break
        default:
            result.income = null
            result.outcome = null
    }
}

void setGraph11(def row, def result) {
    def graph6 = getRefBookValue(16, row.transactionType)?.TYPE?.stringValue
    switch (row.transactionKind){
        case "DF FX":
        case "NDF FX":
            if (row.minPrice <= row.courseFix && row.courseFix <= row.maxPrice ||
                    "Покупка".equals(graph6) && row.courseFix <= row.maxPrice ||
                    "Продажа".equals(graph6) && row.courseFix >= row.maxPrice){
                result.deviationMinPrice = 0
                result.deviationMaxPrice = 0
            }
            def sum10 = row.income + row.outcome
            if ("Покупка".equals(graph6) && row.courseFix > row.maxPrice){
                result.deviationMinPrice = 0
                //«Графа 11.2» = («Графа.10.1» + «Графа 10.2») х («Графа 7.2» -«Графа 8.2») / («Графа 7.2» -«Графа 7.1») – («Графа 10.1» + «Графа 10.2»)
                result.deviationMaxPrice = sum10 * (row.course - row.maxPrice) / (row.course - row.courseFix) - sum10
            }
            if ("Продажа".equals(graph6) && row.courseFix < row.maxPrice){
                //«Графа 11.1» = («Графа.10.1» + «Графа 10.2») х («Графа 8.1» -»Графа 7.2») / («Графа 7.1» - «Графа 7.2») - («Графа.10.1» + «Графа 10.2»)
                result.deviationMinPrice = sum10 * (row.minPrice - row.course) / (row.courseFix - row.course) - sum10
                result.deviationMaxPrice = 0
            }
            break
        case "FRA":
            if (row.minPrice <= row.courseFix && row.courseFix <= row.maxPrice ||
                    "Покупка".equals(graph6) && row.courseFix <= row.minPrice ||//различие от пред
                    "Продажа".equals(graph6) && row.courseFix >= row.maxPrice){
                result.deviationMinPrice = 0
                result.deviationMaxPrice = 0
            }
            def sum10 = row.income + row.outcome
            if ("Покупка".equals(graph6) && row.courseFix > row.maxPrice){
                result.deviationMinPrice = 0
                //«Графа 11.2» = («Графа.10.1» + «Графа 10.2») х («Графа 7.2» -«Графа 8.2») / («Графа 7.2» -«Графа 7.1») – («Графа 10.1» + «Графа 10.2»)
                result.deviationMaxPrice = sum10 * (row.course - row.maxPrice) / (row.course - row.courseFix) - sum10
            }
            if ("Продажа".equals(graph6) && row.courseFix < row.minPrice){//различие от пред
                //«Графа 11.1» = («Графа.10.1» + «Графа 10.2») х («Графа 8.1» -»Графа 7.2») / («Графа 7.1» - «Графа 7.2») - («Графа.10.1» + «Графа 10.2»)
                result.deviationMinPrice = sum10 * (row.minPrice - row.course) / (row.courseFix - row.course) - sum10
                result.deviationMaxPrice = 0
            }
            break
        case "DF PM":
        case "NDF PM":
            if (row.minPrice <= row.courseFix && row.courseFix <= row.maxPrice ||
                    "Покупка".equals(graph6) && row.courseFix <= row.minPrice ||//различие от пред
                    "Продажа".equals(graph6) && row.courseFix >= row.maxPrice){
                result.deviationMinPrice = 0
                result.deviationMaxPrice = 0
            }
            def sum10 = row.income + row.outcome
            if ("Покупка".equals(graph6) && row.courseFix > row.maxPrice){
                result.deviationMinPrice = 0
                //«Графа 11.2» = («Графа.10.1» + «Графа 10.2») х («Графа 7.2» -«Графа 8.2») / («Графа 7.2» -«Графа 7.1») – («Графа 10.1» + «Графа 10.2»)
                result.deviationMaxPrice = sum10 * (row.course - row.maxPrice) / (row.course - row.courseFix) - sum10
            }
            if ("Продажа".equals(graph6) && row.courseFix < row.minPrice){//различие от пред
                //«Графа 11.1» = («Графа.10.1» + «Графа 10.2») х («Графа 8.1» -»Графа 7.2») / («Графа 7.1» - «Графа 7.2») - («Графа.10.1» + «Графа 10.2»)
                result.deviationMinPrice = sum10 * (row.minPrice - row.course) / (row.courseFix - row.course) - sum10
                result.deviationMaxPrice = 0
            }
            break
        default:
            result.deviationMinPrice = null
            result.deviationMaxPrice = null
    }
}