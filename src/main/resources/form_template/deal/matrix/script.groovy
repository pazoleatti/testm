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
        if (source != null && source.state == WorkflowState.ACCEPTED  && source.getFormType().getTaxType() == TaxType.DEAL) {
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
    // TODO Подменить Id автозаменой
    // Графа 14
    switch (type.id) {
        case 1:
            row.taxpayerSideCode = '004'
            break
        case 2:
        case 3:
        case 5:
            row.taxpayerSideCode = '012'
            break
        case 4:
            row.taxpayerSideCode = '029'
            break
        case 6:
            if (srcRow.outcomeSum == null) {
                row.taxpayerSideCode = '027'
            }
            if (srcRow.incomeSum == null) {
                row.taxpayerSideCode = '026'
            }
            break
        case 7:
            row.taxpayerSideCode = '011'
            break
        case 8:
        case 16:
        case 17:
        case 18:
            row.taxpayerSideCode = '052'
            break
        case 9:
            // TODO значение справочника (S, B)
            // row.taxpayerSideCode = srcRow.transactionType
            break
        case 10:
        case 12:
        case 14:
            row.taxpayerSideCode = '022'
            break
        case 11:
        case 13:
            row.taxpayerSideCode = '005'
            break
        case 15:
            row.taxpayerSideCode = '030'
            break
        case 19:
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
        case 1:
            row.income = srcRow.incomeBankSum
            break
        case 7:
            row.income = srcRow.bankIncomeSum
            break
        case 4:
        case 12:
            row.income = srcRow.sum
            break
        case 2:
        case 3:
        case 5:
            row.income = 0
            break
        case 6:
            row.income = srcRow.cost
            break
        case 8:
            row.income = srcRow.priceFirstRub
            break
        case 9:
            row.income = srcRow.transactionSumRub
            break
        case 10:
            row.income = srcRow.totalCost
            break
        case 11:
        case 13:
        case 14:
            row.income = srcRow.total
            break
        case 15:
        case 16:
            row.income = srcRow.total
            break
        case 17:
            row.income = srcRow.cost
            break
        case 18:
            row.income = srcRow.totalNds
            break
        case 19:
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
        case 1:
            row.dealSubjectName = ''
            break
        case 2:
            row.dealSubjectName = ''
            break
        case 3:
            row.dealSubjectName = ''
            break
        case 4:
            row.dealSubjectName = ''
            break
        case 5:
            row.dealSubjectName = ''
            break
        case 6:
            row.dealSubjectName = ''
            break
        case 7:
            row.dealSubjectName = ''
            break
        case 8:
            row.dealSubjectName = ''
            break
        case 9:
            row.dealSubjectName = ''
            break
        case 10:
            row.dealSubjectName = ''
            break
        case 11:
            row.dealSubjectName = ''
            break
        case 12:
            row.dealSubjectName = ''
            break
        case 13:
            row.dealSubjectName = ''
            break
        case 14:
            row.dealSubjectName = ''
            break
        case 15:
            row.dealSubjectName = ''
            break
        case 16:
            row.dealSubjectName = ''
            break
        case 17:
            row.dealSubjectName = ''
            break
        case 18:
        case 19:
            row.dealSubjectName = ''
            break
    }
}

// 24.	row.dealSubjectName
// 25.	row.dealSubjectCode1
// 26.	row.dealSubjectCode2
// 27.	row.dealSubjectCode3
// 28.	row.otherNum
// 29.	row.contractNum
// 30.	row.contractDate
// 31.	row.countryCode
// 32.	row.countryCode1
// 33.	region1
// 34.	row.city1
// 35.	row.locality1
// 36.	row.countryCode2
// 37.	row.region2
// 38.	row.city2
// 39.	row.locality2
// 40.	row.deliveryCode
// 41.	row.okeiCode
// 42.	row.count
// 43.	row.price
// 44.	row.total
// 45.	row.dealDoneDate
// 46.	row.dealNum3
// 47.	row.dealMemberNum
// 48.	row.organInfo
// 49.	row.countryCode3
// 50.	row.organName
// 51.	row.organINN
// 52.	row.organKPP
// 53.	row.organRegNum
// 54.	row.taxpayerCode
// 55.	row.address