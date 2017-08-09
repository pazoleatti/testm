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
            '$scope', '$timeout', '$state', '$stateParams', '$http', 'NdflPersonResource', '$filter', 'ShowToDoDialog',
            function ($scope, $timeout, $state, $stateParams, $http, NdflPersonResource, $filter, $showToDoDialog) {

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
                                        inp: $scope.searchFilter.params.inp,
                                        innNp: $scope.searchFilter.params.innNp,
                                        innForeign: $scope.searchFilter.params.innForeign,
                                        snils: $scope.searchFilter.params.snils,
                                        idDocNumber: $scope.searchFilter.params.idDocNumber,
                                        lastName: $scope.searchFilter.params.lastName,
                                        firstName: $scope.searchFilter.params.firstName,
                                        middleName: $scope.searchFilter.params.middleName,
                                        dateFrom: $scope.searchFilter.params.dateFrom,
                                        dateTo: $scope.searchFilter.params.dateTo
                                    })
                                };
                            },
                            height: 250,
                            colNames: [
                                $filter('translate')('title.numberpp'),
                                $filter('translate')('title.inp'),
                                $filter('translate')('title.lastName'),
                                $filter('translate')('title.firstName'),
                                $filter('translate')('title.middleName'),
                                $filter('translate')('title.dateOfBirth'),
                                $filter('translate')('title.snils'),
                                $filter('translate')('title.citizenship'),
                                $filter('translate')('title.innNp'),
                                $filter('translate')('title.innForeign'),
                                $filter('translate')('title.codeDul'),
                                $filter('translate')('title.numberDul'),
                                $filter('translate')('title.statusCode'),
                                $filter('translate')('title.subjectCode'),
                                $filter('translate')('title.index'),
                                $filter('translate')('title.area'),
                                $filter('translate')('title.city'),
                                $filter('translate')('title.locality'),
                                $filter('translate')('title.street'),
                                $filter('translate')('title.house'),
                                $filter('translate')('title.building'),
                                $filter('translate')('title.flat')],
                            colModel: [
                                {name: 'rowNum', index: 'row_num', width: 60, key: true},
                                {name: 'inp', index: 'inp', width: 170},
                                {name: 'lastName', index: 'last_name', width: 200},
                                {name: 'firstName', index: 'first_name', width: 175},
                                {name: 'middleName', index: 'middle_name', width: 200},
                                {
                                    name: 'birthDay',
                                    index: 'birth_day',
                                    width: 240,
                                    formatter: $filter('dateFormatter')
                                },
                                {name: 'snils', index: 'snils', width: 190, sortable: false},
                                {name: 'citizenship', index: 'citizenship', width: 185},
                                {name: 'innNp', index: 'inn_np', width: 95},
                                {name: 'innForeign', index: 'inn_foreign', width: 195},
                                {name: 'idDocType', index: 'id_doc_type', width: 85},
                                {name: 'idDocNumber', index: 'id_doc_number', width: 95},
                                {name: 'status', index: 'status', width: 100},
                                {name: 'regionCode', index: 'region_code', width: 205, sortable: false},
                                {name: 'postIndex', index: 'post_index', width: 170},
                                {name: 'area', index: 'area', width: 155},
                                {name: 'city', index: 'city', width: 165},
                                {name: 'locality', index: 'locality', width: 240},
                                {name: 'street', index: 'street', width: 240},
                                {name: 'house', index: 'house', width: 145},
                                {name: 'building', index: 'building', width: 170},
                                {name: 'flat', index: 'flat', width: 205}
                            ],
                            rowNum: 10,
                            rowList: [10, 20, 30],
                            sortname: 'row_num',
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
            }])

}());