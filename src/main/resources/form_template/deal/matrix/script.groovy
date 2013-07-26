package form_template.deal.matrix

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormType
import com.aplana.sbrf.taxaccounting.model.TaxType

/**
 * Матрица
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CALCULATE:
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
    case FormDataEvent.AFTER_MOVE_CREATED_TO_ACCEPTED:
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
}

// 1.	dealNum1
// 2.	interdependenceSing
// 3.	f121
// 4.	f122
// 5.	f123
// 6.	f124
// 7.	f131
// 8.	f132
// 9.	f133
// 10.	f134
// 11.	f135
// 12.	similarTransactionsGroup
// 13.	dealNameCode
// 14.	taxpayerSideCode
// 15.	dealPriceSign
// 16.	dealPriceCode
// 17.	dealMemberCount
// 18.	income
// 19.	incomeIncludingRegulation
// 20.	outcome
// 21.	outcomeIncludingRegulation
// 22.	dealNum2
// 23.	dealType
// 24.	dealSubjectName
// 25.	dealSubjectCode1
// 26.	dealSubjectCode2
// 27.	dealSubjectCode3
// 28.	otherNum
// 29.	contractNum
// 30.	contractDate
// 31.	countryCode
// 32.	countryCode1
// 33.	region1
// 34.	city1
// 35.	locality1
// 36.	countryCode2
// 37.	region2
// 38.	city2
// 39.	locality2
// 40.	deliveryCode
// 41.	okeiCode
// 42.	count
// 43.	price
// 44.	total
// 45.	dealDoneDate
// 46.	dealNum3
// 47.	dealMemberNum
// 48.	organInfo
// 49.	countryCode3
// 50.	organName
// 51.	organINN
// 52.	organKPP
// 53.	organRegNum
// 54.	taxpayerCode
// 55.	address

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId,
            formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Формирование новой матрицы невозможно, т.к. матрица с указанными параметрами уже сформирована.')
    }
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? currentDataRow.getIndex() : (size == 0 ? 1 : size)
    dataRowHelper.insert(row, index)
    dataRows.add(row)
    // TODO пересмотреть редактируемость (пока все редактируемо)
    for (column in formData.getFormColumns()) {
        if (column.alias.equals('dealNum1')) {
            continue;
        }
        row.getCell(column.alias).editable = true
        row.getCell(column.alias).setStyleAlias('Редактируемая')
    }
    dataRowHelper.save(dataRows)
}

void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
    dataRowHelper.save(dataRowHelper.getAllCached())
}

/**
 * Логические проверки
 */
void logicCheck() {
    // TODO
    // 1. Обязательные поля
    // 2. Проверка наличия элемента справочника «Да/Нет»
    // 3. Проверка наличия элемента справочника «Коды сторон сделки»
    // 4. Проверка наличия элемента справочника «Коды типов предмета сделки»
    // 5. Проверка наличия элемента справочника «ОКП»
    // 6. Проверка наличия элемента справочника «ОКСМ»
    // 7. Проверка наличия элемента справочника «Коды субъектов Российской Федерации» для атрибута 33
    // 8. Проверка наличия элемента справочника «Коды субъектов Российской Федерации» для атрибута 37
    // 9. Проверка наличия элемента справочника «Коды условий поставки»
    // 10. Проверка наличия элемента «Коды единиц измерения на основании ОКЕИ»справочника «Коды единиц измерения на основании ОКЕИ»
    // 11. Проверка наличия элемента справочника «Коды сведений об организации»
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // Порядковый номер строки
        row.dealNum1 = row.getIndex()
        row.dealNum2 = row.getIndex()
        row.dealNum3 = row.getIndex()
        // TODO расчет полей по справочникам
    }

    dataRowHelper.save(dataRows);
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
        if (source != null && source.state == WorkflowState.ACCEPTED && source.getFormType().getTaxType() == TaxType.DEAL) {
            formDataService.getDataRowHelper(source).getAllCached().each { srcRow ->
                if (srcRow.getAlias() == null) {
                    def row = buildRow(srcRow, source.getFormType())
                    dataRowHelper.insert(row, index++)
                    dataRows.add(row)
                }
            }
        }
    }
    dataRowHelper.save(dataRows);
}

/**
 * Подготовка строки матрицы из первичных и консолидированных отчетов модуля УКС
 * @param row
 * @param type
 */
