// API 响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页响应
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

// 用户
export interface User {
  id: number
  username: string
  email?: string
  nickname?: string
  avatar?: string
  balance?: number
  role?: string
}

// 登录响应
export interface LoginResponse {
  token: string
  userInfo: User
}

// 游戏
export interface Game {
  id: number
  title: string
  description?: string
  detailDescription?: string
  coverImage?: string
  bannerImage?: string
  screenshots?: string
  videoUrl?: string
  originalPrice: number
  discountPrice?: number
  discountPercent?: number
  developer?: string
  publisher?: string
  releaseDate?: string
  minRequirements?: string
  recRequirements?: string
  tags?: string
  stock?: number
  salesCount?: number
  rating?: number
  ratingCount?: number
  isFeatured?: number
}

// 分类
export interface Category {
  id: number
  name: string
  description?: string
  icon?: string
  sortOrder?: number
}

// 购物车项
export interface CartItem {
  id: number
  userId: number
  gameId: number
  quantity: number
  game: Game
}

// 愿望单项
export interface WishlistItem {
  id: number
  userId: number
  gameId: number
  createdAt: string
  game: Game
}

// 订单
export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  payAmount: number
  discountAmount?: number
  status: 'PENDING' | 'PAID' | 'CANCELLED' | 'COMPLETED'
  payTime?: string
  createdAt: string
  orderItems?: OrderItem[]
}

// 订单项
export interface OrderItem {
  id: number
  orderId: number
  gameId: number
  gameTitle: string
  gameCover?: string
  price: number
  quantity: number
}

// 用户游戏库
export interface UserLibrary {
  id: number
  userId: number
  gameId: number
  orderId?: number
  playTime?: number
  lastPlayedAt?: string
  createdAt: string
  game: Game
}

// 评论
export interface GameReview {
  id: number
  userId: number
  gameId: number
  rating: number
  content?: string
  isRecommend?: number
  helpfulCount?: number
  createdAt: string
  user?: User
}

// 游戏查询参数
export interface GameQueryParams {
  keyword?: string
  categoryId?: number
  priceRange?: string
  sortBy?: string
  sortOrder?: string
  page?: number
  size?: number
  onSale?: boolean
  featured?: boolean
}
