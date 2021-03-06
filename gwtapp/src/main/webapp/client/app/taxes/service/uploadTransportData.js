(function () {
    'use strict';

    /**
     * @description Модуль для загрузки ТФ
     */

    angular.module('app.uploadTransportData', ['ui.router', 'ngFileUpload', 'app.rest', 'app.logPanel'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('uploadTransportData', {
                url: '/taxes/service/uploadTransportData',
                templateUrl: 'client/app/taxes/service/uploadTransportData.html',
                controller: 'uploadTransportDataController',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_SERVICE)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        /**
         * @description Контроллер страницы РНУ НДФЛ и вкладки "Реквизиты"
         */
        .controller('uploadTransportDataController', ['$scope', '$http', 'Upload', '$logPanel',
            function ($scope, $http, Upload, $logPanel) {
                $scope.uploadButtonClick = function (file) {
                    if (file) {
                        Upload.upload({
                            url: 'controller/actions/transportData/upload',
                            data: {uploader: file}
                        }).progress(function (e) {
                        }).success(function (data) {
                            if(data && data.uuid) {
                                $logPanel.open('log-panel-container', data.uuid);
                            }
                        });
                    }
                };

                $scope.loadAllButtonClick = function () {
                    $http.post('controller/actions/transportData/loadAll')
                        .then(function (response) {
                            if(response.data && response.data.uuid) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            }
                        });
                };
            }
        ]);
}());