package form_template.deal.precious_metals_trade.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * 394 - Купля-продажа драгоценных металлов (19)
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

// Кэш провайдеров
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

def getAtributes() {
    [
            rowNum:             ['rowNum',              'гр. 1', '№ п/п'],
            fullName:           ['fullName',            'гр. 2.1', 'Полное наименование с указанием ОПФ'],
            interdependence:    ['interdependence',     'гр. 2.2', 'Признак взаимозависимости'],
            inn:                ['inn',                 'гр. 3', 'ИНН/ КИО'],
            countryName:        ['countryName',         'гр. 4.1', 'Наименование страны регистрации'],
            countryCode:        ['countryCode',         'гр. 4.2', 'Код страны регистрации по классификатору ОКСМ'],
            docNumber:          ['docNumber',           'гр. 5', 'Номер договора'],
            docDate:            ['docDate',             'гр. 6', 'Дата договора'],
            dealNumber:         ['dealNumber',          'гр. 7', 'Номер сделки'],
            dealDate:           ['dealDate',            'гр. 8', 'Дата заключения сделки'],
            dealFocus:          ['dealFocus',           'гр. 9', 'Направленность сделки'],
            deliverySign:       ['deliverySign',        'гр. 10', 'Признак физической поставки драгоценного металла'],
            metalName:          ['metalName',           'гр. 11', 'Наименование драгоценного металла'],
            foreignDeal:        ['foreignDeal',         'гр. 12', 'Внешнеторговая сделка'],
            countryCodeNumeric: ['countryCodeNumeric',  'гр. 13.1', 'Код страны по классификатору ОКСМ (цифровой)'],
            regionCode:         ['regionCode',          'гр. 13.2', 'Регион (код)'],
            city:               ['city',                'гр. 13.3', 'Город'],
            locality:           ['locality',            'гр. 13.4', 'Населенный пункт (село, поселок и т.д.)'],
            countryCodeNumeric2:['countryCodeNumeric2', 'гр. 14.1', 'Код страны по классификатору ОКСМ (цифровой)'],
            region2:            ['region2',             'гр. 14.2', 'Регион (код)'],
            city2:              ['city2',               'гр. 14.3', 'Город'],
            locality2:          ['locality2',           'гр. 14.4', 'Населенный пункт (село, поселок и т.д.)'],
            deliveryCode:       ['deliveryCode',        'гр. 15', 'Код условия поставки'],
            count:              ['count',               'гр. 16', 'Количество'],
            incomeSum:          ['incomeSum',           'гр. 17', 'Сумма доходов Банка по данным бухгалтерского учета, руб.'],
            outcomeSum:         ['outcomeSum',          'гр. 18', 'Сумма расходов Банка по данным бухгалтерского учета, руб.'],
            price:              ['price',               'гр. 19', 'Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.'],
            total:              ['total',               'гр. 20', 'Итого стоимость без учета НДС, акцизов и пошлины, руб.'],
            dealDoneDate:       ['dealDoneDate',        'гр. 21', 'Дата совершения сделки']
    ]
}

/*
Возвращает графу вида "гр. хх"
 */
def getGrafNum(def alias) {
    def atr = getAtributes().find { it -> it.getValue()[0] == alias }
    atr.getValue()[1]
}

// гр. 2.1, гр. 3, гр. 5, гр. 6, гр. 9, гр. 10, гр. 11, гр. 12, гр. 15
def getGroupColumns() {
    ['fullName', 'inn', 'docNumber', 'docDate', 'dealFocus',
            'deliverySign', 'metalName', 'foreignDeal', 'deliveryCode']
}

