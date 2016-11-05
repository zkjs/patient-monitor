package com.fintech.hospital.rssi;

import com.fintech.hospital.domain.AP;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author baoqiang
 */
public class RssiMeasure {

  static final Logger LOG = LoggerFactory.getLogger(RssiMeasure.class);

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

  private static final long E_QUATORIAL_EARTH_RADIUS = 6378137;

  private static final double DEG_2_RAD = (Math.PI / 180D);

  public static double[] transform2RelativeCoords(List<TimedPosition> points) {

    Vector2D originCoord = points.stream().map(p -> p.getGps().vector())
    .reduce(new Vector2D(Double.MAX_VALUE, Double.MIN_VALUE), (origin, v) -> {
      double x = origin.getX() > v.getX() ? v.getX() : origin.getX(),
          y = origin.getY() > v.getY() ? origin.getY() : v.getY();
      return new Vector2D(x, y);
    });
    LOG.debug("origin coords: {}", originCoord);

    Vector2D gcenter = points.stream().map(p -> p.getGps().vector())
        .reduce(new Vector2D(0, 0), Vector2D::add).scalarMultiply(1.0 / points.size());

    LOG.debug("center coords: {}", gcenter);

    double dist = distance(gcenter, originCoord);
    double pixelScale = 480 / dist;
    points.forEach(p -> {
      p.getGps().set(
          pixelScale * distance(new Vector2D(p.getGps().getLng(), 0), new Vector2D(originCoord.getX(), 0)),
          pixelScale * distance(new Vector2D(0, originCoord.getY()), new Vector2D(0, p.getGps().getLat()))
      );
      p.setRadius(pixelScale*p.getRadius()/dist);
    });
    LOG.debug("pixel scale: {}", pixelScale);
    return new double[]{pixelScale, originCoord.getX(), originCoord.getY(), dist};
  }


  public static TimedPosition positioning(List<TimedPosition> positions, String bracelet, boolean euclidean) {
    final List<Vector2D> observes = positions.stream().map(p -> p.getGps().vector()).collect(Collectors.toList());

    MultivariateJacobianFunction distancesToCurrentCenter = point -> {
      Vector2D center = new Vector2D(point.getEntry(0), point.getEntry(1));
      RealVector value = new ArrayRealVector(observes.size());
      RealMatrix jacobian = new Array2DRowRealMatrix(observes.size(), 2);
      for (int i = 0; i < observes.size(); ++i) {
        Vector2D o = observes.get(i);
        double modelI = euclidean ? Vector2D.distance(o, center) : distance(o, center);
        value.setEntry(i, modelI);
        jacobian.setEntry(i, 0, (center.getX() - o.getX()) / modelI);
        jacobian.setEntry(i, 1, (center.getY() - o.getY()) / modelI);
      }
      return new Pair<>(value, jacobian);
    };

    double[] prescribedDistance = euclidean ?
        positions.stream().mapToDouble(TimedPosition::getRadius).toArray() :
        positions.stream().mapToDouble(p -> lnglatDistance(p.getRadius())).toArray();

    double distanceSum = positions.stream().mapToDouble(TimedPosition::getRadius).sum();
    double ratioSum = positions.stream().mapToDouble(p -> distanceSum - p.getRadius()).sum();
    double[] ratios = positions.stream().mapToDouble(p -> (distanceSum - p.getRadius()) / ratioSum).toArray();
    TimedPosition start = TimedPosition.mean(positions, ratios);

    LOG.debug("position evaluation starting from {}", start);
    LOG.debug("targeting {}, ratios: {}", Arrays.toString(prescribedDistance));
    LeastSquaresProblem problem = new LeastSquaresBuilder()
        .checkerPair(new SimpleVectorValueChecker(1e-7, 1e-5))
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
    LOG.info("{} positioning starting@{}, RMS: {}", bracelet, center, optimum.getRMS());
    LOG.debug("{} evaluations: {}", bracelet, optimum.getEvaluations());
    LOG.debug("{} iterations: {}", bracelet, optimum.getIterations());
    start.setGps(center);
    return start;
  }


