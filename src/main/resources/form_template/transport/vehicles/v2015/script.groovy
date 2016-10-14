package form_template.transport.vehicles.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * Сведения о транспортных средствах, по которым уплачивается транспортный налог
 * formTypeId=201
 * formTemplateId=2201
 *
 * форма действует с 01.01.2015
 *
 * @author Stanislav Yasinskiy
 */

// графа 1  - rowNumber
// графа    - fix
// графа 2  - codeOKATO         - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
// графа 3  - regionName        - зависит от графы 2 - атрибут 841 - NAME - «Наименование», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
// графа 4  - tsTypeCode        - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
// графа 5  - tsType            - зависит от графы 4 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
// графа 6  - model
// графа 7  - ecoClass          - атрибут 400 - CODE - «Код экологического класса», справочник 40 «Экологические классы»
// графа 8  - identNumber
// графа 9  - regNumber
// графа 10 - regDate
// графа 11 - regDateEnd
// графа 12 - taxBase
// графа 13 - baseUnit          - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
// графа 14 - year
// графа 15 - pastYear
// графа 16 - stealDateStart
// графа 17 - stealDateEnd
// графа 18 - share
// графа 19 - costOnPeriodBegin
// графа 20 - costOnPeriodEnd
// графа 21 - benefitStartDate
// графа 22 - benefitEndDate
// графа 23 - taxBenefitCode    - атрибут 19 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 7 «Параметры налоговых льгот транспортного налога»
// графа 24 - base
// графа 25 - version           - атрибут 2183 - MODEL - «Модель (версия)», справочник 218 «Средняя стоимость транспортных средств»
// графа 26 - averageCost       - атрибут 2111 - NAME - «Наименование», справочник 211 «Категории средней стоимости транспортных средств»

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        if (formData.kind == FormDataKind.PRIMARY) {
            copyData()
            formDataService.saveCachedDataRows(formData, logger)
        }
        break
    case FormDataEvent.CALCULATE:
        checkRegionId()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        checkRegionId()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        checkRegionId()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        checkRegionId()
        consolidation()
        calc()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        checkRegionId()
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        checkRegionId()
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

@Field
def summaryTypeId = 203

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Редактируемые атрибуты (графа 2, 4, 6..14, 16..23, 25)
@Field
def editableColumns = ['codeOKATO', 'tsTypeCode', 'model', 'ecoClass', 'identNumber', 'regNumber',
                       'regDate', 'regDateEnd', 'taxBase', 'baseUnit', 'year', 'stealDateStart', 'stealDateEnd',
                       'share', 'costOnPeriodBegin', 'costOnPeriodEnd', 'benefitStartDate', 'benefitEndDate', 'taxBenefitCode', 'version']

// Проверяемые на пустые значения атрибуты (графа 1..6, 8..10, 12..15, 18. Графа 1, 3, 5 исключены)
@Field
def nonEmptyColumns = [/*'rowNumber',*/ 'codeOKATO', /*'regionName',*/ 'tsTypeCode', /*'tsType',*/ 'model',
                       'identNumber', 'regNumber', 'regDate', 'taxBase', 'baseUnit', 'year', 'pastYear', 'share']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'averageCost']

//
@Field
def totalColumns = ['costOnPeriodBegin', 'costOnPeriodEnd']

// дата начала отчетного периода
@Field
def start = null

// дата окончания отчетного периода
@Field
def endDate = null

// Признак периода ввода остатков
@Field
def isBalancePeriod

// отчетный период формы
@Field
def currentReportPeriod = null

// список алиасов подразделов
@Field
def sections = ['A', 'B', 'C']

@Field
def copyColumns = ['codeOKATO', 'tsTypeCode', 'model', 'ecoClass', 'identNumber', 'regNumber', 'regDate',
                   'regDateEnd', 'taxBase', 'baseUnit', 'year', 'pastYear', 'stealDateStart', 'stealDateEnd', 'share',
                   'costOnPeriodBegin', 'costOnPeriodEnd', 'benefitStartDate', 'benefitEndDate', 'taxBenefitCode', 'base', 'version']

@Field
def copyColumns201 = ['codeOKATO', 'tsTypeCode', 'identNumber', 'model', 'ecoClass', 'regNumber',
                      'baseUnit', 'year', 'regDate', 'regDateEnd', 'stealDateStart', 'stealDateEnd']

// Соответствие задается совпадением граф 2, 4, 8, 12, 13
@Field
def columnsForEquals = ['codeOKATO', 'tsTypeCode', 'identNumber', 'taxBase', 'baseUnit']

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value, null,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String columnName,
              def Date date, boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, null, date,
            rowIndex, columnName, logger, required)
}

/**
 * Аналог FormDataServiceImpl.getRefBookRecord(...) но ожидающий получения из справочника больше одной записи.
 * @return первая из найденных записей
 */
def getRecord(def refBookId, def filter, Date date) {
    if (refBookId == null) {
        return null
    }
    String dateStr = date?.format('dd.MM.yyyy')
    if (recordCache.containsKey(refBookId)) {
        Long recordId = recordCache.get(refBookId).get(dateStr + filter)
        if (recordId != null) {
            if (refBookCache != null) {
                def key = getRefBookCacheKey(refBookId, recordId)
                return refBookCache.get(key)
            } else {
                def retVal = new HashMap<String, RefBookValue>()
                retVal.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId))
                return retVal
            }
        }
    } else {
        recordCache.put(refBookId, [:])
    }

    def records = getProvider(refBookId).getRecords(date, null, filter, null)
    // отличие от FormDataServiceImpl.getRefBookRecord(...)
    if (records.size() > 0) {
        def retVal = records.get(0)
        Long recordId = retVal.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
        recordCache.get(refBookId).put(dateStr + filter, recordId)
        if (refBookCache != null) {
            def key = getRefBookCacheKey(refBookId, recordId)
            refBookCache.put(key, retVal)
        }
        return retVal
    }
    return null
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

//// Кастомные методы

def addRow() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'B').getIndex()
    } else {
        index = currentDataRow.getIndex() + 1
    }
    dataRows.add(index - 1, getNewRow())
    formDataService.saveCachedDataRows(formData, logger)
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def currentYear = getReportPeriod().taxPeriod.year
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // графа 15
        row.pastYear = calc15(row, currentYear)
        // графа 24
        row.base = calc24(row)
    }

    sortFormDataRows(false)
}

