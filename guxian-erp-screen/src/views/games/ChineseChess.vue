<template>
  <div class="game-page">
    <el-card shadow="never" class="game-card">
      <div class="game-header">
        <h2>♟️ 中国象棋</h2>
        <div class="score-info">
          <el-tag size="large" :type="turn === 'r' ? 'danger' : 'info'">
            {{ gameOver ? '对局结束' : (turn === 'r' ? '🔴 你的回合' : '⚫ AI 思考中...') }}
          </el-tag>
          <el-tag v-if="checkTip && !gameOver" size="large" type="warning">{{ checkTip }}</el-tag>
          <el-tag size="large" type="success">你赢：{{ wins }}</el-tag>
          <el-tag size="large" type="danger">AI 赢：{{ losses }}</el-tag>
        </div>
      </div>

      <div class="game-container">
        <div class="board-wrap">
          <canvas ref="canvasRef" :width="W" :height="H" @click="handleClick"></canvas>
          <div v-if="gameOver" class="game-over">
            <h3>{{ winner === 'r' ? '🎉 你赢了！' : '😅 AI 赢了！' }}</h3>
            <p class="over-sub">{{ overReason }}</p>
            <el-button type="primary" @click="resetGame">再来一局</el-button>
          </div>
        </div>

        <div class="side-panel">
          <div class="panel-block">
            <div class="panel-title">走子记录</div>
            <div class="move-list">
              <div v-for="(m, i) in moveLog" :key="i" class="move-row">
                <span class="seq">{{ i + 1 }}.</span>
                <span :class="m.side === 'r' ? 'txt-red' : 'txt-black'">{{ m.text }}</span>
              </div>
              <div v-if="!moveLog.length" class="empty">还没开始走棋</div>
            </div>
          </div>
          <div class="panel-block">
            <div class="panel-title">操作</div>
            <el-button style="width:100%;margin-bottom:10px" :disabled="!canUndo || gameOver" @click="undo">悔棋一步</el-button>
            <el-button type="primary" style="width:100%" @click="resetGame">重新开始</el-button>
          </div>
          <div class="panel-block tip">
            <div class="panel-title">玩法</div>
            <p>你执红先行，点击棋子选中，绿点是可以走的位置。</p>
            <p>吃掉对方将/帅，或把对方走到无棋可走即胜。</p>
          </div>
        </div>
      </div>

      <div class="game-footer">
        <el-button type="primary" @click="resetGame">重新开始</el-button>
        <span class="hint">提示：上班摸鱼记得随时 Alt+Tab 切回来哦~</span>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

/** 棋子类型：K 将/帅 G 士/仕 E 象/相 H 马 R 车 C 炮 P 兵/卒 */
type Kind = 'K' | 'G' | 'E' | 'H' | 'R' | 'C' | 'P'
type Side = 'r' | 'b'
interface Piece { k: Kind; s: Side }

/** 棋盘 10 行 9 列，idx = row*9 + col；红在下（row 7-9），黑在上（row 0-2） */
const ROWS = 10
const COLS = 9
const M = 46
const C = 60
const W = M * 2 + (COLS - 1) * C
const H = M * 2 + (ROWS - 1) * C

const canvasRef = ref<HTMLCanvasElement>()
const turn = ref<Side>('r')
const gameOver = ref(false)
const winner = ref<Side | null>(null)
const overReason = ref('')
const checkTip = ref('')
const wins = ref(Number(localStorage.getItem('chess_wins') || 0))
const losses = ref(Number(localStorage.getItem('chess_losses') || 0))
const moveLog = ref<{ side: Side; text: string }[]>([])
const canUndo = ref(false)

let ctx: CanvasRenderingContext2D | null = null
let board: (Piece | null)[] = []
let selected = -1
let targets: number[] = []
let lastMove: { from: number; to: number } | null = null
let history: { board: (Piece | null)[]; turn: Side; last: { from: number; to: number } | null; logLen: number }[] = []
let aiTimer: any = null

