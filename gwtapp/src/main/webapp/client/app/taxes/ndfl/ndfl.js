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
        'sbrfNdfl.logBusines'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndfl', {
                url: '/taxes/ndfl/{formId}',
                templateUrl: 'client/app/taxes/ndfl/ndfl.html',
                controller: 'ndflCtrl'
            });
        }])

        /**
         * @description Контроллер страницы РНУ НДФЛ и вкладки "Реквизиты"
         */
        .controller('ndflCtrl', [
            '$scope', '$timeout', '$state', '$stateParams', 'dialogs', 'ShowToDoDialog', '$http', 'DeclarationDataResource',
            function ($scope, $timeout, $state, $stateParams, dialogs, $showToDoDialog, $http, DeclarationDataResource) {

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
                    $showToDoDialog();
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Удалить"
                 */
                $scope.deleteRecordClick = function () {
                    $showToDoDialog();
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
                            id: $stateParams.formId,
                            projection: "getDeclarationData"
                        },
                        function (data) {
                            if (data) {
                                $scope.department = data.department;
                                $scope.formNumber = $stateParams.formId;
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