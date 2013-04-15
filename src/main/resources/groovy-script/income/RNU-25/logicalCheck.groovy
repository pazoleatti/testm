/**
 * Логические проверки. Проверки соответствия НСИ (logicalCheck.groovy).
 * Форма "(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения".
 *
 * TODO:
 *		- проверки не доделаны, потому что возможно они поменяются
 *
 * @author rtimerbaev
 */

/** Предыдущий отчётный период. */
def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

/** Данные нф за предыдущий отчетный период. */
def formDataOld = null
if (reportPeriodOld != null) {
    formDataOld = FormDataService.find(325, FormDataKind.PRIMARY, formData.departmentId, reportPeriodOld.id)
}

if (!formData.dataRows.isEmpty()) {
    def index = 1
    for (def row : formData.dataRows) {
        if (isTotal(row)) {
            continue
        }

        // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 6, 13)
        if (row.lotSizeCurrent == 0 && row.reserve != row.reserveRecovery) {
            logger.warn('Графы 6 и 13 неравны!')
            break
        }

        // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 7, 10, 11)
        if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
            logger.warn('Графы 7, 10 и 11 ненулевые!')
            break
        }

        // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 4, 6, 13)
        if (row.lotSizePrev == 0 && (row.reserve != 0 || row.reserveRecovery != 0)) {
            logger.error('Графы 6 и 13 ненулевые!')
            break
        }

        // 5. Проверка необращающихся акций (графа 8, 11, 12)
        if (row.signSecurity == 'x' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
            logger.warn('Облигации необращающиеся, графы 11 и 12 ненулевые!')
            break
        }

        // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        if (row.signSecurity == '+' && row.reserveCalcValue - row.reserve > 0 && row.reserveRecovery != 0) {
            logger.error('Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
            break
        }

        // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 12)
        if (row.signSecurity == '+' && row.reserveCalcValue - row.reserve < 0 && row.reserveCreation != 0) {
            logger.error('Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
            break
        }

        // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        if (row.signSecurity == '+' && row.reserveCalcValue - row.reserve == 0 &&
                (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
            logger.error('Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
            break
        }

        // 9. Проверка на положительные значения при наличии созданного резерва
        if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
            logger.warn('Резерв сформирован. Графы 5, 7, 10 и 11 неположительные!')
            break
        }

        // 10. Проверка корректности формирования резерва (графа 6, 11, 12, 13)
        if (row.reserve + row.reserveCreation == row.reserveCalcValue + row.reserveRecovery) {
            logger.error('Резерв сформирован неверно!')
            break
        }

        if (formDataOld != null) {
            totalRow = formData.getDataRow('total')
            totalRowOld = formDataOld.getDataRow('total')

            // 1. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 11) в текущем отчетном периоде (выполняется один раз для всего экземпляра)
            /*
                   Если «графа 11» > 0 формы «Регистр налогового учёта доходов по выданным гарантиям» за предыдущий отчётный период, то
                   проверяется, есть ли запись с таким же номером сделки (графа 3) в текущем периоде  («графа 3» = «графа 3» для предыдущего отчётного периода)
                   0
                   •	Если записи нет:
                   Отсутствуют строки с номерами сделок : <список номеров сделок>!
                   •	Если записей несколько:
                   Существует несколько строк с номерами сделок: <список номеров сделок>!
                   (сообщения появляются в протоколе ошибок после всех строк)
               */
            if (totalRowOld.reserveCalcValue > 0 && totalRow.tradeNumber != totalRowOld.tradeNumber) {
                logger.warn('') // TODO (Ramil Timerbaev)
                break
            }

            // 11. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 4, 5 (за предыдущий период) )
            // TODO (Ramil Timerbaev) про эту проверку Карина уточнит
            if (totalRow.tradeNumber == totalRowOld.tradeNumber && totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                logger.error('РНУ сформирован некорректно!.')
                break
            }

            // 12. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 6, 11 (за предыдущий период) )
            // TODO (Ramil Timerbaev) про эту проверку Карина уточнит
            if (totalRow.tradeNumber == totalRowOld.tradeNumber && totalRow.reserve != totalRowOld.reserveCalcValue) {
                logger.error('РНУ сформирован некорректно!')
                break
            }

            // 13. Проверка корректности заполнения РНУ (графа 4, 5 (за предыдущий период))
            if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                logger.error('РНУ сформирован некорректно!')
                break
            }

            // 14. Проверка корректности заполнения РНУ (графа 6, 11 (за предыдущий период))
            if (totalRow.reserve != totalRowOld.reserveCalcValue) {
                logger.error('РНУ сформирован некорректно!')
                break
            }
        }

        // 15. Проверка на заполнение поля «<Наименование поля>». Графа 1..3, 5..13
        def colNames = []
        // Список проверяемых столбцов
        ['regNumber', 'tradeNumber', 'lotSizeCurrent', 'reserve', 'cost',
                'signSecurity', 'marketQuotation', 'costOnMarketQuotation',
                'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each {
            if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
            }
        }
        if (!colNames.isEmpty()) {
            def errorMsg = colNames.join(', ')
            logger.error("Поля $errorMsg не заполнены!")
            break
        }

        // 16. Проверка на уникальность поля «№ пп» (графа 1)
        if (index != row.rowNumber) {
            logger.error('Нарушена уникальность номера по порядку!')
            break
        }
        index += 1
    }
}

/*
 * Вспомогалельные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}