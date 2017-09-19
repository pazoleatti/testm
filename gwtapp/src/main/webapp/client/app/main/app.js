(function () {
    'use strict';
    /**
     * @description Основной модуль приложения
     */
    angular
        .module('app', [
            // Стандартные/внешние модули, плагины, компоненты
            'ui.router',
            'ui.validate',
            'ui.select2',
            'ui.bootstrap',
            'dialogs.main',
            'ngMessages',
            'angularFileUpload',
            'ngCookies',
            'pascalprecht.translate',
            // Наши компоненты
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
            'aplana.link',
            // Модули приложения
            'app.header',
            'app.logPanel',
            'app.ndfl',
            'app.ndflJournal',
            'app.filterUtils',
            'app.rest',
            'app.formatters',
            'app.modals'
        ])
        /**
         * @description Отображение модального окна с сообщением "Функционал находится в разработке".
         */
        .factory('ShowToDoDialog', ['appModals', '$filter', function (appModals, $filter) {
            return function () {
                appModals.message($filter('translate')('messageDialog.toDo.title'), $filter('translate')('messageDialog.toDo.message'));
            };
        }])

        /**
         * @description Конфигурирование роутера и локализации сообщений для приложения
         */
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
        ])

    var UserDataResource = angular.injector(['app.rest']).get('UserDataResource');
    UserDataResource.query({
            projection: "user"
        },
        function (data) {
            angular.element(document).ready(function () {
                var $inj = angular.bootstrap(document, ['app']);
                var $rootScope = $inj.get("$rootScope");

                $rootScope.user = {
                    name: data.taUserInfo.user.name,
                    login: data.taUserInfo.user.login,
                    department: data.department,
                    permissions: data.taUserInfo.user.permissions,
                    roles: data.taUserInfo.user.roles,
                    hasRole: function (role) {
                        var roleAliasList = data.taUserInfo.user.roles.map(function (userRole) {
                            return userRole.alias;
                        });
                        return roleAliasList.indexOf(role) >= 0;
                    }
                };
                $rootScope.permissionChecker = angular.injector(['app.permissionUtils']).get('PermissionChecker');
                $rootScope.APP_CONSTANTS = angular.injector(['app.constants']).get('APP_CONSTANTS');
            });
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