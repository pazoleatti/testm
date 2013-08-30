package form_template.income.rnu60

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel

import java.text.SimpleDateFormat

/**
 * 6.41 (РНУ-60) Регистр налогового учёта закрытых сделок РЕПО с обязательством покупки по 2-й части
 * ЧТЗ http://conf.aplana.com/pages/viewpage.action?pageId=8588102 ЧТЗ_сводные_НФ_Ф2_Э1_т2.doc
 * @author ekuvshinov
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
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        allCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        allCheck()
        // для сохранения изменений приемников
        getData(formData).commit()
        break
    case FormDataEvent.IMPORT :
        importData()
        break
}

/**

 № графы    ALIAS               Наименование поля                                                           Тип поля        Формат
 1.         tradeNumber         Номер сделки первая часть / вторая часть                                    Строка /41/
 2.         securityName        Наименование ценной бумаги                                                  Строка /255/
 3.         currencyCode        Код валюты                                                                  Строка /3/                      Должно содержать значение поля «Код валюты. Цифровой» справочника «Общероссийский классификатор валют»          Нет Да
 4.         nominalPrice        Номинальная стоимость ценных бумаг (ед. вал.)                               Число/17.2/
 5.         part1REPODate       Дата первой части РЕПО                                                      Дата            ДД.ММ.ГГГ
 6.         part2REPODate       Дата второй части РЕПО                                                      Дата            ДД.ММ.ГГГ
 7.         acquisitionPrice    Стоимость реализации, в т.ч. НКД, по первой части РЕПО (руб.коп.)           Число/17.2/
 8.         salePrice           Стоимость приобретения, в т.ч. НКД, по второй части РЕПО (руб.коп.)         Число/17.2/
 9.         income              Доходы (-) по сделке РЕПО (руб.коп.)                                        Число/17.2/
 10.        outcome             Расходы (+) по сделке РЕПО (руб.коп.)                                       Число/17.2/
 11.        rateBR              Ставка Банка России (%)                                                     Число/17.2/
 12.        outcome269st        Расходы по сделке РЕПО, рассчитанные с учётом ст. 269 НК РФ (руб.коп.)      Число/17.2/
 13.        outcomeTax          Расходы по сделке РЕПО, учитываемые для целей налогообложения (руб.коп.)    Число/17.2/

 */

