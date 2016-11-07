package com.fintech.hospital.rssi;

import com.google.common.collect.Lists;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author baoqiang
 */
public class RssiTriangleMeasure {

  private List<Vector2D> points = new ArrayList<>();
  private List<Double> radiuss = new ArrayList<>();
  private List<Double> linesLength;
  private double tolerance;
  private double previousDiff;
  private double previousDeviation;
  private boolean euclidean;
  /* current evaluation */
  private Vector2D current;
  private Vector2D previous;
  private double radius;
  private StandardDeviation standardDeviation = new StandardDeviation();

  private Logger LOG = LoggerFactory.getLogger(RssiTriangleMeasure.class);

  public RssiTriangleMeasure(List<Vector2D> trianglePoints, List<Double> radiuss, double errorTolerance, boolean euclidean) {
    this.points.addAll(trianglePoints);
    this.radiuss.addAll(radiuss);
    this.tolerance = errorTolerance;
    this.euclidean = euclidean;
    this.linesLength = new ArrayList<>(points.size());
    for (int i = 0; i < points.size(); i++) {
      int next = i + 1 == points.size() ? 0 : i + 1;
      linesLength.add(euclidean ?
          trianglePoints.get(i).distance(trianglePoints.get(next)) :
          RssiMeasure.distance(trianglePoints.get(i), trianglePoints.get(next))
      );
    }
  }

  /**
   * select initial points based on the current distances <br/>
   * O. get the gravity center GC<br/>
   * I. for all distances,
   * <ol>
   * <li>if all radius_i > lines(i-1, i+1), choose the least error point's mirror of GC</li>
   * <li>if 2 radius_i > lines(i-1, i+1), choose the least error point's mirror of GC  </li>
   * <li>if 1 radius_i > lines(i-1, i+1), choose the max error point's line mirror of GC</li>
   * <li>else use GC</li>
   * </ol>
   */
  private Vector2D initialPoint() {
    Vector2D gcenter = points.stream().reduce(new Vector2D(0, 0), Vector2D::add).scalarMultiply(1.0 / points.size());
    double[] errors = IntStream.range(0, radiuss.size()).mapToDouble(i -> {
      int anotherLineIdx = i - 1 < 0 ? radiuss.size() - 1 : i - 1;
      double ei = radiuss.get(i) - linesLength.get(i),
          eai = radiuss.get(i) - linesLength.get(anotherLineIdx);
      return (ei < 0 ? 0 : ei) + (eai < 0 ? 0 : eai);
    }).toArray();
    /* count errors>0 and find the least */
    int leastIndex = 0, maxIndex = 0, errCount = 0;
    for (int i = 0; i < errors.length; i++) {
      if (errors[i] > 0) errCount++;
      if (errors[i] < errors[leastIndex]) leastIndex = i;
      if (errors[i] > errors[maxIndex]) maxIndex = i;
    }
    if (errCount > 1) {
      /* choose least error point's mirror of GC */
      return points.get(leastIndex).scalarMultiply(2).subtract(gcenter);
    } else if (errCount == 1) {
      /* choose max error point's line mirror of GC */
      Vector2D p1 = points.get(maxIndex + 1 >= points.size() ? 0 : maxIndex + 1),
          p2 = points.get(maxIndex - 1 < 0 ? points.size() - 1 : maxIndex - 1);
      if (p1.getX() == p2.getX()) {
        return new Vector2D(2 * p1.getX() - gcenter.getX(), gcenter.getY());
      } else {
        double ax = (p1.getY() - p2.getY()) / (p1.getX() - p2.getX()),
            c = (p1.getX() * p2.getY() - p2.getX() * p1.getY()) / (p1.getX() - p2.getX()),
            d = (gcenter.getX() + (gcenter.getY() - c) * ax) / (1 + ax * ax);
        return new Vector2D(2 * d - gcenter.getX(), 2 * d * ax - gcenter.getY() + 2 * c);
      }
    }
    return gcenter;
  }


