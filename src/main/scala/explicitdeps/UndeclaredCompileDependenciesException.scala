package explicitdeps

object UndeclaredCompileDependenciesException extends sbt.FeedbackProvidedException {
  override def toString: String = "Failing the build because undeclared dependencies were found"
}
