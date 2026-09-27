const app = getApp()

function request(path, { method = 'GET', data = {}, auth = true } = {}) {
  return new Promise((resolve, reject) => {
    const header = { 'Content-Type': 'application/json' }
    if (auth && app.globalData.token) {
      header['Authorization'] = 'Bearer ' + app.globalData.token
    }
    wx.request({
      url: app.globalData.baseUrl + path,
      method,
      data,
      header,
      success(res) {
        if (res.statusCode === 401) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          app.globalData.token = ''
          wx.reLaunch({ url: '/pages/login/login' })
          return reject(new Error('未登录'))
        }
        const body = res.data || {}
        if (body.code === 200) {
          resolve(body.data)
        } else {
          wx.showToast({ title: body.msg || '请求失败', icon: 'none' })
          reject(new Error(body.msg))
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = { request }
