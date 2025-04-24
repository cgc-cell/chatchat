import moment from 'moment'

const Isempty = (obj) => {
  if (obj === null || obj === undefined || obj === '') {
    return true
  }
  return false
}
const getAreaInfo = (area) => {
  if (Isempty(area)) {
    return '-'
  }
  return area.replace(',', ' ')
}

const formatDate = (timeStamp) => {
  const timeStampTime = moment(timeStamp)
  const days =
    Number.parseInt(moment().format('YYYYMMDD')) - Number.parseInt(timeStampTime.format('YYYYMMDD'))
  if (days === 0) {
    return timeStampTime.format('HH:mm')
  }
  if (days === 1) {
    return '昨天'
  }
  if (days > 2 && days < 7) {
    return timeStampTime.format('dddd')
  }
  return timeStampTime.format('YY-MM-DD')
}

const size2Str = (limit) => {
  var size = ''
  if (limit < 0.1 * 1024) {
    //小于0.1KB，则转化成B
    size = limit.toFixed(2) + 'B'
  } else if (limit < 1024 * 1024) {
    size = (limit / 1024).toFixed(2) + 'KB'
  } else if (limit < 1024 * 1024 * 1024) {
    size = (limit / (1024 * 1024)).toFixed(2) + 'MB'
  } else {
    size = (limit / (1024 * 1024 * 1024)).toFixed(2) + 'GB'
  }
  var sizeStr = size + ''
  var index = sizeStr.indexOf('.')
  var dou = sizeStr.substring(index + 1, 2)
  if (dou == '00') {
    return sizeStr.substring(0, index) + sizeStr.substr(index + 3, 2)
  }
  return size
}
export default {
  Isempty,
  getAreaInfo,
  formatDate,
  size2Str
}
