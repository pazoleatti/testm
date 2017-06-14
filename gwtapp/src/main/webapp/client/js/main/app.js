(function () {
    'use strict';

    angular
        .module('sbrfNdfl', [
            'app.header',
            'sbrfNdfl.ndflForms',
            'sbrfNdfl.ndflDetailsForms',
            'app.filterDirectives',
            'app.filterUtils',
            'app.formLeaveConfirmer',
            'ngCookies',
            'pascalprecht.translate',
            'aplana.overlay',
            'aplana.alert',
            'aplana.modal',
            'aplana.utils',
            'aplana.grid',
            'aplana.submitValid',
            'aplana.dropdownMenu',
            'aplana.collapse',
            'aplana.field',
            'aplana.modal.dialogs',
            'ui.router',
            'ui.validate',
            'dialogs.main',
            'ngMessages',
            'angularFileUpload',
            'ui.grid.moveColumns',
            'ui.grid',
            'ui.grid.pagination',     // пейджинг
            'ui.grid.resizeColumns',  // изменение ширины столбцов
            'ui.grid.saveState',      // сохранение состояния таблицы при переходах
            'ui.grid.selection',      // выделение строк
            'ui.grid.autoResize',
            'ui.bootstrap',
            'ui.select',
            'ngMessages',
            'ui.select2',
            'datePicker',
            'app.dropdown'
        ])
        .config(['$stateProvider', '$urlRouterProvider', '$translateProvider',
            function ($stateProvider, $urlRouterProvider, $translateProvider) {
                // Указание страницы по умолчанию
                $urlRouterProvider.otherwise('/');
                // Настройка обработчика главной страницы
                $stateProvider
                    .state('/', {
                        url: '/',
                        templateUrl: 'client/js/main/app.html'
                    });

                // Настройка источника локализованных сообщений
                $translateProvider.useStaticFilesLoader({
                    prefix: 'resources/locale-',
                    suffix: '.json'
                });
                $translateProvider.preferredLanguage('ru_RU');
                $translateProvider.useLocalStorage();
                $translateProvider.useSanitizeValueStrategy('sanitizeParameters');
                // Добавляем форматирование дат
                Date.prototype.format = function (mask, utc) {
                    return dateFormat(this, mask, utc);
                };
                // Добавляем возможность прибавления дней к дате
                Date.prototype.addDays = function (days) {
                    var dat = new Date(this.valueOf());
                    dat.setDate(dat.getDate() + days);
                    return dat;
                };
            }
        ]);

    // Получение информации о текущем пользователе и запуск приложения
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
     * Поиск по нажатию на enter
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