<template>
  <view class="job-edit">
    <view class="form-card">
      <view class="form-item">
        <text class="label">岗位标题</text>
        <input v-model="form.jobTitle" placeholder="请输入岗位标题" />
      </view>
      <view class="form-item">
        <text class="label">工作地点</text>
        <input v-model="form.city" placeholder="如：上海-浦东" />
      </view>
      <view class="form-item salary-row">
        <view class="salary-field">
          <text class="label">最低薪资(元)</text>
          <input v-model.number="form.salaryMin" type="number" placeholder="最低" />
        </view>
        <view class="salary-field">
          <text class="label">最高薪资(元)</text>
          <input v-model.number="form.salaryMax" type="number" placeholder="最高" />
        </view>
        <view class="salary-field">
          <text class="label">薪资月数</text>
          <input v-model.number="form.salaryMonths" type="number" placeholder="如 16" />
        </view>
      </view>
      <view class="form-item">
        <text class="label">工作类型</text>
        <picker :range="jobTypes" @change="onTypeChange">
          <view class="picker-value">{{ jobTypeLabel }}</view>
        </picker>
      </view>

      <view class="form-item" v-if="jobTypeValue === 0">
        <text class="label">经验要求</text>
        <picker :range="experienceOptions" @change="onExperienceChange">
          <view class="picker-value">{{ experienceLabel }}</view>
        </picker>
      </view>

      <view class="form-item salary-row" v-if="jobTypeValue === 1">
        <view class="salary-field">
          <text class="label">每周实习天数</text>
          <input v-model.number="form.internshipDaysPerWeek" type="number" placeholder="如 3" />
        </view>
        <view class="salary-field">
          <text class="label">实习时长(月)</text>
          <input v-model.number="form.internshipDurationMonths" type="number" placeholder="如 6" />
        </view>
      </view>
      <view class="form-item textarea">
        <text class="label">岗位描述</text>
        <textarea v-model="form.description" placeholder="职责、项目背景等"></textarea>
      </view>
      <view class="form-item textarea">
        <text class="label">岗位职责</text>
        <textarea v-model="form.responsibilities" placeholder="主要负责内容"></textarea>
      </view>
      <view class="form-item textarea">
        <text class="label">任职要求</text>
        <textarea v-model="form.requirements" placeholder="技能、经验要求等"></textarea>
      </view>
      <view class="form-item">
        <view class="label-row">
          <text class="label">技能要求</text>
          <text class="link-button" @click="addSkill">添加技能</text>
        </view>
        <view class="skill-list" v-if="form.skills && form.skills.length">
          <view class="skill-item" v-for="(item, idx) in form.skills" :key="`skill-${idx}`">
            <input v-model="item.skillName" placeholder="如：Java / Vue.js" />
            <view class="skill-actions">
              <view class="skill-flag" @click="toggleSkillRequired(idx)">
                <text class="skill-flag-text">{{ item.isRequired === 1 ? '必填' : '加分' }}</text>
              </view>
              <text class="remove-skill" @click="removeSkill(idx)">删除</text>
            </view>
          </view>
        </view>
        <view class="skill-empty" v-else>暂未添加技能要求</view>
      </view>
      <button class="primary" :disabled="saving" @click="handleSubmit">{{ submitText }}</button>
    </view>

    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import {
  createJobPosition,
  updateJobPosition,
  getJobPositionById,
  type JobPositionCreatePayload,
  type JobPosition,
  type JobSkill,
} from '@/services/api/hr';
import { getHrInfo } from '@/services/api/hr';
import { useHrStore } from '@/store/hr';

const jobTypes = ['全职', '实习'];
const jobTypeValue = ref<number | null>(null);
const experienceOptions = ['应届/0年', '1年以内', '1-3年', '3-5年', '5-10年', '10年以上'];
const experienceValue = ref<number | null>(null);
const jobId = ref<number | null>(null);
const loading = ref(false);
const saving = ref(false);
const hrStore = useHrStore();

