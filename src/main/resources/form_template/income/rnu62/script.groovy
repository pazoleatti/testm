package form_template.income.rnu62

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Скрипт для РНУ-62
 * Форма "(РНУ-62) Регистр налогового учёта расходов по дисконтным векселям ОАО «Сбербанк России»"
 *
 * TODO походу расчеты еще изменят
 * @author bkinzyabulatov
 *
 * Графа 1  rowNumber           № пп
 * Графа 2  billNumber          Номер векселя
 * Графа 3  creationDate        Дата составления
 * Графа 4  nominal             Номинал
 * Графа 5  sellingPrice        Цена реализации
 * Графа 6  currencyCode        Код валюты - Справочник 15 - 64 атрибут CODE
 * Графа 7  rateBRBillDate      Курс Банка России на дату составления векселя
 * Графа 8  rateBROperationDate Курс Банка России на дату совершения операции
 * Графа 9  paymentTermStart    Дата наступления срока платежа
 * Графа 10 paymentTermEnd      Дата окончания срока платежа
 * Графа 11 interestRate        Процентная ставка
 * Графа 12 operationDate       Дата совершения операции
 * Графа 13 rateWithDiscCoef    Ставка с учётом дисконтирующего коэффициента
 * Графа 14 sumStartInCurrency  Сумма дисконта начисленного на начало отчётного периода в валюте
 * Графа 15 sumStartInRub       Сумма дисконта начисленного на начало отчётного периода в рублях
 * Графа 16 sumEndInCurrency    Сумма дисконта начисленного на конец отчётного периода в валюте
 * Графа 17 sumEndInRub         Сумма дисконта начисленного на конец отчётного периода в рублях
 * Графа 18 sum                 Сумма дисконта начисленного за отчётный период (руб.)
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, arithmeticCheckAlias)
        break
    case FormDataEvent.DELETE_ROW :
        if (!currentDataRow?.getAlias()?.contains('itg')) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
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
// обобщить
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

//Все аттрибуты
@Field
def allColumns = ["rowNumber","billNumber", "creationDate", "nominal", "sellingPrice",
            "currencyCode", "rateBRBillDate", "rateBROperationDate",
            "paymentTermStart", "paymentTermEnd", "interestRate",
            "operationDate", "rateWithDiscCoef", "sumStartInCurrency",
            "sumStartInRub", "sumEndInCurrency", "sumEndInRub", "sum"]

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ["sum"]

// Редактируемые атрибуты
@Field
def editableColumns = ["billNumber", "creationDate", "nominal",
            "sellingPrice", "currencyCode", "rateBRBillDate",
            "rateBROperationDate", "paymentTermStart", "paymentTermEnd",
            "interestRate", "operationDate", "rateWithDiscCoef"]

// Автозаполняемые атрибуты
@Field
def arithmeticCheckAlias = ["rowNumber", "rateBRBillDate", "rateBROperationDate",
        "sumStartInCurrency", "sumStartInRub", "sumEndInCurrency", "sumEndInRub", "sum"]

@Field
def arithmeticCheckAliasWithoutNSI = ["sumStartInCurrency", "sumStartInRub",
        "sumEndInCurrency", "sumEndInRub", "sum"]

@Field
def sortColumns = ["billNumber", "operationDate"]

// Автозаполняемые атрибуты
// TODO уточнить
@Field
def nonEmptyColumns = ["rowNumber", "billNumber", "creationDate", "nominal",
        "sellingPrice", "currencyCode", "rateBRBillDate",
        "rateBROperationDate", "paymentTermStart", "paymentTermEnd",
        "interestRate", "operationDate", "sumEndInCurrency", "sumEndInRub", "sum"]

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

//// Некастомные методы
/**
 * Получить курс валюты
 */
def getCourse(def row, def date) {
    def currency = row.currencyCode
    if (date!=null && currency!=null && !isRubleCurrency(currency)) {
        def res = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache,
                'CODE_NUMBER', currency, date, row.getIndex(), getColumnName(row, "currencyCode"), logger, false)
        return res.RATE.getNumberValue()
    } else if (isRubleCurrency(currency)){
        return 1;
    } else {
        return null
    }
}

/**
 * Проверка валюты на рубли
 */
def isRubleCurrency(def currencyCode) {
    return  refBookService.getStringValue(15,currencyCode,'CODE')=='810'
}

/**
 * Получить форму предыдущего периода
 * @return
 */
def FormData getFormDataPrev() {
    def reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def formDataPrev = reportPeriodPrev? formDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id):null
    if (formDataPrev != null && formDataPrev.state == WorkflowState.ACCEPTED){
        return formDataPrev
    } else {
        return null
    }
}

