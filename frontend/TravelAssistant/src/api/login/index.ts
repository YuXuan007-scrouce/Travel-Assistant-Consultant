import http from "../../utils/http/index"

import type {
  loginQueryInterface,UserInfoInterface
} from "./type";


/**
 * @description 登录
 * @param params
 */
export function login(params: loginQueryInterface) {
  return http.post<string>(`/app/login`, params);
}

/**
 * @description 获取短信验证码
 * @param params
 */

export function getSmsCode(phone: string) {
  return http.get("/app/login/getCode", {
    phone
  });
}
/**
 * @description 获取用户基本信息即对应后端ThreadLocal中的用户信息
 */
export function getUserInfo() {
  return http.get<UserInfoInterface>(`/app/me`);
}
