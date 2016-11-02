package com.fintech.hospital.push.supplier.yunba;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
@Component("yunbaRestSupplier4Mon")
@Scope(SCOPE_SINGLETON)
public class YunbaRestSupplier4Mon extends YunbaRestSupplier {

  @Autowired
  YunbaRestSupplier4Mon(@Value("${yunba.server.rest.url}") String yunbaServerUrl,
                        @Value("${yunba.appkey.mon}") String yunbaAppKey,
                        @Value("${yunba.appsec.mon}") String yunbaAppSec) {
    super(yunbaServerUrl, yunbaAppKey, yunbaAppSec);
  }

}
