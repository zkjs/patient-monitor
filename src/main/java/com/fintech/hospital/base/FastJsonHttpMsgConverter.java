package com.fintech.hospital.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author baoqiang
 * @since 0.11
 */
public class FastJsonHttpMsgConverter extends AbstractHttpMessageConverter<Object> {

  public final static Charset UTF8 = Charset.forName("UTF-8");

  private Charset charset = UTF8;

  private SerializerFeature[] features = new SerializerFeature[0];

  private SerializeFilter[] filters = new SerializeFilter[0];

  public FastJsonHttpMsgConverter() {
    super(new MediaType("application", "json", UTF8), new MediaType("application", "*+json", UTF8));
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return clazz != String.class;
  }

  public Charset getCharset() {
    return this.charset;
  }

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  public SerializerFeature[] getFeatures() {
    return features;
  }

  public void setFeatures(SerializerFeature... features) {
    this.features = features;
  }

  public void setFilters(SerializeFilter... filters) {
    this.filters = filters;
  }


  @Override
  protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException,
      HttpMessageNotReadableException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    InputStream in = inputMessage.getBody();

    byte[] buf = new byte[1024];
    for (; ; ) {
      int len = in.read(buf);
      if (len == -1) {
        break;
      }

      if (len > 0) {
        baos.write(buf, 0, len);
      }
    }

    byte[] bytes = baos.toByteArray();
    return JSON.parseObject(bytes, 0, bytes.length, charset.newDecoder(), clazz);
  }

  private static final ValueFilter OBJECTID_FILTER = (object, name, value) -> {
    //if (value instanceof ObjectId) return ((ObjectId) value).toHexString();
    return value;
  };

  @Override
  protected void writeInternal(Object obj, HttpOutputMessage outputMessage) throws IOException,
      HttpMessageNotWritableException {
    OutputStream out = outputMessage.getBody();
    SerializeFilter[] serializeFilters = Arrays.copyOf(filters, filters.length + 1);
    serializeFilters[filters.length] = OBJECTID_FILTER;
    String text = JSON.toJSONString(obj, serializeFilters, features);

    byte[] bytes = text.getBytes(charset);
    out.write(bytes);
  }

}
