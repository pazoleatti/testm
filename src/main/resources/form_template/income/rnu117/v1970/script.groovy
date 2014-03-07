package form_template.income.rnu117.v1970

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Форма "(РНУ-117) Регистр налогового учёта доходов и расходов, по операциям со сделками форвард, квалифицированным в качестве операций с ФИСС для целей налогообложения"
 * formTemplateId=370
 *
 * @author bkinzyabulatov
 *
 * fix
 * Графа 1    rowNumber           № пп
 * Графа 2.1  transactionNumber   Общая информация о сделке. Номер сделки
 * Графа 2.2  transactionKind     Общая информация о сделке. Вид сделки 91 справочник 831 атрибут KIND
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

// Все аттрибуты
@Field
def allColumns = ["fix", "rowNumber", "transactionNumber", "transactionKind", "contractor", "transactionDate",
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
def arithmeticCheckAlias = ["income", "outcome", "deviationMinPrice", "deviationMaxPrice"]

// Обязательно заполняемые атрибуты
@Field
def nonEmptyColumns = ["rowNumber", "transactionNumber", "transactionKind", "contractor", "transactionDate",
        "transactionEndDate", "resolveDate", "transactionType", "courseFix", "course", "minPrice", "maxPrice",
        "request", "liability", "income", "outcome", "deviationMinPrice", "deviationMaxPrice"]

@Field
def sdf = new SimpleDateFormat('dd.MM.yyyy')

//// Обертки методов
// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
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

        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)


        if (++i != row.rowNumber) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        def values = [:]

        calc10(row, values)
        calc11(row, values)

        // Проверка курса валюты	(графа 7.2)
        if (row.resolveDate != null && row.course != null &&
                getRecord(22, "RATE = $row.course", row.resolveDate) == null) {
            RefBook rb = refBookFactory.get(22)
            def rbName = rb.getName()
            def attrName = rb.getAttribute('RATE').getName()
            logger.error(errorMsg + "В справочнике «$rbName» не найдено значение «${row.course}», соответствующее атрибуту «$attrName»!")
        }

        checkCalc(row, arithmeticCheckAlias, values, logger, true)
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

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // Удаление итогов
    deleteAllAliased(dataRows)

    def index = 0
    // Расчет ячеек
    dataRows.each{row->
        row.rowNumber=++index
        calc10(row, row)
        calc11(row, row)
    }

    // Добавление строки итогов
    def totalRow = formData.createDataRow()
    totalRow.fix = "Итого"
    totalRow.getCell("fix").setColSpan(2)
    totalRow.setAlias('itg')
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

void calc10(def row, def result) {
    def kind = getRefBookValue(91, row.transactionKind)?.KIND?.stringValue
    if(kind == null){
        return
    }
    switch (kind){
        case "DF FX":
        case "DF PM":
        case "NDF PM":
            def sum9 = row.request + row.liability
            if (sum9 > 0){
                result.income = sum9
                result.outcome = BigDecimal.ZERO
            } else {
                result.income = BigDecimal.ZERO
                result.outcome = sum9
            }
            break
        case "NDF FX":
        case "FRA":
            if (row.request != null){
                result.income = row.request
                result.outcome = BigDecimal.ZERO
            } else if (row.liability != null) {
                result.income = BigDecimal.ZERO
                result.outcome = row.liability
            }
            break
        default:
            result.income = null
            result.outcome = null
    }
}

