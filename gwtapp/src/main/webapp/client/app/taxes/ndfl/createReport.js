(function () {
    'use strict';

    /**
     * @description Модуль для создания отчетности
     */
    angular.module('app.createReport', ['app.constants', 'app.modals', 'app.rest', 'app.formatters'])

    /**
     * @description Контроллер окна "Создание отчетности"
     */
        .controller('createReportCtrl', [
            '$http', '$scope', '$rootScope', '$filter', 'appModals', '$uibModalInstance', 'APP_CONSTANTS', 'data',
            function ($http, $scope, $rootScope, $filter, appModals, $uibModalInstance, APP_CONSTANTS, data) {
                $scope.reportFormKind = APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS;

                //Отчетный период из списка периодов в выпадающем списке, у которого самая поздняя дата окончания
                $scope.latestReportPeriod = {};

                $scope.reportData = {
                    department: $rootScope.user.department
                };

                if (data.latestSelectedPeriod) {
                    $scope.reportData.period = data.latestSelectedPeriod;
                } else {
                    $scope.$watch("latestReportPeriod.period", function (value) {
                        $scope.reportData.period = value;
                    });
                }
                /**
                 * Создание отчётности
                 */
                $scope.save = function () {
                    // Запоминаем период выбранный пользователем
                    $rootScope.latestSelectedPeriod = $scope.reportData.period;
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/createReport",
                        params: {
                            declarationTypeId: $scope.reportData.declarationType.id,
                            departmentId: $scope.reportData.department.id,
                            periodId: $scope.reportData.period.id
                        }
                    }).then(function (response) {
                        $uibModalInstance.close(response);
                    });
                };
                /**
                 * Закрытие окна
                 */
                $scope.cancel = function () {
                    appModals.confirm($filter('translate')('createDeclaration.cancel.header'), $filter('translate')('createDeclaration.cancel.text'))
                        .result.then(function () {
                        $uibModalInstance.dismiss('Canceled');
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