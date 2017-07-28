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
            '$scope', '$timeout', '$state', '$stateParams', 'dialogs', '$http', 'NdflPersonResource', '$filter', 'ShowToDoDialog',
            function ($scope, $timeout, $state, $stateParams, dialogs, $http, NdflPersonResource, $filter, $showToDoDialog) {

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
                                    declarationDataId: $stateParams.declarationId,
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
                            {name: 'rowNum', index: 'np.rowNum', width: 60, key: true},
                            {name: 'inp', index: 'np.inp', width: 170},
                            {name: 'operationId', index: 'np.operationId', width: 200},
                            {name: 'incomeCode', index: 'np.incomeCode', width: 175},
                            {name: 'incomeType', index: 'np.incomeType', width: 200},
                            {name: 'incomeAccruedDate', index: 'np.incomeAccruedDate', width: 240, formatter: $filter('dateFormatter')},
                            {name: 'incomePayoutDate', index: 'np.incomePayoutDate', width: 240, formatter: $filter('dateFormatter')},
                            {name: 'kpp', index: 'np.kpp', width: 190, sortable: false},
                            {name: 'oktmo', index: 'np.oktmo', width: 185},
                            {name: 'incomeAccruedSumm', index: 'np.incomeAccruedSumm', width: 95},
                            {name: 'incomePayoutSumm', index: 'np.incomePayoutSumm', width: 195},
                            {name: 'totalDeductionsSumm', index: 'np.totalDeductionsSumm', width: 85},
                            {name: 'taxBase', index: 'np.taxBase', width: 95},
                            {name: 'taxRate', index: 'np.taxRate', width: 100},
                            {name: 'taxDate', index: 'np.taxDate', width: 205, formatter: $filter('dateFormatter')},
                            {name: 'calculatedTax', index: 'np.calculatedTax', width: 170},
                            {name: 'withholdingTax', index: 'np.withholdingTax', width: 155},
                            {name: 'notHoldingTax', index: 'np.notHoldingTax', width: 165},
                            {name: 'overholdingTax', index: 'np.overholdingTax', width: 240},
                            {name: 'refoundTax', index: 'np.refoundTax', width: 240},
                            {name: 'taxTransferDate', index: 'np.taxTransferDate', width: 145, formatter: $filter('dateFormatter')},
                            {name: 'paymentDate', index: 'np.paymentDate', width: 170, formatter: $filter('dateFormatter')},
                            {name: 'paymentNumber', index: 'np.paymentNumber', width: 205},
                            {name: 'taxSumm', index: 'np.taxSumm', width: 205}
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
                    if ($scope.searchFilter.params.kpp) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.kpp",
                            value: $scope.searchFilter.params.kpp
                        });
                    }
                    if ($scope.searchFilter.params.oktmo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.oktmo",
                            value: $scope.searchFilter.params.oktmo
                        });
                    }
                    if ($scope.searchFilter.params.incomeCode) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.incomeCode",
                            value: $scope.searchFilter.params.incomeCode
                        });
                    }
                    if ($scope.searchFilter.params.incomeType) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.incomeType",
                            value: $scope.searchFilter.params.incomeType
                        });
                    }
                    if ($scope.searchFilter.params.taxRate) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.taxRate",
                            value: $scope.searchFilter.params.taxRate
                        });
                    }
                    if ($scope.searchFilter.params.numberPaymentOrder) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.paymentNumber",
                            value: $scope.searchFilter.params.numberPaymentOrder
                        });
                    }
                    if ($scope.searchFilter.params.transferDateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.taxTransferDate",
                            value: $scope.searchFilter.params.transferDateFrom
                        });
                    }
                    if ($scope.searchFilter.params.transferDateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.taxTransferDate",
                            value: $scope.searchFilter.params.transferDateTo
                        });
                    }
                    if ($scope.searchFilter.params.calculationDateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.taxDate",
                            value: $scope.searchFilter.params.calculationDateFrom
                        });
                    }
                    if ($scope.searchFilter.params.calculationDateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.taxDate",
                            value: $scope.searchFilter.params.calculationDateTo
                        });
                    }
                    if ($scope.searchFilter.params.paymentDateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.paymentDate",
                            value: $scope.searchFilter.params.paymentDateFrom
                        });
                    }
                    if ($scope.searchFilter.params.paymentDateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "np.paymentDate",
                            value: $scope.searchFilter.params.paymentDateTo
                        });
                    }
                };

            }])

}());