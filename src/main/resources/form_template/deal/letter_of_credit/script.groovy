package form_template.deal.letter_of_credit

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import java.text.SimpleDateFormat

/**
 * 386 - Предоставление инструментов торгового финансирования и непокрытых аккредитивов (11)
 * (похож на guarantees "Предоставление гарантий")
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
// После принятия из Утверждено
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
        logicCheck()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicCheck()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        logicCheck()
        break
// импорт из xls
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            deleteAllStatic()
            sort()
            calc()
            addAllStatic()
            logicCheck()
        }
        break
}

def getAtributes(){
    [
            rowNumber:   ['rowNumber',   'гр. 1', '№ п/п'],
            fullName:    ['fullName',    'гр. 2', 'Полное наименование с указанием ОПФ'],
            inn:         ['inn',         'гр. 3', 'ИНН/ КИО'],
            countryName: ['countryName', 'гр. 4', 'Страна регистрации'],
            docNumber:   ['docNumber',   'гр. 5', 'Номер договора'],
            docDate:     ['docDate',     'гр. 6', 'Дата договора'],
            dealNumber:  ['dealNumber',  'гр. 7', 'Номер сделки'],
            dealDate:    ['dealDate',    'гр. 8', 'Дата сделки'],
            sum:         ['sum',         'гр. 9', 'Сумма доходов Банка по данным бухгалтерского учета, руб.'],
            price:       ['price',       'гр. 10','Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.'],
            total:       ['total',       'гр. 11','Итого стоимость без учета НДС, акцизов и пошлины, руб.'],
            dealDoneDate:['dealDoneDate','гр. 12','Дата совершения сделки']
    ]
}

def getGroupColumns() {
    ['fullName', 'inn', 'docNumber', 'docDate']
}

def getEditColumns() {
    ['fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'sum', 'dealDoneDate']
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
    dataRowHelper.save(dataRowHelper.getAllCached())
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = 0
    row.keySet().each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    getEditColumns().each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def pointRow = currentDataRow
        while (pointRow.getAlias() != null && index > 0) {
            pointRow = dataRows.get(--index)
        }
        if (index != currentDataRow.getIndex() && dataRows.get(index).getAlias() == null) {
            index++
        }
    } else if (size > 0) {
        for (int i = size - 1; i >= 0; i--) {
            def pointRow = dataRows.get(i)
            if (pointRow.getAlias() == null) {
                index = dataRows.indexOf(pointRow) + 1
                break
            }
        }
    }
    dataRowHelper.insert(row, index + 1)
}
/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkUniq() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()

    def dataRows = dataRowHelper.getAllCached()
    def rowNum = 0
    for (row in dataRows) {
        rowNum++
        if (row.getAlias() != null) {
            continue
        }
        def dealDateCell = row.getCell('dealDate')
        def docDateCell = row.getCell('docDate')
        [
                'rowNumber',   // № п/п
                'fullName',    // Полное наименование юридического лица с указанием ОПФ
                'inn',         // ИНН/КИО
                'countryCode', // Страна регистрации
                'docNumber',   // Номер договора
                'docDate',     // Дата договора
                'dealNumber',  // Номер сделки
                'dealDate',    // Дата сделки
                'sum',         // Сумма доходов Банка по данным бухгалтерского учета, руб.
                'price',       // Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
                'total',       // Итого стоимость без учета НДС, акцизов и пошлин, руб.
                'dealDoneDate' // Дата совершения сделки
        ].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
            }
        }
        //  Корректность даты договора
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }
        // Корректность даты сделки
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }
        // Проверка доходности
        def sumCell = row.getCell('sum')
        def priceCell = row.getCell('price')
        def totalCell = row.getCell('total')
        def msgSum = sumCell.column.name
        if (priceCell.value != sumCell.value) {
            def msg = priceCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может отличаться от «$msgSum»!")
        }
        if (totalCell.value != sumCell.value) {
            def msg = totalCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может отличаться от «$msgSum»!")
        }
        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }
        //Проверки соответствия НСИ
        checkNSI(row, "fullName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryCode", "ОКСМ", 10)
    }

    //Проверки подитоговых сумм
    def testRows = dataRows.findAll { it -> it.getAlias() == null }
    //добавляем итоговые строки для проверки
    for (int i = 0; i < testRows.size(); i++) {
        def testRow = testRows.get(i)
        def nextRow = null

        if (i < testRows.size() - 1) {
            nextRow = testRows.get(i + 1)
        }

        if (testRow.getAlias() == null && nextRow == null || isDiffRow(testRow, nextRow, getGroupColumns())) {
            def itogRow = calcItog(i, testRows)
            testRows.add(++i, itogRow)
        }
    }

    def testItogRows = testRows.findAll { it -> it.getAlias() != null }
    def itogRows = dataRows.findAll { it -> it.getAlias() != null }


    if (testItogRows.size() > itogRows.size()) {            //если удалили итоговые строки

        for (int i = 0; i < dataRows.size(); i++) {
            def row = dataRows[i]
            def nextRow = dataRows[i + 1]
            if (row.getAlias() == null) {
                if (nextRow == null ||
                        nextRow.getAlias() == null && isDiffRow(row, nextRow, getGroupColumns())) {
                    def String groupCols = getValuesByGroupColumn(row)
                    if (groupCols != null) {
                        logger.error("Группа «$groupCols» не имеет строки подитога!")
                    }
                }
            }
        }

    } else if (testItogRows.size() < itogRows.size()) {     //если удалили все обычные строки, значит где то 2 подряд подитог.строки

        for (int i = 0; i < dataRows.size(); i++) {
            if (dataRows[i].getAlias() != null) {
                if (i - 1 < -1 || dataRows[i - 1].getAlias() != null) {
                    logger.error("Строка ${dataRows[i].getIndex()}: Строка подитога не относится к какой-либо группе!")
                }
            }
        }
    } else {
        def totalName = getAtributes().total[2]
        def priceName = getAtributes().price[2]
        def sumName = getAtributes().sum[2]

        for (int i = 0; i < testItogRows.size(); i++) {
            def testItogRow = testItogRows[i]
            def realItogRow = itogRows[i]
            int itg = Integer.valueOf(testItogRow.getAlias().replaceAll("itg#", ""))
            if (dataRows[itg].getAlias() != null) {
                logger.error("Строка ${dataRows[i].getIndex()}: Строка подитога не относится к какой-либо группе!")
            } else {
                def String groupCols = getValuesByGroupColumn(dataRows[itg])
                def mes = "Строка ${realItogRow.getIndex()}: Неверное итоговое значение по группе «$groupCols» в графе"
                if (groupCols != null) {
                    if (testItogRow.price != realItogRow.price) {
                        logger.error(mes + " «${priceName}»")
                    }
                    if (testItogRow.total != realItogRow.total) {
                        logger.error(mes + " «${totalName}»")
                    }
                    if (testItogRow.sum != realItogRow.sum) {
                        logger.error(mes + " «${sumName}»")
                    }
                }
            }
        }
    }
}

/**
 * Проверка соответствия НСИ
 */
