package form_template.transport.summary_1.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Расчет суммы налога по каждому транспортному средству(new)
 * formTemplateId=203
 *
 * графа 1  - rowNumber
 * графа 2  - taxAuthority
 * графа 3  - kpp
 * графа 4  - okato 96 840 CODE
 * графа 5  - tsTypeCode 42 422 CODE
 * графа 6  - tsType 42 423 NAME
 * графа 7  - model
 * графа 8  - ecoClass 40 400 CODE
 * графа 9  - vi
 * графа 10 - regNumber
 * графа 11 - regDate
 * графа 12 - regDateEnd
 * графа 13 - taxBase
 * графа 14 - taxBaseOkeiUnit 12 57 CODE
 * графа 15 - createYear
 * графа 16 - years
 * графа 17 - stealDateStart
 * графа 18 - stealDateEnd
 * графа 19 - periodStartCost
 * графа 20 - periodEndCost
 * графа 21 - ownMonths
 * графа 22 - partRight
 * графа 23 - coef362
 * графа 24 - taxRate 41 416 VALUE
 * графа 25 - calculatedTaxSum
 * графа 26 - benefitMonths
 * графа 27 - benefitStartDate
 * графа 28 - benefitEndDate
 * графа 29 - coefKl
 * графа 30 - taxBenefitCode 7 19 TAX_BENEFIT_ID 6 16 NAME
 * графа 31 - benefitSum
 * графа 32 - taxBenefitCodeDecrease 7 19 TAX_BENEFIT_ID 6 16 NAME
 * графа 33 - benefitSumDecrease
 * графа 34 - benefitCodeReduction 7 19 TAX_BENEFIT_ID 6 16 NAME
 * графа 35 - benefitSumReduction
 * графа 36 - koefKp
 * графа 37 - taxSumToPay
 * графа 38 - benefitBase
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        checkRegionId()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        checkRegionId()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        checkRegionId()
        consolidation()
        calc()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        checkRegionId()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
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

@Field
def groups = ['A', 'B', 'C']

//Все аттрибуты
@Field
def allColumns = ['fix', 'rowNumber', 'taxAuthority', 'kpp', 'okato', 'tsTypeCode', 'tsType', 'model', 'ecoClass', 'vi',
                  'regNumber', 'regDate', 'regDateEnd', 'taxBase', 'taxBaseOkeiUnit', 'createYear', 'years',
                  'stealDateStart', 'stealDateEnd', 'periodStartCost', 'periodEndCost', 'ownMonths', 'partRight',
                  'coef362', 'taxRate', 'calculatedTaxSum', 'benefitMonths', 'benefitStartDate', 'benefitEndDate',
                  'coefKl', 'taxBenefitCode', 'benefitSum', 'taxBenefitCodeDecrease', 'benefitSumDecrease',
                  'benefitCodeReduction', 'benefitSumReduction', 'koefKp', 'taxSumToPay', 'benefitBase']

// Поля, для которых подсчитываются итоговые значения (19, 20, 25, 31, 33, 35, 37)
@Field
def totalColumns = ['periodStartCost', 'periodEndCost', 'calculatedTaxSum', 'benefitSum', 'benefitSumDecrease', 'benefitSumReduction', 'taxSumToPay']

@Field
def editableColumns = ['taxAuthority', 'kpp']

// Проверяемые на пустые значения атрибуты (графа 1..9, 11..15, 21)
@Field
def nonEmptyColumns = ['taxAuthority', 'kpp', 'okato', 'tsTypeCode', 'model', 'vi', 'regNumber', 'regDate',
                       'taxBase', 'taxBaseOkeiUnit', 'createYear', 'years', 'ownMonths', 'partRight', 'coef362',
                       'taxRate', 'calculatedTaxSum', 'taxSumToPay']

@Field
def monthCountInPeriod

@Field
def sdf = new SimpleDateFormat('dd.MM.yyyy')

@Field
def reportDay = null

// Надо ли выполнять проверку графы 24 в логических проверках
@Field
def needCheckTaxRate = true

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String columnName,
              def Date date, boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date,
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
    String dateStr = sdf.format(date)
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

def getProvider(def id) {
    return formDataService.getRefBookProvider(refBookFactory, id, providerCache)
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    if (reportDay == null) {
        reportDay = reportPeriodService.getReportDate(formData.reportPeriodId)?.time
    }
    return reportDay
}