const form = reactive<JobPositionCreatePayload>({
  companyId: 0,
  jobTitle: '',
  city: '',
  salaryMin: undefined,
  salaryMax: undefined,
  salaryMonths: undefined,
  jobType: 0,
  experienceRequired: undefined,
  internshipDaysPerWeek: undefined,
  internshipDurationMonths: undefined,
  description: '',
  responsibilities: '',
  requirements: '',
  skills: [],
});

const submitText = computed(() => (jobId.value ? '保存修改' : '发布岗位'));
const jobTypeLabel = computed(() => {
  if (jobTypeValue.value === null) return '请选择工作类型';
  return jobTypes[jobTypeValue.value] || '请选择工作类型';
});

const experienceLabel = computed(() => {
  if (experienceValue.value === null) return '请选择经验要求';
  return experienceOptions[experienceValue.value] || '请选择经验要求';
});

const ensureCompanyId = async () => {
  if (hrStore.companyId) {
    form.companyId = hrStore.companyId;
    return;
  }
  try {
    const info = await getHrInfo();
    if (info?.companyId) {
      form.companyId = info.companyId;
      hrStore.setCompanyId(info.companyId);
      hrStore.setCompanyName(info.companyName);
    }
  } catch (error) {
    console.error('Failed to load HR info:', error);
    uni.showToast({ title: '无法获取企业信息', icon: 'none' });
  }
};

const normalizeSkills = (skills?: Array<JobSkill | string>) => {
  if (!Array.isArray(skills)) return [];
  return skills
    .map((skill) => {
      if (typeof skill === 'string') {
        const name = skill.trim();
        if (!name) return null;
        return { skillName: name, isRequired: 0 };
      }
      const name = skill.skillName?.trim();
      if (!name) return null;
      return { skillName: name, isRequired: skill.isRequired === 1 ? 1 : 0 };
    })
    .filter((item): item is JobSkill => Boolean(item));
};

const buildSkillsPayload = () => {
  const skills = form.skills ?? [];
  return skills
    .map((item) => {
      const name = item.skillName?.trim();
      if (!name) return null;
      return { skillName: name, isRequired: item.isRequired === 1 ? 1 : 0 };
    })
    .filter((item): item is JobSkill => Boolean(item));
};

const addSkill = () => {
  if (!form.skills) {
    form.skills = [];
  }
  form.skills.push({ skillName: '', isRequired: 0 });
};

const removeSkill = (index: number) => {
  form.skills?.splice(index, 1);
};

const toggleSkillRequired = (index: number) => {
  const skill = form.skills?.[index];
  if (!skill) return;
  skill.isRequired = skill.isRequired === 1 ? 0 : 1;
};

const populateForm = (data?: JobPosition) => {
  if (!data) {
    return;
  }
  form.companyId = data.companyId ?? form.companyId;
  form.jobTitle = data.jobTitle || '';
  form.city = data.city || '';
  form.salaryMin = data.salaryMin;
  form.salaryMax = data.salaryMax;
  form.salaryMonths = data.salaryMonths;
  form.jobType = data.jobType ?? 0;
  form.experienceRequired = data.experienceRequired;
  form.internshipDaysPerWeek = data.internshipDaysPerWeek;
  form.internshipDurationMonths = data.internshipDurationMonths;
  form.description = data.description || '';
  form.responsibilities = data.responsibilities || '';
  form.requirements = data.requirements || '';
  form.skills = normalizeSkills(data.skills);
  jobTypeValue.value = data.jobType ?? null;
  experienceValue.value = typeof data.experienceRequired === 'number' ? data.experienceRequired : null;
};

