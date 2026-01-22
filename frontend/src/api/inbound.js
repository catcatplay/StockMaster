import request from '@/utils/request'

export function createInbound(data) {
  return request({
    url: '/inbound/create',
    method: 'post',
    data
  })
}

export function getInboundList(params) {
  // 如果传入了current参数，使用分页接口
  const url = params && params.current ? '/inbound/page' : '/inbound/list'
  return request({
    url,
    method: 'get',
    params
  })
}

export function getInboundByGoodsId(goodsId) {
  return request({
    url: `/inbound/listByGoods/${goodsId}`,
    method: 'get'
  })
}

export function getInboundByTime(startTime, endTime) {
  return request({
    url: '/inbound/listByTime',
    method: 'get',
    params: { startTime, endTime }
  })
}

export function updateSettlement(data) {
  return request({
    url: '/inbound/updateSettlement',
    method: 'put',
    data
  })
}