void calc11(def row, def result) {
    def kind = getRefBookValue(91, row.transactionKind)?.KIND?.stringValue
    if (kind == null){
        return
    }
    def graph6 = getRefBookValue(16, row.transactionType)?.TYPE?.stringValue
    switch (kind){
        case "DF FX":
        case "NDF FX":
            if (row.minPrice <= row.courseFix && row.courseFix <= row.maxPrice ||
                    "Покупка".equals(graph6) && row.courseFix <= row.maxPrice ||
                    "Продажа".equals(graph6) && row.courseFix >= row.maxPrice){
                result.deviationMinPrice = BigDecimal.ZERO
                result.deviationMaxPrice = BigDecimal.ZERO
            }
            def sum10 = row.income + row.outcome
            if ("Покупка".equals(graph6) && row.courseFix > row.maxPrice){
                result.deviationMinPrice = BigDecimal.ZERO
                //«Графа 11.2» = («Графа.10.1» + «Графа 10.2») х («Графа 7.2» -«Графа 8.2») / («Графа 7.2» -«Графа 7.1») – («Графа 10.1» + «Графа 10.2»)
                result.deviationMaxPrice = sum10 * (row.course - row.maxPrice) / (row.course - row.courseFix) - sum10
            }
            if ("Продажа".equals(graph6) && row.courseFix < row.maxPrice){
                //«Графа 11.1» = («Графа.10.1» + «Графа 10.2») х («Графа 8.1» -»Графа 7.2») / («Графа 7.1» - «Графа 7.2») - («Графа.10.1» + «Графа 10.2»)
                result.deviationMinPrice = sum10 * (row.minPrice - row.course) / (row.courseFix - row.course) - sum10
                result.deviationMaxPrice = BigDecimal.ZERO
            }
            break
        case "FRA":
            if (row.minPrice <= row.courseFix && row.courseFix <= row.maxPrice ||
                    "Покупка".equals(graph6) && row.courseFix <= row.minPrice ||//различие от пред
                    "Продажа".equals(graph6) && row.courseFix >= row.maxPrice){
                result.deviationMinPrice = BigDecimal.ZERO
                result.deviationMaxPrice = BigDecimal.ZERO
            }
            def sum10 = row.income + row.outcome
            if ("Покупка".equals(graph6) && row.courseFix > row.maxPrice){
                result.deviationMinPrice = BigDecimal.ZERO
                //«Графа 11.2» = («Графа.10.1» + «Графа 10.2») х («Графа 7.2» -«Графа 8.2») / («Графа 7.2» -«Графа 7.1») – («Графа 10.1» + «Графа 10.2»)
                result.deviationMaxPrice = sum10 * (row.course - row.maxPrice) / (row.course - row.courseFix) - sum10
            }
            if ("Продажа".equals(graph6) && row.courseFix < row.minPrice){//различие от пред
                //«Графа 11.1» = («Графа.10.1» + «Графа 10.2») х («Графа 8.1» -»Графа 7.2») / («Графа 7.1» - «Графа 7.2») - («Графа.10.1» + «Графа 10.2»)
                result.deviationMinPrice = sum10 * (row.minPrice - row.course) / (row.courseFix - row.course) - sum10
                result.deviationMaxPrice = BigDecimal.ZERO
            }
            break
        case "DF PM":
        case "NDF PM":
            if (row.minPrice <= row.courseFix && row.courseFix <= row.maxPrice ||
                    "Покупка".equals(graph6) && row.courseFix <= row.minPrice ||//различие от пред
                    "Продажа".equals(graph6) && row.courseFix >= row.maxPrice){
                result.deviationMinPrice = BigDecimal.ZERO
                result.deviationMaxPrice = BigDecimal.ZERO
            }
            def sum10 = row.income + row.outcome
            if ("Покупка".equals(graph6) && row.courseFix > row.maxPrice){
                result.deviationMinPrice = BigDecimal.ZERO
                //«Графа 11.2» = («Графа.10.1» + «Графа 10.2») х («Графа 7.2» -«Графа 8.2») / («Графа 7.2» -«Графа 7.1») – («Графа 10.1» + «Графа 10.2»)
                result.deviationMaxPrice = sum10 * (row.course - row.maxPrice) / (row.course - row.courseFix) - sum10
            }
            if ("Продажа".equals(graph6) && row.courseFix < row.minPrice){//различие от пред
                //«Графа 11.1» = («Графа.10.1» + «Графа 10.2») х («Графа 8.1» -»Графа 7.2») / («Графа 7.1» - «Графа 7.2») - («Графа.10.1» + «Графа 10.2»)
                result.deviationMinPrice = sum10 * (row.minPrice - row.course) / (row.courseFix - row.course) - sum10
                result.deviationMaxPrice = BigDecimal.ZERO
            }
            break
        default:
            result.deviationMinPrice = null
            result.deviationMaxPrice = null
    }
}

/**
 * Аналог FormDataServiceImpl.getRefBookRecord(...) но ожидающий получения из справочника больше одной записи.
 *
 * @return первая из найденных записей
 */
def getRecord(def refBookId, def filter, Date date) {
    if (refBookId == null) {
        return null
    }
    String dateStr = sdf.format(date)
    if (recordCache.containsKey(refBookId)) {
        Long recordId = recordCache.get(refBookId).get(dateStr + filter)
        if (recordId != null) {
            if (refBookCache != null) {
                return refBookCache.get(recordId)
            } else {
                def retVal = new HashMap<String, RefBookValue>()
                retVal.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId))
                return retVal
            }
        }
    } else {
        recordCache.put(refBookId, [:])
    }

    def provider
    if (!providerCache.containsKey(refBookId)) {
        providerCache.put(refBookId, refBookFactory.getDataProvider(refBookId))
    }
    provider = providerCache.get(refBookId)

    def records = provider.getRecords(date, null, filter, null)
    // отличие от FormDataServiceImpl.getRefBookRecord(...)
    if (records.size() > 0) {
        def retVal = records.get(0)
        Long recordId = retVal.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
        recordCache.get(refBookId).put(dateStr + filter, recordId)
        if (refBookCache != null)
            refBookCache.put(recordId, retVal)
        return retVal
    }
    return null
}