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
            'app.editNdflDates',
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
            'app.rnuNdflPersonFaceMenu',
            'app.returnToCreatedDialog',
            'app.createNdfl2_6DataReport',
            'app.createExcelTemplateModal'])
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
                                    if (data.existDeclarationData && (data.declarationKindId === APP_CONSTANTS.NDFL_DECLARATION_KIND.PRIMARY.id || data.declarationKindId === APP_CONSTANTS.NDFL_DECLARATION_KIND.CONSOLIDATED.id)) {
                                        d.resolve();
                                    } else {
                                        d.reject();
                                        var message;
                                        if (data.existDeclarationData) {
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
            '$scope', '$q', '$timeout', '$window', '$stateParams', 'ShowToDoDialog', '$http', 'DeclarationDataResource', '$filter', '$logPanel', '$aplanaModal', '$dialogs',
            '$rootScope', 'RefBookValuesResource', 'APP_CONSTANTS', '$state', '$interval', 'acceptDeclarationData',
            'checkDeclarationData', 'moveToCreatedDeclarationData', 'Upload', 'PermissionChecker', 'CommonFilterUtils',
            function ($scope, $q, $timeout, $window, $stateParams, $showToDoDialog, $http, DeclarationDataResource, $filter,
                      $logPanel, $aplanaModal, $dialogs, $rootScope, RefBookValuesResource, APP_CONSTANTS, $state,
                      $interval, acceptDeclarationData, checkDeclarationData, moveToCreatedDeclarationData, Upload, PermissionChecker,
                      CommonFilterUtils) {

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
                                        oldActualDate = $scope.declarationData.actualDataDate;
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
                                        $scope.declarationData.actualDataDate = oldActualDate;
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

                $scope.getActiveTab = function () {
                    return $scope.ndflTabsCtrl.getActiveTab();
                };

                $scope.refreshGrid = function (page) {
                    $scope.ndflTabs.forEach(function (tab) {
                        tab.isDataLoaded = false;
                    });

                    $scope.ndflTabsCtrl.getActiveTab().refreshGrid(page);
                };

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {
                        person: {},
                        income: {taxRefundCondition: {condition: true}},
                        deduction: {},
                        prepayment: {}
                    },
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
                    $scope.searchFilter.isClear = !(isEmpty($scope.ndflFilter.person) && isEmpty($scope.ndflFilter.income) &&
                        isEmpty($scope.ndflFilter.deduction) && isEmpty($scope.ndflFilter.prepayment));
                };

                // Стирает значение если выбран унарный оператор
                $scope.$watch('searchFilter.params.income.taxRefundCondition.operator', function () {
                    if ($scope.searchFilter.params.income.taxRefundCondition.operator && $scope.searchFilter.params.income.taxRefundCondition.operator.unary) {
                        $scope.searchFilter.params.income.taxRefundCondition.argument2 = undefined;
                    }
                });

                // Возвращяет признак того, что объект незаполнен
                function isEmpty(object) {
                    return CommonFilterUtils.isEmpty(object);
                }

                // Возвращяет признак того, что объект, задающий условие для фильтрации, незаполнен
                function isFilterConditionEmpty(filterCondition) {
                    return !filterCondition.operator || !filterCondition.operator.unary && !filterCondition.argument2;
                }

                /**
                 * @description сброс фильтра
                 */
                $scope.searchFilter.resetFilterParams = function () {
                    /* очистка всех инпутов на форме */
                    $scope.searchFilter.params = {
                        person: {},
                        income: {taxRefundCondition: {condition: true}},
                        deduction: {},
                        prepayment: {}
                    };
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
                    if ($scope.searchFilter.disableTaxTransferDate) {
                        filter.income.transferDateFrom = $filter('translate')('title.taxTransferDateZeroDate');
                        filter.income.transferDateTo = $filter('translate')('title.taxTransferDateZeroDate');
                    }
                    if (filter.income.urmList) {
                        filter.income.urmList = filter.income.urmList.map(function (urm) {
                            return urm.enumName;
                        });
                    }
                    if (!isFilterConditionEmpty(filter.income.taxRefundCondition)) {
                        filter.income.taxRefundCondition.operator = filter.income.taxRefundCondition.operator.enumName;
                    } else {
                        filter.income.taxRefundCondition.operator = undefined;
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
                 * @description Формирование рну ндфл для отдельного физ лица
                 */
                $scope.createRnuNdflByPersonReport = function () {
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
                 * @description Формирование рну ндфл для всех ФЛ на основе Меню выбора
                 */
                $scope.createRnuNdflByAllPersonsReportMenu = function () {
                    var row = $scope.ndflTabsCtrl.getActiveTab().getSelectedRows()[0];

                    $aplanaModal.open({
                        title: $filter('translate')('rnuPersonFaceMenu.title'),
                        templateUrl: 'client/app/taxes/ndfl/taxForm/rnuNdflPersonFaceMenu.html',
                        controller: 'rnuNdflPersonFaceMenuFormCtrl',
                        windowClass: 'modal450',
                        resolve: {
                            $shareData: function () {
                                return {
                                    filter: $scope.searchFilter.params,
                                    filterIsClear: !$scope.searchFilter.isClear,
                                    selectedRow: $scope.ndflTabsCtrl
                                };
                            }
                        }
                    });
                };

                /**
                 * Все записи можно редактировать, если выбрана 2 вкладка, и там есть хотя бы 1 строка.
                 */
                $scope.canEditAllRows = function () {
                    return ($scope.ndflTabsCtrl.getActiveTab().getSection() === 2) && ($scope.ndflTabsCtrl.getActiveTab().getRowsCount() > 0);
                };

                /**
                 * Событие, которое возникает по нажатию на кнопку "Редактировать строку"
                 */
                $scope.showEditRowModal = function () {
                    var row = $scope.ndflTabsCtrl.getActiveTab().getSelectedRows()[0];

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
                        title = "ndflPrepayment.edit.title";
                        templateUrl = "client/app/taxes/ndfl/taxForm/editing/editNdflPrepayment.html";
                        controller = "editNdflPrepaymentFormCtrl";
                    }

                    lock().then(function () {
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
                        }).result.then(function (edited) {
                            unlockEdit();

                            if (edited) {
                                $scope.refreshGrid(1);
                            }
                        });
                    });
                };

                // Блокирует форму на редактирование
                function lock() {
                    return $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $scope.declarationDataId + "/lockEdit"
                    }).then(function (response) {
                        if (response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        if (!response.data.success) {
                            return $q.reject();
                        }
                    });
                }

                // Метод снятия блокировки с редактирования формы.
                function unlockEdit() {
                    $http({
                        method: 'POST',
                        url: 'controller/actions/declarationData/' + $stateParams.declarationDataId + '/unlockEdit'
                    }).then(function (unlock) {
                        if (unlock.data.uuid) {
                            $logPanel.open('log-panel-container', unlock.data.uuid);
                        }
                    });
                }

                /**
                 * Событие по пунктам меню "Редактировать даты строк".
                 * @param byFilter boolean режим запуска, по фильтру или по выбранным строкам
                 */
                $scope.checkRowsAndShowModal = function (byFilter) {
                    var selectedRows = $scope.ndflTabsCtrl.getActiveTab().getSelectedRows();
                    var rowsCount = byFilter ? $scope.ndflTabsCtrl.getActiveTab().getRowsCount() : selectedRows.length;

                    if (rowsCount > 0) {
                        $http({
                            method: 'GET',
                            url: 'controller/actions/checkRowsEditCountParam',
                            params: {
                                count: rowsCount
                            }
                        }).then(function (response) {
                            if (response.data.uuid) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            }
                            if (response.data.success === true) {
                                $scope.showBulkEditDatesModal(byFilter);
                            }
                        });
                    }
                };

                /**
                 * Показ окна "Массовое редактирование дат".
                 * @param byFilter режим показа окна, по фильтру или по выбранным строкам
                 */
                $scope.showBulkEditDatesModal = function (byFilter) {
                    var selectedRows = $scope.ndflTabsCtrl.getActiveTab().getSelectedRows();
                    var title = byFilter ?
                        $filter('translate')('incomesAndTax.editDates.byFilter.title') :
                        $filter('translate')('incomesAndTax.editDates.selected.title');

                    lock().then(function () {
                        $aplanaModal.open({
                            title: title,
                            templateUrl: 'client/app/taxes/ndfl/taxForm/editing/editNdflDates.html',
                            controller: "editNdflDatesFormCtrl",
                            windowClass: 'modal500',
                            resolve: {
                                $shareData: function () {
                                    return {
                                        byFilter: byFilter,
                                        declarationId: $stateParams.declarationDataId,
                                        filter: $scope.searchFilter.params,
                                        rowIds: $filter('idExtractor')(selectedRows)
                                    };
                                }
                            },
                            closeCallback: function (scope) {
                                scope.close();
                            }
                        }).result.then(function (edited) {
                            unlockEdit();

                            if (edited) {
                                $scope.refreshGrid(1);
                            }
                        });
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
                                    if (response.data.success) {
                                        //Обновить страницу и, если есть сообщения, показать их
                                        var params = (response.data && response.data.uuid && response.data.uuid !== null) ? {uuid: response.data.uuid} : {};
                                        $state.go("ndflJournal", params, {reload: true});
                                    } else {
                                        $logPanel.open('log-panel-container', response.data.uuid);
                                    }
                                }
                            });
                        }
                    });
                };

                $scope.isEnableButtonDeleteSelectedRow = function () {

                    if ($scope.declarationData.declarationType !== APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_PRIMARY.id) {
                        return;
                    }

                    if (!$scope.permissionChecker.check(
                        $scope.declarationData,
                        APP_CONSTANTS.DECLARATION_PERMISSION.DELETE_ROWS)) {
                        return;
                    }

                    if (!$scope.ndflTabsCtrl.getActiveTab) {
                        return;
                    }
                    var tab = $scope.ndflTabsCtrl.getActiveTab();
                    if (!tab) {
                        return;
                    }
                    var section = tab.getSection && tab.getSection();
                    var count = section === APP_CONSTANTS.NDFL_PERSON_REPORT_ACTIVE_TAB.PERSONS.id ? tab.getGrid &&
                        tab.getGrid() && tab.getGrid().ctrl &&
                        tab.getGrid().ctrl.getCountRecords && tab.getGrid().ctrl.getCountRecords()
                        : tab.getRowsCount && tab.getRowsCount();

                    return section < APP_CONSTANTS.NDFL_PERSON_REPORT_ACTIVE_TAB.DEDUCTIONS.id && count > 0 &&
                        tab.getSelectedRows().length > 0;
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Удалить выбранные строки"
                 */
                $scope.deleteSelectedRows = function () {
                    var tab = $scope.ndflTabsCtrl.getActiveTab();
                    var sectionId = tab.getSection();
                    var rows = tab.getSelectedRows();
                    var data = {
                        declarationDataId: $stateParams.declarationDataId,
                        section: 'SECTION' + sectionId,
                        sectionIds: _.map(rows, function (r) {
                            return r.id;
                        })
                    };

                    $dialogs.confirmDialog({
                        title: $filter('translate')('ndfl.dialog.deleteSelectedConfirmation.title'),
                        content: $filter('translate')('ndfl.dialog.deleteSelectedConfirmation.content', {
                            n: sectionId,
                            info: sectionId === 2 ? "выбранным операциям" : "выбранным ФЛ"
                        }),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $http({
                                method: "POST",
                                url: "controller/actions/declarationData/delete/selected",
                                data: data
                            }).then(function (response) {
                                if (response.data && response.data.uuid && response.data.uuid !== null) {
                                    if (response.data.success) {
                                        //Обновить страницу и, если есть сообщения, показать их
                                        var params = (response.data && response.data.uuid && response.data.uuid !== null) ? {uuid: response.data.uuid} : {};
                                        $state.go("ndflJournal", params, {reload: true});
                                    } else {
                                        $logPanel.open('log-panel-container', response.data.uuid);
                                    }
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
                            data: {uploader: file}
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
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/updatePersonsData"
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
                        windowClass: 'modal1400'
                    });
                };

                $scope.isKnf = function (declarationData) {
                    return isKnf(declarationData.declarationType);
                };

                $scope.isKnfForApp2 = function (declarationData) {
                    return declarationData
                        && isKnf(declarationData.declarationType)
                        && APP_CONSTANTS.KNF_TYPE.FOR_APP2.id === declarationData.knfType.id;
                };

                function isKnf(declarationTypeId) {
                    return APP_CONSTANTS.DECLARATION_TYPE.RNU_NDFL_CONSOLIDATED.id === declarationTypeId;
                }

                $scope.downloadXlsx = function () {
                    $window.open("controller/rest/declarationData/" + $stateParams.declarationDataId + "/xlsx", '_blank');
                };
                $scope.downloadRnuNdflByAllPersonsReport = function () {
                    downloadSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_PERSON_ALL_DB);
                };
                $scope.downloadRateReport = function () {
                    downloadSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_RATE_REPORT);
                };
                $scope.downloadPaymentReport = function () {
                    downloadSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_PAYMENT_REPORT);
                };
                $scope.downloadNdflDetailReport = function () {
                    downloadSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_DETAIL_REPORT);
                };
                $scope.downloadNdfl2_6DataXlsxReport = function () {
                    downloadSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_2_6_DATA_XLSX_REPORT);
                };
                $scope.downloadNdfl2_6DataTxtReport = function () {
                    downloadSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_2_6_DATA_TXT_REPORT);
                };
                $scope.downloadPairKppOktmo = function () {
                    downloadSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.REPORT_KPP_OKTMO);
                };
                $scope.downloadExcelTemplate = function () {
                    $window.open("controller/rest/declarationData/" + $stateParams.declarationDataId + "/excelTemplate", '_blank');
                };

                function downloadSpecificReport(reportCode) {
                    $window.open("controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/" + reportCode, '_blank');
                }

                $scope.createReportXlsx = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/reportXsls"
                    }).success(function (response) {
                        if (response) {
                            $logPanel.open('log-panel-container', response);
                        }
                    });
                };

                /**
                 * формирование спецотчета "РНУ НДФЛ по всем ФЛ"
                 */
                $scope.createRnuNdflByAllPersonsReport = function () {
                    $scope.createSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_PERSON_ALL_DB);
                };

                /**
                 * Создание спецотчета "Отчет Карманниковой: Отчет в разрезе ставок"
                 */
                $scope.createRateReport = function () {
                    $scope.createSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_RATE_REPORT);
                };

                /**
                 * Создание спецотчета "Отчет Карманниковой: Отчет в разрезе платёжных поручений"
                 */
                $scope.createPaymentReport = function () {
                    $scope.createSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_PAYMENT_REPORT);
                };

                /**
                 * Создание спецотчета "Отчет Карманниковой: Отчет в разрезе платёжных поручений"
                 */
                $scope.createNdflDetailReport = function () {
                    $scope.createSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.RNU_NDFL_DETAIL_REPORT);
                };

                /**+
                 * Создание спецотчета "Реестр сформированной отчетности"
                 */
                $scope.createPairKppOktmo = function () {
                    $scope.createSpecificReport(APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.REPORT_KPP_OKTMO);
                };

                $scope.createSpecificReport = function (reportCode) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/specific/" + reportCode
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

                /**
                 * Формирует запрос на создание шаблона Excel-файла для загрузки
                 */
                $scope.createExcelTemplate = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('ndfl.report.excelTemplate.modal.title'),
                        templateUrl: 'client/app/taxes/ndfl/taxForm/createExcelTemplateModal.html',
                        controller: 'createExcelTemplateModalCtrl',
                        windowClass: 'modal450',
                        resolve: {
                            $shareData: function () {
                                return {
                                    selectedRow: $scope.ndflTabsCtrl
                                };
                            }
                        }
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
                        templateUrl: 'client/app/taxes/ndfl/reportForm/createReportForm.html',
                        controller: 'createReportFormCtrl',
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
