# sbt-explicit-dependencies

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

## Why?

If you want to avoid dependency hell, it's good practice to explicitly declare
all libraries that your code directly depends on for compilation.

## How to install

Add the plugin in `project/plugins.sbt` or as a global plugin in
`~/.sbt/1.0/plugins/plugins.sbt`:

```
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.1.0")
```

## How to use

### Finding accidental use of transitive dependencies

The `undeclaredCompileDependencies` shows a list of dependencies that should be
declared explicitly:

```
sbt:example> undeclaredCompileDependencies
[warn] The project depends on the following libraries for compilation but they are not declared in libraryDependencies:
[warn] "com.chuusai" %% "shapeless" % "2.3.3"
[warn] "com.google.guava" % "guava" % "26.0-jre"
[warn] "org.typelevel" %% "cats-core" % "1.2.0"
[warn] "org.typelevel" %% "cats-effect" % "0.10.1"
[success] Total time: 2 s, completed 14-Sep-2018 12:21:16
```

There is also a task `undeclaredCompileDependenciesTest` which will fail the
build if there are any undeclared dependencies. This can be useful as part of a
CI pipeline:

```
sbt:example> undeclaredCompileDependenciesTest
[warn] The project depends on the following libraries for compilation but they are not declared in libraryDependencies:
[warn] "com.chuusai" %% "shapeless" % "2.3.3"
[warn] "com.google.guava" % "guava" % "26.0-jre"
[warn] "org.typelevel" %% "cats-core" % "1.2.0"
[warn] "org.typelevel" %% "cats-effect" % "0.10.1"
[error] (undeclaredCompileDependenciesTest) Failing the build because undeclared depedencies were found
[error] Total time: 1 s, completed 14-Sep-2018 12:38:23
```

### Finding unnecessary dependencies

The `unusedCompileDependencies` task shows a list of libraries that have been
declared as dependencies but are not actually needed for compilation:

```
sbt:example> unusedCompileDependencies
[warn] The following libraries are declared in libraryDependencies but are not needed for compilation:
[warn] "com.github.cb372" %% "scalacache-guava" % "0.24.3"
[warn] "org.http4s" %% "http4s-circe" % "0.18.16"
[warn] "org.postgresql" % "postgresql" % "42.2.5"
[warn] "org.tpolecat" %% "doobie-postgres" % "0.5.3"
[success] Total time: 2 s, completed 14-Sep-2018 16:30:50
```

This is also a task `unusedCompileDependenciesTest` which will fail the build if
this list is non-empty. This can be useful as part of a CI pipeline:

```
sbt:example> unusedCompileDependenciesTest
[warn] The following libraries are declared in libraryDependencies but are not needed for compilation:
[warn] "com.github.cb372" %% "scalacache-guava" % "0.24.3"
[warn] "org.http4s" %% "http4s-circe" % "0.18.16"
[warn] "org.postgresql" % "postgresql" % "42.2.5"
[warn] "org.tpolecat" %% "doobie-postgres" % "0.5.3"
[error] (unusedCompileDependenciesTest) Failing the build because unused depedencies were found
[error] Total time: 1 s, completed 14-Sep-2018 16:36:11
```

## Example project

There is an example sbt project in the `example` folder so you can see the
plugin in action.
