const { request } = require('../../utils/request')
const app = getApp()

Page({
  data: { phone: '', loading: false },
  onPhone(e) { this.setData({ phone: e.detail.value }) },
  async login() {
    if (!/^1\d{10}$/.test(this.data.phone)) {
      wx.showToast({ title: '请输入正确手机号', icon: 'none' }); return
    }
    this.setData({ loading: true })
    try {
      const data = await request('/mini/auth/login', { method: 'POST', data: { phone: this.data.phone }, auth: false })
      app.globalData.token = data.token
      app.globalData.userInfo = data
      wx.setStorageSync('token', data.token)
      wx.setStorageSync('userInfo', data)
      wx.showToast({ title: '欢迎 ' + data.realName, icon: 'success' })
      setTimeout(() => wx.switchTab({ url: '/pages/task/task' }), 800)
    } catch (e) {
      console.error(e)
    } finally {
      this.setData({ loading: false })
    }
  }
})
