(function () {
    'use strict';

    /**
     * @description Модуль для для работы со страницей РНУ НДФЛ
     */

    angular.module('sbrfNdfl.ndfl',
        ['ui.router',
        'sbrfNdfl.createOrEditFLDialog',
        'sbrfNdfl.ndflFL',
        'sbrfNdfl.incomesAndTax',
        'sbrfNdfl.deduction',
        'sbrfNdfl.prepayment'])
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

                /**
                 * @description Инициализация кнопок
                 */
                function setButtonsEnabled() {
                    //Доступности кнопок над вкладками
                    $scope.checkButtonEnabled = true;
                    $scope.calculateButtonEnabled = true;
                    $scope.acceptButtonEnabled = $scope.state === "Создана";
                    $scope.returnButtonEnabled = $scope.state !== "Создана";
                    $scope.deleteButtonEnabled = true;
                }


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
                    setButtonsEnabled();
                }

                initPage();
            }])
    ;
}());