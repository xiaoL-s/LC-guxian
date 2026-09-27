const { request } = require('../../utils/request')

Page({
  data: {
    workId: 0,
    detail: {},
    processes: [],
    processNames: [],
    procIndex: 0,
    qualifiedNum: 1,
    badNum: 0,
    loading: false
  },
  onLoad(opt) {
    if (opt.workId) {
      this.setData({ workId: Number(opt.workId) })
      this.loadDetail()
    }
    this.loadProcesses()
  },
  async scan() {
    wx.scanCode({
      onlyFromCamera: true,
      success: (res) => {
        // 二维码内容形如 workId:4 或直接是工单号
        const raw = res.result || ''
        const m = raw.match(/(\d+)/)
        if (m) {
          this.setData({ workId: Number(m[1]) })
          this.loadDetail()
        } else {
          wx.showToast({ title: '二维码无法识别', icon: 'none' })
        }
      }
    })
  },
  async loadProcesses() {
    try {
      const list = await request('/mini/work/processes')
      this.setData({
        processes: list || [],
        processNames: (list || []).map(p => ({ name: p.processName, code: p.processCode, id: p.processId }))
      })
    } catch (e) {}
  },
  async loadDetail() {
    try {
      const d = await request('/mini/work/detail/' + this.data.workId)
      this.setData({ detail: d || {} })
    } catch (e) {}
  },
  onProcChange(e) { this.setData({ procIndex: Number(e.detail.value) }) },
  onQ(e) { this.setData({ qualifiedNum: Number(e.detail.value) }) },
  onB(e) { this.setData({ badNum: Number(e.detail.value) }) },
  async submit() {
    const p = this.data.processNames[this.data.procIndex]
    if (!p) { wx.showToast({ title: '请选工序', icon: 'none' }); return }
    this.setData({ loading: true })
    try {
      await request('/mini/work/report', {
        method: 'POST',
        data: {
          workId: this.data.workId,
          processId: p.id,
          qualifiedNum: this.data.qualifiedNum,
          badNum: this.data.badNum
        }
      })
      wx.showToast({ title: '报工成功', icon: 'success' })
      this.loadDetail()
    } catch (e) {
    } finally { this.setData({ loading: false }) }
  }
})
