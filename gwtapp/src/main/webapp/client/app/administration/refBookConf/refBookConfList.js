(function () {
    'use strict';

    /**
     * @description Модуль для списка справочников из настройщика
     */
    angular.module('app.refBookConfList', [])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('refBookConfList', {
                url: '/administration/refBookConfList',
                templateUrl: 'client/app/administration/refBookConf/refBookConfList.html',
                controller: 'RefBookConfListCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_SETTINGS)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        .controller('RefBookConfListCtrl', ['$scope', '$filter', '$window', 'RefBookConfResource', 'Upload', '$http', 'APP_CONSTANTS', '$logPanel',
            function ($scope, $filter, $window, RefBookConfResource, Upload, $http, APP_CONSTANTS, $logPanel) {

                $scope.refBookConfListGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: RefBookConfResource,
                        requestParameters: function () {
                            return {
                                projection: 'refBookConfList'
                            };
                        },
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
                    $http({
                        method: "GET",
                        url: "controller/actions/refBookConf/export"
                    }).success(function (uuid) {
                        if (uuid) {
                            $window.location = "controller/rest/blobData/" + uuid + "/conf";
                        }
                    });
                };

                /** Импорт скриптов и xsd */
                $scope.refBookImport = function (file) {
                    if (file) {
                        Upload.upload({
                            url: 'controller/actions/refBookConf/import',
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