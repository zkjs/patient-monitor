package com.fintech.hospital.api;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

/**
 * @author baoqiang
 */
@RestController
@RequestMapping("/photo")
public class MediaRest {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @PostMapping(value = "/{braceletId}", consumes = {IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE})
  public Object uploadPic(
      HttpServletRequest req, HttpServletResponse resp,
      @PathVariable String braceletId,
      @RequestParam("ap") String apId,
      @RequestParam("time") long time
  ) {
    //TODO save photo somewhere
    byte[] buffer = parseInput(req);
    Date shotTime = new Date(time);
    String filename = String.format("pmphotos/%s/%s/%s.jpg", braceletId, apId, DateFormatUtils.format(shotTime, "yyyy-MM-dd"));
    try {
      FileUtils.writeByteArrayToFile(new File(filename), buffer);
    }catch (IOException ioe){
      LOG.error("failed to save photo of {} shot by {} @{}", braceletId, apId, shotTime);
    }
    return null;
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
