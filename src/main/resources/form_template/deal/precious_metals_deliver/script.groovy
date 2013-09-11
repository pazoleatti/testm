package form_template.deal.precious_metals_deliver

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import java.text.SimpleDateFormat

/**
 * 393 - Поставочные срочные сделки с драгоценными металлами
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
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        logicCheck()

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
    ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate', 'innerCode',
            'unitCountryCode', 'signPhis', 'countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2',
            'city2', 'settlement2', 'conditionCode', 'count', 'incomeSum', 'consumptionSum', 'transactionDate'].each {
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

        [
                'rowNum', // № п/п
                'name', // Полное наименование с указанием ОПФ
                'innKio', // ИНН/КИО
                'country', // Наименование страны регистрации
                'countryCode1', // Код страны по классификатору ОКСМ
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'transactionNum', // Номер сделки
                'transactionDeliveryDate', // Дата заключения сделки
                'innerCode', // Внутренний код
                'okpCode', // Код ОКП
                'unitCountryCode', // Код страны происхождения предмета сделки по классификатору ОКСМ
                'signPhis', // Признак физической поставки драгоценного металла
                'signTransaction', // Признак внешнеторговой сделки
                'count', // Количество
                'priceOne', // Цена (тариф) за единицу измерения без учета НДС, руб.
                'totalNds', // Итого стоимость без учета НДС, руб.
                'transactionDate' // Дата совершения сделки
        ].each {
            if (row.getCell(it).value == null || row.getCell(it).value.toString().isEmpty()) {
                msg = row.getCell(it).column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }

        def transactionDeliveryDate = row.transactionDeliveryDate
        def contractDate = row.contractDate
        // def countryCode2 = row.getCell('countryCode2').value
        // def countryCode3 = row.getCell('countryCode3').value
        def settlement1 = row.settlement1
        def city1 = row.city1
        def settlement2 = row.settlement2
        def city2 = row.city2
        def incomeSum = row.incomeSum
        def consumptionSum = row.consumptionSum
        def priceOne = row.priceOne
        def totalNds = row.totalNds
        def count = row.count
        def transactionDate = row.transactionDate

        // Проверка зависимости от признака физической поставки
        def signPhis = row.signPhis
        if (signPhis != null && refBookService.getNumberValue(18, signPhis, 'CODE') == 1) {
            ['countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2', 'city2',
                    'settlement2', 'conditionCode'].each {
                if (row.getCell(it).value != null && !row.getCell(it).value.toString().isEmpty()) {
                    def msg1 = row.getCell('signPhis').column.name
                    def msg2 = row.getCell(it).column.name
                    logger.warn("«$msg1» указан «ОМС», графа «$msg2» строки $rowNum заполняться не должна!")
                }
            }
        }

        // Корректность даты договора
        def dt = contractDate
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

        // Корректность заполнения признака внешнеторговой сделки
        def msg14 = row.getCell('signTransaction').column.name
        def sign = refBookService.getNumberValue(38, row.signTransaction, 'CODE')
        if (row.countryCode2 == row.countryCode3 && sign != 0) {
            logger.warn("«$msg14» в строке $rowNum должен быть «Нет»!")
        } else if (row.countryCode2 != row.countryCode3 && sign != 1) {
            logger.warn("«$msg14» в строке $rowNum должен быть «Да»!")
        }

        // Проверка населенного пункта 1
        if (settlement1 != null && !settlement1.toString().isEmpty() && city1 != null && !city1.toString().isEmpty()) {
            def msg1 = row.getCell('settlement1').column.name
            def msg2 = row.getCell('city1').column.name
            logger.warn("Если указан «$msg1», не должен быть указан «$msg2» в строке $rowNum")
        }

        // Проверка населенного пункта 2
        if (settlement2 != null && !settlement2.toString().isEmpty() && city2 != null && !city2.toString().isEmpty()) {
            def msg1 = row.getCell('settlement2').column.name
            def msg2 = row.getCell('city2').column.name
            logger.warn("Если указан «$msg1», не должен быть указан «$msg2» в строке $rowNum")
        }

        // Проверка доходов и расходов
        if (incomeSum != null && consumptionSum != null) {
            def msg1 = row.getCell('incomeSum').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            logger.warn("«$msg1» и «$msg2» в строке $rowNum не могут быть одновременно заполнены!")
        }

        if (incomeSum == null && consumptionSum == null) {
            def msg1 = row.getCell('incomeSum').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            logger.warn("Одна из граф «$msg1» и «$msg2» в строке $rowNum должна быть заполнена!")
        }

        // Проверка доходов/расходов и стоимости
        if (incomeSum != null && consumptionSum == null && priceOne != incomeSum) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('incomeSum').column.name
            logger.warn("Графа «$msg1» должна быть равна «$msg2» в строке $rowNum!")
        }
        if (incomeSum == null && consumptionSum != null && priceOne != consumptionSum) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            logger.warn("Графа «$msg1» должна быть равна «$msg2» в строке $rowNum!")
        }
        if (incomeSum != null && consumptionSum != null &&
                (priceOne == null
                        || consumptionSum == null
                        || incomeSum == null
                        || priceOne.abs() != (consumptionSum - incomeSum).abs())) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('consumptionSum').column.name
            def msg3 = row.getCell('incomeSum').column.name
            logger.warn("Графа «$msg1» должна быть равна разнице графы «$msg2» и графы «$msg3» по модулю в строке $rowNum!")
        }

        // Проверка количества
        if (count != null && count != 1) {
            def msg = row.getCell('count').column.name
            logger.warn("В графе «$msg» может быть указано только значение «1» в строке $rowNum!")
        }

        // Корректность дат сделки
        if (transactionDate < transactionDeliveryDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('transactionDeliveryDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        // Проверка заполнения стоимости сделки
        if (priceOne != totalNds) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('totalNds').column.name
            logger.warn("«$msg1» не может отличаться от «$msg2» сделки в строке $rowNum!")
        }

        // Проверки соответствия НСИ
        checkNSI(row, "name", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryCode1", "ОКСМ", 10)
        checkNSI(row, "unitCountryCode", "ОКСМ", 10)
        checkNSI(row, "country", "ОКСМ", 10)
        checkNSI(row, "countryCode3", "ОКСМ", 10)
        checkNSI(row, "region1", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "region2", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "okpCode", "Коды драгоценных металлов", 17)
        checkNSI(row, "signPhis", "Признаки физической поставки", 18)
        checkNSI(row, "conditionCode", "Коды условий поставки", 63)
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
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def index = 1;
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // Порядковый номер строки
        row.rowNum = index++
        // Графы 27 и 28 из 25 и 26
        incomeSum = row.incomeSum
        consumptionSum = row.incomeSum

        if (incomeSum != null) {
            row.priceOne = incomeSum
            row.totalNds = incomeSum
        }

        if (consumptionSum != null) {
            row.priceOne = consumptionSum
            row.totalNds = consumptionSum
        }

        // Расчет полей зависимых от справочников
        if (row.name != null) {
            def map = refBookService.getRecordData(9, row.name)
            row.innKio = map.INN_KIO.numberValue
            row.country = map.COUNTRY.referenceValue
            row.countryCode1 = map.COUNTRY.referenceValue
        } else {
            row.innKio = null
            row.country = null
            row.countryCode1 = null
        }
        if (row.signPhis == 1) {
            row.countryCode2 = null
            row.region1 = null
            row.city1 = null
            row.settlement1 = null
            row.countryCode3 = null
            row.region2 = null
            row.city2 = null
            row.settlement2 = null
        }
        if (row.countryCode2 == row.countryCode3) {
            row.signTransaction = Long.valueOf(182632)
        } else {
            row.signTransaction = Long.valueOf(182633)
        }
    }
    dataRowHelper.update(dataRows);
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
 * Сортировка строк по гр.
 */
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    dataRows.sort({ DataRow a, DataRow b ->
        // гр. 2, гр. 3, гр. 6, гр. 7, гр. 10, гр. 11, гр. 12, гр. 13, гр. 14, гр. 15, гр. 16, гр. 17, гр. 18, гр. 19,
        // гр. 20, гр. 21, гр. 22, гр. 23, гр. 24.
        sortRow(['name', 'innKio', 'contractNum', 'contractDate', 'innerCode', 'okpCode', 'unitCountryCode', 'signPhis',
                'signTransaction', 'countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2',
                'city2', 'settlement2', 'conditionCode', 'count'], a, b)
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
                if (nextRow == null
                        || row.name != nextRow.name
                        || row.innKio != nextRow.innKio
                        || row.contractNum != nextRow.contractNum
                        || row.contractDate != nextRow.contractDate
                        || row.innerCode != nextRow.innerCode
                        || row.okpCode != nextRow.okpCode
                        || row.unitCountryCode != nextRow.unitCountryCode
                        || row.signPhis != nextRow.signPhis
                        || row.signTransaction != nextRow.signTransaction
                        || row.countryCode2 != nextRow.countryCode2
                        || row.region1 != nextRow.region1
                        || row.city1 != nextRow.city1
                        || row.settlement1 != nextRow.settlement1
                        || row.countryCode3 != nextRow.countryCode3
                        || row.region2 != nextRow.region2
                        || row.city2 != nextRow.city2
                        || row.settlement2 != nextRow.settlement2
                        || row.conditionCode != nextRow.conditionCode
                        || row.count != nextRow.count) {

                    def itogRow = calcItog(i)
                    dataRowHelper.insert(itogRow, ++i + 1)
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

    newRow.getCell('itog').colSpan = 26
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    BigDecimal totalNdsItg = 0, priceOneItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)

        priceOne = row.priceOne
        totalNds = row.totalNds

        priceOneItg += priceOne != null ? priceOne : 0
        totalNdsItg += totalNds != null ? totalNds : 0
    }

    newRow.priceOne = priceOneItg
    newRow.totalNds = totalNdsItg

    newRow
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

def getHeaderRowCount(){
    return 4
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
        if (!checkTableHead(xml)) {
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
def checkTableHead(def xml) {
    def colCount = 28
    def rc = getHeaderRowCount()
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < rc || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (
            checkHeaderRow(xml, rc, 0, ['Полное наименование с указанием ОПФ', '', '2', 'гр. 2']) &&
            checkHeaderRow(xml, rc, 1, ['ИНН/ КИО', '', '3', 'гр. 3']) &&
            checkHeaderRow(xml, rc, 2, ['Наименование страны регистрации', '', '4', 'гр. 4.1']) &&
            checkHeaderRow(xml, rc, 3, ['Код страны регистрации по классификатору ОКСМ', '', '5', 'гр. 4.2']) &&
            checkHeaderRow(xml, rc, 4, ['Номер договора', '', '6', 'гр. 5']) &&
            checkHeaderRow(xml, rc, 5, ['Дата договора', '', '7', 'гр. 6']) &&
            checkHeaderRow(xml, rc, 6, ['Номер сделки', '', '8', 'гр. 7']) &&
            checkHeaderRow(xml, rc, 7, ['Дата заключения сделки', '', '9', 'гр. 8']) &&
            checkHeaderRow(xml, rc, 8, ['Характеристика базисного актива', 'Внутренний код', '10', 'гр. 9.1']) &&
            checkHeaderRow(xml, rc, 9, ['', 'Код по ОКП', '11', 'гр. 9.2']) &&
            checkHeaderRow(xml, rc, 10, ['', 'Код страны происхождения предмета сделки', '12', 'гр. 9.3']) &&
            checkHeaderRow(xml, rc, 11, ['Признак физической поставки драгоценного металла', '', '13', 'гр. 10']) &&
            checkHeaderRow(xml, rc, 12, ['Признак внешнеторговой сделки', '', '14', 'гр. 11']) &&
            checkHeaderRow(xml, rc, 13, ['Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами', 'Код страны по классификатору ОКСМ', '15', 'гр. 12.1']) &&
            checkHeaderRow(xml, rc, 14, ['', 'Регион (код)', '16', 'гр. 12.2']) &&
            checkHeaderRow(xml, rc, 15, ['', 'Город', '17', 'гр. 12.3']) &&
            checkHeaderRow(xml, rc, 16, ['', 'Населенный пункт', '18', 'гр. 12.4']) &&
            checkHeaderRow(xml, rc, 17, ['Место совершения сделки (адрес места доставки (разгрузки) драгоценного металла)', 'Код страны по классификатору ОКСМ', '19', 'гр. 13.1']) &&
            checkHeaderRow(xml, rc, 18, ['', 'Регион (код)', '20', 'гр. 13.2']) &&
            checkHeaderRow(xml, rc, 19, ['', 'Город', '21', 'гр. 13.3']) &&
            checkHeaderRow(xml, rc, 20, ['', 'Населенный пункт', '22', 'гр. 13.4']) &&
            checkHeaderRow(xml, rc, 21, ['Код условия поставки', '', '23', 'гр. 14']) &&
            checkHeaderRow(xml, rc, 22, ['Количество', '', '24', 'гр. 15']) &&
            checkHeaderRow(xml, rc, 23, ['Сумма доходов Банка по данным бухгалтерского учета, руб.', '', '25', 'гр. 16']) &&
            checkHeaderRow(xml, rc, 24, ['Сумма расходов Банка по данным бухгалтерского учета, руб.', '', '26', 'гр. 17']) &&
            checkHeaderRow(xml, rc, 25, ['Цена (тариф) за единицу измерения без учета НДС, руб.', '', '27', 'гр. 18']) &&
            checkHeaderRow(xml, rc, 26, ['Итого стоимость без учета НДС, руб.', '', '28', 'гр. 19']) &&
            checkHeaderRow(xml, rc, 27, ['Дата совершения сделки', '', '29', 'гр. 20'])
    )
    return result
}

def checkHeaderRow(def xml, def headRowCount, def cellNumber, def arrayHeaders){
    for (int i = 0; i < headRowCount; i++){
        String value = xml.row[i].cell[cellNumber]
        //убираем перевод строки, множественное использование пробелов
        value = value.replaceAll('\\n', '').replaceAll('\\r','').replaceAll(' {2,}', ' ').trim()
        if (value != arrayHeaders[i]){
            println("row index '"+ i +"' row value '" + value + "' cellNumber '" + cellNumber + "' value '" + arrayHeaders[i] + "'")
            return false
        }
    }
    return true
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    Date date = new Date()

    def headShift = getHeaderRowCount() - 1
    def cache = [:]
    def data = formDataService.getDataRowHelper(formData)
    data.clear()

    def indexRow = -1     // пропустить шапку таблицы
    for (def row : xml.row) {
        indexRow++
        // пропустить шапку таблицы
        if (indexRow <= headShift) {
            continue
        }

        if ((row.cell.find {it.text() != ""}.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        ['name', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate', 'innerCode',
                'unitCountryCode', 'signPhis', 'countryCode2', 'region1', 'city1', 'settlement1', 'countryCode3', 'region2',
                'city2', 'settlement2', 'conditionCode', 'count', 'incomeSum', 'consumptionSum', 'transactionDate'].each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def indexCell = 0
        // столбец 1
        newRow.rowNum = newRow.index = indexRow - headShift

        // столбец 2
        newRow.name = getRecordId(9, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // столбец 3
        newRow.innKio = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // столбец 4
        newRow.country = getRecordId(10, 'NAME', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // столбец 5
        newRow.countryCode1 = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // столбец 6
        newRow.contractNum = row.cell[indexCell].text()
        indexCell++

        // столбец 7
        newRow.contractDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // столбец 8
        newRow.transactionNum = row.cell[indexCell].text()
        indexCell++

        // столбец 9
        newRow.transactionDeliveryDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // столбец 10
        newRow.innerCode = getRecordId(17, 'INNER_CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // столбец 11
        newRow.okpCode = getRecordId(17, 'OKP_CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // столбец 12
        newRow.unitCountryCode = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // столбец 13
        newRow.signPhis = getRecordId(18, 'SIGN', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // столбец 14
        newRow.signTransaction = getRecordId(38, 'VALUE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // столбец 15
        newRow.countryCode2 = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // столбец 16
        String code = row.cell[indexCell].text()
        if (code.length() == 1){    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.region1 = getRecordId(4, 'CODE', code, date, cache, indexRow, indexCell)
        indexCell++

        // столбец 17
        newRow.city1 = row.cell[indexCell].text()
        indexCell++

        // столбец 18
        newRow.settlement1 = row.cell[indexCell].text()
        indexCell++

        // столбец 19
        newRow.countryCode3 = getRecordId(10, 'CODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // столбец 20
        code = row.cell[indexCell].text()
        if (code.length() == 1){    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.region2 = getRecordId(4, 'CODE', code, date, cache, indexRow, indexCell)
        indexCell++

        // столбец 21
        newRow.city2 = row.cell[indexCell].text()
        indexCell++

        // столбец 22
        newRow.settlement2 = row.cell[indexCell].text()
        indexCell++

        // столбец 23
        newRow.conditionCode = getRecordId(63, 'STRCODE', row.cell[indexCell].text(), date, cache, indexRow, indexCell)
        indexCell++

        // столбец 24
        newRow.count = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // столбец 25
        newRow.incomeSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // столбец 26
        newRow.consumptionSum = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // столбец 27
        newRow.priceOne = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // столбец 28
        newRow.totalNds = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // столбец 29
        newRow.transactionDate = getDate(row.cell[indexCell].text(), indexRow, indexCell)

        data.insert(newRow, indexRow - headShift)
        println(indexRow - headShift)
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
        throw new Exception("Строка ${indexRow - (getHeaderRowCount() - 1)} столбец ${indexCell + 2} содержит недопустимый тип данных!")
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
    throw new Exception("Строка ${indexRow - (getHeaderRowCount() - 1)} столбец ${indexCell + 2} содержит значение, отсутствующее в справочнике!")
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
        throw new Exception("Строка ${indexRow - (getHeaderRowCount() - 1)} столбец ${indexCell + 2} содержит недопустимый тип данных!")
    }
}