  /**
   * select next point based on current differences
   * for all diffs, choose the i, which diff_i > e and diff_i = max(diffs) <br/>
   * gradient on line(P_i, currentPoint), step slopeRate*e <br/>
   * if next point's avg point diff > previousDiffAvg, terminate
   */
  private Vector2D selectNextPoint(double[] diffs) {
    previousDeviation = standardDeviation.evaluate(diffs);
    previousDiff = Arrays.stream(diffs).map(Math::abs).sum();
    previous = current;
    int maxDiffIdx = -1;
    double maxDiff = Double.MIN_VALUE;
    for (int i = 0; i < diffs.length; i++) {
      double absDiff = Math.abs(diffs[i]);
      if (maxDiff < absDiff && absDiff > tolerance) {
        maxDiff = absDiff;
        maxDiffIdx = i;
      }
    }
    radius = (maxDiff == Double.MIN_VALUE ? Arrays.stream(diffs).max().getAsDouble() : maxDiff);
    if (maxDiffIdx == -1) return null;
    Vector2D nextPoint = decrGradient(points.get(maxDiffIdx), current, diffs[maxDiffIdx]);
    double[] nextDiffs = diffs(nextPoint);
    double currentDeviation = standardDeviation.evaluate(nextDiffs),
        currentDiff = Arrays.stream(nextDiffs).map(Math::abs).sum();
    if (Math.abs(currentDiff - previousDiff) > 1.2 * tolerance && currentDeviation > previousDeviation)
      return null;
    else {
      System.out.println("error: " + Arrays.toString(nextDiffs));
      System.out.println("next: " + nextPoint);
      return nextPoint;
    }
  }

  /**
   * if diff > 0, go closer to point,
   * else go further from point
   */
  private Vector2D decrGradient(Vector2D point, Vector2D target, double diff) {
    if (point.getX() == target.getX()) {
      double closer = Math.signum(point.getY() - target.getY());
      return new Vector2D(point.getX(), target.getY() + (diff > 0 ? closer : -1 * closer) * (euclidean ? tolerance : (1.0+Math.random())*Math.PI*1e-6 * tolerance));
    } else {
      double ax = (point.getY() - target.getY()) / (point.getX() - target.getX()),
          c = (point.getX() * target.getY() - target.getX() * point.getY()) / (point.getX() - target.getX()),
          closer = Math.signum(point.getX() - target.getX()),
          x = target.getX() + (diff > 0 ? closer : -1 * closer) * (euclidean ? tolerance : (1.0+Math.random())*Math.PI*1e-6 * tolerance);
      return new Vector2D(x, ax * x + c);
    }
  }


  private double[] diffs(Vector2D aPoint) {
    return IntStream.range(0, points.size())
        .mapToDouble(i ->
            (this.euclidean ?
                aPoint.distance(points.get(i)) :
                RssiMeasure.distance(aPoint, points.get(i))
            ) - radiuss.get(i)
        ).toArray();
  }


  public Vector2D positioning() {
    current = initialPoint();
    while (current != null) {
      double[] diffs = diffs(current);
      current = selectNextPoint(diffs);
    }
    return previous;
  }

  public double getRadius() {
    if (previous == null) throw new IllegalStateException("run positioning first!");
    return this.radius;
  }


//  public static void main(String[] args) {
//    double c = -0.7801144,
//    m = 2.3889582,
//    p = 3.6463172,
//    T = -62;
//    RssiDistanceModel model = new RssiDistanceModel(c, m, p, T);
//    RssiTriangleMeasure measure = new RssiTriangleMeasure(Lists.newArrayList(
//        new Vector2D(113.943667, 22.529193),//110
//        new Vector2D(113.94365, 22.529074), //119
//        new Vector2D(113.943704, 22.529133) //112
//      ),
//      Lists.newArrayList(
//          model.distance(-40), //110
//          model.distance(-92), //119
//          model.distance(-86) //112
//      ),
//      1,
//      false
//    );
//    System.out.println(measure.positioning());
//    System.out.println(measure.getRadius());
//  }

}
