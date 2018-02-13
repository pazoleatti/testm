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
                templateUrl: 'client/app/taxes/ndfl/configParams/configParam.html',
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
        .controller('configParamController', ['$scope', '$filter', 'APP_CONSTANTS', '$aplanaModal', '$rootScope',
            function ($scope, $filter, APP_CONSTANTS, $aplanaModal, $rootScope) {

                $scope.configParamTabsCtrl = {};
                $scope.commonParam = {
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

                $scope.configParamTabs = [$scope.commonParam, $scope.asyncParam];

                /**
                 * @description открытие модального окна создания конфигурационного параметра
                 */
                $scope.createRecord = function () {
                    $aplanaModal.open({
                        tittle: $filter('translate')('configParam.modal.createParam.title'),
                        templateUrl: 'client/app/taxes/ndfl/configParams/modal/createRecordModal.html?v=${buildUuid}',
                        controller: 'createRecordModalCtrl',
                        windowClass: $scope.commonParam.active ? 'modal600' : 'modal1000',
                        resolve: {
                            $shareData: function () {
                                return {
                                    isCreate: true,
                                    activeTab: getActiveTab()
                                };
                            }
                        }
                    }).result.then(function (resolve) {
                        if (resolve) {
                            $scope.refreshGrid();
                        }
                    });
                };

                /**
                 * @description возбуждение события редактирования записи конфигурационного параметра
                 */
                $scope.editRecord = function () {
                    if ($scope.commonParam.active) {
                        $rootScope.$broadcast("EDIT_COMMON_PARAM");
                    } else if ($scope.asyncParam.active) {
                        $rootScope.$broadcast("EDIT_ASYNC_PARAM");
                    }
                };

                /**
                 * @description возбуждение события удаления записи конфигурационного параметра
                 */
                $scope.removeRecord = function () {
                    $rootScope.$broadcast("DELETE_COMMON_PARAMS");
                };

                /**
                 * @description возбуждение события обновления данных в гридах вкладок
                 */
                $scope.refreshGrid = function () {
                    $rootScope.$broadcast("UPDATE_CONFIG_GRID_DATA");
                };

                /**
                 * @description Получение информации об активной вкладке
                 * @return {string} наименование активной вкладки
                 */
                var getActiveTab = function () {
                    return $scope.commonParam.active ? APP_CONSTANTS.CONFIGURATION_PARAM_TAB.COMMON_PARAM : ($scope.asyncParam.active ? APP_CONSTANTS.CONFIGURATION_PARAM_TAB.ASYNC_PARAM : null);
                };

                /**
                 * @description возбуждение события на пересчет количества выбранных в гриде записей
                 */
                $scope.$watch("commonParam.active", function () {
                    if (getActiveTab() === APP_CONSTANTS.CONFIGURATION_PARAM_TAB.COMMON_PARAM) {
                        $rootScope.$broadcast("UPDATE_INFO_COMMON_GRID_DATA");
                    } else {
                        $rootScope.$broadcast("UPDATE_INFO_ASYNC_GRID_DATA");
                    }
                });

                /**
                 * @description проверка прав на действия над параметрами
                 * @param permission действие, право на выполнение которого проверяется
                 * @return {boolean} признак доступа на выполнение действия
                 */
                $scope.permissionCheckerGridValue = function (permission) {
                    if ($rootScope.user.roles[0].alias !== APP_CONSTANTS.USER_ROLE.ROLE_ADMIN) {
                        return false;
                    }
                    if (permission === APP_CONSTANTS.CONFIGURATION_PERMISSION.DELETE || permission === APP_CONSTANTS.CONFIGURATION_PERMISSION.CREATE) {
                        return getActiveTab() === APP_CONSTANTS.CONFIGURATION_PARAM_TAB.COMMON_PARAM;
                    } else if (permission === APP_CONSTANTS.CONFIGURATION_PERMISSION.EDIT) {
                        if (getActiveTab() === APP_CONSTANTS.CONFIGURATION_PARAM_TAB.COMMON_PARAM) {
                            $rootScope.$broadcast("UPDATE_INFO_COMMON_GRID_DATA");
                        } else {
                            $rootScope.$broadcast("UPDATE_INFO_ASYNC_GRID_DATA");
                        }
                        // параметр, отвечающий за количетво выбранных в гриде записей на активной вкладке
                        return $rootScope.configParamGridLength === 1;
                    }
                };

            }
        ])

    ;
}());