void allCheck() {
    logicalCheck()
    checkNSI()
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
void logicalCheck() {
    def data = getData(formData)
    for (row in getRows(data)) {
        if (row.getAlias() == null) {
            // 1. Проверка на заполнение поля «<Наименование поля>»
            for (alias in ['outcome269st', 'outcomeTax']) {
                if (row.getCell(alias).value == null) {
                    setError(row.getCell(alias).column)
                }
            }
            // 2. Проверка даты первой части РЕПО
            if (row.part1REPODate != null && reportDate.time.before((Date) row.part1REPODate)) {
                logger.error("Неверно указана дата первой части сделки!")
            }
            // 3. Проверка даты второй части РЕПО
            if (row.part2REPODate != null
                    && (reportPeriodService.getStartDate(formData.reportPeriodId).time.after((Date) row.part2REPODate) || reportPeriodService.getEndDate(formData.reportPeriodId).time.before((Date) row.part2REPODate)
            )) {
                logger.error("Неверно указана дата второй части сделки!")
            }

            // 4. Проверка финансового  результата на основе http://jira.aplana.com/browse/SBRFACCTAX-2870
            if (row.outcome > 0 && row.income > 0) {
                logger.error("Задвоение финансового результата!")
            }

            // 6. Проверка финансового результата
            if (row.outcome == 0 && !(row.outcome269st == 0 && row.outcomeTax == 0)) {
                logger.error("Задвоение финансового результата!")
            }

            // 7. Проверка финансового результата
            BigDecimal temp = (row.salePrice ?: 0) - (row.acquisitionPrice ?: 0)
            if (temp < 0 && !(temp.abs() == row.income)) {
                logger.warn("Неверно определены доходы")
            }

            // 8. Проверка финансового результата
            if (temp > 0 && !(temp == row.outcome)) {
                logger.warn("Неверно определены расходы")
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
                    logger.error("Неверно рассчитана графа " + row.getCell(check).column.name.replace('%', '') + "!")
                }
            }
        }
    }
    // 10. Проверка итоговых значений по всей форме
    List itogoSum = ['nominalPrice', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax']
    DataRow realItogo = getRealItogo()
    if (realItogo!=null) {
        for (String alias in itogoSum) {
            if (realItogo.getCell(alias).value != itogo.getCell(alias).value) {
                logger.error("Итоговые значения рассчитаны неверно!")
                break
            }
        }
    }
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def data = getData(formData)
    if (!getRows(data).isEmpty()) {

        for (def row : getRows(data)) {
            if (isItogoRow(row)) {
                continue
            }

            // 1. Проверка кода валюты со справочным (графа 3)
            if (row.currencyCode!=null && getCurrency(row.currencyCode)==null) {
                logger.warn('Неверный код валюты!')
            }

            // 2. Проверка соответствия ставки рефинансирования ЦБ (графа 11) коду валюты (графа 3)
            def col11 = roundTo2(calc11(row, row.part2REPODate))
            if (col11!=null && col11!=row.rateBR) {
                logger.error('Неверно указана ставка Банка России!')
                return false
            }
        }
    }
    return true
}

void setError(Column c) {

    if (!c.name.empty) {
        logger.error('Поле ' + c.name.replace('%', '') + ' не заполнено')
    }
}

Calendar getReportDate() {
    Calendar periodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)
    Calendar reportingDate = periodEndDate
    reportingDate.set(Calendar.DATE, reportingDate.get(Calendar.DATE) + 1)
    return reportingDate
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
    List itogoSum = ['nominalPrice', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax']
    for (DataRow row in getRows(getData(formData))) {
        if (row.getAlias() == null) {
            for (String name in itogoSum) {
                if (itogo.getCell(name).value == null) {
                    itogo.getCell(name).value = row.getCell(name).value ?: 0
                } else {
                    itogo.getCell(name).value += row.getCell(name).value ?: 0
                }
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
    ['tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

BigDecimal calc9(DataRow row) {
    BigDecimal result
    BigDecimal a = (row.salePrice!=null ?row.salePrice: 0) - (row.acquisitionPrice!=null ?row.acquisitionPrice: 0)
    BigDecimal c = a.abs().setScale(2, BigDecimal.ROUND_HALF_UP)

    /**
     * Если  .A>0, то
     «графа 9» = 0
     «графа 10» = B
     Иначе Если  A<0
     «графа 9» = C
     «графа 10» = 0
     Иначе
     «графа 9»= «графа 10» = 0

     где
     A=«графа8» - «графа7»
     B=ОКРУГЛ(A;2),
     C= ОКРУГЛ(ABS(A);2),

     ABS() – операция получения модуля(абсолютного значения)  числа.

     Реализация немного проще и логичней чем в аналитике, но она даёт теже самые результаты
     вообщем оптимизированный вариант

     */
    if (a < 0) {
        result = c
    } else {
        result = 0
    }
    return roundTo2(result)
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

BigDecimal calc10(DataRow row) {
    BigDecimal result
    BigDecimal a = (row.salePrice ?: 0) - (row.acquisitionPrice ?: 0)
    BigDecimal b = a.setScale(2, BigDecimal.ROUND_HALF_UP)

    /**
     * Если  .A>0, то
     «графа 9» = 0
     «графа 10» = B
     Иначе Если  A<0
     «графа 9» = C
     «графа 10» = 0
     Иначе
     «графа 9»= «графа 10» = 0

     где
     A=«графа8» - «графа7»
     B=ОКРУГЛ(A;2),
     C= ОКРУГЛ(ABS(A);2),

     ABS() – операция получения модуля(абсолютного значения)  числа.

     Реализация немного проще и логичней чем в аналитике, но она даёт теже самые результаты
     вообщем оптимизированный вариант

     */

    if (a > 0) {
        result = b
    } else {
        result = 0
    }
    return roundTo2(result)
}

/**
 * Метод возвращает значение для графы 11
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 * @param rateDate
 */
def calc11(DataRow row, def rateDate){
    def currency = getCurrency(row.currencyCode)
    def rate = getRate(rateDate)
    // Если «графа 10» = 0, то « графа 11» не заполняется; && Если «графа 3» не заполнена, то « графа 11» не заполняется
    if (!isItogoRow(row) && row.outcome != 0 && row.currencyCode != null){
        // Если «графа 3» = 810, то «графа 11» = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на дату «графа 6»,
        if (currency == '810')    {
            return rate
        } else{ // Если «графа 3» ≠ 810), то
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

BigDecimal calc12(DataRow row) {
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    Date date01_11_2009 = format.parse('01.11.2009')
    BigDecimal result = null
    def countDaysInYear = getCountDaysInYear()
    def currency = getCurrency(row.currencyCode)
    if (row.outcome != null && row.outcome > 0) {
        long difference = row.part2REPODate.getTime() - row.part1REPODate.getTime() / (1000 * 60 * 60 * 24) // необходимо получить кол-во в днях
        difference = difference == 0 ? 1 : difference   // Эти вычисления для того чтобы получить разницу в днях, если она нулевая считаем равной 1 так написано в чтз
        if (currency == '810') {
            if (inPeriod(row.part2REPODate, '01.09.2008', '31.12.2009')) {
                /*
                a.  Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009, то:
                    «графа 12» = («графа 7» ? «графа 11» ? 1,5) ? ((«графа6» - «графа5») / 365 (366)) / 100;

                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 1.5) * (difference / countDaysInYear) / 100
            } else if (inPeriod(row.part2REPODate, '01.01.2010', '30.06.2010') && row.part1REPODate.compareTo(date01_11_2009) < 0) {
                /*
                b.  Если «графа 6» принадлежит периоду с 01.01.2010 по 30.06.2010 и одновременно сделка открыта до 01.11.2009 («графа 5» < 01.11.2009 г.), то
                    «графа 12» = («графа 7» ? «графа 11» ? 2) ? ((«графа 6» - «графа 5») / 365 (366)) / 100;
                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 2) * (difference / countDaysInYear) / 100
            } else if (inPeriod(row.part2REPODate, '01.01.2010', '31.12.2012')) {
                /*
                c.  Если «графа 6» принадлежит периоду с 01.01.2010 по 31.12.2012, то:
                    «графа 12» = («графа 7» ? «графа 11» ? 1,8) ? ((«графа6» - «графа5») / 365(366)) / 100.
                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 1.8) * (difference / countDaysInYear) / 100
            } else {
                /*
                d.  Иначе
                    «графа 12» = («графа 7» ? «графа 11» ? 1,1) ? ((«графа 6» -« графа 5») / 365 (366)) / 100;.
                 */
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 1.1) * (difference / countDaysInYear) / 100
            }
        } else {
            result = (row.acquisitionPrice * (row.rateBR ?: 0)) * (difference / countDaysInYear) / 100
            if (inPeriod(row.part2REPODate, '01.01.2011', '31.12.2012')) {
                result = (row.acquisitionPrice * (row.rateBR ?: 0) * 0.8) * (difference / countDaysInYear) / 100
            }
        }
    }
    if (row.outcome != null && row.outcome == 0) {
        result = 0
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
    def data = getData(formData)
    for (DataRow row in getRows(data)) {
        if (row.getAlias() == null) {
            row.income = calc9(row)
            row.outcome = calc10(row)
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
    Calendar periodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    return countDaysOfYear = (new GregorianCalendar()).isLeapYear(periodStartDate.get(Calendar.YEAR)) ? 365 : 366
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
    DataRow<Cell> newRow = formData.createDataRow()
    [
            'tradeNumber', 'securityName', 'currencyCode', 'nominalPrice', 'part1REPODate', 'part2REPODate', 'acquisitionPrice', 'salePrice'
    ].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
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
                        data.insert(row,getRows(data).size()+1)
                    }
                }
            }
        }
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
    if (date!=null) {
        def refDataProvider = refBookFactory.getDataProvider(23)
        def res = refDataProvider.getRecords(date, null, null, null);
        return res.getRecords().get(0).RATE.getNumberValue()
    } else {
        return null;
    }
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
    if (!fileName.contains('.xml')) {
        logger.error('Формат файла должен быть *.xml')
        return
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    def xmlString = importService.getData(is, fileName)
    if (xmlString == null || xmlString == '') {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    // сохранить начальное состояние формы
    def data = getData(formData)
    def rowsOld = getRows(data)
    try {
        // добавить данные в форму
        addData(xml)

        // расчитать и проверить
        deleteAllStatic()
        sort()
        calc()
        addAllStatic()
        allCheck()
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка!')
    }
    // откатить загрузку если есть ошибки
    if (logger.containsLevel(LogLevel.ERROR)) {
        data.clear()
        data.insert(rowsOld, 1)
    }
    data.commit()
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
void addData(def xml) {
    def data = getData(formData)
    data.clear()

    def tmp
    def index
    // справочник 15 "Общероссийский классификатор валют"
    def refDataProvider15 = refBookFactory.getDataProvider(15)

    // TODO (Ramil Timerbaev) Проверка корректности данных
    for (def row : xml.exemplar.table.detail.record) {
        index = 1

        def newRow = getNewRow()

        // графа 1
        newRow.tradeNumber = row.field[index].text()
        index++

        // графа 2
        newRow.securityName = row.field[index].text()
        index++

        // графа 3 - справочник 15, атрибут 64
        tmp = null
        if (row.field[index].@value.text() != null && row.field[index].@value.text().trim() != '') {
            tmp = getRecordId(refDataProvider15, 'CODE', getNumber(row.field[index].@value.text()))
        }
        newRow.currencyCode = tmp
        index++

        // графа 4
        newRow.nominalPrice = getNumber(row.field[index].@value.text())
        index++

        // графа 5
        newRow.part1REPODate = getDate(row.field[index].@value.text())
        index++

        // графа 6
        newRow.part2REPODate = getDate(row.field[index].@value.text())
        index++

        // графа 7
        newRow.acquisitionPrice = getNumber(row.field[index].@value.text())
        index++

        // графа 8
        newRow.salePrice = getNumber(row.field[index].@value.text())
        index++

        // графа 9
        newRow.income = getNumber(row.field[index].@value.text())
        index++

        // графа 10
        newRow.outcome = getNumber(row.field[index].@value.text())
        index++

        // графа 11
        newRow.rateBR = getNumber(row.field[index].@value.text())
        index++

        // графа 12
        newRow.outcome269st = getNumber(row.field[index].@value.text())
        index++

        // графа 13
        newRow.outcomeTax = getNumber(row.field[index].@value.text())

        insert(data, newRow)
    }
    // проверка итоговых данных
    // TODO (Ramil Timerbaev) переделать
    if (false && xml.exemplar.table.total.record.size() > 1 && !getRows(data).isEmpty()) {
        // графы 7-9, 11-13, 15, 17-22
        // TODO (Ramil Timerbaev) убрал нередактируемые вычисляемые графы (их итоги)
        def columnsAlias = ['amountBonds': 7, 'acquisitionPrice': 8, 'costOfAcquisition': 9,
                'marketPriceInRub': 11, /*'acquisitionPriceTax': 12,*/ 'redemptionValue': 13,
                'priceInFactRub': 15, /*'marketPriceInRub1': 17, 'salePriceTax': 18,*/ 'expensesOnSale': 19 /*,
                'expensesTotal': 20, 'profit': 21, 'excessSalePriceTax': 22*/]

        index = 0
        for (def row : xml.exemplar.table.total.record) {
            index++
            def totalRow = (index == 1 ? getItogoKvartal() : getItogoKvartal())

            // сравнить посчитанные суммы итогов с итогами из транспортного файла (графы 7-9, 11-13, 15, 17-22)
            def exit = false
            columnsAlias.each { alias, i ->
                if (!exit && totalRow.getCell(alias).getValue() != getNumber(row.field[i].@value.text())) {
                    logger.error('Итоговые значения неправильные.')
                    exit = true
                }
            }
            if (exit) {
                return
            }
        }
    }
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    if (value == null) {
        return null
    }
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    // поменять запятую на точку и убрать пробелы
    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
    return new BigDecimal(tmp)
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

    // графа 2..11, 13..15, 19
    ['tradeNumber', 'singSecurirty', 'issue', 'acquisitionDate', 'saleDate', 'amountBonds',
            'acquisitionPrice', 'costOfAcquisition', 'marketPriceInPerc', 'marketPriceInRub',
            'redemptionValue', 'priceInFactPerc', 'priceInFactRub', 'expensesOnSale'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

/**
 * Получить идентификатор записи из справочника.
 *
 * @param provider справочник
 * @param searchByAlias алиас атрибута справочника по которому ищется запись
 * @param value значение по которому ищется запись
 */
def getRecordId(def provider, def searchByAlias, def value) {
    def records = provider.getRecords(new Date(), null, searchByAlias + " = '" + value + "'", null);
    if (records != null && !records.getRecords().isEmpty()) {
        return records.getRecords().get(0).get('record_id').getNumberValue()
    }
    return null
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value) {
    if (value == null || value == '') {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    return format.parse(value)
}