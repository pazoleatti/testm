package form_template.deal.app_6_18.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * 838 - 6.18. Купля-продажа драгоценных металлов
 * formTemplateId=838
 *
 * TODO в заголовках есть кавычки. Не убираем т.к. заказчик не меняет ЧТЗ.
 * @author Bulat Kinzyabulatov
 */

// 1	rowNumber           № п/п
// 2	name                Полное наименование с указанием ОПФ
// 3	dependence          Признак взаимозависимости
// 4	iksr                ИНН/ КИО
// 5	countryName         Наименование страны регистрации
// 6	countryCode         Код страны регистрации по классификатору ОКСМ
// 7	docNumber           Номер договора
// 8	docDate             Дата договора
// 9	dealNumber          Номер сделки
// 10	dealDate            Дата заключения сделки
// 11	dealFocus           Направленность сделки
// 12	signPhis            Признак физической поставки драгоценного металла
// 13	metalName           Наименование драгоценного металла
// 14	foreignDeal         Внешнеторговая сделка
// 15	countryCodeNumeric  Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами. "Код страны по классификатору ОКСМ (цифровой)"
// 16	regionCode          Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами. "Регион (код)"
// 17	city                Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами. Город
// 18	locality            Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами. Населенный пункт
// 19	countryCodeNumeric2 Место совершения сделки (адрес места доставки /разгрузки драгоценного металла). Код страны по классификатору ОКСМ (цифровой)
// 20	region2             Место совершения сделки (адрес места доставки /разгрузки драгоценного металла). "Регион (код)"
// 21	city2               Место совершения сделки (адрес места доставки /разгрузки драгоценного металла). Город
// 22	locality2           Место совершения сделки (адрес места доставки /разгрузки драгоценного металла). Населенный пункт (село, поселок и т.д.)
// 23	deliveryCode        Код условия поставки
// 24	count               Количество
// 25	incomeSum           Сумма доходов Банка по данным бухгалтерского учета, руб.
// 26	outcomeSum          Сумма расходов Банка по данным бухгалтерского учета, руб.
// 27	price               Цена (тариф) за единицу измерения без учета НДС, акцизов и пошлины, руб.
// 28	total               Итого стоимость без учета НДС, акцизов и пошлины, руб.
// 29	dealDoneDate        Дата совершения сделки

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Все поля
@Field
def allColumns = ['rowNumber', 'name', 'dependence', 'iksr', 'countryName', 'countryCode',
                  'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealFocus', 'signPhis',
                  'metalName', 'foreignDeal', 'countryCodeNumeric', 'regionCode', 'city', 'locality',
                  'countryCodeNumeric2', 'region2', 'city2', 'locality2', 'deliveryCode', 'count',
                  'incomeSum', 'outcomeSum', 'price', 'total', 'dealDoneDate']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealFocus',
                       'signPhis', 'metalName', 'countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2',
                       'region2', 'city2', 'locality2', 'deliveryCode', 'incomeSum', 'outcomeSum', 'dealDoneDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'dependence', 'iksr', 'countryName', 'countryCode', 'foreignDeal', 'count', 'price', 'total']

// Группируемые атрибуты
@Field
def groupColumns = ['name', 'docNumber', 'docDate', 'dealFocus', 'foreignDeal']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'dependence', 'docNumber', 'docDate', 'dealNumber', 'dealDate', 'dealFocus', 'signPhis',
                       'metalName', 'foreignDeal', 'count', 'incomeSum', 'outcomeSum', 'price', 'total', 'dealDoneDate']

@Field
def totalColumns = ['count', 'incomeSum', 'outcomeSum', 'total']

// Дата окончания отчетного периода
@Field
def endDate = null

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

