/**
 * aplana-outer-width (Внешняя ширина)
 * Директивы устанавливающие определенную пользователем внешнюю ширину виджета
 * http://localhost:8080/#/aplana_outerWidth
 */
(function () {
    'use strict';
    var TOTAL_INPUT_LEFT_RIGHT_PADDING = 14; // 12px left/right padding + 2px left/right border

    angular.module('aplana.outerWidth', [ 'aplana.utils' ])
        .directive('aplanaInputOuterWidth', [ function () {
            function setInputOuterWidth(_element, outerWidth) {
            	if (outerWidth.charAt(outerWidth.length - 1)=='%') {
            		// ширина задана в процентах
            		_element.wrap( "<div class='cbr-inputWidthPercentWrapper'></div>" );
            		_element.css("width", outerWidth);
            	} else {
            		// ширина задана в пикселях
            		var inputWidth = outerWidth - TOTAL_INPUT_LEFT_RIGHT_PADDING;
                    _element.css("width", inputWidth);
            	}
            }

            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    if (attrs.aplanaInputOuterWidth) {
                        setInputOuterWidth(element, attrs.aplanaInputOuterWidth);
                    }
                }
            };
        } ])
        .directive('aplanaFileUploadOuterWidth', [ function () {
            function setFileUploadOuterWidth(_element, outerWidth) {
            	_element.closest("div.cbr-fileUpload").css("width", outerWidth);
            }

            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    if (attrs.aplanaFileUploadOuterWidth) {
                        setFileUploadOuterWidth(element, attrs.aplanaFileUploadOuterWidth);
                    }
                }
            };
        } ])
        .directive('aplanaPickerOuterWidth', [ function () {
            function setPickerOuterWidth(_element, outerWidth) {
            	_element.find("div.cbr-picker").css("width", outerWidth);
            	//_element.closest("div.input-append").css("width", outerWidth);
            }

            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    if (attrs.aplanaPickerOuterWidth) {
                        setPickerOuterWidth(element, attrs.aplanaPickerOuterWidth);
                    }
                }
            };
        } ])
        .directive('aplanaAddressOuterWidth', [ function () {
            function setAddressOuterWidth(_element, outerWidth) {
                _element.css("width", outerWidth);
            }

            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    if (attrs.aplanaAddressOuterWidth) {
                        setAddressOuterWidth(element, attrs.aplanaAddressOuterWidth);
                    }
                }
            };
        } ]);
}());