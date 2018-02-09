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
            '$scope', '$stateParams', 'NdflPersonResource', '$filter', 'APP_CONSTANTS',
            function ($scope, $stateParams, NdflPersonResource, $filter, APP_CONSTANTS) {

                var tab = $scope.ndfFLTab;

                tab.refreshGrid = function (page) {
                    if ($scope.ndflPersonGrid.ctrl.refreshGrid) {
                        $scope.ndflPersonGrid.ctrl.refreshGrid(page);
                    }
                };

                // Обработчик на активацию таба
                $scope.$watch("ndfFLTab.active", function (newValue, oldValue) {
                    if (newValue && !oldValue) {
                        $scope.submitSearch();
                    }
                });

                $scope.ndflPersonGrid = {
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
            }]);
}());