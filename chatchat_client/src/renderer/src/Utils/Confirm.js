import { ElMessageBox } from 'element-plus'

const confirm = ({ message, okfun, showCancelBtn = true, okTest = '确定' }) => {
  ElMessageBox.confirm(message, '提示', {
    'close-on-click-modal': false,
    confirmButtonText: okTest,
    cancelButtonText: '取消',
    showCancelButton: showCancelBtn,
    type: 'info'
  })
    .then(async () => {
      if (okfun) {
        okfun()
      }
    })
    .catch(() => {})
}
export default confirm
