package form_template.property.property_945_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
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
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
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
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

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

@Field
def patternMap = [
        /Стоимость имущества по субъекту Федерации \((.+)\)/                              : 'priceSubject',
        /Стоимость движимого имущества, отраженного на балансе до 01.01.2013 \((.+)\)/    : 'headPriceMovableOKTMO',
        /Стоимость недвижимого имущества по субъекту Федерации \((.+)\)/                  : 'headPriceUnmovableSubject',
        /Стоимость льготируемого имущества по субъекту Федерации \((.+)\)/                : 'priceBenefitSubject',
        /Имущество, подлежащее налогообложению/                                           : 'propertyTaxed',
        /в т\.ч\. стоимость недвижимого имущества по населенному пункту \"(.+)\" \((.+)\)/: 'priceUnmovableCityOKTMO',
        /Льготируемое имущество \(всего\)/                                                : 'headBenefitProperty',
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
        'headBenefitProperty' : 'Льготируемое имущество (всего)',
        'titleCorrection' : 'корректировка:',
        'titleCorrectionObject' : 'корректировка (пообъектно):',
        'titleCategory' : '- <Категория 1 имущества> (пообъектно):',
        'titleCategoryCorrection' : 'корректировка <Категория 1 имущества>  (пообъектно):',
        'totalCorrection' : 'итого по корректировкам',
        'totalUsingCorrection' : 'ИТОГО с учетом корректировки',
        'totalCategoryUsingCorrection' : 'ИТОГО <Категория 1 имущества>  с учетом корректировки'
]

@Field
def aliasNums = ['priceSubject' : 1, // №1 строка 1 - может дублироваться вместе со всеми последующими
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
                 'headBenefitProperty' : 19, // строка 19 - последующие могут дублироваться
                 'titleCategory' : 20, // строка 20
                 'titleCategoryCorrection' : 23, // строка 23
                 'totalCorrection' : 26, // строка 26
                 'totalCategoryUsingCorrection' : 27 // строка 27
]

@Field
def startDate = null

@Field
def endDate = null

@Field
def isBalancePeriod

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
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
    for (def key : patternMap.keySet()){
        if (row.name ==~ key) {
            return patternMap[key]
        }
    }
    return null
}

def getTitlePattern(def row) {
    for (def key : patternMap.keySet()){
        if (row.name ==~ key) {
            return key
        }
    }
    return null
}

