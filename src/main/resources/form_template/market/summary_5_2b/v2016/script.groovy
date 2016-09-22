package form_template.market.summary_5_2b.v2016

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.Logger
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 5.2(б) Отчет о выданных Банком гарантиях, аккредитивах и ИТФ
 * formTemplateId = 915
 *
 * @author Bulat Kinzyabulatov
 *
 * графа 1  - rowNum              - № пп
 * графа 2  - code                - ID сделки в CRM
 * графа 3  - name                - Информация о Клиенте. Наименование Клиента и ОПФ
 * графа 4  - country             - Информация о Клиенте. Страна регистрации (местоположения Клиента)
 * графа 5  - relatedPerson       - Информация о Клиенте. Взаимозависимое лицо Банка (Да / Нет)
 * графа 6  - offshore            - Информация о Клиенте. Резидент оффшорной зоны (Да / Нет)
 * графа 7  - innKio              - Информация о Клиенте. ИНН / КИО Клиента
 * графа 8  - creditRating        - Информация о Продукте / ИТФ. Кредитный рейтинг
 * графа 9  - internationalRating - Информация о Продукте / ИТФ. Международный кредитный рейтинг
 * графа 10 - number              - Информация о Продукте / ИТФ. Номер обязательства
 * графа 11 - issuanceDate        - Информация о Продукте / ИТФ. Дата выдачи обязательства
 * графа 12 - docDate             - Информация о Продукте / ИТФ. Дата заключения договора
 * графа 13 - endDate             - Информация о Продукте / ИТФ. Дата окончания действия обязательства
 * графа 14 - beneficiaryName     - Информация о Продукте / ИТФ. Наименование Бенефициара
 * графа 15 - beneficiaryInn      - Информация о Продукте / ИТФ. ИНН Бенефициара
 * графа 16 - period              - Информация о Продукте / ИТФ. Срок обязательства, лет
 * графа 17 - obligationType      - Информация о Продукте / ИТФ. Вид обязательства
 * графа 18 - currencyCode        - Информация о Продукте / ИТФ. Валюта обязательства
 * графа 19 - sum                 - Информация о Продукте / ИТФ. Сумма обязательства (по договору), ед. валюты
 * графа 20 - rate                - Информация о Продукте / ИТФ. Ставка вознаграждения, в % годовых
 * графа 21 - provisionPresence   - Информация о Продукте / ИТФ. Наличие обеспечения (Да / Нет)
 * графа 22 - currencyRate        - Информация о Продукте / ИТФ. Курс на дату выдачи обязательства
 * графа 23 - endSum              - Информация о Продукте / ИТФ. Сумма обязательства в рублях на дату выдачи
 * графа 24 - groupExclude        - Исключить из группировки (Да / Нет)
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
}

@Field
def allColumns = ['rowNum', 'code', 'name', 'country', 'relatedPerson', 'offshore', 'innKio', 'creditRating',
                  'internationalRating', 'number', 'issuanceDate', 'docDate', 'endDate', 'beneficiaryName',
                  'beneficiaryInn', 'period', 'obligationType', 'currencyCode', 'sum', 'rate',
                  'provisionPresence', 'currencyRate', 'endSum', 'groupExclude']

@Field
def editableColumns = ['groupExclude']


// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'relatedPerson', 'offshore', 'innKio', 'creditRating',
                       'internationalRating', 'issuanceDate', 'endDate', 'period', 'obligationType', 'currencyCode',
                       'sum', 'rate', 'currencyRate', 'endSum', 'groupExclude']

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
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, null,
            getReportPeriodEndDate(), -1, null, logger, true)
}

@Field
def refBookCache = [:]

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(Long refBookId, String alias, String value, String msgValue, Date day, int rowIndex, String cellName, Logger logger,
        boolean required) {
    if (value == null) {
        return null
    }
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, msgValue,
            day, rowIndex, cellName, logger, required)
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
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // графа 16
        row.period = calc16(row)
        // графа 22
        row.currencyRate = calc22(row)
        // графа 23
        row.endSum = calc23(row)
    }
}

@Field
def recordCache = [:]

@Field
def providerCache = [:]

