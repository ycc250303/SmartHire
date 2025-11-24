import dayjs from 'dayjs';

// 工作待办
export interface DashboardTodoItem {
  id: string;
  title: string;
  desc: string;
  count: number;
  route: string;
}

// 招聘统计卡片
export interface RecruitStatistic {
  id: string;
  label: string;
  value: number;
  unit: string;
  trend: 'up' | 'down' | 'flat';
}

// 智能洞察
export interface InsightCardItem {
  id: string;
  title: string;
  desc: string;
  severity: 'info' | 'warning' | 'danger';
  actionText?: string;
  route?: string;
}

// 职位概览
export type JobStatus = 'open' | 'paused' | 'closed';
export interface JobSummary {
  jobId: number;
  title: string;
  city: string;
  salary: string;
  status: JobStatus;
  applied: number;
  interviewing: number;
  offer: number;
  updatedAt: string;
}

export interface CandidateMatch {
  id: string;
  name: string;
  jobId: number;
  intentPosition: string;
  score: number;
  status: '新投递' | '简历通过' | '面试中' | '已淘汰';
  tags: string[];
  matchExplain: string;
}

export interface JDQualityReport {
  score: number;
  risks: Array<{
    type: 'discrimination' | 'ambiguity' | 'other';
    message: string;
  }>;
  suggestions: string[];
  lastAnalyzedAt: string;
}

export interface RiskAlert {
  level: 'medium' | 'high';
  message: string;
  detail: string;
}

export interface ConversationPreview {
  id: string;
  candidate: string;
  jobTitle: string;
  lastMessage: string;
  timestamp: string;
  unread: boolean;
}

export interface NotificationItem {
  id: string;
  type: 'application' | 'interview' | 'warning' | 'system';
  title: string;
  desc: string;
  timestamp: string;
  route?: string;
}

export interface MessageBubble {
  id: string;
  from: 'hr' | 'candidate';
  content: string;
  time: string;
}

export interface HRProfile {
  name: string;
  role: string;
  email: string;
  phone: string;
  company: string;
  companyStatus: '已认证' | '未认证';
  notifyEnabled: boolean;
  darkMode: boolean;
}

export const mockDashboardTodos: DashboardTodoItem[] = [
  {
    id: 'new-applications',
    title: '新投递',
    desc: '需要初筛的候选人',
    count: 8,
    route: '/pages/hr/jobs/detail?jobId=101&section=candidates',
  },
  {
    id: 'pending-interview',
    title: '待安排面试',
    desc: '需要协调时间的候选人',
    count: 3,
    route: '/pages/hr/interview/index',
  },
  {
    id: 'unread-messages',
    title: '未读消息',
    desc: '候选人已回复等待处理',
    count: 5,
    route: '/pages/hr/messages/index?tab=chat',
  },
];

export const mockRecruitStats: RecruitStatistic[] = [
  { id: 'apply', label: '本周投递', value: 126, unit: '人', trend: 'up' },
  { id: 'interview', label: '面试人数', value: 48, unit: '场', trend: 'flat' },
  { id: 'offer', label: 'Offer', value: 9, unit: '份', trend: 'down' },
];

export const mockInsights: InsightCardItem[] = [
  {
    id: 'jd-quality',
    title: 'JD 合规提醒',
    desc: '2 个 JD 检测到潜在敏感描述，建议优化表述。',
    severity: 'warning',
    actionText: '查看详情',
    route: '/pages/hr/jobs/detail?jobId=101&section=jdQuality',
  },
  {
    id: 'anti-fraud',
    title: '异常职位提示',
    desc: '后端开发岗位曝光高于行业均值，请确认岗位信息。',
    severity: 'danger',
    actionText: '查看风险',
    route: '/pages/hr/jobs/detail?jobId=102&section=risk',
  },
  {
    id: 'match-efficiency',
    title: '匹配效率建议',
    desc: '前端开发岗位面试通过率 18%，建议优化 JD 或筛选条件。',
    severity: 'info',
    route: '/pages/hr/analytics/index',
  },
];

export const mockJobSummaries: JobSummary[] = [
  {
    jobId: 101,
    title: '高级前端工程师',
    city: '上海',
    salary: '25-35k·16薪',
    status: 'open',
    applied: 68,
    interviewing: 15,
    offer: 2,
    updatedAt: dayjs().subtract(2, 'hour').toISOString(),
  },
  {
    jobId: 102,
    title: 'Java 服务端工程师',
    city: '深圳',
    salary: '28-40k·16薪',
    status: 'paused',
    applied: 92,
    interviewing: 22,
    offer: 4,
    updatedAt: dayjs().subtract(1, 'day').toISOString(),
  },
  {
    jobId: 103,
    title: '数据平台产品经理',
    city: '北京',
    salary: '30-38k·15薪',
    status: 'open',
    applied: 34,
    interviewing: 6,
    offer: 1,
    updatedAt: dayjs().subtract(3, 'day').toISOString(),
  },
];

