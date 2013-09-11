package form_template.deal.nondeliverable

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import java.text.SimpleDateFormat

/**
 * 392 - Беспоставочные срочные сделки
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        calc()
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
        calc()
        logicCheck()
        break
// Импорт
    case FormDataEvent.IMPORT:
        importData()
        break
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId,
            formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = 0
    ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate',
            'transactionType', 'incomeSum', 'consumptionSum', 'transactionDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def pointRow = currentDataRow
        while(pointRow.getAlias()!=null && index>0){
            pointRow = dataRows.get(--index)
        }
        if(index!=currentDataRow.getIndex() && dataRows.get(index).getAlias()==null){
            index++
        }
    }else if (size>0) {
        for(int i = size-1;i>=0;i--){
            def pointRow = dataRows.get(i)
            if(pointRow.getAlias()==null){
                index = dataRows.indexOf(pointRow)+1
                break
            }
        }
    }
    dataRowHelper.insert(row, index+1)
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    // Налоговый период
    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod

    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()

    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }

        def rowNum = row.getIndex()

        if (row.getAlias() != null) {
            continue
        }

        [
                'rowNum', // № п/п
                'name', // Полное наименование с указанием ОПФ
                'innKio', // ИНН/КИО
                'country', // Наименование страны регистрации
                'countryCode', // Код страны по классификатору ОКСМ
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'transactionNum', // Номер сделки
                'transactionDeliveryDate', // Дата заключения сделки
                'transactionType', // Вид срочной сделки
                'price', // Цена (тариф) за единицу измерения, руб.
                'cost', // Итого стоимость, руб.
                'transactionDate' // Дата совершения сделки
        ].each {
            if (row.getCell(it).value == null || row.getCell(it).value.toString().isEmpty()) {
                msg = row.getCell(it).column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }

        // Проверка доходов и расходов
        def consumptionSum = row.consumptionSum
        def incomeSum = row.incomeSum
        def price = row.price
        def transactionDeliveryDate = row.transactionDeliveryDate
        def contractDate = row.contractDate
        def cost = row.cost
        def transactionDate = row.transactionDate

        // В одной строке не должны быть одновременно заполнены графы 12 и 13
        if (consumptionSum != null && price != null) {
            def msg1 = row.getCell('consumptionSum').column.name
            def msg2 = row.getCell('cost').column.name
            logger.warn("«$msg1» и «$msg2» в строке $rowNum не могут быть одновременно заполнены!")
        }

        // В одной строке если не заполнена графа 12, то должна быть заполнена графа 13 и наоборот
        if (consumptionSum == null && price == null) {
            def msg1 = row.getCell('consumptionSum').column.name
            def msg2 = row.getCell('cost').column.name
            logger.warn("Одна из граф «$msg1» и «$msg2» в строке $rowNum должно быть заполнена!")
        }

        // Корректность даты договора
        def dt = row.getCell('contractDate').value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = row.getCell('contractDate').column.name

            if (dt > dTo) {
                logger.warn("«$msg» не может быть больше даты окончания отчётного периода в строке $rowNum!")
            }

            if (dt < dFrom) {
                logger.warn("«$msg» не может быть меньше даты начала отчётного периода в строке $rowNum!")
            }
        }

        // Корректность даты заключения сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        // Проверка доходов/расходов и стоимости
        if (incomeSum != null && consumptionSum == null && price != incomeSum) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('incomeSum').column.name
            logger.warn("Графа «$msg1» должна быть равна «$msg2» в строке $rowNum!")
        }
        if (incomeSum == null && consumptionSum != null && price != consumptionSum) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            logger.warn("Графа «$msg1» должна быть равна «$msg2» в строке $rowNum!")
        }
        if (incomeSum != null && consumptionSum != null &&
                (price == null
                        || consumptionSum == null
                        || incomeSum == null
                        || price.abs() != (consumptionSum - incomeSum).abs())) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            def msg3 = row.getCell('incomeSum').column.name
            logger.warn("Графа «$msg1» должна быть равна разнице графы «$msg2» и графы «$msg3» по модулю в строке $rowNum!")
        }

        // Проверка стоимости сделки
        if (cost != price) {
            def msg1 = row.getCell('cost').column.name
            def msg2 = row.getCell('price').column.name
            logger.warn("«$msg1» не может отличаться от «$msg2» сделки в строке $rowNum!")
        }

        // Корректность дат сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('transactionDeliveryDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        //Проверки соответствия НСИ
        checkNSI(row, "name", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "country", "ОКСМ", 10)
        checkNSI(row, "countryCode", "ОКСМ", 10)
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
        logger.warn("В справочнике «$msg» не найден элемент графы «$msg2», указанный в строке $rowNum!")
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

            if (row.getAlias() == null && nextRow == null
                    || row.name != nextRow.name
                    || row.innKio != nextRow.innKio
                    || row.contractNum != nextRow.contractNum
                    || row.contractDate != nextRow.contractDate
                    || row.transactionType != nextRow.transactionType) {

                def itogRow = calcItog(i)
                dataRowHelper.insert(itogRow, ++i+1)
            }
        }
    }
}

/**
 * Расчет подитогового значения
 * @param i
 * @return
 */
