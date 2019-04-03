(function () {
    'use strict';

    /**
     * @description Модуль для выполнения groovy-скриптов
     */

    angular.module('app.scriptExecution', ['ui.router', 'ngFileUpload', 'app.rest', 'app.logPanel'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('scriptExecution', {
                url: '/administration/scriptExecution',
                templateUrl: 'client/app/administration/scriptExecution.html',
                controller: 'scriptExecutionCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_ADMINISTRATION_SETTINGS)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        .controller('scriptExecutionCtrl', ['$scope', '$http', 'Upload', '$logPanel',
            function ($scope, $http, Upload, $logPanel) {
                var editor = CodeMirror.fromTextArea(document.getElementById("code"), {
                    lineNumbers: true,
                    matchBrackets: true,
                    mode: "text/x-groovy",
                    autoRefresh:true
                });
                editor.setSize(null, 500);

                /**
                 * Извлекает скрипт из архива и возвращает на клиент\
                 */
                $scope.extractScript = function (file) {
                    if (file) {
                        Upload.upload({
                            url: 'controller/rest/extractScript',
                            data: {uploader: file}
                        }).progress(function (e) {
                        }).success(function (data) {
                            editor.setValue(data);
                            if (data && data.uuid) {
                                $logPanel.open('log-panel-container', data.uuid);
                            }
                        });
                    }
                };

                /**
                 * Запускает выполнение скрипта
                 */
                $scope.executeScript = function () {
                    $http({
                        method: "POST",
                        url: "controller/rest/executeScript",
                        data: JSON.stringify(editor.getValue())
                    }).then(function (response) {
                        if (response.data && response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };
            }
        ]);
}());