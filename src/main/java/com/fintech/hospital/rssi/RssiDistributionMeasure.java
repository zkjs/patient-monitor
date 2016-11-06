package com.fintech.hospital.rssi;

import com.dreizak.miniball.highdim.Miniball;
import com.dreizak.miniball.model.ArrayPointSet;
import com.fintech.hospital.domain.TimedPosition;
import com.google.common.collect.Lists;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author baoqiang
 */
public class RssiDistributionMeasure {

  double m;
  double p;
  double c;
  double T;
  int row;
  int column;
  Object[] matrixes;

  public RssiDistributionMeasure() {
    m = 1.9229884;
    p = 6.4525179;
    c = 0.1820634;
    T = -75;
    row = 60;
    column = 60;
  }

  void distances(double[] target, double[]... pos) {
    Arrays.asList(pos).forEach(x -> System.out.println(new EuclideanDistance().compute(target, x)));
  }

  double[] genRSSIMatrix(List<double[]> beaconLocations) {
    double[] originCoord = beaconLocations.stream()
        .reduce(new double[]{Double.MAX_VALUE, Double.MAX_VALUE}, (origin, v) -> {
          double x = origin[0] > v[0] ? v[0] : origin[0],
              y = origin[1] > v[1] ? v[1] : origin[1];
          return new double[]{x,y};
    });
    originCoord[0] -= 2e-4;
    originCoord[1] -= 2e-4;

    //define a map of 30*30
    Object[][] rssiMatrix = new Object[row][column];
    Map<String, List<double[]>> signalMap = new HashMap<>();
    for (int i = 0; i < row; i++) {
      for (int j = 0; j < column; j++) {
        //slice the area to 1e-5*1e-5: 1 square meter
        int idx = i, jdx = j;
        double[] sigs;
        if (beaconLocations.size() == 1) {
          /* distance in meters, every meter away */
          double distance = RssiMeasure.distance(
              new Vector2D(beaconLocations.get(0)),
              new Vector2D(originCoord[0] + i * 1e-5, originCoord[1] + j * 1e-5));
          rssiMatrix[idx][jdx] = rssiFromDistance(distance);
          sigs = getSigs(new double[]{(Double) rssiMatrix[idx][jdx]});
        } else {
          double[] distances = new double[beaconLocations.size()];
          for (int di = 0; di < distances.length; di++) {
            distances[di] =
                RssiMeasure.distance(
                    new Vector2D(beaconLocations.get(di)),
                    new Vector2D(originCoord[0] + i * 1e-5, originCoord[1] + j * 1e-5)
                );
          }
          //for each area in the map, get a rssi matrix and a distance matrix
          rssiMatrix[idx][jdx] = rssisFromDistances(distances);
          sigs = getSigs((double[]) rssiMatrix[idx][jdx]);
        }
        String sigKey = sigKey(sigs);
        if (sigKey != null) {
          if (signalMap.containsKey(sigKey)) {
            signalMap.get(sigKey).add(new double[]{idx, jdx});
            //based on the rssi matrix, make a signal matrix
            //iterate the signal matrix and group them into a "signal_vector:area_list" Smap
          } else signalMap.put(sigKey, Lists.newArrayList(new double[]{idx, jdx}));
        }
      }
    }
    matrixes = new Object[]{rssiMatrix, signalMap};
    return originCoord;
  }

  /**
   * given target signal vector and current signal vector keys, find the most similar signal vector
   */
  List<String> topSimilarSigs(double[] sig, Set<String> sigKeys) {
    //similarity = cos(v1, v2) = v1 * v2 / (|v1| |v2|)

    Map<String, Double> sigKeySims = new HashMap<>(sigKeys.size());
    sigKeys.forEach(key -> {
      double cosineSim = cosineSim(sig, fromString(key, sig.length));
      sigKeySims.put(key, cosineSim);
    });
    Map<String, List<Map.Entry<String, Double>>> mostSimilarKeys =
        sigKeySims.entrySet().stream().sorted((k1, k2) -> (-1) * Double.compare(k1.getValue(), k2.getValue()))
            .filter(k -> k.getValue() > 0.6)
            .collect(Collectors.groupingBy(k -> {
              int vector = (int) (k.getValue() * 10);
              if (vector >= 9) return "HIGH";
              if (vector >= 7 && vector < 9) return "MEDIUM";
              return "LOW";
            }));
    List<String> tmpKeys = new ArrayList<>(40);
    if (mostSimilarKeys.containsKey("HIGH")) {
      tmpKeys.addAll(mostSimilarKeys.get("HIGH").stream().map(Map.Entry::getKey).collect(Collectors.toList()));
      if (tmpKeys.size() > 9) return tmpKeys.subList(0, 9);
    } else if (mostSimilarKeys.containsKey("MEDIUM")) {
      tmpKeys.addAll(mostSimilarKeys.get("MEDIUM").stream().map(Map.Entry::getKey).collect(Collectors.toList()));
      if (tmpKeys.size() > 17) return tmpKeys.subList(0, 17);
    }
    return tmpKeys;
  }

