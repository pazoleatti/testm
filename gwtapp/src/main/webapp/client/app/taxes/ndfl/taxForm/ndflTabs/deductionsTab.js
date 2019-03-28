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
            '$scope', '$stateParams', 'NdflPersonResource', '$filter', 'APP_CONSTANTS', '$rootScope',
            function ($scope, $stateParams, NdflPersonResource, $filter, APP_CONSTANTS, $rootScope) {

                var tab = $scope.deductionsTab;

                tab.refreshGrid = function (page) {
                    if ($scope.deductionGrid.ctrl.refreshGrid) {
                        $scope.deductionGrid.ctrl.refreshGrid(page);
                    }
                };

                // Обработчик на активацию таба
                $scope.$watch("deductionsTab.active", function (newValue, oldValue) {
                    if (!tab.isDataLoaded) {
                        if (newValue && !oldValue) {
                            tab.refreshGrid(1);
                        }
                    }
                });

                // Получение номера раздела, который отображается на вкладке
                tab.getSection = function () {
                    return 3;
                };

                // Получение строк выбранных в таблице внутри вкладки
                tab.getSelectedRows = function () {
                    return $scope.deductionGrid.value;
                };

                $scope.deductionGrid = {
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
                                projection: "personsDeduction",
                                ndflFilter: JSON.stringify($scope.ndflFilter)
                            };
                        },
                        height: 250,
                        colNames: [
                            $filter('translate')('ndflDeduction.title.numberpp'),
                            $filter('translate')('ndflDeduction.title.inp'),
                            $filter('translate')('ndflDeduction.title.deductionCode'),
                            $filter('translate')('ndflDeduction.title.notifType'),
                            $filter('translate')('ndflDeduction.title.notifDate'),
                            $filter('translate')('ndflDeduction.title.notifNum'),
                            $filter('translate')('ndflDeduction.title.notifSource'),
                            $filter('translate')('ndflDeduction.title.notifSumm'),
                            $filter('translate')('ndflDeduction.title.incomeIdOperation'),
                            $filter('translate')('ndflDeduction.title.incomeAccrued'),
                            $filter('translate')('ndflDeduction.title.income.incomeCode'),
                            $filter('translate')('ndflDeduction.title.incomeSumm'),
                            $filter('translate')('ndflDeduction.title.periodPrevDate'),
                            $filter('translate')('ndflDeduction.title.periodPrevSumm'),
                            $filter('translate')('ndflDeduction.title.periodCurrDate'),
                            $filter('translate')('ndflDeduction.title.periodCurrSumm'),
                            $filter('translate')('ndflDeduction.title.id'),
                            $filter('translate')('ndflDeduction.title.modifiedDate'),
                            $filter('translate')('ndflDeduction.title.modifiedBy'),
                            $filter('translate')('ndflDeduction.title.asnu')],
                        colModel: [
                            {name: 'rowNum', index: 'rowNum', width: 75, key: true},
                            {name: 'inp', index: 'inp', width: 100},
                            {name: 'typeCode', index: 'typeCode', width: 105},
                            {name: 'notifType', index: 'notifType', width: 230},
                            {
                                name: 'notifDate',
                                index: 'notifDate',
                                width: 235,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'notifNum', index: 'notifNum', width: 250},
                            {name: 'notifSource', index: 'notifSource', width: 295},
                            {name: 'notifSumm', index: 'notifSumm', width: 245},
                            {name: 'operationId', index: 'operationId', width: 245},
                            {
                                name: 'incomeAccrued',
                                index: 'incomeAccrued',
                                width: 105,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'incomeCode', index: 'incomeCode', width: 145},
                            {name: 'incomeSumm', index: 'incomeSumm', width: 115},
                            {
                                name: 'periodPrevDate',
                                index: 'periodPrevDate',
                                width: 190,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'periodPrevSumm', index: 'periodPrevSumm', width: 200},
                            {
                                name: 'periodCurrDate',
                                index: 'periodCurrDate',
                                width: 215,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'periodCurrSumm', index: 'periodCurrSumm', width: 220},
                            {name: 'id', index: 'id', width: 170},
                            {name: 'modifiedDate', index: 'modifiedDate', width: 215, formatter: $filter('dateTimeFormatter')},
                            {name: 'modifiedBy', index: 'modifiedBy', width: 300},
                            {name: 'asnuName', index: 'name', width: 300}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'rowNum',
                        viewrecords: true,
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: true,
                        disableAutoLoad: true,
                        fullScreen: true
                    }
                };
            }]);

}());