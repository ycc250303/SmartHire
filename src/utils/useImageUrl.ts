import { getApiBaseUrl } from '@/services/http';

export function useImageUrl() {
  function processImageUrl(url: string | null | undefined, fallback?: string): string {
    if (!url || url.trim() === '' || url === 'undefined' || url === 'null') {
      return fallback || '/static/user-avatar.png';
    }

    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }

    if (url.startsWith('/static/') || url.startsWith('data:') || url.startsWith('blob:')) {
      return url;
    }

    const apiPath = '/api/message/get-conversations';
    const baseUrl = getApiBaseUrl(apiPath);
    
    if (!baseUrl) {
      return url.startsWith('/') ? url : `/${url}`;
    }

    let normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
    let normalizedPath = url.startsWith('/') ? url : `/${url}`;
    
    return `${normalizedBaseUrl}${normalizedPath}`;
  }

  return {
    processImageUrl,
  };
}

