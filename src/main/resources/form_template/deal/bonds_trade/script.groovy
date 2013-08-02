package form_template.deal.bonds_trade

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import java.math.RoundingMode

/**
 * Реализация и приобретение ценных бумаг
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
    ['transactionDeliveryDate', 'contraName', 'transactionMode', 'transactionSumCurrency', 'currency',
            'courseCB', 'transactionSumRub', 'contractNum', 'contractDate', 'transactionDate', 'bondRegCode',
            'bondCount', 'transactionType'].each {
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
    // Отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // Налоговый период
    def taxPeriod = taxPeriodService.get(reportPeriod.taxPeriodId)

    def dFrom = taxPeriod.getStartDate()
    def dTo = taxPeriod.getEndDate()

    for (row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue;
        }

        def rowNum = row.getIndex()

        [
                'rowNum', // № п/п
                'transactionDeliveryDate', // Дата сделки (поставки)
                'contraName', // Наименование контрагента и ОПФ
                'transactionMode', // Режим переговорных сделок
                'innKio', // ИНН/КИО контрагента
                'contraCountry', // Страна местонахождения контрагента
                'contraCountryCode', // Код страны местонахождения контрагента
                'transactionSumCurrency', // Сумма сделки (с учетом НКД), в валюте расчетов
                'currency', // Валюта расчетов по сделке
                'courseCB', // Курс ЦБ РФ
                'transactionSumRub', // Сумма сделки (с учетом НКД), руб.
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'transactionDate', // Дата заключения сделки
                'bondRegCode', // Регистрационный код ценной бумаги
                'bondCount', // Количество бумаг по сделке, шт.
                'priceOne', // Цена за 1 шт., руб.
                'transactionType' // Тип сделки
        ].each {
            if (row.getCell(it).value == null || row.getCell(it).value.toString().isEmpty()) {
                def msg = row.getCell(it).column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }

        def transactionDeliveryDate = row.transactionDeliveryDate
        def transactionDate = row.transactionDate
        def transactionSumRub = row.transactionSumRub
        def bondCount = row.bondCount
        def priceOne = row.priceOne
        def courseCB = row.courseCB
        def transactionSumCurrency = row.transactionSumCurrency
        def contractDate = row.contractDate

        // Корректность даты сделки
        if (transactionDeliveryDate < transactionDate) {
            def msg1 = row.getCell('transactionDeliveryDate').column.name
            def msg2 = row.getCell('transactionDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        // Проверка конверсии
        if (courseCB == null || transactionSumCurrency == null || transactionSumRub != (courseCB * transactionSumCurrency).setScale(0, RoundingMode.HALF_UP)) {
            def msg1 = row.getCell('transactionSumRub').column.name
            def msg2 = row.getCell('courseCB').column.name
            def msg3 = row.getCell('transactionSumCurrency').column.name
            logger.warn("«$msg1» не соответствует «$msg2» с учетом данных «$msg3» в строке $rowNum!")
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
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        // Проверка цены сделки
        def res = null

        if (transactionSumRub != null && bondCount != null) {
            res = (transactionSumRub / bondCount).setScale(0, RoundingMode.HALF_UP)
        }

        if (transactionSumRub != null || bondCount == null || priceOne != res) {
            def msg1 = row.getCell('priceOne').column.name
            def msg2 = row.getCell('transactionSumRub').column.name
            def msg3 = row.getCell('bondCount').column.name
            logger.warn("«$msg1» не равно отношению «$msg2» и «$msg3» в строке $rowNum!")
        }

        //Проверки соответствия НСИ
        checkNSI(row, "contraName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "contraCountry", "ОКСМ", 10)
        checkNSI(row, "contraCountryCode", "ОКСМ", 10)
        checkNSI(row, "transactionMode", "Режим переговорных сделок", 14)
        checkNSI(row, "transactionType", "Типы сделок", 16)
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
        // Расчет поля "Цена за 1 шт., руб."
        transactionSumRub = row.transactionSumRub
        bondCount = row.bondCount

        if (transactionSumRub != null && bondCount != null && bondCount != 0) {
            row.priceOne = transactionSumRub / bondCount;
        }

        // Расчет полей зависимых от справочников
        if (row.contraName != null) {
            def map = refBookService.getRecordData(9, row.contraName)
            row.innKio = map.INN_KIO.numberValue
            row.contraCountry = map.COUNTRY.referenceValue
            row.contraCountryCode = map.COUNTRY.referenceValue
        } else {
            row.innKio = null
            row.contraCountry = null
            row.contraCountryCode = null
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