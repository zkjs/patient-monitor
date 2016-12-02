package com.fintech.hospital.push.supplier.socketio;

import com.fintech.hospital.push.PushSupplier;
import com.fintech.hospital.push.model.PushMsg;
import com.fintech.hospital.push.socketio.SocketIOClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author baoqiang
 */
@Component("supplierSocketIO")
public class SocketIOSupplier extends SocketIOClient implements PushSupplier {

  protected SocketIOSupplier(@Value("${socket.io.remote.uri}") String serverUri,
                             @Value("${socket.io.app.key}") String appKey) {
    super(serverUri, appKey);
  }

  @Override
  public void publish(PushMsg pushMsg) {
    socket.emit(pushMsg.getSubject(), pushMsg.getMessage());
  }

}
