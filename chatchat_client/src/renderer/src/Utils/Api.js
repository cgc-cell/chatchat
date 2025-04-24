const api = {
  proDomain: 'http://127.0.0.1:5050',
  devDomain: 'http://127.0.0.1:5050',
  proWsDomain: 'ws:/127.0.0.1:5051/ws',
  devWsDomain: 'ws:/127.0.0.1:5051/ws',
  checkCode: '/account/checkCode', // 验证码
  login: '/account/login', // 登录
  register: '/account/register', // 注册
  search: '/contact/search', // 搜索
  applyAdd: '/contact/applyAdd', // 添加好友
  loadContact: '/contact/loadContact', // 加载联系人
  getContactInfo: '/contact/getUserInfo', // 获取所拥有的所有联系人信息
  getContactUserInfo: '/contact/getContactUserInfo', // 获取联系人个体具体信息
  loadApply: '/contact/loadApply', // 加载申请
  dealWithApply: '/contact/dealWithApply', // 处理申请
  saveGroup: '/group/saveGroup', // 保存群组
  loadMyGroup: '/group/loadMyGroup', // 加载我的群组
  leaveGroup: '/group/leaveGroup', // 离开群组
  getGroupInfo: '/group/loadGroupInfo', // 获取某个群组详细信息
  dissolutionGroup: '/group/dissolutionGroup', // 解散群组
  getGroupInfo4Chat: '/group/getGroupInfo4Chat', // 获取群组信息用于聊天
  addOrRemoveGroupUser: '/group/addOrRemoveMember', // 添加或删除群成员
  addContact2BlackList: '/contact/addContact2BlackList', // 添加联系人到黑名单
  delContact: '/contact/deleteContact', // 删除联系人
  getUserInfo: '/userInfo/getMyUserInfo', // 获取用户信息
  saveUserInfo: '/userInfo/saveMyUserInfo', // 保存用户信息
  updatePassword: '/userInfo/updatePassword', // 修改密码
  logout: '/userInfo/logout', // 退出登录
  sendMessage: '/chat/sendMessage', // 发送消息
  getSysSetting: '/account/getSystemSetting', // 获取系统设置
  checkVersion: '/update/checkVersion', // 检查版本
  loadAdminAccount: '/admin/loadUser', // 加载管理员账号
  updateUserStatus: '/admin/updateUserStatus', // 更新用户状态
  forceOffLine: '/admin/forceOffLine', // 强制下线
  loadBeautyAccount: '/admin/loadBeautyAccountList', // 加载美容账号
  saveBeautAccount: '/admin/saveBeautAccount', // 保存美容账号
  loadGroup: '/admin/loadGroup', // 加载群组
  adminDissolutionGroup: '/admin/dissolutionGroup', // 管理员解散群组
  getSysSetting4Admin: '/admin/getSysSetting', // 获取系统设置
  loadUpdateDataList: '/admin/loadUpdateList', // 加载更新数据列表
  saveUpdate: '/admin/saveUpdate', // 保存更新数据
  postUpdate: '/admin/postUpdate', // 发布更新
  delUpdate: '/admin/delUpdate' // 删除更新
}
export default api
