(function () {
    'use strict';

    /**
     * @description конфигурация модального окна выбора срока сдачи отчетности
     */

    angular.module('app.deadlinePeriodModal', ['ui.router', 'app.rest'])

        .controller('deadlinePeriodController', ['$scope', '$filter', '$shareData', '$http', '$modalInstance', '$dialogs', 'ValidationUtils',
            function ($scope, $filter, $shareData, $http, $modalInstance, $dialogs, ValidationUtils) {

                $scope.filter = {
                    departmentReportPeriod: $shareData.period,
                    department: undefined,
                    deadline: undefined,
                    withChild: false
                };

                $scope.save = function () {
                    if (ValidationUtils.checkDateValidateInterval($scope.deadline)) {
                        $dialogs.confirmDialog({
                            title: $filter('translate')('title.confirm'),
                            content: $filter('translate')('reportPeriod.confirm.text'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                createQuery(true);
                            },
                            cancelBtnClick: function () {
                                createQuery(false);
                            }

                        });
                    }else {
                        $dialogs.errorDialog({
                            content: $filter('translate')('common.validation.dateInterval')
                        });
                    }
                };
                $scope.close = function () {
                    $modalInstance.dismiss();
                };

                var createQuery = function (withChild) {
                    $http({
                        method: "POST",
                        url: "controller/actions/departmentReportPeriod/updateDeadline",
                        params: {
                            filter: JSON.stringify({
                                id: $scope.filter.departmentReportPeriod.id,
                                departmentId: $scope.filter.department.id,
                                utilDeadline: $scope.filter.deadline
                            }),
                            withChild: withChild
                        }
                    }).then(function () {
                        $modalInstance.close();
                    });
                };
            }])
    ;
}());