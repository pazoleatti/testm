(function () {
    'use strict';

    /**
     * @description Модуль, содержащий форматтеры
     */

    angular.module('app.formatters', ['app.constants'])
    /**
     * @description Фильтр даты
     *
     * @param value - значение, которое необходимо отформатировать
     * @return Дата в формате 'dd.MM.yyyy'
     */
        .filter('dateFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (!value) {
                    return '';
                }
                if (!value.millis) {
                    return $filter('date')(value, 'dd.MM.yyyy');
                }
                else {
                    return $filter('date')(value.millis, 'dd.MM.yyyy');
                }
            };
        }])

        /**
         * @description Фильтр даты и времени
         *
         * @param value - значение, которое необходимо отформатировать
         * @return Дата и время в формате 'dd.MM.yyyy HH:mm:ss'
         */
        .filter('dateTimeFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (!value) {
                    return '';
                }
                if (!value.millis) {
                    return $filter('date')(value, 'dd.MM.yyyy HH:mm:ss');
                }
                else {
                    return $filter('date')(value.millis, 'dd.MM.yyyy HH:mm:ss');
                }
            };
        }])

        /**
         * @description Фильтр создаёт по коллекции объектов коллекцию их идентификаторов.
         *
         * @param items - коллекция объектов
         * @param property - имя свойства объекта, содержащее идентификатор (по-умолчанию 'id')
         * @return коллекция идентификаторов объектов
         */
        .filter('idExtractor', function () {
            return function (items, property) {
                if (!property) {
                    property = 'id';
                }

                var result = [];
                angular.forEach(items, function (item) {
                    result.push(_.property(property)(item));
                });

                return result;
            };
        })

        /**
         * @description Возвращает текст в зависимости от переданного числа
         *
         * @param num - число
         * @param nominative - единственное число
         * @param singular - множественное число от 2 до 4
         * @param plural - множественное число
         * @return Текст, склонение которого зависит от переданного числа
         */
        .filter('amountCasesFormatter', function () {
            return function (num, nominative, singular, plural) {
                var text;
                if (num > 10 && ((num % 100) / 10) === 1) {
                    return num + " " + plural;
                }
                switch (num % 10) {
                    case 1:
                        text = nominative;
                        break;
                    case 2:
                    case 3:
                    case 4:
                        text = singular;
                        break;
                    default: // case 0, 5-9
                        text = plural;
                }
                return text;
            };
        })


        /**
         * @description Форматтер для получения наименования сущности
         * @param entity Сущность
         */
        .filter('nameFormatter', function () {
            return function (entity) {
                return entity ? entity.name : "";
            };
        })

        /**
         * @description Форматтер для получения полного пути
         * @param entity Сущность
         */
        .filter('fullNameFormatter', function () {
            return function (entity) {
                return entity ? entity.fullName : "";
            };
        })

        /**
         * @description Форматтер для получения наименования отчетного периода в нужном формате "год: наименование периода"
         * @param reportPeriod Отчетный период
         */
        .filter('periodFormatter', function () {
            return function (reportPeriod) {
                return reportPeriod ? reportPeriod.taxPeriod.year + ": " + reportPeriod.name : "";
            };
        })

        /**
         * @description Форматтер для параметров запроса
         * @param param параметр запроса
         */
        .filter('requestParamsFormatter', function () {
            return function (param) {
                return !param ? undefined : param;
            };
        })

        /**
         * @description Форматтер для получения наименования сущности
         * @param entity Сущность
         */
        .filter('periodTypeFormatter', function () {
            return function (entity) {
                return entity ? entity.name : "";
            };
        })

        /**
         * @description Форматтер для преобразования тега корректировки из enum в boolean
         * @param correctionTag
         */
        .filter('correctionTagFormatter', ['APP_CONSTANTS', function (APP_CONSTANTS) {
            return function (correctionTag) {
                if (correctionTag && correctionTag.id !== undefined) {
                    switch (correctionTag.id) {
                        case APP_CONSTANTS.CORRETION_TAG.ALL.id:
                            return undefined;
                        case APP_CONSTANTS.CORRETION_TAG.ONLY_PRIMARY.id:
                            return false;
                        case APP_CONSTANTS.CORRETION_TAG.ONLY_CORRECTIVE.id:
                            return true;
                    }
                }
                return undefined;
            };
        }])

        /**
         * @description Преобразует значение признака активности периода в текст (Открыт/Закрыт/Не задано)
         * @param value признак активности периода
         */
        .filter('activeStatusPeriodFormatter', ['$filter', function ($filter) {
            return function (value) {
                return value === null || value === undefined ? $filter('translate')('common.undefined') :
                    (value ? $filter('translate')('reportPeriod.grid.status.open') :
                        $filter('translate')('reportPeriod.grid.status.close'));
            };
        }])

        /**
         * @description Преобразует булевые значения в текст (Да/Нет)
         * @param value булевое значение
         */
        .filter('yesNoFormatter', ['$filter', function ($filter) {
            return function (value) {
                return value ?
                    $filter('translate')('yes') :
                    $filter('translate')('no');
            };
        }])

        /**
         * @description Форматтер периода формы в виде
         * "<Год>, <Название периода><Дата корректировки через запятую (если имеется)>"
         * @param value признак активности периода
         */
        .filter('ndflPeriodFormatter', ['$filter', function ($filter) {
            return function (declarationData) {
                if (declarationData) {
                    return $filter('translate')('title.period.value', {
                        year: declarationData.reportPeriodYear,
                        periodName: declarationData.reportPeriod,
                        correctionString:
                            declarationData.correctionDate ?
                                $filter('translate')('title.period.value.correctionString', {correctionDate: $filter('date')(declarationData.correctionDate, 'dd.MM.yyyy')}) :
                                ''
                    });
                }
                return '';
            };
        }])

        /**
         * @description Соединяет объекты в строку через разделитель,
         * при этом в строку попадает не сам объект, а свойство объекта, полученное по пути
         * @param array массив объектов
         * @param separator разделитель (', ' по умолчанию)
         * @param path путь к полю, которое будет записано в строку через разделитель (name по-умолчанию)
         * @param checkDistinction проверять уникальность значений свойств объектов (false по умолчанию)
         */
        .filter('joinObjectsPropFormatter', function () {
            return function (array, separator, path, checkDistinction) {
                if (array && array.length > 0) {
                    var nameArray = _.map(array, function (obj) {
                        return _.deep(obj, path || 'name');
                    });

                    if(checkDistinction) {
                        var distinctValues = [];
                        angular.forEach(nameArray, function (value) {
                            if(distinctValues.indexOf(value) === -1) {
                                distinctValues.push(value);
                            }
                        });
                        nameArray = distinctValues;
                    }

                    return nameArray.join(separator || ', ');
                }

                return '';
            };
        })

        /**
         * @description Форматирует конфигурационный параметр для отображения описания сущности
         */
        .filter('configParamFormatter', function () {
            return function (entity) {
                return entity.description ? entity.description : "";
            };
        })

        /**
         * @description Форматирует значение асинхронного параметра. Если значение 0 - то на выход "", иначе, значение
         */
        .filter('asyncLimitFormatter', function () {
            return function (value) {
                return value === 0 ? "" : value;
            };
        })

        /**
         * @description Форматтер для получения наименования ОКТМО в нужном формате "код: наименование"
         * @param oktmo запись из справочника ОКТМО
         */
        .filter('oktmoFormatter', function () {
            return function (oktmo) {
                return oktmo ? oktmo.code + ": " + oktmo.name : "";
            };
        })
    /**
    * @description Фильтр даты. Если значение даты будет равно '1901-01-01', то отображаться будет '00.00.0000'
    *
    * @param value значение, которое необходимо отформатировать
    * @return Дата в формате 'dd.MM.yyyy'
    */
        .filter('dateZeroFormatter', ['$filter', function($filter) {
            return function (value) {
                if (!value) {
                    return '';
                }
                if (value === $filter('translate')('title.taxTransferDateZeroDate')) {
                    return $filter('translate')('title.taxTransferDateZeroString');
                }
                if (!value.millis) {
                    return $filter('date')(value, 'dd.MM.yyyy');
                }
                else {
                    return $filter('date')(value.millis, 'dd.MM.yyyy');
                }
            };
        }])
    ;
}());