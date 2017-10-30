(function () {
    'use strict';

    /**
     * @description Модуль для работы окном 'Общие параметры'
     */

    angular.module('app.commonParams', ['ui.router', 'app.rest', 'app.logPanel', 'app.uploadParams', 'app.confirmationAction'])

        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('commonParams', {
                url: '/taxes/commonParams/uploadCommonParams',
                templateUrl: 'client/app/taxes/commonParams/commonParams.html',
                controller: 'commonParamsCtrl'
            });
        }])

        /**
         * @description Контроллер для общих параметров
         */
        .controller('commonParamsCtrl', ['$scope', 'CommonParams', '$filter', '$http', 'appModals', 'APP_CONSTANTS',
            function ($scope, CommonParams, $filter, $http, appModals, APP_CONSTANTS) {

                /**
                 * @description Создание и заполнение грида
                 */
                $scope.commonParamsGrid = {
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
                 * @description Изменение общих параметров на значения по умолчанию
                 */
                $scope.changeToDefault = function () {
                    appModals.create('client/app/taxes/commonParams/confirmationAction.html', 'confirmationActionCtrl', {commonParamsGrid: $scope.commonParamsGrid}, {size: 'md'});
                };


                /**
                 * @description Редактирование параметра
                 */
                $scope.upload = function () {
                    $scope.valOiu = $scope.commonParamsGrid.value;
                    $scope.commonParamsGrid.ctrl.refreshGrid();
                    appModals.create('client/app/taxes/commonParams/uploadCommonParams.html', 'uploadParamsCtrl', {commonParamsGrid: $scope.commonParamsGrid}, {size: 'md'});
                };

            }]);
}());

