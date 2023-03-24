package com.example.api1updaterspring;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketTextHandler extends TextWebSocketHandler {

  private static List<WebSocketSession> sessions = new CopyOnWriteArrayList<WebSocketSession>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws java.lang.Exception {
    System.out.println("afterConnectionEstablished");
    sessions.add(session);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws java.lang.Exception {
    System.out.println("afterConnectionClosed");
    sessions.remove(session);
    session.close();
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message)
      throws InterruptedException, IOException {

    String payload = message.getPayload();
    System.out.println(String.format("handleTextMessage() received payload '%s'", payload));

    broadcastJsonString(payload);
  }

  public void broadcastJsonString(String jsonString)
      throws InterruptedException, IOException {
    System.out.println(String.format("broadcastJsonString: '%s'", jsonString));
    for (WebSocketSession webSocketSession : sessions) {
      webSocketSession.sendMessage(new TextMessage(jsonString));
    }
  }

}