/** Алгоритмы заполнения полей формы (9.1.1.8.1) Табл. 45. */
def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached

    def int monthCountInPeriod = getMonthCount()

    // Отчетная дата
    def reportDate = getReportDate()

    def reportPeriodStartDate = getReportPeriodStartDate()
    def reportPeriodEndDate = getReportPeriodEndDate()

    /** Уменьшающий процент. */
    def reducingPerc
    /** Пониженная ставка. */
    def loweringRates

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // получение региона по ОКТМО
        def region = getRegionByOKTMO(row.okato, errorMsg)

        // графа 2, 3
        fillTaKpp(row, errorMsg)

        // графа 21
        row.ownMonths = calc21(row, reportPeriodStartDate, reportPeriodEndDate)

        // Графа 23 - Коэффициент Кв
        if (row.ownMonths != null) {
            row.coef362 = (row.ownMonths / monthCountInPeriod).setScale(4, BigDecimal.ROUND_HALF_UP)
        } else {
            row.coef362 = null
            placeError(row, 'coef362', ['ownMonths'], errorMsg)
        }

        // Графа 24 (Налоговая ставка)
        row.taxRate = calc24(row, region, errorMsg)

        def partRight = new BigDecimal(row.partRight)
        // Графа 25 (Сумма исчисления налога) = Расчет суммы исчисления налога
        if (row.taxBase != null && row.coef362 != null && row.taxRate != null && partRight != null && row.koefKp != null) {
            def taxRate = getRefBookValue(41, row.taxRate)?.VALUE?.numberValue
            row.calculatedTaxSum = (row.taxBase * taxRate * partRight * row.coef362 * row.koefKp).setScale(0, BigDecimal.ROUND_HALF_UP)
        } else {
            row.calculatedTaxSum = null
            placeError(row, 'calculatedTaxSum', ['taxBase', 'coef362', 'taxRate', 'partRight', 'koefKp'], errorMsg)
        }
        // Графа 26 Определяется количество полных месяцев использования льготы в отчетном году
        if (row.benefitStartDate == null && row.benefitEndDate == null) {
            row.benefitMonths = null
        } else {
            if (row.benefitEndDate != null && row.benefitEndDate.compareTo(reportPeriodStartDate) < 0 ||
                    row.benefitStartDate.compareTo(reportPeriodEndDate) > 0) {
                row.benefitMonths = 0
            } else {
                //Определяем Доконч
                def dOkonch = null
                if (row.benefitEndDate != null || row.benefitEndDate.compareTo(reportPeriodStartDate) > 0) {
                    dOkonch = reportPeriodEndDate
                } else {
                    dOkonch = row.benefitEndDate
                }
                // Определяем Днач
                def dNach = (row.benefitStartDate.compareTo(reportPeriodStartDate) < 0) ? reportPeriodStartDate : row.benefitStartDate
                // Определяем Мльгот
                row.benefitMonths = dOkonch[Calendar.MONTH] - dOkonch[Calendar.MONTH] + 1
            }
        }

        // Графа 29 Коэффициент Кл
        if (row.taxBenefitCode != null) {
            if (row.benefitStartDate != null && row.benefitEndDate != null) {
                int start = row.benefitStartDate.month
                int end = row.benefitEndDate.month
                row.coefKl = (end - start + 1) / monthCountInPeriod
            } else {
                row.coefKl = null
            }
        }

        def taxRate = null
        if (row.taxRate != null) {
            taxRate = getRefBookValue(41, row.taxRate)?.VALUE?.numberValue

            // Графа 31
            if (row.taxBenefitCode != null) {
                if (row.taxBase != null && partRight != null && row.koefKp != null && row.coefKl != null) {
                    row.benefitSum = (row.taxBase * taxRate * partRight * row.koefKp * row.coefKl).setScale(0, BigDecimal.ROUND_HALF_UP)
                }
            }

            // Графа 33
            if (row.taxBenefitCodeDecrease != null) {
                reducingPerc = getCodeRecord(row.taxBenefitCodeDecrease, region, errorMsg)?.PERCENT?.numberValue
                if (reducingPerc != null && row.taxBase != null && partRight != null && row.koefKp != null && row.coefKl != null) {
                    row.benefitSumDecrease = (row.taxBase * taxRate * partRight * row.koefKp * row.coefKl * reducingPerc).setScale(0, BigDecimal.ROUND_HALF_UP) / 100
                }
            }

            // Графа 35
            if (row.benefitCodeReduction != null) {
                loweringRates = getCodeRecord(row.benefitCodeReduction, region, errorMsg)?.RATE?.numberValue
                if (loweringRates != null && row.taxBase != null && partRight != null && row.koefKp != null && row.coefKl != null) {
                    row.benefitSumReduction = (row.taxBase * (taxRate - loweringRates) / 100 * partRight * row.koefKp * row.coefKl).setScale(0, BigDecimal.ROUND_HALF_UP)
                }
            }
        }

        // Графа 37 - Исчисленная сумма налога, подлежащая уплате в бюджет.
        if (row.taxBenefitCode != null) {
            if (row.calculatedTaxSum != null) {
                row.taxSumToPay = (row.calculatedTaxSum - (row.benefitSum ?: 0)).setScale(0, BigDecimal.ROUND_HALF_UP)
            } else {
                row.taxSumToPay = null
                placeError(row, 'taxSumToPay', ['calculatedTaxSum', 'benefitSum'], errorMsg)
            }
        } else {
            if (row.calculatedTaxSum != null) {
                row.taxSumToPay = (row.calculatedTaxSum - (row.benefitSumDecrease ?: 0) - (row.benefitSumReduction ?: 0)).setScale(0, BigDecimal.ROUND_HALF_UP)
            } else {
                row.taxSumToPay = null
                placeError(row, 'taxSumToPay', ['calculatedTaxSum', 'benefitSumDecrease', 'benefitSumReduction'], errorMsg)
            }
        }
        /*
         * Графа 9 Единица измерения налоговой базы по ОКЕИ
         * Скрипт для выставления значения по умолчанию в столбец "Единица измерения налоговой базы по ОКЕИ",
         * если это значение не задано.
         */
        if (row.taxBaseOkeiUnit == null) {
            row.taxBaseOkeiUnit = getRecord(12, 'CODE', '251', index, getColumnName(row, 'taxBaseOkeiUnit'), reportDate)?.record_id?.numberValue
        }
    }

    groups.each { section ->
        def from = getDataRow(dataRows, section).getIndex()
        def to = getDataRow(dataRows, "total$section").getIndex() - 2
        calcTotalSum(dataRows[from..to], getDataRow(dataRows, section), totalColumns)
    }

    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(dataRows, totalRow, totalColumns)

    dataRowHelper.save(dataRows)

    // Сортировка групп и строк
    sortFormDataRows()
}

