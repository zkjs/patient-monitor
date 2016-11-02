package com.fintech.hospital.utils.http;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * convenient http utilities based on apache fluent http client
 */
public class Http {

  private static final Logger LOG = LoggerFactory.getLogger(Http.class);

  private static final String[] AGENTS =
      new String[]{
          "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36",
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1664.3 Safari/537.36",
          "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (Windows NT 6.0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_5_8) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.66 Safari/535.11",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; WOW64; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; Media Center PC 5.0; .NET CLR 3.5.21022; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/4.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.5.30729; .NET CLR 3.0.30729; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/4.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.5.30729; InfoPath.2; .NET CLR 3.0.30729; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/4.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; OfficeLiveConnector.1.4; OfficeLivePatch.1.3; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; GTB6; .NET CLR 2.0.50727; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; GTB6; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 1.1.4322; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; GTB6.3; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 1.1.4322; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; GTB0.0; InfoPath.1; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 4.0.20506; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 1.1.4322; InfoPath.2; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 1.0.3705; .NET CLR 1.1.4322; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 1.1.4322; .NET CLR 3.0.04506.30; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; InfoPath.2; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 1.0.3705; .NET CLR 1.1.4322; Media Center PC 4.0; .NET CLR 2.0.50727; InfoPath.1; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 1.1.4322; GreenBrowser)",
          "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 1.1.4322; GreenBrowser)",
          "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36"};

  public static HttpResp<String> get(String uri, Header... headers) throws IOException {
    Request req = Request.Get(uri);
    Arrays.stream(headers).forEach(req::addHeader);
    return execute(req);
  }

  public static HttpResp<InputStream> getStream(String uri, Header... headers) throws IOException {
    Request req = Request.Get(uri);
    Arrays.stream(headers).forEach(req::addHeader);
    return executeStream(req);
  }

  public static HttpResp<String> delete(String uri, Header... headers) throws IOException {
    Request req = Request.Delete(uri);
    Arrays.stream(headers).forEach(req::addHeader);
    return execute(req);
  }

  public static HttpResp<String> put(String url, HttpEntity putBody, Header[] headers) throws IOException {
    Request req = Request.Put(url).body(putBody);
    Arrays.stream(headers).forEach(req::addHeader);
    return execute(req);
  }

  public static HttpResp<String> post(String uri, HttpEntity postBody) throws IOException {
    return post(uri, postBody, Lists.newArrayList());
  }

  public static HttpResp<String> post(String uri, HttpEntity postBody, Header... headers)
      throws IOException {
    Request req = Request.Post(uri);
    if (postBody != null) req = req.body(postBody);
    Arrays.stream(headers).forEach(req::addHeader);
    return execute(req);
  }

  public static HttpResp<String> post(String uri, HttpEntity postBody, Iterable<Header> headers)
      throws IOException {
    Request req = Request.Post(uri);
    if (postBody != null) req = req.body(postBody);
    StreamSupport.stream(headers.spliterator(), true).forEach(req::addHeader);
    return execute(req);
  }

  public static HttpResp<String> post(String uri, List<NameValuePair> params, Charset charset,
                                      Header... headers) throws IOException {
    Request req = Request.Post(uri).bodyForm(params, charset);
    Arrays.stream(headers).forEach(req::addHeader);
    return execute(req);
  }

  public static HttpResp<String> sslPostWithClient(HttpClient client, String uri, HttpEntity postBody)
      throws IOException {
    Request req = Request.Post(uri).body(postBody);
    Response resp = Executor.newInstance(client).execute(req);
    HttpResponse httpResp = resp.returnResponse();
    HttpEntity entity = httpResp.getEntity();
    return new HttpResp<>(httpResp.getStatusLine().getStatusCode(),
        new String(EntityUtils.toByteArray(entity), ObjectUtils.defaultIfNull(ContentType
            .getOrDefault(entity).getCharset(), UTF_8)));
  }

  public static HttpResp<String> sslPost(String uri, HttpEntity postBody) throws IOException {
    Request req = Request.Post(uri).body(postBody);
    Response resp = Executor.newInstance().execute(req);
    HttpResponse httpResp = resp.returnResponse();
    HttpEntity entity = httpResp.getEntity();
    return new HttpResp<>(httpResp.getStatusLine().getStatusCode(),
        new String(EntityUtils.toByteArray(entity), ObjectUtils.defaultIfNull(ContentType
            .getOrDefault(entity).getCharset(), UTF_8)));
  }

  public static HttpResp<String> sslPost(String uri, HttpEntity postBody, Header... headers) throws IOException {
    Request req = Request.Post(uri).body(postBody);
    Arrays.stream(headers).forEach(req::addHeader);
    Response resp = Executor.newInstance().execute(req);
    HttpResponse httpResp = resp.returnResponse();
    HttpEntity entity = httpResp.getEntity();
    return new HttpResp<>(httpResp.getStatusLine().getStatusCode(),
        new String(EntityUtils.toByteArray(entity), ObjectUtils.defaultIfNull(ContentType
            .getOrDefault(entity).getCharset(), UTF_8)));
  }

  private static HttpResp<String> execute(Request request) throws IOException {
    Response resp =
        request.connectTimeout(3000).socketTimeout(10000).userAgent(randomAgent())
            .execute();
    HttpResponse httpResp = resp.returnResponse();
    HttpEntity entity = httpResp.getEntity();
    if (entity == null) {
      entity = new StringEntity("");
      LOG.warn("empty entity for request {}, response code:{}", request, httpResp.getStatusLine());
    }
    HttpResp<String> respResult = new HttpResp<>(httpResp.getStatusLine().getStatusCode(),
        new String(EntityUtils.toByteArray(entity), ObjectUtils.defaultIfNull(ContentType
            .getOrDefault(entity).getCharset(), UTF_8)));
    if (httpResp.containsHeader("Set-Cookie")) {
          /* now only supports single set-cookie header */
      HttpCookie cookie = HttpCookie.parse(httpResp.getFirstHeader("Set-Cookie").toString()).get(0);
      respResult.appendCookie(cookie);
    }
    //respResult.appendHeaders(httpResp.getAllHeaders());
    return respResult;
  }

  private static HttpResp<InputStream> executeStream(Request request) throws IOException {
    Response resp =
        request.connectTimeout(3000).socketTimeout(10000).userAgent(randomAgent()).execute();
    HttpResponse httpResp = resp.returnResponse();
    HttpEntity entity = httpResp.getEntity();
    if (entity == null) {
      entity = new StringEntity("");
      LOG.warn("empty entity for request {}, response code:{}", request, httpResp.getStatusLine());
    }
    return new HttpResp<>(httpResp.getStatusLine().getStatusCode(), entity.getContent());
  }

  public static String randomAgent() {
    return AGENTS[RandomUtils.nextInt(0, AGENTS.length)];
  }

  public static String parseUrl(String uri, List<NameValuePair> params) {
    return uri + "?" + URLEncodedUtils.format(params, UTF_8);
  }

  public static class HttpResp<T> {
    public final int status;
    public final T resp;
    private HttpCookie cookie;
    private Header[] extraHeaders;

    public HttpResp(int status, T resp) {
      this.status = status;
      this.resp = resp;
    }

    public void appendCookie(HttpCookie cookie) {
      this.cookie = cookie;
    }

    public void appendHeaders(Header... headers) {
      List<Header> headerList = Arrays.stream(headers).filter(header ->
          !StringUtils.containsAny(header.getName(), "Content-Type", "Date", "Content-Length", "Server", "Connection"))
          .map(header -> {
            if (header.getName().equals("Set-Cookie")) {
              HttpCookie cookie = HttpCookie.parse(header.toString()).get(0);
              return HttpHelper.basicHeader("Cookie", cookie.toString());
            }
            return header;
          }).collect(Collectors.toList());
      Header[] resultHeaders = new Header[headerList.size()];
      headerList.toArray(resultHeaders);
      this.extraHeaders = resultHeaders;
    }

    public Header[] headers() {
      return this.extraHeaders;
    }

    public HttpCookie cookie() {
      return this.cookie;
    }
  }
}
