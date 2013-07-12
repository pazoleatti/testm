/**
 * Форма "Расчет суммы налога по каждому транспортному средству".
 */

switch (formDataEvent) {
// создать
    case FormDataEvent.CREATE :
        checkBeforeCreate()
        calculationTotal()
        break
// расчитать
    case FormDataEvent.CALCULATE :
        deleteTotal()
        fillForm()
        determinationTransportType()
        checkRequiredField()
        checkNSI()
        logicalChecks()
        sort()
        calculationTotal()
        setRowIndex()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        sort()
        break
// проверить
    case FormDataEvent.CHECK :
        checkRequiredField()
        deleteTotal()
        determinationTransportType()
        fillForm()
        checkNSI()
        logicalChecks()
        sort()
        calculationTotal()
        setRowIndex()
        break
// утвердить
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :
        logicalChecks()
        checkNSI()
        break
// принять из утверждена
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED :
        logicalChecks()
        checkNSI()
        break
// принять из создана
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :
        logicalChecks()
        checkNSI()
        break
// вернуть из принята в создана
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED :
        checkToCancelAccept()
        break
    case FormDataEvent.ADD_ROW :
        deleteTotal()
        addRow()
        calculationTotal()
        setRowIndex()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

/**
 * Скрипт для добавления новой строки.
 */
void addRow() {
    def row = formData.createDataRow()
    formData.dataRows.add(row)
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

    // подготовка названии колонок по которым будут производиться подсчеты
    def columns = ['calculatedTaxSum', 'benefitSum', 'taxSumToPay']
    def sums = []

    // подсчет сумм
    def rowCount = formData.dataRows.size();
    if (rowCount > 0) {
        columns.collect(sums) {
            [it, summ(formData, new ColumnRange(it, 0, rowCount - 1))]
        }
    }

    // добавление строки ИТОГО
    def totalRow = formData.appendDataRow('total');
    totalRow.tsType = 'ИТОГО:'

    // вставка подсчитанных сумм в строку ИТОГО
    sums.each {
        totalRow[it[0]] = it[1]
    }
}

/**
 * Скрипт для проверки создания.
 *
 * @since 15.02.2013 18:20
 */
void checkBeforeCreate() {
    def findForm = FormDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.');
    }

    if (formData.kind != FormDataKind.SUMMARY) {
        logger.error("Нельзя создавать форму с типом ${formData.kind?.name}");
    }

    if (formDataDepartment.type == DepartmentType.ROOT_BANK) {
        logger.error('Нельзя создавать форму на уровне банка.')
    }
}

/**
 * Скрипт для проверки соответствия НСИ.
 */
