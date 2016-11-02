package com.fintech.hospital.push;

import com.alibaba.fastjson.JSONObject;

/**
 * @author baoqiang
 */
public interface PushConsumer {

  void consume(String msg);

}
