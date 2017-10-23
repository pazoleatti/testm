(function () {
    'use strict';

    /**
     * @description Модуль для работы окном 'Общие параметры'
     */

    angular.module('app.commonParams', ['ui.router', 'app.rest', 'app.logPanel', 'app.redactParams', 'app.confirmationAction'])

        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('uploadCommonParams', {
                url: '/taxes/commonParams/uploadCommonParams',
                templateUrl: 'client/app/taxes/commonParams/commonParams.html',
                controller: 'controllerCommonParams'
            });
        }])

        .controller('controllerCommonParams', ['$scope', 'CommonParams', '$filter', '$http', 'appModals', 'APP_CONSTANTS',
            function ($scope, CommonParams, $filter, $http, appModals, APP_CONSTANTS) {

                //Доступность кнопки редактировать
                $scope.enabledRedact = false;


                /**
                 * @description Создание и заполнение грида
                 */
                $scope.CommonParamsGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: CommonParams,
                        height: 280,
                        colNames: [
                            "",
                            $filter('translate')('title.comParams.param'),
                            $filter('translate')('title.comParams.paramValue')
                        ],
                        colModel: [
                            {name: 'departmentId', index: 'departmentId', hidden: true, key: true},
                            {name: 'code', index: 'code', width: 400, key: true},
                            {name: 'value', index: 'value', width: 1600, key: true}
                        ],

                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'departmentId',
                        viewrecords: true,
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: false
                    }
                };
                /**
                 * @description Отвечает за доступность недоступность кнопки 'сформировать'
                 */
                $scope.chekRow = function () {
                    if ($scope.CommonParamsGrid.value.length !== null) {
                        $scope.enabledRedact = true;
                    }
                };

                $scope.changeToDefault = function () {
                    appModals.create('client/app/taxes/commonParams/confirmationAction.html', 'controllerConfirmationAction', {commonParamsGrid: $scope.CommonParamsGrid}, {size: 'md'});
                };


                /**
                 * @description Редактирование параметра
                 */
                $scope.redact = function () {
                    $scope.valOiu = $scope.CommonParamsGrid.value;
                    $scope.CommonParamsGrid.ctrl.refreshGrid();
                    appModals.create('client/app/taxes/commonParams/redactCommonParams.html', 'controllerRedactParams', {commonParamsGrid: $scope.CommonParamsGrid}, {size: 'md'});
                };
                $scope.chekRow = function () {
                    if ($scope.CommonParamsGrid.value.length !== null) {
                        $scope.enabledRedact = true;
                    }
                };

            }]);

}());


