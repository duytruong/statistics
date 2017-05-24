lazy val root = (project in file(".")).settings(
  name := "statistics",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  mainClass in Compile := Some("duy.statistics.PageView")
)

resolvers += "Maven Repo" at "https://repo1.maven.org/maven2/"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.11" % "2.1.1" % "provided",
  // https://mvnrepository.com/artifact/org.apache.spark/spark-streaming_2.11
  "org.apache.spark" % "spark-streaming_2.11" % "2.1.1" % "provided",
  // https://search.maven.org/#artifactdetails%7Corg.apache.spark%7Cspark-streaming-kafka-0-8_2.11%7C2.1.1%7Cjar
  "org.apache.spark" % "spark-streaming-kafka-0-8_2.11" % "2.1.1",
  "org.apache.avro" % "avro" % "1.8.1",
  "com.twitter" % "bijection-avro_2.10" % "0.9.5",
  "redis.clients" % "jedis" % "2.9.0"
)

assemblyMergeStrategy in assembly <<= (assemblyMergeStrategy in assembly) {
  (old) => {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }
}