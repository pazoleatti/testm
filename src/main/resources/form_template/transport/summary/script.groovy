package form_template.transport.summary

import groovy.time.TimeCategory

/**
 * Форма "Расчет суммы налога по каждому транспортному средству".
 */

switch (formDataEvent) {
// создать
    case FormDataEvent.CREATE :
        checkBeforeCreate()
        // save(getData(formData))
        // calculationTotal()
        break
// расчитать
    case FormDataEvent.CALCULATE :
        deleteTotal()
        if (checkRequiredField()) {
            fillForm()
            determinationTransportType()
            checkNSI()
            if (logicalChecks()){
                sort()
                calculationTotal()
                setRowIndex()
            }
        }
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        sort()
        break
// проверить
    case FormDataEvent.CHECK :
        if (checkRequiredField()){
            deleteTotal()
            determinationTransportType()
            fillForm()
            checkNSI()
            if (logicalChecks()){
                sort()
                calculationTotal()
                setRowIndex()
            }
        }
        break
// утвердить
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :
        if (logicalChecks()){
            checkNSI()
        }
        break
// принять из утверждена
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED :
        if (logicalChecks()){
            checkNSI()
        }
        break
// принять из создана
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :
        if (logicalChecks()){
            checkNSI()
        }
        break
// вернуть из принята в создана
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED :
        checkToCancelAccept()
        break
    case FormDataEvent.ADD_ROW :
        deleteTotal()
        addRow()
        //calculationTotal()
        setRowIndex()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1  - rowNumber
// графа 2  - okato
// графа 3  - tsTypeCode
// графа 4  - tsType
// графа 5  - vi
// графа 6  - model
// графа 7  - regNumber
// графа 8  - taxBase
// графа 9  - taxBaseOkeiUnit
// графа 10 - ecoClass
// графа 11 - years
// графа 12 - ownMonths
// графа 13 - coef362
// графа 14 - taxRate
// графа 15 - calculatedTaxSum
// графа 16 - taxBenefitCode
// графа 17 - benefitStartDate
// графа 18 - benefitEndDate
// графа 19 - coefKl
// графа 20 - benefitSum
// графа 21 - taxSumToPay


/**
 * Скрипт для добавления новой строки.
 */
void addRow() {
    def data = getData(formData)

    def row = formData.createDataRow()
    data.insert(row, data.getAllCached().size() + 1)
    ['okato', 'tsTypeCode', 'vi', 'model', 'regNumber', 'taxBase',
            'taxBaseOkeiUnit', 'ecoClass', 'years', 'ownMonths',
            'taxBenefitCode', 'benefitStartDate', 'benefitEndDate'].each { alias ->
        row.getCell(alias).editable = true
        row.getCell(alias).setStyleAlias("Редактируемое поле")
    }
}

/**
 * Скрипт для подсчета строки ИТОГО.
 *
 * @author mfayzullin
 */
void calculationTotal() {
    def data = getData(formData)

    // подготовка названии колонок по которым будут производиться подсчеты
    def columns = ['calculatedTaxSum', 'benefitSum', 'taxSumToPay']
    def sums = []

    // подсчет сумм
    def rowCount = data.getAllCached().size()
    if (rowCount > 0) {
        columns.collect(sums) {
            [it, summ(formData, data.getAllCached(), new ColumnRange(it, 0, rowCount - 1))]
        }
    }

    // добавление строки ИТОГО
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.getCell("fix").setColSpan(2)
    totalRow.fix = 'ИТОГО:'

    // вставка подсчитанных сумм в строку ИТОГО
    sums.each {
        totalRow[it[0]] = it[1]
    }
    data.insert(totalRow, data.getAllCached().size() + 1)
    save(data)
}

/**
 * Скрипт для проверки создания.
 *
 * @since 15.02.2013 18:20
 */
void checkBeforeCreate() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }

    if (formData.kind != FormDataKind.SUMMARY) {
        logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
    }

    if (formDataDepartment.type == DepartmentType.ROOT_BANK) {
        logger.error('Нельзя создавать форму на уровне банка.')
    }
}

