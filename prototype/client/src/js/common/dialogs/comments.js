/**
 * aplana-comments (Комментарии)
 * Директива предназначенная отображения комментариев.
 * http://localhost:8080/#/aplana_comments
 */
angular.module('mtsUsim.comments', ['aplana.utils'])
    .filter('fioFormatter', function () {
        return function (user) {
            return user.fullName;
        };
    })
    .run(['$templateCache', '$interpolate', function($templateCache, $interpolate) {
        var startSym = $interpolate.startSymbol();
        var endSym = $interpolate.endSymbol();

        $templateCache.put('/comments/comments.html','<div class="comments"><ul><li style="list-style-type: none; text-align: left;"><span class="comment-body"></span></li></ul></div>');
    } ])
    .constant('aplanaCommentsConfig', {
        date: 'date',
        author: 'author',
        comment: 'comment'
    })
    .constant('aplanaCommentsConfigValidation', function (configuration) {
        "use strict";

        var validOptions = ['date', 'author', 'comment'];

        for (var prop in configuration) {
            if (configuration.hasOwnProperty(prop)) {
                if (validOptions.indexOf(prop) < 0) {
                    throw ("invalid option: " + prop);
                }
            }
        }
    })
    .directive('aplanaComments', ['aplanaCommentsConfig', 'aplanaCommentsConfigValidation', 'AplanaUtils', '$parse',
        function (defaultConfig, validateConfigurationFunction, AplanaUtils, $parse) {
            return {
                restrict: 'A',
                replace: true,
                transclude: true,
                templateUrl: "/comments/comments.html",//AplanaUtils.templatePath + '/comments/comments.html',
                compile: function (element, attrs) {


                    var directiveConfig = {};

                    if (attrs.aplanaCommentsConfig) {
                        directiveConfig = $parse(attrs.aplanaCommentsConfig)();
                    }

                    var configuration = {};

                    angular.extend(configuration, defaultConfig, directiveConfig);

                    validateConfigurationFunction(configuration);

                    element[0].removeAttribute('data-aplana-comments');

                    var divElement = element.find('div');
                    var ulElement = element.find('ul');
                    var liElement = element.find('li');

                    // Сформируем id элемента
                    var ngModelAttr = attrs.ngModel || AplanaUtils.findNgModel(divElement);
                    var modelId = AplanaUtils.buildModelId(ngModelAttr);
                    ulElement.attr('id', modelId);

                    if (ngModelAttr) {
                        liElement.attr('data-ng-repeat', "item in " + ngModelAttr + " | orderBy:'-" + configuration.date + "'");
                    }

                    //var commentHead = element.find('span.comment-head');
                    //commentHead.text("{{item." + configuration.date + " | date:'dd.MM.yyyy HH:mm:ss'}} " +"{{item." + configuration.author + " | fioFormatter}}");

                    var commentBody = element.find('span.comment-body');
                    commentBody.text("{{item." + configuration.comment + "}}");
                }
            };
        }]);