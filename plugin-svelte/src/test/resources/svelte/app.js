import { mount } from "svelte";
import SimpleComponent from './SimpleComponent.svelte';

// directly mount the component on load
mount(SimpleComponent, { target: document.querySelector('#target') });
