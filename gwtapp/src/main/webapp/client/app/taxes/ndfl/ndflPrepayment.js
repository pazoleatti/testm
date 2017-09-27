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
            '$scope', '$timeout', '$state', '$stateParams', '$http', 'NdflPersonResource', '$filter', '$rootScope',
            function ($scope, $timeout, $state, $stateParams, $http, NdflPersonResource, $filter, $rootScope) {

                $scope.$on('INP_CHANGED', function(event, data) {
                    if (!_.isEqual($scope.searchFilter.params.inp, data)){
                        $scope.searchFilter.params.inp = data;
                        $scope.submitSearch();
                    }
                });

                $scope.$on('OPERATION_ID_CHANGED', function(event, data) {
                    if (!_.isEqual($scope.searchFilter.params.operationId, data)){
                        $scope.searchFilter.params.operationId = data;
                        $scope.submitSearch();
                    }
                });

                $scope.$on('tabSelected', function(event, data) {
                    if (_.isEqual(data, 'prepayment')){
                        $scope.submitSearch();
                    }
                });

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
                                        declarationDataId: $stateParams.declarationDataId,
                                        inp: $scope.searchFilter.params.inp && $scope.searchFilter.params.inp !== "" ? $scope.searchFilter.params.inp : undefined,
                                        operationId: $scope.searchFilter.params.operationId && $scope.searchFilter.params.operationId !== "" ? $scope.searchFilter.params.operationId : undefined,
                                        notifNum: $scope.searchFilter.params.notifNum && $scope.searchFilter.params.notifNum !== "" ? $scope.searchFilter.params.notifNum : undefined,
                                        notifSource: $scope.searchFilter.params.notifSource && $scope.searchFilter.params.notifSource !== "" ? $scope.searchFilter.params.notifSource : undefined,
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
                };
            }])

}());