void fillTaKpp(def row, def errorMsg) {
    String filter = "DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() + " and OKTMO = " + row.okato?.toString()
    def records = getProvider(210L).getRecords(getReportPeriodEndDate(), null, filter, null)
    if (records.size() == 1) {
        row.taxAuthority = records[0].TAX_ORGAN_CODE?.value
        row.kpp = records[0].KPP?.value
    } else {
        logger.error(errorMsg + "Для кода ОКТМО «${row.okato}» нет данных в справочнике «Параметры представления деклараций по транспортному налогу»!")
    }
}

def checkTaKpp(def row, def errorMsg) {
    def String filter =  String.format("DECLARATION_REGION_ID = ${formDataDepartment.regionId?.toString()}"+
            " and OKTMO = ${row.okato?.toString()}" +
            " and LOWER(TAX_ORGAN_CODE) = LOWER('${row.taxAuthority?.toString()}') " +
            " and LOWER(KPP) = LOWER('${row.kpp?.toString()}')")
    def records = getProvider(210L).getRecords(getReportPeriodEndDate(), null, filter, null)
    if (records.size() != 1) {
        logger.error(errorMsg + "Для заданных параметров декларации («Код НО», «КПП», «Код ОКТМО» ) нет данных в справочнике «Параметры представления деклараций по транспортному налогу»!")
    }
}

