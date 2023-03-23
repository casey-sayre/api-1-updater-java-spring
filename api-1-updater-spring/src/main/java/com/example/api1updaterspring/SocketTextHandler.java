package com.example.api1updaterspring;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketTextHandler extends TextWebSocketHandler {

  List<WebSocketSession> sessions = new CopyOnWriteArrayList<WebSocketSession>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws java.lang.Exception {
    System.out.println("afterConnectionEstablished");
    sessions.add(session);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws java.lang.Exception {
    System.out.println("afterConnectionClosed");
    sessions.remove(session);
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message)
      throws InterruptedException, IOException {

    String payload = message.getPayload();
    System.out.println(String.format("handleTextMessage() received payload '%s'", payload));
    // JSONObject jsonObject = new JSONObject(payload);
    // session.sendMessage(new TextMessage("Hi " + jsonObject.get("user") + " how
    // may we help you?"));

    // broadcast
    for (WebSocketSession webSocketSession : sessions) {
      webSocketSession.sendMessage(new TextMessage(payload));
    }
  }

}