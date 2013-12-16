package form_template.income.output2

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper
import java.text.SimpleDateFormat

/**
 * 6.3.2    Расчет налога на прибыль с доходов, удерживаемого налоговым агентом
 *
 * formTemplateId=307
 */

DataRowHelper getData() {
    DataRowHelper dataRowsHelper = null
    if (formData.id != null) {
        dataRowsHelper = formDataService.getDataRowHelper(formData)
    }
    return dataRowsHelper
}

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkDecl()
        logicCheck()
        break
    case FormDataEvent.MOVE_PREPARED_TO_CREATED:
        break
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkDecl()
        logicCheck()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:
        checkDecl()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.IMPORT :
        importData()
        break
}

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

void deleteRow() {
    if (currentDataRow != null) {
        data.delete(currentDataRow)
    }
}

void addRow() {
    data.insert(getNewRow(), getRows(data).size() + 1)
}

/**
 * Проверяет уникальность в отчётном периоде и вид
 */
void checkUniq() {

    FormData findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
    if (formData.kind != FormDataKind.ADDITIONAL) {
        logger.error('Нельзя создавать форму с типом ${formData.kind?.name}')
    }
}

/**
 * Проверка наличия декларации для текущего department
 */
void checkDecl() {
    declarationType = 2;    // Тип декларации которую проверяем(Налог на прибыль)
    declaration = declarationService.find(declarationType, formData.getDepartmentId(), formData.getReportPeriodId())
    if (declaration != null && declaration.isAccepted()) {
        logger.error("Декларация банка находиться в статусе принята")
    }
}

void logicCheck() {
    // справочник "Коды субъектов Российской Федерации"
    def refDataProvider = refBookFactory.getDataProvider(4)
    def rowNum = 0
    for (row in getRows(data)) {
        rowNum++
        for (alias in ['title', 'subdivisionRF', 'surname', 'name', 'dividendDate', 'sumDividend', 'sumTax']) {
            if (row.getCell(alias).value == null) {
                logger.error("Строка $rowNum: Поле " + row.getCell(alias).column.name.replace('%', '%%') + " не заполнено")
            }
        }

        String zipCode = (String) row.zipCode;
        if (zipCode == null || zipCode.length() != 6 || !zipCode.matches('[0-9]*')) {
            logger.error("Строка $rowNum: Неправильно указан почтовый индекс!")
        }
        if (!logger.containsLevel(LogLevel.ERROR)) {
            // графа 3 - справочник "Коды субъектов Российской Федерации"
            def record = refDataProvider.getRecordData(row.subdivisionRF)
            if (record == null) {
                logger.warn("Строка $rowNum: Неверное наименование субъекта РФ!")
            }
        }
    }
}

