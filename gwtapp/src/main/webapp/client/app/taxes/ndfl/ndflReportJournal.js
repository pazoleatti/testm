(function () {
    'use strict';

    /**
     * @description Модуль для работы с формой "Отчетность"
     */
    angular.module('app.ndflReportJournal', ['ui.router', 'app.createReport'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflReportJournal', {
                url: '/taxes/ndflReportJournal',
                templateUrl: 'client/app/taxes/ndfl/ndflReportJournal.html',
                controller: 'ndflReportJournalCtrl'
            });
        }])

        .controller('ndflReportJournalCtrl', ['$scope', 'ShowToDoDialog', '$filter', 'DeclarationDataResource', '$http', '$logPanel', 'appModals',
            function ($scope, $showToDoDialog, $filter, DeclarationDataResource, $http, $logPanel, appModals) {
                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.ndflReportJournalGrid.ctrl.refreshGrid(page);
                };

                $scope.createReport = function () {
                    appModals.create('client/app/taxes/ndfl/createReport.html', 'createReportCtrl', null, {size: 'md'});
                };
                $scope.downloadReport = function () {
                    $showToDoDialog();
                };
                $scope.check = function () {
                    var ids = [];
                    _.each($scope.ndflReportJournalGrid.value, function (element) {
                        ids.push(element.declarationDataId);
                    });
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/checkDeclarationDataList",
                        params: {
                            declarationDataIds: ids
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };
                $scope.delete = function () {
                    var ids = [];
                    _.each($scope.ndflReportJournalGrid.value, function (element) {
                        ids.push(element.declarationDataId);
                    });
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/deleteDeclarationDataList",
                        params: {
                            declarationDataIds: ids
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        $scope.refreshGrid(1);
                    });
                };
                $scope.accept = function () {
                    var ids = [];
                    _.each($scope.ndflReportJournalGrid.value, function (element) {
                        ids.push(element.declarationDataId);
                    });
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/acceptDeclarationDataList",
                        params: {
                            declarationDataIds: ids
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        $scope.refreshGrid(1);
                    });
                };
                $scope.returnToCreated = function () {
                    appModals.inputMessage($filter('translate')('title.indicateReasonForReturn'), $filter('translate')('title.reasonForReturn'))
                        .result.then(function (text) {
                        var ids = [];
                        _.each($scope.ndflReportJournalGrid.value, function (element) {
                            ids.push(element.declarationDataId);
                        });
                        $http({
                            method: "POST",
                            url: "controller/actions/declarationData/returnToCreatedDeclarationDataList",
                            params: {
                                declarationDataIds: ids,
                                reasonForReturn: text
                            }
                        }).then(function (response) {
                            if (response.data && response.data.uuid && response.data.uuid !== null) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            }
                            $scope.refreshGrid(1);
                        });
                    });
                };

                $scope.ndflReportJournalGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: DeclarationDataResource,
                        requestParameters: function () {
                            return {
                                projection: 'declarations',
                                isReport: true
                            };
                        },
                        height: 250,
                        colNames: [
                            $filter('translate')('title.numberDeclaration'),
                            $filter('translate')('title.formKind'),
                            $filter('translate')('title.department'),
                            $filter('translate')('title.period'),
                            $filter('translate')('title.state'),
                            $filter('translate')('title.kpp'),
                            $filter('translate')('title.oktmo'),
                            $filter('translate')('title.codeNO'),
                            $filter('translate')('title.docState'),
                            $filter('translate')('title.dateAndTimeCreate'),
                            $filter('translate')('title.creator'),
                            $filter('translate')('title.xmlFile'),
                            $filter('translate')('title.note')],
                        colModel: [
                            {name: 'declarationDataId', index: 'declarationDataId', width: 110, key: true},
                            {name: 'declarationType', index: 'declarationType', width: 170, formatter: linkformatter},
                            {name: 'department', index: 'department', width: 200},
                            {name: 'reportPeriod', index: 'reportPeriod', width: 175},
                            {name: 'state', index: 'state', width: 175},
                            {name: 'kpp', index: 'kpp', width: 85},
                            {name: 'oktmo', index: 'oktmo', width: 80},
                            {name: 'taxOrganCode', index: 'taxOrganCode', width: 70},
                            {name: 'docState', index: 'docState', width: 130},
                            {
                                name: 'creationDate',
                                index: 'creationDate',
                                width: 205,
                                formatter: $filter('dateTimeFormatter')
                            },
                            {name: 'creationUserName', index: 'creationUserName', width: 130},
                            {name: 'fileName', index: 'fileName', width: 200, formatter: linkFileFormatter},
                            {name: 'note', index: 'note', width: 200}
                        ],
                        rowNum: 20,
                        rowList: [10, 20, 30, 50, 100],
                        sortname: 'declarationDataId',
                        viewrecords: true,
                        sortorder: "desc",
                        hidegrid: false,
                        multiselect: true
                    }
                };

                /**
                 * @description форматтер для поля 'Вид налоговой формы' для перехода на конкретную НФ
                 * @param cellValue значение ячейки
                 * @param options данные таблицы
                 */
                function linkformatter(cellValue, options) {
                    return "<a href='index.html#/taxes/ndfl/" + options.rowId + "'>" + cellValue + "</a>";
                }

                /**
                 * @description форматтер для поля 'Файл ТФ' для получения файла ТФ
                 * @param cellValue значение ячейки
                 * @param options данные таблицы
                 */
                function linkFileFormatter(cellValue, options) {
                    if (!cellValue) {
                        cellValue = '';
                    }
                    return "<a target='_blank' href='controller/rest/declarationData/" + options.rowId + "/xml'>" + cellValue + "</a>";
                }
            }])
}());