// Графа 16 = ОКРУГЛ ((ДНИ(графа 13; графа 11) +1) / 365; 2)
def calc16(def row) {
    if (row.endDate == null || row.issuanceDate == null) {
        return null
    }
    def period = new BigDecimal((row.endDate - row.issuanceDate + 1).toString())
    return period.divide(new BigDecimal("365"), 2, RoundingMode.HALF_UP)
}

def calc22(def row) {
    if (row.issuanceDate == null || row.currencyCode == null) {
        return null
    }
    def code = getRefBookValue(15, row.currencyCode)?.CODE_2?.value
    if ('RUR'.equals(code)) {
        return 1
    }
    // получить запись (поле курс валюты) из справочника курс валют (22) по буквенному коду валюты
    def record22 = getRefBookRecord(22, 'CODE_LETTER', row.currencyCode.toString(), code, row.issuanceDate, row.getIndex(), null, new Logger(), false)
    if (record22 == null) {
        logger.error("Строка %s: В справочнике «Курсы валют» не найден курс валюты для «%s» на дату %s!",
            row.getIndex(), code, row.issuanceDate.format('dd.MM.yyyy'))
        return null
    }
    return record22?.RATE?.value
}

// Графа 23 = ОКРУГЛ (графа 19 * графа 22; 2)
def calc23(def row) {
    if (row.sum == null || row.currencyRate == null) {
        return null
    }
    return ((BigDecimal)(row.sum * row.currencyRate)).setScale(2, RoundingMode.HALF_UP)
}

@Field
def formType_2_1 = 910

@Field
def formType_5_2a = 911

@Field
def formTypeLetter = 913

enum SourceType {
    TYPE_2_1, // 2.1 (Сводный) Реестр выданных Банком гарантий (контргарантий, поручительств)
    TYPE_5_2A, // 5.2(а) Отчет о выданных Банком инструментах торгового финансирования
    TYPE_LETTER // Данные по непокрытым аккредитивам
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    List<DataRow> dataRows = []
    List<Relation> sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)
    sourcesInfo.each { Relation sourceInfo ->
        switch (sourceInfo.formType.id) {
            case formType_2_1:
                addSourceRows(sourceInfo.formDataId, dataRows, SourceType.TYPE_2_1)
                break
            case formType_5_2a:
                addSourceRows(sourceInfo.formDataId, dataRows, SourceType.TYPE_5_2A)
                break
            case formTypeLetter:
                addSourceRows(sourceInfo.formDataId, dataRows, SourceType.TYPE_LETTER)
                break
        }
    }
    updateIndexes(dataRows)
    dataRowHelper.setAllCached(dataRows)
}

void addSourceRows(def formDataId, def dataRows, SourceType sourceType) {
    def sourceFormData = formDataService.get(formDataId, null)
    def sourceRows = formDataService.getDataRowHelper(sourceFormData).allSaved
    if (sourceType == SourceType.TYPE_2_1) {
        sourceRows = sourceRows.findAll { row ->
            checkGetRow_2_1(row)
        }
    } else if (sourceType == SourceType.TYPE_LETTER) {
        sourceRows = sourceRows.findAll { row ->
            checkGetRowLetter(row)
        }
    }
    sourceRows.eachWithIndex { sourceRow, index ->
        def newRow = getNewRow();
        newRow.setIndex(index + 1);
        dataRows.add(newRow)
        fillNewRow(newRow, sourceRow, sourceType, sourceFormData)
    }
}

