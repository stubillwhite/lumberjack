# ClojureScript Single Page Application #

## Overview ##

A basic ClojureScript single-page application with batteries built in:

- Pretty UI components
- Basic navigation pages
- Browser history and navigation

All of this is just plugging together the awesome work of others:

- [bhauman/figwheel-main](https://github.com/bhauman/figwheel-main) for development tooling
- [reagent-project/reagent](https://github.com/reagent-project/reagent) to interface to React
- [priornix/antizer](https://github.com/priornix/antizer) to interface to the Ant Design React UI component library
- [venantius/accountant](https://github.com/venantius/accountant) for single-page application navigation
- [clj-commons/secretary](https://github.com/clj-commons/secretary) for routing

## Prerequisites ##

To run the integration tests you will need Chrome installed, and ChromeDriver (a WebDriver for Chrome). It's possible to
run the tests in other browsers, but you'll need to tweak the code.

- `brew install chrome`
- `brew cask install chromedriver`

## Running a REPL ##

- Run `lein fig:build`
- Browse to http://localhost:9500/

Thanks to `figwheel`, any source changes will automatically be compiled and the browser reloaded.

## Running unit tests in the browser ##

Thanks to `figwheel`, unit tests are automatically run in the browser. To view the results and receive notifications when
tests pass or fail:

- Start a REPL as described above
- Browse to http://localhost:9500/figwheel-extra-main/auto-testing

## Debugging in the browser ##

If you haven't previously done so, configure your browser to link preprocessed code to source:

- [Map Preprocessed Code to Source Code](https://developers.google.com/web/tools/chrome-devtools/javascript/source-maps?hl=en)
    - From the menu select `View` > `Developer Tools`
    - Select `Sources` tab
    - Open the drop down menu (three dots) and select `Settings`
    - Tick `Enable Javascript source maps`

With this done, you should now be able to set breakpoints in the code:

- From the menu select `View` > `Developer Tools`
- Select `Sources` tab
- Expand `cljs-out` > `dev` > `foobar`
- Click on the `.cljs` file (not the `.js` file) and set breakpoints as normal

## Creating a production build ##

- Run `lein cljsbuild once`

## Running end-to-end integration tests ##

- Run `lein cljsbuild once`
- Run `lein test :integration`

In the real world, we would need to stand up a back-end server to end-to-end test against. This is just a simple example
that tests the front-end.
