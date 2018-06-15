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
            'app.returnToCreatedDialog'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndfl', {
                url: '/taxes/ndfl/{declarationDataId}?uuid',
                templateUrl: 'client/app/taxes/ndfl/ndfl.html?v=${buildUuid}',
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
            'checkDeclarationData', 'moveToCreatedDeclarationData', 'Upload',
            function ($scope, $timeout, $window, $stateParams, $showToDoDialog, $http, DeclarationDataResource, $filter,
                      $logPanel, $aplanaModal, $dialogs, $rootScope, RefBookValuesResource, APP_CONSTANTS, $state,
                      $interval, acceptDeclarationData, checkDeclarationData, moveToCreatedDeclarationData, Upload) {

                if ($stateParams.uuid) {
                    $logPanel.open('log-panel-container', $stateParams.uuid);
                }

                /**
                 * @description Инициализация первичных данных на странице
                 */
                $scope.updateDeclarationInfo = function updateDeclarationInfo() {
                    DeclarationDataResource.query({
                            declarationDataId: $stateParams.declarationDataId,
                            projection: "declarationData",
                            nooverlay: true
                        },
                        function (data) {
                            if (data) {
                                var isRefreshGridNeeded = false;
                                if ($scope.declarationData && $scope.declarationData.actualDataDate &&
                                    $scope.declarationData.actualDataDate < data.lastDataModifiedDate) {
                                    isRefreshGridNeeded = true;
                                }
                                $scope.declarationData = data;
                                $scope.declarationDataId = $stateParams.declarationDataId;
                                if (isRefreshGridNeeded) {
                                    $scope.refreshGrid(1);
                                }
                            }
                        },
                        function (e) {
                            if (e.status === 403) {
                                $dialogs.errorDialog({
                                    content: $filter('translate')('ndfl.not.access')
                                });
                            }
                        }
                    );
                };

                $scope.updateDeclarationInfoPeriodically = function() {
                    if (angular.isDefined($scope.stop)) {
                        return;
                    }
                    $scope.updateDeclarationInfo();
                    $scope.stop = $interval($scope.updateDeclarationInfo, 3000);
                };

                $scope.$on('$destroy', function() {
                    if (angular.isDefined($scope.stop)) {
                        $interval.cancel($scope.stop);
                        $scope.stop = undefined;
                    }
                });

                $scope.updateDeclarationInfoPeriodically();

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
                                $scope.availableExcelTemplate = data.downloadExcelTemplateAvailable;
                                if (!$scope.intervalId) {
                                    $scope.intervalId = $interval(function () {
                                        updateAvailableReports();
                                    }, 10000);
                                }
                            }
                        }
                    );
                }

                updateAvailableReports();

                $rootScope.$on("$locationChangeStart", function () {
                    $interval.cancel($scope.intervalId);
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
                    contentUrl: 'client/app/taxes/ndfl/ndflTabs/personsTab.html?v=${buildUuid}',
                    fetchTab: true,
                    active: true
                };
                $scope.incomesAndTaxTab = {
                    title: $filter('translate')('tab.ndfl.informationOnIncomesAndNdfl'),
                    contentUrl: 'client/app/taxes/ndfl/ndflTabs/incomesTab.html?v=${buildUuid}',
                    fetchTab: true
                };
                $scope.deductionsTab = {
                    title: $filter('translate')('tab.ndfl.informationOnDeductions'),
                    contentUrl: 'client/app/taxes/ndfl/ndflTabs/deductionsTab.html?v=${buildUuid}',
                    fetchTab: true
                };
                $scope.prepaymentTab = {
                    title: $filter('translate')('tab.ndfl.informationOnAdvancePayments'),
                    contentUrl: 'client/app/taxes/ndfl/ndflTabs/prepaymentsTab.html?v=${buildUuid}',
                    fetchTab: true
                };
                $scope.ndflTabs = [$scope.ndfFLTab, $scope.incomesAndTaxTab, $scope.deductionsTab, $scope.prepaymentTab];

                $scope.refreshGrid = function (page) {
                    $scope.ndflTabs.forEach(function (tab) {
                        tab.isDataLoaded = false;
                    });
                    $scope.ndflFilter = getNdflFilter();
                    $scope.ndflTabsCtrl.getActiveTab().refreshGrid(page);
                };

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {person: {}, income: {}, deduction: {}, prepayment: {}},
                    filterName: 'ndflFilterForDec' + $stateParams.declarationDataId
                };

                $scope.ndflFilter = getNdflFilter();

                /**
                 * @description Поиск по фильтру
                 */
                $scope.submitSearch = function () {
                    $scope.refreshGrid(1);
                };

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
                    filter.income.urmList = filter.income.urmList ? filter.income.urmList.map(function(urm) { return urm.enumName; }) : undefined;
                    return filter;
                }

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
                 * @description Обработать результат операций "Идентифицировать ФЛ" и "Консолидировать"
                 */
                function calculateResult(response, force, cancelTask) {
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
                }

                /**
                 * Флаг, означающий, может ли текущий пользоватеть выполнить редактирование строки в таблице
                 * Зависит от выделенных строк на вкладках, поэтому реализовано через события
                 */
                $scope.canEditRow = false;
                $rootScope.$on("selectedRowCountChanged", function(event, count){
                    $scope.canEditRow = count === 1
                });

                /**
                 * Событие, которое возникает по нажатию на кнопку "Редактировать строку"
                 */
                $scope.showEditRowModal = function () {
                    var row = $scope.ndflTabsCtrl.getActiveTab().getRows()[0];

                    //Раздел 2 (Сведения о доходах и НДФЛ)
                    var title = "incomesAndTax.edit.title";
                    var templateUrl = "client/app/taxes/ndfl/editNdflIncomesAndTax.html?v=${buildUuid}";
                    var controller = "editNdflIncomesAndTaxFormCtrl";

                    if ($scope.ndflTabsCtrl.getActiveTab().getSection() === 3) {
                        //Раздел 3 (Сведения о вычетах)
                        title = "ndflDeduction.edit.title";
                        templateUrl = "client/app/taxes/ndfl/editNdflDeduction.html?v=${buildUuid}";
                        controller = "editNdflDeductionFormCtrl";
                    } else if ($scope.ndflTabsCtrl.getActiveTab().getSection() === 4) {
                        //Раздел 4 (Сведения о доходах в виде авансовых платежей)
                        title = "ndlfPrepayment.edit.title";
                        templateUrl = "client/app/taxes/ndfl/editNdflPrepayment.html?v=${buildUuid}";
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
                        closeCallback: function(scope) {
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
                $scope.identify = function (force, cancelTask) {
                    {
                        force = typeof force !== 'undefined' ? force : false;
                        cancelTask = typeof cancelTask !== 'undefined' ? cancelTask : false;
                    }
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/identify",
                        params: {
                            force: force ? force : false,
                            cancelTask: cancelTask ? cancelTask : false
                        }
                    }).then(function (response) {
                        calculateResult(response, force, cancelTask);
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Консолидировать"
                 */
                $scope.consolidate = function (force, cancelTask) {
                    {
                        force = typeof force !== 'undefined' ? force : false;
                        cancelTask = typeof cancelTask !== 'undefined' ? cancelTask : false;
                    }
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/consolidate",
                        params: {
                            force: force ? force : false,
                            cancelTask: cancelTask ? cancelTask : false
                        }
                    }).then(function (response) {
                        calculateResult(response, force, cancelTask);
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
                                $scope.updateDeclarationInfo();
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
                            $scope.updateDeclarationInfo();
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
                            } else if (response.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.NOT_EXIST_XML) {
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
                                    $scope.updateDeclarationInfo();
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
                            }).then(function (response) {
                                //Обновить страницу и, если есть сообщения, показать их
                                var params = (response.data && response.data.uuid && response.data.uuid !== null) ? {uuid: response.data.uuid} : {};
                                $state.go("ndflJournal", params, {reload: true});
                            });
                        }
                    });
                };

                /**
                 * Возможность выгружать шаблон Excel-файла для загрузки
                 */
                $scope.canCreateExcelTemplate = function () {
                    return $scope.declarationData && $scope.declarationData.declarationFormKind === APP_CONSTANTS.NDFL_DECLARATION_KIND.PRIMARY.name;
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
                $scope.doImport = function (file, force) {
                    if (file) {
                        Upload.upload({
                            url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/import",
                            data: {uploader: file},
                            params: {force: !!force}
                        }).progress(function (e) {
                        }).then(function (response) {
                            if (response.data.uuid && response.data.uuid !== null) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            } else {
                                if (response.data.status === APP_CONSTANTS.CREATE_ASYNC_TASK_STATUS.LOCKED) {
                                    $dialogs.confirmDialog({
                                        title: $filter('translate')('title.confirm'),
                                        content: response.data.restartMsg,
                                        okBtnCaption: $filter('translate')('common.button.yes'),
                                        cancelBtnCaption: $filter('translate')('common.button.no'),
                                        okBtnClick: function () {
                                            $scope.doImport(file, true);
                                        }
                                    });
                                }
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
                            $scope.updateDeclarationInfo();
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

                $scope.downloadXml = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xml";
                };
                $scope.downloadXlsx = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/xlsx";
                };
                $scope.downloadSpecific = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/rnu_ndfl_person_all_db";
                };
                $scope.downloadPairKppOktmo = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/specific/" + APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.REPORT_KPP_OKTMO;
                };
                $scope.downloadExcelTemplate = function () {
                    $window.location = "controller/rest/declarationData/" + $stateParams.declarationDataId + "/excelTemplate";
                };

                $scope.createReportXlsx = function (force) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/reportXsls",
                        params: {
                            force: force ? force : false
                        }
                    }).success(function (response) {
                        performReportSuccessResponse(response, $scope.createReportXlsx, "availableXlsxReport");
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
                        performReportSuccessResponse(response, $scope.createReportAllRnu, "availableRnuNdflPersonAllDb");
                    });
                };

                /**+
                 * Создание спецотчета "Реестр сформированной отчетности"
                 * @param force
                 */
                $scope.createPairKppOktmo = function (force) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/pairKppOktmoReport",
                        params: {
                            force: force
                        }
                    }).success(function (response) {
                        performReportSuccessResponse(response, $scope.createPairKppOktmo, "availableReportKppOktmo");
                    });
                };

                /**
                 * Формирует запрос на создание шаблона Excel-файла для загрузки
                 */
                $scope.createExcelTemplate = function (force) {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $stateParams.declarationDataId + "/excelTemplate",
                        params: {
                            force: !!force
                        }
                    }).then(function (response) {
                        performReportSuccessResponse(response.data, $scope.createExcelTemplate, "availableExcelTemplate");
                    });
                };

            }]
        );
}());