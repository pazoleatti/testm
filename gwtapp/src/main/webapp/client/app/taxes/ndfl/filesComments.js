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
            '$scope', '$http', '$modalInstance', '$shareData', '$filter', '$logPanel', '$dialogs', 'DeclarationDataResource', 'Upload', 'APP_CONSTANTS', '$q', '$modalStack',
            'PermissionChecker',
            function ($scope, $http, $modalInstance, $shareData, $filter, $logPanel, $dialogs, DeclarationDataResource, Upload, APP_CONSTANTS, $q, $modalStack, PermissionChecker) {

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
                            {name: 'permissions', index: 'permissions', hidden: true},
                            {name: 'fileName', index: 'fileName', width: 360, formatter: linkFileFormatter},
                            {
                                name: 'fileTypeId', index: 'fileTypeId', width: 90,
                                editable: true,
                                edittype: 'select',
                                formatter: 'select',
                                editoptions: {value: attachFileType}
                            },
                            {
                                name: 'note',
                                index: 'note',
                                width: 200,
                                editable: true,
                                edittype: 'text',
                                editoptions: {
                                    dataEvents: [
                                        {
                                            type: 'change',
                                            fn: function () {
                                                var grid = $scope.fileCommentGrid.ctrl.getGrid();
                                                var row = grid.getLocalRow(this.getAttribute("rowid"));
                                                row.note = this.value;
                                            }
                                        }
                                    ]
                                }
                            },
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
                 * @description форматтер для возможности скачивания файла из списка
                 * @param cellValue значение ячейки
                 * @param options данные таблицы
                 * @param row строка таблицы
                 * без cellValue и options ссылка формируется некорректно
                 */
                function linkFileFormatter(cellValue, options, row) {
                    return "<a target='_self' href='controller/actions/declarationData/" + $shareData.declarationDataId + "/download/" + row.uuid + "'>" + row.fileName + " </a>";

                }

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
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $shareData.declarationDataId + "/lockFilesAndComments"
                    }).then(function (response) {
                        if (response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                        if (!response.data.success) {
                            $modalInstance.dismiss('Не можем установить блокировку');
                        } else {
                            loadFiles();
                        }
                    });
                }


                /**
                 * @description Обработчик кнопки "Добавить файл"
                 **/
                $scope.addFileClick = function (file) {
                    if (file) {
                        Upload.upload({
                            url: 'controller/actions/declarationData/uploadFile',
                            data: {
                                uploader: file,
                                declarationDataId: $shareData.declarationDataId
                            },
                            method: 'POST'
                        }).progress(function (e) {
                        }).success(function (data) {
                            if (data && data.uuid) {
                                var newFile = [{
                                    uuid: data.uuid,
                                    fileName: file.name,
                                    fileTypeId: defaultFileType.id,
                                    fileTypeName: defaultFileType.name,
                                    note: "",
                                    permissions: ~0,
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
                                }
                            } else if (data && data.errorUuid) {
                                $logPanel.open('log-panel-container', data.errorUuid);
                            }
                        });
                    }
                };

                $scope.filesDeleteAvailable = function () {
                    var files = $scope.fileCommentGrid.value;
                    if (files && files.length > 0) {
                        return files.every(function (item) {
                            return PermissionChecker.check(item, APP_CONSTANTS.DECLARATION_FILE_PERMISSION.DELETE);
                        });
                    }
                    return false;
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
                    $scope.dSave = $q.defer();
                    var grid = $scope.fileCommentGrid.ctrl.getGrid();
                    var files = getFilesInGrid(grid);

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
                                $scope.dSave.resolve(true);
                                if (data.uuid && data.uuid !== null) {
                                    $logPanel.open('log-panel-container', data.uuid);
                                }
                                if (data.declarationDataFiles !== null) {
                                    $scope.fileCommentGrid.ctrl.refreshGridData(data.declarationDataFiles);
                                    $scope.commentForm.comment = data.comment;
                                }
                            } else {
                                $scope.dSave.reject("Fail");
                            }
                        });
                    return $scope.dSave.promise;
                };

                /**
                 * @description Обработчик кнопки "Закрыть"
                 **/
                $scope.close = function () {
                    $scope.getData().then(function (data) {
                        if ($scope.commentForm.comment !== data.comment || !equals(data.declarationDataFiles, getFilesInGrid($scope.fileCommentGrid.ctrl.getGrid()))) {
                            $dialogs.confirmDialog({
                                content: $filter('translate')('filesComment.close.saveChange'),
                                okBtnCaption: $filter('translate')('common.button.yes'),
                                cancelBtnCaption: $filter('translate')('common.button.no'),
                                okBtnClick: function () {
                                    $scope.save().then(function () {
                                        $scope.unlock();
                                    });
                                    $modalInstance.close('Saved');
                                },
                                cancelBtnClick: function () {
                                    $scope.unlock();
                                    $modalInstance.dismiss("Canceled");
                                }
                            });
                        } else {
                            $scope.unlock();
                            $modalInstance.dismiss("Canceled");
                        }
                    });
                };

                /**
                 * @description Проверяет наличие изменений в файлах
                 */
                var equals = function (dbFiles, formFiles) {

                    if (dbFiles.length !== formFiles.length) {
                        return false;
                    }
                    var contains = true;
                    dbFiles.forEach(function (dbFile) {
                        contains = false;
                        formFiles.forEach(function (formFile) {
                            if (dbFile.uuid === formFile.uuid && dbFile.fileTypeId === dbFile.fileTypeId && formFile.note === dbFile.note) {
                                contains = true;
                            }
                            if (!contains) {
                                contains = false;
                                return;
                            }
                        });
                        if (!contains) {
                            return;
                        }
                    });
                    return contains;
                };

                /**
                 * @description Получает список записей, которые находятся в гриде
                 */
                var getFilesInGrid = function (grid) {

                    // В jqGrid значение редактируемой ячейки сохраняется только в том случае, если нажать Enter или
                    // выделить другую ячейку.
                    // 0, 0 - ячейка, которая не отображается на экране. false - выделить без редактирования
                    grid.jqGrid("editCell", 0, 0, false);

                    var ids = grid.getDataIDs();
                    var files = [];
                    _.each(ids, function (element) {
                        files.push(grid.getLocalRow(element));
                    });
                    return files;
                };

                /**
                 * @description Снимает блокировку с декларации
                 */
                $scope.unlock = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $shareData.declarationDataId + "/unlockFilesAndComments"
                    }).then(function (response) {
                        if (!response.data.success && response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                };

                /**
                 * Получение данных формы с БД
                 */
                $scope.getData = function () {
                    $scope.d = $q.defer();
                    DeclarationDataResource.query({
                            declarationDataId: $shareData.declarationDataId,
                            projection: "filesComments"
                        },
                        function (data) {
                            if (data) {
                                $scope.d.resolve(data);
                            } else {
                                $scope.d.reject();
                            }
                        }
                    );
                    return $scope.d.promise;
                };

                /**
                 * Переопределенный метод модуля {aplana.modal}, чтобы по нажатию на крестик
                 * выполнялась логика кнопки "Закрыть"
                 */
                $scope.modalCloseCallback = function () {
                    $scope.close();
                };

                initPage();
            }]);
}());