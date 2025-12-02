<template>
  <view class="job-edit">
    <view class="form-card">
      <view class="form-item">
        <text class="label">岗位标题</text>
        <input v-model="form.title" placeholder="请输入职位标题" />
      </view>
      <view class="form-item">
        <text class="label">工作地点</text>
        <input v-model="form.city" placeholder="如：上海-浦东" />
      </view>
      <view class="form-item">
        <text class="label">薪资范围</text>
        <input v-model="form.salary" placeholder="如：25-35k·16薪" />
      </view>
      <view class="form-item">
        <text class="label">工作类型</text>
        <picker :range="jobTypes" @change="onTypeChange">
          <view class="picker-value">{{ form.jobType || '请选择工作类型' }}</view>
        </picker>
      </view>
      <view class="form-item textarea">
        <text class="label">岗位描述</text>
        <textarea v-model="form.description" placeholder="职责、项目背景等"></textarea>
      </view>
      <view class="form-item textarea">
        <text class="label">任职要求</text>
        <textarea v-model="form.requirements" placeholder="技能、经验要求等"></textarea>
      </view>
      <button class="primary" @click="handleSubmit">保存</button>
      <button class="ghost" @click="runJDCheck">JD 质量巡检（示例）</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchJobDetail } from '@/mock/hr';

interface JobForm {
  jobId?: number;
  title: string;
  city: string;
  salary: string;
  jobType: string;
  description: string;
  requirements: string;
}

const jobTypes = ['全职', '实习', '外包', '兼职'];

const form = reactive<JobForm>({
  title: '',
  city: '',
  salary: '',
  jobType: '',
  description: '',
  requirements: '',
});

const populateForm = async (id: number) => {
  // TODO: GET /api/hr/jobs/:id
  const data = await fetchJobDetail(id);
  Object.assign(form, {
    jobId: data.job.jobId,
    title: data.job.title,
    city: data.job.city,
    salary: data.job.salary,
    jobType: '全职',
    description: '负责核心功能迭代，联合 AI 能力提升前端体验。',
    requirements: '熟练 Vue3/TS，具备 B 端经验者优先。',
  });
};

const handleSubmit = () => {
  if (!form.title || !form.city) {
    uni.showToast({ title: '请填写必填信息', icon: 'none' });
    return;
  }
  // TODO: POST /api/hr/jobs or PUT /api/hr/jobs/:id
  uni.showToast({ title: '保存成功', icon: 'success' });
  setTimeout(() => {
    uni.navigateBack();
  }, 600);
};

const onTypeChange = (event: any) => {
  const index = Number(event.detail.value);
  form.jobType = jobTypes[index];
};

const runJDCheck = () => {
  uni.showToast({ title: 'JD 检查功能待接入', icon: 'none' });
};

onLoad((options) => {
  const id = Number(options?.jobId);
  if (id) {
    populateForm(id);
  }
});
</script>

<style scoped lang="scss">
.job-edit {
  background: #f6f7fb;
  min-height: 100vh;
  padding: 24rpx;
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

.label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 26rpx;
  color: #5a6275;
}

input,
textarea {
  width: 100%;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f5f7fb;
  border: none;
}

textarea {
  min-height: 200rpx;
}

.picker-value {
  padding: 20rpx;
  background: #f5f7fb;
  border-radius: 16rpx;
  color: #7a8398;
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

button.ghost {
  width: 100%;
  height: 84rpx;
  border-radius: 18rpx;
  border: 2rpx dashed #b9c6e6;
  color: #2f7cff;
  margin-top: 20rpx;
  background: transparent;
}
</style>
