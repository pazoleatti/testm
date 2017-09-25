(function () {
    'use strict';
    /**
     * @description Модуль реализует обмен с сервером по стандартам REST.
     * Требуется jsGrid для запроса данных через options.angularResource,
     * а также используются методы ресурса для создания/изменения сущностей.
     *
     * Каждый ресурс по умолчанию содержит следующие HTTP-методы:
     * { 'get':   {method:'GET'},
     *  'save':   {method:'POST'},
     *  'query':  {method:'GET', isArray:true},
     *  'remove': {method:'DELETE'},
     *  'delete': {method:'DELETE'} };
     */
    angular.module('app.rest', ['ngResource'])
    /**
     * @description Конфигурация
     */
        .factory('ConfigResource', ['$resource', function ($resource) {
            return $resource('controller/rest/config', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Данные о пользователе
         */
        .factory('UserDataResource', ['$resource', function ($resource) {
            return $resource('controller/rest/userData', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Оповещения
         */
        .factory('NotificationResource', ['$resource', function ($resource) {
            return $resource('controller/rest/notification?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Данные НДФЛ по физлицу
         * @description Данные НДФЛ по физлицу
         */
        .factory('NdflPersonResource', ['$resource', function ($resource) {
            return $resource('controller/rest/ndflPerson?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Декларация
         */
        .factory('DeclarationDataResource', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])
        /**
         * @description Уведомления
         */
        .factory('LogEntryResource', ['$resource', function ($resource) {
            return $resource('controller/rest/logEntry/:uuid?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Файлы и комментарии
         */
        .factory('FilesCommentsResource', ['$resource', function ($resource) {
            return $resource('controller/rest/declaration/filesComments?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Справочники
         */
        .factory('RefBookValuesResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBookValues/:refBookId', {}, {
                query: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Поиск лиц для формирования персонального РНУ НДФЛ по физическому лицу
         */
        .factory('RnuPerson', ['$resource', function ($resource) {
            return $resource('controller/rest/getListPerson/rnuPerson/:rnuPersons?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])


        /**
         * @description Формирование рну ндфл
         */
        .factory('RnuPersonDocument', ['$resource', function ($resource) {
            return $resource('/actions/declarationData/declarationDataId}/rnuDoc?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description
         */
        .factory('DeclarationTypeForCreateResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBook/207/declarationTypesForCreate?declarationKind=:declarationKind&departmentId=:departmentId&periodId=:periodId', {}, {
                query: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Поиск лиц для формирования персонального РНУ НДФЛ по физическому лицу
         */
        .factory('RnuPerson', ['$resource', function ($resource) {
            return $resource('controller/rest/getListPerson/rnuPerson/:rnuPersons?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])


        /**
         * @description Формирование рну ндфл
         */
        .factory('RnuPersonDocument', ['$resource', function ($resource) {
            return $resource('/actions/declarationData/declarationDataId}/rnuDoc?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])


    ;
}());