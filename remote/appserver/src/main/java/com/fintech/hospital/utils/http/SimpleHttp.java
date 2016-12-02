package com.fintech.hospital.utils.http;

import com.google.common.collect.Lists;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * basic http request util for Http requests
 */
public final class SimpleHttp {

  public static String get(String uri, Header... headers) throws IOException {
    return Http.get(uri, headers).resp;
  }

  public static InputStream getStream(String uri, Header... headers) throws IOException {
    return Http.getStream(uri, headers).resp;
  }

  public static String delete(String uri, Header... headers) throws IOException {
    return Http.delete(uri, headers).resp;
  }

  public static String put(String url, HttpEntity putBody, Header[] headers) throws IOException {
    return Http.put(url, putBody, headers).resp;
  }

  public static String post(String uri, HttpEntity postBody) throws IOException {
    return Http.post(uri, postBody, Lists.newArrayList()).resp;
  }

  public static String post(String uri, HttpEntity postBody, Header... headers) throws IOException {
    return Http.post(uri, postBody, headers).resp;
  }

  public static String post(String uri, HttpEntity postBody, Iterable<Header> headers)
      throws IOException {
    return Http.post(uri, postBody, headers).resp;
  }

  public static String post(String uri, List<NameValuePair> params, Charset charset,
                            Header... headers) throws IOException {
    return Http.post(uri, params, charset, headers).resp;
  }

  public static String sslPostWithClient(HttpClient client, String uri, HttpEntity postBody)
      throws IOException {
    return Http.sslPostWithClient(client, uri, postBody).resp;
  }

  public static String sslPost(String uri, HttpEntity postBody) throws IOException {
    return Http.sslPost(uri, postBody).resp;
  }

  public static String sslPost(String uri, HttpEntity postBody, Header... headers)
      throws IOException {
    return Http.sslPost(uri, postBody, headers).resp;
  }

}
