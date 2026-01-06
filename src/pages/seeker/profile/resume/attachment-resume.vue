<template>
  <view class="attachment-resume-page">
    <view v-if="loading" class="state-card">
      <text class="state-text">{{ t('pages.resume.attachment.uploading') }}</text>
    </view>

    <scroll-view v-else class="resume-list" scroll-y>
      <view v-if="resumeList.length" class="resume-items">
        <view
          v-for="resume in resumeList"
          :key="resume.id"
          class="resume-item"
        >
          <view class="resume-item-content" @tap="handleEditResume(resume)">
            <view class="resume-item-header">
              <text class="resume-name">{{ resume.resumeName }}</text>
              <text class="resume-privacy">{{ getPrivacyLabel(resume.privacyLevel) }}</text>
            </view>
            <text class="resume-meta">{{ formatDate(resume.updatedAt) }}</text>
          </view>
          <view class="resume-item-actions">
            <view class="action-button" @tap.stop="handleDownload(resume)">
              <text class="action-icon">⬇</text>
            </view>
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-text">{{ t('pages.resume.attachment.empty') }}</text>
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view class="upload-button" @tap="handleUpload">
        <text class="button-text">{{ t('pages.resume.attachment.upload') }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { t } from '@/locales';
import { useNavigationTitle } from '@/utils/useNavigationTitle';
import type { Resume } from '@/services/api/resume';
import { getResumes, deleteResume } from '@/services/api/resume';

useNavigationTitle('navigation.attachmentResume');

const loading = ref(false);
const resumeList = ref<Resume[]>([]);
const downloading = ref<number | null>(null);

onLoad(() => {
  loadResumes();
});

onShow(() => {
  loadResumes();
});

async function loadResumes() {
  loading.value = true;
  try {
    resumeList.value = await getResumes();
  } catch (error) {
    console.error('Failed to load resumes:', error);
    uni.showToast({
      title: t('pages.resume.attachment.uploadError'),
      icon: 'none',
    });
  } finally {
    loading.value = false;
  }
}

function handleUpload() {
  // #ifdef H5
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = '.pdf';
  input.style.display = 'none';
  document.body.appendChild(input);
  
  input.onchange = async (e: any) => {
    const file = e.target.files?.[0];
    document.body.removeChild(input);
    
    if (!file) return;
    
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      uni.showToast({
        title: t('pages.resume.attachment.uploadErrorFormat'),
        icon: 'none',
      });
      return;
    }
    
    const reader = new FileReader();
    reader.onload = (event: any) => {
      const base64 = event.target.result;
      navigateToUpload(base64, file.name);
    };
    reader.onerror = () => {
      uni.showToast({
        title: t('pages.resume.attachment.uploadError'),
        icon: 'none',
      });
    };
    reader.readAsDataURL(file);
  };
  
  input.click();
  // #endif
  
  // #ifndef H5
  // #ifdef APP-PLUS
  chooseFileForApp();
  // #endif
  
  // #ifdef MP-WEIXIN
  if (typeof uni.chooseMessageFile !== 'undefined') {
    uni.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['.pdf'],
      success: (res) => {
        if (res.tempFiles && res.tempFiles.length > 0) {
          const file = res.tempFiles[0];
          if (!file) {
            uni.showToast({
              title: t('pages.resume.attachment.fileRequired'),
              icon: 'none',
            });
            return;
          }
          const fileName = file.name || 'resume.pdf';
          if (!fileName.toLowerCase().endsWith('.pdf')) {
            uni.showToast({
              title: t('pages.resume.attachment.uploadErrorFormat'),
              icon: 'none',
            });
            return;
          }
          navigateToUpload(file.path, fileName);
        }
      },
      fail: (error) => {
        if (error.errMsg && !error.errMsg.includes('cancel')) {
          uni.showToast({
            title: t('pages.resume.attachment.uploadError'),
            icon: 'none',
          });
        }
      },
    });
  } else {
    tryChooseFile();
  }
  // #endif
  
  // #ifndef APP-PLUS && MP-WEIXIN
  tryChooseFile();
  // #endif
  // #endif
}

