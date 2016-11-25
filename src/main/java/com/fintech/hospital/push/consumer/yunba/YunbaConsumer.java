package com.fintech.hospital.push.consumer.yunba;

import com.alibaba.fastjson.JSONObject;
import com.fintech.hospital.push.PushConsumer;
import com.fintech.hospital.push.yunba.YunbaIO;
import io.socket.IOAcknowledge;

/**
 * @author baoqiang
 */
abstract class YunbaConsumer extends YunbaIO implements PushConsumer {

  protected YunbaConsumer(String yunbaServerUrl, String yunbaAppKey) {
    super(yunbaServerUrl, yunbaAppKey);
  }

  @Override
  public void onMessage(String data, IOAcknowledge ack) {
    LOG.debug("msg: {}", data);
    consume(data);
  }

  @Override
  public void onMessage(JSONObject jsonObject, IOAcknowledge ioAcknowledge) {
    LOG.debug("msg from {}: {}", jsonObject.getString("topic"), jsonObject.toString());
    consume(jsonObject.getString("msg"));
  }


}