/**
 * Скрипт для проверки соответствия НСИ.
 */
void checkNSI() {
    def data = getData(formData)
    for (def row : data.getAllCached()) {
        if (row.getAlias() == 'total') {
            continue
        }

        /*
         * Проверка совпадения ОКАТО со справочным
         *
         * В справочнике «Коды ОКАТО» должна быть строка для которой выполняется условие:
         * «графа 2» текущей строки формы =  «графа 1» строки справочника
         */
        //def refOkatoCodeDataProvider = refBookFactory.getDataProvider(3)
        if (row.okato != null && getRefBookValue(3, row.okato, "OKATO") == null){ // refOkatoCodeDataProvider.getRecords(new Date(), null, "OKATO like '"+getRefBookValue(3, row.okato, "OKATO")+"'", null).getRecords().size == 0){
            logger.error('Неверный код ОКАТО')
        }

        /*
         * Проверка совпадения кода вида ТС со справочным
         *
         * В справочнике  «Коды видов транспортного средства» должна быть строка для которой выполняется условие:
         * «графа 3» (поле «Код вида транспортного средства (ТС)») текущей строки формы = «графа 2» (поле  «Код вида ТС») строки справочника
         */

        if (row.tsTypeCode != null && getRefBookValue(42, row.tsTypeCode, "CODE") == null){//refTransportCodeDataProvider.getRecords(new Date(), null, "CODE like '"+row.tsTypeCode+"'", null).getRecords().size == 0) {
            logger.error('Неверный код вида транспортного средства!')
        }

        /**
         * Проверка наименования вида ТС коду вида ТС
         *
         * Значение «графы 4» (поле «Вид транспортного средства») совпадает со значение поля «Наименование вида транспортного средства» строки справочника «Коды видов транспортных средств»,  для которой
         * «графа 3» (поле «Код вида транспортного средства (ТС)») текущей строки формы = «графа 2» (поле  «Код вида ТС») строки справочника
         * TODO
         */
        def refTransportCodeDataProvider = refBookFactory.getDataProvider(42)
        def tsTypeCode = getRefBookValue(42, row.tsTypeCode, "CODE")
        def tsType = getRefBookValue(42, row.tsType, "NAME")

        if (row.tsType != null && row.tsTypeCode != null &&(tsTypeCode == null || tsType == null || refTransportCodeDataProvider.getRecords(new Date(), null, "CODE like '"+tsTypeCode+"' and NAME LIKE '"+tsType+"'", null).getRecords().size() == 0)){
            logger.error('Название вида ТС не совпадает с Кодом вида ТС')
        }

        /*
         * Проверка совпадения единицы измерения налоговой базы по ОКЕИ со справочной
         *
         * В справочнике «Коды единиц измерения налоговой базы на основании ОКЕИ» должна быть строка, для которой выполняется условие:
         * «графа 9» текущей строки формы = «графа 1» строки справочника
         */
        //def refTaxBaseCodeDataProvider = refBookFactory.getDataProvider(12)
        if (row.taxBaseOkeiUnit != null && getRefBookValue(12, row.taxBaseOkeiUnit, "CODE") == null) {//refTaxBaseCodeDataProvider.getRecords(new Date(), null, "CODE LIKE '"+row.taxBaseOkeiUnit+"'", null).getRecords().size == 0){
            logger.error("Неверный код единицы измерения налоговой базы")
        }

        /**
         * Проверка совпадения экологического класса со справочным
         *
         * В справочнике «Экологические классы» должна быть строка, для которой выполняется условие:
         * «графа 10» текущей строки формы = «графа 1» строки справочника
         */
        def refEcoClassDataProvider = refBookFactory.getDataProvider(40)
        if (row.ecoClass!=null && getRefBookValue(40, row.ecoClass, "NAME") == null) {// refEcoClassDataProvider.getRecords(new Date(), null, "NAME LIKE '"+row.ecoClass+"'", null).getRecords().size == 0) {
            logger.error("Неверный экологический класс")
        }

        /**
         * Проверка льготы
         */
        if (row.taxBenefitCode != null){
            def refTaxBenefitParameters = refBookFactory.getDataProvider(7)
            def region = getRegionByOkatoOrg(row.okato)
            query = "TAX_BENEFIT_ID ="+row.taxBenefitCode+" AND DICT_REGION_ID = "+region.record_id
            if (refTaxBenefitParameters.getRecords(new Date(), null, query, null).getRecords().size() == 0){
                logger.error("Выбранная льгота для текущего региона не предусмотрена")
            }
        }
    }
}

