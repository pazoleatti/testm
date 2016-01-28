package form_template.income.rnu_123.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 РНУ 123. Регистр налогового учёта доходов по гарантиям и аккредитивам и иным гарантийным продуктам,
 * включая инструменты торгового финансирования, предоставляемым Взаимозависимым лицам и резидентам оффшорных зон по ценам, не соответствующим рыночному уровню
 *
 * formTemplateId=841
 */

// fix
// rowNumber    		(1) -  № пп
// name         		(2) -  Наименование Взаимозависимого лица (резидента оффшорной зоны)
// iksr					(3) -  Идентификационный номер
// countryName			(4) -  Страна регистрации
// code        			(5) -  Код классификации дохода / расхода
// docNumber 			(6) -  номер
// docDate 				(7) -  дата
// sum1 				(8) -  Сумма кредита для расчёта (остаток задолженности, невыбранный лимит кредита), ед. вал.
// course   			(9) -  Валюта
// transDoneDate		(10) - Фактическое отражение в бухгалтерском учете
// taxDoneDate			(11) - Для целей доначисления дохода в налоговом учете
// course2				(12) - Курс На дату фактического отражения в бухгалтерском учете
// course3				(13) - Курс Для целей доначисления дохода в налоговом учете
// startDate1			(14) - Дата начала
// endDate1				(15) - Дата окончания
// startDate2			(16) - Дата начала
// endDate2				(17) - Дата окончания
// base					(18) - База для расчета, кол. дней
// dealPay				(19) - Плата по условиям сделки, % год./ед. вал.
// sum2					(20) - По данным бухгалтерского учета
// sum3					(21) - Доначисление для целей налогового учета
// sum4					(22) - Всего по данным налогового учета
// tradePay				(23) - Рыночная Плата, % годовых / ед. вал.
// sum5					(24) - Доходу начисленному по данным налогового учета согласно условиям сделк
// sum6					(25) - Доначисленному доходу для целей налогового учета
// sum7					(26) - Всей сумме дохода, начисленного по данным налогового учета
// sum8					(27) - Фактическому доходу, начисленному по данным налогового учета согласно условиям сделки
// sum9					(28) - Доначисленному доходу для целей налогового учета
// sum10				(29) - Всей сумме дохода, начисленного по данным налогового учета


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
def allColumns = ['fix', 'rowNumber', 'name', 'iksr', 'countryName', 'code', 'docNumber', 'docDate', 'sum1', 'course',
                  'transDoneDate', 'taxDoneDate', 'course2', 'course3', 'startDate1', 'endDate1', 'startDate2', 'endDate2',
                  'base', 'dealPay', 'sum2', 'sum3', 'sum4', 'tradePay', 'sum5', 'sum6', 'sum7', 'sum8', 'sum9', 'sum10']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'code', 'docNumber', 'docDate', 'sum1', 'course', 'transDoneDate', 'taxDoneDate', 'course2',
                       'course3', 'startDate1', 'endDate1', 'startDate2', 'endDate2', 'base', 'dealPay', 'sum2', 'sum3', 'tradePay', 'sum9', 'sum10']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'iksr', 'countryName', 'sum4', 'sum5', 'sum6', 'sum7', 'sum8']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'code', 'docNumber', 'docDate', 'sum1', 'course', 'transDoneDate', 'course2', 'startDate1',
                       'endDate1', 'base', 'dealPay', 'sum2', 'sum4', 'tradePay', 'sum7']

@Field
def totalColumns = ['sum2', 'sum3', 'sum4', 'sum5', 'sum6', 'sum7', 'sum8', 'sum9', 'sum10']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Наименование отчетного периода
@Field
def periodName = null

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

def getPeriodName() {
    if (periodName == null) {
        periodName = reportPeriodService.get(formData.reportPeriodId).getName()
    }
    return periodName
}

def getPrevForm(def form) {
    return formDataService.getFormDataPrev(formData)
}

// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
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

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // Проверка уникальности строк
        def msg2 = row.getCell('name').column.name
        def val2 = row['name']
        def map = getRefBookValue(520, row.name)
        if (map != null) {
            val2 = map.NAME?.stringValue
        } else {
            val2 = 'графа 2 не задана'
        }
        def msg3 = row.getCell('iksr').column.name
        if (map != null) {
            val3 = map.IKSR?.stringValue
        } else {
            val3 = 'графа 3 не задана'
        }
        def msg5 = row.getCell('code').column.name
        def val5 = row.code != null ? row.code : 'графа 5 не задана'
        def msg6 = row.getCell('docNumber').column.name
        def val6 = row.docNumber != null ? row.docNumber : 'графа 6 не задана'
        def msg7 = row.getCell('docDate').column.name
        def val7 = row.docDate != null ? row.docDate : 'графа 7 не задана'
        def msg9 = row.getCell('course').column.name
        def val9 = getRefBookValue(15, row.course) != null ? getRefBookValue(15, row.course).CODE?.value : 'графа 9 не задана'
        def matchRowNum = []
        for (int i = 0; i < dataRows.size(); i++) {
            if (dataRows.get(i).getIndex() != row.getIndex()) {
                boolean flag = true
                ['name', 'iksr', 'code', 'docNumber', 'docDate', 'course'].each {
                    flag = flag && (dataRows.get(i)[it] == row[it])
                }
                if (flag) {
                    matchRowNum.add(dataRows.get(i).getIndex())
                }
            }

        }
        def str = matchRowNum.join(", ")
        if (matchRowNum.size() > 0) {
            logger.warn("Строки $str: Неуникальное значение граф «$msg2»= $val2 , «$msg3»= $val3 , «$msg5»= $val5 , «$msg6»= $val6 , «$msg7»= $val7 , «$msg9»= $val9!")
        }

        // Проверка корректности даты первичного документа
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        //Проверка суммы гарантии/ аккредитива
        if (row.sum1 != null && row.sum1 < 0) {
            def msg = row.getCell('sum1').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
        }

        // Проверка даты операции фактически отраженной в бухгалтерском учете
        checkDatePeriod(logger, row, 'transDoneDate', getReportPeriodStartDate(), getReportPeriodEndDate(), true)

        // Проверка корректности даты операции для целей доначисления дохода в налоговом учете
        checkDatePeriod(logger, row, 'taxDoneDate', getReportPeriodStartDate(), getReportPeriodEndDate(), true)

        // Проверка значения граф 7, 10
        checkDatePeriod(logger, row, 'transDoneDate', 'docDate', getReportPeriodEndDate(), true)

        // Проверка значения граф 7, 11
        checkDatePeriod(logger, row, 'taxDoneDate', 'docDate', getReportPeriodEndDate(), true)

        // Проверка курса валюты
        if (row.course2 != null && row.course2 <= 0) {
            def msg = row.getCell('course2').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше «0»!")
        }

        // Проверка курса валюты
        if (row.course3 != null && row.course3 <= 0) {
            def msg = row.getCell('course3').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше «0»!")
        }

        // Проверка количества дней
        if (row.base != null && row.base < 1) {
            def msg = row.getCell('base').column.name
            logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «1»!")
        }

        // Проверка допустимых значений
        def pattern = /[0-9]+([\.|\,][0-9]+)?\%?/
        if (row.dealPay != null && !(row.dealPay ==~ pattern)) {
            def msg = row.getCell('dealPay').column.name
            logger.error("Строка $rowNum: Значение графы «%s» должно соответствовать следующему формату: первые символы: (0-9)," +
                    " следующие символы («.» или «,»), следующие символы (0-9), последний символ %s или пусто!", msg, "(%)")
        }
        if (row.tradePay != null && !(row.tradePay ==~ pattern)) {
            def msg = row.getCell('tradePay').column.name
            logger.error("Строка $rowNum: Значение графы «%s» должно соответствовать следующему формату: первые символы: (0-9)," +
                    " следующие символы («.» или «,»), следующие символы (0-9), последний символ %s или пусто!", msg, "(%)")
        }

        // Проверка положительной суммы дохода/расхода 16,17,19-21
        ['sum2', 'sum3', 'sum5', 'sum6'].each {
            if (row.getCell(it).value != null && row.getCell(it).value < 0) {
                def msg = row.getCell(it).column.name
                logger.error("Строка $rowNum: Значение графы «$msg» должно быть больше или равно «0»!")
            }
        }

        // проверки автозаполняемых
        def msg8 = row.getCell('sum1').column.name
        def msg12 = row.getCell('course2').column.name
        def msg13 = row.getCell('course3').column.name
        def msg14 = row.getCell('startDate1').column.name
        def msg15 = row.getCell('endDate1').column.name
        def msg16 = row.getCell('startDate2').column.name
        def msg17 = row.getCell('endDate2').column.name
        def msg18 = row.getCell('base').column.name
        def msg20 = row.getCell('sum2').column.name
        def msg21 = row.getCell('sum3').column.name
        def msg22 = row.getCell('sum4').column.name
        def msg23 = row.getCell('tradePay').column.name
        def msg24 = row.getCell('sum5').column.name
        def msg25 = row.getCell('sum6').column.name
        def msg26 = row.getCell('sum7').column.name
        def msg27 = row.getCell('sum8').column.name
        def msg28 = row.getCell('sum9').column.name
        def msg29 = row.getCell('sum10').column.name
        course810 = getRecordId(15, 'CODE', '810')

        // Проверка корректности суммы фактического дохода по данным налогового учета
        if (row.sum4 != null) {
            if (row.sum3 == null && row.sum4 != calc22(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно значению графы «$msg20»!")
            } else if (getPeriodName() == "1 квартал" && row.sum4 != calc22(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно разности значений графы «$msg20» и «$msg21»!")
            } else if (getPeriodName() == "год" && row.sum4 != calc22(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg22» должно быть равно сумме значений графы «$msg20» и «$msg21»!")
            } else {
                logger.error("Строка $rowNum: Значение графы «$msg22» заполнена значением «0», т.к. не выполнены условия расчета графы!")
            }
        }

        // Проверка корректности суммы рыночного дохода по данным налогового учета согласно условиям сделки
        if (row.sum3 == null && row.sum5 != null) {
            logger.error("Строка $rowNum: Значение графы «$msg24» должно быть не заполнено!")
        } else if (!calcFlag23(row)) {
            if (row.course == course810 && row.sum5 != calc24(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg24» должно быть равно значению графы «%s»!", msg23)
            } else if (row.course != course810 && row.sum5 != calc24(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg24» должно быть равно значению выражения  «%s»*«$msg12»!", msg23)
            }
        } else if (calcFlag23(row)) {
            if (row.course == course810 && row.sum5 != calc24(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg24» должно быть равно значению выражения  " +
                        "«%s»*«$msg8»*(«$msg15»-«$msg14»+1)/«$msg18»!", msg23)
            } else if (row.course != course810 && row.sum5 != calc24(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg24» должно быть равно значению выражения  " +
                        "«%s»*«$msg8»*(«$msg15»-«$msg14»+1)/«$msg18»*«$msg12»!", msg23)
            }
        } else {
            logger.error("Строка $rowNum: Значение графы «$msg24» заполнена значением «0», т.к. не выполнены условия расчета графы!")
        }

        // Проверка корректности суммы рыночного дохода по доначисленному  доходу для целей налогового учета
        if (row.sum3 == null && row.sum6 != null) {
            logger.error("Строка $rowNum: Значение графы «$msg25» должно быть не заполнено!")
        } else if (!calcFlag23(row)) {
            if (row.course == course810 && getPeriodName() == "год" && row.sum6 != calc25(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg25» должно быть равно значению выражения  " +
                        "«%s»*(«$msg17»-«$msg16»+1)/«$msg18»!", msg23)
            } else if (row.course != course810 && getPeriodName() == "год" && row.sum6 != calc25(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg25» должно быть равно значению выражения  " +
                        "«%s»*(«$msg17»-«$msg16»+1)/«$msg18»*«$msg13»!", msg23)
            }
        } else if (calcFlag23(row)) {
            if (row.course == course810 && getPeriodName() == "год" && row.sum6 != calc25(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg25» должно быть равно значению выражения  " +
                        "«%s»*«$msg8»*(«$msg17»-«$msg16»+1)/«$msg18»!", msg23)
            } else if (row.course != course810 && getPeriodName() == "год" && row.sum6 != calc25(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg25» должно быть равно значению выражения  " +
                        "«%s»*«$msg8»*(«$msg17»-«$msg16»+1)/«$msg18»*«$msg13»!", msg23)
            }
        } else if (calcFlag23(getPeriodName() == "1 квартал")) {
            if (getPrevForm(formData) != null && findMatch(row) == 1 && row.sum6 != calc25(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg25» должно быть равно значению графы «$msg25» предыдущего налогового периода!")
            } else if (getPrevForm(formData) != null && findMatch(row) == 0 && row.sum6 == calc25(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg25» заполнено значением «0», т.к. не существует ни одной строки формы РНУ-123 за предыдущий налоговый период, соответствующей уникальным атрибутам проверяемой строки!")
            } else if (getPrevForm(formData) != null && findMatch(row) > 1 && row.sum6 == calc25(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg25» заполнено значением «0», т.к. существует более одной строки формы РНУ-123 за предыдущий налоговый период, соответствующей уникальным атрибутам проверяемой строки!")
            } else if (getPrevForm(formData) == null && row.sum6 == calc25(row)) {
                logger.error("Строка $rowNum: Значение графы «$msg25» заполнено значением «0», т.к. не существует формы РНУ-123 за предыдущий налоговый период!")
            }
        } else {
            logger.error("Строка $rowNum: Значение графы «$msg25» заполнена значением «0», т.к. не выполнены условия расчета графы!")
        }

        // Проверка корректности суммы рыночного дохода по данным налогового учета
        if (row.sum5 == null && row.sum6 == null) {
            if (!calcFlag23(row)) {
                if (row.course == course810 && row.sum7 != calc26(row)) {
                    logger.error("Строка $rowNum: Значение графы «$msg26» должно быть равно значению графы «%s»!", msg23)
                } else if (row.course != course810 && row.sum7 != calc26(row)) {
                    logger.error("Строка $rowNum: Значение графы «$msg26» должно быть равно значению выражения «%s»*«$msg12»!", msg23)
                }
            } else if (calcFlag23(row)) {
                if (row.course == course810 && row.sum7 != calc26(row)) {
                    logger.error("Строка $rowNum: Значение графы «$msg26» должно быть равно значению выражения  " +
                            "«%s»*«$msg8»*(«$msg15»-«$msg14»+1)/«$msg18»!", msg23)
                } else if (row.course != course810 && row.sum7 != calc26(row)) {
                    logger.error("Строка $rowNum: Значение графы «$msg26» должно быть равно значению выражения  " +
                            "«%s»*«$msg8»*(«$msg15»-«$msg14»+1)/«$msg18»*«$msg12»!", msg23)
                }
            }
        } else if (getPeriodName() == "1 квартал") {
            logger.error("Строка $rowNum: Значение графы «$msg26» должно быть равно разности значений графы «$msg24» и «$msg25»!")
        } else if (getPeriodName() == "год") {
            logger.error("Строка $rowNum: Значение графы «$msg26» должно быть равно сумме значений графы «$msg24» и «$msg25»!")
        } else {
            logger.error("Строка $rowNum: Значение графы «$msg26» заполнена значением «0», т.к. не выполнены условия расчета графы!")
        }

        // Проверка корректности суммы доначисления  дохода до рыночного уровня по данным налогового учета согласно условиям сделки
        if (row.sum5 == null && row.sum8 != null) {
            logger.error("Строка $rowNum: Значение графы «$msg27» должно быть не заполнено!")
        } else if (row.sum8 != calc27(row)) {
            logger.error("Строка $rowNum: Значение графы «$msg27» должно быть равно разности значений графы «$msg24» и «$msg20»!")
        }

        // Проверка корректности суммы доначисления  дохода до рыночного уровня по доначисленному доходу для целей налогового учета
        if (row.sum3 != null && row.sum6 != null && row.sum9 != null) {
            if (getPeriodName() == "год" && row.sum9 != row.sum6 - row.sum3) {
                logger.error("Строка $rowNum: Значение графы «$msg28» должно быть равно разности графы «$msg25» и «$msg21»")
            } else if (getPrevForm(formData) != null && (row.sum9 != row.sum6 - row.sum3 || row.sum9 != calcPrev28(row))) {
                logger.error("Строка $rowNum: Значение графы «$msg28» должно быть равно разности графы «$msg25» и «$msg21»" +
                        " или значению графы «$msg28» за предыдущий налоговый период!")
            }
        }

        // Проверка корректности суммы доначисления  дохода до рыночного уровня по данным налогового учета
        if (row.sum6 != null && row.sum10 != null) {
            if (row.sum8 == null && row.sum9 == null && row.sum10 != row.sum7 - row.sum4) {
                logger.error("Строка $rowNum: Значение графы «$msg28» должно быть равно разности графы «$msg26» и «$msg22»")
            } else if (getPeriodName() == "1 квартал" && (row.sum10 != row.sum7 - row.sum4 || row.sum10 != row.sum8 - row.sum9)) {
                logger.error("Строка $rowNum: Значение графы «$msg28» должно быть равно разности графы «$msg25» и «$msg21»" +
                        " или разности значений графы  «$msg27» и «$msg28»!")
            } else if (getPeriodName() == "год" && (row.sum10 != row.sum7 - row.sum4 || row.sum10 != row.sum8 + row.sum9)) {
                logger.error("Строка $rowNum: Значение графы «$msg28» должно быть равно разности графы «$msg25» и «$msg21»" +
                        " или сумме значений графы  «$msg27» и «$msg28»!")
            } else {
                logger.error("Строка $rowNum: Значение графы «$msg29» заполнено значением «0», т.к. не выполнен порядок заполнения графы!")
            }
        }
    }

    // Проверка итоговых значений пофиксированной строке «Итого»
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

    // Удаление итогов
    deleteAllAliased(dataRows)

    for (row in dataRows) {
        // графа 22
        row.sum4 = calc22(row)
        // графа 24
        row.sum5 = calc24(row)
        // графа 25
        row.sum6 = calc25(row)
        // графа 26
        row.sum7 = calc26(row)
        // графа 27
        row.sum8 = calc27(row)
    }

    // Общий итог
    def total = calcTotalRow(dataRows)
    dataRows.add(total)

    sortFormDataRows(false)
}

def calc22(def row) {
    if (row.sum3 == null) {
        return row.sum2
    } else if (getPeriodName() == "1 квартал") {
        return row.sum2 - row.sum3
    } else if (getPeriodName() == "год") {
        return row.sum2 + row.sum3
    } else {
        return 0
    }
}

def calcFlag23(def row) {
    if (row.tradePay != null) {
        String col23 = row.tradePay.trim()
        return (col23[-1] != "%") ? false : true
    }
}

def calc24(def row) {
    def rowNum = row.getIndex()
    def pattern = /[0-9]+([\.|\,][0-9]+)?\%?/
    if (!(row.tradePay ==~ pattern)) {
        def msg = row.getCell('tradePay').column.name
        logger.error("Строка $rowNum: Значение графы «%s» должно соответствовать следующему формату: первые символы: (0-9)," +
                " следующие символы («.» или «,»), следующие символы (0-9), последний символ %s или пусто!", msg, "(%)")
    } else {
        course810 = getRecordId(15, 'CODE', '810')
        String col23 = row.tradePay.trim()
        def flag23 = calcFlag23(row)
        def calcCol23 = flag23 ? new BigDecimal(col23[0..-2]).setScale(2, BigDecimal.ROUND_HALF_UP) :
                new BigDecimal(col23).setScale(2, BigDecimal.ROUND_HALF_UP)

        if (row.sum3 == null) {
            return null
        } else if (!flag23) {
            if (row.course == course810) {
                return calcCol23
            } else {
                return roundValue(calcCol23 * row.course2, 2)
            }
        } else if (flag23) {
            if (row.course == course810) {
                return roundValue(calcCol23 * row.sum1 * (row.endDate1 - row.startDate1 + 1) / row.base, 2)
            } else {
                return roundValue(calcCol23 * row.sum1 * (row.endDate1 - row.startDate1 + 1) / (row.base * row.course2), 2)
            }
        } else {
            return 0
        }
    }
}

// метод для применения в ЛП
def findMatch(def row) {
    def form = getPrevForm(formData)
    def dataRows = formDataService.getDataRowHelper(form).allCached
    int cnt = 0
    for (dataRow in dataRows) {
        boolean flag = true
        ['name', 'iksr', 'code', 'docNumber', 'docDate', 'course'].each {
            flag = flag && (dataRow[it] == row[it])
        }
        if (flag) {
            cnt++
        }
    }
    return cnt
}

def calcPrev25(def row) {
    def form = getPrevForm(formData)
    if (form != null) {
        def dataRows = formDataService.getDataRowHelper(form).allCached
        int cnt = 0
        for (dataRow in dataRows) {
            boolean flag = true
            ['name', 'iksr', 'code', 'docNumber', 'docDate', 'course'].each {
                flag = flag && (dataRow[it] == row[it])
            }
            if (flag) {
                cnt++
                value = dataRow.sum6
            }
        }
        return (cnt == 1) ? value : 0
    }
    return 0
}

def calc25(def row) {
    def rowNum = row.getIndex()
    def pattern = /[0-9]+([\.|\,][0-9]+)?\%?/
    if (!(row.tradePay ==~ pattern)) {
        def msg = row.getCell('tradePay').column.name
        logger.error("Строка $rowNum: Значение графы «%s» должно соответствовать следующему формату: первые символы: (0-9)," +
                " следующие символы («.» или «,»), следующие символы (0-9), последний символ %s или пусто!", msg, "(%)")
    } else {
        course810 = getRecordId(15, 'CODE', '810')
        String col23 = row.tradePay.trim()
        def flag23 = calcFlag23(row)
        def calcCol23 = flag23 ? new BigDecimal(col23[0..-2]).setScale(2, BigDecimal.ROUND_HALF_UP) :
                new BigDecimal(col23).setScale(2, BigDecimal.ROUND_HALF_UP)

        if (row.sum3 == null) {
            return null
        } else if (!flag23) {
            if (row.course == course810 && getPeriodName() == "год") {
                return roundValue(calcCol23 * (row.endDate2 - row.startDate2 + 1) / row.base, 2)
            } else if (row.course != course810 && getPeriodName() == "год") {
                return roundValue(calcCol23 * (row.endDate2 - row.startDate2 + 1) / (row.base * row.course3), 2)
            }
        } else if (flag23) {
            if (row.course == course810 && getPeriodName() == "год") {
                return roundValue(calcCol23 * row.sum1 * (row.endDate2 - row.startDate2 + 1) / row.base, 2)
            } else if (row.course != course810 && getPeriodName() == "год") {
                return roundValue(calcCol23 * row.sum1 * (row.endDate2 - row.startDate2 + 1) / (row.base * row.course3), 2)
            }
        } else if (getPeriodName() == "1 квартал") {
            // в calcPrev25 вся логика, связанная с формой предыдущего отчетного периода
            return calcPrev25(row)
        } else {
            return 0
        }
    }
}

def calc26(def row) {
    def rowNum = row.getIndex()
    if (row.sum5 == null && row.sum6 == null) {
        def pattern = /[0-9]+([\.|\,][0-9]+)?\%?/
        if (!(row.tradePay ==~ pattern)) {
            def msg = row.getCell('tradePay').column.name
            logger.error("Строка $rowNum: Значение графы «%s» должно соответствовать следующему формату: первые символы: (0-9)," +
                    " следующие символы («.» или «,»), следующие символы (0-9), последний символ %s или пусто!", msg, "(%)")
        } else {
            String col23 = row.tradePay.trim()
            def flag23 = calcFlag23(row)
            def calcCol23 = flag23 ? new BigDecimal(col23[0..-2]).setScale(2, BigDecimal.ROUND_HALF_UP) :
                    new BigDecimal(col23).setScale(2, BigDecimal.ROUND_HALF_UP)

            if (!flag23) {
                if (row.course == course810) {
                    return calcCol23
                } else {
                    return roundValue(calcCol23 * row.course2, 2)
                }
            } else if (flag23) {
                if (row.course == course810) {
                    return roundValue(calcCol23 * row.sum1 * (row.endDate1 - row.startDate1 + 1) / row.base, 2)
                } else {
                    return roundValue(calcCol23 * row.sum1 * (row.endDate1 - row.startDate1 + 1) / (row.base * row.course2), 2)
                }
            } else {
                return 0
            }
        }
    } else {
        if (getPeriodName() == "1 квартал") {
            return row.sum5 - row.sim6
        } else if (getPeriodName() == "год") {
            return row.sum5 + row.sum6
        } else {
            return 0
        }
    }
}

def calc27(def row) {
    if (row.sum5 == null) {
        return null
    } else {
        return row.sum5 - row.sum2
    }
}

def calcPrev28(def row) {
    def form = getPrevForm(formData)
    if (form != null) {
        def dataRows = formDataService.getDataRowHelper(form).allCached
        int cnt = 0
        for (dataRow in dataRows) {
            boolean flag = true
            ['name', 'iksr', 'code', 'docNumber', 'docDate', 'course'].each {
                flag = flag && (dataRow[it] == row[it])
            }
            if (flag) {
                cnt++
                value = dataRow.sum6
            }
        }
        return (cnt == 1) ? value : 0
    }
    return 0
}

def calcTotalRow(def dataRows) {
    def totalRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Всего'
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
    int COLUMN_COUNT = 29
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = '№ пп'
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

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
    def totalRowFromFileMap = [:] // мапа для хранения строк подитогов со значениями из файла (стили простых строк)

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
            ([(headerRows[0][2]): getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'code')]),
            ([(headerRows[0][6]): 'Первичный документ']),
            ([(headerRows[0][7]): '']),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'sum1')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'course')]),
            ([(headerRows[0][10]): 'Дата операции']),
            ([(headerRows[0][11]): '']),
            ([(headerRows[0][12]): 'Курс Банка России (руб.)']),
            ([(headerRows[0][13]): '']),
            ([(headerRows[0][14]): 'Расчетный период']),
            ([(headerRows[0][15]): '']),
            ([(headerRows[0][16]): '']),
            ([(headerRows[0][17]): '']),
            ([(headerRows[0][18]): getColumnName(tmpRow, 'base')]),
            ([(headerRows[0][19]): getColumnName(tmpRow, 'dealPay')]),
            ([(headerRows[0][20]): 'Сумма фактического дохода, ']),
            ([(headerRows[0][21]): '']),
            ([(headerRows[0][22]): '']),
            ([(headerRows[0][23]): getColumnName(tmpRow, 'tradePay')]),
            ([(headerRows[0][24]): 'Рыночная сумма дохода (руб.), соответствующая: ']),
            ([(headerRows[0][25]): '']),
            ([(headerRows[0][26]): '']),
            ([(headerRows[0][27]): 'Сумма доначисления дохода до рыночного уровня (руб.), соответствующая: ']),
            ([(headerRows[0][28]): '']),
            ([(headerRows[0][29]): ''])
    ]
    (2..19).each {
        headerMapping.add([(headerRows[3][it]): it.toString()])
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
    def nameFromFile = values[2]

    def int colIndex = 2

    def recordId = getTcoRecordId(nameFromFile, values[3], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), true, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2
    newRow.name = recordId
    colIndex++

    // графа 3
    if (map != null) {
        formDataService.checkReferenceValue(520, values[colIndex], map.IKSR?.stringValue, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4
    if (map != null) {
        def countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            def expectedValues = [countryMap.NAME?.stringValue, countryMap.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'countryName'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 5
    newRow.code = values[colIndex]
    colIndex++

    // графа 6
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 7
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 8
    newRow.sum1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.course = getRecordIdImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10
    newRow.transDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.taxDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.course2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13
    newRow.course3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 14
    newRow.startDate1 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
    newRow.endDate1 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 16
    newRow.startDate2 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 17
    newRow.endDate2 = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 18
    newRow.base = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 19
    newRow.dealPay = values[colIndex]
    colIndex++

    // графа 20
    newRow.sum2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 21
    newRow.sum3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 22
    newRow.sum4 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 23
    newRow.tradePay = values[colIndex]
    colIndex++

    // графы 24-29
    ['sum5', 'sum6', 'sum7', 'sum8', 'sum9', 'sum10'].each {
        newRow[it] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    return newRow
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

    // графа 20
    colIndex = 20
    newRow.sum2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 21
    colIndex = 21
    newRow.sum3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 22
    colIndex = 22
    newRow.sum4 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    colIndex = 24
    ['sum5', 'sum6', 'sum7', 'sum8', 'sum9', 'sum10'].each {
        newRow[it] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }

    return newRow
}
// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, dataRows.find { it.getAlias() == 'total' }, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}
