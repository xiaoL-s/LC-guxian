App({
  globalData: {
    // 后端地址：默认电脑局域网IP；换WiFi后可在登录页「网络设置」里修改并自动记住
    // 上线时改成 https://你的域名（在登录页网络设置填一次即可，不再改动代码）
    baseUrl: '',
    token: '',
    userInfo: null
  },
  onLaunch() {
    // 优先使用用户在「网络设置」里保存的地址，没有则用默认IP
    this.globalData.baseUrl = wx.getStorageSync('baseUrl') || 'http://192.168.11.112:8086'
    this.globalData.token = wx.getStorageSync('token') || ''
    this.globalData.userInfo = wx.getStorageSync('userInfo') || null
  }
})
