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
            'app.filesComments',
            'app.rest',
            'app.rnuNdflPersonFace',
            'app.returnToCreatedDialog'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndfl', {
                url: '/taxes/ndfl/{declarationDataId}?uuid',
                templateUrl: 'client/app/taxes/ndfl/ndfl.html?v=${buildUuid}',
                controller: 'ndflCtrl'
            });
        }])

        /**
         * @description Контроллер страницы РНУ НДФЛ и вкладки "Реквизиты"
         */
        .controller('ndflCtrl', [
            '$scope', '$timeout', '$window', '$stateParams', 'ShowToDoDialog', '$http', 'DeclarationDataResource', '$filter', '$logPanel', '$aplanaModal', '$dialogs',
            '$rootScope', 'RefBookValuesResource', 'APP_CONSTANTS', '$state', '$interval', 'acceptDeclarationData',
            'checkDeclarationData', 'moveToCreatedDeclarationData',
            function ($scope, $timeout, $window, $stateParams, $showToDoDialog, $http, DeclarationDataResource, $filter,
                      $logPanel, $aplanaModal, $dialogs, $rootScope, RefBookValuesResource, APP_CONSTANTS, $state,
                      $interval, acceptDeclarationData, checkDeclarationData, moveToCreatedDeclarationData) {

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

                /**
                 * @description Проверяет готовность отчетов у открытой формы
                 */
                // TODO: Убрать использование постоянных запросов
                function updateAvailableReports() {
                    if (!($scope.availableReports && $scope.availableXlsxReport && $scope.availableRnuNdflPersonAllDb && $scope.availableReportKppOktmo)) {
                        DeclarationDataResource.query({
                                declarationDataId: $stateParams.declarationDataId,
                                projection: "availableReports",
                                nooverlay: true
                            },
                            function (data) {
                                if (data) {
                                    if (!data.declarationDataExist) {
                                        $interval.cancel($scope.intervalId);
                                        var message = $filter('translate')('ndfl.removedDeclarationDataBegin') + $stateParams.declarationDataId + $filter('translate')('ndfl.removedDeclarationDataEnd');
                                        $dialogs.errorDialog({
                                            content: message,
                                            closeBtnClick: function () {
                                                $state.go("/");
                                            }
                                        });
                                        return;
                                    }
                                    $scope.availableReports = data.downloadXmlAvailable;
                                    $scope.availableXlsxReport = data.downloadXlsxAvailable;
                                    $scope.availableRnuNdflPersonAllDb = data.downloadRnuNdflPersonAllDb;
                                    $scope.availableReportKppOktmo = data.downloadReportKppOktmo;
                                    if (!$scope.intervalId) {
                                        $scope.intervalId = $interval(function () {
                                            updateAvailableReports();
                                        }, 10000);
                                    }
                                }
                            }
                        );
                    } else {
                        $interval.cancel($scope.intervalId);
                    }
                }

                updateAvailableReports();

                $rootScope.$on("$locationChangeStart", function () {
                    $interval.cancel($scope.intervalId);
                });

                $scope.showToDoDialog = function () {
                    $showToDoDialog();
                };

                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');

                $scope.openHistoryOfChange = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('logBusiness.title'),
                        templateUrl: 'client/app/taxes/ndfl/logBusines.html?v=${buildUuid}',
                        controller: 'logBusinesFormCtrl',
                        windowClass: 'modal1000',
                        resolve: {
                            $shareData: function () {
                                return {
                                    declarationDataId: $scope.declarationDataId
                                };
                            }
                        }
                    });
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
                        $aplanaModal.open({
                            title: $filter('translate')('filesComment.header'),
                            templateUrl: 'client/app/taxes/ndfl/filesComments.html?v=${buildUuid}',
                            controller: 'filesCommentsCtrl',
                            windowClass: 'modalMax',
                            resolve: {
                                $shareData: function () {
                                    return {
                                        declarationState: $scope.declarationData.state,
                                        declarationDataId: $scope.declarationDataId,
                                        attachFileTypes: attachFileTypes
                                    };
                                }
                            }
                        });
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Формирование отчетов"
                 */
                $scope.createReport = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('rnuPersonFace.title'),
                        templateUrl: 'client/app/taxes/ndfl/rnuNdflPersonFace.html',
                        controller: 'rnuNdflPersonFaceFormCtrl',
                        windowClass: 'modal1000',
                        resolve: {
                            $shareData: function () {
                                return {
                                    declarationDataId: $scope.declarationDataId
                                };
                            }
                        }
                    });
                };

                /**
                 * Метод инкапсулирующий действия в случае выполнение в случае успешного формирования отчета
                 * @param response
                 * @param location
                 * @param create
                 */
                function performReportSuccessResponse(response, location, create) {
                    if (response.uuid && response.uuid !== null) {
                        $logPanel.open('log-panel-container', response.uuid);
                    } else {
                        if (response.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.NOT_EXIST_XML) {
                            $dialogs.messageDialog({
                                content: $filter('translate')('title.noCalculationPerformed')
                            });
                        } else if (response.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.LOCKED) {
                            $dialogs.confirmDialog({
                                title: $filter('translate')('title.confirm'),
                                content: response.restartMsg,
                                okBtnCaption: $filter('translate')('common.button.yes'),
                                cancelBtnCaption: $filter('translate')('common.button.no'),
                                okBtnClick: function () {
                                    $scope.createPairKppOktmo(true, create);
                                }
                            });
                        } else if (response.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.EXIST) {
                            $window.location = location;
                        }
                    }
                }

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Рассчитать"
                 */
                $scope.calculate = function (force, cancelTask) {
                    {
                        force = typeof force !== 'undefined' ? force : false;
                        cancelTask = typeof cancelTask !== 'undefined' ? cancelTask : false;
                    }
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
                                $dialogs.confirmDialog({
                                    content: response.data.restartMsg,
                                    okBtnCaption: $filter('translate')('common.button.yes'),
                                    cancelBtnCaption: $filter('translate')('common.button.no'),
                                    okBtnClick: function () {
                                        $scope.calculate(true, cancelTask);
                                    }
                                });
                            } else if (response.data.status === "EXIST_TASK" && !cancelTask) {
                                $dialogs.confirmDialog({
                                    content: $filter('translate')('title.returnExistTask'),
                                    okBtnCaption: $filter('translate')('common.button.yes'),
                                    cancelBtnCaption: $filter('translate')('common.button.no'),
                                    okBtnClick: function () {
                                        $scope.calculate(force, true);
                                    }
                                });
                            }
                        }
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.accept = function (force, cancelTask) {
                    {
                        force = typeof force !== 'undefined' ? force : false;
                        cancelTask = typeof cancelTask !== 'undefined' ? cancelTask : false;
                    }
                    acceptDeclarationData.query({declarationDataId: $stateParams.declarationDataId },{
                        taxType: 'NDFL',
                            force: force ,
                            cancelTask: cancelTask
                        },
                    function (response) {
                        if (response.uuid && response.uuid !== null) {
                            $logPanel.open('log-panel-container', response.uuid);
                            initPage();
                        } else {
                            if (response.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.LOCKED && !force) {
                                $dialogs.confirmDialog({
                                    content: response.restartMsg,
                                    okBtnCaption: $filter('translate')('common.button.yes'),
                                    cancelBtnCaption: $filter('translate')('common.button.no'),
                                    okBtnClick:function () {
                                        $scope.accept(true, cancelTask);
                                    }
                            });
                            } else if (response.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.EXIST_TASK && !cancelTask) {
                                $dialogs.confirmDialog({
                                    content:$filter('translate')('title.returnExistTask'),
                                    okBtnCaption: $filter('translate')('common.button.yes'),
                                    cancelBtnCaption: $filter('translate')('common.button.no'),
                                    okBtnClick:function () {
                                        $scope.accept(force, true);}
                                    });
                            } else if (response.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.NOT_EXIST_XML) {
                                $window.alert($filter('translate')('title.acceptImpossible'));}
                            }
                        }
                    );
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Проверить"
                 */
                $scope.check = function (force) {

                    force = typeof force !== 'undefined' ? force : false;

                    checkDeclarationData.query({declarationDataId: $stateParams.declarationDataId}, {
                        force: force
                    }, function (response) {
                        if (response.uuid && response.uuid !== null) {
                            $logPanel.open('log-panel-container', response.uuid);
                            initPage();
                        } else {
                            if (response.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.LOCKED && !force) {
                                appModals.confirm($filter('translate')('title.confirm'), response.restartMsg)
                                    .result.then(
                                    function () {
                                        $scope.check(true);
                                    });
                            } else if (response.data.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.NOT_EXIST_XML) {
                                $window.alert($filter('translate')('title.checkImpossible'));
                            }
                        }
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Вернуть в создана"
                 */
                $scope.returnToCreated = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('title.indicateReasonForReturn'),
                        templateUrl: 'client/app/taxes/ndfl/returnToCreatedDialog.html?v=${buildUuid}',
                        controller: 'returnToCreatedCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    msg: $filter('translate')('title.reasonForReturn')
                                };
                            }
                        }
                    }).result.then(
                        function (reason) {
                            moveToCreatedDeclarationData.query({declarationDataId: $stateParams.declarationDataId}, {
                                    reason: reason
                                }, function (response) {
                                    initPage();
                                }
                            );
                        });
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Удалить"
                 */
                $scope.delete = function () {
                    $dialogs.confirmDialog({
                        content: $filter('translate')('title.deleteDeclaration'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/delete"
                            }).success(function () {
                                $state.go("ndflJournal", {});
                            });
                        }
                    });
                };


                /**
                 * @description Обработка события, которое возникает при нажании на ссылку "Источники"
                 */
                $scope.showSourcesClick = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('sources.title.sourcesList'),
                        templateUrl: 'client/app/taxes/ndfl/formSources.html?v=${buildUuid}',
                        controller: 'sourcesFormCtrl',
                        windowClass: 'modal1200'
                    });
                };

                $scope.selectTab = function (tab) {
                    $rootScope.$broadcast('tabSelected', tab);
                };

                $scope.downloadXml = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xml";
                };
                $scope.downloadXlsx = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xlsx";
                };
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
                        performReportSuccessResponse(response, "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xlsx");
                    });
                };

                /**
                 * формирование спецотчета "РНУ НДФЛ по всем ФЛ"
                 * @param force
                 */
                $scope.createReportAllRnu = function (force) {

                    force = typeof force !== 'undefined' ? force : false;

                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/allRnuReport",
                        params: {
                            force: force ? force : false
                        }
                    }).success(function (response) {
                        performReportSuccessResponse(response, "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_PERSON_ALL_DB);
                    });
                };

                /**+
                 * Создание спецотчета "Реестр сформированной отчетности"
                 * @param force
                 * @param create
                 */
                $scope.createPairKppOktmo = function (force, create) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/pairKppOktmoReport",
                        params: {
                            force: force,
                            create: create
                        }
                    }).success(function (response) {
                        performReportSuccessResponse(response, "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.REPORT_KPP_OKTMO, create);
                    });
                };

            }]);
}());