def getCodeRecord (def recordId, def region, def errorMsg) {
    if (recordId != null) {
        // получение параметров региона
        // запрос по выборке данных из справочника
        def query = "TAX_BENEFIT_ID = " + recordId + " and DICT_REGION_ID = " + region.record_id
        def record = getRecord(7, query, reportDate)

        if (record == null) {
            logger.error(errorMsg + "Ошибка при получении параметров налоговых льгот.")
            return null
        } else {
            return record
        }
    }
    return null
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached
    def int monthCountInPeriod = getMonthCount()

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля (графа 1..9, 11..15, 21)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        def benefitCode = row.taxBenefitCode ?: (row.taxBenefitCodeDecrease ?: (row.benefitCodeReduction ?: null))
        def benefitSum = row.benefitSum ?: (row.benefitSumDecrease ?: (row.benefitSumReduction ?: null))

        // 2. Поверка на соответствие дат использования льготы
        if (benefitCode && row.benefitEndDate != null && (row.benefitStartDate == null || row.benefitStartDate > row.benefitEndDate)) {
            logger.error(errorMsg + "Дата начала(окончания) использования льготы неверная!")
        }

        // 3. Проверка, что Сумма исчисления налога больше или равна Сумма налоговой льготы
        if (row.calculatedTaxSum != null && benefitSum != null && row.calculatedTaxSum < benefitSum) {
            logger.error(errorMsg + 'Исчисленная сумма налога меньше Суммы налоговой льготы!')
        }

        // 4. Проверка значения поля Кв
        if (row.coef362 != null && (row.coef362 < 0.0 || row.coef362 > 1.0)) {
            logger.error(errorMsg + 'Коэффициент, определяемый в соответствии с п.3 ст.362 НК РФ меньше нуля либо больше единицы!')
        }

        // 5. Проверка значения поля Кл
        if (row.coefKl != null && (row.coefKl < 0.0 || row.coefKl > 1.0)) {
            logger.error(errorMsg + 'Коэффициент, определяемый в соответствии с законами субъектов РФ меньше нуля либо больше единицы!')
        }

        // 6. Проверка одновременного заполнения данных о налоговой льготе
        def notNull17_20 = row.benefitStartDate != null && row.benefitEndDate != null && row.coefKl != null && benefitSum != null
        if ((benefitCode != null) ^ notNull17_20) {
            logger.error(errorMsg + "Данные о налоговой льготе указаны не полностью.")
        }

        // 7. В справочнике «Параметры представления деклараций по транспортному налогу» существует хотя бы одна запись,
        // удовлетворяющая условиям выборки, приведённой в алгоритме расчёта «Графы 2 и 3»
        fillTaKpp(row, errorMsg)

        // дополнительная проверка для 12 графы
        if (row.ownMonths != null && row.ownMonths > monthCountInPeriod) {
            logger.warn('Срок владение ТС не должен быть больше текущего налогового периода.')
        }

        // 8. Проверка налоговой ставки ТС
        // В справочнике «Ставки транспортного налога» существует строка, удовлетворяющая условиям выборки,
        // приведённой в алгоритме расчёта «графы 24» Табл. 3
        if (needCheckTaxRate) {
            // получение региона по ОКТМО
            def region = getRegionByOKTMO(row.okato, errorMsg)
            calc24(row, region, errorMsg)
        }

        // 9. Проверка на корректность заполнения кода НО и КПП согласно справочнику параметров представления деклараций
        checkTaKpp(row, errorMsg)
    }
}

/**
 * Получение региона по коду ОКТМО
 */
def getRegionByOKTMO(def oktmoCell, def errorMsg) {
    def reportDate = getReportDate()

    def oktmo3 = getRefBookValue(96, oktmoCell)?.CODE?.stringValue?.substring(0, 2)
    if (oktmo3.equals("719")) {
        return getRecord(4, 'CODE', '89', null, null, reportDate);
    } else if (oktmo3.equals("718")) {
        return getRecord(4, 'CODE', '86', null, null, reportDate);
    } else if (oktmo3.equals("118")) {
        return getRecord(4, 'CODE', '83', null, null, reportDate);
    } else {
        def filter = "OKTMO_DEFINITION like '" + oktmo3?.substring(0, 2) + "%'"
        def record = getRecord(4, filter, reportDate)
        if (record != null) {
            return record
        } else {
            logger.error(errorMsg + "Не удалось определить регион по коду ОКТМО")
            return null
        }
    }
}

/**
 * Консолидация формы
 * Собирает данные с консолидированных нф
 */
