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
        <h1 class="text-2xl font-bold mb-4">Routes</h1>
        <table class="w-full border border-gray-300 rounded-lg overflow-hidden">
            <thead class="bg-gray-100">
                <tr>
                    <th class="text-left px-4 py-2 border-b">Source</th>
                    <th class="text-left px-4 py-2 border-b">Destination</th>
                    <th class="text-center px-4 py-2 border-b">Action</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="route in routes" :key="route.id" class="hover:bg-gray-50">
                    <td class="px-4 py-2 border-b">{{ route.source }}</td>
                    <td class="px-4 py-2 border-b">{{ route.destination }}</td>
                    <td class="px-4 py-2 border-b text-center">
                        <button @click="togglePlay(route)"
                                class="px-3 py-1 rounded-md text-white"
                                :class="route.playing ? 'bg-red-500' : 'bg-green-500'">
                            {{ route.playing ? 'Pause' : 'Play' }}
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<style scoped>
    table {
        border-collapse: collapse;
    }
</style>