  double[] fromString(String string, int dimension) {
    String[] strings = string.replace("[", "").replace("]", "").split(",[ ]?");

    double result[] = new double[dimension];
    for (String str : strings) {
      int sig = Integer.parseInt(str);
      if (sig / 1000 <= 1 + result.length) result[sig / 1000 - 1] = sig % 100;
    }
    return result;
  }

  double cosineSim(double[] targetSig, double[] modelSig) {
    double[] fullModelSig = modelSig;
    if (targetSig.length > modelSig.length) {
      fullModelSig = new double[targetSig.length];
      Arrays.fill(fullModelSig, modelSig.length, targetSig.length - 1, 1);
    }
    ArrayRealVector target = new ArrayRealVector(targetSig);
    ArrayRealVector vector = new ArrayRealVector(fullModelSig);
    return target.dotProduct(vector) / (target.getNorm() * vector.getNorm());
  }

  String sigKey(double[] sigs) {
    if (sigs.length == 1 && sigs[0] == 99) return null;
    Object[] sigArr = IntStream.range(0, sigs.length).mapToObj(idx -> new int[]{idx + 1, (int) sigs[idx]})
        //[0-6,1-9]
        .sorted((sig1, sig2) -> -1 * Integer.compare(sig1[1], sig2[1]))
        //[1-9,0-6]
        .filter(sig -> sig[1] > 0 && sig[1] < 99)
        //[1-9,0-6]
        .map(sig -> sig[0] * 1000 + sig[1]).toArray();
    return sigArr.length > 0 ? Arrays.toString(sigArr) : null;
    //[1-9,0-6]
  }

  double[] getSigs(double[] rssi) {
    double[] sigs = new double[rssi.length];
    for (int i = 0; i < rssi.length; i++) {
      if (rssi[i] > -100) sigs[i] = 99 - rssiToDistance(rssi[i]);
        //Double.valueOf(Math.abs(rssi[i]/5)).intValue()+1;
      else sigs[i] = 1;
    }
    return sigs;
  }

  double[] areaCoords(double areaX, double areaY, double areaWidth, double areaHeight) {
    return new double[]{(areaX + 1) * areaWidth - areaWidth / 2, (areaY + 1) * areaHeight - areaHeight / 2};
  }


  /**
   * mocking use
   */
  double[] rssisFromDistances(double[] distances) {
    double[] rssis = new double[distances.length];
    for (int i = 0; i < rssis.length; i++) {
      if (distances[i] > 21) {
        rssis[i] = 0;
        continue;
      }
      double expBase = (distances[i] - c) / m;
      double rssi = Math.exp(Math.log(expBase) / p) * T;
      rssis[i] = rssi < -100 ? 0 : rssi;
      if (distances[i] == 0) rssis[i] -= 1 + new Random().nextInt(5);
    }
    return rssis;
  }

  double rssiFromDistance(double distance) {
    if (distance > 10) return 0;
    double expBase = (distance - c) / m;
    double rssi = Math.exp(Math.log(expBase) / p) * T;
    if (distance == 0) rssi -= 1 + new Random().nextInt(5);
    return rssi;
  }

  int rssiToDistance(double rssi) {
    if (rssi == 0) return 99;
    double d = m * Math.pow(rssi / T, p) + c;
    return d < 1 ? 1 : (int) Math.round(d);
  }

  int randomRSSI(int high) {
    Random r = new Random(System.currentTimeMillis());
    return -1 - r.nextInt(high);
  }

  ArrayPointSet pointSetFromAreas(List<double[]> areas) {
    ArrayPointSet arrayPointSet = new ArrayPointSet(areas.get(0).length, areas.size());
    //use all the points in the area list and get a miniCircle center
    for (int i = 0; i < areas.size(); i++) {
      double[] area = areas.get(i);
      //uncomment following line to check selected areas
      //System.out.println(Arrays.toString(area));
      double[] coords = areaCoords(area[0], area[1], 1.0, 1.0);
      for (int j = 0; j < area.length; j++) {
        arrayPointSet.set(i, j, coords[j]);
      }
    }
    return arrayPointSet;
  }

