package form_template.property.property_945_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormToFormRelation
import com.aplana.sbrf.taxaccounting.model.Formats
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

/**
 * Данные бухгалтерского учета для расчета налога на имущество
 * formTemplateId = 610
 *
 * @author Bulat Kinzyabulatov
 */

// графа 1 - name           Наименование показателя
// графа 2 - taxBase1       Налоговая база (в руб. коп.). 60401, 60410, 60411
// графа 3 - taxBase2       Налоговая база (в руб. коп.). 60601
// графа 4 - taxBase3       Налоговая база (в руб. коп.). 60804
// графа 5 - taxBase4       Налоговая база (в руб. коп.). 60805
// графа 6 - taxBase5       Налоговая база (в руб. коп.). 91211
// графа 7 - taxBaseSum     Налоговая база (в руб. коп.). Сумма

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
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
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
    case FormDataEvent.IMPORT:
        checkRegionId()
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
    case FormDataEvent.GET_SOURCES:
        getSources()
        break
}

// Все атрибуты
@Field
def allColumns = ['name', 'taxBase1', 'taxBase2', 'taxBase3', 'taxBase4', 'taxBase5', 'taxBaseSum']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'taxBase1', 'taxBase2', 'taxBase3', 'taxBase4', 'taxBase5']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['taxBaseSum']

// Итоговые атрибуты
@Field
def totalColumns = ['taxBase1', 'taxBase2', 'taxBase3', 'taxBase4', 'taxBase5']

// Мапа для хранения полного названия подразделения (id подразделения  -> полное название)
@Field
def departmentFullNameMap = [:]

// Мапа для хранения подразделений (id подразделения  -> подразделение)
@Field
def departmentMap = [:]

// Мапа для хранения типов форм (id типа формы -> тип формы)
@Field
def formTypeMap = [:]

// Мапа для хранения переодичности форм источников-приемников (id типа формы + id периода -> периодичность ежемесячная или квартальная)
@Field
def monthlyMap = [:]

// Идентификатор типа формы приемника текущей формы
@Field
def destinationFormTypeId = 615

@Field
def patternMap = [
        /Стоимость имущества по субъекту Федерации \((.+)\)/                              : 'priceSubject',
        /Стоимость движимого имущества, отраженного на балансе до 01.01.2013 \((.+)\)/    : 'headPriceMovableOKTMO',
        /Стоимость недвижимого имущества по субъекту Федерации \((.+)\)/                  : 'headPriceUnmovableSubject',
        /Стоимость льготируемого имущества по субъекту Федерации \((.+)\)/                : 'priceBenefitSubject',
        /Имущество, подлежащее налогообложению/                                           : 'propertyTaxed',
        /в т\.ч\. стоимость недвижимого имущества по населенному пункту \"(.+)\" \((.+)\)/: 'priceUnmovableCityOKTMO',
        /Льготируемое имущество \(всего\)/                                                : 'headBenefitPropertyTotal',
        /Льготируемое имущество/                                                          : 'headBenefitProperty',
        /корректировка:/                                                                  : 'titleCorrection',
        /корректировка \(пообъектно\):/                                                   : 'titleCorrectionObject',
        /- (.+) \(пообъектно\):/                                                          : 'titleCategory',
        /корректировка (.+) \(пообъектно\):/                                              : 'titleCategoryCorrection',
        /итого по корректировкам/                                                         : 'totalCorrection',
        /ИТОГО с учетом корректировки/                                                    : 'totalUsingCorrection',
        /ИТОГО (.+) с учетом корректировки/                                               : 'totalCategoryUsingCorrection'
]

@Field
def titleMap = [
        'priceSubject' : 'Стоимость имущества по субъекту Федерации (<Код субъекта 1>)',
        'headPriceMovableOKTMO' : 'Стоимость движимого имущества, отраженного на балансе до 01.01.2013 (<Код ОКТМО 1>)',
        'headPriceUnmovableSubject' : 'Стоимость недвижимого имущества по субъекту Федерации (<Код субъекта 1>)',
        'priceBenefitSubject' : 'Стоимость льготируемого имущества по субъекту Федерации (<Код субъекта 1>)',
        'propertyTaxed' : 'Имущество, подлежащее налогообложению',
        'priceUnmovableCityOKTMO' : 'в т.ч. стоимость недвижимого имущества по населенному пункту "1" (<Код ОКТМО 1>)',
        'headBenefitPropertyTotal' : 'Льготируемое имущество (всего)',
        'headBenefitProperty' : 'Льготируемое имущество',
        'titleCorrection' : 'корректировка:',
        'titleCorrectionObject' : 'корректировка (пообъектно):',
        'titleCategory' : '- <Категория 1 имущества> (пообъектно):',
        'titleCategoryCorrection' : 'корректировка <Категория 1 имущества>  (пообъектно):',
        'totalCorrection' : 'итого по корректировкам',
        'totalUsingCorrection' : 'ИТОГО с учетом корректировки',
        'totalCategoryUsingCorrection' : 'ИТОГО <Категория 1 имущества>  с учетом корректировки'
]

