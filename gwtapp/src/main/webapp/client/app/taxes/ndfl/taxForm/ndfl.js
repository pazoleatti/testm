(function () {
    'use strict';

    /**
     * @description Модуль для для работы со страницей РНУ НДФЛ
     */

    angular.module('app.ndfl',
        ['ui.router',
            'app.editNdflIncomesAndTax',
            'app.editNdflDeduction',
            'app.editNdflPrepayment',
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
            'app.returnToCreatedDialog',
            'app.createNdfl2_6DataReport'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndfl', {
                url: '/taxes/ndfl/{declarationDataId}?uuid',
                templateUrl: 'client/app/taxes/ndfl/taxForm/ndfl.html',
                controller: 'ndflCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_NDFL)) {
                            $state.go("/");
                        }
                    }],
                resolve: {
                    checkExistenceAndKind: ['$q', '$interval', 'DeclarationDataResource', '$dialogs', '$state', '$filter', '$stateParams', 'APP_CONSTANTS',
                        function ($q, $interval, DeclarationDataResource, $dialogs, $state, $filter, $stateParams, APP_CONSTANTS) {
                            var d = $q.defer();
                            DeclarationDataResource.query({
                                    declarationDataId: $stateParams.declarationDataId,
                                    projection: "existenceAndKind"
                                },
                                function (data) {
                                    if (data.exists && (data.declarationKindId === APP_CONSTANTS.NDFL_DECLARATION_KIND.PRIMARY.id || data.declarationKindId === APP_CONSTANTS.NDFL_DECLARATION_KIND.CONSOLIDATED.id)) {
                                        d.resolve();
                                    } else {
                                        d.reject();
                                        var message;
                                        if (data.exists) {
                                            message = $filter('translate')('ndfl.notPersonalOrConsolidatedDeclarationDataBegin') + $stateParams.declarationDataId + $filter('translate')('ndfl.notPersonalOrConsolidatedDeclarationDataEnd');
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
        .controller('ndflCtrl', [
            '$scope', '$timeout', '$window', '$stateParams', 'ShowToDoDialog', '$http', 'DeclarationDataResource', '$filter', '$logPanel', '$aplanaModal', '$dialogs',
            '$rootScope', 'RefBookValuesResource', 'APP_CONSTANTS', '$state', '$interval', 'acceptDeclarationData',
            'checkDeclarationData', 'moveToCreatedDeclarationData', 'Upload', 'PermissionChecker',
            function ($scope, $timeout, $window, $stateParams, $showToDoDialog, $http, DeclarationDataResource, $filter,
                      $logPanel, $aplanaModal, $dialogs, $rootScope, RefBookValuesResource, APP_CONSTANTS, $state,
                      $interval, acceptDeclarationData, checkDeclarationData, moveToCreatedDeclarationData, Upload, PermissionChecker) {

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
                                    var isRefreshGridNeeded = false;
                                    // Обновляем информацию об актуальности если обновляем грид.
                                    // Обновляем грид если были изменения в течение просмотра формы.
                                    // Логика в этом блоке выстроена для этой цели.
                                    var oldActualDate = null;
                                    if ($scope.declarationData && $scope.declarationData.actualDataDate) {
                                        oldActualDate = $scope.declarationData.actualDataDate
                                    }
                                    if ($scope.declarationData && $scope.declarationData.actualDataDate &&
                                        $scope.declarationData.actualDataDate < data.lastDataModifiedDate) {
                                        isRefreshGridNeeded = true;
                                    }
                                    $scope.declarationData = data;
                                    $scope.declarationDataId = $stateParams.declarationDataId;
                                    if (isRefreshGridNeeded) {
                                        $scope.refreshGrid(1);
                                    } else if (oldActualDate) {
                                        $scope.declarationData.actualDataDate = oldActualDate
                                    }
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
                    DeclarationDataResource.query({
                            declarationDataId: $stateParams.declarationDataId,
                            projection: "availableReports",
                            nooverlay: true
                        },
                        function (data) {
                            if (data) {
                                if (!data.declarationDataExist) {
                                    cancelAllIntervals();
                                    showDeclarationDataNotExistsError();
                                } else {
                                    $scope.availableReports = data.reportAvailable.XML_DEC;
                                    $scope.availableXlsxReport = data.reportAvailable.EXCEL_DEC;
                                    $scope.availableExcelTemplate = data.reportAvailable.EXCEL_TEMPLATE_DEC;
                                    $scope.availableRnuNdflPersonAllDb = data.reportAvailable.rnu_ndfl_person_all_db;
                                    $scope.availableRateReport = data.reportAvailable.rnu_rate_report;
                                    $scope.availablePaymentReport = data.reportAvailable.rnu_payment_report;
                                    $scope.availableNdflDetailReport = data.reportAvailable.rnu_ndfl_detail_report;
                                    $scope.availableNdfl2_6DataXlsxReport = data.reportAvailable.rnu_ndfl_2_6_data_xlsx_report;
                                    $scope.availableNdfl2_6DataTxtReport = data.reportAvailable.rnu_ndfl_2_6_data_txt_report;
                                    $scope.availableReportKppOktmo = data.reportAvailable.report_kpp_oktmo;
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
                    var message = $filter('translate')('ndfl.removedDeclarationDataBegin') + $stateParams.declarationDataId + $filter('translate')('ndfl.removedDeclarationDataEnd');
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

                // Чтобы положение datepicker менялось при скроллинге
                angular.element('#ndflScrollPanel').scroll(function () {
                    $rootScope.$broadcast('WINDOW_SCROLLED_MSG');
                });

                $scope.showToDoDialog = function () {
                    $showToDoDialog();
                };

                $rootScope.$broadcast('UPDATE_NOTIF_COUNT');

                $scope.ndflTabsCtrl = {};
                $scope.ndfFLTab = {
                    title: $filter('translate')('tab.ndfl.requisites'),
                    contentUrl: 'client/app/taxes/ndfl/taxForm/ndflTabs/personsTab.html',
                    fetchTab: true,
                    active: true
                };
                $scope.incomesAndTaxTab = {
                    title: $filter('translate')('tab.ndfl.informationOnIncomesAndNdfl'),
                    contentUrl: 'client/app/taxes/ndfl/taxForm/ndflTabs/incomesTab.html',
                    fetchTab: true
                };
                $scope.deductionsTab = {
                    title: $filter('translate')('tab.ndfl.informationOnDeductions'),
                    contentUrl: 'client/app/taxes/ndfl/taxForm/ndflTabs/deductionsTab.html',
                    fetchTab: true
                };
                $scope.prepaymentTab = {
                    title: $filter('translate')('tab.ndfl.informationOnAdvancePayments'),
                    contentUrl: 'client/app/taxes/ndfl/taxForm/ndflTabs/prepaymentsTab.html',
                    fetchTab: true
                };
                $scope.ndflTabs = [$scope.ndfFLTab, $scope.incomesAndTaxTab, $scope.deductionsTab, $scope.prepaymentTab];

                $scope.refreshGrid = function (page) {
                    $scope.ndflTabs.forEach(function (tab) {
                        tab.isDataLoaded = false;
                    });

                    $scope.ndflTabsCtrl.getActiveTab().refreshGrid(page);
                };

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {person: {}, income: {}, deduction: {}, prepayment: {}},
                    filterName: 'ndflFilterForDec' + $stateParams.declarationDataId
                };

                $scope.ndflFilter = getNdflFilter();
                $scope.searchFilter.fillFilterParams = function () {
                    $scope.ndflFilter = getNdflFilter();
                };

                /**
                 * @description Установка признака заполненности фильтра
                 */
                $scope.searchFilter.isClearByFilterParams = function () {
                    $scope.searchFilter.isClear = !(isEmpty($scope.ndflFilter.person) && isEmpty($scope.ndflFilter.income)
                    && isEmpty($scope.ndflFilter.deduction) && isEmpty($scope.ndflFilter.prepayment));
                };

                function isEmpty(object) {
                    return Object.keys(object).every(function (key) {
                        return !object[key];
                    });
                }

                /**
                 * @description сброс фильтра
                 */
                $scope.searchFilter.resetFilterParams = function () {
                    /* очистка всех инпутов на форме */
                    $scope.searchFilter.params = {person: {}, income: {}, deduction: {}, prepayment: {}};
                };

                /**
                 * @description возвращяет фильтр, который будет отправлен на сервер для составления запросов
                 */
                function getNdflFilter() {
                    var filter = {
                        declarationDataId: $stateParams.declarationDataId,
                        person: $scope.searchFilter.params.person,
                        income: angular.copy($scope.searchFilter.params.income),
                        deduction: $scope.searchFilter.params.deduction,
                        prepayment: $scope.searchFilter.params.prepayment
                    };
                    if (filter.income.urmList) {
                        filter.income.urmList = filter.income.urmList.map(function (urm) {
                            return urm.enumName;
                        });
                    }
                    return filter;
                }

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
                 * @description Событие, которое возникает по нажатию на кнопку "Формирование отчетов"
                 */
                $scope.createReport = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('rnuPersonFace.title'),
                        templateUrl: 'client/app/taxes/ndfl/taxForm/rnuNdflPersonFace.html',
                        controller: 'rnuNdflPersonFaceFormCtrl',
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
                 * Обрабатывает результат формирования отчета
                 * @param response результат
                 * @param restartFunc функция повторного запуска формирования, если формирование уже запущено, то вызывается эта функция после подтверждения
                 * @param reportAvailableModel модель отвечающая за доступность скачивания отчета
                 */
                function performReportSuccessResponse(response, restartFunc, reportAvailableModel) {
                    if (response.uuid && response.uuid !== null) {
                        // задача запустилась
                        $logPanel.open('log-panel-container', response.uuid);
                        $scope[reportAvailableModel] = false;
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
                                    restartFunc(true);
                                }
                            });
                        }
                    }
                }

                /**
                 * Флаг, означающий, может ли текущий пользоватеть выполнить редактирование строки в таблице
                 * Зависит от выделенных строк на вкладках, поэтому реализовано через события
                 */
                $scope.canEditRow = false;
                $rootScope.$on("selectedRowCountChanged", function (event, count) {
                    $scope.canEditRow = count === 1
                });

                /**
                 * Событие, которое возникает по нажатию на кнопку "Редактировать строку"
                 */
                $scope.showEditRowModal = function () {
                    var row = $scope.ndflTabsCtrl.getActiveTab().getRows()[0];

                    //Раздел 2 (Сведения о доходах и НДФЛ)
                    var title = "incomesAndTax.edit.title";
                    var templateUrl = "client/app/taxes/ndfl/taxForm/editing/editNdflIncomesAndTax.html";
                    var controller = "editNdflIncomesAndTaxFormCtrl";

                    if ($scope.ndflTabsCtrl.getActiveTab().getSection() === 3) {
                        //Раздел 3 (Сведения о вычетах)
                        title = "ndflDeduction.edit.title";
                        templateUrl = "client/app/taxes/ndfl/taxForm/editing/editNdflDeduction.html";
                        controller = "editNdflDeductionFormCtrl";
                    } else if ($scope.ndflTabsCtrl.getActiveTab().getSection() === 4) {
                        //Раздел 4 (Сведения о доходах в виде авансовых платежей)
                        title = "ndlfPrepayment.edit.title";
                        templateUrl = "client/app/taxes/ndfl/taxForm/editing/editNdflPrepayment.html";
                        controller = "editNdflPrepaymentFormCtrl";
                    }

                    $aplanaModal.open({
                        title: $filter('translate')(title, {
                            rowNum: row.rowNum,
                            operationId: row.operationId
                        }),
                        templateUrl: templateUrl,
                        controller: controller,
                        windowClass: 'modal900',
                        resolve: {
                            $shareData: function () {
                                return {
                                    row: $.extend(true, {}, row),
                                    declarationId: $stateParams.declarationDataId,
                                    department: $scope.declarationData.department
                                };
                            }
                        },
                        closeCallback: function (scope) {
                            scope.close();
                        }
                    }).result.then(
                        function (result) {
                            $http({
                                method: "POST",
                                url: "controller//actions/declarationData/" + $stateParams.declarationDataId + "/unlock"
                            });
                            if (result) {
                                $scope.canEditRow = false;
                                $scope.refreshGrid(1)
                            }
                        });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Идентифицировать ФЛ"
                 */
                $scope.identify = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/identify",
                        data: [$stateParams.declarationDataId]
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Консолидировать"
                 */
                $scope.consolidate = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/consolidate",
                        data: [$stateParams.declarationDataId]
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
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
                                    //Обновить страницу и, если есть сообщения, показать их
                                    var params = (response.data && response.data.uuid && response.data.uuid !== null) ? {uuid: response.data.uuid} : {};
                                    $state.go("ndflJournal", params, {reload: true});
                                }
                            });
                        }
                    });
                };

                /**
                 * @description Запрос на подтверждение выполнения опрерации
                 */
                $scope.confirmImport = function () {
                    // если данные уже загружались, то просим подтвердить
                    if ($scope.declarationData.lastDataModifiedDate) {
                        $dialogs.confirmDialog({
                            content: $filter('translate')('title.importDeclaration.confirm'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                angular.element('#upload').trigger('click');
                            }
                        });
                    } else {
                        angular.element('#upload').trigger('click');
                    }
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Загрузить из ТФ (Excel)"
                 */
                $scope.doImport = function (file) {
                    if (file) {
                        Upload.upload({
                            url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/import",
                            data: {uploader: file},
                        }).progress(function (e) {
                        }).then(function (response) {
                            if (response.data.uuid && response.data.uuid !== null) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            }
                        });
                    }
                };

                /**
                 * @description Событие которое возникает при нажатии на кнопку "Обновить данные ФЛ"
                 */
                $scope.updatePersonsData = function () {
                    $http({
                        method: "GET",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/updatePersonsData",
                    }).then(function (response) {
                        if (response.data && response.data !== null) {
                            $logPanel.open('log-panel-container', response.data);
                            updateDeclarationInfo();
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
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xml";
                };
                $scope.downloadXlsx = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xlsx";
                };
                $scope.downloadSpecific = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_PERSON_ALL_DB;
                };
                $scope.downloadRateReport = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_RATE_REPORT;
                };
                $scope.downloadPaymentReport = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_PAYMENT_REPORT;
                };
                $scope.downloadNdflDetailReport = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_DETAIL_REPORT;
                };
                $scope.downloadNdfl2_6DataXlsxReport = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_2_6_DATA_XLSX_REPORT;
                };
                $scope.downloadNdfl2_6DataTxtReport = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_2_6_DATA_TXT_REPORT;
                };
                $scope.downloadPairKppOktmo = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.REPORT_KPP_OKTMO;
                };
                $scope.downloadExcelTemplate = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/excelTemplate";
                };

                $scope.createReportXlsx = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/reportXsls",
                    }).success(function (response) {
                        if (response.uuid && response.uuid !== null) {
                            $logPanel.open('log-panel-container', response.uuid);
                        }
                    });
                };

                /**
                 * формирование спецотчета "РНУ НДФЛ по всем ФЛ"
                 */
                $scope.createReportAllRnu = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_PERSON_ALL_DB,
                    }).success(function (response) {
                        if (response) {
                            $logPanel.open('log-panel-container', response);
                        }
                    });
                };

                /**
                 * Создание спецотчета "Отчет Карманниковой: Отчет в разрезе ставок"
                 */
                $scope.createRateReport = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_RATE_REPORT,
                    }).success(function (response) {
                        if (response) {
                            $logPanel.open('log-panel-container', response);
                        }
                    });
                };

                /**
                 * Создание спецотчета "Отчет Карманниковой: Отчет в разрезе платёжных поручений"
                 */
                $scope.createPaymentReport = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_PAYMENT_REPORT,
                    }).success(function (response) {
                        if (response) {
                            $logPanel.open('log-panel-container', response);
                        }
                    });
                };

                /**
                 * Создание спецотчета "Отчет Карманниковой: Отчет в разрезе платёжных поручений"
                 */
                $scope.createNdflDetailReport = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_DETAIL_REPORT,
                    }).success(function (response) {
                        if (response) {
                            $logPanel.open('log-panel-container', response);
                        }
                    });
                };

                /**
                 * Создание спецотчета "Данные для включения в разделы 2-НДФЛ и 6-НДФЛ" (type = 'xlsx' или 'txt')
                 */
                $scope.showCreateNdfl2_6DataReport = function (type) {
                    $aplanaModal.open({
                        title: $filter('translate')('ndfl.report.ndfl2_6XlsxReport.modal.title'),
                        templateUrl: 'client/app/taxes/ndfl/taxForm/createNdfl2_6DataReportModal.html',
                        controller: 'createNdfl2_6DataReportCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    type: type,
                                    declarationData: $scope.declarationData
                                };
                            }
                        }
                    }).result.then(function (success) {
                        if (success) {
                            if (type === 'xlsx') {
                                $scope.availableNdfl2_6DataXlsxReport = false;
                            } else {
                                $scope.availableNdfl2_6DataTxtReport = false;
                            }
                        }
                    });
                };

                /**+
                 * Создание спецотчета "Реестр сформированной отчетности"
                 */
                $scope.createPairKppOktmo = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.REPORT_KPP_OKTMO
                    }).success(function (response) {
                        if (response) {
                            $logPanel.open('log-panel-container', response);
                        }
                    });
                };

                /**
                 * Формирует запрос на создание шаблона Excel-файла для загрузки
                 */
                $scope.createExcelTemplate = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/excelTemplate",
                    }).then(function (response) {
                        performReportSuccessResponse(response.data, $scope.createExcelTemplate, "availableExcelTemplate");
                    });
                };

                $scope.canCreateNdflReport = function () {
                    return $scope.declarationData && $scope.declarationData.declarationType === APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_CONSOLIDATED.id &&
                        $scope.declarationData.hasNdflPersons &&
                        PermissionChecker.check($scope.user, APP_CONSTANTS.USER_PERMISSION.CREATE_DECLARATION_REPORT);
                };

                $scope.createNdflReport = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('title.creatingReport'),
                        templateUrl: 'client/app/taxes/ndfl/reportForm/createReport.html',
                        controller: 'createReportCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    knf: $scope.declarationData
                                };
                            }
                        }
                    }).result.then(function (response) {
                        if (response) {
                            if (response.data && response.data.uuid && response.data.uuid !== null) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            }
                        }
                    });
                };
            }]
        );
}());