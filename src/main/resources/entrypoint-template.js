{#for i in imports}
{#if i.as}
import * as {i.as} from "./{i.from}";
{#else}
import "./{i.from}";
{/if}
{/for}
