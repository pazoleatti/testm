package form_template.income.rnu_116.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * РНУ 116. Регистр налогового учёта доходов и расходов, возникающих в связи с применением в конверсионных сделках с Взаимозависимыми
 * лицами и резидентами оффшорных зон курса, не соответствующих рыночному уровню
 *
 * formTemplateId = 844
 *
 * @author Stanislav Yasinskiy
 */
// fix					() -
// rowNumber			(1) -
// dealNum				(2) - Номер сделки
// dealType				(3) - Вид сделки
// dealDate				(4) - Дата заключения сделки
// dealDoneDate			(5) - Дата окончания сделки
// iksr					(6) - Идентификационный номер
// countryName			(7) - Страна местоположения взаимозависимого лица (резидента оффшорной зоны)
// name					(8) - Контрагент
// dealFocus			(9) - Тип сделки
// reqCurCode			(10) - Код валюты (драгоценных металлов) по сделке приобретения (требования)
// reqVolume			(11) - Объем покупаемой валюты / драгоценных металлов (в граммах)
// guarCurCode			(12) - Код валюты (драгоценных металлов) по сделке продажи (обязательства)
// guarVolume			(13) - Объем продаваемой валюты / драгоценных металлов (в граммах)
// price				(14) - Цена сделки
// reqCourse			(15) - Курс Банка России на дату исполнения (досрочного исполнения) сделки, руб. В отношении требования
// guarCourse			(16) - Курс Банка России на дату исполнения (досрочного исполнения) сделки, руб. В отношении обязательства
// reqSum				(17) - Требования (обязательства) по сделке, руб. Требования
// guarSum				(18) - Требования (обязательства) по сделке, руб. Обязательства
// incomeSum			(19) - Доходы
// outcomeSum			(20) - Расходы
// marketPrice			(21) - Рыночная цена сделки
// incomeDelta			(22) - Отклонения по доходам, в руб
// outcomeDelta			(23) - Отклонения по расходам, в руб

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def allColumns = ['fix', 'rowNumber', 'dealNum', 'dealType', 'dealDate', 'dealDoneDate', 'iksr', 'countryName', 'name',
                  'dealFocus', 'reqCurCode', 'reqVolume', 'guarCurCode', 'guarVolume', 'price', 'reqCourse', 'guarCourse',
                  'reqSum', 'guarSum', 'incomeSum', 'outcomeSum', 'marketPrice', 'incomeDelta', 'outcomeDelta']

// Редактируемые атрибуты
@Field
def editableColumns = ['dealNum', 'dealType', 'dealDate', 'dealDoneDate', 'name', 'dealFocus', 'reqCurCode', 'reqVolume',
                       'guarCurCode', 'guarVolume', 'price', 'reqCourse', 'guarCourse', 'reqSum', 'guarSum', 'marketPrice']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['iksr', 'countryName', 'incomeSum', 'outcomeSum', 'incomeDelta', 'outcomeDelta']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['dealNum', 'dealType', 'dealDate', 'dealDoneDate', 'name', 'dealFocus', 'reqCurCode', 'reqVolume',
                       'guarCurCode', 'guarVolume', 'price', 'reqCourse', 'guarCourse', 'reqSum', 'guarSum', 'incomeSum',
                       'outcomeSum', 'marketPrice', 'incomeDelta', 'outcomeDelta']

