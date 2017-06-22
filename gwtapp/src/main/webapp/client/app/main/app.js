(function () {
    'use strict';

    angular
        .module('sbrfNdfl', [
            'app.header',
            'sbrfNdfl.ndflJournal',
            'sbrfNdfl.ndfl',
            'dateFromToFilter',
            'app.filterUtils',
            'app.formLeaveConfirmer',
            'ngCookies',
            'pascalprecht.translate',
            'aplana.overlay',
            'aplana.alert',
            'aplana.utils',
            'aplana.grid',
            'aplana.submitValid',
            'aplana.collapse',
            'aplana.field',
            'ui.router',
            'ui.validate',
            'dialogs.main',
            'ngMessages',
            'angularFileUpload',
            'ui.bootstrap',
            'ngMessages',
            'ui.select2',
            'datePicker',
            'app.dropdown'
        ])

        // Отображение диалогового окна с сообщением .
        .factory('ShowToDoDialog', ['dialogs', '$filter', function (dialogs, $filter) {
            return function () {
                dialogs.message($filter('translate')('messageDialog.toDo.title'),$filter('translate')('messageDialog.toDo.message'));
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

    /**
     *
     * @description Получение информации о текущем пользователе и запуск приложения
     * @param {{user_data}} response
     */
    var initInjector = angular.injector(['ng']);
    var $http = initInjector.get('$http');
    $http.get('controller/rest/configService/getConfig').then(
        function (response) {
            angular.module('userData', []).constant('USER_DATA', response.data.user_data.user);
            angular.element(document).ready(function () {
                angular.bootstrap(document, ['sbrfNdfl']);
            });
        }
    );
    /**
     * @description Поиск по нажатию на enter
     */
    window.addEventListener("keydown", function(event) {
        if (event.keyCode === 13){
            event.preventDefault();
            event.stopPropagation();
            var doc = $(event.target).closest(".grid-filter");

            if (doc){
                var buttonSearch = doc.find("#searchButton");
                if (buttonSearch){
                    buttonSearch.click();
                }
            }
        }
    });
}());