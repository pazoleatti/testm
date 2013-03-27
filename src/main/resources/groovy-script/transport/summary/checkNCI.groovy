/**
 * Скрипт для проверки соответствия НСИ.
 * Форма "Расчет суммы налога по каждому транспортному средству".
 */

// Проверка совпадения ОКАТО со справочным
if (row.okato != null) {
    if (!transportTaxDao.validateOkato(row.okato)) {
        logger.error('Неверный код ОКАТО');
    }
}

// Проверка совпадения кода вида ТС со справочным
if (row.tsTypeCode != null) {
    if(transportTaxDao.validateTransportTypeCode(row.tsTypeCode)) {
        // Проверка наименования вида ТС коду вида ТС
        if (transportTaxDao.getTsTypeName(row.tsTypeCode) != row.tsType) {
            logger.error('Название вида ТС не совпадает с Кодом вида ТС');
        }
    } else {
        logger.error('Неверный код вида транспортного средства!');
    }
}

// Проверка поля "Регистрационный знак" на содержание только разрашенного количества символов (может содежать: до 9 символов, цифр, символы кириллицы А, В, Е, К, М, Н, О, Р, С, Т, У, Х)
if (row.regNumber != null) {
    if(!row.regNumber.matches(/[АВЕКМНОРСТУХ\d]{1,9}/)) {
        logger.error('Недопустимые символы в поле "Регистранионный знак".');
    }
}

// Проверка совпадения единицы измерения налоговой базы по ОКЕИ со справочной
if (row.taxBaseOkeiUnit != null) {
    if (!transportTaxDao.validateTaxBaseUnit(row.taxBaseOkeiUnit)) {
        logger.error('Недопустимый код единицы измерения налоговой базы.');
    }
}

// Проверка совпадения экологического класса со справочным
if (row.ecoClass!=null) {
    if(!transportTaxDao.validateEcoClass(row.ecoClass)) {
        logger.error('Недопустимый экологический класс');
    }
}