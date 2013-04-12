/**
 * Логические проверки. Проверки соответствия НСИ (logicalCheck.groovy).
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 *
 * TODO:
 *		- откуда брать "Отчетная дата"?
 *
 * @author rtimerbaev
 */

for (def row : formData.dataRows) {
    if (isTotal(row)) {
        continue
    }

    // 1. Проверка даты приобретения (открытия короткой позиции) (графа 4)
    def reportDay = new Date()
    if (row.shortPositionDate > reportDay) {
        logger.error('Неверно указана дата приобретения (открытия короткой позиции)!')
        break
    }
}

/*
 * Вспомогалельные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias() in ['total', 'A', 'B', 'totalA', 'totalB']
}