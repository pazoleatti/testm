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
    def newRow = formData.createDataRow()
    formData.dataRows.add(formData.dataRows.size() > 0 ? formData.dataRows.size() - 1 : 0, newRow )

    // проставление номеров строк
    def i = 1;
    formData.dataRows.each{ row->
        row.rowNumber = i++
    }

    // Графы 2-5 Заполняется вручную
    ['code', 'balance', 'name', 'sum'].each{ column ->
        newRow.getCell(column).setEditable(true)
    }
}

/**
 * Удаление строки
 */
def deleteRow(){
    def row = (DataRow)additionalParameter
    if (!(row.getAlias() in ['totalByCode', 'total'])){
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
    formData.dataRows.each{row->
        if (row.getAlias() != null && isTotalRow(row)) {
            totalAlases += row.getAlias()
        }
    }

    totalAlases.each{ alias ->
        formData.deleteDataRow(formData.getDataRow(alias))
    }


    // сортируем по кодам
    formData.dataRows.sort { it.code }
    // сумма блока по коду
    def summByCode = 0
    // итого по таблице
    def total = 0
    // индекс строки
    def i = 0;

    while(formData.dataRows.size() > i){
        // текущая строка
        def row = formData.dataRows[i]
        // начиная со второй строки, если код предыдущей и этой различается то вставляем итого
        if((i != 0 && row.code != formData.dataRows[i-1].code)){
            def newRow = formData.appendDataRow(i, "total"+row.code)
            newRow.sum = summByCode
            summByCode = 0
            newRow.code = 'Итого по коду'
            newRow.balance = formData.dataRows[i-1].code
            i++
        }

        // к сумме добавляем тукущее значени
        summByCode += row.sum ?:0
        total += row.sum ?:0

        // добавляем последняю строку по группам
        if (i + 1 == formData.dataRows.size() && !isTotalRow(formData.dataRows[i])){
            def newRow = formData.appendDataRow(i+1, "total"+row.code)
            newRow.sum = summByCode
            newRow.code = 'Итого по коду'
            newRow.balance = formData.dataRows[i-1].code
            // пропуск текущей добавленной строки
            i++
        }
        // переход к следующей строке
        i++
    }

    // добавляем итого
    def newRow = formData.appendDataRow('total')
    newRow.sum = total
    newRow.code = 'Итого'

}

/**
 * Функция возвращает суммы значений по кодам
 * возвращет Мар [index->[code, sumByCode]]
 */
def sumByCode(){
    while(formData.dataRows.size() > i){
        // текущая строка
        def row = formData.dataRows[i]
        // начиная со второй строки, если код предыдущей и этой различается то вставляем итого
        if((i != 0 && row.code != formData.dataRows[i-1].code)){
            def newRow = formData.appendDataRow(i, "total"+row.code)
            newRow.sum = summByCode
            summByCode = 0
            newRow.code = 'Итого по коду'
            newRow.balance = formData.dataRows[i-1].code
            i++
        }

        // к сумме добавляем тукущее значени
        summByCode += row.sum ?:0
        total += row.sum ?:0

        // добавляем последняю строку по группам
        if (i + 1 == formData.dataRows.size() && !isTotalRow(formData.dataRows[i])){
            def newRow = formData.appendDataRow(i+1, "total"+row.code)
            newRow.sum = summByCode
            newRow.code = 'Итого по коду'
            newRow.balance = formData.dataRows[i-1].code
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
    formData.dataRows.each{ row ->
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
    formData.dataRows.each{ row ->
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
