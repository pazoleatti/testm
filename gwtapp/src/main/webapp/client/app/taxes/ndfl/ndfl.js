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
            '$scope', '$timeout', '$state', '$stateParams', 'dialogs', 'ShowToDoDialog', '$http',
            function ($scope, $timeout, $state, $stateParams, dialogs, $showToDoDialog, $http) {

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

                    $http.get('controller/actions/declarationData/getDeclarationData/' + $stateParams.formId)
                        .then(
                            function (response) {
                                $scope.department = response.data.department;
                                $scope.formNumber = $stateParams.formId;
                                $scope.creator = response.data.creator_user_name;
                                $scope.formType = response.data.form_kind;
                                $scope.period = response.data.report_period;
                                $scope.state = response.data.state;
                                $scope.nameAsnu = response.data.asnu_name;
                                $scope.dateAndTimeCreate = response.data.date_and_time_create;
                            }
                        );
                    setButtonsEnabled();
                }

                initPage();
            }])
    ;
}());