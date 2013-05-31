/**
 * Скрипт для РНУ-26 (rnu26.groovy).
 * Форма "(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *      - логическая проверка 5: графа 10 может принимать значение только + или -, а в этой проверке сравнивается с "x"
 *      - графа 8, 14-17 расчитываются, но в перечне полей они могут редактироваться
 *      - логическая проверка 16: проверяемые значения и присваиваемые отличаются
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck(true)
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
    // проверка при "подготовить"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :
        checkOnPrepareOrAcceptance('Подготовка')
        break
    // проверка при "принять"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED :
        checkOnPrepareOrAcceptance('Принятие')
        break
    // проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED :
        checkOnCancelAcceptance()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        acceptance()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        checkNSI()
        break
}

// графа 1  - rowNumber
// графа 2  - issuer
// графа 3  - shareType
// графа 4  - tradeNumber
// графа 5  - currency
// графа 6  - lotSizePrev
// графа 7  - lotSizeCurrent
// графа 8  - reserveCalcValuePrev
// графа 9  - cost
// графа 10 - signSecurity
// графа 11 - marketQuotation
// графа 12 - rubCourse
// графа 13 - marketQuotationInRub
// графа 14 - costOnMarketQuotation
// графа 15 - reserveCalcValue
// графа 16 - reserveCreation
// графа 17 - reserveRecovery

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)

    // графа 2..7, 9..13
    ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent',
            'cost', 'signSecurity', 'marketQuotation', 'rubCourse'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
}

/**
 * Удалить строку.
 */