/**
 * Проверка обязательных полей.
 *
 * @author rtimerbaev
 * @since 19.02.2013 13:30
 */
def checkRequiredField() {
    def data = getData(formData)
    for (def row : data.getAllCached()) {
        if (row.getAlias() == 'total') {
            continue
        }

        def errorMsg = ''

        // 2, 3, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 20 , 21
        ['okato', 'tsTypeCode', 'vi', 'model', 'regNumber', 'taxBase', 'ownMonths'].each {
            if (row.getCell(it) != null && (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue()))) {
                errorMsg += (!''.equals(errorMsg) ? ', ' : '') + '"' + row.getCell(it).getColumn().getName() + '"'
            }
        }


        /**
         * Проверка одновременного не заполнения данных о налоговой льготе
         *
         * Если  «графа 16» не заполнена ТО не заполнены графы 17,18,19,20
         */
        def notNull17_20 = row.benefitStartDate != null && row.benefitEndDate != null
        if ((row.taxBenefitCode != null) ^ notNull17_20){
            logger.error("Данные о налоговой льготе указаны не полностью")
            return false
        }


        if (!''.equals(errorMsg)) {
            logger.error("Не заполнены поля в колонках : $errorMsg.")
            return false
        }
    }
    return true
}

/**
 * Проверка при "отменить принятие".
 */
void checkToCancelAccept() {
    if (declarationService.find(1, formData.departmentId, formData.reportPeriodId) != null) {
        logger.error('Отмена принятия сводной налоговой формы невозможно, т.к. уже подготовлена декларация.')
    }
}

/*
 * Скрипт для удаления строки.
 */
void deleteRow() {
    def data = getData(formData)
    def row = currentDataRow
    if (row != null && row.getAlias() != 'total'){
        // удаление строки
        data.delete(row)

        // пересчет номеров строк
        def n = 1
        data.getAllCached().each{ r ->
            r.rowNumber = n++
        }
        save(data)
    }
}

/**
 * Скрипт для удаления строки ИТОГО.
 *
 * @author rtimerbaev
 */
void deleteTotal() {
    def data = getData(formData)
    if (!hasTotal(data)) {
        return
    }
    def row = (data.getAllCached().size() > 0 ? getRow(data, 'total') : null)
    if (row != null) {
        formDataService.getDataRowHelper(formData).delete(row)
        save(data)
    }
}

/**
 * Определение наименования типа транспортного средства
 * Скрипт для получения названия вида транспортного средства по коду ТС.
 */
void determinationTransportType() {
    def data = getData(formData)
    data.getAllCached().each { row ->
        if (row.tsTypeCode != null){
            def refTransportCodeDataProvider = refBookFactory.getDataProvider(42)
            row.tsType = refTransportCodeDataProvider.getRecords(new Date(), null, "CODE like '"+getRefBookValue(42, row.tsTypeCode, "CODE") +"'", null).getRecords().get(0).record_id.numberValue
        }
    }
    save(data)
}

/**
 * 2. Алгоритмы заполнения полей формы (9.1.1.8.1) Табл. 45.
 *
 * @author auldanov
 * @since 24.02.2013 14:00
 */
