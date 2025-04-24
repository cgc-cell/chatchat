import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import '@/assets/base.scss'
import '@/assets/cust-elementplus.scss'
import '@/assets/icon/iconfont.css'
import Utils from './Utils/Utils'
import Verify from './Utils/Verify'
import message from './Utils/Message'
import api from './Utils/Api'
import Request from './Utils/Request'
import * as Pinia from 'pinia'
import ContentPanel from './components/ContentPanel.vue'
import Layout from './components/Layout.vue'
import WinOp from './components/WinOp.vue'
import ShowLocalImage from './components/ShowLocalImage.vue'
import UserBaseInfo from './components/UserBaseInfo.vue'
import Dailog from './components/Dailog.vue'
import AvatarUpload from './components/AvatarUpload.vue'
import confirm from './Utils/Confirm'
import Badge from './components/Badge.vue'
const app = createApp(App)
app.config.globalProperties.Utils = Utils
app.config.globalProperties.Verify = Verify
app.config.globalProperties.message = message
app.config.globalProperties.api = api
app.config.globalProperties.Request = Request
app.config.globalProperties.confirm = confirm
app.component('Badge', Badge)
app.component('ShowLocalImage', ShowLocalImage)
app.component('WinOp', WinOp)
app.component('Layout', Layout)
app.component('ContentPanel', ContentPanel)
app.component('UserBaseInfo', UserBaseInfo)
app.component('Dailog', Dailog)
app.component('AvatarUpload', AvatarUpload)
app.use(Pinia.createPinia())
app.use(ElementPlus)
app.use(router)
app.mount('#app')