@Field
def Map<String, Integer> aliasNums = ['priceSubject' : 1, // №1 строка 1 - может дублироваться вместе со всеми последующими
                 'headPriceMovableOKTMO' : 2, // №1.1 строка 2
                 'titleCorrectionObject1' : 3, // строка 3
                 'totalCorrection1' : 6, // строка 6
                 'totalUsingCorrection1' : 7,   // строка 7
                 'headPriceUnmovableSubject' : 8, // №1.2(1.2.1) строка 8
                 'totalCorrection2' : 9, // строка 9
                 'totalUsingCorrection2' : 10, // строка 10
                 'priceBenefitSubject' : 11, // строка 11
                 'priceUnmovableCityOKTMO' : 12, //№ 1.2.2(1.2.2.1) строка 12 - может дублироваться вместе со всеми последующими
                 'propertyTaxed' : 13, // строка 13
                 'titleCorrectionObject2' :14, // строка 14
                 'totalCorrection3' : 17, // строка 17
                 'totalUsingCorrection3' : 18, // № 1.2.2.2 строка 18
                 'headBenefitPropertyTotal' : 19, // строка 19 - последующие могут дублироваться
                 'titleCategory' : 20, // строка 20
                 'titleCategoryCorrection' : 23, // строка 23
                 'totalCorrection' : 26, // строка 26
                 'totalCategoryUsingCorrection' : 27, // строка 27
                 'headBenefitProperty' : 19_1, // строка 20 если всё имущество подразделения льготируемое
                 'totalCorrection4' : 20_1, // строка 21 если всё имущество подразделения льготируемое
                 'totalUsingCorrection4' : 21_1 // строка 22 если всё имущество подразделения льготируемое
]

@Field
def endDate = null

@Field
def isBalancePeriod

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

class Category {
    def Integer index20;
    def List<Integer> index21 = new ArrayList<Integer>();
    def Integer index23;
    def List<Integer> index24 = new ArrayList<Integer>();
    def Integer index26;
    def Integer index27;

    @Override
    public String toString() {
        return "Category{" +
                "index20=" + index20 +
                ", index21=" + index21 +
                ", index23=" + index23 +
                ", index24=" + index24 +
                ", index26=" + index26 +
                ", index27=" + index27 +
                '}';
    }
}

class OKTMO {
    def Integer index12;
    def Integer index13;
    def Integer index14;
    def Integer index15;
    def Integer index17;
    def Integer index18;
    def Integer index19;
    def Integer index19_1;
    def Integer index20_1;
    def Integer index21_1;
    def List<Category> categories = new ArrayList<Category>();

    @Override
    public String toString() {
        return "OKTMO{" +
                "index12=" + index12 +
                ", index13=" + index13 +
                ", index14=" + index14 +
                ", index15=" + index15 +
                ", index17=" + index17 +
                ", index18=" + index18 +
                ", index19=" + index19 +
                ", index19_1=" + index19_1 +
                ", index20_1=" + index20_1 +
                ", index21_1=" + index21_1 +
                ", categories=" + categories +
                '}';
    }
}

class Subject {
    def Integer index1;
    def Integer index2;
    def Integer index3;
    def List<Integer> index4 = new ArrayList<Integer>();
    def Integer index6;
    def Integer index7;
    def Integer index8;
    def Integer index9;
    def Integer index10;
    def Integer index11;
    def List<OKTMO> oktmos = new ArrayList<OKTMO>();

    @Override
    public String toString() {
        return "Subject{" +
                "index1=" + index1 +
                ", index2=" + index2 +
                ", index3=" + index3 +
                ", index4=" + index4 +
                ", index6=" + index6 +
                ", index7=" + index7 +
                ", index8=" + index8 +
                ", index9=" + index9 +
                ", index10=" + index10 +
                ", index11=" + index11 +
                ", oktmos=" + oktmos +
                '}';
    }
}

def String getTitleAlias(def row) {
    def key = getTitlePattern(row)
    key ? patternMap[key] : null
}

def getTitlePattern(def row) {
    for (def key : patternMap.keySet()){
        if (row.name?.toLowerCase() ==~ key.toLowerCase()) {
            return key
        }
    }
    return null
}

def getRefBookRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName) {
    def filter = "$alias = '$value'"
    def records = refBookFactory.getDataProvider(refBookId).getRecords(getReportPeriodEndDate(), null, filter, null)
    def RefBook refBook = refBookFactory.get(refBookId)
    def refBookAttribute = refBook.attributes.find{ it.alias == alias}
    if (records.size() == 0) {
        loggerError(null, "Строка $rowIndex: Графа «$cellName» содержит значение «$value» параметра «${refBookAttribute.name}», отсутствующее в справочнике «${refBook.name}»!")
    } else {
        return records.get(0)
    }
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    calcCheckSubjects(dataRows, true)
    dataRows.each { row ->
        row.taxBaseSum = (row.taxBase1?:0) - (row.taxBase2?:0) + (row.taxBase3?:0) - (row.taxBase4?:0) - (row.taxBase5?:0)
    }
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def subject
    def subjectId = null
    def oktmo = null
    def propertyCategory = null

    def String expectedOKTMO = null
    def expectedOKTMOIndex = null
    def actualOKTMOList = new ArrayList<String>()

    dataRows.each { row ->
        def index = row.getIndex()
        def errorMsg = "Строка $index: "
        def titleAlias = getTitleAlias(row)

        // заполнение полей
        checkNonEmptyColumns(row, index, allColumns, logger, true)

        // Проверка значений в «Графе 2» и «Графе 3»
        if (row.taxBase1 < row.taxBase2) {
            loggerError(row, errorMsg + "Графа «${getColumnName(row, 'taxBase2')}» не может быть больше «${getColumnName(row, 'taxBase1')}»!")
        }

        // Проверка значений в «Графе 4» и «Графе 5»
        if (row.taxBase3 < row.taxBase4) {
            loggerError(row, errorMsg + "Графа «${getColumnName(row, 'taxBase4')}» не может быть больше «${getColumnName(row, 'taxBase3')}»!")

        }

        if (titleAlias == getTitle(1)) {
            // если новый субъект(после сущ-го), то проверяем ОКТМО в 2 и 12-х строках
            if (subjectId && expectedOKTMOIndex) {
                if (!actualOKTMOList.contains(expectedOKTMO)) {
                    loggerError(null, "Строка $expectedOKTMOIndex: " + "Строки вида «${getEmptyPattern(getTitle(2))}», «${getEmptyPattern(getTitle(12))}» данной группы строк содержат разные значения параметров «Код ОКТМО»!")
                }
                actualOKTMOList.clear()
                expectedOKTMO = null
                expectedOKTMOIndex = null
            }
            // вытаскиваем субъект из строки вида 1
            subject = extractValue(row, 1)
            if (subject) {
                subjectId = getRefBookRecord(4L, 'CODE', subject, row.getIndex(),  getColumnName(row,'name'))?.recordId
            }
        }
        if (titleAlias == getTitle(2)) {
            expectedOKTMOIndex = row.getIndex()
            expectedOKTMO = extractValue(row, 1)
            getRefBookRecord(96L, 'CODE', expectedOKTMO, row.getIndex(),  getColumnName(row,'name'))?.recordId
        }
        if (titleAlias in [getTitle(8), getTitle(11)]) {
            // вытаскиваем субъект из строки вида 8(или 11)
            tempSubject = extractValue(row, 1)
            if (tempSubject != subject) {
                loggerError(row, errorMsg + "Строки вида «${getEmptyPattern(getTitle(1))}», «${getEmptyPattern(getTitle(8))}», «${getEmptyPattern(getTitle(11))}» данной группы строк содержат разные значения параметров «Код субъекта»!")
            }
        }
        if (titleAlias == getTitle(12)) {
            // вытаскиваем октмо из строки вида 12
            oktmo = extractValue(row, 2)
            actualOKTMOList.add(oktmo)
            if (oktmo) {
                oktmoId = getRefBookRecord(96L, 'CODE', oktmo, row.getIndex(),  getColumnName(row,'name'))?.recordId
            }
        }
        if (titleAlias == getTitle(20)) {
            // вытаскиваем категорию имущества из строки вида 20
            propertyCategory = extractValue(row, 1)
        }
        if (titleAlias in [getTitle(23), getTitle(27)]) {
            // вытаскиваем категорию из строки вида 23, 27
            tempCategory = extractValue(row, 1)
            if (tempCategory != propertyCategory) {
                loggerError(row, errorMsg + "Строки вида «${getEmptyPattern(getTitle(20))}», «${getEmptyPattern(getTitle(23))}», «${getEmptyPattern(getTitle(27))}» данной группы строк содержат разные значения параметров «Категория имущества»!")
            }
        }

        // Проверка существования параметров налоговых льгот для категорий имущества субъекта
        if (titleAlias == getTitle(27)) {
            if (subjectId != null && propertyCategory != null){
                String filter = "DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() + " and REGION_ID = " + subjectId.toString() + " and LOWER(ASSETS_CATEGORY) = '" + propertyCategory + "' and PARAM_DESTINATION = 1"
                def records = refBookFactory.getDataProvider(203).getRecords(getReportPeriodEndDate(), null, filter, null)
                if (records.size() == 0) {
                    loggerError(row, errorMsg + "Для текущего субъекта и категории имущества не предусмотрена налоговая льгота (в справочнике «Параметры налоговых льгот налога на имущество» отсутствует необходимая запись)!")
                }
            }
        }
        // Проверка существования параметров налоговых льгот по всему имуществу субъекта
        if (titleAlias == getTitle(21_1)) {
            boolean isZero = false
            for (def column : (allColumns - 'name')) {
                if (row[column]) {
                    isZero = true
                    break
                }
            }
            if (!isZero) {
                String filter = "DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() + " and REGION_ID = " + subjectId.toString() + " and LOWER(ASSETS_CATEGORY) = '" + propertyCategory + "' and PARAM_DESTINATION = 0"
                def records = refBookFactory.getDataProvider(203).getRecords(getReportPeriodEndDate(), null, filter, null)
                if (records.size() == 0) {
                    loggerError(row, errorMsg + "Для текущего субъекта не предусмотрена налоговая льгота (в справочнике «Параметры налоговых льгот налога на имущество» отсутствует необходимая запись)!")
                }
            }
        }
        // Проверка существования параметров декларации для субъекта-ОКТМО
        if (titleAlias == getTitle(12)) {
            if (subjectId != null && oktmoId != null) {
                String filter = "DECLARATION_REGION_ID = " + formDataDepartment.regionId?.toString() + " and REGION_ID = " + subjectId.toString() + " and OKTMO = " + oktmoId.toString()
                def records = refBookFactory.getDataProvider(200).getRecords(getReportPeriodEndDate(), null, filter, null)
                if (records.size() == 0) {
                    loggerError(row, errorMsg + "Текущие параметры представления декларации (Код субъекта, Код ОКТМО) не предусмотрены (в справочнике «Параметры представления деклараций по налогу на имущество» отсутствует такая запись)!")
                }
            }
        }
        // Проверка итоговых значений Графы 7
        if (row.taxBaseSum != null && row.taxBase1 != null && row.taxBase2 != null && row.taxBase3 != null && row.taxBase4 != null && row.taxBase5 != null &&
                row.taxBaseSum != row.taxBase1 - row.taxBase2 + row.taxBase3 - row.taxBase4 - row.taxBase5) {
            loggerError(row, errorMsg + "Итоговые значения рассчитаны неверно в графе «${getColumnName(row, 'taxBaseSum')}»!")
        }
    }

    if (expectedOKTMO && !actualOKTMOList.contains(expectedOKTMO)) {
        loggerError(null, "Строка $expectedOKTMOIndex: " + "Строки вида «${getEmptyPattern(getTitle(2))}», «${getEmptyPattern(getTitle(12))}» данной группы строк содержат разные значения параметров «Код ОКТМО»!")
    }

    calcCheckSubjects(dataRows, false)
}

