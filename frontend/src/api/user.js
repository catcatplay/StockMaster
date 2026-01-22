import request from '../utils/request'

export const getUserList = (params) => {
  return request({
    url: '/api/user/list',
    method: 'get',
    params
  })
}

export const getUserById = (id) => {
  return request({
    url: `/api/user/${id}`,
    method: 'get'
  })
}

export const addUser = (data) => {
  return request({
    url: '/api/user/add',
    method: 'post',
    data
  })
}

export const updateUser = (data) => {
  return request({
    url: '/api/user/update',
    method: 'put',
    data
  })
}

export const deleteUser = (id) => {
  return request({
    url: `/api/user/delete/${id}`,
    method: 'delete'
  })
}

export const updateUserStatus = (id, status) => {
  return request({
    url: `/api/user/status/${id}`,
    method: 'put',
    params: { status }
  })
}
