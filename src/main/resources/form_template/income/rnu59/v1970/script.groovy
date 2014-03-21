package form_template.income.rnu59.v1970

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * (РНУ-59) Регистр налогового учёта закрытых сделок РЕПО с обязательством продажи по 2-й части
 * formTemplateId=350
 *
 * @author auldanov
 * @version 55
 *
 * TODO:
 *      - убрать loggerError и заменить на logger.error
 *      - этой формы нет в базе
 *
 * Столбцы
 * 1. Номер сделки первая часть / вторая часть - tradeNumber
 * 2. Наименование ценной бумаги - securityName
 * 3. Код валюты - currencyCode Справочник
 * 4. Номинальная стоимость ценных бумаг (ед. вал.) - nominalPrice
 * 5. Дата первой части РЕПО - part1REPODate
 * 6. Дата второй части РЕПО - part2REPODate
 * 7. Стоимость приобретения, в т.ч. НКД, по первой части РЕПО (руб.коп.) - acquisitionPrice
 * 8. Стоимость реализации, в т.ч. НКД, по второй части РЕПО (руб.коп.) - salePrice
 * 9. Доходы (+) по сделке РЕПО (руб.коп.) - income
 * 10. Расходы (-) по сделке РЕПО (руб.коп.) - outcome
 * 11. Ставка Банка России (%) - rateBR
 * 12. Расходы по сделке РЕПО, рассчитанные с учётом ст. 269 НК РФ (руб.коп.) - outcome269st
 * 13. Расходы по сделке РЕПО, учитываемые для целей налогообложения (руб.коп.) - outcomeTax
 */

/**
 * Выполнение действий по событиям
 *
 */
switch (formDataEvent){
// Инициирование Пользователем проверки данных формы в статусе «Создана», «Подготовлена», «Утверждена», «Принята»
    case FormDataEvent.CHECK:
        //1. Логические проверки значений налоговой формы
        //2. Проверки соответствия НСИ
        logicalCheck()
        break
// Инициирование Пользователем создания формы
    case FormDataEvent.CREATE:
        //1.    Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при создании формы.
        checkCreation()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        //1.    Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе в статус «Подготовлена».
        //2.    Логические проверки значений налоговой формы.
        //3.    Проверки соответствия НСИ.
        logicalCheck()
        break
// Инициирование Пользователем выполнения перехода «Отменить принятие»
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED:
        //1.    Проверка наличия и статуса формы, консолидирующей данные текущей налоговой формы, при переходе «Отменить принятие».
        //2.    Логические проверки значений налоговой формы.
        //3.    Проверки соответствия НСИ.
        logicalCheck()
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
        fillForm()
        !hasError() && logicalCheck()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        fillForm()
        !hasError() && logicalCheck()
        break
    case FormDataEvent.IMPORT :
        importData()
        if (!hasError()) {
            fillForm()
        }
        break
    case FormDataEvent.MIGRATION :
        importData()
        if (!hasError()) {
            def total = getCalcTotalRow()
            def data = getData(formData)
            insert(data, total)
        }
        break
}

@Field
def endDate = null

/**
 * Добавление новой строки
 */
