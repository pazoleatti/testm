package form_template.deal.precious_metals_trade

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
    dataRowHelper.insert(row, index)
    dataRows.add(row)
    ['fullName', 'interdependence', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealFocus', 'deliverySign', 'metalName',
            'foreignDeal', 'countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2',
            'locality2', 'deliveryCode', 'incomeSum', 'outcomeSum', 'dealDoneDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.save(dataRows)
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
        // TODO (проверка связана со справочниками) if (row.getCell('deliverySign').value == 1 ){
        if (false) {
            ['countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2', 'locality2'].each {
                cell = row.getCell(it)
                if (cell.value != null && !cell.value.toString().isEmpty()) {
                    logger.warn('«' + row.getCell('deliverySign').column.name + '» указан «ОМС», ' +
                            'графа «' + cell.column.name + '» строки ' + rowNum + ' заполняться не должна!')
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
            logger.warn("В графе «$msg» в строке $rowNum может  быть указано только  значение «1»!")
        }
        // Проверка внешнеторговой сделки
        // TODO (проверки связаны со справочниками)
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
    }
    checkNSI()
}

/**
 * Проверка соответствия НСИ
 */
void checkNSI() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    for (row in dataRowHelper.getAllCached()) {
        // TODO добавить проверки НСИ
    }
}

/**
 * Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    for (row in dataRowHelper.getAllCached()) {
        // Расчет поля "Цена"
        row.price = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        // Расчет поля "Итого"
        row.total = row.price
        // Расчет поля "Количество"
        row.count = 1
        // TODO расчет полей по справочникам
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
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            formDataService.getDataRowHelper(source).getAllCached().each { row ->
                if (row.getAlias() == null) {
                    dataRowHelper.insert(row, index++)
                    dataRows.add(row)
                }
            }
        }
    }
    dataRowHelper.save(dataRows);
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
    dataRowHelper.save(dataRows);
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
        dataRowHelper.save(dataRows);
    }
}