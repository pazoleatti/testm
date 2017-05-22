/**
 * aplana-click-other
 * Директива осуществляет клик по другой кнопке найденной по указанным css селекторам.
 * 
 * Пример: 
 *         <div class="parentCssClass"> 
 *             <form>
 *                 <!-- скрытая кнопка -->
 *                 <button type="submit" class="ng-hide otherSubmitFormButtonCssClass">Submit</button>
 *             </form>
 *             <!-- кнопка вызывающая скрытую кнопку -->
 *             <button data-aplana-click-other=".parentCssClass,.otherSubmitFormButtonCssClass">Submit by outside form</button>
 *         </div>
 *         
 */
(function () {
    'use strict';
    angular.module('aplana.clickOther', [])
        .directive('aplanaClickOther', [ function () {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                	var cssSelectors = attrs.aplanaClickOther.split(",");
                	var parentClickedOtherEl = element.closest(cssSelectors[0]);
                	var clickedOtherEl = parentClickedOtherEl.find(cssSelectors[1]);
                    element.bind('click', function (event) {
                    	clickedOtherEl.click();
                    });
                }
            };
        } ]);
}());