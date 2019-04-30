(function () {
    'use strict';

    /**
     * Контроллер страницы "Обмен с ФП АС Учет Налогов".
     */
    angular.module('app.transportMessage')

        .controller('transportMessageJournalCtrl', ['$scope', '$filter', '$http', '$aplanaModal', 'transportMessageResource', 'APP_CONSTANTS',
            function ($scope, $filter, $http, $aplanaModal, transportMessageResource, APP_CONSTANTS) {

                $scope.searchFilter = {
                    params: {}
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
                                    departmentIds: $filter('idExtractor')(params.departmentIds),
                                    declarationId: params.declarationId,
                                    fileName: params.fileName,
                                    dateFrom: params.dateFrom,
                                    dateTo: params.dateTo
                                }
                            }
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
                                name: 'declarationId',
                                index: 'declaration_id',
                                formatter: $filter('tmNdflReportLinkFormatter')
                            }, {
                                name: 'department',
                                index: 'department_name',
                                formatter: $filter('nameFormatter')
                            }, {
                                name: 'messageUuid',
                                index: 'message_uuid',
                                width: 250
                            }, {
                                name: 'bodyFileName',
                                index: 'has_body',
                                formatter: $filter('tmBodyFileLinkFormatter'),
                                width: 250
                            }, {
                                name: 'blob',
                                index: 'blob_name',
                                width: 250,
                                formatter: $filter('tmFileLinkFormatter')
                            }
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'id',
                        viewrecords: true,
                        sortorder: "asc",
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
            }
        ])
}());
