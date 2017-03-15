import breeze.linalg.eig
import breeze.stats.distributions.{Gamma, Gaussian}
import io.github.mandar2812.dynaml.kernels._
import io.github.mandar2812.dynaml.models.bayes.{LinearTrendESGPrior, LinearTrendGaussianPrior}
import io.github.mandar2812.dynaml.probability.{BlockedMESNRV, GaussianRV, MultGaussianPRV, RandomVariable}
import io.github.mandar2812.dynaml.analysis.implicits._
import com.quantifind.charts.Highcharts._


val rbfc = new TStudentCovFunc(1.5)
val n = new MAKernel(0.8)

val gp_prior = new LinearTrendGaussianPrior[Double](rbfc, n, 0.0, 0.0)
val sgp_prior = new LinearTrendESGPrior[Double](rbfc, n, 0.75, 0.1, 0.0, 0.0)

val xs = Seq.tabulate[Double](20)(0.5*_)

val ys: MultGaussianPRV = gp_prior.priorDistribution(xs)
val sgp_ys: BlockedMESNRV = sgp_prior.priorDistribution(xs)

val samples = (1 to 8).map(_ => ys.sample()).map(s => s.toBreezeVector.toArray.toSeq)

val samples_sgp = (1 to 8).map(_ => sgp_ys.sample()).map(s => s.toBreezeVector.toArray.toSeq)

spline(xs, samples.head)
hold()
samples.tail.foreach((s: Seq[Double]) => spline(xs, s))
unhold()
title("Gaussian Process prior samples")

spline(xs, samples_sgp.head)
hold()
samples_sgp.tail.foreach((s: Seq[Double]) => spline(xs, s))
unhold()
title("Ext. Skew Gaussian Process prior samples")


val gammaRV = RandomVariable(new Gamma(2.0, 2.0))
val noiseAdd = GaussianRV(0.0, 1.0)

val dataset = Seq.tabulate[(Double, Double)](10){i => (gammaRV.sample(), noiseAdd.sample())}

//Seq((11.1, gammaRV.sample()), (-0.5, 2.5))

//Set hyper-parameter selection configuration
gp_prior.globalOptConfig_(Map("gridStep" -> "0.15", "gridSize" -> "5"))
sgp_prior.globalOptConfig_(Map("gridStep" -> "0.15", "gridSize" -> "2"))

val gpModel = gp_prior.posteriorModel(dataset)
val sgpModel = sgp_prior.posteriorModel(dataset)

val zs: MultGaussianPRV = gpModel.predictiveDistribution(xs)
val sgp_zs: BlockedMESNRV = sgpModel.predictiveDistribution(xs)

val MultGaussianPRV(m, c) = zs
val eigD = eig(c.toBreezeMatrix)
val eValuesPositive = eigD.eigenvalues.toArray.forall(_ >= 0.0)

if(eValuesPositive) {
  val samplesPost = zs.iid(8).sample().map(s => s.toBreezeVector.toArray.toSeq)

  spline(xs, samplesPost.head)
  hold()
  samplesPost.tail.foreach((s: Seq[Double]) => spline(xs, s))
  unhold()
  title("Gaussian Process posterior samples")

} else {
  println("Predictive Covariance Ill-Posed!")
}

val samplesSGPPost = sgp_zs.iid(8).sample().map(s => s.toBreezeVector.toArray.toSeq)

spline(xs, samplesSGPPost.head)
hold()
samplesSGPPost.tail.foreach((s: Seq[Double]) => spline(xs, s))
unhold()
title("Ext. Skew Gaussian Process posterior samples")