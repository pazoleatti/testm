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
            '$scope', '$timeout', '$state', '$stateParams', 'dialogs', '$http', 'NdflPersonResource', '$filter',
            function ($scope, $timeout, $state, $stateParams, dialogs, $http, NdflPersonResource, $filter) {
                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.prepaymentGrid.ctrl.refreshGrid(page);
                };

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {},
                    isClear: false,
                    filterName: 'prepaymentFilter'
                };

                $scope.prepaymentGrid =
                    {
                        ctrl: {},
                        value: [],
                        options: {
                            datatype: "angularResource",
                            angularResource: NdflPersonResource,
                            requestParameters: function () {
                                return {
                                    projection: "personsPrepayment",
                                    ndflPersonPrepaymentFilter: JSON.stringify({
                                        declarationDataId: $stateParams.declarationId,
                                        inp: $scope.searchFilter.params.inp,
                                        operationId: $scope.searchFilter.params.operationId,
                                        notifNum: $scope.searchFilter.params.notifNum,
                                        notifSource: $scope.searchFilter.params.notifSource,
                                        notifDateFrom: $scope.searchFilter.params.notifDateFrom,
                                        notifDateTo: $scope.searchFilter.params.notifDateTo
                                    })
                                };
                            },
                            height: 250,
                            colNames: [
                                $filter('translate')('title.numberpp'),
                                $filter('translate')('title.inp'),
                                $filter('translate')('title.idOperation'),
                                $filter('translate')('title.summ'),
                                $filter('translate')('title.prepayment.notifNum'),
                                $filter('translate')('title.prepayment.notifDate'),
                                $filter('translate')('title.prepayment.notifSource')],
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
                                {name: 'notifSource', index: 'notifSource', width: 631}
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
                    if ($scope.searchFilter.params.notifNum) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "notifNum",
                            value: $scope.searchFilter.params.notifNum
                        });
                    }
                    if ($scope.searchFilter.params.notifSource) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "notifSource",
                            value: $scope.searchFilter.params.notifSource
                        });
                    }
                    if ($scope.searchFilter.params.notifDateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "notifDate",
                            value: $scope.searchFilter.params.notifDateFrom
                        });
                    }
                    if ($scope.searchFilter.params.notifDateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "notifDate",
                            value: $scope.searchFilter.params.notifDateTo
                        });
                    }
                }
            }])

}());