package com.fintech.hospital.push.consumer.socketio;

import com.fintech.hospital.data.Cache;
import com.fintech.hospital.push.socketio.SocketIOClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
@Service("consumerAppSocketIO")
@Scope(SCOPE_SINGLETON)
public class Socket4App extends SocketIOClient {

  protected Socket4App(@Value("${socket.io.app.uri}") String serverUri,
                       @Value("${socket.io.app.key}") String appKey) {
    super(serverUri, appKey);
  }

  @Value("${socket.io.app.event.sub}")
  private String sub;

  @Value("${socket.io.app.event.unsub}")
  private String unsub;

  @Autowired
  private Cache cache;

  @Override
  protected void subEvents() {
    socket.on(sub, args -> {
      //TODO record new sub on bracelet
      LOG.info("{} new sub on {}", socket.id(), args);
      cache.sub((String) args[0]);
    });
    socket.on(unsub, args -> {
      //TODO record unsub on bracelet
      LOG.info("{} no longer cares about {}", socket.id(), args);
      cache.unsub((String) args[0]);
    });
  }


}
