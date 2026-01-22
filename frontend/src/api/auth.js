import request from '../utils/request'

export const login = (data) => {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

export const getUserInfo = () => {
  return request({
    url: '/api/auth/info',
    method: 'get'
  })
}

export const register = (data) => {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}