  /**
   * @param rssi rssi from the single beacon
   * @return mini-ball encapsulating all related areas
   */
  public Miniball singleBeaconMiniball(double[] rssi){
    Object[][] realMatrix = (Object[][]) matrixes[0];
    Map<String, List<double[]>> signalMap = (Map<String, List<double[]>>) matrixes[1];
    double matrixSize = Math.sqrt(Math.sqrt(row * column));
    //assume device is at A(5,7), we have rssi
    //now we have a signal vector
    double[] sig = getSigs(rssi);
    //query the signal Smap and the area lists
    List<double[]> areas = signalMap.get(sigKey(sig));
    if (areas == null) {
      return null;
    }
        /* sort the area according to RSSI vector similarity */
    List<double[]> sortedAreas = areas.stream().sorted((area1, area2) ->
        Double.compare(Math.abs(rssi[0] - (Double) realMatrix[(int) area1[0]][(int) area1[1]]),
            Math.abs(rssi[0] - (Double) realMatrix[(int) area2[0]][(int) area2[1]])
        )
    ).collect(Collectors.toList());
    double[] exactArea = sortedAreas.get(0);
    ArrayPointSet arrayPointSet;
    if (rssiToDistance(rssi[0]) < 3 && areas.size() < matrixSize &&
        Math.abs((Double) realMatrix[(int) exactArea[0]][(int) exactArea[1]] - rssi[0]) < 5) {
      List<double[]> list = new ArrayList<>();
      list.add(exactArea);
      arrayPointSet = pointSetFromAreas(list);
    } else
      arrayPointSet = pointSetFromAreas(sortedAreas.subList(0, 3 > sortedAreas.size() / 3 ? sortedAreas.size() : 3));
    return new Miniball(arrayPointSet);
  }

  public Miniball doubleBeaconMiniball(double[] rssi){
    Object[][] rssiMatrix = (Object[][]) matrixes[0];
    Map<String, List<double[]>> signalMap = (Map<String, List<double[]>>) matrixes[1];
    double[] sig = getSigs(rssi);
    String sigKey = sigKey(sig);
        /* query the signal Smap and the area lists */
    List<double[]> areas = signalMap.get(sigKey);
    if (areas == null) {
            /* if no areas detected, check the cosine similarity of all the signal vectors */
      List<String> keys = topSimilarSigs(sig, signalMap.keySet());
      List<double[]> tmpAreas = new ArrayList<>();
      keys.forEach(k -> tmpAreas.addAll(signalMap.get(k)));
      areas = tmpAreas;
    } else {
      List<double[]> sortedAreas = areas.stream().sorted((area1, area2) ->
          Double.compare(
              cosineSim(rssi, (double[]) rssiMatrix[(int) area2[0]][(int) area2[1]]),
              cosineSim(rssi, (double[]) rssiMatrix[(int) area1[0]][(int) area1[1]])
          )
      ).collect(Collectors.toList());
      List<double[]> exactAreas = sortedAreas.stream().filter(area -> cosineSim(rssi, (double[]) rssiMatrix[(int) area[0]][(int) area[1]]) > .9)
          .collect(Collectors.toList());
      if (exactAreas.size() > 0) areas = exactAreas;
      else areas = sortedAreas.subList(0, sortedAreas.size() > 5 ? 5 : sortedAreas.size());
    }
    ArrayPointSet arrayPointSet = pointSetFromAreas(areas);
    return new Miniball(arrayPointSet);
  }

  public Miniball multiBeaconMiniball(double[] rssi){
    Object[][] rssiMatrix = (Object[][]) matrixes[0];
    Map<String, List<double[]>> signalMap = (Map<String, List<double[]>>) matrixes[1];
    //now we have a signal vector
    double[] sig = getSigs(rssi);
    //query the signal Smap and the area lists
    List<double[]> areas = signalMap.get(sigKey(sig));
    //if no areas detected, check the cosine similarity of all the signal vectors
    if (areas == null) {
      List<String> keys = topSimilarSigs(sig, signalMap.keySet());
      if (keys.isEmpty()) return null;
      List<double[]> tmpAreas = new ArrayList<>();
      keys.forEach(k -> tmpAreas.addAll(signalMap.get(k)));
      areas = new ArrayList<>(tmpAreas);
    } else {
      List<double[]> sortedAreas = areas.stream().sorted((area1, area2) ->
          Double.compare(
              cosineSim(rssi, (double[]) rssiMatrix[(int) area2[0]][(int) area2[1]]),
              cosineSim(rssi, (double[]) rssiMatrix[(int) area1[0]][(int) area1[1]])
          )
      ).collect(Collectors.toList());
      List<double[]> exactAreas = sortedAreas.stream().filter(area -> cosineSim(rssi, (double[]) rssiMatrix[(int) area[0]][(int) area[1]]) > .9)
          .collect(Collectors.toList());
      if (exactAreas.size() > 0) areas = exactAreas;
      else areas = sortedAreas.subList(0, sortedAreas.size() > 8 ? 8 : sortedAreas.size());
    }
    ArrayPointSet arrayPointSet = pointSetFromAreas(areas);
    return new Miniball(arrayPointSet);
  }

  public static void main(String[] args) {
    RssiDistributionMeasure measure = new RssiDistributionMeasure();
    double[] origin = measure.genRSSIMatrix(Lists.newArrayList(
        new double[]{113.943667, 22.529193}, //110
        new double[]{113.94365, 22.529074}, //119
        new double[]{113.943704, 22.529133} //112
    ));

    System.out.println(measure.rssiToDistance(-80));
    Miniball miniball = measure.multiBeaconMiniball(new double[]{-85, -88, -62});
    System.out.println(miniball.toString());
    System.out.println(origin[0] + 1e-5*miniball.center()[0]);
    System.out.println(origin[1] + 1e-5*miniball.center()[1]);
  }


}
