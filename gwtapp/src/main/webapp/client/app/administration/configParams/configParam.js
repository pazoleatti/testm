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
            'app.createRecordModal'
        ])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('configParam', {
                url: '/taxes/configParam/',
                templateUrl: 'client/app/administration/configParams/configParam.html',
                controller: 'configParamController',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', 'UserDataResource',
                    function ($state, PermissionChecker, APP_CONSTANTS, UserDataResource) {
                        UserDataResource.query({
                            projection: "user"
                        }, function (data) {
                            var user = {
                                name: data.taUserInfo.user.name,
                                login: data.taUserInfo.user.login,
                                department: data.department,
                                permissions: data.taUserInfo.user.permissions,
                                roles: data.taUserInfo.user.roles
                            };
                            if (!PermissionChecker.check(user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_CONFIG)) {
                                $stateProvider.state('main', {
                                    url: '/',
                                    templateUrl: 'client/app/main/app.html'
                                });
                                $state.go('main');
                            }
                        });
                    }
                ]
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

                $scope.configParamTabs = [$scope.commonParam, $scope.asyncParam];
            }
        ])

    ;
}());
