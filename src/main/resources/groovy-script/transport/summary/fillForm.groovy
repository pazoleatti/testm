/**
 * 2. Алгоритмы заполнения полей формы (9.1.1.8.1) Табл. 45 (fillForm.groovy).
 * Форма "Расчет суммы налога по каждому транспортному средству".
 *
 *
 * @author auldanov
 * @since 24.02.2013 14:00
 */

/** Число полных месяцев в текущем периоде (либо отчетном либо налоговом). */
int monthCountInPeriod = 0
def period = reportPeriodService.get(formData.reportPeriodId)
if (period == null) {
    info.error('Не найден отчетный период для налоговой формы.')
} else {
    monthCountInPeriod = period.getMonths()
}

/** Уменьшающий процент. */
def reducingPerc
/** Пониженная ставка. */
def loweringRates

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

row.rowNumber = rowIndex + 1

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
    logger.error("\"Коэффициент Кл\" не может быть вычислена, т.к. поля $errors не были вычислены или заполнены.")
}

/*
 * Графа 20 - Сумма налоговой льготы (руб.)
 */
if (row.taxBenefitCode == '20210' || row.taxBenefitCode == '30200') {
    row.benefitSum = round(row.taxBase * row.coefKl * row.taxRate, 0)
} else if (row.taxBenefitCode == '20220') {
    row.benefitSum = round(row.taxBase * row.taxRate * row.coefKl * reducingPerc / 100, 0)
} else if (row.taxBenefitCode == '20230') {
    row.benefitSum = round(row.coefKl * row.taxRate * (row.taxRate - loweringRates), 0)
} else {
    row.benefitSum = 0
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