(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Параметры асинхронных заданий"
     */

    angular.module('app.asyncParam', [])

    /**
     * @description контроллер вкладки "Параметры асинхронных заданий"
     */
        .controller('asyncParamController', ['$scope', '$filter', 'AsyncParamResource', 'APP_CONSTANTS', '$aplanaModal', 'PermissionChecker', '$rootScope',
            function ($scope, $filter, AsyncParamResource, APP_CONSTANTS, $aplanaModal, PermissionChecker, $rootScope) {

                $scope.asyncParamGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        gridName: 'asyncParamGrid',
                        datatype: "angularResource",
                        angularResource: AsyncParamResource,
                        value: [],
                        colNames: [
                            $filter('translate')('asyncParam.grid.columnName.taskType'),
                            $filter('translate')('asyncParam.grid.columnName.limitKind'),
                            $filter('translate')('asyncParam.grid.columnName.taskLimit'),
                            $filter('translate')('asyncParam.grid.columnName.shortQueueLimit')

                        ],
                        colModel: [
                            {name: 'name', index: 'name', width: 400},
                            {name: 'limitKind', index: 'limitKind', width: 250},
                            {
                                name: 'taskLimit',
                                index: 'taskLimit',
                                width: 400,
                                formatter: $filter('asyncLimitFormatter')
                            },
                            {
                                name: 'shortQueueLimit',
                                index: 'shortQueueLimit',
                                width: 650,
                                formatter: $filter('asyncLimitFormatter')
                            }

                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'name',
                        viewrecords: true,
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: true
                    }
                };

                /**
                 * @description открытие модального окна обовления записи
                 */
                $scope.updateRecord = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('configParam.modal.editParam.title'),
                        templateUrl: 'client/app/administration/configParams/modal/createRecordModal.html',
                        controller: 'createRecordModalCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    isCreate: false,
                                    activeTab: APP_CONSTANTS.CONFIGURATION_PARAM_TAB.ASYNC_PARAM,
                                    asyncParam: $scope.asyncParamGrid.value[0]
                                };
                            }
                        }
                    }).result.then(function (resolve) {
                        if (resolve) {
                            $scope.asyncParamGrid.ctrl.refreshGrid(1);
                        }
                    });
                };

                /**
                 * @description проверка прав для записи грида
                 */
                $scope.checkPermissionForGridValue = function () {
                    return PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_CONFIG) && $scope.asyncParamGrid.value.length === 1;

                };
            }]);
}());