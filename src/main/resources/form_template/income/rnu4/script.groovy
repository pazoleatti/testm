/**
 * РНУ-4
 * @author auldanov
 * TODO
 *  1. При добавлении строки к редактируемым ячейкам добавлять стиль
 *  2. Проставление номеров строк, нужно определиться с сервисом который будет возвращать номер строки
 *
 * @version 55
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
        //logicalCheck()
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
        deleteRow()
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

    def data = getData(formData)
    def newRow = formData.createDataRow()


    // Графы 2-5 Заполняется вручную
    ['code', 'balance', 'name', 'sum'].each{ column ->
        newRow.getCell(column).setEditable(true)
    }
    insert(data, newRow)

    // проставление номеров строк
    def i = 1;
    getRows(data).each{ row->
        row.rowNumber = i++
    }
    save(data)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    def data = getData(formData)
    data.delete(currentDataRow)
}

/**
 * Заполнение полей формы
 * 6.1.2.3	Алгоритмы заполнения полей формы
 */
def fillForm(){
    /**
     * Табл. 3 Алгоритмы заполнения полей формы «Регистр налогового учёта «доходы»»
     * реализации не требудет т.к. номер у нас проставляется при добавлении строки
     */

    /**
     * Табл. 4 Алгоритмы заполнения строки «Итого по коду»  формы «Регистр налогового учёта «доходы»»
     * для этого отсортируем данные в таблице по коду и проставим строки с сумоой по каждому блоку с кодом
     */

    /**
     * Удалим все строки и итого
     * для этого соберем алиасы, затем удалим все
     */
    def totalAlases = []
    def data = getData(formData)
    getRows(data).each{row->
        if (row.getAlias() != null && isTotalRow(row)) {
            totalAlases += row.getAlias()
        }
    }

    totalAlases.each{ alias ->
        data.delete(getRowByAlias(data, alias))
    }

    // сортируем по кодам
    getRows(data).sort { it.code }
    // сумма блока по коду
    def summByCode = 0
    // итого по таблице
    def total = 0
    // индекс строки
    def i = 0;

    while(getRows(data).size() > i){
        // текущая строка
        def row = getRows(data)[i]
        // начиная со второй строки, если код предыдущей и этой различается то вставляем итого
        if((i != 0 && row.code != getRows(data)[i-1].code)){
            def newRow = formData.createDataRow()
            newRow.setAlias('total' + row.code)
            newRow.sum = summByCode
            summByCode = 0
            //newRow.code = 'Итого по коду' TODO (Aydar Kadyrgulov)
            newRow.balance = getRows(data)[i-1].code.toString()
            logger.info("i = "+(i+1))
            data.insert(newRow, i+1)
            i++
        }

        // к сумме добавляем тукущее значение
        summByCode += row.sum ?:0
        total += row.sum ?:0

        // добавляем последняю строку по группам
        if (i + 1 == getRows(data).size() && !isTotalRow(getRows(data)[i])){

            def newRow = formData.createDataRow()
            newRow.setAlias('total' + row.code)
            newRow.sum = summByCode
            newRow.fix = 'Итого по КНУ'
            newRow.getCell('fix').colSpan = 2
            newRow.balance = getRows(data)[i-1].code
            data.insert(newRow, i + 1)
            // пропуск текущей добавленной строки
            i++
        }
        // переход к следующей строке
        i++
    }

    // добавляем итого
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.sum = total
    newRow.fix = 'Итого'
    newRow.getCell('fix').colSpan = 2
    data.insert(newRow, i + 1)

}

/**
 * Функция возвращает суммы значений по кодам
 * возвращет Мар [index->[code, sumByCode]]
 */
def sumByCode(){
    while(getData(formData).size() > i){
        // текущая строка
        def row = getData(formData)[i]
        // начиная со второй строки, если код предыдущей и этой различается то вставляем итого
        if((i != 0 && row.code != getData(formData)[i-1].code)){
            def newRow = formData.appendDataRow(i, "total"+row.code)
            newRow.sum = summByCode
            summByCode = 0
            newRow.code = 'Итого по коду'
            newRow.balance = getData(formData)[i-1].code
            i++
        }

        // к сумме добавляем тукущее значени
        summByCode += row.sum ?:0
        total += row.sum ?:0

        // добавляем последняю строку по группам
        if (i + 1 == getData(formData).size() && !isTotalRow(getData(formData)[i])){
            def newRow = formData.appendDataRow(i+1, "total"+row.code)
            newRow.sum = summByCode
            newRow.code = 'Итого по коду'
            newRow.balance = getData(formData)[i-1].code
            // пропуск текущей добавленной строки
            i++
        }
        // переход к следующей строке
        i++
    }
}


/**
 * Логические проверки
 */
def logicalCheck(){

    /**
     * Проверка на заполнение поля «<Наименование поля>»
     * Обязательность заполнения поля графы 1-5
     */
    def data = getData(formData)
    getRows(data).each{ row ->
        if (!isTotalRow(row)){
            ['rowNumber', 'code', 'balance', 'name', 'sum'].each{ alias ->
                if (row[alias] == null || row[alias] == '')
                    logger.error('Поле «'+row.getCell(alias).getColumn().getName()+'» не заполнено! Строка №пп - '+row.rowNumber)
            }
        }
    }

    // TODO нужно реализовать Проверка на уникальность поля «№ пп»

    /**
     *  Проверка итогового значения по коду для графы 5
     */



    /**
     *  Проверка итогового значения графы 5
     *  Пройдемся по всем строкам посчитаем сумму строк с одинаковым коодом и выберем значения итоговых строк
     */
    def sumAllTotalByCodeRows = 0
    getRows(data).each{ row ->
        def totalRows = [:]
        def sumRowsByCode = [:]

        // если строка
        if (isTotalRowByCode(row) ){
            totalRows[row.code] = row.code
        }
    }


}

/**
 * Проверка является ли строка итововой по коду
 */
def isTotalRowByCode(row){
    row.getAlias()==~/total\d{1}/
}

/**
 * Проверка является ли строка итововой (последняя строка)
 */
def isMainTotalRow(row){
    row.getAlias()==~/total/
}

/**
 * Проверка является ли строка итововой (любой итоговой, т.е. по коду, либо основной)
 */
def isTotalRow(row){
    row.getAlias()==~/total\d*/
}

//////////////////////////////////////////////////////////////////////

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    return data.getDataRow(getRows(data), alias)
}

/**
 * Получить индекс строки по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 */
def getIndexByAlias(def data, def alias) {
    return data.getDataRowIndex(getRows(data), alias)
}

/**
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void save(def data) {
    data.save(getRows(data))
}

/**
 * Удалить строку из нф
 *
 * @param data данные нф (helper)
 * @param row строка для удаления
 */
void deleteRow(def data, def row) {
    data.delete(row)
}

/**
 * Вставить новыую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}