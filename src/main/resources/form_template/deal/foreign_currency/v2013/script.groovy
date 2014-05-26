package form_template.deal.foreign_currency.v2013

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 390 - Купля-продажа иностранной валюты (15)
 *
 * formTemplateId=390
 *
 * @author Stanislav Yasinskiy
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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
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
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
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

// Редактируемые атрибуты
@Field
def editableColumns = ['fullName', 'docNum', 'docDate', 'dealNumber', 'dealDate', 'currencyCode', 'countryDealCode',
        'incomeSum', 'outcomeSum', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'inn', 'countryName', 'countryCode', 'price', 'total']

// Группируемые атрибуты
@Field
def groupColumns = ['fullName', 'docNum', 'docDate', 'currencyCode', 'countryDealCode']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'fullName', 'countryName', 'currencyCode', 'countryDealCode', 'price', 'total',
        'dealDoneDate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
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
    if (!fileName.endsWith('.xls') && !fileName.endsWith('.xlsx') && !fileName.endsWith('.xlsm')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls/xlsx/xlsm!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    return xml
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, false)

        def docDateCell = row.getCell('docDate')

        // Проверка заполнения доходов и расходов Банка
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            def msgIn = incomeSumCell.column.name
            def msgOut = outcomeSumCell.column.name
            logger.warn("Строка $rowNum: Графы «$msgIn» и «$msgOut» не могут быть одновременно заполнены!")
        }
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            def msgIn = incomeSumCell.column.name
            def msgOut = outcomeSumCell.column.name
            logger.warn("Строка $rowNum: Одна из граф «$msgIn» и «$msgOut» должна быть заполнена!")
        }
        // Корректность даты заключения сделки
        def dealDateCell = row.getCell('dealDate')
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }
        // Проверка заполнения стоимости сделки
        if (row.total != row.price) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('total').column.name
            logger.warn("Строка $rowNum: «$msg1» не может отличаться от «$msg2»!")
        }
        // Корректность дат сделки dealDate - 9гр, dealDoneDate - 16гр
        def dealDoneDate = row.getCell('dealDoneDate')
        if (dealDateCell.value > dealDoneDate.value) {
            def msg1 = dealDoneDate.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }
    }

    if (formData.kind == FormDataKind.CONSOLIDATED) {
        checkItog(dataRows)
    }
}

