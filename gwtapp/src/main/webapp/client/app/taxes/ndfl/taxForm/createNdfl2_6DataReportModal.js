(function () {
    'use strict';

    /**
     * @description Модуль для создания спецотчета "Данные для включения в разделы 2-НДФЛ и 6-НДФЛ"
     */

    angular.module('app.createNdfl2_6DataReport', [])
        .controller('createNdfl2_6DataReportCtrl', ['$scope', '$http', '$modalInstance', '$filter', 'APP_CONSTANTS', '$shareData', '$logPanel',
            function ($scope, $http, $modalInstance, $filter, APP_CONSTANTS, $shareData, $logPanel) {

                $scope.type = $shareData.type;
                $scope.declarationData = $shareData.declarationData;
                $scope.form = {
                    dateFrom: $scope.declarationData.calendarStartDate,
                    dateTo: $scope.declarationData.endDate,
                    negativeValuesAdjustment: APP_CONSTANTS.NEGATIVE_VALUE_ADJUSTMENT.NOT_CORRECT
                };
                $scope.kppSelectFilter = {declarationDataId: $scope.declarationData.id};

                $scope.$watchGroup(["form.dateFrom", "form.dateTo"], function () {
                    if ($scope.isDatesValid($scope.form.dateFrom, $scope.form.dateTo)) {
                        $scope.createNdfl2_6DataReportFrom["form_datefrom"].$setValidity('versionDate', true);
                    }
                });

                /**
                 * Создание отчета
                 */
                $scope.create = function () {
                    if (!$scope.isDatesValid($scope.form.dateFrom, $scope.form.dateTo)) {
                        $scope.createNdfl2_6DataReportFrom["form_datefrom"].$setValidity('versionDate', false);
                    } else {
                        $scope.createNdfl2_6DataReportFrom["form_datefrom"].$setValidity('versionDate', true);
                        $http({
                            method: "POST",
                            url: "controller/actions/declarationData/" + $scope.declarationData.id + "/specific/" +
                            ($scope.type === 'xlsx' ? APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_2_6_DATA_XLSX_REPORT : APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_2_6_DATA_TXT_REPORT),
                            data: {
                                dateFrom: $scope.form.dateFrom,
                                dateTo: $scope.form.dateTo,
                                adjustNegativeValues: $scope.form.negativeValuesAdjustment === APP_CONSTANTS.NEGATIVE_VALUE_ADJUSTMENT.CORRECT,
                                kppList: $scope.form.kppSelectList ? $scope.form.kppSelectList.map(function (a) {
                                    return a.kpp;
                                }) : undefined
                            }
                        }).success(function (response) {
                            if (response) {
                                $logPanel.open('log-panel-container', response);
                            }
                        });
                    }
                };

                /**
                 * Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.dismiss();
                };

                function toDate(value) {
                    return typeof value === 'string' ? new Date(value) : value;
                }
                $scope.isDatesValid = function (dateFrom, dateTo) {
                    return !dateFrom || !dateTo || toDate(dateFrom) < toDate(dateTo);
                };
            }]);
}());