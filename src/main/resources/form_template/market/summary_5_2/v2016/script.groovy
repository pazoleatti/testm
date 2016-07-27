package form_template.market.summary_5_2.v2016

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 5.2 Отчет о выданных Банком кредитах
 * formTemplateId = 907
 *
 * @author Bulat Kinzyabulatov
 *
 * графа 1  - rowNum               - № пп
 * графа 2  - dealNum              - Номер сделки
 * графа 3  - debtorName           - Заёмщик. Наименование заёмщика и ОПФ
 * графа 4  - country              - Заёмщик. Страна регистрации (местоположения заемщика)
 * графа 5  - relatedPerson        - Заёмщик. Взаимозависимое лицо Банка (Да / Нет)
 * графа 6  - offshore             - Заёмщик. Резидент оффшорной зоны (Да / Нет)
 * графа 7  - innKio               - Заёмщик. ИНН / КИО заёмщика
 * графа 8  - creditRating         - Кредитный договор. Кредитный рейтинг / класс кредитоспособности
 * графа 9  - internationalRating  - Кредитный договор. Международный кредитный рейтинг
 * графа 10 - docNum               - Кредитный договор. Номер кредитного договора
 * графа 11 - docDate              - Кредитный договор. Дата кредитного договора (дд.мм.гг.)
 * графа 12 - creditDate           - Кредитный договор. Дата выдачи (дд.мм.гг.)
 * графа 13 - closeDate            - Кредитный договор. Дата планируемого погашения с учетом последней пролонгации (дд.мм.гг.)
 * графа 14 - partRepayment        - Кредитный договор. Частичное погашение основного долга (Да / Нет)
 * графа 15 - avgPeriod            - Кредитный договор. Средневзвешенный срок кредита, лет
 * графа 16 - currencyCode         - Кредитный договор. Валюта суммы кредита
 * графа 17 - creditSum            - Кредитный договор. Сумма кредита (по договору), ед. валюты
 * графа 18 - rateType             - Кредитный договор. Вид процентной ставки
 * графа 19 - rateBase             - Кредитный договор. База для расчета процентной ставки
 * графа 20 - rate                 - Кредитный договор. Процентная ставка, % годовых
 * графа 21 - creditRate           - Кредитный договор. Ставка Кредитных плат, % годовых
 * графа 22 - totalRate            - Кредитный договор. Совокупная процентная ставка, % годовых
 * графа 23 - provideCategory      - Кредитный договор. Категория обеспечения
 * графа 24 - specialPurpose       - Целевые источники финансирования. Использование целевого финансирования (Да / Нет)
 * графа 25 - purposeSum           - Целевые источники финансирования. Стоимость Целевого финансирования, % годовых
 * графа 26 - purposeFond          - Целевые источники финансирования. Стоимость фондирования Целевого привлечения, % годовых
 * графа 27 - economySum           - Целевые источники финансирования. Экономия по стоимости ресурсов, % годовых
 * графа 28 - economyRate          - Целевые источники финансирования. Совокупная процентная ставка с учетом корректировки на показатель Экономии по стоимости ресурсов, % годовых
 * графа 29 - groupExclude         - Исключить из группировки (Да / Нет)
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

@Field
def allColumns = ['rowNum', 'dealNum', 'debtorName', 'country', 'relatedPerson', 'offshore', 'innKio', 'creditRating',
                  'internationalRating', 'docNum', 'docDate', 'creditDate', 'closeDate', 'partRepayment', 'avgPeriod',
                  'currencyCode', 'creditSum', 'rateType', 'rateBase', 'rate', 'creditRate', 'totalRate', 'provideCategory',
                  'specialPurpose', 'purposeSum', 'purposeFond', 'economySum', 'economyRate', 'groupExclude']

@Field
def editableColumns = ['groupExclude']


// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['dealNum', 'debtorName', 'relatedPerson', 'offshore', 'innKio', 'creditRating',
                       'docNum', 'docDate', 'closeDate', 'partRepayment', 'avgPeriod',
                       'currencyCode', 'creditSum', 'rateType', 'rate', 'creditRate', 'totalRate', 'provideCategory',
                       'specialPurpose', 'purposeSum', 'purposeFond', 'economySum', 'economyRate', 'groupExclude']

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
}

@Field
def refBookCache = [:]

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// "Да"
@Field
def Long recYesId

def Long getRecYesId() {
    if (recYesId == null)
        recYesId = getRecordId(38, 'CODE', '1')
    return recYesId
}

// "Нет"
@Field
def Long recNoId

