import { defineStore } from "pinia";

interface HrState {
  companyName: string;
  companyId: number | null;
  darkMode: boolean;
}

export const useHrStore = defineStore("hr", {
  state: (): HrState => ({
    companyName: "智聘星科技",
    companyId: null,
    darkMode: false,
  }),
  actions: {
    setCompanyName(name: string) {
      this.companyName = name;
    },
    setCompanyId(id: number | null) {
      this.companyId = id;
    },
    toggleDarkMode(enabled: boolean) {
      this.darkMode = enabled;
    },
  },
});
