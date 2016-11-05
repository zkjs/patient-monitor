package com.fintech.hospital.api;

import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.AP;
import com.fintech.hospital.domain.BraceletPosition;
import com.fintech.hospital.domain.TimedPosition;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.fintech.hospital.rssi.RssiMeasure.transform2RelativeCoords;

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
    BraceletPosition position = mongo.getBraceletTrack(bracelet);
    if (position == null) return "{'status': 'err', 'error': 'bracelet not found'}";
    return ImmutableMap.of(
        "list", position.getPosition()
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
    BraceletPosition position = mongo.getBraceletTrack(bracelet);
    if (position == null) return "{'status': 'err', 'error': 'bracelet not found'}";
    List<String> aps = position.getPosition().stream().map(TimedPosition::getAp).distinct().collect(Collectors.toList());
    List<AP> apList = mongo.getAPByNames(aps);
    transform2RelativeCoords(position.getPosition(), apList);
    return ImmutableMap.of(
        "list", position.getPosition(),
        "aps", apList
    );
  }


}