def calcItog(int i) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 12
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    BigDecimal priceItg = 0, costItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)

        price = row.price
        cost = row.cost

        priceItg += price != null ? price : 0
        costItg += cost != null ? cost : 0
    }

    newRow.price = priceItg
    newRow.cost = costItg
    newRow
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
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
        row.rowNum = index++
        // Графы 13 и 14 из 11 и 12
        incomeSum = row.incomeSum
        consumptionSum = row.consumptionSum

        if (incomeSum != null) {
            row.price = incomeSum
            row.cost = incomeSum
        }

        if (consumptionSum != null) {
            row.price = consumptionSum
            row.cost = consumptionSum
        }

        // Расчет полей зависимых от справочников
        if (row.name != null) {
            def map = refBookService.getRecordData(9, row.name)
            row.innKio = map.INN_KIO.numberValue
            row.country = map.COUNTRY.referenceValue
            row.countryCode = map.COUNTRY.referenceValue
        } else {
            row.innKio = null
            row.country = null
            row.countryCode = null
        }
    }
    dataRowHelper.update(dataRows);
}

/**
 * Сортировка строк по гр. 2, гр. 3, гр. 6, гр. 7, гр. 10
 */
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    dataRows.sort({ DataRow a, DataRow b ->
        sortRow(['name', 'innKio', 'contractNum', 'contractDate', 'transactionType'], a, b)
    })

    dataRowHelper.save(dataRows);
}

