import { reactive } from 'vue'

export function usePagination(loadData, options = {}) {
  const { defaultPageSize = 20 } = options

  const pagination = reactive({
    currentPage: 1,
    pageSize: defaultPageSize,
    total: 0
  })

  const handleSizeChange = (val) => {
    pagination.pageSize = val
    pagination.currentPage = 1
    loadData()
  }

  const handleCurrentChange = (val) => {
    pagination.currentPage = val
    loadData()
  }

  const resetPage = () => {
    pagination.currentPage = 1
  }

  return {
    pagination,
    handleSizeChange,
    handleCurrentChange,
    resetPage
  }
}
