package com.fintech.hospital.data;

import com.fintech.hospital.domain.*;
import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;

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

  @Value("${db.collection.bracelet.position}")
  private String DB_BP;

  @Value("${db.collection.bracelet.trace}")
  private String DB_BT;

  @Value("${db.collection.bracelet}")
  private String DB_BRACELET;

  public AP getAP(String apid) {
    return template.findOne(new Query(where("name").is(apid)), AP.class, DB_AP);
  }

  public void addBraceletTrace(String bracelet, BraceletTrace trace) {
    /* add new trace to bracelet positions */
    trace.setBracelet(bracelet);
    template.insert(trace, DB_BT);
    LOG.info("{} new trace added for {}", trace.getId(), bracelet);
  }

  public void addBraceletPosition(String bracelet, TimedPosition pos) {
    template.upsert(
        new Query(where("_id").is(new ObjectId(bracelet))),
        new Update().push("position", pos),
        BraceletPosition.class,
        DB_BP
    );
  }

  public BraceletPosition getBraceletTrack(String bracelet) {
    long _30minAgo = Instant.now().minus(30, ChronoUnit.MINUTES).toEpochMilli();
    Query query = new Query(where("_id").is(bracelet).and("create").gt(_30minAgo));
    query.fields().slice("position", -30, 15);
    return template.findOne(
        query,
        BraceletPosition.class,
        DB_BP
    );
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

  public List<AP> getAPByNames(List<String> aps) {
    return template.find(
        new Query(where("name").in(aps)),
        AP.class,
        DB_AP
    );
  }

  private final AggregationOperation GROUP_BY_AP = c -> c.getMappedObject(
      new BasicDBObject("$group", new BasicDBObject("_id", "$ap"))
  );

  public List<String> tracedAP(String bracelet) {

    long _30minAgo = Instant.now().minus(30, ChronoUnit.MINUTES).toEpochMilli();
    Criteria matchingCriteria = Criteria.where("bracelet").is(bracelet)
        .and("create_on").gt(new Date(_30minAgo));
    MatchOperation matchBracelet = new MatchOperation(matchingCriteria);
    return template.aggregate(Aggregation.newAggregation(matchBracelet, GROUP_BY_AP), DB_BT, String.class).getMappedResults();
  }
}
