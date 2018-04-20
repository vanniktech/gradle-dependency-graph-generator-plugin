# Change Log

Version 0.5.0 *(In development)*
--------------------------------

Version 0.4.0 *(2018-04-20)*
----------------------------

- Add a test for a project which is named like one of the dependency names. [\#43](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/43) ([vanniktech](https://github.com/vanniktech))
- Remove rank and let dot do the ranking. [\#42](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/42) ([vanniktech](https://github.com/vanniktech))
- Update several dependencies. [\#38](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/38) ([vanniktech](https://github.com/vanniktech))
- Nuke badges in README. [\#37](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/37) ([vanniktech](https://github.com/vanniktech))
- Update Detekt to 1.0.0.RC6-4 [\#36](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/36) ([vanniktech](https://github.com/vanniktech))
- Special name for Jetbrains annotations and sqldelight runtime. [\#35](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/35) ([vanniktech](https://github.com/vanniktech))
- Generate PNG image into build/reports/dependency-graph directory. [\#34](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/34) ([vanniktech](https://github.com/vanniktech))
- Support module dependencies with project\(\) and drop rootSuffix. [\#33](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/33) ([vanniktech](https://github.com/vanniktech))
- Generator: Add API to filter out projects. [\#32](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/32) ([vanniktech](https://github.com/vanniktech))
- Handle Android Architecture components special since their module name sucks. [\#31](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/31) ([vanniktech](https://github.com/vanniktech))
- Generate dot file into reports/dependency-graph folder. [\#30](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/30) ([vanniktech](https://github.com/vanniktech))
- Test against Gradle 4 as minimum supported Gradle version. [\#29](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/29) ([vanniktech](https://github.com/vanniktech))
- Use identifier for detecting duplicate connections instead of the ResolvedDependency. [\#27](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/27) ([vanniktech](https://github.com/vanniktech))
- Easier integration test of the plugin. [\#22](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/22) ([vanniktech](https://github.com/vanniktech))
- Adding minimum Gradle version to Readme [\#21](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/21) ([Thunderforge](https://github.com/Thunderforge))

Version 0.3.0 *(2018-03-13)*
----------------------------

- Add MavenCentral badge. [\#18](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/18) ([johnjohndoe](https://github.com/johnjohndoe))
- Update ktlint to 0.19.0 [\#17](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/17) ([vanniktech](https://github.com/vanniktech))
- Add AppVeyor. [\#16](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/16) ([vanniktech](https://github.com/vanniktech))
- Fix Gradle task on Linux and add integration test. [\#14](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/14) ([vanniktech](https://github.com/vanniktech))
- Adding Windows support [\#10](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/10) ([Thunderforge](https://github.com/Thunderforge))

Version 0.2.0 *(2018-03-07)*
----------------------------

- Support Android flavors and buildTypes out of the box and add include Configuration extension point to Generator. [\#9](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/9) ([vanniktech](https://github.com/vanniktech))
- Minor improvements of README [\#6](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/6) ([AlexKorovyansky](https://github.com/AlexKorovyansky))
- Fix typo [\#5](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/5) ([rmtheis](https://github.com/rmtheis))
- Simplify PNG generation and remove copy step. [\#4](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/4) ([vanniktech](https://github.com/vanniktech))
- Use rank feature only when necessary. [\#3](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/3) ([vanniktech](https://github.com/vanniktech))
- Fail early and give tailored message when dot command is not present. [\#2](https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/pull/2) ([vanniktech](https://github.com/vanniktech))

Version 0.1.0 *(2018-03-04)*
----------------------------

- Initial release