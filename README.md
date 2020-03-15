# lumberjack #

Quick hackday project to try to tame our forest of dependencies.

## Stuff to do ##

- [X] Parse SBT output and construct the dependency tree
- [X] Find clashes
- [X] Trace dependencies to particular projects
- [X] Basic REST API
- [X] Basic UI
- [ ] Add the ability to include/exclude specific projects on the fly
- [ ] Add the ability to show which projects include a particular dependency
- [ ] Add target rules
- [ ] Find dependencies satisfying and not satisfying the target rules
- [ ] Automatic upgrade of dependencies using target rules

## Future things ##

- [ ] Integrate with SBT and pull the dependency information out rather than parsing it
- [ ] Integrate with Maven repositories and pull dependencies from what was built
- [ ] Handle semantic versioning
- [ ] Handle Scala versioning
- [ ] Query repositories to find when dependencies are outdated
