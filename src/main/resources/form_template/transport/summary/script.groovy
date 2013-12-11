package form_template.transport.summary

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

import java.text.SimpleDateFormat

import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.*

import groovy.transform.Field

/**
 * Расчет суммы налога по каждому транспортному средству
 * formTemplateId=200
 *
 * TODO:
 *      - при отсутствии строк форму можно принять
 *      - убрал редактирование
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
def nonEmptyColumns = [['okato', 'tsTypeCode', 'vi', 'model', 'regNumber', 'taxBase']]

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

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

/** Алгоритмы заполнения полей формы (9.1.1.8.1) Табл. 45. */
def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached

    deleteAllAliased(dataRows)
    dataRows.sort{ a,b ->
        def tempA = getRefBookValue(3, a.okato)?.OKATO?.stringValue
        def tempB = getRefBookValue(3, b.okato)?.OKATO?.stringValue
        if (tempA == tempB){
            tempA = getRefBookValue(42, a.tsTypeCode)?.CODE?.stringValue
            tempB = getRefBookValue(42, b.tsTypeCode)?.CODE?.stringValue
        }
        return tempA <=> tempB
    }

    def int monthCountInPeriod = getMonthCount()

    /** Уменьшающий процент. */
    def reducingPerc
    /** Пониженная ставка. */
    def loweringRates

    def i = 1
    dataRows.each { row ->
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // получение региона по ОКАТО
        def region = getRegionByOkatoOrg(row.okato, errorMsg)

        row.rowNumber = i++

        /*
         * Гафа 9 Единица измерения налоговой базы по ОКЕИ
         * Скрипт для выставления значения по умолчанию в столбец "Единица измерения налоговой базы по ОКЕИ",
         * если это значение не задано.
         */
        if (row.taxBaseOkeiUnit == null) {
            row.taxBaseOkeiUnit = getRecord(12, 'CODE', '251', index, getColumnName(row, 'taxBaseOkeiUnit'), new Date())?.record_id?.numberValue
        }

        /*
         * Графа 13 - Коэффициент Кв
         * Скрипт для вычисления автоматических полей 13, 19, 20
         */
        if (row.ownMonths != null) {
            row.coef362 = (row.ownMonths / monthCountInPeriod).setScale(4, BigDecimal.ROUND_HALF_UP)
        } else {
            row.coef362 = null
            placeError(row, 'coef362', ['ownMonths'])
        }

        /*
         * Графа 14 (Налоговая ставка)
         * Скрипт для вычисления налоговой ставки
         */
        row.taxRate = null
        def tsTypeCode
        if (row.tsTypeCode != null && row.years != null && row.taxBase != null) {
            tsTypeCode = getRefBookValue(42, row.tsTypeCode)?.CODE?.stringValue
            // запрос по выборке данных из справочника
            def query = " and ((MIN_POWER is null or MIN_POWER < " + row.taxBase + ") and (MAX_POWER is null or MAX_POWER > " + row.taxBase + "))" +
                    "and (UNIT_OF_POWER is null or UNIT_OF_POWER = " + row.taxBaseOkeiUnit + ")" +
                    "and ((MIN_AGE is null or MIN_AGE < " + row.years + ") and (MAX_AGE is null or MAX_AGE > " + row.years + "))";

            /**
             * Переберем варианты
             * 1. код = коду ТС && регион указан
             * 2. код = коду ТС && регион НЕ указан
             * 3. код = соответствует 2м двум символом кода ТС && регион указан
             * 4. код = соответствует 2м двум символом кода ТС && регион НЕ указан
             */

            def regionSqlPartID = " and DICT_REGION_ID = " + region.record_id
            def regionSqlPartNull = " and DICT_REGION_ID is null"

            // вариант 1
            def queryLikeStrictly = "CODE LIKE '" + tsTypeCode + "'" + query
            def finalQuery = queryLikeStrictly + regionSqlPartID
            def record = getRecord(41, finalQuery, new Date())
            // вариант 2
            if (record == null) {
                finalQuery = queryLikeStrictly + regionSqlPartNull
                record = getRecord(41, finalQuery, new Date())
            }

            def queryLike = "CODE LIKE '" + tsTypeCode.substring(0, 2) + "%'" + query
            // вариант 3
            if (record == null) {
                finalQuery = queryLike + regionSqlPartID
                record = getRecord(41, finalQuery, new Date())
            }
            // вариант 4
            if (record == null) {
                finalQuery = queryLike + regionSqlPartNull
                record = getRecord(41, finalQuery, new Date())
            }

            if (record != null) {
                row.taxRate = record.record_id.numberValue
            }
        } else {
            row.taxRate = null
            placeError(row, 'taxRate', ['tsTypeCode', 'years', 'taxBase'])
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
            placeError(row, 'calculatedTaxSum', ['taxBase', 'coef362', 'taxRate'])
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
                    def record = getRecord(7, query, new Date())

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
            placeError(row, 'taxSumToPay', ['calculatedTaxSum', 'benefitSum'])
        }
        if (tsTypeCode != null) {
            row.tsType = getRecord(42, 'CODE', tsTypeCode, index, getColumnName(row, 'tsType'), new Date())?.record_id?.numberValue
        }
    }
    // добавление строки ИТОГО
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.getCell("fix").setColSpan(2)
    totalRow.fix = 'ИТОГО'
    allColumns.each{
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

        //Проверки НСИ
        checkNSI(35, row, 'okato')
        checkNSI(42, row, 'tsTypeCode')
        checkNSI(12, row, 'taxBaseOkeiUnit')
        checkNSI(40, row, 'ecoClass')

        /**
         * Проверка наименования вида ТС коду вида ТС
         *
         * Значение «графы 4» (поле «Вид транспортного средства») совпадает со значение поля «Наименование вида транспортного средства» строки справочника «Коды видов транспортных средств»,  для которой
         * «графа 3» (поле «Код вида транспортного средства (ТС)») текущей строки формы = «графа 2» (поле  «Код вида ТС») строки справочника
         */
        def tsTypeCode = getRefBookValue(42, row.tsTypeCode)?.CODE?.stringValue
        def tsType = getRefBookValue(42, row.tsType)?.NAME?.stringValue
        if (row.tsType != null && row.tsTypeCode != null && (tsTypeCode == null || tsType == null || getRecord(42, "CODE like '" + tsTypeCode + "' and NAME LIKE '" + tsType + "'", new Date()) == null)) {
            logger.error(errorMsg + 'Название вида ТС не совпадает с Кодом вида ТС')
        }

        /**
         * Проверка льготы
         * Проверка осуществляется только для кодов 20210, 20220, 20230
         */
        if (row.taxBenefitCode != null && getRefBookValue(6, row.taxBenefitCode)?.CODE?.numberValue in [20210, 20220, 20230]) {
            def region = getRegionByOkatoOrg(row.okato, errorMsg)
            query = "TAX_BENEFIT_ID =" + row.taxBenefitCode + " AND DICT_REGION_ID = " + region.record_id
            if (getRecord(7, query, new Date()) == null) {
                logger.error(errorMsg + "Выбранная льгота для текущего региона не предусмотрена!")
            }
        }
    }
}

