/**
 * Логические проверки. Проверки соответствия НСИ (logicalCheck.groovy).
 * Форма "(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения".
 *
 * TODO:
 *		- проверки не доделаны, потому что возможно они поменяются
 *
 * @author rtimerbaev
 */

/** Предыдущий отчётный период. */
def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

/** Данные формы предыдущего отчетного периода. */
def formDataOld = null
if (reportPeriodOld != null) {
    FormDataService.find(325, FormDataKind.PRIMARY, formData.departmentId, reportPeriodOld.id)
}

if (!formData.dataRows.isEmpty()) {
    def index = 1
    for (def row : formData.dataRows) {
        if (isTotal(row)) {
            continue
        }

        // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 8, 17)
        if (row.lotSizeCurrent == 0 && row.reserveCalcValuePrev != row.reserveRecovery) {
            logger.warn('Графы 8 и 17 неравны!')
            break
        }

        // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 9, 14, 15)
        if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
            logger.warn('Графы 9, 14 и 15 ненулевые!')
            break
        }

        // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6, 8, 17)
        if (row.lotSizePrev == 0 && (row.reserveCalcValuePrev != 0 || row.reserveRecovery != 0)) {
            logger.error('Графы 8 и 17 ненулевые!')
            break
        }

        // 5. Проверка необращающихся акций (графа 10, 15, 16)
        if (row.signSecurity == 'x' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
            logger.warn('Акции необращающиеся, графы 15 и 16 ненулевые!')
            break
        }

        // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
        if (row.signSecurity == '+' && row.reserveCalcValue - row.reserveCalcValuePrev > 0 && row.reserveRecovery != 0) {
            logger.error('Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
            break
        }

        // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 16)
        if (row.signSecurity == '+' && row.reserveCalcValue - row.reserveCalcValuePrev < 0 && row.reserveCreation != 0) {
            logger.error('Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
            break
        }

        // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
        if (row.signSecurity == '+' && row.reserveCalcValue - row.reserveCalcValuePrev == 0 &&
                (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
            logger.error('Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
            break
        }

        // 9. Проверка корректности формирования резерва (графа 8, 15, 16, 17)
        if (row.reserveCalcValuePrev + row.reserveCreation == row.reserveCalcValue + row.reserveRecovery) {
            logger.error('Резерв сформирован неверно!')
            break
        }

        // 10. Проверка на положительные значения при наличии созданного резерва
        if (row.reserveCreation > 0 && row.lotSizeCurrent > 0 && row.cost > 0 &&
                row.costOnMarketQuotation > 0 && row.reserveCalcValue > 0) {
            logger.warn('Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!')
            break
        }

        if (formDataOld != null) {
            totalRow = formData.getDataRow('total')
            totalRowOld = formDataOld.getDataRow('total')

            // 1. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде 
            /*
               Если «графа 15» формы «Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения» за предыдущий отчётный период > 0, то «графа  4» = «графа 4» формы «Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения» за предыдущий отчётный период	0	•	Если записи нет:
               Отсутствуют строки с номерами сделок : <список номеров сделок>!
               •	Если записей несколько:
               Существует несколько строк с номерами сделок: <список номеров сделок>!
               (сообщения  появляются в протоколе ошибок после всех строк)
               */
            if (totalRowOld.reserveCalcValue > 0 && totalRow.tradeNumber != totalRowOld.tradeNumber) {
                logger.warn('') // TODO (Ramil Timerbaev)
                break
            }

            // 11. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 6, 7 (за предыдущий период) )
            // TODO (Ramil Timerbaev) про эту проверку Карина уточнит
            if (totalRow.tradeNumber == totalRowOld.tradeNumber && totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                def curCol = 4
                def curCol2 = 4
                def prevCol = 4
                def prevCol2 = 7
                logger.warn("РНУ сформирован некорректно! Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы «Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения» за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы «Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения» за предыдущий отчётный период.")
                break
            }

            // 12. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 8, 15 (за предыдущий период) )
            // TODO (Ramil Timerbaev) про эту проверку Карина уточнит
            if (totalRow.tradeNumber == totalRowOld.tradeNumber && totalRow.reserveCalcValuePrev != totalRowOld.reserveCalcValue) {
                def curCol = 4
                def curCol2 = 4
                def prevCol = 8
                def prevCol2 = 15
                logger.error("РНУ сформирован некорректно! Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы «Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения» за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы «Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения» за предыдущий отчётный период.")
                break
            }

            // 13. Проверка корректности заполнения РНУ (графа 6, 7 (за предыдущий период))
            if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                def curCol = 6
                def prevCol = 7
                logger.error("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе $curCol = «Итого» по графе $prevCol формы «Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения» за предыдущий отчётный период.")
                break
            }


            // 14. Проверка корректности заполнения РНУ (графа 8, 15 (за предыдущий период))
            if (totalRow.cost != totalRowOld.reserveCalcValue) {
                def curCol = 8
                def prevCol = 15
                logger.error("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе $curCol = «Итого» по графе $prevCol формы «Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения» за предыдущий отчётный период.")
                break
            }
        }

        // 15. Проверка на заполнение поля «<Наименование поля>». Графа 1..3, 5..10, 13, 14
        def colNames = []
        // Список проверяемых столбцов
        ['issuer', 'shareType', 'currency', 'lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
                'cost', 'signSecurity', 'marketQuotationInRub', 'costOnMarketQuotation'].each {
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