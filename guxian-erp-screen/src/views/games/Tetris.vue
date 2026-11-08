<template>
  <div class="game-page">
    <el-card shadow="never" class="game-card">
      <div class="game-header">
        <h2>🧱 俄罗斯方块</h2>
        <div class="score-info">
          <el-tag size="large" type="success">得分：{{ score }}</el-tag>
          <el-tag size="large" type="primary">等级：{{ level }}</el-tag>
          <el-tag size="large" type="warning">最高分：{{ highScore }}</el-tag>
        </div>
      </div>
      <div class="game-container">
        <div class="board-wrap">
          <canvas ref="boardRef" width="300" height="600"></canvas>
          <div v-if="gameOver" class="game-over">
            <h3>游戏结束！</h3>
            <p>得分：{{ score }}</p>
            <el-button type="primary" @click="startGame">再来一局</el-button>
          </div>
          <div v-if="!started && !gameOver" class="game-start">
            <h3>准备开始</h3>
            <p>← → 移动<br>↑ 旋转<br>↓ 加速下落</p>
            <el-button type="primary" @click="startGame">开始游戏</el-button>
          </div>
        </div>
        <div class="side-panel">
          <h4>下一个</h4>
          <canvas ref="nextRef" width="120" height="120"></canvas>
          <div class="controls">
            <el-button @click="rotate" :disabled="!started || gameOver">旋转</el-button>
            <el-button @click="moveLeft" :disabled="!started || gameOver">← 左移</el-button>
            <el-button @click="moveRight" :disabled="!started || gameOver">右移 →</el-button>
            <el-button @click="softDrop" :disabled="!started || gameOver">↓ 下落</el-button>
            <el-button @click="hardDrop" :disabled="!started || gameOver">直接落底</el-button>
            <el-button @click="pauseGame" v-if="started && !gameOver">
              {{ paused ? '继续' : '暂停' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const boardRef = ref<HTMLCanvasElement>()
const nextRef = ref<HTMLCanvasElement>()
const score = ref(0)
const level = ref(1)
const highScore = ref(Number(localStorage.getItem('tetris_high') || 0))
const gameOver = ref(false)
const started = ref(false)
const paused = ref(false)

const COLS = 10
const ROWS = 20
const BLOCK = 30

let ctx: CanvasRenderingContext2D | null = null
let nextCtx: CanvasRenderingContext2D | null = null
let board: number[][] = []
let currentPiece: { shape: number[][]; x: number; y: number; color: string } | null = null
let nextPiece: { shape: number[][]; color: string } | null = null
let gameLoop: number | null = null

const SHAPES = [
  { shape: [[1,1,1,1]], color: '#00f5ff' }, // I
  { shape: [[1,1],[1,1]], color: '#ffff00' }, // O
  { shape: [[1,1,1],[0,1,0]], color: '#9d00ff' }, // T
  { shape: [[1,1,0],[0,1,1]], color: '#00ff00' }, // S
  { shape: [[0,1,1],[1,1,0]], color: '#ff0000' }, // Z
  { shape: [[1,0,0],[1,1,1]], color: '#0000ff' }, // J
  { shape: [[0,0,1],[1,1,1]], color: '#ff7f00' }, // L
]

onMounted(() => {
  ctx = boardRef.value?.getContext('2d') || null
  nextCtx = nextRef.value?.getContext('2d') || null
  initBoard()
  draw()
  drawNext()
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  if (gameLoop) clearInterval(gameLoop)
})

function initBoard() {
  board = Array(ROWS).fill(null).map(() => Array(COLS).fill(0))
}

function handleKeydown(e: KeyboardEvent) {
  if (!started.value || gameOver.value || paused.value) return
  if (e.key === 'ArrowLeft') moveLeft()
  else if (e.key === 'ArrowRight') moveRight()
  else if (e.key === 'ArrowUp') rotate()
  else if (e.key === 'ArrowDown') softDrop()
}

function randomPiece() {
  const s = SHAPES[Math.floor(Math.random() * SHAPES.length)]
  return { shape: s.shape.map(r => [...r]), color: s.color }
}

function startGame() {
  initBoard()
  score.value = 0
  level.value = 1
  gameOver.value = false
  started.value = true
  paused.value = false
  nextPiece = randomPiece()
  spawnPiece()
  if (gameLoop) clearInterval(gameLoop)
  gameLoop = window.setInterval(tick, 1000)
  draw()
  drawNext()
}

function pauseGame() {
  paused.value = !paused.value
  if (paused.value) {
    if (gameLoop) clearInterval(gameLoop)
  } else {
    gameLoop = window.setInterval(tick, Math.max(100, 1000 - (level.value - 1) * 100))
  }
}

function spawnPiece() {
  currentPiece = {
    shape: nextPiece!.shape,
    color: nextPiece!.color,
    x: Math.floor(COLS / 2) - 1,
    y: 0
  }
  nextPiece = randomPiece()
  drawNext()
  // 游戏结束
  if (checkCollision(currentPiece.shape, currentPiece.x, currentPiece.y)) {
    endGame()
  }
}

function checkCollision(shape: number[][], x: number, y: number): boolean {
  for (let r = 0; r < shape.length; r++) {
    for (let c = 0; c < shape[r].length; c++) {
      if (shape[r][c]) {
        const nx = x + c
        const ny = y + r
        if (nx < 0 || nx >= COLS || ny >= ROWS) return true
        if (ny >= 0 && board[ny][nx]) return true
      }
    }
  }
  return false
}

function moveLeft() {
  if (!currentPiece) return
  if (!checkCollision(currentPiece.shape, currentPiece.x - 1, currentPiece.y)) {
    currentPiece.x--
    draw()
  }
}

function moveRight() {
  if (!currentPiece) return
  if (!checkCollision(currentPiece.shape, currentPiece.x + 1, currentPiece.y)) {
    currentPiece.x++
    draw()
  }
}

function rotate() {
  if (!currentPiece) return
  const rotated = currentPiece.shape[0].map((_, i) =>
    currentPiece!.shape.map(row => row[i]).reverse()
  )
  if (!checkCollision(rotated, currentPiece.x, currentPiece.y)) {
    currentPiece.shape = rotated
    draw()
  }
}

function softDrop() {
  if (!currentPiece) return
  if (!checkCollision(currentPiece.shape, currentPiece.x, currentPiece.y + 1)) {
    currentPiece.y++
    draw()
  } else {
    lockPiece()
    clearLines()
    spawnPiece()
  }
}

function hardDrop() {
  if (!currentPiece) return
  while (!checkCollision(currentPiece.shape, currentPiece.x, currentPiece.y + 1)) {
    currentPiece.y++
  }
  lockPiece()
  clearLines()
  spawnPiece()
  draw()
}

function lockPiece() {
  if (!currentPiece) return
  for (let r = 0; r < currentPiece.shape.length; r++) {
    for (let c = 0; c < currentPiece.shape[r].length; c++) {
      if (currentPiece.shape[r][c]) {
        const ny = currentPiece.y + r
        const nx = currentPiece.x + c
        if (ny >= 0) {
          board[ny][nx] = currentPiece.color
        }
      }
    }
  }
}

function clearLines() {
  let lines = 0
  for (let r = ROWS - 1; r >= 0; r--) {
    if (board[r].every(cell => cell)) {
      board.splice(r, 1)
      board.unshift(Array(COLS).fill(0))
      r++
      lines++
    }
  }
  if (lines > 0) {
    score.value += lines * 100 * level.value
    level.value = Math.floor(score.value / 500) + 1
    if (score.value > highScore.value) {
      highScore.value = score.value
      localStorage.setItem('tetris_high', String(highScore.value))
    }
    if (gameLoop) clearInterval(gameLoop)
    gameLoop = window.setInterval(tick, Math.max(100, 1000 - (level.value - 1) * 100))
  }
}

function tick() {
  softDrop()
}

function endGame() {
  gameOver.value = true
  started.value = false
  if (gameLoop) clearInterval(gameLoop)
}

function draw() {
  if (!ctx) return
  ctx.fillStyle = '#1a1a2e'
  ctx.fillRect(0, 0, 300, 600)

  // 已固定的方块
  for (let r = 0; r < ROWS; r++) {
    for (let c = 0; c < COLS; c++) {
      if (board[r][c]) {
        ctx.fillStyle = board[r][c]
        ctx.fillRect(c * BLOCK + 1, r * BLOCK + 1, BLOCK - 2, BLOCK - 2)
      }
    }
  }

  // 当前方块
  if (currentPiece) {
    ctx.fillStyle = currentPiece.color
    for (let r = 0; r < currentPiece.shape.length; r++) {
      for (let c = 0; c < currentPiece.shape[r].length; c++) {
        if (currentPiece.shape[r][c]) {
          ctx.fillRect(
            (currentPiece.x + c) * BLOCK + 1,
            (currentPiece.y + r) * BLOCK + 1,
            BLOCK - 2,
            BLOCK - 2
          )
        }
      }
    }
  }
}

function drawNext() {
  if (!nextCtx || !nextPiece) return
  nextCtx.fillStyle = '#1a1a2e'
  nextCtx.fillRect(0, 0, 120, 120)
  nextCtx.fillStyle = nextPiece.color
  const offsetX = (120 - nextPiece.shape[0].length * 30) / 2
  const offsetY = (120 - nextPiece.shape.length * 30) / 2
  for (let r = 0; r < nextPiece.shape.length; r++) {
    for (let c = 0; c < nextPiece.shape[r].length; c++) {
      if (nextPiece.shape[r][c]) {
        nextCtx.fillRect(offsetX + c * 30 + 1, offsetY + r * 30 + 1, 28, 28)
      }
    }
  }
}
</script>

<style scoped>
.game-page {
  display: flex;
  justify-content: center;
  padding: 20px;
}
.game-card {
  width: 600px;
}
.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.game-header h2 {
  margin: 0;
}
.score-info {
  display: flex;
  gap: 10px;
}
.game-container {
  display: flex;
  gap: 20px;
  justify-content: center;
}
.board-wrap {
  position: relative;
}
canvas {
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
}
.side-panel {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.side-panel h4 {
  margin: 0;
  text-align: center;
}
.controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.game-over, .game-start {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255,255,255,0.95);
  padding: 30px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.3);
}
</style>
