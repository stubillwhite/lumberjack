SHELL=/bin/bash

# Constants

COLOR_RED=\033[0;31m
COLOR_GREEN=\033[0;32m
COLOR_YELLOW=\033[0;33m
COLOR_BLUE=\033[0;34m
COLOR_NONE=\033[0m
COLOR_CLEAR_LINE=\r\033[K

JAR="target/lumberjack-0.1.0-SNAPSHOT-standalone.jar"
JS="client/resources/dist/dev-main.js"

# Targets

help:
	@grep -E '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| sort \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "$(COLOR_BLUE)%-10s$(COLOR_NONE) %s\n", $$1, $$2}'

.PHONY: clean
clean: ## Remove all built artefacts
	@echo 'Removing built artefacts'
	@pushd client/ \
		&& lein clean \
		&& popd
	@pushd server/ \
		&& lein clean \
		&& popd

$(JS):
	@echo 'Building client'
	@pushd client/ \
		&& make dist \
		&& popd

client: $(JS) ## Build the client

$(JAR): client
	@echo "Copying client artefacts"
	@mkdir -p server/resources/public/cljs-out
	@cp -r -v client/resources/dist/dev-main.js   server/resources/public/cljs-out/
	@cp -r -v client/resources/public/*           server/resources/public
	@pushd server/ \
		&& lein uberjar \
		&& popd

server: $(JAR) ## Build the server

all: server client
