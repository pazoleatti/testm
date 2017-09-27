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
            '$http', '$scope', '$rootScope', '$filter', 'appModals', '$uibModalInstance', 'RefBookValuesResource', 'APP_CONSTANTS', 'data',
            function ($http, $scope, $rootScope, $filter, appModals, $uibModalInstance, RefBookValuesResource, APP_CONSTANTS, data) {
                $scope.reportFormKind = APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS;

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

                function checkFields() {
                    return $scope.reportData.period !== null
                        && $scope.reportData.department !== null
                        && $scope.reportData.declarationType !== null
                }

                $scope.save = function () {
                    if (checkFields()) {
                        $http({
                            method: "POST",
                            url: "controller/actions/declarationDate/createReports",
                            params: {
                                declarationTypeId: $scope.reportData.declarationType.id,
                                departmentId: $scope.reportData.department.id,
                                periodId: $scope.reportData.period.id
                            }
                        }).then(function (response) {
                            $uibModalInstance.close(response);
                        });
                    } else {
                        appModals.error($filter('translate')('DIALOGS_ERROR'), $filter('translate')('ndflReportJournal.message.emptyFilterFields'))
                    }
                };
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