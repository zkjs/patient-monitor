package com.fintech.hospital.rssi;

import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
@Component
@Scope(SCOPE_SINGLETON)
public class RssiMeasure {

  @Value("${measure.arma.speed}")
  private double armaSpeed;

  /**
   * smooth the rssi changes for each beacon with an arma filter
   */
  private void addMeasurement(Map<String, Double> measures, String unique, int rssi) {
        /* use first measurement as initialization */
    if (!measures.containsKey(unique)) {
      measures.put(unique, 1.00 * rssi);
    }
    double previousMeasure = measures.get(unique);
    measures.put(unique, previousMeasure - armaSpeed * (previousMeasure - rssi));
  }

  private void position(Map<String, Double[]> lnglats, Double[] distances) {

//    MathUtils


  }

  public static void main(String[] args) {
    final double radius = 70.0;
    final Vector2D[] observedPoints = new Vector2D[] {
        new Vector2D(1, 1),
        new Vector2D(1, -1),
        new Vector2D(-1, 1)
//        new Vector2D( 30.0,  68.0),
//        new Vector2D( 50.0,  -6.0),
//        new Vector2D(110.0, -20.0),
//        new Vector2D( 35.0,  15.0),
//        new Vector2D( 45.0,  97.0)
    };

    // the model function components are the distances to current estimated center,
    // they should be as close as possible to the specified radius
    MultivariateJacobianFunction distancesToCurrentCenter = new MultivariateJacobianFunction() {
      public Pair<RealVector, RealMatrix> value(final RealVector point) {

        Vector2D center = new Vector2D(point.getEntry(0), point.getEntry(1));

        RealVector value = new ArrayRealVector(observedPoints.length);
        RealMatrix jacobian = new Array2DRowRealMatrix(observedPoints.length, 2);

        for (int i = 0; i < observedPoints.length; ++i) {
          Vector2D o = observedPoints[i];
          double modelI = Vector2D.distance(o, center);
          value.setEntry(i, modelI);
          // derivative with respect to p0 = x center
          jacobian.setEntry(i, 0, (center.getX() - o.getX()) / modelI);
          // derivative with respect to p1 = y center
          jacobian.setEntry(i, 1, (center.getX() - o.getX()) / modelI);
        }

        return new Pair<RealVector, RealMatrix>(value, jacobian);

      }
    };

    // the target is to have all points at the specified radius from the center
    double[] prescribedDistances = new double[observedPoints.length];
    Vector2D center = new Vector2D(1, 0);
    for( int i=0;i<observedPoints.length;i++){
      prescribedDistances[i] = observedPoints[i].distance(center);
    }
    //Arrays.fill(prescribedDistances, radius);

    // least squares problem to solve : modeled radius should be close to target radius
    LeastSquaresProblem problem = new LeastSquaresBuilder().
        start(new double[] { 0.3, 0.3 }).
        model(distancesToCurrentCenter).
        target(prescribedDistances).
        lazyEvaluation(false).
        maxEvaluations(1000).
        maxIterations(1000).
        build();
    LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().optimize(problem);
    Vector2D fittedCenter = new Vector2D(optimum.getPoint().getEntry(0), optimum.getPoint().getEntry(1));
    System.out.println("fitted center: " + fittedCenter.getX() + " " + fittedCenter.getY());
    System.out.println("RMS: "           + optimum.getRMS());
    System.out.println("evaluations: "   + optimum.getEvaluations());
    System.out.println("iterations: "    + optimum.getIterations());
  }

}
//x = Symbol('x')
//    y = Symbol('y')
//    #define variant
//    [x1, y1] = args[0][0]
//    [x2, y2] = args[1][0]
//    [x3, y3] = args[2][0]
//    s1 = float(args[0][1]) - float(35)
//    s2 = float(args[1][1]) - float(35)
//    s3 = float(args[2][1]) - float(35)
//
//    #x1 ap1's lng, y1 ap1's lat, s1 ap1's rssi - standard, k ration from rssi
//    #to dlnglat; solution is a new lnglat;
//    #(x - x1)**2 + (y - y1)**2 = (s1*k)**2 # for one signal;
//    k = float(1800/(65 *math.pi * 6378000))
//    eq = [
//    (x-x1)**2 + (x-x2)**2 - (k*s1)**2,
//    (x-x2)**2 + (x-x2)**2 - (k*s2)**2,
//    (x-x3)**2 + (x-x3)**2 - (k*s3)**2
//    ]
//
//    ans = solve(eq, [x, y])