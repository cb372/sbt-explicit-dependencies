package explicitdeps

object UnusedCompileDependenciesException extends sbt.FeedbackProvidedException {
  override def toString: String = "Failing the build because unused dependencies were found"
}