// #ifndef H5
function tryChooseFile() {
  if (typeof uni.chooseFile !== 'undefined') {
    uni.chooseFile({
      count: 1,
      extension: ['.pdf'],
      success: (res) => {
        const files = Array.isArray(res.tempFiles) ? res.tempFiles : (res.tempFiles ? [res.tempFiles] : []);
        
        if (files.length > 0) {
          const file = files[0];
          if (!file) {
            uni.showToast({
              title: t('pages.resume.attachment.fileRequired'),
              icon: 'none',
            });
            return;
          }
          
          const fileObj = file as any;
          const fileName = fileObj.name || 'resume.pdf';
          const filePath = fileObj.path || '';
          
          if (!filePath) {
            uni.showToast({
              title: t('pages.resume.attachment.uploadError'),
              icon: 'none',
            });
            return;
          }
          
          if (!fileName.toLowerCase().endsWith('.pdf')) {
            uni.showToast({
              title: t('pages.resume.attachment.uploadErrorFormat'),
              icon: 'none',
            });
            return;
          }
          
          navigateToUpload(filePath, fileName);
        } else {
          uni.showToast({
            title: t('pages.resume.attachment.fileRequired'),
            icon: 'none',
          });
        }
      },
      fail: (error) => {
        const errorMsg = error.errMsg || '';
        if (!errorMsg.includes('cancel') && !errorMsg.includes('取消')) {
          uni.showToast({
            title: t('pages.resume.attachment.uploadError'),
            icon: 'none',
            duration: 3000,
          });
        }
      },
    });
  } else {
    uni.showToast({
      title: t('pages.resume.attachment.uploadError'),
      icon: 'none',
    });
  }
}

// #ifdef APP-PLUS
function chooseFileForApp() {
  try {
    if (typeof uni.chooseFile !== 'undefined') {
      tryChooseFile();
      return;
    }
    
    // Fallback: use plus.io for file selection
    const Intent = (plus as any).android?.importClass('android.content.Intent');
    const Activity = (plus as any).android?.runtimeMainActivity();
    
    if (Intent && Activity) {
      const intent = new Intent(Intent.ACTION_GET_CONTENT);
      intent.setType('application/pdf');
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      
      Activity.startActivityForResult(intent, 1001, (resultCode: number, data: any) => {
        if (resultCode === Activity.RESULT_OK && data) {
          const uri = data.getData();
          if (uri) {
            const filePath = (plus as any).io.convertAbsoluteFileSystem(uri.toString());
            if (filePath) {
              navigateToUpload(filePath, 'resume.pdf');
            } else {
              uni.showToast({
                title: t('pages.resume.attachment.uploadError'),
                icon: 'none',
              });
            }
          }
        }
      });
    } else {
      uni.showToast({
        title: t('pages.resume.attachment.uploadError'),
        icon: 'none',
      });
    }
  } catch (error) {
    console.error('Failed to choose file:', error);
    uni.showToast({
      title: t('pages.resume.attachment.uploadError'),
      icon: 'none',
    });
  }
}
// #endif
// #endif

function navigateToUpload(filePath: string, fileName: string) {
  uni.navigateTo({
    url: `/pages/seeker/profile/resume/edit/upload-resume?filePath=${encodeURIComponent(filePath)}&defaultName=${encodeURIComponent(fileName)}`,
  });
}

function handleEditResume(resume: Resume) {
  uni.navigateTo({
    url: `/pages/seeker/profile/resume/edit/edit-resume?id=${resume.id}`,
  });
}

function getPrivacyLabel(level: number): string {
  const labels: Record<number, string> = {
    0: t('pages.resume.attachment.privacyPublic'),
    1: t('pages.resume.attachment.privacyPrivate'),
    2: t('pages.resume.attachment.privacyConfidential'),
  };
  return labels[level] || t('pages.resume.attachment.privacyPublic');
}

