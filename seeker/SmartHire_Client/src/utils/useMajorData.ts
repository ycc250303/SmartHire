import majorsData from '@/assets/majors.json';

export interface MajorItem {
  name: string;
  category1: string;
  category2: string;
}

export interface CategoryItem {
  name: string;
  majors: MajorItem[];
}

class MajorDataManager {
  private categories: CategoryItem[] = [];
  private initialized = false;

  private initialize() {
    if (this.initialized) return;

    this.categories = majorsData as CategoryItem[];
    this.initialized = true;
  }

  getCategories(): CategoryItem[] {
    this.initialize();
    return this.categories;
  }

  getMajorsByCategory(categoryIndex: number): MajorItem[] {
    this.initialize();
    if (categoryIndex < 0 || categoryIndex >= this.categories.length) {
      return [];
    }
    const category = this.categories[categoryIndex];
    if (!category) {
      return [];
    }
    return category.majors || [];
  }

  searchMajors(keyword: string): MajorItem[] {
    this.initialize();
    if (!keyword || !keyword.trim()) {
      return [];
    }

    const searchKeyword = keyword.trim().toLowerCase();
    const results: MajorItem[] = [];

    this.categories.forEach((category) => {
      category.majors.forEach((major) => {
        if (major.name.toLowerCase().includes(searchKeyword)) {
          results.push(major);
        }
      });
    });

    return results;
  }

  formatMajorString(major: MajorItem | null): string {
    if (!major || !major.name) {
      return '';
    }
    return major.name;
  }

  parseMajorString(majorString: string | null | undefined): MajorItem | null {
    if (!majorString || !majorString.trim()) {
      return null;
    }

    this.initialize();
    const searchName = majorString.trim();

    for (const category of this.categories) {
      for (const major of category.majors) {
        if (major.name === searchName) {
          return major;
        }
      }
    }

    return null;
  }
}

const majorDataManager = new MajorDataManager();

export function useMajorData() {
  return {
    getCategories: () => majorDataManager.getCategories(),
    getMajorsByCategory: (categoryIndex: number) =>
      majorDataManager.getMajorsByCategory(categoryIndex),
    searchMajors: (keyword: string) => majorDataManager.searchMajors(keyword),
    formatMajorString: (major: MajorItem | null) =>
      majorDataManager.formatMajorString(major),
    parseMajorString: (majorString: string | null | undefined) =>
      majorDataManager.parseMajorString(majorString),
  };
}

