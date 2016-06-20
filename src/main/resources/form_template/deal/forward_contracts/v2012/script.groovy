package form_template.deal.forward_contracts.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook

import java.text.SimpleDateFormat

/**
 * 391 - Поставочные срочные сделки, базисным активом которых является иностранная валюта (16)
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
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
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
            rowNumber:      ['rowNumber',       'гр. 1',   '№ п/п'],
            fullName:       ['fullName',        'гр. 2',   'Полное наименование с указанием ОПФ'],
            inn:            ['inn',             'гр. 3',   'ИНН/ КИО'],
            countryName:    ['countryName',     'гр. 4.1', 'Наименование страны регистрации'],
            countryCode:    ['countryCode',     'гр. 4.2', 'Код страны по классификатору ОКСМ'],
            docNumber:      ['docNumber',       'гр. 5',   'Номер договора'],
            docDate:        ['docDate',         'гр. 6',   'Дата договора'],
            dealNumber:     ['dealNumber',      'гр. 7',   'Номер сделки'],
            dealDate:       ['dealDate',        'гр. 8',   'Дата заключения сделки'],
            dealType:       ['dealType',        'гр. 9',   'Вид срочной сделки'],
            currencyCode:   ['currencyCode',    'гр. 10',  'Код валюты по сделке'],
            countryDealCode:['countryDealCode', 'гр. 11',  'Код страны происхождения предмета сделки по классификатору ОКСМ'],
            incomeSum:      ['incomeSum',       'гр. 12',  'Сумма доходов Банка по данным бухгалтерского учета, руб.'],
            outcomeSum:     ['outcomeSum',      'гр. 13',  'Сумма расходов Банка по данным бухгалтерского учета, руб.'],
            price:          ['price',           'гр. 14',  'Цена (тариф) за единицу измерения, руб.'],
            total:          ['total',           'гр. 15',  'Итого стоимость, руб.'],
            dealDoneDate:   ['dealDoneDate',    'гр. 16',  'Дата совершения сделки']
    ]
}

def getGroupColumns() {
    ['fullName', 'inn', 'docNumber', 'docDate', 'dealType']
}

def getEditColumns() {
    ['fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealType',
            'currencyCode', 'countryDealCode', 'incomeSum', 'outcomeSum', 'dealDoneDate']
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
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
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentReportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    def dFrom = startDate.time
    startDate.add(Calendar.YEAR, 1)
    startDate.add(Calendar.DAY_OF_YEAR, -1)
    def dTo = startDate.time

    def dataRows = dataRowHelper.getAllCached()

    int index = 1
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = index++
        def dealDateCell = row.getCell('dealDate')
        def docDateCell = row.getCell('docDate')
        [
                'rowNumber',        // № п/п
                'fullName',         // Полное наименование с указанием ОПФ
                'inn',              // ИНН/КИО
                'countryName',      // Наименование страны регистрации
                'countryCode',      // Код страны по классификатору ОКСМ
                'docNumber',        // Номер договора
                'docDate',          // Дата договора
                'dealNumber',       // Номер сделки
                'dealDate',         // Дата заключения сделки
                'dealType',         // Вид срочной сделки
                'currencyCode',     // Код валюты расчетов по сделке
                'countryDealCode',  // Код страны происхождения предмета сделки по классификатору ОКСМ
                'price',            // Цена (тариф) за единицу измерения, руб.
                'total',            // Итого стоимость, руб.
                'dealDoneDate'      // Дата совершения сделки
        ].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
            }
        }
        // Проверка заполнения расходов/доходов.
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            logger.warn("Строка $rowNum: Одна из граф «$msgIn» и «$msgOut» должна быть заполнена!")
        }
        //  Корректность даты договора
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg» не может быть вне налогового периода!")
        }
        // Корректность даты заключения сделки
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }
        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверка доходов/расходов и стоимости
        def msgPrice = getColumnName(row, 'price')
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            if (row.price.abs() != (incomeSumCell.value - outcomeSumCell.value).abs())
                logger.warn("Строка $rowNum: Графа «$msgPrice» должна быть равна разнице графы «$msgIn» и «$msgOut» по модулю!")
        } else if (incomeSumCell.value != null) {
            if (row.price != incomeSumCell.value)
                logger.warn("Строка $rowNum: Графа «$msgPrice» должна быть равна «$msgIn»!")
        } else if (outcomeSumCell.value != null) {
            if (row.price != outcomeSumCell.value)
                logger.warn("Строка $rowNum: Графа «$msgPrice» должна быть равна «$msgOut»!")
        }

        // Проверка заполнения стоимости сделки
        if (row.total != row.price) {
            def msg1 = getColumnName(row, 'total')
            logger.warn("Строка $rowNum: «$msg1» не может отличаться от «$msgPrice» сделки!")
        }

        //Проверки соответствия НСИ
        checkNSI(row, "fullName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryName", "ОКСМ", 10)
        checkNSI(row, "countryCode", "ОКСМ", 10)
        checkNSI(row, "countryDealCode", "ОКСМ", 10)
        checkNSI(row, "currencyCode", "Единый справочник валют", 15)
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
 * ['fullName', 'inn', 'docNumber', 'docDate', 'dealType']
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
    if (row.dealType != null)
        builder.append(row.dealType).append(sep)

    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    retVal.substring(0, retVal.length() - 2)
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
        if (row.incomeSum != null && row.outcomeSum != null) {
            row.price = (row.incomeSum - row.outcomeSum).abs()
        } else {
            row.price = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        }
        // Расчет поля "Итого"
        row.total = row.price

        // Расчет полей зависимых от справочников
        if (row.fullName != null) {
            def map = refBookService.getRecordData(9, row.fullName)
            row.inn = map.INN_KIO.stringValue
            row.countryName = map.COUNTRY.referenceValue
            row.countryCode = map.COUNTRY.referenceValue
        } else {
            row.inn = null
            row.countryName = null
            row.countryCode = null
        }
    }
    dataRowHelper.update(dataRows);
}

/**
 * Расчет подитогового значения
 * @param i
 * @return
 */