/**
 * Количество дней в году за который делаем
 * @return
 */
int getCountDaysInYear(def Date date) {
    def calendar = Calendar.getInstance()
    calendar.setTime(date)
    return (new GregorianCalendar()).isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365
}

def boolean isZeroEmpty(def value){
    return value==null || value == 0
}

def int getDiffBetweenYears(def Date dateA, def Date dateB){
    def calendarA = Calendar.getInstance()
    calendarA.setTime(dateA)
    def calendarB = Calendar.getInstance()
    calendarB.setTime(dateB)
    return calendarA.get(Calendar.YEAR) - calendarB.get(Calendar.YEAR)
}

//// Кастомные методы
void logicCheck(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = null
    def formDataPrev = formDataService.getFormDataPrev(formData, formData.departmentId)
    formDataPrev = formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null
    if(formDataPrev==null){
        //TODO получить РНУ-62 за прошлый период
        logger.warn("Не найдены экземпляры РНУ-62 за прошлый отчетный период!")
    }
    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time
    // Номер последний строки предыдущей формы
    def i = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)
    for (def DataRow row : dataRows){
        if (row?.getAlias()?.contains('itg')) {
            totalRow = row
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        if (row.operationDate != null && (row.operationDate.compareTo(dFrom)<0 || row.operationDate.compareTo(dTo)>0)){
            logger.error(errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }
        if (++i != row.rowNumber) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }
        if (isZeroEmpty(row.sumStartInCurrency) &&
                isZeroEmpty(row.sumStartInRub) &&
                isZeroEmpty(row.sumEndInCurrency) &&
                isZeroEmpty(row.sumEndInRub) &&
                isZeroEmpty(row.sum)){
            logger.error(errorMsg + "Все суммы по операции нулевые!")
        }
        def rowPrev = getRowPrev(formDataPrev, row)
        if(rowPrev==null){
            //TODO получить РНУ-62 за прошлый период
            logger.warn(errorMsg + "Отсутствуют данные в РНУ-62 за предыдущий отчетный период!")
        }
        def values = [:]

        //TODO проверить, в аналитике неадекватно описано
        values.with {
            rateBRBillDate=roundTo(getGraph7(row), 2)
            rateBROperationDate=roundTo(getGraph8(row), 2)
            sumStartInCurrency=roundTo(getGraph14(row, rowPrev), 2)
            sumStartInRub=roundTo(getGraph15(rowPrev), 2)
            sumEndInCurrency=roundTo(getGraph16(row, dFrom), 2)
            sumEndInRub=roundTo(getGraph17(row), 2)
            sum=roundTo(getGraph18(row), 2)
        }

        // Проверяем расчеты для параметров(14-18), не использующих справочники, остальные (7,8) проверяются ниже
        checkCalc(row, arithmeticCheckAliasWithoutNSI, values, logger, true)
        // Проверки соответствия НСИ
        checkNSI(15, row, "currencyCode")
        if (row.rateBRBillDate != getGraph7(row)){
            logger.warn(errorMsg + "В справочнике \"Курсы валют\" не найдено значение ${row.rateBRBillDate} в поле \"${getColumnName(row, 'rateBRBillDate')}\"!")
        }
        if (row.rateBROperationDate != getGraph8(row)){
            logger.warn(errorMsg + "В справочнике \"Курсы валют\" не найдено значение ${row.rateBROperationDate} в поле \"${getColumnName(row, 'rateBROperationDate')}\"!")
        }
    }
    // Не стал усложнять проверку итогов для одной графы
    if (totalRow == null || (totalRow.sum != dataRows.sum{it -> (it.getAlias()==null) ? it.sum?:0 : 0})){
        logger.error("Итоговые значения рассчитаны неверно!")
    }
}

void calc(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def formDataPrev = formDataService.getFormDataPrev(formData, formData.departmentId)
    formDataPrev = formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null

    if(reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)!=null && formDataPrev==null){
        //Прерываем расчет, при проверке сообщение выведется
        //logger.error("Не найдены экземпляры РНУ-62 за прошлый отчетный период!")
        return
    }

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка
    sortRows(dataRows, sortColumns)

    // Номер последний строки предыдущей формы
    def index = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)
    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time

    // Расчет ячеек
    dataRows.each{row->
        def rowPrev = getRowPrev(formDataPrev, row)
        row.with {
            rowNumber=++index
            rateBRBillDate=roundTo(getGraph7(row), 2)
            rateBROperationDate=roundTo(getGraph8(row), 2)
            sumStartInCurrency=roundTo(getGraph14(row, rowPrev), 2)
            sumStartInRub=roundTo(getGraph15(rowPrev), 2)
            sumEndInCurrency=roundTo(getGraph16(row, dFrom), 2)
            sumEndInRub=roundTo(getGraph17(row), 2)
            sum=roundTo(getGraph18(row), 2)
        }
    }

    // Добавление строки итогов
    def totalRow = formData.createDataRow()
    totalRow.billNumber = "Итого"
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

