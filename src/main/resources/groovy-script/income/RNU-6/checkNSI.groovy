/**
 * Проверки соответствия НСИ (checkNSI.groovy).
 * Форма "(РНУ-6) Справка бухгалтера для отражения доходов, учитываемых в РНУ-4, учёт которых требует применения метода начисления".
 *
 * TODO:
 *		- условия проверок отсутствуют (не описаны в чтз)
 *
 * @author rtimerbaev
 */

// 1. Проверка балансового счёта для кода классификации дохода - Проверка актуальности «графы 2»
if (false) {
    logger.warn('Балансовый счёт в справочнике отсутствует!')
}

// 2. Проверка балансового счёта для кода классификации дохода - Проверка актуальности «графы 4»	0
if (false) {
    logger.warn('Балансовый счёт в справочнике отсутствует!')
}

// 3. Проверка кода классификации дохода для данного РНУ - Проверка актуальности «графы 4» на дату по «графе 3»
if (false) {
    logger.error('Операция в РНУ не учитывается!')

}
// 4. Проверка кода валюты - Проверка актуальности «графы 7»	1
if (false) {
    logger.error('Код валюты в справочнике отсутствует!')
}

// 5. Проверка курса валюты со справочным - Проверка актуальности «графы 8» на дату по «графе 3»
if (false) {
    logger.warn('Неверный курс валюты!')
}