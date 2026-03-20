export interface loginQueryInterface {
  // 手机号码
  phone: string;
  // 	短信验证码
  code: string;
}

// 用户信息
export interface UserInfoInterface {
  // 用户id
  id: number;
   // 用户名
  nickName: string;
  // 头像
  icon: string;
}
// 用户state
export interface UserStateInterface {
  // 用户信息
  userInfo: UserInfoInterface | null;
  // token
  token: string | null;
}

// 用户详情
export interface UserDetailInterface {
  introduce: string;
  city: string;
  gender: 0 | 1;
  fans: number;
  followee: number;
}