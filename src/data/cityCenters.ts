export interface CityCenter {
  latitude: number;
  longitude: number;
}

export const CITY_CENTER_MAP: Record<string, CityCenter> = {
  北京: { latitude: 39.9042, longitude: 116.4074 },
  上海: { latitude: 31.2304, longitude: 121.4737 },
  广州: { latitude: 23.1291, longitude: 113.2644 },
  深圳: { latitude: 22.5431, longitude: 114.0579 },
  杭州: { latitude: 30.2741, longitude: 120.1551 },
  成都: { latitude: 30.5728, longitude: 104.0668 },
  南京: { latitude: 32.0603, longitude: 118.7969 },
  武汉: { latitude: 30.5931, longitude: 114.3054 },
};


