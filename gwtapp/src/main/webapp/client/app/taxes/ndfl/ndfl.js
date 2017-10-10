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
            'app.rest',
            'app.rnuNdflPersonFace',
            'app.returnToCreatedDialog'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndfl', {
                url: '/taxes/ndfl/{declarationDataId}?uuid',
                templateUrl: 'client/app/taxes/ndfl/ndfl.html',
                controller: 'ndflCtrl'
            });
        }])

        /**
         * @description Контроллер страницы РНУ НДФЛ и вкладки "Реквизиты"
         */
        .controller('ndflCtrl', [
            '$scope', '$timeout', '$window', '$stateParams', 'ShowToDoDialog', '$http', 'DeclarationDataResource', '$filter', '$logPanel', 'appModals', '$rootScope',
            'RefBookValuesResource', 'APP_CONSTANTS', '$state',
            function ($scope, $timeout, $window, $stateParams, $showToDoDialog, $http, DeclarationDataResource, $filter, $logPanel, appModals, $rootScope,
                      RefBookValuesResource, APP_CONSTANTS, $state) {

                if ($stateParams.uuid) {
                    $logPanel.open('log-panel-container', $stateParams.uuid);
                }

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

                initPage();

                function updateAvailableReports () {
                    if (!($scope.availableReports && $scope.availableXlsxReport && $scope.availableSpecificReport)) {
                        DeclarationDataResource.query({
                                declarationDataId: $stateParams.declarationDataId,
                                projection: "availableReports"
                            },
                            function (data) {
                                if (data) {
                                    $scope.availableReports = data.downloadXmlAvailable;
                                    $scope.availableXlsxReport = data.downloadXlsxAvailable;
                                    $scope.availableSpecificReport = data.downloadSpecificAvailable;
                                    if (!$scope.intervalId){
                                        $scope.intervalId = setInterval(function () {
                                            updateAvailableReports();
                                        }, 60000);
                                    }
                                }
                            }
                        );
                    } else {
                        clearInterval($scope.intervalId);
                    }
                }

                updateAvailableReports();


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
                 * @description Событие, которое возникает по нажатию на кнопку "Формирование отчетов"
                 */
                $scope.createReport = function () {
                    appModals.create('client/app/taxes/ndfl/rnuNdflPersonFace.html', 'rnuNdflPersonFaceFormCtrl', {declarationDataId: $scope.declarationDataId});
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
                            initPage();
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
                    appModals.create('client/app/taxes/ndfl/returnToCreatedDialog.html', 'returnToCreatedCtrl', {header: $filter('translate')('title.indicateReasonForReturn'), msg: $filter('translate')('title.reasonForReturn')}, {size : 'md'})
                        .result.then(
                        function (reason) {
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/returnToCreated",
                                params: {
                                    reason: reason
                                }
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
                            }).success(function () {
                                $state.go("ndflJournal", {});
                            });
                        });
                };


                /**
                 * @description Обработка события, которое возникает при нажании на ссылку "Источники"
                 */
                $scope.showSourcesClick = function () {
                    appModals.create('client/app/taxes/ndfl/formSources.html', 'sourcesFormCtrl');
                };

                $scope.selectTab = function(tab) {
                    $rootScope.$broadcast('tabSelected', tab);
                };

                $scope.downloadXml = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xml";
                };
                $scope.downloadXlsx = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xlsx";
                }
                $scope.downloadSpecific = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/rnu_ndfl_person_all_db";
                };

                $scope.createReportXlsx = function (force) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/reportXsls",
                        params: {
                            force: force ? force : false
                        }
                    }).success(function (response) {
                        if (response.uuid && response.uuid !== null) {
                            $logPanel.open('log-panel-container', response.uuid);
                        } else {
                            if (response.status === "LOCKED" && !force) {
                                appModals.confirm($filter('translate')('title.confirm'), response.restartMsg)
                                    .result.then(
                                    function () {
                                        $scope.createReportXlsx(true);
                                    });
                            } else if (response.status === "NOT_EXIST_XML") {
                                $window.alert($filter('translate')('title.noCalculationPerformed'));
                            } else if (response.status === "EXIST") {
                                $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xlsx";
                            }
                        }
                    });
                };

                $scope.createReportAllRnu = function (force) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/allRnuReport",
                        params: {
                            force: force ? force : false
                        }
                    }).success(function (response) {
                        if (response.uuid && response.uuid !== null) {
                            $logPanel.open('log-panel-container', response.uuid);
                        } else {
                            if (response.status === "LOCKED" && !force) {
                                appModals.confirm($filter('translate')('title.confirm'), response.restartMsg)
                                    .result.then(
                                    function () {
                                        $scope.createReportXlsx(true);
                                    });
                            } else if (response.status === "NOT_EXIST_XML") {
                                $window.alert($filter('translate')('title.noCalculationPerformed'));
                            } else if (response.status === "EXIST") {
                                $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/rnu_ndfl_person_all_db";
                            }
                        }
                    });
                };
            }]);
}());