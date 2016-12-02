package com.fintech.hospital.push.consumer.socketio;

import com.fintech.hospital.push.PushConsumer;
import com.fintech.hospital.push.socketio.SocketIOClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
@Service("consumerLocalSocketIO")
@Scope(SCOPE_SINGLETON)
public class Socket4Remote extends SocketIOClient {

  protected Socket4Remote(@Value("${socket.io.server.uri}") String serverUri,
                          @Value("${socket.io.server.key}") String appKey) {
    super(serverUri, appKey);
  }

  @Autowired
  @Qualifier("PosConsumer")
  private PushConsumer consumer;

  @Value("${socket.io.server.event.position}")
  private String event;

  @Override
  protected void subEvents() {
    socket.on(event, args -> consumer.consume((String) args[0]));
  }


}
