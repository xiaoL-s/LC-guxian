const app = getApp()
const { openNetSetting } = require('../../utils/netSetting')

Page({
  data: { realName: '', postType: '' },
  onShow() {
    const u = app.globalData.userInfo || wx.getStorageSync('userInfo')
    if (!app.globalData.token) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.setData({ realName: (u && u.realName) || '', postType: (u && u.postType) || '' })
  },
  goProduce() {
    wx.navigateTo({ url: '/pages/produce/produce' })
  },
  openNetSetting,
  logout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.globalData.token = ''
          app.globalData.userInfo = null
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          wx.reLaunch({ url: '/pages/login/login' })
        }
      }
    })
  }
})
