(function () {
    "use strict";
    /**
     * Модуль с утилитными методами для валидации полей
     */
    angular.module('app.validationUtils', [])
        .factory('ValidationUtils', [function () {

            // Отбрасывает часы, минуты, секунды, миллисекунды
            var truncHMS = function (date) {
                if (!_.isUndefined(date) && !_.isNull(date) && date !== "") {
                    date.setHours(0);
                    date.setMinutes(0);
                    date.setSeconds(0);
                    date.setMilliseconds(0);
                    return date.getTime();
                }
                return date;
            };
            // Отбрасывает секунды и миллисекунды
            var truncS = function (date) {
                if (!_.isUndefined(date) && !_.isNull(date) && date !== "") {
                    date.setSeconds(0);
                    date.setMilliseconds(0);
                    return date.getTime();
                }
                return date;
            };

            var patternValidate = function (value, pattern) {
                if (angular.isString(value) && pattern && pattern.test) {
                    return pattern.test(value);
                } else {
                    return true;
                }
            };

            return {
                /**
                 * Валидатор диапазона дат. Проверяет, что стартовая дата не превышает конечную
                 *
                 * @param start    дата начала диапазона
                 * @param end      дата конца диапазона
                 * @param withTime учет времени
                 * @returns {boolean} признак корректности диапазона дат
                 */
                checkDateInterval: function (start, end, withTime) {
                    return (start === undefined || start === null || start === "") ||
                        (end === undefined || end === null || end === "") ||
                        (
                            withTime ? (truncS(new Date(start)) <= truncS(new Date(end))) :
                                (truncHMS(new Date(start)) <= truncHMS(new Date(end)))
                        );
                },
                /**
                 * Валидатор. Дата лежит в диапазоне.
                 *
                 * @param date     проверяемая дата
                 * @param start    дата начала диапазона
                 * @param end      дата конца диапазона
                 * @param withTime учет времени
                 * @returns {boolean} признак попадания в диапазон
                 */
                checkDateInInterval: function (date, start, end, withTime) {
                    return (start === undefined || start === null || start === "") ||
                        (end === undefined || end === null || end === "") ||
                        (date === undefined || date === null || date === "") ||
                        (
                            withTime ? ((truncS(new Date(date)) <= truncS(new Date(end))) &&
                            (truncS(new Date(start)) <= truncS(new Date(date))) ) :
                                ((truncHMS(new Date(date)) <= truncHMS(new Date(end))) &&
                                (truncHMS(new Date(start)) <= truncHMS(new Date(date))) )
                        );
                },
                /**
                 * Валидатор зыкрытого интервала. Проверяет, что обе границы интервала заданы либо обе границы не указаны.
                 *
                 * @param start дата начала интервала.
                 * @param end дата конца интервала.
                 * @returns {boolean} признак закрытого интервала.
                 */
                checkClosedInterval: function (start, end) {
                    var startExist = (start !== undefined && start !== null && start !== "");
                    var endExist = (end !== undefined && end !== null && end !== "");
                    return (startExist === endExist);
                },
                /**
                 * Валидотор диапазона интервала дат в виде числа дней.
                 * Например: выводим данные за последние дни - от 5 до 10 дня.
                 *
                 * @param start начало интервала
                 * @param end   конец интервала
                 * @returns {boolean} признак корректности диапазона дат
                 */
                checkDayInterval: function (start, end) {
                    return (start === undefined || start === "" || start === null) ||
                        (end === undefined || end === "" || end === null) ||
                        (parseInt(start, 10) <= parseInt(end, 10));
                },
                /**
                 * Валидатор для ИНН юридических лиц (10 символов) с расчетом контрольного разряда.
                 * Пример ИНН: 1234567894
                 */
                innJurValidate: function (value) {
                    if (!angular.isUndefined(value) && angular.isString(value) && value.length === 10) {

                        // Проверка на все нули в ИНН
                        if (value.charAt(0) === "0" && value.charAt(1) === "0") {
                            return false;
                        }

                        var summ = 0;
                        var multiArray = [2, 4, 10, 3, 5, 9, 4, 6, 8];
                        for (var index = 0; index < 9; index++) {
                            summ += parseInt(value.charAt(index), 10) * multiArray[index];
                        }
                        return (summ % 11 % 10) === parseInt(value.charAt(9), 10);
                    } else {
                        return true;
                    }
                },
                /**
                 * Валидатор для ИНН физических лиц (12 символов) с расчетом контрольного разряда.
                 * Пример ИНН: 123456789110
                 */
                innPhysValidate: function (value) {
                    if (!angular.isUndefined(value) && angular.isString(value) && value.length === 12) {

                        // Проверка на все нули в ИНН
                        if (value.charAt(0) === "0" && value.charAt(1) === "0") {
                            return false;
                        }

                        var valid = true;
                        var summ = 0;
                        var multiArray11 = [7, 2, 4, 10, 3, 5, 9, 4, 6, 8];
                        for (var index = 0; index < 10; index++) {
                            summ += parseInt(value.charAt(index), 10) * multiArray11[index];
                        }
                        valid = valid && (summ % 11 % 10) === parseInt(value.charAt(10), 10);

                        summ = 0;
                        var multiArray12 = [3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8];
                        for (index = 0; index < 11; index++) {
                            summ += parseInt(value.charAt(index), 10) * multiArray12[index];
                        }
                        valid = valid && (summ % 11 % 10) === parseInt(value.charAt(11), 10);

                        return valid;
                    } else {
                        return true;
                    }
                },
                /**
                 * Валидатор для СНИЛС
                 * Работает только с последовательностью чисел.
                 * Для формата СНИЛС "112-233-445 95" и любых других строк возвращает true
                 */
                snilsValidate: function (value) {
                    if (!angular.isUndefined(value) && angular.isString(value) && value.length === 11 && !isNaN(value)) {
                        var summ = 0;
                        var multiArray = [9, 8, 7, 6, 5, 4, 3, 2, 1];
                        for (var index = 0; index < 9; index++) {
                            summ += parseInt(value.charAt(index), 10) * multiArray[index];
                        }
                        if (summ === 100 || summ === 101) {
                            return parseInt(value.substring(9), 10) === 0;
                        } else {
                            summ %= 101;
                            summ = summ > 100 ? 0 : summ;
                            return summ === parseInt(value.substring(9), 10);
                        }
                    } else {
                        return true;
                    }
                },
                /**
                 * Валидатор pattern. Просто проверяет соответствие RegExp шаблону
                 *
                 * @param value значение для проверки
                 * @param pattern шаблон
                 */
                patternValidate: function (value, pattern) {
                    return patternValidate(value, pattern);
                },
                /**
                 * Валидатор длины строки
                 * @param value значение для проверки
                 * @param maxlength максимальная длина
                 */
                maxlengthValidate: function (value, maxlength) {
                    if (angular.isString(value) && angular.isNumber(maxlength)) {
                        return value.length <= maxlength;
                    } else {
                        return true;
                    }
                },
                /**
                 * Валидатор пустоты, так же подходит для массивов
                 * @param value значение для проверки
                 */
                emptyValidate: function (value) {
                    return !(!value || !value.length);
                },

                /**
                 * Проверка, что проверяемый период раньше периода проведения
                 *
                 * PROC_OBJECT.START_DATE >= PROC_OBJECT.CHECK_START_DATE
                 * PROC_OBJECT.END_DATE >= PROC_OBJECT.CHECK_END_DATE
                 *
                 * @param object объект с периодами
                 */
                checkDateIntervalBefore: function (object){
                    if (!angular.isDefined(object) || !angular.isDefined(object.checkEndDate) || !angular.isDefined(object.startDate)) {
                        return true;
                    }

                    var checkEndDate = new Date(object.checkEndDate);
                    var checkStartDate = new Date(object.checkStartDate);
                    var startDate = new Date(object.startDate);
                    var endDate = new Date(object.endDate);

                    return ((checkStartDate <= startDate) && (endDate >= checkEndDate));
                },
                /**
                 * Дата попадает в квартал заданного года
                 *
                 * @param date проверяемая дата
                 * @param year год
                 * @param quarter квартал
                 */
                checkDateInYearQuarter: function(date, year, quarter) {
                    if (!date || !year || !quarter) {
                        return true;
                    }

                    date = new Date(date);

                    var currentQuarter = moment(date).utc().quarter();
                    var currentYear = date.getFullYear();

                    return parseInt(quarter) === currentQuarter && parseInt(year) === currentYear;
                },

                /**
                 * Валидатор pattern. Просто проверяет соответствие RegExp шаблону
                 *
                 * @param value     значение для проверки
                 * @param precision всего цифр
                 * @param scale     цифр после запятой
                 */
                numberPatternValidate: function (value, precision, scale) {
                    var pattern = new RegExp("^\\d{1," + (precision - scale) + "}((\\.|\\,)\\d{0," + scale + "}){0,1}$|^$");
                    return patternValidate(value, pattern);
                },

                /**
                 * Проверка того, проверяемая дата небольше текущей.
                 *
                 * @param dateValue    проверяемая дата
                 * @returns {boolean} признак корректности даты
                 */
                checkDateBefore: function (dateValue) {
                    return (dateValue === undefined || dateValue === null || dateValue === "") ||
                        (
                            truncHMS(new Date(dateValue)) <= truncHMS(new Date())
                        );
                },

                /**
                 * Проверяет, что стартовый год меньше конечного
                 * @param startYear - стартовый год
                 * @param endYear - конечный год
                 * @return boolean Признак корректности диапозона лет
                 */
                checkYearInterval : function (startYear, endYear) {
                    return (startYear === undefined || startYear === null || startYear === "") ||
                        (endYear === undefined || endYear === null || endYear === "") ||
                        (startYear <= endYear);
                },

                /**
                 * Проверяет, что дата входит в валидный интервал [01.01.1990; 31.12.2099]
                 * @param date - проверяемая дата
                 */
                checkDateValidateInterval: function (date) {
                    var minDate = new Date();
                    minDate.setMonth(1,1);
                    minDate.setYear(1990);
                    var maxDate = new Date();
                    maxDate.setMonth(1,1);
                    maxDate.setYear(2100);
                    return minDate <= date && date < maxDate;
                }
            };
        }])

    ;
}());
