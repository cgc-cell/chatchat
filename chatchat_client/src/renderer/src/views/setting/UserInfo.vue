<template>
  <ContentPanel>
    <div v-if="showType == 0" class="show-info">
      <div class="user-info">
        <UserBaseInfo :user-info="userInfo" :show-area="true"></UserBaseInfo>
        <div class="more-op">
          <el-dropdown trigger="click" placement="bottom-end">
            <span class="el-dropdown-link">
              <div class="iconfont icon-more"></div>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="changePart(1)">修改个人信息</el-dropdown-item>
                <el-dropdown-item @click="changePart(2)">修改密码</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <div class="part-item">
        <div class="part-title">朋友权限:</div>
        <div class="part-content">
          {{ userInfo.joinType == 0 ? '直接加入' : '加我好友需要验证' }}
        </div>
      </div>
      <div class="part-item">
        <div class="part-title">个性签名:</div>
        <div class="part-content">
          {{ userInfo.personalSignature || '-' }}
        </div>
      </div>
      <div class="logout">
        <el-button @click="logout">退出登录</el-button>
      </div>
    </div>
    <div v-if="showType == 1">
      <UserInfoEdit :data="userInfo" @callback="callback"></UserInfoEdit>
    </div>
    <div v-if="showType == 2">
      <UserInfoPassWord @callback="callback"></UserInfoPassWord>
    </div>
  </ContentPanel>
</template>

<script setup>
import ContentPanel from '../../components/ContentPanel.vue'
import UserBaseInfo from '../../components/UserBaseInfo.vue'
import { ref, getCurrentInstance } from 'vue'
import UserInfoEdit from './UserInfoEdit.vue'
import UserInfoPassWord from './UserInfoPassWord.vue'
const { proxy } = getCurrentInstance()
const userInfo = ref({})
//获取个人信息填入上方组件中
const getUserInfo = async () => {
  let result = await proxy.Request({
    url: proxy.api.getUserInfo,
    params: {}
  })
  if (!result) {
    return
  }
  userInfo.value = result
}
getUserInfo()
//通过showType来控制显示的内容
const showType = ref(0)
const changePart = (type) => {
  showType.value = type
}
//当点击保存个人信息后，重新获取个人信息并显示
const callback = () => {
  // console.log('callback')

  showType.value = 0
  getUserInfo()
}
//退出登录
const logout = () => {
  proxy.confirm({
    message: '是否退出登录?',
    okfun: async () => {
      window.ipcRenderer.send('reLogin')
      let result = await proxy.Request({
        url: proxy.api.logout
      })
      if (!result) {
        return
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.show-info {
  .user-info {
    position: relative;
    .more-op {
      position: absolute;
      right: 0;
      top: 20px;
      .icon-more {
        color: #9e9e9e;
        &:hover {
          background: #dddddd;
        }
      }
    }
  }
  .part-item {
    display: flex;
    border-bottom: 1px solid #eaeaea;
    padding: 20px 0;
    .part-title {
      width: 60px;
      color: #9e9e9e;
    }
    .part-content {
      flex: 1;
      margin-left: 15px;
      color: #161616;
    }
  }
  .logout {
    text-align: center;
    margin-top: 20px;
  }
}
</style>
