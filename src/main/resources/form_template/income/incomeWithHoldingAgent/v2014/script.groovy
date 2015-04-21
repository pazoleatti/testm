package form_template.income.incomeWithHoldingAgent.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов).
 * formTemplateId=419
 *
 * TODO:
 *      - логические проверки в чтз пока не описаны
 */

// графа 1  - rowNum      		№ п/п
// графа 2  - emitentName   	Эмитент. Наименование
// графа 3  - emitentInn    	Эмитент. ИНН
// графа 4  - all  		        Дивиденды полученные ОАО "Сбербанк России". Всего
// графа 5  - rateZero 	    	Дивиденды полученные ОАО "Сбербанк России". По ставке 0%
// графа 6  - distributionSum	Сумма дивидендов Эмитента, распределяемая в пользу всех получателей, уменьшенная на сумму дивидендов, полученных самим Эмитентом
// графа 7  - decisionNumber    Дивиденды выплаченные. Решение о распределении дивидендов. Номер
// графа 8  - decisionDate      Дивиденды выплаченные. Решение о распределении дивидендов. Дата
// графа 9  - year      		Дивиденды выплаченные. Отчетный год. Год
// графа 10 - firstMonth        Дивиденды выплаченные. Отчетный год. Период распределения. Первый месяц
// графа 11 - lastMonth         Дивиденды выплаченные. Отчетный год. Период распределения. Последний месяц
// графа 12 - allSum            Дивиденды выплаченные. Сумма дивидендов. Всего
// графа 13 - addresseeName     Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Получатель. Наименование (ЮЛ) или ФИО (ФЛ)
// графа 14 - inn               Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Получатель. ИНН
// графа 15 - kpp               Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Получатель. КПП
// графа 16 - type              Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Получатель. Тип
// графа 17 - status            Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Получатель. Статус
// графа 18 - birthday          Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Получатель. Физическое лицо. Дата рождения
// графа 19 - citizenship       Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Получатель. Физическое лицо. Гражданство
//     в 0.5.1 изменился тип со строки на справочник - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
// графа 20 - kind              Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Получатель. Физическое лицо. Документ. Вид
//     в 0.5.1 изменился тип со строки на справочник - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
// графа 21 - series            Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Получатель. Физическое лицо. Документ. Серия и номер
// графа 22 - rate              Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Ставка
// графа 23 - dividends         Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Дивиденды начисленные
// графа 24 - sum               Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Платёжное поручение. Сумма
// графа 25 - date              Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Платёжное поручение. Дата
// графа 26 - number            Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Платёжное поручение. Номер
// графа 27 - withheldSum       Дивиденды выплаченные. Сумма удержанного налога. Выплачиваемая через ОАО "Сбербанк России". Платёжное поручение. Сумма	Число /15/
// графа 28 - withheldDate      Дивиденды выплаченные. Сумма удержанного налога. Выплачиваемая через ОАО "Сбербанк России". Платёжное поручение. Дата
// графа 29 - withheldNumber    Дивиденды выплаченные. Сумма удержанного налога. Выплачиваемая через ОАО "Сбербанк России". Платёжное поручение. Номер
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
        formDataService.consolidationSimple(formData, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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

// обязательные (графа 1..17, 23..25, 27)
@Field
def nonEmptyColumns = ['emitentName', 'emitentInn', 'all', 'rateZero', 'distributionSum', 'decisionNumber',
        'decisionDate', 'year', 'firstMonth', 'lastMonth', 'allSum', 'addresseeName', 'inn', 'kpp', 'type',
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // сортировка
    sortRows(dataRows, sortColumns)

    dataRowHelper.save(dataRows)
}

def logicCheck() {
    // TODO (Ramil Timerbaev) в чтз пока не описано
    def dataRows = formDataService.getDataRowHelper(formData)?.getAllCached()

    for (def row in dataRows) {
        def index = row.getIndex()

        // . Проверка обязательных полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)
    }
}

def roundValue(BigDecimal value, def int precision) {
    value?.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Раздел 1', null, 42, 9)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 42, 9)

    def headerMapping = [
            // раздел 1
            (xml.row[0].cell[0]) : 'Раздел 1',
            (xml.row[1].cell[0]) : '№ п/п',
            (xml.row[1].cell[1]) : 'Эмитент',
            (xml.row[2].cell[1]) : 'Наименование',
            (xml.row[2].cell[2]) : 'ИНН',

            // раздел 2
            (xml.row[0].cell[3]) : 'Раздел 2',
            (xml.row[1].cell[3]) : 'Дивиденды полученные ОАО "Сбербанк России"',
            (xml.row[2].cell[3]) : 'всего',
            (xml.row[2].cell[4]) : 'по ставке 0%',
            (xml.row[1].cell[5]) : 'Сумма дивидендов Эмитента, распределяемая в пользу всех получателей, уменьшенная на сумму дивидендов, полученных самим Эмитентом',

            // раздел 3
            (xml.row[0].cell[6]) : 'Раздел 3',
            (xml.row[1].cell[6]) : 'Дивиденды выплаченные',

            (xml.row[2].cell[6]) : 'Решение о распределении дивидендов',
            (xml.row[3].cell[6]) : 'номер',
            (xml.row[3].cell[7]) : 'дата',

            (xml.row[2].cell[8]) : 'Отчетный год',
            (xml.row[3].cell[8]) : 'Год',
            (xml.row[3].cell[9]) : 'Период распределения',
            (xml.row[4].cell[9]) : 'первый месяц',
            (xml.row[4].cell[10]): 'последний месяц',

            (xml.row[2].cell[11]): 'Сумма дивидендов',
            (xml.row[2].cell[26]): 'Сумма удержанного налога',
            (xml.row[3].cell[11]): 'Всего',
            (xml.row[3].cell[12]): 'Выплачиваемая через ОАО "Сбербанк России"',

            (xml.row[4].cell[12]): 'Получатель',
            (xml.row[5].cell[12]): 'Наименование (ЮЛ) или ФИО (ФЛ)',
            (xml.row[5].cell[13]): 'ИНН',
            (xml.row[5].cell[14]): 'КПП',
            (xml.row[5].cell[15]): 'тип',
            (xml.row[5].cell[16]): 'статус',
            (xml.row[5].cell[17]): 'Физическое лицо',
            (xml.row[6].cell[17]): 'дата рождения',
            (xml.row[6].cell[18]): 'гражданство',
            (xml.row[6].cell[19]): 'документ',
            (xml.row[7].cell[19]): 'вид',
            (xml.row[7].cell[20]): 'серия и номер',

            (xml.row[4].cell[21]): 'Ставка',
            (xml.row[4].cell[22]): 'Дивиденды начисленные',
            (xml.row[4].cell[23]): 'Платёжное поручение',
            (xml.row[5].cell[23]): 'сумма',
            (xml.row[5].cell[24]): 'дата',
            (xml.row[5].cell[25]): 'номер',
            (xml.row[4].cell[26]): 'Платёжное поручение',
            (xml.row[5].cell[26]): 'сумма',
            (xml.row[5].cell[27]): 'дата',
            (xml.row[5].cell[28]): 'номер',

            // раздел 4
            (xml.row[0].cell[29]): 'Раздел 4',

            (xml.row[2].cell[29]): 'Место нахождения (адрес) получателя',
            (xml.row[3].cell[29]): 'Индекс',
            (xml.row[3].cell[30]): 'Код региона',
            (xml.row[3].cell[31]): 'Район',
            (xml.row[3].cell[32]): 'Город',
            (xml.row[3].cell[33]): 'Населённый пункт (село, посёлок и т.п.)',
            (xml.row[3].cell[34]): 'Улица (проспект, переулок и т.д.)',
            (xml.row[3].cell[35]): 'Номер',
            (xml.row[4].cell[35]): 'дома (владения)',
            (xml.row[4].cell[36]): 'корпуса (строения)',
            (xml.row[4].cell[37]): 'офиса (квартиры)',

            (xml.row[2].cell[38]): 'Руководитель организации – получателя',
            (xml.row[3].cell[38]): 'Фамилия',
            (xml.row[3].cell[39]): 'Имя',
            (xml.row[3].cell[40]): 'Отчество',
            (xml.row[2].cell[41]): 'Контактный телефон',
    ]
    (0..41).each { index ->
        headerMapping.put((xml.row[8].cell[index]), (index + 1).toString())
    }

    checkHeaderEquals(headerMapping)

    // добавить данные в форму
    addData(xml, 9)
}

void addData(def xml, headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1
    def required = true

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow < headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)

        def xmlIndexCol

        // графа 2
        xmlIndexCol = 1
        newRow.emitentName = row.cell[xmlIndexCol].text()

        // графа 3
        xmlIndexCol++
        newRow.emitentInn = row.cell[xmlIndexCol].text()

        // графа 4
        xmlIndexCol++
        newRow.all = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 5
        xmlIndexCol++
        newRow.rateZero = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 6
        xmlIndexCol++
        newRow.distributionSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 7
        xmlIndexCol++
        newRow.decisionNumber = row.cell[xmlIndexCol].text()

        // графа 8
        xmlIndexCol++
        newRow.decisionDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 9
        xmlIndexCol++
        newRow.year = parseDate(row.cell[xmlIndexCol].text(), "yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 10
        xmlIndexCol++
        newRow.firstMonth = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 11
        xmlIndexCol++
        newRow.lastMonth = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 12
        xmlIndexCol++
        newRow.allSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 13
        xmlIndexCol++
        newRow.addresseeName = row.cell[xmlIndexCol].text()

        // графа 14
        xmlIndexCol++
        newRow.inn = row.cell[xmlIndexCol].text()

        // графа 15
        xmlIndexCol++
        newRow.kpp = row.cell[xmlIndexCol].text()

        // графа 16
        xmlIndexCol++
        newRow.type = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 17
        xmlIndexCol++
        newRow.status = row.cell[xmlIndexCol].text()

        // графа 18
        xmlIndexCol++
        newRow.birthday = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 19 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
        xmlIndexCol++
        newRow.citizenship = getRecordIdImport(10L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // графа 20 - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
        xmlIndexCol++
        newRow.kind = getRecordIdImport(360L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // графа 21
        xmlIndexCol++
        newRow.series = row.cell[xmlIndexCol].text()

        // графа 22
        xmlIndexCol++
        newRow.rate = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 23
        xmlIndexCol++
        newRow.dividends = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 24
        xmlIndexCol++
        newRow.sum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 25
        xmlIndexCol++
        newRow.date = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 26
        xmlIndexCol++
        newRow.number = row.cell[xmlIndexCol].text()

        // графа 27
        xmlIndexCol++
        newRow.withheldSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 28
        xmlIndexCol++
        newRow.withheldDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // графа 29
        xmlIndexCol++
        newRow.withheldNumber = row.cell[xmlIndexCol].text()

        // графа 30
        xmlIndexCol++
        newRow.postcode = row.cell[xmlIndexCol].text()

        // графа 31 - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
        xmlIndexCol++
        newRow.region = getRecordIdImport(4L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // графа 32
        xmlIndexCol++
        newRow.district = row.cell[xmlIndexCol].text()

        // графа 33
        xmlIndexCol++
        newRow.city = row.cell[xmlIndexCol].text()

        // графа 34
        xmlIndexCol++
        newRow.locality = row.cell[xmlIndexCol].text()

        // графа 35
        xmlIndexCol++
        newRow.street = row.cell[xmlIndexCol].text()

        // графа 36
        xmlIndexCol++
        newRow.house = row.cell[xmlIndexCol].text()

        // графа 37
        xmlIndexCol++
        newRow.housing = row.cell[xmlIndexCol].text()

        // графа 38
        xmlIndexCol++
        newRow.apartment = row.cell[xmlIndexCol].text()

        // графа 39
        xmlIndexCol++
        newRow.surname = row.cell[xmlIndexCol].text()

        // графа 40
        xmlIndexCol++
        newRow.name = row.cell[xmlIndexCol].text()

        // графа 41
        xmlIndexCol++
        newRow.patronymic = row.cell[xmlIndexCol].text()

        // графа 42
        xmlIndexCol++
        newRow.phone = row.cell[xmlIndexCol].text()

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

void importTransportData() {
    int COLUMN_COUNT = 42
    int ROW_MAX = 1000
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    checkBeforeGetXml(ImportInputStream, UploadFileName)

    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()

    String[] rowCells
    int countEmptyRow = 0	// количество пустых строк
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++

        def isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
        if (isEmptyRow) {
            if (countEmptyRow > 0) {
                // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
                // итоговая строка тф
                rowCells = reader.readNext()
                isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
                totalTF = (isEmptyRow ? null : getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, ++rowIndex))
                break
            }
            countEmptyRow++
            continue
        }

        // если еще не было пустых строк, то это первая строка - заголовок (пропускается)
        // обычная строка
        if (countEmptyRow != 0 && !addRow(newRows, rowCells, COLUMN_COUNT, fileRowIndex, ++rowIndex)) {
            break
        }

        // периодически сбрасываем строки
        if (newRows.size() > ROW_MAX) {
            dataRowHelper.insert(newRows, dataRowHelper.allCached.size() + 1)
            newRows.clear()
        }
    }
    reader.close()

    if (newRows.size() != 0) {
        dataRowHelper.insert(newRows, dataRowHelper.allCached.size() + 1)
    }

    // сравнение итогов
    if (totalTF) {
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
        def totalTmp = formData.createDataRow()
        totalColumnsIndexMap.keySet().asList().each { alias ->
            totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
        }

        // подсчет итогов
        def dataRows = dataRowHelper.allCached
        for (def row : dataRows) {
            if (row.getAlias()) {
                continue
            }
            totalColumnsIndexMap.keySet().asList().each { alias ->
                def value1 = totalTmp.getCell(alias).value
                def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
                totalTmp.getCell(alias).setValue(value1 + value2, null)
            }
        }

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = totalTmp.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
    }
}

/** Добавляет строку в текущий буфер строк. */
boolean addRow(def rows, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    def newRow = getNewRow(rowCells, columnCount, fileRowIndex, rowIndex)
    if (newRow == null) {
        return false
    }
    rows.add(newRow)
    return true
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
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
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
    newRow.status = pure(rowCells[colIndex])

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
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}