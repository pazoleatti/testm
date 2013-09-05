package form_template.deal.tech_service

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import java.math.RoundingMode

/**
 * 377 - Техническое обслуживание нежилых помещений
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
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

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
    ['jurName', 'bankSum', 'contractNum', 'contractDate', 'country', 'region', 'city', 'settlement', 'count', 'price', 'transactionDate'].each {
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
                'jurName', // Полное наименование юридического лица с указанием ОПФ
                'innKio', // ИНН/КИО
                'countryCode', // Код страны по классификатору ОКСМ
                'bankSum', // Сумма расходов Банка, руб.
                'contractNum', // Номер договора
                'contractDate', // Дата договора
                'country', // Адрес местонахождения объекта недвижимости (Страна)
                'price', // Цена
                'cost', // Стоимость
                'transactionDate' // Дата совершения сделки
        ].each {
            if (row.getCell(it).value == null || row.getCell(it).value.toString().isEmpty()) {
                def msg = row.getCell(it).column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }

        // Проверка стоимости
        def cost = row.cost
        def price = row.price
        def count = row.count
        def bankSum = row.bankSum
        def transactionDate = row.transactionDate
        def contractDate = row.contractDate

        if (price == null || count == null && cost != price * count) {
            def msg1 = row.getCell('cost').column.name
            def msg2 = row.getCell('price').column.name
            def msg3 = row.getCell('count').column.name
            logger.warn("«$msg1» не равна произведению «$msg2» и «$msg3» в строке $rowNum!")
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

        // Корректность даты совершения сделки
        if (transactionDate < contractDate) {
            def msg1 = row.getCell('transactionDate').column.name
            def msg2 = row.getCell('contractDate').column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }

        // Проверка цены сделки
        if (count != null) {
            def res = null

            if (bankSum != null && count != null) {
                res = (bankSum / count).setScale(0, RoundingMode.HALF_UP)
            }

            if (bankSum == null || count == null || price != res) {
                def msg1 = row.getCell('price').column.name
                def msg2 = row.getCell('bankSum').column.name
                def msg3 = row.getCell('count').column.name
                logger.warn("«$msg1» не равно отношению «$msg2» и «$msg3» в строке $rowNum!")
            }
        } else {
            if (price != bankSum) {
                def msg1 = row.getCell('price').column.name
                def msg2 = row.getCell('bankSum').column.name
                logger.warn("«$msg1» не равно «$msg2» в строке $rowNum!")
            }
        }

        // Проверка расходов
        if (cost != bankSum) {
            def msg1 = row.getCell('cost').column.name
            def msg2 = row.getCell('bankSum').column.name
            logger.warn("«$msg1» не равно «$msg2» в строке $rowNum!")
        }

        // Проверка заполнения региона
        def country = row.country
        if (country != null) {
            def msg1 = row.getCell('region').column.name
            def msg2 = row.getCell('country').column.name
            if (country == 643 && row.region == null) {
                logger.warn("«$msg1» в строке $rowNum должен быть заполнен, т.к. в «$msg2» указан код 643!")
            } else if (country != 643 && row.region != null) {
                logger.warn("«$msg1» в строке $rowNum не должен быть заполнен, т.к. в «$msg2» указан код, отличный от 643!")
            }
        }
        // Проверка населенного пункта
        if (row.city != null && row.city.toString().isEmpty() && row.settlement != null && row.city.settlement().isEmpty()) {
            def msg1 = row.getCell('city').column.name
            def msg2 = row.getCell('settlement').column.name
            logger.warn("Если указан «$msg1» в строке $rowNum, не должен быть указан «$msg2» в строке $rowNum!")
        }

        //Проверки соответствия НСИ
        checkNSI(row, "jurName", "Организации-участники контролируемых сделок", 9)
        checkNSI(row, "countryCode", "ОКСМ", 10)
        checkNSI(row, "country", "ОКСМ", 10)
        checkNSI(row, "region", "Коды субъектов Российской Федерации", 4)
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

        // Расчет поля "Населенный пункт"
        if (row.city != null && !row.city.toString().isEmpty()) {
            row.settlement = row.city
        }

        count = row.count
        bankSum = row.bankSum
        // Расчет поля "Цена"
        row.price = count == null ? bankSum : bankSum / count
        // Расчет поля "Стоимость"
        row.cost = bankSum

        // Расчет полей зависимых от справочников
        if (row.jurName != null) {
            def map = refBookService.getRecordData(9, row.jurName)
            row.innKio = map.INN_KIO.numberValue
            row.countryCode = map.COUNTRY.referenceValue
        } else {
            row.innKio = null
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