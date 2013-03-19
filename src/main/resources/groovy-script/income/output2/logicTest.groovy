/**
 * ЧТЗ выходные налоговые формы Ф2 Э1-2 П6.3.2.9.2.1
 * Применяется для каждой строки
 * @author ekuvshinov
 * @since 08.02.2013
 */

//com.aplana.sbrf.taxaccounting.log.Logger logger

void setError(String cellName) {

    if (!cellName.empty) {
        logger.error('Поле ' + cellName.replace('%', '') + ' не заполнено')
    }
}

for (alias in ['title', 'subdivisionRF', 'surname', 'name', 'dividendDate', 'sumDividend', 'sumTax']) {
//noinspection GroovyVariableNotAssigned
    if (row.getCell(alias).value == null) {
        setError(row.getCell(alias).column.name)
    }
}

String zipCode = (String) row.zipCode;
if (zipCode == null || zipCode.length() != 6 || !zipCode.matches('[0-9]*')) {
    logger.error('Неправильно указан почтовый индекс (формат: ××××××)!')
}
if (!logger.containsLevel(LogLevel.ERROR)) {
    if (!dictionaryRegionService.isValidCode(row.subdivisionRF.intValue())) {
        logger.error('Неверное наименование субъекта РФ!')
    }
}