def getEditColumns() {
    ['fullName', 'interdependence', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealFocus', 'deliverySign', 'metalName',
            'countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2',
            'locality2', 'deliveryCode', 'incomeSum', 'outcomeSum', 'dealDoneDate']
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
    def Date date = new Date()
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    def dFrom = startDate.time
    startDate.add(Calendar.YEAR, 1)
    startDate.add(Calendar.DAY_OF_YEAR, -1)
    def dTo = startDate.time

    def dataRows = dataRowHelper.getAllCached()

    def rowNum = 0;
    for (row in dataRows) {
        rowNum++
        if (row.getAlias() != null) {
            continue
        }
        def docDateCell = row.getCell('docDate')
        def dealDateCell = row.getCell('dealDate')
        [
                'rowNum',         // № п/п
                'fullName',       // Полное наименование с указанием ОПФ
                'interdependence',// Признак взаимозависимости
                'inn',            // ИНН/КИО
                'countryName',    // Наименование страны регистрации
                'countryCode',    // Код страны по классификатору ОКСМ
                'docNumber',      // Номер договора
                'docDate',        // Дата договора
                'dealNumber',     // Номер сделки
                'dealDate',       // Дата заключения сделки
                'dealFocus',      // Направленности сделок
                'deliverySign',   // Признак физической поставки драгоценного металла
                'metalName',      // Наименование драгоценного металла
                'foreignDeal',    // Внешнеторговая сделка
                'count',          // Количество
                'price',          // Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
                'total',          // Итого стоимость без учета НДС, акцизов и пошлин, руб.
                'dealDoneDate'    // Дата совершения сделки

        ].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Строка $rowNum: Графа «$msg» не заполнена!")
            }
        }

        // Корректность даты договора
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

        // Зависимости от признака физической поставки
        def Boolean deliveryPhis = null
        if (row.deliverySign != null) {
            deliveryPhis = getRefBookValue(18, row.deliverySign).CODE.numberValue == 1
        }

        if (deliveryPhis != null && deliveryPhis) {
            def isHaveNotEmptyField = false
            def msg1 = getColumnName(row, 'deliverySign')
            def checkField = ['countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2', 'locality2', 'deliveryCode']
            for (it in checkField) {
                isHaveNotEmptyField = row.getCell(it).value != null && !row.getCell(it).value.toString().isEmpty()
                if (isHaveNotEmptyField)
                    break
            }
            if (isHaveNotEmptyField) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < checkField.size(); i++) {
                    builder.append("«").append(getAtributes().get(checkField.get(i))[2])
                            .append("» ").append("(")
                            .append(getAtributes().get(checkField.get(i))[1])
                            .append(")")
                    if (i != (checkField.size() - 1)) {
                        builder.append(", ")
                    }
                }
                logger.warn("Строка $rowNum: В графе «$msg1» указан «ОМС», графы ${builder.toString()} заполняться не должны!")
            }
        }

        // Проверка заполнения населенного пункта
        localityCell = row.getCell('locality');
        def locGrafNum = getGrafNum('locality')
        def cityGrafNum = getGrafNum('city')
        cityCell = row.getCell('city');
        if (localityCell.value != null && !localityCell.value.toString().isEmpty() && cityCell.value != null && !cityCell.value.toString().isEmpty()) {
            logger.warn("Строка $rowNum: Если указан «${localityCell.column.name}»($locGrafNum), не должен быть указан  «${cityCell.column.name}»($cityGrafNum)!")
        }
        localityCell = row.getCell('locality2');
        cityCell = row.getCell('city2');
        locGrafNum = getGrafNum('locality2')
        cityGrafNum = getGrafNum('city2')
        if (localityCell.value != null && !localityCell.value.toString().isEmpty() && cityCell.value != null && !cityCell.value.toString().isEmpty()) {
            logger.warn("Строка $rowNum: Если указан «${localityCell.column.name}»($locGrafNum), не должен быть указан  «${cityCell.column.name}»($cityGrafNum)!")
        }

        // Проверка доходов и расходов
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            logger.warn("Строка $rowNum: Одна из граф «$msgIn» и «$msgOut» должна быть заполнена!")
        }

        // Проверка доходов/расходов и стоимости
        def msgPrice = getColumnName(row, 'price')
        if (incomeSumCell.value != null && outcomeSumCell.value != null && row.price != null) {
            if (row.price.abs() != (incomeSumCell.value - outcomeSumCell.value).abs())
                logger.warn("Строка $rowNum: Графа «$msgPrice» должна быть равна разнице графы «$msgIn» и «$msgOut» по модулю!")
        } else if (incomeSumCell.value != null) {
            if (row.price != incomeSumCell.value)
                logger.warn("Строка $rowNum: Графа «$msgPrice» должна быть равна «$msgIn»!")
        } else if (outcomeSumCell.value != null) {
            if (row.price != outcomeSumCell.value)
                logger.warn("Строка $rowNum: Графа «$msgPrice» должна быть равна «$msgOut»!")
        }

        // Проверка количества
        if (row.count != 1) {
            def msg = getColumnName(row, 'count')
            logger.warn("Строка $rowNum: В графе «$msg» может быть указано только значение «1»!")
        }

        // Проверка внешнеторговой сделки
        def msg14 = getColumnName(row, 'foreignDeal')

        // "Да"
        def recYesId = getRecordId(38, 'CODE', '1', date, rowNum-2, msg14, false)
        // "Нет"
        def recNoId = getRecordId(38, 'CODE', '0', date, rowNum-2, msg14, false)

        if (row.countryCodeNumeric == row.countryCodeNumeric2) {
            if (row.foreignDeal != recNoId) {
                logger.warn("Строка $rowNum: «$msg14» должно иметь значение «Нет»!")
            }
        }
        else if (deliveryPhis != null && deliveryPhis) {
            if (row.foreignDeal != recNoId) {
                logger.warn("Строка $rowNum: «$msg14» должно иметь значение «Нет»!")
            }
        } else {
            if (row.foreignDeal != recYesId) {
                logger.warn("Строка $rowNum: «$msg14» должно иметь значение «Да»!")
            }
        }

        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("Строка $rowNum: «$msg1» не может быть меньше «$msg2»!")
        }

        // Проверка заполнения стоимости сделки
        def total = row.getCell('total')
        def price = row.getCell('price')
        if (total.value != price.value) {
            def msg1 = total.column.name
            def msg2 = price.column.name
            logger.warn("Строка $rowNum: «$msg1» не может отличаться от «$msg2»!")
        }

        // Проверка заполнения региона отправки
        if (row.countryCodeNumeric != null) {
            def country = getRefBookValue(10, row.countryCodeNumeric).CODE.stringValue
            if (country != null) {
                def regionName = getColumnName(row, 'regionCode')
                def countryName = getColumnName(row, 'countryCodeNumeric')
                if (country == '643' && row.regionCode == null) {
                    logger.warn("Строка $rowNum: «$regionName» должен быть заполнен, т.к. в «$countryName» указан код 643!")
                } else if (country != '643' && row.regionCode != null) {
                    logger.warn("Строка $rowNum: «$regionName» не должен быть заполнен, т.к. в «$countryName» указан код, отличный от 643!")
                }
            }
        }

        // Проверка заполнения региона доставки
        if (row.countryCodeNumeric2 != null) {
            def country = getRefBookValue(10, row.countryCodeNumeric2).CODE.stringValue//refBookService.getStringValue(10, row.countryCodeNumeric2, 'CODE')
            if (country != null) {
                def regionName = getColumnName(row, 'region2')
                def countryName = getColumnName(row, 'countryCodeNumeric2')
                if (country == '643' && row.region2 == null) {
                    logger.warn("Строка $rowNum: «$regionName» должен быть заполнен, т.к. в «$countryName» указан код 643!")
                } else if (country != '643' && row.region2 != null) {
                    logger.warn("Строка $rowNum: «$regionName» не должен быть заполнен, т.к. в «$countryName» указан код, отличный от 643!")
                }
            }
        }

        //Проверки соответствия НСИ
        checkNSI(row, "fullName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryCode", "ОКСМ", 10)
        checkNSI(row, "countryCodeNumeric", "ОКСМ", 10)
        checkNSI(row, "countryCodeNumeric2", "ОКСМ", 10)
        checkNSI(row, "regionCode", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "region2", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "metalName", "Коды драгоценных металлов", 17)
        checkNSI(row, "deliverySign", "Признаки физической поставки", 18)
        checkNSI(row, "deliveryCode", "Коды условий поставки", 63)
        checkNSI(row, "dealFocus", "Направленности сделок", 20)
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
                        logger.error(mes + " «${getAtributes().price[2]}»")
                    }
                    if (testItogRow.total != realItogRow.total) {
                        logger.error(mes + " «${getAtributes().total[2]}»")
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
    if (cell.value != null && getRefBookValue(id, cell.value) == null) {
        def msg2 = cell.column.name
        def rowNum = row.getIndex()
        logger.warn("Строка $rowNum: В справочнике «$msg» не найден элемент «$msg2»!")
    }
}

