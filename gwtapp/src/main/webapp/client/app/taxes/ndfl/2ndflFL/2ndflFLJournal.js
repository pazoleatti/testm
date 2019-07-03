(function () {
    'use strict';

    /**
     * @description Модуль для работы со формами ндфл
     */
    angular.module('app.2ndflFLJournal', [])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('2ndflFLJournal', {
                url: '/taxes/2ndflFLJournal',
                templateUrl: 'client/app/taxes/ndfl/2ndflFL/2ndflFLJournal.html',
                controller: '2ndflFLJournalCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION._2NDFL_FL)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        .controller('2ndflFLJournalCtrl', ['$scope', '$filter', 'APP_CONSTANTS', '$logPanel', '$aplanaModal', 'DeclarationDataResource',
            function ($scope, $filter, APP_CONSTANTS, $logPanel, $aplanaModal, DeclarationDataResource) {

                $scope.refreshGrid = function (page) {
                    $scope._2ndflFLGrid.ctrl.refreshGrid(page);
                };

                function getDefaultFilterParams() {
                    return {};
                }

                $scope.searchFilter = {
                    params: getDefaultFilterParams(),
                    ajaxFilter: [],
                    filterName: '2ndflFLJournalFilter',
                    onCreateComplete: function () {
                        $scope.refreshGrid();
                    },
                    resetFilterParams: function () {
                        $scope.searchFilter.params = getDefaultFilterParams();
                    },
                    isClearByFilterParams: function () {
                        $scope.searchFilter.isClear = stringify($scope.searchFilter.params) !== stringify(getDefaultFilterParams());
                    }
                };

                // поля со значениями null, undefined или "" будут считаться эквивалентными
                function stringify(value) {
                    return JSON.stringify(value, function (key, value) {
                        return value ? value : undefined;
                    });
                }

                /**
                 * Параметры фильтра для запроса данных таблицы ФЛ
                 */
                $scope.filterRequestParam = function () {
                    return stringify({
                        // Реквизиты форм
                        reportPeriodIds: $filter('idExtractor')($scope.searchFilter.params.periods),
                        departmentIds: $filter('idExtractor')($scope.searchFilter.params.departments),
                        declarationDataId: $scope.searchFilter.params.declarationNumber,
                        creationDateFrom: $filter('dateTimeSerializer')($scope.searchFilter.params.creationDateFrom),
                        creationDateTo: $filter('dateTimeSerializer')($scope.searchFilter.params.creationDateTo),
                        formStates: $scope.searchFilter.params.state ? [$scope.searchFilter.params.state.id] : undefined,
                        note: $scope.searchFilter.params.note,
                        creationUserName: $scope.searchFilter.params.creationUserName,
                        kpp: $scope.searchFilter.params.kpp,
                        oktmo: $scope.searchFilter.params.oktmo,
                        // Реквизиты ФЛ
                        lastName: $scope.searchFilter.params.lastName,
                        firstName: $scope.searchFilter.params.firstName,
                        middleName: $scope.searchFilter.params.middleName,
                        birthDateFrom: $scope.searchFilter.params.birthDateFrom,
                        birthDateTo: $scope.searchFilter.params.birthDateTo,
                        docTypeIds: $filter('idExtractor')($scope.searchFilter.params.documentTypes),
                        documentNumber: $scope.searchFilter.params.documentNumber,
                        citizenshipCountryIds: $filter('idExtractor')($scope.searchFilter.params.citizenshipCountries),
                        taxpayerStateIds: $filter('idExtractor')($scope.searchFilter.params.taxpayerStates),
                        inn: $scope.searchFilter.params.inn,
                        innForeign: $scope.searchFilter.params.innForeign,
                        snils: $scope.searchFilter.params.snils
                    });
                };

                $scope._2ndflFLGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        gridName: '2ndflFLGrid',
                        datatype: "angularResource",
                        angularResource: DeclarationDataResource,
                        requestParameters: function () {
                            return {
                                filter: $scope.filterRequestParam(),
                                projection: '2ndflFLDeclarations'
                            };
                        },
                        colNames: [
                            $filter('translate')('title.numberDeclaration'),
                            $filter('translate')('title.formKind'),
                            $filter('translate')('title.person'),
                            $filter('translate')('title.department'),
                            $filter('translate')('title.period'),
                            $filter('translate')('title.state'),
                            $filter('translate')('title.kpp'),
                            $filter('translate')('title.oktmo'),
                            $filter('translate')('title.dateAndTimeCreate'),
                            $filter('translate')('title.creator'),
                            $filter('translate')('title.note')
                        ],
                        colModel: [
                            {name: 'declarationDataId', index: 'declarationDataId', width: 120, key: true},
                            {name: 'declarationType', index: 'declarationType', width: 170},
                            {name: 'person', index: 'person', width: 200, formatter: personLink},
                            {name: 'department', index: 'department', width: 200},
                            {name: 'reportPeriod', index: 'reportPeriod', width: 175},
                            {name: 'state', index: 'state', width: 100},
                            {name: 'kpp', index: 'kpp', width: 85},
                            {name: 'oktmo', index: 'oktmo', width: 80},
                            {
                                name: 'creationDate',
                                index: 'creationDate',
                                width: 215,
                                formatter: $filter('dateTimeFormatter')
                            },
                            {name: 'creationUserName', index: 'creationUserName', width: 130},
                            {name: 'note', index: 'note', width: 200}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'declarationDataId',
                        sortorder: "desc",
                        viewrecords: true,
                        disableAutoLoad: true,
                        multiselect: true,
                        fullScreen: true
                    }
                };

                $scope.delete = function () {
                    if ($scope._2ndflFLGrid.value && $scope._2ndflFLGrid.value.length > 0) {
                        var selectedItems = $scope.ndflReportJournalGrid.value;
                        $dialogs.confirmDialog({
                            content: $filter('translate')('title.deleteDeclarations'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                $http({
                                    method: "POST",
                                    url: "controller/actions/declarationData/delete",
                                    data: $filter('idExtractor')(selectedItems, 'declarationDataId')
                                }).then(function (response) {
                                    if (response.data && response.data.uuid) {
                                        $logPanel.open('log-panel-container', response.data.uuid);
                                    }
                                });
                            }
                        });
                    }
                };

                function personLink(cellValue, options, row) {
                    var value = cellValue;
                    if (!cellValue) {
                        value = $filter('translate')('refBook.fl.table.label.undefined');
                    }
                    return "<a href='index.html#/personRegistry/personCard/" + row.personId + "'>" + value + "</a>";
                }
            }]
        );
}());