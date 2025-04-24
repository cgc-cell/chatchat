import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  mode: 'history',
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '首页',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login.vue')
    },
    {
      path: '/showMedia',
      name: '展示页面',
      component: () => import('@/views/show/showMedia.vue')
    },
    {
      path: '/admin',
      name: '管理后台',
      component: () => import('@/views/admin/Admin.vue'),
      redirect: '/admin/userList',
      children: [
        {
          path: '/admin/userList',
          name: '用户管理',
          component: () => import('@/views/admin/UserList.vue')
        },
        {
          path: '/admin/groupList',
          name: '群组管理',
          component: () => import('@/views/admin/GroupList.vue')
        },
        {
          path: '/admin/beautyAccount',
          name: '靓号管理',
          component: () => import('@/views/admin/BeautyAccount.vue')
        },
        {
          path: '/admin/update',
          name: '应用更新',
          component: () => import('@/views/admin/Update.vue')
        },
        {
          path: '/admin/sysSetting',
          name: '系统设置',
          component: () => import('@/views/admin/SysSetting.vue')
        }
      ]
    },
    {
      path: '/main',
      name: '聊天主页面',
      component: () => import('@/views/main.vue'),
      redirect: '/chat',
      children: [
        {
          path: '/contact',
          name: '联系人',
          component: () => import('@/views/contact/Contact.vue'),
          redirect: '/contact/blank',
          children: [
            {
              path: '/contact/blank',
              name: '空白页',
              component: () => import('@/views/contact/BlankPage.vue')
            },
            {
              path: '/contact/search',
              name: '搜索',
              component: () => import('@/views/contact/Search.vue')
            },
            {
              path: '/contact/createGroup',
              name: '新建群聊',
              component: () => import('@/views/contact/GroupEdit.vue')
            },
            {
              path: '/contact/userDetail',
              name: '用户详情',
              component: () => import('@/views/contact/UserDetail.vue')
            },
            {
              path: '/contact/groupDetail',
              name: '群组详情',
              component: () => import('@/views/contact/GroupDetail.vue')
            },
            {
              path: '/contact/contactNotice',
              name: '好友申请通知',
              component: () => import('@/views/contact/ContactApply.vue')
            }
          ]
        },
        {
          path: '/chat',
          name: 'Chat',
          component: () => import('@/views/chat/Chat.vue')
        },
        {
          path: '/setting',
          name: '设置',
          component: () => import('@/views/setting/Setting.vue'),
          redirect: '/setting/userInfo',
          children: [
            {
              path: '/setting/userInfo',
              name: '设置个人信息',
              component: () => import('@/views/setting/UserInfo.vue')
            },
            {
              path: '/setting/fileManage',
              name: '设置文件管理',
              component: () => import('@/views/setting/FileManage.vue')
            },
            {
              path: '/setting/about',
              name: '设置关于我们',
              component: () => import('@/views/setting/About.vue')
            }
          ]
        }
      ]
    }
  ]
})

export default router
