(function () {
    'use strict';

    /**
     * @description Модуль для работы с вкладкой "Реквизиты"
     */
    angular.module('app.ndflFL', [])

    /**
     * @description Контроллер вкладки "Реквизиты"
     */
        .controller('ndflFLCtrl', [
            '$scope', '$stateParams', 'NdflPersonResource', '$filter', 'APP_CONSTANTS', '$rootScope',
            function ($scope, $stateParams, NdflPersonResource, $filter, APP_CONSTANTS, $rootScope) {

                var tab = $scope.ndfFLTab;

                tab.refreshGrid = function (page) {
                    if ($scope.ndflPersonGrid.ctrl.refreshGrid) {
                        $scope.ndflPersonGrid.ctrl.refreshGrid(page);
                    }
                };

                tab.getGrid = function () {
                    return $scope.ndflPersonGrid;
                };

                // Обработчик на активацию таба
                $scope.$watch("ndfFLTab.active", function (newValue, oldValue) {
                    if (!tab.isDataLoaded) {
                        if (newValue && !oldValue) {
                            tab.refreshGrid(1);
                        }
                    }
                });

                // Получение номера раздела, который отображается на вкладке
                tab.getSection = function () {
                    return 1
                };

                $scope.ndflPersonGrid = {
                    init: function (ctrl) {
                        ctrl.loadComplete = function () {
                            tab.isDataLoaded = true;
                        };
                    },
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: NdflPersonResource,
                        requestParameters: function () {
                            return {
                                projection: "ndflPersons",
                                ndflFilter: JSON.stringify($scope.ndflFilter)
                            };
                        },
                        height: 250,
                        colNames: [
                            $filter('translate')('ndflFL.title.numberpp'),
                            $filter('translate')('ndflFL.title.inp'),
                            $filter('translate')('ndflFL.title.lastName'),
                            $filter('translate')('ndflFL.title.firstName'),
                            $filter('translate')('ndflFL.title.middleName'),
                            $filter('translate')('ndflFL.title.dateOfBirth'),
                            $filter('translate')('ndflFL.title.citizenship'),
                            $filter('translate')('ndflFL.title.innNp'),
                            $filter('translate')('ndflFL.title.innForeign'),
                            $filter('translate')('ndflFL.title.codeDul'),
                            $filter('translate')('ndflFL.title.numberDul'),
                            $filter('translate')('ndflFL.title.statusCode'),
                            $filter('translate')('ndflFL.title.subjectCode'),
                            $filter('translate')('ndflFL.title.index'),
                            $filter('translate')('ndflFL.title.area'),
                            $filter('translate')('ndflFL.title.city'),
                            $filter('translate')('ndflFL.title.locality'),
                            $filter('translate')('ndflFL.title.street'),
                            $filter('translate')('ndflFL.title.house'),
                            $filter('translate')('ndflFL.title.building'),
                            $filter('translate')('ndflFL.title.flat'),
                            $filter('translate')('ndflFL.title.snils'),
                            $filter('translate')('ndflFL.title.id'),
                            $filter('translate')('ndflFL.title.modifiedDate'),
                            $filter('translate')('ndflFL.title.modifiedBy'),
                            $filter('translate')('ndflFL.title.asnu')],
                        colModel: [
                            {name: 'rowNum', index: 'rowNum', width: 75, key: true},
                            {name: 'inp', index: 'inp', width: 100},
                            {name: 'lastName', index: 'lastName', width: 130, formatter: $filter('personLinkFormatters')},
                            {name: 'firstName', index: 'firstName', width: 110},
                            {name: 'middleName', index: 'middleName', width: 130},
                            {
                                name: 'birthDay',
                                index: 'birthDay',
                                width: 125,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'citizenship', index: 'citizenship', width: 195},
                            {name: 'innNp', index: 'innNp', width: 95},
                            {name: 'innForeign', index: 'innForeign', width: 95},
                            {name: 'idDocType', index: 'idDocType', width: 85},
                            {name: 'idDocNumber', index: 'idDocNumber', width: 105},
                            {name: 'status', index: 'status', width: 100},
                            {name: 'regionCode', index: 'regionCode', width: 105, sortable: false},
                            {name: 'postIndex', index: 'postIndex', width: 100},
                            {name: 'area', index: 'area', width: 160},
                            {name: 'city', index: 'city', width: 160},
                            {name: 'locality', index: 'locality', width: 160},
                            {name: 'street', index: 'street', width: 160},
                            {name: 'house', index: 'house', width: 70},
                            {name: 'building', index: 'building', width: 70},
                            {name: 'flat', index: 'flat', width: 80},
                            {name: 'snils', index: 'snils', width: 120, sortable: false},
                            {name: 'id', index: 'id', width: 170},
                            {name: 'modifiedDate', index: 'modifiedDate', width: 215, formatter: $filter('dateTimeFormatter')},
                            {name: 'modifiedBy', index: 'modifiedBy', width: 300},
                            {name: 'asnuName', index: 'name', width: 300}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'rowNum',
                        viewrecords: true,
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: true,
                        fullScreen: true
                    }
                };
            }])

        .filter('personLinkFormatters', function () {
                return function (cellValue, options, data) {
                    if (data.personId) {
                        return "<a href='index.html#/personRegistry/personCard/" + data.personId + "' target='_blank'>" + cellValue + "</a>";
                    }
                    return cellValue;
                }
            }
        );
}());