(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Сведения о вычетах"
     */
    angular.module('app.deduction', [])

    /**
     * @description Контроллер вкладки "Сведения о вычетах"
     */
        .controller('deductionCtrl', [
            '$scope', '$timeout', '$state', '$stateParams', '$http', 'NdflPersonResource', '$filter', '$rootScope',
            function ($scope, $timeout, $state, $stateParams, $http, NdflPersonResource, $filter, $rootScope) {

                $scope.$on('INP_CHANGED', function(event, data) {
                    if (!_.isEqual($scope.searchFilter.params.inp, data)){
                        $scope.searchFilter.params.inp = data;
                        $scope.submitSearch();
                    }
                });

                $scope.$on('OPERATION_ID_CHANGED', function(event, data) {
                    if (!_.isEqual($scope.searchFilter.params.operationId, data)){
                        $scope.searchFilter.params.operationId = data;
                        $scope.submitSearch();
                    }
                });

                $scope.$on('tabSelected', function(event, data) {
                    if (_.isEqual(data, 'deductions')){
                        $scope.submitSearch();
                    }
                });

                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.deductionGrid.ctrl.refreshGrid(page);
                };

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {},
                    isClear: false,
                    filterName: 'deductionFilter'
                };

                $scope.deductionGrid =
                    {
                        ctrl: {},
                        value: [],
                        options: {
                            datatype: "angularResource",
                            angularResource: NdflPersonResource,
                            requestParameters: function () {
                                return {
                                    projection: "personsDeduction",
                                    ndflPersonDeductionFilter: JSON.stringify({
                                        declarationDataId: $stateParams.declarationDataId,
                                        inp: $scope.searchFilter.params.inp && $scope.searchFilter.params.inp !== "" ? $scope.searchFilter.params.inp : undefined,
                                        operationId: $scope.searchFilter.params.operationId && $scope.searchFilter.params.operationId !== "" ? $scope.searchFilter.params.operationId : undefined,
                                        deductionCode: $scope.searchFilter.params.deductionCode && $scope.searchFilter.params.deductionCode !== "" ? $scope.searchFilter.params.deductionCode : undefined,
                                        incomeCode: $scope.searchFilter.params.incomeCode && $scope.searchFilter.params.incomeCode !== "" ? $scope.searchFilter.params.incomeCode : undefined,
                                        calculationDateFrom: $scope.searchFilter.params.calculationDateFrom,
                                        calculationDateTo: $scope.searchFilter.params.calculationDateTo,
                                        deductionDateFrom: $scope.searchFilter.params.deductionDateFrom,
                                        deductionDateTo: $scope.searchFilter.params.deductionDateTo
                                    })
                                };
                            },
                            height: 250,
                            colNames: [
                                $filter('translate')('title.numberpp'),
                                $filter('translate')('title.inp'),
                                $filter('translate')('title.deductionCode'),
                                $filter('translate')('title.notifType'),
                                $filter('translate')('title.notifDate'),
                                $filter('translate')('title.notifNum'),
                                $filter('translate')('title.notifSource'),
                                $filter('translate')('title.notifSumm'),
                                $filter('translate')('title.incomeIdOperation'),
                                $filter('translate')('title.incomeAccrued'),
                                $filter('translate')('title.income.incomeCode'),
                                $filter('translate')('title.incomeSumm'),
                                $filter('translate')('title.periodPrevDate'),
                                $filter('translate')('title.periodPrevSumm'),
                                $filter('translate')('title.periodCurrDate'),
                                $filter('translate')('title.periodCurrSumm')],
                            colModel: [
                                {name: 'rowNum', index: 'rowNum', width: 60, key: true},
                                {name: 'inp', index: 'inp', width: 170},
                                {name: 'typeCode', index: 'typeCode', width: 200},
                                {name: 'notifType', index: 'notifType', width: 175},
                                {
                                    name: 'notifDate',
                                    index: 'notifDate',
                                    width: 200,
                                    formatter: $filter('dateFormatter')
                                },
                                {name: 'notifNum', index: 'notifNum', width: 240},
                                {name: 'notifSource', index: 'notifSource', width: 240},
                                {name: 'notifSumm', index: 'notifSumm', width: 190},
                                {name: 'operationId', index: 'operationId', width: 185},
                                {
                                    name: 'incomeAccrued',
                                    index: 'incomeAccrued',
                                    width: 95,
                                    formatter: $filter('dateFormatter')
                                },
                                {name: 'incomeCode', index: 'incomeCode', width: 195},
                                {name: 'incomeSumm', index: 'incomeSumm', width: 85},
                                {
                                    name: 'periodPrevDate',
                                    index: 'periodPrevDate',
                                    width: 95,
                                    formatter: $filter('dateFormatter')
                                },
                                {name: 'periodPrevSumm', index: 'periodPrevSumm', width: 100},
                                {
                                    name: 'periodCurrDate',
                                    index: 'periodCurrDate',
                                    width: 205,
                                    formatter: $filter('dateFormatter')
                                },
                                {name: 'periodCurrSumm', index: 'periodCurrSumm', width: 170}
                            ],
                            rowNum: 10,
                            rowList: [10, 20, 30],
                            sortname: 'rowNum',
                            viewrecords: true,
                            sortorder: "asc",
                            hidegrid: false,
                            multiselect: true
                        }
                    };

                /**
                 * @description Поиск по фильтру
                 */
                $scope.submitSearch = function () {
                    $scope.searchFilter.ajaxFilter = [];
                    $scope.searchFilter.fillFilterParams();
                    $scope.refreshGrid(1);
                    $scope.searchFilter.isClear = !_.isEmpty($scope.searchFilter.ajaxFilter);
                };

                /**
                 * @description сброс фильтра
                 */
                $scope.resetFilter = function () {
                    $rootScope.$broadcast('INP_CHANGED', $scope.searchFilter.params.inp);
                    $rootScope.$broadcast('OPERATION_ID_CHANGED', $scope.searchFilter.params.operationId);
                    /* очистка всех инпутов на форме */
                    $scope.searchFilter.params = {};

                    /* убираем надпись "Сброс" */
                    $scope.isClear = false;

                    $scope.submitSearch();
                };

                /**
                 * @description Заполнение ajaxFilter
                 */
                $scope.searchFilter.fillFilterParams = function () {
                    if ($scope.searchFilter.params.inp) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "inp",
                            value: $scope.searchFilter.params.inp
                        });
                    }
                    if ($scope.searchFilter.params.operationId) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "operationId",
                            value: $scope.searchFilter.params.operationId
                        });
                    }
                    if ($scope.searchFilter.params.deductionCode) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "typeCode",
                            value: $scope.searchFilter.params.deductionCode
                        });
                    }
                    if ($scope.searchFilter.params.incomeCode) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "incomeCode",
                            value: $scope.searchFilter.params.incomeCode
                        });
                    }
                    if ($scope.searchFilter.params.calculationDateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "incomeAccrued",
                            value: $scope.searchFilter.params.calculationDateFrom
                        });
                    }
                    if ($scope.searchFilter.params.calculationDateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "incomeAccrued",
                            value: $scope.searchFilter.params.calculationDateTo
                        });
                    }
                    if ($scope.searchFilter.params.deductionDateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "periodCurrDate",
                            value: $scope.searchFilter.params.deductionDateFrom
                        });
                    }
                    if ($scope.searchFilter.params.deductionDateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "periodCurrDate",
                            value: $scope.searchFilter.params.deductionDateTo
                        });
                    }
                };
            }])

}());