export const mockCandidateMatches: CandidateMatch[] = [
  {
    id: 'c1',
    name: '张琪',
    jobId: 101,
    intentPosition: '高级前端开发',
    score: 87,
    status: '面试中',
    tags: ['React', '5年', '上海'],
    matchExplain: '项目经验与岗位 JD 高度匹配。',
  },
  {
    id: 'c2',
    name: '李越',
    jobId: 101,
    intentPosition: '前端工程师',
    score: 78,
    status: '新投递',
    tags: ['Vue3', 'SaaS'],
    matchExplain: '拥有 B 端经验，技能贴合要求。',
  },
  {
    id: 'c3',
    name: '陈然',
    jobId: 101,
    intentPosition: '全栈工程师',
    score: 65,
    status: '简历通过',
    tags: ['Node.js', '小程序'],
    matchExplain: '技能覆盖前后端，沟通能力良好。',
  },
];

export const mockJDQualityReport: JDQualityReport = {
  score: 82,
  risks: [
    { type: 'discrimination', message: '“35 岁以下”存在年龄限制风险。' },
    { type: 'ambiguity', message: '“熟悉富文本前端”描述过于笼统。' },
  ],
  suggestions: [
    '补充岗位绩效指标，更易于候选人理解。',
    '拆分关键技能要求，突出优先项。',
  ],
  lastAnalyzedAt: dayjs().subtract(4, 'hour').toISOString(),
};

export const mockRiskAlert: RiskAlert = {
  level: 'high',
  message: '该岗位曝光异常升高，请复核 JD 信息。',
  detail: '后台检测到近期多条异常举报，建议联系法务复核招聘内容。',
};

export const mockConversations: ConversationPreview[] = [
  {
    id: 'chat-1',
    candidate: '张琪',
    jobTitle: '高级前端工程师',
    lastMessage: '好的，周三下午我有空，可以视频面。',
    timestamp: dayjs().subtract(12, 'minute').toISOString(),
    unread: true,
  },
  {
    id: 'chat-2',
    candidate: '李越',
    jobTitle: '前端工程师',
    lastMessage: '收到，我会尽快完善作业并发链接。',
    timestamp: dayjs().subtract(2, 'hour').toISOString(),
    unread: false,
  },
];

export const mockNotifications: NotificationItem[] = [
  {
    id: 'notice-1',
    type: 'application',
    title: '新的候选人投递',
    desc: '王晨投递了 高级前端工程师 岗位',
    timestamp: dayjs().subtract(1, 'hour').toISOString(),
    route: '/pages/hr/jobs/detail?jobId=101&section=candidates',
  },
  {
    id: 'notice-2',
    type: 'interview',
    title: '面试提醒',
    desc: '张琪 今日 14:00 二面，请提前确认',
    timestamp: dayjs().subtract(3, 'hour').toISOString(),
    route: '/pages/hr/interview/detail?id=2001',
  },
  {
    id: 'notice-3',
    type: 'warning',
    title: '风控提示',
    desc: '智能风控检测到 后端开发岗 有异常举报',
    timestamp: dayjs().subtract(1, 'day').toISOString(),
    route: '/pages/hr/jobs/detail?jobId=102&section=risk',
  },
];

export const mockChatHistory: MessageBubble[] = [
  {
    id: 'msg-1',
    from: 'candidate',
    content: 'HR 您好，想了解下岗位的技术栈。',
    time: dayjs().subtract(2, 'hour').format('HH:mm'),
  },
  {
    id: 'msg-2',
    from: 'hr',
    content: '主要使用 Vue3 + TypeScript + Vite，配合微前端。',
    time: dayjs().subtract(110, 'minute').format('HH:mm'),
  },
  {
    id: 'msg-3',
    from: 'candidate',
    content: '了解，面试安排有更新吗？',
    time: dayjs().subtract(15, 'minute').format('HH:mm'),
  },
];

export const mockProfile: HRProfile = {
  name: '陈 HR',
  role: '高级招聘顾问',
  email: 'chen.hr@smarthire.cn',
  phone: '138-0000-0000',
  company: '智聘星科技术有限公司',
  companyStatus: '已认证',
  notifyEnabled: true,
  darkMode: false,
};

// TODO: 后续替换为真实接口请求，例如 useRequest(() => http.get('/api/hr/dashboard'))
export const fetchDashboardData = async () => ({
  todos: mockDashboardTodos,
  stats: mockRecruitStats,
  insights: mockInsights,
});

export const fetchJobSummaries = async () => mockJobSummaries;
export const fetchJobDetail = async (jobId: number) => ({
  job: mockJobSummaries.find((j) => j.jobId === jobId) || mockJobSummaries[0],
  candidates: mockCandidateMatches,
  jdQuality: mockJDQualityReport,
  risk: mockRiskAlert,
});

export const fetchConversations = async () => mockConversations;
export const fetchNotifications = async () => mockNotifications;
export const fetchChatHistory = async () => mockChatHistory;
export const fetchProfile = async () => mockProfile;