/**
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    def Date date = new Date()
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def index = 1;
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // Порядковый номер строки
        row.rowNum = index++
        // Расчет поля "Цена"
        if (row.incomeSum != null && row.outcomeSum != null) {
            row.price = (row.incomeSum - row.outcomeSum).abs()
        } else
            row.price = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        // Расчет поля "Итого"
        row.total = row.price
        // Расчет поля "Количество"
        row.count = 1

        // Расчет полей зависимых от справочников
        if (row.fullName != null) {
            def map = getRefBookValue(9, row.fullName)
            row.inn = map.INN_KIO.stringValue
            row.countryCode = map.COUNTRY.referenceValue
            row.countryName = map.COUNTRY.referenceValue
        } else {
            row.inn = null
            row.countryCode = null
            row.countryName = null
        }

        // Признак физической поставки
        def Boolean deliveryPhis = null
        if (row.deliverySign != null) {
            deliveryPhis = getRefBookValue(18, row.deliverySign).CODE.numberValue == 1
        }

        if (deliveryPhis != null && deliveryPhis) {
            row.countryCodeNumeric = null
            row.regionCode = null
            row.city = null
            row.locality = null
            row.countryCodeNumeric2 = null
            row.region2 = null
            row.city2 = null
            row.locality2 = null
        }

        if (row.countryCodeNumeric == row.countryCodeNumeric2 || deliveryPhis) {
            row.foreignDeal = getRecordId(38, 'CODE', '0', date, index-3,  getColumnName(row, 'foreignDeal'), false)
        } else {
            row.foreignDeal = getRecordId(38, 'CODE', '1', date, index-3, getColumnName(row, 'foreignDeal'), false)
        }
    }

    dataRowHelper.update(dataRows);
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
 * Сортировка строк по гр.
 */
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    dataRows.sort({ DataRow a, DataRow b ->
        // гр. 2.1, гр. 3, гр. 5, гр. 6, гр. 9, гр. 10, гр. 11, гр. 12, гр. 15
        sortRow(getGroupColumns(), a, b)
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
 * Расчет подитогового значения
 * @param i
 * @return
 */
def calcItog(int i, def dataRows) {
    def newRow = formData.createDataRow()

    newRow.getCell('itog').colSpan = 26
    newRow.itog = 'Подитог:'
    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fix').colSpan = 2

    // Расчеты подитоговых значений
    def BigDecimal priceItg = 0, totalItg = 0
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        row = dataRows.get(j)

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

/**
 *  Возвращает строку со значениями полей строки по которым идет группировка
 *  ['fullName', 'inn', 'docNumber', 'docDate', 'dealFocus', 'deliverySign', 'metalName', 'foreignDeal', 'deliveryCode']
 */
String getValuesByGroupColumn(DataRow row) {
    def sep = ", "
    StringBuilder builder = new StringBuilder()
    def map = row.fullName != null ? getRefBookValue(9, row.fullName)/*refBookService.getRecordData(9, row.fullName)*/ : null
    if (map != null)
        builder.append(map.NAME.stringValue).append(sep)
    if (row.inn != null)
        builder.append(row.inn).append(sep)
    if (row.docNumber != null)
        builder.append(row.docNumber).append(sep)
    if (row.docDate != null)
        builder.append(row.docDate).append(sep)
    dealFocus = getRefBookValue(20, row.dealFocus, 'DIRECTION')
    if (dealFocus != null)
        builder.append(dealFocus).append(sep)
    deliverySign = getRefBookValue(18, row.deliverySign, 'SIGN')
    if (deliverySign != null)
        builder.append(deliverySign).append(sep)
    metalName = getRefBookValue(17, row.metalName, 'INNER_CODE')
    if (metalName != null)
        builder.append(metalName).append(sep)
    foreignDeal = getRefBookValue(38, row.foreignDeal, 'VALUE')
    if (foreignDeal != null)
        builder.append(foreignDeal).append(sep)
    deliveryCode = getRefBookValue(63, row.deliveryCode, 'STRCODE')
    if (deliveryCode != null)
        builder.append(deliveryCode).append(sep)

    def String retVal = builder.toString()
    if (retVal.length() < 2)
        return null
    retVal.substring(0, retVal.length() - 2)
}

def getRefBookValue(int id, def cell, def alias) {
    def map = cell != null ? getRefBookValue(id, cell)/*refBookService.getRecordData(id, cell)*/ : null
    return map == null ? null : map.get(alias).stringValue
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

def isEquals(def xmlValue, String value) {
    return xmlValue.text().trim() == value.trim()
}
/**
 * Проверить шапку таблицы.
 *
 * @param xml данные
 * @param headRowCount количество строк в шапке
 */
def checkTableHead(def xml, def headRowCount) {
    def colCount = 28
    // проверить количество строк и колонок в шапке
    if (xml.row.size() < headRowCount || xml.row[0].cell.size() < colCount) {
        return false
    }
    def result = (
            isEquals(xml.row[0].cell[0], 'Полное наименование с указанием ОПФ') &&
                    isEquals(xml.row[2].cell[0], 'гр. 2.1') &&
                    isEquals(xml.row[0].cell[1], 'Признак взаимозависимости') &&
                    isEquals(xml.row[2].cell[1], 'гр. 2.2') &&
                    isEquals(xml.row[0].cell[2], 'ИНН/ КИО') &&
                    isEquals(xml.row[2].cell[2], 'гр. 3') &&
                    isEquals(xml.row[0].cell[3], 'Наименование страны регистрации') &&
                    isEquals(xml.row[2].cell[3], 'гр. 4.1') &&
                    isEquals(xml.row[0].cell[4], 'Код страны регистрации по классификатору ОКСМ') &&
                    isEquals(xml.row[2].cell[4], 'гр. 4.2') &&
                    isEquals(xml.row[0].cell[5], 'Номер договора') &&
                    isEquals(xml.row[2].cell[5], 'гр. 5') &&
                    isEquals(xml.row[0].cell[6], 'Дата договора') &&
                    isEquals(xml.row[2].cell[6], 'гр. 6') &&
                    isEquals(xml.row[0].cell[7], 'Номер сделки') &&
                    isEquals(xml.row[2].cell[7], 'гр. 7') &&
                    isEquals(xml.row[0].cell[8], 'Дата заключения сделки') &&
                    isEquals(xml.row[2].cell[8], 'гр. 8') &&
                    isEquals(xml.row[0].cell[9], 'Направленность сделки') &&
                    isEquals(xml.row[2].cell[9], 'гр. 9') &&
                    isEquals(xml.row[0].cell[10], 'Признак физической поставки драгоценного металла') &&
                    isEquals(xml.row[2].cell[10], 'гр. 10') &&
                    isEquals(xml.row[0].cell[11], 'Наименование драгоценного металла') &&
                    isEquals(xml.row[2].cell[11], 'гр. 11') &&
                    isEquals(xml.row[0].cell[12], 'Внешнеторговая сделка') &&
                    isEquals(xml.row[2].cell[12], 'гр. 12') &&
                    //isEquals(xml.row[0].cell[13], 'Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами') &&
                    isEquals(xml.row[1].cell[13], '\"Код страны по классификатору ОКСМ (цифровой)\"') &&
                    isEquals(xml.row[2].cell[13], 'гр. 13.1') &&
                    isEquals(xml.row[0].cell[14], '') &&
                    isEquals(xml.row[1].cell[14], '\"Регион (код)\"') &&
                    isEquals(xml.row[2].cell[14], 'гр. 13.2') &&
                    isEquals(xml.row[0].cell[15], '') &&
                    isEquals(xml.row[1].cell[15], 'Город') &&
                    isEquals(xml.row[2].cell[15], 'гр. 13.3') &&
                    isEquals(xml.row[0].cell[16], '') &&
                    isEquals(xml.row[1].cell[16], 'Населенный пункт (село, поселок и т.д.)') &&
                    isEquals(xml.row[2].cell[16], 'гр. 13.4') &&
                    isEquals(xml.row[0].cell[17], 'Место совершения сделки (адрес места доставки (разгрузки драгоценного металла)') &&
                    isEquals(xml.row[1].cell[17], 'Код страны по классификатору ОКСМ (цифровой)') &&
                    isEquals(xml.row[2].cell[17], 'гр. 14.1') &&
                    isEquals(xml.row[0].cell[18], '') &&
                    isEquals(xml.row[1].cell[18], '\"Регион (код)\"') &&
                    isEquals(xml.row[2].cell[18], 'гр. 14.2') &&
                    isEquals(xml.row[0].cell[19], '') &&
                    isEquals(xml.row[1].cell[19], 'Город') &&
                    isEquals(xml.row[2].cell[19], 'гр. 14.3') &&
                    isEquals(xml.row[0].cell[20], '') &&
                    isEquals(xml.row[1].cell[20], 'Населенный пункт (село, поселок и т.д.)') &&
                    isEquals(xml.row[2].cell[20], 'гр. 14.4') &&
                    isEquals(xml.row[0].cell[21], 'Код условия поставки') &&
                    isEquals(xml.row[2].cell[21], 'гр. 15') &&
                    isEquals(xml.row[0].cell[22], 'Количество') &&
                    isEquals(xml.row[2].cell[22], 'гр. 16') &&
                    isEquals(xml.row[0].cell[23], 'Сумма доходов Банка по данным бухгалтерского учета, руб.') &&
                    isEquals(xml.row[2].cell[23], 'гр. 17') &&
                    isEquals(xml.row[0].cell[24], 'Сумма расходов Банка по данным бухгалтерского учета, руб.') &&
                    isEquals(xml.row[2].cell[24], 'гр. 18') &&
                    isEquals(xml.row[0].cell[25], 'Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.') &&
                    isEquals(xml.row[2].cell[25], 'гр. 19') &&
                    isEquals(xml.row[0].cell[26], 'Итого стоимость без учета НДС, акцизов и пошлины, руб.') &&
                    isEquals(xml.row[2].cell[26], 'гр. 20') &&
                    isEquals(xml.row[0].cell[27], 'Дата совершения сделки') &&
                    isEquals(xml.row[2].cell[27], 'гр. 21'))

    return result
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml, int headRowCount) {
    Date date = reportPeriodService.get(formData.reportPeriodId).taxPeriod.getEndDate()

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def rows = new LinkedList()

    def xmlIndexRow = -1
    def rowIndex = 0

    for (def row : xml.row) {
        xmlIndexRow++

        // пропустить шапку таблицы
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        rowIndex++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        getEditColumns().each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def xmlIndexCell = 0

        // графа 1
        newRow.rowNum = xmlIndexRow - headRowCount

        // графа 2.1
        newRow.fullName = getRecordId(9, 'NAME', row.cell[xmlIndexCell].text(), date, xmlIndexRow, getColumnName(newRow, 'fullName'), false)
        def map = newRow.fullName == null ? null : getRefBookValue(9, newRow.fullName)
        xmlIndexCell++

        // графа 2.2
        newRow.interdependence = getRecordId(38, 'VALUE', row.cell[xmlIndexCell].text(), date, xmlIndexRow, getColumnName(newRow, 'interdependence'), false)
        xmlIndexCell++

        // графа 3
        if (map != null) {
            def text = row.cell[xmlIndexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.INN_KIO.stringValue)) || ((text == null || text.isEmpty()) && map.INN_KIO.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xmlIndexRow+2} столбец ${xmlIndexCell+2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(9).getName()+"»!")
            }
        }
        xmlIndexCell++

        // графа 4.1
        if (map != null) {
            def text = row.cell[xmlIndexCell].text()
            map = getRefBookValue(10, map.COUNTRY.referenceValue)
            if ((text != null && !text.isEmpty() && !text.equals(map.NAME.stringValue)) || ((text == null || text.isEmpty()) && map.NAME.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xmlIndexRow+3} столбец ${xmlIndexCell+2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        xmlIndexCell++

        // графа 4.2
        if (map != null) {
            def text = row.cell[xmlIndexCell].text()
            if ((text != null && !text.isEmpty() && !text.equals(map.CODE.stringValue)) || ((text == null || text.isEmpty()) && map.CODE.stringValue != null)) {
                logger.warn("Проверка файла: Строка ${xmlIndexRow+3} столбец ${xmlIndexCell+2} содержит значение, отсутствующее в справочнике «" + refBookFactory.get(10).getName()+"»!")
            }
        }
        xmlIndexCell++

        // графа 5
        newRow.docNumber = row.cell[xmlIndexCell].text()
        xmlIndexCell++

        // графа 6
        newRow.docDate = getDate(row.cell[xmlIndexCell].text(), xmlIndexRow, getColumnName(newRow, 'docDate'))
        xmlIndexCell++

        // графа 7
        newRow.dealNumber = row.cell[xmlIndexCell].text()
        xmlIndexCell++

        // графа 8
        newRow.dealDate = getDate(row.cell[xmlIndexCell].text(), xmlIndexRow, getColumnName(newRow, 'dealDate'))
        xmlIndexCell++

        // графа 9
        newRow.dealFocus = getRecordId(20, 'DIRECTION', row.cell[xmlIndexCell].text(), date, xmlIndexRow, getColumnName(newRow, 'dealFocus'), false)
        xmlIndexCell++

        // графа 10
        newRow.deliverySign = getRecordId(18, 'SIGN', row.cell[xmlIndexCell].text(), date, xmlIndexRow, getColumnName(newRow, 'deliverySign'), false)
        xmlIndexCell++

        // графа 11
        newRow.metalName = getRecordId(17, 'INNER_CODE', row.cell[xmlIndexCell].text(), date, xmlIndexRow, getColumnName(newRow, 'metalName'), false)
        xmlIndexCell++

        // графа 12
        newRow.foreignDeal = getRecordId(38, 'VALUE', row.cell[xmlIndexCell].text(), date, xmlIndexRow, getColumnName(newRow, 'foreignDeal'), false)
        xmlIndexCell++

        // графа 13.1
        newRow.countryCodeNumeric = getRecordId(10, 'CODE', row.cell[xmlIndexCell].text(), date, xmlIndexRow, getColumnName(newRow, 'countryCodeNumeric'), false)
        xmlIndexCell++

        // графа 13.2
        String code = row.cell[xmlIndexCell].text()
        if (code.length() == 1) {    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.regionCode = getRecordId(4, 'CODE', code, date, xmlIndexRow, getColumnName(newRow, 'regionCode'), false)
        xmlIndexCell++

        // графа 13.3
        newRow.city = row.cell[xmlIndexCell].text()
        xmlIndexCell++

        // графа 13.4
        newRow.locality = row.cell[xmlIndexCell].text()
        xmlIndexCell++

        // графа 14.1
        newRow.countryCodeNumeric2 = getRecordId(10, 'CODE', row.cell[xmlIndexCell].text(), date, xmlIndexRow, getColumnName(newRow, 'countryCodeNumeric2'), false)
        xmlIndexCell++

        // графа 14.2
        code = row.cell[xmlIndexCell].text()
        if (code.length() == 1) {    //для кодов 1, 2, 3...9
            code = "0".concat(code)
        }
        newRow.region2 = getRecordId(4, 'CODE', code, date, xmlIndexRow, getColumnName(newRow, 'region2'), false)
        xmlIndexCell++

        // графа 14.3
        newRow.city2 = row.cell[xmlIndexCell].text()
        xmlIndexCell++

        // графа 14.4
        newRow.locality2 = row.cell[xmlIndexCell].text()
        xmlIndexCell++

        // графа 15
        newRow.deliveryCode = getRecordId(63, 'STRCODE', row.cell[xmlIndexCell].text(), date, xmlIndexRow, getColumnName(newRow, 'deliveryCode'), false)
        xmlIndexCell++

        // графа 16
        xmlIndexCell++

        // графа 17
        newRow.incomeSum = getNumber(row.cell[xmlIndexCell].text(), xmlIndexRow, getColumnName(newRow, 'incomeSum'))
        xmlIndexCell++

        // графа 18
        newRow.outcomeSum = getNumber(row.cell[xmlIndexCell].text(), xmlIndexRow, getColumnName(newRow, 'outcomeSum'))
        xmlIndexCell++

        // графа 19
        xmlIndexCell++

        // графа 20
        xmlIndexCell++

        // графа 21
        newRow.dealDoneDate = getDate(row.cell[xmlIndexCell].text(), xmlIndexRow, getColumnName(newRow, 'dealDoneDate'))
        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value, int indexRow, String cellName) {
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
        throw new Exception("Проверка файла: Строка ${indexRow + 2}, графа «$cellName» содержит недопустимый тип данных!")
    }
}

/**
 * Получить record_id элемента справочника.
 *
 * @param value
 */
def getRecordId(def ref_id, String alias, String value, Date date, int rowIndex, String cellName, boolean mandatory = true) {
    String filter = "LOWER($alias) = LOWER('$value')"
    if (value == '') filter = "$alias is null"
    if (recordCache[ref_id] != null) {
        if (recordCache[ref_id][filter] != null) {
            return recordCache[ref_id][filter]
        }
    } else {
        recordCache[ref_id] = [:]
    }
    def refDataProvider = getProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null)
    if (records.size() == 1) {
        recordCache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return recordCache[ref_id][filter]
    } else {
        def msg = "Проверка файла: Строка ${rowIndex-2}, графа «$cellName» содержит значение, отсутствующее в справочнике «" + refBookFactory.get(ref_id).getName()+"»!"
        if (mandatory) {
            throw new Exception(msg)
        } else {
            logger.warn(msg)
        }
    }
    return null
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value, int indexRow, String cellName) {
    if (value == null || value == '') {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    try {
        return format.parse(value)
    } catch (Exception e) {
        throw new Exception("Проверка файла: Строка ${indexRow + 2}, графа «$cellName» содержит недопустимый тип данных!")
    }
}

def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

def getRefBookValue(def long refBookId, def long recordId) {
    if (!refBookCache.containsKey(recordId)) {
        refBookCache.put(recordId, refBookService.getRecordData(refBookId, recordId))
    }
    return refBookCache.get(recordId)
}