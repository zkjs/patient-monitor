package com.fintech.hospital.rssi;

import com.fintech.hospital.domain.LngLat;
import com.fintech.hospital.domain.TimedPosition;
import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author baoqiang
 */
public class RssiMeasure {

  /**
   * smooth the rssi changes for each beacon with an arma filter
   */
  private void addMeasurement(Map<String, Double> measures, String unique, int rssi) {
    /* use first measurement as initialization */
    if (!measures.containsKey(unique)) {
      measures.put(unique, 1.00 * rssi);
    }
    double previousMeasure = measures.get(unique);
    measures.put(unique, previousMeasure - 0.8 * (previousMeasure - rssi));
  }

  public static TimedPosition positioning(List<Pair<TimedPosition, Double>> positions, String bracelet) {
    Logger LOG = LoggerFactory.getLogger("Positioning");
    final Vector2D[] observes = (Vector2D[]) positions.stream().map(p -> p.getKey().getGps().vector()).toArray();

    MultivariateJacobianFunction distancesToCurrentCenter = point -> {
      Vector2D center = new Vector2D(point.getEntry(0), point.getEntry(1));
      RealVector value = new ArrayRealVector(observes.length);
      RealMatrix jacobian = new Array2DRowRealMatrix(observes.length, 2);
      for (int i = 0; i < observes.length; ++i) {
        Vector2D o = observes[i];
        double modelI = Vector2D.distance(o, center);
        value.setEntry(i, modelI);
        jacobian.setEntry(i, 0, (center.getX() - o.getX()) / modelI);
        jacobian.setEntry(i, 1, (center.getX() - o.getX()) / modelI);
      }
      return new Pair<>(value, jacobian);
    };
    double[] prescribedDistance = positions.stream().mapToDouble(p -> lnglatDistance(p.getValue())).toArray();

    double distanceSum = positions.stream().mapToDouble(Pair::getValue).sum();
    double[] ratios = positions.stream().mapToDouble(p -> p.getValue() / distanceSum).toArray();
    TimedPosition start = TimedPosition.mean((TimedPosition[]) positions.stream().map(Pair::getKey).toArray(), ratios);

    LeastSquaresProblem problem = new LeastSquaresBuilder()
        .checkerPair(new SimpleVectorValueChecker(1e-8, 1e-8))
        .model(distancesToCurrentCenter)
        .maxEvaluations(100)
        .maxIterations(50)
        .target(prescribedDistance)
        .lazyEvaluation(false)
        .start(start.getGps().arr())
        .build();
    LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().optimize(problem);
    LngLat center = new LngLat(optimum.getPoint().toArray());
    LOG.debug("{} fitted center: [{}, {}]", bracelet, center.getLng(), center.getLat());
    LOG.info("{} positioning RMS: ", bracelet, optimum.getRMS());
    LOG.debug("{} evaluations: ", bracelet, optimum.getEvaluations());
    LOG.debug("{} iterations: ", bracelet, optimum.getIterations());
    start.setGps(center);
    return start;
  }

  private static double lnglatDistance(double distance) {
    return distance * 180 / (Math.PI * 63781370);
  }


}