const RED_WORDS: Record<Kind, string> = { K: '帅', G: '仕', E: '相', H: '马', R: '车', C: '炮', P: '兵' }
const BLACK_WORDS: Record<Kind, string> = { K: '将', G: '士', E: '象', H: '马', R: '车', C: '炮', P: '卒' }

const px = (col: number) => M + col * C
const py = (row: number) => M + row * C
const rowOf = (i: number) => Math.floor(i / COLS)
const colOf = (i: number) => i % COLS
const idxOf = (r: number, c: number) => r * COLS + c
const inside = (r: number, c: number) => r >= 0 && r < ROWS && c >= 0 && c < COLS

function initBoard() {
  const b: (Piece | null)[] = Array(ROWS * COLS).fill(null)
  const back: Kind[] = ['R', 'H', 'E', 'G', 'K', 'G', 'E', 'H', 'R']
  for (let c = 0; c < COLS; c++) {
    b[idxOf(0, c)] = { k: back[c], s: 'b' }
    b[idxOf(9, c)] = { k: back[c], s: 'r' }
  }
  b[idxOf(2, 1)] = { k: 'C', s: 'b' }
  b[idxOf(2, 7)] = { k: 'C', s: 'b' }
  b[idxOf(7, 1)] = { k: 'C', s: 'r' }
  b[idxOf(7, 7)] = { k: 'C', s: 'r' }
  for (const c of [0, 2, 4, 6, 8]) {
    b[idxOf(3, c)] = { k: 'P', s: 'b' }
    b[idxOf(6, c)] = { k: 'P', s: 'r' }
  }
  return b
}

onMounted(() => {
  ctx = canvasRef.value?.getContext('2d') || null
  resetGame()
})

function resetGame() {
  if (aiTimer) clearTimeout(aiTimer)
  board = initBoard()
  turn.value = 'r'
  gameOver.value = false
  winner.value = null
  overReason.value = ''
  checkTip.value = ''
  moveLog.value = []
  selected = -1
  targets = []
  lastMove = null
  history = []
  canUndo.value = false
  draw()
}

/* ========== 走法生成 ========== */

function pieceMoves(b: (Piece | null)[], from: number): number[] {
  const p = b[from]
  if (!p) return []
  const r = rowOf(from)
  const c = colOf(from)
  const out: number[] = []
  const push = (rr: number, cc: number) => {
    if (!inside(rr, cc)) return
    const t = b[idxOf(rr, cc)]
    if (t && t.s === p.s) return
    out.push(idxOf(rr, cc))
  }

  if (p.k === 'R' || p.k === 'C') {
    const dirs = [[-1, 0], [1, 0], [0, -1], [0, 1]]
    for (const [dr, dc] of dirs) {
      let rr = r + dr
      let cc = c + dc
      let jumped = false
      while (inside(rr, cc)) {
        const t = b[idxOf(rr, cc)]
        if (p.k === 'R') {
          if (!t) out.push(idxOf(rr, cc))
          else {
            if (t.s !== p.s) out.push(idxOf(rr, cc))
            break
          }
        } else {
          // 炮：不吃子时不能跳，吃子必须隔一个
          if (!jumped) {
            if (!t) out.push(idxOf(rr, cc))
            else jumped = true
          } else {
            if (t) {
              if (t.s !== p.s) out.push(idxOf(rr, cc))
              break
            }
          }
        }
        rr += dr
        cc += dc
      }
    }
    return out
  }

  if (p.k === 'H') {
    const jumps = [[-2, -1], [-2, 1], [-1, -2], [-1, 2], [1, -2], [1, 2], [2, -1], [2, 1]]
    for (const [dr, dc] of jumps) {
      const tr = r + dr
      const tc = c + dc
      if (!inside(tr, tc)) continue
      // 蹩马腿
      const lr = Math.abs(dr) === 2 ? r + dr / 2 : r
      const lc = Math.abs(dc) === 2 ? c + dc / 2 : c
      if (b[idxOf(lr, lc)]) continue
      const t = b[idxOf(tr, tc)]
      if (t && t.s === p.s) continue
      out.push(idxOf(tr, tc))
    }
    return out
  }

  if (p.k === 'E') {
    const dirs = [[-2, -2], [-2, 2], [2, -2], [2, 2]]
    for (const [dr, dc] of dirs) {
      const tr = r + dr
      const tc = c + dc
      if (!inside(tr, tc)) continue
      // 不过河
      if (p.s === 'r' && tr < 5) continue
      if (p.s === 'b' && tr > 4) continue
      // 塞象眼
      if (b[idxOf(r + dr / 2, c + dc / 2)]) continue
      const t = b[idxOf(tr, tc)]
      if (t && t.s === p.s) continue
      out.push(idxOf(tr, tc))
    }
    return out
  }

  if (p.k === 'G') {
    for (const [dr, dc] of [[-1, 0], [1, 0], [0, -1], [0, 1]]) {
      const tr = r + dr
      const tc = c + dc
      if (!inPalace(p.s, tr, tc)) continue
      push(tr, tc)
    }
    return out
  }

  if (p.k === 'K') {
    for (const [dr, dc] of [[-1, 0], [1, 0], [0, -1], [0, 1]]) {
      const tr = r + dr
      const tc = c + dc
      if (!inPalace(p.s, tr, tc)) continue
      push(tr, tc)
    }
    return out
  }

  // 兵/卒
  const forward = p.s === 'r' ? -1 : 1
  push(r + forward, c)
  const crossed = p.s === 'r' ? r <= 4 : r >= 5
  if (crossed) {
    push(r, c - 1)
    push(r, c + 1)
  }
  return out
}

