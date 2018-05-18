(function () {
    'use strict';

    /**
     * @description Модуль для отображения списка справочников
     */
    angular.module('app.refBookList', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('refBookList', {
                url: '/refbooks/refBookList',
                templateUrl: 'client/app/refbooks/refBookList.html?v=${buildUuid}',
                controller: 'refBookListCtrl'
            });
        }])

        .controller('refBookListCtrl', ['$scope', '$filter', 'RefBookListResource', 'APP_CONSTANTS', '$state',
            function ($scope, $filter, RefBookListResource, APP_CONSTANTS, $state) {

            function linkFormatter(cellValue, options, row) {
                var url;
                if (row.refBookType === APP_CONSTANTS.REFBOOK_TYPE.LINEAR) {
                    url = $state.href('linearRefBook', {refBookId: row.refBookId});
                } else {
                    url = $state.href('hierRefBook', {refBookId: row.refBookId});
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

            $scope.refBookListGrid = {
                ctrl: {},
                value: [],
                options: {
                    datatype: "angularResource",
                    angularResource: RefBookListResource,
                    colNames: [
                        $filter('translate')('refBooks.refBooksList.columnHeader.refBookName'),
                        $filter('translate')('refBooks.refBooksList.columnHeader.refBookType')
                    ],
                    colModel: [
                        {
                            name: 'refBookName',
                            index: 'refBookName',
                            width: 600,
                            formatter: linkFormatter
                        },
                        {
                            name: 'isReadOnly',
                            index: 'isReadOnly',
                            width: 250,
                            formatter: typeFormatter
                        }
                    ],
                    sortname: 'refBoookName',
                    viewrecords: true,
                    sortorder: "asc",
                    hidegrid: false
                }
            };
        }])
    ;
}());