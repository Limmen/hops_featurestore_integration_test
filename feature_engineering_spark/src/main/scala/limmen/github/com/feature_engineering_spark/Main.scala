package limmen.github.com.feature_engineering_spark

import org.apache.log4j.{ Level, LogManager, Logger }
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{ Row, SparkSession }
import org.apache.spark.{ SparkConf, SparkContext }
import org.rogach.scallop.ScallopConf

/**
 * Parser of command-line arguments
 */
class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val input = opt[String](required = false, descr = "input path")
  val cluster = opt[Boolean](descr = "Flag set to true means that the application is running in cluster mode, otherwise it runs locally")
  val partitions = opt[Int](required = true, validate = (0<), default = Some(1), descr = "number of partitions to distribute the graph")
  val featuregroup = opt[String](required = true, default = Some("customer_type_lookup"), descr = "which featuregroup to compute")
  val version = opt[Int](required = true, validate = (0<), default = Some(1), descr = "featuregroup version")
  verify()
}

object Main {

  def main(args: Array[String]): Unit = {

    // Setup logging
    val log = LogManager.getRootLogger()
    log.setLevel(Level.INFO)
    log.info(s"Starting Feature Engineering in Spark to compute Feature groups")

    //Parse cmd arguments
    val conf = new Conf(args)

    //Save the configuration string
    val argsStr = printArgs(conf, log)

    // Setup Spark
    var sparkConf: SparkConf = null
    if (conf.cluster()) {
      sparkConf = sparkClusterSetup()
    } else {
      sparkConf = localSparkSetup()
    }

    val spark = SparkSession.builder().config(sparkConf).getOrCreate();

    val sc = spark.sparkContext

    val clusterStr = sc.getConf.toDebugString
    log.info(s"Cluster settings: \n" + clusterStr)

    import spark.implicits._

    conf.featuregroup() match {
      case "customer_type_lookup" => featuregroup.CustomerTypeLookup.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "gender_lookup" => featuregroup.GenderLookup.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "pep_lookup" => featuregroup.PepLookup.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "trx_type_lookup" => featuregroup.TrxTypeLookup.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "country_lookup" => featuregroup.CountryLookup.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "industry_sector_lookup" => featuregroup.IndustrySectorLookup.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "alert_type_lookup" => featuregroup.AlertTypeLookup.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "rule_name_lookup" => featuregroup.RuleNameLookup.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "web_address_lookup" => featuregroup.WebAddressLookup.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "browser_action_lookup" => featuregroup.BrowserActionLookup.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "demographic_features" => featuregroup.DemographicFeatures.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "trx_graph_edge_list" => featuregroup.TrxGraphEdgeList.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "customer_node_embeddings" => featuregroup.CustomerNodeEmbeddings.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "trx_graph_summary_features" => featuregroup.TrxSummaryFeatures.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "trx_features" => featuregroup.TrxFeatures.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "trx_summary_features" => featuregroup.TrxSummaryFeatures.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "hipo_table_features" => featuregroup.HipoTableFeatures.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "alert_features" => featuregroup.AlertFeatures.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "police_report_features" => featuregroup.PoliceReportFeatures.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "web_logs_features" => featuregroup.WebLogsFeatures.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
      case "hipo_features" => featuregroup.HiPoFeatures.computeFeatures(conf.input(), conf.featuregroup(), conf.version(), conf.partitions())
    }
    log.info("Shutting down spark job")
    spark.close
  }

  /**
   * Hard coded settings for local spark training
   *
   * @return spark configuration
   */
  def localSparkSetup(): SparkConf = {
    new SparkConf().setAppName("feature_engineering_spark").setMaster("local[*]")
  }

  /**
   * Hard coded settings for cluster spark training
   *
   * @return spark configuration
   */
  def sparkClusterSetup(): SparkConf = {
    new SparkConf().setAppName("feature_engineering_spark").set("spark.executor.heartbeatInterval", "20s").set("spark.rpc.message.maxSize", "512").set("spark.kryoserializer.buffer.max", "1024")
  }

  /**
   * Utility function for printing training configuration
   *
   * @param conf command line arguments
   * @param log logger
   * @return configuration string
   */
  def printArgs(conf: Conf, log: Logger): String = {
    val argsStr = s"Args:  | input: ${conf.input()} | cluster: ${conf.cluster()} | partitions: ${conf.partitions()} |  featuregroup: ${conf.featuregroup()} | version: ${conf.version()}"
    log.info(argsStr)
    argsStr
  }
}