// Проверки подитоговых сумм
void checkItog(def dataRows) {
    def testRows = dataRows.findAll { it -> it.getAlias() == null }
    addAllAliased(testRows, new CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, testRows)
        }
    }, groupColumns)
    // Рассчитанные строки итогов
    def testItogRows = testRows.findAll { it -> it.getAlias() != null }
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it -> it.getAlias() != null }

    // TODO в 0.3.7 перенести в ScriptUtils
    // Последняя строка должна быть подитоговой
    if (testItogRows.size() > itogRows.size() && dataRows.size() != 0 && dataRows.get(dataRows.size() - 1).getAlias() == null) {
        String groupCols = getValuesByGroupColumn(dataRows.get(dataRows.size() - 1));
        if (groupCols != null) {
            logger.error(GROUP_WRONG_ITOG, groupCols);
        }
    }

    checkItogRows(dataRows, testItogRows, itogRows, groupColumns, logger, new GroupString() {
        @Override
        String getString(DataRow<Cell> row) {
            return getValuesByGroupColumn(row)
        }
    }, new CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            if (row1.price != row2.price) {
                return getColumnName(row1, 'price')
            }
            if (row1.total != row2.total) {
                return getColumnName(row1, 'total')
            }
            return null
        }
    })
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Сортировка
    sortRows(dataRows, groupColumns)

    def index = 1
    for (row in dataRows) {
        // Порядковый номер строки
        row.rowNumber = index++

        // Расчет поля "Цена"
        if (row.incomeSum != null && row.outcomeSum == null) {
            row.price = row.incomeSum
        } else if (row.incomeSum == null && row.outcomeSum != null) {
            row.price = row.outcomeSum
        }

        // Расчет поля "Итого"
        row.total = row.price

        // Расчет полей зависимых от справочников
        row.countryName = getRefBookValue(9, row.fullName)?.COUNTRY?.referenceValue
    }

    // Добавление подитов
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        addAllAliased(dataRows, new CalcAliasRow() {
            @Override
            DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
                return calcItog(i, dataRows)
            }
        }, groupColumns)
    }

    dataRowHelper.save(dataRows)
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 13
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    def BigDecimal priceItg = 0, totalItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        def row = dataRows.get(j)
        def price = row.price
        def total = row.total

        priceItg += price != null ? price : 0
        totalItg += total != null ? total : 0
    }
    newRow.price = priceItg
    newRow.total = totalItg

    return newRow
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def sep = ", "
    def StringBuilder builder = new StringBuilder()
    def map = getRefBookValue(9, row.fullName)
    if (map != null)
        builder.append(map.NAME?.stringValue).append(sep)
    if (row.docNum != null)
        builder.append(row.docNum).append(sep)
    if (row.docDate != null)
        builder.append(formatDate(row.docDate, 'dd.MM.yyyy')).append(sep)
    map = getRefBookValue(15, row.currencyCode)
    if (map != null)
        builder.append(map.CODE_2.stringValue).append(sep)
    map = getRefBookValue(10, row.countryDealCode)
    if (map != null)
        builder.append(map.CODE_2.stringValue).append(sep)

    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    return retVal.substring(0, retVal.length() - 2)
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML('Общие сведения о контрагенте - юридическом лице', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 17, 3)

    def headerMapping = [
            (xml.row[1].cell[3]): getColumnName(tmpRow, 'inn'),
            (xml.row[1].cell[4]): getColumnName(tmpRow, 'countryName'),
            (xml.row[1].cell[5]): getColumnName(tmpRow, 'countryCode'),
            (xml.row[1].cell[6]): getColumnName(tmpRow, 'docNum'),
            (xml.row[1].cell[7]): getColumnName(tmpRow, 'docDate'),
            (xml.row[1].cell[8]): getColumnName(tmpRow, 'dealNumber'),
            (xml.row[1].cell[9]): getColumnName(tmpRow, 'dealDate'),
            (xml.row[1].cell[10]): getColumnName(tmpRow, 'currencyCode'),
            (xml.row[1].cell[11]): getColumnName(tmpRow, 'countryDealCode'),
            (xml.row[1].cell[12]): getColumnName(tmpRow, 'incomeSum'),
            (xml.row[1].cell[13]): getColumnName(tmpRow, 'outcomeSum'),
            (xml.row[1].cell[14]): getColumnName(tmpRow, 'price'),
            (xml.row[1].cell[15]): getColumnName(tmpRow, 'total'),
            (xml.row[1].cell[17]): getColumnName(tmpRow, 'dealDoneDate'),
            (xml.row[2].cell[3]): 'гр. 3',
            (xml.row[2].cell[4]): 'гр. 4.1',
            (xml.row[2].cell[5]): 'гр. 4.2',
            (xml.row[2].cell[17]): 'гр. 15'
    ]
    (6..15).each{
        headerMapping.put(xml.row[2].cell[it], 'гр. ' + (it-1))
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // пропустить шапку таблицы
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        newRow.rowNumber = xmlIndexRow - headRowCount
        xmlIndexCol++

        // графа fix
        xmlIndexCol++

        // графа 2
        newRow.fullName = getRecordIdImport(9, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        def map = getRefBookValue(9, newRow.fullName)
        xmlIndexCol++

        // графа 3
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO?.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO?.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xlsIndexRow}, столбец ${xmlIndexCol + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(9).getName()+"»!")
            }
        }
        xmlIndexCol++

        // графа 4.1
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            map = getRefBookValue(10, map.COUNTRY.referenceValue)
            if ((text != null && !text.isEmpty() && !text.equals(map?.NAME?.stringValue)) || ((text == null || text.isEmpty()) && map?.NAME?.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xlsIndexRow}, столбец ${xmlIndexCol + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        xmlIndexCol++

        // графа 4.2
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE?.stringValue)) || ((text == null || text.isEmpty()) && map.CODE?.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xlsIndexRow}, столбец ${xmlIndexCol + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        xmlIndexCol++

        // графа 5
        newRow.docNum = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 6
        newRow.docDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 7
        newRow.dealNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        // графа 8
        newRow.dealDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 9
        newRow.currencyCode = getRecordIdImport(15, 'CODE_2', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 10
        newRow.countryDealCode = getRecordIdImport(10, 'CODE_2', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 11
        newRow.incomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 12
        newRow.outcomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 13
        xmlIndexCol++
        // графа 14
        xmlIndexCol++
        // графа fix
        xmlIndexCol++

        // графа 15
        newRow.dealDoneDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}