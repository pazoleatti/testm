/**
 * Скрипт для РНУ-48-1 (rnu48_1.groovy).
 * Форма "3.1	(РНУ-48.1) Регистр налогового учёта «ведомость ввода в эксплуатацию инвентаря и принадлежностей
 * до 40 000 руб.  ".
 *
 * Версия ЧТЗ: 64
 *
 * TODO:
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
        logicalCheckWithTotalDataRowCheck()
        break
    case FormDataEvent.CALCULATE :
        if (logicalCheckWithoutTotalDataRowCheck()) {
            calc()
        }
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteCurrentRow()
        break
}

/**
 * Расчеты
 */
void calc() {
    //расчитываем новые итоговые значения
    def totalResults = getTotalResultsForCols()
    def totalRow = formData.getDataRow(getTotalDataRowAlias())
    getTotalColsAliases().each { colName ->
        totalRow[colName] = totalResults[colName]
    }
}

/**
 * проверяем все строки, включая итоговые
 */
boolean logicalCheckWithTotalDataRowCheck(){
    if (logicalCheckWithoutTotalDataRowCheck()) {
        checkTotalDataRowValid()
    }
}

/**
 * проверяем все строки, за исключением итоговых
 */
boolean logicalCheckWithoutTotalDataRowCheck(){
    def formIsValid = true
    //  проверка, что в таблице есть хотя бы одна строка
    if (formData.dataRows.isEmpty()) {
        logger.error ('В таблице отсутствуют заполненные строки!')
        return false
    }

    // 1.  Обязательность заполнения поля графы (с 1 по 4)
    if (! requiredColsFilled()) {
        logger.error('Не заполнены обязательные значения!')
        return false
    }

    def reportPeriodRange = getReportPeriodRange()

    //построчно проверяем данные формы
    for (def row : formData.dataRows) {
        if ( ! isInTotalRowsAliases(row.getAlias())) {
            // 2.  Проверка даты ввода в эксплуатацию и границ отчетного периода
            // Дата начала отчётного периода ≤ графа 3 ≤ дата окончания отчётного периода
            if (! reportPeriodRange.containsWithinBounds(row.usefulDate)){
                formIsValid = false
                logger.error('Дата ввода в эксплуатацию вне границ отчетного периода!')
            }
        }
    }

    return formIsValid
}

/**
 * Проверяет
 * 1.  Обязательность заполнения поля графы (с 1 по 4)
 * @return {@value true} если все обязательные поля заполнены, иначе {@value false}
 */
boolean requiredColsFilled() {
    def formIsValid = true

    for (def row : formData.dataRows) {
        if ( ! isInTotalRowsAliases(row.getAlias())) {      //строку ИТОГО не проверяем
            def fieldNumber = formData.dataRows.indexOf(row) + 1
            for (def col : getEditableColsAliases()) {
                final def value = row.get(col)
                if (isBlankOrNull(value)) {
                    formIsValid = false
                    logger.error("Строка $fieldNumber не заполнена!")
                    break
                }
            }
        }
    }
    return formIsValid
}

/**
 * проверяем, что итоговые значения рассчитаны верно
 */
boolean checkTotalDataRowValid() {
    def totalDataRow = formData.getDataRow(getTotalDataRowAlias())
    if (totalDataRow == null) {
        logger.error('Строка Итого не найдена')
        return false
    }
    def controlTotalResultsForCols = getTotalResultsForCols()
    for (def colName : getTotalColsAliases()) {
        if (totalDataRow[colName] != controlTotalResultsForCols[colName]) {
            logger.error('Строка Итого рассчитана неверно!')
            return false
        }
    }

    return true
}

def getTotalDataRowAlias() {
    return 'total'
}

/********************************   ОБЩИЕ ФУНКЦИИ   ********************************/

/**
 * false, если в строке нет символов или строка null
 * true, если в строке есть символы
 */
boolean isBlankOrNull(value) {
    return (value == null || value.equals(''))
}

/**
 * возвращает список алиасов столбцов, доступных для редактирования
 */
def getEditableColsAliases() {
    return ['inventoryNumber', 'usefulDate', 'amount']
}

/**
 * возвращает true, если в таблице выделен какой-нибудь столбце
 * иначе возвращает false
 */
boolean isCurrentDataRowSelected() {
    return (currentDataRow != null && formData.dataRows.indexOf(currentDataRow) >= 0)
}

/***********   ОБЩИЕ ФУНКЦИИ ДЛЯ ИТОГОВЫХ СТРОК   ***********/

