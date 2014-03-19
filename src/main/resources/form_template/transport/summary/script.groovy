package form_template.transport.summary

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Расчет суммы налога по каждому транспортному средству
 * formTemplateId=200
 *
 * графа 1  - rowNumber
 * графа 2  - okato
 * графа 3  - tsTypeCode
 * графа 4  - tsType
 * графа 5  - vi
 * графа 6  - model
 * графа 7  - regNumber
 * графа 8  - taxBase
 * графа 9  - taxBaseOkeiUnit
 * графа 10 - ecoClass
 * графа 11 - years
 * графа 12 - ownMonths
 * графа 13 - coef362
 * графа 14 - taxRate
 * графа 15 - calculatedTaxSum
 * графа 16 - taxBenefitCode
 * графа 17 - benefitStartDate
 * графа 18 - benefitEndDate
 * графа 19 - coefKl
 * графа 20 - benefitSum
 * графа 21 - taxSumToPay
 *
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

//Все аттрибуты
@Field
def allColumns = ['fix', 'rowNumber', 'okato', 'tsTypeCode', 'tsType', 'vi', 'model', 'regNumber', 'taxBase',
        'taxBaseOkeiUnit', 'ecoClass', 'years', 'ownMonths', 'coef362', 'taxRate', 'calculatedTaxSum',
        'taxBenefitCode', 'benefitStartDate', 'benefitEndDate', 'coefKl', 'benefitSum', 'taxSumToPay']

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ['calculatedTaxSum', 'benefitSum', 'taxSumToPay']

@Field
def sortColumns = ["okato", "tsTypeCode"]

// Автозаполняемые атрибуты
@Field
def nonEmptyColumns = ['okato', 'tsTypeCode', 'vi', 'model', 'regNumber', 'taxBase']

@Field
def monthCountInPeriod

@Field
def sdf = new SimpleDateFormat('dd.MM.yyyy')

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
                return refBookCache.get(recordId)
            } else {
                def retVal = new HashMap<String, RefBookValue>()
                retVal.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId))
                return retVal
            }
        }
    } else {
        recordCache.put(refBookId, [:])
    }

    def provider
    if (!providerCache.containsKey(refBookId)) {
        providerCache.put(refBookId, refBookFactory.getDataProvider(refBookId))
    }
    provider = providerCache.get(refBookId)

    def records = provider.getRecords(date, null, filter, null)
    // отличие от FormDataServiceImpl.getRefBookRecord(...)
    if (records.size() > 0) {
        def retVal = records.get(0)
        Long recordId = retVal.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
        recordCache.get(refBookId).put(dateStr + filter, recordId)
        if (refBookCache != null)
            refBookCache.put(recordId, retVal)
        return retVal
    }
    return null
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    return reportPeriodService.getReportDate(formData.reportPeriodId)?.time
}

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