def consolidation() {
    // очистить форму
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    dataRows.removeAll{ it.getAlias() == null }
    Map<String, List> dataRowsMap = ['A':[], 'B':[], 'C':[]]
    List<DataRow<Cell>> sourcesRows = new ArrayList()
    List<Department> departments = new ArrayList()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            def sourceDataRowHelper = formDataService.getDataRowHelper(source)
            def sourceDataRows = sourceDataRowHelper.allCached
            def formTypeVehicleId = 204
            if (source.formType.id == formTypeVehicleId) {
                Department sDepartment = departmentService.get(it.departmentId)
                def alias = null
                for (sRow in sourceDataRows) {
                    if (sRow.getAlias() != null) {
                        alias = sRow.getAlias()
                        continue
                    }
                    /**
                     * Если нашлись две строки с одинаковыми данными то ее не добавляем
                     * в общий список, и проверим остальные поля
                     */
                    def containedRow = sourcesRows.find { el ->
                        (el.codeOKATO.equals(sRow.codeOKATO) && el.identNumber.equals(sRow.identNumber)
                                && el.taxBase.equals(sRow.taxBase) && el.baseUnit.equals(sRow.baseUnit))
                    }
                    if (containedRow != null) {
                        DataRow<Cell> row = containedRow
                        // если поля совпадают то ругаемся и убираем текущую совпавшую с коллекции
                        if (row.taxBenefitCode == sRow.taxBenefitCode &&
                                row.benefitStartDate.equals(sRow.benefitStartDate) &&
                                row.benefitEndDate.equals(sRow.benefitEndDate)) {
                            def department = departments.get(sourcesRows.indexOf(row))
                            logger.error("Обнаружены несколько разных строк, у которых совпадают " +
                                    +getIdentGraphsValue(sRow) +
                                    " для форм «Сведения о транспортных средствах …» в подразделениях «" +
                                    sDepartment.name + "», «" + department.name + "». Строки : " + sRow.getIndex() + ", " + row.getIndex())
                            departments.remove(sourcesRows.indexOf(row))
                            sourcesRows.remove(sRow)
                        }
                    } else {
                        sourcesRows.add(sRow)
                        departments.add(sDepartment)

                        if (alias != null && alias in groups) {
                            def newRow = formNewRow(sRow)
                            dataRowsMap[alias].add(newRow)
                        }
                    }
                }
            } else if (source.getFormType().getId() == formData.getFormType().getId()) {
                def alias = null
                for (row in sourceDataRows) {
                    if (row.getAlias() != null) {
                        alias = row.getAlias()
                        continue
                    }
                    if (alias != null && alias in groups) {
                        dataRowsMap[alias].add(row)
                    }
                }
            }
        }
    }

    // копирование данных по разделам
    groups.each { section ->
        def copyRows = dataRowsMap[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, "$section").getIndex()
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            dataRows.eachWithIndex { row, i ->
                row.setIndex(i + 1)
            }
        }
    }

    dataRowHelper.save(dataRows)
}

