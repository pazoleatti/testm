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
        .controller('registryFLCtrl', ['$scope', '$filter', '$window', 'RefBookFLResource', 'APP_CONSTANTS', '$http', '$logPanel',
            function ($scope, $filter, $window, RefBookFLResource, APP_CONSTANTS, $http, $logPanel) {

                $scope.refreshGrid = function (page) {
                    $scope.flGrid.ctrl.refreshGrid(page);
                };

                $scope.filterParamsInitialState = function () {
                    return {
                        allVersions: APP_CONSTANTS.SHOW_VERSIONS.BY_DATE,
                        versionDate: new Date()
                    }
                };

                $scope.searchFilter = {
                    params: $scope.filterParamsInitialState(),
                    resetFilterParams: function () {
                        $scope.searchFilter.params = $scope.filterParamsInitialState();
                    }
                };

                /**
                 * Строковое представление содержимого фильтра.
                 * @returns {string} содержимое фильтра в виде JSON-строки
                 */
                $scope.filterRequestParam = function () {
                    return JSON.stringify({
                        id: $scope.searchFilter.params.id,
                        lastName: $scope.searchFilter.params.lastName,
                        firstName: $scope.searchFilter.params.firstName,
                        middleName: $scope.searchFilter.params.middleName,
                        birthDateFrom: $scope.searchFilter.params.birthDateFrom,
                        birthDateTo: $scope.searchFilter.params.birthDateTo,
                        documentNumber: $scope.searchFilter.params.documentNumber,
                        allVersions: $filter('versionsVisibilityFormatter')($scope.searchFilter.params.allVersions),
                        versionDate: $scope.searchFilter.params.versionDate
                    });
                };

                /**
                 * Инициализация таблицы пользователей
                 */
                $scope.flGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: RefBookFLResource,
                        requestParameters: function () {
                            return {
                                filter: $scope.filterRequestParam()
                            }
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
                                formatter: $filter('personIdFormatter'),
                                width: 120
                            },
                            {
                                name: 'vip',
                                formatter: $filter('vipFormatter'),
                                width: 80
                            },
                            {
                                name: 'lastName',
                                formatter: $filter('personLinkFormatter')
                            },
                            {
                                name: 'firstName'
                            },
                            {
                                name: 'middleName'
                            },
                            {
                                name: 'birthDate',
                                formatter: $filter('dateFormatter'),
                                width: 80
                            },
                            {
                                name: 'docName',
                                formatter: $filter('permissiveFormatter'),
                                width: 250
                            },
                            {
                                name: 'docNumber',
                                formatter: $filter('permissiveFormatter')
                            },
                            {
                                name: 'citizenship',
                                formatter: $filter('citizenshipFormatter')
                            },
                            {
                                name: 'taxpayerState',
                                formatter: $filter('codeFormatter'),
                                width: 50
                            },
                            {
                                name: 'inn',
                                formatter: $filter('permissiveFormatter'),
                                width: 110
                            },
                            {
                                name: 'innForeign',
                                formatter: $filter('permissiveFormatter')
                            },
                            {
                                name: 'snils',
                                formatter: $filter('permissiveFormatter'),
                                width: 110
                            },
                            {
                                name: 'address',
                                formatter: $filter('russianAddressFormatter'),
                                width: 400
                            },
                            {
                                name: 'address',
                                formatter: $filter('foreignAddressFormatter'),
                                width: 300
                            },
                            {
                                name: 'source',
                                formatter: $filter('nameFormatter')
                            },
                            {
                                name: 'version',
                                formatter: $filter('dateFormatter'),
                                width: 80
                            },
                            {
                                name: 'versionEnd',
                                formatter: $filter('dateFormatter'),
                                width: 80
                            },
                            {
                                name: 'id'
                            }
                        ],
                        rowNum: 100,
                        rowList: [5, 10, 50, 100, 200, 300],
                        viewrecords: true
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
            }])

        /**
         * @description Форматтер для поля 'Фамилия' для перехода на карточку ФЛ
         * @param cellValue Значение ячейки
         * @param options Данные таблицы
         */
        .filter('personLinkFormatter', function () {
            return function (cellValue, options) {
                return "<a href='index.html#/personRegistry/personCard/" + options.rowId + "'>" + cellValue + "</a>";
            };
        })
}());