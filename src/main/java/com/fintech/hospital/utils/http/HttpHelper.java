package com.fintech.hospital.utils.http;

import com.fintech.hospital.utils.http.Http.HttpResp;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;

public final class HttpHelper {

  public static HttpResp<String> get(String url, List<NameValuePair> params) {
    try {
      return Http.get(Http.parseUrl(url, params));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HttpResp<String> get(String url, List<NameValuePair> params, Header... headers) {
    try {
      return Http.get(Http.parseUrl(url, params), headers);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HttpResp<String> get(String url) {
    return get(url, null);
  }

  public static HttpResp<InputStream> getStream(String url, Header... headers) {
    try {
      return Http.getStream(url, headers);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HttpResp<String> post(String url, HttpEntity postBody) {
    try {
      return Http.post(url, postBody);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HttpResp<String> post(String url, HttpEntity postBody, Header... headers) {
    try {
      return Http.post(url, postBody, headers);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HttpResp<String> put(String url, HttpEntity postBody, Header... headers) {
    try {
      return Http.put(url, postBody, headers);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HttpResp<String> delete(String url, Header... headers) {
    try {
      return Http.delete(url, headers);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  public static HttpResp<String> post(String url, HttpEntity postBody, Iterable<Header> headers) {
    try {
      return Http.post(url, postBody, headers);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HttpResp<String> post(String url, List<NameValuePair> params) {
    try {
      return Http.post(url, params, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HttpResp<String> sslPost(String url, HttpEntity postBody) {
    try {
      return Http.sslPost(url, postBody);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HttpResp<String> sslPost(String url, HttpEntity postBody, Header... headers) {
    try {
      return Http.sslPost(url, postBody, headers);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static HttpResp<String> postWXPayment(String merId, String url, String xml) {
    try (InputStream inputStream = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(merId + "_apiclient_cert.p12"))) {
      KeyStore keyStore = KeyStore.getInstance("PKCS12");
      keyStore.load(inputStream, merId.toCharArray());
      SSLContext sslcontext = SSLContexts.custom()
          .loadKeyMaterial(keyStore, merId.toCharArray())
          .build();
            /* Allow TLSv1 protocol only */
      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
          sslcontext,
          new String[]{"TLSv1"},
          null,
          SSLConnectionSocketFactory.getDefaultHostnameVerifier());
      CloseableHttpClient httpClient = HttpClients.custom()
          .setSSLSocketFactory(sslsf)
          .build();
      StringEntity entity = new StringEntity(xml, StandardCharsets.UTF_8);
      entity.setContentType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
      return Http.sslPostWithClient(httpClient, url, entity);
    } catch (GeneralSecurityException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void addParameter(List<NameValuePair> params, String name, Object value) {
    if (value == null) {
      throw new IllegalArgumentException("param value of " + name + " cannot be null");
    }
    params.add(new BasicNameValuePair(name, value.toString()));
  }

  public static void addNotNullParameter(List<NameValuePair> params, String name, Object value) {
    if (value != null) {
      params.add(new BasicNameValuePair(name, value.toString()));
    }
  }

  public static void addTrueParameter(List<NameValuePair> params, String name, Boolean value) {
    if (Boolean.TRUE.equals(value)) {
      params.add(new BasicNameValuePair(name, value.toString()));
    }
  }

  public static Header basicHeader(String name, String value) {
    return new BasicHeader(name, value);
  }

  public static HttpEntity stringEntity(String body) {
    return new StringEntity(body, StandardCharsets.UTF_8);
  }

  public static HttpEntity stringEntity(String body, Charset charset) {
    return new StringEntity(body, charset);
  }

  public static HttpEntity streamEntity(InputStream stream) {
    return MultipartEntityBuilder.create()
        .addPart("file", new InputStreamBody(stream, ContentType.APPLICATION_OCTET_STREAM)).build();
  }

  public static String urlEncode(String param, Charset charset) {
    try {
      return URLEncoder.encode(param, charset.name());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

}
