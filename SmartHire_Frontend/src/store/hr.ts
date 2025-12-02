import { defineStore } from 'pinia';

interface HrState {
  companyName: string;
  darkMode: boolean;
}

export const useHrStore = defineStore('hr', {
  state: (): HrState => ({
    companyName: '鏅鸿仒鏄熺鎶€',
    darkMode: false,
  }),
  actions: {
    setCompanyName(name: string) {
      this.companyName = name;
    },
    toggleDarkMode(enabled: boolean) {
      this.darkMode = enabled;
    },
  },
});
