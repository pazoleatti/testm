/**
 * РНУ-64
 * @author auldanov
 *
 * @version 55
 *
 * Описание столбцов
 * 1. number - № пп
 * 2. date - Дата сделки
 * 3. part - Часть сделки
 * 4. dealingNumber - Номер сделки
 * 5. bondKind - Вид ценных бумаг
 * 6. costs - Затраты (руб.коп.)
 */

/**
 * Выполнение действий по событиям
 *
 */
switch (formDataEvent){
// Инициирование Пользователем проверки данных формы в статусе «Создана», «Подготовлена», «Утверждена», «Принята»
    case FormDataEvent.CHECK:
        //1. Логические проверки значений налоговой формы
        logicalCheck()
        //2. Проверки соответствия НСИ
        //NCICheck()
        break
// Инициирование Пользователем создания формы
    case FormDataEvent.CREATE:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при создании формы.
        //2.	Логические проверки значений налоговой.
        logicalCheck()
        //3.	Проверки соответствия НСИ.
        break
// Инициирование Пользователем перехода «Подготовить»
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Подготовлена».
        //2.	Логические проверки значений налоговой формы.
        logicalCheck()
        //3.	Проверки соответствия НСИ.
        break
// Инициирование Пользователем  выполнение перехода «Утвердить»
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Утверждена».
        //2.	Логические проверки значений налоговой формы.
        //3.	Проверки соответствия НСИ.
        break
// Инициирование Пользователем  выполнение перехода «Принять»
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Принята».
        //2.	Логические проверки значений налоговой формы.
        //3.	Проверки соответствия НСИ.
        break
// Инициирование Пользователем выполнения перехода «Отменить принятие»
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED:
        //1.	Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
        //2.	Логические проверки значений налоговой формы.
        //3.	Проверки соответствия НСИ.
        break

// Событие добавить строку
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break

// событие удалить строку
    case FormDataEvent.DELETE_ROW:
        break

    case FormDataEvent.CALCULATE:
        logicalCheck()
        fillForm()
        break
}


/**
 * Добавление новой строки
 */
def addNewRow(){
    DataRow<Cell> newRow = formData.createDataRow()
    int index // Здесь будет позиция вставки

    if (formData.dataRows.size() > 0) {
        DataRow<Cell> selectRow
        // Форма не пустая
        log("Форма не пустая")
        log("size = " + formData.dataRows.size())
        if (currentDataRow != null && formData.dataRows.indexOf(currentDataRow) != -1) {
            // Значит выбрал строку куда добавлять
            log("Строка вставки выбрана")
            log("indexOf = " + formData.dataRows.indexOf(currentDataRow))
            selectRow = currentDataRow
        } else {
            // Строку не выбрал поэтому добавляем в самый конец
            log("Строка вставки не выбрана, поставим в конец формы")
            selectRow = formData.dataRows.get(formData.dataRows.size() - 1) // Вставим в конец
        }

        int indexSelected = formData.dataRows.indexOf(selectRow)
        log("indexSelected = " + indexSelected.toString())

        // Определим индекс для выбранного места
        if (selectRow.getAlias() == null) {
            // Выбрана строка не итого
            log("Выбрана строка не итого")
            index = indexSelected // Поставим на то место новую строку
        } else {
            // Выбрана строка итого, для статических строг итого тут проще и надо фиксить под свою форму
            // Для динимаческих строк итого идём вверх пока не встретим конец формы или строку не итого
            log("Выбрана строка итого")

            for (index = indexSelected; index >= 0; index--) {
                log("loop index = " + index.toString())
                if (formData.dataRows.get(index).getAlias() == null) {
                    log("Нашел строку отличную от итого")
                    index++
                    break
                }
            }
            if (index < 0) {
                // Значит выше строки итого нет строк, добавим новую в начало
                log("выше строки итого нет строк")
                index = 0
            }
            log("result index = " + index.toString())
        }
    } else {
        // Форма пустая поэтому поставим строку в начало
        log("Форма пустая поэтому поставим строку в начало")
        index = 0
    }
    formData.dataRows.add(index, newRow)

    ['date', 'part', 'dealingNumber', 'bondKind', 'costs'].each {
        newRow.getCell(it).editable = true
        //newRow.getCell(it).setStyleAlias('Редактируемая')
    }
}

def log(String message, Object... args) {
    //logger.info(message, args)
}
/**
 * Удаление строки
 */
def deleteRow(){
    def row = (DataRow)additionalParameter
    if (!isTotalRow(row)){
        // удаление строки
        formData.deleteDataRow(row)
    }

    // пересчет номеров строк таблицы
    def i = 1;
    formData.dataRows.each{rowItem->
        rowItem.rowNumber = i++
    }
}

/**
 * Заполнение полей формы
 * 6.1.2.3	Алгоритмы заполнения полей формы
 */
def fillForm(){
    // удаляем строки итого
    formData.dataRows = formData.dataRows.findAll{ it.getAlias() != 'totalQuarter' && it.getAlias() != 'total'}

    // добавляем строки итого
    def newRowQuarter = formData.createDataRow()
    newRowQuarter.setAlias("totalQuarter")
    //2,3,4 Заполняется строкой «Итого за текущий квартал»
    newRowQuarter.getCell("fix").setColSpan(4)
    newRowQuarter.fix = "Итого за текущий квартал"
    formData.dataRows.add(formData.dataRows.size() > 0 ? formData.dataRows.size(): 0, newRowQuarter )

    // 6 графа Содержит сумму значений "графы 6" для всех строк данной таблицы, за исключением итоговых строк («Итого за текущий квартал», «Итого за текущий отчетный (налоговый) период»)
    def row6val = 0
    formData.dataRows.each{ row->
        if (!isTotalRow(row)){
            row6val += row.costs?:0
        }
    }
    newRowQuarter.costs = row6val

    // строка Итого за текущий отчетный (налоговый) период
    def newRowTotal = formData.createDataRow()
    newRowTotal.setAlias("total")
    //2,3,4 Заполняется строкой «Итого за текущий квартал»
    newRowTotal.getCell("fix").setColSpan(4)
    newRowTotal.fix = "Итого за текущий отчетный (налоговый) период"
    formData.dataRows.add(formData.dataRows.size(), newRowTotal)
    // возьмем форму за предыдущий отчетный период
    getPrevReportPeriod(formData.reportPeriodId, taxPeriodId)

    newRowTotal.costs = newRowQuarter.costs +
}

/**
 * Логические проверки
 */
def logicalCheck(){
}

/**
 * Проверка является ли строка итововой за текущий квартал
 */
def isQuarterTotal(row){
    row.getAlias()=='totalQuarter'
}

/**
 * Проверка является ли строка итововой (последняя строка)
 */
def isMainTotalRow(row){
    row.getAlias()=='total'
}

/**
 * Проверка является ли строка итововой (любой итоговой, т.е. за квартал, либо основной)
 */
def isTotalRow(row){
    return row.getAlias()=='total' || row.getAlias()=='totalQuarter'
}
