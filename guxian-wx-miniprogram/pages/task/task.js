const { request } = require('../../utils/request')
const app = getApp()

Page({
  data: { list: [], userInfo: {} },
  onShow() {
    if (!app.globalData.token) { wx.reLaunch({ url: '/pages/login/login' }); return }
    this.setData({ userInfo: app.globalData.userInfo })
    this.load()
  },
  async load() {
    try {
      const data = await request('/mini/work/tasks?workStatus=PROCESSING')
      const rows = (data && data.records) || []
      this.setData({ list: rows })
    } catch (e) {}
  },
  openDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/report/report?workId=' + id })
  },
  logout() {
    wx.removeStorageSync('token'); wx.removeStorageSync('userInfo')
    app.globalData.token = ''
    wx.reLaunch({ url: '/pages/login/login' })
  }
})
