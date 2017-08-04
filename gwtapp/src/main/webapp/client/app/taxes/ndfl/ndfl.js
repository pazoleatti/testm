(function () {
    'use strict';

    /**
     * @description Модуль для для работы со страницей РНУ НДФЛ
     */

    angular.module('app.ndfl',
        ['ui.router',
            'app.createOrEditFLDialog',
            'app.ndflFL',
            'app.incomesAndTax',
            'app.deduction',
            'app.prepayment',
            'app.formSources',
            'app.logBusines',
            'app.logPanel',
            'app.dialogs',
            'app.filesComments'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndfl', {
                url: '/taxes/ndfl/{declarationId}',
                templateUrl: 'client/app/taxes/ndfl/ndfl.html',
                controller: 'ndflCtrl'
            });
        }])

        /**
         * @description Контроллер страницы РНУ НДФЛ и вкладки "Реквизиты"
         */
        .controller('ndflCtrl', [
            '$scope', '$timeout', '$window', '$stateParams', 'ShowToDoDialog', '$http', 'DeclarationDataResource', '$filter', '$logPanel', 'appDialogs', '$rootScope',
            function ($scope, $timeout, $window, $stateParams, $showToDoDialog, $http, DeclarationDataResource, $filter, $logPanel, appDialogs, $rootScope) {

                $scope.showToDoDialog = function () {
                    $showToDoDialog();
                };

                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');

                $scope.openHistoryOfChange = function () {
                    appDialogs.create('client/app/taxes/ndfl/logBusines.html', 'logBusinesFormCtrl', {declarationId: $scope.formNumber});
                };

                /**
                 * @description Обработка события, которое возникает при нажании на ссылку "Файлы и комментарии"
                 */
                $scope.filesAndComments = function () {
                    appDialogs.create('client/app/taxes/ndfl/filesComments.html', 'filesCommentsDialogCtrl', {declarationId: $scope.formNumber});
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Рассчитать"
                 */
                $scope.calculate = function (force, cancelTask) {
                    $http({
                        method: "POST",
                        url: "/controller/actions/declarationData/recalculate",
                        params: {
                            declarationDataId: $stateParams.declarationId,
                            force: force ? force : false,
                            cancelTask: cancelTask ? cancelTask : false
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        } else {
                            var dlg;
                            if (response.data.status === "LOCKED" && !force) {
                                dlg = appDialogs.confirm($filter('translate')('title.confirm'), response.data.restartMsg);
                                dlg.result.then(
                                    function () {
                                        $scope.calculate(true, cancelTask);
                                    });
                            } else if (response.data.status === "EXIST_TASK" && !cancelTask) {
                                dlg = appDialogs.confirm($filter('translate')('title.confirm'), $filter('translate')('title.returnExistTask'));
                                dlg.result.then(
                                    function () {
                                        $scope.calculate(force, true);
                                    });
                            }
                        }
                    })
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.accept = function (force, cancelTask) {
                    $http({
                        method: "POST",
                        url: "/controller/actions/declarationData/accept",
                        params: {
                            declarationDataId: $stateParams.declarationId,
                            force: force ? force : false,
                            cancelTask: cancelTask ? cancelTask : false
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                            initPage();
                        } else {
                            var dlg;
                            if (response.data.status === "LOCKED" && !force) {
                                dlg = appDialogs.confirm($filter('translate')('title.confirm'), response.data.restartMsg);
                                dlg.result.then(
                                    function () {
                                        $scope.accept(true, cancelTask);
                                    });
                            } else if (response.data.status === "EXIST_TASK" && !cancelTask) {
                                dlg = appDialogs.confirm($filter('translate')('title.confirm'), $filter('translate')('title.returnExistTask'));
                                dlg.result.then(
                                    function () {
                                        $scope.accept(force, true);
                                    });
                            } else if (response.data.status === "NOT_EXIST_XML") {
                                $window.alert($filter('translate')('title.acceptImpossible'));
                            }
                        }
                    })
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Проверить"
                 */
                $scope.check = function (force) {
                    $http({
                        method: "POST",
                        url: "/controller/actions/declarationData/check",
                        params: {
                            declarationDataId: $stateParams.declarationId,
                            force: force ? force : false
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        } else {
                            if (response.data.status === "LOCKED" && !force) {
                                var dlg = appDialogs.confirm($filter('translate')('title.confirm'), response.data.restartMsg);
                                dlg.result.then(
                                    function () {
                                        $scope.check(true);
                                    });
                            } else if (response.data.status === "NOT_EXIST_XML") {
                                $window.alert($filter('translate')('title.checkImpossible'));
                            }
                        }
                    })
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Вернуть в создана"
                 */
                $scope.returnToCreated = function () {
                    var dlg = appDialogs.confirm($filter('translate')('title.confirm'), $filter('translate')('title.returnToCreatedDeclaration'));
                    dlg.result.then(
                        function () {
                            $http({
                                method: "POST",
                                url: "/controller/actions/declarationData/returnToCreated",
                                params: {
                                    declarationDataId: $stateParams.declarationId
                                }
                            }).then(function () {
                                initPage();
                            })
                        });
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Удалить"
                 */
                $scope.delete = function () {
                    var dlg = appDialogs.confirm($filter('translate')('title.confirm'), $filter('translate')('title.deleteDeclaration'));
                    dlg.result.then(
                        function () {
                            $http({
                                method: "POST",
                                url: "/controller/actions/declarationData/delete",
                                params: {
                                    declarationDataId: $stateParams.declarationId
                                }
                            }).then(function () {
                                $window.location.assign('/index.html#/taxes/ndflJournal');
                            })
                        });
                };


                /**
                 * @description Обработка события, которое возникает при нажании на ссылку "Источники"
                 */
                $scope.showSourcesClick = function () {
                    appDialogs.create('client/app/taxes/ndfl/formSources.html', 'sourcesFormCtrl');
                };

                /**
                 * @description Инициализация первичных данных на странице
                 */
                function initPage() {
                    DeclarationDataResource.query({
                            declarationDataId: $stateParams.declarationId,
                            projection: "declarationData"
                        },
                        function (data) {
                            if (data) {
                                $scope.declarationData = data;
                                $scope.formNumber = $stateParams.declarationId;
                            }
                        }
                    );
                }

                initPage();
            }]);
}());