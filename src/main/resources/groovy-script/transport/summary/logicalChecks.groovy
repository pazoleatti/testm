/*
 * Условия выполнени скрипта
 */
row.getAlias() != 'total'



/**
 * Скрипт логические проверки сводной формы.
 * "Расчет суммы налога по каждому транспортному средству".
 *
 * @since 18.02.2013 14:00
 */

/** Число полных месяцев в текущем периоде (либо отчетном либо налоговом). */
int monthCountInPeriod = 0;
def period = reportPeriodService.get(formData.reportPeriodId);
if (period == null) {
    info.error('Не найден отчетный период для налоговой формы.');
} else {
    monthCountInPeriod = period.getMonths();
}


// 13 графа - Поверка на соответствие дат использования льготы
if (row.benefitEndDate != null && (row.benefitStartDate == null || row.benefitStartDate > row.benefitEndDate)) {
    logger.error('Дата начала(окончания) использования льготы неверная!')
}

// 14 граафа - Проверка, что Сумма исчисления налога больше или равна Сумма налоговой льготы
if (row.calculatedTaxSum != null && row.benefitSum != null
        && row.calculatedTaxSum < row.benefitSum) {
    logger.error('Сумма исчисления налога меньше Суммы налоговой льготы.')
}

// 15 графа - Проверка Коэффициент Кв
logger.info('kv = ' + row.coef362)
if (row.coef362 != null) {
    if (row.coef362 < 0.0) {
        logger.error('Коэффициент Кв меньше нуля.');
    } else if (row.coef362 > 1.0) {
        logger.error('Коэффициент Кв больше единицы.');
    }
}

// 16 графа - Проверка Коэффициент Кл
logger.info('kl = ' + row.coefKl)
if (row.coefKl != null) {
    if (row.coefKl < 0.0){
        logger.error('Коэффициент Кл меньше нуля.');
    } else if (row.coefKl > 1.0) {
        logger.error('Коэффициент Кл больше единицы.');
    }
}

// 17 графа - Проверка заполнения полей льгот
if (
        (row.taxBenefitCode == null || row.benefitStartDate == null || row.benefitEndDate == null || row.coefKl == null || row.benefitSum == null) &&
                (row.taxBenefitCode != null || row.benefitStartDate != null || row.benefitEndDate != null || row.coefKl != null || row.benefitSum != null)
) {
    logger.warn('Данные о налоговой льготе указаны неполностью.')
}

// дополнительная проверка для 12 графы (Ramil Timerbaev)
if (row.ownMonths != null && row.ownMonths > monthCountInPeriod) {
    logger.warn('Срок владение ТС не должен быть больше текущего налогового периода.')
}
