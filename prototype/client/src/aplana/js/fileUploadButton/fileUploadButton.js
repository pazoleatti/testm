/**
 * aplana-file-upload-button (Выбор файла - кнопка)
 * Директива aplana-file-upload-button служит для отображения кнопки выбора файла
 * http://localhost:8080/#/aplana_file_upload_button
 */
(function () {
    'use strict';
    /**
     * Директива - обертка
     */
    angular.module('aplana.fileUploadButton', ['aplana.utils'])
        .directive('aplanaFileUploadButton', ['AplanaUtils', function (AplanaUtils) {
            return {
                restrict: 'A',
                scope: {
                    requestParameterName: '@',
                    ajaxSubmitOptions: '='
                },
                replace: true,
                transclude: true,
                templateUrl: AplanaUtils.templatePath + 'fileUploadButton/fileUploadButton.html',
                link: function (scope, element, attrs, ngModelCtrl) {
                    if (angular.isUndefined(scope.ajaxSubmitOptions) || !angular.isObject(scope.ajaxSubmitOptions)) {
                        throw new Error('Необходимо указать обязательный атрибут директивы - ajaxSubmitOptions');
                    }

                    if (!scope.requestParameterName) {
                        throw new Error('Необходимо указать обязательный атрибут директивы - requestParameterName');
                    }

                    scope.uniqueId = _.uniqueId();

                    scope.inputElement = $(element.find('input'));

                    /**
                     * После выбора файла нужно сразу отправить его на сервер
                     * Для отправки файла нужна отдельная форма, что в большинстве случаев невозможно,
                     * т.к. вокруг компонента уже есть другая форма, со своими робработчиками
                     *
                     * Значит на нужно создать свою форму, скопировать в нее инпут, запостить, вернуть инпут на весто и
                     * удалить форму.
                     */
                    scope.inputElement.bind('change', function (event) {
                            if (!!event.target.value) {
                                //Создаем форму
                                var form = $('<form style="display: none;" enctype="multipart/form-data" method="POST"></form>');
                                form.appendTo($('body'));

                                //Клонируем исходный инпут
                                var clone = scope.inputElement.clone();
                                //Помещаем его после исходного, в данном случае просто чтобы пометить место
                                scope.inputElement.after(clone);
                                //Переносим исходный инпут во временную форму
                                scope.inputElement.appendTo(form);

                                //Подменим обработчик завершения отправки так, чтобы выполнилось и наше действие и пользовательское
                                var request = angular.copy(scope.ajaxSubmitOptions);
                                request.complete = function (jqXHR, textStatus ) {
                                    //Вернем исходный инпут в помеченное место
                                    clone.after(scope.inputElement);
                                    //Удалим клон и форму
                                    clone.remove();
                                    form.remove();

                                    if (angular.isFunction(scope.ajaxSubmitOptions)) {
                                        scope.ajaxSubmitOptions(jqXHR, textStatus);
                                    }
                                    //Необходимо очищать input для того что бы можно было добавлять один и тотже файл подряд,
                                    //например: добавили, удалили, добавили. Данный способ совместим с IE 8.
                                    //http://stackoverflow.com/questions/1043957/clearing-input-type-file-using-jquery/1043969#1043969
                                    scope.inputElement.replaceWith(scope.inputElement = scope.inputElement.clone(true));
                                };

                                //Запостим форму
                                form.ajaxSubmit(request);
                            }
                        }
                    );
                }
            };
        }]);
}());