def fillForm() {
    /** Число полных месяцев в текущем периоде (либо отчетном либо налоговом). */
    int monthCountInPeriod = 0
    def period = reportPeriodService.get(formData.reportPeriodId)
    if (period == null) {
        info.error('Не найден отчетный период для налоговой формы.')
    } else {
        monthCountInPeriod = period.getMonths()
    }

    def data = getData(formData)

    /** Уменьшающий процент. */
    def reducingPerc = 1 // TODO (Ramil Timerbaev)
    /** Пониженная ставка. */
    def loweringRates = 0 // TODO (Ramil Timerbaev)

    def index = 1
    data.getAllCached().each { row ->
        // получение региона по ОКАТО
        def region = getRegionByOkatoOrg(row.okato)
        // получение параметров региона
        if (row.taxBenefitCode){
            // датапровайдер для справочника "Параметры налоговых льгот"
            def  refDataProvideTaxBenefit = refBookFactory.getDataProvider(7)
            // запрос по выборке данных из справочника
            def query = "TAX_BENEFIT_ID = "+row.taxBenefitCode+" and DICT_REGION_ID = "+region.record_id
            def records = refDataProvideTaxBenefit.getRecords(new Date(), null, query, null).getRecords()

            if (records.size() == 0){
                logger.error('Ошибка при получении параметров налоговых льгот')
                return;
            } else{
                reducingPerc = records.get(0).percent
                loweringRates = records.get(0).rate
            }
        }

        /*
         * Графа 1 (№ пп) - Установка номера строки
         * Скрипт для установки номера строки.
         */
        row.rowNumber = index
        index += 1

        /*
         * Гафа 9 Единица измерения налоговой базы по ОКЕИ
         * Скрипт для выставления значения по умолчанию в столбец "Единица измерения налоговой базы по ОКЕИ",
         * если это значение не задано.
         */
        if (row.taxBaseOkeiUnit == null) {
            def refTaxBaseCodeDataProvider = refBookFactory.getDataProvider(12)
            def taxBaseOkeiUnitData = refTaxBaseCodeDataProvider.getRecords(new Date(), null, "CODE LIKE '251'", null).getRecords()
            row.taxBaseOkeiUnit = taxBaseOkeiUnitData.get(0).record_id.numberValue
        }


        /*
         * Графа 13 - Коэффициент Кв
         * Скрипт для вычисления автоматических полей 13, 19, 20
         */
        if (row.ownMonths != null) {
            row.coef362 = (row.ownMonths / monthCountInPeriod).setScale(4, BigDecimal.ROUND_HALF_UP)
        } else {
            row.coef362 = null

            def errors = []
            if (row.ownMonths == null) {
                errors.add('"Срок владения ТС (полных месяцев)"')
            }
            logger.error("\"Коэффициент Кв\" не может быть вычислена, т.к. поля $errors не были вычислены или заполнены.")
        }


        /*
         * Графа 14 (Налоговая ставка)
         * Скрипт для вычисления налоговой ставки
         */
        row.taxRate = null
        if (row.tsTypeCode != null && row.years != null && row.taxBase != null) {
            def tsTypeCode = getRefBookValue(42 ,row.tsTypeCode, "CODE")
            // Провайдер для справочника «Ставки транспортного налога»
            def  refDataProvideTransportRate = refBookFactory.getDataProvider(41)
            // запрос по выборке данных из справочника
            def query = " and ((MIN_POWER is null or MIN_POWER < "+row.taxBase+") and (MAX_POWER is null or MAX_POWER > "+row.taxBase+"))"+
                    "and ((MIN_AGE is null or MIN_AGE < "+row.years+") and (MAX_AGE is null or MAX_AGE > "+row.years+"))";

            /**
             * Переберем варианты
             * 1. код = коду ТС && регион указан
             * 2. код = коду ТС && регион НЕ указан
             * 3. код = соответствует 2м двум символом кода ТС && регион указан
             * 4. код = соответствует 2м двум символом кода ТС && регион НЕ указан
             */

            def regionSqlPartID = " and DICT_REGION_ID = "+region.record_id
            def regionSqlPartNull = " and DICT_REGION_ID is null"

            // вариант 1
            def queryLikeStrictly = "CODE LIKE '"+tsTypeCode.toString()+"'"+query
            def finalQuery = queryLikeStrictly + regionSqlPartID
            def record = refDataProvideTransportRate.getRecords(new Date(), null, finalQuery, null).getRecords()
            // вариант 2
            if (record.size() == 0){
                finalQuery = queryLikeStrictly + regionSqlPartNull
                record = refDataProvideTransportRate.getRecords(new Date(), null, finalQuery, null).getRecords()
            }

            def queryLike = "CODE LIKE '"+tsTypeCode.toString().substring(0, 2)+"%'"+  query
            // вариант 3
            if (record.size() == 0){
                finalQuery = queryLike + regionSqlPartID
                record = refDataProvideTransportRate.getRecords(new Date(), null, finalQuery, null).getRecords()
            }
            // вариант 4
            if (record.size() == 0){
                finalQuery = queryLike + regionSqlPartNull
                record = refDataProvideTransportRate.getRecords(new Date(), null, finalQuery, null).getRecords()
            }



            if (record.size() != 0){
                row.taxRate = record.get(0).record_id.numberValue
            } else{
                logger.error("Ошибка определения налоговой ставки")
            }
            // TODO удалить этот старый код -> row.taxRate = transportTaxDao.getTaxRate(row.tsTypeCode, row.years, row.taxBase, region.code)
        } else {
            row.taxRate = null
            def fields = []

            if(row.tsTypeCode == null) {
                fields.add('"Код вида транспортного средства (ТС)"')
            }
            if(row.years == null) {
                fields.add('"Возраст ТС (полных лет)"')
            }
            if(row.taxBase == null) {
                fields.add('"Налоговая база"')
            }

            logger.error("Налоговая ставка не может быть вычислена, т.к. не заполнены поля: ${fields}.")
        }


        /*
         * Графа 15 (Сумма исчисления налога) = Расчет суммы исчисления налога
         * Скрипт для вычисления значения столбца "сумма исчисления налога".
         */
        if (row.taxBase != null && row.coef362 != null && row.taxRate != null) {
            row.calculatedTaxSum = (row.taxBase * row.coef362 * row.taxRate).setScale(0, BigDecimal.ROUND_HALF_UP)
        } else {
            row.calculatedTaxSum = null

            def errors = []
            if(row.taxBase == null) {
                errors.add('"Налоговая база"')
            }
            if(row.coef362 == null) {
                errors.add('"Коэффициент Кв"')
            }
            if(row.taxRate == null) {
                errors.add('"Налоговая ставка"')
            }

            logger.error("\"Сумма исчисления налога\" не может быть вычислена, т.к. поля $errors не были вычислены или заполнены.")
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

                def errors = []
                if (row.benefitStartDate == null) {
                    errors.add('"Дата начала"')
                }
                if (row.benefitEndDate == null) {
                    errors.add('"Дата окончания"')
                }
                //logger.error("\"Коэффициент Кл\" не может быть вычислена, т.к. поля $errors не были вычислены или заполнены.")
            }
        }

        /*
         * Графа 20 - Сумма налоговой льготы (руб.)
         */
        if (row.taxBenefitCode != null) {
            def taxBenefitCode = getRefBookValue(6, row.taxBenefitCode, 'CODE').stringValue
            if (taxBenefitCode == '20210' || taxBenefitCode == '30200') {
                row.benefitSum = (row.taxBase * row.coefKl * row.taxRate).setScale(0, BigDecimal.ROUND_HALF_UP)
            } else if (taxBenefitCode == '20220') {
                row.benefitSum = round(row.taxBase * row.taxRate * row.coefKl * reducingPerc / 100, 0)
            } else if (taxBenefitCode == '20230') {
                row.benefitSum = round(row.coefKl * row.taxRate * (row.taxRate - loweringRates), 0)
            } else {
                row.benefitSum = 0
            }
        }

        /*
         * Графа 21 - Исчисленная сумма налога, подлежащая уплате в бюджет.
         * Скрипт для вычисления значения столбца "Исчисленная сумма налога, подлежащая уплате в бюджет".
         */
        if (row.calculatedTaxSum != null) {
            row.taxSumToPay = (row.calculatedTaxSum - (row.benefitSum?:0)).setScale(0, BigDecimal.ROUND_HALF_UP)
        } else {
            row.taxSumToPay = null

            def errors = []
            if(row.calculatedTaxSum == null) {
                errors.add('"Сумма исчисления налога"')
            }
            if(row.benefitSum == null) {
                errors.add('"Сумма налоговой льготы (руб.)"')
            }

            logger.error("\"Исчисленная сумма налога, подлежащая уплате в бюджет\" не может быть вычислена, т.к. поля $errors не были вычислены или заполнены.")
        }
    }
    save(data)
}

