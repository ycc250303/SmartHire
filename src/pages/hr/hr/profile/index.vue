<template>
  <view class="profile-page">
    <view class="card" v-if="hrInfo">
      <view class="name-row">
        <view class="profile-info">
          <view class="avatar-wrapper">
            <image :src="avatarImg" mode="aspectFill" />
          </view>
          <view>
            <view class="name">{{ hrInfo.realName || '-' }}</view>
            <view class="role">{{ hrInfo.position || '招聘者' }} · {{ hrInfo.companyName || '公司未填写' }}</view>
          </view>
        </view>
        <view class="badge">{{ badgeText }}</view>
      </view>
      <view class="info">工号/UID：{{ hrInfo.userId }}</view>
      <view class="info">公司ID：{{ hrInfo.companyId }}</view>
    </view>

    <view class="card">
      <view class="section-title">基础信息</view>
      <view v-if="!hrInfo" class="hint">首次使用请先填写并保存（将自动完成HR信息登记）。</view>
      <view class="form-item">
        <text class="label">真实姓名</text>
        <input v-model="form.realName" placeholder="请输入真实姓名" />
      </view>
      <view class="form-item">
        <text class="label">职位</text>
        <input v-model="form.position" placeholder="如：资深招聘/HRBP" />
      </view>
      <view class="form-item">
        <text class="label">工作电话</text>
        <input v-model="form.workPhone" placeholder="手机或座机" />
      </view>
      <button class="primary" :disabled="saving" @click="handleSave">保存</button>
    </view>

    <view class="card" v-if="isCompanyAdmin">
      <view class="section-title">公司信息（老板）</view>
      <view class="form-item">
        <text class="label">公司名称</text>
        <input v-model="companyForm.companyName" placeholder="请输入公司名称" />
      </view>
      <view class="form-item">
        <text class="label">公司Logo</text>
        <input v-model="companyForm.companyLogo" placeholder="请输入Logo地址" />
      </view>
      <view class="form-item">
        <text class="label">公司规模</text>
        <picker :range="companyScaleOptions" :range-key="'label'" @change="onCompanyScaleChange">
          <view class="picker-value">{{ companyScaleLabel }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">融资阶段</text>
        <picker :range="financingStageOptions" :range-key="'label'" @change="onFinancingStageChange">
          <view class="picker-value">{{ financingStageLabel }}</view>
        </picker>
      </view>
      <view class="form-item">
        <text class="label">行业</text>
        <input v-model="companyForm.industry" placeholder="请输入行业" />
      </view>
      <view class="form-item textarea">
        <text class="label">公司介绍</text>
        <textarea v-model="companyForm.description" placeholder="请输入公司介绍"></textarea>
      </view>
      <view class="form-item">
        <text class="label">官网</text>
        <input v-model="companyForm.website" placeholder="请输入官网地址" />
      </view>
      <button class="primary" :disabled="companySaving" @click="handleSaveCompany">保存公司信息</button>
    </view>

    <view class="card logout-card">
      <button class="logout" @click="logout">退出登录</button>
    </view>

    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { getHrInfo, registerHr, updateHrInfo, type HrInfoUpdatePayload, type HrInfo } from '@/services/api/hr';
import { getCompanyInfo, updateCompanyInfo, type CompanyInfo } from '@/services/api/company';
import { useHrStore } from '@/store/hr';
import avatarImg from '@/static/user-avatar.png';

const hrInfo = ref<HrInfo | null>(null);
const form = ref<HrInfoUpdatePayload>({
  realName: '',
  position: '',
  workPhone: '',
});
const companyForm = ref<CompanyInfo>({
  companyId: 0,
  companyName: '',
  companyLogo: '',
  companyScale: undefined,
  financingStage: undefined,
  industry: '',
  description: '',
  website: '',
});
const companySaving = ref(false);
const loading = ref(false);
const saving = ref(false);
const hrStore = useHrStore();

const isCompanyAdmin = computed(() => {
  const raw = (hrInfo.value as HrInfo | null)?.isCompanyAdmin;
  if (raw === 1 || raw === true) return true;
  if (typeof raw === 'string') return raw === '1' || raw.toLowerCase() === 'true';
  return false;
});

const badgeText = computed(() => (isCompanyAdmin.value ? '公司管理者' : 'HR'));

const companyScaleOptions = [
  { label: '1-50人', value: 1 },
  { label: '51-200人', value: 2 },
  { label: '201-500人', value: 3 },
  { label: '501-2000人', value: 4 },
  { label: '2000人以上', value: 5 },
];

const financingStageOptions = [
  { label: '未融资', value: 0 },
  { label: '天使轮', value: 1 },
  { label: 'A轮', value: 2 },
  { label: 'B轮', value: 3 },
  { label: 'C轮', value: 4 },
  { label: '上市', value: 5 },
];

const companyScaleLabel = computed(() => {
  const option = companyScaleOptions.find((item) => item.value === companyForm.value.companyScale);
  return option?.label || '请选择公司规模';
});

const financingStageLabel = computed(() => {
  const option = financingStageOptions.find((item) => item.value === companyForm.value.financingStage);
  return option?.label || '请选择融资阶段';
});

const onCompanyScaleChange = (event: any) => {
  const option = companyScaleOptions[event.detail.value];
  if (option) {
    companyForm.value.companyScale = option.value;
  }
};

const onFinancingStageChange = (event: any) => {
  const option = financingStageOptions[event.detail.value];
  if (option) {
    companyForm.value.financingStage = option.value;
  }
};

const loadCompanyInfo = async (companyId?: number) => {
  if (!companyId) return;
  try {
    const data = await getCompanyInfo(companyId);
    companyForm.value = {
      companyId,
      companyName: data.companyName || '',
      companyLogo: data.companyLogo || '',
      companyScale: data.companyScale,
      financingStage: data.financingStage,
      industry: data.industry || '',
      description: data.description || '',
      website: data.website || '',
    };
  } catch (error) {
    console.error('Failed to load company info:', error);
  }
};

const loadProfile = async () => {
  loading.value = true;
  try {
    const data = await getHrInfo();
    hrInfo.value = data;
    form.value = {
      realName: data.realName,
      position: data.position,
      workPhone: data.workPhone,
    };
    hrStore.setCompanyName(data.companyName);
    hrStore.setCompanyId(data.companyId);
    if (data.isCompanyAdmin === 1) {
      loadCompanyInfo(data.companyId);
    }
  } catch (error) {
    console.error('Failed to load HR info:', error);
    // 允许“首次登记”场景：接口失败时仍展示表单，让用户走 register 流程
    hrInfo.value = null;
    uni.showToast({ title: '未获取到HR信息，请先填写并保存', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const handleSave = async () => {
  saving.value = true;
  try {
    const realName = form.value.realName?.trim();
    const position = form.value.position?.trim();
    const workPhone = form.value.workPhone?.trim();
    if (!realName || !position || !workPhone) {
      uni.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }
    if (!hrInfo.value) {
      await registerHr({ realName, position, workPhone });
    } else {
      await updateHrInfo({ realName, position, workPhone });
    }
    await loadProfile();
    uni.showToast({ title: '已保存', icon: 'success' });
  } catch (error) {
    console.error('Failed to update HR info:', error);
    uni.showToast({ title: '保存失败', icon: 'none' });
  } finally {
    saving.value = false;
  }
};

const handleSaveCompany = async () => {
  if (!hrInfo.value?.companyId) {
    uni.showToast({ title: '缺少公司ID', icon: 'none' });
    return;
  }
  companySaving.value = true;
  try {
    const payload = {
      companyName: companyForm.value.companyName?.trim() || undefined,
      companyLogo: companyForm.value.companyLogo?.trim() || undefined,
      companyScale: companyForm.value.companyScale,
      financingStage: companyForm.value.financingStage,
      industry: companyForm.value.industry?.trim() || undefined,
      description: companyForm.value.description?.trim() || undefined,
      website: companyForm.value.website?.trim() || undefined,
    };
    if (!payload.companyName) {
      uni.showToast({ title: '请输入公司名称', icon: 'none' });
      return;
    }
    await updateCompanyInfo(hrInfo.value.companyId, payload);
    uni.showToast({ title: '公司信息已保存', icon: 'success' });
    await loadCompanyInfo(hrInfo.value.companyId);
  } catch (error) {
    console.error('Failed to update company info:', error);
    uni.showToast({ title: '保存失败', icon: 'none' });
  } finally {
    companySaving.value = false;
  }
};

const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确认退出当前账号吗？',
    success: (res) => {
      if (res.confirm) {
        uni.reLaunch({ url: '/pages/auth/login' });
      }
    },
  });
};

onMounted(() => {
  loadProfile();
});
</script>

<style scoped lang="scss">
.profile-page {
  padding: 24rpx;
  background: #f6f7fb;
  min-height: 100vh;
}

.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-info {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  width: 112rpx;
  height: 112rpx;
  border-radius: 16rpx;
  background: #eef2ff;
  margin-right: 24rpx;
  overflow: hidden;
}

.avatar-wrapper image {
  width: 100%;
  height: 100%;
}

.name {
  font-size: 34rpx;
  font-weight: 600;
}

.role {
  color: #8a92a7;
  margin-top: 8rpx;
}

.badge {
  padding: 8rpx 20rpx;
  background: #e8f3ff;
  color: #2f7cff;
  border-radius: 999rpx;
}

.info {
  margin-top: 10rpx;
  color: #6f788f;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.hint {
  padding: 16rpx 18rpx;
  border-radius: 16rpx;
  background: #f5f7fb;
  color: #6f788f;
  font-size: 24rpx;
  margin-bottom: 16rpx;
}

.form-item {
  margin-bottom: 20rpx;
}

.label {
  display: block;
  margin-bottom: 8rpx;
  color: #6f788f;
}

input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  background: #f5f7fb;
  border-radius: 16rpx;
  border: none;
  box-sizing: border-box;
}

textarea {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx;
  background: #f5f7fb;
  border-radius: 16rpx;
  border: none;
  box-sizing: border-box;
}

.picker-value {
  padding: 20rpx;
  background: #f5f7fb;
  border-radius: 16rpx;
  color: #7a8398;
}

button.primary {
  width: 100%;
  height: 90rpx;
  background: #2f7cff;
  color: #fff;
  border: none;
  border-radius: 18rpx;
  margin-top: 12rpx;
}

.logout-card {
  text-align: center;
}

.logout {
  width: 100%;
  height: 90rpx;
  background: #ff4d4f;
  color: #fff;
  border: none;
  border-radius: 18rpx;
}

.loading {
  text-align: center;
  color: #8a92a7;
  margin-top: 20rpx;
}
</style>
