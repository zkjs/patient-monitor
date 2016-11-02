package com.fintech.hospital.rssi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author baoqiang
 */
@Component
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
}
