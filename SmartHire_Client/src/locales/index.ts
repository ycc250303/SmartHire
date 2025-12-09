import { ref, computed } from 'vue';
import zhCN from './zh-CN';
import enUS from './en-US';

type Locale = 'zh-CN' | 'en-US';

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS
};

const currentLocale = ref<Locale>('zh-CN');

/**
 * Get translated text by key path
 */
export function t(keyPath: string): string {
  const keys = keyPath.split('.');
  let value: any = messages[currentLocale.value];
  
  for (const key of keys) {
    if (value && typeof value === 'object') {
      value = value[key];
    } else {
      return keyPath;
    }
  }
  
  return typeof value === 'string' ? value : keyPath;
}

/**
 * Get reactive translation function
 */
export function useT() {
  return computed(() => (keyPath: string) => {
    const keys = keyPath.split('.');
    let value: any = messages[currentLocale.value];
    
    for (const key of keys) {
      if (value && typeof value === 'object') {
        value = value[key];
      } else {
        return keyPath;
      }
    }
    
    return typeof value === 'string' ? value : keyPath;
  });
}

/**
 * Switch locale
 */
export function setLocale(locale: Locale): void {
  if (currentLocale.value !== locale) {
    currentLocale.value = locale;
    if (typeof uni !== 'undefined') {
      uni.$emit('locale-changed', locale);
    }
  }
}

/**
 * Get current locale
 */
export function getLocale(): Locale {
  return currentLocale.value;
}

/**
 * Get reactive locale ref
 */
export function getLocaleRef() {
  return currentLocale;
}

export default { t, setLocale, getLocale, getLocaleRef, useT };

