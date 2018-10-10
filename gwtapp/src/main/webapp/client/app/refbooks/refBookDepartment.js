(function () {
    'use strict';

    /**
     * @description Модуль для отображения иерархического справочника
     */
    angular.module('app.departmentRefBook', ['aplana.treeview'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('departmentRefBook', {
                url: '/refBooks/department/{refBookId}?uuid',
                templateUrl: 'client/app/refbooks/refBookDepartment.html',
                controller: 'departmentRefBookCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_NSI)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        .controller('departmentRefBookCtrl', ['$scope', "$stateParams", "$injector", "$compile", "APP_CONSTANTS",
            "RefBookResource", "RefBookValuesResource", "$aplanaModal", '$filter', "$http", "$logPanel",
            function ($scope, $stateParams, $injector, $compile, APP_CONSTANTS, RefBookResource, RefBookValuesResource, $aplanaModal, $filter, $http, $logPanel) {
                $scope.search = {
                    text: "",
                    precise: false
                };

                /**
                 * Получение записей спраочника и обновление дерева
                 */
                $scope.fetchRecords = function () {
                    // Получаем данные справочника
                    RefBookResource.query({
                        id: $stateParams.refBookId
                    }, function (data) {
                        $scope.refBook = data;
                        // Получаем записи справочника
                        RefBookValuesResource.query({
                            projection: 'allDepartmentsWithChildren',
                            refBookId: $stateParams.refBookId,
                            searchPattern: $scope.search.text,
                            exactSearch: $scope.search.precise
                        }, function (data) {
                            $scope.records = data;
                            $scope.constructTree();
                        });
                    });
                };

                $scope.fetchRecords();

                /**
                 * Динамически добавляет дерево на страницу и заполняет его данными
                 */
                $scope.constructTree = function () {
                    $scope.refBookTree = {
                        ctrl: {},
                        required: true,
                        options: {
                            nodeLabel: 'name',
                            nodeChildren: 'children',
                            iconFolderCloseClass: 'tree-node tree-node-closed',
                            iconFolderOpenClass: 'tree-node tree-node-open',
                            iconFileClass: 'tree-node'
                        },
                        data: $scope.records
                    };

                    $injector.invoke(function ($compile) {
                        var parent = angular.element(document.querySelector("#refBookTreeContainer"));
                        var refBookGrid = $compile("<div data-aplana-treeview=\"refBookTree.ctrl\"\n" +
                            "         id=\"refBookTree\"\n" +
                            "         data-options=\"refBookTree.options\"\n" +
                            "         data-tree-data=\"refBookTree.data\"\n" +
                            "         data-multi-select=\"false\"\n" +
                            "         data-collapsed=\"true\"></div>")(parent.scope());
                        parent.html(refBookGrid);
                    });
                };

                /**
                 * Отображает диалог для просмотра записи справочника
                 */
                $scope.showRecord = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('refBooks.showRecord'),
                        templateUrl: 'client/app/refbooks/modal/refBookRecordModal.html',
                        controller: 'refBookRecordModalCtrl',
                        windowClass: 'modal800',
                        resolve: {
                            $shareData: function () {
                                return {
                                    mode: "VIEW",
                                    refBook: $scope.refBook,
                                    record: $scope.refBookTree.ctrl.selection
                                };
                            }
                        }
                    });
                };

                /**
                 * Формирование XLSX выгрузки записей справочника
                 */
                $scope.createReportXlsx = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/refBook/" + $stateParams.refBookId + "/reportXlsx",
                        params: {
                            searchPattern: $scope.search.text,
                            exactSearch: $scope.search.precise
                        }
                    }).success(function (response) {
                        $logPanel.open('log-panel-container', response.uuid);
                    });
                };

                /**
                 * Формирование CSV выгрузки записей справочника
                 */
                $scope.createReportCsv = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/refBook/" + $stateParams.refBookId + "/reportCsv",
                        params: {
                            searchPattern: $scope.search.text,
                            exactSearch: $scope.search.precise
                        }
                    }).success(function (response) {
                        $logPanel.open('log-panel-container', response.uuid);
                    });
                };
            }]);
}());