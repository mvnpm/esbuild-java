<script lang="ts">
const count: number = 0;

export default {
    data() {
        return {
            count,
        };
    },
};
</script>

<style>
button {
    border: 0 none;
}
</style>

<template>
    <button @click="count++">You clicked me {{ count }} times.</button>
</template>
