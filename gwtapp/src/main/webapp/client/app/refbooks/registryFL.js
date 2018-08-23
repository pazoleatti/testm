(function () {
    'use strict';

    /**
     * @description Модуль для страницы "Реестр Физических лиц"
     */
    angular.module('app.registryFL', ['app.rest'])
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
        .controller('registryFLCtrl', ['$scope', '$filter', '$window', 'RefBookFLResource', 'APP_CONSTANTS',
            function ($scope, $filter, $window, RefBookFLResource, APP_CONSTANTS) {

                $scope.refreshGrid = function (page) {
                    $scope.flGrid.ctrl.refreshGrid(page);
                };

                /**
                 * Инициализация объекта фильтра.
                 */
                $scope.searchFilter = {
                    params: {}
                };

                /**
                 * Строковое представление содержимого фильтра.
                 * @returns {string} содержимое фильтра в виде JSON-строки
                 */
                // $scope.filterParams = function () {
                //     return JSON.stringify({
                //         departmentIds: $filter('idExtractor')($scope.searchFilter.params.departments),
                //         roleIds: $filter('idExtractor')($scope.searchFilter.params.roles),
                //         active: $filter('activityAttributeFormatter')($scope.searchFilter.params.active),
                //         userName: $scope.searchFilter.params.userName
                //     });
                // };

                /**
                 * Инициализация таблицы пользователей
                 */
                $scope.flGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: RefBookFLResource,
                        // requestParameters: function () {
                        //     return {
                        //         filter: $scope.filterParams()
                        //     }
                        // },
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
                                formatter: $filter('personIdFormatter')
                            },
                            {
                                name: 'vip',
                                formatter: $filter('vipFormatter')
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
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'docName',
                                formatter: $filter('permissiveFormatter')
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
                                formatter: $filter('codeFormatter')
                            },
                            {
                                name: 'inn',
                                formatter: $filter('permissiveFormatter')
                            },
                            {
                                name: 'innForeign',
                                formatter: $filter('permissiveFormatter')
                            },
                            {
                                name: 'snils',
                                formatter: $filter('permissiveFormatter')
                            },
                            {
                                name: 'address',
                                formatter: $filter('russianAddressFormatter')
                            },
                            {
                                name: 'address',
                                formatter: $filter('foreignAddressFormatter')
                            },
                            {
                                name: 'source',
                                formatter: $filter('nameFormatter')
                            },
                            {
                                name: 'version',
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'versionEnd',
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'id'
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION
                    }
                };
            }
        ])
}());