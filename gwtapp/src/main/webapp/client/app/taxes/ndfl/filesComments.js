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
            '$modalInstance',
            '$shareData',
            '$filter',
            '$logPanel',
            '$dialogs',
            'DeclarationDataResource',
            'Upload',
            'APP_CONSTANTS',
            function ($scope, $http, $modalInstance, $shareData, $filter, $logPanel, $dialogs, DeclarationDataResource, Upload, APP_CONSTANTS) {

                $scope.editMode = false;

                var attachFileType = $shareData.attachFileTypes;

                var defaultFileType = {
                    id: APP_CONSTANTS.ATTACHE_FILE_TYPE.DEFAULT_TYPE_ID,
                    name: attachFileType[APP_CONSTANTS.ATTACHE_FILE_TYPE.DEFAULT_TYPE_ID]
                };


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
                            {name: 'date', index: 'date', width: 119, formatter: $filter('dateTimeFormatter')},
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
                 * Загрузка записей о файлах в таблицу
                 */
                function loadFiles() {
                    DeclarationDataResource.query({
                            declarationDataId: $shareData.declarationDataId,
                            projection: "filesComments"
                        },
                        function (data) {
                            if (data) {
                                $scope.fileCommentGrid.ctrl.refreshGridData(data.declarationDataFiles);
                                $scope.commentForm.comment = data.comment;
                            }
                        }
                    );
                }

                /**
                 * @description Инициализация таблицы
                 **/
                function initPage() {
                    if($shareData.declarationState !== APP_CONSTANTS.STATE.ACCEPTED.name) {
                        $http({
                            method: "POST",
                            url: "controller/actions/declarationData/" + $shareData.declarationDataId + "/lock"
                        }).then(function (response) {
                            $scope.editMode = response.data.declarationDataLocked;
                            if (response.data.uuid) {
                                $logPanel.open('log-panel-container', response.data.uuid);
                            }
                            loadFiles();
                        });
                    } else {
                        loadFiles();
                    }
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
                                    fileTypeId: defaultFileType.id,
                                    fileTypeName: defaultFileType.name,
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
                        $dialogs.confirmDialog({
                            title: $filter('translate')('filesComment.delete.header'),
                            content: $filter('translate')('filesComment.delete.text'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                var grid = $scope.fileCommentGrid.ctrl.getGrid();
                                _.each($scope.fileCommentGrid.value, function (element) {
                                    grid.delRowData(element.uuid);
                                });
                            }
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
                            comment: $scope.commentForm.comment,
                            declarationDataId: $shareData.declarationDataId
                        },
                        function (data) {
                            if (data) {
                                if (data.uuid && data.uuid !== null) {
                                    $logPanel.open('log-panel-container', data.uuid);
                                }
                                if (data.declarationDataFiles !== null && data.comment !== null) {
                                    $scope.fileCommentGrid.ctrl.refreshGridData(data.declarationDataFiles);
                                    $scope.commentForm.comment = data.comment;
                                }
                            }
                        });
                };

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    if ($scope.editMode) {
                        $http({
                            method: "POST",
                            url: "controller/actions/declarationData/" + $shareData.declarationDataId + "/unlock"
                        }).then(function (response) {
                        });
                    }
                    $modalInstance.dismiss('Canceled');
                };

                initPage();
            }]);
}());