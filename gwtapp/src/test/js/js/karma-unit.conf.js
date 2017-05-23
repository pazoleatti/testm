module.exports = function(config) {
    config.set({
        basePath: '../../../',
        files: [
            'main/webapp/js/js/lib/jquery-3.2.1.js',
            'test/js/lib/angular-1.5.9.js',
            'test/js/lib/angular-mocks-1.5.9.js',
            'test/js/lib/underscore-1.8.3.js',
            'main/webapp/js/js/common/utils/*.js',
            'test/js/js/utils/*.spec.js'
        ],
        autoWatch: true,
        singleRun: true,
        frameworks: ['jasmine'],
        browsers: ['Chrome'],
        plugins: [
            'karma-jasmine',
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-ie-launcher',
            'karma-junit-reporter'
        ],
        reporters: ['progress', 'junit'],
        junitReporter: {
            outputFile: 'karma-unit-report.xml',
            suite: 'unit',
            useBrowserName: false
        },
        logLevel: config.LOG_DEBUG,
        loggers: [
            {type: 'console'}
        ],
        colors: true
    });
};