/**
 *
 * @param dataRows строки НФ
 * @param indexResult индекс строки куда записывать/сравнивать итог расчета
 * @param indexBegin индекс начала диапазона строк для суммирования
 * @param indexEnd индекс конца диапазона строк для суммирования
 * @param isCalc флаг расчет/проверка
 * @param compare флаг сравнения значений граничных строк (в случае пустого диапазона)
 */
void calcCheckSumBetween(def dataRows, def indexResult, def indexBegin, def indexEnd, boolean isCalc, boolean compare) {
    if (indexBegin < indexEnd - 1){
        totalColumns.each { column ->
            sum = dataRows[(indexBegin)..(indexEnd - 2)].sum { it[column] }
            def row = dataRows[indexResult - 1]
            calcCheck(row, column, sum, isCalc)
        }
    } else if (compare){
        def errorMsg = "Строки $indexBegin, $indexEnd: "
        totalColumns.each { column ->
            def rowBegin = dataRows[indexBegin - 1]
            def rowEnd = dataRows[indexEnd - 1]
            if (rowBegin[column] != rowEnd[column]) {
                loggerError(null, errorMsg + "Итоговые значения заполнены неверно в графе «${getColumnName(rowBegin, column)}»!")
            }
        }
    }
}

void calcCheckSum(def dataRows, def indexResult, def indexSum1, def indexSum2, boolean isCalc) {
    totalColumns.each { column ->
        def sum = dataRows[indexSum1 - 1][column]?:0 + dataRows[indexSum2 - 1][column]?:0
        def row = dataRows[indexResult - 1]
        calcCheck(row, column, sum, isCalc)
    }
}

void calcCheckSumList(def dataRows, def indexResult, def indexList, boolean isCalc) {
    indexList.removeAll{
        it == null
    }
    if (!indexList || indexList.isEmpty()) {
        return
    }
    totalColumns.each { column ->
        def sum = indexList.sum { index ->
            dataRows[index - 1][column]
        }
        def row = dataRows[indexResult - 1]
        calcCheck(row, column, sum, isCalc)
    }
}

void calcCheck(def row, def column, def sum, boolean isCalc) {
    if (isCalc) {
        row[column] = sum
    } else if (row[column] != sum) {
        def errorMsg = "Строка ${row.getIndex()}: "
        loggerError(row, errorMsg + "Итоговые значения рассчитаны неверно в графе «${getColumnName(row, column)}»!")
    }
}

void calcCheckSubjects(def dataRows, boolean isCalc) {
    def List<Subject> subjects = new ArrayList<Subject>()
    def titles = aliasNums.keySet().asList()
    def String aliasRoot = titles[0] // идем с корня
    def List<String> validAliasList = Arrays.asList(aliasRoot)
    def Integer rowTypeIndex = 0
    def Subject currentSubject = null
    def OKTMO currentOKTMO = null
    def Category currentCategory = null
    def currentRow = null
    boolean isValidEnd = false
    // проходим по строкам НФ
    for (def i = 0; i < dataRows.size(); i++) {
        // берем строку
        isValidEnd = false
        def row = dataRows[i]
        // алиас текущей строки, может повторяться
        def titleAlias = getTitleAlias(row)
        // случай повтора строк типа 1 или 12
        if (rowTypeIndex == -1) {
            if (titleAlias) {
                rowTypeIndex = titles.indexOf(titleAlias)
            }
        }
        currentRow = row
        // если строка невалидна, то ошибка
        if (!isValidRow(titleAlias, validAliasList)) {
            break
        } else {
            // новый субъект
            switch (aliasNums[titles[rowTypeIndex]]){
                case 1: currentSubject = new Subject()
                    currentSubject.index1 = row.getIndex()
                    subjects.add(currentSubject)
                    currentOKTMO = null
                    currentCategory = null
                    break
                case 2: currentSubject.index2 = row.getIndex()
                    break
                case 3: if (titleAlias != null) {
                    currentSubject.index3 = row.getIndex()
                } else {
                    currentSubject.index4.add(row.getIndex())
                }
                    break
                case 6: currentSubject.index6 = row.getIndex()
                    break
                case 7: currentSubject.index7 = row.getIndex()
                    break
                case 8: currentSubject.index8 = row.getIndex()
                    break
                case 9: currentSubject.index9 = row.getIndex()
                    break
                case 10: currentSubject.index10 = row.getIndex()
                    break
                case 11: currentSubject.index11 = row.getIndex()
                    break
                case 12: currentOKTMO = new OKTMO()
                    currentOKTMO.index12 = row.getIndex()
                    currentSubject.oktmos.add(currentOKTMO)
                    currentCategory = null
                    break
                case 13: currentOKTMO.index13 = row.getIndex()
                    break
                case 14: if (titleAlias != null) {
                    currentOKTMO.index14 = row.getIndex()
                } else {
                    currentOKTMO.index15.add(row.getIndex())
                }
                    break
                case 17: currentOKTMO.index17 = row.getIndex()
                    break
                case 18: currentOKTMO.index18 = row.getIndex()
                    break
                case 19: if (titleAlias == getTitle(19)) {
                    currentOKTMO.index19 = row.getIndex()
                } else if (titleAlias == getTitle(19_1)) {
                    currentOKTMO.index19_1 = row.getIndex()
                    rowTypeIndex = titles.indexOf(getTitle(19_1))
                }
                    break
                case 20: if (titleAlias != null) {
                    currentCategory = new Category()
                    currentCategory.index20 = row.getIndex()
                    currentOKTMO.categories.add(currentCategory)
                } else {
                    currentCategory.index21.add(row.getIndex())
                }
                    break
                case 23: if (titleAlias != null) {
                    currentCategory.index23 = row.getIndex()
                } else {
                    currentCategory.index24.add(row.getIndex())
                }
                    break
                case 26: currentCategory.index26 = row.getIndex()
                    break
                case 27: currentCategory.index27 = row.getIndex()
                    break
                case 20_1: currentOKTMO.index20_1 = row.getIndex()
                    break
                case 21_1: currentOKTMO.index21_1 = row.getIndex()
                    break
                default: errorExpected(currentRow,  validAliasList)
            }
            // иначе получаем список возможных следующих псевдонимов
            def temp = getNextAliasRowTypeIndex(rowTypeIndex, titleAlias)
            rowTypeIndex = temp.rowTypeIndex
            validAliasList = temp.nextValidAliasList
            isValidEnd = temp.isValidEnd
        }
    }
    if (!isValidEnd) {
        if (!isCalc) {
            errorExpected(currentRow,  validAliasList)
        }
    } else {
        isCalc ? calcTotals(subjects, dataRows) : checkTotals(subjects, dataRows)
    }
}