void checkNSI(DataRow<Cell> row, String alias, String msg, Long id) {
    def cell = row.getCell(alias)
    if (cell.value != null && refBookService.getRecordData(id, cell.value) == null) {
        def msg2 = cell.column.name
        def rowNum = row.getIndex()
        logger.warn("Строка $rowNum: В справочнике «$msg» не найден элемент «$msg2»!")
    }
}

/**
 * Возвращает строку со значениями полей строки по которым идет группировка
 * ['fullName', 'inn', 'docNumber', 'docDate']
 */
String getValuesByGroupColumn(DataRow row) {
    def sep = ", "
    StringBuilder builder = new StringBuilder()
    def map = row.fullName != null ? refBookService.getRecordData(9, row.fullName) : null
    if (map != null)
        builder.append(map.NAME.stringValue).append(sep)
    if (row.inn != null)
        builder.append(row.inn).append(sep)
    if (row.docNumber != null)
        builder.append(row.docNumber).append(sep)
    if (row.docDate != null)
        builder.append(row.docDate).append(sep)

    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    retVal.substring(0, retVal.length() - 2)
}

/**
 * Сортировка строк
 */
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    dataRows.sort({ DataRow a, DataRow b ->
        sortRow(getGroupColumns(), a, b)
    })

    dataRowHelper.save(dataRows);
}

int sortRow(List<String> params, DataRow a, DataRow b) {
    for (String param : params) {
        def aD = a.getCell(param).value
        def bD = b.getCell(param).value

        if (aD != bD) {
            return aD <=> bD
        }
    }
    return 0
}

/**
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def int index = 1
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        // Порядковый номер строки
        row.rowNumber = index++
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Итог"
        row.total = row.sum

        // Расчет полей зависимых от справочников
        if (row.fullName != null) {
            def map = refBookService.getRecordData(9, row.fullName)
            row.inn = map.INN_KIO.stringValue
            row.countryCode = map.COUNTRY.referenceValue
        } else {
            row.inn = null
            row.countryCode = null
        }
    }
    dataRowHelper.update(dataRows);
}

/**
 * Консолидация
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()

    int index = 1;
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            formDataService.getDataRowHelper(source).getAllCached().each { row ->
                if (row.getAlias() == null) {
                    dataRowHelper.insert(row, index++)
                }
            }
        }
    }
}

/**
 * Удаление всех статическиех строк "Подитог" из списка строк
 */
void deleteAllStatic() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        def row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            iter.remove()
            dataRowHelper.delete(row)
        }
    }
}

/**
 * Проставляет статические строки
 */
void addAllStatic() {
    if (!logger.containsLevel(LogLevel.ERROR)) {

        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.getAllCached()

        for (int i = 0; i < dataRows.size(); i++) {
            def row = dataRows.get(i)
            def nextRow = null

            if (i < dataRows.size() - 1) {
                nextRow = dataRows.get(i + 1)
            }
            if (row.getAlias() == null)
                if (nextRow == null || isDiffRow(row, nextRow, getGroupColumns())) {
                    def itogRow = calcItog(i, dataRows)
                    dataRowHelper.insert(itogRow, ++i + 1)
                }
        }
    }
}