BigDecimal getGraph7(def row) {
    return getCourse(row, row.creationDate)
}

BigDecimal getGraph8(def row) {
    return getCourse(row, row.operationDate)
}

BigDecimal getGraph14(def row, def rowPrev) {
    if (row.rateWithDiscCoef!=null){
        return null
    } else {
        if (rowPrev !=null){
            return rowPrev.sumEndInCurrency
        } else {
            return BigDecimal.ZERO
        }
    }
}

BigDecimal getGraph15(def rowPrev) {
    if (rowPrev !=null){
        return rowPrev.sumEndInRub
    } else {
        return BigDecimal.ZERO
    }
}

BigDecimal getGraph16(def row, def dFrom) {
    def tmp
    if (row.operationDate != null && row.paymentTermStart != null && row.operationDate < row.paymentTermStart){
        if (row.nominal != null && row.sellingPrice != null && row.creationDate != null) {
            tmp = (row.nominal - row.sellingPrice)*(row.operationDate - row.creationDate) / (row.paymentTermStart - row.creationDate)
        } else {
            tmp = null
        }
    }
    if (row.operationDate != null && row.paymentTermStart != null && row.operationDate > row.paymentTermStart){
        if (row.nominal != null && row.sellingPrice != null) {
            tmp = row.nominal - row.sellingPrice
        } else {
            tmp = null
        }
    }
    if (row.rateWithDiscCoef != null){
        countDaysInYear = getCountDaysInYear(dFrom)
        if (row.interestRate < row.rateWithDiscCoef){
            tmp = row.sellingPrice * (row.operationDate - row.creationDate) / countDaysInYear * row.interestRate / 100
        } else {
            tmp = row.sellingPrice * (row.operationDate - row.creationDate) / countDaysInYear * row.rateWithDiscCoef / 100
        }
    }

    if (row.creationDate != null && row.paymentTermStart != null && (getCountDaysInYear(row.creationDate) - getCountDaysInYear(row.paymentTermStart) != 0)){
        //TODO заполняется вручную, но возможна формула
        tmp = row.sumEndInCurrency
    }

    if (row.paymentTermEnd != null && row.operationDate != null && getDiffBetweenYears(row.paymentTermEnd, row.operationDate)>=3){
        tmp = BigDecimal.ZERO
    }
    if(!isRubleCurrency(row.currencyCode)){
        tmp = null
    }
    return tmp
}

BigDecimal getGraph17(def row) {
    def tmp
    if(row.rateWithDiscCoef != null &&
        row.sumStartInCurrency != null &&
        row.sumEndInCurrency != null){
        if (row.operationDate != null && row.paymentTermStart != null) {
            if(row.operationDate >= row.paymentTermStart){
                if (row.nominal != null && row.rateBROperationDate != null && row.sellingPrice != null && row.rateBRBillDate != null) {
                    tmp = (row.nominal * row.rateBROperationDate)-(row.sellingPrice * row.rateBRBillDate)
                } else {
                    tmp = null
                }
            } else {
               //TODO "второй строкой"?
                if (row.sumStartInRub != null && row.rateBROperationDate != null && row.sellingPrice != null && row.rateBRBillDate != null) {
                    tmp = (row.sellingPrice * (row.rateBROperationDate - row.rateBRBillDate)) + row.sumStartInRub
                } else {
                    tmp = null
                }
            }
        } else {
            return null
        }
    } else {
        tmp = (row.sumEndInCurrency != null && row.rateBROperationDate != null) ? (row.sumEndInCurrency * row.rateBROperationDate) : null//TODO check
    }
    return tmp
}

BigDecimal getGraph18(def row) {
    return (row.sumEndInRub != null && row.sellingPrice != null) ? (row.sumEndInRub - row.sellingPrice) : null //TODO check
}

def DataRow getRowPrev(def formDataPrev, def DataRow row) {
    if (formDataPrev != null) {
        def dataPrev = formDataService.getDataRowHelper(formDataPrev)
        for(def rowPrev : dataPrev.allCached){
            if (!row?.getAlias()?.contains('itg') && row.billNumber!=null && row.billNumber == rowPrev.billNumber){
                return row
            }
        }
    }
    return null
}

BigDecimal roundTo(BigDecimal value, int newScale) {
    return value?.setScale(newScale, BigDecimal.ROUND_HALF_UP)
}

