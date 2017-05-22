/**
 * aplana-i18n (i18n)
 * http://localhost:8080/#/aplana_i18n
 */
(function () {
    'use strict';
    angular.module('aplana.i18n', ['pascalprecht.translate'])
        .provider('I18n', ['$translateProvider', function ($translateProvider) {
            var ru = {
                    "400": "Сервер обнаружил в запросе клиента синтаксическую ошибку.",
                    "401": "Неавторизованный доступ.",
                    "403": "Запрещенный доступ к ресурсу.",
                    "404": "Не найдено. Сервер не может найти данные.",
                    "500": "Внутренняя серверная ошибка.",
                    "503": "Сервис недоступен.",
                    "0"  : "Удаленный сервер недоступен",
                    "12029"  : "Удаленный сервер недоступен",
                    "unknown": "Неизвестная ошибка."
                },
                en = {
                    "400": "Server understood the request but request content was invalid.",
                    "401": "Unauthorised access.",
                    "403": "Forbidden resource can't be accessed.",
                    "404": "Not found.",
                    "500": "Internal Server Error.",
                    "503": "Service Unavailable.",
                    "0"  : "Address unreachable",
                    "12029"  : "Address unreachable",
                    "unknown": "Unknown error."
                };

            return {
                setLang: function (lang) {
                    $translateProvider.preferredLanguage(lang);
                },
                getRu: function () {
                    return ru;
                },
                getEn: function () {
                    return en;
                },
                $get: {}
            };
        }])
        .config(['$translateProvider', 'I18nProvider', function ($translateProvider, I18nProvider) {

            $translateProvider.translations('ru', I18nProvider.getRu());

            $translateProvider.translations('en', I18nProvider.getEn());

            //это по дефолту, если в конфиги наслед. модуля не будт стоять метода setLang()
            $translateProvider.preferredLanguage('ru');

        }]);
}());