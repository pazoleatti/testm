package form_template.income.output2

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import groovy.transform.Field

/**
 * Расчет налога на прибыль с доходов, удерживаемого налоговым агентом
 *
 * formTemplateId=307
 */
// графа 1  - title
// графа 2  - zipCode
// графа 3  - subdivisionRF
// графа 4  - area
// графа 5  - city
// графа 6  - region
// графа 7  - street
// графа 8  - homeNumber
// графа 9  - corpNumber
// графа 10 - apartment
// графа 11 - surname
// графа 12 - name
// графа 13 - patronymic
// графа 14 - phone
// графа 15 - dividendDate
// графа 16 - sumDividend
// графа 17 - sumTax

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CALCULATE:
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
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def editableColumns = ['title', 'zipCode', 'subdivisionRF', 'area', 'city', 'region', 'street', 'homeNumber',
        'corpNumber', 'apartment', 'surname', 'name', 'patronymic', 'phone', 'dividendDate', 'sumDividend', 'sumTax']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['title', 'subdivisionRF', 'surname', 'name', 'dividendDate', 'sumDividend', 'sumTax']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

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

void checkCreation() {
    if (formData.kind != FormDataKind.ADDITIONAL) {
        logger.error('Нельзя создавать форму с типом ${formData.kind?.name}')
    }
    formDataService.checkUnique(formData, logger)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (row in dataRows) {
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        String zipCode = (String) row.zipCode;
        if (zipCode == null || zipCode.length() != 6 || !zipCode.matches('[0-9]*')) {
            logger.error("Строка $rowNum: Неправильно указан почтовый индекс!")
        }

        // Проверки соответствия НСИ
        checkNSI(4, row, "subdivisionRF")
    }
}

void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }
    if (!fileName.contains('.xls')) {
        logger.error('Формат файла должен быть *.xls')
        return
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    def columnsCount = 22
    def xmlString = importService.getData(is, fileName, 'windows-1251', '№ стр.', null, columnsCount)
    if (xmlString == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    checkHeaderSize(xml.row.size(), xml.row[0].cell.size(), 4, 22)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ стр.',
            (xml.row[0].cell[1]): 'Код территориального банка',
            (xml.row[0].cell[2]): 'Наименование территориального банка',
            (xml.row[0].cell[3]): 'Наименование получателя',
            (xml.row[0].cell[4]): 'ИНН',
            (xml.row[0].cell[5]): 'КПП',
            (xml.row[0].cell[6]): 'Юридический адрес ( место жительства )',
            (xml.row[0].cell[7]): 'Юридический адрес ( место нахождения )',
            (xml.row[1].cell[7]): 'Почтовый индекс',
            (xml.row[1].cell[8]): 'Субъект Российской Федерации',
            (xml.row[2].cell[8]): 'Код',
            (xml.row[2].cell[9]): 'Наименование субъекта РФ',
            (xml.row[1].cell[10]): 'Район',
            (xml.row[1].cell[11]): 'Город',
            (xml.row[1].cell[12]): 'Населенный пункт (село, поселок и т.п.)',
            (xml.row[1].cell[13]): 'Улица (проспект, переулок и т.д.)',
            (xml.row[1].cell[14]): 'Номер дома (владения)',
            (xml.row[1].cell[15]): 'Номер корпуса (строения)',
            (xml.row[1].cell[16]): 'Номер офиса (квартиры)',
            (xml.row[0].cell[17]): 'Руководитель организации (Ф.И.О.)',
            (xml.row[0].cell[18]): 'Контактный телефон',
            (xml.row[0].cell[19]): 'Дата перечисления дивидентов',
            (xml.row[0].cell[20]): 'Сумма дивидентов',
            (xml.row[0].cell[21]): 'Сумма налога'
    ]

    checkHeaderEquals(headerMapping)

    // добавить данные в форму
    addData(xml, 4)
}

void addData(def xml, headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    // количество графов в таблице
    def columnCount = 22

    def tmp
    def rows = []

    def indexRow = 0

    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= headRowCount) {
            continue
        }

        // проверить по грн итоговая ли это строка
        tmp = (row.cell[0] != null ? row.cell[0].text() : null)
        if (tmp != null && tmp.contains('Итог')) {
            continue
        }

        if (row.cell.size() >= columnCount) {
            def newRow = formData.createDataRow()
            editableColumns.each {
                newRow.getCell(it).editable = true
                newRow.getCell(it).setStyleAlias('Редактируемая')
            }

            // графа 1
            newRow.title = row.cell[2].text()

            // графа 2
            newRow.zipCode = row.cell[7].text()

            // графа 3 - справочник "Коды субъектов Российской Федерации"
            newRow.subdivisionRF = getRecordIdImport(4, 'CODE', row.cell[8].text(), indexRow, 8, true)

            // графа 4
            newRow.area = row.cell[10].text()

            // графа 5
            newRow.city = row.cell[11].text()

            // графа 6
            newRow.region = row.cell[12].text()

            // графа 7
            newRow.street = row.cell[13].text()

            // графа 8
            newRow.homeNumber = row.cell[14].text()

            // графа 9
            newRow.corpNumber = row.cell[15].text()

            // графа 10
            newRow.apartment = row.cell[16].text()

            // разбиение ФИО
            def fio = row.cell[17].text()
            if (fio != null &&
                    !"".equals(fio.trim()) &&
                    !'данные не представлены'.equals(fio.trim().toLowerCase())) {
                tmp = fio.trim().split(',', 3)
                // графа 11
                newRow.surname = tmp[0]
                // графа 12
                newRow.name = (tmp.size() > 1 ? tmp[1] : null)
                // графа 13
                newRow.patronymic = (tmp.size() > 2 ? tmp[2] : null)
            }

            // графа 14
            newRow.phone = row.cell[18].text()

            // графа 15
            newRow.dividendDate = parseDate(row.cell[19].text(), "dd.MM.yyyy", indexRow, 19, logger, false)

            // графа 16
            newRow.sumDividend = parseNumber(row.cell[20].text(), indexRow, 20, logger, false)

            // графа 17
            newRow.sumTax = parseNumber(row.cell[21].text(), indexRow, 21, logger, false)

            rows.add(newRow)
        }
    }
    dataRowHelper.save(rows)
}