int sortRow(List<String> params, DataRow a, DataRow b) {
    for (String param : params) {
        aD = a.getCell(param).value
        bD = b.getCell(param).value

        if (aD != bD) {
            return aD <=> bD
        }
    }
    return 0
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
 * Консолидация
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    dataRows.clear()

    int index = 1;
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null
                && source.state == WorkflowState.ACCEPTED
                && source.getFormType().getId() == formData.getFormType().getId()) {
            formDataService.getDataRowHelper(source).getAllCached().each { row ->
                if (row.getAlias() == null) {
                    dataRowHelper.insert(row, index++)
                }
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

    if (!fileName.contains('.xls')) {
        logger.error('Формат файла должен быть *.xls')
        return
    }

    def xmlString = importService.getData(is, fileName, 'windows-1251', 'Полное наименование с указанием ОПФ', 'Подитог:')
    if (xmlString == null) {
        logger.error('Отсутствие значения после обработки потока данных')
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Ошибка в обработке данных!')
        return
    }

    // добавить данные в форму
    try {
        if (!checkTableHead(xml, 4)) {
            logger.error('Заголовок таблицы не соответствует требуемой структуре!')
            return
        }
        addData(xml)
    } catch (Exception e) {
        logger.error("Ошибка при заполнении данных с файла! " + e.message)
    }
}

/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, def headRowCount) {
    def colCount = 13
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }

    def names = (
            xml.row[0].cell[0] == 'Полное наименование с указанием ОПФ' &&
            xml.row[0].cell[1] == 'ИНН/ КИО' &&
            xml.row[0].cell[2] == 'Наименование страны регистрации' &&
            xml.row[0].cell[3] == 'Код страны регистрации по классификатору ОКСМ' &&
            xml.row[0].cell[4] == 'Номер договора' &&
            xml.row[0].cell[5] == 'Дата договора' &&
            xml.row[0].cell[6] == 'Номер сделки' &&
            xml.row[0].cell[7] == 'Дата заключения сделки' &&
            xml.row[0].cell[8] == 'Вид срочной сделки' &&
            xml.row[0].cell[9] == 'Сумма доходов Банка по данным бухгалтерского учета, руб.' &&
            xml.row[0].cell[10] == 'Сумма расходов Банка по данным бухгалтерского учета, руб.' &&
            xml.row[0].cell[11] == 'Цена (тариф) за единицу измерения, руб.' &&
            xml.row[0].cell[12] == 'Итого стоимость, руб.' &&
            xml.row[0].cell[13] == 'Дата совершения сделки'
    )
    if (!names) return false

    // пустая строка
    xml.row[1].cell.each { it ->
        if (it.text() != '') {
            if (it.text() == 'Вариант 2') {     //TODO (alivanov 9.09.13) выпилить проверку
            } else {
                return false
            }
        }
    }

    //строка с нумерацией
    for (int i = 0; i <= colCount; i++) {
        if (xml.row[2].cell[i] != ('' + (i + 2))) {
            return false
        }
    }

    // строка с нумерацией граф
    def grafRow = (
    xml.row[3].cell[0] == 'гр. 2' &&
    xml.row[3].cell[1] == 'гр. 3' &&
    xml.row[3].cell[2] == 'гр. 4.1' &&
    xml.row[3].cell[3] == 'гр. 4.2' &&
    xml.row[3].cell[4] == 'гр. 5' &&
    xml.row[3].cell[5] == 'гр. 6' &&
    xml.row[3].cell[6] == 'гр. 7' &&
    xml.row[3].cell[7] == 'гр. 8' &&
    xml.row[3].cell[8] == 'гр. 9' &&
    xml.row[3].cell[9] == 'гр. 10' &&
    xml.row[3].cell[10] == 'гр. 11' &&
    xml.row[3].cell[11] == 'гр. 12' &&
    xml.row[3].cell[12] == 'гр. 13' &&
    xml.row[3].cell[13] == 'гр. 14'
    )

    return grafRow
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    Date date = new Date()

    def cache = [:]
    def data = formDataService.getDataRowHelper(formData)
    data.clear()

    def indexRow = -1
    for (def row : xml.row) {
        indexRow++

        // пропустить шапку таблицы
        if (indexRow <= 3) {
            continue
        }

        if ((row.cell.find {it.text() != ""}.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate',
                'transactionType', 'incomeSum', 'consumptionSum', 'transactionDate'].each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // графа 1
        newRow.rowNum = indexRow - 3

        // графа 2
        newRow.name = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 3
        //newRow.inn = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 4.1
        newRow.country = getRecordId(10, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 4.2
        //newRow.countryCode = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // графа 5
        newRow.contractNum = row.cell[indexCell].text()
        indexCell++

        // графа 6
        newRow.contractDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 7
        newRow.transactionNum = row.cell[indexCell].text()
        indexCell++

        // графа 8
        newRow.transactionDeliveryDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 9
        newRow.transactionType = row.cell[indexCell].text()
        indexCell++

        // графа 10
        newRow.incomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 11
        newRow.consumptionSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 12
        newRow.price = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 13
        newRow.cost = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 14
        newRow.transactionDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        data.insert(newRow, indexRow - 3)
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
        throw new Exception("Строка ${indexRow - 3} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String code, String value, Date date, def cache, int indexRow, int indexCell) {

    String filter;
    if (value == null || value.equals("")) {
        filter = code + " is null"
    } else {
        filter = code + "= '" + value + "'"
    }
    if (cache[ref_id] != null) {
        if (cache[ref_id][filter] != null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    throw new Exception("Строка ${indexRow - 3} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике!")
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
        throw new Exception("Строка ${indexRow - 3} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}