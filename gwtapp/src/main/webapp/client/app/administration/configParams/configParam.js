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
            'app.commonParam',
            'app.emailParam',
            'app.createRecordModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('configParam', {
                url: '/taxes/configParam/',
                templateUrl: 'client/app/administration/configParams/configParam.html',
                controller: 'configParamController',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_CONFIG)) {
                            $state.go("/");
                        }
                    }]
            });
        }])


        /**
         * @description Контроллер для работы с формой "Администрирование - Конфигурационные параметры"
         */
        .controller('configParamController', ['$scope', '$filter',
            function ($scope, $filter) {

                $scope.configParamTabsCtrl = {};
                $scope.commonParam = {
                    title: $filter('translate')('tab.configParam.commonParam'),
                    contentUrl: 'client/app/administration/configParams/configTabs/commonParam.html?v=${buildUuid}',
                    fetchTab: true,
                    active: true
                };
                $scope.asyncParam = {
                    title: $filter('translate')('tab.configParam.asyncParam'),
                    contentUrl: 'client/app/administration/configParams/configTabs/asyncParam.html?v=${buildUuid}',
                    fetchTab: true,
                    active: false
                };
                $scope.emailParam = {
                    title: $filter('translate')('tab.configParam.emailConfig'),
                    contentUrl: 'client/app/administration/configParams/configTabs/emailParam.html?v=${buildUuid}',
                    fetchTab: true,
                    active: false
                };

                $scope.configParamTabs = [$scope.commonParam, $scope.asyncParam, $scope.emailParam];
            }
        ])

    ;
}());
