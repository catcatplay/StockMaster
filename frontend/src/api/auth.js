import request from '../utils/request'

export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const getUserInfo = () => {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export const getCaptcha = () => {
  return request({
    url: '/auth/captcha',
    method: 'get'
  })
}
