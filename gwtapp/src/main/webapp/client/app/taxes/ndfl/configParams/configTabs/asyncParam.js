(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Параметры асинхронных заданий"
     */

    angular.module('app.asyncParam', [])

    /**
     * @description контроллер вкладки "Параметры асинхронных заданий"
     */
        .controller('asyncParamController', ['$scope', '$filter', 'AsyncTaskResource', 'APP_CONSTANTS',
            function ($scope, $filter, AsyncTaskResource, APP_CONSTANTS) {


                $scope.asyncParamGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        gridName: 'asyncParamGrid',
                        datatype: "angularResource",
                        angularResource: AsyncTaskResource,
                        value: [],
                        colNames: [
                            $filter('translate')('asyncParam.grid.columnName.taskType'),
                            $filter('translate')('asyncParam.grid.columnName.limitKind'),
                            $filter('translate')('asyncParam.grid.columnName.taskLimit'),
                            $filter('translate')('asyncParam.grid.columnName.shortQueueLimit')

                        ],
                        colModel: [
                            {name: 'name', index: 'name', width: 400},
                            {name: 'limitKind', index: 'limitKind', width: 250},
                            {name: 'taskLimit', index: 'taskLimit', width: 400},
                            {name: 'shortQueueLimit', index: 'shortQueueLimit', width: 250, formatter: 'htmlFormat'}

                        ]
                    },
                    rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                    rowList: APP_CONSTANTS.COMMON.PAGINATION,
                    sortname: 'name',
                    viewrecords: true,
                    sortorder: "asc",
                    hidegrid: false

                };
            }])

    ;
}());