@Field
def totalColumns = ['incomeDelta', 'outcomeDelta']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    dealType1 = getRecordId(92, 'NAME', 'Кассовая сделка')
    dealType2 = getRecordId(92, 'NAME', 'Срочная сделка')
    dealType3 = getRecordId(92, 'NAME', 'Премия по опциону')
    dealType4 = getRecordId(92, 'NAME', 'Промежуточный платёж')
    direction1 = getRecordId(20, 'DIRECTION', 'покупка')
    direction2 = getRecordId(20, 'DIRECTION', 'продажа')

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка даты заключения сделки
        checkDatePeriod(logger, row, 'dealDate', getReportPeriodStartDate(), getReportPeriodEndDate(), true)

        // Проверка даты исполнения сделки
        if (row.dealDate && row.dealDoneDate && row.dealDate > row.dealDoneDate) {
            def msg1 = row.getCell('dealDate').column.name
            def msg2 = row.getCell('dealDoneDate').column.name
            logger.error("Строка $rowNum: Значение графы «$msg2» должно быть не меньше значения графы «$msg1»!")
        }

        // Проверка даты исполнения сделки
        checkDatePeriod(logger, row, 'dealDoneDate', getReportPeriodStartDate(), getReportPeriodEndDate(), true)

        // Проверка объема покупаемой валюты
        if (row.reqVolume != null && row.reqVolume < 0) {
            def msg = row.getCell('reqVolume').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка объема продаваемой валюты
        if (row.guarVolume != null && row.guarVolume < 0) {
            def msg = row.getCell('guarVolume').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка курса сделки
        if (row.price != null && row.price <= 0) {
            def msg = row.getCell('price').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше «0»!")
        }

        // Проверка курса в отношении требования
        if (row.reqCourse != null && row.reqCourse <= 0) {
            def msg = row.getCell('reqCourse').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше «0»!")
        }

        // Проверка курса в отношении обязательства
        if (row.guarCourse != null && row.guarCourse <= 0) {
            def msg = row.getCell('guarCourse').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше «0»!")
        }

        boolean flag = true
        // Проверка суммы требований
        if (row.reqSum != null && row.reqSum < 0) {
            flag = false
            def msg = row.getCell('reqSum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка корректности суммы требований
        if (row.dealType != null && row.reqVolume != null && row.reqCourse != null && row.reqSum != null) {
            if ((row.dealType == dealType1 || row.dealType == dealType2) && row.reqSum != calc17(row)) {
                flag = false
                def msg = row.getCell('reqSum').column.name
                def msg1 = row.getCell('reqVolume').column.name
                def msg2 = row.getCell('reqCourse').column.name
                logger.error("Строка $rowNum: Значение графы «$msg» должно равняться произведению «$msg1» и «$msg2»!")
            }
        }

        // Проверка суммы обязательств
        if (row.guarSum != null && row.guarSum < 0) {
            flag = false
            def msg = row.getCell('guarSum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка корректности суммы обязательств
        if (row.dealType != null && row.guarVolume != null && row.guarCourse != null && row.guarSum != null) {
            if ((row.dealType == dealType2 || row.dealType == dealType3) && row.reqSum != calc18(row)) {
                flag = false
                def msg = row.getCell('guarSum').column.name
                def msg1 = row.getCell('guarVolume').column.name
                def msg2 = row.getCell('guarCourse').column.name
                logger.error("Строка $rowNum: Значение графы «$msg» должно равняться произведению «$msg1» и «$msg2»!")
            }
        }

        boolean flag2 = true
        // Проверка доходов учитываемых в целях налога на прибыль по сделке
        if (row.incomeSum != null && row.incomeSum < 0) {
            flag2 = false
            def msg = row.getCell('incomeSum').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка доходов учитываемых в целях налога на прибыль по сделке
        if (flag && row.reqSum != null && row.guarSum != null && row.incomeSum != null) {
            flag2 = false
            def diff = row.reqSum - row.guarSum
            def msg = row.getCell('reqSum').column.name
            def msg1 = row.getCell('guarSum').column.name
            def msg2 = row.getCell('incomeSum').column.name
            if (diff > 0 && row.incomeSum != diff) {
                logger.error("Строка $rowNum: Значение графы «$msg2» должно быть равно разнице значений граф «$msg» и «$msg1»!")
            } else if (diff >= 0 && row.incomeSum != 0) {
                logger.error("Строка $rowNum: Значение графы «$msg2» должно быть равно нулю!")
            }
        }

        // Проверка расходов учитываемых в целях налога на прибыль по сделке
        if (flag && row.reqSum != null && row.guarSum != null && row.outcomeSum != null) {
            flag2 = false
            def diff = row.reqSum - row.guarSum
            def msg = row.getCell('reqSum').column.name
            def msg1 = row.getCell('guarSum').column.name
            def msg2 = row.getCell('outcomeSum').column.name
            if (diff < 0 && row.outcomeSum != diff) {
                logger.error("Строка $rowNum: Значение графы «$msg2» должно быть равно разнице значений граф «$msg» и «$msg1»!")
            } else if (diff >= 0 && row.outcomeSum != 0) {
                logger.error("Строка $rowNum: Значение графы «$msg2» должно быть равно нулю!")
            }
        }

        //Проверка рыночной цены
        if (row.marketPrice != null && row.marketPrice <= 0) {
            flag2 = false
            def msg = row.getCell('marketPrice').column.name
            logger.error("Строка $rowNum: Графа «$msg» должна быть больше «0»!")
        }

        ['reqVolume', 'guarVolume', 'price', 'reqCourse', 'guarCourse', 'marketPrice'].each {
            if (row[it] == null) {
                flag2 = false
            }
        }

        def msg11 = row.getCell('reqVolume').column.name
        def msg13 = row.getCell('guarVolume').column.name
        def msg14 = row.getCell('price').column.name
        def msg15 = row.getCell('reqCourse').column.name
        def msg16 = row.getCell('guarCourse').column.name
        def msg19 = row.getCell('incomeSum').column.name
        def msg21 = row.getCell('marketPrice').column.name

        // Проверка отклонений по доходам
        if (flag2 && row.incomeDelta != null) {
            def msg22 = row.getCell('incomeDelta').column.name
            if (row.incomeSum == 0 && row.incomeDelta != calc22(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно нулю!")
            } else {
                if (row.dealType == dealType1 || row.dealType == dealType2) {
                    if (row.dealFocus == direction2 && row.price >= row.marketPrice && row.incomeDelta != calc22(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно нулю!")
                    }
                    if (row.dealFocus != direction2 && row.price < row.marketPrice && row.incomeDelta != calc22(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно значению следующего выражения:" +
                                " «$msg13»*(«$msg21» - «$msg14»)*«$msg15»!")
                    }
                    if (row.dealFocus != direction1 && row.price <= row.marketPrice && row.incomeDelta != calc22(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно нулю!")
                    }
                    if (row.dealFocus != direction1 && row.price > row.marketPrice
                            && row.incomeDelta != calc22(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно значению следующего выражения:" +
                                " «$msg11»*(«$msg14» - «$msg21»)*«$msg16»!")
                    }
                } else if (row.dealType == dealType3) {
                    if (row.incomeSum > 0 && row.price > row.marketPrice && row.incomeDelta != calc22(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно нулю!")
                    } else if (row.incomeSum != 0 && row.price < row.marketPrice && row.incomeDelta != calc22(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно значению следующего выражения:" +
                                "(«$msg21» - «$msg14»)*«$msg15»!")
                    } else if (row.incomeSum == 0 && row.incomeDelta != calc22(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно нулю!")
                    }
                } else if (row.dealType == dealType4) {
                    if (row.incomeSum > 0 && row.price > row.marketPrice && row.incomeDelta != calc22(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно нулю!")
                    } else if (row.incomeSum != null && row.incomeSum != 0 && row.price < row.marketPrice && row.incomeDelta != calc22(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно значению следующего выражения:" +
                                "«$msg19» / «$msg14» * «$msg21» - «$msg19»!")
                    }
                }
            }
        }

        // Проверка отклонений по расходам
        if (flag2 && row.outcomeDelta != null) {
            def msg23 = row.getCell('outcomeDelta').column.name
            if (row.outcomeSum == 0 && row.outcomeDelta != calc23(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg23» должно быть равно нулю!")
            } else {
                if (row.dealType == dealType1 || row.dealType == dealType2) {
                    if (row.dealFocus != direction1 && row.price <= row.marketPrice && row.outcomeDelta != calc23(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg23» должно быть равно нулю!")
                    }
                    if (row.dealFocus != direction1 && row.price > row.marketPrice && row.outcomeDelta != calc23(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg23» должно быть равно значению следующего выражения:" +
                                " «$msg11»*(«$msg14» - «$msg21»)*«$msg16»!")
                    }
                    if (row.dealFocus != direction2 && row.price >= row.marketPrice && row.outcomeDelta != calc23(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg23» должно быть равно нулю!")
                    }
                    if (row.dealFocus != direction2 && row.price < row.marketPrice && row.outcomeDelta != calc23(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg23» должно быть равно значению следующего выражения:" +
                                " «$msg13»*(«$msg21» - «$msg14»)*«$msg15»!")
                    }
                } else if (row.dealType == dealType3) {
                    if (row.outcomeSum < 0 && row.price < row.marketPrice && row.outcomeDelta != calc23(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg23» должно быть равно нулю!")
                    } else if (row.price >= row.marketPrice && row.outcomeDelta != calc23(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg23» должно быть равно значению следующего выражения:" +
                                "-(«$msg21» - «$msg14»)*«$msg16»!")
                    } else if (row.outcomeDelta != calc23(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg23» должно быть заполнено значением «0», т.к. не выполнен " +
                                "порядок заполнения графы!")
                    }
                } else if (row.dealType == dealType4) {
                    if (row.outcomeSum < 0 && row.price < row.marketPrice && row.outcomeDelta != calc23(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg23» должно быть равно нулю!")
                    } else if (row.outcomeSum != null && row.price > row.marketPrice && row.outcomeDelta != calc23(row)) {
                        logger.error("Строка $rowNum: Значение графы «$msg23» должно быть равно значению следующего выражения:" +
                                "-(«$msg20» - («$msg20» / «$msg14»))*«$msg21»!")
                    }
                }
            }
        }
    }

    // Проверка итоговых значений по фиксированной строке «Итого»
    if (dataRows.find { it.getAlias() == 'total' }) {
        checkTotalSum(dataRows, totalColumns, logger, true)
    }
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(BigDecimal value, def precision) {
    value.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    // Удаление подитогов
    deleteAllAliased(dataRows)

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // 19, 20
        def diff = (row.reqSum ?: 0) - (row.guarSum ?: 0)
        row.incomeSum = diff > 0 ? diff : 0
        row.outcomeSum = diff < 0 ? diff : 0

        // 22, 23
        row.incomeDelta = calc22(row)
        row.outcomeDelta = calc23(row)
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    updateIndexes(dataRows)
}

def BigDecimal calc17(def row) {
    if (row.reqVolume != null && row.reqCourse != null) {
        return roundValue(row.reqVolume * row.reqCourse, 2)
    }
    return null
}

def BigDecimal calc18(def row) {
    if (row.guarVolume != null && row.guarCourse != null) {
        return roundValue(row.guarVolume * row.guarCourse, 2)
    }
    return null
}

def BigDecimal calc22(def row) {
    dealType1 = getRecordId(92, 'NAME', 'Кассовая сделка')
    dealType2 = getRecordId(92, 'NAME', 'Срочная сделка')
    dealType3 = getRecordId(92, 'NAME', 'Премия по опциону')
    dealType4 = getRecordId(92, 'NAME', 'Промежуточный платёж')
    direction1 = getRecordId(20, 'DIRECTION', 'покупка')
    direction2 = getRecordId(20, 'DIRECTION', 'продажа')
    if (row.incomeSum == 0) {
        return 0
    }
    if (row.dealType == dealType1 || row.dealType == dealType2) {
        if (row.dealFocus == direction2 && row.price >= row.marketPrice) {
            return 0
        }
        if (row.dealFocus == direction2 && row.price < row.marketPrice) {
            return roundValue(-1 * row.guarVolume * (row.marketPrice - row.price) * row.reqCourse, 2)
        }
        if (row.dealFocus == direction1 && row.price <= row.marketPrice) {
            return 0
        }
        if (row.dealFocus == direction1 && row.price > row.marketPrice) {
            return roundValue(-1 * row.reqVolume * (row.price - row.marketPrice) * row.guarCourse, 2)
        }
    }
    if (row.dealType == dealType3) {
        if (row.incomeSum > 0 && row.price > row.marketPrice) {
            return 0
        }
        if (row.price < row.marketPrice) {
            return roundValue(-1 * (row.marketPrice - row.price) * row.reqCourse, 2)
        }
        if (row.incomeSum == 0) {
            return 0
        }
    }
    if (row.dealType == dealType4) {
        if (row.incomeSum > 0 && row.price > row.marketPrice) {
            return 0
        }
        if (row.price < row.marketPrice) {
            return roundValue((row.incomeSum / row.price) * row.marketPrice - row.incomeSum, 2)
        }
    }

}

def BigDecimal calc23(def row) {
    dealType1 = getRecordId(92, 'NAME', 'Кассовая сделка')
    dealType2 = getRecordId(92, 'NAME', 'Срочная сделка')
    dealType3 = getRecordId(92, 'NAME', 'Премия по опциону')
    dealType4 = getRecordId(92, 'NAME', 'Промежуточный платёж')
    direction1 = getRecordId(20, 'DIRECTION', 'покупка')
    direction2 = getRecordId(20, 'DIRECTION', 'продажа')
    if (row.outcomeSum == 0) {
        return 0
    }
    if (row.dealType == dealType1 || row.dealType == dealType2) {
        if (row.dealFocus == direction1 && row.price <= row.marketPrice) {
            return 0
        }
        if (row.dealFocus == direction1 && row.price > row.marketPrice) {
            return roundValue(-row.reqVolume * (row.price - row.marketPrice) * row.guarCourse, 2)
        }
        if (row.dealFocus == direction2 && row.price >= row.marketPrice) {
            return 0
        }
        if (row.dealFocus == direction2 && row.price < row.marketPrice) {
            return roundValue(-row.guarVolume * (row.marketPrice - row.price) * row.reqCourse, 2)
        }
    }
    if (row.dealType == dealType3) {
        if (row.outcomeSum < 0 && row.price < row.marketPrice) {
            return 0
        }
        if (row.price > row.marketPrice) {
            return roundValue(-(row.marketPrice - row.price) * row.guarCourse, 2)
        }
        if (row.outcomeSum == 0) {
            return 0
        }
    }
    if (row.dealType == dealType4) {
        if (row.outcomeSum < 0 && row.price < row.marketPrice) {
            return 0
        }
        if (row.price > row.marketPrice) {
            return roundValue(-(row.outcomeSum.abs() - row.outcomeSum / row.price) * row.marketPrice, 2)
        }
        if (row.outcomeSum == 0) {
            return 0
        }

    }
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 24
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = '№ пп'
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()
    def totalRowFromFile = null

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Итого") {
            totalRowFromFile = getNewTotalFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // сравнение итогов
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    if (totalRowFromFile) {
        compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][2]): getColumnName(tmpRow, 'dealNum')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'dealType')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'dealFocus')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'reqCurCode')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'reqVolume')]),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'guarCurCode')]),
            ([(headerRows[0][13]): getColumnName(tmpRow, 'guarVolume')]),
            ([(headerRows[0][14]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[0][15]): 'Курс Банка России на дату исполнения (досрочного исполнения) сделки, руб.']),
            ([(headerRows[0][16]): '']),
            ([(headerRows[0][17]): 'Требования (обязательства) по сделке, руб.']),
            ([(headerRows[0][18]): '']),
            ([(headerRows[0][19]): 'Доходы (расходы) учитываемые в целях налога на прибыль по сделке, руб.']),
            ([(headerRows[0][20]): '']),
            ([(headerRows[0][21]): getColumnName(tmpRow, 'marketPrice')]),
            ([(headerRows[0][22]): getColumnName(tmpRow, 'incomeDelta')]),
            ([(headerRows[0][23]): getColumnName(tmpRow, 'outcomeDelta')])
    ]
    (1..23).each {
        headerMapping.add([(headerRows[2][it]): it.toString()])
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    def String iksrName = getColumnName(newRow, 'iksr')
    def nameFromFile = values[8]

    def int colIndex = 2

    // графа 2
    newRow.dealNum = values[colIndex]
    colIndex++

    // графа 3
    newRow.dealType = getRecordIdImport(92, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 4
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 5
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    def recordId = getTcoRecordId(nameFromFile, values[6], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), true, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 6
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 7
    if (map != null) {
        def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            def expectedValues = [countryMap.NAME?.stringValue, countryMap.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'countryName'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 8
    newRow.name = recordId
    colIndex++

    // графа 9
    newRow.dealFocus = getRecordIdImport(20, 'DIRECTION', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10
    newRow.reqCurCode = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 11
    newRow.reqVolume = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.guarCurCode = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13
    newRow.guarVolume = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графы 14-23
    ['price', 'reqCourse', 'guarCourse', 'reqSum', 'guarSum', 'incomeSum', 'outcomeSum', 'marketPrice', 'incomeDelta', 'outcomeDelta'].each {
        newRow[it] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }
    return newRow
}
// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(dataRows.findAll { it.getAlias() == null })
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void sortRows(def dataRows) {
    dataRows.sort { def rowA, def rowB ->
        def aValue = getRefBookValue(520, rowA.name)?.NAME?.value
        def bValue = getRefBookValue(520, rowB.name)?.NAME?.value
        if (aValue != bValue) {
            return aValue <=> bValue
        }
        aValue = rowA.reasonNumber
        bValue = rowB.reasonNumber
        if (aValue != bValue) {
            return aValue <=> bValue
        }
        aValue = rowA.reasonDate
        bValue = rowB.reasonDate
        if (aValue != bValue) {
            return aValue <=> bValue
        }
        return 0
    }
}

/**
 * Получить итоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewTotalFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 22
    def colIndex = 22
    newRow.incomeDelta = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 23
    colIndex = 23
    newRow.outcomeDelta = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}
