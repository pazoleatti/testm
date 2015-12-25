package form_template.income.income_agent_1.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов) (с 9 месяцев 2015).
 * formTemplateId=314
 *
 * TODO:
 *      - логические проверки в чтз пока не описаны
 */

@Field
def formTypeIdOld = 10070

// графа 1  - rowNum      		№ п/п
// графа 2  - emitentName   	Эмитент. Наименование
// графа 3  - emitentInn    	Эмитент. ИНН
// графа 4  - all  		        Дивиденды полученные ПАО Сбербанк. Всего
// графа 5  - rateZero 	    	Дивиденды полученные ПАО Сбербанк. По ставке 0%
// графа 6  - distributionSum	Сумма дивидендов Эмитента, распределяемая в пользу всех получателей, уменьшенная на сумму дивидендов, полученных самим Эмитентом
// графа 7  - decisionNumber    Дивиденды выплаченные. Решение о распределении дивидендов. Номер
// графа 8  - decisionDate      Дивиденды выплаченные. Решение о распределении дивидендов. Дата
// графа 9  - year      		Дивиденды выплаченные. Отчетный год. Год
// графа 10 - firstMonth        Дивиденды выплаченные. Отчетный год. Период распределения. Первый месяц
// графа 11 - lastMonth         Дивиденды выплаченные. Отчетный год. Период распределения. Последний месяц
// графа 12 - allSum            Дивиденды выплаченные. Сумма дивидендов. Всего
// графа 13 - addresseeName     Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Получатель. Наименование (ЮЛ) или ФИО (ФЛ)
// графа 14 - inn               Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Получатель. ИНН
// графа 15 - kpp               Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Получатель. КПП
// графа 16 - type              Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Получатель. Тип
// графа 17 - status            Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Получатель. Статус
// графа 18 - birthday          Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Получатель. Физическое лицо. Дата рождения
// графа 19 - citizenship       Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Получатель. Физическое лицо. Гражданство
//     в 0.5.1 изменился тип со строки на справочник - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
// графа 20 - kind              Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Получатель. Физическое лицо. Документ. Вид
//     в 0.5.1 изменился тип со строки на справочник - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
// графа 21 - series            Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Получатель. Физическое лицо. Документ. Серия и номер
// графа 22 - rate              Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Ставка
// графа 23 - dividends         Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Дивиденды начисленные
// графа 24 - sum               Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Платёжное поручение. Сумма
// графа 25 - date              Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Платёжное поручение. Дата
// графа 26 - number            Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ПАО Сбербанк. Платёжное поручение. Номер
// графа 27 - withheldSum       Дивиденды выплаченные. Сумма удержанного налога. Выплачиваемая через ПАО Сбербанк. Платёжное поручение. Сумма	Число /15/
// графа 28 - withheldDate      Дивиденды выплаченные. Сумма удержанного налога. Выплачиваемая через ПАО Сбербанк. Платёжное поручение. Дата
// графа 29 - withheldNumber    Дивиденды выплаченные. Сумма удержанного налога. Выплачиваемая через ПАО Сбербанк. Платёжное поручение. Номер
// графа 30 - postcode          Дивиденды выплаченные. Место нахождения (адрес) получателя. Индекс
// графа 31 - region            Дивиденды выплаченные. Место нахождения (адрес) получателя. Код региона
//     в 0.5.1 изменился тип со строки на справочник - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
// графа 32 - district          Дивиденды выплаченные. Место нахождения (адрес) получателя. Район
// графа 33 - city              Дивиденды выплаченные. Место нахождения (адрес) получателя. Город
// графа 34 - locality          Дивиденды выплаченные. Место нахождения (адрес) получателя. Населённый пункт (село, посёлок и т.п.)
// графа 35 - street            Дивиденды выплаченные. Место нахождения (адрес) получателя. Улица (проспект, переулок и т.д.)
// графа 36 - house             Дивиденды выплаченные. Место нахождения (адрес) получателя. Номер дома (владения)
// графа 37 - housing           Дивиденды выплаченные. Место нахождения (адрес) получателя. Номер корпуса (строения)
// графа 38 - apartment         Дивиденды выплаченные. Место нахождения (адрес) получателя. Номер офиса (квартиры)
// графа 39 - surname           Дивиденды выплаченные. Руководитель организации – получателя. Фамилия
// графа 40 - name              Дивиденды выплаченные. Руководитель организации – получателя. Имя
// графа 41 - patronymic        Дивиденды выплаченные. Руководитель организации – получателя. Отчество
// графа 42 - phone             Дивиденды выплаченные. Контактный телефон	Строка /20/

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
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// редактируемые (графа 2..42)
@Field
def editableColumns = ['emitentName', 'emitentInn', 'all', 'rateZero', 'distributionSum', 'decisionNumber',
                       'decisionDate', 'year', 'firstMonth', 'lastMonth', 'allSum', 'addresseeName', 'inn', 'kpp', 'type',
                       'status', 'birthday','citizenship', 'kind', 'series', 'rate', 'dividends', 'sum', 'date', 'number',
                       'withheldSum', 'withheldDate', 'withheldNumber', 'postcode', 'region', 'district', 'city', 'locality',
                       'street', 'house', 'housing', 'apartment', 'surname', 'name', 'patronymic', 'phone']

