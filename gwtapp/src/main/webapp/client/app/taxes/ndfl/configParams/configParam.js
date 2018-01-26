(function () {
    'use strict';

    /**
     * @description Модуль для работы с формой "Администрирование - Конфигурационные параметры"
     */

    angular.module('app.configParam',
        ['ui.router',
            'app.logPanel',
            'app.rest',
            'app.asyncParam',
            'app.commonParam'
        ])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('configParam', {
                url: '/taxes/configParam/',
                templateUrl: 'client/app/taxes/ndfl/configParams/configParam.html',
                controller: 'configParamController'
            });
        }])


        /**
         * @description Контроллер для работы с формой "Администрирование - Конфигурационные параметры"
         */
        .controller('configParamController', ['$scope', '$filter', 'APP_CONSTANTS',
            function ($scope, $filter, APP_CONSTANTS) {

                $scope.configParamTabsCtrl = {};
                $scope.defaultParam = {
                    title: $filter('translate')('tab.configParam.commonParam'),
                    contentUrl: 'client/app/taxes/ndfl/configParams/configTabs/commonParam.html?v=${buildUuid}',
                    fetchTab: true,
                    active: true
                };
                $scope.asyncParam = {
                    title: $filter('translate')('tab.configParam.asyncParam'),
                    contentUrl: 'client/app/taxes/ndfl/configParams/configTabs/asyncParam.html?v=${buildUuid}',
                    fetchTab: true,
                    active: false
                };

                $scope.configParamTabs = [$scope.defaultParam, $scope.asyncParam];

            }])

    ;
}());
