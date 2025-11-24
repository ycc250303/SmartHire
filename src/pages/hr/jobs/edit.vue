<template>
  <view class="job-edit">
    <view class="form-card">
      <view class="form-item">
        <text class="label">鑱屼綅鏍囬</text>
        <input v-model="form.title" placeholder="璇疯緭鍏ヨ亴浣嶆爣棰? />
      </view>
      <view class="form-item">
        <text class="label">宸ヤ綔鍦扮偣</text>
        <input v-model="form.city" placeholder="濡傦細涓婃捣-娴︿笢" />
      </view>
      <view class="form-item">
        <text class="label">钖祫鑼冨洿</text>
        <input v-model="form.salary" placeholder="濡傦細25-35k路16钖? />
      </view>
      <view class="form-item">
        <text class="label">宸ヤ綔绫诲瀷</text>
        <picker :range="jobTypes" @change="onTypeChange">
          <view class="picker-value">{{ form.jobType || '璇烽€夋嫨宸ヤ綔绫诲瀷' }}</view>
        </picker>
      </view>
      <view class="form-item textarea">
        <text class="label">宀椾綅鎻忚堪</text>
        <textarea v-model="form.description" placeholder="鑱岃矗銆侀」鐩儗鏅瓑"></textarea>
      </view>
      <view class="form-item textarea">
        <text class="label">浠昏亴瑕佹眰</text>
        <textarea v-model="form.requirements" placeholder="鎶€鑳姐€佺粡楠岃姹?></textarea>
      </view>
      <button class="primary" @click="handleSubmit">淇濆瓨</button>
      <button class="ghost" @click="runJDCheck">JD 璐ㄩ噺妫€娴嬶紙鍗犱綅锛?/button>
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

const jobTypes = ['鍏ㄨ亴', '瀹炰範', '澶栧寘', '鍏艰亴'];

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
    jobType: '鍏ㄨ亴',
    description: '璐熻矗鏍稿績鍔熻兘杩唬锛屽崗鍚?AI 鑳藉姏澧炲己鍓嶇浣撻獙',
    requirements: '鐔熸倝 Vue3/TS锛屾湁 B 绔粡楠屼紭鍏?,
  });
};

const handleSubmit = () => {
  if (!form.title || !form.city) {
    uni.showToast({ title: '璇峰～鍐欏繀瑕佷俊鎭?, icon: 'none' });
    return;
  }
  // TODO: POST /api/hr/jobs or PUT /api/hr/jobs/:id
  uni.showToast({ title: '淇濆瓨鎴愬姛', icon: 'success' });
  setTimeout(() => {
    uni.navigateBack();
  }, 600);
};

const onTypeChange = (event: any) => {
  const index = Number(event.detail.value);
  form.jobType = jobTypes[index];
};

const runJDCheck = () => {
  uni.showToast({ title: 'JD 妫€娴嬪緟鎺ュ叆', icon: 'none' });
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
