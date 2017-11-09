(function () {
    'use strict';

    /**
     * @description конфигурация модального окна выбора срока сдачи отчетности
     */

    angular.module('app.deadlinePeriodModal', ['ui.router', 'app.rest', 'app.modals'])

        .controller('deadlinePeriodController', ['$scope', '$filter', 'data', '$http', '$uibModalInstance', 'appModals',
            function ($scope, $filter, data, $http, $uibModalInstance, appModals) {

                $scope.filter = {
                    departmentReportPeriod: data.period,
                    department: null,
                    deadline: null,
                    withChild: false
                };

                $scope.save = function () {
                    var modal = appModals.create('client/lib/templates/confirmDialog.html', 'deadlineConfirm', {}, {size: "md"});
                    modal.result.then(function (response) {
                        $scope.filter.withChild = response;
                        $http({
                            method: "POST",
                            url: "controller/rest/reportPeriods/deadline",
                            params: {
                                filter: JSON.stringify({
                                    id: $scope.filter.departmentReportPeriod.id,
                                    departmentId: $scope.filter.department.id,
                                    simpleCorrectionDate: $scope.filter.deadline
                                }),
                                withChild: $scope.filter.withChild
                            }
                        }).then(function () {
                            $uibModalInstance.close();
                        });
                    });

                };
                $scope.close = function () {
                    $uibModalInstance.dismiss();
                };
            }])

        .controller('deadlineConfirm', ['$scope', '$filter', '$uibModalInstance', '$translate',
            function ($scope, $filter, $uibModalInstance, $translate) {
                $scope.msg = $filter('translate')('reportPeriod.confirm.text');
                $scope.header = $filter('translate')('title.confirm');
                $scope.labelYes = $translate.instant('DIALOGS_YES');
                $scope.labelNo = $translate.instant('DIALOGS_NO');
                $scope.yes = function () {
                    $uibModalInstance.close(true);
                };
                $scope.no = function () {
                    $uibModalInstance.close(false);
                };
            }])
    ;
}());