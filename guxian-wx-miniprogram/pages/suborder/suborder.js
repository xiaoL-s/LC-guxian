const { request } = require('../../utils/request')

Page({
  data: {
    // 工单信息（扫码后填充）
    work: null,
    // 订单产品明细行（一个工单多产品，分别报工分别计价）
    items: [],
    itemNames: [],
    itemIndex: 0,
    // 工序列表
    processes: [],
    // 当前产品可报工序（单价>0，0元=该产品无此工序）
    curProcesses: [],
    processNames: [],
    procIndex: 0,
    // 当前产品当前工序已报/剩余
    procStat: null,
    // 完成数量
    qualifiedNum: 1,
    // 已扫二维码内容
    qrRaw: '',
    submitting: false
  },
  onLoad() {
    this.loadProcesses()
  },
  onShow() {
    // 从其他页面/后台切回时：重新拉取工单最新状态（PC端撤回工序、报工进度变化后自动同步）
    if (this.data.qrRaw && this.data.work) {
      this.parseQr()
    }
  },
  async loadProcesses() {
    try {
      const list = await request('/mini/work/processes')
      const names = (list || []).map(p => p.processName)
      this.setData({ processes: list || [], processNames: names })
      // 已识别工单时，按当前产品刷新工序下拉状态
      if (this.data.work) this.applyItemState(this.data.work)
    } catch (e) {}
  },
  // 当前选中的产品行
  curItem() {
    return this.data.items[this.data.itemIndex] || null
  },
  // 根据当前产品刷新工序下拉：只显示该产品可报工序（单价>0），已完成置灰、默认选中第一个未完成
  applyItemState(work) {
    const item = this.curItem()
    const list = this.data.processes || []
    let avail
    if (item && item.availableProcesses && item.availableProcesses.length) {
      avail = item.availableProcesses
    } else if (!item) {
      avail = list
    } else {
      // 该产品无可报工序（全部0元/未配置）
      avail = []
    }
    if (!avail.length) {
      this.setData({ curProcesses: [], processNames: ['该产品无可报工序'], procIndex: 0 })
      this.updateProcStat()
      return
    }
    const done = (item && item.doneProcessIds) || []
    const doneSet = {}
    done.forEach(id => { doneSet[id] = true })
    const names = avail.map(p => doneSet[p.processId] ? (p.processName + ' ✓已完成') : p.processName)
    let idx = 0
    for (let i = 0; i < avail.length; i++) {
      if (!doneSet[avail[i].processId]) { idx = i; break }
    }
    this.setData({ curProcesses: avail, processNames: names, procIndex: idx })
    this.updateProcStat()
  },
  // 当前产品当前工序的已报/剩余数量
  updateProcStat() {
    const item = this.curItem()
    const p = this.data.curProcesses[this.data.procIndex]
    if (!item || !p) {
      this.setData({ procStat: null })
      return
    }
    const map = item.processReported || {}
    const reported = map[p.processId] || 0
    const total = item.num || 0
    this.setData({ procStat: { reported, remain: Math.max(0, total - reported) } })
  },
  // 切换产品：重置工序下拉
  onItemChange(e) {
    this.setData({ itemIndex: Number(e.detail.value), qualifiedNum: 1 })
    this.applyItemState(this.data.work)
  },
  // 切换工序
  onProcChange(e) {
    const idx = Number(e.detail.value)
    const p = this.data.curProcesses[idx]
    const item = this.curItem()
    const doneSet = {}
    ;((item && item.doneProcessIds) || []).forEach(id => { doneSet[id] = true })
    if (p && item && doneSet[p.processId]) {
      wx.showToast({ title: '该工序已完成，请选择其他工序', icon: 'none' })
      this.applyItemState(this.data.work)
      return
    }
    this.setData({ procIndex: idx })
    this.updateProcStat()
  },
  onNum(e) {
    if (e.type === 'tap') {
      const op = e.currentTarget.dataset.op
      let v = this.data.qualifiedNum
      if (op === 'plus') v += 1
      else v = Math.max(1, v - 1)
      const remain = (this.data.procStat && this.data.procStat.remain) || 0
      if (remain > 0) v = Math.min(v, remain)
      this.setData({ qualifiedNum: v })
      return
    }
    const v = Number(e.detail.value)
    this.setData({ qualifiedNum: isNaN(v) || v < 1 ? 1 : v })
  },
  // 点击"扫描工序" → 调起微信扫码
  scan() {
    wx.scanCode({
      onlyFromCamera: true,
      success: (res) => {
        this.setData({ qrRaw: res.result || '' })
        this.parseQr()
      },
      fail: () => {
        wx.showToast({ title: '已取消扫码', icon: 'none' })
      }
    })
  },
  // 扫码后解析并拉取工单信息
  async parseQr() {
    const raw = this.data.qrRaw
    if (!raw) return
    wx.showLoading({ title: '识别中...' })
    try {
      const work = await request('/mini/work/qrParse', {
        method: 'POST',
        data: { content: raw }
      })
      this.setData({ work: work || null })
      wx.hideLoading()
      if (work) {
        const items = (work.items || []).map(it => ({
          itemId: it.itemId,
          productName: it.productName || '',
          num: it.num || 0,
          doneProcessIds: Array.isArray(it.doneProcessIds) ? it.doneProcessIds : [],
          processReported: it.processReported || {},
          availableProcesses: Array.isArray(it.availableProcesses) ? it.availableProcesses : []
        }))
        this.setData({
          items,
          itemNames: items.map(it => it.productName + '（' + it.num + '件）'),
          itemIndex: 0,
          qualifiedNum: 1
        })
        this.applyItemState(work)
        wx.showToast({ title: '识别成功：' + work.workNo, icon: 'success' })
      }
    } catch (e) {
      wx.hideLoading()
      this.setData({ work: null, items: [], itemNames: [] })
    }
  },
  // 提交报工
  async submit() {
    if (!this.data.work) {
      wx.showToast({ title: '请先扫码识别工单', icon: 'none' })
      return
    }
    const item = this.curItem()
    if (!item) {
      wx.showToast({ title: '请选择报工产品', icon: 'none' })
      return
    }
    const p = this.data.curProcesses[this.data.procIndex]
    if (!p) {
      wx.showToast({ title: '该产品无可报工序', icon: 'none' })
      return
    }
    // 兜底：该产品该工序已完成禁止提交
    const doneSet = {}
    ;((item.doneProcessIds) || []).forEach(id => { doneSet[id] = true })
    if (doneSet[p.processId]) {
      wx.showToast({ title: '该产品该工序已完成，请选择其他工序', icon: 'none' })
      this.applyItemState(this.data.work)
      return
    }
    const q = this.data.qualifiedNum
    const remain = (this.data.procStat && this.data.procStat.remain) || 0
    if (q > remain) {
      wx.showToast({ title: '最多还可报 ' + remain + ' 件', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      await request('/mini/work/report', {
        method: 'POST',
        data: {
          workId: this.data.work.workId,
          itemId: item.itemId,
          processId: p.processId,
          qualifiedNum: q,
          badNum: 0
        }
      })
      wx.showToast({ title: '报工成功', icon: 'success' })
      // 刷新剩余数量与工序状态
      await this.parseQr()
    } catch (e) {
    } finally {
      this.setData({ submitting: false })
    }
  },
  // 清除已识别的工单
  clearWork() {
    this.setData({ work: null, qrRaw: '', items: [], itemNames: [], procStat: null, curProcesses: [], procIndex: 0 })
    this.applyItemState(null)
  }
})