function inPalace(s: Side, r: number, c: number) {
  if (c < 3 || c > 5) return false
  return s === 'r' ? r >= 7 && r <= 9 : r >= 0 && r <= 2
}

/** 找将/帅位置 */
function kingPos(b: (Piece | null)[], s: Side): number {
  for (let i = 0; i < b.length; i++) {
    const p = b[i]
    if (p && p.k === 'K' && p.s === s) return i
  }
  return -1
}

/** 该方是否被将军（含将帅照面） */
function inCheck(b: (Piece | null)[], s: Side): boolean {
  const kp = kingPos(b, s)
  if (kp < 0) return true
  const opp: Side = s === 'r' ? 'b' : 'r'
  for (let i = 0; i < b.length; i++) {
    const p = b[i]
    if (!p || p.s !== opp) continue
    if (pieceMoves(b, i).includes(kp)) return true
  }
  // 将帅照面：同列且中间无子
  const kr = rowOf(kp)
  const kc = colOf(kp)
  const okp = kingPos(b, opp)
  if (okp >= 0 && colOf(okp) === kc) {
    let blocked = false
    const r1 = Math.min(kr, rowOf(okp))
    const r2 = Math.max(kr, rowOf(okp))
    for (let r = r1 + 1; r < r2; r++) {
      if (b[idxOf(r, kc)]) { blocked = true; break }
    }
    if (!blocked) return true
  }
  return false
}

/** 走后自己是否安全（模拟移动） */
function kingSafeAfter(b: (Piece | null)[], from: number, to: number, s: Side): boolean {
  const nb = b.slice()
  nb[to] = nb[from]
  nb[from] = null
  return !inCheck(nb, s)
}

function legalMoves(b: (Piece | null)[], from: number): number[] {
  const p = b[from]
  if (!p) return []
  return pieceMoves(b, from).filter((to) => kingSafeAfter(b, from, to, p.s))
}

function allLegal(s: Side, b: (Piece | null)[] = board): { from: number; to: number }[] {
  const res: { from: number; to: number }[] = []
  for (let i = 0; i < b.length; i++) {
    const p = b[i]
    if (!p || p.s !== s) continue
    for (const to of legalMoves(b, i)) res.push({ from: i, to })
  }
  return res
}

/* ========== 绘制 ========== */

