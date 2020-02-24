# Constants

COLOR_RED=\033[0;31m
COLOR_GREEN=\033[0;32m
COLOR_YELLOW=\033[0;33m
COLOR_BLUE=\033[0;34m
COLOR_NONE=\033[0m
COLOR_CLEAR_LINE=\r\033[K

PROJECTS=\
	$(HOME)/Dev/recommenders/recs-aws/build.sbt \
	$(HOME)/Dev/recommenders/recs-aws-test/build.sbt 

DEPS=$(PROJECTS:build.sbt=build-dependencies.txt)

CMDSEP=;

# Targets

help:
	@grep -E '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| sort \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "$(COLOR_BLUE)%-15s$(COLOR_NONE) %s\n", $$1, $$2}'

%/build-dependencies.txt: %/build.sbt
	@echo "Building $@"
	@cd $* 														\
		; sbt -Dsbt.log.noformat=true dependencyTree 			\
			| grep --color=never info 							\
			| grep --color=never -v "[info] Loading " 			\
			| grep --color=never -v "[info] Set " 				\
			> $@
	@echo

.PHONY: all
all :: $(DEPS)

.PHONY: clean
clean :: $(DEPS)
	@rm $?