const loadJob = async (id: number) => {
  loading.value = true;
  try {
    const data = await getJobPositionById(id);
    if (!data) {
      uni.showToast({ title: '岗位不存在，已切换为新建', icon: 'none' });
      jobId.value = null;
      return;
    }
    populateForm(data);
  } catch (error) {
    console.error('Failed to load job:', error);
    uni.showToast({ title: '加载失败', icon: 'none' });
    jobId.value = null;
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  if (saving.value) return;
  saving.value = true;
  try {
    await ensureCompanyId();

    const jobTitle = form.jobTitle?.trim();
    const city = form.city?.trim();
    const description = form.description?.trim();

    if (jobTypeValue.value === null) {
      uni.showToast({ title: '请选择工作类型', icon: 'none' });
      return;
    }

    if (form.companyId === 0) {
      uni.showToast({ title: '无法获取企业信息', icon: 'none' });
      return;
    }

    if (!jobTitle || !city || !description) {
      uni.showToast({ title: '请填写必填信息', icon: 'none' });
      return;
    }

    form.jobType = jobTypeValue.value;
    if (form.jobType === 0) {
      if (typeof form.experienceRequired !== 'number') {
        uni.showToast({ title: '全职岗位需填写经验要求', icon: 'none' });
        return;
      }
    } else if (form.jobType === 1) {
      if (typeof form.internshipDaysPerWeek !== 'number') {
        uni.showToast({ title: '实习岗位需填写每周实习天数', icon: 'none' });
        return;
      }
      if (typeof form.internshipDurationMonths !== 'number') {
        uni.showToast({ title: '实习岗位需填写实习时长', icon: 'none' });
        return;
      }
    }

    form.jobTitle = jobTitle;
    form.city = city;
    form.description = description;

    const skillsPayload = buildSkillsPayload();
    const payload = { ...form, skills: skillsPayload };

    if (jobId.value) {
      await updateJobPosition(jobId.value, payload);
      uni.showToast({ title: '保存成功', icon: 'success' });
    } else {
      const newId = await createJobPosition(payload);
      jobId.value = newId;
      uni.showToast({ title: '发布成功', icon: 'success' });
    }
    setTimeout(() => uni.navigateBack(), 600);
  } catch (error) {
    console.error('Failed to submit job:', error);
    uni.showToast({ title: '提交失败', icon: 'none' });
  } finally {
    saving.value = false;
  }
};

const onTypeChange = (event: any) => {
  const index = Number(event.detail.value);
  jobTypeValue.value = index;
  form.jobType = index;
  if (index === 0) {
    form.internshipDaysPerWeek = undefined;
    form.internshipDurationMonths = undefined;
  } else if (index === 1) {
    form.experienceRequired = undefined;
    experienceValue.value = null;
  }
};

const onExperienceChange = (event: any) => {
  const index = Number(event.detail.value);
  experienceValue.value = index;
  form.experienceRequired = index;
};

onLoad(async (options) => {
  await ensureCompanyId();
  const id = Number(options?.jobId);
  if (id) {
    jobId.value = id;
    loadJob(id);
  }
});
</script>

<style scoped lang="scss">
.job-edit {
  background: #f6f7fb;
  min-height: 100vh;
  padding: calc(var(--status-bar-height) + 48rpx) 24rpx 24rpx;
  box-sizing: border-box;
}

.form-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 34, 90, 0.08);
}

.form-item {
  margin-bottom: 24rpx;
}

.salary-row {
  display: flex;
  gap: 16rpx;
}

.salary-field {
  flex: 1;
  min-width: 0;
}

.label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 26rpx;
  color: #5a6275;
}

.link-button {
  font-size: 24rpx;
  color: #2f7cff;
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(47, 124, 255, 0.12);
}

input,
textarea {
  width: 100%;
  border-radius: 16rpx;
  background: #f5f7fb;
  border: none;
  box-sizing: border-box;
}

input {
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
}

textarea {
  min-height: 200rpx;
  padding: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
}

.picker-value {
  padding: 20rpx;
  background: #f5f7fb;
  border-radius: 16rpx;
  color: #7a8398;
}

.skill-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.skill-item {
  background: #f5f7fb;
  border-radius: 16rpx;
  padding: 16rpx;
}

.skill-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12rpx;
}

.skill-flag {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #e8f3ff;
}

.skill-flag-text {
  font-size: 22rpx;
  color: #2f7cff;
}

.remove-skill {
  font-size: 22rpx;
  color: #ff7a00;
}

.skill-empty {
  font-size: 24rpx;
  color: #9aa3b2;
}

button.primary {
  width: 100%;
  height: 84rpx;
  border-radius: 18rpx;
  background: #2f7cff;
  color: #fff;
  border: none;
  margin-top: 20rpx;
}

.loading {
  text-align: center;
  color: #8a92a7;
  margin-top: 20rpx;
}
</style>
