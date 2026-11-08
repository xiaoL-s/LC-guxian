<template>
  <div class="game-page">
    <el-card shadow="never" class="game-card">
      <div class="game-header">
        <h2>⚫ 五子棋</h2>
        <div class="score-info">
          <el-tag size="large" :type="currentPlayer === 'black' ? 'primary' : 'info'">
            {{ currentPlayer === 'black' ? '⚫ 你的回合' : '⚪ AI 思考中...' }}
          </el-tag>
          <el-tag size="large" type="success">你赢：{{ wins }}</el-tag>
          <el-tag size="large" type="danger">AI赢：{{ losses }}</el-tag>
        </div>
      </div>
      <div class="game-container">
        <div class="board-wrap">
          <canvas ref="canvasRef" width="600" height="600" @click="handleClick"></canvas>
          <div v-if="gameOver" class="game-over">
            <h3>{{ winner === 'black' ? '🎉 你赢了！' : '😅 AI赢了！' }}</h3>
            <el-button type="primary" @click="resetGame">再来一局</el-button>
          </div>
        </div>
      </div>
      <div class="game-footer">
        <el-button type="primary" @click="resetGame">重新开始</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const canvasRef = ref<HTMLCanvasElement>()
const currentPlayer = ref<'black' | 'white'>('black')
const gameOver = ref(false)
const winner = ref<'black' | 'white' | null>(null)
const wins = ref(Number(localStorage.getItem('gomoku_wins') || 0))
const losses = ref(Number(localStorage.getItem('gomoku_losses') || 0))

const BOARD_SIZE = 15
const CELL_SIZE = 40
let ctx: CanvasRenderingContext2D | null = null
let board: ('black' | 'white' | null)[][] = []

onMounted(() => {
  ctx = canvasRef.value?.getContext('2d') || null
  resetGame()
})

function resetGame() {
  board = Array(BOARD_SIZE).fill(null).map(() => Array(BOARD_SIZE).fill(null))
  currentPlayer.value = 'black'
  gameOver.value = false
  winner.value = null
  drawBoard()
}

function drawBoard() {
  if (!ctx) return
  // 背景
  ctx.fillStyle = '#DEB887'
  ctx.fillRect(0, 0, 600, 600)

  // 网格线
  ctx.strokeStyle = '#8B4513'
  ctx.lineWidth = 1
  for (let i = 0; i < BOARD_SIZE; i++) {
    ctx.beginPath()
    ctx.moveTo(CELL_SIZE, CELL_SIZE + i * CELL_SIZE)
    ctx.lineTo(600 - CELL_SIZE, CELL_SIZE + i * CELL_SIZE)
    ctx.stroke()
    ctx.beginPath()
    ctx.moveTo(CELL_SIZE + i * CELL_SIZE, CELL_SIZE)
    ctx.lineTo(CELL_SIZE + i * CELL_SIZE, 600 - CELL_SIZE)
    ctx.stroke()
  }

  // 棋子
  for (let r = 0; r < BOARD_SIZE; r++) {
    for (let c = 0; c < BOARD_SIZE; c++) {
      if (board[r][c]) {
        ctx.beginPath()
        ctx.arc(
          CELL_SIZE + c * CELL_SIZE,
          CELL_SIZE + r * CELL_SIZE,
          CELL_SIZE / 2 - 2,
          0,
          Math.PI * 2
        )
        ctx.fillStyle = board[r][c] === 'black' ? '#000' : '#fff'
        ctx.fill()
        ctx.strokeStyle = '#333'
        ctx.stroke()
      }
    }
  }
}