def Long getRecNoId() {
    if (recNoId == null)
        recNoId = getRecordId(38, 'CODE', '0')
    return recNoId
}

// "ВЗЛ"
@Field
def Long vzlId

def Long getVzlTypeId() {
    if (vzlId == null)
        vzlId = getRecordId(525, 'CODE', 'ВЗЛ')
    return vzlId
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
        // 2. Проверка валюты
        if (row.currencyCode != null && ("RUB".equals(getRefBookValue(15, row.currencyCode).CODE_2.value))) {
            logger.error("Строка %s: Для российского рубля должно быть проставлено буквенное значение RUR!", row.getIndex())
        }
        // 3. Неотрицательность графы
        if (row.economyRate != null && row.economyRate < 0) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно 0!", row.getIndex(), getColumnName(row, 'economyRate'))
        }
        // 4. Проверка ИНН на паттерн
        if (row.innKio && !checkFormat(row.innKio, INN_JUR_PATTERN)) {
            logger.error("Строка %s: Значение графы «%s» должно состоять из 10 цифр. Первые две цифры принимают значения 0-9 и 1-9 ИЛИ 1-9 и 0-9!",
                    row.getIndex(), getColumnName(row, 'innKio'))
        } else if (!exclusiveInns.contains(row.innKio)) { // Проверка контрольного числа ИНН
            checkControlSumInn(logger, row, 'innKio', row.innKio, true)
        }
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        calc9(row)
        calc15(row)
        calc22(row)
        calc27(row)
        calc28(row)
    }
}

@Field
def recordCache = [:]

@Field
def providerCache = [:]

def getRatingRecord(def rating) {
    def ratingString = getRefBookValue(604, rating)?.NAME?.value
    def filter = 'LOWER(CREDIT_RATING) = LOWER(\'' + ratingString + '\')'
    if (recordCache[filter] == null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, 603L, providerCache)
        recordCache.put(filter, provider.getRecords(getReportPeriodEndDate(), null, filter, null))
    }
    def records = recordCache[filter]
    if (records != null && !records.isEmpty()) {
        return records[0]
    }
    return null
}

void calc9(def row) {
    row.internationalRating = getRatingRecord(row.creditRating)?.record_id?.value
}

void calc15(def row) {
    if (row.docDate == null) {
        return
    }
    def start
    if (row.creditDate != null) {
        start = [row.docDate, row.creditDate].min()
    } else {
        start = row.docDate
    }
    def end = row.closeDate
    def period = new BigDecimal((end - start).toString())
    row.avgPeriod = period.divide(new BigDecimal("365"), 1, RoundingMode.HALF_UP)
}

// Графа 22 = графа 20 + графа 21
void calc22(def row) {
    if (row.rate != null && row.creditRate != null) {
        row.totalRate = row.rate + row.creditRate
    }
}

// Графа 27 = графа 26 – графа 25
void calc27(def row) {
    if (row.purposeFond != null && row.purposeSum != null) {
        row.economySum = row.purposeFond - row.purposeSum
    }
}

