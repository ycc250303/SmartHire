import zhCN from './zh-CN';
import enUS from './en-US';

type Locale = 'zh-CN' | 'en-US';

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS
};

let currentLocale: Locale = 'zh-CN';

/**
 * Get translated text by key path
 */
export function t(keyPath: string): string {
  const keys = keyPath.split('.');
  let value: any = messages[currentLocale];
  
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
 * Switch locale
 */
export function setLocale(locale: Locale): void {
  currentLocale = locale;
}

/**
 * Get current locale
 */
export function getLocale(): Locale {
  return currentLocale;
}

export default { t, setLocale, getLocale };

