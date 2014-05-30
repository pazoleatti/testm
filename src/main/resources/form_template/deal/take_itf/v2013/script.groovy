package form_template.deal.take_itf.v2013

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * 403 - Привлечение ИТФ и аккредитивов (25)
 * formTemplateId = 403
 * v. 2014
 */

// 1. rowNumber       № п/п
// 2. fullName        Полное наименование с указанием ОПФ
// 3. inn             ИНН/КИО
// 4. countryName     Страна регистрации
// 5. docNumber       Номер договора
// 6. docDate         Дата договора
// 7. dealNumber      Номер сделки
// 8. dealDate        Дата сделки
// 9. outcomeSum      Сумма расходов Банка по данным бухгалтерского учета, руб.
// 10. price          Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
// 11. total          Итого стоимость без учета НДС, акцизов и пошлины, руб.
// 12. dealDoneDate   Дата совершения сделки

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
def editableColumns = ['fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'outcomeSum', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'inn', 'countryName', 'price', 'total']

// Группируемые атрибуты
@Field
def groupColumns = ['fullName', 'docNumber', 'docDate']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate',
        'outcomeSum', 'price', 'total', 'dealDoneDate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

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

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
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

        checkNonEmptyColumns(row, rowNum, ['docNumber', 'docDate'], logger, true)
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns - ['docNumber', 'docDate'], logger, false)

        // Проверка цены
        if (row.outcomeSum != row.price) {
            logger.warn("Строка $rowNum: Цена не может отличаться от суммы расходов Банка!")
        }

        // Проверка стоимости
        if (row.outcomeSum != row.total) {
            logger.warn("Строка $rowNum: Стоимость не может отличаться от суммы расходов Банка!")
        }

        // Корректность даты сделки
        if (row.dealDoneDate < row.docDate) {
            logger.warn("Строка $rowNum: Дата совершения сделки не может быть меньше даты договора!")
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
            if (row1.outcomeSum != row2.outcomeSum) {
                return getColumnName(row1, 'outcomeSum')
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

        // Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
        row.price = row.outcomeSum

        // Итого стоимость без учета НДС, акцизов и пошлины, руб.
        row.total = row.outcomeSum
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

    // Если нет сортировки и подитогов, то dataRowHelper.update(dataRows)
    dataRowHelper.save(dataRows)
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def newRow = formData.createDataRow()

    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('itog').colSpan = 8

    // Расчеты подитоговых значений
    def BigDecimal outcomeSumItg = 0, totalItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        def row = dataRows.get(j)
        def outcomeSum = row.outcomeSum
        def total = row.total

        outcomeSumItg += outcomeSum != null ? outcomeSum : 0
        totalItg += total != null ? total : 0
    }
    newRow.outcomeSum = outcomeSumItg
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
    if (row.docNumber != null)
        builder.append(row.docNumber).append(sep)
    if (row.docDate != null)
        builder.append(row.docDate).append(sep)
    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    return retVal.substring(0, retVal.length() - 2)
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

// Получение импортируемых данных.
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML('Общая информация о контрагенте - юридическом лице', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 12, 3)

    def headerMapping = [
            (xml.row[1].cell[2]): getColumnName(tmpRow, 'fullName'),
            (xml.row[1].cell[3]): getColumnName(tmpRow, 'inn'),
            (xml.row[1].cell[4]): getColumnName(tmpRow, 'countryName'),
            (xml.row[1].cell[5]): getColumnName(tmpRow, 'docNumber'),
            (xml.row[1].cell[6]): getColumnName(tmpRow, 'docDate'),
            (xml.row[1].cell[7]): getColumnName(tmpRow, 'dealNumber'),
            (xml.row[1].cell[8]): getColumnName(tmpRow, 'dealDate'),
            (xml.row[1].cell[9]): getColumnName(tmpRow, 'outcomeSum'),
            (xml.row[1].cell[10]): getColumnName(tmpRow, 'price'),
            (xml.row[1].cell[11]): getColumnName(tmpRow, 'total'),
            (xml.row[1].cell[12]): getColumnName(tmpRow, 'dealDoneDate'),
            (xml.row[2].cell[0]): 'гр. 1'
    ]
    (2..12).each {
        headerMapping.put(xml.row[2].cell[it], 'гр. ' + it)
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def int xmlIndexRow = -1
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
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(9).getName() + "»!")
            }
        }
        xmlIndexCol++

        // графа 4
        if (map != null) {
            def text = row.cell[xmlIndexCol].text()
            if (text != null) {
                if (text.length() == 1) { // для кодов 4, 8 и т.д.
                    text = "00".concat(text)
                } else if (text.length() == 2) { // для кодов 10, 12, 16, 20 и т.д.
                    text = "0".concat(text)
                }
            }
            map = getRefBookValue(10, map.COUNTRY?.referenceValue)

            if ((text != null && !text.isEmpty() && !text.equals(map?.NAME?.stringValue)) || ((text == null || text.isEmpty()) && map?.NAME?.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xlsIndexRow}, столбец ${xmlIndexCol + colOffset} " +
                        "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName() + "»!")
            }
        }
        xmlIndexCol++

        // графа 5
        newRow.docNumber = row.cell[xmlIndexCol].text()
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
        newRow.outcomeSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 10
        newRow.price = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 11
        newRow.total = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 12
        newRow.dealDoneDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}