def calc15(def row, def currentYear) {
    if (row.year) {
        if (row.version) {
            return currentYear - row.year.format('yyyy')?.toInteger() + 1
        } else {
            return currentYear - row.year.format('yyyy')?.toInteger()
        }
    } else {
        return null
    }
}

def calc24(row) {
    if (row.taxBenefitCode == null) {
        return null
    }
    def record = getRefBookValue(7L, row.taxBenefitCode)
    def benefitCode = getRefBookValue(6L, record?.TAX_BENEFIT_ID?.value)?.CODE?.value
    if (!["20200", "20210", "20220", "20230"].contains(benefitCode)) {
        return null
    }
    // дополнить 0 слева если значении меньше четырех
    def section = record.SECTION.value ?: ''
    def item = record.ITEM.value ?: ''
    def subItem = record.SUBITEM.value ?: ''
    return String.format("%s%s%s", section.padLeft(4, '0'), item.padLeft(4, '0'), subItem.padLeft(4, '0'))
}

@Field
def tsCodeNamesMap = [:]

def getSectionTsTypeName(def sectionTsTypeCode) {
    if (tsCodeNamesMap[sectionTsTypeCode] == null) {
        def record = getRecord(42, 'CODE', sectionTsTypeCode, -1, null, getReportPeriodEndDate(), true)
        tsCodeNamesMap[sectionTsTypeCode] = record?.NAME?.value ?: ""
    }
    return tsCodeNamesMap[sectionTsTypeCode]
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

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def reportPeriod = getReportPeriod()
    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()
    def String dFormat = "dd.MM.yyyy"

    def rowMap = getRowEqualsMap(dataRows, columnsForEquals)

    def sectionTsTypeCodeMap = ['A': '50000', 'B': '40200', 'C': '40100']
    def sectionTsTypeCode = null

    // получаем приемники
    List<Relation> destinationsInfo = formDataService.getDestinationsInfo(formData, false, false, null, userInfo, logger)

    for (row in dataRows) {
        if (row.getAlias() != null) {
            sectionTsTypeCode = sectionTsTypeCodeMap[row.getAlias()]
            continue
        }

        def index = row.getIndex()

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index ?: 0, nonEmptyColumns, logger, !isBalancePeriod())

        // 3. Проверка кода вида ТС (графа 4) по разделу «Наземные транспортные средства»
        // 4. Проверка кода вида ТС (графа 4) по разделу «Водные транспортные средства»
        // 5. Проверка кода вида ТС (графа 4) по разделу «Воздушные транспортные средства»
        if (row.tsTypeCode != null) {
            // проверить выбрана ли верхушка деревьев видов ТС
            def tsTypeCode = getRefBookValue(42L, row.tsTypeCode)?.CODE?.value
            def hasError = tsTypeCode in sectionTsTypeCodeMap.values()
            if (!hasError) {
                // проверить корень дерева видов ТС на соответствие
                def parentTypeCode = getParentTsTypeCode(row.tsTypeCode)
                if (parentTypeCode != null) {
                    hasError = sectionTsTypeCode != parentTypeCode
                }
            }
            if (hasError) {
                logger.error("Строка %s: Значение графы «%s» должно относиться к виду ТС «%s»",
                        index, getColumnName(row, 'tsTypeCode'), getSectionTsTypeName(sectionTsTypeCode))
            }
        }

        // 6. Проверка корректности заполнения даты регистрации ТС
        if (row.regDate != null && row.regDate > dTo) {
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно %s",
                    index, getColumnName(row, 'regDate'), dTo.format(dFormat))
        }

        // 7. Проверка корректности заполнения даты снятия с регистрации ТС
        if (row.regDateEnd != null && (row.regDateEnd < dFrom || row.regDateEnd < row.regDate)) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значения графы «%s»",
                    index, getColumnName(row, 'regDateEnd'), dFrom.format(dFormat), getColumnName(row, 'regDate'))
        }

        // 8. Проверка года изготовления ТС
        if (row.year != null) {
            Calendar calendarMake = Calendar.getInstance()
            calendarMake.setTime(row.year)
            if (calendarMake.get(Calendar.YEAR) > reportPeriod.taxPeriod.year) {
                logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно «%s»",
                        index, getColumnName(row, 'year'), reportPeriod.taxPeriod.year)
            }
        }

        // 9. Проверка количества лет, прошедших с года выпуска ТС
        if (formDataEvent != FormDataEvent.CALCULATE && row.pastYear != null && row.pastYear != calc15(row, reportPeriod.taxPeriod.year)) {
            logger.error("Строка %s: Графа «%s» заполнена неверно. Выполните расчет формы",
                    index, getColumnName(row, 'pastYear'))
        }

        // 10. Проверка на наличие даты начала розыска ТС при указании даты возврата ТС
        if (row.stealDateEnd != null && row.stealDateStart == null) {
            logger.error("Строка %s: Графа «%s» должна быть заполнена, если заполнена графа «%s»",
                    index, getColumnName(row, 'stealDateStart'), getColumnName(row, 'stealDateEnd'))
        }

        // 11. Проверка на соответствие дат сведений об угоне
        if (row.stealDateStart != null && row.stealDateEnd != null && row.stealDateEnd < row.stealDateStart) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значения графы «%s»",
                    index, getColumnName(row, 'stealDateEnd'), getColumnName(row, 'stealDateStart'))
        }

        if (row.share != null) {
            def parts = row.share.split('/')

            // 12. Проверка доли налогоплательщика в праве на ТС (графа 18) на корректность формата введенных данных
            def isOnlyDigits = row.share ==~ /\d{1,10}\/\d{1,10}/
            def hasFirstZero = parts.find { it ==~ /0+\d*/ }
            // если числитель больше знаменателя
            def divisorGreaterDenominator = (parts.size() == 2 && (parts[0].size() > parts[1].size() || (parts[0].size() == parts[1].size() && parts[0] > parts[1])))
            if (!isOnlyDigits || hasFirstZero || divisorGreaterDenominator) {
                logger.error("Строка %s: Графа «%s» должна быть заполнена согласно формату: «(от 1 до 10 числовых знаков)/(от 1 до 10 числовых знаков)», числитель должен быть меньше либо равен знаменателю",
                        index, getColumnName(row, 'share'))
            }

            // 13. Проверка значения знаменателя доли налогоплательщика в праве на ТС (графа 18)
            if (parts.size() == 2 && parts[1] ==~ /0{1,10}/) {
                logger.error("Строка %s: Значение знаменателя в графе «%s» не может быть равным нулю", index, getColumnName(row, 'share'))
            }
        }

        // 14. Проверка корректности заполнения даты начала использования льготы
        if (row.benefitStartDate != null && row.benefitStartDate > dTo) {
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно %s", index, getColumnName(row, 'benefitStartDate'), dTo.format(dFormat))
        }

        // 15. Проверка корректности заполнения даты окончания использования льготы
        if (row.benefitEndDate != null && ((row.benefitEndDate < dFrom) || (row.benefitStartDate != null && row.benefitEndDate < row.benefitStartDate))) {
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значения графы «%s»",
                    index, getColumnName(row, 'benefitEndDate'), dFrom.format(dFormat), getColumnName(row, 'benefitStartDate'))
        }

        // 16. Проверка на наличие даты начала использования льготы и кода налоговой льготы
        if ((row.taxBenefitCode != null && row.benefitStartDate == null) || (row.benefitStartDate != null && row.taxBenefitCode == null)) {
            logger.error("Строка %s: Графы «%s», «%s» должны быть одновременно заполнены либо не заполнены",
                    index, getColumnName(row, 'benefitStartDate'), getColumnName(row, 'taxBenefitCode'))
        }

        // ------------------------------------------------------------------------------------------------------------------------------
        // Следующие проверки не проводятся при расчёте
        if (formDataEvent == FormDataEvent.CALCULATE) {
            continue
        }

        // 17. Проверка корректности заполнения «Графы 24»
        if (row.base != calc24(row)) {
            logger.error("Строка %s: Графа «%s» заполнена неверно! Выполните расчет формы", index, getColumnName(row, 'base'))
        }

        // 18. Проверка наличия повышающего коэффициента для ТС с заполненной графой 25
        if (row.version != null) {
            def avgCostId = getRefBookValue(218L, row.version)?.AVG_COST?.value
            // справочник «Повышающие коэффициенты транспортного налога»
            def allRecords = getAllRecords(209L).values() // dTo
            def records = allRecords.findAll { record ->
                record.AVG_COST.value == avgCostId &&
                        record.YEAR_FROM.value <= row.pastYear &&
                        record.YEAR_TO.value >= row.pastYear
            }
            if (records == null || records.isEmpty()) {
                def avgCost = getRefBookValue(211L, avgCostId).NAME.value
                logger.error("Строка %s: В справочнике «Повышающие коэффициенты транспортного налога» отсутствует запись, актуальная на дату %s, " +
                        "в которой поле «Средняя стоимость» равно значению графы «%s» (%s) и значение графы «%s» (%s) больше значения поля " +
                        "«Количество лет, прошедших с года выпуска ТС (от)» и меньше или равно значения поля «Количество лет, прошедших с года выпуска ТС (до)»",
                        index, dTo.format(dFormat), getColumnName(row, 'averageCost'), avgCost, getColumnName(row, 'pastYear'), row.pastYear)
            }
        }

        boolean checkRateTS = row.tsTypeCode != null && row.taxBase != null && row.baseUnit != null && row.pastYear != null
        if (row.codeOKATO != null) {
            def region = getRegion(row.codeOKATO)
            def regionId = region?.record_id?.value
            for (Relation relation in destinationsInfo) {
                if (relation.formType.id != summaryTypeId) {
                    continue
                }
                // 19. Проверка наличия параметров представления декларации для кода ОКТМО
                def declarationRegionId = relation.getDepartment().regionId
                // справочник «Параметры представления деклараций по транспортному налогу»
                def allRecords = getAllRecords(210L).values() // dTo
                def records = allRecords.findAll { record ->
                    record.DECLARATION_REGION_ID.value == declarationRegionId &&
                            record.DICT_REGION_ID.value == regionId &&
                            record.OKTMO.value == row.codeOKATO
                }
                if (records == null || records.isEmpty()) {
                    def declarationRegionCode = getRefBookValue(4, declarationRegionId).CODE.value
                    def regionCode = region.CODE.value
                    def codeOKATO = getRefBookValue(96L, row.codeOKATO).CODE.value
                    logger.error("Строка %s: В справочнике «Параметры представления деклараций по транспортному налогу» отсутствует запись, " +
                            "актуальная на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) справочника «Подразделения» для подразделения «%s» формы-приемника вида «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s»",
                            index, dTo.format(dFormat), declarationRegionCode, relation.getDepartment().name, relation.formTypeName, regionCode, codeOKATO)
                }
                // 20. Проверка наличия ставки для ТС
                if (checkRateTS) {
                    allRecords = getAllRecords(41L).values() // dTo
                    records = allRecords.findAll { record ->
                        record.DECLARATION_REGION_ID.value == declarationRegionId &&
                                record.DICT_REGION_ID.value == regionId &&
                                record.CODE.value == row.tsTypeCode &&
                                record.UNIT_OF_POWER.value == row.baseUnit &&
                                ((record.MIN_AGE.value == null) || (record.MIN_AGE.value < row.pastYear)) &&
                                ((record.MAX_AGE.value == null) || (record.MAX_AGE.value >= row.pastYear)) &&
                                ((record.MIN_POWER.value == null) || (record.MIN_POWER.value < row.taxBase)) &&
                                ((record.MAX_POWER.value == null) || (record.MAX_POWER.value >= row.taxBase))
                    }
                    if (records == null || records.size() != 1) {
                        boolean isMany = records != null && records.size() > 1
                        def declarationRegionCode = getRefBookValue(4, declarationRegionId).CODE.value
                        def regionCode = region.CODE.value
                        def tsTypeCode = getRefBookValue(42L, row.tsTypeCode).CODE.value
                        def baseUnit = getRefBookValue(12L, row.baseUnit).CODE.value
                        def msg1 = isMany ? "более одной записи, актуальной" : "отсутствует запись, актуальная"
                        logger.error("Строка %s: В справочнике «Ставки транспортного налога» %s на дату %s, в которой поле «Код субъекта РФ представителя декларации» " +
                                "равно значению поля «Регион» (%s) справочника «Подразделения» для подразделения «%s» формы-приемника вида «%s», поле «Код субъекта РФ» равно " +
                                "значению «%s», поле «Код ТС» = «%s», поле «Ед. измерения мощности» = «%s»",
                                index, msg1, dTo.format(dFormat), declarationRegionCode, relation.getDepartment().name, relation.formTypeName, regionCode, tsTypeCode, baseUnit)
                    }
                }
            }
        }
    }

    // 2. Проверка на наличие в форме строк с одинаковым значением граф 2, 4, 8, 12, 13
    rowMap.each { key, rowList ->
        if (rowList.size() > 1) {
            def errorRows = []
            rowList.each { row ->
                errorRows.add(row.getIndex())
            }
            if (!errorRows.empty) {
                def row = rowList[0]
                def errorRowsStr = errorRows.join(", ")
                logger.error("Cтроки %s. Код ОКТМО «%s», Код вида ТС «%s», Идентификационный номер ТС «%s», Налоговая база «%s», Единица измерения налоговой базы по ОКЕИ «%s»: " +
                        "на форме не должно быть строк с одинаковым кодом ОКТМО, кодом вида ТС, идентификационным номером ТС, налоговой базой и единицей измерения налоговой базы по ОКЕИ",
                        errorRowsStr, getRefBookValue(96L, row.codeOKATO)?.CODE?.stringValue ?: '\"\"', getRefBookValue(42L, row.tsTypeCode)?.CODE?.stringValue ?: '\"\"', row.identNumber ?: '\"\"', row.taxBase ?: '\"\"',
                        getRefBookValue(12L, row.baseUnit)?.CODE?.value ?: '\"\"')
            }
        }
    }
}