function draw() {
  const g = ctx
  if (!g) return
  g.clearRect(0, 0, W, H)
  // 木色棋盘底
  const grad = g.createLinearGradient(0, 0, W, H)
  grad.addColorStop(0, '#F0D9AE')
  grad.addColorStop(1, '#E4C086')
  g.fillStyle = grad
  g.fillRect(0, 0, W, H)

  g.strokeStyle = '#8B5A2B'
  g.lineWidth = 1.4
  // 横线
  for (let r = 0; r < ROWS; r++) {
    g.beginPath()
    g.moveTo(px(0), py(r))
    g.lineTo(px(COLS - 1), py(r))
    g.stroke()
  }
  // 竖线：左右边线贯通，中间在河界断开
  for (let c = 0; c < COLS; c++) {
    g.beginPath()
    if (c === 0 || c === COLS - 1) {
      g.moveTo(px(c), py(0))
      g.lineTo(px(c), py(ROWS - 1))
      g.stroke()
    } else {
      g.moveTo(px(c), py(0))
      g.lineTo(px(c), py(4))
      g.stroke()
      g.beginPath()
      g.moveTo(px(c), py(5))
      g.lineTo(px(c), py(ROWS - 1))
      g.stroke()
    }
  }
  // 九宫斜线
  g.beginPath()
  g.moveTo(px(3), py(0)); g.lineTo(px(5), py(2))
  g.moveTo(px(5), py(0)); g.lineTo(px(3), py(2))
  g.moveTo(px(3), py(7)); g.lineTo(px(5), py(9))
  g.moveTo(px(5), py(7)); g.lineTo(px(3), py(9))
  g.stroke()
  // 外框
  g.lineWidth = 2.6
  g.strokeRect(px(0) - 7, py(0) - 7, (COLS - 1) * C + 14, (ROWS - 1) * C + 14)

  // 兵位/炮位角标
  g.lineWidth = 1.2
  const marks: number[][] = []
  for (const c of [0, 2, 4, 6, 8]) { marks.push([3, c]); marks.push([6, c]) }
  for (const c of [1, 7]) { marks.push([2, c]); marks.push([7, c]) }
  for (const [r, c] of marks) drawCorner(g, px(c), py(r), c === 0, c === COLS - 1)

  // 楚河汉界
  g.fillStyle = 'rgba(109,68,32,0.72)'
  g.font = '700 24px "KaiTi","STKaiti","PingFang SC",serif'
  g.textAlign = 'center'
  g.textBaseline = 'middle'
  const midY = (py(4) + py(5)) / 2
  g.fillText('楚  河', px(2), midY)
  g.fillText('汉  界', px(6), midY)

  // 上一步标记
  if (lastMove) {
    g.strokeStyle = 'rgba(56,142,60,0.85)'
    g.lineWidth = 2
    for (const i of [lastMove.from, lastMove.to]) ringSquare(g, px(colOf(i)), py(rowOf(i)))
  }

  // 棋子
  for (let i = 0; i < board.length; i++) {
    const p = board[i]
    if (!p) continue
    drawPiece(g, px(colOf(i)), py(rowOf(i)), p, i === selected)
  }

  // 可走点
  g.fillStyle = 'rgba(46,164,79,0.85)'
  for (const i of targets) {
    if (board[i]) {
      g.strokeStyle = 'rgba(46,164,79,0.9)'
      g.lineWidth = 3
      g.beginPath()
      g.arc(px(colOf(i)), py(rowOf(i)), 27, 0, Math.PI * 2)
      g.stroke()
    } else {
      g.beginPath()
      g.arc(px(colOf(i)), py(rowOf(i)), 7, 0, Math.PI * 2)
      g.fill()
    }
  }
  g.textAlign = 'left'
}