// Графа 28 = графа 22 + графа 27
void calc28(def row) {
    if (row.totalRate != null && row.economySum != null) {
        row.economyRate = row.totalRate + row.economySum
    }
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

@Field
def formTypeMis = 901

@Field
def formTypeAmrlirt = 902

@Field
def formTypeChd = 903

@Field
def formType_2_6 = 904

@Field
def formType_7_129 = 906

void consolidation() {
    List<Relation> sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)
    Map<Integer, List<DataRow>> rows_2_6Map = [:]
    Map<String, List<DataRow<Cell>>> rowsChdMap = [:]
    Map<String, List<DataRow<Cell>>> rowsMisMap = [:]
    Map<String, List<DataRow<Cell>>> rows7_219Map = [:]
    Map<String, List<DataRow<Cell>>> rowsAmrlirtMap = [:]
    sourcesInfo.each { Relation sourceInfo ->
        switch (sourceInfo.formType.id) {
            case formType_2_6:
                // выбираем подходящие строки
                selectRows_2_6(sourceInfo.formDataId, rows_2_6Map)
                break
            case formTypeChd:
                // группируем
                fillComplexMap(sourceInfo.formDataId, rowsChdMap, 'innKio', 'docNum', 'docDate')
                break
            case formTypeMis:
                fillComplexMap(sourceInfo.formDataId, rowsMisMap, 'innKio', 'docNum', 'docDate')
                break
            case formType_7_129:
                fillComplexMap(sourceInfo.formDataId, rows7_219Map, 'inn', 'docNum', 'docDate')
                break
            case formTypeAmrlirt:
                fillSimpleMap(sourceInfo.formDataId, rowsAmrlirtMap, 'crmId')
                break
        }
    }
    List<DataRow> dataRows = []
    rows_2_6Map.each { def formDataId, rows2_6 ->
        FormData formData2_6 = getFormData(formDataId)
        def depName = getDepartment(formData2_6.departmentId).name
        def periodName = getReportPeriod(formData2_6.reportPeriodId).name
        rows2_6.each { row2_6 ->
            def key = getKey(row2_6, 'inn', 'docNum', 'docDate')
            def rowChd = rowsChdMap[key]?.get(0)
            def rowMis = rowsMisMap[key]?.get(0)
            def row7_129 = rows7_219Map[key]?.get(0)
            def rowAmrlirt = row7_129 ? rowsAmrlirtMap[row7_129.productId?.toLowerCase()]?.get(0) : null

            if (rowChd == null || rowMis == null) {
                def formNames = ""
                if (rowChd == null) {
                    formNames += ("«" + getFormTypeName(formTypeChd) + "»")
                }
                if (rowMis == null) {
                    if (!formNames.isEmpty()) {
                        formNames += ", "
                    }
                    formNames += ("«" + getFormTypeName(formTypeMis) + "»")
                }
                logger.warn("Форма-источник «%s», Подразделение = «%s», Период = «%s», строка %s: Сделка не была включена в отчет, т.к. не найдена связанная строка в формах-источниках: %s!",
                        getFormTypeName(formType_2_6), depName, periodName, row2_6.getIndex(), formNames)
            } else {
                def newRow = getNewRow()
                fillRow(newRow, row2_6, rowChd, rowMis, rowAmrlirt)
                dataRows.add(newRow)
            }
        }
    }
    sortRows(refBookService, logger, dataRows, null, null, null)
    updateIndexes(dataRows)
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.allCached = dataRows
}

// выбор подходящих строк из сводной 2.6
void selectRows_2_6(def formDataId, def dataRows_2_6Map) {
    def sourceRows = formDataService.getDataRowHelper(formDataService.get(formDataId, null)).allSaved
    sourceRows.each { row ->
        if (checkGetRow_2_6(row)) {
            if (dataRows_2_6Map[formDataId] == null) {
                dataRows_2_6Map[formDataId] = []
            }
            dataRows_2_6Map[formDataId].add(row)
        }
    }
}

@Field
def formTypeNameMap = [:]

def getFormTypeName(def formTypeId) {
    if (formTypeNameMap[formTypeId] == null) {
        formTypeNameMap[formTypeId] = formTypeService.get(formTypeId)?.name
    }
    return formTypeNameMap[formTypeId]
}

@Field
def formDataMap = [:]

FormData getFormData(def formDataId) {
    if (formDataMap[formDataId] == null) {
        formDataMap[formDataId] = formDataService.get(formDataId, null)
    }
    return formDataMap[formDataId]
}

@Field
def departmentMap = [:]

Department getDepartment(def deparmentId) {
    if (departmentMap[deparmentId] == null) {
        departmentMap[deparmentId] = departmentService.get(deparmentId)
    }
    return departmentMap[deparmentId]
}

@Field
def periodMap = [:]

ReportPeriod getReportPeriod(def periodId) {
    if (periodMap[periodId] == null) {
        periodMap[periodId] = reportPeriodService.get(periodId)
    }
    return periodMap[periodId]
}

// нужно ли учитывать строку при консолидации
boolean checkGetRow_2_6(def row) {
        // -	графа 9 «Признак СМП» принимает значение «0» или «3»
    return [0, 3].contains(((BigDecimal) row.sign)?.intValue()) &&
        // -	значение графы 11 «Номер Регламента, в рамках которого предоставлен кредит» не начинается на «1221» или «1948»
        !(['1221', '1948'].contains(row.law)) &&
        // -	не существует такой строки в справочнике «Исключаемые типы кредитов», у которой значение поля «Код» или «Наименование» совпадает со значением графы 12 «Тип кредита»
        !existExcludedCreditType(row.creditType) &&
        // -	значение графы 6 «Организационно-правовая форма» не равна значениям «91» и «98»
        !(['91', '98'].contains(getRefBookValue(605L, row.opf)?.CODE?.value)) &&
        // -	длина строки, указанной в графе 8 «ИНН заемщика», равно 10
        (row.inn?.length() == 10) &&
        // -	значение графы 14 «Дата кредитного договора» больше либо равно дате начала отчетного периода формы-источника «2.6 (Сводный) Отчет о состоянии кредитного портфеля»
        (row.docDate >= getReportPeriodStartDate()) &&
        // -	значение графы 14 «Дата кредитного договора» меньше либо равно дате окончания отчетного периода формы-источника «2.6 (Сводный) Отчет о состоянии кредитного портфеля»
        (row.docDate <= getReportPeriodEndDate())
}

