/**
 * Детализация планирования
 */
(function () {
    'use strict';

    angular
        .module('mtsUsim.productionPlanningDetails', [
            'ui.router',
            'aplana.collapse',
            'ui.grid',
            'pascalprecht.translate',
            'ui.grid.pagination',
            'ui.grid.resizeColumns',
            'ui.grid.saveState',
            'ui.grid.selection',
            'ui.grid.autoResize',
            'ui.bootstrap',
            'ui.select',
            'aplana.dialogs',
            'aplana.utils',
            'mtsUsim.widgets',
            'angularFileUpload',
            'aplana.overlay',
            'aplana.alert',
            'ui.bootstrap',
            'ui.bootstrap.modal'
        ])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('planningDetails', {
                url: '/production/planning/{planningId}',
                templateUrl: 'js/production/planning/planningDetails.html',
                controller: 'planningDetailsCtrl',
                params: {
                    planningId: ''
                }
            });
        }])
        /**
         * Контроллер вкладки "Основная информация"
         */
        .controller('planningDetailsCtrl', [
            '$scope', '$state', '$http', '$stateParams', '$log', 'aplanaEntityUtils', '$location', '$planningService', 'APP_CONSTANTS',
            function ($scope, $state, $http, $stateParams, $log, aplanaEntityUtils, $location, $planningService, APP_CONSTANTS) {
                initPage();

                $scope.selectTab = function (tab) {
                    //Инициация события выбора вкладки
                    $scope.$emit('tabSelected', tab);
                    $scope.$broadcast('tabSelected', tab);
                    $scope.$parent.$broadcast('tabSelected', tab);
                };

                $scope.$on('tabSelected',
                    function (event, data) {
                        if (data === 'mainInfo') {

                        }
                    });

                /**
                 * Инициализация первичных данных на странице
                 */
                function initPage() {
                    fetchData();
                }

                /**
                 * Получение данных с сервера
                 */
                function fetchData() {
                    var url = aplanaEntityUtils.commonRestUrl + 'ProductionPlan/' + $stateParams.planningId;
                    return $http.get(url)
                        .then(function (response) {
                            $scope.planning = response.data;
                            setButtonsEnabled();
                        })
                }

                $scope.returnToList = function () {
                    $location.url('/production/planning')
                };

                $scope.selectTab = function (tab) {
                    if ($scope.gridApi) {
                        $scope.gridApi.core.handleWindowResize();
                    }
                    //Инициация события выбора вкладки
                    $scope.$emit('tabSelected', tab);
                    $scope.$broadcast('tabSelected', tab);
                    $scope.$parent.$broadcast('tabSelected', tab);
                };

                /**
                 * Инициализация кнопок
                 */
                function setButtonsEnabled() {
                    //доступно в статусах «Принят в производство» и «Согласован КЦ»
                    $scope.editButtonEnabled = aplanaEntityUtils.isObjectHasValue($scope.planning, 'status.id', [25, 29]);
                    //доступно в статусах «Принят в производство» и «Корректировка»
                    $scope.pointPlanDeliveryDateButtonEnabled = aplanaEntityUtils.isObjectHasValue($scope.planning, 'status.id', [25, 31]);
                    //доступно в статусе «Согласован КЦ», статусе производства «В производстве»
                    $scope.pointReadyDateButtonEnabled = aplanaEntityUtils.isObjectHasValue($scope.planning, 'status.id', [29])
                        && aplanaEntityUtils.isObjectHasValue($scope.planning, 'planState.id', [1]);
                    //доступно в статусах «Принят в производство» и «Корректировка»
                    $scope.forwardButtonEnabled = aplanaEntityUtils.isObjectHasValue($scope.planning, 'planDeliveryDate')
                        && aplanaEntityUtils.isObjectHasValue($scope.planning, 'priorityDate')
                        && aplanaEntityUtils.isObjectHasValue($scope.planning, 'status.id', [APP_CONSTANTS.ENTITY_STATUS.PLANNING_TO_PRODUCTION.id, APP_CONSTANTS.ENTITY_STATUS.PLANNING_CORRECTION.id])
                        && aplanaEntityUtils.isObjectHasValue($scope.planning, 'planState.id', [APP_CONSTANTS.PLAN_STATE.IN_PRODUCTION.id]);
                    //доступно в статусе «Согласован КЦ», статусе производства «В производстве»
                    $scope.queryDeliveryDateButtonEnabled = aplanaEntityUtils.isObjectHasValue($scope.planning, 'status.id', [29])
                        && aplanaEntityUtils.isObjectHasValue($scope.planning, 'planState.id', [1]);
                    //доступно в статусе «Согласован КЦ», статусе производства «В производстве» и указана «Дата готовности комплектов»
                    $scope.doneButtonEnabled = aplanaEntityUtils.isObjectHasValue($scope.planning, 'status.id', [29])
                        && aplanaEntityUtils.isObjectHasValue($scope.planning, 'planState.id', [1])
                        && aplanaEntityUtils.isObjectHasValue($scope.planning, 'readyDate');
                    //доступно в статусе «На согласовании», статусе производства «В производстве»
                    $scope.approveBERButtonEnabled = aplanaEntityUtils.isObjectHasValue($scope.planning, 'status.id', [26])
                        && aplanaEntityUtils.isObjectHasValue($scope.planning, 'planState.id', [1]);
                    //доступно в статусе ««Запрос на корректировку», статусе производства «В производстве»
                    $scope.confirmDeliveryDateButtonEnabled = aplanaEntityUtils.isObjectHasValue($scope.planning, 'status.id', [30])
                        && aplanaEntityUtils.isObjectHasValue($scope.planning, 'planState.id', [1]);
                    //доступно в статусе «На согласовании» и статусе производства «В производстве»
                    $scope.returnButtonEnabled = aplanaEntityUtils.isObjectHasValue($scope.planning, 'planState.id', [1])
                        && aplanaEntityUtils.isObjectHasValue($scope.planning, 'status.id', [26]);
                    //доступно в статусе ««Запрос на корректировку», статусе производства «В производстве»
                    $scope.rejectDeliveryDateButtonEnabled = aplanaEntityUtils.isObjectHasValue($scope.planning, 'status.id', [30])
                        && aplanaEntityUtils.isObjectHasValue($scope.planning, 'planState.id', [1]);
                }

                /**
                 * Обработчики нажатий на кнопки
                 */
                //Редактирование (установка приоритета)
                $scope.editButtonClick = function () {
                    $planningService.doEdit([$scope.planning])
                        .then(fetchData);
                };
                //Указать (плановую дату отгрузки)
                $scope.pointPlanDeliveryDateButtonClick = function () {
                    $planningService.doPointPlanDelivery([$scope.planning])
                        .then(fetchData);
                };
                //Отправить (на согласование)
                $scope.forwardButtonClick = function () {
                    $planningService.doForwardToApprove([$scope.planning])
                        .then(fetchData);
                };
                //Запросить (корректировку плановой даты отгрузки)
                $scope.queryDeliveryDateButtonClick = function () {
                    $planningService.doQueryDeliveryDate([$scope.planning])
                        .then(fetchData);
                };
                //Подтвердить (корректировку плановой даты отгрузки)
                $scope.confirmDeliveryDateButtonClick = function () {
                    $planningService.doConfirmDeliveryDate([$scope.planning])
                        .then(fetchData);
                };
                //Отказать (в корректировке плановой даты отгрузки)
                $scope.rejectDeliveryDateButtonClick = function () {
                    $planningService.doRejectDeliveryDate([$scope.planning])
                        .then(fetchData);
                };
                //Выполнить
                $scope.doneButtonClick = function () {
                    $planningService.doDone([$scope.planning])
                        .then(fetchData);
                };
                //Указать (дату готовности комплектов)
                $scope.pointReadyDateButtonClick = function () {
                    $planningService.doPointReadyDate([$scope.planning])
                        .then(fetchData);
                };
                //Вернуть (на корректировку)
                $scope.returnButtonClick = function () {
                    $planningService.doReturn($scope.selectedItems)
                        .then(fetchData);
                };
                //Согласовать КЦ
                $scope.approveBERButtonClick = function () {
                    $planningService.approveBER([$scope.planning])
                        .then(fetchData);
                };
            }])
        /**
         * Контроллер вкладки "История"
         */
        .controller('planningDetailsHistoryCtrl', [
            '$scope', '$state', '$http', '$stateParams', '$log', 'aplanaEntityUtils', 'aplanaDialogs', 'APP_CONSTANTS', '$timeout', 'USER_DATA',
            function ($scope, $state, $http, $stateParams, $log, aplanaEntityUtils, aplanaDialogs, APP_CONSTANTS, $timeout, USER_DATA) {

                $scope.$on('tabSelected',
                    function (event, data) {
                        if (data === 'history') {
                            fetchData();
                        }
                    });

                // Параметры отображения данных таблицы
                $scope.dataOptions = {
                    paging: {pageNumber: 1, pageSize: Number(USER_DATA.paging)},
                    sort: null,
                    filter: {},
                    metaData: null // Информация о классе отображаемых данных
                };

                $scope.gridOptions = {
                    paginationPageSizes: aplanaEntityUtils.getPageSizes(),
                    paginationPageSize: $scope.dataOptions.paging.pageSize,
                    rowSelection: true,
                    useExternalPagination: true,
                    useExternalSorting: true,
                    enableFullRowSelection: true,
                    multiSelect: true,
                    modifierKeysToMultiSelect: true,
                    columnDefs: [],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                        $scope.gridApi.core.on.sortChanged($scope, function (grid, sortColumns) {
                            $scope.dataOptions.sort = sortColumns;
                            fetchData()
                        });
                        $scope.gridApi.pagination.on.paginationChanged($scope, function (pageNumber, pageSize) {
                            if ($scope.dataOptions.paging.pageSize != pageSize) {
                                // В случае изменения значения параметра "Кол-во записей на странице" переходим на 1-ю страницу
                                $scope.dataOptions.paging.pageNumber = 1;
                                $scope.gridOptions.paginationCurrentPage = 1
                            } else {
                                $scope.dataOptions.paging.pageNumber = pageNumber
                            }
                            $scope.dataOptions.paging.pageSize = pageSize;
                            fetchData()
                        });
                        $scope.gridApi.selection.on.rowSelectionChanged($scope, function (row, evt) {
                            // Вызывается при выделении(снятии выделение) строки
                            aplanaEntityUtils.setCurrentRow($scope.gridApi.grid.selection.lastSelectedRow, $scope);
                        });
                        $scope.gridApi.selection.on.rowSelectionChangedBatch($scope, function (rows, evt) {
                            // Вызывается когда выделяются(снимается выделение) все строки щелчком по заголовку таблицы
                            aplanaEntityUtils.setCurrentRow($scope.gridApi.grid.selection.lastSelectedRow, $scope);
                        });

                        //Настройка переноса текста в таблице
                        $scope.rowsRenderedTimeout = undefined;
                        var heightRowsChanged = [];
                        $scope.gridApi.core.on.rowsRendered($scope, function () {
                            // each rows rendered event (init, filter, pagination, tree expand)
                            // Timeout needed : multi rowsRendered are fired, we want only the last one
                            if ($scope.rowsRenderedTimeout) {
                                $timeout.cancel($scope.rowsRenderedTimeout)
                            }
                            $scope.rowsRenderedTimeout = $timeout(function () {
                                heightRowsChanged = aplanaEntityUtils.calculateAutoHeight('', $scope.gridApi.grid, heightRowsChanged);
                            });
                        });
                        $scope.gridApi.core.on.scrollEnd($scope, function () {
                            heightRowsChanged = aplanaEntityUtils.calculateAutoHeight('', $scope.gridApi.grid, heightRowsChanged);
                        });
                    }
                };

                initPage();

                $scope.$on('tabSelected',
                    function (event, data) {
                        if (data === 'history') {
                            fetchData();
                        }
                    });

                /**
                 * Инициализация первичных данных на странице
                 */
                function initPage() {
                    $scope.dataOptions.filterList = {};
                    var url = 'rest/service/status/getTransitions/PLANNING';
                    $http.get(url)
                        .then(function (response) {
                            $scope.dataOptions.filterList.transitions = [];
                            var allTransitions = response.data;
                            var transition = undefined;
                            var from = [APP_CONSTANTS.ENTITY_STATUS.PLANNING_TO_PRODUCTION, APP_CONSTANTS.ENTITY_STATUS.PLANNING_ON_APPROVAL, APP_CONSTANTS.ENTITY_STATUS.PLANNING_APPROVED_BER, APP_CONSTANTS.ENTITY_STATUS.PLANNING_QUERY_TO_CORRECT, APP_CONSTANTS.ENTITY_STATUS.PLANNING_CORRECTION];
                            var to = [APP_CONSTANTS.ENTITY_STATUS.PLANNING_TO_PRODUCTION, APP_CONSTANTS.ENTITY_STATUS.PLANNING_ON_APPROVAL, APP_CONSTANTS.ENTITY_STATUS.PLANNING_APPROVED_BER, APP_CONSTANTS.ENTITY_STATUS.PLANNING_QUERY_TO_CORRECT, APP_CONSTANTS.ENTITY_STATUS.PLANNING_CORRECTION, APP_CONSTANTS.ENTITY_STATUS.PLANNING_PERFORMED];
                            for (var i = 0; i < allTransitions.length; i++) {
                                transition = allTransitions[i];
                                if (checkStatus(from, transition.from.id) && checkStatus(to, transition.to.id)) {
                                    if (transition.to.id === APP_CONSTANTS.ENTITY_STATUS.PLANNING_CORRECTION.id) {
                                        if (!(transition.from.id === APP_CONSTANTS.ENTITY_STATUS.PLANNING_TO_PRODUCTION.id
                                            || transition.from.id === APP_CONSTANTS.ENTITY_STATUS.PLANNING_ON_APPROVAL.id
                                            || transition.from.id === APP_CONSTANTS.ENTITY_STATUS.PLANNING_APPROVED_BER.id)) {
                                            $scope.dataOptions.filterList.transitions.push(transition);
                                        }
                                    } else {
                                        $scope.dataOptions.filterList.transitions.push(transition);
                                    }
                                }
                            }
                        });
                }

                function checkStatus(allStatuses, status) {
                    for (var i = 0; i < allStatuses.length; i++) {
                        if (status === allStatuses[i].id) {
                            return true;
                        }
                    }
                    return false;
                }

                /**
                 * Получение данных с сервера
                 */
                function fetchData() {
                    var transition = $scope.dataOptions.filter.transition;
                    if (transition && transition.to) {
                        // statusFrom в случае если пустой, не отправляется на сервер
                        $scope.dataOptions.filter.statusFrom = transition.from == null ? null : transition.from.id;
                        $scope.dataOptions.filter.statusTo = transition.to.id;
                    }
                    return aplanaEntityUtils.fetchData('rest/service/status/getStatusHistory/PLANNING/' + $stateParams.planningId, $scope);
                }

                /**
                 * Действие по кнопке "Найти"
                 */
                $scope.searchHistoryClick = function () {
                    fetchData()
                };
                /**
                 * Очистка полей фильтрации
                 */
                $scope.clearFilterClick = function () {
                    $scope.dataOptions.filter = {};
                    fetchData();
                };

                aplanaEntityUtils.restoreFilter($scope, fetchData, "history");
                $scope.$watchCollection('[dataOptions.filter.transition, dataOptions.filter.user, ' +
                    'dataOptions.filter.historyDateFrom, dataOptions.filter.historyDateTo]', function () {
                    aplanaEntityUtils.saveFilter($scope, "history")
                });
            }])
    ;
}());