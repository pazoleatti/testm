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

        .controller('departmentConfigCtrl', ['$scope', '$filter', '$rootScope', 'APP_CONSTANTS', 'DepartmentConfigResource', '$http', '$aplanaModal', '$dialogs', '$logPanel', 'PermissionChecker', 'Upload',
            function ($scope, $filter, $rootScope, APP_CONSTANTS, DepartmentConfigResource, $http, $aplanaModal, $dialogs, $logPanel, PermissionChecker, Upload) {

                var defaultDepartment;

                $scope.searchFilter = {
                    params: getDefaultFilterParams(),
                    ajaxFilter: [],
                    isClear: false,
                    hideExtendedFilter: false
                };

                // Фильтр по-умолчанию
                function getDefaultFilterParams() {
                    return {
                        department: defaultDepartment,
                        relevance: APP_CONSTANTS.DEPARTMENT_CONFIG_RELEVANCE_SELECT.DATE,
                        relevanceDate: new Date().format("yyyy-mm-dd"),
                        kpp: null, oktmo: null, taxOrganCode: null
                    };
                }

                function isFilterParamsEquals(params1, params2) {
                    return (params1.department ? params1.department.id : null) === (params2.department ? params2.department.id : null) &&
                        params1.relevance.id === params2.relevance.id &&
                        params1.relevanceDate === params2.relevanceDate &&
                        (!params1.kpp && !params2.kpp || params1.kpp === params2.kpp) &&
                        (!params1.oktmo && !params2.oktmo || params1.oktmo === params2.oktmo) &&
                        (!params1.taxOrganCode && !params2.taxOrganCode || params1.taxOrganCode === params2.taxOrganCode);
                }

                function isEmpty(params) {
                    return !params.department && (params.relevance.id == APP_CONSTANTS.DEPARTMENT_CONFIG_RELEVANCE_SELECT.ALL || !params.relevanceDate) &&
                        !params.kpp && !params.oktmo && !params.taxOrganCode;
                }

                // Определение доступности кнопки "Сбросить"
                $scope.searchFilter.isClearByFilterParams = function () {
                    $scope.searchFilter.isClear = !isFilterParamsEquals($scope.searchFilter.params, getDefaultFilterParams());
                    $scope.searchFilter.isEmpty = isEmpty($scope.searchFilter.params);
                };
                // Сброс фильтра
                $scope.searchFilter.resetFilterParams = function () {
                    $scope.searchFilter.params = getDefaultFilterParams();
                };
                // Определение подразделения по-умолчанию
                var unwatchDepartment = $scope.$watch("searchFilter.params.department", function (department) {
                    if (department) {
                        defaultDepartment = department;
                        unwatchDepartment();
                        $scope.refreshGrid();
                    }
                });

                $scope.$watch("searchFilter.params.relevance", function () {
                    if ($scope.searchFilter.params.relevance && $scope.searchFilter.params.relevance.id === APP_CONSTANTS.DEPARTMENT_CONFIG_RELEVANCE_SELECT.ALL.id) {
                        $scope.searchFilter.params.relevanceDate = null;
                    } else {
                        $scope.searchFilter.params.relevanceDate = new Date().format("yyyy-mm-dd");
                    }
                });

                $scope.filterRequestParam = function () {
                    return JSON.stringify({
                        departmentId: $scope.searchFilter.params.department ? $scope.searchFilter.params.department.id : undefined,
                        relevanceDate: $scope.searchFilter.params.relevance.id === APP_CONSTANTS.DEPARTMENT_CONFIG_RELEVANCE_SELECT.DATE.id ?
                            $scope.searchFilter.params.relevanceDate : null,
                        oktmo: $scope.searchFilter.params.oktmo,
                        kpp: $scope.searchFilter.params.kpp,
                        taxOrganCode: $scope.searchFilter.params.taxOrganCode
                    });
                };

                $scope.departmentConfigGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: DepartmentConfigResource,
                        requestParameters: function () {
                            return {
                                filter: $scope.filterRequestParam()
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
                            $filter('translate')('departmentConfig.header.approveOrgName'),
                            $filter('translate')('departmentConfig.header.reorganizationCode'),
                            $filter('translate')('departmentConfig.header.reorgKpp'),
                            $filter('translate')('departmentConfig.header.reorgInn'),
                            $filter('translate')('departmentConfig.header.reorgSuccessorKpp'),
                            $filter('translate')('departmentConfig.header.reorgSuccessorName'),
                            $filter('translate')('departmentConfig.header.relatedKppOktmo')
                        ],
                        colModel: [
                            {name: 'id', index: 'id', hidden: true, key: true},
                            {name: 'rowOrd', index: 'row_ord', width: 55},
                            {
                                name: 'startDate',
                                index: 'start_date',
                                width: 100,
                                formatter: $filter('dateFormatter')
                            },
                            {
                                name: 'endDate',
                                index: 'end_date',
                                width: 120,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'department.name', index: 'department_name', width: 170},
                            {name: 'kpp', index: 'kpp', width: 75},
                            {
                                name: 'oktmo',
                                index: 'oktmo_code',
                                width: 250,
                                formatter: $filter('codeNameFormatter')
                            },
                            {name: 'taxOrganCode', index: 'tax_organ_code', width: 85},
                            {
                                name: 'presentPlace',
                                index: 'present_place_code',
                                width: 210,
                                formatter: $filter('codeNameFormatter')
                            },
                            {name: 'name', index: 'name', width: 150},
                            {name: 'phone', index: 'phone', width: 120},
                            {
                                name: 'signatoryMark',
                                index: 'signatory_code',
                                width: 210,
                                formatter: $filter('codeNameFormatter')
                            },
                            {name: 'signatorySurName', index: 'signatory_surname', width: 110},
                            {name: 'signatoryFirstName', index: 'signatory_firstname', width: 110},
                            {name: 'signatoryLastName', index: 'signatory_lastname', width: 130},
                            {name: 'approveDocName', index: 'approve_doc_name', width: 165},
                            {name: 'approveOrgName', index: 'approve_org_name', width: 165},
                            {
                                name: 'reorganization',
                                index: 'reorg_code',
                                width: 200,
                                formatter: $filter('codeNameFormatter')
                            },
                            {name: 'reorgKpp', index: 'reorg_kpp', width: 150},
                            {name: 'reorgInn', index: 'reorg_inn', width: 150},
                            {name: 'reorgSuccessorKpp', index: 'reorg_successor_kpp', width: 150},
                            {name: 'reorgSuccessorName', index: 'reorg_successor_name', width: 150},
                            {
                                name: 'relatedKppOktmo',
                                index: 'related_kpp_oktmo',
                                width: 150,
                                formatter: $filter('kppOktmoPairFormatter')
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'row_ord',
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
                                    mode: "CREATE",
                                    refreshGrid: $scope.refreshGrid
                                };
                            }
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
                                        record: $scope.departmentConfigGrid.value[0],
                                        refreshGrid: $scope.refreshGrid
                                    };
                                }
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
                    $http({
                        method: "POST",
                        url: "controller/actions/departmentConfig/export/excel",
                        params: {
                            filter: $scope.filterRequestParam(),
                            pagingParams: JSON.stringify({
                                property: $scope.departmentConfigGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortname'),
                                direction: $scope.departmentConfigGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortorder')
                            })
                        }
                    }).success(function (response) {
                        if (response.uuid) {
                            $logPanel.open('log-panel-container', response.uuid);
                        }
                    });
                };

                $scope.importDepartmentConfig = function (file, skipDepartmentCheck) {
                    if (file) {
                        Upload.upload({
                            url: 'controller/actions/departmentConfig/import',
                            data: {
                                uploader: file,
                                action: JSON.stringify({
                                    departmentId: $scope.searchFilter.params.department ? $scope.searchFilter.params.department.id : undefined,
                                    skipDepartmentCheck: skipDepartmentCheck
                                })
                            }
                        }).progress(function (e) {
                        }).then(function (response) {
                            $logPanel.close();
                            if (response.data && response.data.uuid) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            }
                            if (response.data && response.data.confirmDepartmentCheck) {
                                $dialogs.confirmDialog({
                                    content: response.data.confirmDepartmentCheck,
                                    okBtnCaption: $filter('translate')('common.button.yes'),
                                    cancelBtnCaption: $filter('translate')('common.button.no'),
                                    okBtnClick: function () {
                                        $scope.importDepartmentConfig(file, true);
                                    },
                                    cancelBtnClick: function () {
                                        file.msClose && file.msClose();
                                    }
                                });
                            } else {
                                file.msClose && file.msClose();
                            }
                        });
                    }
                };

                // Проверка прав для выделенных строк таблицы
                $scope.checkPermissionForSelectedItems = function (permission) {
                    var selectedItems = $scope.departmentConfigGrid.value;
                    if (selectedItems && selectedItems.length > 0) {
                        return selectedItems.every(function (item) {
                            return PermissionChecker.check(item, permission);
                        });
                    } else {
                        return false;
                    }
                };
            }])
    ;
}());