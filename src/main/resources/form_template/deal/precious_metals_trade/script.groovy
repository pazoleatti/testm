package form_template.deal.precious_metals_trade

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * Купля-продажа драгоценных металлов
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
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
        acceptance()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        acceptance()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
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
    def index = currentDataRow != null ? currentDataRow.getIndex() : (size == 0 ? 1 : size)
    ['fullName', 'interdependence', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealFocus', 'deliverySign', 'metalName',
            'countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2',
            'locality2', 'deliveryCode', 'incomeSum', 'outcomeSum', 'dealDoneDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
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
    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()
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
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }
        //  Корректность даты договора
        def taxPeriod = taxPeriodService.get(reportPeriodService.get(formData.reportPeriodId).taxPeriodId)
        def dFrom = taxPeriod.getStartDate()
        def dTo = taxPeriod.getEndDate()
        def dt = docDateCell.value
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = docDateCell.column.name
            if (dt > dTo) {
                logger.warn("«$msg» в строке $rowNum не может быть больше даты окончания отчётного периода!")
            }
            if (dt < dFrom) {
                logger.warn("«$msg» в строке $rowNum не может быть меньше даты начала отчётного периода!")
            }
        }
        // Корректность даты заключения сделки
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        // Зависимости от признака физической поставки
        if (row.deliverySign == 1) {
            def msg1 = row.getCell('deliverySign').column.name
            ['countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2', 'locality2'].each {
                def cell = row.getCell(it)
                if (cell.value != null && !cell.value.toString().isEmpty()) {
                    def msg2 = cell.column.name
                    logger.warn("«$msg1» указан «ОМС», графа «$msg2» строки $rowNum заполняться не должна!")
                }
            }
        }
        // Проверка заполнения населенного пункта
        localityCell = row.getCell('locality');
        cityCell = row.getCell('city');
        if (localityCell.value != null && !localityCell.value.toString().isEmpty() && cityCell.value != null && !cityCell.value.toString().isEmpty()) {
            logger.warn(' Если указан «' + localityCell.column.name + '», не должен быть указан ' + cityCell.column.name + '» в строке ' + rowNum + '!')
        }
        localityCell = row.getCell('locality2');
        cityCell = row.getCell('city2');
        if (localityCell.value != null && !localityCell.value.toString().isEmpty() && cityCell.value != null && !cityCell.value.toString().isEmpty()) {
            logger.warn(' Если указан «' + localityCell.column.name + '», не должен быть указан ' + cityCell.column.name + '» в строке ' + rowNum + '!')
        }
        // Проверка доходов и расходов
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            logger.warn("«$msgIn» и «$msgOut» в строке $rowNum не могут быть одновременно заполнены!")
        }
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            logger.warn("Одна из граф «$msgIn» и «$msgOut» в строке $rowNum должна быть заполнена!")
        }
        // Проверка количества
        if (row.count != 1) {
            def msg = row.getCell('count').column.name
            logger.warn("В графе «$msg» в строке $rowNum может  быть указано только значение «1»!")
        }
        // Проверка внешнеторговой сделки
        def msg14 = row.getCell('foreignDeal').column.name
        def sign = refBookService.getNumberValue(38, row.foreignDeal, 'CODE')
        if (row.countryCodeNumeric == row.countryCodeNumeric2 && sign != 0) {
            logger.warn("«$msg14» в строке $rowNum должен быть «Нет»!")
        } else if (row.countryCodeNumeric != row.countryCodeNumeric2 && sign != 1) {
            logger.warn("«$msg14» в строке $rowNum должен быть «Да»!")
        }
        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        // Проверка заполнения стоимости сделки
        def total = row.getCell('total')
        def price = row.getCell('price')
        if (total.value != price.value) {
            def msg1 = total.column.name
            def msg2 = price.column.name
            logger.warn("«$msg1» не может отличаться от «$msg2» в строке $rowNum!")
        }
        //Проверки соответствия НСИ
        checkNSI(row, "fullName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryCode", "ОКСМ", 10)
        checkNSI(row, "countryCodeNumeric", "ОКСМ", 10)
        checkNSI(row, "countryCodeNumeric2", "ОКСМ", 10)
        checkNSI(row, "region", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "region2", "Коды субъектов Российской Федерации", 4)
        checkNSI(row, "metalName", "Коды драгоценных металлов", 40)
        checkNSI(row, "deliverySign", "Признаки физической поставки", 44)
        checkNSI(row, "deliveryCode", "Коды условий поставки", 47)
        checkNSI(row, "dealFocus", "Направленности сделок", 46)
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
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (row in dataRows) {
        // Расчет поля "Цена"
        row.price = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        // Расчет поля "Итого"
        row.total = row.price
        // Расчет поля "Количество"
        row.count = 1

        // Расчет полей зависимых от справочников
        if (row.fullName != null) {
            def map = refBookService.getRecordData(9, row.fullName)
            row.inn = map.INN_KIO.numberValue
            row.countryCode = map.COUNTRY.referenceValue
            row.countryName = map.COUNTRY.referenceValue
        } else {
            row.inn = null
            row.countryCode = null
            row.countryName = null
        }
        if (row.deliverySign == 1) {
            row.countryCodeNumeric = null
            row.regionCode = null
            row.city = null
            row.locality = null
            row.countryCodeNumeric2 = null
            row.regionCode2 = null
            row.city2 = null
            row.locality2 = null
        }
        if (row.countryCodeNumeric == row.countryCodeNumeric2) {
            row.foreignDeal = 0
        } else {
            row.foreignDeal = 1
        }
    }
    dataRowHelper.update(dataRows);
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
        row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            dataRowHelper.delete(row)
            iter.remove()
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
        def newRow = formData.createDataRow()

        newRow.fullName = 'Подитог:'
        newRow.setAlias('itg')
        newRow.getCell('fullName').colSpan = 22

        // Расчеты подитоговых значений
        def BigDecimal incomeSumItg = 0, outcomeSumItg = 0, totalItg = 0
        for (row in dataRows) {

            def incomeSum = row.incomeSum
            def outcomeSum = row.outcomeSum
            def total = row.total

            incomeSumItg += incomeSum != null ? incomeSum : 0
            outcomeSumItg += outcomeSum != null ? outcomeSum : 0
            totalItg += total != null ? total : 0
        }

        newRow.incomeSum = incomeSumItg
        newRow.outcomeSum = outcomeSumItg
        newRow.total = totalItg

        dataRows.add(dataRows.size(), newRow)
        dataRowHelper.insert(newRow, dataRows.size())
    }
}