module.exports = function (config) {
    config.set({
        basePath: '../../../',
        files: [
            {pattern: 'test/js/lib/angular-1.5.9.js', watched: false},
            {pattern: 'test/js/lib/angular-mocks-1.5.9.js', watched: false},
            {pattern: 'test/js/lib/underscore-1.8.3.js', watched: false},
            {pattern: 'test/js/lib/moment-2.18.1.js', watched: false},
            {pattern: 'main/webapp/client/components/grid/deep.js', watched: false},
            'main/webapp/client/app/common/*.js',
            'main/webapp/client/app/refbooks/refBookInterceptors.js',
            'main/webapp/client/app/refbooks/interceptor/*.js',
            'test/js/unit/**/*.spec.js'
        ],
        autoWatch: true,
        singleRun: true,
        frameworks: ['jasmine'],
        browsers: ['IE'],
        plugins: [
            'karma-jasmine',
            'karma-ie-launcher',
            'karma-chrome-launcher',
            'karma-junit-reporter',
            'karma-coverage'
        ],
        reporters: [
            'progress',
            'junit',
            'coverage'
        ],
        preprocessors: {
            'main/webapp/client/app/**/*.js': 'coverage'
        },
        coverageReporter: {
            type: 'in-memory'
        },
        junitReporter: {
            outputFile: 'karma-unit-report.xml',
            suite: 'unit',
            useBrowserName: false
        },
        logLevel: config.LOG_INFO
    });
};
