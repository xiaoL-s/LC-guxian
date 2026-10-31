const app = getApp()

/**
 * 网络设置：换WiFi后电脑局域网IP会变，在这里填一次新IP并自动记住。
 * 格式示例：192.168.11.112:8086（可不带 http://，自动补全）
 */
function openNetSetting() {
  const cur = wx.getStorageSync('baseUrl') || app.globalData.baseUrl || ''
  const curIp = cur.replace(/^https?:\/\//, '').replace(/\/+$/, '')
  wx.showModal({
    title: '网络设置',
    editable: true,
    placeholderText: '例如 192.168.11.112:8086',
    content: curIp,
    confirmText: '保存',
    success(res) {
      if (!res.confirm) return
      let v = (res.content || '').trim()
      if (!v) { wx.showToast({ title: '请输入电脑IP和端口', icon: 'none' }); return }
      if (!/^https?:\/\//.test(v)) v = 'http://' + v
      v = v.replace(/\/+$/, '')
      const host = v.replace(/^https?:\/\//, '')
      if (!/^[\w.\-]+(:\d+)?$/.test(host)) {
        wx.showToast({ title: '格式不正确，示例：192.168.11.112:8086', icon: 'none' })
        return
      }
      wx.setStorageSync('baseUrl', v)
      app.globalData.baseUrl = v
      wx.showToast({ title: '已保存', icon: 'success' })
    }
  })
}

module.exports = { openNetSetting }