void errorExpected(def row, def validAliasList) {
    def expectedAliases = validAliasList.collect { alias ->
        getEmptyPattern(alias)
    }.join('» или «')
    loggerError(row, row ?
            "Строка ${row.getIndex()}: Ожидается строка вида «${expectedAliases}»!" :
            "Ожидается строка вида «${expectedAliases}»!")
}

def String getEmptyPattern(def alias) {
    if (alias != null) {
        titleMap[titleMap.find {key, value ->
            // ищем по псевдониму или обрезаем циферки в конце
            alias == key || (alias - key) in ['1', '2', '3', '4']
        }.key]
    } else {
        '<Объект>'
    }
}

// рассчитываем итоги в строках
void calcTotals(def subjects, def dataRows) {
    for (def Subject subject in subjects) {
        for (def OKTMO oktmo in subject.oktmos) {
            for (def Category category in oktmo.categories) {
                // строка 23(26) сумма строк между ними
                calcCheckSumBetween(dataRows, category.index23, category.index23, category.index26, true, false)
                calcCheckSumBetween(dataRows, category.index26, category.index23, category.index26, true, false)
                // строка 20 сумма строк между 20 и 23
                calcCheckSumBetween(dataRows, category.index20, category.index20, category.index23, true, false)
                // строка 27 сумма строк 20 и 26
                calcCheckSum(dataRows, category.index27, category.index20, category.index26, true)
            }
            // строка 19 сумма строк 27 (или 21 есть сумма 19 и 20 для второго случая)
            if (oktmo.index19) {
                calcCheckSumList(dataRows, oktmo.index19, oktmo.categories.collect { it.index27 }, true)
            } else {
                calcCheckSum(dataRows, oktmo.index21_1, oktmo.index19_1, oktmo.index20_1, true)
            }
            // строка 14(17) сумма строк между ними
            calcCheckSumBetween(dataRows, oktmo.index14, oktmo.index14, oktmo.index17, true, false)
            calcCheckSumBetween(dataRows, oktmo.index17, oktmo.index14, oktmo.index17, true, false)
            // строка 18 сумма строк 13 и 17
            calcCheckSum(dataRows, oktmo.index18, oktmo.index13, oktmo.index17, true)
            // строка 12 сумма строк 18 и 19(или 21 для второго случая)
            calcCheckSum(dataRows, oktmo.index12, oktmo.index18, oktmo.index19 ?: oktmo.index21_1, true)
        }
        // строка 11 сумма строк 19 (или 21 для второго случая).
        calcCheckSumList(dataRows, subject.index11, subject.oktmos.collect { it.index19 ?: it.index21_1}, true)
        // строка 3(6) = сумма строк между 3 и 6
        calcCheckSumBetween(dataRows, subject.index3, subject.index3, subject.index6, true, false)
        calcCheckSumBetween(dataRows, subject.index6, subject.index3, subject.index6, true, false)
        // строка 8 = сумма строк 12
        calcCheckSumList(dataRows, subject.index8, subject.oktmos.collect { it.index12 }, true)
        // строка 9 = сумма строк 17
        calcCheckSumList(dataRows, subject.index9, subject.oktmos.collect { it.index17 }, true)
        // строка 10 = сумма строк 8 и 9
        calcCheckSum(dataRows, subject.index10, subject.index8, subject.index9, true)
        // строка 7 = сумма строк 2 и 6
        calcCheckSum(dataRows, subject.index7, subject.index2, subject.index6, true)
        // строка 1 = сумма строк 2 и 8
        calcCheckSum(dataRows, subject.index1, subject.index2, subject.index8, true)
    }
}