  public static TimedPosition positioningEuc(List<TimedPosition> positions, String bracelet, boolean euclidean) {

    double[] pixelScaleAndOrigin = transform2RelativeCoords(positions);

    double[] prescribedDistance = euclidean ?
        positions.stream().mapToDouble(TimedPosition::getRadius).toArray() :
        positions.stream().mapToDouble(p -> lnglatDistance(p.getRadius())).toArray();

    double distanceSum = positions.stream().mapToDouble(TimedPosition::getRadius).sum();
    double ratioSum = positions.stream().mapToDouble(p->distanceSum-p.getRadius()).sum();
    double[] ratios = positions.stream().mapToDouble(p -> (distanceSum - p.getRadius()) / ratioSum).toArray();
    TimedPosition start = TimedPosition.mean(positions, null);

    LOG.debug("position evaluation starting from {}", start);
    LOG.debug("targeting {}, ratios: {}", Arrays.toString(prescribedDistance));


    final List<Vector2D> observes = positions.stream().map(p -> p.getGps().vector()).collect(Collectors.toList());

    MultivariateJacobianFunction distancesToCurrentCenter = point -> {
      Vector2D center = new Vector2D(point.getEntry(0), point.getEntry(1));
      RealVector value = new ArrayRealVector(observes.size());
      RealMatrix jacobian = new Array2DRowRealMatrix(observes.size(), 2);
      for (int i = 0; i < observes.size(); ++i) {
        Vector2D o = observes.get(i);
        double modelI = euclidean ? Vector2D.distance(o, center) : distance(o, center);
        value.setEntry(i, modelI);
        jacobian.setEntry(i, 0, (center.getX() - o.getX()) / modelI);
        jacobian.setEntry(i, 1, (center.getY() - o.getY()) / modelI);
      }
      return new Pair<>(value, jacobian);
    };

    LeastSquaresProblem problem = new LeastSquaresBuilder()
        .checkerPair(new SimpleVectorValueChecker(1e-7, 1e-6))
        .model(distancesToCurrentCenter)
        .maxEvaluations(100)
        .maxIterations(50)
        .target(prescribedDistance)
        .lazyEvaluation(false)
        .start(start.getGps().arr())
        .build();
    LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().optimize(problem);
    double[] eval = optimum.getPoint().toArray();
    LOG.debug("{} fitted center: {} (radius: {})", bracelet, Arrays.toString(eval), start.getRadius());
    LngLat center = new LngLat(
        lnglatDistance(pixelScaleAndOrigin[0]/eval[0]) + pixelScaleAndOrigin[1],
        lnglatDistance(pixelScaleAndOrigin[0]/eval[1]) + pixelScaleAndOrigin[2]
    );
    start.setRadius(start.getRadius()*pixelScaleAndOrigin[3]/pixelScaleAndOrigin[0]);
    start.setGps(center);
    LOG.debug("{} fitted center: {} (radius: {})", bracelet, Arrays.toString(center.arr()), start.getRadius());
    LOG.info("{} positioning starting@{}, RMS: {}", bracelet, start.getGps(), optimum.getRMS());
    LOG.debug("{} evaluations: {}", bracelet, optimum.getEvaluations());
    LOG.debug("{} iterations: {}", bracelet, optimum.getIterations());
    return start;
  }

  private static double distance(Vector2D o, Vector2D center) {
    double dlong = (o.getX() - center.getX()) * DEG_2_RAD;
    double dlat = (o.getY() - center.getY()) * DEG_2_RAD;
    double a = Math.pow(Math.sin(dlat / 2D), 2D)
        + Math.cos(o.getY() * DEG_2_RAD) * Math.cos(o.getY() * DEG_2_RAD)
        * Math.pow(Math.sin(dlong / 2D), 2D);
    double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
    return E_QUATORIAL_EARTH_RADIUS * c;
  }

  public static void transform2RelativeCoords(List<TimedPosition> points, List<AP> apList) {

    LOG.debug("{} aps, {} points", apList.size(), points.size());

    Vector2D originCoord = Stream.concat(
        points.stream().map(p -> p.getGps().vector()),
        apList.stream().map(ap -> new Vector2D(ap.getLongitude(), ap.getLatitude()))
    ).reduce(new Vector2D(Double.MAX_VALUE, Double.MIN_VALUE), (origin, v) -> {
      double x = origin.getX() > v.getX() ? v.getX() : origin.getX(),
          y = origin.getY() > v.getY() ? origin.getY() : v.getY();
      return new Vector2D(x, y);
    });
    LOG.debug("origin coords: {}", originCoord);

    Vector2D gcenter = apList.stream().map(p -> p.getGps().vector())
        .reduce(new Vector2D(0, 0), Vector2D::add).scalarMultiply(1.0 / apList.size());

    LOG.debug("center coords: {}", gcenter);

    double pixelScale = 960 / (2 * distance(gcenter, originCoord));
    points.forEach(p -> p.getGps().set(
        pixelScale * distance(new Vector2D(p.getGps().getLng(), 0), new Vector2D(originCoord.getX(), 0)),
        pixelScale * distance(new Vector2D(0, originCoord.getY()), new Vector2D(0, p.getGps().getLat()))
    ));
    LOG.debug("pixel scale: {}", pixelScale);

    apList.stream().forEach(ap -> ap.setGps(
        pixelScale * distance(new Vector2D(ap.getLongitude(), 0), new Vector2D(originCoord.getX(), 0)),
        pixelScale * distance(new Vector2D(0, originCoord.getY()), new Vector2D(0, ap.getLatitude()))
    ));
  }

  private static double lnglatDistance(double distance) {
    return distance * 180 / (Math.PI * E_QUATORIAL_EARTH_RADIUS);
  }

  private static double euclideanDistance(double lnglatDistance) {
    return lnglatDistance * Math.PI * E_QUATORIAL_EARTH_RADIUS / 180;
  }

}
