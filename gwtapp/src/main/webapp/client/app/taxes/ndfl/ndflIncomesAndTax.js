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
            '$scope', '$stateParams', 'NdflPersonResource', '$filter', 'APP_CONSTANTS',
            function ($scope, $stateParams, NdflPersonResource, $filter, APP_CONSTANTS) {

                var tab = $scope.incomesAndTaxTab;

                tab.refreshGrid = function (page) {
                    if ($scope.incomesAndTaxGrid.ctrl.refreshGrid) {
                        $scope.incomesAndTaxGrid.ctrl.refreshGrid(page);
                    }
                };

                // Обработчик на активацию таба
                $scope.$watch("incomesAndTaxTab.active", function (newValue, oldValue) {
                    if (newValue && !oldValue) {
                        $scope.submitSearch();
                    }
                });

                $scope.incomesAndTaxGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: NdflPersonResource,
                        requestParameters: function () {
                            return {
                                projection: "personsIncome",
                                ndflFilter: JSON.stringify($scope.getNdflFilter())
                            };
                        },
                        height: 250,
                        colNames: [
                            $filter('translate')('incomesAndTax.title.numberpp'),
                            $filter('translate')('incomesAndTax.title.inp'),
                            $filter('translate')('incomesAndTax.title.idOperation'),
                            $filter('translate')('incomesAndTax.title.incomeCode'),
                            $filter('translate')('incomesAndTax.title.incomeAttr'),
                            $filter('translate')('incomesAndTax.title.dateCalcIncome'),
                            $filter('translate')('incomesAndTax.title.datePaymentIncome'),
                            $filter('translate')('incomesAndTax.title.kpp'),
                            $filter('translate')('incomesAndTax.title.oktmo'),
                            $filter('translate')('incomesAndTax.title.amountCalcIncome'),
                            $filter('translate')('incomesAndTax.title.amountPaymentIncome'),
                            $filter('translate')('incomesAndTax.title.amountDeduction'),
                            $filter('translate')('incomesAndTax.title.taxBase'),
                            $filter('translate')('incomesAndTax.title.taxRateWithPercent'),
                            $filter('translate')('incomesAndTax.title.taxDate'),
                            $filter('translate')('incomesAndTax.title.calculatedTax'),
                            $filter('translate')('incomesAndTax.title.withholdingTax'),
                            $filter('translate')('incomesAndTax.title.notHoldingTax'),
                            $filter('translate')('incomesAndTax.title.overholdingTax'),
                            $filter('translate')('incomesAndTax.title.refoundTax'),
                            $filter('translate')('incomesAndTax.title.taxTransferDate'),
                            $filter('translate')('incomesAndTax.title.paymentDate'),
                            $filter('translate')('incomesAndTax.title.paymentNumber'),
                            $filter('translate')('incomesAndTax.title.taxSumm')],
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
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'rowNum',
                        viewrecords: true,
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: true,
                        disableAutoLoad: true
                    }
                };
            }]);

}());