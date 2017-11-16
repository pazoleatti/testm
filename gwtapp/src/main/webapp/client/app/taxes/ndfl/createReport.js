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
                $scope.reportFormKind = [APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id];

                //Отчетный период из списка периодов в выпадающем списке, у которого самая поздняя дата окончания
                $scope.latestReportPeriod = {};
                $scope.reportData = {};

                $scope.userTBDepartment = {
                    id: $rootScope.user.department.parentId
                };

                if ($shareData.latestSelectedPeriod) {
                    $scope.reportData.period = $shareData.latestSelectedPeriod;
                } else {
                    $scope.$watch("latestReportPeriod.period", function (value) {
                        $scope.reportData.period = value;
                    });
                }

                $scope.$watch("userTBDepartment.department", function (department) {
                    $scope.reportData.department = department;
                });
                /**
                 * Создание отчётности
                 */
                $scope.save = function () {
                    // Запоминаем период выбранный пользователем
                    $webStorage.set(APP_CONSTANTS.USER_STORAGE.NAME,
                        APP_CONSTANTS.USER_STORAGE.KEYS.LAST_SELECTED_PERIOD,
                        $scope.reportData.period,
                        true);
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/createReport",
                        params: {
                            declarationTypeId: $scope.reportData.declarationType.id,
                            departmentId: $scope.reportData.department.id,
                            periodId: $scope.reportData.period.id
                        }
                    }).then(function (response) {
                        $modalInstance.close(response);
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
                            $modalInstance.dismiss('Canceled');
                        }
                    });
                };
            }])
        .filter('nameFormatter', function () {
            return function (entity) {
                return entity ? entity.name : "";
            };
        })
        .filter('periodFormatter', function () {
            return function (entity) {
                return entity ? entity.taxPeriod.year + ": " + entity.name : "";
            };
        });

}());