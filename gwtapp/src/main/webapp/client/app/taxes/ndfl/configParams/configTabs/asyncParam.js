(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Параметры асинхронных заданий"
     */

    angular.module('app.asyncParam', [])

    /**
     * @description контроллер вкладки "Параметры асинхронных заданий"
     */
        .controller('asyncParamController', ['$scope', '$filter', 'AsyncTaskResource', 'APP_CONSTANTS', '$aplanaModal', '$rootScope',
            function ($scope, $filter, AsyncTaskResource, APP_CONSTANTS, $aplanaModal, $rootScope) {

                $scope.asyncParamGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        gridName: 'asyncParamGrid',
                        datatype: "angularResource",
                        angularResource: AsyncTaskResource,
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
                            {name: 'taskLimit', index: 'taskLimit', width: 400, formatter: $filter('asyncLimitFormatter')},
                            {name: 'shortQueueLimit', index: 'shortQueueLimit', width: 650, formatter: $filter('asyncLimitFormatter')}

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
                 * @description отлавливает событие обновления грида
                 */
                $scope.$on("UPDATE_CONFIG_GRID_DATA", function () {
                    $scope.asyncParamGrid.ctrl.refreshGrid(1);
                });

                /**
                 * @description отлавливает событие редактирования записи
                 */
                $scope.$on("EDIT_ASYNC_PARAM", function () {
                    $scope.updateRecord();
                });

                /**
                 * @description отлавливает событие пересчета количества выбранных записей на вкладке "Параметры асинхронных заданий"
                 */
                $scope.$on("UPDATE_INFO_ASYNC_GRID_DATA", function () {
                    $rootScope.configParamGridLength = $scope.asyncParamGrid.value.length;
                });

                /**
                 * @description открытие модального окна обовления записи
                 */
                $scope.updateRecord = function () {
                    $aplanaModal.open({
                        tittle: $filter('translate')('configParam.modal.editParam.title'),
                        templateUrl: 'client/app/taxes/ndfl/configParams/modal/createRecordModal.html?v=${buildUuid}',
                        controller: 'createRecordModalCtrl',
                        windowClass: 'modal1000',
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
                        if (resolve){
                            $scope.asyncParamGrid.ctrl.refreshGrid(1);
                        }
                    });
                };
            }]);
}());