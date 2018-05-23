package com.fc.service


import com.fc.model.CFModel
import com.fc.util.ConfigUtils
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.jblas.DoubleMatrix
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.scheduling.annotation.{EnableScheduling, Scheduled}
import org.springframework.stereotype.Service

@Service
@EnableScheduling
class ALSFilterService {
  private[service] val logger = LoggerFactory.getLogger(this.getClass)


  case class Params(
                     //                     input: String = "./data/userInfo.data",
                     //                     userDataInput: String = "/data/personalRatings.txt",
                     kryo: Boolean = false,
                     numIterations: Int = 10,
                     lambda: Double = 0.1, //0.01
                     rank: Int = 10,
                     numUserBlocks: Int = -1,
                     numProductBlocks: Int = -1,
                     implicitPrefs: Boolean = false,
                     url: String = "jdbc:mysql://localhost:3306/df?useUnicode=true&characterEncoding=UTF-8&user=%s1&password=%s2")


  @Scheduled(cron = "0 0/10 * * * ?")
  def run() = {
    println("start train")
    val sparkConf = new SparkConf().setAppName("AitLS_Online").setMaster("local[10]")
    val sc = new SparkContext(sparkConf)
    CFModel.sqlContext = new SQLContext(sc)
    train()
  }

  /**
    * read from mysql user read History (userId,postId,rate = 3???)
    */
  def train() = {
    val sqlContext = CFModel.sqlContext
    val defaultParams = Params()
    val user = ConfigUtils.getString("mysql.username")
    val password = ConfigUtils.getString("mysql.password")
    val url = defaultParams.url.replaceAll("%s1",user).replaceAll("%s2",password)
    val jbdf = sqlContext.read.format("jdbc").options(
      Map("url" -> url,
        "dbtable" -> "user_view_history")).load()

    val dataTrain = jbdf.rdd.map(t => "" + t(1) + "," + t(2) + ",3")

    val ratings = dataTrain.map(_.split(",") match { case Array(uid, pid, rate) =>
      Rating(uid.toInt, pid.toInt, rate.toDouble)
    }).cache()

    CFModel.model = evaluateMode(defaultParams, ratings)
  }


  // 3. 计算根均方差
  def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating]): Double = {
    //    val path = new File("/data/model/result.model")
    val usersProducts = data.map { case Rating(user, product, rate) =>
      (user, product)
    }
    val predictions = model.predict(usersProducts).map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }

    val ratesAndPreds = data.map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }.join(predictions)

    math.sqrt(ratesAndPreds.map { case ((user, product), (r1, r2)) =>
      val err = r1 - r2
      err * err
    }.mean())

    //    if (path.isFile)
    //      path.delete()
    //    ratesAndPreds.sortByKey().repartition(1).sortBy(_._1).map({
    //      case ((user, product), (rate, pred)) => (user + "," + product + "," + rate + "," + pred)
    //    })/*.saveAsTextFile("/data/model/result.model")*/
  }

  def cosineSimilarity(vec1: DoubleMatrix, vec2: DoubleMatrix): Double = {
    vec1.dot(vec2) / (vec1.norm2() * vec2.norm2())
  }

  //寻找相似物品
  def computeSimItem(model: MatrixFactorizationModel, itemId: Int): Unit = {
    val itemFactor = model.productFeatures.lookup(itemId).head
    val itemVector = new DoubleMatrix(itemFactor)

    cosineSimilarity(itemVector, itemVector)

    var sims = model.productFeatures.map { case (id, factor) =>
      val factorVector = new DoubleMatrix(factor)
      val sim = cosineSimilarity(factorVector, itemVector)
      (id, sim)
    }
    val K = 10
    val sortedSims = sims.top(K + 1)(Ordering.by[(Int, Double), Double] { case (id, similarity) => similarity })
    sortedSims.slice(1, K + 1).map { case (id, sim) => (id, sim) }.mkString("\n")
    println(sortedSims.mkString("\n"))
  }

  def predictMovie(userId: Int, num: Int = 10): String = {
    if (CFModel.sign == 0)
      run()
    val model = CFModel.model
    val rs = model.recommendProducts(userId, num)
    var value = ""
    var key = 0

    //保存推荐数据
    rs.foreach(r => {
      key = r.user
      value = value + r.product + " "
    })
    value
  }

  def evaluateMode(params: Params, ratings: RDD[Rating]): MatrixFactorizationModel = {
    logger.info("start train")
    val training = ratings
    //建立模型
    val model = new ALS().setRank(params.rank).setIterations(params.numIterations).setLambda(params.lambda).setImplicitPrefs(params.implicitPrefs).setUserBlocks(params.numUserBlocks).setProductBlocks(params.numProductBlocks).run(training)
    CFModel.sign = 1
    //    val testRmse = computeRmse(model, training)
    //    println("RMSE = " + testRmse)
    model
  }

}
