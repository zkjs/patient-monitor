package com.fintech.hospital.data;

import com.fintech.hospital.domain.*;
import com.fintech.hospital.domain.MapObj.MapPart;
import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author baoqiang
 */
@Component
public class MongoDB {

  final Logger LOG = LoggerFactory.getLogger(MongoDB.class);

  @Autowired
  private MongoTemplate template;

  @Value("${db.collection.ap}")
  private String DB_AP;

  @Value("${db.collection.bracelet}")
  private String DB_BRACELET;

  @Value("${db.collection.map.obj}")
  private String DB_MAPOBJ;

  @Value("${db.collection.map.part}")
  private String DB_MAPPART;

  @Value("${db.collection.map.drawable}")
  private String DB_MAPDRAW;

  public AP getAP(String apid) {
    return template.findOne(new Query(where("name").is(apid)), AP.class, DB_AP);
  }

  public List<Bracelet> braceletList(boolean binded) {
    return template.find(
        new Query(where("status").is(binded ? 1 : 0)),
        Bracelet.class,
        DB_BRACELET
    );
  }

  public Bracelet getBracelet(String idInBLE) {
    return template.findOne(
        new Query(where("name").is(idInBLE)),
        Bracelet.class,
        DB_BRACELET
    );
  }

  public Bracelet bindBracelet(Bracelet fromPatient) {
    Bracelet let = template.findOne(
        new Query(where("_id").is(fromPatient.getId()).and("status").is(0)),
        Bracelet.class,
        DB_BRACELET
    );
    if (let == null) return null;
    let.bindPatient(fromPatient);
    template.save(let, DB_BRACELET);
    return let;
  }

  public Bracelet unbindBracelet(Bracelet bracelet) {
    Bracelet let = template.findOne(
        new Query(
            where("_id").is(bracelet.getId())
                .and("status").is(1)
                .and("patientName").is(bracelet.getPatientName())
                .and("patientDBGender").is(bracelet.getPatientDBGender())
        ),
        Bracelet.class,
        DB_BRACELET
    );
    if (let == null) return null;
    let.unbindPatient();
    template.save(let, DB_BRACELET);
    return let;
  }

  public List<MapObj> mapObjs() {
    List<MapObj> mapObjs = template.findAll(MapObj.class, DB_MAPOBJ);

    List<MapDrawable> drawables = template.find(
        new Query(Criteria.where("part").in(mapObjs.stream().map(MapObj::getId).collect(Collectors.toList())))
            .with(new Sort(Sort.Direction.ASC, "part")),
        MapDrawable.class,
        DB_MAPDRAW
    );

    for (MapObj obj : mapObjs) {
      Iterator<MapDrawable> drawableIterator = drawables.iterator();
      while (drawableIterator.hasNext()) {
        MapDrawable drawable = drawableIterator.next();
        if (drawable.getPart().equals(obj.getId())) {
          obj.addDrawable(drawable);
          drawableIterator.remove();
        }
      }
    }

    return mapObjs;
  }

  public List<MapPart> mapParts(String objId) {
    List<MapPart> parts = template.find(
        new Query(Criteria.where("owner").is(new ObjectId(objId)))
            .with(new Sort(Sort.Direction.ASC, "id")),
        MapPart.class,
        DB_MAPPART
    );
    List<MapDrawable> drawables = template.find(
        new Query(Criteria.where("part").in(parts.stream().map(MapPart::getId).collect(Collectors.toList())))
            .with(new Sort(Sort.Direction.ASC, "part")),
        MapDrawable.class,
        DB_MAPDRAW
    );

    for (MapPart part : parts) {
      Iterator<MapDrawable> drawableIterator = drawables.iterator();
      while (drawableIterator.hasNext()) {
        MapDrawable drawable = drawableIterator.next();
        if (drawable.getPart().equals(part.getId())) {
          part.addDrawable(drawable);
          drawableIterator.remove();
        }
      }
    }

    return parts;

  }
}
