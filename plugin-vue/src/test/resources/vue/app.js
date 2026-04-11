import { createApp } from "vue";
import SfcCompositionApi from "./SfcCompositionApiTs.vue";
import SfcOptionsApi from "./SfcOptionsApiTs.vue";

createApp(SfcCompositionApi).mount("#composition-api");
createApp(SfcOptionsApi).mount("#options-api");
