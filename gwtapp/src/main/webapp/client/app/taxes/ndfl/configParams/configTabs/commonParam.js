(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Общие параметры"
     */

    angular.module('app.commonParam', [])

    /**
     * @description контроллер вкладки "Общие параметры"
     */
        .controller('commonParamController', ['$scope', '$filter', 'CommonParamResource', 'APP_CONSTANTS', '$http', '$logPanel', 'LogEntryResource', '$rootScope', '$aplanaModal',
            function ($scope, $filter, CommonParamResource, APP_CONSTANTS, $http, $logPanel, LogEntryResource, $rootScope, $aplanaModal) {

                $scope.commonParamGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: CommonParamResource,
                        value: [],
                        colNames: [
                            $filter('translate')('commonParam.grid.columnName.code'),
                            $filter('translate')('commonParam.grid.columnName.value')
                        ],
                        colModel: [
                            {name: 'description', index: 'code', width: 600},
                            {name: 'value', index: 'value', width: 250}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'code',
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
                    $scope.commonParamGrid.ctrl.refreshGrid(1);
                });

                /**
                 * @description отлавливает событие удаления записи
                 */
                $scope.$on("DELETE_COMMON_PARAMS", function () {
                    $scope.removeRecords();
                });

                /**
                 * @description отлавливает событие редактирования записи
                 */
                $scope.$on("EDIT_COMMON_PARAM", function () {
                    $scope.updateRecord();
                });

                /**
                 * @description отлавливает событие пересчета количества выбранных записей на вкладке "Общие параметры"
                 */
                $scope.$on("UPDATE_INFO_COMMON_GRID_DATA", function () {
                    $rootScope.configParamGridLength = $scope.commonParamGrid.value.length;
                });

                /**
                 * @description Удаление выбраных в гриде записей
                 */
                $scope.removeRecords = function () {
                    var selectedItems = $scope.commonParamGrid.value;
                    $http({
                        method: "POST",
                        url: "controller/action/configuration/remove",
                        data: $filter('idExtractor')(selectedItems, 'description')
                    }).then(function (logger) {
                        LogEntryResource.query({
                            uuid: logger.data,
                            projection: 'count'
                        }, function (data) {
                            $logPanel.open('log-panel-container', logger.data);
                            if (data.ERROR + data.WARNING < 1){
                                $rootScope.$broadcast("UPDATE_CONFIG_GRID_DATA");
                            }
                        });
                    });
                };

                /**
                 * @description открытие модального окна обовления записи
                 */
                $scope.updateRecord = function () {
                    $aplanaModal.open({
                        tittle: $filter('translate')('configParam.modal.editParam.title'),
                        templateUrl: 'client/app/taxes/ndfl/configParams/modal/createRecordModal.html?v=${buildUuid}',
                        controller: 'createRecordModalCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    isCreate: false,
                                    activeTab: APP_CONSTANTS.CONFIGURATION_PARAM_TAB.COMMON_PARAM,
                                    commonParam: $scope.commonParamGrid.value[0]
                                };
                            }
                        }
                    }).result.then(function (resolve) {
                        if (resolve){
                            $scope.commonParamGrid.ctrl.refreshGrid(1);
                        }
                    });
                };
            }])
    ;
}());