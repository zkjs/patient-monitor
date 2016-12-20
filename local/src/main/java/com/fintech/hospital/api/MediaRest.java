package com.fintech.hospital.api;

import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.BraceletPhoto;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

/**
 * @author baoqiang
 */
@RestController
@RequestMapping("/photo")
public class MediaRest {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private MongoDB mongo;

  @PostMapping(value = "/{braceletId}", consumes = {IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE})
  public Object uploadPic(
      HttpServletRequest req, @PathVariable String braceletId,
      @RequestParam("ap") String apId, @RequestParam("time") long time
  ) {
    byte[] buffer = parseInput(req);
    Date shotTime = new Date(time);
    try {
      String filename = String.format("static/photos/%s/%s/%s.jpg",
          braceletId, apId, format(shotTime, "yyyy-MM-dd-HHmmss"));
      FileUtils.writeByteArrayToFile(new File(filename), buffer);
      mongo.addBraceletPhoto(new BraceletPhoto(braceletId, filename.replace("static/", ""), time));
    } catch (IOException ioe) {
      LOG.error("failed to save photo of {} shot by {} @{}", braceletId, apId, shotTime);
    }
    return new Object();
  }

  private byte[] parseInput(HttpServletRequest req) {
    try {
      return ByteStreams.toByteArray(req.getInputStream());
    } catch (IOException e) {
      LOG.warn("failed to read input bytes");
    }
    return null;
  }

}
