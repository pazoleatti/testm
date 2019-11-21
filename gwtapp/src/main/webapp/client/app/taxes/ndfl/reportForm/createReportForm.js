(function () {
    'use strict';

    /**
     * @description Модуль для создания отчетности
     */
    angular.module('app.createReportForm', ['app.constants', 'app.rest', 'app.formatters'])
        /**
         * @description Сервис для создания отчетности (используется в журналах "Отчетность" и "Приложение 2")
         */
        .factory("NdflReportService", ['$filter', '$logPanel', '$aplanaModal',
            function ($filter, $logPanel, $aplanaModal) {
                var service = {
                    createReport: function (isApp2) {
                        $aplanaModal.open({
                            title: $filter('translate')('title.creatingReport'),
                            templateUrl: 'client/app/taxes/ndfl/reportForm/createReportForm.html',
                            controller: 'createReportFormCtrl',
                            windowClass: 'modal600',
                            resolve: {
                                $shareData: function () {
                                    return {
                                        isApp2: !!isApp2
                                    };
                                }
                            }
                        }).result.then(function (response) {
                            if (response) {
                                if (response.data && response.data.uuid && response.data.uuid !== null) {
                                    $logPanel.open('log-panel-container', response.data.uuid);
                                }
                            }
                        });
                    }
                };
                return service;
            }
        ])

    /**
     * @description Контроллер окна "Создание отчетности"
     */
        .controller('createReportFormCtrl', [
            '$http', '$scope', '$rootScope', '$filter', '$dialogs', '$modalInstance', 'APP_CONSTANTS', '$shareData', '$webStorage',
            function ($http, $scope, $rootScope, $filter, $dialogs, $modalInstance, APP_CONSTANTS, $shareData, $webStorage) {

                $scope.knf = angular.copy($shareData.knf);
                var isApp2 = angular.copy($shareData.isApp2);
                $scope.reportData = {
                    negativeValuesAdjustment: APP_CONSTANTS.NEGATIVE_VALUE_ADJUSTMENT.NOT_CORRECT,
                    taxRefundReflectionMode: APP_CONSTANTS.TAX_REFUND_REFLECT_MODE.NORMAL
                };

                if ($scope.knf) {
                    $scope.reportData.department = {id: $scope.knf.departmentId, name: $scope.knf.department};
                    $scope.reportData.period = {id: $scope.knf.reportPeriodId, name: $scope.knf.reportPeriod};
                } else if (isApp2) {
                    $scope.reportData.isApp2 = isApp2;
                } else {
                    $scope.reportFormKind = [APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id];

                    $scope.$watch("reportData.department", function (department) {
                        if (!department) {
                            $scope.reportData.period = null;
                            $scope.reportData.declarationType = null;
                        }
                    });

                    $scope.$watch("reportData.period", function (period) {
                        if (!period) {
                            $scope.reportData.declarationType = null;
                        } else {
                            $scope.reportData.declarationType = null;
                            if (period.text.indexOf(APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL.name) > -1 ){
                                $scope.reportData.declarationType = new Object(APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL);
                            }
                            if (period.text.indexOf(APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1.name) > -1 ){
                                $scope.reportData.declarationType = new Object(APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1);
                            }
                            if (period.text.indexOf(APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2.name) > -1 ){
                                $scope.reportData.declarationType = new Object(APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2);
                            }
                            if (period.text.indexOf(APP_CONSTANTS.DECLARATION_TYPE.APP_2.name) > -1 ){
                                $scope.reportData.declarationType = new Object(APP_CONSTANTS.DECLARATION_TYPE.APP_2);
                            }
                        }
                    });
                }

                /**
                 * Создание отчётности
                 */
                $scope.save = function () {
                    // Запоминаем период выбранный пользователем
                    $webStorage.set(APP_CONSTANTS.USER_STORAGE.NAME,
                        APP_CONSTANTS.USER_STORAGE.KEYS.LAST_SELECTED_PERIOD,
                        $scope.reportData.period,
                        true);
                    var params = {
                        knfId: $scope.knf ? $scope.knf.id : undefined,
                        declarationTypeId: $scope.reportData.declarationType.id,
                        departmentId: $scope.reportData.department.id,
                        periodId: $scope.reportData.period.id
                    };
                    if ($scope.reportData.kppOktmoPairs) {
                        params.kppOktmoPairs = $scope.reportData.kppOktmoPairs.map(function (kppOktmoPair) {
                            return {kpp: kppOktmoPair.kpp, oktmo: kppOktmoPair.oktmo};
                        });
                    }
                    if ($scope.reportData.declarationType.id === APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL.id) {
                        params.adjustNegativeValues = $scope.reportData.negativeValuesAdjustment === APP_CONSTANTS.NEGATIVE_VALUE_ADJUSTMENT.CORRECT;
                        params.taxRefundReflectionMode = $scope.reportData.taxRefundReflectionMode.enumName;
                        params.reportFormCreationMode = $scope.reportData.reportFormCreationMode.enumName;
                    }
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/createReportForm",
                        data: params
                    }).then(function (response) {
                        $modalInstance.close(response);
                    }).catch(function () {
                        $modalInstance.close();
                    });
                };
                /**
                 * Закрытие окна
                 */
                $scope.cancel = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('createDeclaration.cancel.header'),
                        content: $filter('translate')('createDeclaration.cancel.text'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $modalInstance.dismiss();
                        }
                    });
                };
            }]
        );
}());