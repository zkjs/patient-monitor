package com.fintech.hospital.push.supplier.yunba;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * <b>DO NOT MODIFY THIS FILE: this is a direct copy from URSAMAJOR, refactor needed</b>
 */
@Component("yunbaSupplier4Mon")
@Scope(SCOPE_SINGLETON)
public class YunbaSupplier4Mon extends YunbaSupplier {

  @Autowired
  YunbaSupplier4Mon(@Value("${yunba.server.url}") String yunbaServerUrl,
                    @Value("${yunba.appkey.mon}") String yunbaAppKey) {
    super(yunbaServerUrl, yunbaAppKey);
  }

}
