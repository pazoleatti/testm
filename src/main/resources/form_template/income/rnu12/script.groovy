package form_template.income.rnu12

import com.aplana.sbrf.taxaccounting.model.DataRow

/**
 * Скрипт для РНУ-12 (rnu12.groovy).
 * Форма "(РНУ-12) Регистр налогового учёта расходов по хозяйственным операциям и оказанным Банку услугам".
 * formTemplateId=364
 *
 * @author rtimerbaev
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

// графа 1  - rowNumber
// графа 2  - code
// графа 3  - numberFirstRecord
// графа 4  - opy
// графа 5  - operationDate
// графа 6  - name
// графа 7  - documentNumber
// графа 8  - date
// графа 9  - periodCounts
// графа 10 - advancePayment
// графа 11 - outcomeInNalog
// графа 12 - outcomeInBuh

//Добавить новую строку
void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    // графа 2..10, 12
    ['code', 'numberFirstRecord', 'opy', 'operationDate',
            'name', 'documentNumber', 'date', 'periodCounts',
            'advancePayment', 'outcomeInBuh'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

// Удалить строку
void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
boolean calc() {
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

    // графа 10, 12 для последней строки "итого"
    def total10 = 0
    def total11 = 0
    def total12 = 0

    dataRows.eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1
        // графа 11
        if (row.advancePayment != null && row.advancePayment > 0
                && row.advancePayment != null && row.periodCounts != null && row.periodCounts != 0) {
            row.outcomeInNalog = round(row.advancePayment / row.periodCounts)
        } else {
            row.outcomeInNalog = null
        }

        // графы для последней строки "итого"
        if (row.advancePayment != null)
            total10 += row.advancePayment
        if (row.outcomeInNalog != null)
            total11 += row.outcomeInNalog
        if (row.outcomeInBuh != null)
            total12 += row.outcomeInBuh
    }

    // отсортировать/группировать
    dataRows.sort { refBookService.getStringValue(27, it.code, 'CODE') }
    dataRowHelper.update(dataRows);

    /** Столбцы для которых надо вычислять итого и итого по коду. Графа 10, 11, 12. */
    def totalColumns = ['advancePayment', 'outcomeInNalog', 'outcomeInBuh']

    // посчитать "итого по коду"
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }

    dataRows.eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.code
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != row.code) {
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == dataRows.size() - 1) {
            totalColumns.each {
                def val = row.getCell(it).getValue()
                if(val!=null)
                    sums[it] += row.getCell(it).getValue()
            }
            totalRows.put(i + 1, getNewRow(row.code, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        totalColumns.each {
            def val = row.getCell(it).getValue()
            if(val!=null)
                sums[it] += row.getCell(it).getValue()
        }
        tmp = row.code
    }

    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        dataRowHelper.insert(row, index + i + 1)
        i = i + 1
    }

    // добавить строки "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 9
    totalRow.advancePayment = total10
    totalRow.outcomeInNalog = total11
    totalRow.outcomeInBuh = total12
    setTotalStyle(totalRow)

    dataRowHelper.insert(totalRow, dataRows.size()+1)
}