/**
 * Скрипт логические проверки сводной формы.
 *
 * @since 18.02.2013 14:00
 */
def logicalChecks() {
    def data = getData(formData)
    for (def row : data.getAllCached()) {
        if (row.getAlias() == 'total') {
            continue
        }

        /** Число полных месяцев в текущем периоде (либо отчетном либо налоговом). */
        int monthCountInPeriod = 0
        def period = reportPeriodService.get(formData.reportPeriodId)
        if (period == null) {
            info.error('Не найден отчетный период для налоговой формы.')
        } else {
            monthCountInPeriod = period.getMonths()
        }

        // 13 графа - Поверка на соответствие дат использования льготы
        if (row.taxBenefitCode && row.benefitEndDate != null && (row.benefitStartDate == null || row.benefitStartDate > row.benefitEndDate)) {
            logger.error('Дата начала(окончания) использования льготы неверная!')
        }

        // 14 граафа - Проверка, что Сумма исчисления налога больше или равна Сумма налоговой льготы
        if (row.calculatedTaxSum != null && row.benefitSum != null
                && row.calculatedTaxSum < row.benefitSum) {
            logger.error('Сумма исчисления налога меньше Суммы налоговой льготы.')
        }

        // 15 графа - Проверка Коэффициент Кв
        //logger.info('kv = ' + row.coef362)
        if (row.coef362 != null) {
            if (row.coef362 < 0.0) {
                logger.error('Коэффициент Кв меньше нуля.')
            } else if (row.coef362 > 1.0) {
                logger.error('Коэффициент Кв больше единицы.')
            }
        }

        // 16 графа - Проверка Коэффициент Кл
        //logger.info('kl = ' + row.coefKl)
        if (row.coefKl != null) {
            if (row.coefKl < 0.0){
                logger.error('Коэффициент Кл меньше нуля.')
            } else if (row.coefKl > 1.0) {
                logger.error('Коэффициент Кл больше единицы.')
            }
        }

        /**
         * Проверка одновременного не заполнения данных о налоговой льготе
         *
         * Если  «графа 16» не заполнена ТО не заполнены графы 17,18,19,20
         */
        def notNull17_20 = row.benefitStartDate != null && row.benefitEndDate != null && row.coefKl != null && row.benefitSum != null
        if ((row.taxBenefitCode != null) ^ notNull17_20){
            logger.error("Данные о налоговой льготе указаны не полностью")
            return;
        }

        // дополнительная проверка для 12 графы
        if (row.ownMonths != null && row.ownMonths > monthCountInPeriod) {
            logger.warn('Срок владение ТС не должен быть больше текущего налогового периода.')
        }
    }
    return true;
}