function handleClick(e: MouseEvent) {
  if (gameOver.value || currentPlayer.value !== 'black') return
  const rect = canvasRef.value!.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const c = Math.round((x - CELL_SIZE) / CELL_SIZE)
  const r = Math.round((y - CELL_SIZE) / CELL_SIZE)

  if (r < 0 || r >= BOARD_SIZE || c < 0 || c >= BOARD_SIZE) return
  if (board[r][c]) return

  // 玩家落子
  board[r][c] = 'black'
  drawBoard()

  if (checkWin(r, c, 'black')) {
    gameOver.value = true
    winner.value = 'black'
    wins.value++
    localStorage.setItem('gomoku_wins', String(wins.value))
    return
  }

  // AI 落子
  currentPlayer.value = 'white'
  setTimeout(() => {
    const aiMove = findBestMove()
    board[aiMove.r][aiMove.c] = 'white'
    drawBoard()

    if (checkWin(aiMove.r, aiMove.c, 'white')) {
      gameOver.value = true
      winner.value = 'white'
      losses.value++
      localStorage.setItem('gomoku_losses', String(losses.value))
      return
    }

    currentPlayer.value = 'black'
  }, 300)
}

function checkWin(r: number, c: number, player: 'black' | 'white'): boolean {
  const directions = [[0, 1], [1, 0], [1, 1], [1, -1]]
  for (const [dr, dc] of directions) {
    let count = 1
    // 正方向
    for (let i = 1; i < 5; i++) {
      const nr = r + dr * i
      const nc = c + dc * i
      if (nr < 0 || nr >= BOARD_SIZE || nc < 0 || nc >= BOARD_SIZE) break
      if (board[nr][nc] !== player) break
      count++
    }
    // 反方向
    for (let i = 1; i < 5; i++) {
      const nr = r - dr * i
      const nc = c - dc * i
      if (nr < 0 || nr >= BOARD_SIZE || nc < 0 || nc >= BOARD_SIZE) break
      if (board[nr][nc] !== player) break
      count++
    }
    if (count >= 5) return true
  }
  return false
}

function findBestMove(): { r: number; c: number } {
  let bestScore = -Infinity
  let bestMove = { r: 7, c: 7 }

  // 如果棋盘是空的，下中间
  if (board.every(row => row.every(cell => !cell))) {
    return { r: 7, c: 7 }
  }

  for (let r = 0; r < BOARD_SIZE; r++) {
    for (let c = 0; c < BOARD_SIZE; c++) {
      if (board[r][c]) continue

      // 评估这个位置的分数
      const attackScore = evaluatePosition(r, c, 'white')
      const defendScore = evaluatePosition(r, c, 'black')
      const score = attackScore * 1.2 + defendScore

      if (score > bestScore) {
        bestScore = score
        bestMove = { r, c }
      }
    }
  }
  return bestMove
}

function evaluatePosition(r: number, c: number, player: 'black' | 'white'): number {
  let totalScore = 0
  const directions = [[0, 1], [1, 0], [1, 1], [1, -1]]

  for (const [dr, dc] of directions) {
    let count = 1
    let openEnds = 0

    // 正方向
    for (let i = 1; i < 5; i++) {
      const nr = r + dr * i
      const nc = c + dc * i
      if (nr < 0 || nr >= BOARD_SIZE || nc < 0 || nc >= BOARD_SIZE) break
      if (board[nr][nc] === player) count++
      else if (!board[nr][nc]) { openEnds++; break }
      else break
    }
    // 反方向
    for (let i = 1; i < 5; i++) {
      const nr = r - dr * i
      const nc = c - dc * i
      if (nr < 0 || nr >= BOARD_SIZE || nc < 0 || nc >= BOARD_SIZE) break
      if (board[nr][nc] === player) count++
      else if (!board[nr][nc]) { openEnds++; break }
      else break
    }

    // 评分
    if (count >= 5) totalScore += 100000
    else if (count === 4) {
      if (openEnds === 2) totalScore += 10000
      else if (openEnds === 1) totalScore += 1000
    } else if (count === 3) {
      if (openEnds === 2) totalScore += 1000
      else if (openEnds === 1) totalScore += 100
    } else if (count === 2) {
      if (openEnds === 2) totalScore += 100
      else if (openEnds === 1) totalScore += 10
    } else if (count === 1) {
      totalScore += 1
    }
  }
  return totalScore
}
</script>

<style scoped>
.game-page {
  display: flex;
  justify-content: center;
  padding: 20px;
}
.game-card {
  width: 700px;
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
  justify-content: center;
}
.board-wrap {
  position: relative;
}
canvas {
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
  cursor: pointer;
}
.game-over {
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
.game-footer {
  margin-top: 20px;
  text-align: center;
}
</style>
