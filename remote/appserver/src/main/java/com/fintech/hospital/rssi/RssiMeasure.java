package com.fintech.hospital.rssi;

import com.alibaba.fastjson.JSON;
import com.dreizak.miniball.highdim.Miniball;
import com.fintech.hospital.domain.AP;
import com.fintech.hospital.domain.TimedPosition;
import com.google.common.collect.Lists;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author baoqiang
 */
public class RssiMeasure {

  static final Logger LOG = LoggerFactory.getLogger(RssiMeasure.class);

  private static final long E_QUATORIAL_EARTH_RADIUS = 6378137;

  private static final double DEG_2_RAD = (Math.PI / 180D);


  public static TimedPosition positionByTriangleGradient(
      List<TimedPosition> positions,
      List<AP> apList,
      double tolerance,
      boolean euclideanDistance
  ) {
    LOG.trace("triangle in positions: {}", JSON.toJSONString(positions));
    LOG.trace("triangle in aps: {}", JSON.toJSONString(apList));
    RssiTriangleMeasure measure = new RssiTriangleMeasure(
        apList.stream()
            .sorted((ap1, ap2) -> ap1.getAlias().compareToIgnoreCase(ap2.getAlias()))
            .map(ap -> ap.getGps().vector()).collect(Collectors.toList()),
        positions.stream()
            .sorted((pos1, pos2) -> pos1.getAp().compareToIgnoreCase(pos2.getAp()))
            .map(TimedPosition::getRadius).collect(Collectors.toList()),
        tolerance,
        euclideanDistance
    );
    Vector2D evaluation = measure.positioning();
    TimedPosition start = TimedPosition.mean(positions, null);
    start.getGps().set(evaluation.getX(), evaluation.getY());
    start.setRadius(measure.getRadius());
    LOG.info("triangular positioning: {} ({}), {} iterations", evaluation, measure.getRadius(), measure.getIteration());
    return start;
  }

  public static TimedPosition positionFromDistribution(
      List<TimedPosition> positions,
      List<AP> apList
  ) {
    RssiDistributionMeasure measure = new RssiDistributionMeasure();
    double[] originCoord = measure.genRSSIMatrix(
        apList.stream()
            .sorted((ap1, ap2) -> ap1.getAlias().compareToIgnoreCase(ap2.getAlias()))
            .map(ap -> ap.getGps().arr()).collect(Collectors.toList())
    );
    Miniball miniball = measure.multiBeaconMiniball(
        positions.stream()
            .sorted((pos1, pos2) -> pos1.getAp().compareToIgnoreCase(pos2.getAp()))
            .mapToDouble(TimedPosition::getRssi).toArray()
    );

    double distanceSum = positions.stream().mapToDouble(TimedPosition::getRadius).sum();
    double ratioSum = positions.stream().mapToDouble(p -> distanceSum - p.getRadius()).sum();
    double[] ratios = positions.stream().mapToDouble(p -> (distanceSum - p.getRadius()) / ratioSum).toArray();
    TimedPosition start = TimedPosition.mean(positions, ratios);
    start.setRadius(miniball.radius() * 2);
    start.getGps().set(originCoord[0] + miniball.center()[0] * 5e-6, originCoord[1] + miniball.center()[1] * 5e-6);
    start = TimedPosition.mean(
        Lists.newArrayList(
            start, positions.stream().max(Comparator.comparingDouble(TimedPosition::getRssi)).get()
        ),
        new double[]{0.4, 0.6}
    );
    return start;
  }


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

    double dist = sphereDistance(gcenter, originCoord);
    double pixelScale = 480 / dist;
    points.forEach(p -> {
      p.getGps().set(
          pixelScale * sphereDistance(new Vector2D(p.getGps().getLng(), 0), new Vector2D(originCoord.getX(), 0)),
          pixelScale * sphereDistance(new Vector2D(0, originCoord.getY()), new Vector2D(0, p.getGps().getLat()))
      );
      p.setRadius(pixelScale * p.getRadius() / dist);
    });
    LOG.debug("pixel scale: {}", pixelScale);
    return new double[]{pixelScale, originCoord.getX(), originCoord.getY(), dist};
  }

  static double sphereDistance(Vector2D o, Vector2D center) {
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

    double pixelScale = 960 / (2 * sphereDistance(gcenter, originCoord));
    points.forEach(p -> p.getGps().set(
        pixelScale * sphereDistance(new Vector2D(p.getGps().getLng(), 0), new Vector2D(originCoord.getX(), 0)),
        pixelScale * sphereDistance(new Vector2D(0, originCoord.getY()), new Vector2D(0, p.getGps().getLat()))
    ));
    LOG.debug("pixel scale: {}", pixelScale);

    apList.stream().forEach(ap -> ap.setGps(
        pixelScale * sphereDistance(new Vector2D(ap.getLongitude(), 0), new Vector2D(originCoord.getX(), 0)),
        pixelScale * sphereDistance(new Vector2D(0, originCoord.getY()), new Vector2D(0, ap.getLatitude()))
    ));
  }

  private static double euclideanDistance(double lnglatDistance) {
    return lnglatDistance * Math.PI * E_QUATORIAL_EARTH_RADIUS / 180;
  }

}
