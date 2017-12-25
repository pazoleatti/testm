(function () {
    'use strict';

    /**
     * @description модуль модального окна выбора срока сдачи отчетности
     */

    angular.module('app.deadlinePeriodModal', ['ui.router', 'app.rest'])

    /**
     * @description Контроллер модального откна "Назначить срок сдачи отчетности"
     */
        .controller('deadlinePeriodController', ['$scope', '$filter', '$shareData', '$http', '$modalInstance', '$dialogs', 'ValidationUtils', 'AppointDeadlineResource',
            function ($scope, $filter, $shareData, $http, $modalInstance, $dialogs, ValidationUtils, AppointDeadlineResource) {

                /** в $shareData.period.deadline используется дата в фомате ISO,
                 * а для date-picker нужна дата в формате UTC
                 * для перобразования добавляется 'Z'
                 */
                var deadline = new Date($shareData.period.deadline + "Z");

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
                        $dialogs.confirmDialog({
                            title: $filter('translate')('title.confirm'),
                            content: $filter('translate')('reportPeriod.confirm.text'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                AppointDeadlineResource.doOperation({
                                    id: $scope.filter.departmentReportPeriod.id,
                                    departmentId: $scope.filter.department.id,
                                    utilDeadline: $scope.filter.deadline,
                                    withChild: true
                                }, function () {
                                    $modalInstance.close();
                                });
                            },
                            cancelBtnClick: function () {
                                AppointDeadlineResource.doOperation({
                                    id: $scope.filter.departmentReportPeriod.id,
                                    departmentId: $scope.filter.department.id,
                                    utilDeadline: $scope.filter.deadline,
                                    withChild: false
                                }, function () {
                                    $modalInstance.close();
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
                 * @description Закрыть модальное окно
                 */
                $scope.close = function () {
                    $modalInstance.dismiss();
                };
            }])


    ;
}());