/**
 * Вставить новую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}

/**
 * Получение импортируемых данных.
 */
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

    // количество строк в шапке
    def headRowCount = 3

    // проверка заголовка таблицы
    if (!checkTableHead(xml, headRowCount)) {
        logger.error('Заголовок таблицы не соответствует требуемой структуре!')
        return
    }

    try {
        // добавить данные в форму
        addData(xml, headRowCount)
    } catch(Exception e) {
        logger.error('Проверка файла: ' + e.message)
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
void addData(def xml, headRowCount) {
    def data = getData()

    // количество графов в таблице
    def columnCount = 22

    def tmp
    def newRows = []

    // справочник "Коды субъектов Российской Федерации"
    def refDataProvider = refBookFactory.getDataProvider(4)

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
            // проверить по номеру договора повторяющиеся записи
            def newRow = getNewRow()

            // графа 1
            newRow.title = row.cell[2].text()

            // графа 2
            newRow.zipCode = row.cell[7].text()

            // графа 3 - справочник "Коды субъектов Российской Федерации"
            def records = refDataProvider.getRecords(new Date(), null, "CODE = '" + row.cell[8].text() + "'", null)
            if (records == null || records.getRecords().isEmpty()) {
                logger.error("Проверка файла: Строка $indexRow столбец 9 содержит неверный код субъекта РФ!")
            } else {
                def record = records.getRecords().getAt(0)
                if (record == null) {
                    logger.error("Проверка файла: Строка $indexRow столбец 9 содержит неверный код субъекта РФ!")
                }
                newRow.subdivisionRF = getValue(record, 'record_id')
            }

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
            newRow.dividendDate = getDate(row.cell[19].text(), indexRow, 15)

            // графа 16
            newRow.sumDividend = getNumber(row.cell[20].text(), indexRow, 16)

            // графа 17
            newRow.sumTax = getNumber(row.cell[21].text(), indexRow, 17)

            newRows.add(newRow)
        }
    }
    data.clear()
    newRows.each { newRow ->
        insert(data, newRow)
    }
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value, int indexRow, int indexCell) {
    if (value == null) {
        return null
    }
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    // поменять запятую на точку и убрать пробелы
    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
    try {
        return new BigDecimal(tmp)
    } catch (Exception e) {
        throw new Exception("Проверка файла: Строка $indexRow столбец ${indexCell + 6} содержит недопустимый тип данных!")
    }
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value, int indexRow, int indexCell) {
    if (value == null || value == '') {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    try {
        return format.parse(value)
    } catch (Exception e) {
        throw new Exception("Проверка файла: Строка $indexRow столбец ${indexCell + 6} содержит недопустимый тип данных!")
    }
}

/**
 * Получить новую стролу с заданными стилями.
 */
DataRow<Cell> getNewRow() {
    DataRow<Cell> row = formData.createDataRow()

    for (alias in ['title', 'zipCode', 'subdivisionRF', 'area', 'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment',
            'surname', 'name', 'patronymic', 'phone', 'dividendDate', 'sumDividend', 'sumTax']) {
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias('Редактируемая')
    }
    return row
}

/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, def headRowCount) {
    def colCount = 22
    // проверить количество строк и голонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (xml.row[0].cell[0] == '№ стр.' &&
            xml.row[0].cell[1] == 'Код территориального банка' &&
            xml.row[0].cell[2] == 'Наименование территориального банка' &&
            xml.row[0].cell[3] == 'Наименование получателя' &&
            xml.row[0].cell[4] == 'ИНН' &&
            xml.row[0].cell[5] == 'КПП' &&
            xml.row[0].cell[6] == 'Юридический адрес ( место жительства )' &&
            xml.row[0].cell[7] == 'Юридический адрес ( место нахождения )' &&
            xml.row[1].cell[7] == 'Почтовый индекс' &&
            xml.row[1].cell[8] == 'Субъект Российской Федерации' &&
            xml.row[2].cell[8] == 'Код' &&
            xml.row[2].cell[9] == 'Наименование субъекта РФ' &&
            xml.row[1].cell[10] == 'Район' &&
            xml.row[1].cell[11] == 'Город' &&
            xml.row[1].cell[12] == 'Населенный пункт (село, поселок и т.п.)' &&
            xml.row[1].cell[13] == 'Улица (проспект, переулок и т.д.)' &&
            xml.row[1].cell[14] == 'Номер дома (владения)' &&
            xml.row[1].cell[15] == 'Номер корпуса (строения)' &&
            xml.row[1].cell[16] == 'Номер офиса (квартиры)' &&
            xml.row[0].cell[17] == 'Руководитель организации (Ф.И.О.)' &&
            xml.row[0].cell[18] == 'Контактный телефон' &&
            xml.row[0].cell[19] == 'Дата перечисления дивидентов' &&
            xml.row[0].cell[20] == 'Сумма дивидентов' &&
            xml.row[0].cell[21] == 'Сумма налога')
    return result
}

/**
 * Получить значение атрибута строки справочника.

 * @param record строка справочника
 * @param alias алиас
 */
def getValue(def record, def alias) {
    def value = record.get(alias)
    switch (value.getAttributeType()) {
        case RefBookAttributeType.DATE :
            return value.getDateValue()
        case RefBookAttributeType.NUMBER :
            return value.getNumberValue()
        case RefBookAttributeType.STRING :
            return value.getStringValue()
        case RefBookAttributeType.REFERENCE :
            return value.getReferenceValue()
    }
    return null
}

def getRows(def data) {
    return data.getAllCached();
}