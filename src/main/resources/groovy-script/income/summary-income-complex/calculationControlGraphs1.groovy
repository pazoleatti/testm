/**
 * (calculationControlGraphs1.groovy)
 * Расчет контрольных граф (строки с КВД 400* и 600*) выполнена всоответствии с
 * Табл. 8 Расчет контрольных граф Сводной формы начисленных доходов.
 * Вызыватеся при логических проверках
 * Форма 'Сводная форма начисленных доходов (доходы сложные)'.
 *
 * TODO:
 *		1. индексы строк не использовать, переписать на алиасы
 *
 * @author auldanov
 * @since 22.02.2013 11:10
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
    // графа 12
    row.opuSumByEnclosure2 = summ(fromFormData, new ColumnRange('rnu4Field5Accepted', rowFrom, rowTo))
    // графа 13
    row.opuSumByTableD = summ(fromFormData, new ColumnRange('rnu4Field5PrevTaxPeriod', rowFrom, rowTo))
    // графа 14
    row.opuSumTotal = (row.opuSumByTableD?:0) - (row.incomeBuhSumPrevTaxPeriod?:0)
    // графа 16
    row.opuSumTotal = (row.opuSumByEnclosure2?:0) - (row.incomeTaxSumS?:0)
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
    // графа 11
    row.logicalCheck = ((BigDecimal) summ(fromFormData, new ColumnRange('rnu6Field12Accepted', rowFrom, rowTo))).setScale(2, BigDecimal.ROUND_HALF_UP)
    // графа 12
    row.opuSumByEnclosure2 = summ(fromFormData, new ColumnRange('rnu6Field10Sum', rowFrom, rowTo))
    // графа 13
    row.opuSumByTableD = (row.logicalCheck?:0) - (row.incomeBuhSumAccepted?:0)
    // графа 16
    row.opuSumTotal = (row.opuSumByEnclosure2?:0) - (row.incomeTaxSumS?:0)
}


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
    // графа 12
    row.opuSumByEnclosure2 = summ(fromFormData, new ColumnRange('rnu4Field5Accepted', 220, 221))
    // графа 16
    row.opuSumTotal = (row.opuSumByEnclosure2?:0) - (row.incomeTaxSumS?:0)
} else {
    // если дохода простого нет, то зануляем поля в которые должны были перетянуться данные

    // 40001, 40002, 40011, 40012, 40015
    ['R20', 'R38', 'R78', 'R95', 'R116'].each {
        def row = formData.getDataRow(it)
        //---40001---
        // графа 12
        row.opuSumByEnclosure2 = 0
        // графа 13
        row.opuSumByTableD = 0
        // графа 14
        row.opuSumTotal = 0
        // графа 16
        row.opuSumTotal = 0
    }
    // 60001, 60002, 60011,
    ['R21', 'R39', 'R79'].each {
        def row = formData.getDataRow(it)
        // графа 11
        row.logicalCheck = 0
        // графа 12
        row.opuSumByEnclosure2 = 0
        // графа 13
        row.opuSumByTableD = 0
        // графа 16
        row.opuSumTotal = 0
    }
    //---40016---
    formData.getDataRow('R119').opuSumByEnclosure2 = 0;
    formData.getDataRow('R119').opuSumTotal = 0;
}