def formNewRow(def sRow) {
// новая строка
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).setStyleAlias("Редактируемое поле")
    }
    // «Графа 4» принимает значение «графы 2» формы-источника
    newRow.okato = sRow.codeOKATO
    // «Графа 5» принимает значение «графы 4» формы-источника
    newRow.tsTypeCode = sRow.tsTypeCode
    // «Графа 6» принимает значение «графы 5» формы-источника
    // зависимая графа newRow.tsType = sRow.tsType
    // «Графа 7» принимает значение «графы 6» формы-источника
    newRow.model = sRow.model
    // «Графа 8» принимает значение «графы 7» формы-источника
    newRow.ecoClass = sRow.ecoClass
    // «Графа 9» принимает значение «графы 8» формы-источника
    newRow.vi = sRow.identNumber
    // «Графа 10» принимает значение «графы 9» формы-источника
    newRow.regNumber = sRow.regNumber
    // «Графа 11» принимает значение «графы 10» формы-источника
    newRow.regDate = sRow.regDate
    // «Графа 12» принимает значение «графы 11» формы-источника
    newRow.regDateEnd = sRow.regDateEnd
    // «Графа 13» принимает значение «графы 12» формы-источника
    newRow.taxBase = sRow.taxBase
    // «Графа 14» принимает значение «графы 13» формы-источника
    newRow.taxBaseOkeiUnit = sRow.baseUnit
    // «Графа 15» принимает значение «графы 14» формы-источника
    newRow.createYear = sRow.year
    // «Графа 16» принимает значение «графы 15» формы-источника
    newRow.years = sRow.pastYear
    // «Графа 17» принимает значение «графы 16» формы-источника
    newRow.stealDateStart = sRow.stealDateStart
    // «Графа 18» принимает значение «графы 17» формы-источника
    newRow.stealDateEnd = sRow.stealDateEnd
    // «Графа 19» принимает значение «графы 19» формы-источника
    newRow.periodStartCost = sRow.costOnPeriodBegin
    // «Графа 20» принимает значение «графы 20» формы-источника
    newRow.periodEndCost = sRow.costOnPeriodEnd
    // «Графа 22» принимает значение «графы 18» формы-источника
    newRow.partRight = sRow.share
    // «Графа 27» принимает значение «графы 21» формы-источника
    newRow.benefitStartDate = sRow.benefitStartDate
    // «Графа 28» принимает значение «графы 22» формы-источника
    newRow.benefitEndDate = sRow.benefitEndDate
    // «Графа 38» принимает значение «графы 24» формы-источника
    newRow.benefitBase = sRow.base
    // «Графы 30, 32, 34»
    // Если «Графа 23» формы-источника имеет пустое значение, то «Графа 30», «Графа 32» и «Графа 34» не заполняются.
    def taxBenefitCode = sRow.taxBenefitCode
    if (taxBenefitCode != null) {
        def benefitCode = getBenefitCode(taxBenefitCode)
        switch (benefitCode) {
        // Иначе если « Графа 23 » формы - источника
        // имеет значение « 20220 », то « Графа 32 » принимает значение « Графы 23 » формы - источника, « Графа 30 » и « Графа 34 » не заполняются.
            case '20220': newRow.taxBenefitCodeDecrease = taxBenefitCode
                break
        // Иначе если « Графа 23 » формы - источника
        // имеет значение « 20230 », то « Графа 34 » принимает значение « Графы 23 » формы - источника, « Графа 30 » и « Графа 32 » не заполняются.
            case '20230': newRow.benefitCodeReduction = taxBenefitCode
                break
        // Иначе « Графа 30 » принимает значение « Графы 23 » формы - источника, « Графа 32 » и « Графа 34 » не заполняются.
            default: newRow.taxBenefitCode = taxBenefitCode
        }
    }
    // «Графа 36»
    // Если «Графа 25» формы-источника имеет пустое значение, то «Графа 36» не заполняется
    if (sRow.version != null) {
        // "Средняя стоимость транспортных средст"
        def avgPriceRecord = getRefBookValue(208, sRow.version)
        // В справочнике «Повышающие коэффициенты транспортного налога» найти записи
        def filter = "(YEAR_FROM < " + sRow.pastYear + " OR YEAR_FROM = " + sRow.pastYear + ") and (YEAR_TO > " + sRow.pastYear + " or YEAR_TO = " + sRow.pastYear + ") and AVG_COST = " + avgPriceRecord.AVG_COST.value
        def records = getProvider(209L).getRecords(getReportPeriodEndDate(), null, filter, null)
        if (records.size() == 0) {
            // "Категории средней стоимости транспортных средств"
            logger.error("Для средней стоимости ${getRefBookValue(211, avgPriceRecord.AVG_COST.value).NAME.value} нет данных в справочнике «Повышающие коэффициенты транспортного налога»!")
        } else if (records.size() == 1) {
            newRow.koefKp = records[0].COEF.value
        } else {
            newRow.koefKp = null
        }
    } else {
        newRow.koefKp = null
    }
    return newRow
}

String getIdentGraphsValue(def row) {
    return "Код ОКТМО = ${getRefBookValue(96, row.codeOKATO)?.CODE?.stringValue}, " +
            "Код вида ТС = ${getRefBookValue(42, row.tsTypeCode)?.CODE?.stringValue}, " +
            "Идентификационный номер ТС = $row.identNumber, " +
            "Налоговая база = $row.taxBase, " +
            "Единица измерения налоговой базы по ОКЕИ = ${getRefBookValue(12, row.baseUnit)?.CODE?.stringValue}"
}

