package com.fintech.hospital;


import com.fintech.hospital.base.FastJsonHttpMsgConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import static com.alibaba.fastjson.serializer.SerializerFeature.*;

@SpringBootApplication
@EnableScheduling
public class Application extends SpringBootServletInitializer {

  static final Logger LOG = LoggerFactory.getLogger(Application.class);

  /**
   * only for war package,
   * refer to <a href="http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-create-a-deployable-war-file">SpringBoot: create a deployable war file</a>
   */
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(Application.class);
  }

  @Bean
  public HttpMessageConverters jsonConverter() {
    FastJsonHttpMsgConverter fastjsonConverter = new FastJsonHttpMsgConverter();
    fastjsonConverter.setFeatures(WriteNullListAsEmpty, WriteNullStringAsEmpty, DisableCircularReferenceDetect);
    return new HttpMessageConverters(fastjsonConverter);
  }

  public static void main(String[] args) {
    LOG.debug("about to run this app...");
    SpringApplication.run(Application.class, args);
    LOG.debug("app now running!");
  }

}