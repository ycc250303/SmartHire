import universitiesData from '@/assets/universities.json';

export interface UniversityItem {
  name: string;
  level: string;
  city: string;
}

export interface ProvinceItem {
  name: string;
  universities: UniversityItem[];
}

class UniversityDataManager {
  private provinces: ProvinceItem[] = [];
  private initialized = false;

  private initialize() {
    if (this.initialized) return;

    this.provinces = universitiesData as ProvinceItem[];
    this.initialized = true;
  }

  getProvinces(): ProvinceItem[] {
    this.initialize();
    return this.provinces;
  }

  getUniversitiesByProvince(provinceIndex: number): UniversityItem[] {
    this.initialize();
    if (provinceIndex < 0 || provinceIndex >= this.provinces.length) {
      return [];
    }
    const province = this.provinces[provinceIndex];
    if (!province) {
      return [];
    }
    return province.universities || [];
  }

  searchUniversities(keyword: string): UniversityItem[] {
    this.initialize();
    if (!keyword || !keyword.trim()) {
      return [];
    }

    const searchKeyword = keyword.trim().toLowerCase();
    const results: UniversityItem[] = [];

    this.provinces.forEach((province) => {
      province.universities.forEach((university) => {
        if (university.name.toLowerCase().includes(searchKeyword)) {
          results.push(university);
        }
      });
    });

    return results;
  }

  formatUniversityString(university: UniversityItem | null): string {
    if (!university || !university.name) {
      return '';
    }
    return university.name;
  }

  parseUniversityString(universityString: string | null | undefined): UniversityItem | null {
    if (!universityString || !universityString.trim()) {
      return null;
    }

    this.initialize();
    const searchName = universityString.trim();

    for (const province of this.provinces) {
      for (const university of province.universities) {
        if (university.name === searchName) {
          return university;
        }
      }
    }

    return null;
  }
}

const universityDataManager = new UniversityDataManager();

export function useUniversityData() {
  return {
    getProvinces: () => universityDataManager.getProvinces(),
    getUniversitiesByProvince: (provinceIndex: number) =>
      universityDataManager.getUniversitiesByProvince(provinceIndex),
    searchUniversities: (keyword: string) => universityDataManager.searchUniversities(keyword),
    formatUniversityString: (university: UniversityItem | null) =>
      universityDataManager.formatUniversityString(university),
    parseUniversityString: (universityString: string | null | undefined) =>
      universityDataManager.parseUniversityString(universityString),
  };
}

