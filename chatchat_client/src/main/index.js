import { app, shell, BrowserWindow, Tray, Menu } from 'electron'
import { join } from 'path'
import { electronApp, optimizer, is } from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
import {
  onLoginorRegister,
  onOpenChat,
  onWinTitleOp,
  ongetLocalStore,
  onsetLocalStore,
  onLoadSessionData,
  onDelChatSession,
  onTopChatSession,
  onloadChatMessage,
  onAddLocalMessage,
  onSetSessionSelect,
  OnCreateCover,
  onOpenNewWindow,
  onSaveAs,
  onsaveClipboardFile,
  onLoadContactApply,
  onUpdateContactNoReadCount,
  onReLogin,
  OnOpenLocalFolder,
  onGetSysSetting,
  onChangeLocalFolder,
  OnReloadChatSession,
  onOpenUrl,
  onDownloadUpdate,
  onLoadlocalUser,
  openWindow
} from './ipc'
import { saveWindow } from './WindowProxy'

const NODE_ENV = process.env.NODE_ENV
const login_width = 300
const login_height = 370
const rejister_height = 490
function createWindow() {
  // Create the browser window.
  const mainWindow = new BrowserWindow({
    icon: icon,
    width: login_width,
    height: login_height,
    show: false,
    autoHideMenuBar: true,
    titleBarStyle: 'hidden',
    transparent: true,
    resizable: false,
    frame: true,

    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      sandbox: false,
      contextIsolation: false //关闭隔离后才能实现渲染进程与主进程通信
    }
  })
  //开去控制台
  saveWindow('main', mainWindow)
  if (NODE_ENV === 'development') {
    mainWindow.webContents.openDevTools()
  }
  mainWindow.on('ready-to-show', () => {
    mainWindow.show()
    mainWindow.setTitle('ChatChat')
  })

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url)
    return { action: 'deny' }
  })

  // HMR for renderer base on electron-vite cli.
  // Load the remote URL for development or the local html file for production.
  if (is.dev && process.env['ELECTRON_RENDERER_URL']) {
    mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL'])
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }
  //托盘区域
  const tray = new Tray(icon)
  //定义右键菜单
  const contextMenu = [
    {
      label: '退出ChatChat',
      click: () => {
        app.exit()
      }
    }
  ]
  //创建菜单
  const menu = Menu.buildFromTemplate(contextMenu)
  //设置托盘图标的提示信息
  tray.setToolTip('ChatChat')
  //设置托盘图标的上下文菜单
  tray.setContextMenu(menu)
  //处理托盘图标的点击事件：
  tray.on('click', () => {
    //使主窗口可以在任务栏中显示。
    mainWindow.setSkipTaskbar(false)
    mainWindow.show()
  })

  //切换登录注册窗口
  onLoginorRegister((IsLogin) => {
    mainWindow.setResizable(true)
    if (IsLogin) {
      mainWindow.setSize(login_width, login_height)
    } else {
      mainWindow.setSize(login_width, rejister_height)
    }
    mainWindow.setResizable(false)
  })
  //打开聊天窗口
  onOpenChat((config) => {
    // console.log('config', config)
    mainWindow.setResizable(true)
    mainWindow.setSize(750, 700)
    mainWindow.setMinimumSize(800, 600)
    mainWindow.center()
    mainWindow.setMaximizable(true)
    if (config.admin) {
      contextMenu.unshift({
        label: '后台管理',
        click: () => {
          openWindow({
            windowId: 'admin',
            title: '后台管理',
            path: '/admin',
            width: config.widowWidth,
            height: config.widowHeight,
            data: {
              token: config.token
            }
          })
        }
      })
    }
    //设置托盘
    contextMenu.unshift({
      label: '用户：' + config.nickName,
      click: () => {}
    })
    tray.setContextMenu(Menu.buildFromTemplate(contextMenu))
  })
  //窗口右上角的几个操作
  onWinTitleOp((e, { action, data }) => {
    const webContents = e.sender
    const win = BrowserWindow.fromWebContents(webContents) //获取当前的窗口
    switch (action) {
      case 'close': {
        if (data.closeType === 0) {
          win.close()
        } else {
          win.setSkipTaskbar(true) //它的作用是让指定的窗口在任务栏中不显示。
          win.hide()
        }
        break
      }
      case 'minimize': {
        win.minimize()
        break
      }
      case 'maximize': {
        win.maximize()
        break
      }
      case 'unmaximize': {
        win.unmaximize()
        break
      }
      case 'top': {
        win.setAlwaysOnTop(data.top)
        break
      }
    }
  })
  //设置本地存储
  onsetLocalStore()
  //获取本地存储
  ongetLocalStore()
  //进入聊天拿取数据库内容发送给渲染进程
  onLoadSessionData()
  //删除某个会话
  onDelChatSession()
  //置顶某个会话
  onTopChatSession()
  //拿到某个会话的消息记录
  onloadChatMessage()
  //发送消息后存入本地
  onAddLocalMessage()
  //点击某个会话时保存当前会话的id
  onSetSessionSelect()
  //发送视频消息
  OnCreateCover()
  //点击图片/视频进行预览
  onOpenNewWindow()
  //点击图片/视频进行保存
  onSaveAs()
  //截图上传功能
  onsaveClipboardFile()
  //添加好友申请未读数量
  onLoadContactApply()
  //点击后清除联系人申请未读数量
  onUpdateContactNoReadCount()
  //重新上号
  onReLogin(() => {
    mainWindow.setResizable(true)
    mainWindow.setMinimumSize(login_width, login_height)
    mainWindow.setSize(login_width, login_height)
    mainWindow.center()
    mainWindow.setResizable(false)
  })
  //打开文件夹
  OnOpenLocalFolder()
  //给渲染进程发送系统设置信息
  onGetSysSetting()
  //修改文件路径
  onChangeLocalFolder()
  //改变会话状态，使其在会话列表里显示或隐藏
  OnReloadChatSession()
  //打开其他链接更新
  onOpenUrl()
  onDownloadUpdate()
  onLoadlocalUser()
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  // Set app user model id for windows
  electronApp.setAppUserModelId('com.electron')

  // Default open or close DevTools by F12 in development
  // and ignore CommandOrControl + R in production.
  // see https://github.com/alex8088/electron-toolkit/tree/master/packages/utils
  app.on('browser-window-created', (_, window) => {
    optimizer.watchWindowShortcuts(window)
  })

  createWindow()

  app.on('activate', function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

// In this file you can include the rest of your app"s specific main process
// code. You can also put them in separate files and require them here.
