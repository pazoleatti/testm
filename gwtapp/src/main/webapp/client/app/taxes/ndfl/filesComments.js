(function () {
    'use strict';

    /**
     * @description Модуль для работы с МО "Файлы и комментарии"
     */
    angular.module('app.filesComments', [])
    /**
     * @description Контроллер МО "Файлы и комментарии"
     */
        .controller('filesCommentsCtrl', [
            '$scope',
            '$http',
            '$uibModalInstance',
            '$filter',
            '$logPanel',
            'appModals',
            'DeclarationDataResource',
            'Upload',
            'data',
            'APP_CONSTANTS',
            function ($scope, $http, $uibModalInstance, $filter, $logPanel, appModals, DeclarationDataResource, Upload, data, APP_CONSTANTS) {

                var attachFileType = data.attachFileTypes;

                $scope.fileCommentGrid = {
                    ctrl: {},
                    options: {
                        datatype: "local",
                        data: [],
                        colNames: [
                            '',
                            $filter('translate')('filesComment.title.fileName'),
                            $filter('translate')('filesComment.title.fileType'),
                            $filter('translate')('filesComment.title.comment'),
                            $filter('translate')('filesComment.title.dateTime'),
                            $filter('translate')('filesComment.title.user'),
                            $filter('translate')('filesComment.title.userDepartment')
                        ],
                        colModel: [
                            {name: 'uuid', index: 'uuid', width: 176, key: true, hidden: true},
                            {name: 'fileName', index: 'fileName', width: 415},
                            {
                                name: 'fileTypeId', index: 'fileTypeId', width: 90,
                                editable: true,
                                edittype: 'select',
                                formatter: 'select',
                                editoptions: {value: attachFileType}
                            },
                            {name: 'note', index: 'note', width: 200, editable: true, edittype: 'text'},
                            {name: 'date', index: 'date', width: 119, formatter: $filter('dateFormatter')},
                            {name: 'userName', index: 'userName', width: 135},
                            {name: 'userDepartmentName', index: 'userDepartmentName', width: 220, sortable: false}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        cellEdit: true,
                        cellsubmit: 'clientArray',
                        viewrecords: true,
                        sortname: 'id',
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: true

                    }
                };

                /**
                 * @description Инициализация таблицы
                 **/
                function initPage() {
                    DeclarationDataResource.query({
                            declarationDataId: data.declarationDataId,
                            projection: "filesComments"
                        },
                        function (data) {
                            if (data) {
                                $scope.fileCommentGrid.ctrl.refreshGridData(data.declarationDataFiles);
                                $scope.comment = data.comment;
                            }
                        }
                    );
                }


                /**
                 * @description Обработчик кнопки "Добавить файл"
                 **/
                $scope.addFileClick = function (file) {
                    if (file) {
                        Upload.upload({
                            url: 'controller/actions/blobData/uploadFiles',
                            data: {uploader: file}
                        }).progress(function (e) {
                        }).then(function (response) {
                            if (response.data && response.data.uuid) {
                                var newFile = [{
                                    uuid: response.data.uuid,
                                    fileName: file.name,
                                    fileTypeId: 268574799,
                                    fileTypeName: "",
                                    note: "",
                                    userName: $scope.$parent.security.user.name,
                                    userDepartmentName: $scope.$parent.security.user.department.name,
                                    date: new Date().getTime()
                                }];
                                var grid = $scope.fileCommentGrid.ctrl.getGrid();
                                if (grid.addRowData(newFile[0].uuid, newFile, "last")) {
                                    var files = [];
                                    var ids = grid.getDataIDs();
                                    _.each(ids, function (element) {
                                        files.push(grid.getLocalRow(element));
                                    });
                                    grid.trigger("reloadGrid");
                                    $logPanel.open('log-panel-container', response.data.uuid);
                                }
                            }
                        });
                    }
                };

                /**
                 * @description Обработчик кнопки "Удалить файл"
                 **/
                $scope.removeFileClick = function () {
                    if ($scope.fileCommentGrid.value && $scope.fileCommentGrid.value.length !== 0) {
                        appModals.confirm($filter('translate')('filesComment.delete.header'), $filter('translate')('filesComment.delete.text'))
                            .result.then(
                            function () {
                                var grid = $scope.fileCommentGrid.ctrl.getGrid();
                                _.each($scope.fileCommentGrid.value, function (element) {
                                    grid.delRowData(element.uuid);
                                });
                            });
                    }
                };

                /**
                 * @description Обработчик кнопки "Сохранить"
                 **/
                $scope.save = function () {
                    var grid = $scope.fileCommentGrid.ctrl.getGrid();
                    var ids = grid.getDataIDs();
                    var files = [];
                    _.each(ids, function (element) {
                        files.push(grid.getLocalRow(element));
                    });
                    DeclarationDataResource.save(
                        {
                            projection: "filesComments"
                        },
                        {
                            declarationDataFiles: files,
                            comment: $scope.comment,
                            declarationDataId: data.declarationDataId
                        },
                        function (data) {
                            if (data) {
                                if (data.uuid && data.uuid !== null) {
                                    $logPanel.open('log-panel-container', data.uuid);
                                }
                                if (data.declarationDataFiles !== null && data.comment !== null) {
                                    $scope.fileCommentGrid.ctrl.refreshGridData(data.declarationDataFiles);
                                    $scope.comment = data.comment;
                                }
                            }
                        });
                };

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    $uibModalInstance.dismiss('Canceled');
                };

                initPage();
            }]);
}());