@Field
final ОМS_SIGN = 'ОМС'
@Field
final DEAL_SIGN = 'Поставочная сделка'

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    // "Да" и "Нет"
    def recYesId = getRecordId(38, 'CODE', '1', -1, null, true)
    def recNoId = getRecordId(38, 'CODE', '0', -1, null, true)

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка признака взаимозависимости
        if (row.name && row.dependence) {
            def typeId = getRefBookValue(520, row.name)?.TYPE?.value
            boolean isVzl = 'ВЗЛ'.equals(getRefBookValue(525, typeId)?.CODE?.stringValue)
            if (row.dependence != (isVzl ? recYesId : recNoId)) {
                rowError(logger, row, "Строка $rowNum: Графа «${getColumnName(row,'dependence')}» должна быть заполнена значением «Да», если по графе «${getColumnName(row,'name')}» выбрано ЮЛ типа ВЗЛ!")
            }
        }

        // 3. Проверка корректности даты договора
        checkDatePeriod(logger, row, 'docDate', Date.parse('dd.MM.yyyy', '01.01.1991'), getReportPeriodEndDate(), true)

        // 4. Проверка корректности даты заключения сделки
        checkDatePeriod(logger, row, 'dealDate', 'docDate', getReportPeriodEndDate(), true)

        boolean signPhisCorrect = false
        String singPhisString = getRefBookValue(18, row.signPhis)?.SIGN?.stringValue
        // 5. Проверка признака физической поставки
        if (row.signPhis != null) {
            signPhisCorrect = [ОМS_SIGN, DEAL_SIGN].contains(singPhisString)
            if (!signPhisCorrect) {
                rowError(logger, row, "Строка $rowNum: Графа «${getColumnName(row, 'signPhis')}» может содержать только одно из значений: ОМС, Поставочная сделка!")
            }
        }

        // 6. Проверка признака внешнеторговой сделки
        if (row.foreignDeal != null && row.countryCodeNumeric != null && row.countryCodeNumeric2 != null) {
            // Если «Графа 15» не равна «Графа 19», то «Графа 14» должна быть заполнена значением «Да».
            // Иначе «Графа 14» должна быть заполнена значением «Нет»
            if (((row.countryCodeNumeric != row.countryCodeNumeric2) && (row.foreignDeal != recYesId)) ||
                    ((row.countryCodeNumeric == row.countryCodeNumeric2) && (row.foreignDeal != recNoId))) {
                rowError(logger, row, "Строка $rowNum: Значение графы «${getColumnName(row, 'foreignDeal')}» не соответствует сведениям о стране отправки и стране доставки драгоценных металлов!")
            }
        }

        // 7. Проверка зависимости от признака физической поставки
        if (signPhisCorrect) {
            if (ОМS_SIGN.equals(singPhisString)) {
                def checkFields = ['countryCodeNumeric', 'regionCode', 'city', 'locality', 'countryCodeNumeric2', 'region2', 'city2', 'locality2', 'deliveryCode']
                def foundNonEmptyField = checkFields.find{ row[it] } != null
                if (foundNonEmptyField) {
                    rowError(logger, row, "Строка $rowNum: Графы 13.1-13.4, 14.1-14.4, 15 не должны быть заполнены, т.к. в графе «${getColumnName(row, 'signPhis')}» указано значение «ОМС»!")
                }
            } else if (DEAL_SIGN.equals(singPhisString)) {
                // i.	Графы 15, 19 должны быть заполнены
                if(row.countryCodeNumeric == null || row.countryCodeNumeric2 == null){
                    rowError(logger, row, "Строка $rowNum: Графы «Код страны по классификатору ОКСМ (цифровой)» (13.1, 14.1) должны быть заполнены, т.к. в графе «${getColumnName(row, 'signPhis')}» указано значение «Поставочная сделка»!")
                }
                // ii.	Если графа 15 заполнена элементом с кодом 643, то графа 16 должна быть заполнена;
                // iii.	Если графа 15 заполнена элементом с кодом, отличным от 643, то графа 16 должна быть не заполнена.
                def country = getRefBookValue(10, row.countryCodeNumeric)?.CODE?.stringValue
                if (country != null) {
                    def regionName = getColumnName(row, 'regionCode')
                    if (country == '643' && row.regionCode == null) {
                        rowError(logger, row, "Строка $rowNum: Графа «$regionName» (13.2) должна быть заполнена, т.к. указанная страна отправки Россия!")
                    } else if (country != '643' && row.regionCode != null) {
                        rowError(logger, row, "Строка $rowNum: Графа «$regionName» (13.2) не должна быть заполнена, т.к указанная страна отправки не Россия!")
                    }
                }
                // iv.	Если графа 19 заполнена элементом с кодом 643, то графа 20 должна быть заполнена;
                // v.	Если графа 19 заполнена элементом с кодом, отличным от 643, то графа 20 должна быть не заполнена.
                country = getRefBookValue(10, row.countryCodeNumeric2)?.CODE?.stringValue
                if (country != null) {
                    def regionName = getColumnName(row, 'region2')
                    if (country == '643' && row.region2 == null) {
                        rowError(logger, row, "Строка $rowNum: Графа «$regionName» (14.2) должна быть заполнена, т.к. указанная страна отправки Россия!")
                    } else if (country != '643' && row.region2 != null) {
                        rowError(logger, row, "Строка $rowNum: Графа «$regionName» (14.2) не должна быть заполнена, т.к указанная страна отправки не Россия!")
                    }
                }
                // vi.	Должна быть заполнена графа 17 или 18.
                if ((row.city == null && row.locality == null) || (row.city != null && row.locality != null)) {
                    def msg1 = getColumnName(row, 'city')
                    def msg2 = getColumnName(row, 'locality')
                    rowError(logger, row, "Строка $rowNum: Должна быть заполнена одна из граф «$msg1» (13.3) или «$msg2» (13.4)!")
                }
                // vii.	Должна быть заполнена графа 21 или 22.
                if ((row.city2 == null && row.locality2 == null) || (row.city2 != null && row.locality2 != null)) {
                    def msg1 = getColumnName(row, 'city2')
                    def msg2 = getColumnName(row, 'locality2')
                    rowError(logger, row, "Строка $rowNum: Должна быть заполнена одна из граф «$msg1» (14.3) или «$msg2» (14.4)!")
                }
            }
        }

        // 8. Проверка количества
        if (row.count != null && row.count != 1) {
            rowError(logger, row, "Строка $rowNum: Графа «${getColumnName(row, 'count')}» должна быть заполнена значением «1»!")
        }

        if (row.incomeSum != null && row.outcomeSum != null){
            String msg1 = getColumnName(row, 'price')
            String msg2 = getColumnName(row, 'incomeSum')
            String msg3 = getColumnName(row, 'outcomeSum')

            // Проверка заполнения сумм доходов и расходов
            if(row.incomeSum == 0 && row.outcomeSum  == 0){
                logger.error("Строка $rowNum: Значения граф «$msg2», «$msg3» не должны одновременно быть равны «0»!");
            }

            // Проверка цены и стоимости
            // Проверка цены
            if (row.incomeSum && row.outcomeSum == 0 && row.price != row.incomeSum) {
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
            } else if (row.incomeSum == 0 && row.outcomeSum && row.price != row.outcomeSum) {
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg3»!")
            } else if (row.incomeSum && row.outcomeSum && row.price != (row.incomeSum - row.outcomeSum).abs()) {
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно модулю разности значений граф «$msg2» и «$msg3»!")
            }

            // Проверка цены и стоимости
            // Проверка стоимости
            msg1 = getColumnName(row, 'total')
            if (row.incomeSum && row.outcomeSum == 0 && row.total != row.incomeSum) {
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg2»!")
            } else if (row.incomeSum == 0 && row.outcomeSum && row.total != row.outcomeSum) {
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно значению графы «$msg3»!")
            } else if (row.incomeSum && row.outcomeSum && row.total != (row.incomeSum - row.outcomeSum).abs()) {
                logger.error("Строка $rowNum: Значение графы «$msg1» должно быть равно модулю разности значений граф «$msg2» и «$msg3»!")
            }
        }

        // Проверка корректности даты совершения сделки
        // TODO (SBRFACCTAX-15094) заменить на checkDatePeriodExt
        checkDatePeriodExtLocal(logger, row, 'dealDoneDate', 'dealDate', Date.parse('dd.MM.yyyy', '01.01.' + getReportPeriodEndDate().format('yyyy')), getReportPeriodEndDate(), true)
    }
}

