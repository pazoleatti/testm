/**
 * ЧТЗ выходные налоговые формы Ф2 Э1-2 П6.3.1.9.1	Алгоритмы заполнения полей формы
 * @author ekuvshinov
 * @since 11.02.2013
 */
//com.aplana.sbrf.taxaccounting.log.Logger logger
//com.aplana.sbrf.taxaccounting.model.FormData formData

//noinspection GroovyVariableNotAssigned
def row = formData.appendDataRow()
row.taxPeriod = 34
row.dividendType = 2