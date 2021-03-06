(function () {
    'use strict';

    /**
     * Контроллер страницы "Обмен с ФП АС Учет Налогов".
     */
    angular.module('app.transportMessage')

        .controller('transportMessageJournalCtrl', ['$scope', '$filter', '$http', '$aplanaModal',
            'transportMessageResource', '$dialogs', 'APP_CONSTANTS',
            function ($scope, $filter, $http, $aplanaModal, transportMessageResource, $dialogs, APP_CONSTANTS) {

                function getDefaultFilterParams() {
                    return {};
                }

                // поля со значениями null, undefined или "" будут считаться эквивалентными
                function stringify(value) {
                    return JSON.stringify(value, function (key, value) {
                        return value ? value : undefined;
                    });
                }

                $scope.searchFilter = {
                    params: getDefaultFilterParams(),
                    isClear: false,
                    isClearByFilterParams: function () {
                        $scope.searchFilter.isClear = stringify($scope.searchFilter.params) !== stringify(getDefaultFilterParams());
                    },
                    resetFilterParams: function () {
                        $scope.searchFilter.params = getDefaultFilterParams();
                    }
                };

                /**
                 * @description Обновление таблицы, обязательный элемент.
                 */
                $scope.refreshGrid = function (page) {
                    $scope.transportMessageGrid.ctrl.refreshGrid(page);
                };

                $scope.transportMessageGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: 'angularResource',
                        angularResource: transportMessageResource,
                        requestParameters: function () {
                            var params = $scope.searchFilter.params;
                            return {
                                filter: {
                                    id: params.id,
                                    stateIds: $filter('idExtractor')(params.states),
                                    typeId: params.type ? params.type.id : null,
                                    messageUuid: params.messageUuid,
                                    user: params.user,
                                    senderSubsystemId: params.senderSubsystem ? params.senderSubsystem.id : null,
                                    receiverSubsystemId: params.receiverSubsystem ? params.receiverSubsystem.id : null,
                                    contentTypeIds: $filter('idExtractor')(params.contentTypes),

                                    // Вид формы (SBRFNDFL-8318)
                                    declarationTypes: $filter('idExtractor')(params.declarationTypes),

                                    departmentIds: $filter('idExtractor')(params.departmentIds),
                                    declarationId: params.declarationId,
                                    fileName: params.fileName,
                                    dateFrom: params.dateFrom,
                                    dateTo: params.dateTo,

                                    // SBRFNDFL-8318
                                    declarationType: params.declarationType
                                }
                            };
                        },
                        colNames: [
                            $filter('translate')('transportMessages.title.id'),
                            $filter('translate')('transportMessages.title.datetime'),
                            $filter('translate')('transportMessages.title.senderSubsystem'),
                            $filter('translate')('transportMessages.title.receiverSubsystem'),
                            $filter('translate')('transportMessages.title.user'),
                            $filter('translate')('transportMessages.title.state'),
                            $filter('translate')('transportMessages.title.type'),
                            $filter('translate')('transportMessages.title.content'),
                            $filter('translate')('transportMessages.title.declarationId'),
                            $filter('translate')('transportMessages.title.declarationType'),
                            $filter('translate')('title.department'),
                            $filter('translate')('transportMessages.title.uuid'),
                            $filter('translate')('transportMessages.title.message'),
                            $filter('translate')('transportMessages.title.file')
                        ],
                        colModel: [
                            {
                                name: 'id',
                                index: 'id',
                                width: 80,
                                key: true
                            }, {
                                name: 'dateTime',
                                index: 'dateTime',
                                formatter: $filter('dateTimeFormatter')
                            }, {
                                name: 'senderSubsystem',
                                index: 'sender_id',
                                formatter: $filter('idNameFormatter')
                            }, {
                                name: 'receiverSubsystem',
                                index: 'receiver_id',
                                formatter: $filter('idNameFormatter')
                            }, {
                                name: 'initiatorUser',
                                index: 'user_name',
                                formatter: $filter('userFormatter'),
                                width: 200
                            }, {
                                name: 'state',
                                index: 'state',
                                width: 140,
                                formatter: $filter('tmStateFormatter')
                            }, {
                                name: 'type',
                                index: 'type',
                                formatter: $filter('tmTypeFormatter'),
                                width: 120
                            }, {
                                name: 'contentType',
                                index: 'content_type',
                                formatter: $filter('tmContentTypeFormatter'),
                                width: 200
                            }, {
                                name: 'declaration.id',
                                index: 'declaration_id',
                                formatter: $filter('tmNdflReportLinkFormatter')
                            }, {
                                name: 'declaration.typeName',
                                index: 'declaration_type_name'
                            }, {
                                name: 'declaration.departmentName',
                                index: 'department_name'
                            }, {
                                name: 'messageUuid',
                                index: 'message_uuid',
                                width: 250
                            }, {
                                name: 'bodyFileName',
                                index: 'has_body',
                                formatter: $filter('tmGridBodyFileLinkFormatter'),
                                width: 250,
                                sortable: false
                            }, {
                                name: 'blob',
                                index: 'blob_name',
                                width: 250,
                                formatter: $filter('tmGridFileLinkFormatter')
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'id',
                        viewrecords: true,
                        sortorder: "desc",
                        hidegrid: false,
                        multiselect: true,
                        ondblClickRow: function (rowId) {
                            $scope.openMessage($scope.transportMessageGrid.ctrl.getRawData(rowId));
                        }
                    }
                };

                $scope.openMessage = function (row) {
                    $aplanaModal.open({
                        title: $filter('translate')('transportMessages.title.modal'),
                        templateUrl: 'client/app/administration/transportMessage/transportMessageWindow.html',
                        controller: 'transportMessageWindowCtrl',
                        windowClass: 'modal1200',
                        resolve: {
                            $shareData: function () {
                                return {
                                    message: row
                                };
                            }
                        }
                    });
                }


                function saveAs(response, opts) {
                    opts = opts || {};
                    var data = response.data;
                    var contentDispositionHeader = response.headers("content-disposition");
                    var fname = /attachment\;filename=\"([^\"]*)\"/.exec(contentDispositionHeader)[1];

                    var contentTypeHeader = response.headers("content-type");
                    if (contentTypeHeader) {
                        opts.type = contentTypeHeader;
                    }

                    var file, url, tmp = [];

                    fname = fname == null || fname === '' ? 'Список транспортных сообщений.xls' : fname;
                    fname = decodeURI(fname);

                    if (!$.isArray(data)) {
                        tmp[0] = data;
                    } else {
                        tmp = data;
                    }
                    try {
                        file = new File(tmp, fname, opts);
                    } catch (e) {
                        file = new Blob(tmp, opts);
                    }
                    if (window.navigator && window.navigator.msSaveOrOpenBlob) {
                        window.navigator.msSaveOrOpenBlob(file, fname);
                    } else {
                        url = URL.createObjectURL(file);
                        var a = document.createElement("a");
                        a.href = url;
                        a.download = fname;
                        document.body.appendChild(a);
                        a.click();
                        setTimeout(function () {
                            document.body.removeChild(a);
                            window.URL.revokeObjectURL(url);
                        }, 0);
                    }
                }


                function getFilter() {
                    var params = $scope.searchFilter.params;
                    return {
                        id: params.id,
                        stateIds: $filter('idExtractor')(params.states),
                        typeId: params.type ? params.type.id : null,
                        messageUuid: params.messageUuid,
                        user: params.user,
                        senderSubsystemId: params.senderSubsystem ? params.senderSubsystem.id : null,
                        receiverSubsystemId: params.receiverSubsystem ? params.receiverSubsystem.id : null,
                        contentTypeIds: $filter('idExtractor')(params.contentTypes),

                        // Вид формы (SBRFNDFL-8318)
                        declarationTypes: $filter('idExtractor')(params.declarationTypes),

                        departmentIds: $filter('idExtractor')(params.departmentIds),
                        declarationId: params.declarationId,
                        fileName: params.fileName,
                        dateFrom: params.dateFrom,
                        dateTo: params.dateTo,

                        // SBRFNDFL-8318
                        declarationType: params.declarationType
                    };
                }


                function onExportDone(response) {
                    saveAs(response);
                }

                function onExportError(response) {

                }

                /**
                 * @description Выгрузить в excel
                 */
                $scope.downloadExcelAll = function () {
                    var selectedRows = $scope.transportMessageGrid.value;
                    $http({
                        method: "POST",
                        url: "controller/actions/transportMessages/exportExcel",
                        data: $filter('idExtractor')(selectedRows, 'id'),
                        responseType: 'blob'
                    }).then(onExportDone, onExportError);
                };

                /**
                 * @description Выгрузить в Excel по фильтру
                 */
                $scope.downloadExcelByFilter = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/transportMessages/exportExcelByFilter",
                        params: {
                            filter: JSON.stringify(getFilter())
                        },
                        responseType: 'blob'
                    }).then(onExportDone, onExportError);
                };

                /**
                 * @description Выгрузить в Excel по фильтру по выбранным
                 */
                $scope.downloadExcelBySelected = function () {
                    var selectedRows = $scope.transportMessageGrid.value;
                    $http({
                        method: "POST",
                        url: "controller/actions/transportMessages/exportExcelBySelected",
                        data: $filter('idExtractor')(selectedRows, 'id'),
                        responseType: 'blob'
                    }).then(onExportDone, onExportError);
                };
            }
        ])
}());
