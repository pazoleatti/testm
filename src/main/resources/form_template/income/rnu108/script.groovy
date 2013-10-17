package form_template.income.rnu108
/**
 * Скрипт для РНУ-108
 * Форма "(РНУ-108) Регистр налогового учёта расходов, связанных с приобретением услуг у Взаимозависимых лиц и резидентов оффшорных зон и подлежащих корректировке в связи с применением цен, не соответствующих рыночному уровню"
 * formTemplateId=395
 *
 * @author akadyrgulov
 * @author Stanislav Yasinskiy
 */

// графа 1 - rowNumber
// графа 2 - personName
// графа 3 - inn
// графа 4 - date
// графа 5 - code
// графа 6 - docNumber
// графа 7 - docDate
// графа 8 - contractNumber
// графа 9 - contractDate
// графа 10 - priceService
// графа 11 - priceMarket
// графа 12 - factSum
// графа 13 - correctKoef
// графа 14 - marketSum
// графа 15 - deviatSum
// графа 16 - code2

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
        calc() && logicalCheck()
        break
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

    // отсортировать/группировать
    dataRowHelper.save(dataRowHelper.getAllCached().sort { (it.personName) })

    def total15 = 0

    dataRowHelper.getAllCached().eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        if (row.personName != null) {
            def map = refBookService.getRecordData(9, row.personName)
            row.inn = map.INN_KIO.stringValue
        } else {
            row.inn = null
        }

        // графа 12
        row.factSum = row.priceService

        // графа 14
        // TODO Если цена за весь объем работ
        if (true) {
            row.marketSum = row.priceMarket
        } else if (true && row.priceService != null && ow.priceMarket != null) { // TODO Если за единицу товара
            row.marketSum = row.priceService * row.priceMarket
        } else {
            row.marketSum = null
        }

        // графа 15
        if (row.marketSum != null && row.factSum != null)
            row.deviatSum = (row.marketSum - row.factSum).abs()

        // графа 15 для последней строки "итого"
        if(row.deviatSum!=null)
            total15 += row.deviatSum
    }

    dataRowHelper.save(dataRowHelper.getAllCached().sort { (it.personName) })


    /** Столбцы для которых надо вычислять итого и итого по Взаимозависимому лицу (резиденту оффшорной зоны). Графа 15. */
    def totalColumns = ['deviatSum']

    // посчитать "итого по Взаимозависимому лицу (резиденту оффшорной зоны)"
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }

    dataRowHelper.getAllCached().eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.personName
        }
        // если код расходы поменялся то создать новую строку "итого по Взаимозависимому лицу (резиденту оффшорной зоны)"
        if (tmp != row.personName) {
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по Взаимозависимому лицу (резиденту оффшорной зоны)"
        if (i == dataRowHelper.getAllCached().size() - 1) {
            totalColumns.each {
                if (row.getCell(it).getValue() != null)
                    sums[it] += row.getCell(it).getValue()
            }
            totalRows.put(i + 1, getNewRow(row.personName, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        totalColumns.each {
            if (row.getCell(it).getValue() != null)
                sums[it] += row.getCell(it).getValue()
        }
        tmp = row.personName
    }

    // добавить "итого по Взаимозависимому лицу (резиденту оффшорной зоны)" в таблицу
    def i = 1
    totalRows.each { index, row ->
        dataRowHelper.insert(row, index + i++)
    }

    // добавить строки "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 14
    totalRow.deviatSum = total15
    setTotalStyle(totalRow)

    dataRowHelper.insert(totalRow, dataRows.size() + 1)

    return true
}

/**
 * Логические проверки.
 *
 */
def logicalCheck() {
    def tmp
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    /** Дата начала отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def a = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def b = (tmp ? tmp.getTime() : null)

    if (!dataRows.isEmpty()) {
        def i = 1

        // список обязательных столбцов (все, кроме графы 13)
        def requiredColumns = ['rowNumber', 'personName', 'inn', 'date', 'code',
                'docNumber', 'docDate', 'contractNumber', 'contractDate', 'priceService',
                'priceMarket', 'factSum', 'marketSum', 'deviatSum', 'code2']

        // суммы строки общих итогов
        def totalSums = [:]

        // столбцы для которых надо вычислять итого и итого по коду классификации дохода. Графа 15
        def totalColumns = ['deviatSum']

        // признак наличия итоговых строк
        def hasTotal = false

        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        for (def row : dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 1. Заполненность обязательных графов
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }

            // 2. Проверка на уникальность поля «№ пп»
            for (def rowB : dataRows) {
                if (!row.equals(rowB) && row.rowNumber == rowB.rowNumber) {
                    logger.error("В строке $row.rowNumber нарушена уникальность номера по порядку!")
                    return false
                }
            }

            // 3. Проверка даты совершения операции и границ отчётного периода
            if (row.date < a || b < row.date) {
                logger.error("В строке $row.rowNumber дата совершения операции вне границ отчётного периода!")
                return false
            }

            // 4. Арифметические проверки графы 12, 14, 15
            // графа 12
            if (row.factSum != row.priceService) {
                logger.error("В строке $row.rowNumber неверно рассчитана графа \"Сумма фактически начисленного расхода\"!")
                return false
            }
            // графа 14
            if (row.marketSum != row.priceMarket) {
                logger.error("В строке $row.rowNumber неверно рассчитана графа \"Сумма расхода соответствующая рыночному уровню\"!")
                return false
            }
            // графа 15
            if (row.deviatSum != Math.abs(row.marketSum - row.factSum)) {
                logger.error("В строке $row.rowNumber неверно рассчитана графа \"Сумма отклонения (превышения) фактического расхода от рыночного уровня\"!")
                return false
            }

            // 5. Проверка итоговых значений по кодам классификации дохода - нахождение кодов классификации расхода
            if (!totalGroupsName.contains(row.personName)) {
                totalGroupsName.add(row.personName)
            }

            // 6. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += row.getCell(alias).getValue()
            }

            //Проверки соответствия НСИ
            if (!checkNSI(row, "personName", "Организации-участники контролируемых сделок", 9)) {
                return false
            }
            if (!checkNSI(row, "code", "Классификатор доходов Сбербанка России для целей налогового учёта", 28)) {
                return false
            }
        }

        if (hasTotal) {
            def totalRow = getRowByAlias(dataRowHelper, 'total')

            // 5. Проверка итоговых значений по кодам классификации расхода
            for (def personName : totalGroupsName) {
                def row = getRowByAlias(dataRowHelper, 'total' + personName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(personName, alias) != row.getCell(alias).getValue()) {
                        logger.error("Итоговые значения по наименованию взаимозависимого лица (резидента оффшорной зоны) $personName рассчитаны неверно!")
                        return false
                    }
                }
            }

            // 6. Проверка итогового значений по всей форме
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

//Добавить новую строку
void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    // графы 2..11, 13
    ['personName', 'date',
            'code', 'docNumber',
            'docDate', 'contractNumber',
            'contractDate', 'priceService',
            'priceMarket', 'correctKoef'].each {
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

/*
 * Вспомогательные методы.
 */

// Проверка является ли строка итоговой.
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def data = formDataService.getDataRowHelper(formData)
    def from = 0
    def to = dataRowHelper.getAllCached().size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    newRow.fix = 'Итого по Взаимозависимому лицу (резиденту оффшорной зоны)'
    newRow.getCell('fix').colSpan = 14
    setTotalStyle(newRow)
    return newRow
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'fix', 'personName', 'inn', 'date',
            'docDate', 'contractNumber', 'contractDate',
            'priceMarket', 'factSum', 'correctKoef', 'priceService',
            'marketSum', 'deviatSum', 'code2', 'code', 'docNumber',
    ].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Посчитать сумму указанного графа для строк с общим наименованием взаимозависимого лица (резидента оффшорной зоны)
 *
 * @param personName Наименование взаимозависимого лица (резидента оффшорной зоны)
 * @param alias название графа
 */
def calcSumByCode(def personName, def alias) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def sum = 0
    dataRowHelper.getAllCached().each { row ->
        if (!isTotal(row) && row.personName == personName) {
            sum += row.getCell(alias).getValue()
        }
    }
    return sum
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().indexOf(row)
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    def cell
    columns.each {
        cell = row.getCell(it)
        if (cell.isEditable() && (cell.getValue() == null || row.getCell(it).getValue() == '')) {
            def name = getColumnName(row, it)
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