// обязательные (графа 1..3, 7..13, 16, 17, 23..25, 27)
@Field
def nonEmptyColumns = ['emitentName', 'emitentInn', 'decisionNumber',
                       'decisionDate', 'year', 'firstMonth', 'lastMonth', 'allSum', 'addresseeName', 'type',
                       'status', 'dividends', 'sum', 'date', 'withheldSum']

// сортировка (графа 7, 8)
@Field
def sortColumns = ['decisionNumber', 'decisionDate']

@Field
def endDate = null

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Алгоритмы заполнения полей формы
void calc() {
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached

    def wasError = [false, false]
    def dateFrom = Date.parse('dd.MM.yyyy', '01.01.1900')
    def dateTo = Date.parse('dd.MM.yyyy', '31.12.2099')
    def period = reportPeriodService.get(formData.reportPeriodId)
    def year = period?.taxPeriod?.year
    def departmentInn = getDepartmentParams()?.INN?.value
    def yearRowMap = [:]

    for (def row in dataRows) {
        def index = row.getIndex()
        def rowYear = row.year?.format('yyyy')?.toInteger()

        // 1. Проверка обязательных полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на заполнение зависимого поля ИНН и КПП (графа 14 и 15)
         if (row.type?.intValue() in [1, 3, 4, 5] && row.status == 1 && (row.inn == null || row.kpp == null)) {
            // с 9 месяцев 2015 года
            logger.error("Строка $index: В случае если графы «%s» равна значению «1» / «3» / «4» / «5» и графа и «%s» равны значению «1», должна быть заполнена графа «%s» и «%s»!",
                    getColumnName(row, 'type'), getColumnName(row, 'status'), getColumnName(row, 'inn'), getColumnName(row, 'kpp'))
        }

        // 3. Проверка паттернов (+ 5. Проверка контрольной суммы)
        if (row.emitentInn && checkPattern(logger, row, 'emitentInn', row.emitentInn, INN_JUR_PATTERN, wasError[1] ? null : INN_JUR_MEANING, true)) {
            // 5. Проверка контрольной суммы
            checkControlSumInn(logger, row, 'emitentInn', row.emitentInn, true)
        } else if (row.emitentInn){
            wasError[1] = true
        }
        if (row.type != 2 && !(row.status?.intValue() in [2, 3])) {
            // если хотя бы одна графа из 16-й и 17-й равна 2. то не проверять 14-ю и 15-ю
            if (row.inn && checkPattern(logger, row, 'inn', row.inn, INN_JUR_PATTERN, wasError[1] ? null : INN_JUR_MEANING, true)) {
                // 5. Проверка контрольной суммы
                checkControlSumInn(logger, row, 'inn', row.inn, true)
            } else if (row.inn){
                wasError[1] = true
            }
            if (row.kpp && !checkPattern(logger, row, 'kpp', row.kpp, KPP_PATTERN, wasError[2] ? null : KPP_MEANING, true)) {
                wasError[2] = true
            }
        }

        // 4. Проверка диапазона дат
        if (row.date && dateFrom < row.date && row.date > dateTo) {
            logger.error("Строка $index: Значение даты графы «%s» должно принимать значение из следующего диапазона: 01.01.1900 - 31.12.2099!", getColumnName(row, 'date'))
        }

        // 6. Проверка значения «Графы 17» (статус получателя)
        if (row.status != null && !(row.status?.intValue() in [1, 2, 3])) {
            logger.error("Строка $index: Графа «%s» заполнен неверно (%s)! Возможные значения: «1», «2», «3»",
                    getColumnName(row, 'status'), row.status)
        }

        // 7. Проверка значения «Графы 16» (тип получателя)
        if (row.type != null && !(row.type?.intValue() in [1, 2, 3, 4, 5])) {
            logger.error("Строка $index: Графа «%s» заполнена неверно (%s)! Возможные значения: «1», «2», «3», «4», «5»",
                    getColumnName(row, 'type'), row.type)
        }

        // 8. Проверка значения «Графы 9» (отчетный год)
        if (row.emitentInn && departmentInn && row.emitentInn == departmentInn && row.year) {
            if (rowYear < year - 4 || year < rowYear) {
                logger.warn("Строка $index: Графа «%s» заполнена неверно (%s)! Для Банка (графа «%s» = ИНН %s формы настроек подразделения формы) по данной графе может быть указан отчетный год формы или предыдущие отчетные года с периодом давности до четырех лет включительно.",
                        getColumnName(row, 'year'), rowYear, getColumnName(row, 'emitentInn'), row.emitentInn)
            }
        }

        // 9. Проверка значения «Графы 10» и «Графы 11» (период распределения)
        ['firstMonth', 'lastMonth'].each { alias ->
            if (row[alias] != null && !(row[alias].intValue() in (1..12))) {
                logger.warn("Строка $index: Графа «%s» заполнена неверно (%s)! Возможные значения: «1», «2», «3», «4», «5», «6», «7», «8», «9», «10», «11», «12»",
                        getColumnName(row, alias), row[alias])
            }
        }

        // 10. Начиная с периода формы «9 месяцев 2015»:
        // Проверка по «Графе 7» должна выполняться только для тех строк, в которых «Графа 3» (ИНН) = Значение атрибута «ИНН» формы настроек подразделения текущей формы.
        // Для каждого уникального значения «Графы  9» (отчетный год) уникально значение «Графы 7» (номер решения)
        // формируем карту строк для годов (если год и номер решения заполнены)
        if (row.emitentInn && departmentInn && row.emitentInn == departmentInn && row.year && row.decisionNumber) {
            if (yearRowMap[rowYear] == null) {
                yearRowMap[rowYear] = []
            }
            yearRowMap[rowYear].add(row)
        }

        // 11. Проверка на заполнение зависимых полей Код региона, Фамилия, Имя (графы 31, 39, 40) - Начиная с периода формы «9 месяцев 2015»
        if (row.type != 2 && row.status == 1 && (!row.region || !row.surname || !row.name)) {
            logger.error("Строка $index: В случае если графа «%s» не равна значению «2» и графа «%s» равна значению «1», должна быть заполнена графа «%s», «%s» и «%s»!",
                    getColumnName(row, 'type'), getColumnName(row, 'status'), getColumnName(row, 'region'), getColumnName(row, 'surname'), getColumnName(row, 'name'))
        }
    }
    // 10. Проверка уникальности значения графы 7 (номер решения)
    yearRowMap.each { yearValue, rows ->
        if (rows.size() > 1) {
            def row = rows[0]
            def rowNumbers = rows.collect{ it.rowNum }
            def decisionNumbers = rows.collect{ it.decisionNumber }.unique() // может быть две строки с одинаковых годом и номером решения
            if (decisionNumbers.size() > 1) {
                logger.error("Строки %s: Неуникальное значение графы «%s» (%s) в рамках «%s» = «%s» для строк Банка (графа «%s» = ИНН %s формы настроек подразделения формы)!",
                        rowNumbers.join(", "), getColumnName(row, 'decisionNumber'), decisionNumbers.join(", "), getColumnName(row, 'year'), yearValue, getColumnName(row, 'emitentInn'), departmentInn)
            }
        }
    }
}

