package com.fintech.hospital.push;


import com.fintech.hospital.push.model.PushMsg;

/**
 * all push service suppliers sdk entrance should implement this
 *
 * @author baoqiang
 * @since 0.0.3
 */
public interface PushSupplier {

  void publish(PushMsg pushMsg);

}
