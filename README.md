# sbt-explicit-dependencies

[![CI](https://github.com/cb372/sbt-explicit-dependencies/actions/workflows/ci.yml/badge.svg)](https://github.com/cb372/sbt-explicit-dependencies/actions/workflows/ci.yml)

An sbt plugin to check that your `libraryDependencies` accurately reflects the
libraries that your code depends on in order to compile.

For example, say your project declares only a single dependency:

```
libraryDependencies += "org.typelevel" %% "cats-effect" % "1.0.0"
```

This brings in a few other libraries as transitive dependencies, including
`cats-core`.

If your code directly depends on classes from `cats-core`, e.g.:

```scala
val nel = cats.data.NonEmptyList.of(1, 2, 3)
```

then this plugin will warn you about that fact.

The plugin can also warn you if you have anything in your `libraryDependencies`
that you're not actually using.

## Why?

If you want to avoid dependency hell, it's good practice to explicitly declare
all libraries that your code directly depends on for compilation.

If you want to keep your deployment artifacts small, you don't want to declare
dependencies on any libraries you don't actually need.

## How to install

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.cb372/sbt-explicit-dependencies/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.cb372/sbt-explicit-dependencies/badge.svg)

Add the plugin in `project/plugins.sbt` or as a global plugin in
`~/.sbt/1.0/plugins/plugins.sbt`:

```
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "latest version (see badge above)")
```

## How to use

### Finding accidental use of transitive dependencies

The `undeclaredCompileDependencies` task shows a list of dependencies that
should be declared explicitly:

```
sbt:example> undeclaredCompileDependencies
[warn] example >>> The project depends on the following libraries for compilation but they are not declared in libraryDependencies:
[warn]  - "com.chuusai" %% "shapeless" % "2.3.3"
[warn]  - "com.google.guava" % "guava" % "26.0-jre"
[warn]  - "org.typelevel" %% "cats-core" % "1.2.0"
[warn]  - "org.typelevel" %% "cats-effect" % "0.10.1"
[success] Total time: 0 s, completed 01-Oct-2018 00:16:31
```

There is also a task `undeclaredCompileDependenciesTest` which will fail the
build if there are any undeclared dependencies. This can be useful as part of a
CI pipeline:

```
sbt:example> undeclaredCompileDependenciesTest
[warn] example >>> The project depends on the following libraries for compilation but they are not declared in libraryDependencies:
[warn]  - "com.chuusai" %% "shapeless" % "2.3.3"
[warn]  - "com.google.guava" % "guava" % "26.0-jre"
[warn]  - "org.typelevel" %% "cats-core" % "1.2.0"
[warn]  - "org.typelevel" %% "cats-effect" % "0.10.1"
[error] (undeclaredCompileDependenciesTest) Failing the build because undeclared dependencies were found
[error] Total time: 1 s, completed 01-Oct-2018 00:17:05
```

### Finding unnecessary dependencies

The `unusedCompileDependencies` task shows a list of libraries that have been
declared as dependencies but are not actually needed for compilation:

```
sbt:example> unusedCompileDependencies
[warn] example >>> The following libraries are declared in libraryDependencies but are not needed for compilation:
[warn]  - "com.github.cb372" %% "scalacache-guava" % "0.24.3"
[warn]  - "org.http4s" %% "http4s-circe" % "0.18.16"
[warn]  - "org.postgresql" % "postgresql" % "42.2.5"
[warn]  - "org.tpolecat" %% "doobie-postgres" % "0.5.3"
[success] Total time: 0 s, completed 01-Oct-2018 00:17:34
```

This is also a task `unusedCompileDependenciesTest` which will fail the build if
this list is non-empty. This can be useful as part of a CI pipeline:

```
sbt:example> unusedCompileDependenciesTest
[warn] example >>> The following libraries are declared in libraryDependencies but are not needed for compilation:
[warn]  - "com.github.cb372" %% "scalacache-guava" % "0.24.3"
[warn]  - "org.http4s" %% "http4s-circe" % "0.18.16"
[warn]  - "org.postgresql" % "postgresql" % "42.2.5"
[warn]  - "org.tpolecat" %% "doobie-postgres" % "0.5.3"
[error] (unusedCompileDependenciesTest) Failing the build because unused dependencies were found
[error] Total time: 0 s, completed 01-Oct-2018 00:18:00
```

#### Runtime Dependencies

Some libraries need to be added to the runtime classpath, but aren't required for compilation. (Logging libraries that implement the SLF4J framework are a common example.) These libraries should be added using the `Runtime` configuration so they're added to `Runtime / dependencyClasspath` and not `Compile / dependencyClasspath`, and will be automatically excluded from consideration by this plugin. For example:

```scala
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime
```

### Filtering the results

If the result of either `undeclaredCompileDependencies` or
`unusedCompileDependencies` contains any dependencies you don't care about, you
can exclude them using the `undeclaredCompileDependenciesFilter` and
`unusedCompileDependenciesFilter` settings.

For example:

```
unusedCompileDependenciesFilter -= moduleFilter("org.scalaz", "scalaz")
```

Note: If you're filtering things out because you think the plugin is returning
false-positive results, please open a GitHub issue.

## Debugging

You can pass `-debug` flag to sbt or set logLevel to debug to understand how the plugin computes compile depndencies

```
sbt:example> set logLevel := Level.Debug
sbt:example> unusedCompileDependencies
...
[debug] Source to library relations:
[debug]   sbt-explicit-dependencies/example/src/main/scala/foo/MyCaseClass.scala -> /Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-effect_2.12/0.10.1/cats-effect_2.12-0.10.1.jar
[debug]   sbt-explicit-dependencies/example/src/main/scala/foo/Example.scala -> /Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-core_2.12/1.2.0/cats-core_2.12-1.2.0.jar
...
[debug] Library dependencies:
[debug]   /Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-effect_2.12/0.10.1/cats-effect_2.12-0.10.1.jar
[debug]   /Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-core_2.12/1.2.0/cats-core_2.12-1.2.0.jar
...
[debug] jarFile: cats-effect_2.12-0.10.1.jar -> "org.typelevel" %% "cats-effect" % "0.10.1"
[debug] jarFile: cats-core_2.12-1.2.0.jar -> "org.typelevel" %% "cats-core" % "1.2.0"
...
[debug] Compile depends on:
[debug]   "org.typelevel" %% "cats-effect" % "0.10.1"
[debug]   "org.typelevel" %% "cats-core" % "1.2.0"
...
[debug] Declared dependencies:
[debug]   "org.http4s" %% "http4s-blaze-server" % "0.18.16"
[debug]   "org.http4s" %% "http4s-circe" % "0.18.16"
...
```

## Example project

There is an example sbt project in the `example` folder so you can see the
plugin in action.