/** Алгоритмы заполнения полей формы (9.1.1.8.1) Табл. 45. */
def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached

    deleteAllAliased(dataRows)
    dataRows.sort { a, b ->
        def tempA = getRefBookValue(96, a.okato)?.CODE?.stringValue
        def tempB = getRefBookValue(96, b.okato)?.CODE?.stringValue
        if (tempA == tempB) {
            tempA = getRefBookValue(42, a.tsTypeCode)?.CODE?.stringValue
            tempB = getRefBookValue(42, b.tsTypeCode)?.CODE?.stringValue
        }
        return tempA <=> tempB
    }

    def int monthCountInPeriod = getMonthCount()

    // Отчетная дата
    def reportDate = getReportDate()

    /** Уменьшающий процент. */
    def reducingPerc
    /** Пониженная ставка. */
    def loweringRates

    def i = 1
    dataRows.each { row ->
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // получение региона по ОКТМО
        def region = getRegionByOKTMO(row.okato, errorMsg)

        row.rowNumber = i++

        /*
         * Гафа 9 Единица измерения налоговой базы по ОКЕИ
         * Скрипт для выставления значения по умолчанию в столбец "Единица измерения налоговой базы по ОКЕИ",
         * если это значение не задано.
         */
        if (row.taxBaseOkeiUnit == null) {
            row.taxBaseOkeiUnit = getRecord(12, 'CODE', '251', index, getColumnName(row, 'taxBaseOkeiUnit'), reportDate)?.record_id?.numberValue
        }

        /*
         * Графа 13 - Коэффициент Кв
         * Скрипт для вычисления автоматических полей 13, 19, 20
         */
        if (row.ownMonths != null) {
            row.coef362 = (row.ownMonths / monthCountInPeriod).setScale(4, BigDecimal.ROUND_HALF_UP)
        } else {
            row.coef362 = null
            placeError(row, 'coef362', ['ownMonths'], errorMsg)
        }


        def tsTypeCode
        /*
         * Графа 14 (Налоговая ставка)
         * Скрипт для вычисления налоговой ставки
         */
        row.taxRate = null
        if (row.tsTypeCode != null && row.years != null && row.taxBase != null) {
            tsTypeCode = getRefBookValue(42, row.tsTypeCode)?.CODE?.stringValue

            // запрос по выборке данных из справочника
            def query = " and ((MIN_POWER is null or MIN_POWER < " + row.taxBase + ") " +
                    "and (MAX_POWER is null or MAX_POWER > " + row.taxBase + " or MAX_POWER = " + row.taxBase + "))" +
                    "and (UNIT_OF_POWER is null or UNIT_OF_POWER = " + row.taxBaseOkeiUnit + ")" +
                    "and ((MIN_AGE is null or MIN_AGE < " + row.years + ") " +
                    "and (MAX_AGE is null or MAX_AGE > " + row.years + "))";

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
                row.taxRate = record.record_id.numberValue
            } else {
                logger.error("Для заданных параметров ТС («Код вида транспортного средства», «Мощность от», " +
                        "Мощность до», «Ед. измерения мощности», «Возраст ТС (полных лет)», «Код по ОКТМО») " +
                        "в справочнике «Ставки транспортного налога» не найдена соответствующая налоговая ставка ТС.")
            }
        } else {
            placeError(row, 'taxRate', ['tsTypeCode', 'years', 'taxBase'], errorMsg)
        }

        /*
         * Графа 15 (Сумма исчисления налога) = Расчет суммы исчисления налога
         * Скрипт для вычисления значения столбца "сумма исчисления налога".
         */
        if (row.taxBase != null && row.coef362 != null && row.taxRate != null) {
            def taxRate = getRefBookValue(41, row.taxRate)?.VALUE?.numberValue
            row.calculatedTaxSum = (row.taxBase * row.coef362 * taxRate).setScale(0, BigDecimal.ROUND_HALF_UP)
        } else {
            row.calculatedTaxSum = null
            placeError(row, 'calculatedTaxSum', ['taxBase', 'coef362', 'taxRate'], errorMsg)
        }

        /*
         * Графа 19 Коэффициент Кл
         */
        if (row.taxBenefitCode != null) {
            if (row.benefitStartDate != null && row.benefitEndDate != null) {
                int start = row.benefitStartDate.getMonth()
                int end = row.benefitEndDate.getMonth()
                row.coefKl = (end - start + 1) / monthCountInPeriod
            } else {
                row.coefKl = null
            }
        }

        /*
         * Графа 20 - Сумма налоговой льготы (руб.)
         */
        if (row.taxBenefitCode != null) {
            def taxBenefitCode = getRefBookValue(6, row.taxBenefitCode)?.CODE?.stringValue
            // получение параметров региона
            if (taxBenefitCode != '20210' && taxBenefitCode != '30200') {
                if (row.taxBenefitCode) {
                    // запрос по выборке данных из справочника
                    def query = "TAX_BENEFIT_ID = " + row.taxBenefitCode + " and DICT_REGION_ID = " + region.record_id
                    def record = getRecord(7, query, reportDate)

                    if (record == null) {
                        logger.error(errorMsg + "Ошибка при получении параметров налоговых льгот.")
                        return;
                    } else {
                        reducingPerc = record.percent
                        loweringRates = record.rate
                    }
                }
            }


            if (row.taxRate != null) {
                def taxRate = getRefBookValue(41, row.taxRate)?.VALUE?.numberValue
                if (taxBenefitCode == '20210' || taxBenefitCode == '30200') {
                    row.benefitSum = (row.taxBase * row.coefKl * taxRate).setScale(0, BigDecimal.ROUND_HALF_UP)
                } else if (taxBenefitCode == '20220') {
                    row.benefitSum = round(row.taxBase * taxRate * row.coefKl * reducingPerc / 100, 0)
                } else if (taxBenefitCode == '20230') {
                    row.benefitSum = round(row.coefKl * taxRate * (taxRate - loweringRates), 0)
                } else {
                    row.benefitSum = 0
                }
            }
        }

        /*
         * Графа 21 - Исчисленная сумма налога, подлежащая уплате в бюджет.
         * Скрипт для вычисления значения столбца "Исчисленная сумма налога, подлежащая уплате в бюджет".
         */
        if (row.calculatedTaxSum != null) {
            row.taxSumToPay = (row.calculatedTaxSum - (row.benefitSum ?: 0)).setScale(0, BigDecimal.ROUND_HALF_UP)
        } else {
            row.taxSumToPay = null
            placeError(row, 'taxSumToPay', ['calculatedTaxSum', 'benefitSum'], errorMsg)
        }
    }
    // добавление строки ИТОГО
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.getCell("fix").setColSpan(2)
    totalRow.fix = 'ИТОГО'
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)

    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached
    def int monthCountInPeriod = getMonthCount()

    for (def row : dataRows) {
        if (row.getAlias() == 'total') {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 13 графа - Поверка на соответствие дат использования льготы
        if (row.taxBenefitCode && row.benefitEndDate != null && (row.benefitStartDate == null || row.benefitStartDate > row.benefitEndDate)) {
            logger.error(errorMsg + "Дата начала(окончания) использования льготы неверная!")
        }

        // 14 граафа - Проверка, что Сумма исчисления налога больше или равна Сумма налоговой льготы
        if (row.calculatedTaxSum != null && row.benefitSum != null
                && row.calculatedTaxSum < row.benefitSum) {
            logger.error(errorMsg + 'Сумма исчисления налога меньше Суммы налоговой льготы')
        }

        // 15 графа - Проверка Коэффициент Кв
        if (row.coef362 != null) {
            if (row.coef362 < 0.0) {
                logger.error(errorMsg + 'Коэффициент Кв меньше нуля')
            } else if (row.coef362 > 1.0) {
                logger.error(errorMsg + 'Коэффициент Кв больше единицы')
            }
        }

        // 16 графа - Проверка Коэффициент Кл
        if (row.coefKl != null) {
            if (row.coefKl < 0.0) {
                logger.error(errorMsg + 'Коэффициент Кл меньше нуля')
            } else if (row.coefKl > 1.0) {
                logger.error(errorMsg + 'Коэффициент Кл больше единицы')
            }
        }

        /**
         * Проверка одновременного не заполнения данных о налоговой льготе
         *
         * Если  «графа 16» не заполнена ТО не заполнены графы 17,18,19,20
         */
        def notNull17_20 = row.benefitStartDate != null && row.benefitEndDate != null && row.coefKl != null && row.benefitSum != null
        if ((row.taxBenefitCode != null) ^ notNull17_20) {
            logger.error(errorMsg + "Данные о налоговой льготе указаны не полностью.")
        }

        // дополнительная проверка для 12 графы
        if (row.ownMonths != null && row.ownMonths > monthCountInPeriod) {
            logger.warn('Срок владение ТС не должен быть больше текущего налогового периода.')
        }

        /**
         * Проверка льготы
         * Проверка осуществляется только для кодов 20210, 20220, 20230
         */
        if (row.taxBenefitCode != null && getRefBookValue(6, row.taxBenefitCode)?.CODE?.stringValue in ['20210', '20220', '20230']) {
            def region = getRegionByOKTMO(row.okato, errorMsg)
            query = "TAX_BENEFIT_ID =" + row.taxBenefitCode + " AND DICT_REGION_ID = " + region.record_id
            if (getRecord(7, query, reportDate) == null) {
                logger.error(errorMsg + "Выбранная льгота для текущего региона не предусмотрена!")
            }
        }
    }
}

