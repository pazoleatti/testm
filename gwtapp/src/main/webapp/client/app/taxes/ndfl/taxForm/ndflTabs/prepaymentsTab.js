(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Сведения о доходах в виде авансовых платежей"
     */
    angular.module('app.prepayment', [])

    /**
     * @description Контроллер вкладки "Сведения о доходах в виде авансовых платежей"
     */
        .controller('prepaymentCtrl', [
            '$scope', '$stateParams', 'NdflPersonResource', '$filter', 'APP_CONSTANTS', '$rootScope',
            function ($scope, $stateParams, NdflPersonResource, $filter, APP_CONSTANTS, $rootScope) {

                var tab = $scope.prepaymentTab;

                tab.refreshGrid = function (page) {
                    if ($scope.prepaymentGrid.ctrl.refreshGrid) {
                        $scope.prepaymentGrid.ctrl.refreshGrid(page);
                    }
                };

                // Обработчик на активацию таба
                $scope.$watch("prepaymentTab.active", function (newValue, oldValue) {
                    if (!tab.isDataLoaded) {
                        $rootScope.$emit("selectedRowCountChanged", 0);
                        if (newValue && !oldValue) {
                            tab.refreshGrid(1);
                        }
                    }
                });

                // Получение номера раздела, который отображается на вкладке
                tab.getSection = function () {
                    return 4
                };

                // Получение строк выбранных в таблице внутри вкладки
                tab.getRows = function () {
                    return $scope.prepaymentGrid.value
                };

                $scope.prepaymentGrid = {
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
                                projection: "personsPrepayment",
                                ndflFilter: JSON.stringify($scope.ndflFilter)
                            };
                        },
                        height: 250,
                        colNames: [
                            $filter('translate')('ndflPrepayment.title.numberpp'),
                            $filter('translate')('ndflPrepayment.title.inp'),
                            $filter('translate')('ndflPrepayment.title.idOperation'),
                            $filter('translate')('ndflPrepayment.title.summ'),
                            $filter('translate')('ndflPrepayment.title.notifNum'),
                            $filter('translate')('ndflPrepayment.title.notifDate'),
                            $filter('translate')('ndflPrepayment.title.notifSource'),
                            $filter('translate')('ndflPrepayment.title.id'),
                            $filter('translate')('ndflPrepayment.title.modifiedDate'),
                            $filter('translate')('ndflPrepayment.title.modifiedBy'),
                            $filter('translate')('ndflPrepayment.title.asnu')],
                        colModel: [
                            {name: 'rowNum', index: 'rowNum', width: 75, key: true},
                            {name: 'inp', index: 'inp', width: 100},
                            {name: 'operationId', index: 'operationId', width: 245},
                            {name: 'summ', index: 'summ', width: 295},
                            {name: 'notifNum', index: 'notifNum', width: 155},
                            {
                                name: 'notifDate',
                                index: 'notifDate',
                                width: 190,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'notifSource', index: 'notifSource', width: 325},
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
                        fullScreen: true,
                        onSelectRow: function (rowId, status) {
                            if (status) {
                                $rootScope.$emit("selectedRowCountChanged", $scope.prepaymentGrid.value.length + 1)
                            } else $rootScope.$emit("selectedRowCountChanged", $scope.prepaymentGrid.value.length - 1)

                        }
                    }
                };
            }]);

}());