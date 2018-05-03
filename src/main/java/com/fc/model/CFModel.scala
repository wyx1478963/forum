package com.fc.model

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext


object CFModel {

  var model: MatrixFactorizationModel = _
  var sign: Int = 0
  var data: RDD[String] = _
  var sqlContext: SQLContext = _
}

