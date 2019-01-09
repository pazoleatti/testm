(function () {
    'use strict';

    /**
     * @description Модуль назначения налоговых форм
     */
    angular.module('app.declarationTypeAssignment', ['ui.router', 'app.constants', 'app.rest', 'app.logPanel', 'app.formatters',
        'app.select.common', 'app.filterUtils', 'app.createAssignment', 'app.editAssignment'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('declarationTypeAssignment', {
                url: '/taxes/declarationTypeAssignment',
                templateUrl: 'client/app/taxes/ndfl/assignments/declarationTypeAssignment.html',
                controller: 'declarationTypeAssignmentCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL_SETTINGS)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        /**
         * @description Контроллер назначения налоговых форм
         */
        .controller('declarationTypeAssignmentCtrl', [
            '$scope', '$http', '$state', '$stateParams', '$filter', 'APP_CONSTANTS', '$aplanaModal', '$dialogs', '$logPanel', 'DeclarationTypeAssignmentResource',
            function ($scope, $http, $state, $stateParams, $filter, APP_CONSTANTS, $aplanaModal, $dialogs, $logPanel, DeclarationTypeAssignmentResource) {

                /**
                 * Фильтр
                 */
                $scope.searchFilter = {
                    params: {},
                    ajaxFilter: [],
                    isClear: false,
                    filterName: 'declarationTypeAssignmentsFilter'
                };

                /**
                 * Грид с назначениями
                 */
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

                $scope.assignmentsAreSelected = function () {
                    return $scope.declarationTypeAssignmentGrid && $scope.declarationTypeAssignmentGrid.value && $scope.declarationTypeAssignmentGrid.value.length > 0;
                };

                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
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

                /**
                 * Открыть МО создания назначения
                 */
                $scope.showCreateAssignmentModal = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('declarationTypeAssignment.modal.create.title'),
                        templateUrl: 'client/app/taxes/ndfl/assignments/createDeclarationTypeAssignment.html',
                        controller: 'createDeclarationTypeAssignmentCtrl',
                        windowClass: 'modal600'
                    }).result.then(
                        function (result) {
                            var response = result.response;
                            if (response && response.data) {
                                if (response.data.creatingExistingRelations) {
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
                            if (result.departments && result.departments.length > 0) {
                                $scope.searchFilter.params.departments = result.departments;
                                $scope.searchFilter.isClear = true;
                                $scope.refreshGrid();
                            }
                        }
                    );
                };

                /**
                 * Открыть МО редактирования назначения
                 */
                $scope.showEditAssignmentModal = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('declarationTypeAssignment.modal.edit.title'),
                        templateUrl: 'client/app/taxes/ndfl/assignments/editDeclarationTypeAssignment.html',
                        controller: 'editDeclarationTypeAssignmentCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    selectedAssignments: $scope.declarationTypeAssignmentGrid.value
                                };
                            }
                        }
                    }).result.then(
                        function (result) {
                            if (result.departments && result.departments.length > 0) {
                                $scope.searchFilter.params.departments = result.departments;
                                $scope.searchFilter.isClear = true;
                                $scope.refreshGrid();
                            }
                        }
                    );
                };

                function createAssignmentIdModels(assignments) {
                    var result = assignments.map(function (assignment) {
                        var idModel = {};
                        if (assignment && assignment.department) {
                            idModel.id = assignment.id;
                            idModel.declarationTypeId = assignment.formTypeId;
                            idModel.departmentId = assignment.department.id;
                        }
                        return idModel;
                    });
                    return result;
                }

                /**
                 * Отмена назначений налоговых форм подразделениям
                 */
                $scope.deleteAssignments = function () {
                    var params = {
                        assignments: createAssignmentIdModels($scope.declarationTypeAssignmentGrid.value)
                    };
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationTypeAssignment/delete",
                        data: createAssignmentIdModels($scope.declarationTypeAssignmentGrid.value)
                    }).then(function (response) {
                        if (response.data && response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        $scope.refreshGrid();
                    });
                };
            }])

        /**
         * @description Форматтер для поля 'Исполнитель'
         * @param cellValue Значение ячейки
         * @param options Данные таблицы
         */
        .filter('performersFormatter', ['$filter', function ($filter) {
            return function (cellValue, options) {
                return $filter('joinObjectsPropFormatter')(cellValue, ', ', 'fullName') || '';
            };
        }]);
}());