def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // список обязательных столбцов (графа 1..12)
        def requiredColumns = ['rowNumber', 'code', 'numberFirstRecord', 'opy',
                'operationDate', 'name', 'documentNumber', 'date',
                'periodCounts', 'advancePayment', 'outcomeInBuh']
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

        def i = 1

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

            // 1. Проверка даты совершения операции и границ отчетного периода (графа 5)
            if (row.operationDate < a || b < row.operationDate) {
                logger.error("В строке $row.rowNumber дата совершения операции вне границ отчётного периода!")
                return false
            }

            // 2. Проверка количества отчетных периодов при авансовых платежах (графа 9)
            if (row.periodCounts < 1 || 999 < row.periodCounts) {
                logger.error("В строке $row.rowNumber неверное количество отчетных периодов при авансовых платежах!")
                return false
            }

            // 3. Проверка на нулевые значения (графа 11, 12)
            if (row.outcomeInNalog == 0 && row.outcomeInBuh == 0) {
                logger.error("В строке $row.rowNumber все суммы по операции нулевые!")
                return false
            }

            // 4. Проверка формата номера первой записи
            if (!row.numberFirstRecord.matches('\\d{2}-\\w{6}')) {
                logger.error("В строке $row.rowNumber неправильно указан номер первой записи (формат: ГГ-НННННН, см. №852-р в актуальной редакции)!")
                return false
            }

            // 7. Проверка на уникальность поля «№ пп» (графа 1)
            for (def rowB : dataRows) {
                if (!row.equals(rowB) && row.rowNumber == rowB.rowNumber) {
                    logger.error("В строке $row.rowNumber нарушена уникальность номера по порядку!")
                    return false
                }
            }

            // 8. Проверка итоговых значений по кодам классификации дохода - нахождение кодов классификации расхода
            if (!totalGroupsName.contains(row.code)) {
                totalGroupsName.add(row.code)
            }

            // 9. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += row.getCell(alias).getValue()
            }

            // Проверки соответствия НСИ.
            // Проверка кода классификации расхода для данного РНУ
            if (!checkNSI(row, "code", "Классификатор расходов Сбербанка России для целей налогового учёта", 27)) {
                return false
            }
            // Проверка символа ОПУ для кода классификации расхода
            if (!checkNSI(row, "opy", "Классификатор расходов Сбербанка России для целей налогового учёта", 27)) {
                return false
            }
        }

        if (hasTotal) {
            def totalRow = getRowByAlias(dataRowHelper, 'total')

            // 5. Проверка на превышение суммы расхода по данным бухгалтерского учёта над суммой начисленного расхода (графа 11, 12)
            if (totalRow.outcomeInNalog <= totalRow.outcomeInBuh) {
                logger.warn("Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!")
            }

            // 8. Проверка итоговых значений по кодам классификации расхода
            for (def codeName : totalGroupsName) {
                def row = getRowByAlias(dataRowHelper, 'total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        logger.error("Итоговые значения по коду $codeName рассчитаны неверно!")
                        return false
                    }
                }
            }

            // 9. Проверка итогового значений по всей форме
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

// Проверка соответствия НСИ
boolean checkNSI(DataRow<Cell> row, String alias, String msg, Long id) {
    def cell = row.getCell(alias)
    if (cell.value != null && refBookService.getRecordData(id, cell.value) == null) {
        def msg2 = cell.column.name
        def rowNum = row.getIndex()
        logger.warn("Строка $rowNum: В справочнике «$msg» не найден элемент «$msg2»!")
        return false
    }
    return true
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

// Проверка при создании формы.
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

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

// Проверка является ли строка итоговой.
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

// Получить новую строку.
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    newRow.fix = 'Итого по КНУ ' + (refBookService.getStringValue(27, alias, 'CODE'))
    newRow.getCell('fix').colSpan = 9
    setTotalStyle(newRow)
    return newRow
}

// Установить стиль для итоговых строк.
void setTotalStyle(def row) {
    ['rowNumber', 'fix', 'code', 'numberFirstRecord', 'numberFirstRecord', 'opy', 'operationDate',
            'name', 'documentNumber', 'date', 'periodCounts',
            'advancePayment', 'outcomeInNalog', 'outcomeInBuh'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

//Посчитать сумму указанного графа для строк с общим кодом классификации
def calcSumByCode(def code, def alias) {
    def sum = 0
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().each { row ->
        if (!isTotal(row) && row.code == code) {
            sum += row.getCell(alias).getValue()
        }
    }
    return sum
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
        def index = row.rowNumber
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

/**
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
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

def round(def value, def int precision = 2) {
    if (value == null) {
        return null
    }
    return value.setScale(precision, RoundingMode.HALF_UP)
}
