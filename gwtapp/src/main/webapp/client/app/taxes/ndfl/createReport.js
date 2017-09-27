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

                $scope.periods = data.periods;
                // Определяем самый поздний период и подразделение для формирования отчённости
                $scope.defaultPeriod = $scope.periods[0];
                angular.forEach($scope.periods, function (period) {
                    if (Date.parse($scope.defaultPeriod.endDate) < Date.parse(period.endDate)) {
                        $scope.defaultPeriod = period;
                    }
                });

                $scope.reportData = {
                    department: $rootScope.user.department,
                    period: $scope.defaultPeriod
                };

                $scope.periodSelect = {
                    options: {
                        data: {
                            results: $scope.periods,
                            text: $filter('periodFormatter')
                        },
                        formatSelection: $filter('periodFormatter'),
                        formatResult: $filter('periodFormatter'),
                        multiple: false,
                        allowClear: true,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };

                $scope.departmentsSelect = {
                    options: {
                        ajax: {
                            url: "controller/rest/refBookValues/30?projection=departmentsWithOpenPeriod",
                            quietMillis: 200,
                            data: function (term, page) {
                                return {
                                    filter: JSON.stringify({name: term}),
                                    reportPeriodId: $scope.reportData.period.id,
                                    pagingParams: JSON.stringify({count: 50, page: page})
                                };
                            },
                            results: function (data, page) {
                                var more = (page * 50) < data.records;
                                return {results: data.rows, more: more};
                            }
                        },
                        formatSelection: $filter('nameFormatter'),
                        formatResult: $filter('nameFormatter'),
                        multiple: false,
                        allowClear: true,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };
                $scope.declarationTypeSelect = {
                    options: {
                        data: {
                            results: [],
                            text: $filter('nameFormatter')
                        },
                        formatSelection: $filter('nameFormatter'),
                        formatResult: $filter('nameFormatter'),
                        multiple: false,
                        allowClear: true,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };
                RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.DECLARATION_TYPE}, function (data) {
                    $scope.declarationTypeSelect.options.data.results = data;
                });

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