(function () {
    'use strict';

    /**
     * @description Модуль для создания назначений налоговых форм
     */

    angular.module('app.createAssignment', ['ui.router', 'app.rest', 'app.formatters', 'app.select.common'])

    /**
     * @description Контроллер МО Создания назначения
     */
        .controller('createDeclarationTypeAssignmentCtrl', ['$scope', '$state', '$stateParams', '$filter', 'APP_CONSTANTS', '$aplanaModal',
            '$modalInstance', '$http', '$dialogs',
            function ($scope, $state, $stateParams, $filter, APP_CONSTANTS, $aplanaModal, $modalInstance, $http, $dialogs) {

                /**
                 * Данные о создаваемом назначении
                 */
                $scope.assignmentData = {};

                /**
                 * Сохранение назначения
                 */
                $scope.save = function () {
                    var params = {
                        departmentIds: $filter('idExtractor')($scope.assignmentData.departments),
                        declarationTypeIds: $filter('idExtractor')($scope.assignmentData.declarationTypes),
                        performerIds: $filter('idExtractor')($scope.assignmentData.performers)
                    };
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationTypeAssignment/create",
                        params: params
                    }).then(function (response) {
                        $modalInstance.close({departments: $scope.assignmentData.departments, response: response});
                    });
                };

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    if ($scope.assignmentData.departments && $scope.assignmentData.departments.length > 0 || $scope.assignmentData.declarationTypes && $scope.assignmentData.declarationTypes.length > 0 ||
                        $scope.assignmentData.performers && $scope.assignmentData.performers.length > 0) {
                        $dialogs.confirmDialog({
                            title: $filter('translate')('declarationTypeAssignment.modal.cancel.header'),
                            content: $filter('translate')('declarationTypeAssignment.modal.create.cancel.text'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                $modalInstance.dismiss('Canceled');
                            }
                        });
                    } else {
                        $modalInstance.dismiss('Canceled');
                    }
                };
            }]);
}());