// Сгруппировать строки в карту по общим колонкам
def getRowEqualsMap(def dataRows, def columns) {
    def result = [:]
    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        StringBuilder keySb = new StringBuilder()
        boolean skipRow = false
        for (column in columns) {
            if (row[column] != null) {
                keySb.append(row[column]).append("#")
            } else {
                skipRow = true
            }
        }
        if (skipRow) {
            continue
        }
        String keyString = keySb.toString()
        if (result[keyString] == null) {
            result[keyString] = []
        }
        result[keyString].add(row)
    }
    return result
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить нефиксированные строки
    deleteNotFixedRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allSaved
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRows, dataRows, section)
                }
            }
        }
    }
}

def String checkPrevPeriod(def reportPeriod) {
    if (reportPeriod != null) {
        // ищем форму нового типа иначе две формы старого
        if (formDataService.getLast(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriod.id, formData.periodOrder, formData.comparativePeriodId, formData.accruing) == null) {
            return reportPeriod.name + " " + reportPeriod.taxPeriod.year + ", "
        }
    }
    return ''
}

@Field
def periodNameMap = [1: "первый квартал", 2: "второй квартал", 3: "третий квартал", 4: "четвёртый квартал"]

// Алгоритм копирования данных из форм предыдущего периода при создании формы.
// Также получение данных из старых форм "Сведения о транспортных средствах, по которым уплачивается транспортный налог"
// и "Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог"
def copyData() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def reportPeriod = getReportPeriod()
    def absentPeriods = [absentFormPeriodOrders : [], prevPeriodYear : reportPeriod.taxPeriod.year]

    if (reportPeriod.order == 4) {
        def reportPeriods = reportPeriodService.listByTaxPeriod(reportPeriod.taxPeriod.id)
        [3, 2, 1].each { order ->
            def prevReportPeriod = reportPeriods.find{ it.order == order }
            if (prevReportPeriod != null) {
                dataRows = getPrevRowsForCopy(prevReportPeriod, dataRows, absentPeriods)
            } else {
                absentPeriods.absentFormPeriodOrders.add(order);
            }
        }
    } else {
        def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
        if (prevReportPeriod != null) {
            dataRows = getPrevRowsForCopy(prevReportPeriod, dataRows, absentPeriods)
        } else if (reportPeriod.order == 1) {
            absentPeriods.absentFormPeriodOrders.add(4);
            absentPeriods.prevPeriodYear = reportPeriod.taxPeriod.year - 1
        } else {
            absentPeriods.absentFormPeriodOrders.add(reportPeriod.order - 1);
        }
    }
    if (!absentPeriods.absentFormPeriodOrders.isEmpty()) {
        boolean plural = absentPeriods.absentFormPeriodOrders.size() > 1
        def msg1 = plural ? "отсутствуют первичные формы" : "отсутствует первичная форма"
        def periodNames = absentPeriods.absentFormPeriodOrders.sort().collect { periodNameMap[it] }
        def msg2 = (plural ? "периоды: " : "период ") + periodNames.join(", ")
        logger.warn("Данные по транспортным средстам из предыдущего периода не были скопированы. В Системе %s «%s» подразделения «%s» в состоянии «Принята» за %s %s",
                msg1, formData.formType.name, formDataDepartment.name, msg2, absentPeriods.prevPeriodYear)
    }

    if (dataRows.size() > 3) {
        updateIndexes(dataRows)
        formDataService.getDataRowHelper(formData).allCached = dataRows
    }
}

