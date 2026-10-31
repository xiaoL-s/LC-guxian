const { request } = require('../../utils/request')

Page({
  data: {
    user: { realName: '', postType: '' },
    items: [],
    loading: true,
    errMsg: ''
  },
  onShow() {
    this.load()
  },
  async load() {
    this.setData({ loading: true, errMsg: '' })
    try {
      const data = await request('/mini/work/income')
      // 预先格式化金额，避免 wxml 内方法调用不生效导致金额空白
      const items = ((data && data.items) || []).map(it => ({
        ...it,
        wageText: this.fmtMoney(it.pieceWage)
      }))
      this.setData({
        user: data || { realName: '', postType: '' },
        items
      })
    } catch (e) {
      this.setData({ errMsg: (e && e.message) || '加载失败，请检查网络或重新登录' })
    } finally {
      this.setData({ loading: false })
    }
  },
  fmtMoney(v) {
    if (v === null || v === undefined || v === '') return '0.00'
    return Number(v).toFixed(2)
  }
})
