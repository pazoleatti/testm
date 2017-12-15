(function () {
    'use strict';

    /**
     * @description Модуль для для работы со страницей РНУ НДФЛ
     */

    angular.module('app.ndflReport',
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
            'app.reportNdflPersonFace',
            'app.returnToCreatedDialog'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflReport', {
                url: '/taxes/ndfl/ndflReport/{declarationDataId}?uuid',
                templateUrl: 'client/app/taxes/ndfl/ndflReport.html?v=${buildUuid}',
                controller: 'ndflReportCtrl',
                resolve: {
                    checkExistenceAndKind: ['$q', 'DeclarationDataResource', '$dialogs', '$state', '$filter', '$stateParams', 'APP_CONSTANTS',
                        function ($q, DeclarationDataResource, $dialogs, $state, $filter, $stateParams, APP_CONSTANTS) {
                            var d = $q.defer();
                            DeclarationDataResource.query({
                                    declarationDataId: $stateParams.declarationDataId,
                                    projection: "existenceAndKind"
                                },
                                function (data) {
                                    if(data.exists && data.declarationKindId === APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id) {
                                        d.resolve();
                                    } else {
                                        d.reject();
                                        var message;
                                        if(data.exists) {
                                            message = $filter('translate')('ndfl.notReportDeclarationDataBegin') + $stateParams.declarationDataId + $filter('translate')('ndfl.notReportDeclarationDataEnd');
                                        } else {
                                            message = $filter('translate')('ndfl.removedDeclarationDataBegin') + $stateParams.declarationDataId + $filter('translate')('ndfl.removedDeclarationDataEnd');
                                        }
                                        $dialogs.errorDialog({
                                            content: message,
                                            closeBtnClick: function () {
                                                $state.go("/");
                                            }
                                        });
                                    }
                                }
                            );
                            return d.promise;
                        }]
                }
            });
        }])

        /**
         * @description Контроллер страницы РНУ НДФЛ и вкладки "Реквизиты"
         */
        .controller('ndflReportCtrl', [
            '$scope', '$timeout', '$window', '$stateParams', 'ShowToDoDialog', '$http', 'DeclarationDataResource',
            '$filter', '$logPanel', '$dialogs', '$rootScope', 'RefBookValuesResource', 'APP_CONSTANTS', '$state',
            '$interval', 'acceptDeclarationData', 'createReport', 'getPageImage', 'checkDeclarationData',
            'moveToCreatedDeclarationData', '$aplanaModal',
            function ($scope, $timeout, $window, $stateParams, $showToDoDialog, $http, DeclarationDataResource, $filter,
                      $logPanel, $dialogs, $rootScope, RefBookValuesResource, APP_CONSTANTS, $state, $interval,
                      acceptDeclarationData, createReport, getPageImage, checkDeclarationData,
                      moveToCreatedDeclarationData, $aplanaModal) {

                if ($stateParams.uuid) {
                    $logPanel.open('log-panel-container', $stateParams.uuid);
                }

                /**
                 * @description Инициализация первичных данных на странице
                 */
                var initPage = function () {
                    DeclarationDataResource.query({
                            declarationDataId: $stateParams.declarationDataId,
                            projection: "declarationData",
                            nooverlay: true
                        },
                        function (data) {
                            if (data) {
                                $scope.declarationData = data;
                                $scope.declarationDataId = $stateParams.declarationDataId;
                                switch (data.declarationType) {
                                    case APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1.id:
                                        $scope.declarationTypeName = APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1.name;
                                        break;
                                    case APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2.id:
                                        $scope.declarationTypeName = APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2.name;
                                        break;
                                    case APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL.id:
                                        $scope.declarationTypeName = APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL.name;
                                        break;
                                }


                            }
                        }
                    );
                };

                initPage();

                $scope.$on("UPDATE_DECLARATION_DATA", function () {
                    initPage();
                });

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
                                content: response.restartMsg,
                                okBtnCaption: $filter('translate')('common.button.yes'),
                                cancelBtnCaption: $filter('translate')('common.button.no'),
                                okBtnClick:function () {
                                    $scope.createPairKppOktmo(true, create);
                                }
                            });
                        } else if (response.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.EXIST) {
                            $window.location = location;
                        }
                    }
                }

                $scope.pdfLoading = false;
                $scope.pdfLoaded = false;

                /**
                 * @description Проверяет готовность отчетов у открытой формы
                 */
                // TODO: Убрать использование постоянных запросов
                function updateAvailableReports() {
                    if (!($scope.availablePdf && $scope.availableReports && $scope.availableXlsxReport)) {
                        DeclarationDataResource.query({
                                declarationDataId: $stateParams.declarationDataId,
                                projection: "availableNdflReports",
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
                                    $scope.availablePdf = data.availablePdf;
                                    $scope.availableReports = data.downloadXmlAvailable;
                                    $scope.availableXlsxReport = data.downloadXlsxAvailable;
                                    if (!$scope.pdfLoaded && data.availablePdf) {
                                        getPageImage.query({
                                                declarationDataId: $stateParams.declarationDataId,
                                                pageId: 0
                                            },
                                            function (response) {
                                                $scope.reportImage = response.requestUrl;
                                                $scope.currPage = 1;
                                                $http({
                                                    method: "GET",
                                                    url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/pageCount"
                                                }).success(function (response) {
                                                    $scope.pagesTotal = response;
                                                });
                                            });
                                        $scope.pdfLoaded = true;
                                    }
                                    if (!$scope.availablePdf && !$scope.pdfLoading) {
                                        $scope.pdfMessage = "Область предварительного просмотра. Расчет налоговой формы выполнен. Форма предварительного просмотра не сформирована";
                                    }
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

                /**
                 * @description Выбрать следующую страницу отчета
                 */
                $scope.nextPage = function () {
                    if ($scope.currPage !== $scope.pagesTotal) {
                        getPageImage.query({
                                declarationDataId: $stateParams.declarationDataId,
                                pageId: $scope.currPage
                            },
                            function (response) {
                                $scope.reportImage = response.requestUrl;
                                $scope.currPage++;
                            });
                    }
                };

                /**
                 * @description Выбрать предыдущую страницу отчета
                 */
                $scope.prevPage = function () {
                    if ($scope.currPage > 1) {
                        getPageImage.query({
                                declarationDataId: $stateParams.declarationDataId,
                                pageId: $scope.currPage - 2
                            },
                            function (response) {
                                $scope.reportImage = response.requestUrl;
                                $scope.currPage--;
                            });
                    }
                };

                $rootScope.$on("$locationChangeStart", function () {
                    $interval.cancel($scope.intervalId);
                });

                $scope.showToDoDialog = function () {
                    $showToDoDialog();
                };

                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');

                /**
                 * @description Открытие МО История изменений
                 */
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
                    var title = $scope.declarationData.declarationType === APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1.id?$filter('translate')('reportPersonFace.title'):$filter('translate')('reportPersonFace.title2');
                    $aplanaModal.open({
                        title: title,
                        templateUrl: 'client/app/taxes/ndfl/reportNdflPersonFace.html',
                        controller: 'reportNdflPersonFaceFormCtrl',
                        windowClass: 'modal1200',
                        resolve: {
                            $shareData: function () {
                                return {
                                    declarationDataId: $scope.declarationDataId
                                };
                            }
                        }
                    });
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
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.accept = function (force, cancelTask) {
                    {
                        force = typeof force !== 'undefined' ? force : false;
                        cancelTask = typeof cancelTask !== 'undefined' ? cancelTask : false;
                    }
                    acceptDeclarationData.query({declarationDataId: $stateParams.declarationDataId}, {
                            taxType: 'NDFL',
                            force: force,
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
                                        okBtnClick: function () {
                                            $scope.accept(true, cancelTask);
                                        }
                                    });
                                } else if (response.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.EXIST_TASK && !cancelTask) {
                                    $dialogs.confirmDialog({
                                        content: $filter('translate')('title.returnExistTask'),
                                        okBtnCaption: $filter('translate')('common.button.yes'),
                                        cancelBtnCaption: $filter('translate')('common.button.no'),
                                        okBtnClick: function () {
                                            $scope.accept(force, true);
                                        }
                                    });
                                } else if (response.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.NOT_EXIST_XML) {
                                    $window.alert($filter('translate')('title.acceptImpossible'));
                                }
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
                                $dialogs.confirmDialog({
                                    content: response.restartMsg,
                                    okBtnCaption: $filter('translate')('common.button.yes'),
                                    cancelBtnCaption: $filter('translate')('common.button.no'),
                                    okBtnClick: function () {
                                        $scope.check(true);
                                    }
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
                                $state.go("ndflReportJournal", {});
                            });
                        }
                    });
                };

                /**
                 * @description Событие которое возникает при нажатии на кнопку "Показать"
                 * @param force
                 * @param create
                 */
                $scope.show = function (force, create) {
                    {
                        force = typeof force !== 'undefined' ? force : false;
                        create = typeof create !== 'undefined' ? create : false;
                    }
                    $scope.pdfMessage = "Область предварительного просмотра. Расчет налоговой формы выполнен. Идет формирование формы предварительного просмотра";
                    $scope.pdfLoading = true;
                    createReport.query({
                            declarationDataId: $stateParams.declarationDataId,
                            isForce: force,
                            taxType: 'NDFL',
                            type: 'PDF',
                            create: create
                        },
                        function (response) {
                            if (response.uuid && response.uuid !== null) {
                                $logPanel.open('log-panel-container', response.uuid);
                                initPage();
                            }
                        }
                    );
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

                $scope.downloadXml = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xml";
                };
                $scope.downloadXlsx = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xlsx";
                };
            }]);
}());