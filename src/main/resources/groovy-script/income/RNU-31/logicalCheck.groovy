/**
 * Логические проверки. Проверки соответствия НСИ (logicalCheck.groovy).
 * Форма "(РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям".
 *
 * TODO:
 *		- для проверки 1 нет условия (не ясно как получать предыдущий отчет)
 *		- как определить первый это отчет?
 *
 * @author rtimerbaev
 */

/** Данные предыдущего отчета. */
def formDataOld = null // TODO (Ramil Timerbaev) как получить?

/** Строка из предыдущего отчета. */
def rowOld = (formDataOld != null && !formDataOld.dataRows.isEmpty() ? formDataOld.dataRows.get(0) : null)

/** Строка из текущего отчета. */
def row = (formData != null && !formData.dataRows.isEmpty() ? formData.dataRows.get(0) : null)

// 22. Проверка объязательных полей. Обязательность заполнения поля графы (с 1 по 12)
def colNames = []
// Список проверяемых столбцов 1..12
def requiredColumns = ['ofz', 'municipalBonds',
        'governmentBonds', 'mortgageBonds', 'municipalBondsBefore', 'rtgageBondsBefore',
        'ovgvz', 'eurobondsRF', 'itherEurobonds', 'corporateBonds']

requiredColumns.each {
    if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
        colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
    }
}
if (!colNames.isEmpty()) {

    def errorMsg = colNames.join(', ')
    logger.error("Поля: \"$errorMsg\" не заполнены!")
}

/** Графы для которых тип ошибки нефатальный (графа 5, 9, 10, 11). */
def warnColumns = ['governmentBonds', 'ovgvz', 'eurobondsRF', 'itherEurobonds']

// TODO (Ramil Timerbaev) добавить проверку "начиная с отчета за февраль"
if (false) {
    // 1. Проверка наличия предыдущего экземпляра отчета
    if (rowOld == null) {
        logger.error('Отсутствует предыдущий экземпляр отчета')
        return
    }

    // 2..11 Проверка процентного (купонного) дохода по видам валютных ценных бумаг (графы 3..12)
    for (def column : requiredColumns) {
        if (row.getCell(column).getValue() < rowOld.getCell(column).getValue()) {
            def securitiesType = row.securitiesType
            def message = "Процентный (купонный) доход по $securitiesType уменьшился!"
            if (column in warnColumns) {
                logger.warn(message)
            } else {
                logger.error(message)
            }
            break
        }
    }
}

// 12..21 Проверка на неотрицательные значения (графы 3..12)
for (def column : requiredColumns) {
    if (row.getCell(column).getValue() < 0) {
        def columnName = row.getCell(column).getColumn().getName()
        def message = "Значения графы \"$columnName\" по строке 1 отрицательное!"
        if (column in warnColumns) {
            logger.warn(message)
        } else {
            logger.error(message)
        }
        break
    }
}