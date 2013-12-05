package form_template.income.rnu57

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * (РНУ-57) Регистр налогового учёта финансового результата от реализации (погашения) векселей сторонних эмитентов
 * formTemplateId=353
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        prevPeriodCheck()
        calc()
        logicCheck()
        break
}

// 1    number                  № пп
// 2    bill                    Вексель
// 3    purchaseDate            Дата приобретения
// 4    purchasePrice           Цена приобретения, руб.
// 5    purchaseOutcome         Расходы, связанные с приобретением,  руб.
// 6    implementationDate      Дата реализации (погашения)
// 7    implementationPrice     Цена реализации (погашения), руб.
// 8    implementationOutcome   Расходы, связанные с реализацией,  руб.
// 9    price                   Расчётная цена, руб.
// 10   percent                 Процентный доход, учтённый в целях налогообложения  (для дисконтных векселей), руб.
// 11   implementationpPriceTax Цена реализации (погашения) для целей налогообложения
//                                                              (для дисконтных векселей без процентного дохода),  руб.
// 12   allIncome               Всего расходы по реализации (погашению), руб.
// 13   implementationPriceUp   Превышение цены реализации для целей налогообложения над ценой реализации, руб.
// 14   income                  Прибыль (убыток) от реализации (погашения) руб.

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def editableColumns = ['bill', 'purchaseDate', 'implementationDate', 'implementationPrice', 'implementationOutcome']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'bill', 'purchaseDate', 'purchasePrice', 'purchaseOutcome', 'implementationDate',
        'implementationPrice', 'implementationOutcome', 'price', 'percent', 'implementationpPriceTax', 'allIncome',
        'implementationPriceUp', 'income']

// Атрибуты для итогов
@Field
def totalColumns = ['purchaseOutcome', 'implementationOutcome', 'percent', 'implementationpPriceTax', 'allIncome',
        'implementationPriceUp']

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String columnName,
              def Date date, boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date,
            rowIndex, columnName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    if (getRNU(348) == null) {
        throw new ServiceException(" Не найдены экземпляры «РНУ-55» за текущий отчетный период!")
    }
    if (getRNU(349) == null) {
        throw new ServiceException(" Не найдены экземпляры «РНУ-56» за текущий отчетный период!")
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {
        // Удаление подитогов
        deleteAllAliased(dataRows)
        // сортируем
        dataRowHelper.save(dataRows.sort { getKnu(it.bill) })

        def rnu55DataRows = getRNU(348)
        def rnu56DataRows = getRNU(349)
        // номер последний строки предыдущей формы
        def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
        for (row in dataRows) {
            // графа 1
            row.number = ++index

            def rnu55Row = getRnuSourceRow(rnu55DataRows, row)
            def rnu56Row = getRnuSourceRow(rnu56DataRows, row)

            // графа 4
            row.purchasePrice = calc4(rnu55Row != null ? rnu55Row : rnu56Row)
            // графа 5
            row.purchaseOutcome = calc5(rnu55Row != null ? rnu55Row : rnu56Row)
            // графа 9
            row.price = calc9(row, rnu55Row, rnu56Row)
            // графа 10
            row.percent = calc10(rnu56Row, rnu56DataRows)
            // графа 11
            row.implementationpPriceTax = calc11(row, rnu55Row, rnu56Row)
            // графа 12
            row.allIncome = calc12(row)
            // графа 13
            row.implementationPriceUp = calc13(row)
            // графа 14
            row.income = calc14(row)
        }
        dataRowHelper.save(dataRows)
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size + 1)
    dataRowHelper.save(dataRows)
}