function formatDate(dateString: string): string {
  if (!dateString) return '';
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

async function handleDownload(resume: Resume) {
  if (downloading.value === resume.id) return;
  
  if (!resume.fileUrl) {
    uni.showToast({
      title: t('pages.resume.attachment.downloadError'),
      icon: 'none',
    });
    return;
  }

  downloading.value = resume.id;

  try {
    const token = uni.getStorageSync('auth_token');

    // #ifdef H5
    const link = document.createElement('a');
    link.href = resume.fileUrl;
    link.download = resume.resumeName.endsWith('.pdf') ? resume.resumeName : `${resume.resumeName}.pdf`;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    uni.showToast({
      title: t('pages.resume.attachment.downloadSuccess'),
      icon: 'success',
    });
    // #endif

    // #ifndef H5
    uni.showLoading({
      title: t('pages.resume.attachment.downloading'),
      mask: true,
    });

    uni.downloadFile({
      url: resume.fileUrl,
      header: {
        Authorization: token ? `Bearer ${token}` : '',
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const filePath = res.tempFilePath;
          const fileName = resume.resumeName.endsWith('.pdf') ? resume.resumeName : `${resume.resumeName}.pdf`;
          
          // #ifdef APP-PLUS
          try {
            const Documents = (plus as any).io.PUBLIC_DOCUMENTS;
            (plus as any).io.resolveLocalFileSystemURL(filePath, (entry: any) => {
              entry.copyTo(Documents, fileName, () => {
                uni.hideLoading();
                uni.showToast({
                  title: t('pages.resume.attachment.downloadSuccess'),
                  icon: 'success',
                });
              }, (error: any) => {
                console.error('Save file failed:', error);
                uni.hideLoading();
                uni.showToast({
                  title: t('pages.resume.attachment.downloadError'),
                  icon: 'none',
                });
              });
            }, (error: any) => {
              console.error('Resolve file failed:', error);
              uni.hideLoading();
              uni.showToast({
                title: t('pages.resume.attachment.downloadError'),
                icon: 'none',
              });
            });
          } catch (error) {
            console.error('Download failed:', error);
            uni.hideLoading();
            uni.showToast({
              title: t('pages.resume.attachment.downloadError'),
              icon: 'none',
            });
          }
          // #endif

          // #ifndef APP-PLUS
          uni.saveFile({
            tempFilePath: filePath,
            success: () => {
              uni.hideLoading();
              uni.showToast({
                title: t('pages.resume.attachment.downloadSuccess'),
                icon: 'success',
              });
            },
            fail: () => {
              uni.hideLoading();
              uni.showToast({
                title: t('pages.resume.attachment.downloadError'),
                icon: 'none',
              });
            },
          });
          // #endif
        } else {
          uni.hideLoading();
          uni.showToast({
            title: t('pages.resume.attachment.downloadError'),
            icon: 'none',
          });
        }
      },
      fail: () => {
        uni.hideLoading();
        uni.showToast({
          title: t('pages.resume.attachment.downloadError'),
          icon: 'none',
        });
      },
    });
    // #endif
  } catch (error) {
    console.error('Download failed:', error);
    uni.showToast({
      title: t('pages.resume.attachment.downloadError'),
      icon: 'none',
    });
  } finally {
    downloading.value = null;
  }
}
</script>

<style lang="scss" scoped>
@use "@/styles/variables.scss" as vars;

.attachment-resume-page {
  min-height: 100vh;
  background-color: #F2F2F7;
  display: flex;
  flex-direction: column;
  padding-bottom: 120rpx;
}

.state-card {
  margin: 64rpx 32rpx;
  padding: 48rpx;
  background-color: #FFFFFF;
  border-radius: 20rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.state-text {
  font-size: 32rpx;
  color: #8E8E93;
  font-weight: 400;
}

.resume-list {
  flex: 1;
  padding: 32rpx;
}

.resume-items {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.resume-item {
  background-color: #FFFFFF;
  border-radius: 20rpx;
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  transition: background-color 0.2s;
}

.resume-item-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.resume-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.resume-item-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.resume-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #000000;
}

.resume-privacy {
  font-size: 24rpx;
  color: vars.$primary-color;
  background-color: vars.$primary-color-soft;
  padding: 4rpx 16rpx;
  border-radius: 12rpx;
}

.resume-meta {
  font-size: 24rpx;
  color: #8E8E93;
}

.item-arrow {
  font-size: 48rpx;
  color: #C6C6C8;
  font-weight: 300;
}

.action-button {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  background-color: vars.$primary-color-soft;
  transition: opacity 0.2s;
  
  &:active {
    opacity: 0.7;
  }
}

.action-icon {
  font-size: 32rpx;
  color: vars.$primary-color;
}

.empty-state {
  padding: 80rpx 32rpx;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #8E8E93;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background-color: #F2F2F7;
}

.upload-button {
  background-color: vars.$primary-color;
  border-radius: 16rpx;
  padding: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s;
  
  &:active {
    opacity: 0.8;
  }
}

.button-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #FFFFFF;
}
</style>
