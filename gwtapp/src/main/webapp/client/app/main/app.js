(function () {
    'use strict';
    var translateDictionary = {};
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
            'aplana.modal',
            'aplana.modal.dialogs',
            // Модули приложения
            'app.header',
            'app.logPanel',
            'app.ndfl',
            'app.ndflJournal',
            'app.ndflReport',
            'app.ndflReportJournal',
            'app.schedulerTaskList',
            'app.lockDataList',
            'app.asyncTaskList',
            'app.filterUtils',
            'app.rest',
            'app.formatters'
        ])
        /**
         * @description Отображение модального окна с сообщением "Функционал находится в разработке".
         */
        .factory('ShowToDoDialog', ['$dialogs', '$filter', function ($dialogs, $filter) {
            return function () {
                $dialogs.messageDialog({
                    title: $filter('translate')('messageDialog.toDo.title'),
                    content: $filter('translate')('messageDialog.toDo.message')
                });
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
                        templateUrl: 'client/app/main/app.html?v=${buildUuid}'
                    });

                // Настройка источника локализованных сообщений
                $translateProvider.translations('ru', translateDictionary);
                $translateProvider.preferredLanguage('ru');
                $translateProvider.useSanitizeValueStrategy('sanitizeParameters');
            }
        ]);

    var $http = angular.injector(['ng']).get('$http');
    $http.get('resources/locale-ru_RU.json').then(
        function (data) {
            translateDictionary = data.data[0];
        }
    );

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
            //TODO: (dloshkarev) Нужна нормальная реализация не завязанная на CSS-селекторы
            var tableFilter = $(event.target).closest(".grid-filter");
            var isModal = $(event.target).closest(".modal-body").length != 0;
            var isTable = tableFilter.length != 0;

            if (!isModal && isTable) {
                var buttonSearch = tableFilter.find("#searchButton");
                if (buttonSearch) {
                    event.preventDefault();
                    event.stopPropagation();
                    buttonSearch.click();
                }
            }
        }
    });
}());