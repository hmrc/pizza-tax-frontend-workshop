const path = require('path');

module.exports = function (env, argv) {
  return {
    mode: env.prod ? 'production' : 'development',
    watch: false,
    devtool: 'source-map',
    entry: [
      './javascripts/index.ts',
      env.webjarsDir + '/lib/govuk-frontend/govuk/all.js',
      env.webjarsDir + '/lib/hmrc-frontend/hmrc/all.js'
    ],
    resolve: {
      extensions: ['.js', '.ts']
    },
    module: {
      rules: [
        {
          test: /\.ts$/,
          exclude: /node_modules/,
          use: {
            loader: 'babel-loader',
            options: {
              presets: [
                '@babel/typescript',
                '@babel/preset-env'
              ],
              plugins: [
                '@babel/plugin-proposal-class-properties'
              ]
            }
          }
        },
        {
          test: /\.ts$/,
          exclude: /node_modules|legacy/,
          loader: 'eslint-loader'
        }
      ]
    },
    output: {
      path: env.outputDir + '/javascripts',
      filename: 'application.min.js'
    }
  }
};
