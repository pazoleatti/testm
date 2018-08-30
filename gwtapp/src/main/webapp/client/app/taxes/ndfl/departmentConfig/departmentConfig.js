(function () {
    'use strict';
    /**
     * @description Модуль для отображения настроек подразделений
     */
    angular.module('app.departmentConfig', ['app.departmentConfigModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('departmentConfig', {
                url: '/taxes/departmentConfig',
                templateUrl: 'client/app/taxes/ndfl/departmentConfig/departmentConfig.html',
                controller: 'departmentConfigCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL_SETTINGS)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        .controller('departmentConfigCtrl', ['$scope', '$filter', '$rootScope', 'APP_CONSTANTS', 'DepartmentConfigResource', '$http', '$aplanaModal', '$dialogs', '$logPanel',
            function ($scope, $filter, $rootScope, APP_CONSTANTS, DepartmentConfigResource, $http, $aplanaModal, $dialogs, $logPanel) {
                var defaultDepartment = undefined;
                $scope.searchFilter = {
                    params: getDefaultFilterParams(),
                    ajaxFilter: [],
                    isClear: false,
                    hideExtendedFilter: false
                };

                function getDefaultFilterParams() {
                    return {
                        department: defaultDepartment,
                        relevance: APP_CONSTANTS.DEPARTMENT_CONFIG_RELEVANCE_SELECT.DATE,
                        relevanceDate: new Date().format("yyyy-mm-dd"),
                        kpp: null, oktmo: null, taxOrganCode: null
                    }
                }

                function filterParamsEquals(params1, params2) {
                    return params1.department.id === params2.department.id &&
                        params1.relevance.id === params2.relevance.id &&
                        params1.relevanceDate === params2.relevanceDate &&
                        (!params1.kpp && !params2.kpp || params1.kpp === params2.kpp) &&
                        (!params1.oktmo && !params2.oktmo || params1.oktmo === params2.oktmo) &&
                        (!params1.taxOrganCode && !params2.taxOrganCode || params1.taxOrganCode === params2.taxOrganCode);
                }

                $scope.searchFilter.isClearByFilterParams = function () {
                    $scope.searchFilter.isClear = $scope.searchFilter.params.department && !filterParamsEquals($scope.searchFilter.params, getDefaultFilterParams());
                };

                $scope.searchFilter.resetFilterParams = function () {
                    $scope.searchFilter.params = getDefaultFilterParams();
                };

                var unwatchDepartment = $scope.$watch("searchFilter.params.department", function (department) {
                    if (department) {
                        defaultDepartment = department;
                        unwatchDepartment();
                        $scope.refreshGrid();
                    }
                });

                $scope.onRelevanceChange = function () {
                    if ($scope.searchFilter.params.relevance.id === APP_CONSTANTS.DEPARTMENT_CONFIG_RELEVANCE_SELECT.ALL.id) {
                        $scope.relevanceDateDisabled = true;
                        $scope.searchFilter.params.relevanceDate = null;
                    } else {
                        $scope.relevanceDateDisabled = false;
                        $scope.searchFilter.params.relevanceDate = new Date().format("yyyy-mm-dd");
                    }
                };

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
                                    relevanceDate: $scope.searchFilter.params.relevance.id === APP_CONSTANTS.DEPARTMENT_CONFIG_RELEVANCE_SELECT.DATE.id ?
                                        $scope.searchFilter.params.relevanceDate : null,
                                    oktmo: $scope.searchFilter.params.oktmo,
                                    kpp: $scope.searchFilter.params.kpp,
                                    taxOrganCode: $scope.searchFilter.params.taxOrganCode
                                })
                            };
                        },
                        colNames: [
                            '',
                            $filter('translate')('departmentConfig.header.rowOrd'),
                            $filter('translate')('departmentConfig.header.startDate'),
                            $filter('translate')('departmentConfig.header.endDate'),
                            $filter('translate')('departmentConfig.header.department'),
                            $filter('translate')('departmentConfig.header.kpp'),
                            $filter('translate')('departmentConfig.header.oktmo'),
                            $filter('translate')('departmentConfig.header.taxOrganCode'),
                            $filter('translate')('departmentConfig.header.presentPlace'),
                            $filter('translate')('departmentConfig.header.name'),
                            $filter('translate')('departmentConfig.header.phone'),
                            $filter('translate')('departmentConfig.header.signatoryCode'),
                            $filter('translate')('departmentConfig.header.signatorySurName'),
                            $filter('translate')('departmentConfig.header.signatoryFirstName'),
                            $filter('translate')('departmentConfig.header.signatoryLastName'),
                            $filter('translate')('departmentConfig.header.approveDocName'),
                            $filter('translate')('departmentConfig.header.reorganizationCode'),
                            $filter('translate')('departmentConfig.header.reorgKpp'),
                            $filter('translate')('departmentConfig.header.reorgInn')
                        ],
                        colModel: [
                            {name: 'id', index: 'id', hidden: true, key: true, sortable: false},
                            {name: 'rowOrd', index: 'rowOrd', width: 55, sortable: false},
                            {
                                name: 'startDate',
                                index: 'startDate',
                                width: 100,
                                formatter: $filter('dateFormatter'),
                                sortable: false
                            },
                            {
                                name: 'endDate',
                                index: 'endDate',
                                width: 120,
                                formatter: $filter('dateFormatter'),
                                sortable: false
                            },
                            {name: 'department.name', index: 'department.name', width: 170, sortable: false},
                            {name: 'kpp', index: 'kpp', width: 75, sortable: false},
                            {name: 'oktmo.code', index: 'oktmo.code', width: 80, sortable: false},
                            {name: 'taxOrganCode', index: 'taxOrganCode', width: 85, sortable: false},
                            {name: 'presentPlace.name', index: 'presentPlace.name', width: 110, sortable: false},
                            {name: 'name', index: 'name', width: 150, sortable: false},
                            {name: 'phone', index: 'phone', width: 120, sortable: false},
                            {name: 'signatoryMark.name', index: 'signatoryMark.name', width: 90, sortable: false},
                            {name: 'signatorySurName', index: 'signatorySurName', width: 110, sortable: false},
                            {name: 'signatoryFirstName', index: 'signatoryFirstName', width: 110, sortable: false},
                            {name: 'signatoryLastName', index: 'signatoryLastName', width: 130, sortable: false},
                            {
                                name: 'approveDocName',
                                index: 'approveDocName',
                                width: 165,
                                classes: 'grid-cell-white-space',
                                sortable: false
                            },
                            {name: 'reorganization.code', index: 'reorganization.code', width: 120, sortable: false},
                            {name: 'reorgKpp', index: 'reorgKpp', width: 150, sortable: false},
                            {name: 'reorgInn', index: 'reorgInn', width: 150, sortable: false}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: true,
                        disableAutoLoad: true,
                        hidegrid: false,
                        multiselect: true
                    }
                };

                $scope.refreshGrid = function (page) {
                    $scope.departmentConfigGrid.ctrl.refreshGrid(page);
                };

                $scope.createDepartmentConfig = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('departmentConfig.modal.create.title'),
                        templateUrl: 'client/app/taxes/ndfl/departmentConfig/modal/departmentConfigModal.html',
                        controller: 'departmentConfigModalCtrl',
                        windowClass: 'modal1000',
                        resolve: {
                            $shareData: function () {
                                return {
                                    mode: "CREATE"
                                };
                            }
                        }
                    }).result.then(function (needToRefresh) {
                        if (needToRefresh) {
                            $scope.refreshGrid();
                        }
                    });
                };

                $scope.openDepartmentConfig = function () {
                    if ($scope.departmentConfigGrid.value.length === 1) {
                        $aplanaModal.open({
                            title: $filter('translate')('departmentConfig.modal.open.title'),
                            templateUrl: 'client/app/taxes/ndfl/departmentConfig/modal/departmentConfigModal.html',
                            controller: 'departmentConfigModalCtrl',
                            windowClass: 'modal1000',
                            resolve: {
                                $shareData: function () {
                                    return {
                                        mode: "VIEW",
                                        record: $scope.departmentConfigGrid.value[0]
                                    };
                                }
                            }
                        }).result.then(function (needToRefresh) {
                            if (needToRefresh) {
                                $scope.refreshGrid();
                            }
                        });
                    }
                };

                $scope.deleteDepartmentConfig = function () {
                    $dialogs.confirmDialog({
                        content: $filter('translate')('departmentConfig.dialog.delete.confirmMessage'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $http({
                                method: "POST",
                                url: "controller/actions/departmentConfig/delete",
                                data: $filter('idExtractor')($scope.departmentConfigGrid.value)
                            }).then(function (response) {
                                if (response.data.uuid) {
                                    $logPanel.open('log-panel-container', response.data.uuid);
                                }
                                if (response.data.error) {
                                    $dialogs.errorDialog({content: response.data.error});
                                } else {
                                    $scope.refreshGrid();
                                }
                            });
                        }
                    });
                };

                $scope.exportDepartmentConfig = function () {
                    // TODO
                };

                $scope.importDepartmentConfig = function (file) {
                    if (file) {
                        // TODO
                    }
                };
            }])
    ;
}());