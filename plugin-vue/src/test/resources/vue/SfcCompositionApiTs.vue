<script setup lang="ts">
import { ref } from "vue";

const count = ref(0);
</script>

<style>
button {
    border: 0 none;
}
</style>

<template>
    <button @click="count++">You clicked me {{ count }} times.</button>
</template>
