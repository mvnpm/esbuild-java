<svelte:options customElement="my-element" />
<script lang="ts">
    import ChildComponent from "./ChildComponent.svelte";

    let name: string = $state("world")
</script>

<ChildComponent {name} age={42}/>

<input bind:value={name}/>
