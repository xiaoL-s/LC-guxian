const { request } = require('../../utils/request')
const app = getApp()

Page({
  data: { my: {} },
  onShow() {
    if (!app.globalData.token) { wx.reLaunch({ url: '/pages/login/login' }); return }
    this.load()
  },
  async load() {
    try {
      const list = await request('/mini/work/wage?month=2026-09')
      // byWorker 接口返回全员汇总，取当前登录人
      const me = app.globalData.userInfo || {}
      const mine = (list || []).find(x => x.workerId === me.userId) || {}
      this.setData({ my: mine })
    } catch (e) {}
  }
})