def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    calcCheckSubjects(dataRows, true)
    dataRows.each { row ->
        row.taxBaseSum = row.taxBase1 - row.taxBase2 + row.taxBase3 - row.taxBase4 - row.taxBase5
    }
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def subjectId = null
    def oktmo = null
    def propertyCategory = null

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

        if (titleAlias == getTitle(8)) {
            // вытаскиваем субъект из строки вида 8
            subject = row.name.replaceAll(getTitlePattern(row), '$1')
            if (subject) {
                subjectId = getRecordId(4L, 'NAME', subject, row.getIndex(), null)
            }
        }
        if (titleAlias == getTitle(12)) {
            // вытаскиваем октмо из строки вида 12
            oktmo = row.name.replaceAll(getTitlePattern(row), '$2')
            if (oktmo) {
                oktmoId = getRecordId(96L, 'CODE', oktmo, row.getIndex(), null)
            }
        }
        if (titleAlias == getTitle(20)) {
            // вытаскиваем категорию имущества из строки вида 20
            propertyCategory = row.name.replaceAll(getTitlePattern(row), '$1')
        }

        // Проверка существования параметров налоговых льгот для категорий имущества субъекта
        if (titleAlias == getTitle(27)) {
            if (subjectId != null && propertyCategory != null){
                String filter = "REGION_ID = " + subjectId.toString() + " and ASSETS_CATEGORY = '" + propertyCategory + "'"
                def records = refBookFactory.getDataProvider(203).getRecords(getReportPeriodEndDate(), null, filter, null)
                if (records.size() == 0) {
                    loggerError(row, errorMsg + "Для текущего субъекта и категории имущества не предусмотрена налоговая льгота (в справочнике «Параметры налоговых льгот налога на имущество» отсутствует такая запись)!")
                }
            }
        }
        // Проверка существования параметров декларации для субъекта-ОКТМО
        if (titleAlias == getTitle(12)) {
            if (subjectId != null && oktmoId != null) {
                String filter = "REGION_ID = " + subjectId.toString() + " and OKTMO = " + oktmoId.toString()
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

    calcCheckSubjects(dataRows, false)
}

void calcCheckSumBetween(def dataRows, def indexResult, def indexBegin, def indexEnd, boolean isCalc) {
    if (indexBegin < indexEnd - 1){
        totalColumns.each { column ->
            sum = dataRows[(indexBegin)..(indexEnd - 2)].sum { it[column] }
            def row = dataRows[indexResult - 1]
            calcCheck(row, column, sum, isCalc)
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
    def titles = aliasNums.keySet().toArray()
    def String aliasRoot = titles[0] // идем с корня
    def List<String> validAliasList = Arrays.asList(aliasRoot)
    def Integer rowTypeIndex = 0
    def Subject currentSubject = null
    def OKTMO currentOKTMO = null
    def Category currentCategory = null
    def currentRow = null
    // проходим по строкам НФ
    for (def i = 0; i < dataRows.size(); i++) {
        // берем строку
        def row = dataRows[i]
        // алиас текущей строки, может повторяться
        def titleAlias = getTitleAlias(row)
        // случай повтора строк типа 1 или 12
        if (rowTypeIndex == -1 && titleAlias) {
            rowTypeIndex = titles.indexOf(titleAlias)
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
                case 19: currentOKTMO.index19 = row.getIndex()
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
            }
            // иначе получаем список возможных следующих псевдонимов
            def temp = getNextAliasRowTypeIndex(rowTypeIndex, titleAlias)
            rowTypeIndex = temp.rowTypeIndex
            validAliasList = temp.nextValidAliasList
        }
    }
    if (rowTypeIndex != -1) {
        if (!isCalc) {
            errorExpected(currentRow,  validAliasList)
        }
    } else {
        isCalc ? calcTotals(subjects, dataRows) : checkTotals(subjects, dataRows)
    }
}

void errorExpected(def row, def validAliasList) {
    def expectedAliases = validAliasList.collect { alias ->
        if (alias != null) {
            titleMap[titleMap.find {key, value ->
                // ищем по псевдониму или обрезаем циферки в конце
                alias == key || (alias - key) in ['1', '2', '3']
            }.key]
        } else { '<Объект>'}
    }.join('» или «')
    loggerError(row, row ?
            "Строка ${row.getIndex()}: Ожидается строка вида «${expectedAliases}»!" :
            "Ожидается строка вида «${expectedAliases}»!")
}

// рассчитываем итоги в строках
void calcTotals(def subjects, def dataRows) {
    for (def subject in subjects) {
        for (def oktmo in subject.oktmos) {
            for (def category in oktmo.categories) {
                // строка 23(26) сумма строк между ними
                calcCheckSumBetween(dataRows, category.index23, category.index23, category.index26, true)
                calcCheckSumBetween(dataRows, category.index26, category.index23, category.index26, true)
                // строка 20 сумма строк между 20 и 23
                calcCheckSumBetween(dataRows, category.index20, category.index20, category.index23, true)
                // строка 27 сумма строк 20 и 26
                calcCheckSum(dataRows, category.index27, category.index20, category.index26, true)
            }
            // строка 19 сумма строк 27
            calcCheckSumList(dataRows, oktmo.index19, oktmo.categories.collect { it.index27 }, true)
            // строка 14(17) сумма строк между ними
            calcCheckSumBetween(dataRows, oktmo.index14, oktmo.index14, oktmo.index17, true)
            calcCheckSumBetween(dataRows, oktmo.index17, oktmo.index14, oktmo.index17, true)
            // строка 18 сумма строк 13 и 17
            calcCheckSum(dataRows, oktmo.index18, oktmo.index13, oktmo.index17, true)
            // строка 12 сумма строк 18 и 19
            calcCheckSum(dataRows, oktmo.index12, oktmo.index18, oktmo.index19, true)
        }
        // строка 11 сумма строк 19
        calcCheckSumList(dataRows, subject.index11, subject.oktmos.collect { it.index19 }, true)
        // строка 3(6) = сумма строк между 3 и 6
        calcCheckSumBetween(dataRows, subject.index3, subject.index3, subject.index6, true)
        calcCheckSumBetween(dataRows, subject.index6, subject.index3, subject.index6, true)
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
    for (def subject in subjects) {
        // строка 1 = сумма строк 2 и 8
        calcCheckSum(dataRows, subject.index1, subject.index2, subject.index8, false)
        // строка 3(6) = сумма строк между 3 и 6
        calcCheckSumBetween(dataRows, subject.index3, subject.index3, subject.index6, false)
        calcCheckSumBetween(dataRows, subject.index6, subject.index3, subject.index6, false)
        // строка 7 = сумма строк 2 и 6
        calcCheckSum(dataRows, subject.index7, subject.index2, subject.index6, false)
        // строка 8 = сумма строк 12
        calcCheckSumList(dataRows, subject.index8, subject.oktmos.collect { it.index12 }, false)
        // строка 9 = сумма строк 17
        calcCheckSumList(dataRows, subject.index9, subject.oktmos.collect { it.index17 }, false)
        // строка 10 = сумма строк 8 и 9
        calcCheckSum(dataRows, subject.index10, subject.index8, subject.index9, false)
        // строка 11 сумма строк 19
        calcCheckSumList(dataRows, subject.index11, subject.oktmos.collect { it.index19 }, false)
        for (def oktmo in subject.oktmos) {
            // строка 12 сумма строк 18 и 19
            calcCheckSum(dataRows, oktmo.index12, oktmo.index18, oktmo.index19, false)
            // строка 14(17) сумма строк между ними
            calcCheckSumBetween(dataRows, oktmo.index14, oktmo.index14, oktmo.index17, false)
            calcCheckSumBetween(dataRows, oktmo.index17, oktmo.index14, oktmo.index17, false)
            // строка 18 сумма строк 13 и 17
            calcCheckSum(dataRows, oktmo.index18, oktmo.index13, oktmo.index17, false)
            // строка 19 сумма строк 27
            calcCheckSumList(dataRows, oktmo.index19, oktmo.categories.collect { it.index27 }, false)
            for (def category in oktmo.categories) {
                // строка 20 сумма строк между 20 и 23
                calcCheckSumBetween(dataRows, category.index20, category.index20, category.index23, false)
                // строка 23(26) сумма строк между ними
                calcCheckSumBetween(dataRows, category.index23, category.index23, category.index26, false)
                calcCheckSumBetween(dataRows, category.index26, category.index23, category.index26, false)
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
    def titles = aliasNums.keySet().toArray()
    def String aliasRoot = getTitle(1)
    def String aliasSpecial = getTitle(12)
    def String aliasSpecial2 = getTitle(20)
    def List<String> nextValidAliasList = []
    // если дошли до строки типа 27, то или на 1 строку или на 12-ую или на 20-ую (по типу)
    if (rowTypeIndex == titles.size() - 1) {
        rowTypeIndex = -1
        nextValidAliasList.add(aliasRoot)
        nextValidAliasList.add(aliasSpecial)
        nextValidAliasList.add(aliasSpecial2)
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

    return ['rowTypeIndex' : rowTypeIndex, 'nextValidAliasList' : nextValidAliasList]
}

// получить уникальный алиас строки по типу
def String getTitle(def int typeNum) {
    if (!(typeNum in ((1..3) + (6..14) + (17..20) + [23, 26, 27]))) {
        return null
    }
    return aliasNums.find { key, value -> value == typeNum }.key
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tempRow, 'name'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 7, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tempRow, 'name'),
            (xml.row[0].cell[1]): 'Налоговая база (в руб. коп.).',
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
            row.getCell(it).setStyleAlias('Автозаполняемая')
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

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}
