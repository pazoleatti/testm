/**
 * aplana-file-upload (Выбор файла)
 * Директива aplana-file-upload служит для отображения компонента для выбора файла
 * http://localhost:8080/#/aplana_file_upload
 */
(function () {
    'use strict';
    /**
     * Директива - обертка
     */
    angular.module('aplana.fileUpload', ['aplana.utils'])
        .directive('aplanaFileUpload', ['AplanaUtils', function (AplanaUtils) {
            return {
                restrict: 'A',
                replace: true,
                transclude: true,
                templateUrl: AplanaUtils.templatePath + 'fileUpload/fileUpload.html',
                compile: function (element, attrs) {
                    element[0].removeAttribute('data-aplana-file-upload');
                    var inputElement = element.find('input');
                    AplanaUtils.moveAttributes(element, inputElement);

                    // Сформируем id и name для элемента
                    var ngModelAttr = attrs.ngModel || AplanaUtils.findNgModel(inputElement);
                    var modelId = AplanaUtils.buildModelId(ngModelAttr);
                    inputElement.attr('name', modelId);
                    inputElement.attr('id', modelId);
                }
            };
        }])

    /**
     * Основная реализация директивы
     */
        .directive('aplanaFileUploadImplementation', function () {

            var isIe = (navigator.userAgent.toLowerCase().indexOf('msie') > 0) || (navigator.userAgent.toLowerCase().indexOf('trident') > 0);

            var createFileInput = function (el, scope, attrs, ngModelCtrl) {

                /* width: 0 - для того чтобы при размещении на форме этой директивы
                 общая ширина контрола fileupload не зависела от невидимого элемента
                 (иначе при подгонки размеров появляются полосы прокрутки). */
                var file = angular.element('<input id="' + attrs.attrName + '_id" name="' + attrs.attrName + '" type="file" ' + ' required>');

                /* пропишем связь */
                var buttonLabel = el.parent().parent().find('label');
                buttonLabel.attr('for', attrs.attrName + "_id");

                // отслеживаем изменения
                file.bind('change', function () {
                    setTimeout(function () {
                        scope.$apply(function () {
                            if (isIe) {
                                el.val(file[0].value.substring(file[0].value.lastIndexOf("\\") + 1)); // корректируем имя файла
                                ngModelCtrl.$setViewValue(file[0].value);
                            } else if (file[0].files.length > 0) {
                                el.val(file[0].files[0].name);
                                ngModelCtrl.$setViewValue(file[0].files[0].name);
                            } else {
                                el.val('');
                                ngModelCtrl.$setViewValue(null);
                            }
                        });
                    });
                });

                file.appendTo(el.parent().find(".cbr-fileUpload-hidden"));
                return file;
            };
            /* проверка расширения файла по шаблону если есть данные на вход */
            var isValidExt = function (f, e) {
                return !f || (new RegExp('(^.+(\\.(' + e + ')$))', 'i')).test(f);
            };

            return {
                restrict: 'A',
                require: 'ngModel',
                link: function (scope, element, attrs, ngModelCtrl) {
                    scope.isIe = navigator.userAgent.toLowerCase().indexOf('msie') > 0;

                    var input = createFileInput(element, scope, attrs, ngModelCtrl);

                    scope.remove = function () {
                        input.remove();
                        element.val('');
                        ngModelCtrl.$setViewValue(null);

                        input = createFileInput(element, scope, attrs, ngModelCtrl);
                    };

                    /* секция проверки значений */
                    if (attrs.fileExt) {
                        ngModelCtrl.$parsers.unshift(function (viewValue) {
                            ngModelCtrl.$setValidity('file-extension', isValidExt(viewValue, attrs.fileExt));
                            return viewValue;
                        });

                        ngModelCtrl.$formatters.unshift(function (modelValue) {
                            ngModelCtrl.$setValidity('file-extension', isValidExt(modelValue, attrs.fileExt));
                            return modelValue;
                        });
                    }
                }
            };
        });
}());