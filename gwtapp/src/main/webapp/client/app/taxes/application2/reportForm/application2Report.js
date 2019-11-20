(function () {
    'use strict';

    /**
     * @description Модуль для для работы со отчетностью "Приложение 2"
     */

    angular.module('app.application2Report',
        ['ui.router',
            'app.formSources',
            'app.logBusines',
            'app.logPanel',
            'app.filesComments',
            'app.returnToCreatedDialog',
            'app.resizer',
            'app.pager'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('app2Report', {
                url: '/taxes/app2/app2Report/{declarationDataId}?uuid',
                templateUrl: 'client/app/taxes/application2/reportForm/application2Report.html',
                controller: 'app2ReportCtrl',
                resolve: {
                    checkExistenceAndKind: ['$q', '$interval', 'DeclarationDataResource', '$dialogs', '$state', '$filter', '$stateParams', 'APP_CONSTANTS',
                        function ($q, $interval, DeclarationDataResource, $dialogs, $state, $filter, $stateParams, APP_CONSTANTS) {
                            var d = $q.defer();
                            DeclarationDataResource.query({
                                    declarationDataId: $stateParams.declarationDataId,
                                    projection: "existenceAndKind"
                                },
                                function (data) {
                                    var isApp2ReportForm = data.declarationKindId === APP_CONSTANTS.NDFL_DECLARATION_KIND.REPORTS.id
                                        && data.declarationTypeId === APP_CONSTANTS.DECLARATION_TYPE.APP_2.id
                                    if (data.existDeclarationData && isApp2ReportForm) {
                                        d.resolve();
                                    } else {
                                        d.reject();
                                        var message;
                                        if (data.existDeclarationData) {
                                            if (data.declarationTypeId !== APP_CONSTANTS.DECLARATION_TYPE.APP_2.id) {
                                                message = $filter('translate')('ndfl.notReportDeclarationDataBegin') + $stateParams.declarationDataId + $filter('translate')('ndfl.notApp2DeclarationDataEnd');
                                            } else {
                                                message = $filter('translate')('ndfl.notReportDeclarationDataBegin') + $stateParams.declarationDataId + $filter('translate')('ndfl.notReportDeclarationDataEnd');
                                            }
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
                        }
                    ]
                }
            });
        }])

        /**
         * @description Контроллер страницы просмотра отчетной формы "Приложение 2"
         */
        .controller('app2ReportCtrl', [
            '$scope', '$timeout', '$window', '$stateParams', 'ShowToDoDialog', '$http', 'DeclarationDataResource',
            '$filter', '$logPanel', '$dialogs', '$rootScope', 'RefBookValuesResource', 'APP_CONSTANTS', '$state',
            '$interval', 'acceptDeclarationData', 'checkDeclarationData', 'moveToCreatedDeclarationData', '$aplanaModal',
            'CommonParamResource', 'CommonFilterUtils',
            function ($scope, $timeout, $window, $stateParams, $showToDoDialog, $http, DeclarationDataResource, $filter,
                      $logPanel, $dialogs, $rootScope, RefBookValuesResource, APP_CONSTANTS, $state, $interval,
                      acceptDeclarationData, checkDeclarationData, moveToCreatedDeclarationData, $aplanaModal,
                      CommonParamResource, CommonFilterUtils) {

                if ($stateParams.uuid) {
                    $logPanel.open('log-panel-container', $stateParams.uuid);
                }

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {
                        person: {}
                    },
                    filterName: 'ndflFilterForApp2_' + $stateParams.declarationDataId
                };

                $scope.app2Filter = getApp2Filter();
                $scope.searchFilter.fillFilterParams = function () {
                    $scope.app2Filter = getApp2Filter();
                };

                /**
                 * @description Установка признака заполненности фильтра
                 */
                $scope.searchFilter.isClearByFilterParams = function () {
                    $scope.searchFilter.isClear = !(isEmpty($scope.app2Filter.person));
                };

                /**
                 * @description сброс фильтра
                 */
                $scope.searchFilter.resetFilterParams = function () {
                    /* очистка всех инпутов на форме */
                    $scope.searchFilter.params = {
                        person: {}
                    };
                };

                // Возвращяет признак того, что объект незаполнен
                function isEmpty(object) {
                    return CommonFilterUtils.isEmpty(object);
                }

                /**
                 * @description возвращяет фильтр, который будет отправлен на сервер для составления запросов
                 */
                function getApp2Filter() {
                    var filter = {
                        declarationDataId: $stateParams.declarationDataId,
                        person: $scope.searchFilter.params.person
                    };
                    return filter;
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
                                    $scope.declarationTypeName = APP_CONSTANTS.DECLARATION_TYPE.APP_2.name;
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

                /**
                 * @description Проверяет готовность отчетов у открытой формы
                 */
                function updateAvailableReports() {

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

            }
        ]);
}());