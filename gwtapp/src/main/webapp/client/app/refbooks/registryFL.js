(function () {
    'use strict';

    /**
     * @description Модуль для страницы "Реестр Физических лиц"
     */
    angular.module('app.registryFL', ['app.rest', 'app.formatters', 'ui.router'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('registryFL', {
                url: '/refbooks/registryFL',
                templateUrl: 'client/app/refbooks/registryFL.html',
                controller: 'registryFLCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_NSI)) {
                            $state.go("/");
                        }
                    }
                ]
            });
        }])

        /**
         * @description Контроллер страницы "Реестр Физических лиц"
         */
        .controller('registryFLCtrl', ['$scope', '$filter', '$window', 'RefBookFLResource', 'APP_CONSTANTS', '$http', '$logPanel', '$state',
            function ($scope, $filter, $window, RefBookFLResource, APP_CONSTANTS, $http, $logPanel, $state) {

                $scope.refreshGrid = function (page) {
                    $scope.flGrid.ctrl.refreshGrid(page);
                };

                function getDefaultFilterParams() {
                    return {
                        allVersions: APP_CONSTANTS.SHOW_VERSIONS.BY_DATE,
                        versionDate: new Date().format("yyyy-mm-dd"),
                        showDuplicates: APP_CONSTANTS.SHOW_DUPLICATES.NO
                    };
                }

                $scope.searchFilter = {
                    params: getDefaultFilterParams(),
                    ajaxFilter: [],
                    filterName: 'registryFLFilter',
                    onCreateComplete: function () {
                        $scope.refreshGrid();
                    },
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
                 * Строковое представление содержимого фильтра.
                 * @returns {string} содержимое фильтра в виде JSON-строки
                 */
                $scope.filterRequestParam = function () {
                    return JSON.stringify({
                        id: $scope.searchFilter.params.id,
                        vip: $filter('vipOptionsFormatter')($scope.searchFilter.params.importance),
                        lastName: $scope.searchFilter.params.lastName,
                        firstName: $scope.searchFilter.params.firstName,
                        middleName: $scope.searchFilter.params.middleName,
                        birthDateFrom: $scope.searchFilter.params.birthDateFrom,
                        birthDateTo: $scope.searchFilter.params.birthDateTo,
                        terBankIds: $filter('idExtractor')($scope.searchFilter.params.departments),
                        docTypeIds: $filter('idExtractor')($scope.searchFilter.params.documentTypes),
                        documentNumber: $scope.searchFilter.params.documentNumber,
                        citizenshipCountryIds: $filter('idExtractor')($scope.searchFilter.params.citizenshipCountries),
                        taxpayerStateIds: $filter('idExtractor')($scope.searchFilter.params.taxpayerStates),
                        sourceSystemIds: $filter('idExtractor')($scope.searchFilter.params.sourceSystems),
                        inp: $scope.searchFilter.params.inp,
                        inn: $scope.searchFilter.params.inn,
                        innForeign: $scope.searchFilter.params.innForeign,
                        snils: $scope.searchFilter.params.snils,
                        postalCode: $scope.searchFilter.params.postCode,
                        region: $scope.searchFilter.params.regionCode,
                        district: $scope.searchFilter.params.district,
                        city: $scope.searchFilter.params.city,
                        locality: $scope.searchFilter.params.locality,
                        street: $scope.searchFilter.params.street,
                        countryIds: $filter('idExtractor')($scope.searchFilter.params.countries),
                        foreignAddress: $scope.searchFilter.params.foreignAddress,
                        allVersions: $filter('versionsVisibilityFormatter')($scope.searchFilter.params.allVersions),
                        versionDate: $scope.searchFilter.params.versionDate,
                        duplicates: $filter('duplicatesFilterFormatter')($scope.searchFilter.params.showDuplicates)
                    });
                };

                /**
                 * Инициализация таблицы пользователей
                 */
                $scope.flGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        gridName: 'registryFLGrid',
                        datatype: "angularResource",
                        angularResource: RefBookFLResource,
                        ondblClickRow: function (rowId) {
                            $state.go("personCard", {id: rowId});
                        },
                        requestParameters: function () {
                            return {
                                filter: $scope.filterRequestParam(),
                                projection: 'common'
                            };
                        },
                        colNames: [
                            $filter('translate')('refBook.fl.table.title.id'),
                            $filter('translate')('refBook.fl.table.title.importance'),
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
                            $filter('translate')('refBook.fl.table.title.russianAddress'),
                            $filter('translate')('refBook.fl.table.title.foreignAddress'),
                            $filter('translate')('refBook.fl.table.title.sourceSystem'),
                            $filter('translate')('refBook.fl.table.title.versionStart'),
                            $filter('translate')('refBook.fl.table.title.versionEnd'),
                            $filter('translate')('refBook.fl.table.title.versionId')
                        ],
                        colModel: [
                            {
                                name: 'oldId',
                                index: 'oldId',
                                formatter: $filter('personIdFormatter'),
                                width: 120
                            },
                            {
                                name: 'vip',
                                index: 'vip',
                                formatter: $filter('vipTextFormatter'),
                                width: 80
                            },
                            {
                                name: 'lastName',
                                index: 'lastName',
                                formatter: $filter('personLinkFormatter')
                            },
                            {
                                name: 'firstName',
                                index: 'firstName'
                            },
                            {
                                name: 'middleName',
                                index: 'middleName'
                            },
                            {
                                name: 'birthDate',
                                index: 'birthDate',
                                formatter: $filter('dateFormatter'),
                                width: 80
                            },
                            {
                                name: 'reportDoc',
                                index: 'docType',
                                formatter: $filter('docTypeFormatter'),
                                width: 250
                            },
                            {
                                name: 'reportDoc',
                                index: 'docNumber',
                                formatter: $filter('docNumberFormatter')
                            },
                            {
                                name: 'citizenship',
                                index: 'citizenship',
                                formatter: $filter('permissiveCodeFormatter')
                            },
                            {
                                name: 'taxPayerState',
                                index: 'taxPayerState',
                                formatter: $filter('permissiveCodeFormatter'),
                                width: 50
                            },
                            {
                                name: 'inn',
                                index: 'inn',
                                formatter: $filter('permissiveFormatter'),
                                width: 110
                            },
                            {
                                name: 'innForeign',
                                index: 'innForeign',
                                formatter: $filter('permissiveFormatter')
                            },
                            {
                                name: 'snils',
                                index: 'snils',
                                formatter: $filter('permissiveFormatter'),
                                width: 110
                            },
                            {
                                name: 'address',
                                index: 'address',
                                formatter: $filter('russianAddressFormatter'),
                                width: 400
                            },
                            {
                                name: 'address',
                                index: 'foreignAddress',
                                formatter: $filter('foreignAddressFormatter'),
                                width: 300
                            },
                            {
                                name: 'source',
                                index: 'source',
                                formatter: $filter('codeNameFormatter')
                            },
                            {
                                name: 'startDate',
                                index: 'startDate',
                                formatter: $filter('dateFormatter'),
                                width: 80
                            },
                            {
                                name: 'endDate',
                                index: 'endDate',
                                formatter: $filter('dateFormatter'),
                                width: 80
                            },
                            {
                                name: 'id',
                                index: 'id'
                            }
                        ],
                        rowNum: 100,
                        rowList: [5, 10, 50, 100, 200, 300],
                        viewrecords: true,
                        disableAutoLoad: true
                    }
                };

                $scope.createPersonsExcel = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/refBookFL/export/excel",
                        params: {
                            filter: $scope.filterRequestParam(),
                            pagingParams: JSON.stringify({
                                property: $scope.flGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortname'),
                                direction: $scope.flGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortorder')
                            })
                        }
                    }).success(function (response) {
                        if (response.uuid) {
                            $logPanel.open('log-panel-container', response.uuid);
                        }
                    });
                };

                $scope.$watch("searchFilter.params.versionDate", function (newVal, oldVal) {
                    if (!newVal) {
                        $scope.searchFilter.params.versionDate = oldVal;
                    }
                });
            }])

        /**
         * @description Форматтер для поля 'Фамилия' для перехода на карточку ФЛ
         * @param cellValue Значение ячейки
         * @param options Данные таблицы
         */
        .filter('personLinkFormatter', ['$filter', function ($filter) {
            return function (cellValue, options) {
                var value = cellValue;
                if (!cellValue) {
                    value = $filter('translate')('refBook.fl.table.label.undefined');
                }
                return "<a href='index.html#/personRegistry/personCard/" + options.rowId + "'>" + value + "</a>";
            };
        }])

        /**
         * @description Форматтер для поля 'Фамилия' для перехода на карточку ФЛ в отдельной вкладке
         * @param cellValue Значение ячейки
         * @param options Данные таблицы
         */
        .filter('personLinkSeparateTabFormatter', ['$filter', function ($filter) {
            return function (cellValue, options) {
                var value = cellValue;
                if (!cellValue) {
                    value = $filter('translate')('refBook.fl.table.label.undefined');
                }
                return "<a href='index.html#/personRegistry/personCard/" + options.rowId + "' target='_blank'>" + value + "</a>";
            };
        }]);
}());