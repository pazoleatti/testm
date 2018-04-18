(function () {
    'use strict';

    /**
     * @description Модуль для списка справочников из настройщика
     */
    angular.module('app.refBookConfList', [])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('refBookConfList', {
                url: '/administration/refBookConfList',
                templateUrl: 'client/app/administration/refBookConf/refBookConfList.html?v=${buildUuid}',
                controller: 'RefBookConfListCtrl'
            });
        }])

        .controller('RefBookConfListCtrl', ['$scope', '$filter', '$window', 'RefBookConfResource', 'Upload', '$http', 'APP_CONSTANTS', '$logPanel',
            function ($scope, $filter, $window, RefBookConfResource, Upload, $http, APP_CONSTANTS, $logPanel) {

                RefBookConfResource.querySource({
                        projection: "refBookConfList"
                    },
                    function (data) {
                        if (data) {
                            $scope.refBookConfListGrid.ctrl.refreshGridData(data);
                        }
                    }
                );

                $scope.refBookConfListGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "local",
                        data: [],
                        colNames: [
                            '',
                            $filter('translate')('refBookConfList.grid.refBookName'),
                            $filter('translate')('refBookConfList.grid.visible'),
                            $filter('translate')('refBookConfList.grid.readOnly'),
                            $filter('translate')('refBookConfList.grid.regionality'),
                            $filter('translate')('refBookConfList.grid.refBookType')],
                        colModel: [
                            {name: 'id', index: 'id', key: true, hidden: true},
                            {
                                name: 'name',
                                index: 'name',
                                width: 500
                            }, {
                                name: 'visible',
                                index: 'visible',
                                width: 250,
                                formatter: function (cellValue, options, row) {
                                    if (row.visible) {
                                        return APP_CONSTANTS.REFBOOK_EDITING.IS_READ_ONLY;
                                    } else {
                                        return APP_CONSTANTS.REFBOOK_EDITING.NOT_IS_READ_ONLY;
                                    }
                                }
                            }, {
                                name: 'readOnly',
                                index: 'readOnly',
                                formatter: function (cellValue, options, row) {
                                    if (row.readOnly) {
                                        return APP_CONSTANTS.REFBOOK_EDITING.IS_READ_ONLY;
                                    } else {
                                        return APP_CONSTANTS.REFBOOK_EDITING.NOT_IS_READ_ONLY;
                                    }
                                }
                            },
                            {name: 'regionality', index: 'regionality'},
                            {
                                name: 'refBookType',
                                index: 'refBookType',
                                formatter: function (cellValue) {
                                    return $filter('translate')('refBook.type.' + cellValue);
                                }
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'name',
                        viewrecords: true,
                        sortorder: "asc",
                        hidegrid: false
                    }
                };

                /** Экспорт скриптов и xsd */
                $scope.refBookExport = function () {
                    RefBookConfResource.query({
                        projection: 'exportRefBooks'
                    }, function (data) {
                        if (data.uuid) {
                            $window.location = "controller/rest/blobData/" + data.uuid + "/conf";
                        }
                    });
                };

                /** Импорт скриптов и xsd */
                $scope.refBookImport = function (file) {
                    if (file) {
                        Upload.upload({
                            url: 'controller/rest/refBookConf/import',
                            data: {uploader: file}
                        }).progress(function (e) {
                        }).then(function (response) {
                            var uuid = response.data;
                            if (uuid) {
                                $logPanel.open('log-panel-container', uuid);
                            }
                        });
                    }
                };
            }
        ])
}());