// Расчет графы 21
int calc21(DataRow sRow, Date reportPeriodStartDate, Date reportPeriodEndDate) {
    // Дугона – дата угона
    Date stealingDate = Calendar.getInstance().time
    // Двозврата – дата возврата
    Date returnDate = Calendar.getInstance().time
    // Дпостановки – дата постановки ТС на учет
    Date deliveryDate = Calendar.getInstance().time
    // Дснятия – дата снятия ТС с учета
    Date removalDate = Calendar.getInstance().time
    // владенеи в месяцах
    int ownMonths
    // Срока нахождения в угоне (Мугон)
    int stealingMonths

    /*
     * Если  [«графа 14»(источника) заполнена И «графа 14»(источника)< «Дата начала периода»]
     * ИЛИ [«графа 13»>«Дата окончания периода»], то
     * Графа 12=0
     */
    if ((sRow.regDateEnd != null && sRow.regDateEnd.compareTo(reportPeriodStartDate) < 0)
            || sRow.regDate.compareTo(reportPeriodEndDate) > 0) {
        return 0
    } else { // иначе
        //Определяем Мугон
        //Если «графа 15» (источника) не заполнена, то Мугон = 0
        if (sRow.stealDateStart == null) {
            stealingMonths = 0
        } else { // инчае
            /**
             * Если «графа 15»(источника)< «Дата начала периода», то
             *  Дугона = «Дата начала периода»
             *  Иначе
             *  Дугона = «графа 15»(источника)
             */
            if (sRow.stealDateStart.compareTo(reportPeriodStartDate) < 0) {
                stealingDate = reportPeriodStartDate
            } else {
                stealingDate = sRow.stealDateStart
            }

            /**
             * Определяем Двозврат
             * Если [«графа 16»(источника) не заполнена] ИЛИ [«графа 16»(источника)> «Дата окончания периода»], то
             *  Двозврата = «Дата окончания периода»
             * Иначе
             * Двозврата = «графа 16»(источника)
             *
             */
            if (sRow.stealDateEnd == null || sRow.stealDateEnd.compareTo(reportPeriodEndDate) > 0) {
                returnDate = reportPeriodEndDate
            } else {
                returnDate = sRow.stealDateEnd
            }

            /**
             * Определяем Мугон
             * Если (МЕСЯЦ(Двозврата)-МЕСЯЦ(Дугона)-1)<0, то
             *  Мугон = 0
             * Иначе
             *  Мугон = МЕСЯЦ(Двозврата)-МЕСЯЦ(Дугона)-1
             */
            def diff1 = (returnDate[Calendar.YEAR] * 12 + returnDate[Calendar.MONTH]) - (stealingDate[Calendar.YEAR] * 12 + stealingDate[Calendar.MONTH]) - 1
            if (diff1 < 0) {
                stealingMonths = 0
            } else {
                stealingMonths = diff1
            }
        }

        /**
         * Определяем Дснятия
         * Если «графа 14»(источника) не заполнена ИЛИ «графа 14»(источника)> «Дата окончания периода», то
         *  Дснятия = «Дата окончания периода»
         * Иначе
         *  Дснятия = «графа 14»(источника)
         */
        if (sRow.regDateEnd == null || sRow.regDateEnd.compareTo(reportPeriodEndDate) > 0) {
            removalDate = reportPeriodEndDate
        } else {
            removalDate = sRow.regDateEnd
        }

        /**
         * Определяем Дпостановки
         * Если «графа 13»(источника)< «Дата начала периода», то
         *  Дпостановки = «Дата начала периода»
         * Иначе
         *  Дпостановки = «графа 13»(источника)
         */
        if (sRow.regDate.compareTo(reportPeriodStartDate) < 0) {
            deliveryDate = reportPeriodStartDate
        } else {
            deliveryDate = sRow.regDate
        }

        /**
         * Определяем Мвлад
         * Мвлад = МЕСЯЦ[Дснятия] - МЕСЯЦ[Дпостановки]+1
         */
        ownMonths = (removalDate[Calendar.YEAR] * 12 + removalDate[Calendar.MONTH]) - (deliveryDate[Calendar.YEAR] * 12 + deliveryDate[Calendar.MONTH]) + 1
        /**
         * Определяем графу 12
         * Графа 12=Мвлад-Мугон
         */
        return ownMonths - stealingMonths
    }
}

/** Число полных месяцев в текущем периоде (либо отчетном либо налоговом). */
def getMonthCount() {
    if (monthCountInPeriod == null) {
        def period = reportPeriodService.get(formData.reportPeriodId)
        if (period == null) {
            logger.error('Не найден отчетный период для налоговой формы.')
        } else {
            // 1. Отчетные периоды:
            //  a.	первый квартал (с января по март включительно) - 3 мес.
            //  b.	второй квартал (с апреля по июнь включительно) - 3 мес.
            //  c.	третий квартал (с июля по сентябрь включительно) - 3 мес.
            // 2. Налоговый период
            //  a.	год (с января по декабрь включительно) - 12 мес.
            monthCountInPeriod = period.order < 4 ? 3 : 12
        }
    }
    return monthCountInPeriod
}