def getRNU(def id) {
    def sourceFormData = formDataService.find(id, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (sourceFormData == null)
        return null
    return formDataService.getDataRowHelper(sourceFormData).getAllCached()
}

def getRnuSourceRow(def rnuSourceDataRows, DataRow row) {
    for (def sourceRow : rnuSourceDataRows) {
        if (sourceRow.bill == row.bill) {
            return sourceRow
        }
    }
    return null
}

def BigDecimal calc4(DataRow sourceRow) {
    if (sourceRow == null || sourceRow.nominal == null) {
        return null
    }
    def currencyRate = getCurrencyRate(sourceRow.currency, sourceRow.buyDate)
    if (currencyRate != null) {
        return sourceRow.nominal * currencyRate
    }
    return null
}

def BigDecimal calc5(DataRow sourceRow) {
    if (sourceRow != null) {
        return sourceRow.sumIncomeinRuble
    }
    return null
}

def BigDecimal calc9(DataRow row, DataRow rnu55DataRow, DataRow rnu56DataRow) {
    if (rnu56DataRow != null) {
        if (rnu56DataRow.maturity == null || row.implementationDate == null) {
            return null
        }
        if (rnu56DataRow.maturity == row.implementationDate) {
            return 0
        }
        if (rnu56DataRow.maturity > row.implementationDate) {
            def currencyRate = getCurrencyRate(rnu56DataRow.currency, row.implementationDate)
            if (rnu56DataRow.nominal == null || rnu56DataRow.price == null || rnu56DataRow.buyDate == null
                    || row.implementationDate == null || currencyRate == null) {
                return null
            }
            def n = rnu56DataRow.nominal
            def k = rnu56DataRow.price
            def t = rnu56DataRow.maturity - rnu56DataRow.buyDate
            def d = row.implementationDate - rnu56DataRow.buyDate
            return ((n - k) / t * d + k) * currencyRate
        }
    }
    if (rnu55DataRow != null) {
        def currencyRate = getCurrencyRate(rnu55DataRow.currency, row.implementationDate)
        if (rnu55DataRow.nominal != null && currencyRate != null) {
            return rnu55DataRow.nominal * currencyRate
        }
    }
}

def BigDecimal calc10(DataRow rnu56DataRow, def rnu56DataRows) {
    if (rnu56DataRow != null) {
        if (rnu56DataRow.sum != null) {
            return rnu56DataRow.discountInRub
        }
        if (rnu56DataRow.bill == null) {
            def rnu56TotalDataRow = data56aRowHelper.getDataRow(rnu56DataRows, 'total')
            if (rnu56TotalDataRow != null) {
                return rnu56TotalDataRow.sumIncomeinRuble
            }
        }
    }
    return null
}

def BigDecimal calc11(DataRow row, DataRow rnu55DataRow, DataRow rnu56DataRow) {
    def retVal = null
    if (row.price != null) {
        final def tmpValue = 0.8 * row.price
        if (rnu55DataRow != null || rnu56DataRow != null) {
            if (row.implementationPrice != null && row.implementationPrice >= tmpValue) {
                retVal = row.implementationPrice
            } else {
                retVal = tmpValue
            }
        }
        if (rnu56DataRow != null && row.percent != null) {
            retVal -= row.percent
        }
    }
    return retVal
}

def BigDecimal calc12(DataRow row) {
    if (row.purchasePrice == null || row.purchaseOutcome == null || row.implementationOutcome == null) {
        return null
    }
    return row.purchasePrice + row.purchaseOutcome + row.implementationOutcome
}

def BigDecimal calc13(def row) {
    if (row.implementationpPriceTax != null && row.implementationPrice != null ) {
        def tmpOne = row.implementationpPriceTax - row.implementationPrice
        if (row.percent == 0) {
            return tmpOne
        }
        if (row.percent!=null && row.percent > 0 ) {
            def tmpTwo = row.implementationpPriceTax + row.percent - row.implementationPrice
            if (tmpOne < 0 || tmpTwo < 0) {
                return 0
            } else {
                return tmpTwo
            }
        }
    }
    return null
}

def BigDecimal calc14(def row) {
    if (row.implementationpPriceTax == null || row.allIncome == null ) {
        return null
    }
    return row.implementationpPriceTax - row.allIncome
}

def getCurrencyRate(def currency, def date) {
    if (currency == null || date == null) {
        return null
    }
    if (!isRubleCurrency(currency)) {
        def res = refBookFactory.getDataProvider(22).getRecords(date, null, 'CODE_NUMBER=' + currency, null);
        return (res.getRecords().isEmpty() ? null : res.getRecords().get(0).RATE.getNumberValue())
    } else {
        return 1;
    }
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    return currencyCode != null ? (getRefBookValue(15, currencyCode)?.CODE.stringValue == '810') : false
}

// Логические проверки
boolean logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    def rnu55DataRows = getRNU(348)
    def rnu56DataRows = getRNU(349)

    // алиасы графов для арифметической проверки
    def arithmeticCheckAlias = ['purchasePrice', 'purchaseOutcome', 'price', 'percent', 'implementationpPriceTax',
            'allIncome', 'implementationPriceUp', 'income']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка даты приобретения и границ отчетного периода
        if (row.purchaseDate != null && row.purchaseDate.compareTo(endDate) > 0) {
            logger.error(errorMsg + 'Дата приобретения вне границ отчетного периода!')
        }

        // 3. Проверка даты реализации (погашения) и границ отчетного периода
        if (row.implementationDate != null && (row.implementationDate.compareTo(startDate) < 0 || row.implementationDate.compareTo(endDate) > 0)) {
            logger.error(errorMsg + 'Дата реализации (погашения) вне границ отчетного периода!')
        }

        // 4. Проверка на уникальность поля «№ пп»
        if (++i != row.number) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        def rnu55Row = getRnuSourceRow(rnu55DataRows, row)
        def rnu56Row = getRnuSourceRow(rnu56DataRows, row)

        // 7. Проверка существования необходимых экземпляров форм
        if (rnu55Row == null && rnu56Row == null) {
            logger.error(errorMsg + 'Отсутствуют данные в РНУ-55 (РНУ-56)!')
        } else {
            // 5. Арифметическая проверка граф 4, 5, 9-14
            needValue['purchasePrice'] = calc4(rnu55Row != null ? rnu55Row : rnu56Row)
            needValue['purchaseOutcome'] = calc5(rnu55Row != null ? rnu55Row : rnu56Row)
            needValue['price'] = calc9(row, rnu55Row, rnu56Row)
            needValue['percent'] = calc10(rnu56Row, rnu56DataRows)
            needValue['implementationpPriceTax'] = calc11(row, rnu55Row, rnu56Row)
            needValue['allIncome'] = calc12(row)
            needValue['implementationPriceUp'] = calc13(row)
            needValue['income'] = calc14(row)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
        }
    }

    // 6. Проверка итоговых значений по всей форме
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def calcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.bill = 'Итого'
    nonEmptyColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
}