/**
 * Скрипт для РНУ-48-1 (rnu48_1.groovy).
 * Форма "3.1	(РНУ-48.1) Регистр налогового учёта «ведомость ввода в эксплуатацию инвентаря и принадлежностей
 * до 40 000 руб.  ".
 *
 * Версия ЧТЗ: 57
 *
 * TODO:
 *          -   избавиться от обращений напрямую к formData.dataRows, когда будет готова обертка
 *          -   выяснить, как найти границы отчетного периода
 *          -   уточнить проверки строки ИТОГО, когда ее нет (пользователь еще не нажимал "Рассчитать")
 *          -   что за сквозная нумерация в рамках текущего отчетного года, как ее реализовывать?
 *
 * @author vsergeev
 *
 * Графы:
 * number           -   № пп
 * inventoryNumber  -   Инвентарный номер
 * usefulDate       -   Дата ввода в эксплуатацию
 * amount           -   Сумма, включаемая в состав материальных расходов
 */
switch (formDataEvent) {
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        if (logicalCheck(false)) {
            calc()
        }
        break
    case FormDataEvent.ADD_ROW :
        addNewRowAction()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

/**
 * Добавить новую строку.
 */
def addNewRowAction() {
    def totalAmountRow = tryFindTotalAmountRow()
    if (totalAmountRow != null) {
        //добавляем новую строку перед ИТОГО
        def tmpTotalAmountValue = totalAmountRow.amount
        formData.deleteDataRow(totalAmountRow)
        addNewRowAction()
        def newTotalAmountRow = addNewRow(true)
        newTotalAmountRow.amount = tmpTotalAmountValue
    } else {
        //добавляем новую строку в конец таблицы
        def newRow = addNewRow()
        newRow.number = formData.dataRows.size()
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    //удаляем строку с итогами предыдущих расчетов (если она есть)
    def totalAmountRow = tryFindTotalAmountRow()
    if (totalAmountRow != null) {
        formData.deleteDataRow(totalAmountRow)
    }
    //расчитываем новые итоговые значения
    def totalAmount = getTotalAmount()
    def newRow = addNewRow(true)
    newRow.amount = totalAmount
}

/**
 * Получаем новую строку
 * @param isTotalAmountRow = {@value true когда нужно получить строку итогов}
 * @return
 */
private def addNewRow(boolean isTotalAmountRow = false) {
    def newRow
    if (! isTotalAmountRow) {                   //полуаем просто новую строку, ячейки разрешены для редактирования
        newRow = formData.appendDataRow()
        def editableColsNames = getEditableColsNames()
        editableColsNames.each{ value ->
            newRow.getCell(value).editable = true
        }
    } else {                                    //получаем строку для ИТОГО
        newRow = formData.appendDataRow('total')
        newRow.inventoryNumber = 'Итого'
    }

    return newRow
}

boolean logicalCheck(boolean checkTotalAmount = true){
    def formIsValid = true
    //  проверка, что в таблице есть хотя бы одна строка
    if (formData.dataRows.size() == 0) {
        logger.error ('В таблице отсутствуют заполненные строки!')
        return false
    }

    def currentReportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //построчно проверяем данные формы
    for (def row : formData.dataRows) {
        if (! isTotalRow(row)) {
            // 1.  Обязательность заполнения поля графы (с 1 по 4)
            if (! requiredColsFilled()) {
                formIsValid = false
                break
            }

            // 2.  Проверка даты ввода в эксплуатацию и границ отчетного периода
            // Дата начала отчётного периода ≤ графа 3 ≤ дата окончания отчётного периода
            if (! dateInReportPeriod(row.usefulDate, currentReportPeriod)){
                formIsValid = false
                logger.error('Дата ввода в эксплуатацию вне границ отчетного периода!')
            }
        } else if (checkTotalAmount) {
            // 3.  Проверка итоговых значений по всей форме
            // todo как быть с проверками итоговых значений, если "Рассчитать" еще не нажимали и столбца ИТОГО нет?
            def controlTotalAmount = getTotalAmount()
            if ( ! row.get('amount').equals(controlTotalAmount)) {
                formIsValid = false
                logger.error('Итоговые значения рассчитаны неверно!')
            }
        }
    }

    return formIsValid
}

/**
 * Удалить строку.
 */
def deleteRow() {
    //todo (vsergeev) нумерация, после удаления строки из середины, должна обновляться?
    formData.deleteDataRow(currentDataRow)
}

/**
 * @return получаем сумму всех сумм, исключая сумму в итоговом поле
 */
private BigDecimal getTotalAmount() {
    BigDecimal totalAmount = new BigDecimal(0)
    for (def dataRow : formData.dataRows) {
        if ( ! isTotalRow(dataRow) && dataRow.get('amount') != null)
        totalAmount += dataRow.get('amount')
    }

    return totalAmount
}

boolean dateInReportPeriod(date, reportPeriod) {
    //todo (vsergeev) узнать, как определить границы отчетного периода и дописать проверку. пока всегда возвращаем true
    true
}

/**
 * Проверяет
 * 1.  Обязательность заполнения поля графы (с 1 по 4)
 * @return {@value true} если все обязательные поля заполнены, иначе {@value false}
 */
boolean requiredColsFilled() {
    def formIsValid = true
    def requiredCols = ['number', 'inventoryNumber', 'usefulDate', 'amount']

    for (def row : formData.dataRows) {
        if ( ! isTotalRow(row)) {      //строку ИТОГО не проверяем
            def fieldNumber = row.get('number')
            for (def col : requiredCols) {
                final def value = row.get(col)
                if (isBlankOrNull(value)) {
                    formIsValid = false
                    logger.error("Поле $fieldNumber не заполнено!")
                    break
                }
            }
        }
    }
    return formIsValid
}

/**
 * @return массив имен ячеек, доступных для редактирования
 */
def getEditableColsNames() {
    return ['number']
}

def tryFindTotalAmountRow() {
    return formData.dataRows.find {
        isTotalRow(it)
    }
}

boolean isTotalRow(dataRow) {
    dataRow.getAlias().equals('total')
}

boolean isBlankOrNull(value) {
    value == null || value.equals('')
}