/**
 * Алгоритмы консолидации данных выполнена в соответствии с
 * Табл. 9 Алгоритмы расчета ячеек, заполняемых в результате консолидации (consolidationCalc.groovy).
 * Форма "Сводная форма начисленных доходов (доходы сложные)".
 *
 * TODO:
 *		1. индексы строк не использовать, переписать на алиасы
 *
 * @author auldanov
 * @since 22.02.2013 11:30
 */


// formData для Сводная форма 'Расшифровка видов доходов, учитываемых в простых РНУ' уровня обособленного подразделения
FormData fromFormData = FormDataService.find(301, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)

/**
 * Так как заполнение строк с КВД 400* идентичны,
 * они только отличаются небольшим количеством изменяемых параметров,
 * решено было выделить в отдельную функцию.
 *
 * @param kvd поле КВД с таблицы, является алиасом для строки
 * @param rowFrom используется для вычисления суммы по строкам, задает начальную строку
 * @param rowTo для вычисления суммы по строкам, задает конечную строку
 */
def fill400xRow = {kvd, rowFrom, rowTo->
    def row = formData.getDataRow(kvd)
    def sourceRow = fromFormData.getDataRow(kvd)
    //---40001---
    // графа 7
    row.incomeBuhSumPrevTaxPeriod = sourceRow.rnu4Field5PrevTaxPeriod
    // графа 9
    row.incomeTaxSumS = sourceRow.rnu4Field5Accepted
}

/**
 * Так как заполнение строк с КВД 600* идентичны,
 * они только отличаются небольшим количеством изменяемых параметров,
 * решено было выделить в отдельную функцию.
 *
 * @param kvd поле КВД с таблицы, является алиасом для строки
 * @param rowFrom используется для вычисления суммы по строкам, задает начальную строку
 * @param rowTo для вычисления суммы по строкам, задает конечную строку
 */
def fill600xRow = {kvd, rowFrom, rowTo ->
    //---600x--
    def row = formData.getDataRow(kvd)
    def sourceRow = fromFormData.getDataRow(kvd)
    // графа 6
    row.incomeBuhSumAccepted = sourceRow.rnu6Field12Accepted
    // графа 9
    row.incomeTaxSumS = sourceRow.rnu6Field10Sum
}

/**
 * Реализация алгоритма заполнения.
 */




/**
 * Реализация алгоритма заполнения.
 */
if (fromFormData != null) {
    //---40001---
    fill400xRow( 'R20', 3, 86)
    //---60001---
    fill600xRow('R21', 3, 86)
    //---40002---
    fill400xRow('R38', 90, 93)
    //---60002---
    fill600xRow('R39', 90, 93)
    //---40011---
    fill400xRow('R78', 98 , 209)
    //---60011---
    fill600xRow('R79', 98, 209)
    //---40012---
    fill400xRow('R95', 214, 214)
    //---40015---
    fill400xRow('R116', 217, 217)

    // Для строки с КВД 40016 чуть отличается от стандартного, тем что нет манипуляций с 14 графой.
    //---40016---
    def row = formData.getDataRow('R119')
    def sourceRow = fromFormData.getDataRow('R119')
    // графа 9
    row.incomeTaxSumS = sourceRow.rnu4Field5Accepted
} else {
    // если дохода простого нет, то зануляем поля в которые должны были перетянуться данные

    // 40001, 40002, 40011, 40012, 40015
    ['R20', 'R38', 'R78', 'R95', 'R116'].each {
        def row = formData.getDataRow(it)
        //---40001---
        // графа 7
        row.incomeBuhSumPrevTaxPeriod = 0
        // графа 9
        row.incomeTaxSumS = 0
    }
    // 60001, 60002, 60011,
    ['R21', 'R39', 'R79'].each {
        def row = formData.getDataRow(it)
        //---600x--
        // графа 6
        row.incomeBuhSumAccepted = 0
        // графа 9
        row.incomeTaxSumS = 0
    }
    //---40016---
    formData.getDataRow('R119').incomeTaxSumS = 0;
}