def addNewRow(){
    def newRow = formData.createDataRow()
    def data = getData(formData)

    // Графы 1-10 Заполняется вручную
    ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice'].each{ column ->
        newRow.getCell(column).setEditable(true)
        newRow.getCell(column).setStyleAlias('Редактируемая')
    }
    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(row.getAlias()!=null && index>0){
            row = getRows(data).get(--index)
        }
        if(index!=currentDataRow.getIndex() && getRows(data).get(index).getAlias()==null){
            index++
        }
    }else if (getRows(data).size()>0) {
        for(int i = getRows(data).size()-1;i>=0;i--){
            def row = getRows(data).get(i)
            if(row.getAlias()==null){
                index = getRows(data).indexOf(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
}

/**
 * Удаление строки
 */
def deleteRow(){
    //def row = (DataRow)additionalParameter
    def row = currentDataRow
    if (!(row.getAlias() in ['totalByCode', 'total'])){
        // удаление строки
        getData(formData).delete(row)
    }

// пересчет номеров строк таблицы
//    def i = 1;
//    getData(formData).getAllCached().each{rowItem->
//        rowItem.rowNumber = i++
//    }
}

/**
 * Заполнение полей формы
 * 6.40.2.3 Алгоритмы заполнения полей формы
 */
def fillForm(){

    def data = getData(formData)

    // Проверка обязательных полей. Cписок проверяемых столбцов (графа 1..8)
    def requiredColumns = ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice']
    for (def row : getRows(data)) {
        if (!isTotalRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return
        }
    }

    // удаляем строку итого
    for(def i=0;i<data.getAllCached().size();i++){
        def row = data.getAllCached().get(i)
        if (row.getAlias() == "total") {
            data.delete(row)
        }
    }

    if (formDataEvent != FormDataEvent.IMPORT) {
        sort(data)
    }

    data.getAllCached().each{ DataRow row ->
        /**
         * Табл. 199 Алгоритмы заполнения полей формы «Регистр налогового учёта закрытых сделок РЕПО с обязательством продажи по 2-й части»
         */
        if (formDataEvent != FormDataEvent.IMPORT) {
            // графа 9, 10
            // A=«графа8» - «графа7»
            BigDecimal a = (row.salePrice?:0) - (row.acquisitionPrice?:0)
            // B=ОКРУГЛ(A;2),
            BigDecimal b = roundTo2(a)
            // C= ОКРУГЛ(ABS(A);2),
            BigDecimal c = roundTo2(a.abs())
            /**
             *    Если  .A>0, то
             «графа 9» = B
             «графа 10» = 0
             Иначе Если  A<0
             «графа 9» = 0
             «графа 10» = С
             Иначе
             «графа 9»= «графа 10» = 0
             */
            if (a.compareTo(0) > 0){
                row.income = b
                row.outcome = 0
            } else if (a.compareTo(0) < 0){
                row.income = 0
                row.outcome = c
            }   else{
                row.income = 0
                row.outcome = 0
            }
        }

        // Графа 11
        row.rateBR = roundTo2(calculateColumn11(row,row.part2REPODate))

        // графа 12
        row.outcome269st = roundTo2(calculateColumn12(row))

        // Графа 13
        row.outcomeTax = roundTo2(calculateColumn13(row))
    }
    data.save(data.getAllCached())

    if (data.getAllCached().size()>0) {
        // строка для Итого
        def totalRow = getCalcTotalRow()
        insert(data, totalRow)
    }
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/**
 * 6.40.2.4.1 Логические проверки
 */
def logicalCheck(){
    def data = getData(formData)

    // Проверка обязательных полей. Cписок проверяемых столбцов (графа 1..13)
    def requiredColumns = ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax']
    for (def row : getRows(data)) {
        if (!isTotalRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    def nominalPrice = 0
    def acquisitionPrice = 0
    def salePrice = 0
    def income = 0
    def outcome = 0
    def outcome269st = 0
    def outcomeTax = 0

    def dFrom = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).getTime()
    def dTo = getReportPeriodEndDate()
    def reportDay = reportPeriodService.getReportDate(formData.reportPeriodId)?.time

    data.getAllCached().each{ row ->
        if (!isTotalRow(row)) {
            // Обязательность заполнения поля графы 12 и 13. Текст ошибки - Поле “Наименование поля” не заполнено!
            if(!checkRequiredColumns(row,['outcome269st', 'outcomeTax'])){
                return false
            }

            def index = row.tradeNumber
            def errorMsg
            if (index!=null && index!='') {
                errorMsg = "В строке \"Номер сделки\" равной $index "
            } else {
                index = row.getIndex()
                errorMsg = "В строке $index "
            }

            // графа 5 заполнена и «графа 5» ? «отчётная дата». Текст ошибки - Неверно указана дата первой части сделки! SBRFACCTAX-2575
            if (!(row.part1REPODate != null && (row.part1REPODate.compareTo(reportDay)  <= 0))) {
                loggerError(errorMsg + 'неверно указана дата первой части сделки!')//TODO вернуть error
                return false
            }

            // графа 6 заполнена и графа 6 в рамках отчётного периода. Текст ошибки - Неверно указана дата второй части сделки!
            if (row.part2REPODate != null && (row.part2REPODate < dFrom || row.part2REPODate > dTo)) {
                loggerError(errorMsg + 'неверно указана дата второй части сделки!')//TODO вернуть error
                return false
            }

            // если«графа 9» = 0 ИЛИ  «графа 10» = 0. = Задвоение финансового результата!
            if (!(row.income == 0 || row.outcome == 0)){
                loggerError(errorMsg + "задвоение финансового результата!")//TODO вернуть error
                return false
            }

            // если «графа 10» = 0, то «графа 12» = 0 и «графа 13» = 0
            if (row.outcome == 0 && !(row.outcome269st == 0 && row.outcomeTax == 0)){
                loggerError(errorMsg + "задвоение финансового результата!")//TODO вернуть error
                return false
            }

            //  «графа 9» = «графа 8» - «графа 7», при условии («графа 8» - «графа 7») > 0. = Неверно определены доходы
            def price = row.salePrice?:0
            def acqPrice = row.acquisitionPrice?:0
            if (price - acqPrice > 0 && !(price - acqPrice == row.income)){
                logger.warn(errorMsg + 'неверно определены доходы')
            }

            // «графа 10» =|«графа 8» - «графа 7»|, при условии («графа 8» - «графа 7») < 0.  = Неверно определены расходы
            if ((price - acqPrice) < 0 && !(row.outcome == (price - acqPrice).abs())){
                logger.warn(errorMsg + 'неверно определены расходы')
            }

            // Арифметическая проверка графы 11
            def col11 = roundTo2(calculateColumn11(row,row.part2REPODate))
            if (col11 != null && col11 != row.rateBR){
                loggerError(errorMsg + 'неверно рассчитана графа «Ставка Банка России (%%)»!')//TODO вернуть error
                return false
            }

            // Арифметическая проверка графы 12
            def col12 = roundTo2(calculateColumn12(row))
            if (col12 != null && col12 != row.outcome269st){
                loggerError(errorMsg + 'неверно рассчитана графа «Расходы по сделке РЕПО, рассчитанные с учётом ст. 269 НК РФ (руб.коп.)»!')//TODO вернуть error
            }

            // Арифметическая проверка графы 13
            def col13 = roundTo2(calculateColumn13(row))
            if (col13 != null && col13 != row.outcomeTax){
                loggerError(errorMsg + 'неверно рассчитана графа «Расходы по сделке РЕПО, учитываемые для целей налогообложения (руб.коп.)»!')//TODO вернуть error
                return false
            }
            // экономим на итерациях, подсчитаем сумму для граф 4,7-10, 12-13, суммы нужны для проверок
            nominalPrice += row.nominalPrice ?:0
            acquisitionPrice += row.acquisitionPrice ?:0
            salePrice += row.salePrice ?:0
            income += row.income ?:0
            outcome += row.outcome ?:0
            outcome269st += row.outcome269st ?:0
            outcomeTax += row.outcomeTax ?:0
        }
    }

    // Проверка итоговых значений по всей форме
    for(def dataRow:data.getAllCached()){
        if (isTotalRow(dataRow)){
            def totalRow = data.getDataRow(data.getAllCached(),"total")
            if (totalRow != null && totalRow.nominalPrice != nominalPrice ||
                    totalRow.acquisitionPrice != acquisitionPrice ||
                    totalRow.salePrice != salePrice ||
                    totalRow.income != income ||
                    totalRow.outcome != outcome ||
                    totalRow.outcome269st != outcome269st ||
                    totalRow.outcomeTax != outcomeTax){
                loggerError('Итоговые значения рассчитаны неверно!')//TODO вернуть error
                return false
            }
        }
    }
    return true
}

/**
 * Проверка является ли строка итоговой (любой итоговой, т.е. по коду, либо основной)
 */
def isTotalRow(row){
    row.getAlias()=='total'
}

/**
 * Метод возвращает значение для графы 11
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 * @param rateDate
 */
def calculateColumn11(DataRow row, def rateDate){
    def currency = getCurrency(row.currencyCode)
    def rate = getRate(rateDate)
    // Если «графа 10» = 0, то « графа 11» не заполняется; && Если «графа 3» не заполнена, то « графа 11» не заполняется
    if (!isTotalRow(row) && row.outcome != 0 && row.currencyCode != null){
        // Если «графа 3» = 810, то «графа 11» = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на дату «графа 6»,
        if (currency == '810')    {
            return rate
        } else{ // Если «графа 3» ? 810), то
            // Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009 (включительно), то «графа 11» = 22;
            if (inPeriod(rateDate, '01.09.2008', '31.12.2009')){
                return 22
            } else if (inPeriod(rateDate, '01.01.2011', '31.12.2012')){
                // Если «графа 6» принадлежит периоду с 01.01.2011 по 31.12.2012 (включительно), то
                // графа 11 = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на дату «графа 6»;
                return rate
            } else{
                //Если  «графа 6» не принадлежит отчётным периодам с 01.09.2008 по 31.12.2009 (включительно), с 01.01.2011 по 31.12.2012 (включительно)),
                //то  «графа 11» = 15.
                return 15
            }
        }
    }
}

/**
 * Количество дней в году за который делаем
 * @return
 */
int getCountDaysOfYear() {
    Calendar periodStartDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId)
    return countDaysOfYear = (new GregorianCalendar()).isLeapYear(periodStartDate.get(Calendar.YEAR)) ? 366 : 365
}

/**
 * Метод возвращает значение для графы 12
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверках
 * @param row
 */
def calculateColumn12(DataRow row){
    if(row.rateBR == null){
        return 0
    }
    Date date01_09_2008 = new Date(1220205600000)       // 1220227200000 - 01.09.2008 00:00 GMT
    Date date31_12_2009 = new Date(1262199600000)       // 1262217600000 - 31.12.2009 00:00 GMT
    Date date01_01_2011 = new Date(1293822000000)       // 1293840000000 - 01.01.2011 00:00 GMT
    Date date31_12_2012 = new Date(1356894000000)       // 1356912000000 - 31.12.2012 00:00 GMT
    Date date01_01_2010 = new Date(1262286000000)       // 1262282400000 - 01.01.2010 00:00 GMT
    Date date30_06_2010 = new Date(1277834400000)       // 1277834400000 - 30.06.2010 00:00 GMT
    Date date01_11_2009 = new Date(1257015600000)       // 1257012000000 - 01.11.2009 00:00 GMT

    def currencyCode = getCurrency(row.currencyCode)
    def coefficient = 0
    // Если «графа 10» > 0 И«графа 3» = 810, то:
    if (row.outcome > 0 && currencyCode == '810') {
        if (date01_09_2008 <= row.part2REPODate && row.part2REPODate <= date31_12_2009) {
            // 1.   Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009, то: 1,5
            coefficient = 1.5
        } else if (date01_01_2010 <= row.part2REPODate && row.part2REPODate <= date30_06_2010 && row.part1REPODate < date01_11_2009) {
            // 2.   Если «графа 6» принадлежит периоду с 01.01.2010 по 30.06.2010 И «графа 5» < 01.11.2009, то: 2
            coefficient = 2
        } else if (date01_01_2010 <= row.part2REPODate && row.part2REPODate <= date31_12_2012) {
            // 3.   Если «графа 6» принадлежит периоду с 01.01.2010 по 31.12.2012, то: 1,8
            coefficient = 1.8
        } else {
            // 4.   Иначе: 1,1
            coefficient = 1.1
        }
    } else if (row.outcome > 0 && currencyCode != '810') { // Если «графа 10» > 0 И «графа 3» ? 810, то:
        if (date01_01_2011 <= row.part2REPODate && row.part2REPODate <= date31_12_2012) {
            //Если «графа 6» принадлежит периоду с 01.01.2011 по 31.12.2012, то: 0,8
            coefficient = 0.8
        } else {
            coefficient = 1
        }
    } else if (row.outcome == 0) {
        //  Если «графа 10» = 0, то «графа 12» = 0
        return 0
    }
    def diff65 = row.part2REPODate - row.part1REPODate
    diff65 = diff65 == 0 ? 1 : diff65
    return (row.acquisitionPrice * row.rateBR * coefficient) * (diff65 / countDaysOfYear) / 100
}

/**
 * Метод возвращает значение для графы 13
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 */
def calculateColumn13(DataRow row){
    def tmp = null
    if (row.outcome > 0){
        // Если «графа 10» > 0, то:
        if (row.outcome <= row.outcome269st){
            // Если «графа 10» ? «графа 12», то:  «графа 13» = «графа 10»
            tmp = row.outcome
        }else{
            // 2.   Если «графа 10» > «графа 12», то: «графа 13» = «графа 12»
            tmp = row.outcome269st
        }
    }else if (row.outcome == 0){
        // Если «графа 10» = 0, то «графа 13» = 0
        tmp = 0
    }
    return tmp
}

/**
 * Консолидация.
 */
void consolidation() {
    // удалить все строки и собрать из источников их строки
    def data = getData(formData)
    data.clear()
    def newRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getData(source).getAllCached().each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        newRows.add(row)
                    }
                }
            }
        }
    }
    if (!newRows.isEmpty()) {
        data.insert(newRows, 1)
        sort(data)
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

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
 * Получить строки формы.
 *
 * @param formData форма
 */
def getRows(def data) {
    return data.getAllCached()
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

/**
 * Хелпер для округления чисел
 * @param value
 * @return
 */
BigDecimal roundTo2(BigDecimal value) {
    if (value != null) {
        return value.setScale(2, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

/**
 * Проверить попадает ли указанная дата в период
 */
def inPeriod(def date, def from, to) {
    if (date == null) {
        return false
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def dateFrom = format.parse(from)
    def dateTo = format.parse(to)
    return (dateFrom < date && date <= dateTo)
}

/**
 * Получить ставку рефинансирования ЦБ РФ
 * @param date
 */
def getRate(def date) {
    if (date != null) {
        def res = refBookFactory.getDataProvider(23).getRecords(date, null, null, null);
        if (res.getRecords() != null && res.getRecords().size() > 0)
            return res.getRecords().get(0).RATE.getNumberValue()
    }
    return null
}

/**
 * Получить цифровой код валюты
 */
def getCurrency(def currencyCode) {
    return refBookService.getStringValue(15,currencyCode,'CODE')

}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice',
            'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice',
            'income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получение импортируемых данных.
 * Транспортный файл формата xml.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }

    String charset = ""
    // TODO в дальнейшем убрать возможность загружать RNU для импорта!
    if (formDataEvent == FormDataEvent.IMPORT && fileName.contains('.xml') ||
            formDataEvent == FormDataEvent.MIGRATION && fileName.contains('.xml')) {
        if (!fileName.contains('.xml')) {
            logger.error('Формат файла должен быть *.xml')
            return
        }
    } else {
        if (!fileName.contains('.r')) {
            logger.error('Формат файла должен быть *.rnu')
            return
        }
        charset = 'cp866'
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    def xmlString = importService.getData(is, fileName, charset)
    if (xmlString == null || xmlString == '') {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    try {
        // добавить данные в форму
        def totalLoad = addData(xml, fileName)

        // рассчитать, проверить и сравнить итоги
        if (formDataEvent == FormDataEvent.IMPORT) {
            if (totalLoad != null) {
                checkTotalRow(totalLoad)
            } else {
                logger.error("Нет итоговой строки.")
            }
        }
    } catch (Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml, def fileName) {
    def tmp
    def index
    def data = getData(formData)
    data.clear()
    def cache = [:]
    def date = getReportPeriodEndDate()
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def newRows = []

    def records
    def totalRecords
    def type
    if (formDataEvent == FormDataEvent.MIGRATION ||
            formDataEvent == FormDataEvent.IMPORT && fileName.contains('.xml')) {
        records = xml.exemplar.table.detail.record
        totalRecords = xml.exemplar.table.total.record
        type = 1 // XML
    } else {
        records = xml.row
        totalRecords = xml.rowTotal
        type = 2 // RNU
    }

    for (def row : records) {
        index = 0

        def newRow = getNewRow()

        // графа 1
        newRow.tradeNumber = getCellValue(row, index, type, true)
        index++

        // графа 2
        newRow.securityName = getCellValue(row, index, type, true)
        index++

        // графа 3 - справочник 15 "Общероссийский классификатор валют"
        tmp = null
        if (row.field[index].text() != null && getCellValue(row, index, type, true).trim() != '') {
            tmp = getRecordId(15, 'CODE', getCellValue(row, index, type, true), date, cache)
        }
        newRow.currencyCode = tmp
        index++

        // графа 4
        newRow.nominalPrice = getNumber(getCellValue(row, index, type))
        index++

        // в транспортном файле(XML) порядок колонок по другому (графа 1, 2, 3, 4, 7, 8, 5, 6, 9, 10, 11, 12, 13)
        if (type==1) {
            // графа 7
            newRow.acquisitionPrice = getNumber(getCellValue(row, index, type))
            index++

            // графа 8
            newRow.salePrice = getNumber(getCellValue(row, index, type))
            index++

            // графа 5
            newRow.part1REPODate = getDate(getCellValue(row, index, type), format)
            index++

            // графа 6
            newRow.part2REPODate = getDate(getCellValue(row, index, type), format)
            index++
        } else {
            // графа 5
            newRow.part1REPODate = getDate(getCellValue(row, index, type), format)
            index++

            // графа 6
            newRow.part2REPODate = getDate(getCellValue(row, index, type), format)
            index++

            // графа 7
            newRow.acquisitionPrice = getNumber(getCellValue(row, index, type))
            index++

            // графа 8
            newRow.salePrice = getNumber(getCellValue(row, index, type))
            index++
        }

        if (formDataEvent == FormDataEvent.MIGRATION) {

            // графа 10
            newRow.outcome = getNumber(getCellValue(row, index, type))
            index++

            // графа 9
            newRow.income = getNumber(getCellValue(row, index, type))
            index++

        } else {
            // графа 9
            newRow.income = getNumber(getCellValue(row, index, type))
            index++

            // графа 10
            newRow.outcome = getNumber(getCellValue(row, index, type))
            index++
        }

        // графа 11
        newRow.rateBR= getNumber(getCellValue(row, index, type))
        index++

        // графа 12
        newRow.outcome269st = getNumber(getCellValue(row, index, type))
        index++

        // графа 13
        newRow.outcomeTax = getNumber(getCellValue(row, index, type))

        newRows.add(newRow)
    }
    data.insert(newRows, 1)

    // итоговая строка
    if (totalRecords.size() >= 1) {
        def row = totalRecords[0]
        def totalRow = formData.createDataRow()

        // графа 4
        totalRow.nominalPrice = getNumber(getCellValue(row, 3, type))

        // в транспортном файле(XML) порядок колонок по другому (графа 1, 2, 3, 4, 7, 8, 5, 6, 9, 10, 11, 12, 13)
        if (type==1) {
            // графа 7
            totalRow.acquisitionPrice = getNumber(getCellValue(row, 4, type))

            // графа 8
            totalRow.salePrice = getNumber(getCellValue(row, 5, type))
        } else {
            // графа 7
            totalRow.acquisitionPrice = getNumber(getCellValue(row, 6, type))

            // графа 8
            totalRow.salePrice = getNumber(getCellValue(row, 7, type))
        }

        if (formDataEvent == FormDataEvent.MIGRATION) {

            // графа 9
            totalRow.income = getNumber(getCellValue(row, 9, type))

            // графа 10
            totalRow.outcome = getNumber(getCellValue(row, 8, type))
        } else {
            // графа 9
            totalRow.income = getNumber(getCellValue(row, 8, type))

            // графа 10
            totalRow.outcome = getNumber(getCellValue(row, 9, type))
        }

        // графа 12
        totalRow.outcome269st = getNumber(getCellValue(row, 11, type))

        // графа 13
        totalRow.outcomeTax = getNumber(getCellValue(row, 12, type))

        return totalRow
    } else {
        return null
    }
}

// для получения данных из RNU или XML
String getCellValue(def row, int index, def type, boolean isTextXml = false){
    if (type==1) {
        if (isTextXml) {
            return row.field[index].text()
        } else {
            return row.field[index].@value.text()
        }
    }
    return row.cell[index+1].text()
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    try {
        return new BigDecimal(tmp)
    } catch (Exception e) {
        throw new Exception("Значение \"$value\" не может быть преобразовано в число. " + e.message)
    }
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value, def format) {
    if (isEmpty(value)) {
        return null
    }
    try {
        return format.parse(value)
    } catch (Exception e) {
        throw new Exception("Значение \"$value\" не может быть преобразовано в дату. " + e.message)
    }
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

/**
 * Получить новую строку с заданными стилями.
 */
def getNewRow() {
    def row = formData.createDataRow()

    // Графы 1-10 Заполняется вручную
    ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice'].each{ column ->
        row.getCell(column).setEditable(true)
        row.getCell(column).setStyleAlias('Редактируемая')
    }
    return row
}

/**
 * Рассчитать, проверить и сравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def totalColumns = [4 : 'nominalPrice', 7 : 'acquisitionPrice', 8 : 'salePrice', 9 : 'income', 10 : 'outcome', 12 : 'outcome269st', 13 : 'outcomeTax']

    def totalCalc = getCalcTotalRow()
    def errorColums = []
    if (totalCalc != null) {
        totalColumns.each { index, columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColums.add(index)
            }
        }
    }
    if (!errorColums.isEmpty()) {
        def columns = errorColums.join(', ')
        logger.error("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.tradeNumber
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
            loggerError("В строке \"Номер сделки\" равной $index не заполнены колонки : $errorMsg.")//TODO вернуть error
        } else {
            index = getIndex(row) + 1
            loggerError("В строке $index не заполнены колонки : $errorMsg.")//TODO вернуть error
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
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    getRows(getData(formData)).indexOf(row)
}


/**
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    def totalRow = formData.createDataRow()
    totalRow.alias = "total"
    totalRow.securityName = "Итого"
    setTotalStyle(totalRow)

    // проставим 0ми
    ['nominalPrice', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax'].each{ alias ->
        totalRow[alias] = 0
    }

    def data = getData(formData)
    getRows(data).each{ DataRow row ->
        totalRow.nominalPrice += row.nominalPrice ?:0
        totalRow.acquisitionPrice += row.acquisitionPrice ?:0
        totalRow.salePrice += row.salePrice ?:0
        totalRow.income += row.income ?:0
        totalRow.outcome += row.outcome ?:0
        totalRow.outcome269st += row.outcome269st ?:0
        totalRow.outcomeTax += row.outcomeTax ?:0
    }

    return totalRow
}

/**
 * Получить id справочника.
 *
 * @param ref_id идентификатор справончика
 * @param code атрибут справочника
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return
 */
def getRecordId(def ref_id, String code, def value, Date date, def cache) {
    String filter = code + " = '" + value + "'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter] != null) {
            return cache[ref_id][filter]
        }
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1){
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось найти запись в справочнике «" + refBookFactory.get(ref_id).getName() + "» с атрибутом $code равным $value!")
    return null
}

/**
 * Отсорировать данные (по графе 5, 1).
 *
 * @param data данные нф (хелпер)
 */
void sort(def data) {
    getRows(data).sort { def a, def b ->
        // графа 1 - tradeNumber - Номер сделки первая часть / вторая часть
        // графа 5 - part1REPODate - Дата первой части РЕПО
        if (a.part1REPODate == b.part1REPODate) {
            return a.tradeNumber <=> b.tradeNumber
        }
        return a.part1REPODate <=> b.part1REPODate
    }
}

void loggerError(def msg) {
    //TODO вернуть error
    //logger.error(msg)
    logger.warn(msg)
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}