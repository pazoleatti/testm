module.exports = function (config) {
    config.set({
        basePath: '../../',

        files: [
            'src/js/lib/jquery-2.2.3.js',
            'src/js/lib/angular-1.5.5.js',
            'src/js/lib/angular-sanitize-1.5.6.js'
        ],

        autoWatch: true,

        singleRun: true,

        frameworks: ['jasmine'],

        browsers: ['Chrome'],

        plugins: [
            'karma-junit-reporter',
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-ie-launcher',
            'karma-jasmine',
            'karma-ng-html2js-preprocessor'
        ],

        // generate js files from html templates
        preprocessors: {
            '../../aplana/templates/**/*.html': 'ng-html2js'
        },

        ngHtml2JsPreprocessor: {
            // strip this from the file path
            stripPrefix: 'e:/Projects/cbrsipsga/newProject/sipsga-js/src/main/webapp/',
            // prepend this to the
            // prependPrefix: 'served/',

            // setting this option will create only a single module that contains templates
            // from all the files, so you can load them all with module('foo')
            moduleName: 'aplana-templates-ng-html2js-module'
        },

        junitReporter: {
            outputFile: 'test_out/unit.xml',
            suite: 'unit'
        },

        logLevel: config.LOG_DEBUG,

        loggers: [
            {type: 'console'}
        ]

    });
};