def deleteRow() {
    formData.dataRows.remove(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    /*
     * Проверка объязательных полей.
     */
    for (def row : formData.dataRows) {
        if (!isTotal(row)) {
            // список проверяемых столбцов (графа 2..7, 9, 10, 11)
            def requiredColumns = ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev',
                    'lotSizeCurrent', 'cost', 'signSecurity', 'marketQuotationInRub']

            if (!checkRequiredColumns(row, requiredColumns, true)) {
                return
            }
        }
    }

    /*
     * Расчеты.
     */

    // удалить строку "итого" и "итого по Эмитенту: ..."
    def delRow = []
    formData.dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        formData.dataRows.remove(getIndex(row))
    }
    if (formData.dataRows.isEmpty()) {
        return
    }

    // отсортировать/группировать
    formData.dataRows.sort { it.issuer }

    def tmp
    formData.dataRows.eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 8
        row.reserveCalcValuePrev = getPrevPeriodValue('reserveCalcValue', 'tradeNumber', row.tradeNumber)

        // графа 13
        if (row.marketQuotation != null && row.rubCourse != null) {
            row.marketQuotationInRub = row.marketQuotation * row.rubCourse
        }

        // графа 14
        tmp = (row.marketQuotationInRub == null ? 0 : round(row.lotSizeCurrent * row.marketQuotationInRub, 2))
        row.costOnMarketQuotation = tmp

        // графа 15
        if (row.signSecurity == '+') {
            def a = (row.cost == null ? 0 : row.cost)
            tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
        } else {
            tmp = 0
        }
        row.reserveCalcValue = tmp

        // графа 16
        tmp = row.reserveCalcValue - row.reserveCalcValuePrev
        row.reserveCreation = (tmp > 0 ? tmp : 0)

        // графа 17
        row.reserveRecovery = (tmp < 0 ? Math.abs(tmp) : 0)
    }

    // графы для которых надо вычислять итого и итого по эмитенту (графа 6..9, 14..17)
    def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
            'cost', 'costOnMarketQuotation', 'reserveCalcValue',
            'reserveCreation', 'reserveRecovery']
    // добавить строку "итого"
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.issuer = 'Общий итог'
    setTotalStyle(totalRow)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }

    // посчитать "итого по Эмитенту:..."
    def totalRows = [:]
    def sums = [:]
    tmp = null
    totalColumns.each {
        sums[it] = 0
    }
    formData.dataRows.eachWithIndex { row, i ->
        if (!isTotal(row)) {
            if (tmp == null) {
                tmp = row.issuer
            }
            // если код расходы поменялся то создать новую строку "итого по Эмитента:..."
            if (tmp != row.issuer) {
                totalRows.put(i, getNewRow(tmp, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по Эмитента:..."
            if (i == formData.dataRows.size() - 2) {
                totalColumns.each {
                    sums[it] += (row.getCell(it).getValue() ?: 0)
                }
                totalRows.put(i + 1, getNewRow(row.issuer, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += (row.getCell(it).getValue() ?: 0)
            }
            tmp = row.issuer
        }
    }
    // добавить "итого по Эмитенту:..." в таблицу
    def i = 0
    totalRows.each { index, row ->
        formData.dataRows.add(index + i, row)
        i = i + 1
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    // данные предыдущего отчетного периода
    def formDataOld = getFormDataOld()

    if (formDataOld != null &&
            !formDataOld.dataRows.isEmpty() && !formData.dataRows.isEmpty()) {
        // 1. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде (выполняется один раз для всего экземпляра)
        def count
        def missContract = []
        def severalContract = []
        formDataOld.dataRows.each { prevRow ->
            if (prevRow.reserveCalcValue > 0) {
                count = 0
                formDataOld.dataRows.each { row ->
                    if (row.tradeNumber == prevRow.tradeNumber) {
                        count += 1
                    }
                }
                if (count == 0) {
                    missContract.add(prevRow.tradeNumber)
                } else if (count > 1) {
                    severalContract.add(prevRow.tradeNumber)
                }
            }
        }
        if (!missContract.isEmpty()) {
            def message = missContract.join(', ')
            logger.warn("Отсутствуют строки с номерами сделок: $message!")
        }
        if (!severalContract.isEmpty()) {
            def message = severalContract.join(', ')
            logger.warn("Существует несколько строк с номерами сделок: $message!")
        }
    }

    if (!formData.dataRows.isEmpty()) {
        def i = 1

        // список проверяемых столбцов (графа 1..3, 5..10, 13, 14)
        columns = ['issuer', 'shareType', 'currency', 'lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
                'cost', 'signSecurity', 'marketQuotationInRub', 'costOnMarketQuotation']

        // суммы строки общих итогов
        def totalSums = [:]

        // графы для которых надо вычислять итого и итого по эмитенту (графа 6..9, 14..17)
        def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
                'cost', 'costOnMarketQuotation', 'reserveCalcValue',
                'reserveCreation', 'reserveRecovery']

        // признак наличия итоговых строк
        def hasTotal = false

        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        def tmp
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 15. Обязательность заполнения поля графы 1..3, 5..10, 13, 14
            if (!checkRequiredColumns(row, columns, useLog)) {
                return false
            }

            // дополнительная проверка графы 10
            if (row.signSecurity != '+' && row.signSecurity != '-') {
                logger.error('Графа 10 может принимать только следующие значения: "+" или "-".')
                return
            }

            // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 8, 17)
            if (row.lotSizeCurrent == 0 && row.reserveCalcValuePrev != row.reserveRecovery) {
                logger.warn('Графы 8 и 17 неравны!')
            }

            // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 9, 14, 15)
            if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
                logger.warn('Графы 9, 14 и 15 ненулевые!')
            }

            // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6, 8, 17)
            if (row.lotSizePrev == 0 && (row.reserveCalcValuePrev != 0 || row.reserveRecovery != 0)) {
                logger.error('Графы 8 и 17 ненулевые!')
                return false
            }

            // 5. Проверка необращающихся акций (графа 10, 15, 16)
            if (row.signSecurity == 'x' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                logger.warn('Акции необращающиеся, графы 15 и 16 ненулевые!')
            }

            // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
            tmp = (row.reserveCalcValue ?: 0) - row.reserveCalcValuePrev
            if (row.signSecurity == '+' && tmp > 0 && row.reserveRecovery != 0) {
                logger.error('Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
                return false
            }

            // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 16)
            if (row.signSecurity == '+' && tmp < 0 && row.reserveCreation != 0) {
                logger.error('Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
                return false
            }

            // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
            if (row.signSecurity == '+' && tmp == 0 &&
                    (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
                logger.error('Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
                return false
            }

            // 9. Проверка корректности формирования резерва (графа 8, 15, 16, 17)
            if (row.reserveCalcValuePrev + (row.reserveCreation ?: 0) != (row.reserveCalcValue ?: 0) + (row.reserveRecovery ?: 0)) {
                logger.error('Резерв сформирован неверно!')
                return false
            }

            // 10. Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                    row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
                logger.warn('Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!')
            }

            // 11. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 6, 7 (за предыдущий период) )
            if (checkOld(row, 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', formDataOld)) {
                def curCol = 4
                def curCol2 = 6
                def prevCol = 4
                def prevCol2 = 7
                logger.warn("РНУ сформирован некорректно! Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-26 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-26 за предыдущий отчётный период.")
            }

            // 12. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 8, 15 (за предыдущий период) )
            if (checkOld(row, 'tradeNumber', 'reserveCalcValuePrev', 'reserveCalcValue', formDataOld)) {
                def curCol = 4
                def curCol2 = 4
                def prevCol = 8
                def prevCol2 = 15
                logger.error("РНУ сформирован некорректно! Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-26 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-26 за предыдущий отчётный период.")
                return false
            }

            // 16. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                return false
            }
            i += 1

            // 17. Арифметическая проверка графы 8, 14..17
            // графа 8
            if (row.reserveCalcValuePrev != getPrevPeriodValue('reserveCalcValue', 'tradeNumber', row.tradeNumber)) {
                name = getColumnName(row, 'reserveCalcValuePrev')
                logger.error("Неверно рассчитана графа «$name»!")
                return false
            }

            // графа 13
            if (row.marketQuotation != null && row.rubCourse != null &&
                    row.marketQuotationInRub != row.marketQuotation * row.rubCourse) {
                name = getColumnName(row, 'marketQuotationInRub')
                logger.error("Неверно рассчитана графа «$name»!")
                return false
            }

            // графа 14
            tmp = (row.marketQuotationInRub == null ? 0 : round(row.lotSizeCurrent * row.marketQuotationInRub, 2))
            if (row.costOnMarketQuotation != tmp) {
                name = getColumnName(row, 'costOnMarketQuotation')
                logger.error("Неверно рассчитана графа «$name»!")
                return false
            }

            // графа 15
            if (row.signSecurity == '+') {
                def a = (row.cost == null ? 0 : row.cost)
                tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
            } else {
                tmp = 0
            }
            if (row.reserveCalcValue != tmp) {
                name = getColumnName(row, 'reserveCalcValue')
                logger.error("Неверно рассчитана графа «$name»!")
                return false
            }

            // графа 16
            tmp = (row.reserveCalcValue ?: 0) - row.reserveCalcValuePrev
            if (row.reserveCreation != (tmp > 0 ? tmp : 0)) {
                name = getColumnName(row, 'reserveCreation')
                logger.error("Неверно рассчитана графа «$name»!")
                return false
            }

            // графа 17
            if (row.reserveRecovery != (tmp < 0 ? Math.abs(tmp) : 0)) {
                name = getColumnName(row, 'reserveRecovery')
                logger.error("Неверно рассчитана графа «$name»!")
                return false
            }
            // 17. конец=========================================

            // 18. Проверка итоговых значений по эмитентам
            if (!totalGroupsName.contains(row.issuer)) {
                totalGroupsName.add(row.issuer)
            }

            // 19. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }
        }

        if (formDataOld != null && hasTotal) {
            totalRow = formData.getDataRow('total')
            totalRowOld = formDataOld.getDataRow('total')

            // 13. Проверка корректности заполнения РНУ (графа 6, 7 (за предыдущий период))
            if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                def curCol = 6
                def prevCol = 7
                logger.error("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе $curCol = «Итого» по графе $prevCol формы РНУ-26 за предыдущий отчётный период.")
                return false
            }

            // 14. Проверка корректности заполнения РНУ (графа 8, 15 (за предыдущий период))
            if (totalRow.cost != totalRowOld.reserveCalcValue) {
                def curCol = 8
                def prevCol = 15
                logger.error("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе $curCol = «Итого» по графе $prevCol формы РНУ-26 за предыдущий отчётный период.")
                return false
            }
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 18. Проверка итоговых значений по эмитенту
            for (def codeName : totalGroupsName) {
                def row = formData.getDataRow('total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        logger.error("Итоговые значения по эмитенту $codeName рассчитаны неверно!")
                        return false
                    }
                }
            }

            // 19. Проверка итогового значений по всей форме
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
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    // 1. Проверка курса валюты со справочным - Проверка актуальности значения» графы 6» на дату по «графе 5»
    if (false) {
        logger.warn('Неверный курс валюты!')
    }

    // 1. Проверка актуальности поля «Валюта выпуска ценной бумаги»	Какой алгоритм?
    if (false) {
        logger.warn('Валюта выпуска ценной бумаги указана неверно!')
    }

    // 2. Проверка актуальности поля «Признак ценной бумаги на текущую отчётную дату»
    if (false) {
        logger.warn('')
    }

    // 3. Проверка актуальности поля «Курс рубля к валюте рыночной котировки»
    if (false) {
        logger.warn('')
    }
    return true
}

/**
 * Проверка наличия и статуса консолидированной формы при осуществлении перевода формы в статус "Подготовлена"/"Принята".
 */
void checkOnPrepareOrAcceptance(def value) {
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error("$value первичной налоговой формы невозможно, т.к. уже подготовлена консолидированная налоговая форма.")
            }
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    // удалить все строки и собрать из источников их строки
    formData.dataRows.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                source.getDataRows().each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        formData.dataRows.add(row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверки при переходе "Отменить принятие".
 */
void checkOnCancelAcceptance() {
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(),
            formData.getFormType().getId(), formData.getKind());
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}

/**
 * Принять.
 */
void acceptance() {
    if (!logicalCheck(true) || !checkNSI()) {
        return
    }
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() {
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
    }
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = FormDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def from = 0
    def to = formData.dataRows.size() - 2
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
    newRow.issuer = alias + ' итог'
    setTotalStyle(newRow)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}

/**
 * Сверить данные с предыдущим периодом.
 *
 * @param row строка нф текущего периода
 * @param likeColumnName псевдоним графы по которому ищутся соответствующиеся строки
 * @param curColumnName псевдоним графы текущей нф для второго условия
 * @param prevColumnName псевдоним графы предыдущей нф для второго условия
 * @param prevForm данные нф предыдущего периода
 */
def checkOld(def row, def likeColumnName, def curColumnName, def prevColumnName, def prevForm) {
    if (prevForm == null) {
        return false
    }
    if (row.getCell(likeColumnName).getValue() == null) {
        return false
    }
    for (def prevRow : prevForm.dataRows) {
        if (row.getCell(likeColumnName).getValue() == prevRow.getCell(likeColumnName).getValue() &&
                row.getCell(curColumnName).getValue() != prevRow.getCell(prevColumnName).getValue()) {
            return true
        }
    }
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-26 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = FormDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * * Посчитать сумму указанного графа для строк с общим значением
 *
 * @param value значение общее для всех строк суммирования
 * @param alias название графа
 */
def calcSumByCode(def value, def alias) {
    def sum = 0
    formData.dataRows.each { row ->
        if (!isTotal(row) && row.issuer == value) {
            sum += (row.getCell(alias).getValue() ?: 0)
        }
    }
    return sum
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'issuer', 'shareType', 'tradeNumber', 'currency',
            'lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost', 'signSecurity',
            'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns, def useLog) {
    def colNames = []

    // если не заполнены графа 11 и графа 12, то графа 13 должна быть заполнена вручную
    if (row.marketQuotation != null && row.rubCourse != null) {
        columns -= 'marketQuotationInRub'
    }

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
        def index = getIndex(row) + 1
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
 * Получить значение за предыдущий отчетный период.
 *
 * @param needColumnName псевдоним графы значение которой надо получить (графа значения)
 * @param searchColumnName псевдоним графы по которой нужно отобрать значение (графа поиска)
 * @param searchValue значение графы поиска
 * @return возвращает найденое значение, иначе возвратит 0
 */
def getPrevPeriodValue(def needColumnName, def searchColumnName, def searchValue) {
    def formDataOld = getFormDataOld()
    if (formDataOld != null && !formDataOld.dataRows.isEmpty()) {
        for (def row : formDataOld.dataRows) {
            if (row.getCell(searchColumnName).getValue() == searchValue) {
                return row.getCell(needColumnName)
            }
        }
    }
    return 0
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