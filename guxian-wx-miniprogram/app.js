App({
  globalData: {
    // 后端地址：开发阶段用本机，上线改成 https://你的域名
    baseUrl: 'http://127.0.0.1:8086',
    token: '',
    userInfo: null
  },
  onLaunch() {
    this.globalData.token = wx.getStorageSync('token') || ''
    this.globalData.userInfo = wx.getStorageSync('userInfo') || null
  }
})
