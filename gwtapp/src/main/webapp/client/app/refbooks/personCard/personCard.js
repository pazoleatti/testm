(function () {
    'use strict';

    /**
     * @description Модуль для отображения карточки Физлица
     */
    angular.module('app.personCard', ['app.rest', 'app.regPerson'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('personCard', {
                url: '/personRegistry/personCard/{id}',
                templateUrl: 'client/app/refbooks/personCard/personCard.html',
                controller: 'personCardCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_NSI)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        .controller('personCardCtrl', ['$scope', '$filter', 'RefBookListResource', 'APP_CONSTANTS', '$state', '$http', 'PersonCardResource',
            function ($scope, $filter, RefBookListResource, APP_CONSTANTS, $state, $http, PersonCardResource) {

                $scope.mode = 'VIEW';

                /**
                 * @description Получить данные физлица открытой карточки
                 */
                $scope.dataExtract = function () {
                    var data = PersonCardResource.query({
                            id: $state.params.id
                        });
                    return data;
                };

                /**
                 * @description Объект подгруженного Физлица из рееестра Физлиц
                 */
                $scope.person = $scope.dataExtract();

                console.log($scope.person)

                /**
                 * @description Флаг принимающий значение {@code true} если Физлицо было отредактировано
                 * @type {boolean}
                 */
                $scope.changed = false;

                /**
                 * @description Наблюдает за изменением состояния физлица
                 */
                $scope.$watch("person", function (newValue, oldValue) {
                    if (newValue != oldValue) {
                        $scope.changed = true;
                    }
                });

                $scope.$watch("person.reportDoc.permission", function (newValue, oldValue) {
                    if (newValue != oldValue) {
                        $scope.idDocTab.disabled = !newValue
                    }
                });

                /**
                 * @description Грид ДУЛ
                 */
                $scope.idDocsGrid = {
                    ctrl: {},
                    options: {
                        datatype: "local",
                        data: $scope.person.idDocs,
                        colNames: [
                            '',
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.idDocCode'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.idDocNumber')
                        ],
                        colModel: [
                            {name: 'id', width: 100, key: true, hidden: true},
                            {name: 'DOC_ID', width: 380, formatter: $filter('idDocCodeFormatter')},
                            {
                                name: 'DOC_NUMBER',
                                width: 240,
                                formatter: $filter('simpleRefBookValueFormatter'),
                                sortable: false
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: true,
                        sortname: 'id',
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: false
                    }
                };

                /**
                 * @description Грид ИНП
                 */
                $scope.inpListGrid = {
                    ctrl: {},
                    options: {
                        datatype: "local",
                        data: $scope.inpList,
                        colNames: [
                            '',
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.asnu'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.inp')
                        ],
                        colModel: [
                            {name: 'id', width: 100, key: true, hidden: true},
                            {name: 'AS_NU.referenceObject', width: 380, formatter: $filter('asnuFormatter')},
                            {
                                name: 'INP',
                                width: 240,
                                formatter: $filter('simpleRefBookValueFormatter'),
                                sortable: false
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: true,
                        sortname: 'id',
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: false
                    }
                };

                /**
                 * @description Грид дубликатов
                 */
                $scope.duplicatesGrid = {
                    ctrl: {},
                    options: {
                        datatype: "local",
                        data: $scope.duplicates,
                        colNames: [
                            '',
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.personId'),
                            $filter('translate')('title.lastName'),
                            $filter('translate')('title.firstName'),
                            $filter('translate')('title.middleName'),
                            $filter('translate')('title.dateOfBirth'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.idDocType'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.idDocNumberExpanded'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.innRF'),
                            $filter('translate')('title.snils')
                        ],
                        colModel: [
                            {name: 'id', index: 'id', width: 100, key: true, hidden: true},
                            {name: 'oldId', index: 'oldId', width: 100, sortable: true},
                            {name: 'lastName', index: 'lastName', width: 120, sortable: true},
                            {name: 'firstName', index: 'firstName', width: 120, sortable: true},
                            {name: 'middleName', index: 'middleName', width: 120, sortable: true},
                            {
                                name: 'birthDate',
                                index: 'birthDate',
                                width: 80,
                                formatter: $filter('dateFormatter'),
                                sortable: true
                            },
                            {name: 'reportDoc', width: 300, sortable: true, formatter: $filter('idDocTypeFormatter')},
                            {name: 'reportDoc', width: 120, sortable: true, formatter: $filter('idDocNumberFormatter')},
                            {name: 'inn', index: 'inn', width: 120, sortable: true, formatter: $filter('simplePermissiveFormatter')},
                            {name: 'snils', index: 'snils', width: 120, sortable: true, formatter: $filter('simplePermissiveFormatter')}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: true,
                        sortname: 'id',
                        sortorder: "asc",
                        hidegrid: false
                    }
                };

                /**
                 * @description Грид тербанков
                 */
                $scope.tbListGrid = {
                    ctrl: {},
                    options: {
                        datatype: "local",
                        data: $scope.tbList,
                        colNames: [
                            '',
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.tb'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.date')
                        ],
                        colModel: [
                            {name: 'id', width: 100, key: true, hidden: true},
                            {name: 'TB_DEPARTMENT_ID.referenceObject.NAME.value', width: 450},
                            {
                                name: 'IMPORT_DATE.value',
                                width: 100,
                                formatter: $filter('dateFormatter'),
                                sortable: false
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: true,
                        sortname: 'id',
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: false
                    }
                };

                /**
                 * @description Грид истории изменений
                 */
                $scope.changelogGrid = {
                    ctrl: {},
                    options: {
                        datatype: "local",
                        data: $scope.changelogGrid,
                        colNames: [
                            '',
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.event'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.dateAndTime'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.description'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.userName'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.userRoles'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.userDepartment')
                        ],
                        colModel: [
                            {name: 'id', width: 100, key: true, hidden: true},
                            {name: 'event', width: 120},
                            {name: 'dateAndTime', width: 100},
                            {name: 'description', width: 120},
                            {name: 'userName', width: 400},
                            {name: 'userRoles', width: 220},
                            {name: 'userDepartment', width: 240}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: true,
                        sortname: 'id',
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: false
                    }
                };


                $scope.personRegTabsCtrl = {};

                /**
                 * @description Таб физического лица
                 */
                $scope.personTab = {
                    title: $filter('translate')('refBook.fl.card.tabs.title.person'),
                    contentUrl: 'client/app/refbooks/personCard/tabs/personTab.html',
                    fetchTab: true,
                    active: true
                };

                /**
                 * @description Таб ДУЛ
                 */
                $scope.idDocTab = {
                    title: $filter('translate')('refBook.fl.card.tabs.title.idDoc'),
                    contentUrl: 'client/app/refbooks/personCard/tabs/idDocTab.html',
                    fetchTab: false,
                    active: false
                };



                /**
                 * @description Таб ИНП
                 */
                $scope.inpTab = {
                    title: $filter('translate')('refBook.fl.card.tabs.title.inp'),
                    contentUrl: 'client/app/refbooks/personCard/tabs/inpTab.html',
                    fetchTab: true,
                    active: false
                };

                /**
                 * @description Таб дубликатов
                 */
                $scope.duplicatesTab = {
                    title: $filter('translate')('refBook.fl.card.tabs.title.duplicates'),
                    contentUrl: 'client/app/refbooks/personCard/tabs/duplicatesTab.html',
                    fetchTab: true,
                    active: false,
                    params: {
                        mode: $scope.mode,
                        personId: $scope.person.id
                    }
                };

                /**
                 * @description Таб тербанков
                 */
                $scope.tbTab = {
                    title: $filter('translate')('refBook.fl.card.tabs.title.terbanks'),
                    contentUrl: 'client/app/refbooks/personCard/tabs/tbTab.html',
                    fetchTab: true,
                    active: false
                };

                /**
                 * @description Таб истории изменений
                 */
                $scope.changelogTab = {
                    title: $filter('translate')('refBook.fl.card.tabs.title.changelog'),
                    contentUrl: 'client/app/refbooks/personCard/tabs/changelogTab.html',
                    fetchTab: true,
                    active: false
                };

                $scope.personRegTabs = [$scope.personTab, $scope.idDocTab, $scope.inpTab, $scope.duplicatesTab, $scope.tbTab, $scope.changelogTab];

                /**
                 * @description Получение оригинала Физлица
                 */
                $scope.$watch("duplicatesTab.active", function (newValue, oldValue) {
                    if ($scope.person.oldId !== $scope.person.recordId) {
                        if (newValue && !oldValue) {
                            $http({
                                method: "GET",
                                url: "controller/actions/refBookFL/fetchOriginal/" + $scope.person.id
                            }).success(function (response) {
                                $scope.original = response
                            });
                        }
                    }
                });

                /**
                 * @description Получение списка ДУЛ для ФЛ
                 */
                $scope.fetchIdDocs = function (ctrl) {
                    var page = ctrl.getGrid().jqGrid('getGridParam', 'page');
                    var rows = ctrl.getGrid().jqGrid('getGridParam', 'rowNum');
                    $http({
                        method: "GET",
                        url: "controller/actions/refBookFL/fetchIdDocs/" + $scope.person.recordId,
                        params: {
                            pagingParams: JSON.stringify({
                                page: page,
                                count: rows,
                                startIndex: page === 1 ? 0 : rows * (page - 1)
                            })
                        }
                    }).success(function (response) {
                        $scope.idDocs = response.rows;
                        $scope.idDocsGrid.ctrl.refreshGridData($scope.idDocs);
                    });
                };

                /**
                 * @description Получение ИНП для ФЛ
                 */
                $scope.fetchInp = function (ctrl) {
                    var page = ctrl.getGrid().jqGrid('getGridParam', 'page');
                    var rows = ctrl.getGrid().jqGrid('getGridParam', 'rowNum');
                    $http({
                        method: "GET",
                        url: "controller/actions/refBookFL/fetchInp/" + $scope.person.recordId,
                        params: {
                            pagingParams: JSON.stringify({
                                page: page,
                                count: rows,
                                startIndex: page === 1 ? 0 : rows * (page - 1)
                            })
                        }
                    }).success(function (response) {
                        $scope.inpList = response.rows;
                        $scope.inpListGrid.ctrl.refreshGridData($scope.inpList);
                    });
                };

                /**
                 * Получение списка дубликатов для ФЛ
                 */
                $scope.fetchDuplicates = function (ctrl) {
                    if ($scope.person.oldId === $scope.person.recordId) {
                        var page = ctrl.getGrid().jqGrid('getGridParam', 'page');
                        var rows = ctrl.getGrid().jqGrid('getGridParam', 'rowNum');
                        $http({
                            method: "GET",
                            url: "controller/actions/refBookFL/fetchDuplicates/" + $scope.person.id,
                            params: {
                                pagingParams: JSON.stringify({
                                    page: page,
                                    count: rows,
                                    startIndex: page === 1 ? 0 : rows * (page - 1)
                                })
                            }
                        }).success(function (response) {
                            $scope.duplicates = response.rows;
                            $scope.duplicatesGrid.ctrl.refreshGridData($scope.duplicates);
                        });
                    }
                };

                /**
                 * @description Получение списка Тербанков для Физлица
                 */
                $scope.fetchTb = function (ctrl) {
                    var page = ctrl.getGrid().jqGrid('getGridParam', 'page');
                    var rows = ctrl.getGrid().jqGrid('getGridParam', 'rowNum');
                    $http({
                        method: "GET",
                        url: "controller/actions/refBookFL/fetchTb/" + $scope.person.recordId,
                        params: {
                            pagingParams: JSON.stringify({
                                page: page,
                                count: rows,
                                startIndex: page === 1 ? 0 : rows * (page - 1)
                            })
                        }
                    }).success(function (response) {
                        $scope.tbList = response.rows;
                        $scope.tbListGrid.ctrl.refreshGridData($scope.tbList);
                    });
                };

                $scope.editMode = function () {
                    $scope.mode = 'EDIT';
                };

                $scope.save = function () {

                }

            }
        ]);
}());