// проверяем расчет в строках
void checkTotals(def subjects, def dataRows) {
    for (def Subject subject in subjects) {
        // строка 1 = сумма строк 2 и 8
        calcCheckSum(dataRows, subject.index1, subject.index2, subject.index8, false)
        // строка 3(6) = сумма строк между 3 и 6
        calcCheckSumBetween(dataRows, subject.index3, subject.index3, subject.index6, false, true)
        calcCheckSumBetween(dataRows, subject.index6, subject.index3, subject.index6, false, false)
        // строка 7 = сумма строк 2 и 6
        calcCheckSum(dataRows, subject.index7, subject.index2, subject.index6, false)
        // строка 8 = сумма строк 12
        calcCheckSumList(dataRows, subject.index8, subject.oktmos.collect { it.index12 }, false)
        // строка 9 = сумма строк 17
        calcCheckSumList(dataRows, subject.index9, subject.oktmos.collect { it.index17 }, false)
        // строка 10 = сумма строк 8 и 9
        calcCheckSum(dataRows, subject.index10, subject.index8, subject.index9, false)
        // строка 11 сумма строк 19
        calcCheckSumList(dataRows, subject.index11, subject.oktmos.collect { it.index19 ?: it.index21_1 }, false)
        for (def OKTMO oktmo in subject.oktmos) {
            // строка 12 сумма строк 18 и 19
            calcCheckSum(dataRows, oktmo.index12, oktmo.index18, oktmo.index19 ?: oktmo.index21_1, false)
            // строка 14(17) сумма строк между ними
            calcCheckSumBetween(dataRows, oktmo.index14, oktmo.index14, oktmo.index17, false, true)
            calcCheckSumBetween(dataRows, oktmo.index17, oktmo.index14, oktmo.index17, false, false)
            // строка 18 сумма строк 13 и 17
            calcCheckSum(dataRows, oktmo.index18, oktmo.index13, oktmo.index17, false)
            // строка 19 сумма строк 27 (или 21 есть сумма 19 и 20 для второго случая)
            if (oktmo.index19) {
                calcCheckSumList(dataRows, oktmo.index19, oktmo.categories.collect { it.index27 }, false)
            } else {
                calcCheckSum(dataRows, oktmo.index21_1, oktmo.index19_1, oktmo.index20_1, false)
            }
            for (def Category category in oktmo.categories) {
                // строка 20 сумма строк между 20 и 23
                calcCheckSumBetween(dataRows, category.index20, category.index20, category.index23, false, false)
                // строка 23(26) сумма строк между ними
                calcCheckSumBetween(dataRows, category.index23, category.index23, category.index26, false, true)
                calcCheckSumBetween(dataRows, category.index26, category.index23, category.index26, false, false)
                // строка 27 сумма строк 20 и 26
                calcCheckSum(dataRows, category.index27, category.index20, category.index26, false)
            }
        }
    }
}

def isValidRow(def titleAlias, def aliasList) {
    // если строка не соответствует ожиданиям, то ошибка
    def isValid = aliasList.contains(titleAlias)
    if (!isValid){
        for (it in aliasList) {
            if (it && titleAlias) {
                isValid |= it.contains(titleAlias)
            }
        }
    }
    return isValid
}

/**
 * Получаем список возможных алиасов после текущей строки и индекс последнего использованного алиаса из aliasNums
 * @param rowTypeIndex индекс строки в titles
 * @param currentAlias алиас текущей строки(может повторяться)
 * @return
 */
def getNextAliasRowTypeIndex(def Integer rowTypeIndex, def currentAlias) {
    def titles = aliasNums.keySet().asList()
    def List<String> nextValidAliasList = []
    def isValidEnd = false
    // если дошли до строки типа 27, то или на 1 строку или на 12-ую или на 20-ую (по типу)
    if (rowTypeIndex == titles.indexOf(getTitle(27))) {
        rowTypeIndex = -1
        isValidEnd = true
        nextValidAliasList.add(getTitle(1))
        nextValidAliasList.add(getTitle(12))
        nextValidAliasList.add(getTitle(20))
    } else if (rowTypeIndex == titles.indexOf(getTitle(21_1))) {
        rowTypeIndex = -1
        isValidEnd = true
        nextValidAliasList.add(getTitle(1))
    } else if (rowTypeIndex == titles.indexOf(getTitle(18))) {
        rowTypeIndex++
        nextValidAliasList.add(getTitle(19))
        nextValidAliasList.add(getTitle(19_1))
    } else {
        // после корректировки увеличиваем счетчик если строка с алиасом
        if (currentAlias && (titles[rowTypeIndex].contains('title') || titles[rowTypeIndex].contains('Title'))) {
            rowTypeIndex++
        }
        // при корректировке не увеличиваем счетчик
        if (!currentAlias || (currentAlias.contains('title') || currentAlias.contains('Title'))) {
            nextValidAliasList.add(null)
            nextValidAliasList.add(titles[rowTypeIndex])
        } else {
            rowTypeIndex++
            nextValidAliasList.add(titles[rowTypeIndex])
        }
    }

    return ['rowTypeIndex' : rowTypeIndex, 'isValidEnd': isValidEnd, 'nextValidAliasList' : nextValidAliasList]
}

// получить уникальный алиас строки по типу
def String getTitle(def int typeNum) {
    if (!(typeNum in ((1..3) + (6..14) + (17..20) + [23, 26, 27] + [19_1, 20_1, 21_1]))) {
        return null
    }
    return aliasNums.find { key, value -> value == typeNum }.key
}