/**
 * false, если алиас строки не входит в список алиасов итоговых строк
 * true, если алиас строки входит в алиас итоговых строк
 */
boolean isInTotalRowsAliases(def alias){
    return (totalRowsAliases.find {totalAlias -> alias == totalAlias} != null)
}

/**
 * возвращает список алиасов для итоговых строк
 */
def getTotalRowsAliases() {
    return ['total']
}

/***********   ОБЩИЕ ФУНКЦИИ ДЛЯ СТОЛБЦОВ, ПО КОТОРЫМ ПОДВОДЯТСЯ ИТОГИ   ***********/

/**
 * возвращает список алиасов для стобцов, по которым подводятся итоги
 */
def getTotalColsAliases() {
    return ['amount']
}

/**
 * находим для всех строк, кроме итоговых, суммы по столбцам, по которым подводят итоги
 * возвращаем мапу вида алиас_столбца -> итоговое_значение
 */
def getTotalResultsForCols() {
    def result = [:]
    for (def colAlias : getTotalColsAliases()) {
        result.put(colAlias, formData.dataRows.sum {row ->
            if (! isInTotalRowsAliases(row.getAlias())) {    //строка не входит в итоговые
                row[colAlias]
            } else {
                0
            }
        })
    }
    return result
}

/***********   ДОБАВЛЕНИЕ СТРОКИ В ТАБЛИЦУ С ФИКСИРОВАННЫМИ СТРОКАМИ ИТОГОВ   ***********/

/**
 * добавляет строку в таблицу с фиксированными строками итогов. строка добавляется перед выделенной
 * строкой (если такая есть). если выделенной строки нет, то строка добавляется в конец таблицы перед
 * последней итоговой строкой
 */
def addNewRow() {
    def newRow = formData.createDataRow()

    getEditableColsAliases().each{ value ->
        newRow.getCell(value).editable = true
    }

    int index = getNewRowIndex()

    formData.dataRows.add(index, newRow)

    return newRow
}

/**
 * возвращает индекс для добавляемого столбца
 * (находит строку, удовлетворяющую условиям addNewRow(). на ее место будет произведена вставка новой строки)
 */
int getNewRowIndex() {
    def index

    def isTotalRow = false
    if (isCurrentDataRowSelected()) {
        index = formData.dataRows.indexOf(currentDataRow)
        if ( ! isBlankOrNull(currentDataRow.getAlias())) {
            isTotalRow = true
        }
    } else {
        index = formData.dataRows.size() - 1
    }

    index = goToTopAndGetMaxIndexOfRowWithoutAlias(index)

    if (isTotalRow && index != null) {
        index += 1
    } else if (index == null) {
        index = 0
    }

    return index
}

/**
 * идем вверх по таблице, начиная со строки с индексом startIndex (включительно). находим первую неитоговую
 * строку (алиас которой не помечен как итоговый в getTotalRowsAliases()).
 *
 * возвращает индекс этой строки.
 */
def goToTopAndGetMaxIndexOfRowWithoutAlias(def startIndex) {
    for (int i = startIndex; i >= 0; i--) {
        if (getTotalRowsAliases().find{ totalRowAlias ->
            totalRowAlias == formData.dataRows[i].getAlias()
        } == null) {
            return i
        }
    }

    return null
}

/***********   УДАЛЕНИЕ СТРОКИ ИЗ ТАБЛИЦЫ С ФИКСИРОВАННЫМИ СТРОКАМИ ИТОГОВ   ***********/

/**
 * удаляет выделенную строку, если она не является итоговой
 * если выделенная строки является итоговой, то она не удаляется и выводится сообщение о критичесокй ошибке
 */
def deleteCurrentRow() {
    if (isCurrentDataRowSelected() &&
            totalRowsAliases.find { totalRowAlias ->
                totalRowAlias == currentDataRow.getAlias()
            } == null) {
        formData.dataRows.remove(currentDataRow)
    } else {
        logger.error ('Невозможно удалить фиксированную строку!')
    }
}

/***********   ДЛЯ РАБОТЫ С ОТЧЕТНЫМИ ПЕРИОДАМИ   ***********/

/**
 * возвращает диапазон Date..Date (включительно!) для текущего отчетного периода (с нарастающим итогом)
 */
def getReportPeriodRange() {
    def periodStartsDate = reportPeriodService.getStartDate(formData.reportPeriodId).getTime()
    def periodEndsDate = reportPeriodService.getEndDate(formData.reportPeriodId).getTime()

    return periodStartsDate..periodEndsDate
}