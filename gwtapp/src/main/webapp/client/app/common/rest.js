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
    angular.module('sbrfNdfl.rest', ['ngResource'])
        // Оповещения
        .factory('NotificationResource', ['$resource', function ($resource) {
            return $resource('/controller/rest/notification?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        // ФЛ
        .factory('NdflPersonResource', ['$resource', function ($resource) {
            return $resource('/controller/rest/ndflPerson/get?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        // Уведомления
        .factory('LogEntryResource', ['$resource', function ($resource) {
            return $resource('/controller/rest/logEntry/:uuid?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
    ;
}());