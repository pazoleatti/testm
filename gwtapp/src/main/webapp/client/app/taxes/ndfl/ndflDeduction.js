(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Сведения о вычетах"
     */
    angular.module('sbrfNdfl.deduction', [])

        /**
         * @description Контроллер вкладки "Сведения о вычетах"
         */
        .controller('deductionCtrl', [
            '$scope', '$timeout', '$state', '$stateParams', 'dialogs', '$http', 'NdflPersonResource', '$filter',
            function ($scope, $timeout, $state, $stateParams, dialogs, $http, NdflPersonResource, $filter) {
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
                                projection: "getPersonsDeduction",
                                ndflPersonDeductionFilter: JSON.stringify({
                                    declarationDataId: $stateParams.formId,
                                    inp: $scope.searchFilter.params.inp,
                                    operationId: $scope.searchFilter.params.operationId,
                                    deductionCode: $scope.searchFilter.params.deductionCode,
                                    incomeCode: $scope.searchFilter.params.incomeCode,
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
                            {name: 'rowNum', index: 'np.rowNum', width: 60, key: true},
                            {name: 'inp', index: 'np.inp', width: 170},
                            {name: 'typeCode', index: 'np.typeCode', width: 200},
                            {name: 'notifType', index: 'np.notifType', width: 175},
                            {name: 'notifDate', index: 'np.notifDate', width: 200, formatter: $filter('dateFormatter')},
                            {name: 'notifNum', index: 'np.notifNum', width: 240},
                            {name: 'notifSource', index: 'np.notifSource', width: 240},
                            {name: 'notifSumm', index: 'np.notifSumm', width: 190},
                            {name: 'operationId', index: 'np.operationId', width: 185},
                            {name: 'incomeAccrued', index: 'np.incomeAccrued', width: 95, formatter: $filter('dateFormatter')},
                            {name: 'incomeCode', index: 'np.incomeCode', width: 195},
                            {name: 'incomeSumm', index: 'np.incomeSumm', width: 85},
                            {name: 'periodPrevDate', index: 'np.periodPrevDate', width: 95, formatter: $filter('dateFormatter')},
                            {name: 'periodPrevSumm', index: 'np.periodPrevSumm', width: 100},
                            {name: 'periodCurrDate', index: 'np.periodCurrDate', width: 205, formatter: $filter('dateFormatter')},
                            {name: 'periodCurrSumm', index: 'np.periodCurrSumm', width: 170}
                        ],
                        rowNum: 10,
                        rowList: [10, 20, 30],
                        sortname: 'np.rowNum',
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
                    fillSearchFilter();
                    $scope.refreshGrid(1);
                };

                /**
                 * @description сброс фильтра
                 */
                $scope.resetFilter = function () {
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
                            property: "np.inp",
                            value: $scope.searchFilter.params.inp
                        });
                    }
                    if ($scope.searchFilter.params.operationId) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.operationId",
                            value: $scope.searchFilter.params.operationId
                        });
                    }
                    if ($scope.searchFilter.params.deductionCode) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.typeCode",
                            value: $scope.searchFilter.params.deductionCode
                        });
                    }
                    if ($scope.searchFilter.params.incomeCode) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.incomeCode",
                            value: $scope.searchFilter.params.incomeCode
                        });
                    }
                    if ($scope.searchFilter.params.calculationDateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.incomeAccrued",
                            value: $scope.searchFilter.params.calculationDateFrom
                        });
                    }
                    if ($scope.searchFilter.params.calculationDateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.incomeAccrued",
                            value: $scope.searchFilter.params.calculationDateTo
                        });
                    }
                    if ($scope.searchFilter.params.deductionDateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.periodCurrDate",
                            value: $scope.searchFilter.params.deductionDateFrom
                        });
                    }
                    if ($scope.searchFilter.params.deductionDateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.periodCurrDate",
                            value: $scope.searchFilter.params.deductionDateTo
                        });
                    }
                }
            }])

}());