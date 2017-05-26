module.exports = function(config) {
    config.set({
        basePath: '../../../',
        files: [
            '../../src/js/common/utils/*.js',
            '../../src/test/js/utils/*.spec.js'
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
            outputDir: 'src/test/js/',
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