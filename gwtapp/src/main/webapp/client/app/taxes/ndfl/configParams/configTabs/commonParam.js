(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Общие параметры"
     */

    angular.module('app.commonParam', [])

    /**
     * @description контроллер вкладки "Общие параметры"
     */
        .controller('commonParamController', ['$scope', '$filter', 'CommonParamResource', 'APP_CONSTANTS',
            function ($scope, $filter, CommonParamResource, APP_CONSTANTS) {


                $scope.defaultParamGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        gridName: 'commonParamGrid',
                        datatype: "angularResource",
                        angularResource: CommonParamResource,
                        value: [],
                        colNames: [
                            $filter('translate')('commonParam.grid.columnName.code'),
                            $filter('translate')('commonParam.grid.columnName.value')
                        ],
                        colModel: [
                            {name: 'description', index: 'code', width: 400},
                            {name: 'value', index: 'value', width: 250}
                        ]
                    },
                    rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                    rowList: APP_CONSTANTS.COMMON.PAGINATION,
                    sortname: 'code',
                    viewrecords: true,
                    sortorder: "asc",
                    hidegrid: false

                };
            }])

    ;
}());