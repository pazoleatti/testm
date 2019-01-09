(function () {
    'use strict';

    /**
     * @description Модуль для создания отчетности
     */
    angular.module('app.createReport', ['app.constants', 'app.rest', 'app.formatters'])

    /**
     * @description Контроллер окна "Создание отчетности"
     */
        .controller('createReportCtrl', [
            '$http', '$scope', '$rootScope', '$filter', '$dialogs', '$modalInstance', 'APP_CONSTANTS', '$shareData', '$webStorage',
            function ($http, $scope, $rootScope, $filter, $dialogs, $modalInstance, APP_CONSTANTS, $shareData, $webStorage) {

                $scope.knf = angular.copy($shareData.knf);
                $scope.reportData = {negativeValuesAdjustment: APP_CONSTANTS.NEGATIVE_VALUE_ADJUSTMENT.NOT_CORRECT};

                if ($scope.knf) {
                    $scope.reportData.department = {id: $scope.knf.departmentId, name: $scope.knf.department};
                    $scope.reportData.period = {id: $scope.knf.reportPeriodId, name: $scope.knf.reportPeriod};
                } else {
                    $scope.reportFormKind = [APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id];
                    $scope.userTBDepartment = {};

                    $scope.hasLoaded = false;

                    $scope.depErased = false;
                    $scope.reportErased = false;


                    $scope.$watch("userTBDepartment.department", function (department) {
                        if (typeof(department) !== 'undefined' && department != null) {
                            $scope.reportData.department = department;
                        }
                    });

                    $scope.$watch("reportData.department", function (department) {
                        if (typeof(department) !== 'undefined' && department != null) {
                            $scope.$broadcast(APP_CONSTANTS.EVENTS.DEPARTMENT_SELECTED, $scope.reportData.department.id);
                            $scope.depErased = false;
                            $scope.showDepError = false;
                            $scope.showFormError = false
                        } else if (department === null) {
                            $scope.depErased = true;
                            $scope.reportData.period = null;
                            $scope.reportData.declarationType = null;
                        }
                    });

                    $scope.$watch("reportData.period", function (period) {
                        if (typeof(period) !== 'undefined' && period != null) {
                            $scope.$broadcast(APP_CONSTANTS.EVENTS.DEPARTMENT_AND_PERIOD_SELECTED, $scope.reportData.period.id, $scope.reportData.department.id);
                            $scope.hasLoaded = true;
                            $scope.reportErased = false;
                            $scope.showPeriodError = false;
                            $scope.showFormError = false
                        } else if (period === null) {
                            $scope.reportErased = true;
                            $scope.reportData.declarationType = null;
                        }

                    });
                }

                $scope.$watch("reportData.declarationType", function (declarationType) {
                    if (typeof(declarationType) !== 'undefined' && declarationType != null) {
                        $scope.showFormError = false;
                    }
                });

                /**
                 * Создание отчётности
                 */
                $scope.save = function () {
                    $scope.showDepError = $scope.isValueAbsent($scope.reportData.department);
                    $scope.showPeriodError = $scope.isValueAbsent($scope.reportData.period);
                    $scope.showFormError = $scope.isValueAbsent($scope.reportData.declarationType);
                    // Запоминаем период выбранный пользователем
                    $webStorage.set(APP_CONSTANTS.USER_STORAGE.NAME,
                        APP_CONSTANTS.USER_STORAGE.KEYS.LAST_SELECTED_PERIOD,
                        $scope.reportData.period,
                        true);
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/createReport",
                        data: {
                            knfId: $scope.knf ? $scope.knf.id : undefined,
                            declarationTypeId: $scope.reportData.declarationType.id,
                            departmentId: $scope.knf ? undefined : $scope.reportData.department.id,
                            periodId: $scope.knf ? undefined : $scope.reportData.period.id,
                            adjustNegativeValues: $scope.reportData.negativeValuesAdjustment === APP_CONSTANTS.NEGATIVE_VALUE_ADJUSTMENT.CORRECT
                        }
                    }).then(function (response) {
                        $modalInstance.close(response);
                    }).catch(function () {
                        $modalInstance.close();
                    });
                };
                /**
                 * Закрытие окна
                 */
                $scope.cancel = function () {
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

                /**
                 * Проверят что значение отсутствует
                 * @param validatedValue проверяемое значение
                 * @returns {boolean} {@code true} если значение отсутствует
                 */
                $scope.isValueAbsent = function (validatedValue) {
                    if (typeof(validatedValue) === 'undefined' || validatedValue == null) {
                        return true
                    }
                }
            }]
        );
}());