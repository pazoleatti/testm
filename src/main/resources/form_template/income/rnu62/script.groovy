package form_template.income.rnu62

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState

import java.math.RoundingMode
import groovy.transform.Field

/**
 * Форма "(РНУ-62) Регистр налогового учёта расходов по дисконтным векселям ОАО «Сбербанк России»"
 * formTemplateId=354
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

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod

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

//// Некастомные методы

/** Получить курс валюты */
BigDecimal getCourse(def row, def date) {
    def currency = row.currencyCode
    def isRuble = isRubleCurrency(currency)
    if (date!=null && currency!=null && !isRuble) {
        def res = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache,
                'CODE_NUMBER', currency, date, row.getIndex(), getColumnName(row, "currencyCode"), logger, false)
        return res?.RATE?.numberValue
    } else if (isRuble){
        return 1;
    } else {
        return null
    }
}

/** Проверка валюты на рубли */
def isRubleCurrency(def currencyCode) {
    return  formDataService.getRefBookValue(15, currencyCode, refBookCache)?.CODE?.stringValue=='810'
}

/** Количество дней в году за который делаем */
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
    def dataRowsPrev
    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def countDaysInYear
    if (!isBalancePeriod()) {
        def formDataPrev = formDataService.getFormDataPrev(formData, formData.departmentId)
        formDataPrev = formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null
        if(formDataPrev==null){
            logger.error("Не найдены экземпляры РНУ-62 за прошлый отчетный период!")
        } else {
            dataRowsPrev = formDataService.getDataRowHelper(formDataPrev)?.allCached
        }
        countDaysInYear = getCountDaysInYear(dFrom)
    }
    // Номер последний строки предыдущей формы
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')
    for (def DataRow row : dataRows){
        if (row?.getAlias()?.contains('itg')) {
            totalRow = row
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        if (row.operationDate != null && (row.operationDate.compareTo(dFrom)<0 || row.operationDate.compareTo(dTo)>0)){
            loggerError(errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }
        if (++i != row.rowNumber) {
            loggerError(errorMsg + 'Нарушена уникальность номера по порядку!')
        }
        if (isZeroEmpty(row.sumStartInCurrency) &&
                isZeroEmpty(row.sumStartInRub) &&
                isZeroEmpty(row.sumEndInCurrency) &&
                isZeroEmpty(row.sumEndInRub) &&
                isZeroEmpty(row.sum)){
            loggerError(errorMsg + "Все суммы по операции нулевые!")
        }
        if (!isBalancePeriod()) {
            def rowPrev = getRowPrev(dataRowsPrev, row)
            if(rowPrev==null){
                logger.error(errorMsg + "Отсутствуют данные в РНУ-62 за предыдущий отчетный период!")
            }
            def values = [:]

            //TODO проверить, в аналитике неадекватно описано
            values.with {
                rateBRBillDate=round(getGraph7(row))
                rateBROperationDate=round(getGraph8(row))
                sumStartInCurrency=round(getGraph14(row, rowPrev))
                sumStartInRub=round(getGraph15(rowPrev))
                sumEndInCurrency=round(getGraph16(row, countDaysInYear))
                sumEndInRub=round(getGraph17(row))
                sum=round(getGraph18(row))
            }
            // Проверяем расчеты для параметров(14-18), не использующих справочники, остальные (7,8) проверяются ниже
            checkCalc(row, arithmeticCheckAliasWithoutNSI, values, logger, true)
        }
        // Проверки соответствия НСИ
        formDataService.checkNSI(15, refBookCache, row, "currencyCode", logger, false)
        if (row.rateBRBillDate != getGraph7(row)){
            logger.warn(errorMsg + "В справочнике \"Курсы валют\" не найдено значение ${row.rateBRBillDate} в поле \"${getColumnName(row, 'rateBRBillDate')}\"!")
        }
        if (row.rateBROperationDate != getGraph8(row)){
            logger.warn(errorMsg + "В справочнике \"Курсы валют\" не найдено значение ${row.rateBROperationDate} в поле \"${getColumnName(row, 'rateBROperationDate')}\"!")
        }
    }
    // Не стал усложнять проверку итогов для одной графы
    if (totalRow == null || (totalRow.sum != dataRows.sum{it -> (it.getAlias()==null) ? it.sum?:0 : 0})){
        loggerError("Итоговые значения рассчитаны неверно!")
    }
}

void calc(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def dataRowsPrev
    if (!isBalancePeriod()) {
        def formDataPrev = formDataService.getFormDataPrev(formData, formData.departmentId)
        formDataPrev = formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null

        if(formDataPrev==null){
            //Прерываем расчет, при проверке сообщение об ошибке выведется
            return
        } else {
            dataRowsPrev = formDataService.getDataRowHelper(formDataPrev)?.allCached
        }
    }

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка
    sortRows(dataRows, sortColumns)

    // Номер последний строки предыдущей формы
    def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')
    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def countDaysInYear = getCountDaysInYear(dFrom)

    if (!isBalancePeriod()) {
        // Расчет ячеек
        dataRows.each{row->
            def rowPrev = getRowPrev(dataRowsPrev, row)
            row.with {
                rowNumber=++index
                rateBRBillDate=round(getGraph7(row))
                rateBROperationDate=round(getGraph8(row))
                sumStartInCurrency=round(getGraph14(row, rowPrev))
                sumStartInRub=round(getGraph15(rowPrev))
                sumEndInCurrency=round(getGraph16(row, countDaysInYear))
                sumEndInRub=round(getGraph17(row))
                sum=round(getGraph18(row))
            }
        }
    } else {
        dataRows.each{row->
            row.rowNumber=++index
        }
    }

    // Добавление строки итогов
    def totalRow = formData.createDataRow()
    totalRow.billNumber = "Итого"
    totalRow.setAlias('itg')
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
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

BigDecimal getGraph16(def row, def countDaysInYear) {
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

def DataRow getRowPrev(def dataRowsPrev, def DataRow row) {
    for(def rowPrev : dataRowsPrev){
        if (rowPrev?.getAlias()==null && row.billNumber!=null && row.billNumber == rowPrev.billNumber){
            return rowPrev
        }
    }
}

BigDecimal round(BigDecimal value, int newScale = 2) {
    return value?.setScale(newScale, RoundingMode.HALF_UP)
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

def loggerError(def msg) {
    if (isBalancePeriod()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}