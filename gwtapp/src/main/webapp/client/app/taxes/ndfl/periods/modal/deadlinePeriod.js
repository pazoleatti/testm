(function () {
    'use strict';

    /**
     * @description конфигурация модального окна выбора срока сдачи отчетности
     */

    angular.module('app.deadlinePeriodModal', ['ui.router', 'app.rest', 'app.modals'])

        .controller('deadlinePeriodController', ['$scope', '$filter', '$shareData', '$http', '$modalInstance', '$dialogs',
            function ($scope, $filter, $shareData, $http, $modalInstance, $dialogs) {

                $scope.filter = {
                    departmentReportPeriod: $shareData.period,
                    department: null,
                    deadline: null,
                    withChild: false
                };

                $scope.save = function () {
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