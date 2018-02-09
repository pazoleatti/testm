(function () {
    'use strict';

    /**
     * @description Модуль для создания и редактирования записей конфигураций
     */
    angular.module('app.createRecordModal', ['ui.router', 'app.rest'])

    /**
     * @description Контроллер создания и редактирования записей конфигураций
     */
        .controller('createRecordModalCtrl', ['$scope', '$filter', 'APP_CONSTANTS', '$modalInstance', '$shareData', '$http', '$logPanel', 'LogEntryResource', '$dialogs', '$q',
            '$rootScope',
            function ($scope, $filter, APP_CONSTANTS, $modalInstance, $shareData, $http, $logPanel, LogEntryResource, $dialogs, $q, $rootScope) {

                $scope.commonParamTabActive = false;
                $scope.asyncParamTabActive = false;
                $scope.isCreate = $shareData.isCreate;
                $scope.commonParam = {};
                $scope.asyncParam = {};

                switch ($shareData.activeTab) {
                    case APP_CONSTANTS.CONFIGURATION_PARAM_TAB.COMMON_PARAM :
                        $scope.commonParamTabActive = true;
                        if ($shareData.isCreate) {
                            $scope.commonParam = {};
                        } else {
                            $scope.commonParam.param = $shareData.commonParam;
                            $scope.commonParam.value = $shareData.commonParam.value;
                        }
                        break;
                    case APP_CONSTANTS.CONFIGURATION_PARAM_TAB.ASYNC_PARAM :
                        $scope.asyncParamTabActive = true;
                        if ($shareData.isCreate) {
                            $scope.asyncParam = {};
                        } else {
                            $scope.asyncParam.param = $shareData.asyncParam;
                            $scope.asyncParam.taskLimit = $shareData.asyncParam.taskLimit;
                            $scope.asyncParam.shortQueueLimit = $shareData.asyncParam.shortQueueLimit;
                        }
                        break;
                    default:
                        $modalInstance.close();
                }

                /**
                 * @description сохранение записи
                 */
                $scope.save = function () {
                    $scope.checkAccess().then(function (accept) {
                        if (accept) {
                            if ($scope.commonParamTabActive){
                                // активна вкладка "Общие параметры"
                                $http({
                                    method: "POST",
                                    url: "controller/action/configuration/commonParam/" + ($scope.isCreate ? "create" : "update"),
                                    params: {
                                        commonParam: JSON.stringify({
                                            description: $scope.commonParam.param.description,
                                            value: $scope.commonParam.value
                                        })
                                    }
                                }).then(function (logger) {
                                    if(logger.data) {
                                        LogEntryResource.query({
                                            uuid: logger.data,
                                            projection: 'count'
                                        }, function (data) {
                                            $logPanel.open('log-panel-container', logger.data);
                                            if (data.ERROR + data.WARNING < 1) {
                                                $rootScope.$broadcast("UPDATE_CONFIG_GRID_DATA");
                                                $modalInstance.close(true);
                                            }
                                        });
                                    }
                                });
                            }else if ($scope.asyncParamTabActive){
                                // активна вкладка "Параметры асинхронных задач"
                                $http({
                                    method: "POST",
                                    url: "controller/action/configuration/asyncParam/update",
                                    params: {
                                            asyncParam: JSON.stringify({
                                            id: $scope.asyncParam.param.id,
                                            taskLimit: $scope.asyncParam.taskLimit,
                                            shortQueueLimit: $scope.asyncParam.shortQueueLimit
                                        })
                                    }
                                }).then(function (logger) {
                                    if(logger.data) {
                                        LogEntryResource.query({
                                            uuid: logger.data,
                                            projection: 'count'
                                        }, function (data) {
                                            $logPanel.open('log-panel-container', logger.data);
                                            if (data.ERROR + data.WARNING < 1) {
                                                $rootScope.$broadcast("UPDATE_CONFIG_GRID_DATA");
                                                $modalInstance.close(true);
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                };

                /**
                 * @description закрытие модального окна
                 */
                $scope.close = function () {
                    $dialogs.confirmDialog({
                        title: ($scope.isCreate ? $filter('translate')('configParam.confirm.rejectCreatingConfig.title') : $filter('translate')('configParam.confirm.rejectEditingConfig.title')),
                        content: ($scope.isCreate ? $filter('translate')('configParam.confirm.rejectCreatingConfig.text') : $filter('translate')('configParam.confirm.rejectEditingConfig.text')),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.close(false);
                        }
                    });
                };

                /**
                 * @description проверка доступности создания или редактирования записи
                 *
                 * @return {Promise} признак проверки (true - разрешено сохранение, false - запрещено сохранение)
                 */
                $scope.checkAccess = function () {
                    var checkAccessQDefer = $q.defer();
                    if ($scope.commonParamTabActive) {
                        // проверка доступности введенного пути
                        $http({
                            method: "POST",
                            url: "controller/action/configuration/commonParam?projection=checkReadWriteAccess",
                            params: {
                                param: JSON.stringify({
                                    description: $scope.commonParam.param.description,
                                    value: $scope.commonParam.value
                                })
                            }
                        }).then(function (logger) {
                            if (logger.data) {
                                $logPanel.open('log-panel-container', logger.data);
                                checkAccessQDefer.resolve(false);
                            } else {
                                checkAccessQDefer.resolve(true);
                            }
                        });
                    } else if ($scope.asyncParamTabActive) {
                        // проверка на числовое значение "Ограничение на выполнение задания", отличное от 0
                        if ($scope.asyncParam.taskLimit !== "" && (isNaN(Number($scope.asyncParam.taskLimit)) || Number($scope.asyncParam.taskLimit) === 0)) {
                            $dialogs.errorDialog({
                                content: $filter('translate')('asyncParam.validate.checkNumber', {
                                    taskTitle: $scope.asyncParam.param.name,
                                    limitName: $filter('translate')('asyncParam.grid.columnName.taskLimit'),
                                    limitValue: $scope.asyncParam.taskLimit
                                })
                            });
                            checkAccessQDefer.resolve(false);
                            return checkAccessQDefer.promise;
                        }
                        // проверка на числовое значение "Ограничение на выполнение задания в очереди быстрых заданий", отличное от 0
                        if ($scope.asyncParam.shortQueueLimit !== "" && (isNaN(Number($scope.asyncParam.shortQueueLimit)) ||  Number($scope.asyncParam.shortQueueLimit) === 0)) {
                            $dialogs.errorDialog({
                                content: $filter('translate')('asyncParam.validate.checkNumber', {
                                    taskTitle: $scope.asyncParam.param.name,
                                    limitName: $filter('translate')('asyncParam.grid.columnName.shortQueueLimit'),
                                    limitValue: $scope.asyncParam.shortQueueLimit
                                })
                            });
                            checkAccessQDefer.resolve(false);
                            return checkAccessQDefer.promise;
                        }
                        // проверка, что значения параметра "Загрузка данных из файла в справочник" меньше, чем 1500000
                        if($scope.asyncParam.shortQueueLimit && $scope.asyncParam.param.handlerClassName === APP_CONSTANTS.ASYNC_HANDLER_CLASS_NAME.UPLOAD_REFBOOK_ASYNC_TASK && (Number($scope.asyncParam.taskLimit) > 1500000 || Number($scope.asyncParam.shortQueueLimit) > 1500000)){
                            $dialogs.errorDialog({
                                content: $filter('translate')('asyncParam.validate.tooMuch', {
                                    taskTitle: $scope.asyncParam.param.name,
                                    limitName: (Number($scope.asyncParam.taskLimit) > 1500000 ? $filter('translate')('asyncParam.grid.columnName.taskLimit') : $filter('translate')('asyncParam.grid.columnName.shortQueueLimit')),
                                    limitValue: (Number($scope.asyncParam.taskLimit) > 1500000 ? $scope.asyncParam.taskLimit : $scope.asyncParam.shortQueueLimit)
                                })
                            });
                            checkAccessQDefer.resolve(false);
                            return checkAccessQDefer.promise;
                        }
                        // проверка, что "Ограничение на выполнение задания" больше, чем
                        // "Ограничение на выполнение задания в очереди быстрых заданий"
                        if ($scope.asyncParam.taskLimit !== "" && $scope.asyncParam.shortQueueLimit !== "" && Number($scope.asyncParam.taskLimit) <= Number($scope.asyncParam.shortQueueLimit)) {
                            $dialogs.errorDialog({
                                content: $filter('translate')('asyncParam.validate.checkLimit', {
                                    taskTitle: $scope.asyncParam.param.name,
                                    taskLimit: $scope.asyncParam.taskLimit,
                                    shortQueueLimit: $scope.asyncParam.shortQueueLimit
                                })
                            });
                            checkAccessQDefer.resolve(false);
                            return checkAccessQDefer.promise;
                        }
                        checkAccessQDefer.resolve(true);
                    }
                    return checkAccessQDefer.promise;
                };
            }
        ]);


}());