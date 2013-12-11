package form_template.income.rnu107

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * 502 - (РНУ-107) Регистр налогового учёта доходов, возникающих в связи с применением в сделках
 *                  с Взаимозависимыми лицами и резидентами оффшорных зон тарифов, не соответствующих рыночному уровню
 * formTemplateId=502
 *
 * @author Dmitriy Levykin
 * @author Stanislav Yasinskiy
 */
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
// После принятия из Утверждено
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
        logicalCheck()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicalCheck()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicalCheck()
        break
}

// графа 1  - number
// графа 2  - interdependence
// графа 3  - operationDate
// графа 4  - taxCode
// графа 5  - baseNumber
// графа 6  - baseDate
// графа 7  - operationSum
// графа 8  - rateService
// графа 9  - rateTax
// графа 10 - incomeSumFact
// графа 11 - incomeSumTax
// графа 12 - incomeSumBefore

// Проверки при создании формы
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    // проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId,
            formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

// Добавить новую строку
void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    ['interdependence', 'operationDate', 'taxCode', 'baseNumber', 'baseDate', 'operationSum', 'rateService', 'rateTax',
            'incomeSumTax', 'incomeSumBefore'].each {
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

// Ресчет графы 10
def calc10(def row, def Map<String, Long> map) {
    def val = 0
    if (row.rateService != null && row.rateService != 0 && row.taxCode != null && map != null) {
        taxCode = refBookService.getStringValue(28, row.taxCode, 'CODE')
        val = map.get(taxCode)
    }
    return val == null ? 0: val
}

// Ресчет графы 12
def calc12(def incomeSumTax, def incomeSumFact) {
    def val = null
    if (incomeSumTax != null && incomeSumFact != null)
        val = incomeSumTax - incomeSumFact
    return val
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

    // РНУ-4
    map = getRNU4()
    // индекс
    def index = 0
    for (def row in dataRows) {
        // графа 1
        row.number = ++index
        // графа 10
        row.incomeSumFact = calc10(row, map)
        // графа 12
        row.incomeSumBefore = calc12(row.incomeSumTax, row.incomeSumFact)
    }

    dataRows.sort { refBookService.getStringValue(9, it.interdependence, 'NAME') }
    dataRowHelper.update(dataRows);

    // Столбцы для которых надо вычислять итого и итого по коду. Графа 7, 10..12
    def totalColumns = ['operationSum', 'incomeSumFact', 'incomeSumTax', 'incomeSumBefore']

    // посчитать "итого по "
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }

    dataRows.eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.interdependence
        }
        // если поменялся то создать новую строку "итого по "
        if (tmp != row.interdependence) {
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по "
        if (i == dataRows.size() - 1) {
            totalColumns.each {
                def val = row.getCell(it).getValue()
                if(val!=null)
                    sums[it] += val
            }
            totalRows.put(i + 1, getNewRow(row.interdependence, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        totalColumns.each {
            def val = row.getCell(it).getValue()
            if(val!=null)
                sums[it] += val
        }
        tmp = row.interdependence
    }

    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { ind, row ->
        dataRowHelper.insert(row, ind + i + 1)
        i = i + 1
    }
}

// Получить новую строку
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    newRow.fix = 'Итого по ' + (refBookService.getStringValue(9, alias, 'NAME'))
    newRow.getCell('fix').colSpan = 6
    setTotalStyle(newRow)
    return newRow
}

// Логические проверки
def logicalCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    if (dataRows.isEmpty()) {
        return true
    }

    // список обязательных столбцов (графа 1..12)
    def requiredColumns = ['number', 'interdependence', 'operationDate', 'taxCode', 'baseNumber', 'baseDate', 'operationSum', 'rateService', 'rateTax',
            'incomeSumFact', 'incomeSumTax', 'incomeSumBefore']
    for (def row in dataRows) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    def tmp
    /** Дата начала отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def a = (tmp ? tmp.getTime() : null)
    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def b = (tmp ? tmp.getTime() : null)
    // РНУ-4
    map = getRNU4()
    // суммы строки общих итогов
    def totalSums = [:]
    // столбцы для которых надо вычислять итого и итого по коду классификации дохода. Графа 10, 11, 12
    def totalColumns = ['advancePayment', 'outcomeInNalog', 'outcomeInBuh']
    // признак наличия итоговых строк
    def hasTotal = false
    // список групп кодов классификации для которых надо будет посчитать суммы
    def totalGroupsName = []

    for (def row : dataRows) {
        if (isTotal(row)) {
            hasTotal = true
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 2. Проверка на уникальность поля «№ пп»
        for (def rowB : dataRows) {
            if (!row.equals(rowB) && row.number == rowB.number) {
                logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
                return false
            }
        }

        // 3. Проверка даты совершения операции и границ отчетного периода
        if (row.operationDate < a || b < row.operationDate) {
            logger.error(errorMsg + "Дата совершения операции вне границ отчётного периода!")
            return false
        }

        // 4. Арифметические проверки расчета граф 10 и 12
        if (check(row.getCell('incomeSumFact'), calc10(row, map)) ||
                check(row.getCell('incomeSumBefore'), calc12(row.incomeSumTax, row.incomeSumFact))) {
            return false
        }

        // 5. Арифметическая проверка итоговых значений граф 7, 10-12 строк
        totalColumns.each { alias ->
            if (totalSums[alias] == null) {
                totalSums[alias] = 0
            }
            totalSums[alias] += row.getCell(alias).getValue()
        }

        // Проверки соответствия НСИ.
        // Проверка актуальности поля «Код налогового учета»
        if (!checkNSI(row, "taxCode", "Классификатор доходов Сбербанка России для целей налогового учёта", 28)) {
            return false
        }
        // Проверка актуальности поля Наименование Взаимозависимого лица (резидента оффшорной зоны)
        if (!checkNSI(row, "interdependence", "Организации-участники контролируемых сделок", 9)) {
            return false
        }

        if (!totalGroupsName.contains(row.interdependence)) {
            totalGroupsName.add(row.interdependence)
        }
    }

    if (hasTotal) {
        // 5. Проверка итоговых значений по кодам классификации расхода
        for (def codeName : totalGroupsName) {
            def row = getRowByAlias(dataRowHelper, 'total' + codeName)
            for (def alias : totalColumns) {
                if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                    logger.error("Итоговые значения по группе $codeName рассчитаны неверно!")
                    return false
                }
            }
        }
    }
}

boolean check(def cell, def value) {
    if (cell.value != value) {
        logger.error("Неверно рассчитана графа «" + cell.column.name + "»!")
        return false
    }
    return true
}

// Консолидация
void consolidation() {
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
}

// Проверка является ли строка итоговой (любой итоговой)
def isTotal(row) {
    row.getAlias() ==~ /total\d*/
}

