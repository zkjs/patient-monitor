package com.fintech.hospital.push.model;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.push.supplier.yunba.YunbaOpts;

/**
 * push message for <a href="yunba.io">Yunba</a> push service
 *
 * @author baoqiang
 * @since 0.0.3
 */
public class PushMsg {

  public PushMsg() {
  }

  public PushMsg(final PushType type, final String subject, final String message, final YunbaOpts opts) {
    this.type = type;
    this.subject = subject;
    this.message = message;
    this.opts = opts;
  }

  /**
   * push type: topic, alias
   */
  private PushType type;

  /**
   * message subject
   */
  private String subject;

  /**
   * message body
   */
  private String message;

  /**
   * options for official app push server, such as
   * <ol>
   * <li>apn_json: 如果不填，则不会发送APN. apn 消息中的alert用于在iOS中作消息提示, 可以选择只显示一条简单的提醒信息(<12字),
   * 或选择定制标题(<12字)和内容(<64字)</li>
   * <li>time_to_live: 用来设置离线消息保留多久。单位为秒, 默认值为 5 天，最大不超过 15 天</li>
   * <li>qos: 服务质量等级。有三种取值：“0”表示最多送达一次；“1”表示最少送达一次；“2”表示保证送达且仅送达一次。默认为1</li>
   * <li>platform、time_delay 和 location 参数暂未实现</li>
   * </ol>
   *
   * @see <a href="http://yunba.io/docs2/restful_Quick_Start/#HTTPPOST">Yunba Opts</a>
   * @see <a href="https://github.com/yunba/kb/blob/master/QoS.md">QoS</a>
   * @see <a href="https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html">iOS Push Notifications</a>
   * @see <a href="https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/TheNotificationPayload.html#//apple_ref/doc/uid/TP40008194-CH107-SW1">iOS APN Payload</a>
   */
  private YunbaOpts opts;

  public PushType getType() {
    return type;
  }

  public void setType(PushType type) {
    this.type = type;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public YunbaOpts getOpts() {
    return opts;
  }

  public void setOpts(YunbaOpts opts) {
    this.opts = opts;
  }

  @Override
  public String toString() {
    return JSON.toJSONString(this);
  }
}