/**
 * Установка номера строки.
 *
 * @author rtimerbaev
 * @since 20.02.2013 13:00
 */
void setRowIndex() {
    def data = getData(formData)
    def index = 0
    for (def row : data.getAllCached()) {
        if (row.getAlias() == 'total') {
            continue
        }
        row.rowNumber = index + 1
        index += 1
    }
    save(data)
}

/**
 * Скрипт для сортировки.
 */
void sort() {
    def data = getData(formData)

    // сортировка
    data.getAllCached().sort { a, b ->
        int val = (a.okato ?: "").compareTo(b.okato ?: "")
        if (val == 0) {
            val = (a.tsTypeCode?: "").compareTo(b.tsTypeCode ?: "")
        }
        return val
    }
    save(data)
}

/**
 * Получить строку по алиасу.
 *
 * @param dataRows данные нф (helper)
 * @param alias алиас
 * @return
 */
def getRow(def dataRows, def alias) {
    dataRows.getDataRow(dataRows.getAllCached(), alias)
}

/**
 * Сохранить измененные значения нф.
 *
 * @param dataRows данные нф (helper)
 */
void save(def dataRows) {
    dataRows.save(dataRows.getAllCached())
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
 * Проверить наличие итоговой строки.
 *
 * @param data данные нф
 */
def hasTotal(def data) {
    for (def row: data.getAllCached()) {
        if (row.getAlias() == 'total') {
            return true
        }
    }
    return false
}

/**
 * Получение региона по коду ОКАТО
 * @param okato
 */
def getRegionByOkatoOrg(okatoCell){
    /*
    * первые две цифры проверяемого кода ОКАТО
    * совпадают со значением поля «Определяющая часть кода ОКАТО»
    * справочника «Коды субъектов Российской Федерации»
    */
    // провайдер для справочника - Коды субъектов Российской Федерации
    def okato =  getRefBookValue(3, okatoCell, "OKATO")
    def  refDataProvider = refBookFactory.getDataProvider(4)
    def records = refDataProvider.getRecords(new Date(), null, "OKATO_DEFINITION like '"+okato.toString().substring(0, 2)+"%'", null).getRecords()

    if (records.size() == 1){
        return records.get(0);
    } else if (records.size() == 0){
        logger.error("Не удалось определить регион по коду ОКАТО")
        return null;
    } else{
        /**
         * Если первые пять цифр кода равны "71140" то код ОКАТО соответствует
         * Ямало-ненецкому АО (код 89 в справочнике «Коды субъектов Российской Федерации»)
         */
        def reg89 = records.find{ it.OKATO.toString().substring(0, 4).equals("71140")}
        if (reg89 != null) return reg89;

        /**
         * Если первые пять цифр кода равны "71100" то
         * код ОКАТО соответствует Ханты-мансийскому АО
         * (код 86 в справочнике «Коды субъектов Российской Федерации»)
         */
        def reg86 = records.find{ it.OKATO.toString().substring(0, 4).equals("71100")}
        if (reg86 != null) return reg86;

        /**
         * Если первые четыре цифры кода равны "1110"
         * то код ОКАТО соответствует Ненецкому АО
         * (код 83 в справочнике «Коды субъектов Российской Федерации»)
         */
        def reg83 = records.find{ it.OKATO.toString().substring(0, 4).equals("1110")}
        if (reg83 != null) return reg83;

        logger.error("Не удалось определить регион по коду ОКАТО")
        return null;
    }
}

/**
 * Получение значения (разменовываение)
 */
def getRefBookValue(refBookID, recordId, alias){
    def  refDataProvider = refBookFactory.getDataProvider(refBookID)
    def records = refDataProvider.getRecordData(recordId)

    return records != null ? records.get(alias) : null;
}

/**
 * Консолидация формы
 * Собирает данные с консолидированных нф
 */
def consolidation(){
    // очистить форму
    def dataRowHelper = getData(formData)
    def dataRows = List<DataRow<Cell>>()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            def sourceDataRowHelper = formDataService.getDataRowHelper(source)
            def sourceDataRows = sourceDataRowHelper.allCached
            sourceDataRows.each{ sRow ->
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
                newRow.taxBaseOkeiUnit = sRow.powerVal
                // «Графа 10» принимает значение «графы 8» формы-источника
                newRow.ecoClass = sRow.ecoClass


                /**
                 * «Графа 11» Рассчитывается автоматически по формуле:
                 * Если («отчётный год YYYY» – «Графа 12» (формы-источника) – 1) <= 0
                 * То
                 * «Графа 11»  = 0
                 * Иначе
                 * «Графа 11»  = «отчётный год YYYY» – «Графа 12» (формы-источника) – + 1
                 */
                def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
                def taxPeriod = taxPeriodService.get(reportPeriod.taxPeriodId)
                Calendar cl = Calendar.getInstance()
                cl.setTime(taxPeriod.startDate);
                def year = cl.get(Calendar.YEAR)
                def diff = year - sRow.year - 1
                newRow.years = diff <= 0 ? 0:diff

                /*
                 * Рассчитывается автоматически по формуле:
                 * «Графа 12» =  ПОЛНЫХ_МЕСЯЦЕВ[(«Графа 14» (формы-источника) - «Графа 13» (формы-источника)) – (B-A)], где
                 * B вычисляется согласно алгоритму:
                 * Если  «графа 16» (формы-источника)> «графа 14», то
                 * B=«графа 14»
                 * Иначе
                 * B= «графа 16» (формы-источника)
                 * A вычисляется согласно алгоритму:
                 * Если  «графа 15» (формы-источника)<«графа 13», то
                 * B=«графа 13»
                 * Иначе
                 * B= «графа 15» (формы-источника)
                 * ПОЛНЫХ_МЕСЯЦЕВ[] – операция получения количества полных месяцев.  Срок в месяцах округляется до наибольшего значения.   То есть, если рассчитанный срок владения равен 6,5 мес, операция должна возвратить значение 7
                 */

                // TODO http://jira.aplana.com/browse/SBRFACCTAX-3714
                //newRow.ownMonths =  TimeCategory.minus(new Date(), new Date()).months

                dataRows.add(newRow)
            }
        }
    }
    // сохраняем данные
    dataRowHelper.save(dataRows)
    dataRowHelper.commit()
}

