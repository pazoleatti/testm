(function () {
    'use strict';

    /**
     * @description Модуль для для работы со страницей РНУ НДФЛ
     */

    angular.module('app.ndfl',
        ['ui.router',
            'app.createOrEditFLDialog',
            'app.ndflFL',
            'app.incomesAndTax',
            'app.deduction',
            'app.prepayment',
            'app.formSources',
            'app.logBusines',
            'app.logPanel'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndfl', {
                url: '/taxes/ndfl/{declarationId}',
                templateUrl: 'client/app/taxes/ndfl/ndfl.html',
                controller: 'ndflCtrl'
            });
        }])

        /**
         * @description Контроллер страницы РНУ НДФЛ и вкладки "Реквизиты"
         */
        .controller('ndflCtrl', [
            '$scope', '$timeout', '$window', '$stateParams', 'dialogs', 'ShowToDoDialog', '$http', 'DeclarationDataResource', '$filter', '$logPanel',
            function ($scope, $timeout, $window, $stateParams, dialogs, $showToDoDialog, $http, DeclarationDataResource, $filter, $logPanel) {

                $scope.showToDoDialog = function () {
                    $showToDoDialog();
                };

                $scope.$parent.$broadcast('UPDATE_NOTIF_COUNT');

                $scope.openHistoryOfChange = function () {

                    var dlg = dialogs.create('client/app/taxes/ndfl/logBusines.html', 'logBusinesFormCtrl', {declarationId: $scope.formNumber});
                    return dlg.result.then(function () {
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Рассчитать"
                 */
                $scope.calculate = function (force, cancelTask) {
                    $http({
                        method: "PUT",
                        url: "/controller/actions/declarationData/recalculate",
                        params: {
                            declarationDataId: $stateParams.declarationId,
                            force: force ? force : false,
                            cancelTask: cancelTask ? cancelTask : false
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            // $logPanel.open('log-panel-container', response.data.uuid);
                        } else {
                            var buttons = {
                                labelYes: $filter('translate')('common.button.yes'),
                                labelNo: $filter('translate')('common.button.no')
                            };

                            var opts = {
                                size: 'md'
                            };
                            var dlg;

                            if (response.data.status === "LOCKED" && !force) {
                                dlg = dialogs.confirm($filter('translate')('title.confirm'), response.data.restartMsg, buttons, opts);
                                dlg.result.then(
                                    function () {
                                        $scope.calculate(true, cancelTask);
                                    },
                                    function () {
                                    });
                            } else if (response.data.status === "EXIST_TASK" && !cancelTask) {
                                dlg = dialogs.confirm($filter('translate')('title.confirm'), $filter('translate')('title.returnExistTask'), buttons, opts);
                                dlg.result.then(
                                    function () {
                                        $scope.calculate(force, true);
                                    },
                                    function () {
                                    });
                            }
                        }
                    })
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.accept = function (force, cancelTask) {
                    $http({
                        method: "PUT",
                        url: "/controller/actions/declarationData/accept",
                        params: {
                            declarationDataId: $stateParams.declarationId,
                            force: force ? force : false,
                            cancelTask: cancelTask ? cancelTask : false
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                            initPage();
                        } else {
                            var buttons = {
                                labelYes: $filter('translate')('common.button.yes'),
                                labelNo: $filter('translate')('common.button.no')
                            };

                            var opts = {
                                size: 'md'
                            };
                            var dlg;

                            if (response.data.status === "LOCKED" && !force) {
                                dlg = dialogs.confirm($filter('translate')('title.confirm'), response.data.restartMsg, buttons, opts);
                                dlg.result.then(
                                    function () {
                                        $scope.accept(true, cancelTask);
                                    },
                                    function () {
                                    });
                            } else if (response.data.status === "EXIST_TASK" && !cancelTask) {
                                dlg = dialogs.confirm($filter('translate')('title.confirm'), $filter('translate')('title.returnExistTask'), buttons, opts);
                                dlg.result.then(
                                    function () {
                                        $scope.accept(force, true);
                                    },
                                    function () {
                                    });
                            } else if (response.data.status === "NOT_EXIST_XML") {
                                $window.alert($filter('translate')('title.acceptImpossible'));
                            }
                        }
                    })
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Проверить"
                 */
                $scope.check = function (force) {
                    $http({
                        method: "PUT",
                        url: "/controller/actions/declarationData/check",
                        params: {
                            declarationDataId: $stateParams.declarationId,
                            force: force ? force : false
                        }
                    }).then(function (response) {
                        if (response.data && response.data.uuid && response.data.uuid !== null) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        } else {
                            var buttons = {
                                labelYes: $filter('translate')('common.button.yes'),
                                labelNo: $filter('translate')('common.button.no')
                            };

                            var opts = {
                                size: 'md'
                            };
                            var dlg;

                            if (response.data.status === "LOCKED" && !force) {
                                dlg = dialogs.confirm($filter('translate')('title.confirm'), response.data.restartMsg, buttons, opts);
                                dlg.result.then(
                                    function () {
                                        $scope.check(true);
                                    },
                                    function () {
                                    });
                            } else if (response.data.status === "NOT_EXIST_XML") {
                                $window.alert($filter('translate')('title.checkImpossible'));
                            }
                        }
                    })
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Вернуть в создана"
                 */
                $scope.returnToCreated = function () {
                    var buttons = {
                        labelYes: $filter('translate')('common.button.yes'),
                        labelNo: $filter('translate')('common.button.no')
                    };

                    var opts = {
                        size: 'md'
                    };

                    var dlg = dialogs.confirm($filter('translate')('title.confirm'), $filter('translate')('title.returnToCreatedDeclaration'), buttons, opts);
                    dlg.result.then(
                        function () {
                            $http({
                                method: "PUT",
                                url: "/controller/actions/declarationData/returnToCreated",
                                params: {
                                    declarationDataId: $stateParams.declarationId
                                }
                            }).then(function () {
                                initPage();
                            })
                        },
                        function () {
                        });
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Удалить"
                 */
                $scope.deleteDeclaration = function () {
                    var buttons = {
                        labelYes: $filter('translate')('common.button.yes'),
                        labelNo: $filter('translate')('common.button.no')
                    };

                    var opts = {
                        size: 'md'
                    };

                    var dlg = dialogs.confirm($filter('translate')('title.confirm'), $filter('translate')('title.deleteDeclaration'), buttons, opts);
                    dlg.result.then(
                        function () {
                            $http({
                                method: "POST",
                                url: "/controller/actions/declarationData/delete",
                                params: {
                                    declarationDataId: $stateParams.declarationId
                                }
                            }).then(function () {
                                $window.location.assign('/index.html#/taxes/ndflJournal');
                            })
                        },
                        function () {
                        });
                };

                /**
                 * @description Обработка события, которое возникает при нажании на ссылку "Источники"
                 */
                $scope.showSourcesClick = function () {
                    var dlg = dialogs.create('client/app/taxes/ndfl/formSources.html', 'sourcesFormCtrl');
                    return dlg.result.then(function () {
                    });
                };

                /**
                 * @description Инициализация первичных данных на странице
                 */
                function initPage() {
                    DeclarationDataResource.query({
                            id: $stateParams.declarationId,
                            projection: "getDeclarationData"
                        },
                        function (data) {
                            if (data) {
                                $scope.department = data.department;
                                $scope.formNumber = $stateParams.declarationId;
                                $scope.creator = data.creationUserName;
                                $scope.formType = data.declarationFormKind;
                                $scope.period = data.reportPeriodYear + ", " + data.reportPeriod;
                                $scope.state = data.state;
                                $scope.nameAsnu = data.asnuName;
                                $scope.dateAndTimeCreate = data.creationDate;
                            }
                        }
                    );
                }

                initPage();
            }]);
}());