void fillNewRow(def newRow, def sourceRow, SourceType sourceType, def sourceFormData) {
    // графа 1  - rowNum              - № пп
    // графа 2  - code                - ID сделки в CRM
    newRow.code = calc2(sourceRow, sourceType)
    // графа 3  - name                - Информация о Клиенте. Наименование Клиента и ОПФ
    newRow.name = calc3(newRow, sourceRow, sourceType)
    // графа 4  - country             - Информация о Клиенте. Страна регистрации (местоположения Клиента)
    newRow.country = calc4(newRow, sourceRow, sourceType)
    // графа 5  - relatedPerson       - Информация о Клиенте. Взаимозависимое лицо Банка (Да / Нет)
    newRow.relatedPerson = isRelatedPerson(sourceRow, sourceType) ? getRecYesId() : getRecNoId()
    // графа 6  - offshore            - Информация о Клиенте. Резидент оффшорной зоны (Да / Нет)
    newRow.offshore = isOffshore(newRow.country, sourceRow, sourceType) ? getRecYesId() : getRecNoId()
    // графа 7  - innKio              - Информация о Клиенте. ИНН / КИО Клиента
    newRow.innKio = sourceRow[getInnAlias(sourceType)]
    // графа 8  - creditRating        - Информация о Продукте / ИТФ. Кредитный рейтинг
    newRow.creditRating = sourceRow.creditRating // alias совпал
    // графа 9  - internationalRating - Информация о Продукте / ИТФ. Международный кредитный рейтинг
    newRow.internationalRating = calc9(newRow)
    // графа 10 - number              - Информация о Продукте / ИТФ. Номер обязательства
    newRow.number = calc10(sourceRow, sourceType)
    // графа 11 - issuanceDate        - Информация о Продукте / ИТФ. Дата выдачи обязательства
    newRow.issuanceDate = sourceRow[getDateAlias(sourceType)]
    // графа 12 - docDate             - Информация о Продукте / ИТФ. Дата заключения договора
    newRow.docDate = calc12(sourceRow, sourceType)
    // графа 13 - endDate             - Информация о Продукте / ИТФ. Дата окончания действия обязательства
    newRow.endDate = calc13(sourceRow, sourceType)
    // графа 14 - beneficiaryName     - Информация о Продукте / ИТФ. Наименование Бенефициара
    newRow.beneficiaryName = calc14(sourceRow, sourceType)
    // графа 15 - beneficiaryInn      - Информация о Продукте / ИТФ. ИНН Бенефициара
    newRow.beneficiaryInn = calc15(sourceRow, sourceType)
    // графа 16 - period              - Информация о Продукте / ИТФ. Срок обязательства, лет
    // skip
    // графа 17 - obligationType      - Информация о Продукте / ИТФ. Вид обязательства
    newRow.obligationType = calc17(sourceRow, sourceType, sourceFormData)
    // графа 18 - currencyCode        - Информация о Продукте / ИТФ. Валюта обязательства
    newRow.currencyCode = sourceRow.currency // alias совпал
    // графа 19 - sum                 - Информация о Продукте / ИТФ. Сумма обязательства (по договору), ед. валюты
    newRow.sum = calc19(sourceRow, sourceType)
    // графа 20 - rate                - Информация о Продукте / ИТФ. Ставка вознаграждения, в % годовых
    newRow.rate = calc20(sourceRow, sourceType)
    // графа 21 - provisionPresence   - Информация о Продукте / ИТФ. Наличие обеспечения (Да / Нет)
    newRow.provisionPresence = calc21(sourceRow, sourceType)
    // графа 22 - currencyRate        - Информация о Продукте / ИТФ. Курс на дату выдачи обязательства
    // skip
    // графа 23 - endSum              - Информация о Продукте / ИТФ. Сумма обязательства в рублях на дату выдачи
    // skip
    // графа 24 - groupExclude        - Исключить из группировки (Да / Нет)
    newRow.groupExclude = calc24(newRow)
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
boolean checkGetRow_2_1(def row) {
    // -	значение графы 20 «Дата выдачи обязательства» больше либо равно даты начала отчетного периода текущей формы (01.01)
    return (row.issuanceDate >= getReportPeriodStartDate()) &&
            // -	значение графы 20 «Дата выдачи обязательства» меньше либо равно даты окончания отчетного периода текущей формы (31.12)
            (row.issuanceDate <= getReportPeriodEndDate()) &&
            // -	длина строки, указанной в графе 11 «ИНН Принципала / Налогоплательщика», равна 10, или значение в графе 11 «ИНН Принципала / Налогоплательщика» начинается на «99999»
            (row.taxpayerInn?.length() == 10 || row.taxpayerInn?.startsWith('99999')) &&
            // -	значение в графе 5 «ВНД, в рамках которого выдано ГО» не равно «Альбом 2008»
            !("Альбом 2008".equals(row.vnd)) &&
            // -	задано значение в графе 24 «Дата окончания действия обязательства»
            (row.endDate != null) &&
            // -	задано значение в графе 13 «Рейтинг»
            (row.creditRating != null)

}

boolean checkGetRowLetter(def row) {
    // -	длина строки, указанной в графе 5 равна 10
    return row.innKio?.length() == 10

}

def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
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

boolean isRelatedPerson(def sourceRow, SourceType sourceType) {
    def innAlias = getInnAlias(sourceType)
    def docDateAlias = getDateAlias(sourceType)
    def inn = sourceRow[innAlias]
    def docDate = sourceRow[docDateAlias]
    if (exclusiveInns.contains(inn)) {
        return false
    }
    def filter = searchAttributes.collect { 'LOWER(' + it + ') = LOWER(\'' + inn + '\')' }.join(" OR ")
    def records = getRecords(520L, filter, docDate)
    def record = records.find { record ->
        getVzlTypeId() == record.TYPE?.value &&
                record.START_DATE?.value <= docDate &&
                (record.END_DATE?.value == null || (record.END_DATE?.value >= docDate))
    }
    return record != null
}

boolean isOffshore(def countryId, def sourceRow, SourceType sourceType) {
    if (countryId == null) {
        return false
    }
    def docDateAlias = getDateAlias(sourceType)
    def docDate = sourceRow[docDateAlias]
    def countryName = getRefBookValue(10, countryId)?.NAME?.value
    def filter = 'LOWER(NAME) = LOWER(\'' + countryName + '\') OR LOWER(SHORTNAME) = LOWER(\'' + countryName + '\')'
    def records = getRecords(519L, filter, docDate)
    return records != null && !(records.isEmpty())
}

def calc2(def rowSource, SourceType sourceType) {
    if (sourceType == SourceType.TYPE_2_1) {
        return rowSource.code
    } else {
        return null
    }
}

def calc3(def row, def rowSource, SourceType sourceType) {
    def tmpValue = rowSource[getNameAlias(sourceType)]
    return calc3or4(row, rowSource, tmpValue, sourceType, false)
}

def calc4(def row, def rowSource, SourceType sourceType) {
    def tmpValue = (sourceType != SourceType.TYPE_2_1) ? rowSource.country : null
    return calc3or4(row, rowSource, tmpValue, sourceType, true)
}

def getRatingRecord(def rating) {
    def ratingString = getRefBookValue(604, rating)?.NAME?.value
    def record = getRefBookRecord(603L, 'CREDIT_RATING', ratingString, null, getReportPeriodEndDate(), -1, null, new Logger(), false)
    return record
}

def calc9(def row) {
    return getRatingRecord(row.creditRating)?.record_id?.value
}

def calc10(def rowSource, SourceType sourceType) {
    switch (sourceType) {
        case SourceType.TYPE_2_1:
            return rowSource.number
        case SourceType.TYPE_5_2A:
            return rowSource.tool
        case SourceType.TYPE_LETTER:
            return rowSource.docNumber
    }
}

def calc12(def rowSource, SourceType sourceType) {
    if (sourceType == SourceType.TYPE_LETTER) {
        return rowSource.docDate
    } else {
        return null
    }
}

def calc13(def rowSource, SourceType sourceType) {
    switch (sourceType) {
        case SourceType.TYPE_2_1:
            return rowSource.endDate
        case SourceType.TYPE_5_2A:
            return rowSource.expireDate
        case SourceType.TYPE_LETTER:
            return rowSource.creditEndDate
    }
}

def calc14(def rowSource, SourceType sourceType) {
    if (sourceType == SourceType.TYPE_2_1) {
        return rowSource.beneficiaryName
    } else {
        return null
    }
}

def calc15(def rowSource, SourceType sourceType) {
    if (sourceType == SourceType.TYPE_2_1) {
        return rowSource.beneficiaryInn
    } else {
        return null
    }
}

def calc17(def rowSource, SourceType sourceType, FormData sourceFormData) {
    if (sourceType == SourceType.TYPE_2_1) {
        def record = getRefBookRecord(609L, 'NAME', rowSource.procuct1, null, getReportPeriodEndDate(), -1, null, new Logger(), false)
        if (record != null) {
            return record.GROUP_CODE.value
        } else {
            def formTypeName = getFormTypeName(sourceFormData.formType.id)
            def departmentName = getDepartment(sourceFormData.departmentId).name
            def periodName = getReportPeriod(sourceFormData.reportPeriodId).name
            logger.error("Форма-источник «%s», Подразделение: «%s», Период: «%s», строка %s: В справочнике «Виды обязательств» отсутствует вид обязательства «%s!",
                    formTypeName, departmentName, periodName, rowSource.getIndex(), rowSource.procuct1)
            return null
        }
    } else {
        return getRecordId(608L, 'NAME', 'Иные гарантии и аккредитивы')
    }
}

def calc19(def rowSource, SourceType sourceType) {
    switch (sourceType) {
        case SourceType.TYPE_2_1:
            return rowSource.sumInCurrency
        case SourceType.TYPE_5_2A:
            return rowSource.sum * 1000
        case SourceType.TYPE_LETTER:
            return rowSource.sum
    }
}

def calc20(def rowSource, SourceType sourceType) {
    switch (sourceType) {
        case SourceType.TYPE_2_1:
            // 1.	Если значение графы 32 формы-источника больше 0, тогда:
            if (rowSource.tariff > 0) {
                // Графа 20 = ОКРУГЛ (графа 32; 2)
                return rowSource.tariff.setScale(2, RoundingMode.HALF_UP)
            } else {
                BigDecimal valueA
                // 2.	Иначе, если значение графы 29 равно «Да», тогда (выделил различия в расчете А)
                if (rowSource.isNonRecurring == getRecYesId()) {
                    // A = ДНИ(графа 24; графа 20) +1
                    valueA = new BigDecimal((rowSource.endDate - rowSource.issuanceDate + 1).toString())
                } else {
                    // A = ДНИ(Дата окончания отчетного периода текущей формы; графа 20) +1
                    valueA = new BigDecimal((getReportPeriodEndDate() - rowSource.issuanceDate + 1).toString())
                }
                // Графа 20 = ОКРУГЛ (графа 35 / A * 360 / графа 26 * 100; 2)
                return (rowSource.remunerationIssuance * 36000).divide(valueA * rowSource.sumInRub, 2, RoundingMode.HALF_UP)
            }
        case SourceType.TYPE_5_2A:
            // Графа 20 =ОКРУГЛ (графа 12 формы-источника; 2)
            return rowSource.payRate?.setScale(2, RoundingMode.HALF_UP)
        case SourceType.TYPE_LETTER:
            // Графа 20 =значение графы 15 формы-источника
            return rowSource.faceValueNum
    }
}

@Field
def spRatingIds = []

def getSpRatingIds() {
    if (spRatingIds.isEmpty()) {
        def ratings = ["AAA", "A", "A-", "A+", "AA", "AA+", "AA-", "BBB+", "BBB", "BBB-", "BB", "BB+", "BB-"]
        def filter = ratings.collect { "NAME = '" + it + "'" }.join(" OR ")
        def records = getRecords(604L, filter, getReportPeriodEndDate())
        if (!records.isEmpty()) {
            spRatingIds = records.collect { it.record_id.value }
        }
    }
    return spRatingIds
}

def calc21(def rowSource, SourceType sourceType) {
    switch (sourceType) {
        case SourceType.TYPE_2_1:
            // 1.	Если значение графы 36 равно одному из значений: «без обеспечения», «без основного обеспечения», тогда:
            if ("Без обеспечения".equalsIgnoreCase(rowSource.provide?.trim()) ||
                    "Без основного обеспечения".equalsIgnoreCase(rowSource.provide?.trim())) {
                // Графа 21 = «Нет».
                return getRecNoId()
            } else {
                // Графа 21 = «Да».
                return getRecYesId()
            }
        case SourceType.TYPE_5_2A:
            // 1.	Найти в справочнике «Кредитные рейтинги» запись, у которой значение поля «Краткое наименование» = значение графы 5 формы-источника.
            def record603 = getRefBookValue(603, rowSource.creditRating)
            // 2.	Если значение поля «Международный кредитный рейтинг по шкале S&P» найденной записи равно одному из значений: «AAA», «A», «A-», «A+», «AA», «AA+», «AA-», «BBB+», «BBB», «BBB-», «BB», «BB+», «BB-», тогда:
            if (getSpRatingIds().contains(record603.INTERNATIONAL_CREDIT_RATING.value)) {
                //     Графа 21 = «Да»
                return getRecYesId()
            // 3.	Иначе
            } else {
                //     Графа 21 = «Нет»
                return getRecNoId()
            }
        case SourceType.TYPE_LETTER:
            // Графа 21 = значение графы 17 формы-источника
            return rowSource.sign
    }
}

def calc24(def row) {
    // 1.	Если выполнено одно из условий:
    // -	значение графы 20 равно 0 или больше 20,
    // -	графа 5 = «Да»,
    // -	графа 6 = «Да»,
    // тогда Графа 24 = «Да».
    // 2.	Иначе Графа 24 = «Нет»
    if ((row.rate == BigDecimal.ZERO) || (row.rate > 20) ||
            row.relatedPerson == getRecYesId() ||
            row.offshore == getRecYesId()) {
        return getRecYesId()
    } else {
        return getRecNoId()
    }
}

/**
 * Получить значение для графы 3 или 4.
 *
 * @param row строка приемника
 * @param rowSource строка источника
 * @param tmpValue значение по умолчанию, в случае если запись не найдена
 * @param sourceType тип источника (2.1, 5.2а, Данные по непокрытым аккредитивам)
 * @param isCalcCountry признак определения расчет для графы 3 (false) или 4 (true)
 */
def calc3or4(def row, def rowSource, def tmpValue, SourceType sourceType, def isCalcCountry) {
    def innAlias = getInnAlias(sourceType)
    def inn = rowSource[innAlias] // на самом деле INN или KIO или SWIFT
    def result = tmpValue
    if (!exclusiveInns.contains(inn)) {
        def records = getRecords520(inn)
        if (records != null && records.size() == 1) {
            def alias = (isCalcCountry ? 'COUNTRY_CODE' : 'NAME')
            result = records.get(0).get(alias)?.value

            // сообщение об ошибках при консолидации 2 и 3 (в источнике 2.1 страны нет)
            if (result != tmpValue && !(isCalcCountry && sourceType == SourceType.TYPE_2_1)) {
                def msg = 'Строка %s: Графа «%s» заполнена данными записи из справочника «Участники ТЦО», ' +
                        'в которой атрибут «Полное наименование юридического лица с указанием ОПФ» = «%s», ' +
                        'атрибут «%s» = «%s». В форме-источнике «%s» указано другое наименование %s - «%s»!'
                def columnName = getColumnName(row, (isCalcCountry ? 'country' : 'name'))
                def name = records.get(0)?.NAME?.value
                def attributeAlias = searchAttributes.find{ records.get(0)?.get(it)?.value == inn }
                def attributeName = getRefBookAttributeName(520L, attributeAlias)
                def attributeValue = inn
                def formTypeName = getFormType(getFormTypeId(sourceType))?.name
                def subMsg = (isCalcCountry ? 'страны' : 'клиента')
                def tmp = (isCalcCountry ? getRefBookValue(10L, tmpValue)?.NAME?.value : tmpValue)
                logger.warn(msg, row.getIndex(), columnName, name, attributeName, attributeValue, formTypeName, subMsg, tmp)
            }
        }
    }
    return result
}

def getInnAlias(SourceType sourceType) {
    switch (sourceType) {
        case SourceType.TYPE_2_1:
            return "taxpayerInn"
        case SourceType.TYPE_5_2A:
            return "swift"
        case SourceType.TYPE_LETTER:
            return "innKio"
    }
}

def getFormTypeId(SourceType sourceType) {
    switch (sourceType) {
        case SourceType.TYPE_2_1:
            return formType_2_1
        case SourceType.TYPE_5_2A:
            return formType_5_2a
        case SourceType.TYPE_LETTER:
            return formTypeLetter
    }
}

def getNameAlias(SourceType sourceType) {
    switch (sourceType) {
        case SourceType.TYPE_2_1:
            return "taxpayerName"
        case SourceType.TYPE_5_2A:
            return "nameBank"
        case SourceType.TYPE_LETTER:
            return "name"
    }
}

def getDateAlias(SourceType sourceType) {
    switch (sourceType) {
        case SourceType.TYPE_2_1:
            return "issuanceDate"
        case SourceType.TYPE_5_2A:
            return "issueDate"
        case SourceType.TYPE_LETTER:
            return "creditDate"
    }
}

@Field
def searchAttributes = ['INN', 'KIO', 'SWIFT']

/**
 * Получить список записей из справочника "Участники ТЦО" (id = 520) по ИНН, КИО или SWIFT.
 *
 * @param value значение для поиска по совпадению
 */
def getRecords520(def value) {
    return getRecordsByValue(520L, value, searchAttributes)
}

// мапа хранящая мапы с записями справочника (ключ "id справочника" -> мапа с записями, ключ "значение атрибута" -> список записией)
// например:
// [ id 520 : мапа с записям ]
//      мапа с записями = [ инн 1234567890 : список подходящих записей ]
@Field
def recordsMap = [:]

/**
 * Получить список записей из справочника атрибуты которых равны заданному значению.
 *
 * @param refBookId id справочника
 * @param value значение для поиска
 * @param attributesForSearch список атрибутов справочника по которым искать совпадения
 */
def getRecordsByValue(def refBookId, def value, def attributesForSearch) {
    if (recordsMap[refBookId] == null) {
        recordsMap[refBookId] = [:]
        // получить все записи справочника и засунуть в мапу
        def allRecords = getAllRecords(refBookId)?.values()
        allRecords.each { record ->
            attributesForSearch.each { attribute ->
                def tmpKey = getKeyValue(record[attribute]?.value)
                if (tmpKey) {
                    if (recordsMap[refBookId][tmpKey] == null) {
                        recordsMap[refBookId][tmpKey] = []
                    }
                    if (!recordsMap[refBookId][tmpKey].contains(record)) {
                        recordsMap[refBookId][tmpKey].add(record)
                    }
                }
            }
        }
    }
    def key = getKeyValue(value)
    return recordsMap[refBookId][key]
}

def getKeyValue(def value) {
    return value?.trim()?.toLowerCase()
}

@Field
def allRecordsMap = [:]

/**
 * Получить все записи справочника.
 *
 * @param refBookId id справочника
 * @return мапа с записями справочника (ключ "id записи" -> запись)
 */
def getAllRecords(def refBookId) {
    if (allRecordsMap[refBookId] == null) {
        def date = getReportPeriodEndDate()
        def provider = formDataService.getRefBookProvider(refBookFactory, refBookId, providerCache)
        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(date, null)
        allRecordsMap[refBookId] = provider.getRecordData(uniqueRecordIds)
    }
    return allRecordsMap[refBookId]
}

@Field
def refBookAttributeMap = [:]

def getRefBookAttributeName(def refbookId, def attributAlias) {
    if (refBookAttributeMap[refbookId] == null) {
        refBookAttributeMap[refbookId] = [:]
    }
    if (refBookAttributeMap[refbookId][attributAlias] == null) {
        refBookAttributeMap[refbookId][attributAlias] = getRefBook(refbookId)?.getAttribute(attributAlias)?.getName()
    }
    return refBookAttributeMap[refbookId][attributAlias]
}

@Field
def refBookMap = [:]

def getRefBook(def refbookId) {
    if (refBookMap[refbookId] == null) {
        refBookMap[refbookId] = refBookFactory.get(refbookId)
    }
    return refBookMap[refbookId]
}

@Field
def formTypeMap = [:]

def getFormType(def formTypeId) {
    if (formTypeMap[formTypeId] == null) {
        formTypeMap[formTypeId] = formTypeService.get(formTypeId)
    }
    return formTypeMap[formTypeId]
}

