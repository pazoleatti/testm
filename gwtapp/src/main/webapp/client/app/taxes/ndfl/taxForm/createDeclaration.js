(function () {
    'use strict';

    /**
     * @description Модуль для создания налоговых форм
     */

    angular.module('app.createDeclaration', ['ui.router', 'app.rest', 'app.formatters'])

    /**
     * @description Контроллер МО Создания налоговой формы
     */
        .controller('createDeclarationFormCtrl', ["$scope", "$http", '$state', '$stateParams', "$modalInstance", "$filter",
            "RefBookValuesResource", "APP_CONSTANTS",
            '$shareData', '$dialogs', '$webStorage',
            function ($scope, $http, $state, $stateParams, $modalInstance, $filter,
                      RefBookValuesResource, APP_CONSTANTS, $shareData, $dialogs, $webStorage) {

                $scope.selectedReportPeriod = {};
                $scope.declarationData = {
                    declarationType: APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_PRIMARY,
                    knfType: APP_CONSTANTS.KNF_TYPE.ALL
                };

                $scope.$watch("selectedReportPeriod.period", function (period) {
                    if (period) {
                        $scope.declarationData.period = period;
                    }
                });

                $scope.departmentSelectFilter = {assignedToDeclarationTypeId: $scope.declarationData.declarationType.id};
                $scope.kppSelectFilter = {};

                $scope.$watch("declarationData.declarationType", function (newValue, oldValue) {
                    if (newValue && (!oldValue || newValue.id !== oldValue.id)) {
                        var isKnf = $scope.declarationData.declarationType && $scope.declarationData.declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_CONSOLIDATED.id;
                        $scope.departmentSelectFilter.assignedToDeclarationTypeId = $scope.declarationData.declarationType.id;
                        $scope.departmentSelectFilter.onlyTB = isKnf;
                        if ($scope.declarationData.setDefaultDepartment) {
                            $scope.declarationData.setDefaultDepartment();
                        }
                    }
                    if (!newValue || oldValue && newValue.id !== oldValue.id) {
                        // обнуляем все последующие поля
                        $scope.declarationData.knfType = APP_CONSTANTS.KNF_TYPE.ALL;
                        $scope.declarationData.kppList = [];
                        $scope.declarationData.asnu = null;
                    }
                });

                $scope.$watch("declarationData.department", function (newValue, oldValue) {
                    if (newValue) {
                        $scope.$broadcast(APP_CONSTANTS.EVENTS.DEPARTMENT_SELECTED, $scope.declarationData.department.id);
                        $scope.kppSelectFilter.departmentId = newValue.id;
                        $scope.declarationData.kppList = [];
                    }
                });

                $scope.$watch("declarationData.knfType", function (newValue, oldValue) {
                    if (!newValue || oldValue && newValue.id !== oldValue.id) {
                        $scope.declarationData.kppList = [];
                    }
                });

                /**
                 * Сохранение
                 */
                $scope.save = function () {
                    // Запоминаем период выбранный пользователем
                    $webStorage.set(APP_CONSTANTS.USER_STORAGE.NAME,
                        APP_CONSTANTS.USER_STORAGE.KEYS.LAST_SELECTED_PERIOD,
                        $scope.declarationData.period,
                        true);
                    var asnuId = $scope.declarationData.asnu != null ? $scope.declarationData.asnu.id : null;
                    var params = {
                        declarationTypeId: $scope.declarationData.declarationType.id,
                        departmentId: $scope.declarationData.department.id,
                        periodId: $scope.declarationData.period.id,
                        asnuId: $scope.declarationData.asnu != null ? $scope.declarationData.asnu.id : null
                    };
                    if ($scope.declarationData.declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_PRIMARY.id && asnuId != null) {
                        params.asnuId = asnuId;
                        performSave(params);
                    } else if ($scope.declarationData.declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_CONSOLIDATED.id) {
                        params.knfType = $scope.declarationData.knfType;
                        if ($scope.declarationData.knfType.id === APP_CONSTANTS.KNF_TYPE.BY_KPP.id) {
                            params.kppList = $scope.declarationData.kppList.map(function (kppSelect) {
                                return kppSelect.kpp;
                            });
                        }
                        performSave(params);
                    }
                };

                /**
                 * Выполняет запрос на сохранение
                 * @param params
                 */
                function performSave(params) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/create",
                        params: params
                    }).then(function (response) {
                        $modalInstance.close(response);
                    }).catch(function () {
                        $modalInstance.close();
                    });
                }

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('createDeclaration.cancel.header'),
                        content: $filter('translate')('createDeclaration.cancel.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.dismiss();
                        }
                    });
                };

                $scope.notEmpty = function (list) {
                    return list && list.length > 0;
                };

                $scope.isTerBank = function (department) {
                    return department && department.type.code === 2;
                };
            }]);
}());