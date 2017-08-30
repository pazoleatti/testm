(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Сведения о доходах и НДФЛ"
     */
    angular.module('app.incomesAndTax', [])

    /**
     * @description Контроллер вкладки "Сведения о доходах и НДФЛ"
     */
        .controller('incomesAndTaxCtrl', [
            '$scope', '$timeout', '$state', '$stateParams', '$http', 'NdflPersonResource', '$filter', 'ShowToDoDialog', '$rootScope',
            function ($scope, $timeout, $state, $stateParams, $http, NdflPersonResource, $filter, $showToDoDialog, $rootScope) {

                $scope.$on('INP_CHANGED', function(event, data) {
                    if (!_.isEqual($scope.searchFilter.params.inp, data)){
                        $scope.searchFilter.params.inp = data;
                    }
                });

                $scope.$on('OPERATION_ID_CHANGED', function(event, data) {
                    if (!_.isEqual($scope.searchFilter.params.operationId, data)){
                        $scope.searchFilter.params.operationId = data;
                    }
                });

                $scope.$on('tabSelected', function(event, data) {
                    if (_.isEqual(data, 'incomesAndTax')){
                        $scope.submitSearch();
                    }
                });

                $scope.showToDoDialog = function () {
                    $showToDoDialog();
                };

                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.incomesAndTaxGrid.ctrl.refreshGrid(page);
                };

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {},
                    isClear: false,
                    filterName: 'incomesAndTaxFilter'
                };

                $scope.incomesAndTaxGrid =
                    {
                        ctrl: {},
                        value: [],
                        options: {
                            datatype: "angularResource",
                            angularResource: NdflPersonResource,
                            requestParameters: function () {
                                return {
                                    projection: "personsIncome",
                                    ndflPersonIncomeFilter: JSON.stringify({
                                        declarationDataId: $stateParams.declarationDataId,
                                        inp: $scope.searchFilter.params.inp,
                                        operationId: $scope.searchFilter.params.operationId,
                                        kpp: $scope.searchFilter.params.kpp,
                                        oktmo: $scope.searchFilter.params.oktmo,
                                        incomeCode: $scope.searchFilter.params.incomeCode,
                                        incomeAttr: $scope.searchFilter.params.incomeAttr,
                                        taxRate: $scope.searchFilter.params.taxRate,
                                        numberPaymentOrder: $scope.searchFilter.params.numberPaymentOrder,
                                        transferDateFrom: $scope.searchFilter.params.transferDateFrom,
                                        transferDateTo: $scope.searchFilter.params.transferDateTo,
                                        calculationDateFrom: $scope.searchFilter.params.calculationDateFrom,
                                        calculationDateTo: $scope.searchFilter.params.calculationDateTo,
                                        paymentDateFrom: $scope.searchFilter.params.paymentDateFrom,
                                        paymentDateTo: $scope.searchFilter.params.paymentDateTo
                                    })
                                };
                            },
                            height: 250,
                            colNames: [
                                $filter('translate')('title.numberpp'),
                                $filter('translate')('title.inp'),
                                $filter('translate')('title.idOperation'),
                                $filter('translate')('title.incomeCode'),
                                $filter('translate')('title.incomeAttr'),
                                $filter('translate')('title.dateCalcIncome'),
                                $filter('translate')('title.datePaymentIncome'),
                                $filter('translate')('title.kpp'),
                                $filter('translate')('title.oktmo'),
                                $filter('translate')('title.amountCalcIncome'),
                                $filter('translate')('title.amountPaymentIncome'),
                                $filter('translate')('title.amountDeduction'),
                                $filter('translate')('title.taxBase'),
                                $filter('translate')('title.taxRateWithPercent'),
                                $filter('translate')('title.taxDate'),
                                $filter('translate')('title.calculatedTax'),
                                $filter('translate')('title.withholdingTax'),
                                $filter('translate')('title.notHoldingTax'),
                                $filter('translate')('title.overholdingTax'),
                                $filter('translate')('title.refoundTax'),
                                $filter('translate')('title.taxTransferDate'),
                                $filter('translate')('title.paymentDate'),
                                $filter('translate')('title.paymentNumber'),
                                $filter('translate')('title.taxSumm')],
                            colModel: [
                                {name: 'rowNum', index: 'rowNum', width: 60, key: true},
                                {name: 'inp', index: 'inp', width: 170},
                                {name: 'operationId', index: 'operationId', width: 200},
                                {name: 'incomeCode', index: 'incomeCode', width: 175},
                                {name: 'incomeType', index: 'incomeType', width: 200},
                                {
                                    name: 'incomeAccruedDate',
                                    index: 'incomeAccruedDate',
                                    width: 240,
                                    formatter: $filter('dateFormatter')
                                },
                                {
                                    name: 'incomePayoutDate',
                                    index: 'incomePayoutDate',
                                    width: 240,
                                    formatter: $filter('dateFormatter')
                                },
                                {name: 'kpp', index: 'kpp', width: 190, sortable: false},
                                {name: 'oktmo', index: 'oktmo', width: 185},
                                {name: 'incomeAccruedSumm', index: 'incomeAccruedSumm', width: 95},
                                {name: 'incomePayoutSumm', index: 'incomePayoutSumm', width: 195},
                                {name: 'totalDeductionsSumm', index: 'totalDeductionsSumm', width: 85},
                                {name: 'taxBase', index: 'taxBase', width: 95},
                                {name: 'taxRate', index: 'taxRate', width: 100},
                                {name: 'taxDate', index: 'taxDate', width: 205, formatter: $filter('dateFormatter')},
                                {name: 'calculatedTax', index: 'calculatedTax', width: 170},
                                {name: 'withholdingTax', index: 'withholdingTax', width: 155},
                                {name: 'notHoldingTax', index: 'notHoldingTax', width: 165},
                                {name: 'overholdingTax', index: 'overholdingTax', width: 240},
                                {name: 'refoundTax', index: 'refoundTax', width: 240},
                                {
                                    name: 'taxTransferDate',
                                    index: 'taxTransferDate',
                                    width: 145,
                                    formatter: $filter('dateFormatter')
                                },
                                {
                                    name: 'paymentDate',
                                    index: 'paymentDate',
                                    width: 170,
                                    formatter: $filter('dateFormatter')
                                },
                                {name: 'paymentNumber', index: 'paymentNumber', width: 205},
                                {name: 'taxSumm', index: 'taxSumm', width: 205}
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
                    if ($scope.searchFilter.params.kpp) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "kpp",
                            value: $scope.searchFilter.params.kpp
                        });
                    }
                    if ($scope.searchFilter.params.oktmo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "oktmo",
                            value: $scope.searchFilter.params.oktmo
                        });
                    }
                    if ($scope.searchFilter.params.incomeCode) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "incomeCode",
                            value: $scope.searchFilter.params.incomeCode
                        });
                    }
                    if ($scope.searchFilter.params.incomeType) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "incomeType",
                            value: $scope.searchFilter.params.incomeType
                        });
                    }
                    if ($scope.searchFilter.params.taxRate) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "taxRate",
                            value: $scope.searchFilter.params.taxRate
                        });
                    }
                    if ($scope.searchFilter.params.numberPaymentOrder) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "paymentNumber",
                            value: $scope.searchFilter.params.numberPaymentOrder
                        });
                    }
                    if ($scope.searchFilter.params.transferDateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "taxTransferDate",
                            value: $scope.searchFilter.params.transferDateFrom
                        });
                    }
                    if ($scope.searchFilter.params.transferDateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "taxTransferDate",
                            value: $scope.searchFilter.params.transferDateTo
                        });
                    }
                    if ($scope.searchFilter.params.calculationDateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "taxDate",
                            value: $scope.searchFilter.params.calculationDateFrom
                        });
                    }
                    if ($scope.searchFilter.params.calculationDateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "taxDate",
                            value: $scope.searchFilter.params.calculationDateTo
                        });
                    }
                    if ($scope.searchFilter.params.paymentDateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "paymentDate",
                            value: $scope.searchFilter.params.paymentDateFrom
                        });
                    }
                    if ($scope.searchFilter.params.paymentDateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "paymentDate",
                            value: $scope.searchFilter.params.paymentDateTo
                        });
                    }
                };

            }])

}());