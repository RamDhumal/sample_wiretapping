<script setup>
import { ref } from "vue"

// Example data
const routes = ref([
  { id: 1, source: "New York", destination: "Los Angeles", playing: false },
  { id: 2, source: "London", destination: "Paris", playing: false },
  { id: 3, source: "Tokyo", destination: "Osaka", playing: false },
])

// Toggle play/pause for a row
function togglePlay(route) {
  route.playing = !route.playing
}
</script>

<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-4">Intercept</h1>
    <table class="custom-table">
      <thead>
        <tr>
          <th>Source</th>
          <th>Destination</th>
          <th class="text-center">Action</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="route in routes" :key="route.id">
          <td>{{ route.source }}</td>
          <td>{{ route.destination }}</td>
          <td class="text-center">
            <button
              @click="togglePlay(route)"
              class="action-btn"
              :class="route.playing ? 'pause' : 'play'"
            >
              {{ route.playing ? 'Pause' : 'Play' }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
/* Table styling */
.custom-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #ddd;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.custom-table th {
  background: #f9fafb;
  text-align: left;
  padding: 12px;
  font-weight: 600;
  border-bottom: 2px solid #e5e7eb;
}

.custom-table td {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
  transition: background 0.2s ease-in-out;
}

.custom-table tr:hover td {
  background: #f3f4f6;
}

/* Button styling */
.action-btn {
  padding: 6px 14px;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  transition: background 0.3s, transform 0.2s;
}

.action-btn.play {
  background: #22c55e; /* green */
}

.action-btn.play:hover {
  background: #16a34a;
  transform: scale(1.05);
}

.action-btn.pause {
  background: #ef4444; /* red */
}

.action-btn.pause:hover {
  background: #dc2626;
  transform: scale(1.05);
}
</style>
