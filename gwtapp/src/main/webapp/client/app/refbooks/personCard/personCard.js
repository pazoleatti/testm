(function () {
    'use strict';

    /**
     * @description Модуль для отображения карточки Физлица
     */
    angular.module('app.personCard', ['app.rest', 'app.regPerson', 'app.idDocRecordModal', 'app.personSearch'])
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

        .controller('personCardCtrl', ['$scope', '$rootScope', '$filter', 'RefBookListResource', 'APP_CONSTANTS', '$state', '$http', 'PersonCardResource', '$aplanaModal', '$dialogs', '$logPanel',
            function ($scope, $rootScope, $filter, RefBookListResource, APP_CONSTANTS, $state, $http, PersonCardResource, $aplanaModal, $dialogs, $logPanel) {

                $scope.mode = APP_CONSTANTS.MODE.VIEW;

                $scope.idDocsForDelete = [];
                $scope.editedIdDocs = [];

                $scope.newDuplicates = [];
                $scope.deletedDuplicates = [];

                $scope.doDeleteOriginal = false;

                /**
                 * Флаг указывающий что удаление ДУЛ выполнено
                 * @type {boolean}
                 */
                $scope.idDocsDeleteCompleted = false;

                /**
                 * Флаг указывающий что изменение и создание ДУЛ выполнено
                 * @type {boolean}
                 */
                $scope.idDocsUpdateCompleted = false;

                $scope.personParam = {};

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
                    value: [],
                    options: {
                        datatype: "local",
                        colNames: [
                            '',
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.idDocCode'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.idDocNumber')
                        ],
                        colModel: [
                            {name: 'id.value', width: 100, key: true, hidden: true},
                            {name: 'DOC_ID.referenceObject', width: 380, formatter: $filter('idDocCodeFormatter')},
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
                    value: [],
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
                            {
                                name: 'inn',
                                index: 'inn',
                                width: 120,
                                sortable: true,
                                formatter: $filter('simplePermissiveFormatter')
                            },
                            {
                                name: 'snils',
                                index: 'snils',
                                width: 120,
                                sortable: true,
                                formatter: $filter('simplePermissiveFormatter')
                            }
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
                    if (!$scope.idDocs) {
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
                    } else {
                        ctrl.getGrid().jqGrid('clearGridData');
                        ctrl.getGrid().jqGrid('setGridParam', {data: $scope.idDocs});
                        ctrl.refreshGrid();
                    }
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
                    if (!$scope.duplicates) {
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
                    } else {
                        ctrl.getGrid().jqGrid('clearGridData');
                        ctrl.getGrid().jqGrid('setGridParam', {data: $scope.duplicates});
                        ctrl.refreshGrid();
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

                /**
                 * Перейти в режим редактирования
                 */
                $scope.editMode = function () {
                    $scope.idDocs = null;
                    if ($scope.idDocTab.active) {
                        $scope.fetchIdDocs($scope.idDocsGrid.ctrl)
                    }
                    $scope.mode = APP_CONSTANTS.MODE.EDIT;
                };

                /**
                 * @description Стереть информацию об изменениях в списке ДУЛ
                 */
                var eraseIdDocChangesInfo = function () {
                    $scope.idDocsForDelete = [];
                    $scope.editedIdDocs = [];
                };

                /**
                 * @description Наблюдает обработалась ли информация по ДУЛ и обновляет данные ФЛ. Из-за того что имеем
                 * двустороннюю ссылку у ДУЛ и ФЛ, приходится ждать асинхронного завершения обработки ДУЛ чтобы назначить ссылку на ДУЛ.
                 * При рефакторинге это нужно учесть.
                 */
                $scope.$watchGroup(['idDocsDeleteCompleted', 'idDocsUpdateCompleted'], function (newValues) {
                    if (newValues[0] && newValues[1]) {
                        // Нам нужен идентификатор вновь созданного, но не сохраненного на момент начала операции "save" ДУЛ
                        $http({
                            method: "GET",
                            url: "controller/actions/refBookFL/fetchIdDocs/" + $scope.person.recordId,
                            params: {
                                pagingParams: JSON.stringify({
                                    page: 1,
                                    count: 10000
                                })
                            }
                        }).success(function (response) {
                            angular.forEach(response.rows, function (idDoc) {
                                if (idDoc.DOC_NUMBER && $scope.personParam.reportDoc.value && idDoc.DOC_NUMBER.value === $scope.personParam.reportDoc.value.DOC_NUMBER.value && $scope.personParam.reportDoc && $scope.personParam.reportDoc.value.id.fake) {
                                    $scope.personParam.reportDoc.value = idDoc;
                                }
                            });
                            $http({
                                method: "POST",
                                url: "controller/actions/registryPerson/updatePerson",
                                data: $scope.personParam
                            });
                            eraseIdDocChangesInfo();
                            erasedDuplicatesInfo();
                        });
                        $scope.idDocsDeleteCompleted = false;
                        $scope.idDocsUpdateCompleted = false;
                    }
                });

                /**
                 * @description Сохранить изменения из списка ДУЛ
                 */
                var performIdDocsPersist = function () {
                    if ($scope.idDocsForDelete.length > 0) {
                        $http({
                            method: "POST",
                            url: "controller/actions/refBookFL/deleteIdDocs",
                            data: $scope.idDocsForDelete
                        }).then(function success() {
                            $scope.idDocsDeleteCompleted = true;
                        });
                    } else {
                        $scope.idDocsDeleteCompleted = true;
                    }
                    var size = $scope.idDocs ? $scope.idDocs.length : 0;
                    var completedCounter = 0;
                    angular.forEach($scope.idDocs, function (idDoc) {
                        idDoc.DOC_ID.value = idDoc.DOC_ID.referenceObject.id.value;
                        if ($scope.editedIdDocs.indexOf(idDoc.id.value) != -1 && !idDoc.id.fake) {
                            idDoc.PERSON_ID.value = idDoc.PERSON_ID.referenceObject.id.value;
                            $http({
                                method: "POST",
                                url: "controller/actions/refBook/" + APP_CONSTANTS.REFBOOK.ID_DOC + "/editRecord/" + idDoc.id.value,
                                data: idDoc
                            }).then(function () {
                                completedCounter++;
                                if (size == completedCounter) {
                                    $scope.idDocsUpdateCompleted = true;
                                }
                            });
                        } else if (idDoc.id.fake) {
                            idDoc.DOC_ID.value = idDoc.DOC_ID.referenceObject.id.value;
                            idDoc.id = null;
                            $http({
                                method: "POST",
                                url: "controller/actions/refBook/" + APP_CONSTANTS.REFBOOK.ID_DOC + "/createRecord",
                                data: idDoc
                            }).then(function () {
                                completedCounter++;
                                if (size == completedCounter) {
                                    $scope.idDocsUpdateCompleted = true;
                                }
                            });
                        } else {
                            completedCounter++;
                            if (size == completedCounter) {
                                $scope.idDocsUpdateCompleted = true;
                            }
                        }
                    });
                    if (size == 0) {
                        $scope.idDocsUpdateCompleted = true;
                    }
                };

                /**
                 * @description Стереть информацию о дубликатах
                 */
                var erasedDuplicatesInfo = function () {
                    $scope.newDuplicates = [];
                    $scope.deletedDuplicates = [];
                };

                /**
                 * Сохранить изменения для оригинала и дубликатов
                 */
                var performOriginalAndDuplicatesPersist = function () {
                    var data = {
                        addedOriginalVersionId: $scope.original ? $scope.original.id : null,
                        changingPersonRecordId: $scope.person.recordId,
                        changingPersonOldId: $scope.person.oldId,
                        addedOriginal: $scope.original ? $scope.original.recordId : null,
                        deleteOriginal: $scope.doDeleteOriginal,
                        addedDuplicates: $scope.newDuplicates,
                        deletedDuplicates: $scope.deletedDuplicates
                    };
                    $http({
                        method: "POST",
                        url: "controller/actions/refBookFL/saveOriginalAndDuplicates",
                        data: data
                    });
                };

                /**
                 * @description Сохранить изменения
                 */
                $scope.save = function () {

                    $scope.personParam = $.extend(true, {}, $scope.person);
                    $scope.personParam.vip = $scope.person.vipSelect.value;
                    if ($scope.person.address.value.COUNTRY_ID.referenceObject) {
                        $scope.personParam.address.value.COUNTRY_ID.value = $scope.person.address.value.COUNTRY_ID.referenceObject.id.value;
                    } else {
                        $scope.personParam.address.value.COUNTRY_ID.value = null
                    }
                    var personParamForCheck = $.extend(true, {}, $scope.personParam);
                    personParamForCheck.reportDoc = null;
                    $http({
                        method: "POST",
                        url: "controller/actions/registryPerson/checkVersionOverlapping",
                        data: personParamForCheck
                    }).then(function (response) {
                        if (response.data.error) {
                            $dialogs.errorDialog({content: response.data.error});
                            $scope.cancel();
                        } else {
                            if (response.data.uuid) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                                return;
                            }
                            performIdDocsPersist();
                            performOriginalAndDuplicatesPersist();
                        }
                    });
                    $scope.mode = APP_CONSTANTS.MODE.VIEW;
                };

                /**
                 * Отменить изменения
                 */
                $scope.cancel = function () {
                    if ($scope.idDocTab.active) {
                        $scope.idDocs = null;
                        $scope.fetchIdDocs($scope.idDocsGrid.ctrl)
                    } else if ($scope.inpTab.active) {
                        $scope.fetchInp($scope.inpListGrid.ctrl)
                    } else if ($scope.duplicatesTab.active) {
                        $http({
                            method: "GET",
                            url: "controller/actions/refBookFL/fetchOriginal/" + $scope.person.id
                        }).success(function (response) {
                            $scope.original = response;
                            $scope.doDeleteOriginal = false;
                        });
                        $scope.fetchDuplicates($scope.duplicatesGrid.ctrl)
                    } else if ($scope.tbTab.active) {
                        $scope.fetchTb($scope.tbListGrid.ctrl)
                    }
                    $scope.person = $scope.dataExtract();
                    $scope.mode = APP_CONSTANTS.MODE.VIEW;
                };

                /**
                 * @description Редактировать ДУЛ
                 * @param mode режим редактирования
                 */
                var editIdDoc = function (mode) {
                    var title;
                    var idDoc;
                    switch (mode) {
                        case APP_CONSTANTS.MODE.CREATE:
                            title = $filter('translate')('refBook.fl.card.tabs.idDoc.modal.title.create');
                            idDoc = {
                                id: {fake: true, value: new Date().getTime()},
                                DOC_ID: {attributeType: "REFERENCE"},
                                DOC_NUMBER: {attributeType: "STRING"},
                                PERSON_ID: {attributeType: "REFERENCE", value: $scope.person.id},
                                INC_REP: {attributeType: "NUMBER", value: 0}
                            };

                            break;
                        case APP_CONSTANTS.MODE.EDIT:
                            title = $filter('translate')('refBook.fl.card.tabs.idDoc.modal.title.edit');
                            idDoc = $scope.idDocsGrid.value[0];
                            break;
                    }
                    $aplanaModal.open({
                        title: title,
                        templateUrl: 'client/app/refbooks/personCard/modal/idDocModal.html',
                        controller: 'idDocRecordModalCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    idDoc: idDoc,
                                    mode: mode
                                }
                            }
                        }
                    })
                };

                /**
                 * @description Добавить ДУЛ
                 */
                $scope.addIdDoc = function () {
                    editIdDoc(APP_CONSTANTS.MODE.CREATE)
                };

                /**
                 * @description Изменить ДУЛ
                 */
                $scope.editIdDoc = function () {
                    editIdDoc(APP_CONSTANTS.MODE.EDIT)
                };

                /**
                 * @description Удалить ДУЛ
                 */
                $scope.deleteIdDoc = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('refBook.fl.card.tabs.idDoc.deleteDialog.title'),
                        content: $filter('translate')('refBook.fl.card.tabs.idDoc.deleteDialog.content'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            if (!$scope.idDocsGrid.value[0].id.fake) {
                                $scope.idDocsForDelete.push($scope.idDocsGrid.value[0].id.value);
                            }
                            var i = 0;
                            var deleteIndex = -1;
                            angular.forEach($scope.idDocs, function (item) {
                                if (item.id.value === $scope.idDocsGrid.value[0].id.value) {
                                    deleteIndex = i;
                                }
                                i++;
                            });
                            if (deleteIndex > -1) {
                                $scope.idDocs.splice(deleteIndex, 1);
                                if ($scope.person.reportDoc && $scope.person.reportDoc.value &&
                                    $scope.person.reportDoc.value.id.value === $scope.idDocsGrid.value[0].id.value
                                ) {
                                    $scope.person.reportDoc.value = null;
                                }
                            }
                            $rootScope.$broadcast("addIdDoc", $scope.idDocs, $scope.person);
                            $scope.idDocsGrid.ctrl.refreshGridData($scope.idDocs);
                        }
                    })
                };

                /**
                 * @description Обработка события создания ДУЛ
                 */
                $scope.$on("createIdDoc", function (event, idDoc) {
                    $scope.idDocs.push(idDoc);
                    $rootScope.$broadcast("addIdDoc", $scope.idDocs, $scope.person);
                    $scope.idDocsGrid.ctrl.refreshGridData($scope.idDocs);
                });

                /**
                 * @description Обпработка события изменения ДУЛ
                 */
                $scope.$on("updateIdDoc", function (event, idDoc) {
                    var i = 0;
                    angular.forEach($scope.idDocs, function (item) {
                        if (item.id.value === idDoc.id.value) {
                            $scope.editedIdDocs.push(idDoc.id.value);
                            $scope.idDocs.splice(i, 1, idDoc);
                        }
                        i++;
                    });
                    $scope.idDocsGrid.ctrl.refreshGridData($scope.idDocs);
                });

                /**
                 * Проверяет необходимость заполнения элементов адреса
                 * @param value
                 * @returns {*|boolean}
                 */
                $scope.isAddressRequiredByAddressItems = function (value) {
                    return value || !($scope.person.address.value.APPARTMENT.value ||
                        $scope.person.address.value.POSTAL_CODE.value ||
                        $scope.person.address.value.DISTRICT.value ||
                        $scope.person.address.value.CITY.value ||
                        $scope.person.address.value.LOCALITY.value ||
                        $scope.person.address.value.HOUSE.value ||
                        $scope.person.address.value.BUILD.value)
                };

                /**
                 * Проверяет валидность поля ввода адреса
                 * @param value
                 * @returns {*|boolean}
                 */
                $scope.isAddressValid = function (value) {
                    return value || !$scope.person.address.value.COUNTRY_ID.referenceObject
                };

                /**
                 * Проверяет валидность поля страны проживания
                 * @param value
                 * @returns {*|boolean}
                 */
                $scope.isCountryValid = function (value) {
                    return value || !$scope.person.address.value.ADDRESS.value
                };

                var addPerson = function (title, mode) {
                    $aplanaModal.open({
                        title: title,
                        templateUrl: 'client/app/refbooks/personCard/modal/personSearch.html',
                        controller: 'personSearchCtrl',
                        windowClass: 'modal1200',
                        resolve: {
                            $shareData: function () {
                                return {
                                    id: $scope.person.id,
                                    mode: mode
                                }
                            }
                        }
                    })
                };

                /**
                 * @description Добавить оригинал
                 */
                $scope.addOriginal = function () {
                    addPerson($filter('translate')('refBook.fl.card.tabs.original.modal.title'), APP_CONSTANTS.MODE.ORIGINAL)
                };

                /**
                 * @description Добавить дубликат
                 */
                $scope.addDuplicate = function () {
                    addPerson($filter('translate')('refBook.fl.card.tabs.duplicate.modal.title'), APP_CONSTANTS.MODE.DUPLICATE)
                };

                /**
                 * @description Удалить оригинал
                 */
                $scope.deleteOriginal = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('refBook.fl.card.tabs.original.deleteDialog.title'),
                        content: $filter('translate')('refBook.fl.card.tabs.original.deleteDialog.content'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            $scope.original = null;
                            $scope.doDeleteOriginal = true
                        }
                    })
                };

                /**
                 * @description Удалить дубликат
                 */
                $scope.deleteDuplicate = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('refBook.fl.card.tabs.duplicate.deleteDialog.title'),
                        content: $filter('translate')('refBook.fl.card.tabs.duplicate.deleteDialog.content'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {

                            $scope.deletedDuplicates.push($scope.duplicatesGrid.value[0].oldId);
                            var indexOfDeleting = null;
                            var i = 0;
                            angular.forEach($scope.duplicates, function (item) {
                                if (item.id == $scope.duplicatesGrid.value[0].id) {
                                    indexOfDeleting = i;
                                }
                                i++;
                            });
                            if (indexOfDeleting != null) {
                                $scope.duplicates.splice(indexOfDeleting, 1);
                            }
                            $scope.duplicatesGrid.ctrl.refreshGridData($scope.duplicates);
                        }
                    })
                };

                $scope.$on("addOriginal", function (event, original) {
                    $scope.original = original;
                    $scope.doDeleteOriginal = false;
                });

                $scope.$on("addDuplicate", function (event, duplicate) {
                    $http({
                        method: "GET",
                        url: "controller/rest/personRegistry/fetch/" + duplicate.id
                    }).then(function (response) {
                        $scope.duplicates.push(response.data);
                        $scope.duplicatesGrid.ctrl.refreshGridData($scope.duplicates)
                    });
                    $scope.newDuplicates.push(duplicate.recordId);
                });
            }
        ]);
}());