(function () {
    'use strict';

    /**
     * @description Список ФЛ для создания 2-НДФЛ
     */
    angular.module('app.personsFor2NdflFL', ['app.constants', 'app.create2NdflFL', 'app.logPanel'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('personsFor2NdflFL', {
                url: '/taxes/personsFor2NdflFL',
                templateUrl: 'client/app/taxes/ndfl/2ndflFL/personsFor2NdflFL.html',
                controller: 'personsFor2NdflFLCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION._2NDFL_FL)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        .controller('personsFor2NdflFLCtrl', ['$scope', '$filter', 'RefBookFLResource', 'APP_CONSTANTS', '$logPanel', '$aplanaModal',
            function ($scope, $filter, RefBookFLResource, APP_CONSTANTS, $logPanel, $aplanaModal) {

                $scope.refreshGrid = function (page) {
                    if ($scope.searchFilter.isClear) {
                        $scope.personsGrid.ctrl.refreshGrid(page);
                    } else {
                        $scope.personsGrid.ctrl.getGrid().jqGrid('clearGridData');
                    }
                };

                function getDefaultFilterParams() {
                    return {};
                }

                $scope.searchFilter = {
                    params: getDefaultFilterParams(),
                    ajaxFilter: [],
                    filterName: 'personsFor2NdflFLFilter',
                    resetFilterParams: function () {
                        $scope.searchFilter.params = getDefaultFilterParams();
                    },
                    isClearByFilterParams: function () {
                        $scope.searchFilter.isClear = stringify($scope.searchFilter.params) !== stringify(getDefaultFilterParams());
                    }
                };

                // поля со значениями null, undefined или "" будут считаться эквивалентными
                function stringify(value) {
                    return JSON.stringify(value, function (key, value) {
                        return value ? value : undefined;
                    });
                }

                /**
                 * Параметры фильтра для запроса данных таблицы ФЛ
                 */
                $scope.filterRequestParam = function () {
                    return JSON.stringify({
                        lastName: $scope.searchFilter.params.lastName,
                        firstName: $scope.searchFilter.params.firstName,
                        middleName: $scope.searchFilter.params.middleName,
                        birthDateFrom: $scope.searchFilter.params.birthDateFrom,
                        birthDateTo: $scope.searchFilter.params.birthDateTo,
                        docTypeIds: $filter('idExtractor')($scope.searchFilter.params.documentTypes),
                        documentNumber: $scope.searchFilter.params.documentNumber,
                        citizenshipCountryIds: $filter('idExtractor')($scope.searchFilter.params.citizenshipCountries),
                        taxpayerStateIds: $filter('idExtractor')($scope.searchFilter.params.taxpayerStates),
                        inn: $scope.searchFilter.params.inn,
                        innForeign: $scope.searchFilter.params.innForeign,
                        snils: $scope.searchFilter.params.snils
                    });
                };

                $scope.personsGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        gridName: 'personsGrid',
                        datatype: "angularResource",
                        angularResource: RefBookFLResource,
                        requestParameters: function () {
                            return {
                                filter: $scope.filterRequestParam(),
                                projection: 'for2NdflFL'
                            };
                        },
                        colNames: [
                            '',
                            $filter('translate')('title.lastName'),
                            $filter('translate')('title.firstName'),
                            $filter('translate')('title.middleName'),
                            $filter('translate')('title.dateOfBirth'),
                            $filter('translate')('refBook.fl.table.title.documentType'),
                            $filter('translate')('refBook.fl.table.title.documentNumber'),
                            $filter('translate')('refBook.fl.table.title.citizenship'),
                            $filter('translate')('refBook.fl.table.title.status'),
                            $filter('translate')('refBook.fl.table.title.inn'),
                            $filter('translate')('refBook.fl.table.title.innForeign'),
                            $filter('translate')('refBook.fl.table.title.snils'),
                            $filter('translate')('refBook.fl.table.title.russianAddress')
                        ],
                        colModel: [
                            {name: 'id', index: 'id', key: true, hidden: true},
                            {name: 'lastName', index: 'lastName'},
                            {name: 'firstName', index: 'firstName'},
                            {name: 'middleName', index: 'middleName'},
                            {name: 'birthDate', index: 'birthDate', formatter: $filter('dateFormatter'), width: 80},
                            {
                                name: 'reportDoc.docType',
                                index: 'docType',
                                formatter: $filter('codeNameFormatter'),
                                width: 250
                            },
                            {name: 'reportDoc.documentNumber', index: 'docNumber'},
                            {name: 'citizenship', index: 'citizenship', formatter: $filter('codeNameFormatter')},
                            {
                                name: 'taxPayerState',
                                index: 'taxPayerState',
                                formatter: $filter('codeNameFormatter'),
                                width: 200
                            },
                            {name: 'inn', index: 'inn', width: 110},
                            {name: 'innForeign', index: 'innForeign'},
                            {name: 'snils', index: 'snils', width: 110},
                            {
                                name: 'address',
                                index: 'address',
                                formatter: $filter('personAddressFormatter'),
                                width: 400
                            }
                        ],
                        rowNum: 100,
                        rowList: [5, 10, 50, 100, 200, 300],
                        viewrecords: true,
                        disableAutoLoad: true
                    }
                };

                $scope.create2NdflFL = function () {
                    if ($scope.personsGrid.value && $scope.personsGrid.value.length === 1) {
                        $aplanaModal.open({
                            title: $filter('translate')('title.create2NdflFL'),
                            templateUrl: 'client/app/taxes/ndfl/2ndflFL/modal/create2NdflFL.html',
                            controller: 'Create2NdflFLCtrl',
                            windowClass: 'modal600',
                            resolve: {
                                $shareData: function () {
                                    return {
                                        person: $scope.personsGrid.value[0]
                                    };
                                }
                            }
                        }).result.then(function (response) {
                            if (response) {
                                if (response.data && response.data.uuid) {
                                    $logPanel.open('log-panel-container', response.data.uuid);
                                }
                            }
                        });
                    }
                };
            }]
        );
}());