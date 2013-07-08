/**
 * Скрипт для РНУ-38.1 (rnu38-1.groovy).
 * Форма "РНУ-38.1" "Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1".
 *
 * @author ivildanov
 *
 * Графы
 * 1  -  series
 * 2  -  amount
 * 3  -  shortPositionDate
 * 4  -  maturityDate
 * 5  -  incomeCurrentCoupon
 * 6  -  currentPeriod
 * 7  -  incomePrev
 * 8  -  incomeShortPosition
 * 9  -  totalPercIncome
 *
 */


switch (formDataEvent) {

    case FormDataEvent.CREATE:
        //Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при создании формы.
        checkCreation()
        break

    case FormDataEvent.ADD_ROW:
        addNewRow()
        break

    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break

// Инициирование Пользователем проверки данных формы в статусе «Создана», «Подготовлена», «Утверждена», «Принята»
    case FormDataEvent.CHECK:
        // 1.	Логические проверки значений налоговой формы.
        logicalCheck()
        // 2.	Проверки соответствия НСИ.
        break

    case FormDataEvent.CALCULATE:
        fillForm()
        logicalCheck()
        break

// todo проверить события-переходы
// Инициирование Пользователем перехода «Подготовить»
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Подготовлена».
        checkOnPrepareOrAcceptance('Подготовка')
        //2.	Логические проверки значений налоговой формы.
        logicalCheck()
        //3.	Проверки соответствия НСИ.
        // Не осуществляются.
        break

// Инициирование Пользователем  выполнение перехода «Утвердить»
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Утверждена».
        //2.	Логические проверки значений налоговой формы.
        logicalCheck()
        //3.	Проверки соответствия НСИ.
        // Не осуществляются.
        break

// Инициирование Пользователем  выполнение перехода «Принять»
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Принята».
        //2.	Логические проверки значений налоговой формы.
        logicalCheck()
        //3.	Проверки соответствия НСИ.
        // Не осуществляются.
        break

// Инициирование Пользователем выполнения перехода «Отменить принятие»
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
        //2.	Логические проверки значений налоговой формы.
        logicalCheck()
        //3.	Проверки соответствия НСИ.
        // Не осуществляются.
        break

    case FormDataEvent.COMPOSE:
        consolidation()
        fillForm()
        logicalCheck()
        break
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

/**
 * Добавить новую строку.
 */
def addNewRow() {

    def newRow = formData.createDataRow()
    formData.dataRows.add(formData.dataRows.size() - 1, newRow)

    // Графы 1-6 Заполняется вручную
    ['series', 'amount', 'shortPositionDate', 'maturityDate', 'incomeCurrentCoupon', 'currentPeriod'].each { column ->
        newRow.getCell(column).setEditable(true)
    }
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isTotalRow(currentDataRow)) {
        formData.dataRows.remove(currentDataRow)
    } else {
        logger.error('Невозможно удалить фиксированную строку!')
    }
}

/**
 * Логические проверки
 */
def logicalCheck() {

    if (formData.dataRows.isEmpty() || (formData.dataRows.size() == 1 && formData.dataRows.get(formData.dataRows.size() - 1).getAlias() == 'total')) {
        logger.error('Отсутствуют данные')
        return false
    }

    // отчетная дата
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def reportDay = (tmp ? tmp.getTime() + 1 : null)


    for (def row : formData.dataRows) {
        if (!isTotalRow(row)) {
            //  8.	Проверка даты открытия короткой позиции
            if (row.shortPositionDate > reportDay) {
                logger.error('Неверно указана дата приобретения (открытия короткой позиции)!')
                return false
            }

            // 9. Проверка даты погашения
            if (row.maturityDate > reportDay) {
                logger.error('Неверно указана дата погашения предыдущего купона!')
                return false
            }

            // todo 10. Арифметическая проверка графы 7
            // todo 11. Арифметическая проверка графы 8

            // 12. Арифметическая проверка графы 9
            if (row.totalPercIncome != row.incomePrev + row.incomeShortPosition) {
                logger.error('Неверно рассчитана графа «Всего процентный доход (руб.коп.)»!')
                return false
            }
        }
    }

    // 13. Проверка итоговых значений по всей форме
    def total2 = 0, total7 = 0, total8 = 0, total9 = 0

    formData.dataRows.each { row ->
        if (!isTotalRow(row)) {
            total2 = total2 + row.amount
            total7 = total7 + row.incomePrev
            total8 = total8 + row.incomeShortPosition
            total9 = total9 + row.totalPercIncome
        }
    }

    def totalrow = getTotalRow()
    if (totalrow.amount != total2 || totalrow.incomePrev != total7 || totalrow.incomeShortPosition != total8 || totalrow.totalPercIncome != total9) {
        logger.error('Итоговые значения рассчитаны неверно!')
        return false
    }

    return true
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void fillForm() {

    //todo последний день отчетного месяца?
    def lastDay = new Date()

    /*
     * Заполнение 7,8 и 9 граф
     * Вычисление ИТОГО для граф 2,7-9
     */

    def total2 = 0, total7 = 0, total8 = 0, total9 = 0

    formData.dataRows.each { row ->
        if (!isTotalRow(row)) {
            // графа 7
            row.incomePrev = getColumn7(row, lastDay)

            // графа 8
            row.incomeShortPosition = getColumn8(row, lastDay)

            // графа 9
            row.totalPercIncome = getColumn9(row)

            // подсчет ИТОГО
            total2 = total2 + row.amount

            total7 = total7 + row.incomePrev

            total8 = total8 + row.incomeShortPosition

            total9 = total9 + row.totalPercIncome
        }
    }


    def totalrow = getTotalRow()
    totalrow.amount = total2
    totalrow.incomePrev = total7
    totalrow.incomeShortPosition = total8
    totalrow.totalPercIncome = total9

}

/**
 * Проверка наличия и статуса консолидированной формы при осуществлении перевода формы в статус "Подготовлена"/"Принята".
 */
void checkOnPrepareOrAcceptance(def value) {

    // получаем информацию о формах-потребителях в виде списка
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
                source.getDataRows().each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        formData.dataRows.add(row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка фиксированной.
 */
def isTotalRow(def row) {
    return row != null && row.getAlias() == 'total'
}

/**
 * 	Посчитать значение графы 7.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def getColumn7(def row, def lastDay) {
    // todo ИНАЧЕ?
    if (row.maturityDate > row.shortPositionDate) {
        return round((row.incomeCurrentCoupon * (lastDay - row.maturityDate) / row.currentPeriod), 2) * row.amount
    } else {
        return 0
    }
}

/**
 * 	Посчитать значение графы 8.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def getColumn8(def row, def lastDay) {
    // todo ИНАЧЕ?
    if (row.maturityDate <= row.shortPositionDate) {
        return round((row.incomeCurrentCoupon * (lastDay - row.shortPositionDate) / row.currentPeriod), 2) * row.amount
    } else {
        return 0
    }

}

/**
 * 	Посчитать значение графы 9.
 *
 * @param row строка
 */
def getColumn9(def row) {
    return (row.incomePrev + row.incomeShortPosition)
}

/**
 * 	Поиск итоговой строки
 */
def getTotalRow() {
    for (def row : formData.dataRows) {
        if (row.getAlias() == 'total') {
            return row
        }
    }
    return null
}