// TODO (SBRFACCTAX-15094) удалить
void checkDatePeriodExtLocal(logger, row, String alias, String startAlias, Date yearStartDate, Date endDate, boolean fatal) {
    // дата проверяемой графы
    Date docDate = row.getCell(alias).getDateValue();
    // дата другой графы
    Date startDate = row.getCell(startAlias).getDateValue();

    if (docDate != null && startDate != null && (docDate.before(yearStartDate) || docDate.after(endDate) || docDate.before(startDate))) {
        logger.error(String.format("Строка %d: Дата по графе «%s» должна принимать значение из диапазона %s - %s и быть больше либо равна дате по графе «%s»!",
                row.getIndex(),
                getColumnName(row, alias),
                formatDate(yearStartDate, "dd.MM.yyyy"),
                formatDate(endDate, "dd.MM.yyyy"),
                getColumnName(row, startAlias)
        ));
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    // "Да" / "Нет"
    def recYesId = getRecordId(38, 'CODE', '1', -1, null, true)
    def recNoId = getRecordId(38, 'CODE', '0', -1, null, true)

    for (row in dataRows) {
        // графа 3 заполняется на основе 2-ой
        if (row.name) {
            def typeId = getRefBookValue(520, row.name)?.TYPE?.value
            boolean isVzl = 'ВЗЛ'.equals(getRefBookValue(525, typeId)?.CODE?.stringValue)
            row.dependence = isVzl ? recYesId : recNoId
        }
        // графа 14
        row.foreignDeal = (row.countryCodeNumeric != row.countryCodeNumeric2) ? recYesId : recNoId
        // Расчет поля "Количество"
        row.count = 1
        // графа 27
        row.price = calc1415(row)
        // графа 28
        row.total = calc1415(row)
    }
}

def BigDecimal calc1415(def row) {
    if (row.incomeSum && row.outcomeSum == 0) {
        return row.incomeSum
    } else if (row.incomeSum == 0 && row.outcomeSum) {
        return row.outcomeSum
    } else if (row.incomeSum && row.outcomeSum) {
        return (row.incomeSum - row.outcomeSum).abs()
    }
    return null
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 28
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = 'Общая информация о контрагенте - юридическом лице'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues || rowValues.isEmpty() || !rowValues.find { it }) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]) : 'Общая информация о контрагенте - юридическом лице']),
            ([(headerRows[0][6]) : 'Сведения о сделке']),
            ([(headerRows[1][0]) : getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[1][1]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[1][2]) : getColumnName(tmpRow, 'dependence')]),
            ([(headerRows[1][3]) : getColumnName(tmpRow, 'iksr')]),
            ([(headerRows[1][4]) : getColumnName(tmpRow, 'countryName')]),
            ([(headerRows[1][5]) : getColumnName(tmpRow, 'countryCode')]),
            ([(headerRows[1][6]) : getColumnName(tmpRow, 'docNumber')]),
            ([(headerRows[1][7]) : getColumnName(tmpRow, 'docDate')]),
            ([(headerRows[1][8]) : getColumnName(tmpRow, 'dealNumber')]),
            ([(headerRows[1][9]): getColumnName(tmpRow, 'dealDate')]),
            ([(headerRows[1][10]): getColumnName(tmpRow, 'dealFocus')]),
            ([(headerRows[1][11]): getColumnName(tmpRow, 'signPhis')]),
            ([(headerRows[1][12]): getColumnName(tmpRow, 'metalName')]),
            ([(headerRows[1][13]): getColumnName(tmpRow, 'foreignDeal')]),
            ([(headerRows[1][14]): 'Место отправки (погрузки) драгоценного металла в соответствии с товаросопроводительными документами']),
            ([(headerRows[1][18]): 'Место совершения сделки (адрес места доставки /разгрузки драгоценного металла)']),
            ([(headerRows[1][22]): getColumnName(tmpRow, 'deliveryCode')]),
            ([(headerRows[1][23]): getColumnName(tmpRow, 'count')]),
            ([(headerRows[1][24]): getColumnName(tmpRow, 'incomeSum')]),
            ([(headerRows[1][25]): getColumnName(tmpRow, 'outcomeSum')]),
            ([(headerRows[1][26]): getColumnName(tmpRow, 'price')]),
            ([(headerRows[1][27]): getColumnName(tmpRow, 'total')]),
            ([(headerRows[1][28]): getColumnName(tmpRow, 'dealDoneDate')]),
            ([(headerRows[2][14]): '\"Код страны по классификатору ОКСМ (цифровой)"']),
            ([(headerRows[2][15]): '\"Регион (код)\"']),
            ([(headerRows[2][16]): 'Город']),
            ([(headerRows[2][17]): 'Населенный пункт (село, поселок и т.д.)']),
            ([(headerRows[2][18]): 'Код страны по классификатору ОКСМ (цифровой)']),
            ([(headerRows[2][19]): '\"Регион (код)\"']),
            ([(headerRows[2][20]): 'Город']),
            ([(headerRows[2][21]): 'Населенный пункт (село, поселок и т.д.)']),

            ([(headerRows[3][0]) : 'гр. 1']),
            ([(headerRows[3][1]) : 'гр. 2.1']),
            ([(headerRows[3][2]) : 'гр. 2.2']),
            ([(headerRows[3][3]) : 'гр. 3']),
            ([(headerRows[3][4]) : 'гр. 4.1']),
            ([(headerRows[3][5]) : 'гр. 4.2']),
            ([(headerRows[3][6]) : 'гр. 5']),
            ([(headerRows[3][7]) : 'гр. 6']),
            ([(headerRows[3][8]) : 'гр. 7']),
            ([(headerRows[3][9]): 'гр. 8']),
            ([(headerRows[3][10]): 'гр. 9']),
            ([(headerRows[3][11]): 'гр. 10']),
            ([(headerRows[3][12]): 'гр. 11']),
            ([(headerRows[3][13]): 'гр. 12']),
            ([(headerRows[3][14]): 'гр. 13.1']),
            ([(headerRows[3][15]): 'гр. 13.2']),
            ([(headerRows[3][16]): 'гр. 13.3']),
            ([(headerRows[3][17]): 'гр. 13.4']),
            ([(headerRows[3][18]): 'гр. 14.1']),
            ([(headerRows[3][19]): 'гр. 14.2']),
            ([(headerRows[3][20]): 'гр. 14.3']),
            ([(headerRows[3][21]): 'гр. 14.4']),
            ([(headerRows[3][22]): 'гр. 15']),
            ([(headerRows[3][23]): 'гр. 16']),
            ([(headerRows[3][24]): 'гр. 17']),
            ([(headerRows[3][25]): 'гр. 18']),
            ([(headerRows[3][26]): 'гр. 19']),
            ([(headerRows[3][27]): 'гр. 20']),
            ([(headerRows[3][28]): 'гр. 21'])
    ]
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    def String iksrName = getColumnName(newRow, 'iksr')
    def nameFromFile = values[1]

    def int colIndex = 1

    def recordId = getTcoRecordId(nameFromFile, values[3], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
    def map = getRefBookValue(520, recordId)

    // графа 2.1
    newRow.name = recordId
    colIndex++

    // графа 2.2
    newRow.dependence = getRecordIdImport(38, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 3
    if (map != null) {
        def expectedValues = [ map.INN?.value, map.REG_NUM?.value, map.TAX_CODE_INCORPORATION?.value, map.SWIFT?.value, map.KIO?.value ]
        expectedValues = expectedValues.unique().findAll{ it != null && it != '' }
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'iksr'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    def countryMap
    // графа 4.1
    if (map != null) {
        countryMap = getRefBookValue(10, map.COUNTRY_CODE?.referenceValue)
        if (countryMap != null) {
            def expectedValues = [countryMap.NAME?.stringValue, countryMap.FULLNAME?.stringValue]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'countryName'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
        }
    }
    colIndex++

    // графа 4.2
    if (countryMap != null) {
        formDataService.checkReferenceValue(values[colIndex], [countryMap.CODE?.stringValue], getColumnName(newRow, 'countryCode'), map.NAME.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 5
    newRow.docNumber = values[colIndex]
    colIndex++

    // графа 6
    newRow.docDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 7
    newRow.dealNumber = values[colIndex]
    colIndex++

    // графа 8
    newRow.dealDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 9
    newRow.dealFocus = getRecordIdImport(20, 'DIRECTION', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 10
    newRow.signPhis = getRecordIdImport(18, 'SIGN', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 11
    newRow.metalName = getRecordIdImport(17, 'INNER_CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 12
    newRow.foreignDeal = getRecordIdImport(38, 'VALUE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13.1
    newRow.countryCodeNumeric = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13.2
    String code = values[colIndex]
    if (code.length() == 1) {    //для кодов 1, 2, 3...9
        code = "0".concat(code)
    }
    newRow.regionCode = getRecordIdImport(4, 'CODE', code, fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 13.3
    newRow.city = values[colIndex]
    colIndex++

    // графа 13.4
    newRow.locality = values[colIndex]
    colIndex++

    // графа 14.1
    newRow.countryCodeNumeric2 = getRecordIdImport(10, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 14.2
    code = values[colIndex]
    if (code.length() == 1) {    //для кодов 1, 2, 3...9
        code = "0".concat(code)
    }
    newRow.region2 = getRecordIdImport(4, 'CODE', code, fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 14.3
    newRow.city2 = values[colIndex]
    colIndex++

    // графа 14.4
    newRow.locality2 = values[colIndex]
    colIndex++

    // графа 15
    newRow.deliveryCode = getRecordIdImport(63, 'STRCODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 16
    newRow.count = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 17
    newRow.incomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 18
    newRow.outcomeSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 19
    newRow.price = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 20
    newRow.total = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 21
    newRow.dealDoneDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}