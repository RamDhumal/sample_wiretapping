<script setup>
import { ref } from "vue";

const wsUrl = "wss://140.238.254.38/ws/signal";
const roomId = "123";
let ws;
let pc;
let pendingCandidates = [];

const localStream = ref(null);
const remoteAudio = ref(null);

async function connectWS() {
  ws = new WebSocket(wsUrl);

  ws.onopen = () => console.log("✅ WebSocket connected");

  ws.onmessage = async (event) => {
    const msg = JSON.parse(event.data);
    console.log("📩 Got WS message:", msg);

    if (msg.type === "offer") {
      await initializePeerConnection();
      await pc.setRemoteDescription({ type: "offer", sdp: msg.sdp });

      // ✅ Flush buffered ICE after remoteDescription is set
      await flushBufferedCandidates();

      const answer = await pc.createAnswer();
      await pc.setLocalDescription(answer);
      ws.send(JSON.stringify({ type: "answer", room: roomId, sdp: answer.sdp }));
      console.log("📤 Sent answer");
    }

    if (msg.type === "answer") {
      await pc.setRemoteDescription({ type: "answer", sdp: msg.sdp });
      console.log("✅ Answer set as remote description");

      // ✅ Flush buffered ICE after remoteDescription is set
      await flushBufferedCandidates();
    }

    if (msg.type === "candidate") {
      const candidate = new RTCIceCandidate(msg.candidate);
      if (pc && pc.remoteDescription) {
        try {
          await pc.addIceCandidate(candidate);
          console.log("✅ Added remote ICE candidate");
        } catch (e) {
          console.error("❌ ICE add error", e);
        }
      } else {
        console.warn("⚠️ PC not ready yet, buffering candidate");
        pendingCandidates.push(candidate);
      }
    }
  };

  ws.onclose = () => console.warn("❌ WS closed");
  ws.onerror = (err) => console.error("WS error", err);
}

async function initializePeerConnection() {
  if (pc) return;

  try {
    localStream.value = await navigator.mediaDevices.getUserMedia({ audio: true });
  } catch (err) {
    console.error("❌ Cannot access microphone:", err);
    alert("Microphone access failed: " + err.message);
    return;
  }

  pc = new RTCPeerConnection({
    iceServers: [{ urls: "stun:stun.l.google.com:19302" }]
  });

  localStream.value.getTracks().forEach((track) => pc.addTrack(track, localStream.value));

  pc.ontrack = (event) => {
    if (remoteAudio.value) {
      remoteAudio.value.srcObject = event.streams[0];
      console.log("🎧 Remote audio stream set");
    }
  };

  pc.onicecandidate = (event) => {
    if (event.candidate) {
      ws.send(JSON.stringify({ type: "candidate", room: roomId, candidate: event.candidate }));
      console.log("📤 Sent ICE candidate");
    }
  };
}

// ✅ Helper: flush candidates after remoteDescription is set
async function flushBufferedCandidates() {
  if (pendingCandidates.length > 0) {
    console.log(`🚀 Flushing ${pendingCandidates.length} buffered ICE candidates`);
    for (const c of pendingCandidates) {
      try {
        await pc.addIceCandidate(c);
        console.log("✅ Added buffered ICE candidate");
      } catch (e) {
        console.error("❌ Buffered ICE add error", e);
      }
    }
    pendingCandidates = [];
  }
}

async function startConnectionAsOfferer() {
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    console.error("WebSocket is not connected.");
    return;
  }
  await initializePeerConnection();
  const offer = await pc.createOffer();
  await pc.setLocalDescription(offer);
  ws.send(JSON.stringify({ type: "offer", room: roomId, sdp: offer.sdp }));
  console.log("📤 Sent offer");
}

async function joinRoomAndStart() {
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    console.error("WebSocket is not connected.");
    return;
  }
  ws.send(JSON.stringify({ type: "join", room: roomId }));
  console.log("👥 Joined room and waiting for offer.");
}

function hangup() {
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop());
    localStream.value = null;
  }

  if (pc) {
    pc.close();
    pc = null;
  }

  if (remoteAudio.value) {
    remoteAudio.value.srcObject = null;
  }

  console.log("❌ Connection stopped / Hangup done");
}
</script>

<template>
  <div class="p-4">
    <button @click="connectWS()">🔌 Connect to Signal Server</button>
    <button @click="joinRoomAndStart()">👥 Join Room</button>
    <button @click="startConnectionAsOfferer()">🎤 Start Audio (Offerer)</button>
    <button @click="hangup()">📴 Hangup / Stop Audio</button>

    <div class="mt-4">
      <h3>Remote Audio:</h3>
      <audio ref="remoteAudio" autoplay controls></audio>
    </div>
  </div>
</template>
