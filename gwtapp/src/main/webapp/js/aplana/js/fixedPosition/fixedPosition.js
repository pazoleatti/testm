/**
 * aplana-fixed-position
 * Директива фиксирует позицию элемента на странице.
 *         
 */
(function () {
    'use strict';
    angular.module('aplana.fixedPosition', [])
        .factory('$fixedPositionElements', [ '$timeout', function ($timeout) {
        	var $fixedPositionElements = {};
        	$fixedPositionElements.elements = [];
        	
        	function updateElementsSize() {
        		for (var i=0; i<$fixedPositionElements.elements.length; i++) {
    				var element = $fixedPositionElements.elements[i];
        			
        			if (element.simpleClone) {
        				// calculate element width by clone width
        				var newElementW = $(element.simpleClone).outerWidth(true) + 'px';
        				// optimization set css
        				if (newElementW != element.style.width) {
        					element.style.width = newElementW;
        				}
        				
        				// calculate clone height by element height
        				var elH = $(element).outerHeight(true);
        				var props = element.props
        				var newCloneH;
        				if (props && props.cloneHeightCorrection) {
        					newCloneH = props.cloneHeightCorrection + elH + 'px';
        				} else {
        					newCloneH = elH + 'px';
        				}
        				// optimization set css
        				if (newCloneH != element.simpleClone.style.height) {
        					element.simpleClone.style.height = newCloneH;
        				}
        			}
        		}
        	};
        	$fixedPositionElements.updateElementsSize = updateElementsSize;
        	
        	function markMovedElement(iElement, scrollTop) {
        		if (scrollTop>6) {
        		    iElement.addClass(iElement[0].markMovedCss);
        	    } else {
        	    	iElement.removeClass(iElement[0].markMovedCss);
        	    }
        	};
            
            function updateElementsPosition() {
            	var scrollTop = $(window).scrollTop();
            	var scrollLeft = $(window).scrollLeft();
            	var windowWidth = $(window).width();
            	
            	for (var i=0; i<$fixedPositionElements.elements.length; i++) {
        			var element = $fixedPositionElements.elements[i];
        			var iElement = $(element);
        			markMovedElement(iElement, scrollTop);
        			
        			if (windowWidth < iElement.width()) {
        				iElement.css('left', (element.fixedLeftPosition - $(window).scrollLeft()) + 'px');
                    } else {
                    	iElement.css('left', element.fixedLeftPosition + 'px');
                    }
        		}
            };
        	
        	$fixedPositionElements.removeElement = function (element) {
        		for (var i=0; i<$fixedPositionElements.elements.length; i++) {
        			if($fixedPositionElements.elements[i] === element) {
        				$fixedPositionElements.elements.splice(i, 1);
        				return;
      	            }
        		}
        	};
        	$fixedPositionElements.pushElement = function (element) {
        		$fixedPositionElements.elements.push(element);
        	};
        	$fixedPositionElements.completeElements = function () {
        		var scrollTop = $(window).scrollTop();
        	    for (var i=0; i<$fixedPositionElements.elements.length; i++) {
        			var element = $fixedPositionElements.elements[i];
        			var iElement = $(element);
        			
        			markMovedElement(iElement, scrollTop);
            			
            		var elW = iElement.outerWidth(true);
            		
            		var elOffset = iElement.offset();
            	    element.fixedLeftPosition = elOffset.left;
            	    iElement.css('top', elOffset.top + 'px');
            		iElement.css('left', elOffset.left + 'px');
            		
            		// create clone
            		if (!element.simpleClone) {
            		    var clone = document.createElement("div");
            		    element.parentNode.insertBefore(clone, element.nextSibling);
            		    element.simpleClone = clone;
            		}
            			
            		iElement.css('width', elW + 'px');
            		iElement.css('position', 'fixed');
            	}	
            };
            
            $(window).on('scroll', updateElementsPosition);
            $(window).on("resize", updateElementsSize);
            
        	return $fixedPositionElements;
        }])
        .directive('aplanaFixedPosition', ['$timeout', '$fixedPositionElements', '$rootScope', function ($timeout, $fixedPositionElements, $rootScope) {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                	// markMovedCss
                	var markMovedCss = attrs.aplanaFixedPositionStyle;
                	if (!markMovedCss) {
                		markMovedCss = "markMoved-default";
                	}
                	element[0].markMovedCss = markMovedCss;
                	
                	// props
                	var jsonProps = attrs.aplanaFixedPositionProps;
                	if (jsonProps) {
                		var props = angular.fromJson(jsonProps);
                		element[0].props = props;
                	}
                	
                	$fixedPositionElements.pushElement(element[0]);
                    
                    var listener = scope.$watch(function() {
                    	// I.Zadorozhny: при любом изменении моделей на странице
                    	// необходимо обновлять размеры фиксированных элементов
                    	// т.к. от этого зависит положение на странице
                    	$fixedPositionElements.updateElementsSize();
                    });
                  
                    $fixedPositionElements.completeElements();
         
                    scope.$on('$destroy', function () {
                    	$fixedPositionElements.removeElement(element[0]);
                        scope = null;
                    });
                }
            };
        } ]);
}());