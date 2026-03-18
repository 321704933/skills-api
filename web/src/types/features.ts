export interface Feature {
  id: string
  icon: string
  title: string
  description: string
  tags: string[]
  color: string
}

export const features: Feature[] = [
  {
    id: 'hotsearch',
    icon: 'Flame',
    title: '热搜数据',
    description: '实时采集多平台热搜数据，掌握全网热点动态',
    tags: ['百度', '微博', '抖音', '头条', 'B站'],
    color: '#FF6B6B'
  },
  {
    id: 'morningnews',
    icon: 'Newspaper',
    title: '每日早报',
    description: '7大分类新闻早报推送，一站式资讯获取',
    tags: ['综合', '财经', '科技', '体育', '国际', '汽车', '游戏'],
    color: '#4ECDC4'
  },
  {
    id: 'stockindex',
    icon: 'TrendingUp',
    title: '股票指数',
    description: 'A股、港股、美股三大市场实时指数行情',
    tags: ['A股', '港股', '美股', '实时'],
    color: '#E74C3C'
  },
  {
    id: 'weather',
    icon: 'Cloud',
    title: '天气预报',
    description: '实时天气与7天预报，包含生活指数建议',
    tags: ['实时', '7天预报', '生活指数'],
    color: '#45B7D1'
  },
  {
    id: 'almanac',
    icon: 'Calendar',
    title: '今日黄历',
    description: '传统农历与宜忌查询，吉凶方位一览',
    tags: ['农历', '宜忌', '方位'],
    color: '#96CEB4'
  },
  {
    id: 'ip',
    icon: 'MapPin',
    title: 'IP查询',
    description: '离线IP地理位置查询，零延迟响应',
    tags: ['离线库', '零延迟'],
    color: '#DDA0DD'
  },
  {
    id: 'prose',
    icon: 'Quote',
    title: '散文句子',
    description: '经典散文句子随机获取，文字之美',
    tags: ['随机', '经典'],
    color: '#FFD93D'
  },
  {
    id: 'sensitive',
    icon: 'Shield',
    title: '敏感词检测',
    description: 'DFA算法实时敏感词过滤，内容安全',
    tags: ['DFA算法', '实时过滤'],
    color: '#6BCB77'
  },
  {
    id: 'captcha',
    icon: 'Key',
    title: '验证码服务',
    description: '多类型验证码生成与校验，安全可靠',
    tags: ['生成', '校验', '多类型'],
    color: '#FF8C42'
  },
  {
    id: 'image',
    icon: 'Image',
    title: '图片转换',
    description: '支持多种格式图片转换与处理',
    tags: ['SVG', 'PNG', 'JPG', 'WEBP'],
    color: '#9B59B6'
  }
]
