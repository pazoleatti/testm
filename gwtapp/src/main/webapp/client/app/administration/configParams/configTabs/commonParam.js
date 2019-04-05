(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Общие параметры"
     */

    angular.module('app.commonParam', [])

    /**
     * @description контроллер вкладки "Общие параметры"
     */
        .controller('commonParamController', ['$scope', '$filter', 'CommonParamResource', 'APP_CONSTANTS', '$http', '$logPanel',
            'LogEntryResource', '$rootScope', '$aplanaModal', 'PermissionChecker', '$dialogs',
            function ($scope, $filter, CommonParamResource, APP_CONSTANTS, $http, $logPanel, LogEntryResource, $rootScope, $aplanaModal,
                      PermissionChecker, $dialogs) {

                $scope.user = $rootScope.user;

                $scope.commonParamGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: CommonParamResource,
                        requestParameters: function () {
                            return {
                                projection: 'admin'
                            };
                        },
                        value: [],
                        colNames: [
                            "",
                            $filter('translate')('commonParam.grid.columnName.code'),
                            $filter('translate')('commonParam.grid.columnName.value')
                        ],
                        colModel: [
                            {name: 'departmentId', index: 'departmentId', hidden: true},
                            {name: 'description', index: 'code', width: 500, key: true},
                            {name: 'value', index: 'value', width: 700}
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
                 * @description открытие модального окна создания конфигурационного параметра
                 */
                $scope.createRecord = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('configParam.modal.createParam.title'),
                        templateUrl: 'client/app/administration/configParams/modal/createRecordModal.html',
                        controller: 'createRecordModalCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    isCreate: true,
                                    activeTab: APP_CONSTANTS.CONFIGURATION_PARAM_TAB.COMMON_PARAM
                                };
                            }
                        }
                    }).result.then(function () {
                        $scope.commonParamGrid.ctrl.refreshGrid(1);
                    });
                };

                /**
                 * @description Удаление выбраных в гриде записей
                 */
                $scope.removeRecords = function () {
                    var selectedItems = $scope.commonParamGrid.value;
                    $dialogs.confirmDialog({
                        title: $filter('translate')('configParam.confirm.deleteConfig.title'),
                        content: $filter('translate')('configParam.confirm.deleteConfig.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $http({
                                method: "POST",
                                url: "controller/actions/commonParam/remove",
                                data: $filter('idExtractor')(selectedItems, 'code')
                            }).then(function (logger) {
                                LogEntryResource.query({
                                    uuid: logger.data,
                                    projection: 'count'
                                }, function (data) {
                                    $logPanel.open('log-panel-container', logger.data);
                                    if (data.ERROR + data.WARNING < 1) {
                                        $scope.commonParamGrid.ctrl.refreshGrid(1);
                                    }
                                });
                            });
                        }
                    });
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
                                    activeTab: APP_CONSTANTS.CONFIGURATION_PARAM_TAB.COMMON_PARAM,
                                    commonParam: $scope.commonParamGrid.value[0]
                                };
                            }
                        }
                    }).result.then(function (resolve) {
                        if (resolve) {
                            $scope.commonParamGrid.ctrl.refreshGrid(1);
                        }
                    });
                };

                $scope.checkRecord = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/commonParam/checkValidate",
                        params: {
                            param: JSON.stringify({
                                code: $scope.commonParamGrid.value[0].code,
                                description: $scope.commonParamGrid.value[0].description,
                                value: $scope.commonParamGrid.value[0].value
                            })
                        }
                    }).then(function (logger) {
                        if (logger.data) {
                            $logPanel.open('log-panel-container', logger.data);
                        }
                    });
                };

                /**
                 * @description проверка прав для записи грида
                 * @param permission действие
                 */
                $scope.checkPermissionForGridValue = function (permission) {
                    switch (permission) {
                        case APP_CONSTANTS.CONFIGURATION_PERMISSION.CREATE:
                            return PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_CONFIG);
                        case APP_CONSTANTS.CONFIGURATION_PERMISSION.EDIT:
                            return PermissionChecker.check($scope.commonParamGrid.value[0], APP_CONSTANTS.CONFIGURATION_PERMISSION.EDIT) && $scope.commonParamGrid.value.length === 1;
                        case APP_CONSTANTS.CONFIGURATION_PERMISSION.REMOVE:
                            return PermissionChecker.check($scope.commonParamGrid.value[0], APP_CONSTANTS.CONFIGURATION_PERMISSION.REMOVE);
                    }
                };
            }
        ]);
}());