void checkNSI() {
    for (def row : formData.dataRows) {
        if (row.getAlias() == 'total') {
            continue
        }

        // Проверка совпадения ОКАТО со справочным
        if (row.okato != null) {
            if (!transportTaxDao.validateOkato(row.okato)) {
                logger.error('Неверный код ОКАТО');
            }
        }

        // Проверка совпадения кода вида ТС со справочным
        if (row.tsTypeCode != null) {
            if(transportTaxDao.validateTransportTypeCode(row.tsTypeCode)) {
                // Проверка наименования вида ТС коду вида ТС
                if (transportTaxDao.getTsTypeName(row.tsTypeCode) != row.tsType) {
                    logger.error('Название вида ТС не совпадает с Кодом вида ТС');
                }
            } else {
                logger.error('Неверный код вида транспортного средства!');
            }
        }



        // Проверка совпадения единицы измерения налоговой базы по ОКЕИ со справочной
        if (row.taxBaseOkeiUnit != null) {
            if (!transportTaxDao.validateTaxBaseUnit(row.taxBaseOkeiUnit)) {
                logger.error('Недопустимый код единицы измерения налоговой базы.');
            }
        }

        // Проверка совпадения экологического класса со справочным
        if (row.ecoClass!=null) {
            if(!transportTaxDao.validateEcoClass(row.ecoClass)) {
                logger.error('Недопустимый экологический класс');
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
void checkRequiredField() {
    for (def row : formData.dataRows) {
        if (row.getAlias() == 'total') {
            continue
        }

        def errorMsg = '';

        // 2, 3, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 20 , 21
        ['okato', 'tsTypeCode', 'vi', 'model', 'regNumber', 'taxBase', 'taxBaseOkeiUnit', 'ownMonths','coef362', 'calculatedTaxSum', 'taxSumToPay'].each {
            // Тут у меня непонятки при мерже произошли. Старая строка закомментирована на всякий
            // ['okato', 'tsTypeCode', 'vi', 'model', 'regNumber', 'taxBase', 'taxBaseOkeiUnit', 'ownMonths','coef362', 'taxRate', 'calculatedTaxSum', 'benefitSum', 'taxSumToPay'].each {
            if (row.getCell(it) != null && (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue()))) {
                errorMsg += (!''.equals(errorMsg) ? ', ' : '') + '"' + row.getCell(it).getColumn().getName() + '"'
            }
        }
        if (!''.equals(errorMsg)) {
            logger.error("Не заполнены поля в колонках : $errorMsg.")
        }
    }
}

/*
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
    def row = currentDataRow
    if (row != null && row.getAlias() != 'total'){
        // удаление строки
        formData.deleteDataRow(row)

        // пересчет номеров строк
        def n = 1
        formData.getDataRows().each{ r ->
            r.rowNumber = n++
        }
    }
}

/**
 * Скрипт для удаления строки ИТОГО.
 *
 * @author rtimerbaev
 */
void deleteTotal() {
    def row = (formData.dataRows.size() > 0 ? formData.getDataRow('total') : null)
    if (row != null) {
        formData.getDataRows().remove(row)
    }
}

/**
 * Определение наименования типа транспортного средства
 * Скрипт для получения названия вида транспортного средства по коду ТС.
 */
void determinationTransportType() {
    formData.dataRows.each { row ->
        if (row.tsTypeCode != null){
            row.tsType = transportTaxDao.getTsTypeName(row.tsTypeCode)
        }
    }
}

/**
 * 2. Алгоритмы заполнения полей формы (9.1.1.8.1) Табл. 45.
 *
 * @author auldanov
 * @since 24.02.2013 14:00
 */
void fillForm() {
    /** Число полных месяцев в текущем периоде (либо отчетном либо налоговом). */
    int monthCountInPeriod = 0
    def period = reportPeriodService.get(formData.reportPeriodId)
    if (period == null) {
        info.error('Не найден отчетный период для налоговой формы.')
    } else {
        monthCountInPeriod = period.getMonths()
    }

    /** Уменьшающий процент. */
    def reducingPerc = 1 // TODO (Ramil Timerbaev)
    /** Пониженная ставка. */
    def loweringRates = 0 // TODO (Ramil Timerbaev)

    def index = 1
    formData.dataRows.each { row ->
        // получение региона по ОКАТО
        def region = dictionaryRegionService.getRegionByOkatoOrg(row.okato)

        // получение параметров региона
        if (row.taxBenefitCode){
            def taxBenefitParam = dictionaryTaxBenefitParamService.get(region.code, row.taxBenefitCode)
            if (taxBenefitParam == null){
                logger.error('Ошибка при получении параметров региона')
            }
            else{
                reducingPerc = taxBenefitParam.percent
                loweringRates = taxBenefitParam.rate
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
            row.taxBaseOkeiUnit = 251
        }


        /*
         * Графа 13 - Коэффициент Кв
         * Скрипт для вычисления автоматических полей 13, 19, 20
         */
        if (row.ownMonths != null) {
            row.coef362 = row.ownMonths / monthCountInPeriod
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
            row.taxRate = transportTaxDao.getTaxRate(row.tsTypeCode, row.years, row.taxBase, region.code)
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
            if (row.taxBenefitCode == '20210' || row.taxBenefitCode == '30200') {
                row.benefitSum = round(row.taxBase * row.coefKl * row.taxRate, 0)
            } else if (row.taxBenefitCode == '20220') {
                row.benefitSum = round(row.taxBase * row.taxRate * row.coefKl * reducingPerc / 100, 0)
            } else if (row.taxBenefitCode == '20230') {
                row.benefitSum = round(row.coefKl * row.taxRate * (row.taxRate - loweringRates), 0)
            } else {
                row.benefitSum = 0
            }
        }

        /*
         * Графа 21 - Исчисленная сумма налога, подлежащая уплате в бюджет.
         * Скрипт для вычисления значения столбца "Исчисленная сумма налога, подлежащая уплате в бюджет".
         */
        if (row.calculatedTaxSum != null && row.benefitSum != null) {
            row.taxSumToPay = round(row.calculatedTaxSum - row.benefitSum, 0)
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
}

/**
 * Скрипт логические проверки сводной формы.
 *
 * @since 18.02.2013 14:00
 */
void logicalChecks() {
    for (def row : formData.dataRows) {
        if (row.getAlias() == 'total') {
            continue
        }

        /** Число полных месяцев в текущем периоде (либо отчетном либо налоговом). */
        int monthCountInPeriod = 0;
        def period = reportPeriodService.get(formData.reportPeriodId);
        if (period == null) {
            info.error('Не найден отчетный период для налоговой формы.');
        } else {
            monthCountInPeriod = period.getMonths();
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
                logger.error('Коэффициент Кв меньше нуля.');
            } else if (row.coef362 > 1.0) {
                logger.error('Коэффициент Кв больше единицы.');
            }
        }

        // 16 графа - Проверка Коэффициент Кл
        //logger.info('kl = ' + row.coefKl)
        if (row.coefKl != null) {
            if (row.coefKl < 0.0){
                logger.error('Коэффициент Кл меньше нуля.');
            } else if (row.coefKl > 1.0) {
                logger.error('Коэффициент Кл больше единицы.');
            }
        }

        // 17 графа - Проверка заполнения полей льгот
        // все ячейки заполнены
        def allCellsFill = true
        // все ячейки пустые
        def allCellsEmpty = true
        ['benefitStartDate', 'benefitEndDate', 'coefKl', 'benefitSum', 'taxBenefitCode'].each{
            if (row[it]){
                allCellsEmpty = false
            } else {
                allCellsFill = false
            }
        }
        if (!(allCellsFill || allCellsEmpty)) {
            logger.error("Данные о налоговой льготе указаны не полностью в строке № "+row.rowNumber);
        }

        // дополнительная проверка для 12 графы
        if (row.ownMonths != null && row.ownMonths > monthCountInPeriod) {
            logger.warn('Срок владение ТС не должен быть больше текущего налогового периода.')
        }
    }
}

/**
 * Установка номера строки.
 *
 * @author rtimerbaev
 * @since 20.02.2013 13:00
 */
void setRowIndex() {
    def index = 1
    for (def row : formData.dataRows) {
        if (row.getAlias() == 'total') {
            continue
        }
        row.rowNumber = index + 1
        index += 1
    }
}

/**
 * Скрипт для сортировки.
 */
void sort() {
    // сортировка
    formData.dataRows.sort { a, b ->
        int val = (a.okato ?: "").compareTo(b.okato ?: "")
        if (val == 0) {
            val = (a.tsTypeCode?: "").compareTo(b.tsTypeCode ?: "")
        }
        return val;
    }
}