function drawCorner(g: CanvasRenderingContext2D, x: number, y: number, leftEdge: boolean, rightEdge: boolean) {
  const d = 7
  const len = 12
  const seg = (x1: number, y1: number, x2: number, y2: number, x3: number, y3: number) => {
    g.beginPath()
    g.moveTo(x1, y1)
    g.lineTo(x2, y2)
    g.lineTo(x3, y3)
    g.stroke()
  }
  // 左上
  if (!leftEdge) seg(x - d - len, y - d, x - d, y - d, x - d, y - d - len)
  // 右上
  if (!rightEdge) seg(x + d + len, y - d, x + d, y - d, x + d, y - d - len)
  // 左下
  if (!leftEdge) seg(x - d - len, y + d, x - d, y + d, x - d, y + d + len)
  // 右下
  if (!rightEdge) seg(x + d + len, y + d, x + d, y + d, x + d, y + d + len)
}

function ringSquare(g: CanvasRenderingContext2D, x: number, y: number) {
  const s = 22
  g.beginPath()
  g.moveTo(x - s, y - s + 8)
  g.lineTo(x - s, y - s)
  g.lineTo(x - s + 8, y - s)
  g.moveTo(x + s - 8, y - s)
  g.lineTo(x + s, y - s)
  g.lineTo(x + s, y - s + 8)
  g.moveTo(x + s, y + s - 8)
  g.lineTo(x + s, y + s)
  g.lineTo(x + s - 8, y + s)
  g.moveTo(x - s + 8, y + s)
  g.lineTo(x - s, y + s)
  g.lineTo(x - s, y + s - 8)
  g.stroke()
}

function drawPiece(g: CanvasRenderingContext2D, x: number, y: number, p: Piece, isSel: boolean) {
  const R = 25
  // 底座阴影
  g.beginPath()
  g.fillStyle = 'rgba(90,60,30,0.22)'
  g.arc(x + 1.5, y + 2.5, R, 0, Math.PI * 2)
  g.fill()
  // 棋子面
  g.beginPath()
  g.fillStyle = '#FDF6E3'
  g.arc(x, y, R, 0, Math.PI * 2)
  g.fill()
  // 外圈
  g.strokeStyle = isSel ? '#D4A017' : '#7A4A1E'
  g.lineWidth = isSel ? 3.5 : 2
  g.stroke()
  // 内圈
  g.beginPath()
  g.strokeStyle = p.s === 'r' ? 'rgba(192,57,43,0.55)' : 'rgba(40,50,60,0.55)'
  g.lineWidth = 1.2
  g.arc(x, y, R - 4, 0, Math.PI * 2)
  g.stroke()
  // 字
  const word = p.s === 'r' ? RED_WORDS[p.k] : BLACK_WORDS[p.k]
  g.fillStyle = p.s === 'r' ? '#C0392B' : '#232F3A'
  g.font = '700 24px "KaiTi","STKaiti","PingFang SC",serif'
  g.textAlign = 'center'
  g.textBaseline = 'middle'
  g.fillText(word, x, y + 1)
}

/* ========== 交互 ========== */

function handleClick(e: MouseEvent) {
  if (gameOver.value || turn.value !== 'r') return
  const cv = canvasRef.value
  if (!cv) return
  const rect = cv.getBoundingClientRect()
  const k = rect.width / W
  const x = (e.clientX - rect.left) / k
  const y = (e.clientY - rect.top) / k
  const col = Math.round((x - M) / C)
  const row = Math.round((y - M) / C)
  if (!inside(row, col)) return
  // 距离交叉点太远则忽略
  if (Math.hypot(x - px(col), y - py(row)) > C * 0.55) return
  const i = idxOf(row, col)
  const p = board[i]

  // 已选中且点在可走位置 → 走子
  if (selected >= 0 && targets.includes(i)) {
    playerMove(selected, i)
    return
  }
  // 点自己的棋子 → 选中
  if (p && p.s === 'r') {
    selected = i
    targets = legalMoves(board, i)
    draw()
    return
  }
  // 其他位置取消选中
  selected = -1
  targets = []
  draw()
}

