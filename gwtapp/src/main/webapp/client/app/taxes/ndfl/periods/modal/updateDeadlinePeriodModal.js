(function () {
    'use strict';

    /**
     * @description модуль модального окна выбора срока сдачи отчетности
     */

    angular.module('app.deadlinePeriodModal', ['ui.router', 'app.rest'])

    /**
     * @description Контроллер модального откна "Назначить срок сдачи отчетности"
     */
        .controller('deadlinePeriodController', ['$scope', '$filter', '$shareData', '$http', '$modalInstance', '$dialogs', 'ValidationUtils', '$q',
            function ($scope, $filter, $shareData, $http, $modalInstance, $dialogs, ValidationUtils, $q) {

                /** в $shareData.period.deadline используется дата в фомате ISO,
                 * а для date-picker нужна дата в формате UTC
                 * для перобразования добавляется 'Z'
                 */
                var deadline = null;
                if ($shareData.period.deadline) {
                    deadline = new Date($shareData.period.deadline + "Z");
                }

                $scope.filter = {
                    departmentReportPeriod: $shareData.period,
                    department: undefined,
                    deadline: deadline,
                    withChild: false
                };

                /**
                 * @description Сохранение даты сдачи отчетности
                 */
                $scope.save = function () {
                    if (ValidationUtils.checkDateValidateInterval($scope.filter.deadline)) {
                        checkHasChildDepartment($scope.filter.department.id).then(function (hasChild) {
                            if (!hasChild) {
                                $http({
                                    method: "POST",
                                    url: "controller/actions/departmentReportPeriod/updateDeadline",
                                    params: {
                                        filter: JSON.stringify({
                                            id: $scope.filter.departmentReportPeriod.id,
                                            departmentId: $scope.filter.department.id,
                                            deadline: $scope.filter.deadline,
                                            withChild: false
                                        })
                                    }
                                }).then(function () {
                                        $modalInstance.close();
                                });
                            } else {
                                $dialogs.confirmDialog({
                                    title: $filter('translate')('title.confirm'),
                                    content: $filter('translate')('reportPeriod.confirm.text'),
                                    okBtnCaption: $filter('translate')('common.button.yes'),
                                    cancelBtnCaption: $filter('translate')('common.button.no'),
                                    okBtnClick: function () {
                                        $http({
                                            method: "POST",
                                            url: "controller/actions/departmentReportPeriod/updateDeadline",
                                            params: {
                                                filter: JSON.stringify({
                                                    id: $scope.filter.departmentReportPeriod.id,
                                                    departmentId: $scope.filter.department.id,
                                                    deadline: $scope.filter.deadline,
                                                    withChild: true
                                                })
                                            }
                                        }).then(function () {
                                                $modalInstance.close();
                                        });
                                    },
                                    cancelBtnClick: function () {
                                        $http({
                                            method: "POST",
                                            url: "controller/actions/departmentReportPeriod/updateDeadline",
                                            params: {
                                                filter: JSON.stringify({
                                                    id: $scope.filter.departmentReportPeriod.id,
                                                    departmentId: $scope.filter.department.id,
                                                    deadline: $scope.filter.deadline,
                                                    withChild: false
                                                })
                                            }
                                        }).then(function () {
                                                $modalInstance.close();
                                        });
                                    }

                                });
                            }
                        });
                    } else {
                        $dialogs.errorDialog({
                            content: $filter('translate')('common.validation.dateInterval')
                        });
                    }
                };

                /**
                 * @description Закрыть модальное окно по нажатию на "Закрыть"
                 */
                $scope.close = function () {
                    if ($scope.isEdit) {
                        $dialogs.confirmDialog({
                            title: $filter('translate')('reportPeriod.confirm.deadline.title'),
                            content: $filter('translate')('reportPeriod.confirm.deadline.text'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                $modalInstance.dismiss();
                            }
                        });
                    } else {
                        $modalInstance.dismiss();
                    }
                };

                /**
                 * @description Закрыть модальное окно по нажатию на "Отмена"
                 */
                $scope.cancel = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('reportPeriod.confirm.deadline.title'),
                        content: $filter('translate')('reportPeriod.confirm.deadline.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.dismiss();
                        }
                    });
                };

                /**
                 * @description проверяет наличие дочерних подразделений для выбранного
                 */
                var checkHasChildDepartment = function (id) {
                    var checkHasChildDepartmentDefer = $q.defer();
                    $http({
                        method: "GET",
                        url: "controller/rest/department/" + id + "?projection=checkHasChildDepartment"
                    }).success(function (response) {
                        checkHasChildDepartmentDefer.resolve(response);
                    });
                    return checkHasChildDepartmentDefer.promise;
                };

                $scope.$watch('filter.deadline', function () {
                    $scope.isEdit = ((!deadline && $scope.filter.deadline) || (new Date($scope.filter.deadline.toString()).toString() !== deadline.toString()));
                });
            }])


    ;
}());