import com.aplana.sbrf.taxaccounting.model.FormDataEvent

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
        checkNSI()
        break
// Инициирование Пользователем создания формы
    case FormDataEvent.CREATE:
        checkCreation()
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
        fillForm() && logicalCheck() && checkNSI()
        break

    case FormDataEvent.COMPOSE:
        consolidation()
        fillForm()
        logicalCheck()
        // для сохранения изменений приемников
        getData(formData).commit()
        break
}

/**
 * Добавление новой строки
 */
def addNewRow(){

    def data = getData(formData)
    def newRow = formData.createDataRow()

    // Графы 3-5 Заполняется вручную
    ['balance', 'sum'].each{ column ->
        newRow.getCell(column).setEditable(true)
        newRow.getCell(column).setStyleAlias('Редактируемая')

    }
    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(isTotalRow(row) && index>0){
            row = getRows(data).get(--index)
        }
        if(index!=currentDataRow.getIndex() && !isTotalRow(getRows(data).get(index))){
            index++
        }
    }else if (getRows(data).size()>0) {
        for(int i = getRows(data).size()-1;i>=0;i--){
            def row = getRows(data).get(i)
            if(!isTotalRow(row)){
                index = getRows(data).indexOf(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
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
    def data = getData(formData)
    for (def row : getRows(data)){
        if (!isTotalRow(row)){
            def requiredColumns = ['balance', 'sum']
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }
        }
    }
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
    getRows(data).each{row->
        if (row.getAlias() != null && isTotalRow(row)) {
            totalAlases += row.getAlias()
        }
    }

    totalAlases.each{ alias ->
        data.delete(getRowByAlias(data, alias))
    }

    //выбираем код на основе балансового счета
    getRows(data).each{row->
        row.code = row.balance
        row.name = row.balance
    }
    data.save(getRows(data))

    // сортируем по кодам
    data.save(getRows(data).sort { getKnu(it.code) })
    // cумма "Итого"
    def total = 0

    // нумерация (графа 1) и посчитать "итого"
    getRows(data).eachWithIndex { row, i ->
        row.rowNumber = i + 1
        total += row.sum?:0
    }

    // посчитать "итого по коду"
    def totalRows = [:]
    def tmp = null
    def sum = 0
    getRows(data).eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.code
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != row.code) {
            def code = getKnu(tmp)
            totalRows.put(i, getNewRow(code, sum))
            sum = 0
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == getRows(data).size() - 1) {
            sum += row.sum
            def code = getKnu(row.code)
            def totalRowCode =  getNewRow(code, sum)
            totalRows.put(i + 1, totalRowCode)
            sum = 0
        }

        sum += (row.sum?:0)
        tmp = row.code

    }

    getRows(data).eachWithIndex { row, index ->
        row.rowNumber = index + 1
    }
    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        data.insert(row, index + i + 1)
        i = i + 1
    }

    // добавить строку "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    totalRow.sum = total
    setTotalStyle(totalRow)

    if (getRows(data).size()>1) {
        data.insert(totalRow, getRows(data).size() + 1)
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
            def requiredColumns = ['rowNumber', 'balance', 'code', 'name', 'sum']
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }
            //TODO Проверка на уникальность поля «№ пп»
            getRows(data).each{ rowB ->
                if(!row.equals(rowB) && row.rowNumber ==rowB.rowNumber){
                    logger.error('Нарушена уникальность номера по порядку!')
                }
            }
        }
    }

    /**
     *  Проверка итогового значения по коду для графы 5
     *  Пройдемся по всем строкам посчитаем сумму строк с одинаковым кодом и выберем значения итоговых строк
     */
    def sumAllTotalByCodeRows = 0
    def sumTotalRow = null
    //две карты: одна с реальными значениями итого по кодам, а вторая - с рассчитанными
    def totalRows = [:]
    def sumRowsByCode = [:]
    getRows(data).each{ row ->
        // если строка
        if (isTotalRowByCode(row) ){
            totalRows[row.getAlias().replace('total','')] = row.sum
        }else if(isTotalRow(row)){
            sumTotalRow = row.sum
        }else{
            def code = getKnu(row.code)
            if (sumRowsByCode[code]!=null) {
                sumRowsByCode[code]+=row.sum?:0
            } else {
                sumRowsByCode[code]=row.sum?:0
            }
            sumAllTotalByCodeRows+=(row.sum?:0)
        }
    }
    totalRows.keys.each{key->
        if(!totalRows.isEmpty() && totalRows.get(key)!=sumRowsByCode.get(key)){
            def code = totalRows.get(key)
            logger.error("Неверное итоговое значение по коду $code графы «Сумма доходов за отчётный период (руб.)»!")
        }
    }
    if(sumTotalRow!=null && sumTotalRow!=sumAllTotalByCodeRows){
        logger.error("Неверное итоговое значение графы «Сумма доходов за отчётный период (руб.)»!")
    }
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    def cell
    columns.each {
        cell = row.getCell(it)
        if (cell.getValue() == null || row.getCell(it).getValue() == '') {
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
            index = getRows(getData(formData)).indexOf(row) + 1
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

def checkNSI(){
    def data = getData(formData)
    getRows(data).each{row->
        if (!isTotalRow(row)) {
            def index = row.rowNumber
            def errorMsg
            boolean npp
            if (index != null) {
                npp = true
                errorMsg = "В строке \"№ пп\" равной $index "
            } else {
                npp = false
                index = getRows(getData(formData)).indexOf(row) + 1
                errorMsg = "В строке $index "
            }

            if (row.code!=null && getKnu(row.code)==null){
                logger.warn(errorMsg + 'код налогового учета в справочнике отсутствует!')
            }
            if (row.balance!=null && getBalance(row.balance)==null){
                logger.warn(errorMsg + 'код налогового учета в справочнике отсутствует!')
            }
            def start = reportPeriodService.getStartDate(formData.reportPeriodId)
            def end = reportPeriodService.getEndDate(formData.reportPeriodId)
            if (row.code!=null && isKnuDate(row.code,start.getTime()) && isKnuDate(row.code,end.getTime())){
                if (npp) {
                    logger.error("Операция в строке \"№ пп\" равной $index в РНУ не учитывается!")
                } else {
                    logger.error("Операция в строке $index в РНУ не учитывается!")
                }
            }
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)
    // удалить все строки и собрать из источников их строки
    data.clear()

    departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        def found = false
                        getRows(data).each{ rowB->
                            // в случае совпадения строк из разных источников,
                            // необходимо использовать суммирование значений ячеек строк форм-источников для «графы 5»
                            if(row.code==rowB.code && row.balance==rowB.balance && row.name==rowB.name){
                                rowB.sum+=row.sum
                                found = true
                            }
                        }
                        if (found) {
                            data.save(getRows(data))
                        } else {
                            data.insert(row,getRows(data).size()+1)
                        }
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверка является ли строка итоговой по коду
 */
def isTotalRowByCode(row){
    row.getAlias()==~/total\d+/
}

/**
 * Проверка является ли строка итоговой (последняя строка)
 */
def isMainTotalRow(row){
    row.getAlias()==~/total/
}

/**
 * Проверка является ли строка итоговой (любой итоговой, т.е. по коду, либо основной)
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
 * Получить строки.
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
 * Получить новую строку.
 */
def getNewRow(def alias, def sum) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    newRow.sum = sum
    newRow.fix = 'Итого по КНУ ' + alias
    newRow.getCell('fix').colSpan = 2
    setTotalStyle(newRow)
    return newRow
}

def getKnu(def code) {
    return refBookService.getStringValue(28,code,'CODE')
}

def isKnuDate(def code, def date) {
    def refDataProvider =  refBookFactory.getDataProvider(28);
    def res = refDataProvider.getRecords(date, null, null, null);
    res.getRecords().each{record->
        if (record.CODE==code){
            return true
        }
    }
    return false
}

def getBalance(def balance) {
    return refBookService.getStringValue(28,balance,'NUMBER')
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'fix', 'balance', 'code', 'name', 'sum'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
        row.getCell(it).setEditable(false)
    }
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}