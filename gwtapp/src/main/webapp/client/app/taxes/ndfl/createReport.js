(function () {
    'use strict';

    /**
     * @description Модуль для создания отчетности
     */
    angular.module('app.createReport', ['app.constants', 'app.modals', 'app.rest'])

    /**
     * @description Контроллер окна "Создание отчетности"
     */
        .controller('createReportCtrl', [
            '$scope', '$filter', 'appModals', '$uibModalInstance', 'RefBookValuesResource', 'APP_CONSTANTS',
            function ($scope, $filter, appModals, $uibModalInstance, RefBookValuesResource, APP_CONSTANTS) {
            $scope.save = function () {
                if (checkFields()) {
                    $uibModalInstance.close({
                        period: $scope.createReportFilter.period,
                        department: $scope.createReportFilter.department,
                        declarationType: $scope.createReportFilter.declarationType
                    })
                } else {
                    appModals.message($filter('translate')('DIALOGS_NOTIFICATION'), $filter('translate')('ndflReportJournal.message.emptyFilterFields'))
                }
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('Canceled')
            };

                $scope.periodSelect = {
                    options: {
                        data: {
                            results: [],
                            text: $filter('periodFormatter')
                        },
                        formatSelection: $filter('periodFormatter'),
                        formatResult: $filter('periodFormatter'),
                        multiple: false,
                        allowClear: true,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };
                RefBookValuesResource.query({refBookId: APP_CONSTANTS.REFBOOK.PERIOD}, function (data) {
                    $scope.periodSelect.options.data.results = data;
                });

                $scope.departmentsSelect = {
                    options: {
                        ajax: {
                            url: "controller/rest/refBookValues/30",
                            quietMillis: 200,
                            data: function (term, page) {
                                return {
                                    filter: JSON.stringify({name: term}),
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
                    return $scope.createReportFilter.period !== null
                        && $scope.createReportFilter.department !== null
                        && $scope.createReportFilter.declarationType !== null
                }
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