<script lang="ts">
    interface Props {
        name: string
        age: number
    }

    const props: Props = $props;
</script>

<p>Hello {props.name}!</p>
<p>You have {props.age} yo</p>

<style>
    p { color: red; }
</style>
