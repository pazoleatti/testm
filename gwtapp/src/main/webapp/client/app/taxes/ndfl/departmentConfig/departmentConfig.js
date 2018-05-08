(function () {
    'use strict';
    /**
     * @description Модуль для отображения настроек подразделений
     */
    angular.module('app.departmentConfig', ['app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('departmentConfig', {
                url: '/taxes/departmentConfig',
                templateUrl: 'client/app/taxes/ndfl/departmentConfig/departmentConfig.html',
                controller: 'departmentConfigCtrl'
            });
        }])

        .controller('departmentConfigCtrl', ['$scope', '$filter', '$rootScope', 'APP_CONSTANTS', 'DepartmentConfigResource',
            function ($scope, $filter, $rootScope, APP_CONSTANTS, DepartmentConfigResource) {
                $scope.disableSearchButton = true;
                // Используем для отображения информации о версии
                $scope.versionLabelInfo = $scope.versionLabelInfo = $filter('translate')('departmentConfig.label.version').replace('{0}', '-').replace('{1}', '-');

                $scope.searchFilter = {
                    params: {},
                    ajaxFilter: [],
                    isClear: false,
                    filterName: 'departmentConfigFilter'
                };

                $scope.$on(APP_CONSTANTS.EVENTS.USER_TB_SELECT, function (event, department) {
                    $scope.searchFilter.params.department = department;
                });
                $scope.$on(APP_CONSTANTS.EVENTS.LAST_PERIOD_SELECT, function (event, period) {
                    $scope.searchFilter.params.period = period;
                });
                // наблюдает выбрано ли подразделение и в зависимости от этого запускает инициализацию списка периодов и регулирует активность кнопки поиска
                $scope.$watch("searchFilter.params.department", function (value) {
                    if (typeof(value) !== 'undefined' && value != null) {
                        $scope.$broadcast(APP_CONSTANTS.EVENTS.DEPARTMENT_SELECTED, $scope.searchFilter.params.department.id);
                        $scope.disableSearchButton = false;
                    } else {
                        $scope.disableSearchButton = true;
                    }
                });
                // наблюдает выбран ли период и в зависимости от этого регулирует активность кнопки поиска
                $scope.$watch("searchFilter.params.period", function (value) {
                    if (typeof(value) !== 'undefined' && value != null) {
                        $scope.disableSearchButton = false;
                    } else {
                        $scope.disableSearchButton = true;
                    }
                });
                $scope.refreshGrid = function (page) {
                    $scope.departmentConfigGrid.ctrl.refreshGrid(page);
                };
                $scope.$watch("departmentConfigGrid.ctrl.getRawData()", function (value) {
                    if (value.length !== 0) {
                        var dateFrom = '-';
                        var dateTo = '-';
                        if (typeof(value[0].departmentConfigStartDate) !== 'undefined' && value[0].departmentConfigStartDate !== null) {
                            dateFrom = $filter('dateFormatter')(value[0].departmentConfigStartDate);
                        } else {
                            dateFrom = '-';
                        }
                        if (typeof(value[0].departmentConfigEndDate) !== 'undefined' && value[0].departmentConfigEndDate !== null) {
                            dateTo = $filter('dateFormatter')(value[0].departmentConfigEndDate);
                        } else {
                            dateTo = '-';
                        }
                        $scope.versionLabelInfo = $filter('translate')('departmentConfig.label.version').replace('{0}', dateFrom).replace('{1}', dateTo);
                    }
                });

                $scope.departmentConfigGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: DepartmentConfigResource,
                        requestParameters: function () {
                            return {
                                filter: JSON.stringify({
                                    departmentId: $scope.searchFilter.params.department ? $scope.searchFilter.params.department.id : undefined,
                                    reportPeriodId: $scope.searchFilter.params.period ? $scope.searchFilter.params.period.id : undefined
                                })
                            };
                        },
                        colNames: [
                            $filter('translate')('departmentConfig.header.rowOrd'),
                            $filter('translate')('departmentConfig.header.taxOrganCode'),
                            $filter('translate')('departmentConfig.header.kpp'),
                            $filter('translate')('departmentConfig.header.presentPlace'),
                            $filter('translate')('departmentConfig.header.name'),
                            $filter('translate')('departmentConfig.header.oktmo'),
                            $filter('translate')('departmentConfig.header.phone'),
                            $filter('translate')('departmentConfig.header.reorganization'),
                            $filter('translate')('departmentConfig.header.reorgInn'),
                            $filter('translate')('departmentConfig.header.reorgKpp'),
                            $filter('translate')('departmentConfig.header.signatoryId'),
                            $filter('translate')('departmentConfig.header.signatorySurName'),
                            $filter('translate')('departmentConfig.header.signatoryFirstName'),
                            $filter('translate')('departmentConfig.header.signatoryLastName'),
                            $filter('translate')('departmentConfig.header.approveDocName'),
                            $filter('translate')('departmentConfig.header.approveOrgName'),
                            $filter('translate')('departmentConfig.header.modifiedDate'),
                            $filter('translate')('departmentConfig.header.modifiedBy')
                        ],
                        colModel: [{name: 'rowOrd', index: 'rowOrd', width: 55},
                            {name: 'taxOrganCode', index: 'taxOrganCode', width: 80},
                            {name: 'kpp', index: 'kpp', width: 85},
                            {name: 'presentPlace', index: 'presentPlace', width: 120},
                            {name: 'name', index: 'name', width: 120},
                            {name: 'oktmo', index: 'oktmo', width: 85},
                            {name: 'phone', index: 'phone', width: 150},
                            {name: 'reorganization', index: 'reorganization', width: 120},
                            {name: 'reorgInn', index: 'reorgInn', width: 150},
                            {name: 'reorgKpp', index: 'reorgKpp', width: 150},
                            {name: 'signatoryId', index: 'signatoryId', width: 110},
                            {name: 'signatorySurName', index: 'signatorySurName', width: 150},
                            {name: 'signatoryFirstName', index: 'signatoryFirstName', width: 150},
                            {name: 'signatoryLastName', index: 'signatoryLastName', width: 150},
                            {name: 'approveDocName', index: 'approveDocName', width: 150},
                            {name: 'approveOrgName', index: 'approveOrgName', width: 150},
                            {name: 'modifiedDate', index: 'modifiedDate', width: 150},
                            {
                                name: 'modifiedBy',
                                index: 'modifiedBy',
                                width: 150,
                                formatter: $filter('dateTimeFormatter')
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: false,
                        hidegrid: false,
                        multiselect: true
                    }

                };

            }])
    ;
}());