def calcItog(int i, def dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 8
    newRow.getCell('fix').colSpan = 2
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))

    // Расчеты подитоговых значений
    def BigDecimal sumItg = 0, priceitg = 0, totalItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        def row = dataRows.get(j)

        def sum = row.sum
        def price = row.price
        def total = row.total

        sumItg += sum != null ? sum : 0
        priceitg += price != null ? price : 0
        totalItg += total != null ? total : 0
    }

    newRow.sum = sumItg
    newRow.price = priceitg
    newRow.total = totalItg

    newRow
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

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    if (!fileName.endsWith('.xls')) {
        logger.error('Выбранный файл не соответствует формату xls!')
        return
    }

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Полное наименование с указанием ОПФ', 'Подитог')
    if (xmlString == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    // добавить данные в форму
    try {
        if (!checkTableHead(xml, 3)) {
            logger.error('Заголовок таблицы не соответствует требуемой структуре!')
            return
        }
        addData(xml)
    } catch (Exception e) {
        logger.error("" + e.message)
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    Date date = reportPeriodService.get(formData.reportPeriodId).taxPeriod.getEndDate()

    def cache = [:]
    def data = formDataService.getDataRowHelper(formData)
    data.clear()

    def indexRow = -1
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= 2) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        getEditColumns().each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // графа 1
        newRow.rowNumber = indexRow - 2

        // графа 2
        newRow.fullName = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 3
        def map = refBookService.getRecordData(9, newRow.fullName)
        def String text = row.cell[indexCell].text()
        if ((text != null && !text.equals(map.INN_KIO.stringValue)) || (text == null && map.INN_KIO.stringValue != null))
            throw new Exception("Строка ${indexRow+3} столбец ${indexCell+2} содержит значение, отсутствующее в справочнике!")
        indexCell++

        // графа 4
        text = row.cell[indexCell].text()
        map = refBookService.getRecordData(10, map.COUNTRY.referenceValue)
        if ((text != null && !text.equals(map.FULLNAME.stringValue)) || (text == null && map.FULLNAME.stringValue != null))
            throw new Exception("Строка ${indexRow+3} столбец ${indexCell+2} содержит значение, отсутствующее в справочнике!")
        indexCell++

        indexCell++

        // графа 5
        newRow.docNumber = row.cell[indexCell].text()
        indexCell++

        // графа 6
        newRow.docDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 7
        newRow.dealNumber = row.cell[indexCell].text()
        indexCell++

        // графа 8
        newRow.dealDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 9
        newRow.sum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 10
        indexCell++

        // графа 11
        indexCell++

        // графа 12
        newRow.dealDoneDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        data.insert(newRow, indexRow - 2)
    }
}

/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, def headRowCount) {
    def colCount = 11
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }

    def result = (
    xml.row[0].cell[0] == 'Полное наименование с указанием ОПФ' &&
            xml.row[2].cell[0] == 'гр. 2' &&

            xml.row[0].cell[1] == 'ИНН/ КИО' &&
            xml.row[2].cell[1] == 'гр. 3' &&

            xml.row[0].cell[2] == 'Страна регистрации' &&
            xml.row[2].cell[2] == 'гр. 4' &&

            xml.row[0].cell[4] == 'Номер договора' &&
            xml.row[2].cell[4] == 'гр. 5' &&

            xml.row[0].cell[5] == 'Дата договора' &&
            xml.row[2].cell[5] == 'гр. 6' &&

            xml.row[0].cell[6] == 'Номер сделки' &&
            xml.row[2].cell[6] == 'гр. 7' &&

            xml.row[0].cell[7] == 'Дата сделки' &&
            xml.row[2].cell[7] == 'гр. 8' &&

            xml.row[0].cell[8] == 'Сумма доходов Банка по данным бухгалтерского учета, руб.' &&
            xml.row[2].cell[8] == 'гр. 9' &&

            xml.row[0].cell[9] == 'Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.' &&
            xml.row[2].cell[9] == 'гр. 10' &&

            xml.row[0].cell[10] == 'Итого стоимость без учета НДС, акцизов и пошлины, руб.' &&
            xml.row[2].cell[10] == 'гр. 11' &&

            xml.row[0].cell[11] == 'Дата совершения сделки' &&
            xml.row[2].cell[11] == 'гр. 12')
    return result
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
        throw new Exception("Строка ${indexRow + 3} столбец ${indexCell + 1} содержит недопустимый тип данных!")
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
        throw new Exception("Строка ${indexRow + 3} столбец ${indexCell + 1} содержит недопустимый тип данных!")
    }
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String alias, String value, Date date, def cache, int indexRow, int indexCell) {
    String filter = "LOWER($alias) = LOWER('$value')"
    if (value == '') filter = "$alias is null"
    if (cache[ref_id] != null) {
        if (cache[ref_id][filter] != null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null)
    if (records.size() == 1) {
        cache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return cache[ref_id][filter]
    }
    throw new Exception("Строка ${indexRow + 3} столбец ${indexCell + 1} содержит значение, отсутствующее в справочнике!")
}