/**
 * Метод выводит сообщение о невозможности рассчитать поле
 * @param row раасчитываемое поле
 * @param alias рассчитываемое поле
 * @param errorFields поля от которых оно зависит
 */
void placeError(DataRow row, String alias, ArrayList<String> errorFields, String errorMsg) {
    def fields = []
    for (errAlias in errorFields) {
        if (row[errAlias] == null) {
            fields.add("\"${getColumnName(row, errAlias)}\"")
        }
    }
    logger.error(errorMsg + "\"${getColumnName(row, alias)}\" не может быть вычислена, т.к. не заполнены поля: $fields.")
}

/**
 * Графа 14 (Налоговая ставка)
 * Скрипт для вычисления налоговой ставки
 */
def calc24(def row, def region, def errorMsg) {
    needCheckTaxRate = false
    def tsTypeCode
    if (row.tsTypeCode != null && row.years != null && row.taxBase != null) {
        tsTypeCode = getRefBookValue(42, row.tsTypeCode)?.CODE?.stringValue

        // запрос по выборке данных из справочника
        def query = " and DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() +
                " and ((MIN_POWER is null or MIN_POWER < " + row.taxBase + ") " +
                "and (MAX_POWER is null or MAX_POWER > " + row.taxBase + " or MAX_POWER = " + row.taxBase + "))" +
                "and (UNIT_OF_POWER is null or UNIT_OF_POWER = " + row.taxBaseOkeiUnit + ")" +
                "and ((MIN_AGE is null or MIN_AGE < " + row.years + ") " +
                "and (MAX_AGE is null or MAX_AGE > " + row.years + " or MAX_AGE = " + row.years + "))"

        /**
         * Переберем варианты
         * 1. код = коду ТС && регион указан
         * 2. код = коду ТС && регион НЕ указан
         * 3. код = соответствует 2м двум символом кода ТС && регион указан
         * 4. код = соответствует 2м двум символом кода ТС && регион НЕ указан
         */
        def regionSqlPartID = " and DICT_REGION_ID = " + region?.record_id?.numberValue
        def regionSqlPartNull = " and DICT_REGION_ID is null"
        def queryLike = "CODE LIKE '" + tsTypeCode.substring(0, 2) + "%'" + query
        def queryLikeStrictly = "CODE LIKE '" + tsTypeCode + "'" + query

        def reportDate = getReportDate()

        // вариант 1
        def record = getRecord(41, queryLikeStrictly + regionSqlPartID, reportDate)
        // вариант 2
        if (record == null) {
            record = getRecord(41, queryLikeStrictly + regionSqlPartNull, reportDate)
        }
        // вариант 3
        if (record == null) {
            record = getRecord(41, queryLike + regionSqlPartID, reportDate)
        }
        // вариант 4
        if (record == null) {
            record = getRecord(41, queryLike + regionSqlPartNull, reportDate)
        }

        if (record != null) {
            return record.record_id.numberValue
        } else {
            logger.error(errorMsg + "Для заданных параметров ТС («Код вида ТС», «Налоговая база», " +
                    "«Единица измерения налоговой базы по ОКЕИ», «Количество полных месяцев владения», «Код ОКТМО») " +
                    "в справочнике «Ставки транспортного налога» не найдена соответствующая налоговая ставка ТС!")
        }
    } else {
        placeError(row, 'taxRate', ['tsTypeCode', 'years', 'taxBase'], errorMsg)
    }
    return null
}

// Получение подитоговых строк
def getTotalRow(def dataRows) {
    return dataRows.find { it.getAlias() != null && it.getAlias().equals('total')}
}

def getBenefitCode(def parentRecordId) {
    def recordId = getRefBookValue(7, parentRecordId).TAX_BENEFIT_ID.value
    return  getRefBookValue(6, recordId).CODE.value
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    groups.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, "total$section")
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1
        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        // Массовое разыменование строк НФ
        def columnList = firstRow.keySet().collect{firstRow.getCell(it).getColumn()}
        refBookService.dataRowsDereference(logger, sectionsRows, columnList)

        sortRowsSimple(sectionsRows)
    }

    dataRowHelper.saveSort()
}

// Проверка заполнения атрибута «Регион» подразделения текущей формы (справочник «Подразделения»)
void checkRegionId() {
    if (formDataDepartment.regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }
}