package form_template.income.rnu45
/**
 * Скрипт для РНУ-45
 * Форма "(РНУ-45) Регистр налогового учёта «ведомость начисленной амортизации по нематериальным активам»"
 * formTemplateId=341
 *
 * @author akadyrgulov
 * @author Stanislav Yasinskiy
 */
import com.aplana.sbrf.taxaccounting.model.DataRow
import java.math.RoundingMode

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        logicalCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicalCheck()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        break
// обобщить
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicalCheck()
        break
}

// графа 1	- rowNumber
// графа 2	- inventoryNumber
// графа 3	- name
// графа 4	- buyDate
// графа 5	- usefulLife
// графа 6	- expirationDate
// графа 7	- startCost
// графа 8	- depreciationRate
// графа 9	- amortizationMonth
// графа 10	- amortizationSinceYear
// графа 11	- amortizationSinceUsed

// графы 2..7
def getEditColumns() {
    ['inventoryNumber', 'name', 'buyDate', 'usefulLife',
            'expirationDate', 'startCost']
}

//Добавить новую строку
void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    getEditColumns().each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

// Удалить строку
def deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

// Ресчет графы 8
def calc8(def row) {
    if (row.usefulLife == null || row.usefulLife == 0) {
        return null
    }
    return round(((1 / row.usefulLife) * 100), 4)

}

// Ресчет графы 9
def calc9(def row) {
    if (row.startCost == null || row.depreciationRate == null) {
        return null
    }
    return round(row.startCost * row.depreciationRate)
}

// Ресчет графы 10
def calc10(def row, def reportDateStart, def reportDate, def oldRow10) {
    Calendar buyDate = calc10and11(row)
    if (buyDate != null && reportDateStart != null && reportDate != null)
        return row.amortizationMonth + ((buyDate.get(Calendar.MONTH) == Calendar.JANUARY || (buyDate.after(reportDateStart) && buyDate.before(reportDate))) ? 0 : ((oldRow10 == null) ? 0 : oldRow10))
    return null
}

// Ресчет графы 11
def calc11(def row, def reportDateStart, def reportDate, def oldRow11) {
    Calendar buyDate = calc10and11(row)
    if (buyDate != null && reportDateStart != null && reportDate != null)
        return row.amortizationMonth + ((buyDate.after(reportDateStart) && buyDate.before(reportDate)) ? 0 : ((oldRow11 == null) ? 0 : oldRow11))
    return null
}

// Общая часть ресчета граф 10 и 11
Calendar calc10and11(def row) {
    if (row.buyDate == null) {
        return null
    }
    Calendar buyDate = Calendar.getInstance()
    buyDate.setTime(row.buyDate)
    return buyDate
}

// Расчеты. Алгоритмы заполнения полей формы.
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить строку "итого"
    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
        if (isTotal(row)) {
            iter.remove()
            dataRowHelper.delete(row)
        }
    }

    if (dataRows.isEmpty()) {
        return
    }

    // отсортировать
    // data.save(getRows(data).sort { (it.inventoryNumber) })

    // Отчетная дата
    def reportDate = getReportDate()
    //Начальная дата отчетного периода
    def tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def reportDateStart = (tmp ? tmp.getTime() : null)

    // графа 7, 9, 10, 11 для последней строки "итого"
    def total7 = 0
    def total9 = 0
    def total10 = 0
    def total11 = 0

    def formDataOld = getFormDataOld()
    def dataOld = formDataOld != null ? formDataService.getDataRowHelper(formDataOld) : null
    def index = 0

    for (def row in dataRows) {

        // графа 1
        row.rowNumber = ++index

        // графа 8
        row.depreciationRate = calc8(row)

        // графа 9
        row.amortizationMonth = calc9(row)

        // для граф 10 и 11
        prevValues = getPrev10and11(dataOld, row)

        // графа 10
        row.amortizationSinceYear = calc10(row, reportDateStart, reportDate, prevValues[0])

        // графа 11
        row.amortizationSinceUsed = calc11(row, reportDateStart, reportDate, prevValues[1])

        // графа 7, 9, 10, 11 для последней строки "итого"
        if (row.startCost != null)
            total7 += row.startCost
        if (row.amortizationMonth != null)
            total9 += row.amortizationMonth
        if (row.amortizationSinceYear != null)
            total10 += row.amortizationSinceYear
        if (row.amortizationSinceUsed != null)
            total11 += row.amortizationSinceUsed
    }
    dataRowHelper.update(dataRows);

    // добавить строки "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 6
    setTotalStyle(totalRow)
    totalRow.startCost = total7
    totalRow.amortizationMonth = total9
    totalRow.amortizationSinceYear = total10
    totalRow.amortizationSinceUsed = total11

    dataRowHelper.insert(totalRow, index + 1)
}

