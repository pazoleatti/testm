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

        .controller('refBookListCtrl', ['$scope', '$filter', 'RefBookListResource', 'APP_CONSTANTS', function ($scope, $filter, RefBookListResource, APP_CONSTANTS) {

            function linkFormatter(cellValue, options, row) {
                if (row.refBookType === APP_CONSTANTS.REFBOOK_TYPE.LINEAR) {
                    return "<a href='index.html#/refBooks/linearRefBook/" + row.refBookId + "'>" + cellValue + "</a>";
                } else {
                    return "<a href='index.html#/refBooks/hierRefBook/" + row.refBookId + "'>" + cellValue + "</a>";
                }
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
                            width: 250,
                            formatter: linkFormatter
                        },
                        {
                            name: 'isReadOnly',
                            index: 'isReadOnly',
                            width: 250,
                            formatter: typeFormatter
                        }
                    ],
                    rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                    rowList: APP_CONSTANTS.COMMON.PAGINATION,
                    sortname: 'refBoookName',
                    viewrecords: true,
                    sortorder: "asc",
                    hidegrid: false
                }
            };
        }])
    ;
}());