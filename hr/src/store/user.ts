import { defineStore } from 'pinia';
import { getCurrentUserInfo, type UserInfo } from '@/services/api/user';
import { clearToken } from '@/services/http';

interface UserState {
  userInfo: UserInfo | null;
  isLoaded: boolean;
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    userInfo: null,
    isLoaded: false,
  }),

  getters: {
    isLoggedIn: (state): boolean => {
      return state.userInfo !== null;
    },
  },

  actions: {
    async loadUserInfo(): Promise<void> {
      try {
        const data = await getCurrentUserInfo();
        this.userInfo = data;
        this.isLoaded = true;
      } catch (error) {
        console.error('Failed to load user info:', error);
        throw error;
      }
    },

    setUserInfo(userInfo: UserInfo): void {
      this.userInfo = userInfo;
      this.isLoaded = true;
    },

    clearUserInfo(): void {
      this.userInfo = null;
      this.isLoaded = false;
    },

    logout(): void {
      this.clearUserInfo();
      clearToken();
      uni.redirectTo({
        url: '/pages/hr/auth/login',
        fail: () => {
          console.error('Failed to redirect to login page');
        }
      });
    },
  },
});








