package com.fintech.hospital.api;

import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.Bracelet;
import com.fintech.hospital.domain.BraceletPhoto;
import com.google.common.collect.ImmutableMap;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author baoqiang
 */
@RestController
@RequestMapping("/bracelet")
public class BraceletRest {

  @Autowired
  private MongoDB mongo;

  @GetMapping
  public Object get(@RequestParam(value = "binded", required = false, defaultValue = "0") boolean binded) {
    List<Bracelet> bracelets = mongo.braceletList(binded);
    return ImmutableMap.of(
        "list", bracelets
    );
  }

  @PutMapping
  public Object bind(@Valid @RequestBody Bracelet bracelet, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return String.format("{\"status\": \"err\", \"error\":  \"%s\"}", bindingResult.getFieldError().getField());
    }
    Bracelet let = mongo.bindBracelet(bracelet);
    if (let == null) return "{\"status\": \"err\", \"error\": \"bracelet not found\"}";
    return let;
  }

  @PutMapping("/binded/{bid}")
  public Object bind(
      @PathVariable("bid") String bid,
      @Valid @RequestBody Bracelet bracelet, BindingResult bindingResult
  ) {
    if (bindingResult.hasErrors()) {
      return String.format("{\"status\": \"err\", \"error\": \"%s\"}", bindingResult.getFieldError().getField());
    }
    bracelet.setId(new ObjectId(bid));
    Bracelet let = mongo.unbindBracelet(bracelet);
    if (let == null) return "{\"status\": \"err\", \"error\": \"bracelet not found\"}";
    return let;
  }

  @GetMapping("/photos/{bracelet}")
  public Object photos(
      @PathVariable("bracelet") String braceletId
  ){
    List<BraceletPhoto> photos = mongo.braceletPhotos(braceletId);
    return ImmutableMap.of("list", photos);
  }

}