// не существует такой строки в справочнике «Исключаемые типы кредитов», у которой значение поля «Код» или «Наименование» совпадает со значением графы 12 «Тип кредита»
boolean existExcludedCreditType(def creditType) {
    def filter = 'LOWER(CODE) = LOWER(\'' + creditType + '\') OR LOWER(NAME) = LOWER(\'' + creditType + '\')'
    if (recordCache[filter] == null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, 607L, providerCache)
        recordCache.put(filter, provider.getRecords(getReportPeriodEndDate(), null, filter, null))
    }
    def records = recordCache[filter]
    return records != null && !records.isEmpty()
}

void fillComplexMap(def formDataId, def rowsMap, def innAlias, def docNumAlias, def docDateAlias) {
    def sourceRows = formDataService.getDataRowHelper(formDataService.get(formDataId, null)).allSaved
    sourceRows.each { row ->
        def key = getKey(row, innAlias, docNumAlias, docDateAlias)
        if (rowsMap[key] == null) {
            rowsMap[key] = []
        }
        rowsMap[key].add(row)
    }
}

String getKey(def row, def innAlias, def docNumAlias, def docDateAlias) {
    return (row[innAlias]?.trim() + "#" + row[docNumAlias]?.trim() + "#" + row[docDateAlias]?.format('dd.MM.yyyy')).toLowerCase()
}

void fillSimpleMap(def formDataId, def rowsMap, def alias) {
    def sourceRows = formDataService.getDataRowHelper(formDataService.get(formDataId, null)).allSaved
    sourceRows.each { row ->
        def key = row[alias]?.trim()?.toLowerCase()
        if (rowsMap[key] == null) {
            rowsMap[key] = []
        }
        rowsMap[key].add(row)
    }
}

def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

void fillRow(def newRow, def row2_6, def rowChd, def rowMis, def rowAmrlirt) {
    // графа 2  - dealNum              - Номер сделки
    newRow.dealNum = rowAmrlirt ? rowAmrlirt.crmId : row2_6.codeBank
    // графа 3  - debtorName           - Заёмщик. Наименование заёмщика и ОПФ
    newRow.debtorName = row2_6.debtorName
    // графа 4  - country              - Заёмщик. Страна регистрации (местоположения заемщика)
    newRow.country = rowChd.country
    // графа 5  - relatedPerson        - Заёмщик. Взаимозависимое лицо Банка (Да / Нет)
    newRow.relatedPerson = isRelatedPerson(row2_6.inn, row2_6.docDate) ? getRecYesId() : getRecNoId()
    // графа 6  - offshore             - Заёмщик. Резидент оффшорной зоны (Да / Нет)
    newRow.offshore = isOffshore(rowChd.country, row2_6.docDate) ? getRecYesId() : getRecNoId()
    // графа 7  - innKio               - Заёмщик. ИНН / КИО заёмщика
    newRow.innKio = row2_6.inn
    // графа 8  - creditRating         - Кредитный договор. Кредитный рейтинг / класс кредитоспособности
    newRow.creditRating = row2_6.creditRisk
    // графа 9  - internationalRating  - Кредитный договор. Международный кредитный рейтинг
    // skip
    // графа 10 - docNum               - Кредитный договор. Номер кредитного договора
    newRow.docNum = row2_6.docNum
    // графа 11 - docDate              - Кредитный договор. Дата кредитного договора (дд.мм.гг.)
    newRow.docDate = row2_6.docDate
    // графа 12 - creditDate           - Кредитный договор. Дата выдачи (дд.мм.гг.)
    newRow.creditDate = row2_6.creditDate
    // графа 13 - closeDate            - Кредитный договор. Дата планируемого погашения с учетом последней пролонгации (дд.мм.гг.)
    newRow.closeDate = row2_6.closeDate
    // графа 14 - partRepayment        - Кредитный договор. Частичное погашение основного долга (Да / Нет)
    newRow.partRepayment = rowChd.partRepayment
    // графа 15 - avgPeriod            - Кредитный договор. Средневзвешенный срок кредита, лет
    // skip
    // графа 16 - currencyCode         - Кредитный договор. Валюта суммы кредита
    newRow.currencyCode = row2_6.currencySum
    // графа 17 - creditSum            - Кредитный договор. Сумма кредита (по договору), ед. валюты
    newRow.creditSum = (row2_6.sumDoc != null) ? (row2_6.sumDoc * 1000) : null
    // графа 18 - rateType             - Кредитный договор. Вид процентной ставки
    newRow.rateType = rowMis.rateType
    // графа 19 - rateBase             - Кредитный договор. База для расчета процентной ставки
    newRow.rateBase = rowMis.rateBase
    // графа 20 - rate                 - Кредитный договор. Процентная ставка, % годовых
    newRow.rate = row2_6.rate
    // графа 21 - creditRate           - Кредитный договор. Ставка Кредитных плат, % годовых
    newRow.creditRate = rowMis.creditRate
    // графа 22 - totalRate            - Кредитный договор. Совокупная процентная ставка, % годовых
    // skip
    // графа 23 - provideCategory      - Кредитный договор. Категория обеспечения
    newRow.provideCategory = getProvideCategory(rowAmrlirt, row2_6)
    // графа 24 - specialPurpose       - Целевые источники финансирования. Использование целевого финансирования (Да / Нет)
    newRow.specialPurpose = rowMis.specialPurpose
    // графа 25 - purposeSum           - Целевые источники финансирования. Стоимость Целевого финансирования, % годовых
    newRow.purposeSum = rowMis.fondRate
    // графа 26 - purposeFond          - Целевые источники финансирования. Стоимость фондирования Целевого привлечения, % годовых
    newRow.purposeFond = rowMis.etsRate
    // графа 27 - economySum           - Целевые источники финансирования. Экономия по стоимости ресурсов, % годовых
    // skip
    // графа 28 - economyRate          - Целевые источники финансирования. Совокупная процентная ставка с учетом корректировки на показатель Экономии по стоимости ресурсов, % годовых
    // skip
    // графа 29 - groupExclude         - Исключить из группировки (Да / Нет)
    newRow.groupExclude = (newRow.relatedPerson == getRecYesId() && newRow.offshore == getRecYesId()) ? getRecYesId() : getRecNoId()

}

