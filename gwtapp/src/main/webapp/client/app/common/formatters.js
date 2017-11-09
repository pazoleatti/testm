(function () {
    'use strict';

    /**
     * @description Модуль, содержащий форматтеры
     */

    angular.module('app.formatters', [])
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
        .filter('amountCasesFormatter', ['$filter', function ($filter) {
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
        }])


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
        .filter('fullPathFormatter', function () {
            return function (entity) {
                return entity ? entity.fullPath : "";
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
    ;
}());