def getDepartmentParams() {
    def filter = "DEPARTMENT_ID = $formDataDepartment.id"
    def departmentParamList = refBookFactory.getDataProvider(33).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
    if (departmentParamList && !departmentParamList.isEmpty()) {
        return departmentParamList.get(0)
    }
    return null
}

def roundValue(BigDecimal value, def int precision) {
    value?.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 42
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)
    try {
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (rowCells == null || !isEmptyCells(rowCells)) {
            logger.error('Вторая строка должна быть пустой')
        }
        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }
            newRows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex))
        }
    } finally {
        reader.close()
    }

    showMessages(newRows, logger)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = [
                'all'             : 4,
                'rateZero'        : 5,
                'distributionSum' : 6,
                'allSum'          : 12,
                'dividends'       : 23,
                'sum'             : 24,
                'withheldSum'     : 27
        ]

        // итоговая строка для сверки сумм
        def totalTmp = formData.createStoreMessagingDataRow()
        def totalColumns = totalColumnsIndexMap.keySet().asList()
        totalColumns.each { alias ->
            totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
        }

        // подсчет итогов
        def dataRows = dataRowHelper.allCached
        calcTotalSum(dataRows, totalTmp, totalColumns)

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumns) {
            def v1 = totalTF[alias]
            def v2 = totalTmp[alias]
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR + " Из файла: $v1, рассчитано: $v2", totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(newRows)
        formDataService.getDataRowHelper(formData).allCached = newRows
    }
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}", fileRowIndex))
        return newRow
    }

    def required = true
    def int colOffset = 1
    def int colIndex

    // графа 2
    colIndex = 2
    newRow.emitentName = pure(rowCells[colIndex])

    // графа 3
    colIndex++
    newRow.emitentInn = pure(rowCells[colIndex])

    // графа 4
    colIndex++
    newRow.all = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 5
    colIndex++
    newRow.rateZero = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 6
    colIndex++
    newRow.distributionSum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 7
    colIndex++
    newRow.decisionNumber = pure(rowCells[colIndex])

    // графа 8
    colIndex++
    newRow.decisionDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // графа 9
    colIndex++
    newRow.year = parseDate(pure(rowCells[colIndex]), "yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // графа 10
    colIndex++
    newRow.firstMonth = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 11
    colIndex++
    newRow.lastMonth = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 12
    colIndex++
    newRow.allSum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 13
    colIndex++
    newRow.addresseeName = pure(rowCells[colIndex])

    // графа 14
    colIndex++
    newRow.inn = pure(rowCells[colIndex])

    // графа 15
    colIndex++
    newRow.kpp = pure(rowCells[colIndex])

    // графа 16
    colIndex++
    newRow.type = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 17
    colIndex++
    newRow.status =  parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 18
    colIndex++
    newRow.birthday = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // графа 19 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
    colIndex++
    newRow.citizenship = getRecordIdImport(10L, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // графа 20 - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
    colIndex++
    newRow.kind = getRecordIdImport(360L, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // графа 21
    colIndex++
    newRow.series = pure(rowCells[colIndex])

    // графа 22
    colIndex++
    newRow.rate = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 23
    colIndex++
    newRow.dividends = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 24
    colIndex++
    newRow.sum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 25
    colIndex++
    newRow.date = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // графа 26
    colIndex++
    newRow.number = pure(rowCells[colIndex])

    // графа 27
    colIndex++
    newRow.withheldSum = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // графа 28
    colIndex++
    newRow.withheldDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // графа 29
    colIndex++
    newRow.withheldNumber = pure(rowCells[colIndex])

    // графа 30
    colIndex++
    newRow.postcode = pure(rowCells[colIndex])

    // графа 31 - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
    colIndex++
    newRow.region = getRecordIdImport(4L, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // графа 32
    colIndex++
    newRow.district = pure(rowCells[colIndex])

    // графа 33
    colIndex++
    newRow.city = pure(rowCells[colIndex])

    // графа 34
    colIndex++
    newRow.locality = pure(rowCells[colIndex])

    // графа 35
    colIndex++
    newRow.street = pure(rowCells[colIndex])

    // графа 36
    colIndex++
    newRow.house = pure(rowCells[colIndex])

    // графа 37
    colIndex++
    newRow.housing = pure(rowCells[colIndex])

    // графа 38
    colIndex++
    newRow.apartment = pure(rowCells[colIndex])

    // графа 39
    colIndex++
    newRow.surname = pure(rowCells[colIndex])

    // графа 40
    colIndex++
    newRow.name = pure(rowCells[colIndex])

    // графа 41
    colIndex++
    newRow.patronymic = pure(rowCells[colIndex])

    // графа 42
    colIndex++
    newRow.phone = pure(rowCells[colIndex])

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

def getNewRow() {
    def newRow = formData.createStoreMessagingDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

void importData() {
    int COLUMN_COUNT = 42
    int HEADER_ROW_COUNT = 9
    String TABLE_START_VALUE = 'Раздел 1'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
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
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
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
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    if (headerRows.isEmpty() || headerRows.size() < rowCount) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[rowCount - 1].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            // раздел 1
            ([(headerRows[0][0]): 'Раздел 1']),
            ([(headerRows[1][0]): '№ п/п']),
            ([(headerRows[1][1]): 'Эмитент']),
            ([(headerRows[2][1]): 'Наименование']),
            ([(headerRows[2][2]): 'ИНН']),

            // раздел 2
            ([(headerRows[0][3]): 'Раздел 2']),
            ([(headerRows[1][3]): 'Дивиденды полученные ПАО Сбербанк']),
            ([(headerRows[2][3]): 'всего']),
            ([(headerRows[2][4]): 'по ставке 0%']),
            ([(headerRows[1][5]): 'Сумма дивидендов Эмитента, распределяемая в пользу всех получателей, уменьшенная на сумму дивидендов, полученных самим Эмитентом']),

            // раздел 3
            ([(headerRows[0][6]): 'Раздел 3']),
            ([(headerRows[1][6]): 'Дивиденды выплаченные']),

            ([(headerRows[2][6]): 'Решение о распределении дивидендов']),
            ([(headerRows[3][6]): 'номер']),
            ([(headerRows[3][7]): 'дата']),

            ([(headerRows[2][8]): 'Отчетный год']),
            ([(headerRows[3][8]): 'Год']),
            ([(headerRows[3][9]): 'Период распределения']),
            ([(headerRows[4][9]): 'первый месяц']),
            ([(headerRows[4][10]): 'последний месяц']),

            ([(headerRows[2][11]): 'Сумма дивидендов']),
            ([(headerRows[2][26]): 'Сумма удержанного налога']),
            ([(headerRows[3][11]): 'Всего']),
            ([(headerRows[3][12]): 'Выплачиваемая через ПАО Сбербанк']),

            ([(headerRows[4][12]): 'Получатель']),
            ([(headerRows[5][12]): 'Наименование (ЮЛ) или ФИО (ФЛ)']),
            ([(headerRows[5][13]): 'ИНН']),
            ([(headerRows[5][14]): 'КПП']),
            ([(headerRows[5][15]): 'тип']),
            ([(headerRows[5][16]): 'статус']),
            ([(headerRows[5][17]): 'Физическое лицо']),
            ([(headerRows[6][17]): 'дата рождения']),
            ([(headerRows[6][18]): 'гражданство']),
            ([(headerRows[6][19]): 'документ']),
            ([(headerRows[7][19]): 'вид']),
            ([(headerRows[7][20]): 'серия и номер']),

            ([(headerRows[4][21]): 'Ставка']),
            ([(headerRows[4][22]): 'Дивиденды начисленные']),
            ([(headerRows[4][23]): 'Платёжное поручение']),
            ([(headerRows[5][23]): 'сумма']),
            ([(headerRows[5][24]): 'дата']),
            ([(headerRows[5][25]): 'номер']),
            ([(headerRows[4][26]): 'Платёжное поручение']),
            ([(headerRows[5][26]): 'сумма']),
            ([(headerRows[5][27]): 'дата']),
            ([(headerRows[5][28]): 'номер']),

            // раздел 4
            ([(headerRows[0][29]): 'Раздел 4']),

            ([(headerRows[2][29]): 'Место нахождения (адрес) получателя']),
            ([(headerRows[3][29]): 'Индекс']),
            ([(headerRows[3][30]): 'Код региона']),
            ([(headerRows[3][31]): 'Район']),
            ([(headerRows[3][32]): 'Город']),
            ([(headerRows[3][33]): 'Населённый пункт (село, посёлок и т.п.)']),
            ([(headerRows[3][34]): 'Улица (проспект, переулок и т.д.)']),
            ([(headerRows[3][35]): 'Номер']),
            ([(headerRows[4][35]): 'дома (владения)']),
            ([(headerRows[4][36]): 'корпуса (строения)']),
            ([(headerRows[4][37]): 'офиса (квартиры)']),

            ([(headerRows[2][38]): 'Руководитель организации – получателя']),
            ([(headerRows[3][38]): 'Фамилия']),
            ([(headerRows[3][39]): 'Имя']),
            ([(headerRows[3][40]): 'Отчество']),
            ([(headerRows[2][41]): 'Контактный телефон'])
    ]
    (0..41).each { index ->
        headerMapping.add(([(headerRows[8][index]): (index + 1).toString()]))
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
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    def required = true

    // графа 2
    def colIndex = 1
    newRow.emitentName = values[colIndex]

    // графа 3
    colIndex++
    newRow.emitentInn = values[colIndex]

    // графа 4
    colIndex++
    newRow.all = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 5
    colIndex++
    newRow.rateZero = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 6
    colIndex++
    newRow.distributionSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 7
    colIndex++
    newRow.decisionNumber = values[colIndex]

    // графа 8
    colIndex++
    newRow.decisionDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // графа 9
    colIndex++
    newRow.year = parseDate(values[colIndex], "yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // графа 10
    colIndex++
    newRow.firstMonth = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 11
    colIndex++
    newRow.lastMonth = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 12
    colIndex++
    newRow.allSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 13
    colIndex++
    newRow.addresseeName = values[colIndex]

    // графа 14
    colIndex++
    newRow.inn = values[colIndex]

    // графа 15
    colIndex++
    newRow.kpp = values[colIndex]

    // графа 16
    colIndex++
    newRow.type = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 17
    colIndex++
    newRow.status = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 18
    colIndex++
    newRow.birthday = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // графа 19 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
    colIndex++
    newRow.citizenship = getRecordIdImport(10L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // графа 20 - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
    colIndex++
    newRow.kind = getRecordIdImport(360L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // графа 21
    colIndex++
    newRow.series = values[colIndex]

    // графа 22
    colIndex++
    newRow.rate = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 23
    colIndex++
    newRow.dividends = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 24
    colIndex++
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 25
    colIndex++
    newRow.date = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // графа 26
    colIndex++
    newRow.number = values[colIndex]

    // графа 27
    colIndex++
    newRow.withheldSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // графа 28
    colIndex++
    newRow.withheldDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // графа 29
    colIndex++
    newRow.withheldNumber = values[colIndex]

    // графа 30
    colIndex++
    newRow.postcode = values[colIndex]

    // графа 31 - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
    colIndex++
    newRow.region = getRecordIdImport(4L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // графа 32..42
    ['district', 'city', 'locality', 'street', 'house', 'housing', 'apartment', 'surname', 'name', 'patronymic', 'phone'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    return newRow
}