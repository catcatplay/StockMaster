import { ref } from 'vue'
import { ElMessage } from 'element-plus'

export function useExport() {
  const exportLoading = ref(false)

  const handleExport = async (apiFunction, fileName, params = undefined) => {
    exportLoading.value = true
    try {
      const response = await apiFunction(params)
      const blob = new Blob([response], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = `${fileName}_${Date.now()}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)

      ElMessage.success('导出成功')
    } catch (error) {
      console.error('导出失败', error)
      ElMessage.error('导出失败')
    } finally {
      exportLoading.value = false
    }
  }

  return {
    exportLoading,
    handleExport
  }
}
