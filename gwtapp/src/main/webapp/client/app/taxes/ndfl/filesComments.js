(function () {
    'use strict';

    /**
     * @description Модуль для работы с МО "Файлы и комментарии"
     */
    angular.module('app.filesComments', [])
    /**
     * @description Контроллер МО "Файлы и комментарии"
     */
        .controller('filesCommentsDialogCtrl', [
            '$scope',
            '$http',
            '$uibModalInstance',
            '$filter',
            '$logPanel',
            'dialogs',
            'FilesCommentsResource',
            'Upload',
            function ($scope, $http, $uibModalInstance, $filter,$logPanel, dialogs, FilesCommentsResource, Upload) {

                // TODO: получать данные о типах приаттаченных файлов с сервера.
                var attachFileType = {
                    268574299: "ТФ",
                    268574399: "Исходящий в ФНС",
                    268574499: "Входящий из ФНС",
                    268574599: "Отчет",
                    268574699: "Протокол ошибок",
                    268574799: "Прочее"
                };

                $scope.fileCommentGrid = {
                    ctrl: {},
                    options: {
                        datatype: "local",
                        data:[],
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
                            {name: 'fileName', index: 'fileName', width: 176},
                            {name: 'fileTypeId', index: 'fileTypeId', width: 176,
                                editable: true,
                                edittype: 'select',
                                formatter : 'select',
                                editoptions: {value :attachFileType}},
                            {name: 'note', index: 'note', width: 176, editable: true, edittype: 'text'},
                            {name: 'date', index: 'date', width: 135, formatter: $filter('dateFormatter')},
                            {name: 'userName', index: 'userName', width: 135},
                            {name: 'userDepartmentName', index: 'userDepartmentName', width: 250, sortable: false}
                        ],
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
                    FilesCommentsResource.query({
                            id: $scope.$resolve.data.declarationId,
                            projection: "getDeclarationFilesComments"
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
                    if(file){
                        Upload.upload({
                            url: 'controller/actions/uploadController/formDataFiles',
                            headers:{'Content-type': 'multipart/form-data'},
                            data: {uploader: file}
                        }).progress(function (e) {
                        }).then(function (response) {
                            if(response.data && response.data.uuid) {
                                var newFile = [{
                                    uuid: response.data.uuid,
                                    fileName: file.name,
                                    fileTypeId: 268574799,
                                    fileTypeName:"",
                                    note: "",
                                    userName: $scope.$parent.security.user.name,
                                    userDepartmentName:$scope.$parent.security.user.department,
                                    date: new Date().getTime()
                                }];
                                var grid = $scope.fileCommentGrid.ctrl.getGrid();
                                if(grid.addRowData(newFile.uuid, newFile,"last")){
                                    var files = [];
                                    var ids = grid.getDataIDs();
                                    _.each(ids, function (element) {
                                        files.push(grid.getLocalRow(element));
                                    });

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

                    var buttons = {
                        labelYes: $filter('translate')('common.button.yes'),
                        labelNo: $filter('translate')('common.button.no')
                    };

                    var opts = {
                        size: 'md'
                    };

                    if ($scope.fileCommentGrid.value && $scope.fileCommentGrid.value.length !== 0) {
                        var dlg = dialogs.confirm($filter('translate')('filesComment.delete.header'), $filter('translate')('filesComment.title.delete.text'), buttons, opts);
                        dlg.result.then(
                            function () {
                                var grid = $scope.fileCommentGrid.ctrl.getGrid();
                                _.each($scope.fileCommentGrid.value, function (element) {
                                    grid.delRowData(element.uuid);
                                });
                            },
                            function () {});
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
                    $http({
                        method: "POST",
                        url: "controller/actions/declaration/filesComments/save",
                        data: {
                            declarationDataFiles: files,
                            comment: $scope.comment,
                            declarationId: $scope.$resolve.data.declarationId
                        }
                    }).success(function () {
                        $scope.fileCommentGrid.ctrl.refreshGrid();
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