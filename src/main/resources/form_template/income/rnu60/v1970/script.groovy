package form_template.income.rnu60.v1970

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * 6.41 (РНУ-60) Регистр налогового учёта закрытых сделок РЕПО с обязательством покупки по 2-й части
 * formTemplateId=351
 *
 * ЧТЗ http://conf.aplana.com/pages/viewpage.action?pageId=8588102 ЧТЗ_сводные_НФ_Ф2_Э1_т2.doc
 * @author ekuvshinov
 *
 * TODO убрать loggerError и заменить на logger.error
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        allCheck()
        break
    case FormDataEvent.CALCULATE:
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        allCheck()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        allCheck()
        break
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED : // после принятия из подготовлена
        allCheck()
        break
    case FormDataEvent.COMPOSE : // обобщить
        consolidation()
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        allCheck()
        break
    case FormDataEvent.IMPORT :
        importData()
        if (!hasError()) {
            calcAfterImport()
            addAllStatic()
        }
        break
    case FormDataEvent.MIGRATION :
        importData()
        if (!hasError()) {
            addAllStatic()
        }
}

/**

 № графы    ALIAS               Наименование поля                                                           Тип поля        Формат
 1.         tradeNumber         Номер сделки первая часть / вторая часть                                    Строка /41/
 2.         securityName        Наименование ценной бумаги                                                  Строка /255/
 3.         currencyCode        Код валюты                                                                  Строка /3/                      Должно содержать значение поля «Код валюты. Цифровой» справочника «Общероссийский классификатор валют»          Нет Да
 4.         nominalPrice        Номинальная стоимость ценных бумаг (ед. вал.)                               Число/17.2/
 5.         part1REPODate       Дата первой части РЕПО                                                      Дата            ДД.ММ.ГГГ
 6.         part2REPODate       Дата второй части РЕПО                                                      Дата            ДД.ММ.ГГГ
 7.         salePrice           Стоимость реализации, в т.ч. НКД, по первой части РЕПО (руб.коп.)           Число/17.2/
 8.         acquisitionPrice    Стоимость приобретения, в т.ч. НКД, по второй части РЕПО (руб.коп.)         Число/17.2/
 9.         income              Доходы (-) по сделке РЕПО (руб.коп.)                                        Число/17.2/
 10.        outcome             Расходы (+) по сделке РЕПО (руб.коп.)                                       Число/17.2/
 11.        rateBR              Ставка Банка России (%)                                                     Число/17.2/
 12.        outcome269st        Расходы по сделке РЕПО, рассчитанные с учётом ст. 269 НК РФ (руб.коп.)      Число/17.2/
 13.        outcomeTax          Расходы по сделке РЕПО, учитываемые для целей налогообложения (руб.коп.)    Число/17.2/

 */

// Проверяемые на пустые значения атрибуты (графа 1..13)
@Field
def nonEmptyColumns = ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate',
        'part2REPODate', 'salePrice', 'acquisitionPrice', 'income', 'outcome', 'outcome269st', 'outcomeTax']

@Field
def endDate = null

