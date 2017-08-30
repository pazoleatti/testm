(function () {
    'use strict';

    /**
     * @description Модуль для для работы со страницей РНУ НДФЛ
     */

    angular.module('app.ndfl',
        ['ui.router',
            'app.ndflFL',
            'app.incomesAndTax',
            'app.deduction',
            'app.prepayment',
            'app.formSources',
            'app.logBusines',
            'app.logPanel',
            'app.modals',
            'app.filesComments',
            'app.rest'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndfl', {
                url: '/taxes/ndfl/{declarationDataId}',
                templateUrl: 'client/app/taxes/ndfl/ndfl.html',
                controller: 'ndflCtrl'
            });
        }])

        /**
         * @description Контроллер страницы РНУ НДФЛ и вкладки "Реквизиты"
         */
        .controller('ndflCtrl', [
            '$scope', '$timeout', '$window', '$stateParams', 'ShowToDoDialog', '$http', 'DeclarationDataResource', '$filter', '$logPanel', 'appModals', '$rootScope',
            'RefBookValuesResource', 'APP_CONSTANTS',
            function ($scope, $timeout, $window, $stateParams, $showToDoDialog, $http, DeclarationDataResource, $filter, $logPanel, appModals, $rootScope,
                      RefBookValuesResource, APP_CONSTANTS) {

                /**
                 * @description Инициализация первичных данных на странице
                 */
                function initPage() {
                    DeclarationDataResource.query({
                            declarationDataId: $stateParams.declarationDataId,
                            projection: "declarationData"
                        },
                        function (data) {
                            if (data) {
                                $scope.declarationData = data;
                                $scope.declarationDataId = $stateParams.declarationDataId;
                            }
                        }
                    );
                }

                $scope.showToDoDialog = function () {
                    $showToDoDialog();
                };

                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');

                $scope.openHistoryOfChange = function () {
                    appModals.create('client/app/taxes/ndfl/logBusines.html', 'logBusinesFormCtrl', {declarationDataId: $scope.declarationDataId});
                };

                /**
                 * @description Открытие модального окна "Файлы и комментарии"
                 */
                $scope.filesAndComments = function () {
                    RefBookValuesResource.query({
                        refBookId: APP_CONSTANTS.REFBOOK.ATTACH_FILE_TYPE
                    }, function (data) {
                        var attachFileTypes = {};
                        angular.forEach(data, function (fileType) {
                            attachFileTypes[fileType.id] = fileType.name;
                        });
                        appModals.create('client/app/taxes/ndfl/filesComments.html', 'filesCommentsCtrl',
                            {declarationDataId: $scope.declarationDataId, attachFileTypes: attachFileTypes}, {copy: true});
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Рассчитать"
                 */
                $scope.calculate = function (force, cancelTask) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/recalculate",
                        params: {
                            force: force ? force : false,
                            cancelTask: cancelTask ? cancelTask : false
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        } else {
                            if (response.data.status === "LOCKED" && !force) {
                                appModals.confirm($filter('translate')('title.confirm'), response.data.restartMsg)
                                    .result.then(
                                    function () {
                                        $scope.calculate(true, cancelTask);
                                    });
                            } else if (response.data.status === "EXIST_TASK" && !cancelTask) {
                                appModals.confirm($filter('translate')('title.confirm'), $filter('translate')('title.returnExistTask'))
                                    .result.then(
                                    function () {
                                        $scope.calculate(force, true);
                                    });
                            }
                        }
                    });
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.accept = function (force, cancelTask) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/accept",
                        params: {
                            force: force ? force : false,
                            cancelTask: cancelTask ? cancelTask : false
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                            initPage();
                        } else {
                            if (response.data.status === "LOCKED" && !force) {
                                appModals.confirm($filter('translate')('title.confirm'), response.data.restartMsg)
                                    .result.then(
                                    function () {
                                        $scope.accept(true, cancelTask);
                                    });
                            } else if (response.data.status === "EXIST_TASK" && !cancelTask) {
                                appModals.confirm($filter('translate')('title.confirm'), $filter('translate')('title.returnExistTask'))
                                    .result.then(
                                    function () {
                                        $scope.accept(force, true);
                                    });
                            } else if (response.data.status === "NOT_EXIST_XML") {
                                $window.alert($filter('translate')('title.acceptImpossible'));
                            }
                        }
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Проверить"
                 */
                $scope.check = function (force) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/check",
                        params: {
                            force: force ? force : false
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        } else {
                            if (response.data.status === "LOCKED" && !force) {
                                appModals.confirm($filter('translate')('title.confirm'), response.data.restartMsg)
                                    .result.then(
                                    function () {
                                        $scope.check(true);
                                    });
                            } else if (response.data.status === "NOT_EXIST_XML") {
                                $window.alert($filter('translate')('title.checkImpossible'));
                            }
                        }
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Вернуть в создана"
                 */
                $scope.returnToCreated = function () {
                    appModals.confirm($filter('translate')('title.confirm'), $filter('translate')('title.returnToCreatedDeclaration'))
                        .result.then(
                        function () {
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/returnToCreated"
                            }).then(function () {
                                initPage();
                            });
                        });
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Удалить"
                 */
                $scope.delete = function () {
                    appModals.confirm($filter('translate')('title.confirm'), $filter('translate')('title.deleteDeclaration'))
                        .result.then(
                        function () {
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/delete"
                            }).then(function () {
                                $window.location.assign('/index.html#/taxes/ndflJournal');
                            });
                        });
                };


                /**
                 * @description Обработка события, которое возникает при нажании на ссылку "Источники"
                 */
                $scope.showSourcesClick = function () {
                    appModals.create('client/app/taxes/ndfl/formSources.html', 'sourcesFormCtrl');
                };

                initPage();

                $scope.selectTab = function(tab) {
                    $rootScope.$broadcast('tabSelected', tab);
                };
            }]);
}());