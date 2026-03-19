export type HomeCard = {
  title: string
  items: {
    image: string
    label: string
    link: string
  }[]
}

export type HomeCardVariant = 'grid' | 'featured' | 'strip'

export type HomeThemeConfig = {
  key: string
  title: string
  keywords: string[]
  fallbackItems: HomeCard['items']
}

export type HomeProductPreview = {
  productId: number
  name: string
  image: string
  price?: string
  categoryId?: number
  link: string
  salesCount?: number
  ratingAvg?: string
  commentCount?: number
}

export const DEFAULT_PRODUCT_IMAGE =
  'https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=900&q=80'

export const homeCategoryThemes: HomeThemeConfig[] = [
  {
    key: 'electronics',
    title: '热销电子产品',
    keywords: ['手机', '数码', '耳机', '相机', '电子'],
    fallbackItems: [
      {
        image:
          'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=640&q=80',
        label: '手机数码',
        link: '/category/0?q=手机数码',
      },
      {
        image:
          'https://images.unsplash.com/photo-1583394838336-acd977736f90?auto=format&fit=crop&w=640&q=80',
        label: '智能穿戴',
        link: '/category/0?q=智能穿戴',
      },
      {
        image:
          'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=640&q=80',
        label: '音频设备',
        link: '/category/0?q=音频设备',
      },
      {
        image:
          'https://images.unsplash.com/photo-1517336714739-489689fd1ca8?auto=format&fit=crop&w=640&q=80',
        label: '电脑配件',
        link: '/category/0?q=电脑配件',
      },
    ],
  },
  {
    key: 'office',
    title: '电脑办公',
    keywords: ['电脑', '办公', '键盘', '鼠标', '显示器'],
    fallbackItems: [
      {
        image:
          'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=640&q=80',
        label: '笔记本',
        link: '/category/0?q=笔记本',
      },
      {
        image:
          'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=640&q=80',
        label: '办公外设',
        link: '/category/0?q=办公外设',
      },
      {
        image:
          'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=640&q=80',
        label: '显示设备',
        link: '/category/0?q=显示器',
      },
      {
        image:
          'https://images.unsplash.com/photo-1587831990711-23ca6441447b?auto=format&fit=crop&w=640&q=80',
        label: '桌面收纳',
        link: '/category/0?q=桌面收纳',
      },
    ],
  },
  {
    key: 'home',
    title: '家居好物',
    keywords: ['家居', '厨房', '家电', '收纳', '家具'],
    fallbackItems: [
      {
        image:
          'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=640&q=80',
        label: '卧室焕新',
        link: '/category/0?q=卧室',
      },
      {
        image:
          'https://images.unsplash.com/photo-1484101403633-562f891dc89a?auto=format&fit=crop&w=640&q=80',
        label: '客厅布置',
        link: '/category/0?q=客厅',
      },
      {
        image:
          'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=640&q=80',
        label: '收纳整理',
        link: '/category/0?q=收纳',
      },
      {
        image:
          'https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=640&q=80',
        label: '厨房精选',
        link: '/category/0?q=厨房',
      },
    ],
  },
  {
    key: 'fashion',
    title: '时尚潮玩',
    keywords: ['时尚', '服饰', '鞋', '包', '玩具', '潮玩'],
    fallbackItems: [
      {
        image:
          'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=640&q=80',
        label: '潮流服饰',
        link: '/category/0?q=服饰',
      },
      {
        image:
          'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=640&q=80',
        label: '潮鞋精选',
        link: '/category/0?q=鞋子',
      },
      {
        image:
          'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=640&q=80',
        label: '配饰箱包',
        link: '/category/0?q=箱包',
      },
      {
        image:
          'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?auto=format&fit=crop&w=640&q=80',
        label: '热门潮玩',
        link: '/category/0?q=潮玩',
      },
    ],
  },
]

export const defaultHomeGuideCards: HomeCard[] = homeCategoryThemes.map((theme) => ({
  title: theme.title,
  items: theme.fallbackItems,
}))

export const mockFeaturedProduct: HomeProductPreview = {
  productId: 0,
  name: '新品推荐位即将上线',
  image:
    'https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=900&q=80',
  price: '0',
  categoryId: 0,
  link: '/category/0?q=新品',
}

export const mockStripProducts: HomeProductPreview[] = [
  {
    productId: 101,
    name: '热销耳机',
    image:
      'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80',
    price: '299',
    link: '/category/0?q=耳机',
  },
  {
    productId: 102,
    name: '轻薄笔记本',
    image:
      'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80',
    price: '5299',
    link: '/category/0?q=笔记本',
  },
  {
    productId: 103,
    name: '家用咖啡机',
    image:
      'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=80',
    price: '899',
    link: '/category/0?q=咖啡机',
  },
  {
    productId: 104,
    name: '运动潮鞋',
    image:
      'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80',
    price: '699',
    link: '/category/0?q=运动鞋',
  },
]
