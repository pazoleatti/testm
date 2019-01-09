(function () {
    'use strict';

    /**
     * @description Модуль для редактирования назначений налоговых форм
     */

    angular.module('app.editAssignment', ['ui.router', 'app.rest', 'app.formatters', 'app.select.common'])

    /**
     * @description Контроллер МО Редактирования назначения
     */
        .controller('editDeclarationTypeAssignmentCtrl', ['$scope', '$state', '$stateParams', '$filter', 'APP_CONSTANTS', '$aplanaModal',
            '$modalInstance', '$http', '$dialogs', '$shareData',
            function ($scope, $state, $stateParams, $filter, APP_CONSTANTS, $aplanaModal, $modalInstance, $http, $dialogs, $shareData) {

                /**
                 * Данные о редактируемых назначениях
                 */
                $scope.editedAssignmentsData = {
                    performers: []
                };

                /**
                 * Список наименований подразделений
                 */
                $scope.departmentNames = $filter('joinObjectsPropFormatter')($shareData.selectedAssignments, ', ', 'department.fullName', true) || '';

                /**
                 * Список видов форм
                 */
                $scope.declarationTypes = $filter('joinObjectsPropFormatter')($shareData.selectedAssignments, ', ', 'name', true) || '';

                /**
                 * Список подразделений
                 */
                $scope.departments = [];

                /**
                 * Заполнение списков подразделений и исполнителей. Необходимо, чтобы значения в них не дублировались
                 */
                function fillDepartmentsAndPerformersLists(departments, performers) {
                    var departmentIds = [];
                    var performerIds = [];
                    angular.forEach($shareData.selectedAssignments, function (assignment) {
                        if (assignment.department && assignment.department.id !== undefined && departmentIds.indexOf(assignment.department.id) === -1) {
                            departments.push(assignment.department);
                            departmentIds.push(assignment.department.id);
                        }
                        if (assignment.performers) {
                            angular.forEach(assignment.performers, function (performer) {
                                if (performer && performer.id !== undefined && performerIds.indexOf(performer.id) === -1) {
                                    performers.push(performer);
                                    performerIds.push(performer.id);
                                }
                            });
                        }
                    });
                }

                fillDepartmentsAndPerformersLists($scope.departments, $scope.editedAssignmentsData.performers);

                /**
                 * Сохранение назначения
                 */
                $scope.save = function () {
                    var params = {
                        assignmentIds: $filter('idExtractor')($shareData.selectedAssignments),
                        performerIds: $filter('idExtractor')($scope.editedAssignmentsData.performers)
                    };
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationTypeAssignment/edit",
                        params: params
                    }).then(function (response) {
                        $modalInstance.close({departments: $scope.departments, response: response});
                    });
                };

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('declarationTypeAssignment.modal.cancel.header'),
                        content: $filter('translate')('declarationTypeAssignment.modal.edit.cancel.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.dismiss('Canceled');
                        }
                    });
                };


            }]);
}());