def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // 1. Проверка на заполнение полей 1..11
        def requiredColumns = ['rowNumber', 'inventoryNumber',
                'name', 'buyDate', 'usefulLife',
                'expirationDate', 'startCost',
                'depreciationRate', 'amortizationMonth',
                'amortizationSinceYear', 'amortizationSinceUsed']
        for (def row in dataRows) {
            if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
                return false
            }
        }

        // суммы строки общих итогов
        def totalSums = [:]
        // столбцы для которых надо вычислять итого. Графа 7, 9, 10, 11
        def totalColumns = ['startCost', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']
        // итоговая строка
        def totalRow = null
        // Инвентарные номера
        def List<String> invList = new ArrayList<String>()
        def formDataOld = getFormDataOld()
        def dataOld = formDataOld != null ? formDataService.getDataRowHelper(formDataOld) : null
        // Отчетная дата
        def reportDate = getReportDate()
        //Начальная дата отчетного периода
        def tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
        def reportDateStart = (tmp ? tmp.getTime() : null)

        for (def row in dataRows) {
            if (isTotal(row)) {
                totalRow = row
                continue
            }

            // 2. Проверка на уникальность поля «инвентарный номер»
            if (invList.contains(row.inventoryNumber)) {
                logger.error("Инвентарный номер не уникальный!")
                return false
            } else {
                invList.add(row.inventoryNumber)
            }

            // 3. Проверка на нулевые значения
            if (row.startCost == 0 && row.amortizationMonth == 0 && row.amortizationSinceYear == 0 && row.amortizationSinceUsed == 0) {
                logger.error("Все суммы по операции нулевые!")
                return false
            }

            // 4. Арифметические проверки расчета неитоговых граф
            // графа 8
            if (row.depreciationRate != calc8(row)) {
                logger.error("Неверно рассчитана графа «Первоначальная стоимость (руб.)»!")
                return false
            }
            // графа 9
            if (row.amortizationMonth != calc9(row)) {
                logger.error("Неверно рассчитана графа «Сумма начисленной амортизации за месяц (руб.)»!")
                return false
            }
            prevValues = getPrev10and11(dataOld, row)
            // графа 10
            if (row.amortizationSinceYear != calc10(row, reportDateStart, reportDate, prevValues[0])) {
                logger.error("Неверно рассчитана графа «Сумма начисленной амортизации с начала года (руб.)»!")
                return false
            }
            // графа 11
            if (row.amortizationSinceUsed != calc11(row, reportDateStart, reportDate, prevValues[1])) {
                logger.error("Неверно рассчитана графа «Сумма начисленной амортизации с даты ввода в эксплуатацию (руб.)»!")
                return false
            }

            // 5. Арифметические проверки расчета итоговой строки
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }
        }

        if (totalRow != null) {
            // 5. Арифметические проверки расчета итоговой строки
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    logger.error('Итоговые значения рассчитаны неверно!')
                    return false
                }
            }
        }
    }
    return true
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    // TODO (Aydar Kadyrgulov) пока не реализован выбор месяца.
    // но в будущем у новой формы будет заполнено поле periodOrder (месяц)
    // [16:53:24] Sariya Mustafina: в справочник отчетных периодов, вроде, должны добавить месяцы с какой-то хитрой привязкой к кварталам

    if (findForm != null && findForm.periodOrder == formData.periodOrder) {
        logger.info('periodOrder = ' + findForm.periodOrder)
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

// Проверка является ли строка итоговой
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

// Установить стиль для итоговых строк
void setTotalStyle(def row) {
    ['rowNumber', 'fix', 'inventoryNumber', 'name',
            'buyDate', 'usefulLife', 'expirationDate',
            'startCost', 'depreciationRate', 'amortizationMonth',
            'amortizationSinceYear', 'amortizationSinceUsed'
    ].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    def cell
    columns.each {
        cell = row.getCell(it)
        if (cell.getValue() == null || row.getCell(it).getValue() == '') {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.rowNumber
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

//Получить номер строки в таблице
def getIndex(def row) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().indexOf(row)
}

def round(def value, def int precision = 2) {
    if (value == null) {
        return null
    }
    return value.setScale(precision, RoundingMode.HALF_UP)
}

// Получить данные за предыдущий месяц
def getFormDataOld() {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-55 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

// Получить значение за предыдущий отчетный период для графы 10 и 11
def getPrev10and11(def dataOld, def row) {
    if (dataOld != null)
        for (def rowOld : dataOld.getAllCached()) {
            if (rowOld.inventoryNumber == row.inventoryNumber) {
                return [rowOld.amortizationSinceYear, rowOld.amortizationSinceUsed]
            }
        }
    return [null, null]
}

//Получить отчетную дату
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}

// Консолидация
void consolidation() {
    // удалить все строки и собрать из источников их строки
    def rows = new LinkedList<DataRow<Cell>>()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).getAllCached().each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        rows.add(row)
                    }
                }
            }
        }
    }
    formDataService.getDataRowHelper(formData).save(rows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}