def buildRow(DataRow<Cell> srcRow, FormType type) {
    def row = formData.createDataRow()
    // TODO Заполнить справочные поля
    // Графа 2
    // row.interdependenceSing // справочное
    // Графа 3
    // row.f121 // справочное
    // Графа 4
    // row.f122 // справочное
    // Графа 5
    // row.f123 // справочное
    // Графа 6
    // row.f124 // справочное
    // Графа 7
    // row.f131 // справочное
    // Графа 8
    // row.f132 // справочное
    // Графа 9
    // row.f133 // справочное
    // Графа 10
    // row.f134 // справочное
    // Графа 11
    // row.f135 // справочное
    // Графа 12
    // similarTransactionsGroup // справочное
    // Графа 13

    switch (type.id) {
        case 376:
            row.dealNameCode = '002'
            break
        case 377:
        case 380:
        case 382:
            row.dealNameCode = '019'
            break
        case 375:
            // TODO значение справочника
            // dealNameCode = srcRow.serviceType
            break
        case 379:
        case 381:
        case 393:
        case 394:
            row.dealNameCode = '016'
            break
        case 383:
        case 391:
        case 392:
            row.dealNameCode = '032'
            break
        case 384:
            row.dealNameCode = '015'
            break
        case 385:
            row.dealNameCode = '029'
            break
        case 386:
        case 388:
            row.dealNameCode = '003'
            break
        case 387:
        case 389:
            row.dealNameCode = '012'
            break
        case 390:
            row.dealNameCode = '017'
            break
    }

    // Графа 14
    switch (type.id) {
        case 376:
            row.taxpayerSideCode = '004'
            break
        case 377:
        case 375:
        case 380:
            row.taxpayerSideCode = '012'
            break
        case 379:
            row.taxpayerSideCode = '029'
            break
        case 381:
            if (srcRow.outcomeSum == null) {
                row.taxpayerSideCode = '027'
            }
            if (srcRow.incomeSum == null) {
                row.taxpayerSideCode = '026'
            }
            break
        case 382:
            row.taxpayerSideCode = '011'
            break
        case 383:
        case 391:
        case 392:
        case 393:
            row.taxpayerSideCode = '052'
            break
        case 384:
            // TODO значение справочника (S, B)
            // row.taxpayerSideCode = srcRow.transactionType
            break
        case 385:
        case 387:
        case 389:
            row.taxpayerSideCode = '022'
            break
        case 386:
        case 388:
            row.taxpayerSideCode = '005'
            break
        case 390:
            row.taxpayerSideCode = '030'
            break
        case 394:
            if (srcRow.outcomeSum == null) {
                row.taxpayerSideCode = '027'
            }
            if (srcRow.incomeSum == null) {
                row.taxpayerSideCode = '026'
            }
            break
    }
    // Графа 15
    // dealPriceSign // справочное
    // Графа 16
    // dealPriceCode // справочное
    // Графа 17
    row.dealMemberCount = 2
    // Графа 18
    switch (type.id) {
        case 376:
            row.income = srcRow.incomeBankSum
            break
        case 382:
            row.income = srcRow.bankIncomeSum
            break
        case 379:
        case 387:
            row.income = srcRow.sum
            break
        case 377:
        case 375:
        case 380:
            row.income = 0
            break
        case 381:
            row.income = srcRow.cost
            break
        case 383:
            row.income = srcRow.priceFirstRub
            break
        case 384:
            row.income = srcRow.transactionSumRub
            break
        case 385:
            row.income = srcRow.totalCost
            break
        case 386:
        case 388:
        case 389:
            row.income = srcRow.total
            break
        case 390:
        case 391:
            row.income = srcRow.total
            break
        case 392:
            row.income = srcRow.cost
            break
        case 393:
            row.income = srcRow.totalNds
            break
        case 394:
            row.income = srcRow.total
            break
    }

    // Графа 20
    switch (type.id) {
        case 377:
            row.outcome = srcRow.bankSum
        case 375:
            row.outcome = srcRow.expensesSum
        case 380:
            row.outcome = srcRow.sum
            break
    }

    // Графа 23
    // row.dealType // справочное

    // Графа 24
    switch (type.id) {
        case 376:
            row.dealSubjectName = 'Предоставление в аренду нежилых помещений'
            break
        case 377:
            row.dealSubjectName = 'Оказание услуг по техническому обслуживанию'
            break
        case 375:
            row.dealSubjectName = 'Информационно-технологические услуги'
            break
        case 379:
            row.dealSubjectName = 'Предоставление прав пользования торговым знаком'
            break
        case 380:
            row.dealSubjectName = 'Приобретение услуг по организации и проведению торгов по реализации имущества'
            break
        case 381:
            row.dealSubjectName = 'Купля-продажа ценных бумаг'
            break
        case 382:
        case 384:
            row.dealSubjectName = 'Оказание банковских услуг'
            break
        case 383:
            row.dealSubjectName = 'РЕПО'
            break
        case 385:
            row.dealSubjectName = 'Уступка прав требования по кредитным договорам'
            break
        case 386:
        case 388:
            row.dealSubjectName = 'Предоставление банковских гарантий и иных аналогичных инструментов'
            break
        case 387:
        case 389:
            row.dealSubjectName = 'Предоставление денежных средств на условиях возвратности, платности, срочности'
            break
        case 390:
            row.dealSubjectName = 'Операции с иностранной валютой'
            break
        case 391:
        case 392:
            row.dealSubjectName = 'Операции с финансовыми инструментами срочных сделок'
            break
        case 393:
        case 394:
            row.dealSubjectName = 'Купля-продажа драгоценного металла'
            break
    }

    // Графа 25
    // TODO В аналитике не описан алгоритм
    // if (row.dealType == 1) // См. справочник

    // Графа 26
    switch (type.id) {
        case 390:
            // TODO Добавить значение "000000" в справочник и выбрать его
            // row.dealSubjectCode2 = '000000'
            break
        case 393:
            // TODO Выбрать значение по значению из отчета
            // row.dealSubjectCode2 = srcRow.okpCode
            break
    }

    // Графа 27
    // TODO Заполнение конкретными значениями из справочника
    switch (type.id) {
        case 376:
            // row.dealSubjectCode3 = '70.20.2'
            break
        case 377:
            // row.dealSubjectCode3 = '70.32.2'
            break
        case 375:
            // row.dealSubjectCode3 = '72.2'
            break
        case 379:
        case 380:
            // row.dealSubjectCode3 = '74.8'
            break
        case 381:
        case 384:
        case 386:
        case 388:
        case 391:
        case 392:
        case 393:
        case 394:
            // row.dealSubjectCode3 = '65.23'
            break
        case 382:
            // row.dealSubjectCode3 = '67.12'
            break
        case 383:
        case 385:
        case 387:
        case 389:
            // row.dealSubjectCode3 = '65.22'
            break
        case 390:
            // row.dealSubjectCode3 = '65.12'
            break
    }

    // Графа 28
    row.otherNum = 1

    // Графа 29
    switch (type.id) {
        case 376:
        case 377:
        case 382:
        case 383:
        case 384:
        case 385:
        case 392:
        case 393:
            row.contractNum = srcRow.contractNum
            break
        case 375:
        case 379:
        case 380:
        case 381:
        case 386:
        case 387:
        case 388:
        case 389:
        case 391:
        case 394:
            row.contractNum = srcRow.docNumber
            break
        case 390:
            row.contractNum = srcRow.docNum
            break
    }

    // Графа 30
    switch (type.id) {
        case 376:
        case 377:
        case 382:
        case 383:
        case 384:
        case 385:
        case 392:
        case 393:
            row.contractDate = srcRow.contractDate
            break
        case 375:
        case 379:
        case 380:
        case 381:
        case 386:
        case 387:
        case 388:
        case 389:
        case 390:
        case 391:
        case 394:
            row.docDate = srcRow.docDate
            break
    }

    // Графа 31
    switch (type.id) {
        case 393:
        case 394:
            // TODO Разобраться в аналитике. Справочники.
            break
    }

    // Графа 32
    // if (row.dealType == 1) // TODO См. справочник
    // if (type.id == 393 && srcRow.signPhis == 'физическая поставка') {
    //     row.countryCode1 =  srcRow.countryCode2
    // }
    //
    // if (type.id == 394 && srcRow.deliverySign == 'физическая поставка') {
    //     row.countryCode1 = srcRow.countryCodeNumeric
    // }

    // Графа 33
    // if (row.dealType == 1) // TODO См. справочник
    // if (type.id == 393 && srcRow.signPhis == 'физическая поставка') {
    //     row.region1 =  srcRow.region1
    // }
    //
    // if (type.id == 394 && srcRow.deliverySign == 'физическая поставка') {
    //     row.region1 = srcRow.regionCode
    // }

    // Графа 34
    // if (row.dealType == 1) // TODO См. справочник
    // if (type.id == 393 && srcRow.signPhis == 'физическая поставка') {
    //     row.city1 = srcRow.city1
    // }
    //
    // if (type.id == 394 && srcRow.deliverySign == 'физическая поставка') {
    //     row.city1 = srcRow.city
    // }

    // Графа 35
    // if (row.dealType == 1) // TODO См. справочник
    // if (type.id == 393 && srcRow.signPhis == 'физическая поставка') {
    //     row.locality1 = srcRow.settlement1
    // }
    //
    // if (type.id == 394 && srcRow.deliverySign == 'физическая поставка') {
    //     row.locality1 = srcRow.locality
    // }

    // Графа 36
    if (type.id == 393) {
        // row.countryCode2 = srcRow.countryCode3 // Справочник
    }

    if (type.id == 394) {
        // row.countryCode2 = srcRow.countryCodeNumeric2 // Справочник
    }

    // Графа 37
    if (type.id == 393 || type.id == 394) {
        // row.region2 = srcRow.region2 // Справочник
    }

    // Графа 38
    if (type.id == 393 || type.id == 394) {
        // row.city2 = srcRow.city2 // Справочник
    }

    // Графа 39
    if (type.id == 393) {
        // row.locality2 = srcRow.settlement2 // Справочник
    }

    if (type.id == 394) {
        // row.locality2 = srcRow.locality2 // Справочник
    }

    // Графа 40
    if (type.id == 393) {
        // row.deliveryCode = srcRow.conditionCode // Справочник
    }

    if (type.id == 394) {
        // row.deliveryCode = srcRow.deliveryCode // Справочник
    }

    // Графа 41
    if (type.id == 381 || type.id == 385) {
        // row.okeiCode = srcRow.okeiCode // Справочник
    }

    // Графа 42
    if ([376, 377, 381, 385, 387, 389, 393, 394].contains(type.id)) {
        row.count = srcRow.count
    }

    // Графа 43
    switch (type.id) {
        case 376:
        case 377:
        case 375:
        case 379:
        case 380:
        case 381:
        case 382:
        case 387:
            row.price = srcRow.price
            break
        case 383:
            row.price = srcRow.priceFirstCurrency
            break
        case 384:
            row.price = srcRow.priceOne
            break
        case 385:
        case 386:
        case 388:
        case 389:
        case 394:
            row.price = srcRow.price
            break
    }

    // Графа 44
    switch (type.id) {
        case 376:
        case 377:
        case 375:
        case 379:
        case 380:
        case 381:
        case 382:
        case 387:
            row.total = srcRow.cost
            break
        case 385:
            row.total = srcRow.totalCost
            break
        case 386:
        case 388:
        case 389:
        case 394:
            row.total = srcRow.total
            break
        case 390:
        case 391:
            row.total = srcRow.total
            break
        case 392:
            row.total = srcRow.cost
            break
        case 393:
            row.total = srcRow.totalNds
            break
    }

    // Графа 45
    switch (type.id) {
        case 376:
        case 377:
        case 382:
        case 383:
        case 385:
        case 392:
        case 393:
            row.dealDoneDate = srcRow.transactionDate
            break
        case 375:
        case 379:
        case 381:
        case 387:
            row.dealDoneDate = srcRow.dealDate
            break
        case 380:
            row.dealDoneDate = srcRow.date
            break
        case 386:
        case 388:
        case 389:
        case 390:
        case 391:
        case 394:
            row.dealDoneDate = srcRow.dealDoneDate
            break
        case 384:
            row.dealDoneDate = srcRow.transactionDeliveryDate
            break
    }

    // Графа 47
    row.dealMemberNum = row.otherNum

    // Графа 48
    // row.organInfo = из 50 // Справочное // TODO Перенести поcле 50 и заполнить

    // Графа 49
    switch (type.id) {
        case 376:
        case 377:
            // row.countryCode3 = srcRow.country // Справочное
            break
        case 375:
        case 379:
        case 380:
        case 381:
        case 382:
            // row.countryCode3 = srcRow.countryCode // Справочное
            break
        case 383:
        case 389:
        case 390:
        case 391:
        case 392:
        case 394:
            // row.countryCode3 = srcRow.countryCode // Справочное
            break
        case 393:
            // row.countryCode3 = srcRow.countryCode1 // Справочное
            break
        case 384:
            // row.countryCode3 = srcRow.contraCountryCode // Справочное
            break
        case 385:
            // row.countryCode3 = srcRow.country // Справочное
            break
        case 386:
            // row.countryCode3 = srcRow.countryCode // Справочное
            break
        case 388:
            // row.countryCode3 = srcRow.countryName // Справочное
            break
        case 387:
            // row.countryCode3 = srcRow.countryName // Справочное
            break
    }

    // Графа 50
    switch (type.id) {
        case 376:
        case 377:
        case 382:
        case 383:
            // row.organName = srcRow.jurName // Справочное
            break
        case 375:
        case 379:
        case 380:
        case 381:
        case 387:
            // row.organName = srcRow.fullNamePerson // Справочное
            break
        case 384:
            // row.organName = srcRow.contraName // Справочное
            break
        case 385:
        case 392:
        case 393:
            // row.organName = srcRow.name // Справочное
            break
        case 386:
        case 388:
        case 389:
        case 390:
        case 391:
        case 394:
            // row.organName = srcRow.fullName // Справочное
            break
    }

    // Графа 51
    // row.organINN = // Справочное TODO из графы 50

    // Графа 52
    // row.organKPP = // Справочное TODO из графы 50

    // Графа 53
    // row.organRegNum = // Справочное TODO из графы 50

    // Графа 54
    // row.taxpayerCode = // Справочное TODO из графы 50

    // Графа 55
    // row.address = // Справочное TODO из графы 50
}