def getRNU4() {
    def Map<String, Long> map = new HashMap<String, Long>()
    def rnu4 = formDataService.find(316, formData.kind, formData.departmentId, formData.reportPeriodId)
    def data4 = rnu4 != null ? formDataService.getDataRowHelper(rnu4) : null
    if (data4 != null)
        for (def rowOld : data4.getAllCached()) {
            if (isTotal(rowOld)) {
                map.put(rowOld.fix.replace('Итого по КНУ ', ''), rowOld.sum)
            }
        }
    return map
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 */
def getRowByAlias(def dataRowHelper, def alias) {
    return dataRowHelper.getDataRow(dataRowHelper.getAllCached(), alias)
}

//Посчитать сумму указанного графа для строк с общей графой 2
def calcSumByCode(def interdependence, def alias) {
    def sum = 0
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().each { row ->
        if (!isTotal(row) && row.interdependence == interdependence) {
            sum += row.getCell(alias).getValue()
        }
    }
    return sum
}

// Установить стиль для итоговых строк.
void setTotalStyle(def row) {
    ['number', 'fix', 'interdependence', 'operationDate', 'taxCode', 'baseNumber', 'baseDate', 'operationSum', 'rateService', 'rateTax',
            'incomeSumFact', 'incomeSumTax', 'incomeSumBefore'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

// Проверить заполненость обязательных полей.
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.number
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

// Проверка пустое ли значение.
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

// Получить номер строки в таблице
def getIndex(def row) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().indexOf(row)
}