def allCheck() {
    return !hasError() && logicalCheck()
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
 * 6.41.3.2.1   Логические проверки
 * Табл. 209 Логические проверки формы «Регистр налогового учёта закрытых сделок РЕПО с обязательством покупки по 2-й части»
 */
def logicalCheck() {
    def data = getData(formData)
    def dateStart = reportPeriodService.getCalendarStartDate(formData.reportPeriodId)?.time
    def dateEnd = getReportPeriodEndDate()
    def reportDate = reportPeriodService.getReportDate(formData.reportPeriodId)?.time
    for (row in getRows(data)) {
        if (row.getAlias() == null) {

            def index = row.tradeNumber
            def errorMsg
            if (index!=null && index!='') {
                errorMsg = "В строке \"Номер сделки\" равной $index "
            } else {
                index = row.getIndex()
                errorMsg = "В строке $index "
            }

            // 1. Проверка на заполнение поля «<Наименование поля>»
            if (!checkRequiredColumns(row, nonEmptyColumns)) {
                return false
            }
            // 2. Проверка даты первой части РЕПО
            if (row.part1REPODate != null && reportDate.before((Date) row.part1REPODate)) {
                loggerError(errorMsg + "неверно указана дата первой части сделки!")//TODO вернуть error
                return false
            }
            // 3. Проверка даты второй части РЕПО
            if (row.part2REPODate != null
                    && (dateStart.after((Date) row.part2REPODate) || dateEnd.before((Date) row.part2REPODate)
            )) {
                loggerError(errorMsg + "неверно указана дата второй части сделки!")//TODO вернуть error
                return false
            }

            // 4. Проверка финансового  результата на основе http://jira.aplana.com/browse/SBRFACCTAX-2870
            if (row.outcome > 0 && row.income > 0) {
                loggerError(errorMsg + "задвоение финансового результата!")//TODO вернуть error
                return false
            }

            // 6. Проверка финансового результата
            if (row.outcome == 0 && !(row.outcome269st == 0 && row.outcomeTax == 0)) {
                loggerError(errorMsg + "задвоение финансового результата!")
                return false
            }

            // 7. Проверка финансового результата
            BigDecimal temp = (row.acquisitionPrice ?: 0) - (row.salePrice ?: 0)
            if (temp < 0 && !(temp.abs() == row.income)) {
                logger.warn(errorMsg + "неверно определены доходы")//TODO вернуть error
            }

            // 8. Проверка финансового результата
            if (temp > 0 && !(temp == row.outcome)) {
                logger.warn(errorMsg + "неверно определены расходы")
            }

            // 9. Арифметические проверки граф 9, 10, 11, 12, 13
            List checks = ['income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax']
            Map<String, BigDecimal> value = [:]
            value.put('income', calc9(row))
            value.put('outcome', calc10(row))
            value.put('rateBR', calc11(row,row.part2REPODate))
            value.put('outcome269st', calc12(row))
            value.put('outcomeTax', calc13(row))
            for (String check in checks) {
                if (row.getCell(check).value != value.get(check)) {
                    loggerError(errorMsg + "неверно рассчитана графа " + row.getCell(check).column.name.replace('%', '%%') + "!")//TODO вернуть error
                    return false
                }
            }
        }
    }
    // 10. Проверка итоговых значений по всей форме
    List itogoSum = ['nominalPrice', 'salePrice', 'acquisitionPrice', 'income', 'outcome', 'outcome269st', 'outcomeTax']
    DataRow realItogo = getRealItogo()
    if (realItogo!=null) {
        DataRow itogo = getItogo()
        for (String alias in itogoSum) {
            if (realItogo.getCell(alias).value != itogo.getCell(alias).value) {
                loggerError("Итоговые значения рассчитаны неверно!")//TODO вернуть error
                return false
            }
        }
    }
    return true
}

/**
 * Проверить заполненность обязательных полей.
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
        if (index!=null && index!='') {
            loggerError("В строке \"Номер сделки\" равной $index не заполнены колонки : $errorMsg.")//TODO вернуть error
        } else {
            index = row.getIndex()
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
 * Проставляет статические строки
 */
void addAllStatic() {
    def data=getData(formData)
    if (getRows(data).size()>0) {
        data.insert(itogo,getRows(data).size()+1)
    }
}

/**
 * Получает строку итого
 * @return
 */
DataRow<Cell> getItogo() {
    DataRow<Cell> itogo = formData.createDataRow()
    itogo.setAlias('itogo')
    itogo.securityName = "Итого"
    List itogoSum = ['nominalPrice', 'salePrice', 'acquisitionPrice', 'income', 'outcome', 'outcome269st', 'outcomeTax']
    itogoSum.each { name ->
        itogo.getCell(name).setValue(0, itogo.getIndex())
    }
    for (DataRow row in getRows(getData(formData))) {
        if (row.getAlias() == null) {
            for (String name in itogoSum) {
                def value = itogo.getCell(name).value + (row.getCell(name).value ?: 0)
                itogo.getCell(name).setValue(value, itogo.getIndex())
            }
        }
    }
    setTotalStyle(itogo)
    return itogo
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'salePrice', 'acquisitionPrice', 'income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

BigDecimal calc9(DataRow row) {
    BigDecimal result
    BigDecimal a = (row.acquisitionPrice ?: 0) - (row.salePrice ?: 0)
    BigDecimal c = a.abs().setScale(2, BigDecimal.ROUND_HALF_UP)
    if (a < 0) {
        result = c
    } else {
        result = 0
    }
    return result
}

BigDecimal calc10(DataRow row) {
    BigDecimal result
    BigDecimal a = (row.acquisitionPrice ?: 0) - (row.salePrice ?: 0)
    BigDecimal b = a.setScale(2, BigDecimal.ROUND_HALF_UP)
    if (a > 0) {
        result = b
    } else {
        result = 0
    }
    return result
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
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
 * Метод возвращает значение для графы 11
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 * @param rateDate
 */
def calc11(DataRow row, def rateDate) {
    // Если «графа 10» = 0, то « графа 11» не заполняется;
    // Если «графа 3» не заполнена, то « графа 11» не заполняется
    if (row.outcome == 0 || row.currencyCode == null) {
        return null
    }
    def currency = getCurrency(row.currencyCode)

    // Если «графа 3» = 810, то «графа 11» = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на дату «графа 6»,
    if (currency == '810') {
        return getRate(rateDate)
    } else { // Если «графа 3» ? 810), то
        // Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009 (включительно), то «графа 11» = 22;
        if (inPeriod(rateDate, '01.09.2008', '31.12.2009')){
            return 22
        } else if (inPeriod(rateDate, '01.01.2011', '31.12.2012')){
            // Если «графа 6» принадлежит периоду с 01.01.2011 по 31.12.2012 (включительно), то
            // графа 11 = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на дату «графа 6»;
            return getRate(rateDate)
        } else{
            //Если  «графа 6» не принадлежит отчётным периодам с 01.09.2008 по 31.12.2009 (включительно), с 01.01.2011 по 31.12.2012 (включительно)),
            //то  «графа 11» = 15.
            return 15
        }
    }
}

BigDecimal calc12(DataRow row) {
    if (row.currencyCode == null) {
        return null
    }
    if (row.outcome != null && row.outcome == 0) {
        return roundTo2(0)
    }

    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    Date date01_11_2009 = format.parse('01.11.2009')
    BigDecimal result = null

    if (row.outcome != null && row.outcome > 0) {
        long difference = 0
        if (row.part2REPODate != null && row.part1REPODate != null) {
            difference = row.part2REPODate - row.part1REPODate
        }
        // необходимо получить кол-во в днях
        difference = difference == 0 ? 1 : difference // Эти вычисления для того чтобы получить разницу в днях, если она нулевая считаем равной 1 так написано в чтз

        def coefficient
        def currency = getCurrency(row.currencyCode)
        if (currency == '810') {
            if (row.part2REPODate != null && inPeriod(row.part2REPODate, '01.09.2008', '31.12.2009')) {
                // a. Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009, то: 1.5
                coefficient = 1.5
            } else if (row.part2REPODate != null && row.part1REPODate != null &&
                    inPeriod(row.part2REPODate, '01.01.2010', '30.06.2010') && row.part1REPODate < date01_11_2009) {
                // b. Если «графа 6» принадлежит периоду с 01.01.2010 по 30.06.2010
                // и одновременно сделка открыта до 01.11.2009 («графа 5» < 01.11.2009 г.), то: 2
                coefficient = 2
            } else if (row.part2REPODate != null && inPeriod(row.part2REPODate, '01.01.2010', '31.12.2012')) {
                // c. Если «графа 6» принадлежит периоду с 01.01.2010 по 31.12.2012, то: 1.8
                coefficient = 1.8
            } else {
                // d. Иначе: 1.1
                coefficient = 1.1
            }
        } else {
            if (row.part2REPODate != null && inPeriod(row.part2REPODate, '01.01.2011', '31.12.2012')) {
                coefficient = 0.8
            } else {
                coefficient = 1
            }
        }
        def countDaysInYear = getCountDaysInYear()
        result = ((row.salePrice ?: 0) * (row.rateBR ?: 0) * coefficient) * (difference / countDaysInYear) / 100
    }
    return roundTo2(result)
}

BigDecimal calc13(DataRow row) {
    BigDecimal result = null
    if (row.outcome > 0) {
        if (row.outcome <= row.outcome269st) {
            result = row.outcome
        }
        if (row.outcome > row.outcome269st) {
            result = row.outcome269st
        }
    }
    if (row.outcome == 0) {
        result = 0
    }
    return result
}

/**
 * Табл. 207 Алгоритмы заполнения полей формы «Регистр налогового учёта закрытых сделок РЕПО с обязательством покупки по 2-й части»
 */
void calc() {
    if (formData.kind == FormDataKind.PRIMARY) {
        def data = getData(formData)
        for (DataRow row in getRows(data)) {
            if (row.getAlias() == null) {
                row.income = calc9(row)
                row.outcome = calc10(row)
                row.rateBR = calc11(row, row.part2REPODate)
                row.outcome269st = calc12(row)
                row.outcomeTax = calc13(row)
            }
        }
        data.save(getRows(data));
    }
}

/**
 * Табл. 207 Алгоритмы заполнения полей формы «Регистр налогового учёта закрытых сделок РЕПО с обязательством покупки по 2-й части»
 */
void calcAfterImport() {
    def data = getData(formData)
    for (DataRow row in getRows(data)) {
        if (row.getAlias() == null) {
            row.rateBR = calc11(row,row.part2REPODate)
            row.outcome269st = calc12(row)
            row.outcomeTax = calc13(row)
        }
    }
    data.save(getRows(data));
}

/**
 * Количество дней в году за который делаем
 * @return
 */
int getCountDaysInYear() {
    Calendar periodStartDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId)
    return countDaysOfYear = (new GregorianCalendar()).isLeapYear(periodStartDate.get(Calendar.YEAR)) ? 366 : 365
}

/**
 * Сортирует форму в соответвие с требованиями 6.11.2.1 Перечень полей формы
 */
void sort() {
    getRows(getData(formData)).sort({ DataRow a, DataRow b ->
        if (a.part1REPODate == b.part1REPODate) {
            return a.tradeNumber <=> b.tradeNumber
        }
        return a.part1REPODate <=> b.part1REPODate
    })
}

/**
 * Удаляет строку из формы
 */
void deleteRow() {
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        getData(formData).delete(currentDataRow)
    }
}

/**
 * Удаляет все статические строки(ИТОГО) во всей форме
 */
void deleteAllStatic() {
    def data = getData(formData)
    for(def i=0;i<getRows(data).size();i++){
        def row = getRows(data).get(i)
        if (row.getAlias() == "itogo") {
            data.delete(row)
        }
    }
}

DataRow getRealItogo(){
    def data = getData(formData)
    for(def i=0;i<getRows(data).size();i++){
        def row = getRows(data).get(i)
        if (row.getAlias() == "itogo") {
            return row;
        }
    }
}

/**
 * Вставка строки в случае если форма генирует динамически строки итого (на основе данных введённых пользователем)
 */
void addNewRow() {
    def data = getData(formData)
    DataRow<Cell> newRow = getNewRow()
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
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)
    // удалить все строки и собрать из источников их строки
    data.clear()
    def newRows = []

    departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        newRows.add(row)
                    }
                }
            }
        }
    }
    if (!newRows.isEmpty()) {
        data.insert(newRows, 1)
        sort()
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
    def cached = data.getAllCached()
    return cached
}

