package com.fintech.hospital.domain;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * @author baoqiang
 */
public class MapObj {

  @Id
  private ObjectId id;
  private String title;
  private Integer floors;
  @Field("longitude")
  private Double lng;
  @Field("latitude")
  private Double lat;
  private List<MapDrawable> drawables = new ArrayList<>();

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Integer getFloors() {
    return floors;
  }

  public void setFloors(Integer floors) {
    this.floors = floors;
  }

  public Double getLng() {
    return lng;
  }

  public void setLng(Double lng) {
    this.lng = lng;
  }

  public Double getLat() {
    return lat;
  }

  public void setLat(Double lat) {
    this.lat = lat;
  }

  public List<MapDrawable> getDrawables() {
    return drawables;
  }

  public void setDrawables(List<MapDrawable> drawables) {
    this.drawables = drawables;
  }

  public void addDrawable(MapDrawable drawable) {
    this.drawables.add(drawable);
  }

  public static class MapPart {
    @Id
    private ObjectId id;
    private Integer floor;
    private List<MapDrawable> drawables = new ArrayList<>();

    public ObjectId getId() {
      return id;
    }

    public void setId(ObjectId id) {
      this.id = id;
    }

    public Integer getFloor() {
      return floor;
    }

    public void setFloor(Integer floor) {
      this.floor = floor;
    }

    public List<MapDrawable> getDrawables() {
      return drawables;
    }

    public void setDrawables(List<MapDrawable> drawables) {
      if (drawables == null) return;
      this.drawables = drawables;
    }

    public void addDrawable(MapDrawable drawable) {
      this.drawables.add(drawable);
    }
  }
}