def copyRow(def row, def columns) {
    def newRow = getNewRow()
    columns.each {
        newRow.getCell(it).setValue(row.getCell(it).value, null)
    }
    return newRow
}

// Получить новую строку с заданными стилями.
def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    if (isBalancePeriod()) {
        (editableColumns + ['pastYear', 'base']).each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).styleAlias = 'Редактируемая'
        }
    } else {
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).styleAlias = 'Редактируемая'
        }
        autoFillColumns.each {
            newRow.getCell(it).styleAlias = 'Автозаполняемая'
        }
    }
    return newRow
}

// Получить строки для копирования за предыдущий отчетный период
def getPrevRowsForCopy(def reportPeriod, def dataRows, def absentPeriods) {
    def formData201 = formDataService.getLast(201, formData.kind, formDataDepartment.id, reportPeriod.id, null, formData.comparativePeriodId, formData.accruing)
    if (formData201 != null && formData201.state == WorkflowState.ACCEPTED) {
        if (formData201.formTemplateId == 201) {
            // получить нет формы за предыдущий отчетный период, то попытаться найти старую форму 201
            def dataRows201 = formDataService.getDataRowHelper(formData201)?.allSaved

            if (dataRows201 != null && !dataRows201.isEmpty()) {
                dataRows = copyFromOldForm(dataRows, dataRows201)
            }
        } else {
            // получить форму за предыдущий отчетный период
            def dataRowsPrev = formDataService.getDataRowHelper(formData201)?.allSaved
            if (dataRowsPrev != null && !dataRowsPrev.isEmpty()) {
                dataRows = copyFromOursForm(dataRows, dataRowsPrev)
            }
        }
    } else {
        absentPeriods.absentFormPeriodOrders.add(reportPeriod.order)
        absentPeriods.prevPeriodyear = reportPeriod.taxPeriod.year
    }
    return dataRows
}

