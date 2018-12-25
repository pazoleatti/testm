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

        .controller('personCardCtrl', ['$scope', '$rootScope', '$filter', 'RefBookListResource', 'LogBusinessResource', 'APP_CONSTANTS', '$state', '$http', 'PersonCardResource', '$aplanaModal', '$dialogs',
            function ($scope, $rootScope, $filter, RefBookListResource, LogBusinessResource, APP_CONSTANTS, $state, $http, PersonCardResource, $aplanaModal, $dialogs) {

                $scope.mode = APP_CONSTANTS.MODE.VIEW;

                $scope.personParam = {};

                // Права на редактирование карточки. Временно только для Контролёра УНП, на расширение заведён баг SBRFNDFL-6008.
                $scope.userHasEditPermission = $rootScope.user.hasRole(APP_CONSTANTS.USER_ROLE.N_ROLE_CONTROL_UNP);

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
                            {name: 'id', width: 100, key: true, hidden: true},
                            {name: 'docType', width: 380, formatter: $filter('codeNameFormatter')},
                            {
                                name: 'documentNumber',
                                width: 240,
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
                            $filter('translate')('refBook.fl.card.tabs.inp.tabColumnHeader.asnu'),
                            $filter('translate')('refBook.fl.card.tabs.inp.tabColumnHeader.inp')
                        ],
                        colModel: [
                            {name: 'id', width: 100, key: true, hidden: true},
                            {name: 'asnu', width: 380, formatter: $filter('codeNameFormatter')},
                            {
                                name: 'inp',
                                width: 240,
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
                            {
                                name: 'lastName',
                                index: 'lastName',
                                width: 120,
                                sortable: true,
                                formatter: personLinkFormatter
                            },
                            {name: 'firstName', index: 'firstName', width: 120, sortable: true},
                            {name: 'middleName', index: 'middleName', width: 120, sortable: true},
                            {
                                name: 'birthDate',
                                index: 'birthDate',
                                width: 80,
                                formatter: $filter('dateFormatter'),
                                sortable: true
                            },
                            {name: 'reportDoc', width: 300, sortable: true, formatter: $filter('docTypeFormatter')},
                            {name: 'reportDoc', width: 120, sortable: true, formatter: $filter('docNumberFormatter')},
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

                function personLinkFormatter(cellValue, options) {
                    var value = cellValue;
                    if (!cellValue) {
                        value = $filter('translate')('refBook.fl.table.label.undefined');
                    }
                    var url = $state.href('personCard', {id: options.rowId});
                    return "<a href='" + url + "' target='_blank'>" + value + "</a>";
                }

                /**
                 * @description Грид тербанков
                 */
                $scope.tbListGrid = {
                    ctrl: {},
                    options: {
                        datatype: "local",
                        colNames: [
                            '',
                            $filter('translate')('refBook.fl.card.tabs.tb.tabColumnHeader.tb'),
                            $filter('translate')('refBook.fl.card.tabs.tb.tabColumnHeader.date')
                        ],
                        colModel: [
                            {name: 'id', width: 100, key: true, hidden: true},
                            {name: 'tbDepartment.name', width: 450},
                            {
                                name: 'importDate',
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
                        datatype: "angularResource",
                        angularResource: LogBusinessResource,
                        requestParameters: function () {
                            return {
                                projection: "personBusinessLogs",
                                objectId: $scope.person.id
                            };
                        },
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
                            {name: 'id', index: 'id', width: 100, key: true, hidden: true},
                            {name: 'eventName', index: 'event_name', width: 110, classes: 'grid-cell-white-space'},
                            {name: 'logDate', index: 'log_date', width: 130, formatter: $filter('dateTimeFormatter')},
                            {name: 'note', index: 'note', width: 380, classes: 'grid-cell-white-space'},
                            {name: 'userName', index: 'user_name', width: 170, classes: 'grid-cell-white-space'},
                            {name: 'roles', index: 'roles', width: 200, classes: 'grid-cell-white-space'},
                            {
                                name: 'userDepartmentName',
                                index: 'user_department_name',
                                width: 180,
                                classes: 'grid-cell-white-space'
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: true,
                        sortname: 'log_date',
                        sortorder: "desc",
                        hidegrid: false
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
                 * @description Получение списка ДУЛ для ФЛ
                 */
                $scope.fetchIdDocs = function (ctrl) {
                    if ($scope.mode === APP_CONSTANTS.MODE.VIEW) {
                        ctrl.refreshGridData($scope.person.documents.value);
                    } else {
                        ctrl.refreshGridData($scope.personParam.documents.value);
                    }
                };

                /**
                 * @description Получение ИНП для ФЛ
                 */
                $scope.fetchInp = function (ctrl) {
                    ctrl.refreshGridData($scope.person.personIdentityList);
                };

                /**
                 * Получение списка дубликатов для ФЛ
                 */
                $scope.fetchDuplicates = function (ctrl) {
                    ctrl.refreshGridData($scope.person.duplicates);
                };

                /**
                 * @description Получение списка Тербанков для Физлица
                 */
                $scope.fetchTb = function (ctrl) {
                    ctrl.refreshGridData($scope.person.personTbList);
                };

                /**
                 * Перейти в режим редактирования
                 */
                $scope.editMode = function () {
                    $scope.personParam = $.extend(true, {}, $scope.person);
                    $scope.mode = APP_CONSTANTS.MODE.EDIT;
                };

                /**
                 * Очистить временные идентификаторы у созданных элементов
                 */
                var eraseTempId = function(items) {
                    angular.forEach(items, function (item) {
                        if (item.tempId) {
                            item.id = null;
                        }
                    })
                };

                /**
                 * @description Сохранить изменения
                 */
                $scope.save = function () {
                    eraseTempId($scope.personParam.documents.value);
                    eraseTempId($scope.personParam.personIdentityList);
                    eraseTempId($scope.personParam.personTbList);
                    $scope.personParam.vip = $scope.personParam.vipSelect.value;
                    $http({
                        method: "POST",
                        url: "controller/actions/registryPerson/updatePerson",
                        data: $scope.personParam
                    }).success(function (response) {
                        $scope.person = $scope.dataExtract();
                        angular.forEach($scope.personRegTabs, function (tab) {
                            tab.active = false;
                        });
                        $scope.personTab.active = true;
                    });
                    $scope.mode = APP_CONSTANTS.MODE.VIEW;
                };

                /**
                 * Отменить изменения
                 */
                $scope.cancel = function () {
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
                                id: new Date().getTime(),
                                docType: {},
                                person: {id: $scope.personParam.id},
                                tempId: true
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
                                };
                            }
                        }
                    });
                };

                /**
                 * @description Добавить ДУЛ
                 */
                $scope.addIdDoc = function () {
                    editIdDoc(APP_CONSTANTS.MODE.CREATE);
                };

                /**
                 * @description Изменить ДУЛ
                 */
                $scope.editIdDoc = function () {
                    editIdDoc(APP_CONSTANTS.MODE.EDIT);
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
                            var i = 0;
                            var deleteIndex = -1;
                            angular.forEach($scope.personParam.documents.value, function (item) {
                                if (item.id === $scope.idDocsGrid.value[0].id) {
                                    deleteIndex = i;
                                }
                                i++;
                            });
                            if (deleteIndex > -1) {
                                $scope.personParam.documents.value.splice(deleteIndex, 1);
                                if ($scope.personParam.reportDoc && $scope.personParam.reportDoc.value &&
                                    $scope.personParam.reportDoc.value.id === $scope.idDocsGrid.value[0].id
                                ) {
                                    $scope.personParam.reportDoc.value = null;
                                }
                            }
                            $rootScope.$broadcast("addIdDoc", $scope.personParam);
                            $scope.idDocsGrid.ctrl.refreshGridData($scope.personParam.documents.value);
                        }
                    });
                };

                /**
                 * @description Обработка события создания ДУЛ
                 */
                $scope.$on("createIdDoc", function (event, idDoc) {
                    $scope.personParam.documents.value.push(idDoc);
                    $rootScope.$broadcast("addIdDoc", $scope.personParam);
                    $scope.idDocsGrid.ctrl.refreshGridData($scope.personParam.documents.value);
                });

                /**
                 * @description Обпработка события изменения ДУЛ
                 */
                $scope.$on("updateIdDoc", function (event, idDoc) {
                    var i = 0;
                    angular.forEach($scope.personParam.documents.value, function (item) {
                        if (item.id === idDoc.id) {
                            $scope.personParam.documents.value.splice(i, 1, idDoc);
                        }
                        i++;
                    });
                    $scope.idDocsGrid.ctrl.refreshGridData($scope.personParam.documents.value);
                });

                /**
                 * Проверяет необходимость заполнения элементов адреса
                 * @param value
                 * @returns {*|boolean}
                 */
                $scope.isAddressRequiredByAddressItems = function (value) {
                    return value || !($scope.personParam.address.value.appartment ||
                        $scope.personParam.address.value.postalCode ||
                        $scope.personParam.address.value.district ||
                        $scope.personParam.address.value.city ||
                        $scope.personParam.address.value.locality ||
                        $scope.personParam.address.value.house ||
                        $scope.personParam.address.value.build);
                };

                /**
                 * Проверяет валидность поля ввода адреса
                 * @param value
                 * @returns {*|boolean}
                 */
                $scope.isAddressValid = function (value) {
                    return value || !$scope.personParam.address.value.country;
                };

                /**
                 * Проверяет валидность поля страны проживания
                 * @param value
                 * @returns {*|boolean}
                 */
                $scope.isCountryValid = function (value) {
                    return value || !$scope.personParam.address.value.addressIno;
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
                                    recordId: $scope.person.recordId
                                };
                            }
                        }
                    }).result.then(function (person) {
                        if (person) {
                            if (mode === APP_CONSTANTS.MODE.DUPLICATE) {
                                addDuplicate(person);
                            } else if (mode === APP_CONSTANTS.MODE.ORIGINAL) {
                                addOriginal(person);
                            }
                        }
                    });
                };

                /**
                 * @description Добавить оригинал
                 */
                $scope.addOriginal = function () {
                    addPerson($filter('translate')('refBook.fl.card.tabs.original.modal.title'), APP_CONSTANTS.MODE.ORIGINAL);
                };

                /**
                 * @description Добавить дубликат
                 */
                $scope.addDuplicate = function () {
                    addPerson($filter('translate')('refBook.fl.card.tabs.duplicate.modal.title'), APP_CONSTANTS.MODE.DUPLICATE);
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
                            $scope.personParam.original = null;
                            $scope.person.original = null;
                        }
                    });
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
                            var indexOfDeleting = -1;
                            for (var i = 0; i < $scope.personParam.duplicates.length; i++) {
                                if ($scope.personParam.duplicates[i].id === $scope.duplicatesGrid.value[0].id) {
                                    indexOfDeleting = i;
                                    break;
                                }
                            }
                            if (indexOfDeleting > -1) {
                                $scope.personParam.duplicates.splice(indexOfDeleting, 1);
                            }
                            $scope.duplicatesGrid.ctrl.refreshGridData($scope.personParam.duplicates);
                        }
                    });
                };

                function addOriginal(original) {
                    $scope.personParam.original = original;
                    $scope.person.original = original;
                }

                function addDuplicate(duplicate) {
                    duplicate.recordId = $scope.personParam.recordId;
                    $scope.personParam.duplicates.push(duplicate);
                    $scope.duplicatesGrid.ctrl.refreshGridData($scope.personParam.duplicates);
                }

                $scope.$watchGroup(['personParam.startDate', 'personParam.endDate'], function () {
                    $scope.validateVersionDates();
                });

                /**
                 * @description Редактировать ИНП
                 * @param mode  режим редактирования
                 */
                var editInp = function(mode) {
                    var title;
                    var inp;
                    switch (mode) {
                        case APP_CONSTANTS.MODE.CREATE:
                            title = $filter('translate')('refBook.fl.card.tabs.inp.modal.title.create');
                            inp = {
                                id: new Date().getTime(),
                                person: {id: $scope.personParam.id},
                                tempId: true
                            };
                            break;
                        case APP_CONSTANTS.MODE.EDIT:
                            title = $filter('translate')('refBook.fl.card.tabs.inp.modal.title.edit');
                            inp = $scope.inpListGrid.value[0];
                            break;
                    }
                    $aplanaModal.open({
                        title: title,
                        templateUrl: 'client/app/refbooks/personCard/modal/inpModal.html',
                        controller: 'inpModalCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    inp: inp,
                                    mode: mode
                                };
                            }
                        }
                    });
                };

                /**
                 * @description Добавить ИНП
                 */
                $scope.addInp = function() {
                    editInp(APP_CONSTANTS.MODE.CREATE)
                };

                /**
                 * @description Редактировать ИНП
                 */
                $scope.editInp = function() {
                    editInp(APP_CONSTANTS.MODE.EDIT)
                };

                /**
                 * @description Удалить ИНП
                 */
                $scope.deleteInp = function() {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('refBook.fl.card.tabs.inp.deleteDialog.title'),
                        content: $filter('translate')('refBook.fl.card.tabs.inp.deleteDialog.content'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            var i = 0;
                            var deleteIndex = -1;
                            angular.forEach($scope.personParam.personIdentityList, function (item) {
                                if (item.id === $scope.inpListGrid.value[0].id) {
                                    deleteIndex = i;
                                }
                                i++;
                            });
                            if (deleteIndex > -1) {
                                $scope.personParam.personIdentityList.splice(deleteIndex, 1);
                            }
                            $scope.inpListGrid.ctrl.refreshGridData($scope.personParam.personIdentityList);
                        }
                    });
                };

                /**
                 * @description Обработка события создания ИНП
                 */
                $scope.$on("createInp", function (event, inp) {
                    $scope.personParam.personIdentityList.push(inp);
                    $scope.inpListGrid.ctrl.refreshGridData($scope.personParam.personIdentityList);
                });

                /**
                 * @description Обпработка события изменения ИНП
                 */
                $scope.$on("updateInp", function (event, inp) {
                    var i = 0;
                    angular.forEach($scope.personParam.personIdentityList, function (item) {
                        if (item.id === inp.id) {
                            $scope.personParam.personIdentityList.splice(i, 1, inp);
                        }
                        i++;
                    });
                    $scope.inpListGrid.ctrl.refreshGridData($scope.personParam.personIdentityList);
                });

                /**
                 * @description Редактировать Тербанк
                 * @param mode  режим редактирования
                 */
                var editTb = function(mode) {
                    var title;
                    var tb;
                    var presentedTb = [];
                    angular.forEach($scope.personParam.personTbList, function(item) {
                        presentedTb.push(item.tbDepartment.id)
                    });
                     switch (mode) {
                        case APP_CONSTANTS.MODE.CREATE:
                            title = $filter('translate')('refBook.fl.card.tabs.tb.modal.title.create');
                            tb = {
                                id: new Date().getTime(),
                                person: {id: $scope.personParam.id},
                                tempId: true
};
                            break;
                        case APP_CONSTANTS.MODE.EDIT:
                            title = $filter('translate')('refBook.fl.card.tabs.tb.modal.title.edit');
                            tb = $scope.tbListGrid.value[0];
                            break;
                    }
                    $aplanaModal.open({
                        title: title,
                        templateUrl: 'client/app/refbooks/personCard/modal/personTbModal.html',
                        controller: 'personTbModalCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            $shareData: function () {
                                return {
                                    tb: tb,
                                    mode: mode,
                                    presentedTb: presentedTb
                                };
                            }
                        }
                    });
                };

                /**
                 * @description Добавить Тербанк
                 */
                $scope.addTb = function() {
                    editTb(APP_CONSTANTS.MODE.CREATE)
                };

                /**
                 * @description Редактировать Тербанк
                 */
                $scope.editTb = function() {
                    editTb(APP_CONSTANTS.MODE.EDIT)
                };

                /**
                 * @description Удалить Тербанк
                 */
                $scope.deleteTb = function() {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('refBook.fl.card.tabs.tb.deleteDialog.title'),
                        content: $filter('translate')('refBook.fl.card.tabs.tb.deleteDialog.content'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            var i = 0;
                            var deleteIndex = -1;
                            angular.forEach($scope.personParam.personTbList, function (item) {
                                if (item.id === $scope.tbListGrid.value[0].id) {
                                    deleteIndex = i;
                                }
                                i++;
                            });
                            if (deleteIndex > -1) {
                                $scope.personParam.personTbList.splice(deleteIndex, 1);
                            }
                            $scope.tbListGrid.ctrl.refreshGridData($scope.personParam.personTbList);
                        }
                    });
                };

                /**
                 * @description Обработка события создания Тербанка
                 */
                $scope.$on("createTb", function (event, tb) {
                    $scope.personParam.personTbList.push(tb);
                    $scope.tbListGrid.ctrl.refreshGridData($scope.personParam.personTbList);
                });

                /**
                 * @description Обпработка события изменения Тербанка
                 */
                $scope.$on("updateTb", function (event, tb) {
                    var i = 0;
                    angular.forEach($scope.personParam.personTbList, function (item) {
                        if (item.id === tb.id) {
                            $scope.personParam.personTbList.splice(i, 1, tb);
                        }
                        i++;
                    });
                    $scope.tbListGrid.ctrl.refreshGridData($scope.personParam.personTbList);
                });

                /**
                 * Валидация периода актуальности записи.
                 * Нужна так как стандартная валидация min-date, max-date не умеет работать с динамическими ограничениями, но они все равно используются для блокировки дат в самом календаре
                 */
                $scope.validateVersionDates = function () {
                    var generatedVersionFromId = 'personparam_startdate';
                    var generatedVersionToId = 'personparam_enddate';
                    if ($scope.personCardForm && $scope.personCardForm[generatedVersionFromId] && $scope.personCardForm[generatedVersionToId]) {
                        var versionFrom = $scope.personParam.startDate;
                        var versionTo = $scope.personParam.endDate;

                        if (versionFrom != null && versionTo != null && versionTo < versionFrom) {
                            $scope.personCardForm[generatedVersionFromId].$setValidity('versionDate', false);
                        } else {
                            $scope.personCardForm[generatedVersionFromId].$setValidity('versionDate', true);
                        }
                    }
                };
            }
        ]);
}());