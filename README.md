# lumberjack #

Helping clean up your dependency tree forest.

## Stuff to do ##

- [X] Parse SBT output and construct the dependency tree
- [X] Find clashes
- [X] Trace dependencies to particular projects
- [X] Basic REST API
- [ ] Basic UI
- [ ] Add target rules
- [ ] Find dependencies satisfying and not satisfying the target rules
- [ ] Automatic upgrade of dependencies using target rules

## Future things ##

- [ ] Integrate with SBT and pull the dependency information out rather than parsing it
- [ ] Handle semantic versioning
- [ ] Handle Scala versioning
- [ ] Query repositories to find when dependencies are outdated
