package form_template.deal.forward_contracts

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Поставочные срочные сделки, базисным активом которых является иностранная валюта
 *
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkUniq()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        sort()
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
    ['fullName', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealType',
            'currencyCode', 'countryDealCode', 'incomeSum', 'outcomeSum', 'dealDoneDate'].each {
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
        def dealDateCell = row.getCell('dealDate')
        def docDateCell = row.getCell('docDate')
        [
                'fullName',         // Полное наименование с указанием ОПФ
                'inn',              // ИНН/КИО
                'countryName',      // Наименование страны регистрации
                'countryCode',      // Код страны по классификатору ОКСМ
                'docNumber',        // Номер договора
                'docDate',          // Дата договора
                'dealNumber',       // Номер сделки
                'dealDate',         // Дата заключения сделки
                'dealType',         // Вид срочной сделки
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
        // Проверка доходов и расходов
        def incomeSumCell = row.getCell('incomeSum')
        def outcomeSumCell = row.getCell('outcomeSum')
        def msgIn = incomeSumCell.column.name
        def msgOut = outcomeSumCell.column.name
        if (incomeSumCell.value != null && outcomeSumCell.value != null) {
            logger.error("«$msgIn» и «$msgOut» в строке $rowNum не могут быть одновременно заполнены!")
        }
        if (incomeSumCell.value == null && outcomeSumCell.value == null) {
            logger.warn("Одна из граф «$msgIn» и «$msgOut» в строке $rowNum должна быть заполнена!")
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
        // Корректность даты совершения сделки
        def dealDoneDateCell = row.getCell('dealDoneDate')
        if (dealDoneDateCell.value < dealDateCell.value) {
            def msg1 = dealDoneDateCell.column.name
            def msg2 = dealDateCell.column.name
            logger.warn("«$msg1» не может быть меньше «$msg2» в строке $rowNum!")
        }
        checkNSI(row)
    }
}

/**
 * Проверка соответствия НСИ
 */
void checkNSI(DataRow<Cell> row) {
    def rowNum = row.getIndex()
    def String msg = "В справочнике %s не найден элемент%s, указанный в строке $rowNum!"
    if (row.fullName != null && refBookService.getRecordData(9, row.fullName) == null) {
        logger.warn(String.format(msg, "«Организации-участники контролируемых сделок»", ""))
    }
    if (row.countryName != null && refBookService.getRecordData(10, row.countryName) == null) {
        logger.warn(String.format(msg, "ОКСМ", " «Наименование страны регистрации»"))
    }
    if (row.countryCode != null && refBookService.getRecordData(10, row.countryCode) == null) {
        logger.warn(String.format(msg, "ОКСМ", " «Код страны по классификатору ОКСМ»"))
    }
    if (row.countryDealCode != null && refBookService.getRecordData(10, row.countryDealCode) == null) {
        logger.warn(String.format(msg, "ОКСМ", " «Код страны происхождения предмета сделки по классификатору ОКСМ»"))
    }
    if (row.currencyCode != null && refBookService.getRecordData(15, row.currencyCode) == null) {
        logger.warn(String.format(msg, "Единый справочник валют", " «Код валюты расчетов по сделке»"))
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

        // Расчет полей зависимых от справочников
        if (row.fullName != null) {
            def map = refBookService.getRecordData(9, row.fullName)
            row.inn = map.INN_KIO.numberValue
            row.countryName = map.COUNTRY.referenceValue
            row.countryCode = map.COUNTRY.referenceValue
        } else {
            row.inn = null
            row.countryName = null
            row.countryCode = null
        }
    }
}

/**
 * Расчет подитогового значения
 * @param i
 * @return
 */
def calcItog(int i) {
    def newRow = formData.createDataRow()

    newRow.fullName = 'Подитог:'

    newRow.setAlias('itg#'.concat(i.toString()))
    newRow.getCell('fullName').colSpan = 11

    // Расчеты подитоговых значений
    def BigDecimal incomeSumItg = 0, outcomeSumItg = 0, totalItg = 0
    for (int j = i; j >= 0 && formData.dataRows.get(j).getAlias() == null; j--) {
        def row = formData.dataRows.get(j)

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

    newRow
}

/**
 * Сортировка строк
 */
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    dataRows.sort({ DataRow a, DataRow b ->
        sortRow(['fullName', 'inn', 'docNumber', 'docDate', 'dealType'], a, b)
    })

    dataRowHelper.save(dataRows);
}

int sortRow(List<String> params, DataRow a, DataRow b) {
    for (String param : params) {
        def aD = a.getCell(param).value
        def bD = b.getCell(param).value

        if (aD != bD) {
            return aD <=> bD
        }
    }
    return 0
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

        for (int i = 0; i < dataRows.size(); i++) {
            def row = dataRows.get(i)
            def nextRow = null

            if (i < dataRows.size() - 1) {
                nextRow = dataRows.get(i + 1)
            }

            if (row.getAlias() == null && nextRow == null
                    || row.fullName != nextRow.fullName
                    || row.inn != nextRow.inn
                    || row.docNumber != nextRow.docNumber
                    || row.docDate != nextRow.docDate
                    || row.dealType != nextRow.dealType) {
                def itogRow = calcItog(i)
                dataRows.add(i + 1, itogRow)
                dataRowHelper.insert(itogRow, i + 1)

                i++
            }
        }
        dataRowHelper.save(dataRows);
    }
}
