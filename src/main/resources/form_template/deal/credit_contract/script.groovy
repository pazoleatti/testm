package form_template.deal.credit_contract

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Уступка прав требования по кредитным договорам
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
    dataRowHelper.insert(row, index)
    dataRows.add(row)
    ['name', 'contractNum', 'contractDate', 'okeiCode', 'price', 'transactionDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    // Отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // Налоговый период
    def taxPeriod = taxPeriodService.get(reportPeriod.taxPeriodId)

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
                'country', // Страна регистрации
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'okeiCode', // Код единицы измерения по ОКЕИ
                'count', // Количество
                'price', // Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
                'totalCost', // Итого стоимость без учета НДС, акцизов и пошлин, руб.
                'transactionDate' // Дата совершения сделки
        ].each {
            if (row.getCell(it).value == null || row.getCell(it).value.toString().isEmpty()) {
                def msg = row.getCell(it).column.name
                logger.error("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }

        def transactionDate = row.transactionDate
        def contractDate = row.contractDate
        def totalCost = row.totalCost
        def price = row.price

        // Проверка выбранной единицы измерения
        // TODO поле справочника "код"
        // logger.error('В поле «Код единицы измерения по ОКЕИ» могут быть указаны только следующие элементы: шт.!')

        // Проверка количества
        if (row.count != 1) {
            def msg = row.getCell('transactionDate').column.name
            logger.error("В графе «$msg» может быть указано только значение «1» в строке $rowNum!")
        }

        // Корректность даты договора
        def dt = contractDate
        if (dt != null && (dt < dFrom || dt > dTo)) {
            def msg = row.getCell('contractDate').column.name

            if (dt > dTo) {
                logger.error("«$msg» не может быть больше даты окончания отчётного периода в строке $rowNum!")
            }

            if (dt < dFrom) {
                logger.error("«$msg» не может быть меньше даты начала отчётного периода в строке $rowNum!")
            }
        }

        // Корректность даты совершения сделки
        if (transactionDate < contractDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            logger.error("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        // Проверка заполнения стоимости сделки
        if (totalCost != price) {
            def msg1 = row.getCell('totalCost').column.name
            def msg2 = row.getCell('price').column.name
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
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (row in dataRows) {
        // Порядковый номер строки
        row.rowNum = row.getIndex()
        // Количество
        row.count = 1
        // Итого стоимость без учета НДС, акцизов и пошлин, руб.
        row.totalCost = row.price
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