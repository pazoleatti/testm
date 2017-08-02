(function () {
    'use strict';
    /**
     * @description Основной модуль приложения
     */
    angular
        .module('app', [
            'app.header',
            'app.logPanel',
            'app.ndflJournal',
            'app.ndfl',
            'app.filterUtils',
            'app.rest',
            'app.formatters',
            'app.dialogs',
            'ui.router',
            'ui.validate',
            'ui.select2',
            'ui.bootstrap',
            'dialogs.main',
            'ngMessages',
            'angularFileUpload',
            'ngCookies',
            'pascalprecht.translate',
            'aplana.overlay',
            'aplana.alert',
            'aplana.utils',
            'aplana.grid',
            'aplana.submitValid',
            'aplana.collapse',
            'aplana.field',
            'aplana.dateFromToFilter',
            'aplana.datePicker',
            'aplana.dropdown',
            'aplana.formLeaveConfirmer',
            'aplana.link'
        ])

        // Отображение диалогового окна с сообщением .
        .factory('ShowToDoDialog', ['appDialogs', '$filter', function (appDialogs, $filter) {
            return function () {
                appDialogs.message($filter('translate')('messageDialog.toDo.title'), $filter('translate')('messageDialog.toDo.message'));
            };
        }])

        .config(['$stateProvider', '$urlRouterProvider', '$translateProvider',
            function ($stateProvider, $urlRouterProvider, $translateProvider) {
                // Указание страницы по умолчанию
                $urlRouterProvider.otherwise('/');
                // Настройка обработчика главной страницы
                $stateProvider
                    .state('/', {
                        url: '/',
                        templateUrl: 'client/app/main/app.html'
                    });

                // Настройка источника локализованных сообщений
                $translateProvider.useStaticFilesLoader({
                    prefix: 'resources/locale-',
                    suffix: '.json'
                });
                $translateProvider.preferredLanguage('ru_RU');
                $translateProvider.useLocalStorage();
                $translateProvider.useSanitizeValueStrategy('sanitizeParameters');
            }
        ]);

    angular.element(document).ready(function () {
        angular.bootstrap(document, ['app']);
    });

    /**
     * @description Поиск по нажатию на enter
     */
    window.addEventListener("keydown", function (event) {
        if (event.keyCode === 13) {
            event.preventDefault();
            event.stopPropagation();
            var doc = $(event.target).closest(".grid-filter");

            if (doc) {
                var buttonSearch = doc.find("#searchButton");
                if (buttonSearch) {
                    buttonSearch.click();
                }
            }
        }
    });
}());