function playerMove(from: number, to: number) {
  snapshot()
  doMove(from, to)
  selected = -1
  targets = []
  draw()
  const state = afterMove('r')
  if (state !== 'continue') return
  turn.value = 'b'
  if (aiTimer) clearTimeout(aiTimer)
  aiTimer = setTimeout(() => aiTurn(), 420)
}

function aiTurn() {
  if (gameOver.value) return
  const moves = allLegal('b')
  if (!moves.length) {
    finish('r', '黑方无棋可走')
    return
  }
  // 吃子优先排序，提升剪枝效率
  moves.sort((a, b) => (board[b.to] ? 1 : 0) - (board[a.to] ? 1 : 0))
  let best = moves[0]
  let bestScore = Infinity
  for (const m of moves) {
    doMoveSilent(m.from, m.to)
    const score = search(2, -Infinity, Infinity, true)
    undoSilent()
    if (score < bestScore) {
      bestScore = score
      best = m
    }
  }
  snapshot()
  doMove(best.from, best.to)
  draw()
  afterMove('b')
  if (gameOver.value) return
  turn.value = 'r'
}

/** 走完后处理将军/胜负，返回状态 */
function afterMove(side: Side): 'continue' | 'end' {
  const opp: Side = side === 'r' ? 'b' : 'r'
  if (inCheck(board, opp)) {
    checkTip.value = opp === 'r' ? '⚠️ 你被将军了！' : '💥 将军！'
  } else {
    checkTip.value = ''
  }
  if (!allLegal(opp).length) {
    finish(side, inCheck(board, opp) ? '绝杀取胜' : '对方无棋可走')
    return 'end'
  }
  return 'continue'
}

function finish(win: Side, reason: string) {
  gameOver.value = true
  winner.value = win
  overReason.value = reason
  if (win === 'r') {
    wins.value++
    localStorage.setItem('chess_wins', String(wins.value))
    ElMessage.success('恭喜赢下这盘！')
  } else {
    losses.value++
    localStorage.setItem('chess_losses', String(losses.value))
    ElMessage.warning('这盘输了，再来一把？')
  }
}

/* ========== 落子与记录 ========== */

function doMove(from: number, to: number) {
  const p = board[from]!
  const eaten = board[to]
  board[to] = p
  board[from] = null
  lastMove = { from, to }
  const name = p.s === 'r' ? RED_WORDS[p.k] : BLACK_WORDS[p.k]
  const fromTxt = `${'ABCDEFGHI'[colOf(from)]}${ROWS - rowOf(from)}`
  const toTxt = `${'ABCDEFGHI'[colOf(to)]}${ROWS - rowOf(to)}`
  moveLog.value.push({ side: p.s, text: `${name} ${fromTxt}-${toTxt}${eaten ? ' 吃' + (eaten.s === 'r' ? RED_WORDS[eaten.k] : BLACK_WORDS[eaten.k]) : ''}` })
}

function snapshot() {
  history.push({ board: board.slice(), turn: turn.value, last: lastMove, logLen: moveLog.value.length })
  canUndo.value = true
}

function takeBackSilent() {
  const h = history.pop()
  if (h) {
    board = h.board
    turn.value = h.turn
    lastMove = h.last
    moveLog.value = moveLog.value.slice(0, h.logLen)
  }
}


function undo() {
  if (gameOver.value) return
  if (aiTimer) clearTimeout(aiTimer)
  if (!history.length) return
  // 撤两步：先撤销 AI 的一步，再撤销自己的一步
  takeBackSilent()
  if (history.length) takeBackSilent()
  turn.value = 'r'
  gameOver.value = false
  winner.value = null
  checkTip.value = ''
  selected = -1
  targets = []
  canUndo.value = history.length > 0
  draw()
}

/* ========== AI 搜索 ========== */

const VALUES: Record<Kind, number> = { K: 60000, R: 900, C: 450, H: 400, E: 200, G: 200, P: 100 }

