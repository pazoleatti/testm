(function () {
    'use strict';

    /**
     * @description Модуль для отображения списка справочников
     */
    angular.module('app.refBookList', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('refBookList', {
                url: '/refbooks/refBookList',
                templateUrl: 'client/app/refbooks/refBookList.html',
                controller: 'refBookListCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_NSI)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        .controller('refBookListCtrl', ['$scope', '$filter', 'RefBookListResource', 'APP_CONSTANTS', '$state',
            function ($scope, $filter, RefBookListResource, APP_CONSTANTS, $state) {

                function linkFormatter(cellValue, options, row) {
                    var url;
                    if (row.hierarchic) {
                        url = $state.href('hierRefBook', {refBookId: row.id});
                    } else {
                        url = $state.href('linearRefBook', {refBookId: row.id});
                    }
                    return '<a href="' + url + '">' + cellValue + '</a>';
                }

                function typeFormatter(cellValue, options, row) {
                    if (row.readOnly) {
                        return APP_CONSTANTS.REFBOOK_EDITING.IS_READ_ONLY;
                    } else {
                        return APP_CONSTANTS.REFBOOK_EDITING.NOT_IS_READ_ONLY;
                    }
                }

                // Переменная для поля поиска
                $scope.searchFilter = {
                    params: {}
                };

                $scope.refBookListGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: RefBookListResource,
                        requestParameters: function () {
                            return {
                                filter: $scope.searchFilter.params.text
                            };
                        },
                        colNames: [
                            $filter('translate')('refBooks.refBooksList.columnHeader.refBookName'),
                            $filter('translate')('refBooks.refBooksList.columnHeader.refBookType')
                        ],
                        colModel: [
                            {
                                name: 'name',
                                index: 'name',
                                width: 600,
                                formatter: linkFormatter
                            },
                            {
                                name: 'readOnly',
                                index: 'readOnly',
                                width: 250,
                                formatter: typeFormatter
                            }
                        ],
                        sortname: 'name',
                        hidegrid: false
                    }
                };

                $scope.refreshGrid = function (page) {
                    $scope.refBookListGrid.ctrl.refreshGrid(page);
                };
            }
        ]);
}());