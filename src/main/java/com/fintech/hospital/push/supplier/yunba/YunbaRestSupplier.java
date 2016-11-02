package com.fintech.hospital.push.supplier.yunba;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.fintech.hospital.push.PushSupplier;
import com.fintech.hospital.push.model.PushMsg;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.fintech.hospital.utils.http.HttpHelper.basicHeader;
import static com.fintech.hospital.utils.http.SimpleHttp.post;


/**
 * @author baoqiang
 */
abstract class YunbaRestSupplier implements PushSupplier {

  final Logger LOG;

  YunbaRestSupplier(String yunbaServerUrl, String yunbaAppKey, String yunbaAppSec) {
    LOG = LoggerFactory.getLogger(this.getClass());
    this.yunbaServerUrl = yunbaServerUrl;
    this.yunbaAppKey = yunbaAppKey;
    this.yunbaAppSec = yunbaAppSec;
  }

  private String yunbaServerUrl;
  private String yunbaAppKey;
  private String yunbaAppSec;

  public void publish(PushMsg pushMsg) {
    JSONObject postBody = new JSONObject();
    postBody.put("appkey", yunbaAppKey);
    postBody.put("seckey", yunbaAppSec);
    postBody.put("msg", pushMsg.getMessage());
    postBody.put("opts", pushMsg.getOpts());
    postBody.put(pushMsg.getType().type(), pushMsg.getSubject());
    switch (pushMsg.getType()) {
      case ALIAS:
        postBody.put("method", "publish_to_alias");
        break;
      case BROADCAST:
        postBody.put("method", "publish");
        break;
      default:
        LOG.warn("unsupported push msg type: {}", pushMsg);
        break;
    }
    try {
      final String resp = post(yunbaServerUrl, new StringEntity(postBody.toString(), StandardCharsets.UTF_8),
          basicHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType()));
      LOG.debug("yunba rest push result: {}", resp);
    } catch (IOException e) {
      LOG.error("failed to push yunba msg {}: {}", postBody, e);
    }
  }

  public List<String> publishBatch(List<String> targets, PushMsg pushMsg) {
    if (targets == null)
      throw new IllegalArgumentException("empty targets not allowed!");
    List<String> pushed = new ArrayList<>();
    LOG.info("ready to broadcast activity msg to {} users", targets.size());
    if (targets.size() < 900)
      pushed.addAll(batchPush(targets, pushMsg));
    else {
      ExecutorService executorService = Executors.newWorkStealingPool(3);
      List<Future<List<String>>> pushFutures = new ArrayList<>(targets.size() / 900);
      for (int i = 0; i < targets.size(); i += 900) {
        int rightIndex = i + 900;
        List<String> targetsSubgroup = targets.subList(i, rightIndex > targets.size() ? targets.size() : rightIndex);
        pushFutures.add(executorService.submit(() -> batchPush(targetsSubgroup, pushMsg)));
      }
      pushFutures.parallelStream().forEach(f -> {
        try {
          pushed.addAll(f.get());
        } catch (Exception e) {
          LOG.error("failed to push targets: ", e.getMessage());
        }
      });
    }
    return pushed;
  }

  /**
   * @param targets alias push targets
   * @param pushMsg msg body and args
   * @return pushed targets
   * @see <a href="http://yunba.io/docs2/restful_Quick_Start/#publish_to_alias_batch">yunba#alias_batch</a>
   */
  @NotNull
  private List<String> batchPush(List<String> targets, PushMsg pushMsg) {
    List<String> pushedTargets = new ArrayList<>();
    JSONObject postBody = new JSONObject();
    postBody.put("appkey", yunbaAppKey);
    postBody.put("seckey", yunbaAppSec);
    postBody.put("msg", pushMsg.getMessage());
    postBody.put("opts", pushMsg.getOpts());
    JSONArray aliases = new JSONArray();
    aliases.addAll(targets);
    postBody.put("aliases", aliases);
    postBody.put("method", "publish_to_alias_batch");
    try {
      final String resp = post(yunbaServerUrl, new StringEntity(postBody.toString(), StandardCharsets.UTF_8),
          basicHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType()));
      LOG.debug("yunba rest batch push result: {}", resp);
      if (StringUtils.isBlank(resp)) {
        LOG.warn("response unknown from yunba rest api");
        return new ArrayList<>();
      }
      YunbaRestResp yunbaRestResp = JSON.parseObject(resp, YunbaRestResp.class);
      yunbaRestResp.getResults().entrySet().stream()
          .filter(e -> ((JSONObject) e.getValue()).getIntValue("status") == 0)
          .forEach(e -> pushedTargets.add(e.getKey()));
    } catch (IOException | JSONException e) {
      LOG.error("failed to push yunba msg {}: {}", postBody, e);
    }
    return pushedTargets;
  }

}
