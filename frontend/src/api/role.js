import request from '../utils/request'

export const getRoleList = (params) => {
  return request({
    url: '/api/role/list',
    method: 'get',
    params
  })
}

export const getAllRoles = () => {
  return request({
    url: '/api/role/all',
    method: 'get'
  })
}

export const getRoleById = (id) => {
  return request({
    url: `/api/role/${id}`,
    method: 'get'
  })
}

export const addRole = (data) => {
  return request({
    url: '/api/role/add',
    method: 'post',
    data
  })
}

export const updateRole = (data) => {
  return request({
    url: '/api/role/update',
    method: 'put',
    data
  })
}

export const deleteRole = (id) => {
  return request({
    url: `/api/role/delete/${id}`,
    method: 'delete'
  })
}
