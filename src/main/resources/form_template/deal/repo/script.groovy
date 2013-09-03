package form_template.deal.repo

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * 383 - Сделки РЕПО
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
    def index = currentDataRow != null ? currentDataRow.getIndex() : (size == 0 ? 1 : size)
    ['jurName', 'contractNum', 'contractDate', 'transactionNum', 'transactionDeliveryDate', 'dealsMode',
            'date1', 'date2', 'percentIncomeSum', 'percentConsumptionSum', 'priceFirstCurrency', 'currencyCode',
            'courseCB', 'priceFirstRub', 'transactionDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
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
    def taxPeriod = taxPeriodService.get(reportPeriodService.get(formData.reportPeriodId).taxPeriodId)

    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()

    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }

        def rowNum = row.getIndex()
        [
                'rowNum', // № п/п
                'jurName', // Полное наименование юридического лица с указанием ОПФ
                'innKio', // ИНН/КИО
                'country', // Наименование страны регистрации
                'countryCode', // Код страны по классификатору ОКСМ
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'transactionNum', // Номер сделки
                'transactionDeliveryDate', // Дата заключения сделки
                'dealsMode', // Режим переговорных сделок
                'date1', // Дата исполнения 1-ой части сделки
                'date2', // Дата исполнения 2-ой части сделки
                'percentIncomeSum', // Сумма процентного дохода (руб.)
                'percentConsumptionSum', // Сумма процентного расхода (руб.)
                'priceFirstCurrency', // Цена 1-ой части сделки, ед. валюты
                'currencyCode', // Код валюты расчетов по сделке
                'courseCB', // Курс ЦБ РФ
                'priceFirstRub', // Цена 1-ой части сделки, руб.
                'transactionDate' // Дата совершения сделки
        ].each {
            if (row.getCell(it).value == null || row.getCell(it).value.toString().isEmpty()) {
                def msg = row.getCell(it).column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }

        def contractDate = row.contractDate
        def transactionDate = row.transactionDate
        def transactionDeliveryDate = row.transactionDeliveryDate
        def percentIncomeSum = row.percentIncomeSum
        def percentConsumptionSum = row.percentConsumptionSum

        // Заполнение граф 13 и 14
        if (percentIncomeSum == null && percentConsumptionSum == null) {
            def msg1 = row.getCell('percentIncomeSum').column.name
            def msg2 = row.getCell('percentConsumptionSum').column.name
            logger.warn("Должна быть заполнена графа «$msg1» или графа «$msg2» в строке $rowNum!")
        }
        if (percentIncomeSum != null && percentConsumptionSum != null) {
            def msg1 = row.getCell('percentIncomeSum').column.name
            def msg2 = row.getCell('percentConsumptionSum').column.name
            logger.warn("Графа  «$msg1» и графа «$msg2» в строке $rowNum не могут быть заполнены одновременно!")
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

        // Корректность даты (заключения) сделки
        if (transactionDeliveryDate < contractDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        // Корректность даты исполнения 1–ой части сделки
        def dt1 = row.date1
        if (dt1 != null && (dt1 < dFrom || dt1 > dTo)) {
            def msg = row.getCell('date1').column.name

            if (dt1 > dTo) {
                logger.warn("«$msg» не может быть больше даты окончания отчётного периода в строке $rowNum!")
            }

            if (dt1 < dFrom) {
                logger.warn("«$msg» не может быть меньше даты начала отчётного периода в строке $rowNum!")
            }
        }

        // Корректность даты совершения сделки
        if (transactionDeliveryDate < transactionDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('transactionDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        //Проверки соответствия НСИ
        checkNSI(row, "jurName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "country", "ОКСМ", 10)
        checkNSI(row, "countryCode", "ОКСМ", 10)
        checkNSI(row, "dealsMode", "Режим переговорных сделок", 14)
        checkNSI(row, "currencyCode", "Единый справочник валют", 15)
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

    for (row in dataRows) {
        // Порядковый номер строки
        row.rowNum = row.getIndex()
        // Расчет полей зависимых от справочников
        if (row.jurName != null) {
            def map = refBookService.getRecordData(9, row.jurName)
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