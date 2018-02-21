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
            '$scope', '$stateParams', 'NdflPersonResource', '$filter', 'APP_CONSTANTS',
            function ($scope, $stateParams, NdflPersonResource, $filter, APP_CONSTANTS) {

                var tab = $scope.prepaymentTab;

                tab.refreshGrid = function (page) {
                    if ($scope.prepaymentGrid.ctrl.refreshGrid) {
                        $scope.prepaymentGrid.ctrl.refreshGrid(page);
                    }
                };

                // Обработчик на активацию таба
                $scope.$watch("prepaymentTab.active", function (newValue, oldValue) {
                    if (newValue && !oldValue) {
                        $scope.submitSearch();
                    }
                });

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
                            {name: 'rowNum', index: 'rowNum', width: 90, key: true},
                            {name: 'inp', index: 'inp', width: 170},
                            {name: 'operationId', index: 'operationId', width: 200},
                            {name: 'summ', index: 'summ', width: 320},
                            {name: 'notifNum', index: 'notifNum', width: 200},
                            {
                                name: 'notifDate',
                                index: 'notifDate',
                                width: 240,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'notifSource', index: 'notifSource', width: 631},
                            {name: 'id', index: 'id', width: 200},
                            {name: 'modifiedDate', index: 'modifiedDate', width: 230, formatter: $filter('dateTimeFormatter')},
                            {name: 'modifiedBy', index: 'modifiedBy', width: 170}
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