(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Электронная почта"
     */

    angular.module('app.emailParam', [])

    /**
     * @description контроллер вкладки "Электронная почта"
     */
        .controller('emailParamController', ['$scope', '$filter', 'EmailParamResource', 'APP_CONSTANTS', '$aplanaModal', 'PermissionChecker', '$rootScope',
            'EmailParamCheckerResource', 'LogEntryResource', '$logPanel', '$http',
            function ($scope, $filter, EmailParamResource, APP_CONSTANTS, $aplanaModal, PermissionChecker, $rootScope,
                      EmailParamCheckerResource, LogEntryResource, $logPanel, $http) {

                $scope.emailParamGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        gridName: 'emailParamGrid',
                        datatype: "angularResource",
                        angularResource: EmailParamResource,
                        value: [],
                        colNames: [
                            $filter('translate')('emailParam.field.code'),
                            $filter('translate')('emailParam.field.value'),
                            $filter('translate')('emailParam.field.description')

                        ],
                        colModel: [
                            {name: 'code', index: 'code', width: 400},
                            {name: 'value', index: 'value', width: 250},
                            {name: 'description', index: 'description', width: 950}

                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'id',
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
                                    activeTab: APP_CONSTANTS.CONFIGURATION_PARAM_TAB.EMAIL_PARAM,
                                    emailParam: $scope.emailParamGrid.value[0]
                                };
                            }
                        }
                    }).result.then(function (resolve) {
                        if (resolve) {
                            $scope.emailParamGrid.ctrl.refreshGrid(1);
                        }
                    });
                };

                $scope.checkRecords = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/emailParam/checkValidate"
                    }).then(function (logger) {
                            if (logger.data) {
                                $logPanel.open('log-panel-container', logger.data);
                            }
                        });
                };

                /**
                 * @description проверка прав для записи грида
                 */
                $scope.checkPermissionForGridValue = function () {
                    return PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_CONFIG) && $scope.emailParamGrid.value.length === 1;

                };
            }]);
}());