(function () {
    'use strict';

    /**
     * @description Модуль для создания налоговых форм
     */

    angular.module('app.createDeclaration', ['ui.router', 'app.rest', 'app.modals'])

    /**
     * @description Контроллер МО Создания налоговой формы
     */
        .controller('createDeclarationFormCtrl', ["$scope", "$rootScope", "$http", '$state', '$stateParams', "$uibModalInstance", "$filter",
            "RefBookValuesResource", 'DeclarationTypeForCreateResource', "APP_CONSTANTS",
            'data', 'appModals',
            function ($scope, $rootScope, $http, $state, $stateParams, $uibModalInstance, $filter,
                      RefBookValuesResource, DeclarationTypeForCreateResource, APP_CONSTANTS, data, appModals) {
                //По нажатию на кнопку пользователь может создать только консолидированную форму
                $scope.declarationKind = APP_CONSTANTS.NDFL_DECLARATION_KIND.CONSOLIDATED;

                $scope.periods = data.periods;

                if (data.latestSelectedPeriod) {
                    $scope.defaultPeriod = data.latestSelectedPeriod;
                } else {
                    $scope.defaultPeriod = $scope.periods[0];
                    angular.forEach($scope.periods, function (period) {
                        if (Date.parse($scope.defaultPeriod.endDate) < Date.parse(period.endDate)) {
                            $scope.defaultPeriod = period;
                        }
                    });
                }

                $scope.defaultDepartment = $rootScope.user.department;

                $scope.declarationData = {
                    department: $scope.defaultDepartment,
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
                                    name: term,
                                    reportPeriodId: $scope.declarationData.period.id,
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
                        allowClear: true,
                        placeholder: $filter('translate')('filter.placeholder.select')
                    }
                };

                /**
                 * Обновление списка видов налоговых форм при изменении периода и подразделения
                 */
                $scope.$watchGroup(['declarationData.period', 'declarationData.department'], function (newValues) {
                    if ($scope.declarationData.period && $scope.declarationData.department) {
                        DeclarationTypeForCreateResource.query({
                            declarationKind: $scope.declarationKind.id,
                            departmentId: $scope.declarationData.department.id,
                            periodId: $scope.declarationData.period.id
                        }, function (data) {
                            $scope.declarationTypeSelect.options.data.results = data;
                        });
                    }
                });

                /**
                 * Сохранение
                 */
                $scope.save = function () {
                    if ($scope.declarationData.period && $scope.declarationData.department && $scope.declarationData.declarationType) {
                        $http({
                            method: "POST",
                            url: "controller/actions/declarationData/create",
                            params: {
                                declarationTypeId: $scope.declarationData.declarationType.id,
                                departmentId: $scope.declarationData.department.id,
                                periodId: $scope.declarationData.period.id
                            }
                        }).then(function (response) {
                            $uibModalInstance.close(response);
                        });
                    } else {
                        appModals.error($filter('translate')('DIALOGS_ERROR'), $filter('translate')('createDeclaration.errorMessage'));
                    }
                };

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
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