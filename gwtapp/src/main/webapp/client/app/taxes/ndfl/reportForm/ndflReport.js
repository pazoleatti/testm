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
            'app.returnToCreatedDialog',
            'app.resizer',
            'app.pager',
            'app.updateDocStateModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflReport', {
                url: '/taxes/ndfl/ndflReport/{declarationDataId}?uuid',
                templateUrl: 'client/app/taxes/ndfl/reportForm/ndflReport.html',
                controller: 'ndflReportCtrl',
                resolve: {
                    checkExistenceAndKind: ['$q', '$interval', 'DeclarationDataResource', '$dialogs', '$state', '$filter', '$stateParams', 'APP_CONSTANTS',
                        function ($q, $interval, DeclarationDataResource, $dialogs, $state, $filter, $stateParams, APP_CONSTANTS) {
                            var d = $q.defer();
                            DeclarationDataResource.query({
                                    declarationDataId: $stateParams.declarationDataId,
                                    projection: "existenceAndKind"
                                },
                                function (data) {
                                    if (data.existDeclarationData && (data.declarationKindId === APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id ||
                                        data.declarationKindId === APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS_FL.id) &&
                                        data.declarationTypeId !== APP_CONSTANTS.DECLARATION_TYPE.APP_2.id
                                    ) {
                                        d.resolve();
                                    } else {
                                        d.reject();
                                        var message;
                                        if (data.existDeclarationData) {
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
            '$interval', 'acceptDeclarationData', 'getPageImage', 'checkDeclarationData',
            'moveToCreatedDeclarationData', '$aplanaModal', 'CommonParamResource',
            function ($scope, $timeout, $window, $stateParams, $showToDoDialog, $http, DeclarationDataResource, $filter,
                      $logPanel, $dialogs, $rootScope, RefBookValuesResource, APP_CONSTANTS, $state, $interval,
                      acceptDeclarationData, getPageImage, checkDeclarationData,
                      moveToCreatedDeclarationData, $aplanaModal, CommonParamResource) {

                if ($stateParams.uuid) {
                    $logPanel.open('log-panel-container', $stateParams.uuid);
                }

                /**
                 * @description Инициализация первичных данных на странице
                 */
                function updateDeclarationInfo() {
                    DeclarationDataResource.query({
                            declarationDataId: $stateParams.declarationDataId,
                            projection: "declarationData",
                            nooverlay: true
                        },
                        function (data) {
                            if (data) {
                                if (!data.declarationDataExists) {
                                    cancelAllIntervals();
                                    showDeclarationDataNotExistsError();
                                } else {
                                    $scope.declarationData = data;
                                    $scope.declarationDataId = $stateParams.declarationDataId;
                                    var declarationTypes = [APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1, APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2,
                                        APP_CONSTANTS.DECLARATION_TYPE.REPORT_6_NDFL, APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_FL];
                                    declarationTypes.forEach(function (declarationType) {
                                        if (declarationType.id === data.declarationType) {
                                            $scope.declarationTypeName = declarationType.name;
                                        }
                                    });
                                    $scope.isCurrentForm2NDFLFL = $scope.declarationData.declarationType === APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_FL.id;
                                    $scope.isCurrentForm2NDFL_1_OR_2 = (($scope.declarationData.declarationType === APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1.id) ||
                                                                       ($scope.declarationData.declarationType === APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_2.id));
                                }
                            }
                        }
                    );
                }

                var updateDeclarationInfoInterval;

                function startUpdateDeclarationInfoInterval() {
                    updateDeclarationInfoInterval = $interval(updateDeclarationInfo, 3000);
                    updateDeclarationInfo();
                }

                startUpdateDeclarationInfoInterval();

                function cancelUpdateDeclarationInfoInterval() {
                    $interval.cancel(updateDeclarationInfoInterval);
                }

                $scope.pdfLoading = false;
                $scope.pdfLoaded = false;

                /**
                 * @description Проверяет готовность отчетов у открытой формы
                 */
                // TODO: Убрать использование постоянных запросов
                function updateAvailableReports() {
                    DeclarationDataResource.query({
                            declarationDataId: $stateParams.declarationDataId,
                            projection: "availableNdflReports",
                            nooverlay: true
                        },
                        function (data) {
                            if (data) {
                                if (!data.declarationDataExist) {
                                    cancelAllIntervals();
                                    showDeclarationDataNotExistsError();
                                } else {
                                    $scope.availableXml = data.downloadXmlAvailable;
                                    $scope.availablePdf = data.availablePdf;
                                    $scope.availableXlsxReport = data.downloadXlsxAvailable;
                                    if (!$scope.pdfLoaded && data.availablePdf) {
                                        $http({
                                            method: "GET",
                                            url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/pageCount"
                                        }).success(function (response) {
                                            $scope.pagesTotal = response;
                                        });
                                        $scope.pdfLoaded = true;
                                    }
                                    if (!$scope.availablePdf && !$scope.pdfLoading) {
                                        $scope.pdfMessage = "Область предварительного просмотра. Расчет налоговой формы выполнен. Форма предварительного просмотра не сформирована";
                                    }
                                }
                            }
                        }
                    );
                }

                var updateAvailableReportsInterval;

                function startUpdateAvailableReportsInterval() {
                    updateAvailableReportsInterval = $interval(function () {
                        updateAvailableReports();
                    }, 10000);
                    updateAvailableReports();
                }

                startUpdateAvailableReportsInterval();

                function cancelUpdateAvailableReportsInterval() {
                    $interval.cancel(updateAvailableReportsInterval);
                }

                function cancelAllIntervals() {
                    cancelUpdateDeclarationInfoInterval();
                    cancelUpdateAvailableReportsInterval();
                }

                function showDeclarationDataNotExistsError() {
                    var message = $filter('translate')('ndflReport.removedDeclarationDataBegin') + $stateParams.declarationDataId + $filter('translate')('ndfl.removedDeclarationDataEnd');
                    $dialogs.errorDialog({
                        content: message,
                        closeBtnClick: function () {
                            $state.go("/");
                        }
                    });
                }

                $scope.$on("AUTHORIZATION_EXPIRED", function () {
                    cancelAllIntervals();
                });

                $scope.$on('$destroy', function () {
                    cancelAllIntervals();
                });

                $scope.pager = {};

                /**
                 * @description Загружает страницу отчета
                 */
                $scope.onPageChange = function () {
                    getPageImage.query({
                            declarationDataId: $stateParams.declarationDataId,
                            pageId: $scope.pager.currPage - 1
                        },
                        function (response) {
                            $scope.reportImage = response.requestUrl;
                        });
                };

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
                        templateUrl: 'client/app/taxes/ndfl/logBusines.html',
                        controller: 'logBusinesFormCtrl',
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
                            templateUrl: 'client/app/taxes/ndfl/filesComments.html',
                            controller: 'filesCommentsCtrl',
                            windowClass: 'modal1200',
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
                 * @description Формирование спецотчета по физ лицу для 2-НДФЛ
                 */
                $scope.createReportNdflByPersonReport = function () {
                    var title = $scope.declarationData.declarationType === APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1.id ? $filter('translate')('reportPersonFace.title') : $filter('translate')('reportPersonFace.title2');
                    $aplanaModal.open({
                        title: title,
                        templateUrl: 'client/app/taxes/ndfl/reportForm/reportNdflPersonFace.html',
                        controller: 'reportNdflPersonFaceFormCtrl',
                        windowClass: 'modal1200',
                        resolve: {
                            $shareData: function () {
                                return {
                                    declarationDataId: $scope.declarationDataId,
                                    reportType: APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.REPORT_2NDFL,
                                    isSubReport: true,
                                    declarationData: $scope.declarationData
                                };
                            }
                        }
                    });
                };

                /**
                 * @description Создать 2-НДФЛ по физ лицу
                 */
                $scope.createNdflByPersonReport = function () {
                    var title = $scope.declarationData.declarationType === APP_CONSTANTS.DECLARATION_TYPE.REPORT_2_NDFL_1.id ? $filter('translate')('reportPersonFace.title') : $filter('translate')('reportPersonFace.title2');
                    $aplanaModal.open({
                        title: title,
                        templateUrl: 'client/app/taxes/ndfl/reportForm/reportNdflPersonFace.html',
                        controller: 'reportNdflPersonFaceFormCtrl',
                        windowClass: 'modal1200',
                        resolve: {
                            $shareData: function () {
                                return {
                                    declarationDataId: $scope.declarationDataId,
                                    reportType: APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.REPORT_2NDFL,
                                    isCreateNdfl: true,
                                    declarationData: $scope.declarationData
                                };
                            }
                        }
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.accept = function () {
                    acceptDeclarationData.query({declarationDataId: $stateParams.declarationDataId},
                        function (response) {
                            if (response.uuid && response.uuid !== null) {
                                $logPanel.open('log-panel-container', response.uuid);
                                updateDeclarationInfo();
                            }
                        }
                    );
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Проверить"
                 */
                $scope.check = function () {
                    checkDeclarationData.query({declarationDataId: $stateParams.declarationDataId},
                        function (response) {
                            if (response.uuid && response.uuid !== null) {
                                $logPanel.open('log-panel-container', response.uuid);
                                updateDeclarationInfo();
                            }
                        });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Вернуть в создана"
                 */
                $scope.returnToCreated = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('title.indicateReasonForReturn'),
                        templateUrl: 'client/app/taxes/ndfl/returnToCreatedDialog.html',
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
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/returnToCreated",
                                data: [$stateParams.declarationDataId],
                                params: {
                                    reason: reason
                                }
                            });
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
                                url: "controller/actions/declarationData/delete",
                                data: [$stateParams.declarationDataId]
                            }).then(function (response) {
                                if (response.data && response.data.uuid && response.data.uuid !== null) {
                                    if (response.data.success) {
                                        //Обновить страницу и, если есть сообщения, показать их
                                        var params = (response.data && response.data.uuid && response.data.uuid !== null) ? {uuid: response.data.uuid} : {};
                                        $state.go("ndflReportJournal", params, {reload: true});
                                    } else {
                                        $logPanel.open('log-panel-container', response.data.uuid);
                                    }
                                }
                            });
                        }
                    });
                };

                /**
                 * @description Событие которое возникает при нажатии на кнопку "Показать"
                 */
                $scope.createPdf = function () {
                    $scope.pdfMessage = "Область предварительного просмотра. Расчет налоговой формы выполнен. Идет формирование формы предварительного просмотра";
                    $scope.pdfLoading = true;
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/pdf"
                    }).success(function (response) {
                        if (response) {
                            $logPanel.open('log-panel-container', response);
                        }
                    });
                };

                /**
                 * @description Изменить состояние ЭД
                 */
                $scope.updateDocState = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('title.updateDocState'),
                        templateUrl: 'client/app/taxes/ndfl/reportForm/updateDocStateModal.html',
                        controller: 'UpdateDocStateCtrl',
                        windowClass: 'modal600'
                    }).result.then(function (docState) {
                        if (docState) {
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/updateDocState",
                                data: [$stateParams.declarationDataId],
                                params: {
                                    docStateId: docState.id
                                }
                            }).then(function (response) {
                                if (response.data && response.data.uuid) {
                                    $logPanel.open('log-panel-container', response.data.uuid);
                                }
                            });
                        }
                    });
                };

                /**
                 * @description Отправить в ЭДО
                 */
                $scope.sendEdo = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/sendEdo",
                        data: [$stateParams.declarationDataId]
                    }).then(function (response) {
                        if (response.data && response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };

                /**
                 * @description Обработка события, которое возникает при нажании на ссылку "Источники"
                 */
                $scope.showSourcesClick = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('sources.title.sourcesList'),
                        templateUrl: 'client/app/taxes/ndfl/formSources.html',
                        controller: 'sourcesFormCtrl',
                        windowClass: 'modal1200'
                    });
                };

                $scope.downloadXml = function () {
                    $window.open("controller/rest/declarationData/" + $stateParams.declarationDataId + "/xml", '_blank');
                };
                $scope.downloadPdf = function () {
                    $window.open("controller/rest/declarationData/" + $stateParams.declarationDataId + "/pdf", '_blank');
                };


                /**
                 * @description Получение конфигурационного параметра "Отправлять документы в ФП "Фонды""
                 */
                 $scope.documentsSendingEnabled = CommonParamResource.query({
                     codes: [APP_CONSTANTS.CONFIGURATION_PARAM.DOCUMENTS_SENDING_ENABLED],
                     projection: "allByEnums"
                 }, function (configurationsByCode) {
                     $scope.documentsSendingEnabled = configurationsByCode[APP_CONSTANTS.CONFIGURATION_PARAM.DOCUMENTS_SENDING_ENABLED].value;
                 });
            }]);
}());