def calcItog(int i, def dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 14
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

    newRow
}

/**
 * проверяет разные ли строки по значениям полей группировки
 * @param a первая  строка
 * @param b вторая строка
 * @return true - разные, false = одинаковые
 */
boolean isDiffRow(DataRow row, DataRow nextRow, def groupColumns) {
    def rez = false
    groupColumns.each { def n ->
        rez = rez || (row.get(n) != nextRow.get(n))
    }
    return rez
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
 * Консолидация
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = new LinkedList<DataRow<Cell>>()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentReportPeriodId, it.periodOrder, it.comparativePeriodId, it.accruing)
        if (source != null
                && source.state == WorkflowState.ACCEPTED
                && source.getFormType().getId() == formData.getFormType().getId()) {
            formDataService.getDataRowHelper(source).getAll().each { row ->
                if (row.getAlias() == null) {
                    rows.add(row)
                }
            }
        }
    }
    dataRowHelper.save(rows)
}

/**
 * Удаление всех статическиех строк "Подитог" из списка строк
 */
void deleteAllStatic() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
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

        if (dataRows.size() < 1) {
            return
        }

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

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Полное наименование с указанием ОПФ', 'Подитог:')
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
        addData(xml, 2)
    } catch (Exception e) {
        logger.error("" + e.message)
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml, int headRowCount) {
    Date date = reportPeriodService.get(formData.reportPeriodId).taxPeriod.getEndDate()

    def cache = [:]
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = new LinkedList()

    def indexRow = -1
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= headRowCount) {
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
        newRow.rowNumber = indexRow - headRowCount

        // графа 2
        newRow.fullName = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        def map = newRow.fullName == null ? null : refBookService.getRecordData(9, newRow.fullName)
        indexCell++

        // графа 3
        if (map != null) {
            def text = row.cell[indexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow+1} столбец ${indexCell+2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(9).getName()+"»!")
            }
        }
        indexCell++

        // графа 4.1
        if (map != null) {
            def text = row.cell[indexCell].text()
            map = refBookService.getRecordData(10, map.COUNTRY.referenceValue)
            if ((text != null && !text.isEmpty() && !text.equals(map.NAME.stringValue)) || ((text == null || text.isEmpty()) && map.NAME.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow+1} столбец ${indexCell+2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        indexCell++

        // графа 4.2
        if (map != null) {
            def text = row.cell[indexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE.stringValue)) || ((text == null || text.isEmpty()) && map.CODE.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${indexRow+1} столбец ${indexCell+2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
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
        newRow.dealType = row.cell[indexCell].text()//getRecordId(14, 'MODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 10
        newRow.currencyCode = getRecordId(15, 'CODE_2', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 11
        newRow.countryDealCode = getRecordId(10, 'CODE_2', row.cell[indexCell].text(), date, cache, indexRow, indexCell, false)
        indexCell++

        // графа 12
        newRow.incomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 13
        newRow.outcomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 14
        newRow.price = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 15
        newRow.total = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 16
        newRow.dealDoneDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, int headRowCount) {
    def colCount = 15
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (xml.row[0].cell[0] == 'Полное наименование с указанием ОПФ' &&
            xml.row[2].cell[0] == 'гр. 2' &&
            xml.row[0].cell[1] == 'ИНН/ КИО' &&
            xml.row[2].cell[1] == 'гр. 3' &&
            xml.row[0].cell[2] == 'Наименование страны регистрации' &&
            xml.row[2].cell[2] == 'гр. 4.1' &&
            xml.row[0].cell[3] == 'Код страны регистрации по классификатору ОКСМ' &&
            xml.row[2].cell[3] == 'гр. 4.2' &&
            xml.row[0].cell[4] == 'Номер договора' &&
            xml.row[2].cell[4] == 'гр. 5' &&
            xml.row[0].cell[5] == 'Дата договора' &&
            xml.row[2].cell[5] == 'гр. 6' &&
            xml.row[0].cell[6] == 'Номер сделки' &&
            xml.row[2].cell[6] == 'гр. 7' &&
            xml.row[0].cell[7] == 'Дата заключения сделки' &&
            xml.row[2].cell[7] == 'гр. 8' &&
            xml.row[0].cell[8] == 'Вид срочной сделки' &&
            xml.row[2].cell[8] == 'гр. 9' &&
            xml.row[0].cell[9] == 'Код валюты по сделке' &&
            xml.row[2].cell[9] == 'гр. 10' &&
            xml.row[0].cell[10] == 'Код страны происхождения предмета сделки по классификатору ОКСМ' &&
            xml.row[2].cell[10] == 'гр. 11' &&
            xml.row[0].cell[11] == 'Сумма доходов Банка по данным бухгалтерского учета, руб.' &&
            xml.row[2].cell[11] == 'гр. 12' &&
            xml.row[0].cell[12] == 'Сумма расходов Банка по данным бухгалтерского учета, руб.' &&
            xml.row[2].cell[12] == 'гр. 13' &&
            xml.row[0].cell[13] == 'Цена (тариф) за единицу измерения, руб.' &&
            xml.row[2].cell[13] == 'гр. 14' &&
            xml.row[0].cell[14] == 'Итого стоимость, руб.' &&
            xml.row[2].cell[14] == 'гр. 15' &&
            xml.row[0].cell[15] == 'Дата совершения сделки' &&
            xml.row[2].cell[15] == 'гр. 16')
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
        throw new Exception("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String alias, String value, Date date, def cache, int indexRow, int indexCell, boolean mandatory = true) {
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
    def msg = "Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(ref_id).getName()+"»!"
    if (mandatory) {
        throw new Exception(msg)
    } else {
        logger.warn(msg)
    }
    return null
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
        throw new Exception("Проверка файла: Строка ${indexRow + 2} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}