// графа 1  - rowNumber
// графа 2  - okato
// графа 3  - tsTypeCode
// графа 4  - tsType
// графа 5  - vi
// графа 6  - model
// графа 7  - regNumber
// графа 8  - taxBase
// графа 9  - taxBaseOkeiUnit
// графа 10 - ecoClass
// графа 11 - years
// графа 12 - ownMonths
// графа 13 - coef362
// графа 14 - taxRate
// графа 15 - calculatedTaxSum
// графа 16 - taxBenefitCode
// графа 17 - benefitStartDate
// графа 18 - benefitEndDate
// графа 19 - coefKl
// графа 20 - benefitSum
// графа 21 - taxSumToPay


/**
 * Графы
 * 1 № пп  -  rowNumber
 * 2 Код ОКАТО  -  codeOKATO
 * 3 Муниципальное образование, на территории которого зарегистрировано транспортное средство (ТС)  -  regionName
 * 4 Код вида ТС  -  tsTypeCode
 * 5 Вид ТС  -  tsType
 * 6 Идентификационный номер  -  identNumber
 * 7 Марка  -  model
 * 8 Экологический класс  -  ecoClass
 * 9 Регистрационный знак  -  regNumber
 * 10 Мощность (величина)  -  powerVal
 * 11 Мощность (ед. измерения)  -  baseUnit
 * 12 Год изготовления  -  year
 * 13 Регистрация (дата регистрации)  -  regDate
 * 14 Регистрация (дата снятия с регистрации)  -  regDateEnd
 * 15 Сведения об угоне (дата начала розыска ТС)  -  stealDateStart
 * 16 Сведения об угоне (дата возврата ТС)  -  stealDateEnd
 *
 * ['rowNumber', 'codeOKATO', 'regionName', 'tsTypeCode', 'tsType', 'identNumber', 'model', 'ecoClass', 'regNumber', 'powerVal', 'baseUnit', 'year', 'regDate', 'regDateEnd', 'stealDateStart', 'stealDateEnd']
 */
