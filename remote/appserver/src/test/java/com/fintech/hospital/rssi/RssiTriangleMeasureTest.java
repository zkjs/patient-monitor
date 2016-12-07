package com.fintech.hospital.rssi;

import com.google.common.collect.Lists;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author baoqiang
 */
public class RssiTriangleMeasureTest {

  private RssiTriangleMeasure measure;

  @Before
  public void setup() {
    measure = new RssiTriangleMeasure(
        Lists.newArrayList(
            new Vector2D(0, 0),
            new Vector2D(0, 100),
            new Vector2D(100, 0)
        ),
        Lists.newArrayList(80.07, 160.0, 75.2),
        20,
        true
    );
  }

  @Test
  public void testPositioning(){
    Vector2D eval = measure.positioning();
    assertTrue(eval!=null);
  }


}