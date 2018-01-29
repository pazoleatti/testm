(function () {
    'use strict';

    /**
     * @description Модуль назначения налоговых форм
     */
    angular.module('app.declarationTypeAssignment', ['ui.router', 'app.constants', 'app.rest', 'app.logPanel', 'app.formatters',
        'app.select.common', 'app.filterUtils', 'app.createAssignment'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('declarationTypeAssignment', {
                url: '/taxes/declarationTypeAssignment',
                templateUrl: 'client/app/taxes/ndfl/declarationTypeAssignment.html?v=${buildUuid}',
                controller: 'declarationTypeAssignmentCtrl'
            });
        }])

        /**
         * @description Контроллер назначения налоговых форм
         */
        .controller('declarationTypeAssignmentCtrl', [
            '$scope', '$state', '$stateParams', '$filter', 'APP_CONSTANTS', '$aplanaModal', '$dialogs', '$logPanel', 'DeclarationTypeAssignmentResource',
            function ($scope, $state, $stateParams, $filter, APP_CONSTANTS, $aplanaModal, $dialogs, $logPanel, DeclarationTypeAssignmentResource) {

                $scope.searchFilter = {
                    params: {},
                    ajaxFilter: [],
                    isClear: false,
                    filterName: 'declarationTypeAssignmentsFilter'
                };

                $scope.declarationTypeAssignmentGrid = {
                    ctrl: {},
                    options: {
                        datatype: "angularResource",
                        angularResource: DeclarationTypeAssignmentResource,
                        requestParameters: function () {
                            return {
                                filter: JSON.stringify({
                                    departmentIds: $filter('idExtractor')($scope.searchFilter.params.departments)
                                })
                            };
                        },
                        value: [],
                        colNames: [
                            $filter('translate')('declarationTypeAssignment.grid.columnName.department'),
                            $filter('translate')('declarationTypeAssignment.grid.columnName.declarationType'),
                            $filter('translate')('declarationTypeAssignment.grid.columnName.performer')],
                        colModel: [
                            {
                                name: 'department.fullName',
                                index: 'departmentFullName',
                                width: 500,
                                classes: 'grid-cell-white-space'
                            },
                            {name: 'name', index: 'declarationTypeName', width: 300, classes: 'grid-cell-white-space'},
                            {
                                name: 'performers',
                                index: 'performersFullNames',
                                width: 500,
                                classes: 'grid-cell-white-space',
                                formatter: $filter('performersFormatter')
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: true,
                        hidegrid: false,
                        multiselect: true
                    }
                };

                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    console.log($scope.searchFilter.params);
                    $scope.declarationTypeAssignmentGrid.ctrl.refreshGrid(page);
                };

                // Флаг отображения кнопки "Сбросить"
                $scope.searchFilter.isClearByFilterParams = function () {
                    var needToClear = false;
                    angular.forEach($scope.searchFilter.params, function (value, key) {
                        if (value != null) {
                            if (Array.isArray(value) || typeof(value) === "string" || value instanceof String) {
                                needToClear = needToClear || value.length > 0;
                            } else {
                                needToClear = true;
                            }
                        }
                    });
                    $scope.searchFilter.isClear = needToClear;
                };

                $scope.showCreateAssignmentModal = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('declarationTypeAssignment.modal.create.title'),
                        templateUrl: 'client/app/taxes/ndfl/createDeclarationTypeAssignment.html?v=${buildUuid}',
                        controller: 'createDeclarationTypeAssignmentCtrl',
                        windowClass: 'modal600'
                    }).result.then(
                        function (result) {
                            var response = result.response;
                            if(response && response.data) {
                                if(response.data.creatingExistingRelations) {
                                    $dialogs.warningDialog({
                                        content: $filter('translate')('declarationTypeAssignment.message.existingRelations')
                                    });
                                    if (response.data.uuid && response.data.uuid !== null) {
                                        $logPanel.open('log-panel-container', response.data.uuid);
                                    }
                                } else {
                                    $dialogs.messageDialog({
                                        content: $filter('translate')('declarationTypeAssignment.message.success')
                                    });
                                }
                            }
                            if(result.departments && result.departments.length > 0) {
                                $scope.searchFilter.params.departments = result.departments;
                                $scope.searchFilter.isClear = true;
                                $scope.refreshGrid();
                            }
                        }
                    );
                };
            }])

        /**
         * @description Форматтер для поля 'Исполнитель'
         * @param cellValue Значение ячейки
         * @param options Данные таблицы
         */
        .filter('performersFormatter', function () {
            return function (cellValue, options) {
                var performersFullNames = [];
                if (cellValue) {
                    performersFullNames = cellValue.map(function (performer) {
                        return performer.fullName;
                    });
                }
                return performersFullNames.join(", ");
            };
        });
}());