@Field
def exclusiveInns = ['9999999999', '9999999998']

def getRecords(def refBookId, def filter, def recordDate) {
    def provider = formDataService.getRefBookProvider(refBookFactory, refBookId, providerCache)
    def key = refBookId + filter + recordDate.format('dd.MM.yyyy')
    if (recordCache[key] == null) {
        recordCache.put(key, provider.getRecords(recordDate, null, filter, null))
    }
    return recordCache[key]
}

boolean isRelatedPerson(def inn, def docDate) {
    if (exclusiveInns.contains(inn)) {
        return false
    }
    def filter = 'LOWER(INN) = LOWER(\'' + inn + '\') OR LOWER(KIO) = LOWER(\'' + inn + '\')'
    def records = getRecords(520L, filter, docDate)
    def record = records.find { record ->
        getVzlTypeId() == record.TYPE?.value &&
                record.START_DATE?.value <= docDate &&
                (record.END_DATE?.value == null || (record.END_DATE?.value >= docDate))
    }
    return record != null
}

boolean isOffshore(def countryId, def docDate) {
    if (countryId == null) {
        return false
    }
    def countryName = getRefBookValue(10, countryId)?.NAME?.value
    def filter = 'LOWER(NAME) = LOWER(\'' + countryName + '\') OR LOWER(SHORTNAME) = LOWER(\'' + countryName + '\')'
    def records = getRecords(519L, filter, docDate)
    return records != null && !(records.isEmpty())
}

def getProvideCategory(def rowAmrLirt, def row2_6) {
    if (rowAmrLirt != null) {
        def value = rowAmrLirt.lgd
        if (value >= 0 && value <= 23) {
            return getProvideCategoryId('Полностью обеспеченный')
        } else if (value > 23 && value <= 70) {
            return getProvideCategoryId('Частично обеспеченный')
        } else if (value > 70){
            return getProvideCategoryId('Необеспеченный')
        }
    } else {
        if ("Без обеспечения".equalsIgnoreCase(row2_6.provision?.trim()) ||
                "Без основного обеспечения".equalsIgnoreCase(row2_6.provision?.trim())) {
            return getProvideCategoryId('Необеспеченный')
        } else {
            return getProvideCategoryId('Полностью обеспеченный')
        }
    }
}

def getProvideCategoryId(String name) {
    return getRecords(606, "LOWER(NAME)=LOWER('$name')", getReportPeriodEndDate())?.get(0)?.record_id?.value
}