/**
 * Скопировать строки из аналогичной предыдущей формы.
 *
 * @param dataRows строки текущей формы
 * @param dataRowsPrev строки предыдущей формы
 */
def copyFromOursForm(def dataRows, def dataRowsPrev) {
    def rowsOld = []
    rowsOld.addAll(dataRows)

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()

    def section = null
    def sectionRows = [:]
    sections.each {
        sectionRows[it] = []
    }

    def rowOldMap = getRowEqualsMap(rowsOld, columnsForEquals)

    for (def row : dataRowsPrev) {
        if (row.getAlias() != null) {
            section = row.getAlias()
            continue
        }
        // если период владения ТСом не пересекается с текущим периодом формы, то пропустить эту строку
        if ((row.regDateEnd != null && row.regDateEnd < dFrom) || (row.regDate > dTo)) {
            continue
        }

        def regDateEnd = row.regDateEnd
        if (regDateEnd == null || regDateEnd > dTo) {
            regDateEnd = dTo
        }
        def regDate = row.regDate
        if (regDate < dFrom) {
            regDate = dFrom
        }
        if (regDate > dTo || regDateEnd < dFrom) {
            continue
        }

        // исключаем дубли
        def need = true
        // формируем ключ карты

        def keySb = new StringBuilder()
        for (column in columnsForEquals) {
            // если графа пустая, то считаем что не дубль
            if (row[column] == null) {
                keySb = null
                break
            } else {
                keySb.append(row[column]).append("#")
            }
        }
        if (keySb != null && rowOldMap[keySb.toString()] != null) {
            need = false
        }
        if (need) {
            row.setIndex(rowsOld.size())
            def newRow = copyRow(row, copyColumns)
            rowsOld.add(newRow)
            sectionRows[section].add(newRow)
        }
    }
    sections.each {
        dataRows.addAll(getLastRowIndexInSection(dataRows, it), sectionRows[it])
        updateIndexes(dataRows)
    }
    return dataRows
}

/**
 * Скопировать строки из устаревших предыдущих форм
 * "Сведения о транспортных средствах, по которым уплачивается транспортный налог" и
 * "Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог".
 *
 * @param dataRows строки текущей формы
 * @param dataRows201Old строки предыдущей формы 201
 */
def copyFromOldForm(def dataRows, dataRows201Old) {
    def rowsOld = []
    rowsOld.addAll(dataRows)

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()

    def sectionRows = [:]
    sections.each {
        sectionRows[it] = []
    }

    def sectionMap = ['50000': 'A', '40200': 'B', '40100': 'C']

    def dublColumns = ['codeOKATO', 'identNumber', 'baseUnit']
    def rowOldMap = getRowEqualsMap(rowsOld, dublColumns)

    // получение данных из 201
    for (def row : dataRows201Old) {
        if ((row.regDateEnd != null && row.regDateEnd < dFrom) || (row.regDate > dTo)) {
            continue
        }

        def regDateEnd = row.regDateEnd
        if (regDateEnd == null || regDateEnd > dTo) {
            regDateEnd = dTo
        }
        def regDate = row.regDate
        if (regDate < dFrom) {
            regDate = dFrom
        }
        if (regDate > dTo || regDateEnd < dFrom) {
            continue
        }

        // исключаем дубли
        def need = true
        // формируем ключ карты
        def keySb = new StringBuilder()
        for (column in dublColumns) {
            // если графа пустая, то считаем что не дубль
            if (row[column] == null) {
                keySb = null
                break
            } else {
                keySb.append(row[column]).append("#")
            }
        }
        if (keySb != null && rowOldMap[keySb.toString()] != null) {
            need = false
        }
        if (need) {
            def newRow = copyRow(row, copyColumns201)
            def tsTypeCode = getRefBookValue(42L, row.tsTypeCode)?.CODE?.value
            if (!(tsTypeCode in sectionMap.keySet())) {
                tsTypeCode = getParentTsTypeCode(row.tsTypeCode)
            }
            if (tsTypeCode != null && tsTypeCode in sectionMap.keySet()) {
                sectionRows[sectionMap[tsTypeCode]].add(newRow)
                rowsOld.add(newRow)
            }
        }
    }
    sections.each {
        dataRows.addAll(getLastRowIndexInSection(dataRows, it), sectionRows[it])
        updateIndexes(dataRows)
    }

    return dataRows
}