/**
 * Получение региона по коду ОКТМО
 */
def getRegionByOKTMO(def oktmoCell, def errorMsg) {
    def reportDate = getReportDate()

    def oktmo3 = getRefBookValue(96, oktmoCell)?.CODE?.stringValue.substring(0, 2)
    if (oktmo3.equals("719")) {
        return getRecord(4, 'CODE', '89', null, null, reportDate);
    } else if (oktmo3.equals("718")) {
        return getRecord(4, 'CODE', '86', null, null, reportDate);
    } else if (oktmo3.equals("118")) {
        return getRecord(4, 'CODE', '83', null, null, reportDate);
    } else {
        def filter = "OKTMO_DEFINITION like '" + oktmo3.substring(0, 2) + "%'"
        def record = getRecord(4, filter, reportDate)
        if (record != null) {
            return record
        } else {
            logger.error(errorMsg + "Не удалось определить регион по коду ОКТМО")
            return null;
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
    List dataRows = new ArrayList<DataRow<Cell>>()
    List<DataRow<Cell>> sources202 = new ArrayList()
    List<Department> departments = new ArrayList()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            def sourceDataRowHelper = formDataService.getDataRowHelper(source)
            def sourceDataRows = sourceDataRowHelper.allCached
            // формы типа 202 собираем, проверяем на пересечение, для дальнейшего использования
            if (source.formType.id == 202) {
                Department sDepartment = departmentService.get(it.departmentId)
                sourceDataRows.each { sRow ->
                    /**
                     * Если нашлись две строки с одинаковыми данными то ее не добавляем
                     * в общий список, и проверим остальные поля
                     */
                    def contains = sources202.find { el ->
                        (el.codeOKATO.equals(sRow.codeOKATO) && el.identNumber.equals(sRow.identNumber)
                                && el.powerVal.equals(sRow.powerVal) && el.baseUnit.equals(sRow.baseUnit))
                    }
                    if (contains != null) {
                        DataRow<Cell> row = contains
                        // если поля совпадают то ругаемся и убираем текущую совпавшую с коллекции
                        if (row.taxBenefitCode == sRow.taxBenefitCode &&
                                row.benefitStartDate.equals(sRow.benefitStartDate) &&
                                row.benefitEndDate.equals(sRow.benefitEndDate)) {
                            def department = departments.get(sources202.indexOf(row))
                            logger.error("Обнаружены несколько разных строк, у которых совпадают " +
                                    +getIdentGrafsValue(sRow) +
                                    " для форм «Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог» в подразделениях «" +
                                    sDepartment.name + "», «" + department.name + "». Строки : " + sRow.getIndex() + ", " + row.getIndex())
                            departments.remove(sources202.indexOf(row))
                            sources202.remove(sRow)
                        }
                    } else {
                        sources202.add(sRow)
                        departments.add(sDepartment)
                    }
                }
            } else if (source.formType.id == 201){
                def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
                def taxPeriod = reportPeriod.taxPeriod
                // дата начала отчетного периода
                Calendar reportPeriodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
                // дата конца отчетного периода
                Calendar reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)
                sourceDataRows.each { sRow ->
                    // новая строка
                    def newRow = formData.createDataRow()
                    // «Графа 2» принимает значение «графы 2» формы-источника
                    newRow.okato = sRow.codeOKATO
                    // «Графа 3» принимает значение «графы 4» формы-источника
                    newRow.tsTypeCode = sRow.tsTypeCode
                    // «Графа 4» принимает значение «графы 5» формы-источника
                    // newRow.tsType = sRow.tsType
                    // «Графа 5» принимает значение «графы 6» формы-источника
                    newRow.vi = sRow.identNumber
                    // «Графа 6» принимает значение «графы 7» формы-источника
                    newRow.model = sRow.model
                    // «Графа 7» принимает значение «графы 9» формы-источника
                    newRow.regNumber = sRow.regNumber
                    // «Графа 8» принимает значение «графы 10» формы-источника
                    newRow.taxBase = sRow.powerVal
                    // «Графа 9» принимает значение «графы 11» формы-источника
                    newRow.taxBaseOkeiUnit = sRow.baseUnit
                    // «Графа 10» принимает значение «графы 8» формы-источника
                    newRow.ecoClass = sRow.ecoClass
                    // «Графа 11»
                    Calendar cl2 = Calendar.getInstance()
                    cl2.setTime(sRow.year);
                    newRow.years = taxPeriod.year - cl2.get(Calendar.YEAR)
                    // «Графа 12»
                    newRow.ownMonths = calc12(sRow, reportPeriodStartDate, reportPeriodEndDate)

                    dataRows.add(newRow)
                }
            } else if (source.getFormType().getId() == formData.getFormType().getId()) {
                formDataService.getDataRowHelper(source).getAll().each { row ->
                    if (row.getAlias() == null) {
                        dataRows.add(row)
                    }
                }
            }
        }
    }

    /**
     * Расставим соответствия для формы с 202
     */
    int cnt = 0
    sources202.each { v ->
        cnt++
        // признак подстаноки текущей строки в сводную
        boolean use = false
        // пробежимся по форме расставим данные для текущей 202 строки
        dataRows.each { row ->
            // поиск
            if (v.codeOKATO.equals(row.okato) && v.identNumber.equals(row.vi)
                    && v.powerVal.equals(row.taxBase) && v.baseUnit.equals(row.taxBaseOkeiUnit)) {

                use = true
                row.taxBenefitCode = v.taxBenefitCode
                row.benefitStartDate = v.benefitStartDate
                row.benefitEndDate = v.benefitEndDate
            }
        }

        // если значения этой строки 202 формы не подставлялись в сводную то ругаемся
        if (!use) {
            def department = departments.get(sources202.indexOf(v))
            logger.warn("Для строки " + cnt + " в форме \"Сведения о льготируемых транспортных средствах, по которым " +
                    "уплачивается транспортный налог\" подразделения " + department.name + " указана льгота для  " +
                    "транспортного средства, не указанного в одной из форм \"Сведения о транспортных средствах, по " +
                    "которым уплачивается транспортный налог\". " + getIdentGrafsValue(v) + "!")
        }
    }
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
    dataRowHelper.save(dataRows)
}

