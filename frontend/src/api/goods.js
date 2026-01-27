import request from '@/utils/request'

export function addGoods(data) {
  return request({
    url: '/goods/add',
    method: 'post',
    data
  })
}

export function updateGoods(data) {
  return request({
    url: '/goods/update',
    method: 'put',
    data
  })
}

export function deleteGoods(id) {
  return request({
    url: `/goods/delete/${id}`,
    method: 'delete'
  })
}

export function getGoodsList(params) {
  // 如果传入了current参数，使用分页接口
  const url = params && params.current ? '/goods/page' : '/goods/list'
  return request({
    url,
    method: 'get',
    params
  })
}

export function searchGoods(name) {
  return request({
    url: '/goods/search',
    method: 'get',
    params: { name }
  })
}

export function getGoodsStockCount(type) {
  return request({
    url: '/goods/stock-count',
    method: 'get',
    params: { type }
  })
}