void importData() {
    def tempRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tempRow, 'name'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 7, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tempRow, 'name'),
            (xml.row[0].cell[1]): 'Налоговая база (в руб. коп.)',
            (xml.row[1].cell[1]): '60401, 60410, 60411',
            (xml.row[1].cell[2]): '60601',
            (xml.row[1].cell[3]): '60804',
            (xml.row[1].cell[4]): '60805',
            (xml.row[1].cell[5]): '91211',
            (xml.row[1].cell[6]): 'Сумма'
    ]
    (1..7).each { index ->
        headerMapping.put((xml.row[2].cell[index - 1]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        /* Пропуск строк шапок */
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 1
        newRow.name = row.cell[0].text()
        // графа 2
        newRow.taxBase1 = parseNumber(row.cell[1].text(), xlsIndexRow, 1 + colOffset, logger, true)
        // графа 3
        newRow.taxBase2 = parseNumber(row.cell[2].text(), xlsIndexRow, 2 + colOffset, logger, true)
        // графа 4
        newRow.taxBase3 = parseNumber(row.cell[3].text(), xlsIndexRow, 3 + colOffset, logger, true)
        // графа 5
        newRow.taxBase4 = parseNumber(row.cell[4].text(), xlsIndexRow, 4 + colOffset, logger, true)
        // графа 6
        newRow.taxBase5 = parseNumber(row.cell[5].text(), xlsIndexRow, 5 + colOffset, logger, true)
        // графа 7
        newRow.taxBaseSum = parseNumber(row.cell[6].text(), xlsIndexRow, 6 + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 7, 0)

    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)

        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 1
        newRow.name = row.cell[1].text()
        // графа 2
        newRow.taxBase1 = parseNumber(row.cell[2].text(), rnuIndexRow, 2 + colOffset, logger, true)
        // графа 3
        newRow.taxBase2 = parseNumber(row.cell[3].text(), rnuIndexRow, 3 + colOffset, logger, true)
        // графа 4
        newRow.taxBase3 = parseNumber(row.cell[4].text(), rnuIndexRow, 4 + colOffset, logger, true)
        // графа 5
        newRow.taxBase4 = parseNumber(row.cell[5].text(), rnuIndexRow, 5 + colOffset, logger, true)
        // графа 6
        newRow.taxBase5 = parseNumber(row.cell[6].text(), rnuIndexRow, 6 + colOffset, logger, true)
        // графа 7
        newRow.taxBaseSum = parseNumber(row.cell[7].text(), rnuIndexRow, 7 + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

def extractValue(Object row, int count) {
    return row.name.toLowerCase().replaceAll(getTitlePattern(row).toLowerCase(), "\$$count")
}

// Проверка заполнения атрибута «Регион» подразделения текущей формы (справочник «Подразделения»)
void checkRegionId() {
    if (formDataDepartment.regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }
}

/** Получить результат для события FormDataEvent.GET_SOURCES. */
def getSources() {
    def reportPeriods = []
    def monthOrder = formData.periodOrder
    def monthInQuarter = 3
    def needPrevPeriod = monthOrder % monthInQuarter == 1
    // для месяцев не являющиеся первыми месяцами кварталов использовать все приемники
    def useAll = !needPrevPeriod
    def hasPrevPeriod = true
    // ожидаемый номер предыдущего периода
    def expectedPrevPeriodOrder = null
    // ожидаемый год предыдущего периода
    def expectedPrevPeriodYear = null

    // текущий период используется всегда
    def currentPeriod = reportPeriodService.get(formData.reportPeriodId)
    reportPeriods.add(currentPeriod)

    // получить предыдущий период
    // если первый месяц квартала (order = 1, 4, 7, 10) (так как для 945.1 период смещен на один месяц вперед, то это последний месяц в его смещенных периодах)
    // тогда надо получить приемник для предыдущего месяца (3, 6, 9) (предыдущей месяц - будет последний месяц в нормальных кварталах)
    if (needPrevPeriod) {
        // получить предыдущий период
        def prevReportPeriod = reportPeriodService.getPrevReportPeriod(currentPeriod.id)
        if (prevReportPeriod != null) {
            // ожидаемый номер предыдущего периода
            expectedPrevPeriodOrder = (currentPeriod.order == 1 ? 4 : currentPeriod.order - 1)
            // ожидаемый год предыдущего периода
            expectedPrevPeriodYear = (currentPeriod.order == 1 ? currentPeriod.taxPeriod.year - 1 : currentPeriod.order)
            if (prevReportPeriod.order == expectedPrevPeriodOrder && expectedPrevPeriodYear == prevReportPeriod.taxPeriod.year) {
                reportPeriods.add(prevReportPeriod)
            } else {
                // номер в налоговом периоде или год предыдущего периода отличается от ожидаемого (т.е. отсутстует ожидаемый предыдущий период)
                hasPrevPeriod = false
            }
        }
    }

    // мапа с периодами и приемниками (период -> список приемников)
    def periodDestinationMap = [:]
    // мапа с периодами и источниками (период -> список источников)
    def periodSourceMap = [:]
    // приемники отсутствующего предыдущего периода
    def missingDestinationDepartmentFormTypes = []
    reportPeriods.each { reportPeriod ->
        def start = reportPeriodService.getCalendarStartDate(reportPeriod.id).time
        def end = reportPeriodService.getEndDate(reportPeriod.id).time

        // приемники
        def formDestinations = []
        def destinationDepartmentFormTypes = departmentFormTypeService.getFormDestinations(formDataDepartment.id,
                formData.formType.id, formData.kind, start, end)
        if (destinationDepartmentFormTypes != null && !destinationDepartmentFormTypes.isEmpty()) {
            if (useAll) {
                // если это не первые месяцы кварталов то использовать все приемники
                formDestinations.addAll(destinationDepartmentFormTypes)
            } else {
                for (def destinationDepartmentFormType : destinationDepartmentFormTypes) {
                    if (reportPeriod != currentPeriod && destinationDepartmentFormType.formTypeId == destinationFormTypeId) {
                        // если это не текущий период, то использовать только приемники 945.5
                        formDestinations.add(destinationDepartmentFormType)
                    } else if (reportPeriod == currentPeriod && destinationDepartmentFormType.formTypeId == destinationFormTypeId && monthOrder == 1) {
                        // если это текущий период и январь и приемник 945.5, то использовать приемник
                        formDestinations.add(destinationDepartmentFormType)
                    } else if (reportPeriod == currentPeriod && destinationDepartmentFormType.formTypeId != destinationFormTypeId) {
                        // если это текущий период и не 945.5), то использовать приемник
                        formDestinations.add(destinationDepartmentFormType)
                    }
                    // если нужно было получить предыдущий период, но его не существует, то надо запонить приемники 945.5 текущего периода
                    if (needPrevPeriod && !hasPrevPeriod && reportPeriod == currentPeriod && destinationDepartmentFormType.formTypeId == destinationFormTypeId) {
                        missingDestinationDepartmentFormTypes.add(destinationDepartmentFormType)
                    }
                }
            }
        }
        periodDestinationMap[reportPeriod] = formDestinations

        // источники
        if (reportPeriod == currentPeriod) {
            periodSourceMap[reportPeriod] = departmentFormTypeService.getFormSources(formDataDepartment.id,
                    formData.formType.id, formData.kind, start, end)
        }
    }

    // проходим по периодам источников и приемников
    addToResult(sources.sourceList, periodDestinationMap, false)
    addToResult(sources.sourceList, periodSourceMap, true)

    def periodNames = [
            1 : "1 квартал",
            2 : "полгодие",
            3 : "девять месяцев",
            4 : "год",
    ]
    // добавляем приемники предыдущего отсутствующего периода
    missingDestinationDepartmentFormTypes.each { departmentFormType ->
        def tmpFormData = null
        def isSource = false
        def periodName= periodNames[expectedPrevPeriodOrder]
        def periodYear = expectedPrevPeriodYear
        def tmpMonthOrder = (expectedPrevPeriodOrder == 4 ? "" : currentPeriod.name)

        FormToFormRelation formToFormRelation = getFormToFormRelation(tmpFormData, departmentFormType, isSource, periodYear, periodName, tmpMonthOrder)
        sources.sourceList.add(formToFormRelation)
    }

    sources.sourcesProcessedByScript = true
    return sources.sourceList
}

/**
 * Добавить данные из мапы с периодами и источниками-приемниками в список.
 *
 * @param resultList список с результатом
 * @param periodDepartmentFormTypesMap мапы с периодами и источниками-приемниками (период -> источник-применик)
 * @param isSource признак источника
 */
def addToResult(def resultList, def periodDepartmentFormTypesMap, def isSource) {
    periodDepartmentFormTypesMap.each { period, departmentFormTypes ->
        // проходим по всем источникам-приемникам в каждом периоде
        departmentFormTypes.each { departmentFormType ->
            def monthOrder = (isMonthlyForm(departmentFormType.formTypeId, period.id) ? formData.periodOrder : null)
            FormData tmpFormData = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind,
                    departmentFormType.departmentId, period.id, monthOrder)

            FormToFormRelation formToFormRelation = getFormToFormRelation(tmpFormData, departmentFormType, isSource, period.taxPeriod.year, period.name, monthOrder)
            resultList.add(formToFormRelation)
        }
    }
}

/** Получить полное название подразделения по id подразделения. */
def getDepartmentFullName(def id) {
    if (departmentFullNameMap[id] == null) {
        departmentFullNameMap[id] = departmentService.getParentsHierarchy(id)
    }
    return departmentFullNameMap[id]
}

/** Получить подразделение по id. */
def getDepartmentById(def id) {
    if (id == null) {
        return null
    }
    if (departmentMap[id] == null) {
        departmentMap[id] = departmentService.get(id)
    }
    return departmentMap[id]
}

/** Получить тип фомры по id. */
def getFormTypeById(def id) {
    if (formTypeMap[id] == null) {
        formTypeMap[id] = formTypeService.get(id)
    }
    return formTypeMap[id]
}

def isMonthlyForm(def formTemplateId, def periodId) {
    def key = formTemplateId?.toString() + periodId?.toString()
    if (monthlyMap[key] == null) {
        monthlyMap[key] = formDataService.getFormTemplate(formTemplateId, periodId)?.monthly
    }
    return monthlyMap[key]
}

/**
 * Получить запись для источника-приемника.
 *
 * @param tmpFormData нф
 * @param departmentFormType информация об источнике приемнике
 * @param isSource признак источника
 * @param periodYear год периода
 * @param periodName название периода
 * @param monthOrder номер месяца (для ежемесячной формы)
 */
def getFormToFormRelation(def tmpFormData, def departmentFormType, def isSource, def periodYear, def periodName, def monthOrder) {
    FormToFormRelation formToFormRelation = new FormToFormRelation()
    formToFormRelation.fullDepartmentName = getDepartmentFullName(departmentFormType.departmentId)
    formToFormRelation.performer = getDepartmentById(departmentFormType.performerId);
    formToFormRelation.formDataKind = departmentFormType.kind
    formToFormRelation.source = isSource
    formToFormRelation.month = (monthOrder ? Formats.getRussianMonthNameWithTier(monthOrder) : null)
    formToFormRelation.year = periodYear
    formToFormRelation.periodName = periodName
    if (tmpFormData != null) {
        formToFormRelation.created = true
        formToFormRelation.formType = tmpFormData.formType
        formToFormRelation.state = tmpFormData.state
        formToFormRelation.formDataId = tmpFormData.id
        formToFormRelation.correctionDate = departmentReportPeriodService.get(tmpFormData.departmentReportPeriodId)?.correctionDate
    } else {
        formToFormRelation.formType = getFormTypeById(departmentFormType.formTypeId)
        formToFormRelation.created = false
    }
    return formToFormRelation
}