/**
 * Получение региона по коду ОКАТО
 */
def getRegionByOkatoOrg(def okatoCell, def errorMsg) {
    /*
    * первые две цифры проверяемого кода ОКАТО
    * совпадают со значением поля «Определяющая часть кода ОКАТО»
    * справочника «Коды субъектов Российской Федерации»
    */
    def okato = getRefBookValue(3, okatoCell)?.OKATO?.stringValue

    if (okato.substring(0, 4).equals("71140")) {
        return getRecord(4, 'CODE', '89', null, null, new Date());
    } else if (okato.substring(0, 4).equals("71100")) {
        return getRecord(4, 'CODE', '86', null, null, new Date());
    } else if (okato.substring(0, 3).equals("1110")) {
        return getRecord(4, 'CODE', '83', null, null, new Date());
    } else {
        def filter = "OKATO_DEFINITION like '" + okato.toString().substring(0, 2) + "%'"
        def record = getRecord(4, filter, new Date())
        if (record != null) {
            return record
        } else {
            logger.error(errorMsg + "Не удалось определить регион по коду ОКАТО")
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
    List<DataRow<Cell>> sourses202 = new ArrayList()
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
                    def contains = sourses202.find { el ->
                        el.codeOKATO.equals(sRow.codeOKATO) && el.identNumber.equals(sRow.identNumber) && el.regNumber.equals(sRow.regNumber)
                    }
                    if (contains != null) {
                        DataRow<Cell> row = contains
                        // если поля совпадают то ругаемся и убираем текущую совпавшую с коллекции
                        if (row.taxBenefitCode == sRow.taxBenefitCode &&
                                row.benefitStartDate.equals(sRow.benefitStartDate) &&
                                row.benefitEndDate.equals(sRow.benefitEndDate)) {
                            def department = departments.get(sourses202.indexOf(row))
                            logger.error("Обнаружены несколько разных строк, у которых совпадают Код ОКАТО = " + sRow.codeOKATO
                                    + ", Идентификационный номер = " + sRow.identNumber + ", Регистрационный знак=" + sRow.regNumber
                                    + " для форм «Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог» в подразделениях «"
                                    + sDepartment.name + "», «" + department.name + "». Строки : " + sRow.getIndex() + ", " + row.getIndex())
                            departments.remove(sourses202.indexOf(row))
                            sourses202.remove(sRow)
                        }
                    } else {
                        sourses202.add(sRow)
                        departments.add(sDepartment)
                    }
                }
            } else {
                def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
                def taxPeriod = reportPeriod.taxPeriod
                Calendar cl = Calendar.getInstance()
                cl.setTime(taxPeriod.startDate);
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
                    newRow.tsType = sRow.tsType
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

                    /**
                     * «Графа 11» Рассчитывается автоматически по формуле:
                     * Если («отчётный год YYYY» – «Графа 12» (формы-источника) – 1) <= 0
                     * То
                     * «Графа 11»  = 0
                     * Иначе
                     * «Графа 11»  = «отчётный год YYYY» – «Графа 12» (формы-источника) – 1
                     */

                    Calendar cl2 = Calendar.getInstance()
                    cl2.setTime(sRow.year);

                    def diff = cl.get(Calendar.YEAR) - cl2.get(Calendar.YEAR) - 1
                    if (diff <= 0) {
                        newRow.years = 0
                    } else {
                        newRow.years = diff
                    }

                    /*
                     * «Графа 12»
                     */

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
                        newRow.ownMonths = 0
                    } else { // иначе
                        //Определяем Мугон
                        /**
                         * Если «графа 15» (источника) не заполнена, то Мугон = 0
                         */
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
                                returnDate = Calendar.getInstance()
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
                        newRow.ownMonths = ownMonths - stealingMonths
                    }
                    dataRows.add(newRow)
                }
            }
        }
    }

    /**
     * Расставим соответствия для формы с 202
     */
    int cnt = 0
    sourses202.each { v ->
        cnt++
        // признак подстаноки текущей строки в сводную
        boolean use = false
        // пробежимся по форме расставим данные для текущей 202 строки
        dataRows.each { row ->
            // поиск
            if (v.codeOKATO.equals(row.okato)
                    && v.identNumber.equals(row.vi)
                    && v.regNumber.equals(row.regNumber)) {

                use = true
                row.taxBenefitCode = v.taxBenefitCode
                row.benefitStartDate = v.benefitStartDate
                row.benefitEndDate = v.benefitEndDate
            }
        }

        // если значения этой строки 202 формы не подставлялись в сводную то ругаемся
        if (!use) {
            def department = departments.get(sourses202.indexOf(v))
            logger.warn("Для строки " + cnt + " в форме \"Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог\" подразделения "
                    + department.name + " указана льгота для  транспортного средства, не указанного в одной из форм \"Сведения о транспортных средствах, по которым уплачивается транспортный налог\" . Код ОКАТО = " + v.codeOKATO
                    + ", Идентификационный номер = " + v.identNumber + ", Регистрационный знак=" + v.regNumber + "!")
        }
    }
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
    dataRowHelper.save(dataRows)
}

/** Число полных месяцев в текущем периоде (либо отчетном либо налоговом). */
def getMonthCount() {
    if (monthCountInPeriod == null) {
        def period = reportPeriodService.get(formData.reportPeriodId)
        if (period == null) {
            logger.error('Не найден отчетный период для налоговой формы.')
        } else {
            monthCountInPeriod = period.getMonths()
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
void placeError(DataRow row, String alias, ArrayList<String> errorFields) {
    def fields = []
    for(errAlias in errorFields){
        if (row[errAlias] == null) {
            fields.add("\"${getColumnName(row,errAlias)}\"")
        }
    }
    logger.error(errorMsg + "\"${getColumnName(row, alias)}\" не может быть вычислена, т.к. не заполнены поля: $fields.")
}