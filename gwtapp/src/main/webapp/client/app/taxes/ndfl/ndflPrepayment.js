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
                    $rootScope.$emit("selectedRowCountChanged", 0);
                    if (newValue && !oldValue) {
                        $scope.submitSearch();
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
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: NdflPersonResource,
                        requestParameters: function () {
                            return {
                                projection: "personsPrepayment",
                                ndflFilter: JSON.stringify($scope.getNdflFilter())
                            };
                        },
                        height: 250,
                        colNames: [
                            $filter('translate')('ndlfPrepayment.title.numberpp'),
                            $filter('translate')('ndlfPrepayment.title.inp'),
                            $filter('translate')('ndlfPrepayment.title.idOperation'),
                            $filter('translate')('ndlfPrepayment.title.summ'),
                            $filter('translate')('ndlfPrepayment.title.notifNum'),
                            $filter('translate')('ndlfPrepayment.title.notifDate'),
                            $filter('translate')('ndlfPrepayment.title.notifSource'),
                            $filter('translate')('ndlfPrepayment.title.id'),
                            $filter('translate')('ndlfPrepayment.title.modifiedDate'),
                            $filter('translate')('ndlfPrepayment.title.modifiedBy')],
                        colModel: [
                            {name: 'rowNum', index: 'rowNum', width: 60, key: true},
                            {name: 'inp', index: 'inp', width: 100},
                            {name: 'operationId', index: 'operationId', width: 245},
                            {name: 'summ', index: 'summ', width: 285},
                            {name: 'notifNum', index: 'notifNum', width: 140},
                            {
                                name: 'notifDate',
                                index: 'notifDate',
                                width: 180,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'notifSource', index: 'notifSource', width: 315},
                            {name: 'id', index: 'id', width: 160},
                            {name: 'modifiedDate', index: 'modifiedDate', width: 200, formatter: $filter('dateTimeFormatter')},
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
                                $rootScope.$emit("selectedRowCountChanged", $scope.prepaymentGrid.value.length + 1)
                            } else $rootScope.$emit("selectedRowCountChanged", $scope.prepaymentGrid.value.length - 1)

                        }
                    }
                };
            }]);

}());