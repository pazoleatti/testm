(function () {
    'use strict';

    /**
     * @description Модуль для создания налоговых форм
     */

    angular.module('app.createDeclaration', ['ui.router', 'app.rest', 'app.formatters'])

    /**
     * @description Контроллер МО Создания налоговой формы
     */
        .controller('createDeclarationFormCtrl', ["$scope", "$rootScope", "$http", '$state', '$stateParams', "$modalInstance", "$filter",
            "RefBookValuesResource", 'DeclarationTypeForCreateResource', "APP_CONSTANTS",
            '$shareData', '$dialogs', '$webStorage',
            function ($scope, $rootScope, $http, $state, $stateParams, $modalInstance, $filter,
                      RefBookValuesResource, DeclarationTypeForCreateResource, APP_CONSTANTS, $shareData, $dialogs, $webStorage) {
                //По нажатию на кнопку пользователь может создать только консолидированную форму
                $scope.declarationKind = [APP_CONSTANTS.NDFL_DECLARATION_KIND.PRIMARY.id, APP_CONSTANTS.NDFL_DECLARATION_KIND.CONSOLIDATED.id];

                //Отчетный период из списка периодов в выпадающем списке, у которого самая поздняя дата окончания
                $scope.latestReportPeriod = {};

                $scope.declarationData = {
                    department: $rootScope.user.department
                };

                if ($shareData.latestSelectedPeriod) {
                    $scope.declarationData.period = $shareData.latestSelectedPeriod;
                } else {
                    $scope.$watch("latestReportPeriod.period", function (value) {
                        $scope.declarationData.period = value;
                    });
                }

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
                    }).catch(function() {
                        $modalInstance.close();
                    });
                }

                /**
                 * Проверка, является ли выбранный период корректирующим
                 */
                $scope.$watchGroup(['declarationData.period', 'declarationData.department'], function (newValues) {
                    if ($scope.declarationData.period && $scope.declarationData.department) {
                        $http({
                            method: "GET",
                            url: "controller//rest/departmentReportPeriod",
                            params: {
                                projection: "fetchLast",
                                departmentId: $scope.declarationData.department.id,
                                reportPeriodId: $scope.declarationData.period.id
                            }
                        }).success(function (departmentPeportPeriod) {
                            $scope.correctionDate = departmentPeportPeriod.correctionDate;
                            $scope.correctionPeriod = departmentPeportPeriod.correctionDate !== undefined && departmentPeportPeriod.correctionDate !== null;
                        });
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
                        manuallyCreated: true,
                        asnuId: $scope.declarationData.asnu != null ? $scope.declarationData.asnu.id : null
                    };
                    if ($scope.declarationData.declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_PRIMARY.id && asnuId != null) {
                        params.asnuId = asnuId;
                        performSave(params);
                    } else if ($scope.declarationData.declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_CONSOLIDATED.id) {
                        performSave(params);
                    }
                };

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

                /**
                 * Условие выбора АСНУ
                 */
                $scope.allowSetAsnu = function () {
                    return !!($scope.declarationData.declarationType && $scope.declarationData.declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_PRIMARY.id);

                };
            }]);
}());