String getIdentGrafsValue(def row) {
    return "Код ОКТМО = ${getRefBookValue(96, row.codeOKATO)?.CODE?.stringValue}, " +
            "Идентификационный номер = $row.identNumber, " +
            "Мощность (величина) = $row.powerVal, " +
            "Мощность (ед. измерения) = ${getRefBookValue(12, row.baseUnit)?.CODE?.stringValue}"
}

// Расчет графы 12 при консолидации
int calc12(DataRow sRow, Calendar reportPeriodStartDate, Calendar reportPeriodEndDate) {
    // Дугона – дата угона
    Calendar stealingDate = Calendar.getInstance()
    // Двозврата – дата возврата
    Calendar returnDate = Calendar.getInstance()
    // Дпостановки – дата постановки ТС на учет
    Calendar deliveryDate = Calendar.getInstance()
    // Дснятия – дата снятия ТС с учета
    Calendar removalDate = Calendar.getInstance()
    // владенеи в месяцах
    int ownMonths
    // Срока нахождения в угоне (Мугон)
    int stealingMonths

    /*
     * Если  [«графа 14»(источника) заполнена И «графа 14»(источника)< «Дата начала периода»]
     * ИЛИ [«графа 13»>«Дата окончания периода»], то
     * Графа 12=0
     */
    if ((sRow.regDateEnd != null && sRow.regDateEnd.compareTo(reportPeriodStartDate.time) < 0)
            || sRow.regDate.compareTo(reportPeriodEndDate.time) > 0) {
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
            if (sRow.stealDateStart.compareTo(reportPeriodStartDate.time) < 0) {
                stealingDate = reportPeriodStartDate
            } else {
                stealingDate.setTime(sRow.stealDateStart)
            }

            /**
             * Определяем Двозврат
             * Если [«графа 16»(источника) не заполнена] ИЛИ [«графа 16»(источника)> «Дата окончания периода»], то
             *  Двозврата = «Дата окончания периода»
             * Иначе
             * Двозврата = «графа 16»(источника)
             *
             */
            if (sRow.stealDateEnd == null || sRow.stealDateEnd.compareTo(reportPeriodEndDate.time) > 0) {
                returnDate = reportPeriodEndDate
            } else {
                returnDate.setTime(sRow.stealDateEnd)
            }

            /**
             * Определяем Мугон
             * Если (МЕСЯЦ(Двозврата)-МЕСЯЦ(Дугона)-1)<0, то
             *  Мугон = 0
             * Иначе
             *  Мугон = МЕСЯЦ(Двозврата)-МЕСЯЦ(Дугона)-1
             */
            def diff1 = (returnDate.time.year * 12 + returnDate.time.month) - (stealingDate.time.year * 12 + stealingDate.time.month) - 1
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
        if (sRow.regDateEnd == null || sRow.regDateEnd.compareTo(reportPeriodEndDate.time) > 0) {
            removalDate = reportPeriodEndDate
        } else {
            removalDate.setTime(sRow.regDateEnd)
        }

        /**
         * Определяем Дпостановки
         * Если «графа 13»(источника)< «Дата начала периода», то
         *  Дпостановки = «Дата начала периода»
         * Иначе
         *  Дпостановки = «графа 13»(источника)
         */
        if (sRow.regDate.compareTo(reportPeriodStartDate.time) < 0) {
            deliveryDate = reportPeriodStartDate
        } else {
            deliveryDate.setTime(sRow.regDate)
        }

        /**
         * Определяем Мвлад
         * Мвлад = МЕСЯЦ[Дснятия] - МЕСЯЦ[Дпостановки]+1
         */
        ownMonths = (removalDate.get(Calendar.YEAR) * 12 + removalDate.get(Calendar.MONTH)) - (deliveryDate.get(Calendar.YEAR) * 12 + deliveryDate.get(Calendar.MONTH)) + 1
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