def getReportPeriodStartDate() {
    if (!start) {
        start = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return start
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getReportPeriod() {
    if (currentReportPeriod == null) {
        currentReportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return currentReportPeriod
}

void importTransportData() {
    ScriptUtils.checkTF(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 26
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2
    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null        // итоговая строка со значениями из тф для добавления
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    try {
        // проверить первые строки тф - заголовок и пустая строка
        checkFirstRowsTF(reader, logger)

        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }

            def newRow = getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex)
            newRows.add(newRow)
        }
    } finally {
        reader.close()
    }

    showMessages(newRows, logger)
    if (logger.containsLevel(LogLevel.ERROR) || newRows == null || newRows.isEmpty()) {
        return
    }

    // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки в xml)
    def totalColumnsIndexMap = ['costOnPeriodBegin': 19, 'costOnPeriodEnd': 20]

    // итоговая строка для сверки сумм
    def totalTmp = formData.createDataRow()
    totalColumnsIndexMap.keySet().asList().each { alias ->
        totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
    }

    // подсчет итогов
    for (def row : newRows) {
        if (row.getAlias()) {
            continue
        }
        totalColumnsIndexMap.keySet().asList().each { alias ->
            def value1 = totalTmp.getCell(alias).value
            def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
            totalTmp.getCell(alias).setValue(value1 + value2, null)
        }
    }

    // сравнение итогов
    checkAndSetTFSum(totalTmp, totalTF, totalColumns, totalTF?.getImportIndex(), logger, false)

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    def templateRows = formTemplate.rows

    def sectionMap = ['50000': 'A', '40200': 'B', '40100': 'C']
    def mapRows = [:]
    sections.each {
        mapRows[it] = []
    }
    // собрать строки по разделам, раздел определяется по графе 4 (tsTypeCode)
    newRows.each { row ->
        if (row.tsTypeCode != null) {
            // проверить корень дерева видов ТС на соответствие
            def perentTsTypeCode = getParentTsTypeCode(row.tsTypeCode)
            if (perentTsTypeCode != null) {
                // ожидается наземные (50000), водные (40200), воздушные (40100) ТС, остальные отбрасываются
                def key = sectionMap[perentTsTypeCode]
                if (key != null) {
                    mapRows[key].add(row)
                }
            }
        }
    }
    def rows = []
    sections.each { section ->
        def firstRow = getDataRow(templateRows, section)
        def copyRows = mapRows[section]
        rows.add(firstRow)
        if (copyRows != null && !copyRows.isEmpty()) {
            rows.addAll(copyRows)
        }
    }
    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}'", fileRowIndex))
        return newRow
    }

    def int colOffset = 1
    def int colIndex = 1

    // графа 1
    colIndex++

    // графа 2 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
    def record = getRecordImport(96, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    newRow.codeOKATO = record?.record_id?.value
    colIndex++

    // графа 3 - зависит от графы 2 - атрибут 841 - NAME - «Наименование», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
    if (record != null) {
        formDataService.checkReferenceValue(96, pure(rowCells[colIndex]), record?.NAME?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 4 - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
    // http://jira.aplana.com/browse/SBRFACCTAX-8572 исправить загрузку Кода Вида ТС (убираю пробелы)
    record = getRecordImport(42, 'CODE', pure(rowCells[colIndex]).replace(' ', ''), fileRowIndex, colIndex + colOffset, false)
    newRow.tsTypeCode = record?.record_id?.value
    colIndex++

    // графа 5 - зависит от графы 4 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
    if (record != null) {
        formDataService.checkReferenceValue(42, pure(rowCells[colIndex]), record?.NAME?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }
    colIndex++

    // графа 6
    newRow.model = pure(rowCells[colIndex])
    colIndex++

    // графа 7
    newRow.ecoClass = getRecordIdImport(40, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 8
    newRow.identNumber = pure(rowCells[colIndex])
    colIndex++

    // графа 9
    newRow.regNumber = pure(rowCells[colIndex]).replace(' ', '')
    colIndex++

    // графа 10
    newRow.regDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 11
    newRow.regDateEnd = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 12
    newRow.taxBase = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 13 - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
    newRow.baseUnit = getRecordIdImport(12, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    colIndex++

    // графа 14
    newRow.year = parseDate(pure(rowCells[colIndex]), "yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 15
    newRow.pastYear = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 16
    newRow.stealDateStart = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 17
    newRow.stealDateEnd = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 18
    newRow.share = pure(rowCells[colIndex])
    colIndex++

    // графа 19
    newRow.costOnPeriodBegin = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 20
    newRow.costOnPeriodEnd = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 21
    newRow.benefitStartDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 22
    newRow.benefitEndDate = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 23 - атрибут 19 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 7 «Параметры налоговых льгот транспортного налога»
    newRow.taxBenefitCode = getTaxBenefitCodeImport(pure(rowCells[colIndex]), rowIndex, colIndex + colOffset, newRow.codeOKATO)
    colIndex++

    // графа 24
    newRow.base = pure(rowCells[colIndex])
    colIndex++

    // графа 25 - атрибут 2183 - MODEL - «Модель (версия)», справочник 218 «Средняя стоимость транспортных средств»
    newRow.version = getRecordIdImport(218L, 'MODEL', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, section)
        def from = firstRow.getIndex()
        def to = getLastRowIndexInSection(dataRows, section)
        def sectionRows = (from < to ? dataRows.subList(from, to) : [])

        // Массовое разыменовывание граф НФ
        def columnNameList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, sectionRows, columnNameList)

        sortRowsSimple(sectionRows)
    }
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

/**
 * Получить номер последнего элемента раздела.
 *
 * @param dataRows все строки
 * @param section алиас раздела
 */
def getLastRowIndexInSection(def dataRows, def section) {
    if (section == 'A') {
        return getDataRow(dataRows, 'B').getIndex() - 1
    } else if (section == 'B') {
        return getDataRow(dataRows, 'C').getIndex() - 1
    } else {
        dataRows.get(dataRows.size() - 1).getIndex()
    }
}

// Поправить индексы.
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

// Удалить нефиксированные строки
void deleteNotFixedRows(def dataRows) {
    def deleteRows = dataRows.findAll { row -> row.getAlias() == null }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param section псевдоним строки с которой копировать строки (НЕ включительно)
 */
void copyRows(def sourceDataRows, def destinationDataRows, def section) {
    def from = getDataRow(sourceDataRows, section).getIndex()
    def to = getLastRowIndexInSection(sourceDataRows, section)
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    from = getDataRow(destinationDataRows, section).getIndex()
    destinationDataRows.addAll(from, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
}

/**
 * Получить "Код вида ТС" родителя.
 *
 * @param tsTypeCode id на справочник 42 «Коды видов транспортных средств»
 */
def getParentTsTypeCode(def tsTypeCode) {
    // справочник 42 «Коды видов транспортных средств»
    def ids = getProvider(42L).getParentsHierarchy(tsTypeCode)
    if (ids != null && !ids.isEmpty()) {
        return getRefBookValue(42L, ids.get(0))?.CODE?.value
    }
    return null
}

def getProvider(def id) {
    return formDataService.getRefBookProvider(refBookFactory, id, providerCache)
}

@Field
String REF_BOOK_NOT_FOUND_IMPORT_ERROR_NEW = "Проверка файла: Строка %d, столбец %s: В справочнике «%s» не найдена запись для графы «%s» актуальная на дату %s!";

@Field
String REF_BOOK_TOO_MANY_FOUND_IMPORT_ERROR_NEW = "Проверка файла: Строка %d, столбец %s: В справочнике «%s» найдено более одной записи для графы «%s» актуальная на дату %s!";

/**
 * Получить id записи из справочника по фильтру.
 * Не используется унифицированный метод formDataService.getRefBookRecordIdImport потому что в нем нет возможности
 * искать запись по фильтру.
 *
 * @param refBookId идентификатор справочника
 * @param date дата актуальности записи
 * @param filter фильтр для поиска
 * @param columnName название графы формы для которого ищется значение
 * @param rowIndex номер строки в файле
 * @param colIndex номер колонки в файле
 * @param required фатальность
 */
Long getRefBookRecordIdImport(Long refBookId, Date date, String filter, String columnName,
                              int rowIndex, int colIndex, boolean required) {
    if (refBookId == null) {
        return null
    }
    def records = getProvider(refBookId).getRecords(date, null, filter, null)
    if (records != null && records.size() == 1) {
        return records.get(0).record_id.value
    }

    def tooManyValue = (records != null && records.size() > 1)
    RefBook rb = refBookFactory.get(refBookId)

    String msg = String.format(tooManyValue ? REF_BOOK_TOO_MANY_FOUND_IMPORT_ERROR_NEW : REF_BOOK_NOT_FOUND_IMPORT_ERROR_NEW,
            rowIndex, getXLSColumnName(colIndex), rb.getName(), columnName, date.format('dd.MM.yyyy'))
    if (required) {
        throw new ServiceException("%s", msg)
    } else {
        logger.warn("%s", msg)
    }
    return null
}

// Проверка заполнения атрибута «Регион» подразделения текущей формы (справочник «Подразделения»)
void checkRegionId() {
    if (formDataDepartment.regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 27
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('rowNumber').column.name
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def sectionNumber = 0
    def sectionAlias = null
    def mapRows = [:]

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
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
        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        def title = rowValues[INDEX_FOR_SKIP]
        if (title == 'Наземные транспортные средства' || title == 'Водные транспортные средства' || title == 'Воздушные транспортные средства') {
            sectionAlias = sections[sectionNumber]
            sectionNumber++
            mapRows.put(sectionAlias, [])

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        if (sectionAlias == null) {
            logger.error("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
        } else {
            // простая строка
            rowIndex++
            def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            mapRows[sectionAlias].add(newRow)
        }
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    sections.each { section ->
        if (mapRows[section] != null) {
            showMessages(mapRows[section], logger)
        }
    }
    if (!logger.containsLevel(LogLevel.ERROR)) {
        // получить строки из шаблона
        def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
        def templateRows = formTemplate.rows
        def dataRows = []
        dataRows.addAll(templateRows)
        updateIndexes(dataRows)

        // копирование данных по разделам
        sections.each { section ->
            def copyRows = mapRows[section]
            if (copyRows != null && !copyRows.isEmpty()) {
                def insertIndex = getDataRow(dataRows, section).getIndex()
                dataRows.addAll(insertIndex, copyRows)
                // поправить индексы, потому что они после вставки не пересчитываются
                updateIndexes(dataRows)
            }
        }
        formDataService.getDataRowHelper(formData).allCached = dataRows
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
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]): tmpRow.getCell('rowNumber').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('codeOKATO').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('regionName').column.name]),
            ([(headerRows[0][4]): tmpRow.getCell('tsTypeCode').column.name]),
            ([(headerRows[0][5]): tmpRow.getCell('tsType').column.name]),
            ([(headerRows[0][6]): tmpRow.getCell('model').column.name]),
            ([(headerRows[0][7]): tmpRow.getCell('ecoClass').column.name]),
            ([(headerRows[0][8]): tmpRow.getCell('identNumber').column.name]),
            ([(headerRows[0][9]): tmpRow.getCell('regNumber').column.name]),
            ([(headerRows[0][10]): tmpRow.getCell('regDate').column.name]),
            ([(headerRows[0][11]): tmpRow.getCell('regDateEnd').column.name]),
            ([(headerRows[0][12]): tmpRow.getCell('taxBase').column.name]),
            ([(headerRows[0][13]): tmpRow.getCell('baseUnit').column.name]),
            ([(headerRows[0][14]): tmpRow.getCell('year').column.name]),
            ([(headerRows[0][15]): tmpRow.getCell('pastYear').column.name]),
            ([(headerRows[0][16]): 'Сведения об угоне']),
            ([(headerRows[1][16]): 'Дата начала розыска ТС']),
            ([(headerRows[1][17]): 'Дата возврата ТС']),
            ([(headerRows[0][18]): tmpRow.getCell('share').column.name]),
            ([(headerRows[0][19]): tmpRow.getCell('costOnPeriodBegin').column.name]),
            ([(headerRows[0][20]): tmpRow.getCell('costOnPeriodEnd').column.name]),
            ([(headerRows[0][21]): tmpRow.getCell('benefitStartDate').column.name]),
            ([(headerRows[0][22]): tmpRow.getCell('benefitEndDate').column.name]),
            ([(headerRows[0][23]): tmpRow.getCell('taxBenefitCode').column.name]),
            ([(headerRows[0][24]): tmpRow.getCell('base').column.name]),
            ([(headerRows[0][25]): tmpRow.getCell('version').column.name]),
            ([(headerRows[0][26]): tmpRow.getCell('averageCost').column.name]),
            ([(headerRows[2][0]): '1'])
    ]
    (2..25).each { index ->
        headerMapping.add(([(headerRows[2][index]): index.toString()]))
    }
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
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 1
    def int colIndex = 1

    // графа 2 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
    colIndex++
    def recordOKTMO = getRecordImport(96, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    newRow.codeOKATO = recordOKTMO?.record_id?.value

    // графа 3 - зависит от графы 2 - атрибут 841 - NAME - «Наименование», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
    colIndex++
    if (recordOKTMO != null) {
        formDataService.checkReferenceValue(96, values[colIndex], recordOKTMO?.NAME?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4 - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
    // http://jira.aplana.com/browse/SBRFACCTAX-8572 исправить загрузку Кода Вида ТС (убираю пробелы)
    colIndex++
    record = getRecordImport(42, 'CODE', values[colIndex].replace(' ', ''), fileRowIndex, colIndex + colOffset, false)
    newRow.tsTypeCode = record?.record_id?.value

    // графа 5 - зависит от графы 4 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
    colIndex++
    if (record != null) {
        formDataService.checkReferenceValue(42, values[colIndex], record?.NAME?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 6
    colIndex++
    newRow.model = values[colIndex]

    // графа 7
    colIndex++
    newRow.ecoClass = getRecordIdImport(40, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 8
    colIndex++
    newRow.identNumber = values[colIndex]

    // графа 9
    colIndex++
    newRow.regNumber = values[colIndex].replace(' ', '')

    // графа 10
    colIndex++
    newRow.regDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 11
    colIndex++
    newRow.regDateEnd = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 12
    colIndex++
    newRow.taxBase = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 13 - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
    colIndex++
    newRow.baseUnit = getRecordIdImport(12, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 14
    colIndex++
    newRow.year = parseDate(values[colIndex], "yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 15
    colIndex++
    newRow.pastYear = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 16
    colIndex++
    newRow.stealDateStart = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 17
    colIndex++
    newRow.stealDateEnd = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 18
    colIndex++
    newRow.share = values[colIndex]

    // графа 19
    colIndex++
    newRow.costOnPeriodBegin = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 20
    colIndex++
    newRow.costOnPeriodEnd = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 21
    colIndex++
    newRow.benefitStartDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 22
    colIndex++
    newRow.benefitEndDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 23 - атрибут 19 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 7 «Параметры налоговых льгот транспортного налога»
    colIndex++
    newRow.taxBenefitCode = getTaxBenefitCodeImport(values[colIndex], rowIndex, colIndex + colOffset, newRow.codeOKATO)

    // графа 24
    colIndex++
    newRow.base = values[colIndex]

    // графа 25 - атрибут 2183 - MODEL - «Модель (версия)», справочник 218 «Средняя стоимость транспортных средств»
    colIndex++
    newRow.version = getRecordIdImport(218L, 'MODEL', values[colIndex], fileRowIndex, colIndex + colOffset)

    return newRow
}

def getOkato(String codeOkato) {
    if(!codeOkato || codeOkato.length() < 3){
        return codeOkato
    }
    codeOkato = codeOkato?.substring(0, 3)
    if(codeOkato && !(codeOkato in ["719", "718", "118"])){
        codeOkato = codeOkato?.substring(0, 2)
    }
    return codeOkato
}

def getTaxBenefitCodeImport(def taxBenefit, def rowIndex, def colIndex, def okatoId) {
    if (taxBenefit) {
        def regionId = formDataDepartment.regionId
        if (!regionId && rowIndex == 1) {
            logger.warn("На форме невозможно заполнить графу «Код налоговой льготы», так как атрибут «Регион» " +
                    "подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
        } else if (regionId) {
            if (!okatoId) {
                logger.warn("Строка $rowIndex, столбец " + ScriptUtils.getXLSColumnName(colIndex) + ": " +
                        "На форме невозможно заполнить графу «Код налоговой льготы», так как в файле не заполнена графа «Код ОКТМО»!")
            } else {
                def okato = getOkato(getRefBookValue(96, okatoId)?.CODE?.stringValue)
                def region = getRegion(okatoId)
                if(!region){
                    logger.warn("Строка $rowIndex, столбец " + ScriptUtils.getXLSColumnName(colIndex) + ": " +
                            "На форме невозможно заполнить графу «Код налоговой льготы», так как в справочнике " +
                            "«Коды субъектов Российской Федерации» отсутствует запись, в которой графа " +
                            "«Определяющая часть кода ОКТМО» равна значению первых символов графы «Код ОКТМО» ($okato) формы!")
                } else {
                    def taxBenefitId = getRecordIdImport(6L, 'CODE', taxBenefit, rowIndex, colIndex)
                    def ref_id = 7
                    def dictRegionId = region?.record_id?.value
                    String filter = "DECLARATION_REGION_ID = $regionId and DICT_REGION_ID = $dictRegionId and TAX_BENEFIT_ID =$taxBenefitId"
                    if (recordCache[ref_id] != null) {
                        if (recordCache[ref_id][filter] != null) {
                            return recordCache[ref_id][filter]
                        }
                    } else {
                        recordCache[ref_id] = [:]
                    }

                    def provider = refBookFactory.getDataProvider(ref_id)
                    def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
                    if (records.size() > 0) {
                        recordCache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
                        return recordCache[ref_id][filter]
                    } else {
                        // наименование субъекта РФ для атрибута «Регион» подразделения формы
                        def regionName = getRefBookValue(4L, regionId)?.CODE?.value
                        logger.warn("Строка $rowIndex, столбец " + ScriptUtils.getXLSColumnName(colIndex) + ": " +
                                "На форме не заполнена графа «Код налоговой льготы», так как в справочнике " +
                                "«Параметры налоговых льгот транспортного налога» не найдена запись, " +
                                "актуальная на дату «" + getReportPeriodEndDate().format("dd.MM.yyyy") + "», " +
                                "в которой поле «Код субъекта РФ представителя декларации» = «$regionName», " +
                                "поле «Код субъекта РФ» = «$okato», поле «Код налоговой льготы» = «$taxBenefit»!")
                    }
                }
            }
        }
    }
}

def getRegion(def record96Id) {
    def record96 = getRefBookValue(96, record96Id)
    def okato = getOkato(record96?.CODE?.stringValue)
    if (okato) {
        def allRecords = getAllRecords(4L).values()
        allRecords.find { record ->
            record.OKTMO_DEFINITION.value == okato
        }
    }
    return null
}