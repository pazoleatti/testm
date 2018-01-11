(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Реквизиты"
     */
    angular.module('app.ndflFL', [])

    /**
     * @description Контроллер вкладки "Реквизиты"
     */
        .controller('ndflFLCtrl', [
            '$scope', '$timeout', '$state', '$stateParams', '$http', 'NdflPersonResource', '$filter', 'ShowToDoDialog', '$rootScope', 'APP_CONSTANTS',
            function ($scope, $timeout, $state, $stateParams, $http, NdflPersonResource, $filter, $showToDoDialog, $rootScope, APP_CONSTANTS) {

                $scope.$on('INP_CHANGED', function(event, data) {
                    if (!_.isEqual($scope.searchFilter.params.inp, data)){
                        $scope.searchFilter.params.inp = data;
                        $scope.submitSearch();
                    }
                });

                var isTabActive = true;

                $scope.$on('tabSelected', function(event, data) {
                    if (_.isEqual(data, 'ndflFL')){
                        $scope.submitSearch();
                        isTabActive = true;
                    }
                });

                $scope.$on('tabDeselected', function(event, data) {
                    if (_.isEqual(data, 'ndflFL')){
                        isTabActive = false;
                    }
                });

                $scope.$on('refreshDeclarationGrid', function(event, data) {
                    if (isTabActive) {
                        $scope.refreshGrid();
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
                    $scope.ndflPersonGrid.ctrl.refreshGrid(page);
                };

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {},
                    isClear: false,
                    filterName: 'ndflFilter'
                };

                $scope.ndflPersonGrid =
                    {
                        ctrl: {},
                        value: [],
                        options: {
                            datatype: "angularResource",
                            angularResource: NdflPersonResource,
                            requestParameters: function () {
                                return {
                                    projection: "ndflPersons",
                                    ndflPersonFilter: JSON.stringify({
                                        declarationDataId: $stateParams.declarationDataId,
                                        inp: $filter('requestParamsFormatter')($scope.searchFilter.params.inp),
                                        innNp: $filter('requestParamsFormatter')($scope.searchFilter.params.innNp),
                                        innForeign: $filter('requestParamsFormatter')($scope.searchFilter.params.innForeign),
                                        snils: $filter('requestParamsFormatter')($scope.searchFilter.params.snils),
                                        idDocNumber: $filter('requestParamsFormatter')($scope.searchFilter.params.idDocNumber),
                                        lastName: $filter('requestParamsFormatter')($scope.searchFilter.params.lastName),
                                        firstName: $filter('requestParamsFormatter')($scope.searchFilter.params.firstName),
                                        middleName: $filter('requestParamsFormatter')($scope.searchFilter.params.middleName),
                                        dateFrom: $scope.searchFilter.params.dateFrom,
                                        dateTo: $scope.searchFilter.params.dateTo
                                    })
                                };
                            },
                            height: 250,
                            colNames: [
                                $filter('translate')('ndflFL.title.numberpp'),
                                $filter('translate')('ndflFL.title.inp'),
                                $filter('translate')('ndflFL.title.lastName'),
                                $filter('translate')('ndflFL.title.firstName'),
                                $filter('translate')('ndflFL.title.middleName'),
                                $filter('translate')('ndflFL.title.dateOfBirth'),
                                $filter('translate')('ndflFL.title.citizenship'),
                                $filter('translate')('ndflFL.title.innNp'),
                                $filter('translate')('ndflFL.title.innForeign'),
                                $filter('translate')('ndflFL.title.codeDul'),
                                $filter('translate')('ndflFL.title.numberDul'),
                                $filter('translate')('ndflFL.title.statusCode'),
                                $filter('translate')('ndflFL.title.subjectCode'),
                                $filter('translate')('ndflFL.title.index'),
                                $filter('translate')('ndflFL.title.area'),
                                $filter('translate')('ndflFL.title.city'),
                                $filter('translate')('ndflFL.title.locality'),
                                $filter('translate')('ndflFL.title.street'),
                                $filter('translate')('ndflFL.title.house'),
                                $filter('translate')('ndflFL.title.building'),
                                $filter('translate')('ndflFL.title.flat'),
                                $filter('translate')('ndflFL.title.snils')],
                            colModel: [
                                {name: 'rowNum', index: 'rowNum', width: 80, key: true},
                                {name: 'inp', index: 'inp', width: 150},
                                {name: 'lastName', index: 'lastName', width: 200},
                                {name: 'firstName', index: 'firstName', width: 175},
                                {name: 'middleName', index: 'middleName', width: 200},
                                {
                                    name: 'birthDay',
                                    index: 'birthDay',
                                    width: 240,
                                    formatter: $filter('dateFormatter')
                                },
                                {name: 'citizenship', index: 'citizenship', width: 185},
                                {name: 'innNp', index: 'innNp', width: 95},
                                {name: 'innForeign', index: 'innForeign', width: 195},
                                {name: 'idDocType', index: 'idDocType', width: 85},
                                {name: 'idDocNumber', index: 'idDocNumber', width: 95},
                                {name: 'status', index: 'status', width: 100},
                                {name: 'regionCode', index: 'regionCode', width: 205, sortable: false},
                                {name: 'postIndex', index: 'postIndex', width: 170},
                                {name: 'area', index: 'area', width: 155},
                                {name: 'city', index: 'city', width: 165},
                                {name: 'locality', index: 'locality', width: 240},
                                {name: 'street', index: 'street', width: 240},
                                {name: 'house', index: 'house', width: 145},
                                {name: 'building', index: 'building', width: 170},
                                {name: 'flat', index: 'flat', width: 205},
                                {name: 'snils', index: 'snils', width: 190, sortable: false}
                            ],
                            rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                            rowList: APP_CONSTANTS.COMMON.PAGINATION,
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
                    if ($scope.searchFilter.params.snils) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "snils",
                            value: $scope.searchFilter.params.snils
                        });
                    }
                    if ($scope.searchFilter.params.innNp) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "innNp",
                            value: $scope.searchFilter.params.innNp
                        });
                    }
                    if ($scope.searchFilter.params.innForeign) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "innForeign",
                            value: $scope.searchFilter.params.innForeign
                        });
                    }
                    if ($scope.searchFilter.params.idDocNumber) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "idDocNumber",
                            value: $scope.searchFilter.params.idDocNumber
                        });
                    }
                    if ($scope.searchFilter.params.lastName) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "lastName",
                            value: $scope.searchFilter.params.lastName
                        });
                    }
                    if ($scope.searchFilter.params.firstName) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "firstName",
                            value: $scope.searchFilter.params.firstName
                        });
                    }
                    if ($scope.searchFilter.params.middleName) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "middleName",
                            value: $scope.searchFilter.params.middleName
                        });
                    }
                    if ($scope.searchFilter.params.dateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "birthDay",
                            value: $scope.searchFilter.params.dateFrom
                        });
                    }
                    if ($scope.searchFilter.params.dateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "birthDay",
                            value: $scope.searchFilter.params.dateTo
                        });
                    }
                };
            }]);

}());