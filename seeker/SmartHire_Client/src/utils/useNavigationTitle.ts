import { watch } from 'vue';
import { getLocaleRef, t } from '@/locales';

export function useNavigationTitle(titleKey: string) {
  const localeRef = getLocaleRef();

  const updateTitle = () => {
    const title = t(titleKey);
    
    uni.setNavigationBarTitle({
      title: title
    });
  };

  updateTitle();

  watch(localeRef, () => {
    updateTitle();
  });
}

