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
                controller: 'uploadTransportDataController'
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
                        }).then(function (response) {
                            if(response.data && response.data.uuid) {
                                $logPanel.open('log-panel-container', response.data.uuid);
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