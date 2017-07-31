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
            'app.logBusines'])
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
            '$scope', '$timeout', '$window', '$stateParams', 'dialogs', 'ShowToDoDialog', '$http', 'DeclarationDataResource', '$filter',
            function ($scope, $timeout, $window, $stateParams, dialogs, $showToDoDialog, $http, DeclarationDataResource, $filter) {

                $scope.$parent.$broadcast('UPDATE_NOTIF_COUNT');

                $scope.openHistoryOfChange = function () {

                    var dlg = dialogs.create('client/app/taxes/ndfl/logBusines.html', 'logBusinesFormCtrl', {declarationId: $scope.formNumber});
                    return dlg.result.then(function () {
                    });
                };

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Проверить"
                 */
                $scope.checkButtonClick = function () {
                    $showToDoDialog();
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Рассчитать"
                 */
                $scope.calculateButtonClick = function () {
                    $showToDoDialog();
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.acceptButtonClick = function () {
                    $showToDoDialog();
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Вернуть в создана"
                 */
                $scope.returnButtonClick = function () {
                    var buttons = {
                        labelYes: $filter('translate')('common.button.yes'),
                        labelNo: $filter('translate')('common.button.no')
                    };

                    var opts = {
                        size: 'md'
                    };

                    var dlg = dialogs.confirm("Подтверждение", "Вы действительно хотите вернуть в статус \"Создана\" формы?", buttons, opts);
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
                $scope.deleteRecordClick = function () {
                    var buttons = {
                        labelYes: $filter('translate')('common.button.yes'),
                        labelNo: $filter('translate')('common.button.no')
                    };

                    var opts = {
                        size: 'md'
                    };

                    var dlg = dialogs.confirm("Подтверждение", "Вы уверены, что хотите удалить форму?", buttons, opts);
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