package com.fintech.hospital.data;

import com.fintech.hospital.domain.*;
import com.fintech.hospital.domain.MapObj.MapPart;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author baoqiang
 */
@Component
public class MongoDB {

  final Logger LOG = LoggerFactory.getLogger(MongoDB.class);

  @Autowired
  @Qualifier("mapTemplate")
  private MongoTemplate template;

  @Value("${db.collection.bracelet.photo}")
  private String DB_BPHOTO;

  @Value("${db.collection.ap}")
  private String DB_AP;

  @Value("${db.collection.bracelet.trace}")
  private String DB_BT;

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

  public void braceletDropped(Bracelet bracelet) {
    WriteResult result = template.updateFirst(
        new Query(where("_id").is(bracelet.getId())),
        new Update().set("status", 2),
        Bracelet.class,
        DB_BRACELET
    );
    LOG.debug("{} bracelet(s) dropped", result.getN());
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

  public List<AP> getAPByNames(List<String> aps) {
    return template.find(
        new Query(where("name").in(aps)),
        AP.class,
        DB_AP
    );
  }

  public void addBraceletTrace(String bracelet, BraceletTrace trace) {
    /* add new trace to bracelet positions */
    trace.setBracelet(bracelet);
    template.insert(trace, DB_BT);
    LOG.info("{} new trace added for {}", trace.getId(), bracelet);
  }

  public void addBraceletPhoto(BraceletPhoto photo) {
    template.insert(photo, DB_BPHOTO);
  }

  public void addBraceletPhotos(List<BraceletPhoto> photos) {
    template.insert(photos, DB_BPHOTO);
  }

  public List<BraceletPhoto> braceletPhotos(String braceletId) {
    return template.find(
        new Query(where("bracelet").is(new ObjectId(braceletId)))
            .with(new PageRequest(0, 30, DESC, "_id")),
        BraceletPhoto.class, DB_BPHOTO
    );
  }

  public void updateAPStatus(APStatus apStatus) {
    template.updateFirst(new Query(where("name").is(apStatus.getBssid())),
        new Update().set("current", apStatus), AP.class, DB_AP);
  }

  public List<AP> apList() {
    return template.find(
        new Query(where("status").is(1)),
        AP.class,
        DB_AP
    );
  }

  public boolean updateAP(AP ap) {
    Update update = new Update()
        .set("triggers", ap.getTriggers())
        .set("camera", ap.getCamera())
        .set("update", new Date());
    if (ap.shotEnabled()) {
      update.set("triggerLogic", ap.getTriggerLogic());
    } else {
      update.unset("triggerLogic");
    }
    WriteResult result = template.updateFirst(
        new Query(where("_id").is(ap.getId())),
        update,
        AP.class,
        DB_AP
    );
    return result.getN() == 1;
  }

  List<AP> getAPCameras() {
    return template.find(
        new Query(where("camera").is(1)),
        AP.class,
        DB_AP
    );
  }

  public void addUntrackedBraclet(String bandId, String mac) {
    Bracelet bracelet = new Bracelet(bandId, mac);
    template.insert(bracelet, DB_BRACELET);
  }

}
