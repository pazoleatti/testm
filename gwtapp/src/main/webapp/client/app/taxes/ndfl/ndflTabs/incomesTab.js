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
            '$scope', '$stateParams', 'NdflPersonResource', '$filter', 'APP_CONSTANTS', '$rootScope',
            function ($scope, $stateParams, NdflPersonResource, $filter, APP_CONSTANTS, $rootScope) {

                var tab = $scope.incomesAndTaxTab;

                tab.refreshGrid = function (page) {
                    if ($scope.incomesAndTaxGrid.ctrl.refreshGrid) {
                        $scope.incomesAndTaxGrid.ctrl.refreshGrid(page);
                    }
                };

                // Обработчик на активацию таба
                $scope.$watch("incomesAndTaxTab.active", function (newValue, oldValue) {
                    if (!tab.isDataLoaded) {
                        $rootScope.$emit("selectedRowCountChanged", 0);
                        if (newValue && !oldValue) {
                            tab.refreshGrid(1);
                        }
                    }
                });

                // Получение номера раздела, который отображается на вкладке
                tab.getSection = function () {
                    return 2
                };

                // Получение строк выбранных в таблице внутри вкладки
                tab.getRows = function () {
                    return $scope.incomesAndTaxGrid.value
                };

                $scope.incomesAndTaxGrid = {
                    init: function (ctrl) {
                        ctrl.loadComplete = function () {
                            tab.isDataLoaded = true;
                        };
                    },
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: NdflPersonResource,
                        requestParameters: function () {
                            return {
                                projection: "personsIncome",
                                ndflFilter: JSON.stringify($scope.ndflFilter)
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
                            $filter('translate')('incomesAndTax.title.taxSumm'),
                            $filter('translate')('incomesAndTax.title.id'),
                            $filter('translate')('incomesAndTax.title.modifiedDate'),
                            $filter('translate')('incomesAndTax.title.modifiedBy')],
                        colModel: [
                            {name: 'rowNum', index: 'rowNum', width: 65},
                            {name: 'inp', index: 'inp', width: 100},
                            {name: 'operationId', index: 'operationId', width: 245},
                            {name: 'incomeCode', index: 'incomeCode', width: 90},
                            {name: 'incomeType', index: 'incomeType', width: 120},
                            {
                                name: 'incomeAccruedDate',
                                index: 'incomeAccruedDate',
                                width: 170,
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'incomePayoutDate',
                                index: 'incomePayoutDate',
                                width: 150,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'kpp', index: 'kpp', width: 80, sortable: false},
                            {name: 'oktmo', index: 'oktmo', width: 100},
                            {name: 'incomeAccruedSumm', index: 'incomeAccruedSumm', width: 195},
                            {name: 'incomePayoutSumm', index: 'incomePayoutSumm', width: 200},
                            {name: 'totalDeductionsSumm', index: 'totalDeductionsSumm', width: 110},
                            {name: 'taxBase', index: 'taxBase', width: 115},
                            {name: 'taxRate', index: 'taxRate', width: 155},
                            {name: 'taxDate', index: 'taxDate', width: 90, formatter: $filter('dateFormatter')},
                            {name: 'calculatedTax', index: 'calculatedTax', width: 145},
                            {name: 'withholdingTax', index: 'withholdingTax', width: 140},
                            {name: 'notHoldingTax', index: 'notHoldingTax', width: 155},
                            {name: 'overholdingTax', index: 'overholdingTax', width: 195},
                            {name: 'refoundTax', index: 'refoundTax', width: 180},
                            {
                                name: 'taxTransferDate',
                                index: 'taxTransferDate',
                                width: 200,
                                formatter: $filter('dateZeroFormatter')
                            },
                            {
                                name: 'paymentDate',
                                index: 'paymentDate',
                                width: 195,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'paymentNumber', index: 'paymentNumber', width: 205},
                            {name: 'taxSumm', index: 'taxSumm', width: 205},
                            {name: 'id', index: 'id', width: 160, key: true},
                            {name: 'modifiedDate', index: 'modifiedDate', width: 205, formatter: $filter('dateTimeFormatter')},
                            {name: 'modifiedBy', index: 'modifiedBy', width: 300}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'rowNum',
                        viewrecords: true,
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: true,
                        disableAutoLoad: true,
                        onSelectRow: function (rowId, status) {
                            if (status) {
                                $rootScope.$emit("selectedRowCountChanged", $scope.incomesAndTaxGrid.value.length + 1)
                            } else $rootScope.$emit("selectedRowCountChanged", $scope.incomesAndTaxGrid.value.length - 1)

                        }
                    }
                };
            }]);

}());