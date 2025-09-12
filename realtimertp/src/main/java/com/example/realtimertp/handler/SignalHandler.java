package com.example.realtimertp.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SignalHandler extends TextWebSocketHandler {

  private final ObjectMapper mapper = new ObjectMapper();

  // roomId -> set of sessions
  private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    System.out.println("🔌 New WebSocket connection: " + session.getId());
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    JsonNode node = mapper.readTree(message.getPayload());

    if (node.get("type") == null) {
      System.err.println("⚠️ Invalid message: no type");
      return;
    }

    String type = node.get("type").asText();

    // Handle join message
    if ("join".equals(type)) {
      String roomId = node.has("room") ? node.get("room").asText() : "default";
      rooms.putIfAbsent(roomId, Collections.newSetFromMap(new ConcurrentHashMap<>()));
      rooms.get(roomId).add(session);

      System.out.println("👥 " + session.getId() + " joined room " + roomId);
      return;
    }

    // Handle reject
    if ("call_reject".equals(type)) {
      String roomId = node.has("room") ? node.get("room").asText() : "default";
      Set<WebSocketSession> peers = rooms.get(roomId);
      if (peers != null) {
        for (WebSocketSession peer : peers) {
          if (peer.isOpen() && !peer.getId().equals(session.getId())) {
            peer.sendMessage(new TextMessage("{\"type\":\"call_reject\",\"from\":\"" + session.getId() + "\"}"));
          }
        }
      }
      return;
    }

    // Relay signaling messages (offer, answer, candidate)
    if (Arrays.asList("offer", "answer", "candidate").contains(type)) {
      String roomId = node.has("room") ? node.get("room").asText() : "default";

      Set<WebSocketSession> peers = rooms.get(roomId);
      if (peers == null) {
        System.err.println("⚠️ No peers in room " + roomId);
        return;
      }

      for (WebSocketSession peer : peers) {
        if (peer.isOpen() && !peer.getId().equals(session.getId())) {
          if ("offer".equals(type)) {
            // 🔔 Send call invite before actual offer
            String callInvite = String.format(
              "{\"type\":\"call_invite\",\"from\":\"%s\"}", session.getId()
            );
            peer.sendMessage(new TextMessage(callInvite));
          }

          // Forward original signaling message
          peer.sendMessage(new TextMessage(message.getPayload()));
        }
      }
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    rooms.values().forEach(set -> set.remove(session));
    System.out.println("❌ WebSocket closed: " + session.getId());
  }
}