/**
 * Получить ставку рефинансирования ЦБ РФ
 * @param date
 */
def getRate(def date) {
    if (date != null) {
        def refDataProvider = refBookFactory.getDataProvider(23)
        def records = refDataProvider.getRecords(date, null, null, null);
        if (records != null && records.getRecords() != null && records.getRecords().size() > 0) {
            return records.getRecords().getAt(0).RATE.numberValue
        }
    }
    return null;
}

/**
 * Получить цифровой код валюты
 */
def getCurrency(def currencyCode) {
    return  refBookService.getStringValue(15,currencyCode,'CODE')
}

/**
 * Проверить попадает ли указанная дата в период
 */
def inPeriod(def date, def from, def to) {
    if (date == null) {
        return false
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def dateFrom = format.parse(from)
    def dateTo = format.parse(to)
    return (dateFrom < date && date <= dateTo)
}

/**
 * Проверка является ли строка итоговой (любой итоговой, т.е. по коду, либо основной)
 */
def isItogoRow(row){
    row.getAlias()=='itogo'
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
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml, def fileName) {
    def date = getReportPeriodEndDate()
    def cache = [:]
    def data = getData(formData)
    data.clear()
    def index
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

        // графа 3 - справочник 15, атрибут 64
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
            newRow.salePrice = getNumber(getCellValue(row, index, type))
            index++

            // графа 8
            newRow.acquisitionPrice = getNumber(getCellValue(row, index, type))
            index++

            // графа 5
            newRow.part1REPODate = getDate(getCellValue(row, index, type), format)
            index++

            // графа 6
            newRow.part2REPODate = getDate(getCellValue(row, index, type), format)
            index++
        } else {  //если формат файла RNU порядок не меняется
            // графа 5
            newRow.part1REPODate = getDate(getCellValue(row, index, type), format)
            index++

            // графа 6
            newRow.part2REPODate = getDate(getCellValue(row, index, type), format)
            index++

            // графа 7
            newRow.salePrice = getNumber(getCellValue(row, index, type))
            index++

            // графа 8
            newRow.acquisitionPrice = getNumber(getCellValue(row, index, type))
            index++
        }

        if (formDataEvent == FormDataEvent.MIGRATION) {
            //При миграции нужно поменять порядок зполнения этих полей
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
        newRow.rateBR = getNumber(getCellValue(row, index, type))
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
            totalRow.salePrice = getNumber(getCellValue(row, 4, type))

            // графа 8
            totalRow.acquisitionPrice = getNumber(getCellValue(row, 5, type))
        } else {
            // графа 7
            totalRow.salePrice = getNumber(getCellValue(row, 6, type))

            // графа 8
            totalRow.acquisitionPrice = getNumber(getCellValue(row, 7, type))
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
 * Вставить новыую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def row = formData.createDataRow()
    [
            'tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'salePrice', 'acquisitionPrice'
    ].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
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
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value, def format) {
    if (value == null || value == '') {
        return null
    }
    try {
        return format.parse(value)
    } catch (Exception e) {
        throw new Exception("Значение \"$value\" не может быть преобразовано в дату. " + e.message)
    }
}

/**
 * Cравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def totalColumns = [4: 'nominalPrice', 7: 'salePrice', 8: 'acquisitionPrice',
            9: 'income', 10: 'outcome', 12: 'outcome269st', 13: 'outcomeTax']

    def totalCalc = getItogo()
    def errorColums = []
    if (totalCalc != null) {
        totalColumns.each { index, columnAlias ->
            if (totalCalc[columnAlias] != totalRow[columnAlias]) {
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