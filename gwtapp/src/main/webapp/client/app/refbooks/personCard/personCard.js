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

                $scope.deletedDuplicates = [];

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
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.asnu'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.inp')
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

                /**
                 * @description Грид тербанков
                 */
                $scope.tbListGrid = {
                    ctrl: {},
                    options: {
                        datatype: "local",
                        colNames: [
                            '',
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.tb'),
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.date')
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
                 * @description Сохранить изменения
                 */
                $scope.save = function () {
                    angular.forEach($scope.personParam.documents.value, function (item) {
                        if (item.tempId) {
                            item.id = null;
                        }
                    });
                    if (!$scope.personParam.reportDoc.value) {
                        $scope.personParam.reportDoc.value = {id: null}
                    }
                    $scope.personParam.vip = $scope.personParam.vipSelect.value;
                    $scope.personParam.duplicates = $scope.personParam.duplicates.concat($scope.deletedDuplicates);
                    $http({
                        method: "POST",
                        url: "controller/actions/registryPerson/updatePerson",
                        data: $scope.personParam
                    }).success(function (response) {
                        $scope.person = $scope.dataExtract();
                        angular.forEach($scope.personRegTabs, function(tab) {
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
                    })
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
                        $scope.personParam.address.value.build)
                };

                /**
                 * Проверяет валидность поля ввода адреса
                 * @param value
                 * @returns {*|boolean}
                 */
                $scope.isAddressValid = function (value) {
                    return value || !$scope.personParam.address.value.country.code
                };

                /**
                 * Проверяет валидность поля страны проживания
                 * @param value
                 * @returns {*|boolean}
                 */
                $scope.isCountryValid = function (value) {
                    return value.code || !$scope.personParam.address.value.addressIno
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
                                    recordId: $scope.person.recordId,
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
                            $scope.personParam.original = null;
                            $scope.person.original = null;
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
                            var indexOfDeleting = -1;
                            var i = 0;
                            angular.forEach($scope.personParam.duplicates, function (item) {
                                if (item.id == $scope.duplicatesGrid.value[0].id) {
                                    indexOfDeleting = i;
                                    item.recordId = item.oldId;
                                    $scope.deletedDuplicates.push(item)
                                }
                                i++;
                            });
                            if (indexOfDeleting > -1) {
                                $scope.personParam.duplicates.splice(indexOfDeleting, 1);
                            }
                            $scope.duplicatesGrid.ctrl.refreshGridData($scope.personParam.duplicates);
                        }
                    });
                };

                $scope.$on("addOriginal", function (event, original) {
                    $scope.personParam.original = original;
                    $scope.person.original = original;
                });

                $scope.$on("addDuplicate", function (event, duplicate) {
                    duplicate.recordId = $scope.personParam.recordId;
                    $scope.personParam.duplicates.push(duplicate);
                    $scope.duplicatesGrid.ctrl.refreshGridData($scope.personParam.duplicates)
                });
            }
        ]);
}());