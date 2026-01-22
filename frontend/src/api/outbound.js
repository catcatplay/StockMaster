import request from '@/utils/request'

export function createOutbound(data) {
  return request({
    url: '/outbound/create',
    method: 'post',
    data
  })
}

export function getOutboundList(params) {
  // 如果传入了current参数，使用分页接口
  const url = params && params.current ? '/outbound/page' : '/outbound/list'
  return request({
    url,
    method: 'get',
    params
  })
}

export function getOutboundByGoodsId(goodsId) {
  return request({
    url: `/outbound/listByGoods/${goodsId}`,
    method: 'get'
  })
}

export function getOutboundByTime(startTime, endTime) {
  return request({
    url: '/outbound/listByTime',
    method: 'get',
    params: { startTime, endTime }
  })
}

export function getTotalQuantity(goodsId) {
  return request({
    url: `/outbound/totalQuantity/${goodsId}`,
    method: 'get'
  })
}
