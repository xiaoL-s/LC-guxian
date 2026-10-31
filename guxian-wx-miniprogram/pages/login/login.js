const { request } = require('../../utils/request')
const { openNetSetting } = require('../../utils/netSetting')
const app = getApp()

Page({
  data: { name: '', password: '', loading: false },
  onName(e) { this.setData({ name: e.detail.value }) },
  onPassword(e) { this.setData({ password: e.detail.value }) },
  openNetSetting,
  async login() {
    const name = this.data.name.trim()
    const password = this.data.password
    if (!name) { wx.showToast({ title: '请输入员工姓名', icon: 'none' }); return }
    if (!password) { wx.showToast({ title: '请输入员工密码', icon: 'none' }); return }
    this.setData({ loading: true })
    try {
      const data = await request('/mini/auth/login', { method: 'POST', data: { name, password }, auth: false })
      app.globalData.token = data.token
      app.globalData.userInfo = data
      wx.setStorageSync('token', data.token)
      wx.setStorageSync('userInfo', data)
      wx.showToast({ title: '欢迎 ' + (data.realName || ''), icon: 'success' })
      setTimeout(() => wx.reLaunch({ url: '/pages/index/index' }), 600)
    } catch (e) {
      console.error(e)
    } finally {
      this.setData({ loading: false })
    }
  }
})
