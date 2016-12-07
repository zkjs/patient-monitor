package com.fintech.hospital.api;

import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.AP;
import com.fintech.hospital.domain.BraceletPosition;
import com.fintech.hospital.domain.LngLat;
import com.fintech.hospital.domain.TimedPosition;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.fintech.hospital.rssi.RssiMeasure.transform2RelativeCoords;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author baoqiang
 */
@RestController
@RequestMapping("/track")
public class BraceletTrackRest {

  @Autowired
  private MongoDB mongo;

  @GetMapping("{bid}")
  public Object tracks(@PathVariable("bid") String bracelet) {
    if (bracelet == null || bracelet.length() < 23) bracelet = "581b1a6542aa101eebc77e61";
    BraceletPosition position = mongo.getBraceletTrack(bracelet);

    if (position == null) return "{'status': 'err', 'error': 'bracelet not found'}";

    List<TimedPosition> lastestDiffPosList = position.getPosition().stream()
        .collect(groupingBy(timedPosition -> {
          LngLat lnglat = timedPosition.getGps();
          return String.format("%.1f-%.1f", lnglat.getLng(), lnglat.getLat());
        })).values().stream().map(c -> c.get(0))
        .sorted((p1,p2)-> Long.compare(p2.getTimestamp(), p1.getTimestamp()))
        .collect(Collectors.toList());
    return ImmutableMap.of(
        "list", lastestDiffPosList.subList(0, Math.min(lastestDiffPosList.size(), 20))
    );
  }

  @GetMapping("{bid}/last")
  public Object lastPosition(@PathVariable("bid") String bracelet) {
    BraceletPosition position = mongo.getBraceletTrack(bracelet);
    if (position == null) return "{'status': 'err', 'error': 'bracelet not found'}";
    TimedPosition pos = position.getPosition().get(position.getPosition().size() - 1);
    AP ap = mongo.getAP(pos.getAp());
    return ap == null ? ImmutableMap.of(
        "timestamp", pos.getTimestamp(),
        "gps", pos.getGps()
    ) : ImmutableMap.of(
        "address", ap.getAddress() == null ? "" : ap.getAddress(),
        "floor", ap.getFloor() == null ? "" : ap.getFloor(),
        "timestamp", pos.getTimestamp(),
        "gps", pos.getGps()
    );
  }

  @GetMapping("rel/{bid}")
  public Object tracksRelativeCoords(@PathVariable("bid") String bracelet) {
    if (bracelet == null || bracelet.length() < 23) bracelet = "581b1a6542aa101eebc77e61";
    BraceletPosition position = mongo.getBraceletTrack(bracelet);
    if (position == null) return "{'status': 'err', 'error': 'bracelet not found'}";
    List<String> aps = mongo.tracedAP(bracelet);
    List<AP> apList = mongo.getAPByNames(aps);
    smooth(position.getPosition());
    transform2RelativeCoords(position.getPosition(), apList);
    return ImmutableMap.of(
        "list", position.getPosition(),
        "aps", apList
    );
  }

  private void smooth(List<TimedPosition> positions) {
    for (int i = 0; i < positions.size(); i += 3) {
      if (i + 3 >= positions.size()) break;
      TimedPosition mean = TimedPosition.mean(positions.subList(i, i + 3), null);
      Vector2D meanV = positions.get(i).getGps().vector().add(mean.getGps().vector()).scalarMultiply(0.5);
      positions.get(i).setGps(new LngLat(meanV.getX(), meanV.getY()));
      meanV = positions.get(i + 1).getGps().vector().add(mean.getGps().vector()).scalarMultiply(0.5);
      positions.get(i + 1).setGps(new LngLat(meanV.getX(), meanV.getY()));
      meanV = positions.get(i + 2).getGps().vector().add(mean.getGps().vector()).scalarMultiply(0.5);
      positions.get(i + 2).setGps(new LngLat(meanV.getX(), meanV.getY()));
    }

    if (positions.size() > 15) {
      for (int i = 0; i < positions.size(); i += 5) {
        if (i + 5 >= positions.size()) break;
        TimedPosition mean = TimedPosition.mean(positions.subList(i, i + 5), null);
        Vector2D meanV = positions.get(i).getGps().vector().add(mean.getGps().vector()).scalarMultiply(0.5);
        positions.get(i).setGps(new LngLat(meanV.getX(), meanV.getY()));
        meanV = positions.get(i + 1).getGps().vector().add(mean.getGps().vector()).scalarMultiply(0.5);
        positions.get(i + 1).setGps(new LngLat(meanV.getX(), meanV.getY()));
        meanV = positions.get(i + 2).getGps().vector().add(mean.getGps().vector()).scalarMultiply(0.5);
        positions.get(i + 2).setGps(new LngLat(meanV.getX(), meanV.getY()));
        meanV = positions.get(i + 3).getGps().vector().add(mean.getGps().vector()).scalarMultiply(0.5);
        positions.get(i + 3).setGps(new LngLat(meanV.getX(), meanV.getY()));
        meanV = positions.get(i + 4).getGps().vector().add(mean.getGps().vector()).scalarMultiply(0.5);
        positions.get(i + 5).setGps(new LngLat(meanV.getX(), meanV.getY()));
      }
    }

  }


}