function evalBoard(): number {
  let score = 0
  for (let i = 0; i < board.length; i++) {
    const p = board[i]
    if (!p) continue
    let v = VALUES[p.k]
    const r = rowOf(i)
    const c = colOf(i)
    // 兵过河加分，靠近中路加一点
    if (p.k === 'P') {
      if (p.s === 'r' && r <= 4) v += 60
      if (p.s === 'b' && r >= 5) v += 60
    }
    if (p.k === 'H' || p.k === 'C') {
      v += (4 - Math.abs(c - 4)) * 6
    }
    // 车马炮活跃度：越靠前越好（红向上，黑向下）
    if (p.k === 'R' || p.k === 'C' || p.k === 'H') {
      v += p.s === 'r' ? (9 - r) * 3 : r * 3
    }
    score += p.s === 'r' ? v : -v
  }
  return score
}

function search(depth: number, alpha: number, beta: number, redTurn: boolean): number {
  if (depth === 0) return evalBoard()
  const side: Side = redTurn ? 'r' : 'b'
  const moves = allLegal(side)
  if (!moves.length) return redTurn ? -99999 : 99999

  if (redTurn) {
    let best = -Infinity
    for (const m of moves) {
      doMoveSilent(m.from, m.to)
      const s = search(depth - 1, alpha, beta, false)
      undoSilent()
      if (s > best) best = s
      if (best > alpha) alpha = best
      if (beta <= alpha) break
    }
    return best
  }
  let best = Infinity
  for (const m of moves) {
    doMoveSilent(m.from, m.to)
    const s = search(depth - 1, alpha, beta, true)
    undoSilent()
    if (s < best) best = s
    if (best < beta) beta = best
    if (beta <= alpha) break
  }
  return best
}

// 搜索用的无副作用落子（只动棋盘数组，不动记录）
let searchStack: { from: number; to: number; piece: Piece; eaten: Piece | null }[] = []

function doMoveSilent(from: number, to: number) {
  const piece = board[from]!
  const eaten = board[to] || null
  searchStack.push({ from, to, piece, eaten })
  board[to] = piece
  board[from] = null
}

function undoSilent() {
  const m = searchStack.pop()
  if (!m) return
  board[m.from] = m.piece
  board[m.to] = m.eaten
}
</script>

<style scoped>
.game-page {
  padding: 20px;
}
.game-card {
  max-width: 1080px;
  margin: 0 auto;
}
.game-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid #eef0f4;
}
.game-header h2 {
  margin: 0;
  color: #1f2a37;
  font-size: 20px;
}
.score-info {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.game-container {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: center;
}
.board-wrap {
  position: relative;
  flex: 1 1 420px;
  max-width: 572px;
  min-width: 0;
}
canvas {
  border-radius: 12px;
  box-shadow: 0 6px 22px rgba(90, 60, 30, 0.18);
  cursor: pointer;
  width: 100%;
  max-width: 572px;
  height: auto;
  display: block;
}
.game-over {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.82);
  border-radius: 12px;
}
.game-over h3 {
  margin: 0;
  font-size: 22px;
  color: #1f2a37;
}
.over-sub {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}
.side-panel {
  flex: 1 1 240px;
  min-width: 240px;
  max-width: 300px;
}
.panel-block {
  margin-bottom: 16px;
}
.panel-title {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}
.move-list {
  height: 300px;
  overflow-y: auto;
  background: #f8f9fb;
  border-radius: 10px;
  padding: 10px;
  font-size: 13px;
  line-height: 1.9;
}
.move-row {
  display: flex;
  gap: 8px;
}
.seq {
  color: #9aa4b2;
  width: 26px;
  flex: 0 0 auto;
}
.txt-red {
  color: #c0392b;
}
.txt-black {
  color: #232f3a;
}
.empty {
  color: #9aa4b2;
}
.panel-block.tip p {
  margin: 0 0 6px 0;
  font-size: 12.5px;
  color: #6b7280;
  line-height: 1.7;
}
.game-footer {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid #eef0f4;
}
.hint {
  color: #9aa4b2;
  font-size: 12.5px;
}
</style>
