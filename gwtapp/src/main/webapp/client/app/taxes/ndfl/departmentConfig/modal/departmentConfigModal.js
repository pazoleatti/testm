(function () {
    'use strict';
    /**
     * @description Модуль для модального окна содания/изменения записи настроек подразделений
     */
    angular.module('app.departmentConfigModal', [])
        .controller('departmentConfigModalCtrl', ['$scope', '$filter', 'APP_CONSTANTS', '$modalInstance', '$shareData', '$dialogs', 'DepartmentConfigResource', '$http', '$logPanel',
            function ($scope, $filter, APP_CONSTANTS, $modalInstance, $shareData, $dialogs, DepartmentConfigResource, $http, $logPanel) {
                $scope.mode = $shareData.mode;

                $scope.record = $shareData.mode === 'CREATE' ? {} : $.extend(true, {}, $shareData.record);

                function resetRecord() {
                    $scope.record = $.extend(true, {}, $shareData.record);
                }

                function flushRecord() {
                    $shareData.record = $.extend(true, {}, $scope.record);
                }

                $scope.save = function () {
                    if ($scope.mode === 'CREATE') {
                        createDepartmentConfig();
                    } else if ($scope.mode === 'EDIT') {
                        updateDepartmentConfig();
                    }
                };
                // Необходимость заполнения фамилии и имени в зависимости от значения поля "признак подписанта"
                $scope.isNameRequiredBySignatoryMark = function ($value) {
                    return !(!$scope.record.signatoryMark || $scope.record.signatoryMark.code !== 2 || !!$value);
                };
                // Валидация КПП полей
                $scope.isKppValid = function ($value) {
                    var set = ["01", "02", "03", "05", "31", "32", "43", "45"];
                    return !$value || $value.length !== 9 || set.indexOf($value.substring(4, 6)) !== -1;
                };
                // Валидация дат актуальности
                $scope.isVersionDatesValid = function (dateFrom, dateTo) {
                    return !dateFrom || !dateTo || toDate(dateFrom) <= toDate(dateTo);
                };

                function toDate(value) {
                    return typeof value === 'string' ? new Date(value) : value;
                }

                // Создание записи настроек подразделений
                function createDepartmentConfig() {
                    $logPanel.close();
                    $http({
                        method: "POST",
                        url: "controller/actions/departmentConfig/create",
                        data: JSON.stringify(angular.extend({}, $scope.record, {
                            department: {id: $scope.record.department.id, name: $scope.record.department.name},
                            oktmo: {id: $scope.record.oktmo.id, code: $scope.record.oktmo.code},
                            presentPlace: {id: $scope.record.presentPlace.id},
                            signatoryMark: {id: $scope.record.signatoryMark.id},
                            reorganization: $scope.record.reorganization ? {id: $scope.record.reorganization.id} : undefined
                        }))
                    }).then(function (response) {
                        if (response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        if (response.data.error) {
                            $dialogs.errorDialog({content: response.data.error});
                        } else {
                            $shareData.refreshGrid();
                            $modalInstance.close();
                        }
                    });
                }

                // Изменение записи настроек подразделений
                function updateDepartmentConfig() {
                    $logPanel.close();
                    $http({
                        method: "POST",
                        url: "controller/actions/departmentConfig/update",
                        data: JSON.stringify(angular.extend({}, $scope.record, {
                            department: {id: $scope.record.department.id, name: $scope.record.department.name},
                            oktmo: {id: $scope.record.oktmo.id, code: $scope.record.oktmo.code},
                            presentPlace: {id: $scope.record.presentPlace.id},
                            signatoryMark: {id: $scope.record.signatoryMark.id},
                            reorganization: $scope.record.reorganization ? {id: $scope.record.reorganization.id} : undefined
                        }))
                    }).then(function (response) {
                        if (response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        if (response.data.error) {
                            $dialogs.errorDialog({content: response.data.error});
                        } else {
                            $shareData.refreshGrid();
                            flushRecord();
                            $scope.returnToView();
                        }
                    });
                }

                // Вход в режим редактирования
                $scope.edit = function () {
                    $scope.mode = 'EDIT';
                    $modalInstance.updateTitle($filter('translate')('departmentConfig.modal.edit.title'));
                };

                // Выход из режима редактирования
                $scope.returnToView = function () {
                    $scope.mode = 'VIEW';
                    $modalInstance.updateTitle($filter('translate')('departmentConfig.modal.open.title'));
                    $scope.departmentConfigForm.$setPristine();
                    initRecord();
                };

                $scope.cancel = function (close) {
                    if (($scope.mode === 'CREATE' || $scope.mode === 'EDIT') && $scope.departmentConfigForm.$dirty) {
                        $dialogs.confirmDialog({
                            title: $filter('translate')('title.confirm'),
                            content: $filter('translate')('departmentConfig.confirm.cancelEdit'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                if (close || $scope.mode !== 'EDIT') {
                                    $modalInstance.close();
                                } else {
                                    $scope.returnToView();
                                    resetRecord();
                                }
                            }
                        });
                    } else {
                        if (close || $scope.mode !== 'EDIT') {
                            $modalInstance.close();
                        } else {
                            $scope.returnToView();
                            resetRecord();
                        }
                    }
                };

                // срабатывает при нажатии на крестик в модальном окне
                $scope.modalCloseCallback = function () {
                    $scope.cancel();
                };
            }]);
}());