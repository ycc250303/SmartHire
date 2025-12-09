import mainlandCities from '@/assets/cities/cities-mainland.json';
import hkmotwCities from '@/assets/cities/cities-HKMOTW.json';

export interface CityItem {
  name: string;
  code?: string;
  children?: CityItem[];
}

export interface ProvinceItem {
  name: string;
  code?: string;
  cities: CityItem[];
}

export interface DistrictItem {
  name: string;
}

export interface CitySelection {
  province: string;
  city: string;
  district: string;
}

export interface CityIndex {
  provinceIndex: number;
  cityIndex: number;
  districtIndex: number;
}

class CityDataManager {
  private mainlandProvinces: ProvinceItem[] = [];
  private hkmotwProvinces: ProvinceItem[] = [];
  private allProvinces: ProvinceItem[] = [];
  private initialized = false;

  private initialize() {
    if (this.initialized) return;

    // Process mainland cities
    this.mainlandProvinces = (mainlandCities as any[]).map((province) => ({
      name: province.name,
      code: province.code,
      cities: province.children?.[0]?.children
        ? province.children.map((city: any) => ({
            name: city.name,
            code: city.code,
            children: city.children?.map((district: any) => ({
              name: district.name,
              code: district.code,
            })),
          }))
        : [],
    }));

    // Process HKMOTW cities
    this.hkmotwProvinces = Object.entries(hkmotwCities as Record<string, Record<string, string[]>>).map(
      ([provinceName, cities]) => ({
        name: provinceName,
        cities: Object.entries(cities).map(([cityName, districts]) => ({
          name: cityName,
          children: districts.map((districtName) => ({
            name: districtName,
          })),
        })),
      })
    );

    // Combine all provinces
    this.allProvinces = [...this.mainlandProvinces, ...this.hkmotwProvinces];
    this.initialized = true;
  }

  getProvinces(): ProvinceItem[] {
    this.initialize();
    return this.allProvinces;
  }

  getCities(provinceIndex: number): CityItem[] {
    this.initialize();
    if (provinceIndex < 0 || provinceIndex >= this.allProvinces.length) {
      return [];
    }
    const province = this.allProvinces[provinceIndex];
    if (!province) {
      return [];
    }
    return province.cities || [];
  }

  getDistricts(provinceIndex: number, cityIndex: number): DistrictItem[] {
    this.initialize();
    if (provinceIndex < 0 || provinceIndex >= this.allProvinces.length) {
      return [];
    }
    const province = this.allProvinces[provinceIndex];
    if (!province) {
      return [];
    }
    const cities = province.cities || [];
    if (cityIndex < 0 || cityIndex >= cities.length) {
      return [];
    }
    const city = cities[cityIndex];
    if (!city) {
      return [];
    }
    return city.children || [];
  }

  formatCityString(selection: CitySelection): string {
    return `${selection.province}-${selection.city}-${selection.district}`;
  }

  parseCityString(cityString: string | null | undefined): CityIndex | null {
    if (!cityString) return null;

    const parts = cityString.split('-');
    if (parts.length !== 3) return null;

    const [provinceName, cityName, districtName] = parts;

    this.initialize();

    // Find province index
    const provinceIndex = this.allProvinces.findIndex((p) => p.name === provinceName);
    if (provinceIndex === -1) return null;

    const province = this.allProvinces[provinceIndex];
    if (!province) return null;

    // Find city index
    const cities = province.cities || [];
    const cityIndex = cities.findIndex((c) => c.name === cityName);
    if (cityIndex === -1) return null;

    const city = cities[cityIndex];
    if (!city) return null;

    // Find district index
    const districts = city.children || [];
    const districtIndex = districts.findIndex((d) => d.name === districtName);
    if (districtIndex === -1) return null;

    return {
      provinceIndex,
      cityIndex,
      districtIndex,
    };
  }

  getCitySelectionFromIndex(index: CityIndex): CitySelection | null {
    this.initialize();

    if (
      index.provinceIndex < 0 ||
      index.provinceIndex >= this.allProvinces.length
    ) {
      return null;
    }

    const province = this.allProvinces[index.provinceIndex];
    if (!province) {
      return null;
    }

    const cities = province.cities || [];

    if (index.cityIndex < 0 || index.cityIndex >= cities.length) {
      return null;
    }

    const city = cities[index.cityIndex];
    if (!city) {
      return null;
    }

    const districts = city.children || [];

    if (index.districtIndex < 0 || index.districtIndex >= districts.length) {
      return null;
    }

    const district = districts[index.districtIndex];
    if (!district) {
      return null;
    }

    return {
      province: province.name,
      city: city.name,
      district: district.name,
    };
  }
}

const cityDataManager = new CityDataManager();

export function useCityData() {
  return {
    getProvinces: () => cityDataManager.getProvinces(),
    getCities: (provinceIndex: number) => cityDataManager.getCities(provinceIndex),
    getDistricts: (provinceIndex: number, cityIndex: number) =>
      cityDataManager.getDistricts(provinceIndex, cityIndex),
    formatCityString: (selection: CitySelection) => cityDataManager.formatCityString(selection),
    parseCityString: (cityString: string | null | undefined) => cityDataManager.parseCityString(cityString),
    getCitySelectionFromIndex: (index: CityIndex) => cityDataManager.getCitySelectionFromIndex(index),
  };
}

