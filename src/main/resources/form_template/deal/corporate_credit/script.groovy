package form_template.deal.corporate_credit

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Предоставление корпоративного кредита
 *
 * похож на  software_development (Разработка, внедрение, поддержка и модификация программного обеспечения, приобретение лицензий)
 * похож на  trademark (Предоставление права пользования товарным знаком)
 * @author Stanislav Yasinskiy
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
    ['fullNamePerson', 'sum', 'docNumber', 'docDate', 'dealDate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.save(dataRows)
}

/**
 * Проверяет уникальность в отчётном периоде и вид
 * (не был ли ранее сформирован отчет, параметры которого совпадают с параметрами, указанными пользователем )
 */
void checkCreation() {
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
        [
                'fullNamePerson',// Полное наименование юридического лица с указанием ОПФ
                'inn',           // ИНН/КИО
                'countryName',   // Страна регистрации
                'sum',           // Сумма доходов Банка, руб.
                'docNumber',     // Номер договора
                'docDate',       // Дата договора
                'count',         // Количество
                'price',         // Цена
                'cost',          // Стоимость
                'dealDate'       // Дата совершения сделки
        ].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }
        // Проверка количества
        if (row.count != 1) {
            def msg = row.getCell('count').column.name
            logger.warn("В графе «$msg» в строке $rowNum может  быть указано только  значение «1»!")
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
        // Проверка доходов
        def sumCell = row.getCell('sum')
        def priceCell = row.getCell('price')
        def costCell = row.getCell('cost')
        def msgSum = sumCell.column.name
        if (priceCell.value != sumCell.value) {
            def msg = priceCell.column.name
            logger.warn("«$msg» в строке $rowNum не может отличаться от «$msgSum»!")
        }
        if (costCell.value != sumCell.value) {
            def msg = costCell.column.name
            logger.warn("«$msg» в строке $rowNum не может отличаться от «$msgSum»!")
        }
        // Корректность даты совершения сделки
        def dealDateCell = row.getCell('dealDate')
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
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
        // В поле "Количество" подставляется значение «1»
        row.count = 1
        // Расчет поля "Цена"
        row.price = row.sum
        // Расчет поля "Стоимость"
        row.cost = row.sum
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