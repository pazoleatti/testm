package form_template.deal.foreign_currency

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

/**
 * 390 - Купля-продажа иностранной валюты
 * (похож на nondeliverable " Беспоставочные срочные сделки")
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
}

void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? currentDataRow.getIndex() : (size == 0 ? 1 : size)
    ['fullName', 'docNum', 'docDate', 'dealNumber', 'dealDate', 'currencyCode',
            'countryDealCode', 'incomeSum', 'outcomeSum', 'dealDoneDate'].each {
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
        [
                'rowNumber',        // № п/п
                'fullName',         // Полное наименование с указанием ОПФ
                'inn',              // ИНН/КИО
                'countryName',      // Наименование страны регистрации
                'countryCode',      // Код страны по классификатору ОКСМ
                'docNum',           // Номер договора
                'docDate',          // Дата договора
                'dealNumber',       // Номер сделки
                'dealDate',         // Дата заключения сделки
                'currencyCode',     // Код валюты расчетов по сделке
                'countryDealCode',  // Код страны происхождения предмета сделки по классификатору ОКСМ
                'price',            // Цена (тариф) за единицу измерения, руб.
                'total',            // Итого стоимость, руб.
                'dealDoneDate'      // Дата совершения сделки
        ].each {
            def rowCell = row.getCell(it)
            if (rowCell.value == null || rowCell.value.toString().isEmpty()) {
                def msg = rowCell.column.name
                logger.warn("Графа «$msg» в строке $rowNum не заполнена!")
            }
        }
        // Проверка заполнения доходов и расходов Банка
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
        //  Корректность даты договора
        def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod

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
        def dealDateCell = row.getCell('dealDate')
        if (docDateCell.value > dealDateCell.value) {
            def msg1 = dealDateCell.column.name
            def msg2 = docDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        // Проверка заполнения стоимости сделки
        if (row.total != row.price) {
            def msg1 = row.getCell('price').column.name
            def msg2 = row.getCell('total').column.name
            logger.warn("«$msg1» не может отличаться от «$msg2» в строке $rowNum!")
        }
        // Корректность дат сделки  dealDate - 9гр, dealDoneDate - 16гр
        def dealDoneDate = row.getCell('dealDoneDate')
        if (dealDateCell.value > dealDoneDate.value) {
            def msg1 = dealDoneDate.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        //Проверки соответствия НСИ
        checkNSI(row, "fullName", "Организации-участники контролируемых сделок",9)
        checkNSI(row, "countryName", "ОКСМ",10)
        checkNSI(row, "countryCode", "ОКСМ",10)
        checkNSI(row, "countryDealCode", "ОКСМ",10)
        checkNSI(row, "currencyCode", "Единый справочник валют",15)
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
        if (row.getAlias() != null) {
            continue
        }
        // Порядковый номер строки
        row.rowNumber = row.getIndex()
        // Расчет поля "Цена"
        row.price = row.incomeSum != null ? row.incomeSum : row.outcomeSum
        // Расчет поля "Итого"
        row.total = row.price

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
        def row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            dataRowHelper.delete(row)
            break
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

        newRow.setAlias('itg')
        newRow.itog = 'Подитог:'
        newRow.getCell('itog').colSpan = 11
        newRow.rowNumber = dataRows.size()+1

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

       // dataRows.add(dataRows.size(), newRow)
        dataRowHelper.insert(newRow, dataRows.size()+1)
    }
}